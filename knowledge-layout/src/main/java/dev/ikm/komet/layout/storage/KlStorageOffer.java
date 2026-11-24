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
package dev.ikm.komet.layout.storage;

import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import javafx.scene.Node;

/**
 * Something that can be done with a stored group: the gesture a person makes elsewhere in Komet for
 * the same thing, with the question it asks and the report it makes.
 *
 * @param id      its identity, as the control that offers it is named
 * @param label   its label, as a button shows it
 * @param tooltip what it does, in a sentence
 * @param perform performs it
 */
public record KlStorageOffer(String id, String label, String tooltip, Perform perform) {

    /** Performs an offer. */
    @FunctionalInterface
    public interface Perform {
        /**
         * Performs the offer, asking first when the gesture does. Called on the JavaFX thread.
         *
         * @param from a node in the window the person asked from
         * @param edit the edit coordinate of the person
         * @param done told on the JavaFX thread once it is done, refused or given up, so the list can be read again
         */
        void perform(Node from, EditCoordinate edit, Runnable done);
    }
}
