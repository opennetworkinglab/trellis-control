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
import org.apache.karaf.shell.api.action.Option;
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
import org.onosproject.segmentrouting.policy.api.TrafficMatchPriority;

/**
 * Command to add a traffic match.
 */
@Service
@Command(scope = "onos", name = "sr-tm-add",
        description = "Create a new traffic match")
public class TrafficMatchAddCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "policyId",
            description = "policy id",
            required = true, multiValued = false)
    String policyId;

    @Argument(index = 1, name = "priority",
            description = "priority",
            required = true, multiValued = false)
    int priority;

    @Option(name = "-sip", aliases = "--srcIp",
            description = "src IP",
            valueToShowInHelp = "10.0.0.1",
            multiValued = false)
    String srcIp;

    @Option(name = "-sp", aliases = "--srcPort",
            description = "src port",
            valueToShowInHelp = "1001",
            multiValued = false)
    short srcPort;

    @Option(name = "-dip", aliases = "--dstIp",
            description = "dst IP",
            valueToShowInHelp = "10.0.0.2",
            multiValued = false)
    String dstIp;

    @Option(name = "-dp", aliases = "--dstPort",
            description = "dst port",
            valueToShowInHelp = "1002",
            multiValued = false)
    short dstPort;

    @Option(name = "-p", aliases = "--proto",
            description = "IP protocol",
            valueToShowInHelp = "0x11",
            multiValued = false)
    String proto;

    // TODO Consider to filter out the following fields for red policies
    @Option(name = "-smac", aliases = "--srcMac",
            description = "src MAC",
            valueToShowInHelp = "00:00:00:00:00:01",
            multiValued = false)
    String srcMac;

    @Option(name = "-dmac", aliases = "--dstMac",
            description = "dst MAC",
            valueToShowInHelp = "00:00:00:00:00:02",
            multiValued = false)
    String dstMac;

    @Option(name = "-vid", aliases = "--VlanId",
            description = "vlan ID",
            valueToShowInHelp = "10",
            multiValued = false)
    short vlanId = -1;

    @Override
    protected void doExecute() {
        TrafficSelector trafficSelector = parseArguments();
        if (trafficSelector.equals(DefaultTrafficSelector.emptySelector())) {
            print("Empty traffic selector is not allowed");
            return;
        }
        TrafficMatchPriority trafficMatchPriority;
        try {
            trafficMatchPriority = new TrafficMatchPriority(priority);
        } catch (IllegalArgumentException ex) {
            print(ex.getMessage());
            return;
        }

        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        TrafficMatchId trafficMatchId = policyService.addOrUpdateTrafficMatch(
                new TrafficMatch(trafficSelector, PolicyId.of(policyId), trafficMatchPriority));
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
