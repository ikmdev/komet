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

import dev.ikm.tinkar.common.service.PluggableService;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

import java.util.Optional;

/**
 * Finds help: the book a {@link KlHelpProvider} answers for, and the viewer a
 * {@link KlHelpViewer.Factory} makes, both discovered as services through
 * {@link PluggableService}, so plugins in their own layers are found too.
 */
public final class KlHelp {

    private KlHelp() {
    }

    /**
     * The book with the given identifier.
     *
     * @param bookId the book's identifier
     * @return the book, or empty when no provider answers for it
     */
    public static Optional<KlHelpProvider> book(String bookId) {
        for (KlHelpProvider provider : PluggableService.load(KlHelpProvider.class)) {
            if (provider.bookId().equals(bookId)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    /**
     * A viewer for a book: the first viewer service's, or one that shows a topic's source as text
     * when no plugin provides a viewer.
     *
     * @param book the book
     * @return the viewer
     */
    public static KlHelpViewer viewer(KlHelpProvider book) {
        return PluggableService.load(KlHelpViewer.Factory.class).findFirst()
                .map(factory -> factory.create(book))
                .orElseGet(() -> new SourceViewer(book));
    }

    /** The fallback viewer: a topic's AsciiDoc source, selectable, in a read-only text area. */
    private static final class SourceViewer implements KlHelpViewer {
        private final KlHelpProvider book;
        private final TextArea text = new TextArea();

        SourceViewer(KlHelpProvider book) {
            this.book = book;
            text.setEditable(false);
            text.setWrapText(true);
        }

        @Override
        public Region fxObject() {
            return text;
        }

        @Override
        public void show(String topicId) {
            text.setText(book.source(topicId).orElse("The book " + book.title() + " has no topic " + topicId + "."));
        }
    }
}
