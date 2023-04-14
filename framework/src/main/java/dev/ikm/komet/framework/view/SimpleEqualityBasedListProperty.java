package dev.ikm.komet.framework.view;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Collection;

/**
 * All properties that inherit from ObjectPropertyBase use object identity, rather than
 * object equality to determine if a value has changed. Since at least ConceptSpecification implementations
 * are not singletons, and can be implemented by more than one class, this optimization does not hold true.
 * <br>
 * This class overrides set and setValue to use equality rather than object identity to determine if a value has changed.
 * @param <T>
 */
public class SimpleEqualityBasedListProperty<T> extends SimpleListProperty<T> {

    public SimpleEqualityBasedListProperty() {
    }

    public SimpleEqualityBasedListProperty(ObservableList<T> initialValue) {
        super(initialValue);
    }

    public SimpleEqualityBasedListProperty(Object bean, String name) {
        super(bean, name);
    }

    public SimpleEqualityBasedListProperty(Object bean, String name, ObservableList<T> initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(ObservableList<T> newValue) {
        if (this.getValue() == null) {
            super.set(newValue);
        } else if (this.getValue().equals(newValue) == false) {
            super.set(newValue);
        }
    }

    @Override
    public boolean setAll(T... elements) {
        if (Arrays.equals(this.getValue().toArray(), elements) == false) {
            return super.setAll(elements);
        }
        return false;
    }

    @Override
    public boolean setAll(Collection<? extends T> elements) {
        if (this.getValue().equals(elements) == false) {
            return super.setAll(elements);
        }
        return false;
    }
}
