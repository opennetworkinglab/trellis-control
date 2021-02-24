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

import org.onosproject.segmentrouting.policy.api.Policy;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyState;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Policy request tracked by the system.
 */
final class PolicyRequest {
    // Stores need to track policy info and the state
    private final Policy policy;
    private PolicyState policyState;

    /**
     * Creates a policy request in pending add.
     *
     * @param pol the policy
     */
    public PolicyRequest(Policy pol) {
        policy = pol;
        policyState = PolicyState.PENDING_ADD;
    }

    /**
     * Returns the current state of the request.
     *
     * @return the policy state
     */
    public PolicyState policyState() {
        return policyState;
    }

    /**
     * Returns the policy id.
     *
     * @return the policy id
     */
    public PolicyId policyId() {
        return policy.policyId();
    }

    /**
     * Returns the policy type.
     *
     * @return the type of a policy
     */
    public Policy.PolicyType policyType() {
        return policy.policyType();
    }

    /**
     * To update the policy state.
     *
     * @param policystate the new state.
     */
    public void policyState(PolicyState policystate) {
        policyState = policystate;
    }

    /**
     * Returns the policy associated to this request.
     *
     * @return the policy
     */
    public Policy policy() {
        return policy;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PolicyRequest)) {
            return false;
        }
        final PolicyRequest other = (PolicyRequest) obj;
        return Objects.equals(this.policy, other.policy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policy);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("policyState", policyState)
                .add("policy", policy)
                .toString();
    }

}
