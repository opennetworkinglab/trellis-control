/*
 * Copyright 2015-present Open Networking Foundation
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
package org.onosproject.segmentrouting.cli;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.onlab.packet.IPv4;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.VlanId;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.cli.net.IpProtocol;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;

/**
 * Command to add a traffic match.
 */
@Service
@Command(scope = "onos", name = "sr-tmatch-add",
        description = "Create a new traffic match")
public class TrafficMatchAddCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "policyId",
            description = "policy id",
            required = true, multiValued = false)
    String policyId;

    @Argument(index = 1, name = "srcIp",
            description = "src IP",
            required = false, multiValued = false)
    String srcIp;

    @Argument(index = 2, name = "srcPort",
            description = "src port",
            required = false, multiValued = false)
    short srcPort;

    @Argument(index = 3, name = "dstIp",
            description = "dst IP",
            required = false, multiValued = false)
    String dstIp;

    @Argument(index = 4, name = "dstPort",
            description = "dst port",
            required = false, multiValued = false)
    short dstPort;

    @Argument(index = 5, name = "proto",
            description = "IP protocol",
            required = false, multiValued = false)
    String proto;

    @Argument(index = 6, name = "srcMac",
            description = "src MAC",
            required = false, multiValued = false)
    String srcMac;

    @Argument(index = 7, name = "dstMac",
            description = "dst MAC",
            required = false, multiValued = false)
    String dstMac;

    @Argument(index = 8, name = "vlanId",
            description = "VLAN id",
            required = false, multiValued = false)
    short vlanId = -1;

    @Override
    protected void doExecute() {
        TrafficSelector trafficSelector = parseArguments();
        if (trafficSelector.equals(DefaultTrafficSelector.emptySelector())) {
            print("Empty traffic selector is not allowed");
            return;
        }

        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        TrafficMatchId trafficMatchId = policyService.addOrUpdateTrafficMatch(
                new TrafficMatch(trafficSelector, PolicyId.of(policyId)));
        print("Traffic match %s has been submitted", trafficMatchId);
    }

    private TrafficSelector parseArguments() {
        TrafficSelector.Builder trafficSelectorBuilder = DefaultTrafficSelector.builder();
        if (srcIp != null) {
            trafficSelectorBuilder.matchIPSrc(IpPrefix.valueOf(srcIp));
        }
        if (dstIp != null) {
            trafficSelectorBuilder.matchIPDst(IpPrefix.valueOf(dstIp));
        }
        byte ipProtocol = 0;
        if (proto != null) {
            ipProtocol = (byte) (0xFF & IpProtocol.parseFromString(proto));
            trafficSelectorBuilder.matchIPProtocol(ipProtocol);
        }
        if (srcPort != 0) {
            if (ipProtocol == IPv4.PROTOCOL_TCP) {
                trafficSelectorBuilder.matchTcpSrc(TpPort.tpPort(srcPort));
            } else if (ipProtocol == IPv4.PROTOCOL_UDP) {
                trafficSelectorBuilder.matchUdpSrc(TpPort.tpPort(srcPort));
            }
        }
        if (dstPort != 0) {
            if (ipProtocol == IPv4.PROTOCOL_TCP) {
                trafficSelectorBuilder.matchTcpDst(TpPort.tpPort(dstPort));
            } else if (ipProtocol == IPv4.PROTOCOL_UDP) {
                trafficSelectorBuilder.matchUdpDst(TpPort.tpPort(dstPort));
            }
        }
        if (srcMac != null) {
            trafficSelectorBuilder.matchEthSrc(MacAddress.valueOf(srcMac));
        }
        if (dstMac != null) {
            trafficSelectorBuilder.matchEthDst(MacAddress.valueOf(dstMac));
        }
        if (vlanId != -1) {
            trafficSelectorBuilder.matchVlanId(VlanId.vlanId(vlanId));
        }
        return trafficSelectorBuilder.build();
    }
}
