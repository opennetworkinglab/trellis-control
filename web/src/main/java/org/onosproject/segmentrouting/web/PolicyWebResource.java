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
package org.onosproject.segmentrouting.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.segmentrouting.policy.api.DropPolicy;
import org.onosproject.segmentrouting.policy.api.Policy;
import org.onosproject.segmentrouting.policy.api.Policy.PolicyType;
import org.onosproject.segmentrouting.policy.api.PolicyData;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.RedirectPolicy;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;
import org.onosproject.segmentrouting.policy.api.TrafficMatchData;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Query, create and remove Policies and Traffic Matches.
 */
@Path("policy")
public class PolicyWebResource extends AbstractWebResource {
    private static Logger log = LoggerFactory.getLogger(PolicyWebResource.class);

    private static final String EMPTY_TRAFFIC_SELECTOR =
            "Empty traffic selector is not allowed";
    private static final String POLICY = "policy";
    private static final String POLICY_ID = "policy_id";
    private static final String TRAFFIC_MATCH = "trafficMatch";
    private static final String TRAFFIC_MATCH_ID = "traffic_match_id";

    /**
     * Get all Policies.
     *
     * @return 200 OK will a collection of Policies
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPolicies() {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();
        ArrayNode policiesArr = root.putArray(POLICY);

        //Create a filter set contains all PolicyType
        Set<PolicyType> policyTypes = Set.of(PolicyType.values());

        for (PolicyData policyData : policyService.policies(policyTypes)) {
            Policy policy = policyData.policy();
            switch (policy.policyType()) {
                case DROP:
                    policiesArr.add(codec(DropPolicy.class).encode((DropPolicy) policy, this));
                    break;
                case REDIRECT:
                    policiesArr.add(codec(RedirectPolicy.class).encode((RedirectPolicy) policy, this));
                    break;
                default:
                    continue;
            }
        }

        return Response.ok(root).build();
    }

    /**
     * Get all Drop Policies.
     *
     * @return 200 OK will a collection of Dop Policies
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop")
    public Response getDropPolicies() {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();
        ArrayNode policiesArr = root.putArray(POLICY);

        Set<PolicyType> policyTypes = Set.of(PolicyType.DROP);

        for (PolicyData policyData : policyService.policies(policyTypes)) {
            Policy policy = policyData.policy();
            policiesArr.add(codec(DropPolicy.class).encode((DropPolicy) policy, this));
        }

        return Response.ok(root).build();
    }

    /**
     * Create a new Drop Policy.
     *
     * @return 200 OK and policyId
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop")
    public Response createDropPolicy() {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();

        DropPolicy dropPolicy = new DropPolicy();
        policyService.addOrUpdatePolicy(dropPolicy);

        root.put(POLICY_ID, dropPolicy.policyId().toString());

        return Response.ok(root).build();
    }

    /**
     * Get all Redirect Policies.
     *
     * @return 200 OK will a collection of Redirect Policies
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("redirect")
    public Response getRedirectPolicies() {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();
        ArrayNode policiesArr = root.putArray(POLICY);

        Set<PolicyType> policyTypes = Set.of(PolicyType.REDIRECT);

        for (PolicyData policyData : policyService.policies(policyTypes)) {
            Policy policy = policyData.policy();
            policiesArr.add(codec(RedirectPolicy.class).encode((RedirectPolicy) policy, this));
        }

        return Response.ok(root).build();
    }

    /**
     * Create a new Redirect Policy.
     *
     * @param input Json for the Redirect Policy
     * @return 200 OK and policyId
     * @onos.rsModel RedirectPolicyCreate
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("redirect")
    public Response createRedirectPolicy(InputStream input) {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();

        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), input);
            RedirectPolicy redirectPolicy = codec(RedirectPolicy.class).
                    decode(jsonTree, this);
            policyService.addOrUpdatePolicy(redirectPolicy);
            root.put(POLICY_ID, redirectPolicy.policyId().toString());
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }

        return Response.ok(root).build();
    }

    /**
     * Delete a Policy by policyId.
     *
     * @param policyId Policy identifier
     * @return 204 NO CONTENT
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{policyId}")
    public Response deletePolicy(@PathParam("policyId") String policyId) {
        PolicyService policyService = get(PolicyService.class);

        policyService.removePolicy(PolicyId.of(policyId));

        return Response.noContent().build();
    }

    /**
     * Get all Traffic Matches.
     *
     * @return 200 OK will a collection of Traffic Matches
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("trafficmatch")
    public Response getTrafficMatches() {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();
        ArrayNode trafficMatchArr = root.putArray(TRAFFIC_MATCH);

        for (TrafficMatchData trafficMatchData : policyService.trafficMatches()) {
            TrafficMatch trafficMatch = trafficMatchData.trafficMatch();
            trafficMatchArr.add(codec(TrafficMatch.class).encode(trafficMatch, this));
        }

        return Response.ok(root).build();
    }

    /**
     * Create a new Traffic Match.
     *
     * @param input Json for the Traffic Match
     * @return status of the request - CREATED and TrafficMatchId if the JSON is correct,
     * BAD_REQUEST if the JSON is invalid
     * @onos.rsModel TrafficMatchCreate
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("trafficmatch")
    public Response createTrafficMatch(InputStream input) {
        PolicyService policyService = get(PolicyService.class);
        ObjectNode root = mapper().createObjectNode();

        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), input);
            TrafficMatch trafficMatch = codec(TrafficMatch.class).
                    decode(jsonTree, this);
            if (trafficMatch.trafficSelector()
                    .equals(DefaultTrafficSelector.emptySelector())) {
                throw new IllegalArgumentException(EMPTY_TRAFFIC_SELECTOR);
            }
            policyService.addOrUpdateTrafficMatch(trafficMatch);
            root.put(TRAFFIC_MATCH_ID, trafficMatch.trafficMatchId().toString());
        } catch (IOException | IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex);
        }

        return Response.ok(root).build();
    }

    /**
     * Delete a Traffic Match by trafficMatchId.
     *
     * @param trafficMatchId Traffic Match identifier
     * @return 204 NO CONTENT
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("trafficmatch/{trafficMatchId}")
    public Response deleteTrafficMatch(@PathParam("trafficMatchId") String trafficMatchId) {
        PolicyService policyService = get(PolicyService.class);

        policyService.removeTrafficMatch(TrafficMatchId.of(trafficMatchId));

        return Response.noContent().build();
    }
}
