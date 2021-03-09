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

/**
 * Represents the state of a traffic match as seen by the system.
 */
// TODO consider to add a FAILED state for an invalid traffic match that cannot be fulfilled even after a retry
public enum TrafficMatchState {
    /**
     * The traffic match is in the process of being added.
     */
    PENDING_ADD,

    /**
     * The traffic match has been added.
     */
    ADDED,

    /**
     * The traffic match is in the process of being removed.
     */
    PENDING_REMOVE
}
