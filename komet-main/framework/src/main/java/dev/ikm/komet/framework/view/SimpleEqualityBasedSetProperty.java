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

import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * All properties that inherit from ObjectPropertyBase use object identity, rather than
 * object equality to determine if a value has changed. Since at least ConceptSpecification implementations
 * are not singletons, and can be implemented by more than one class, this optimization does not hold true.
 * <br>
 * This class overrides set and setValue to use equality rather than object identity to determine if a value has changed.
 * @param <T>
 */
public class SimpleEqualityBasedSetProperty<T> extends SimpleSetProperty<T> {

    public SimpleEqualityBasedSetProperty() {
    }

    public SimpleEqualityBasedSetProperty(ObservableSet<T> initialValue) {
        super(initialValue);
    }

    public SimpleEqualityBasedSetProperty(Object bean, String name) {
        super(bean, name);
    }

    public SimpleEqualityBasedSetProperty(Object bean, String name, ObservableSet<T> initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(ObservableSet<T> newValue) {
        if (this.getValue() == null || this.getValue().equals(newValue) == false) {
            super.set(newValue);
        }
    }

    @Override
    public void setValue(ObservableSet<T> newValue) {
        if (this.getValue() == null || this.getValue().equals(newValue) == false) {
            super.setValue(newValue);
        }
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return super.retainAll(objects);
    }

    public boolean setAll(T... elements) {
        List<T> elementList = Arrays.asList(elements);
        if (size() != elements.length || this.containsAll(elementList) == false) {
            super.clear();
            return super.addAll(elementList);
        }
        return true;
    }

    public boolean setAll(Collection<? extends T> elements) {
        if (size() != elements.size() || this.containsAll(elements) == false) {
            super.clear();
            return super.addAll(elements);
        }
        return true;
    }
}
