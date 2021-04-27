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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.TpPort;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.codec.impl.MockCodecContext;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;
import org.onosproject.segmentrouting.policy.api.TrafficMatchPriority;

import java.io.InputStream;

public class TrafficMatchCodecTest extends TestCase {
    private TrafficMatch trafficMatch;
    private TrafficSelector trafficSelector;
    private PolicyId policyId;

    private CodecContext context;
    private JsonCodec<TrafficMatch> codec;

    @Before
    public void setUp() throws Exception {
        context = new MockCodecContext();
        codec = new TrafficMatchCodec();

        trafficSelector = DefaultTrafficSelector.builder()
                .matchIPProtocol((byte) 0x06)
                .matchIPSrc(Ip4Address.valueOf("10.0.0.1").toIpPrefix())
                .matchIPDst(Ip4Address.valueOf("10.0.0.2").toIpPrefix())
                .matchTcpSrc(TpPort.tpPort(80))
                .matchTcpDst(TpPort.tpPort(81))
                .build();
        policyId = PolicyId.of("DROP");
        TrafficMatchPriority trafficMatchPriority = new TrafficMatchPriority(60000);
        trafficMatch = new TrafficMatch(trafficSelector, policyId, trafficMatchPriority);
    }

    @Test
    public void testEncode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream jsonStream1 = RedirectPolicyCodecTest.class.getResourceAsStream("/trafficmatch.json");
        JsonNode expected = mapper.readTree(jsonStream1);

        JsonNode actual = codec.encode(trafficMatch, context);

        assertEquals(expected.get(TrafficMatchCodec.TRAFFIC_MATCH_ID), actual.get(TrafficMatchCodec.TRAFFIC_MATCH_ID));
        assertEquals(expected.get(TrafficMatchCodec.POLICY_ID), actual.get(TrafficMatchCodec.POLICY_ID));
        for (int i = 0; i < trafficMatch.trafficSelector().criteria().size(); i++) {
            assertEquals(expected.get(TrafficMatchCodec.TRAFFIC_SELECTOR).get(i),
                    actual.get(TrafficMatchCodec.TRAFFIC_SELECTOR).get(i));
        }
    }

    @Test
    public void testDecode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream jsonStream1 = RedirectPolicyCodecTest.class.getResourceAsStream("/trafficmatch.json");
        ObjectNode json = mapper.readTree(jsonStream1).deepCopy();

        TrafficMatch actual = codec.decode(json, context);

        assertEquals(trafficMatch, actual);
    }
}