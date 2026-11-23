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

import dev.ikm.komet.layout_engine.host.JournalWindows.OpenWindow;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How a journal makes its open windows known: under its own topic, read when asked, and gone
 * once the journal removes its registration. No JavaFX toolkit: a journal's listing is whatever
 * it supplies.
 */
class JournalWindowsTest {

    private static OpenWindow card(String title) {
        return new OpenWindow("card", title, title + " 1", null, "a.Provider", "journal_card_" + title);
    }

    @Test
    void aJournalListsItsWindowsUnderItsOwnTopicOnly() {
        UUID journal = UUID.randomUUID();
        Runnable remove = JournalWindows.register(journal, () -> List.of(card("Rules")));
        try {
            assertEquals(List.of(card("Rules")), JournalWindows.of(journal).orElseThrow());
            assertEquals(Optional.empty(), JournalWindows.of(UUID.randomUUID()), "another topic has no journal");
            assertEquals(Optional.empty(), JournalWindows.of(null));
        } finally {
            remove.run();
        }
        assertEquals(Optional.empty(), JournalWindows.of(journal), "a closed journal lists nothing");
    }

    @Test
    void theListingIsReadWhenAskedSoItFollowsTheWorkspace() {
        UUID journal = UUID.randomUUID();
        List<OpenWindow> workspace = new ArrayList<>();
        Runnable remove = JournalWindows.register(journal, () -> workspace);
        try {
            assertTrue(JournalWindows.of(journal).orElseThrow().isEmpty());
            workspace.add(card("Instruction Editor"));
            assertEquals(List.of(card("Instruction Editor")), JournalWindows.of(journal).orElseThrow());
        } finally {
            remove.run();
        }
    }

    @Test
    void removingAnEarlierRegistrationLeavesTheLaterOne() {
        UUID journal = UUID.randomUUID();
        Runnable first = JournalWindows.register(journal, () -> List.of(card("First")));
        Runnable second = JournalWindows.register(journal, () -> List.of(card("Second")));
        try {
            first.run();
            assertEquals(List.of(card("Second")), JournalWindows.of(journal).orElseThrow(),
                    "a journal reopened under the same topic keeps its listing when the old one closes late");
        } finally {
            second.run();
        }
    }

    @Test
    void aWindowWithNothingToSayHasEmptyTextsAndNoComponent() {
        OpenWindow window = new OpenWindow(null, null, null, null, null, null);
        assertEquals(new OpenWindow("", "", "", List.of(), "", ""), window);
    }
}
