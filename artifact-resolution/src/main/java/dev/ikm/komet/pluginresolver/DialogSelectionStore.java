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
package dev.ikm.komet.pluginresolver;

import java.util.Optional;

/**
 * Stores the last-used "Add from Maven" dialog configuration, keyed by flow (e.g. one entry
 * for SpinedArray-store downloads, a separate entry for protobuf-changeset downloads), so the
 * dialog can pre-fill itself on a repeat visit instead of starting blank.
 */
public interface DialogSelectionStore {

    /**
     * The remembered selection for {@code flowKey}, if any.
     *
     * @param flowKey identifies which download flow this selection belongs to
     * @return the remembered selection, or empty if none has been recorded yet
     */
    Optional<DialogSelection> get(String flowKey);

    /**
     * Records {@code selection} as the last-used configuration for {@code flowKey}, replacing
     * any previous entry.
     *
     * @param flowKey identifies which download flow this selection belongs to
     * @param selection the selection to remember
     */
    void put(String flowKey, DialogSelection selection);
}
