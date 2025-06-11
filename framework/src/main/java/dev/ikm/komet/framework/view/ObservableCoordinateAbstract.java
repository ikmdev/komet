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

import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The class that actually makes the ImmutableCoordinate of the "Observable" coordinates Observable.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * 
 */
public abstract class ObservableCoordinateAbstract<T extends ImmutableCoordinate> implements ObservableCoordinate<T> {
    protected static final Logger LOG = LoggerFactory.getLogger(ObservableCoordinateAbstract.class);
    /**
     * Since immutable coordinates are singletons, using SimpleEqualityBasedObjectProperty is not necessary, and
     *  inefficient.
     */
    private final SimpleObjectProperty<T> immutableCoordinate;

    private final List<ChangeListener<? super T>> changeListenerList = new ArrayList<>();

    protected ObservableCoordinateAbstract(T immutableCoordinate, String coordinateName) {
        this.immutableCoordinate = new SimpleObjectProperty<>(this, coordinateName);
        this.immutableCoordinate.setValue(immutableCoordinate);
    }

    SimpleObjectProperty<T> baseCoordinateProperty() {
        return immutableCoordinate;
    }
    protected abstract void removeListeners();

    protected abstract void addListeners();

    protected void changeBaseCoordinate(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        // TODO look into another way to not have to physically remove then add the listeners
        // the protected instance variable listening
        removeListeners();
        this.immutableCoordinate.setValue(baseCoordinateChangedListenersRemoved(observable, oldValue, newValue));
        addListeners();
    }

    /**
     *
     * @param observable
     * @param oldValue
     * @param newValue
     * @return A final base coordinate with overrides applied.
     */
    protected abstract T baseCoordinateChangedListenersRemoved(ObservableValue<? extends T> observable, T oldValue, T newValue);

    @Override
    public void setValue(T value) {
        T oldValue = getValue();
        if (!Objects.equals(oldValue, value)) {
            changeBaseCoordinate(this.immutableCoordinate, oldValue, value);

            // copy the listener list to avoid the ConcurrentModificationException
            var copiedListenerList = new ArrayList<ChangeListener<? super T>>(changeListenerList);

            var iterator = copiedListenerList.iterator();
            while (iterator.hasNext()) {
                iterator.next().changed(immutableCoordinate, oldValue, value);
            }
        }
    }

    @Override
    public T getValue() {
        return this.immutableCoordinate.getValue();
    }


    @Override
    public void addListener(ChangeListener<? super T> listener) {
        this.changeListenerList.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        this.changeListenerList.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        this.immutableCoordinate.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        this.immutableCoordinate.removeListener(listener);
    }

    @Override
    public void bind(ObservableValue<? extends T> observable) {
        this.immutableCoordinate.bind(observable);
    }

    @Override
    public void unbind() {
        this.immutableCoordinate.unbind();
    }

    @Override
    public boolean isBound() {
        return this.immutableCoordinate.isBound();
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        this.immutableCoordinate.bindBidirectional(other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        this.immutableCoordinate.unbindBidirectional(other);
    }

    @Override
    public Object getBean() {
        return this.immutableCoordinate.getBean();
    }

    @Override
    public String getName() {
        return this.immutableCoordinate.getName();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n" +
                getValue().toString() +
                "\n}";
    }
}
