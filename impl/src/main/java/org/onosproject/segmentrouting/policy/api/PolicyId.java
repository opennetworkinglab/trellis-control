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

import org.onlab.util.Identifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of a policy id.
 */
public final class PolicyId extends Identifier<String> {

    protected PolicyId(String id) {
        super(id);
    }

    /**
     * Returns the id of the policy given the value.
     *
     * @param name policy id value
     * @return policy id
     */
    public static PolicyId of(String name) {
        checkNotNull(name);
        checkArgument(!name.isEmpty(), "Name cannot be empty");
        return new PolicyId(name);
    }

}
