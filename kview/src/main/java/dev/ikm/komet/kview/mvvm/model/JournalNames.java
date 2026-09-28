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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * The single source of truth for journal names (ike-issues#1128). Everything that shows a journal's
 * name — the journal window's title, the name in the journal header, the landing-page journal
 * card — follows {@link #nameProperty(UUID)}, and every change goes through
 * {@link #rename(UUID, String)}, so a rename appears everywhere at once.
 * <p>
 * A journal is identified by its topic UUID, never by its name: names are display text and may
 * repeat. Today names are UI state, stored in each journal's preferences node. The seam is this
 * interface: a knowledge-base implementation, holding each journal's name as a description on a
 * concept whose public id is the journal topic, can replace the preference-backed one without
 * touching the UI.
 * <p>
 * Implementations are used from the JavaFX application thread.
 */
public interface JournalNames {

    /**
     * Returns the application's journal names, backed by each journal's preferences node under the
     * configuration root.
     *
     * @return the application-wide journal names
     */
    static JournalNames get() {
        return PreferencesJournalNames.APPLICATION;
    }

    /**
     * Returns journal names backed by the given preferences nodes, one per journal.
     *
     * @param journalPreferences resolves a journal topic to that journal's preferences node
     * @return journal names stored in those nodes
     */
    static JournalNames backedBy(Function<UUID, KometPreferences> journalPreferences) {
        return new PreferencesJournalNames(journalPreferences);
    }

    /**
     * Returns the observable name of a journal. The same property is returned for a journal until it
     * is {@linkplain #forget(UUID) forgotten}; it holds the empty string for a journal that has never
     * been named.
     *
     * @param journalTopic the journal's topic, its identity
     * @return the journal's live name
     */
    ReadOnlyStringProperty nameProperty(UUID journalTopic);

    /**
     * Returns a journal's current name.
     *
     * @param journalTopic the journal's topic, its identity
     * @return the name, or the empty string for a journal that has never been named
     */
    default String name(UUID journalTopic) {
        return nameProperty(journalTopic).get();
    }

    /**
     * Names or renames a journal and stores the name. Surrounding whitespace is trimmed. A blank
     * name is rejected and the journal keeps its previous name. Names need not be unique.
     *
     * @param journalTopic  the journal's topic, its identity
     * @param requestedName the new name; may be {@code null}
     * @return the name the journal now has, or empty when the requested name was rejected
     */
    Optional<String> rename(UUID journalTopic, String requestedName);

    /**
     * Drops what is held in memory for a journal, for use once the journal is deleted. It does not
     * delete the stored name; deleting a journal removes its whole preferences node.
     *
     * @param journalTopic the deleted journal's topic
     */
    void forget(UUID journalTopic);

    /**
     * Returns the name a requested name stands for: trimmed, and rejected when blank.
     *
     * @param requestedName a requested journal name; may be {@code null}
     * @return the trimmed name, or empty when it is {@code null} or blank
     */
    static Optional<String> acceptable(String requestedName) {
        if (requestedName == null || requestedName.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(requestedName.strip());
    }
}
