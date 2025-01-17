package dev.ikm.komet.layout.preferences;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

public class PreferenceProperty<T> implements Property<T> {
    final Property<T> property;

    private PreferenceProperty(Property<T> property) {
        this.property = property;
    }

    public static Property<String> stringProperty() {
        PreferenceProperty stringProperty = new PreferenceProperty<>(new SimpleStringProperty());
        return stringProperty;
    }

    @Override
    public void bind(javafx.beans.value.ObservableValue<? extends T> observable) {
        property.bind(observable);
    }

    @Override
    public void unbind() {
        property.unbind();
    }

    @Override
    public boolean isBound() {
        return property.isBound();
    }

    @Override
    public void setValue(T value) {
        property.setValue(value);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        property.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        property.removeListener(listener);
    }

    @Override
    public T getValue() {
        return property.getValue();
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        property.bindBidirectional(other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        property.unbindBidirectional(other);
    }

    @Override
    public Object getBean() {
        return property.getBean();
    }

    @Override
    public String getName() {
        return property.getName();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        property.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        property.removeListener(listener);
    }
}
