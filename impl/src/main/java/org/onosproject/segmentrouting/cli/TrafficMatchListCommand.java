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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.TrafficMatchData;

/**
 * Command to show the list of traffic matches.
 */
@Service
@Command(scope = "onos", name = "sr-tm",
        description = "Lists all traffic matches")
public class TrafficMatchListCommand extends AbstractShellCommand {

    private static final String FORMAT_MAPPING_TRAFFIC_MATCH =
            "  id=%s, state=%s, policyId=%s";
    private static final String FORMAT_MAPPING_OPERATION =
            "    op=%s";

    @Override
    protected void doExecute() {
        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        policyService.trafficMatches().forEach(this::printTrafficMatch);
    }

    private void printTrafficMatch(TrafficMatchData trafficMatchData) {
        print(FORMAT_MAPPING_TRAFFIC_MATCH, trafficMatchData.trafficMatch().trafficMatchId(),
                trafficMatchData.trafficMatchState(), trafficMatchData.trafficMatch().policyId());
        trafficMatchData.operations().forEach(operation -> print(FORMAT_MAPPING_OPERATION, operation));
    }
}
