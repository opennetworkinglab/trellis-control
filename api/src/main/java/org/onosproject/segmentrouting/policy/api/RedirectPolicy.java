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
package org.onosproject.segmentrouting.policy.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.onosproject.net.DeviceId;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of the redirect policy.
 */
public final class RedirectPolicy extends AbstractPolicy {
    private List<DeviceId> spinesToEnforce = Lists.newArrayList();

    /**
     * Builds up a REDIRECT policy.
     *
     * @param spines the spines to enforce
     */
    public RedirectPolicy(Set<DeviceId> spines) {
        super(PolicyType.REDIRECT);
        checkArgument(!spines.isEmpty(), "Must have at least one spine");
        // Creates an ordered set
        TreeSet<DeviceId> sortedSpines = Sets.newTreeSet(Comparator.comparing(DeviceId::toString));
        sortedSpines.addAll(spines);
        spinesToEnforce.addAll(sortedSpines);
        policyId = computePolicyId();
    }

    /**
     * Returns the spines to be enforced during the path computation.
     *
     * @return the spines to be enforced
     */
    public List<DeviceId> spinesToEnforce() {
        return spinesToEnforce;
    }

    @Override
    protected PolicyId computePolicyId() {
        return PolicyId.of(policyType().name() + spinesToEnforce);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RedirectPolicy)) {
            return false;
        }
        final RedirectPolicy other = (RedirectPolicy) obj;
        return Objects.equals(policyType(), other.policyType()) &&
                Objects.equals(policyId(), other.policyId()) &&
                Objects.equals(spinesToEnforce, other.spinesToEnforce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyId(), policyType(), spinesToEnforce);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("policyId", policyId())
                .add("policyType", policyType())
                .add("spinesToEnforce", spinesToEnforce)
                .toString();
    }
}
