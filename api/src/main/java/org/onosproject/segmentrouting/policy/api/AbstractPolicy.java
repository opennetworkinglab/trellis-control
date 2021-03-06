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

/**
 * Abstract implementation of the policy interface.
 */
public abstract class AbstractPolicy implements Policy {
    // Shared state among policies
    protected PolicyId policyId;
    private PolicyType policyType;

    /**
     * Init the basic information of a policy.
     *
     * @param pType the policy type
     */
    protected AbstractPolicy(PolicyType pType) {
        policyType = pType;
    }

    @Override
    public PolicyId policyId() {
        return policyId;
    }

    @Override
    public PolicyType policyType() {
        return policyType;
    }

    /**
     * Computes the policy id. The actual computation is left to
     * the implementation class that can decide how to generate the
     * policy id.
     *
     * @return the computed policy id
     */
    protected abstract PolicyId computePolicyId();

}
