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
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.glassfish.jersey.internal.guava.Sets;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.cli.net.DeviceIdCompleter;
import org.onosproject.net.DeviceId;
import org.onosproject.segmentrouting.policy.api.PolicyId;
import org.onosproject.segmentrouting.policy.api.PolicyService;
import org.onosproject.segmentrouting.policy.api.RedirectPolicy;

import java.util.Set;

/**
 * Command to add a new redirect policy.
 */
@Service
@Command(scope = "onos", name = "sr-policy-redirect-add",
        description = "Create a new redirect policy")
public class PolicyRedirectAddCommand extends AbstractShellCommand {

    @Option(name = "-s", aliases = "--spine",
            description = "Pin to spine",
            valueToShowInHelp = "device:spine1",
            multiValued = true)
    @Completion(DeviceIdCompleter.class)
    String[] spines = null;

    @Override
    protected void doExecute() {
        Set<DeviceId> spinesToEnforce = spinesToEnforce();
        if (spinesToEnforce.isEmpty()) {
            print("Unable to submit redirect policy");
            return;
        }
        PolicyService policyService = AbstractShellCommand.get(PolicyService.class);
        PolicyId policyId = policyService.addOrUpdatePolicy(new RedirectPolicy(spinesToEnforce));
        print("Policy %s has been submitted", policyId);
    }

    private Set<DeviceId> spinesToEnforce() {
        Set<DeviceId> spinesToEnforce = Sets.newHashSet();
        if (spines != null) {
            for (String spine : spines) {
                spinesToEnforce.add(DeviceId.deviceId(spine));
            }
        }
        return spinesToEnforce;
    }
}
