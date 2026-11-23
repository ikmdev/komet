/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout_engine.host;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The windows open on each journal's workspace, for what asks about them from outside the journal,
 * such as the Komet Assistant answering whether an editor is open. Each open journal registers how
 * to list its windows under its journal topic and removes the registration when it closes, so the
 * asker never sees the journal controller: the same inversion as {@link MakeCardWindowEvent}, which
 * asks a journal to open a window.
 *
 * <p>The list is read on the JavaFX application thread, where the workspace changes.
 */
public final class JournalWindows {

    /** Lists each open journal's windows, by journal topic. */
    private static final Map<UUID, Supplier<List<OpenWindow>>> JOURNALS = new ConcurrentHashMap<>();

    /**
     * One window open on a journal's workspace.
     *
     * @param kind      what the window shows: {@code card} for a card a plugin or tool contributes,
     *                  {@code concept}, {@code pattern} or {@code semantic} for a component's window,
     *                  {@code layout} for a window made from a designed layout, or the window type's
     *                  name for any other
     * @param title     the title on the window's tab, or empty when the window shows a component and
     *                  takes its title from it
     * @param label     the name the journal's Tiles menu lists the window under, or empty when it has none
     * @param component the UUIDs of the component the window shows, or empty when it shows none
     * @param provider  the {@link KlCardProvider} class name of a card window, or empty
     * @param tile      the name of the window's preferences node, which
     *                  {@link MakeCardWindowEvent#FOCUS_OR_REOPEN} takes for a card window
     */
    public record OpenWindow(String kind, String title, String label, List<UUID> component, String provider,
                             String tile) {

        /** Copies the component's UUIDs and turns a missing text into an empty one. */
        public OpenWindow {
            kind = kind == null ? "" : kind;
            title = title == null ? "" : title;
            label = label == null ? "" : label;
            component = component == null ? List.of() : List.copyOf(component);
            provider = provider == null ? "" : provider;
            tile = tile == null ? "" : tile;
        }
    }

    private JournalWindows() {
    }

    /**
     * Registers how a journal lists the windows open on its workspace, replacing any earlier
     * registration under the same topic.
     *
     * @param journalTopic the journal's topic
     * @param windows      lists the windows, in the order the workspace has them; called on the JavaFX
     *                     application thread
     * @return removes this registration, and only this one; run it when the journal closes
     */
    public static Runnable register(UUID journalTopic, Supplier<List<OpenWindow>> windows) {
        JOURNALS.put(journalTopic, windows);
        return () -> JOURNALS.remove(journalTopic, windows);
    }

    /**
     * The windows open on a journal's workspace. Call it on the JavaFX application thread.
     *
     * @param journalTopic the journal's topic
     * @return the windows, or empty when no open journal has that topic
     */
    public static Optional<List<OpenWindow>> of(UUID journalTopic) {
        if (journalTopic == null) {
            return Optional.empty();
        }
        Supplier<List<OpenWindow>> windows = JOURNALS.get(journalTopic);
        return windows == null ? Optional.empty() : Optional.of(List.copyOf(windows.get()));
    }
}
