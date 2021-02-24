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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.glassfish.jersey.internal.guava.Sets;

import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.segmentrouting.policy.api.Policy;
import org.onosproject.segmentrouting.policy.api.PolicyData;
import org.onosproject.segmentrouting.policy.api.PolicyService;

import java.util.Set;

/**
 * Command to show the list of policies.
 */
@Service
@Command(scope = "onos", name = "sr-policy-list",
        description = "Lists all policies")
public class PolicyListCommand extends AbstractShellCommand {

    private static final String FORMAT_MAPPING_POLICY =
            "  id=%s, state=%s, type=%s";
    private static final String FORMAT_MAPPING_OPERATION =
            "    op=%s";

    @Option(name = "-filt", aliases = "--filter",
            description = "Filter based on policy type",
            valueToShowInHelp = "DROP",
            multiValued = true)
    String[] filters = null;

    @Override
    protected void doExecute() {
        PolicyService policyService =
                AbstractShellCommand.get(PolicyService.class);
        policyService.policies(policyTypes()).forEach(this::printPolicy);
    }

    private Set<Policy.PolicyType> policyTypes() {
        Set<Policy.PolicyType> policyTypes = Sets.newHashSet();
        if (filters != null) {
            for (String filter : filters) {
                policyTypes.add(Policy.PolicyType.valueOf(filter));
            }
        }
        return policyTypes;
    }

    private void printPolicy(PolicyData policyData) {
        print(FORMAT_MAPPING_POLICY, policyData.policy().policyId(), policyData.policyState(),
                policyData.policy().policyType());
        policyData.operations().forEach(operation -> print(FORMAT_MAPPING_OPERATION, operation));
    }
}