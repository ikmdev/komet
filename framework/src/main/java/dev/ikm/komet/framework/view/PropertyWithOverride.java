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
package dev.ikm.komet.framework.view;

import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public interface PropertyWithOverride<T> extends Property<T> {
    boolean isOverridden();

    void removeOverride();

    Property<T> overriddenProperty();

    /**
     * Stops this property following its {@linkplain #overriddenProperty() overridden property} for good,
     * removing every listener it registered there. The overridden property belongs to the parent view,
     * which outlives this one, so a discarded override view must detach its properties, or the parent's
     * properties keep the whole discarded view reachable (and whatever observes it: a closed card, for
     * one). Call only when the property is being discarded. The default does nothing, for a property that
     * registers nothing strong on its parent (a JavaFX binding's listener is weak).
     */
    default void detach() {
    }

    default T getOriginalValue() {
        return overriddenProperty().getValue();
    }

    default String getOverrideName(ViewCalculator viewCalculator) {
        return viewCalculator.toPreferredEntityStringOrInputString(getName());
    }
}
