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
package org.onosproject.segmentrouting.policy.impl;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.segmentrouting.policy.api.Policy;
import org.onosproject.segmentrouting.policy.api.TrafficMatch;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * keep track of the operation and of its status in the system.
 */
final class Operation {
    private boolean isInstall;
    private boolean isDone;
    private Objective objectiveOperation;
    private Policy policy;
    private TrafficMatch trafficMatch;

    private Operation(boolean install, boolean done, Objective objective,
                      Policy pol, TrafficMatch tMatch) {
        isInstall = install;
        isDone = done;
        objectiveOperation = objective;
        policy = pol;
        trafficMatch = tMatch;
    }

    /**
     * Returns whether or not the operation is done.
     *
     * @return true if operation is done. False otherwise.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns whether or not it is an installation.
     *
     * @return true if it is an installation. False for removal
     */
    public boolean isInstall() {
        return isInstall;
    }

    /**
     * Returns the objective operation.
     *
     * @return the associated flow objective
     */
    public Objective objectiveOperation() {
        return objectiveOperation;
    }

    /**
     * Returns the policy if present.
     *
     * @return the policy
     */
    public Optional<Policy> policy() {
        return Optional.ofNullable(policy);
    }

    /**
     * Returns the traffic match if present.
     *
     * @return the traffic match
     */
    public Optional<TrafficMatch> trafficMatch() {
        return Optional.ofNullable(trafficMatch);
    }

    /**
     * Updates isDone.
     *
     * @param isDone if it is done
     */
    public void isDone(boolean isDone) {
        this.isDone = isDone;
    }

    /**
     * Updates the flowObjective.
     *
     * @param objectiveOperation the flowObjective
     */
    public void objectiveOperation(Objective objectiveOperation) {
        this.objectiveOperation = objectiveOperation;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Operation)) {
            return false;
        }
        final Operation other = (Operation) obj;
        return  this.isInstall == other.isInstall &&
                this.isDone == other.isDone &&
                Objects.equals(this.objectiveOperation, other.objectiveOperation) &&
                Objects.equals(this.policy, other.policy) &&
                Objects.equals(this.trafficMatch, other.trafficMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInstall, isDone, objectiveOperation, policy, trafficMatch);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = toStringHelper(this)
                .add("isInstall", isInstall)
                .add("isDone", isDone)
                .add("objective", objectiveOperation);
        if (policy != null) {
            helper.add("policy", policy);
        }
        if (trafficMatch != null) {
            helper.add("trafficMatch", trafficMatch);
        }
        return helper.toString();
    }

    public String toStringMinimal() {
        MoreObjects.ToStringHelper helper = toStringHelper(this)
                .add("isInstall", isInstall)
                .add("isDone", isDone);
        if (policy != null) {
            helper.add("policy", policy);
        }
        if (trafficMatch != null) {
            helper.add("trafficMatch", trafficMatch);
        }
        return helper.toString();
    }

    /**
     * Creates a new operation builder.
     *
     * @return an operation builder
     */
    public static Operation.Builder builder() {
        return new Operation.Builder();
    }

    /**
     * Creates a new operation builder using the supplied operation.
     * The boolean isDone and the objective operation won't be copied
     * by the supplied operation.
     *
     * @param operation the operation
     * @return an operation builder
     */
    public static Operation.Builder builder(Operation operation) {
        return new Operation.Builder(operation);
    }

    /**
     * Builder for Operation objects.
     */
    public static final class Builder {
        private boolean isInstall;
        private boolean isDone;
        private Objective objectiveOperation;
        private Policy policy;
        private TrafficMatch trafficMatch;

        private Builder() {
            // Hide constructor
        }

        private Builder(Operation operation) {
            isInstall = operation.isInstall();
            policy = operation.policy().orElse(null);
            trafficMatch = operation.trafficMatch().orElse(null);
        }

        /**
         * Sets the flowObjective.
         *
         * @param objectiveOperation the flowObjective
         * @return this builder
         */
        public Builder objectiveOperation(Objective objectiveOperation) {
            this.objectiveOperation = objectiveOperation;
            return this;
        }

        /**
         * Sets if it is done.
         *
         * @param isDone if it is done
         * @return this builder
         */
        public Builder isDone(boolean isDone) {
            this.isDone = isDone;
            return this;
        }

        /**
         * Sets if it is an installation.
         *
         * @param isInstall if it is an installation
         * @return this builder
         */
        public Builder isInstall(boolean isInstall) {
            this.isInstall = isInstall;
            return this;
        }

        /**
         * Sets the policy.
         *
         * @param policy the policy
         * @return this builder
         */
        public Builder policy(Policy policy) {
            this.policy = policy;
            return this;
        }

        /**
         * Sets the traffic match.
         *
         * @param trafficMatch the traffic match
         * @return this builder
         */
        public Builder trafficMatch(TrafficMatch trafficMatch) {
            this.trafficMatch = trafficMatch;
            return this;
        }

        /**
         * Builds an operation object from the accumulated parameters.
         *
         * @return operation object
         */
        public Operation build() {
            if ((policy == null && trafficMatch == null) ||
                    (policy != null && trafficMatch != null)) {
                throw new IllegalArgumentException("Policy and traffic cannot be both null or both set");
            }
            return new Operation(isInstall, isDone, objectiveOperation, policy, trafficMatch);
        }
    }
}
