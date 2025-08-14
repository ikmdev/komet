/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.app;

/**
 * Represents the various states of the Komet application lifecycle.
 * <p>
 * The {@code AppState} enum defines the different stages that the application transitions through,
 * from initialization to shutdown. These states help manage the application's behavior
 * and user interactions at each phase of its execution.
 * </p>
 */
public enum AppState {
    /**
     * The application is in the process of starting up.
     */
    STARTING,

    /**
     * The application is handling user authentication.
     */
    LOGIN,

    /**
     * The application is prompting the user to select a data source.
     */
    SELECT_DATA_SOURCE,

    /**
     * A data source has been selected by the user.
     */
    SELECTED_DATA_SOURCE,

    /**
     * The application is currently loading data from the selected source.
     */
    LOADING_DATA_SOURCE,

    /**
     *
     */
    SELECT_USER,

    /**
     * The application is fully operational and running.
     */
    RUNNING,

    /**
     * The application is in the process of shutting down.
     */
    SHUTDOWN;
}
