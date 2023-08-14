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

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ListPropertyWithOverride<T> extends SimpleEqualityBasedListProperty<T> implements PropertyWithOverride<ObservableList<T>> {

    private final ListProperty<T> overriddenProperty;
    private boolean overridden = false;

    public ListPropertyWithOverride(ListProperty<T> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
        this.bind(overriddenProperty);
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
    public Property<ObservableList<T>> overriddenProperty() {
        return this.overriddenProperty;
    }

    @Override
    public void set(ObservableList<T> newValue) {
        if (!overridden && newValue != null) {
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
    public boolean add(T element) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.add(element);
            super.set(copy);
            return true;
        }
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.addAll(elements);
            super.set(copy);
            return true;
        }
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.addAll(i, copy);
            super.set(copy);
            return true;
        }
        return super.addAll(i, elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.removeAll(objects);
            super.set(copy);
            return true;
        }
        return super.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.retainAll(objects);
            super.set(copy);
            return true;
        }
        return super.retainAll(objects);
    }

    @Override
    public void clear() {
        if (!overridden) {
            this.unbind();
            overridden = true;
            super.set(FXCollections.observableArrayList());
        }
        super.clear();
    }

    @Override
    public T set(int i, T element) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            T old = copy.set(i, element);
            super.set(copy);
            return old;
        }
        return super.set(i, element);
    }

    @Override
    public void add(int i, T element) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.add(i, element);
            super.set(copy);
        } else {
            super.add(i, element);
        }
    }

    @Override
    public T remove(int i) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            T old = copy.remove(i);
            super.set(copy);
            return old;
        }
        return super.remove(i);
    }

    @Override
    public boolean addAll(T... elements) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.addAll(elements);
            super.set(copy);
            return true;
        }
        return super.addAll(elements);
    }

    @Override
    public boolean removeAll(T... elements) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            boolean returnValue = copy.removeAll(elements);
            super.set(copy);
            return returnValue;
        }
        return super.removeAll(elements);
    }

    @Override
    public boolean retainAll(T... elements) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            boolean returnValue = copy.retainAll(elements);
            super.set(copy);
            return returnValue;
        }
        return super.retainAll(elements);
    }

    @Override
    public void remove(int from, int to) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.remove(from, to);
            super.set(copy);
        } else {
            super.remove(from, to);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            copy.replaceAll(operator);
            super.set(copy);
        } else {
            super.replaceAll(operator);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (!overridden) {
            overridden = true;
            ObservableList<T> copy = FXCollections.observableArrayList(get());
            this.unbind();
            boolean returnValue = copy.removeIf(filter);
            super.set(copy);
            return returnValue;
        } else {
            return super.removeIf(filter);
        }
    }

    @Override
    public void bindBidirectional(Property<ObservableList<T>> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContentBidirectional(ObservableList<T> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContent(ObservableList<T> list) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<T> subList(int from, int to) {
        throw new UnsupportedOperationException();
    }

}
