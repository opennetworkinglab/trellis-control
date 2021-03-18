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
 * Policy data retrieved from the system.
 */
public final class PolicyData {
    // We want to provide access to the policy data as well as
    // the policy operations in the system and their status
    private final PolicyState policyState;
    private final Policy policy;
    private final List<String> operations;

    /**
     * Creates a policy data.
     *
     * @param pState the policy state
     * @param pol the policy
     * @param ops the operations associated
     */
    public PolicyData(PolicyState pState, Policy pol, List<String> ops) {
        policy = pol;
        policyState = pState;
        operations = ops;
    }

    /**
     * Returns the current state of the policy.
     *
     * @return the policy state
     */
    public PolicyState policyState() {
        return policyState;
    }


    /**
     * Returns the policy associated.
     *
     * @return the policy
     */
    public Policy policy() {
        return policy;
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
