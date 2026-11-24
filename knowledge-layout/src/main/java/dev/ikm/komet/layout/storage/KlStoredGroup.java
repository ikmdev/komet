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
import dev.ikm.tinkar.entity.boundary.DataKind;

import java.util.List;
import java.util.Objects;

/**
 * One group of shared or local data in the knowledge base, as the Storage window lists it: what is
 * removed, shared or exported as a whole.
 *
 * @param provider the provider that answers for it, or {@code null} when none does
 * @param root     the root its scope is resolved from: a pattern, or a concept or semantic when it has no pattern of its own
 * @param kind     the kind of data it is
 * @param name     its name
 * @param from     where it came from, in words, such as a file's folder or the card that shows it
 * @param shown    whether a list elsewhere in Komet shows it
 * @param offers   what can be done with it
 */
public record KlStoredGroup(KlStorageProvider provider, PublicId root, DataKind kind, String name, String from, boolean shown,
                            List<KlStorageOffer> offers) {

    public KlStoredGroup {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(kind, "kind");
        from = from == null ? "" : from;
        offers = List.copyOf(offers);
    }
}
