package dev.ikm.komet.layout.preferences;

import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class PreferenceProperty<T, I extends Property<T>> implements Property<T>, ClassConceptBinding {
    public static final String INITIAL_STRING_VALUE = "uninitialized";
    public static final boolean INITIAL_BOOLEAN_VALUE = false;
    public static final int INITIAL_INTEGER_VALUE = Integer.MIN_VALUE;

    final I implInstance;
    final ClassConceptBinding binding;

    protected PreferenceProperty(I implInstance, ClassConceptBinding binding) {
        this.implInstance = implInstance;
        this.binding = binding;
    }

    public final I impl() {
        return implInstance;
    }

    public static PreferencePropertyString stringProp(ClassConceptBinding binding) {
        return PreferencePropertyString.create(binding);
    }

    public static PreferencePropertyInteger integerProp(ClassConceptBinding binding) {
        return PreferencePropertyInteger.create(binding);
    }

    public static PreferencePropertyBoolean booleanProp(ClassConceptBinding binding) {
        return PreferencePropertyBoolean.create(binding);
    }

    @Override
    public void bind(ObservableValue<? extends T> observable) {
        this.implInstance.bind(observable);
    }

    @Override
    public void unbind() {
        this.implInstance.unbind();
    }

    @Override
    public boolean isBound() {
        return this.implInstance.isBound();
    }

    @Override
    public void setValue(T value) {
        this.implInstance.setValue(value);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        this.implInstance.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        this.implInstance.removeListener(listener);
    }

    @Override
    public T getValue() {
        return this.implInstance.getValue();
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        this.implInstance.bindBidirectional(other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        this.implInstance.unbindBidirectional(other);
    }

    @Override
    public Object getBean() {
        return this.implInstance.getBean();
    }

    @Override
    public String getName() {
        // TODO make this work to get right name and dialect with view coordinate.
        return this.binding.regularNames().get(0);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        this.implInstance.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        this.implInstance.removeListener(listener);
    }
}
