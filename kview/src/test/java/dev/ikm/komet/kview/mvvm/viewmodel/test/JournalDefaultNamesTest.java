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

import dev.ikm.komet.kview.mvvm.model.JournalDefaultNames;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Default journal numbering (IKE-Network/ike-issues#1127): a new journal is "Journal N", with N
 * starting at one more than the number of journals and stepping up while that name is taken. The
 * replaced counter was set once per journal as journals loaded, so the last journal loaded won; and
 * numbering from the highest default name gave "Journal 3" with five journals when only
 * "Journal 1" and "Journal 2" still had default names.
 */
class JournalDefaultNamesTest {

    @Test
    void firstJournalIsJournalOne() {
        assertEquals("Journal 1", JournalDefaultNames.next(List.of()));
    }

    @Test
    void numberingStartsPastTheJournalCount() {
        // Five journals, three renamed: the new one is the sixth, not "Journal 3".
        assertEquals("Journal 6", JournalDefaultNames.next(List.of(
                "Journal 1", "Journal 2", "Platlet", "5 window journal", "Renal cohort")));
        assertEquals("Journal 3", JournalDefaultNames.next(List.of("Diabetes review", "Study 2024")));
    }

    @Test
    void aTakenNameIsSteppedPast() {
        assertEquals("Journal 4", JournalDefaultNames.next(List.of("Journal 3", "Journal 2")));
        assertEquals("Journal 5", JournalDefaultNames.next(List.of("Journal 2", "Journal 3", "Journal 4")));
    }

    @Test
    void loadOrderDoesNotMatter() {
        assertEquals("Journal 4", JournalDefaultNames.next(List.of("Journal 7", "Journal 3", "Journal 2")));
        assertEquals("Journal 4", JournalDefaultNames.next(List.of("Journal 2", "Journal 3", "Journal 7")));
    }

    @Test
    void onlyAnExactNameIsTaken() {
        assertEquals("Journal 3", JournalDefaultNames.next(List.of("journal 3", "Journal 3 draft")));
    }

    @Test
    void aJournalWithNoNameStillCounts() {
        assertEquals("Journal 3", JournalDefaultNames.next(Arrays.asList(null, "Journal 1")));
    }
}
