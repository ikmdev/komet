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

import javafx.beans.property.SimpleObjectProperty;

/**
 * All properties that inherit from ObjectPropertyBase use object identity, rather than
 * object equality to determine if a value has changed. Since at least ConceptSpecification implementations
 * are not singletons, and can be implemented by more than one class, this optimization does not hold true.
 * <br>
 * This class overrides set and setValue to use equality rather than object identity to determine if a value has changed.
 * @param <T>
 */
public class SimpleEqualityBasedObjectProperty<T> extends SimpleObjectProperty<T> {
    public SimpleEqualityBasedObjectProperty() {
    }

    public SimpleEqualityBasedObjectProperty(T initialValue) {
        super(initialValue);
    }

    public SimpleEqualityBasedObjectProperty(Object bean, String name) {
        super(bean, name);
    }

    public SimpleEqualityBasedObjectProperty(Object bean, String name, T initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(T newValue) {
        if (this.get() == newValue) {
            return;
        }
        if (this.get() != null && this.get().equals(newValue)) {
            return;
        }
        super.set(newValue);
    }
}
