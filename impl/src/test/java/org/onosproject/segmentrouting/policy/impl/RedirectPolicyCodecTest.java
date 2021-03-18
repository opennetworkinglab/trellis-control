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
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.codec.impl.MockCodecContext;
import org.onosproject.net.DeviceId;
import org.onosproject.segmentrouting.policy.api.RedirectPolicy;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RedirectPolicyCodecTest extends TestCase {
    private RedirectPolicy redirectPolicy;

    private CodecContext context;
    private JsonCodec<RedirectPolicy> codec;

    @Before
    public void setUp() throws Exception {
        context = new MockCodecContext();
        codec = new RedirectPolicyCodec();

        List<DeviceId> deviceIds = new LinkedList<>();
        deviceIds.add(DeviceId.deviceId("of:0000000000000001"));
        deviceIds.add(DeviceId.deviceId("of:0000000000000002"));
        deviceIds.add(DeviceId.deviceId("of:0000000000000003"));
        redirectPolicy = new RedirectPolicy(Set.copyOf(deviceIds));
    }

    @Test
    public void testEncode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream jsonStream1 = RedirectPolicyCodecTest.class.getResourceAsStream("/redirectpolicy.json");
        JsonNode expected = mapper.readTree(jsonStream1);

        JsonNode actual = codec.encode(redirectPolicy, context);

        assertEquals(expected.get(RedirectPolicyCodec.POLICY_ID), actual.get(RedirectPolicyCodec.POLICY_ID));
        assertEquals(expected.get(RedirectPolicyCodec.POLICY_TYPE), actual.get(RedirectPolicyCodec.POLICY_TYPE));
        assertEquals(expected.get(RedirectPolicyCodec.DEVICE_ID), actual.get(RedirectPolicyCodec.DEVICE_ID));
    }

    @Test
    public void testDecode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream jsonStream1 = RedirectPolicyCodecTest.class.getResourceAsStream("/redirectpolicy.json");
        ObjectNode json = mapper.readTree(jsonStream1).deepCopy();

        RedirectPolicy actual = codec.decode(json, context);

        assertEquals(redirectPolicy, actual);
    }
}