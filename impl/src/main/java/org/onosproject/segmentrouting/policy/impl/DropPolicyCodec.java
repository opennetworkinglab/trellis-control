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

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.segmentrouting.policy.api.DropPolicy;

/**
 * Codec of DropPolicy class.
 */
public final class DropPolicyCodec extends JsonCodec<DropPolicy> {

    // JSON field names
    public static final String POLICY_ID = "policy_id";
    public static final String POLICY_TYPE = "policy_type";

    @Override
    public ObjectNode encode(DropPolicy policy, CodecContext context) {
        final ObjectNode result = context.mapper().createObjectNode()
                .put(POLICY_ID, policy.policyId().toString())
                .put(POLICY_TYPE, policy.policyType().toString());

        return result;
    }

    @Override
    public DropPolicy decode(ObjectNode json, CodecContext context) {
        return new DropPolicy();
    }
}
