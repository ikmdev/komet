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
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * One override property for every coordinate dimension (ike-issues#697): a single generic that replaces
 * the four {@code *PropertyWithOverride} variants. Each dimension is held as one whole immutable value
 * {@code V} — an Eclipse {@code ImmutableList}/{@code ImmutableSet}, a {@code StateSet}, a {@code Long},
 * a {@code ConceptFacade}, … — so one object-valued override covers every dimension, list dimensions
 * included (their value is an immutable list, not a mutable {@code ListProperty}).
 *
 * <p>Semantics (pin-wins, depth-independent):
 * <ul>
 *   <li><b>Not overridden</b> — {@link #get()} returns the inherited parent value, and a change to the
 *       parent fires <i>this</i> property's change event, so observers (and the composite coordinate's
 *       whole-{@code ViewCoordinateRecord} re-emission) see it. This is the propagation the cascade
 *       relies on.</li>
 *   <li><b>Overridden (pinned)</b> — {@link #get()} returns the pinned value, and parent changes are
 *       silent (the pin wins).</li>
 *   <li>A {@link #set(Object) set} equal to the parent value is not an override (it clears the pin);
 *       {@link #removeOverride()} drops the pin and reverts to inheriting the parent.</li>
 * </ul>
 *
 * <p>Change is decided by value equality, not identity (via {@link SimpleEqualityBasedObjectProperty}) —
 * so an order-significant immutable list reorder is a genuine change and fires, while an order-insensitive
 * set reshuffle is not.
 *
 * <p>This generalizes the proven {@code ObjectPropertyWithOverride} firing; it does not change that
 * behavior, it makes it the only behavior. {@code bind}/{@code bindBidirectional} are not part of an
 * override dimension's contract and are not used by any consumer; consumers reach this through a
 * value-only accessor, not the writable {@code Property} surface.
 *
 * @param <V> the immutable value type of the dimension
 */
public class OverrideOf<V> extends SimpleEqualityBasedObjectProperty<V> implements PropertyWithOverride<V> {

    /** The inherited value source — the parent dimension this one overrides. */
    private final Property<V> overriddenProperty;
    /** True while this dimension carries a pinned value distinct from the parent. */
    private boolean overridden = false;
    /** The value before the most recent change, for change-event delivery. */
    private V oldValue;
    /** True while {@link #privateSet} drives {@code super.set}; suppresses the base's own change/invalidation
     *  events so a single logical change fires exactly once (privateSet fires it explicitly). */
    private boolean settingValue;

    private HashSet<InvalidationListener> invalidationListeners;
    private HashSet<ChangeListener<? super V>> changeListeners;

    // Held as final fields (not method references at the add/remove site) so they compare equal by
    // identity and can actually be removed from the parent — the classic JavaFX listener-removal trap.
    private final ChangeListener<? super V> overriddenChangedListener = this::overriddenChanged;
    private final InvalidationListener overriddenInvalidatedListener = this::overriddenInvalidated;

    /**
     * Creates an override over {@code overriddenProperty} (the inherited parent value source).
     *
     * @param overriddenProperty the parent dimension whose value is inherited until overridden
     * @param bean               the bean owning this property
     */
    public OverrideOf(Property<V> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
    }

    @Override
    public boolean isOverridden() {
        return overridden;
    }

    @Override
    public Property<V> overriddenProperty() {
        return overriddenProperty;
    }

    @Override
    public void removeOverride() {
        privateSet(null);
    }

    @Override
    public V get() {
        return overridden ? super.get() : overriddenProperty.getValue();
    }

    @Override
    public V getValue() {
        return get();
    }

    @Override
    public void set(V newValue) {
        privateSet(newValue);
    }

    @Override
    public void setValue(V newValue) {
        privateSet(newValue);
    }

    private void privateSet(V newValue) {
        this.oldValue = get();
        this.settingValue = true;
        try {
            if (newValue == null || newValue.equals(this.overriddenProperty.getValue())) {
                // Clear the pin — either an explicit removeOverride (null) or setting the inherited value;
                // both revert to inheriting the parent.
                this.overridden = false;
                super.set(null);
            } else {
                // a genuine override
                super.set(newValue);
                this.overridden = true;
            }
        } finally {
            this.settingValue = false;
        }
        // Exactly one event per logical change (the super.set-triggered events were suppressed). This also
        // fixes the proven ObjectPropertyWithOverride, which fired twice when a pin was cleared and missed
        // the event when a pin was cleared by setting the inherited value (ike-issues#697).
        if (!Objects.equals(this.oldValue, get())) {
            invalidated();
            fireValueChangedEvent();
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        if (this.invalidationListeners == null) {
            this.invalidationListeners = new HashSet<>();
            this.overriddenProperty.addListener(this.overriddenInvalidatedListener);
        }
        this.invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        if (this.invalidationListeners != null) {
            this.invalidationListeners.remove(listener);
            if (this.invalidationListeners.isEmpty()) {
                this.overriddenProperty.removeListener(this.overriddenInvalidatedListener);
                this.invalidationListeners = null;
            }
        }
    }

    @Override
    public void addListener(ChangeListener<? super V> listener) {
        if (this.changeListeners == null) {
            this.changeListeners = new HashSet<>();
            this.overriddenProperty.addListener(this.overriddenChangedListener);
        }
        this.changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super V> listener) {
        if (this.changeListeners != null) {
            this.changeListeners.remove(listener);
            if (this.changeListeners.isEmpty()) {
                this.overriddenProperty.removeListener(this.overriddenChangedListener);
                this.changeListeners = null;
            }
        }
    }

    /** Parent value changed: when not overridden, re-emit it as this property's change (the cascade). */
    private void overriddenChanged(ObservableValue<? extends V> observable, V oldParent, V newParent) {
        if (!this.overridden) {
            this.oldValue = oldParent;
            fireValueChangedEvent();
        }
    }

    /** Parent invalidated: when not overridden, propagate the invalidation. */
    private void overriddenInvalidated(Observable observable) {
        if (!this.overridden) {
            invalidated();
        }
    }

    @Override
    protected void fireValueChangedEvent() {
        if (this.settingValue) {
            return; // suppress the base's set-triggered event; privateSet fires exactly once explicitly
        }
        V newValue = get();
        if (!Objects.equals(this.oldValue, newValue)) {
            notifyListeners(newValue);
        }
    }

    private void notifyListeners(V newValue) {
        if (this.changeListeners != null) {
            for (ChangeListener<? super V> listener : new ArrayList<>(this.changeListeners)) {
                listener.changed(this, this.oldValue, newValue);
            }
        }
    }

    @Override
    protected void invalidated() {
        if (this.settingValue) {
            return;
        }
        if (this.invalidationListeners != null) {
            for (InvalidationListener listener : new ArrayList<>(this.invalidationListeners)) {
                listener.invalidated(this);
            }
        }
    }
}
