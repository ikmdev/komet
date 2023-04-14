package dev.ikm.komet.framework.view;

import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public interface PropertyWithOverride<T> extends Property<T> {
    boolean isOverridden();

    void removeOverride();

    Property<T> overriddenProperty();

    default T getOriginalValue() {
        return overriddenProperty().getValue();
    }

    default String getOverrideName(ViewCalculator viewCalculator) {
        return viewCalculator.toPreferredEntityStringOrInputString(getName());
    }
}
