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

/**
 * A per-card selection nexus: the scene-graph channel through which a card's body tells its drawer (or an
 * in-place editor) which field is being edited, without the two sides referencing each other. A card creates
 * one via {@link KlSelectionContextFactory} and registers it on its root node; both the body (writer) and the
 * drawer (reader) discover it by walking up the scene graph, so the graph itself is the coupling — nothing
 * holds a hard pointer to the other side.
 *
 * <p>The selection is carried as the grounded, canonical {@link ObservableField.Editable} — the field of a
 * version of a component, in its editable form. The publisher (the body, which has the composer context)
 * resolves the canonical editable and sets it here; the drawer binds an editor directly to it. Because the
 * editable is canonical (one instance per field/stamp), the drawer's editor and any other view of the field
 * mutate the same instance, and the composer's transaction records the change — no value is pushed by hand.
 *
 * <p>In this first form it is deliberately just the one observable. The factory indirection lets the nexus
 * grow (multi-select, selection history, derived state) without changing how cards or drawers obtain it.
 */
public interface KlSelectionContext {

    /**
     * The focused editable field, or {@code null} when nothing is selected. The body sets it; the drawer
     * subscribes.
     *
     * @return the focused-field property
     */
    ObjectProperty<ObservableField.Editable<?>> focusedFieldProperty();

    /**
     * Returns the currently focused editable field, or {@code null}.
     *
     * @return the focused editable field
     */
    default ObservableField.Editable<?> getFocusedField() {
        return focusedFieldProperty().get();
    }

    /**
     * Sets the focused editable field.
     *
     * @param field the focused editable field, or {@code null} to clear
     */
    default void setFocusedField(ObservableField.Editable<?> field) {
        focusedFieldProperty().set(field);
    }

    /**
     * Clears the focused field (nothing selected).
     */
    default void clearFocusedField() {
        focusedFieldProperty().set(null);
    }
}
