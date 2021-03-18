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
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;

import static org.onlab.util.Tools.nullIsIllegal;

/**
 * Codec of TrafficMatch class.
 */
public final class TrafficMatchCodec extends JsonCodec<TrafficMatch> {

    // JSON field names
    public static final String TRAFFIC_MATCH_ID = "traffic_match_id";
    public static final String TRAFFIC_SELECTOR = "selector";
    public static final String POLICY_ID = "policy_id";
    public static final String MISSING_MEMBER_MESSAGE =
            " member is required in Traffic Match";

    @Override
    public ObjectNode encode(TrafficMatch trafficMatch, CodecContext context) {
        final JsonCodec<TrafficSelector> selectorCodec =
                context.codec(TrafficSelector.class);

        final ObjectNode selector = selectorCodec.encode(trafficMatch.trafficSelector(), context);
        final ObjectNode result = context.mapper().createObjectNode()
                .put(TRAFFIC_MATCH_ID, trafficMatch.trafficMatchId().toString())
                .put(POLICY_ID, trafficMatch.policyId().toString())
                .set(TRAFFIC_SELECTOR, selector);

        return result;
    }

    @Override
    public TrafficMatch decode(ObjectNode json, CodecContext context) {
        final JsonCodec<TrafficSelector> selectorCodec =
                context.codec(TrafficSelector.class);

        ObjectNode selectorJson = nullIsIllegal(get(json, TRAFFIC_SELECTOR),
                TRAFFIC_SELECTOR + MISSING_MEMBER_MESSAGE);
        TrafficSelector trafficSelector = selectorCodec.decode(selectorJson, context);

        PolicyId policyId = PolicyId.of(nullIsIllegal(json.get(POLICY_ID),
                POLICY_ID + MISSING_MEMBER_MESSAGE).asText());

        return new TrafficMatch(trafficSelector, policyId);
    }
}
