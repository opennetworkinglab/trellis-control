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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.DeviceId;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.onlab.util.Tools.nullIsIllegal;

/**
 * Codec of RedirectPolicy class.
 */
public final class RedirectPolicyCodec extends JsonCodec<RedirectPolicy> {

    // JSON field names
    public static final String POLICY_ID = "policy_id";
    public static final String POLICY_TYPE = "policy_type";
    public static final String SPINES_TO_ENFORCES = "spinesToEnforce";
    public static final String DEVICE_ID = "deviceId";
    public static final String MISSING_MEMBER_MESSAGE =
            " member is required in Redirect Policy";

    @Override
    public ObjectNode encode(RedirectPolicy policy, CodecContext context) {
        final ObjectNode result = context.mapper().createObjectNode()
                .put(POLICY_ID, policy.policyId().toString())
                .put(POLICY_TYPE, policy.policyType().toString());

        ArrayNode deviceIdArr = result.putObject(SPINES_TO_ENFORCES).putArray(DEVICE_ID);
        for (DeviceId deviceId : policy.spinesToEnforce()) {
            deviceIdArr.add(deviceId.toString());
        }

        return result;
    }

    @Override
    public RedirectPolicy decode(ObjectNode json, CodecContext context) {
        List<DeviceId> spinesToEnforce = new LinkedList<>();

        ObjectNode spinesNode = nullIsIllegal(get(json, SPINES_TO_ENFORCES),
                SPINES_TO_ENFORCES + MISSING_MEMBER_MESSAGE);
        ArrayNode deviceIdArr = nullIsIllegal((ArrayNode) spinesNode.get(DEVICE_ID),
                DEVICE_ID + MISSING_MEMBER_MESSAGE);
        deviceIdArr.forEach(deviceId -> spinesToEnforce.add(DeviceId.deviceId(deviceId.textValue())));

        return new RedirectPolicy(Set.copyOf(spinesToEnforce));
    }
}
