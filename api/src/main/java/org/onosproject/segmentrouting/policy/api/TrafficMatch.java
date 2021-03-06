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


import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.onosproject.net.flow.TrafficSelector;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Representation of a traffic match.
 */
public final class TrafficMatch {
    // Traffic match internal state
    private TrafficMatchId trafficMatchId;
    private TrafficSelector trafficSelector;
    private PolicyId policyId;
    private TrafficMatchPriority trafficMatchPriority;

    /**
     * Builds a traffic match.
     *
     * @param trafficSelector the traffic selector
     * @param policyId the associated policy id
     * @deprecated in version 3.0.2
     */
    public TrafficMatch(TrafficSelector trafficSelector, PolicyId policyId) {
        this.trafficSelector = trafficSelector;
        this.trafficMatchId = TrafficMatchId.trafficMatchId(computeTrafficMatchId());
        this.policyId = policyId;
        this.trafficMatchPriority = new TrafficMatchPriority(PolicyService.TRAFFIC_MATCH_PRIORITY);
    }

    /**
     * Builds a traffic match.
     *
     * @param trafficSelector the traffic selector
     * @param policyId the associated policy id
     * @param trafficMatchPriority the priority
     */
    public TrafficMatch(TrafficSelector trafficSelector, PolicyId policyId, TrafficMatchPriority trafficMatchPriority) {
        this.trafficSelector = trafficSelector;
        this.trafficMatchId = TrafficMatchId.trafficMatchId(computeTrafficMatchId());
        this.policyId = policyId;
        this.trafficMatchPriority = trafficMatchPriority;
    }

    /**
     * Returns the traffic match id.
     *
     * @return the id of the traffic match
     */
    public TrafficMatchId trafficMatchId() {
        return trafficMatchId;
    }

    /**
     * Returns the id of the policy associated with.
     *
     * @return the policy id
     */
    public PolicyId policyId() {
        return policyId;
    }

    /**
     * Returns the traffic selector associated with.
     *
     * @return the traffic selector
     */
    public TrafficSelector trafficSelector() {
        return trafficSelector;
    }

    /**
     * Returns the priority.
     *
     * @return the priority
     */
    public TrafficMatchPriority trafficMatchPriority() {
        return trafficMatchPriority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trafficMatchId, trafficSelector, policyId, trafficMatchPriority);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TrafficMatch) {
            final TrafficMatch other = (TrafficMatch) obj;
            return Objects.equals(this.trafficMatchId, other.trafficMatchId) &&
                    Objects.equals(trafficSelector, other.trafficSelector) &&
                    Objects.equals(policyId, other.policyId) &&
                    Objects.equals(trafficMatchPriority, other.trafficMatchPriority);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("trafficMatchId", trafficMatchId)
                .add("trafficSelector", trafficSelector)
                .add("policyId", policyId)
                .add("trafficMatchPriority", trafficMatchPriority)
                .toString();
    }

    // Compute the id using the traffic selector. This method results to be consistent across the cluster.
    private int computeTrafficMatchId() {
        Funnel<TrafficSelector> selectorFunnel = (from, into) -> from.criteria()
                .forEach(c -> into.putUnencodedChars(c.toString()));
        HashFunction hashFunction = Hashing.murmur3_32();
        HashCode hashCode = hashFunction.newHasher()
                .putObject(trafficSelector, selectorFunnel)
                .hash();
        return hashCode.asInt();
    }
}
