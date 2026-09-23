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

import java.util.HashSet;
import java.util.Set;

/**
 * Default names for new journals: {@code "Journal N"} (ike-issues#1127).
 * <p>
 * N starts at one more than the number of journals that exist now, and steps up while
 * {@code "Journal N"} is already some journal's name. So with five journals, a new one is
 * {@code "Journal 6"} unless a journal already has that name. The number is derived from the
 * journals that exist, never from a running counter, so it does not depend on the order journals
 * load in. A journal's name is display text, not its identity — the journal topic is.
 */
public final class JournalDefaultNames {

    /** The fixed text every default journal name starts with. */
    public static final String PREFIX = "Journal ";

    private JournalDefaultNames() {
    }

    /**
     * Returns the default name for a new journal, given the names of the journals that exist now.
     *
     * @param existingNames the name of every current journal, one entry per journal; a
     *                      {@code null} entry still counts as a journal
     * @return {@code "Journal N"} for the smallest N greater than the number of journals whose name
     *         no current journal has
     */
    public static String next(Iterable<String> existingNames) {
        Set<String> taken = new HashSet<>();
        int journalCount = 0;
        for (String name : existingNames) {
            journalCount++;
            if (name != null) {
                taken.add(name);
            }
        }
        int number = journalCount + 1;
        while (taken.contains(PREFIX + number)) {
            number++;
        }
        return PREFIX + number;
    }
}
