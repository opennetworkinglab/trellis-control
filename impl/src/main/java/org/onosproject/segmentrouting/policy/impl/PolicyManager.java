/*
 * Copyright 2021-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.segmentrouting.policy.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.glassfish.jersey.internal.guava.Sets;
import org.onlab.packet.MacAddress;
import org.onlab.util.KryoNamespace;
import org.onlab.util.PredictableExecutor;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.NodeId;
import org.onosproject.codec.CodecService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.DefaultNextObjective;
import org.onosproject.net.flowobjective.DefaultObjectiveContext;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.flowobjective.ObjectiveContext;
import org.onosproject.net.intent.WorkPartitionService;
import org.onosproject.net.link.LinkService;
import org.onosproject.segmentrouting.SegmentRoutingService;
import org.onosproject.segmentrouting.policy.api.DropPolicy;
import org.onosproject.segmentrouting.policy.api.Policy;
import org.onosproject.segmentrouting.policy.api.PolicyData;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.PolicyState;
import org.onosproject.segmentrouting.policy.api.RedirectPolicy;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;
import org.onosproject.segmentrouting.policy.api.TrafficMatchData;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;
import org.onosproject.segmentrouting.policy.api.TrafficMatchPriority;
import org.onosproject.segmentrouting.policy.api.TrafficMatchState;
import org.onosproject.segmentrouting.config.DeviceConfigNotFoundException;
import org.onosproject.segmentrouting.policy.api.Policy.PolicyType;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.MapEvent;
import org.onosproject.store.service.MapEventListener;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageException;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the policy service interface.
 */
@Component(immediate = true, service = PolicyService.class)
public class PolicyManager implements PolicyService {

    // App related things
    private static final String APP_NAME = "org.onosproject.segmentrouting.policy";
    private ApplicationId appId;
    private Logger log = getLogger(getClass());
    static final String KEY_SEPARATOR = "|";

    // Supported policies
    private static final Set<PolicyType> SUPPORTED_POLICIES = ImmutableSet.of(
            PolicyType.DROP, PolicyType.REDIRECT);

    // Driver should use this meta to match ig_port_type field in the ACL table
    private static final long EDGE_PORT = 1;

    // Policy/TrafficMatch store related objects. We use these consistent maps to keep track of the
    // lifecycle of a policy/traffic match. These are decomposed in multiple operations which have
    // to be performed on multiple devices in order to have a policy/traffic match in ADDED state.
    // TODO Consider to add store and delegate
    private static final String POLICY_STORE = "sr-policy-store";
    private ConsistentMap<PolicyId, PolicyRequest> policies;
    private MapEventListener<PolicyId, PolicyRequest> mapPolListener = new InternalPolMapEventListener();
    private Map<PolicyId, PolicyRequest> policiesMap;

    private static final String OPS_STORE = "sr-ops-store";
    private ConsistentMap<String, Operation> operations;
    private MapEventListener<String, Operation> mapOpsListener = new InternalOpsMapEventListener();
    private Map<String, Operation> opsMap;

    private static final String TRAFFIC_MATCH_STORE = "sr-tmatch-store";
    private ConsistentMap<TrafficMatchId, TrafficMatchRequest> trafficMatches;
    private MapEventListener<TrafficMatchId, TrafficMatchRequest> mapTMatchListener =
            new InternalTMatchMapEventListener();
    private Map<TrafficMatchId, TrafficMatchRequest> trafficMatchesMap;

    // Leadership related objects - consistent hashing
    private static final HashFunction HASH_FN = Hashing.md5();
    // Read only cache of the Policy leader
    private Map<PolicyId, NodeId> policyLeaderCache;

    // Worker threads for policy and traffic match related ops
    private static final int DEFAULT_THREADS = 4;
    protected PredictableExecutor workers;

    // Serializers and ONOS services
    private static final KryoNamespace.Builder APP_KRYO_BUILDER = KryoNamespace.newBuilder()
            .register(KryoNamespaces.API)
            .register(PolicyId.class)
            .register(PolicyType.class)
            .register(DropPolicy.class)
            .register(RedirectPolicy.class)
            .register(PolicyState.class)
            .register(PolicyRequest.class)
            .register(TrafficMatchId.class)
            .register(TrafficMatchState.class)
            .register(TrafficMatchPriority.class)
            .register(TrafficMatch.class)
            .register(TrafficMatchRequest.class)
            .register(Operation.class);
    private Serializer serializer = Serializer.using(Lists.newArrayList(APP_KRYO_BUILDER.build()));

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CodecService codecService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private WorkPartitionService workPartitionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private SegmentRoutingService srService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private LinkService linkService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication(APP_NAME);

