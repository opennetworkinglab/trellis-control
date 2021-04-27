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

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Priorities for traffic match.
 */
public final class TrafficMatchPriority {
    /**
     * Priority of the TrafficMatch.
     */
    private final int priority;

    /**
     * Lowest allowable priority value.
     */
    public static final int MIN = 50000;

    /**
     * Medium priority value.
     */
    public static final int MEDIUM = 55000;

    /**
     * Highest allowable priority value.
     */
    public static final int MAX = 60000;

    private static final String ILLEGAL_PRIORITY_MESSAGE = "The priority value is out of range.";

    /**
     * Using arbitrary or pre-defined value.
     *
     * @param priority A arbitrary or pre-defined priority value.
     * @throws IllegalArgumentException if priority value less or greater than lower/upper bound.
     */
    public TrafficMatchPriority(int priority) throws IllegalArgumentException {
        if (priority < MIN || priority > MAX) {
            throw new IllegalArgumentException(ILLEGAL_PRIORITY_MESSAGE);
        } else {
            this.priority = priority;
        }
    }

    /**
     * Get priority value.
     *
     * @return the priority value.
     */
    public int priority() {
        return this.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TrafficMatchPriority) {
            final TrafficMatchPriority other = (TrafficMatchPriority) obj;
            return this.priority() == other.priority();
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("priority", priority)
                .toString();
    }
}
