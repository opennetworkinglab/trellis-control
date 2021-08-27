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
package org.onosproject.segmentrouting.metadata;

import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.MetadataCriterion;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flowobjective.FilteringObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.Objective;

/**
 * Defines the SegmentRouting metadata extensions.
 */
public final class SRObjectiveMetadata {
    // Helper class
    private SRObjectiveMetadata() {
    }

    //////////////////////////////////////////////////////////////////////////////
    // 64 .... 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0 //
    //  X      X  X  X  X  X  X  X  X  X  X  X  X  X  X  X  X X X X X 1 1 1 1 1 //
    //////////////////////////////////////////////////////////////////////////////
    // Metadata instruction is used as 8 byte sequence to carry up to 64 metadata

    // FIXME We are assuming SR as the only app programming this meta.
    // SDFAB-530 to get rid of this limitation

    /**
     * SR is setting this metadata when a double tagged filtering objective is removed
     * and no other hosts is sharing the same input port. Thus, termination mac entries
     * can be removed together with the vlan table entries.
     *
     * See org.onosproject.segmentrouting.RoutingRulePopulator#buildDoubleTaggedFilteringObj()
     * See org.onosproject.segmentrouting.RoutingRulePopulator#processDoubleTaggedFilter()
     */
    public static final long CLEANUP_DOUBLE_TAGGED_HOST_ENTRIES = 1L;

    /**
     * SR is setting this metadata when an interface config update has been performed
     * and thus termination mac entries should not be removed.
     *
     * See org.onosproject.segmentrouting.RoutingRulePopulator#processSinglePortFiltersInternal
     */
    public static final long INTERFACE_CONFIG_UPDATE = 1L << 1;

    /**
     * SR is setting this metadata to signal the driver when the config is for the pair port,
     * i.e. ports connecting two leaves.
     *
     *  See org.onosproject.segmentrouting.RoutingRulePopulator#portType
     */
    public static final long PAIR_PORT = 1L << 2;

    /**
     * SR is setting this metadata to signal the driver when the config is for an edge port,
     * i.e. ports facing an host.
     *
     * See org.onosproject.segmentrouting.policy.impl.PolicyManager#trafficMatchFwdObjective
     * See org.onosproject.segmentrouting.RoutingRulePopulator#portType
     */
    public static final long EDGE_PORT = 1L << 3;

    /**
     * SR is setting this metadata to signal the driver when the config is for an infra port,
     * i.e. ports connecting a leaf with a spine.
     */
    public static final long INFRA_PORT = 1L << 4;

    private static final long METADATA_MASK = 0x1FL;

    /**
     * Check metadata passed from SegmentRouting app.
     *
     * @param obj the objective containing the metadata
     * @return true if the objective contains valid metadata, false otherwise
     */
    public static boolean isValidSrMetadata(Objective obj) {
        long meta = 0;
        if (obj instanceof FilteringObjective) {
            FilteringObjective filtObj = (FilteringObjective) obj;
            if (filtObj.meta() == null) {
                return true;
            }
            Instructions.MetadataInstruction metaIns = filtObj.meta().writeMetadata();
            if (metaIns == null) {
                return true;
            }
            meta = metaIns.metadata() & metaIns.metadataMask();
        } else if (obj instanceof ForwardingObjective) {
            ForwardingObjective fwdObj = (ForwardingObjective) obj;
            if (fwdObj.meta() == null) {
                return true;
            }
            MetadataCriterion metaCrit = (MetadataCriterion) fwdObj.meta().getCriterion(Criterion.Type.METADATA);
            if (metaCrit == null) {
                return true;
            }
            meta = metaCrit.metadata();
        }
        return meta != 0 && (meta ^ METADATA_MASK) <= METADATA_MASK;
    }

    /**
     * Verify if a given flag has been set into the metadata.
     *
     * @param obj the objective containing the metadata
     * @param flag the flag to verify
     * @return true if the flag is set, false otherwise
     */
    public static boolean isSrMetadataSet(Objective obj, long flag) {
        long meta = 0;
        if (obj instanceof FilteringObjective) {
            FilteringObjective filtObj = (FilteringObjective) obj;
            if (filtObj.meta() == null) {
                return false;
            }
            Instructions.MetadataInstruction metaIns = filtObj.meta().writeMetadata();
            if (metaIns == null) {
                return false;
            }
            meta = metaIns.metadata() & metaIns.metadataMask();
        } else if (obj instanceof ForwardingObjective) {
            ForwardingObjective fwdObj = (ForwardingObjective) obj;
            if (fwdObj.meta() == null) {
                return false;
            }
            MetadataCriterion metaCrit = (MetadataCriterion) fwdObj.meta().getCriterion(Criterion.Type.METADATA);
            if (metaCrit == null) {
                return false;
            }
            meta = metaCrit.metadata();
        }
        return (meta & flag) == flag;
    }
}
