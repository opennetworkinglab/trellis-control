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
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyService;

/**
 * Command to remove a policy.
 */
@Service
@Command(scope = "onos", name = "sr-policy-remove",
        description = "Remove a policy")
public class PolicyRemoveCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "policyId",
            description = "policy id",
            required = true, multiValued = false)
    String policyId;

    @Override
    protected void doExecute() {
        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        if (policyService.removePolicy(PolicyId.of(policyId))) {
            print("Removing policy %s", policyId);
        } else {
            print("Unable to remove policy %s", policyId);
        }
    }
}