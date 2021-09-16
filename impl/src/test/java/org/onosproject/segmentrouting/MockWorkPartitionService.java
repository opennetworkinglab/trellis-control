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

import org.onosproject.cluster.LeadershipService;
import org.onosproject.cluster.NodeId;
import org.onosproject.cluster.PartitionId;
import org.onosproject.net.intent.WorkPartitionServiceAdapter;

import java.util.function.Function;

public class MockWorkPartitionService extends WorkPartitionServiceAdapter {

    LeadershipService leadershipService;
    static final int NUM_PARTITIONS = 14;

    @Override
    public <K> NodeId getLeader(K id, Function<K, Long> hasher) {
        int partition = Math.abs(hasher.apply(id).intValue()) % NUM_PARTITIONS;
        PartitionId partitionId = new PartitionId(partition);
        return leadershipService.getLeadership(partitionId.toString()).leaderNodeId();
    }

}
