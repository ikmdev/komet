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
package dev.ikm.komet.layout.selection;

import dev.ikm.komet.framework.observable.ObservableField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Creates the per-card {@link KlSelectionContext selection nexus}. Cards obtain the nexus through this factory
 * rather than instantiating a concrete type, so the nexus can evolve (or be replaced by a richer
 * implementation) without touching the cards or drawers that use it.
 *
 * <p>{@link #provider()} returns the default implementation today; an SPI-resolved override can supersede it
 * later without changing call sites.
 */
public interface KlSelectionContextFactory {

    /**
     * Creates a fresh selection nexus.
     *
     * @return a new {@link KlSelectionContext}
     */
    KlSelectionContext create();

    /**
     * Returns the factory to use for creating selection contexts.
     *
     * @return the selection-context factory
     */
    static KlSelectionContextFactory provider() {
        return Default.INSTANCE;
    }

    /**
     * The default factory, producing a minimal property-only {@link KlSelectionContext}.
     */
    final class Default implements KlSelectionContextFactory {

        private static final Default INSTANCE = new Default();

        private Default() {
        }

        @Override
        public KlSelectionContext create() {
            return new BasicSelectionContext();
        }
    }

    /**
     * A minimal selection nexus: a single focused-feature property.
     */
    final class BasicSelectionContext implements KlSelectionContext {

        private final ObjectProperty<ObservableField.Editable<?>> focusedField =
                new SimpleObjectProperty<>(this, "focusedField");

        @Override
        public ObjectProperty<ObservableField.Editable<?>> focusedFieldProperty() {
            return focusedField;
        }
    }
}
