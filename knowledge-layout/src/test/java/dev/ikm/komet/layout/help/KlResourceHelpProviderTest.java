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
package dev.ikm.komet.layout.help;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads the test book in {@code src/test/resources/META-INF/komet-help/test-book}: its contents
 * from {@code nav.adoc} and its topics' sources.
 */
class KlResourceHelpProviderTest {

    /** The test book, read from this module's resources like a plugin's. */
    private static final class TestBook extends KlResourceHelpProvider {
        TestBook() {
            super("test-book", "Test book");
        }
    }

    private final TestBook book = new TestBook();

    @Test
    void contentsFollowTheNavigationFileInOrderAndDepth() {
        assertEquals(List.of(
                        new KlHelpProvider.Entry("first", "The first topic", 1),
                        new KlHelpProvider.Entry("second", "The second topic", 2),
                        new KlHelpProvider.Entry("missing", "Missing title", 1)),
                book.contents());
    }

    @Test
    void anEntryWithoutTextTakesItsTopicsTitle() {
        assertEquals("The second topic", book.contents().get(1).title());
    }

    @Test
    void aTopicsSourceIsItsAsciiDoc() {
        assertTrue(book.source("first").orElseThrow().contains("xref:second.adoc[the second]"));
        assertEquals(Optional.empty(), book.source("missing"));
    }

    @Test
    void theConventionalRootIsUnderMetaInf() {
        assertEquals("META-INF/komet-help/test-book/", KlResourceHelpProvider.defaultRoot("test-book"));
        assertEquals(Optional.of("Title"), KlResourceHelpProvider.documentTitle(":toc:\n= Title\n\nText"));
        assertEquals(Optional.empty(), KlResourceHelpProvider.documentTitle("== Section only"));
    }
}
