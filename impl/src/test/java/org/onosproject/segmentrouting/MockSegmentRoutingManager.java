/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onosproject.segmentrouting;

import org.onlab.packet.MacAddress;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock Segment Routing Manager.
 */
public class MockSegmentRoutingManager extends SegmentRoutingManager {
    private Map<Integer, TrafficTreatment> nextTable;
    private Map<DeviceId, MacAddress> routerMacs;
    private List<DeviceId> infraDeviceIds;
    private AtomicInteger atomicNextId = new AtomicInteger();

    MockSegmentRoutingManager(Map<Integer, TrafficTreatment> nextTable,
                              Map<DeviceId, MacAddress> routerMacs) {
        appId = new DefaultApplicationId(1, SegmentRoutingManager.APP_NAME);
        this.nextTable = nextTable;
        this.routerMacs = routerMacs;
        this.infraDeviceIds = List.of(DeviceId.deviceId("device:1"));
    }

    @Override
    public int getPortNextObjectiveId(DeviceId deviceId, PortNumber portNum,
                                      TrafficTreatment treatment,
                                      TrafficSelector meta,
                                      boolean createIfMissing) {
        int nextId = atomicNextId.incrementAndGet();
        nextTable.put(nextId, treatment);
        return nextId;
    }

    @Override
    public List<DeviceId> getInfraDeviceIds() {
        return List.copyOf(infraDeviceIds);
    }

    public void setInfraDeviceIds(List<DeviceId> infraDeviceIds) {
        this.infraDeviceIds = infraDeviceIds;
    }

    @Override
    public MacAddress getDeviceMacAddress(DeviceId deviceId) {
        return routerMacs.get(deviceId);
    }
}
