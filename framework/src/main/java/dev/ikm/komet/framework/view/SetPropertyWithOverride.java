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

    @Override
    public void set(ObservableSet<T> newValue) {
        if (!overridden) {
            overridden = true;
            this.unbind();
        }
        if (newValue == null) {
            overridden = false;
            this.bind(overriddenProperty);
        } else {
            super.set(newValue);
        }
    }

    @Override
    public boolean setAll(T... elements) {
        return setAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(Collection<? extends T> elements) {
        if (!this.get().equals(elements)) {
            if (!overridden) {
                overridden = true;
                this.unbind();
            }
            return super.setAll(elements);
        }
        return false;
    }

    @Override
    public boolean remove(Object obj) {
        if (!overridden) {
            overridden = true;
            ObservableSet<T> set = FXCollections.observableSet(get());
            this.unbind();
            boolean returnValue = set.remove(obj);
            super.set(set);
            return returnValue;
        }
        return super.remove(obj);
    }

    @Override
    public boolean add(T element) {
        if (!overridden) {
            overridden = true;
            ObservableSet<T> set = FXCollections.observableSet(get());
            this.unbind();
            boolean returnValue = set.add(element);
            super.set(FXCollections.observableSet(get()));
            return returnValue;
        }
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            ObservableSet<T> set = FXCollections.observableSet(get());
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
            ObservableSet<T> set = FXCollections.observableSet(get());
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
            ObservableSet<T> set = FXCollections.observableSet(get());
            this.unbind();
            boolean returnValue = set.retainAll(objects);
            super.set(set);
            return returnValue;
        }
        return super.retainAll(objects);
    }

    @Override
    public void clear() {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet());
        }
        super.clear();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (!overridden) {
            overridden = true;
            ObservableSet<T> set = FXCollections.observableSet(get());
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
