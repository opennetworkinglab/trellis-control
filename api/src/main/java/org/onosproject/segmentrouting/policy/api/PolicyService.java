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
package org.onosproject.segmentrouting.policy.api;

import java.util.Set;

/**
 * Policies to drop, reroute, apply QoS and overlay the traffic.
 */
public interface PolicyService {
    /**
     * Traffic match priority.
     */
    int TRAFFIC_MATCH_PRIORITY = 60000;

    /**
     * Creates or updates a policy.
     *
     * @param policy the policy to create
     * @return the id of the policy being created. Otherwise null.
     */
    PolicyId addOrUpdatePolicy(Policy policy);

    /**
     * Issues a policy removal.
     *
     * @param policyId the id of the policy to remove
     * @return whether or not the operation was successful
     */
    boolean removePolicy(PolicyId policyId);

    /**
     * Returns a set of policies. The policy types can be used
     * as filter.
     *
     * @param filter the policy types
     * @return the policies stored in the system observing
     * the filtering rule
     */
    Set<PolicyData> policies(Set<Policy.PolicyType> filter);

    /**
     * Attaches a traffic match to a policy.
     *
     * @param trafficMatch the traffic match
     * @return the traffic match id or null if not successful
     */
    TrafficMatchId addOrUpdateTrafficMatch(TrafficMatch trafficMatch);

    /**
     * Issues a traffic match removal.
     *
     * @param trafficMatchId the id of the traffic match to remove
     * @return whether or not the operation was successful
     */
    boolean removeTrafficMatch(TrafficMatchId trafficMatchId);

    /**
     * Returns a set of traffic matches.
     *
     * @return the traffic matches stored in the system
     */
    Set<TrafficMatchData> trafficMatches();
}
