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
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.boundary.DataKind;
import dev.ikm.tinkar.entity.boundary.EntityBoundaries;
import dev.ikm.tinkar.entity.scope.DataInventory;
import dev.ikm.tinkar.entity.scope.Scope;
import dev.ikm.tinkar.entity.scope.ScopeOperations;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What the {@link KlStorageProvider} services list, read as one, with the groups none of them lists
 * (IKE-Network/ike-issues#1313; step 3 of {@code plan-2026-11-22-remove-export-import-data}). Every
 * group of shared and local data appears once. Call off the JavaFX thread.
 */
public final class KlStorage {

    private static final Logger LOG = LoggerFactory.getLogger(KlStorage.class);

    private KlStorage() {
    }

    /**
     * A group with its size.
     *
     * @param group      the group
     * @param components the concepts, patterns and semantics it has
     */
    public record Sized(KlStoredGroup group, int components) { }

    /** @return the providers, as the service loader finds them */
    public static List<KlStorageProvider> providers() {
        List<KlStorageProvider> providers = new ArrayList<>();
        try {
            for (KlStorageProvider provider : PluggableService.load(KlStorageProvider.class)) {
                providers.add(provider);
            }
        } catch (RuntimeException e) {
            LOG.warn("Could not load the storage providers", e);
        }
        return providers;
    }

    /** @return the groups the providers list, in the order they list them, each root once */
    public static List<KlStoredGroup> groups() {
        Map<Long, KlStoredGroup> byRoot = new LinkedHashMap<>();
        for (KlStorageProvider provider : providers()) {
            try {
                for (KlStoredGroup group : provider.groups()) {
                    if (PrimitiveData.get().hasPublicId(group.root())) {
                        byRoot.putIfAbsent(PrimitiveData.nid(group.root()), group);
                    }
                }
            } catch (RuntimeException e) {
                LOG.warn("Could not read what {} stored", provider.sourceName(), e);
            }
        }
        return List.copyOf(byRoot.values());
    }

    /**
     * Counts the components of listed groups, and finds the groups none of them is.
     *
     * @param listed the groups the providers list
     * @return every group with its size: the listed ones first, then the rest
     */
    public static List<Sized> measure(List<KlStoredGroup> listed) {
        List<Sized> sized = new ArrayList<>();
        MutableLongSet members = LongSets.mutable.empty();
        for (KlStoredGroup group : listed) {
            try {
                Scope scope = Scope.of(PrimitiveData.nid(group.root()));
                members.addAll(scope.members());
                sized.add(new Sized(group, scope.members().size()));
            } catch (RuntimeException gone) {
                LOG.info("{} is no longer in the knowledge base", group.name(), gone);
            }
        }
        for (Scope leftover : DataInventory.unlisted(members)) {
            sized.add(new Sized(describe(leftover), leftover.members().size()));
        }
        return sized;
    }

    private static KlStoredGroup describe(Scope leftover) {
        for (KlStorageProvider provider : providers()) {
            try {
                Optional<KlStoredGroup> known = provider.describe(leftover);
                if (known.isPresent()) {
                    return known.get();
                }
            } catch (RuntimeException e) {
                LOG.warn("{} could not describe a group", provider.sourceName(), e);
            }
        }
        long root = leftover.roots().getFirst();
        return new KlStoredGroup(null, PrimitiveData.publicId(root), leftover.kind(), name(root), "", false, List.of());
    }

    /**
     * A component's name, for a person: its description, or its public id when it has none, and never
     * a nid, which means nothing outside the store that gave it.
     *
     * @param nid the component
     * @return its name
     */
    public static String name(long nid) {
        return PrimitiveData.textOptional(nid).orElseGet(() -> PrimitiveData.publicId(nid).asUuidList().makeString(" "));
    }

    /**
     * The root a component's group is resolved from: the pattern of a semantic written as the same
     * kind of data, as a document's own pattern is, and otherwise the component itself.
     *
     * @param component a component of shared or local data
     * @return the root
     */
    public static PublicId rootOf(PublicId component) {
        long nid = PrimitiveData.nid(component);
        if (EntityHandle.get(nid).entity().orElse(null) instanceof SemanticEntity<?> semantic
                && EntityBoundaries.kindOf(semantic.patternNid()) != DataKind.KNOWLEDGE) {
            return PrimitiveData.publicId(semantic.patternNid());
        }
        return component;
    }

    /**
     * What an export of a group adds beside its components, from every provider.
     *
     * @param root the group's root
     * @return the entries by simple file name
     */
    public static Map<String, byte[]> exportEntries(PublicId root) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        providers().forEach(provider -> entries.putAll(provider.exportEntries(root)));
        return entries;
    }

    /**
     * Tells the providers that a group was imported.
     *
     * @param imported what was imported
     * @param entries  what the file had beside the components
     * @return what it is, in words for the person, when a provider knows
     */
    public static Optional<String> imported(ScopeOperations.Imported imported, Map<String, byte[]> entries) {
        Optional<String> words = Optional.empty();
        for (KlStorageProvider provider : providers()) {
            try {
                Optional<String> said = provider.imported(imported, entries);
                words = words.isPresent() ? words : said;
            } catch (RuntimeException e) {
                LOG.warn("{} could not take in what was imported", provider.sourceName(), e);
            }
        }
        return words;
    }
}
