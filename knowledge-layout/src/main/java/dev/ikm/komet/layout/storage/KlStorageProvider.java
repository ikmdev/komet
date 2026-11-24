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
package dev.ikm.komet.layout.storage;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.scope.Scope;
import dev.ikm.tinkar.entity.scope.ScopeOperations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A source of the groups of shared and local data that Komet's Storage window lists, discovered as a
 * service (a {@code provides KlStorageProvider with …} clause): a plugin that writes such data answers
 * here for what it wrote (IKE-Network/ike-issues#1313). A document is one group, and so is a
 * conversation with the assistant. Every method is called off the JavaFX thread.
 */
public interface KlStorageProvider {

    /** @return the provider's name, for a message about it */
    String sourceName();

    /** @return the groups this provider shows a person somewhere, or keeps in the Trash */
    List<KlStoredGroup> groups();

    /**
     * Says what a group is that no provider lists, when this provider knows: a conversation's
     * journal whose conversation is gone is an example.
     *
     * @param leftover the group
     * @return the group as the window lists it, or empty when it is not this provider's
     */
    default Optional<KlStoredGroup> describe(Scope leftover) {
        return Optional.empty();
    }

    /**
     * What an export of a group adds beside its components, such as a conversation's own file.
     *
     * @param root the group's root, as {@link KlStoredGroup#root()} gives it
     * @return the entries by simple file name, empty when the group is not this provider's
     */
    default Map<String, byte[]> exportEntries(PublicId root) {
        return Map.of();
    }

    /**
     * Told that a group was imported, once the knowledge base has it, so it can be shown.
     *
     * @param imported what was imported
     * @param entries  what the file had beside the components
     * @return what it is, in words for the person, such as {@code the conversation “…”}; empty when it
     *         is not this provider's
     */
    default Optional<String> imported(ScopeOperations.Imported imported, Map<String, byte[]> entries) {
        return Optional.empty();
    }
}
