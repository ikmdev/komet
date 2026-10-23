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

import java.util.List;
import java.util.Optional;

/**
 * A plugin's help: the book of topics that explain its cards, discovered as a service (a
 * {@code provides KlHelpProvider with …} clause), so a card names a topic and the framework finds
 * whichever plugin answers for it, without knowing where or how that plugin keeps its help.
 *
 * <p>A topic is AsciiDoc in the IKE dialect, the same source the plugin's documentation is
 * written in; a {@link KlHelpViewer} renders it. {@link KlResourceHelpProvider} keeps the topics
 * as resources in the plugin's own jar, so they are deployed, versioned and downloaded with it.
 */
public interface KlHelpProvider {

    /**
     * One line of the book's contents.
     *
     * @param topicId the topic's identifier within the book
     * @param title   the title shown for it
     * @param depth   its nesting under the entries before it, 1 for a top-level entry
     */
    record Entry(String topicId, String title, int depth) {
    }

    /** @return the book's identifier, which a card's {@link KlHelpTopic} names */
    String bookId();

    /** @return the book's title, shown above its contents */
    String title();

    /** @return the book's topics in reading order */
    List<Entry> contents();

    /**
     * The AsciiDoc source of a topic.
     *
     * @param topicId the topic's identifier within the book
     * @return its source, or empty when the book has no such topic
     */
    Optional<String> source(String topicId);
}
