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

import org.onosproject.net.DeviceId;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Traffic match key used by the store.
 */
public class TrafficMatchKey {
    private DeviceId deviceId;
    private TrafficMatchId trafficMatchId;

    /**
     * Constructs new traffic match key with given device id and traffic match id.
     *
     * @param deviceId device id
     * @param trafficMatchId traffic match id
     */
    public TrafficMatchKey(DeviceId deviceId, TrafficMatchId trafficMatchId) {
        this.deviceId = deviceId;
        this.trafficMatchId = trafficMatchId;
    }

    /**
     * Gets device id.
     *
     * @return device id of the policy key
     */
    public DeviceId deviceId() {
        return deviceId;
    }

    /**
     * Gets traffic match id.
     *
     * @return the id of the traffic match
     */
    public TrafficMatchId trafficMatchId() {
        return trafficMatchId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrafficMatchKey)) {
            return false;
        }
        final TrafficMatchKey other = (TrafficMatchKey) obj;
        return Objects.equals(this.deviceId, other.deviceId) &&
                Objects.equals(this.trafficMatchId, other.trafficMatchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, trafficMatchId);
    }

    /**
     * Parses from a string the police key.
     *
     * @param str the string to parse
     * @return the policy key if present in the str, null otherwise
     */
    public static TrafficMatchKey fromString(String str) {
        TrafficMatchKey policyKey = null;
        if (str != null && str.contains(PolicyManager.KEY_SEPARATOR)) {
            StringTokenizer tokenizer = new StringTokenizer(str, PolicyManager.KEY_SEPARATOR);
            if (tokenizer.countTokens() == 2) {
                policyKey = new TrafficMatchKey(DeviceId.deviceId(tokenizer.nextToken()),
                        TrafficMatchId.of(tokenizer.nextToken()));
            }
        }
        return policyKey;
    }

    @Override
    public String toString() {
        return deviceId.toString() + PolicyManager.KEY_SEPARATOR + trafficMatchId.toString();
    }
}
