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
package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.layout.Region;

/**
 * A draggable and stylable region that indicates a drop target in a Komet workspace.
 * <p>
 * This class can be used to represent areas where items (e.g., concept panels) can be
 * dragged and dropped. By default, it uses a pseudo-class to toggle between two visual
 * states: {@code BOX} or {@code LINE}.
 */
public class KLDropRegion extends Region {

    /**
     * The default style class for this region, used for CSS styling.
     */
    private static final String DEFAULT_STYLE_CLASS = "drop-region";

    /**
     * The default drop region type, set to {@link Type#BOX}.
     */
    private static final Type DEFAULT_TYPE = Type.BOX;

    /**
     * A pseudo-class for the "box" visual state.
     */
    private static final PseudoClass BOX_PSEUDO_CLASS = PseudoClass.getPseudoClass("box");

    /**
     * A pseudo-class for the "line" visual state.
     */
    private static final PseudoClass LINE_PSEUDO_CLASS = PseudoClass.getPseudoClass("line");

    /**
     * Minimum width of concept panels placed in the workspace.
     */
    private static final double MIN_WIDTH = 672.0;

    /**
     * Minimum height of concept panels placed in the workspace.
     */
    private static final double MIN_HEIGHT = 427.0;

    /**
     * Maximum height of concept panels placed in the workspace.
     */
    private static final double MAX_HEIGHT = 940.0;

    /**
     * An enumeration of drop region types: {@code BOX} or {@code LINE}.
     * <ul>
     *     <li>{@code BOX} - indicates a box-like drop target</li>
     *     <li>{@code LINE} - indicates a line-like drop target</li>
     * </ul>
     */
    public enum Type {
        BOX, LINE
    }

    /**
     * Constructs a new {@code KLDropRegion} with default styling and the default
     * {@link Type#BOX} pseudo-class state.
     */
    public KLDropRegion() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        updatePseudoClass(getType());
    }

    // -----------------------------------------------------------------------------------------
    // Type Property
    // -----------------------------------------------------------------------------------------

    private ObjectProperty<Type> type;

    /**
     * Returns the current drop region type.
     *
     * @return the {@link Type} of this drop region
     */
    public Type getType() {
        return (type == null) ? DEFAULT_TYPE : type.get();
    }

    /**
     * Sets the drop region type, switching the associated pseudo-classes appropriately.
     *
     * @param type the new {@link Type} for this drop region
     */
    public void setType(Type type) {
        typeProperty().set(type);
    }

    /**
     * An {@link ObjectProperty} that holds the current {@link Type} of this drop region.
     * <p>
     * Changes in this property will trigger updates to the CSS pseudo-classes.
     *
     * @return the property storing this region's {@link Type}
     */
    public ObjectProperty<Type> typeProperty() {
        if (type == null) {
            type = new SimpleObjectProperty<>(this, "type", DEFAULT_TYPE) {
                @Override
                protected void invalidated() {
                    updatePseudoClass(get());
                }
            };
        }
        return type;
    }

    /**
     * Updates the pseudo-class state based on the specified {@link Type}.
     *
     * @param type the new {@link Type} to apply
     */
    private void updatePseudoClass(Type type) {
        // Disable both pseudo-classes first.
        pseudoClassStateChanged(BOX_PSEUDO_CLASS, false);
        pseudoClassStateChanged(LINE_PSEUDO_CLASS, false);

        // Enable the relevant pseudo-class.
        switch (type) {
            case BOX  -> pseudoClassStateChanged(BOX_PSEUDO_CLASS, true);
            case LINE -> pseudoClassStateChanged(LINE_PSEUDO_CLASS, true);
        }
    }
}
