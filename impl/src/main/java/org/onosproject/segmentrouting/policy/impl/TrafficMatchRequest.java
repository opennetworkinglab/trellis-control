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

import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;
import org.onosproject.segmentrouting.policy.api.TrafficMatchPriority;
import org.onosproject.segmentrouting.policy.api.TrafficMatchState;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Representation of a traffic match.
 */
public final class TrafficMatchRequest {
    // Stores need to track the traffic match info and the state
    private final TrafficMatch trafficMatch;
    private TrafficMatchState trafficMatchState;

    /**
     * Builds a traffic match request in pending add.
     *
     * @param tMatch the traffic match
     */
    public TrafficMatchRequest(TrafficMatch tMatch) {
        trafficMatch = tMatch;
        trafficMatchState = TrafficMatchState.PENDING_ADD;
    }

    /**
     * To update the traffic match state.
     *
     * @param trafficMatchState the new state
     */
    public void trafficMatchState(TrafficMatchState trafficMatchState) {
        this.trafficMatchState = trafficMatchState;
    }

    /**
     * Returns the traffic match state.
     *
     * @return the state of the traffic match
     */
    public TrafficMatchState trafficMatchState() {
        return trafficMatchState;
    }

    /**
     * Returns the traffic match id.
     *
     * @return the id of the traffic match
     */
    public TrafficMatchId trafficMatchId() {
        return trafficMatch.trafficMatchId();
    }

    /**
     * Returns the id of the policy associated with.
     *
     * @return the policy id
     */
    public PolicyId policyId() {
        return trafficMatch.policyId();
    }

    /**
     * Returns the traffic selector associated with.
     *
     * @return the traffic selector
     */
    public TrafficSelector trafficSelector() {
        return trafficMatch.trafficSelector();
    }

    /**
     * Returns the traffic match associated with.
     *
     * @return the traffic match
     */
    public TrafficMatch trafficMatch() {
        return trafficMatch;
    }

    /**
     * Returns the priority.
     *
     * @return the priority
     */
    public TrafficMatchPriority priority() {
        return trafficMatch.trafficMatchPriority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(trafficMatch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TrafficMatchRequest) {
            final TrafficMatchRequest other = (TrafficMatchRequest) obj;
            return Objects.equals(trafficMatch, other.trafficMatch);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("trafficMatch", trafficMatch)
                .add("trafficMatchState", trafficMatchState)
                .toString();
    }
}
