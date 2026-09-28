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
package dev.ikm.komet.kview.mvvm.viewmodel.test;

import dev.ikm.komet.kview.mvvm.model.JournalNames;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import javafx.beans.property.ReadOnlyStringProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Journal names are the single source every name display follows (IKE-Network/ike-issues#1128):
 * a rename is stored, survives a restart, and reaches every observer of the journal's name; blank
 * names are rejected; names need not be unique, because a journal's identity is its topic.
 *
 * <p>The names under test are stored in a throw-away preferences node, removed afterwards, so the
 * real journals of whoever runs the build are never touched.
 */
class JournalNamesTest {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/journal-names";

    private static KometPreferences root;

    private JournalNames names;

    @BeforeAll
    static void throwAwayNode() throws BackingStoreException {
        root = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
    }

    @AfterAll
    static void removeThrowAwayNode() throws BackingStoreException {
        root.removeNode();
    }

    @BeforeEach
    void freshNames() {
        names = namesOverTestNode();
    }

    /** Names over the test node, as a newly started application would see them. */
    private static JournalNames namesOverTestNode() {
        return JournalNames.backedBy(topic -> root.node(topic.toString()));
    }

    @Test
    void aJournalNeverNamedHasTheEmptyName() {
        assertEquals("", names.name(UUID.randomUUID()));
    }

    @Test
    void aRenameIsStoredAndSurvivesARestart() {
        UUID journal = UUID.randomUUID();
        assertEquals(Optional.of("Diabetes review"), names.rename(journal, "Diabetes review"));
        assertEquals("Diabetes review", names.name(journal));
        assertEquals(Optional.of("Diabetes review"), root.node(journal.toString()).get(JOURNAL_TITLE));
        assertEquals("Diabetes review", namesOverTestNode().name(journal), "a restarted application reads the stored name");
    }

    @Test
    void aStoredNameIsReadWithoutARename() {
        UUID journal = UUID.randomUUID();
        root.node(journal.toString()).put(JOURNAL_TITLE, "Journal 4");
        assertEquals("Journal 4", names.name(journal));
    }

    @Test
    void surroundingWhitespaceIsTrimmed() {
        UUID journal = UUID.randomUUID();
        assertEquals(Optional.of("Study 2024"), names.rename(journal, "  Study 2024 \t"));
        assertEquals("Study 2024", names.name(journal));
    }

    @Test
    void blankNamesAreRejectedAndThePreviousNameKept() {
        UUID journal = UUID.randomUUID();
        names.rename(journal, "Journal 1");
        for (String blank : new String[] {null, "", "   ", "\t\n"}) {
            assertTrue(names.rename(journal, blank).isEmpty(), "rejected: [" + blank + "]");
            assertEquals("Journal 1", names.name(journal));
        }
        assertEquals("Journal 1", namesOverTestNode().name(journal), "nothing blank was stored");
    }

    @Test
    void aRenameReachesEveryObserverOfTheName() {
        UUID journal = UUID.randomUUID();
        ReadOnlyStringProperty name = names.nameProperty(journal);
        assertSame(name, names.nameProperty(journal), "one live name per journal");
        List<String> seen = new ArrayList<>();
        name.addListener((observable, oldName, newName) -> seen.add(newName));

        names.rename(journal, "Journal 1");
        names.rename(journal, "Journal 1");
        names.rename(journal, "Renal cohort");
        names.rename(journal, " ");

        assertEquals(List.of("Journal 1", "Renal cohort"), seen,
                "each real change is announced once; unchanged and rejected names are not");
    }

    @Test
    void namesNeedNotBeUnique() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        names.rename(first, "Same name");
        names.rename(second, "Same name");
        names.rename(first, "Renamed");
        assertEquals("Renamed", names.name(first));
        assertEquals("Same name", names.name(second), "journals are told apart by topic, not name");
    }

    @Test
    void forgettingAJournalRereadsItFromTheStore() {
        UUID journal = UUID.randomUUID();
        ReadOnlyStringProperty before = names.nameProperty(journal);
        names.rename(journal, "Journal 2");
        names.forget(journal);
        ReadOnlyStringProperty after = names.nameProperty(journal);
        assertFalse(before == after, "a forgotten journal gets a new live name");
        assertEquals("Journal 2", after.get());
    }
}
