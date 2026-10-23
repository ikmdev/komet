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

import javafx.scene.layout.Region;

/**
 * Shows a help book: a topic at a time, with its contents and a search over it. The card's help
 * drawer hosts one; a plugin supplies the renderer as a {@link Factory} service, and without one
 * the topic's source is shown as text.
 */
public interface KlHelpViewer {

    /** @return the viewer's node, which the drawer reveals */
    Region fxObject();

    /**
     * Shows a topic of the book.
     *
     * @param topicId the topic's identifier within the book
     */
    void show(String topicId);

    /** Makes a viewer for a book; discovered as a service. */
    interface Factory {

        /**
         * Makes a viewer.
         *
         * @param book the book it shows
         * @return the viewer
         */
        KlHelpViewer create(KlHelpProvider book);
    }
}
