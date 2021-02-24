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
import org.onosproject.segmentrouting.policy.api.PolicyId;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Policy key used by the store.
 */
public class PolicyKey {
    private DeviceId deviceId;
    private PolicyId policyId;

    /**
     * Constructs new policy key with given device id and policy id.
     *
     * @param deviceId device id
     * @param policyId policy id
     */
    public PolicyKey(DeviceId deviceId, PolicyId policyId) {
        this.deviceId = deviceId;
        this.policyId = policyId;
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
     * Gets policy id.
     *
     * @return the id of the policy
     */
    public PolicyId policyId() {
        return policyId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PolicyKey)) {
            return false;
        }
        final PolicyKey other = (PolicyKey) obj;
        return Objects.equals(this.deviceId, other.deviceId) &&
                Objects.equals(this.policyId, other.policyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, policyId);
    }

    /**
     * Parses from a string the police key.
     *
     * @param str the string to parse
     * @return the policy key if present in the str, null otherwise
     */
    public static PolicyKey fromString(String str) {
        PolicyKey policyKey = null;
        if (str != null && str.contains(PolicyManager.KEY_SEPARATOR)) {
            StringTokenizer tokenizer = new StringTokenizer(str, PolicyManager.KEY_SEPARATOR);
            if (tokenizer.countTokens() == 2) {
                policyKey = new PolicyKey(DeviceId.deviceId(tokenizer.nextToken()),
                        PolicyId.of(tokenizer.nextToken()));
            }
        }
        return policyKey;
    }

    @Override
    public String toString() {
        return deviceId.toString() + PolicyManager.KEY_SEPARATOR + policyId.toString();
    }
}
