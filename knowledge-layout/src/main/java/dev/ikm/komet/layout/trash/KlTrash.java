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
package dev.ikm.komet.layout.trash;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;
import dev.ikm.tinkar.common.service.PluggableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

/**
 * Komet's Trash (KEC 2026-10-23): everything the {@link KlTrashProvider} services hold, listed,
 * put back and deleted as one, with the preference to empty it when Komet quits. The journal's
 * sidebar Trash shows it; {@link KlTrashLifecycle} empties it on quit.
 */
public final class KlTrash {

    private static final Logger LOG = LoggerFactory.getLogger(KlTrash.class);

    /** Per-user preference: empty the Trash when Komet quits (on unless turned off). */
    public static final String PREF_EMPTY_ON_QUIT = "komet.trash.emptyOnQuit";

    /**
     * What deleting did.
     *
     * @param deleted  how many items were deleted
     * @param problems what could not be deleted, one line each; those items stay in the Trash
     */
    public record Deleted(int deleted, List<String> problems) {

        public Deleted {
            problems = List.copyOf(problems);
        }

        /**
         * @param other another result
         * @return the two together
         */
        public Deleted plus(Deleted other) {
            List<String> all = new ArrayList<>(problems);
            all.addAll(other.problems());
            return new Deleted(deleted + other.deleted(), all);
        }
    }

    private KlTrash() {
    }

    /** @return the providers, as the service loader finds them */
    public static List<KlTrashProvider> providers() {
        List<KlTrashProvider> providers = new ArrayList<>();
        try {
            for (KlTrashProvider provider : PluggableService.load(KlTrashProvider.class)) {
                providers.add(provider);
            }
        } catch (RuntimeException e) {
            LOG.warn("Could not load the Trash providers", e);
        }
        return providers;
    }

    /** @return everything in the Trash, most recently trashed first */
    public static List<KlTrashItem> items() {
        List<KlTrashItem> items = new ArrayList<>();
        for (KlTrashProvider provider : providers()) {
            try {
                items.addAll(provider.items());
            } catch (RuntimeException e) {
                LOG.warn("Could not read the Trash of {}", provider.sourceName(), e);
            }
        }
        items.sort(Comparator.comparingLong(KlTrashItem::trashedAt).reversed().thenComparing(KlTrashItem::name));
        return items;
    }

    /**
     * Puts items back, each through its provider.
     *
     * @param items the items
     */
    public static void putBack(List<KlTrashItem> items) {
        byProvider(items).forEach((provider, mine) -> provider.putBack(mine));
    }

    /**
     * Deletes items for good, each through its provider.
     *
     * @param items the items
     * @return what was done
     */
    public static Deleted delete(List<KlTrashItem> items) {
        Deleted all = new Deleted(0, List.of());
        for (Map.Entry<KlTrashProvider, List<KlTrashItem>> mine : byProvider(items).entrySet()) {
            try {
                all = all.plus(mine.getKey().delete(mine.getValue()));
            } catch (RuntimeException e) {
                LOG.warn("Could not delete from the Trash of {}", mine.getKey().sourceName(), e);
                all = all.plus(new Deleted(0, List.of(mine.getKey().sourceName() + ": " + e.getMessage())));
            }
        }
        return all;
    }

    /**
     * Empties the Trash.
     *
     * @return what was done
     */
    public static Deleted empty() {
        return delete(items());
    }

    /** @return whether the Trash is emptied when Komet quits */
    public static boolean emptyOnQuit() {
        return !"false".equals(PreferencesService.userPreferences().get(PREF_EMPTY_ON_QUIT, "true"));
    }

    /**
     * Sets whether the Trash is emptied when Komet quits.
     *
     * @param empty whether to empty it
     */
    public static void setEmptyOnQuit(boolean empty) {
        KometPreferences user = PreferencesService.userPreferences();
        user.put(PREF_EMPTY_ON_QUIT, Boolean.toString(empty));
        try {
            user.sync();
        } catch (BackingStoreException e) {
            LOG.warn("Could not save the Trash preference", e);
        }
    }

    /**
     * Listens for changes to the Trash, from every provider.
     *
     * @param listener called after each change, on any thread
     */
    public static void onChange(Runnable listener) {
        providers().forEach(provider -> provider.onChange(listener));
    }

    /**
     * Stops listening.
     *
     * @param listener a listener given to {@link #onChange}
     */
    public static void stopListening(Runnable listener) {
        providers().forEach(provider -> provider.stopListening(listener));
    }

    private static Map<KlTrashProvider, List<KlTrashItem>> byProvider(List<KlTrashItem> items) {
        Map<KlTrashProvider, List<KlTrashItem>> grouped = new LinkedHashMap<>();
        for (KlTrashItem item : items) {
            grouped.computeIfAbsent(item.provider(), p -> new ArrayList<>()).add(item);
        }
        return grouped;
    }
}
