package dev.ikm.komet.framework.view;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.*;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;

import java.util.HashSet;
import java.util.Locale;

public class LongPropertyWithOverride extends SimpleLongProperty implements PropertyWithOverride<Number> {

    private HashSet<InvalidationListener> invalidationListeners;
    private HashSet<ChangeListener<? super Number>> changeListeners;

    private final LongProperty overriddenProperty;
    private boolean overridden = false;
    private Number oldValue;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<? super Number> overriddenPropertyChangedListener = this::overriddenPropertyChanged;
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final InvalidationListener overriddenPropertyInvalidationListener = this::overriddenPropertyInvalidated;

    public LongPropertyWithOverride(LongProperty overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
    }

    @Override
    public boolean isOverridden() {
        return overridden;
    }

    @Override
    public void removeOverride() {
        this.setValue(null);
    }

    private void overriddenPropertyChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (!overridden) {
            this.oldValue = oldValue;
            fireValueChangedEvent();
        }
    }

    private void overriddenPropertyInvalidated(Observable observable) {
        if (!this.overridden) {
            invalidated();
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        if (this.invalidationListeners == null) {
            this.invalidationListeners = new HashSet<>();
            this.overriddenProperty.addListener(this.overriddenPropertyInvalidationListener);
        }
        this.invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        this.invalidationListeners.remove(listener);
        if (this.invalidationListeners.isEmpty()) {
            this.overriddenProperty.removeListener(this.overriddenPropertyInvalidationListener);
            this.invalidationListeners = null;
        }
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        if (this.changeListeners == null) {
            this.changeListeners = new HashSet<>();
            this.overriddenProperty.addListener(this.overriddenPropertyChangedListener);
        }
        this.changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        if (this.changeListeners != null) {
            this.changeListeners.remove(listener);
            if (this.changeListeners.isEmpty()) {
                this.overriddenProperty.removeListener(this.overriddenPropertyChangedListener);
                this.changeListeners = null;
            }
        }
    }

    @Override
    protected void fireValueChangedEvent() {
        Number newValue = get();
        if (this.oldValue != newValue) {
            if (this.oldValue != null) {
                if (!this.oldValue.equals(newValue)) {
                    this.changeListeners.forEach(changeListener -> changeListener.changed(this, this.oldValue, newValue));
                }
            }
        }
    }

    @Override
    protected void invalidated() {
        if (this.invalidationListeners != null) {
            this.invalidationListeners.forEach(invalidationListener ->
                    invalidationListener.invalidated(this));
        }
    }

    @Override
    public long get() {
        if (this.overridden) {
            return super.get();
        }
        return this.overriddenProperty.get();
    }

    @Override
    public void set(long newValue) {
        privateSet(newValue);
    }

    private void privateSet(long newValue) {
        this.oldValue = get();
        if (newValue == this.overriddenProperty.getValue()) {
            this.overridden = false;
            if (this.oldValue != null) {
                if (this.oldValue != null &! this.oldValue.equals(this.overriddenProperty.get())) {
                    invalidated();
                    fireValueChangedEvent();
                }
            }
        } else {
            // values not equal
            super.set(newValue);
            this.overridden = true;
            invalidated();
            fireValueChangedEvent();
        }
    }

    @Override
    public void setValue(Number v) {
        if (v == null) {
            privateSet(overriddenProperty.get());
        } else {
            privateSet(v.longValue());
        }
    }

    @Override
    public Long getValue() {
        if (this.overridden) {
            return super.getValue();
        }
        return this.overriddenProperty.getValue();
    }

    @Override
    public int intValue() {
        return getValue().intValue();
    }

    @Override
    public long longValue() {
        return get();
    }

    @Override
    public float floatValue() {
        return getValue().floatValue();
    }

    @Override
    public double doubleValue() {
        return get();
    }

    @Override
    public Property<Number> overriddenProperty() {
        return this.overriddenProperty;
    }

    @Override
    public void bind(ObservableValue<? extends Number> rawObservable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindBidirectional(Property<Number> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding negate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleBinding add(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatBinding add(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding add(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding add(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleBinding subtract(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatBinding subtract(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding subtract(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding subtract(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleBinding multiply(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatBinding multiply(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding multiply(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding multiply(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleBinding divide(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatBinding divide(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding divide(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongBinding divide(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberBinding add(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberBinding subtract(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberBinding multiply(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberBinding divide(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(ObservableNumberValue other) {
        return super.isEqualTo(other);
    }

    @Override
    public BooleanBinding isEqualTo(ObservableNumberValue other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(double other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(float other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(long other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isEqualTo(int other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(ObservableNumberValue other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(double other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(float other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(long other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding isNotEqualTo(int other, double epsilon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThan(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThan(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThan(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThan(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThan(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThan(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThan(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThan(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThan(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThan(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(ObservableNumberValue other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(double other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(float other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(long other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(int other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBinding asString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBinding asString(String format) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBinding asString(Locale locale, String format) {
        throw new UnsupportedOperationException();
    }
}