        codecService.registerCodec(DropPolicy.class, new DropPolicyCodec());
        codecService.registerCodec(RedirectPolicy.class, new RedirectPolicyCodec());
        codecService.registerCodec(TrafficMatch.class, new TrafficMatchCodec());

        policies = storageService.<PolicyId, PolicyRequest>consistentMapBuilder()
                .withName(POLICY_STORE)
                .withSerializer(serializer).build();
        policies.addListener(mapPolListener);
        policiesMap = policies.asJavaMap();

        trafficMatches = storageService.<TrafficMatchId, TrafficMatchRequest>consistentMapBuilder()
                .withName(TRAFFIC_MATCH_STORE)
                .withSerializer(serializer).build();
        trafficMatches.addListener(mapTMatchListener);
        trafficMatchesMap = trafficMatches.asJavaMap();

        operations = storageService.<String, Operation>consistentMapBuilder()
                .withName(OPS_STORE)
                .withSerializer(serializer).build();
        operations.addListener(mapOpsListener);
        opsMap = operations.asJavaMap();

        policyLeaderCache = Maps.newConcurrentMap();

        workers = new PredictableExecutor(DEFAULT_THREADS,
                groupedThreads("sr-policy", "worker-%d", log));

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        // Teardown everything
        codecService.unregisterCodec(DropPolicy.class);
        codecService.unregisterCodec(RedirectPolicy.class);
        codecService.unregisterCodec(TrafficMatch.class);
        policies.removeListener(mapPolListener);
        policies.destroy();
        policiesMap.clear();
        trafficMatches.removeListener(mapTMatchListener);
        trafficMatches.destroy();
        trafficMatchesMap.clear();
        operations.removeListener(mapOpsListener);
        operations.destroy();
        operations.clear();
        workers.shutdown();

