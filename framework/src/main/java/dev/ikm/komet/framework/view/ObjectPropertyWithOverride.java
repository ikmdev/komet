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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.HashSet;

public class ObjectPropertyWithOverride<T> extends SimpleEqualityBasedObjectProperty<T>
    implements PropertyWithOverride<T> {

    private HashSet<InvalidationListener> invalidationListeners;
    private HashSet<ChangeListener<? super T>> changeListeners;

    private final ObjectProperty<T> overriddenProperty;
    private T oldValue;
    private boolean overridden = false;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<? super T> overriddenPropertyChangedListener = this::overriddenPropertyChanged;
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final InvalidationListener overriddenPropertyInvalidationListener = this::overriddenPropertyInvalidated;

    public ObjectPropertyWithOverride(ObjectProperty<T> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
    }

    @Override
    public boolean isOverridden() {
        return overridden;
    }

    @Override
    public void removeOverride() {
        this.set(null);
    }

    @Override
    public Property<T> overriddenProperty() {
        return this.overriddenProperty;
    }

    @Override
    public T get() {
        if (this.overridden) {
            return super.get();
        }
        return this.overriddenProperty.get();
    }

    @Override
    public void set(T newValue) {
        privateSet(newValue);
    }

    private void privateSet(T newValue) {
        this.oldValue = get();
        if (newValue == null) {
            this.overridden = false;
            if (this.oldValue != null) {
                super.set(null);
                if (this.oldValue != null &! this.oldValue.equals(this.overriddenProperty.get())) {
                    invalidated();
                    fireValueChangedEvent();
                }
            }
        } else if (newValue.equals(this.overriddenProperty.get())) {
            // values equal so not an override.
            this.overridden = false;
            super.set(null);
        } else {
            // values not equal
             super.set(newValue);
            this.overridden = true;
            invalidated();
            fireValueChangedEvent();
        }
    }

    @Override
    public void setValue(T v) {
        privateSet(v);
    }

    @Override
    public T getValue() {
        if (this.overridden) {
            return super.getValue();
        }
        return this.overriddenProperty.getValue();
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
    public void addListener(ChangeListener<? super T> listener) {
        if (this.changeListeners == null) {
            this.changeListeners = new HashSet<>();
            this.overriddenProperty.addListener(this.overriddenPropertyChangedListener);
        }
        this.changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        if (this.changeListeners != null) {
            this.changeListeners.remove(listener);
            if (this.changeListeners.isEmpty()) {
                this.overriddenProperty.removeListener(this.overriddenPropertyChangedListener);
                this.changeListeners = null;
            }
        }
    }

    private void overriddenPropertyChanged(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        if (!overridden) {
            this.oldValue = oldValue;
            fireValueChangedEvent();
        }
    }

    @Override
    protected void fireValueChangedEvent() {
        T newValue = get();
        if (this.oldValue != newValue) {
            if (this.oldValue != null) {
                if (!this.oldValue.equals(newValue)) {
                    notify(newValue);
                }
            } else {
                notify(newValue);
            }
        }
    }

    private void notify(T newValue) {
        if (this.changeListeners != null) {
            ArrayList<ChangeListener<? super T>> listenerList = new ArrayList<>(this.changeListeners);
            listenerList.forEach(changeListener -> changeListener.changed(this, this.oldValue, newValue));
        }
    }

    private void overriddenPropertyInvalidated(Observable observable) {
        if (!this.overridden) {
            invalidated();
        }
    }

    @Override
    protected void invalidated() {
        HashSet<InvalidationListener> listeners = this.invalidationListeners;
        if (listeners != null) {
            listeners.forEach(invalidationListener ->
                    invalidationListener.invalidated(this));
        }
    }


}
