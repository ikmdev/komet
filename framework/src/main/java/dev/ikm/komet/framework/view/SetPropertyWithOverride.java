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
import javafx.beans.property.SetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class SetPropertyWithOverride <T> extends SimpleEqualityBasedSetProperty<T>
    implements PropertyWithOverride<ObservableSet<T>> {

    private final SetProperty<T> overriddenProperty;
    private boolean overridden = false;


    public SetPropertyWithOverride(SetProperty<T> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
        this.bind(overriddenProperty);
    }

    @Override
    public Property<ObservableSet<T>> overriddenProperty() {
        return this.overriddenProperty;
    }

    @Override
    public boolean isOverridden() {
        return overridden;
    }

    @Override
    public void removeOverride() {
        this.set(null);
    }

    /// called by ViewMenuTask when view coordinate Set.setValue() is called
    @Override
    public void set(ObservableSet<T> newValue) {
        if (newValue == null || Objects.equals(newValue, this.overriddenProperty.get())) {
            // values equal so not an override.
            this.overridden = false;
            this.bind(overriddenProperty);
        } else {
            // values not equal
            if (!overridden) {
                this.overridden = true;
                this.unbind();
            }
            super.set(newValue);
        }
    }

    @Override
    public boolean setAll(T... elements) {
        return setAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(Collection<? extends T> elements) {
        if (!Objects.equals(this.get(), elements)) {
            if (!overridden) {
                overridden = true;
                this.unbind();
            }
            return super.setAll(elements);
        } else {
            overridden = false;
            this.bind(overriddenProperty);
        }

        return false;
    }

    /// called by ViewMenuTask when view coordinate Set is removed from
    @Override
    public boolean remove(Object obj) {
        boolean returnValue = false;

        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            returnValue = set.remove(obj);
            super.set(set);
            return returnValue;
        } else {
            // must make a copy to remove the object from the copied set
            // to use to compare with the overriddenProperty FIRST

            HashSet<T> copiedSet = new HashSet<>(get());
            returnValue = copiedSet.remove(obj);

            if (Objects.equals(copiedSet, this.overriddenProperty.get())) {
                overridden = false;
                this.bind(overriddenProperty);
            } else {
                // remove() calls the Set property listeners, which updates the view coordinate menu
                returnValue = super.remove(obj);
            }
        }

        return returnValue;
    }

    /**
     * called by ViewMenuTask when view coordinate Set is added to
     * <p>
     * Note:  cannot use the parent's overriddenProperty because doing so causes the
     * parent property to be changed when the child property is changed
     *
     * see https://openjfx.io/javadoc/21/javafx.base/javafx/collections/FXCollections.html#observableSet(java.util.Set)
     */
    @Override
    public boolean add(T element) {
        boolean returnValue = false;

        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            returnValue = set.add(element);
            super.set(set);

            return returnValue;
        } else {
            // must make a copy to add the object to the copied set
            // to use to compare with the overriddenProperty FIRST

            HashSet<T> copiedSet = new HashSet<>(get());
            returnValue = copiedSet.add(element);

            if (Objects.equals(this.get(), this.overriddenProperty.get())) {
                overridden = false;
                this.bind(overriddenProperty);
            } else {
                // add() calls the Set property listeners, which updates the view coordinate menu
                returnValue = super.add(element);
            }
        }

        return returnValue;
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            boolean returnValue = set.addAll(elements);
            super.set(set);
            return returnValue;
        }
        return super.addAll(elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            boolean returnValue = set.removeAll(objects);
            super.set(set);
            return returnValue;
        }
        return super.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            boolean returnValue = set.retainAll(objects);
            super.set(set);
            return returnValue;
        }
        return super.retainAll(objects);
    }

    /// called by ViewMenuTask when view coordinate Set is cleared
    @Override
    public void clear() {
        if (!overridden) {
            overridden = true;
            this.unbind();
            // create a new empty set
            super.set(FXCollections.observableSet(new HashSet<>()));
        } else {
            // FIRST check to see if the overriddenProperty Set is empty
            if (this.overriddenProperty.get().isEmpty()) {
                overridden = false;
                this.bind(overriddenProperty);
            } else {
                // clear() calls the Set property listeners, which updates the view coordinate menu
                super.clear();
            }
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (!overridden) {
            overridden = true;

            // make a copy of the current set
            HashSet<T> copiedSet = new HashSet<>(get());

            ObservableSet<T> set = FXCollections.observableSet(copiedSet);
            this.unbind();
            boolean returnValue = set.removeIf(filter);
            super.set(set);
            return returnValue;
        }
        return super.removeIf(filter);
    }
    @Override
    public void bindBidirectional(Property<ObservableSet<T>> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContentBidirectional(ObservableSet<T> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContent(ObservableSet<T> list) {
        throw new UnsupportedOperationException();
    }
}
