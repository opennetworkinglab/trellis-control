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

import java.util.List;

/**
 * Traffic match data retrieved from the system.
 */
public final class TrafficMatchData {
    // We want to provide access to the traffic match data as well as
    // the traffic match operations in the system and their status
    private final TrafficMatchState trafficMatchState;
    private final TrafficMatch trafficMatch;
    private final List<String> operations;


    public TrafficMatchData(TrafficMatchState tState, TrafficMatch tMatch, List<String> ops) {
        trafficMatch = tMatch;
        trafficMatchState = tState;
        operations = ops;
    }

    /**
     * Returns the current state of the traffic match.
     *
     * @return the traffic match state
     */
    public TrafficMatchState trafficMatchState() {
        return trafficMatchState;
    }


    /**
     * Returns the traffic match associated.
     *
     * @return the traffic match
     */
    public TrafficMatch trafficMatch() {
        return trafficMatch;
    }

    /**
     * Returns the operations in the system in form of strings.
     *
     * @return the operations
     */
    public List<String> operations() {
        return operations;
    }
}
