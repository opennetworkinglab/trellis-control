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

import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.TrafficMatchId;

/**
 * Command to remove a traffic match.
 */
@Service
@Command(scope = "onos", name = "sr-tm-remove",
        description = "Remove a traffic match")
public class TrafficMatchRemoveCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "trafficMatchId",
            description = "traffic match id",
            required = true, multiValued = false)
    String trafficMatchId;

    @Override
    protected void doExecute() {
        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        trafficMatchId = trafficMatchId.replace("\\", "");
        if (policyService.removeTrafficMatch(TrafficMatchId.of(trafficMatchId))) {
            print("Removing traffic match %s", trafficMatchId);
        } else {
            print("Unable to remove traffic match %s", trafficMatchId);
        }
    }
}