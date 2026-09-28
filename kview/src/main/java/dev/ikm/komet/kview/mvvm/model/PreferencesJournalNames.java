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

import dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;

/**
 * Journal names stored as UI state: each journal's name is the {@code JOURNAL_TITLE} value in its
 * preferences node (ike-issues#1128). This class is the only reader and writer of that value; it
 * stores the current name only, with no history.
 */
final class PreferencesJournalNames implements JournalNames {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesJournalNames.class);

    /** The application's journal names, in each journal's node under the configuration root. */
    static final PreferencesJournalNames APPLICATION =
            new PreferencesJournalNames(KlWindowPreferencesUtils::getJournalPreferences);

    private final Function<UUID, KometPreferences> journalPreferences;

    /** One live name per journal, loaded from the journal's node on first use. */
    private final Map<UUID, ReadOnlyStringWrapper> names = new HashMap<>();

    /**
     * Creates journal names stored in the given per-journal preferences nodes.
     *
     * @param journalPreferences resolves a journal topic to that journal's preferences node
     */
    PreferencesJournalNames(Function<UUID, KometPreferences> journalPreferences) {
        this.journalPreferences = Objects.requireNonNull(journalPreferences, "journalPreferences");
    }

    @Override
    public ReadOnlyStringProperty nameProperty(UUID journalTopic) {
        return liveName(journalTopic).getReadOnlyProperty();
    }

    @Override
    public Optional<String> rename(UUID journalTopic, String requestedName) {
        Optional<String> accepted = JournalNames.acceptable(requestedName);
        if (accepted.isEmpty()) {
            return Optional.empty();
        }
        String name = accepted.get();
        ReadOnlyStringWrapper liveName = liveName(journalTopic);
        if (!name.equals(liveName.get())) {
            KometPreferences node = journalPreferences.apply(journalTopic);
            node.put(JOURNAL_TITLE, name);
            try {
                node.flush();
            } catch (BackingStoreException e) {
                // The value stays in the node and is written with the node's next flush.
                LOG.warn("Journal {} renamed to '{}', but the name could not be flushed yet",
                        journalTopic, name, e);
            }
            liveName.set(name);
        }
        return Optional.of(name);
    }

    @Override
    public void forget(UUID journalTopic) {
        names.remove(journalTopic);
    }

    private ReadOnlyStringWrapper liveName(UUID journalTopic) {
        Objects.requireNonNull(journalTopic, "journalTopic");
        return names.computeIfAbsent(journalTopic, topic -> new ReadOnlyStringWrapper(
                this, "name", journalPreferences.apply(topic).get(JOURNAL_TITLE).orElse("")));
    }
}