        log.info("Stopped");
    }

    @Override
    //FIXME update does not work well
    public PolicyId addOrUpdatePolicy(Policy policy) {
        PolicyId policyId = policy.policyId();
        try {
            policies.put(policyId, new PolicyRequest(policy));
        } catch (StorageException e) {
            log.error("{} thrown a storage exception: {}", e.getStackTrace()[0].getMethodName(),
                    e.getMessage(), e);
            policyId = null;
        }
        return policyId;
    }

    @Override
    public boolean removePolicy(PolicyId policyId) {
        boolean result;
        if (dependingTrafficMatches(policyId).isPresent()) {
            if (log.isDebugEnabled()) {
                log.debug("Found depending traffic matches");
            }
            return false;
        }
        try {
            result = Versioned.valueOrNull(policies.computeIfPresent(policyId, (k, v) -> {
                if (v.policyState() != PolicyState.PENDING_REMOVE) {
                    v.policyState(PolicyState.PENDING_REMOVE);
                }
                return v;
            })) != null;
        } catch (StorageException e) {
            log.error("{} thrown a storage exception: {}", e.getStackTrace()[0].getMethodName(),
                    e.getMessage(), e);
            result = false;
        }
        return result;
    }

    @Override
    public Set<PolicyData> policies(Set<PolicyType> filter) {
        Set<PolicyData> policyData = Sets.newHashSet();
        List<DeviceId> edgeDeviceIds = srService.getEdgeDeviceIds();
        Set<PolicyRequest> policyRequests;
        if (filter.isEmpty()) {
            policyRequests = ImmutableSet.copyOf(policiesMap.values());
        } else {
            policyRequests = policiesMap.values().stream()
                    .filter(policyRequest -> filter.contains(policyRequest.policyType()))
                    .collect(Collectors.toSet());
        }
        PolicyKey policyKey;
        List<String> ops;
        for (PolicyRequest policyRequest : policyRequests) {
            ops = Lists.newArrayList();
            for (DeviceId deviceId : edgeDeviceIds) {
                policyKey = new PolicyKey(deviceId, policyRequest.policyId());
                Operation operation = Versioned.valueOrNull(operations.get(policyKey.toString()));
                if (operation != null) {
                    ops.add(deviceId + " -> " + operation.toStringMinimal());
                }
            }
            policyData.add(new PolicyData(policyRequest.policyState(), policyRequest.policy(), ops));
        }
        return policyData;
    }

    @Override
    //FIXME update does not work well
    public TrafficMatchId addOrUpdateTrafficMatch(TrafficMatch trafficMatch) {
        TrafficMatchId trafficMatchId = trafficMatch.trafficMatchId();
        try {
            trafficMatches.put(trafficMatchId, new TrafficMatchRequest(trafficMatch));
        } catch (StorageException e) {
            log.error("{} thrown a storage exception: {}", e.getStackTrace()[0].getMethodName(),
                    e.getMessage(), e);
            trafficMatchId = null;
        }
        return trafficMatchId;
    }

    @Override
    public boolean removeTrafficMatch(TrafficMatchId trafficMatchId) {
        boolean result;
        try {
            result = Versioned.valueOrNull(trafficMatches.computeIfPresent(trafficMatchId, (k, v) -> {
                if (v.trafficMatchState() != TrafficMatchState.PENDING_REMOVE) {
                    v.trafficMatchState(TrafficMatchState.PENDING_REMOVE);
                }
                return v;
            })) != null;
        } catch (StorageException e) {
            log.error("{} thrown a storage exception: {}", e.getStackTrace()[0].getMethodName(),
                    e.getMessage(), e);
            result = false;
        }
        return result;
    }

    @Override
    public Set<TrafficMatchData> trafficMatches() {
        Set<TrafficMatchData> trafficMatchData = Sets.newHashSet();
        List<DeviceId> edgeDeviceIds = srService.getEdgeDeviceIds();
        Set<TrafficMatchRequest> trafficMatchRequests = ImmutableSet.copyOf(trafficMatchesMap.values());
        TrafficMatchKey trafficMatchKey;
        List<String> ops;
        for (TrafficMatchRequest trafficMatchRequest : trafficMatchRequests) {
            ops = Lists.newArrayList();
            for (DeviceId deviceId : edgeDeviceIds) {
                trafficMatchKey = new TrafficMatchKey(deviceId, trafficMatchRequest.trafficMatch().trafficMatchId());
                Operation operation = Versioned.valueOrNull(operations.get(trafficMatchKey.toString()));
                if (operation != null) {
                    ops.add(deviceId + " -> " + operation.toStringMinimal());
                }
            }
            trafficMatchData.add(new TrafficMatchData(trafficMatchRequest.trafficMatchState(),
                    trafficMatchRequest.trafficMatch(), ops));
        }
        return trafficMatchData;
    }

    // Install/remove the policies on the edge devices
    private void sendPolicy(Policy policy, boolean install) {
        if (!isLeader(policy.policyId())) {
            if (log.isDebugEnabled()) {
                log.debug("Instance is not leader for policy {}", policy.policyId());
            }
            return;
        }
        // We know that we are the leader, offloads to the workers the remaining
        // part: issue fobj installation/removal and update the maps
        List<DeviceId> edgeDeviceIds = srService.getEdgeDeviceIds();
        for (DeviceId deviceId : edgeDeviceIds) {
            workers.execute(() -> {
                if (install) {
                    installPolicyInDevice(deviceId, policy);
                } else {
                    removePolicyInDevice(deviceId, policy);
                }
            }, deviceId.hashCode());
        }
    }

    // Orchestrate policy installation according to the type
    private void installPolicyInDevice(DeviceId deviceId, Policy policy) {
        if (!SUPPORTED_POLICIES.contains(policy.policyType())) {
            log.warn("Policy {} type {} not yet supported",
                    policy.policyId(), policy.policyType());
            return;
        }
        PolicyKey policyKey;
        Operation.Builder operation;
        if (log.isDebugEnabled()) {
            log.debug("Installing {} policy {} for dev: {}",
                    policy.policyType(), policy.policyId(), deviceId);
        }
        policyKey = new PolicyKey(deviceId, policy.policyId());
        operation = Operation.builder()
                .isInstall(true)
                .policy(policy);
        // TODO To better handle different policy types consider the abstraction of a compiler (subtypes ?)
        if (policy.policyType() == PolicyType.DROP) {
            // DROP policies do not need the next objective installation phase
            // we can update directly the map and signal the ops as done
            operation.isDone(true);
            operations.put(policyKey.toString(), operation.build());
        } else if (policy.policyType() == PolicyType.REDIRECT) {
            // REDIRECT Uses next objective context to update the ops as done when
            // it returns successfully. In the other cases leaves the ops as undone
            // and the relative policy will remain in pending.
            operations.put(policyKey.toString(), operation.build());
            NextObjective.Builder builder = redirectPolicyNextObjective(deviceId, (RedirectPolicy) policy);
            // Handle error here - leave the operation as undone and pending
            if (builder != null) {
                CompletableFuture<Objective> future = new CompletableFuture<>();
                if (log.isDebugEnabled()) {
                    log.debug("Installing REDIRECT next objective for dev: {}", deviceId);
                }
                ObjectiveContext context = new DefaultObjectiveContext(
                        (objective) -> {
                            if (log.isDebugEnabled()) {
                                log.debug("REDIRECT next objective for policy {} installed in dev: {}",
                                        policy.policyId(), deviceId);
                            }
                            future.complete(objective);
                        },
                        (objective, error) -> {
                            log.warn("Failed to install REDIRECT next objective for policy {}: {} in dev: {}",
                                    policy.policyId(), error, deviceId);
                            future.complete(null);
                        });
                // Context is not serializable
                NextObjective serializableObjective = builder.add();
                flowObjectiveService.next(deviceId, builder.add(context));
                future.whenComplete((objective, ex) -> {
                    if (ex != null) {
                        log.error("Exception installing REDIRECT next objective", ex);
                    } else if (objective != null) {
                        operations.computeIfPresent(policyKey.toString(), (k, v) -> {
                            if (!v.isDone() && v.isInstall()) {
                                v.isDone(true);
                                v.objectiveOperation(serializableObjective);
                            }
                            return v;
                        });
                    }
                });
            }
        }
    }

    // Remove policy in a device according to the type
    private void removePolicyInDevice(DeviceId deviceId, Policy policy) {
        PolicyKey policyKey = new PolicyKey(deviceId, policy.policyId());
        Operation operation = Versioned.valueOrNull(operations.get(policyKey.toString()));
        // Policy might be still in pending or not present anymore
        if (operation == null || operation.objectiveOperation() == null) {
            log.warn("There are no ops associated with {}", policyKey);
            operation = Operation.builder()
                    .isDone(true)
                    .isInstall(false)
                    .policy(policy)
                    .build();
            operations.put(policyKey.toString(), operation);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Removing {} policy {} in device {}", policy.policyType(), policy.policyId(), deviceId);
            }
            Operation.Builder operationBuilder = Operation.builder()
                    .isInstall(false)
                    .policy(policy);
            if (policy.policyType() == PolicyType.DROP) {
                operationBuilder.isDone(true);
                operations.put(policyKey.toString(), operationBuilder.build());
            } else if (policy.policyType() == PolicyType.REDIRECT) {
                // REDIRECT has to remove the next objective first
                NextObjective oldObj = (NextObjective) operation.objectiveOperation();
                operations.put(policyKey.toString(), operationBuilder.build());
                NextObjective.Builder builder = oldObj.copy();
                CompletableFuture<Objective> future = new CompletableFuture<>();
                if (log.isDebugEnabled()) {
                    log.debug("Removing REDIRECT next objective for dev: {}", deviceId);
                }
                ObjectiveContext context = new DefaultObjectiveContext(
                        (objective) -> {
                            if (log.isDebugEnabled()) {
                                log.debug("REDIRECT next objective for policy {} removed in dev: {}",
                                        policy.policyId(), deviceId);
                            }
                            future.complete(objective);
                        },
                        (objective, error) -> {
                            log.warn("Failed to remove REDIRECT next objective for policy {}: {} in dev: {}",
                                    policy.policyId(), error, deviceId);
                            future.complete(null);
                        });
                NextObjective serializableObjective = builder.remove();
                flowObjectiveService.next(deviceId, builder.remove(context));
                future.whenComplete((objective, ex) -> {
                    if (ex != null) {
                        log.error("Exception Removing REDIRECT next objective", ex);
                    } else if (objective != null) {
                        operations.computeIfPresent(policyKey.toString(), (k, v) -> {
                            if (!v.isDone() && !v.isInstall())  {
                                v.isDone(true);
                                v.objectiveOperation(serializableObjective);
                            }
                            return v;
                        });
                    }
                });
            }
        }
    }

    // Updates policy status if all the pending ops are done
    private void updatePolicy(PolicyId policyId, boolean install) {
        if (!isLeader(policyId)) {
            if (log.isDebugEnabled()) {
                log.debug("Instance is not leader for policy {}", policyId);
            }
            return;
        }
        workers.execute(() -> updatePolicyInternal(policyId, install), policyId.hashCode());
    }

    private void updatePolicyInternal(PolicyId policyId, boolean install) {
        // If there are no more pending ops we are ready to go; potentially we can check
        // if the id is contained. Updates policies only if they are still present
        Optional<Map.Entry<String, Versioned<Operation>>> notYetDone = operations.entrySet().stream()
                .filter(entry -> entry.getValue().value().policy().isPresent())
                .filter(entry -> PolicyKey.fromString(entry.getKey()).policyId().equals(policyId))
                .filter(entry -> !entry.getValue().value().isDone() && entry.getValue().value().isInstall() == install)
                .findFirst();
        if (notYetDone.isEmpty()) {
            PolicyRequest policyRequest = Versioned.valueOrNull(policies.computeIfPresent(policyId, (k, v) -> {
               if (v.policyState() == PolicyState.PENDING_ADD && install)  {
                   if (log.isDebugEnabled()) {
                       log.debug("Policy {} is ready", policyId);
                   }
                   v.policyState(PolicyState.ADDED);
               } else if (v.policyState() == PolicyState.PENDING_REMOVE && !install) {
                   if (log.isDebugEnabled()) {
                       log.debug("Policy {} is removed", policyId);
                   }
                   v = null;
               }
               return v;
            }));
            // Greedy check for pending traffic matches
            if (policyRequest != null && policyRequest.policyState() == PolicyState.ADDED) {
                updatePendingTrafficMatches(policyRequest.policyId());
            }
        }
    }

    // Install/remove the traffic match on the edge devices
    private void sendTrafficMatch(TrafficMatch trafficMatch, boolean install) {
        if (!isLeader(trafficMatch.policyId())) {
            if (log.isDebugEnabled()) {
                log.debug("Instance is not leader for policy {}", trafficMatch.policyId());
            }
            return;
        }
        // We know that we are the leader, offloads to the workers the remaining
        // part: issue fobj installation/removal and update the maps
        List<DeviceId> edgeDeviceIds = srService.getEdgeDeviceIds();
        for (DeviceId deviceId : edgeDeviceIds) {
            workers.execute(() -> {
                if (install) {
                    installTrafficMatchToDevice(deviceId, trafficMatch);
                } else {
                    removeTrafficMatchInDevice(deviceId, trafficMatch);
                }
            }, deviceId.hashCode());
        }
    }

    // Orchestrate traffic match installation according to the type
    private void installTrafficMatchToDevice(DeviceId deviceId, TrafficMatch trafficMatch) {
        if (log.isDebugEnabled()) {
            log.debug("Installing traffic match {} associated to policy {}",
                    trafficMatch.trafficMatchId(), trafficMatch.policyId());
        }
        TrafficMatchKey trafficMatchKey = new TrafficMatchKey(deviceId, trafficMatch.trafficMatchId());
        Operation trafficOperation = Versioned.valueOrNull(operations.get(trafficMatchKey.toString()));
        if (trafficOperation != null && trafficOperation.isInstall()) {
            if (log.isDebugEnabled()) {
                log.debug("There is already an install operation for traffic match {} associated to policy {} " +
                        "for device {}", trafficMatch.trafficMatchId(), trafficMatch.policyId(), deviceId);
            }
            return;
        }
        // For the DROP policy we need to set an ACL drop in the fwd objective. The other
        // policies require to retrieve the next Id and sets the next step.
        PolicyKey policyKey = new PolicyKey(deviceId, trafficMatch.policyId());
        Operation policyOperation = Versioned.valueOrNull(operations.get(policyKey.toString()));
        if (policyOperation == null || !policyOperation.isDone() ||
                !policyOperation.isInstall() || policyOperation.policy().isEmpty() ||
                (policyOperation.policy().get().policyType() == PolicyType.REDIRECT &&
                        policyOperation.objectiveOperation() == null)) {
            log.info("Deferring traffic match {} installation on device {}. Policy {} not yet installed",
                    trafficMatch.trafficMatchId(), deviceId, trafficMatch.policyId());
            return;
        }
        // Updates the store and then send the versatile fwd objective to the pipeliner
        trafficOperation = Operation.builder()
                .isInstall(true)
                .trafficMatch(trafficMatch)
                .build();
        operations.put(trafficMatchKey.toString(), trafficOperation);
        Policy policy = policyOperation.policy().get();
        ForwardingObjective.Builder builder = trafficMatchFwdObjective(trafficMatch, policy.policyType());
        if (policy.policyType() == PolicyType.DROP) {
            // Firstly builds the fwd objective with the wipeDeferred action.
            TrafficTreatment dropTreatment = DefaultTrafficTreatment.builder()
                    .wipeDeferred()
                    .build();
            builder.withTreatment(dropTreatment);
        } else if (policy.policyType() == PolicyType.REDIRECT) {

            // Here we need to set only the next step
            builder.nextStep(policyOperation.objectiveOperation().id());
        }
        // Once, the fwd objective has completed its execution, we update the policiesOps map
        CompletableFuture<Objective> future = new CompletableFuture<>();
        if (log.isDebugEnabled()) {
            log.debug("Installing forwarding objective for dev: {}", deviceId);
        }
        ObjectiveContext context = new DefaultObjectiveContext(
                (objective) -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Forwarding objective for policy {} installed", trafficMatch.policyId());
                    }
                    future.complete(objective);
                },
                (objective, error) -> {
                    log.warn("Failed to install forwarding objective for policy {}: {}",
                            trafficMatch.policyId(), error);
                    future.complete(null);
                });
        // Context is not serializable
        ForwardingObjective serializableObjective = builder.add();
        flowObjectiveService.forward(deviceId, builder.add(context));
        future.whenComplete((objective, ex) -> {
            if (ex != null) {
                log.error("Exception installing forwarding objective", ex);
            } else if (objective != null) {
                operations.computeIfPresent(trafficMatchKey.toString(), (k, v) -> {
                    if (!v.isDone() && v.isInstall())  {
                        v.isDone(true);
                        v.objectiveOperation(serializableObjective);
                    }
                    return v;
                });
            }
        });
    }

    // Updates traffic match status if all the pending ops are done
    private void updateTrafficMatch(TrafficMatch trafficMatch, boolean install) {
        if (!isLeader(trafficMatch.policyId())) {
            if (log.isDebugEnabled()) {
                log.debug("Instance is not leader for policy {}", trafficMatch.policyId());
            }
            return;
        }
        workers.execute(() -> updateTrafficMatchInternal(trafficMatch.trafficMatchId(), install),
                trafficMatch.policyId().hashCode());
    }

    private void updateTrafficMatchInternal(TrafficMatchId trafficMatchId, boolean install) {
        // If there are no more pending ops we are ready to go; potentially we can check
        // if the id is contained. Updates traffic matches only if they are still present
        Optional<Map.Entry<String, Versioned<Operation>>> notYetDone = operations.entrySet().stream()
                .filter(entry -> entry.getValue().value().trafficMatch().isPresent())
                .filter(entry -> TrafficMatchKey.fromString(entry.getKey()).trafficMatchId().equals(trafficMatchId))
                .filter(entry -> !entry.getValue().value().isDone() && entry.getValue().value().isInstall() == install)
                .findFirst();
        if (notYetDone.isEmpty()) {
            trafficMatches.computeIfPresent(trafficMatchId, (k, v) -> {
                if (v.trafficMatchState() == TrafficMatchState.PENDING_ADD && install)  {
                    if (log.isDebugEnabled()) {
                        log.debug("Traffic match {} is ready", trafficMatchId);
                    }
                    v.trafficMatchState(TrafficMatchState.ADDED);
                } else if (v.trafficMatchState() == TrafficMatchState.PENDING_REMOVE && !install) {
                    if (log.isDebugEnabled()) {
                        log.debug("Traffic match {} is removed", trafficMatchId);
                    }
                    v = null;
                }
                return v;
            });
        }
    }

    // Look for any pending traffic match waiting for the policy
    private void updatePendingTrafficMatches(PolicyId policyId) {
        Set<TrafficMatchRequest> pendingTrafficMatches = trafficMatches.stream()
                .filter(trafficMatchEntry -> trafficMatchEntry.getValue().value().policyId().equals(policyId) &&
                        trafficMatchEntry.getValue().value().trafficMatchState() == TrafficMatchState.PENDING_ADD)
                .map(trafficMatchEntry -> trafficMatchEntry.getValue().value())
                .collect(Collectors.toSet());
        for (TrafficMatchRequest trafficMatch : pendingTrafficMatches) {
            sendTrafficMatch(trafficMatch.trafficMatch(), true);
        }
    }

    // Traffic match removal in a device
    private void removeTrafficMatchInDevice(DeviceId deviceId, TrafficMatch trafficMatch) {
        if (log.isDebugEnabled()) {
            log.debug("Removing traffic match {} associated to policy {}",
                    trafficMatch.trafficMatchId(), trafficMatch.policyId());
        }
        TrafficMatchKey trafficMatchKey = new TrafficMatchKey(deviceId, trafficMatch.trafficMatchId());
        Operation operation = Versioned.valueOrNull(operations.get(trafficMatchKey.toString()));
        if (operation == null || operation.objectiveOperation() == null) {
            log.warn("There are no ops associated with {}", trafficMatchKey);
            operation = Operation.builder()
                    .isDone(true)
                    .isInstall(false)
                    .trafficMatch(trafficMatch)
                    .build();
            operations.put(trafficMatchKey.toString(), operation);
        } else if (!operation.isInstall()) {
            if (log.isDebugEnabled()) {
                log.debug("There is already an uninstall operation for traffic match {} associated to policy {}" +
                        " for device {}", trafficMatch.trafficMatchId(), trafficMatch.policyId(), deviceId);
            }
        } else {
            ForwardingObjective oldObj = (ForwardingObjective) operation.objectiveOperation();
            operation = Operation.builder(operation)
                    .isInstall(false)
                    .build();
            operations.put(trafficMatchKey.toString(), operation);
            ForwardingObjective.Builder builder = DefaultForwardingObjective.builder(oldObj);
            CompletableFuture<Objective> future = new CompletableFuture<>();
            if (log.isDebugEnabled()) {
                log.debug("Removing forwarding objectives for dev: {}", deviceId);
            }
            ObjectiveContext context = new DefaultObjectiveContext(
                    (objective) -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Forwarding objective for policy {} removed", trafficMatch.policyId());
                        }
                        future.complete(objective);
                    },
                    (objective, error) -> {
                        log.warn("Failed to remove forwarding objective for policy {}: {}",
                                trafficMatch.policyId(), error);
                        future.complete(null);
                    });
            ForwardingObjective serializableObjective = builder.remove();
            flowObjectiveService.forward(deviceId, builder.remove(context));
            future.whenComplete((objective, ex) -> {
                if (ex != null) {
                    log.error("Exception removing forwarding objective", ex);
                } else if (objective != null) {
                    operations.computeIfPresent(trafficMatchKey.toString(), (k, v) -> {
                        if (!v.isDone() && !v.isInstall())  {
                            v.isDone(true);
                            v.objectiveOperation(serializableObjective);
                        }
                        return v;
                    });
                }
            });
        }
    }

    // It is used when a policy has been removed but there are still traffic matches depending on it
    private Optional<TrafficMatchRequest> dependingTrafficMatches(PolicyId policyId) {
        return trafficMatches.stream()
                .filter(trafficMatchEntry -> trafficMatchEntry.getValue().value().policyId().equals(policyId) &&
                        trafficMatchEntry.getValue().value().trafficMatchState() == TrafficMatchState.ADDED)
                .map(trafficMatchEntry -> trafficMatchEntry.getValue().value())
                .findFirst();
    }

    // Utility that removes operations related to a policy or to a traffic match.
    private void removeOperations(PolicyId policyId, Optional<TrafficMatchId> trafficMatchId) {
        if (!isLeader(policyId)) {
            if (log.isDebugEnabled()) {
                log.debug("Instance is not leader for policy {}", policyId);
            }
            return;
        }
        List<DeviceId> edgeDeviceIds = srService.getEdgeDeviceIds();
        for (DeviceId deviceId : edgeDeviceIds) {
            workers.execute(() -> {
                String key;
                if (trafficMatchId.isPresent()) {
                    key = new TrafficMatchKey(deviceId, trafficMatchId.get()).toString();
                } else {
                    key = new PolicyKey(deviceId, policyId).toString();
                }
                operations.remove(key);
            }, deviceId.hashCode());
        }
    }

    private ForwardingObjective.Builder trafficMatchFwdObjective(TrafficMatch trafficMatch, PolicyType policyType) {
        TrafficSelector.Builder metaBuilder = DefaultTrafficSelector.builder(trafficMatch.trafficSelector());
        if (policyType == PolicyType.REDIRECT) {
            metaBuilder.matchMetadata(EDGE_PORT);
        }
        return DefaultForwardingObjective.builder()
                .withPriority(trafficMatch.trafficMatchPriority().priority())
                .withSelector(trafficMatch.trafficSelector())
                .withMeta(metaBuilder.build())
                .fromApp(appId)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .makePermanent();
    }

    private NextObjective.Builder redirectPolicyNextObjective(DeviceId srcDevice, RedirectPolicy redirectPolicy) {
        Set<Link> egressLinks = linkService.getDeviceEgressLinks(srcDevice);
        Map<ConnectPoint, DeviceId> egressPortsToEnforce = Maps.newHashMap();
        List<DeviceId> edgeDevices = srService.getEdgeDeviceIds();
        egressLinks.stream()
                .filter(link -> redirectPolicy.spinesToEnforce().contains(link.dst().deviceId()) &&
                                !edgeDevices.contains(link.dst().deviceId()))
                .forEach(link -> egressPortsToEnforce.put(link.src(), link.dst().deviceId()));
        // No ports no friend
        if (egressPortsToEnforce.isEmpty()) {
            log.warn("There are no port available for the REDIRECT policy {}", redirectPolicy.policyId());
            return null;
        }
        // We need to add a treatment for each valid egress port. The treatment
        // requires to set src and dst mac address and set the egress port. We are
        // deliberately not providing the metadata to prevent the programming of
        // some tables which are already controlled by SegmentRouting or are unnecessary
        int nextId = flowObjectiveService.allocateNextId();
        DefaultNextObjective.Builder builder = DefaultNextObjective.builder()
                .withId(nextId)
                .withType(NextObjective.Type.HASHED)
                .fromApp(appId);
        MacAddress srcDeviceMac;
        try {
            srcDeviceMac = srService.getDeviceMacAddress(srcDevice);
        } catch (DeviceConfigNotFoundException e) {
            log.warn(e.getMessage() + " Aborting installation REDIRECT policy {}", redirectPolicy.policyId());
            return null;
        }
        MacAddress neigborDeviceMac;
        TrafficTreatment.Builder tBuilder;
        for (Map.Entry<ConnectPoint, DeviceId> entry : egressPortsToEnforce.entrySet()) {
            try {
                neigborDeviceMac = srService.getDeviceMacAddress(entry.getValue());
            } catch (DeviceConfigNotFoundException e) {
                log.warn(e.getMessage() + " Aborting installation REDIRECT policy {}", redirectPolicy.policyId());
                return null;
            }
            tBuilder = DefaultTrafficTreatment.builder()
                    .setEthSrc(srcDeviceMac)
                    .setEthDst(neigborDeviceMac)
                    .setOutput(entry.getKey().port());
            builder.addTreatment(tBuilder.build());
        }
        return builder;
    }

    // Each map has an event listener enabling the events distribution across the cluster
    private class InternalPolMapEventListener implements MapEventListener<PolicyId, PolicyRequest> {
        @Override
        public void event(MapEvent<PolicyId, PolicyRequest> event) {
            Versioned<PolicyRequest> value = event.type() == MapEvent.Type.REMOVE ?
                    event.oldValue() : event.newValue();
            PolicyRequest policyRequest = value.value();
            Policy policy = policyRequest.policy();
            switch (event.type()) {
                case INSERT:
                case UPDATE:
                    switch (policyRequest.policyState()) {
                        case PENDING_ADD:
                            sendPolicy(policy, true);
                            break;
                        case PENDING_REMOVE:
                            sendPolicy(policy, false);
                            break;
                        case ADDED:
                            break;
                        default:
                            log.warn("Unknown policy state type {}", policyRequest.policyState());
                    }
                    break;
                case REMOVE:
                    removeOperations(policy.policyId(), Optional.empty());
                    break;
                default:
                    log.warn("Unknown event type {}", event.type());

            }
        }
    }

    private class InternalTMatchMapEventListener implements MapEventListener<TrafficMatchId, TrafficMatchRequest> {
        @Override
        public void event(MapEvent<TrafficMatchId, TrafficMatchRequest> event) {
            Versioned<TrafficMatchRequest> value = event.type() == MapEvent.Type.REMOVE ?
                    event.oldValue() : event.newValue();
            TrafficMatchRequest trafficMatchRequest = value.value();
            TrafficMatch trafficMatch = trafficMatchRequest.trafficMatch();
            switch (event.type()) {
                case INSERT:
                case UPDATE:
                    switch (trafficMatchRequest.trafficMatchState()) {
                        case PENDING_ADD:
                            sendTrafficMatch(trafficMatch, true);
                            break;
                        case PENDING_REMOVE:
                            sendTrafficMatch(trafficMatch, false);
                            break;
                        case ADDED:
                            break;
                        default:
                            log.warn("Unknown traffic match state type {}", trafficMatchRequest.trafficMatchState());
                    }
                    break;
                case REMOVE:
                    removeOperations(trafficMatch.policyId(), Optional.of(trafficMatch.trafficMatchId()));
                    break;
                default:
                    log.warn("Unknown event type {}", event.type());
            }
        }
    }

    private class InternalOpsMapEventListener implements MapEventListener<String, Operation> {
        @Override
        public void event(MapEvent<String, Operation> event) {
            String key = event.key();
            Versioned<Operation> value = event.type() == MapEvent.Type.REMOVE ?
                    event.oldValue() : event.newValue();
            Operation operation = value.value();
            switch (event.type()) {
                case INSERT:
                case UPDATE:
                    if (operation.isDone()) {
                        if (operation.policy().isPresent()) {
                            PolicyKey policyKey = PolicyKey.fromString(key);
                            updatePolicy(policyKey.policyId(), operation.isInstall());
                        } else if (operation.trafficMatch().isPresent()) {
                            updateTrafficMatch(operation.trafficMatch().get(), operation.isInstall());
                        } else {
                            log.warn("Unknown pending operation");
                        }
                    }
                    break;
                case REMOVE:
                    break;
                default:
                    log.warn("Unknown event type {}", event.type());
            }
        }
    }

    // Using the work partition service defines who is in charge of a given policy.
    private boolean isLeader(PolicyId policyId) {
        final NodeId currentNodeId = clusterService.getLocalNode().id();
        final NodeId leader = workPartitionService.getLeader(policyId, this::hasher);
        if (leader == null) {
            log.error("Fail to elect a leader for {}.", policyId);
            return false;
        }
        policyLeaderCache.put(policyId, leader);
        return currentNodeId.equals(leader);
    }

    private Long hasher(PolicyId policyId) {
        return HASH_FN.newHasher()
                .putUnencodedChars(policyId.toString())
                .hash()
                .asLong();
    }

    // TODO Periodic checker, consider to add store and delegates.

    // Check periodically for any issue and try to resolve automatically if possible
    private final class PolicyChecker implements Runnable {
        @Override
        public void run() {
        }
    }
}
