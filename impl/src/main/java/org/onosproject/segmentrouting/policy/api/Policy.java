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
 * Represents a policy in TOST.
 */
public interface Policy {
    /**
     * Represents the type of a policy.
     */
    enum PolicyType {
        /**
         * The policy drops the associated traffic.
         */
        DROP,

        /**
         * The policy redirects traffic using custom routing.
         */
        REDIRECT
    }

    /**
     * Returns the policy id.
     *
     * @return the policy id
     */
    PolicyId policyId();

    /**
     * Returns the policy type.
     *
     * @return the type of a policy
     */
    PolicyType policyType();
}
