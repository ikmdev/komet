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
package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.entity.Field;

/**
 * Editable field wrapper providing cached editing for semantic version fields.
 * <p>
 * Symmetric counterpart to {@link ObservableField}:
 * <ul>
 *   <li>ObservableField - read-only, writes directly to DB</li>
 *   <li>ObservableEditableField - editable, caches changes in ObservableEditableSemanticVersion</li>
 * </ul>
 * <p>
 * Editable fields are obtained from {@link ObservableEditableSemanticVersion#getEditableFields()}
 * and provide JavaFX properties that can be bound to GUI controls. Changes accumulate until
 * the parent editable version's save() or commit() is called.
 *
 * @param <DT> the data type of the field value
 */
public final class ObservableEditableField<DT> extends ObservableEditableFeature<DT> {

    /**
     * Package-private constructor. Use {@link ObservableEditableSemanticVersion#getEditableFields()}.
     *
     * @param observableField the read-only field to wrap
     * @param initialValue the initial value
     * @param fieldIndex the index of this field in the semantic version
     */
    ObservableEditableField(ObservableField<DT> observableField, DT initialValue, int fieldIndex) {
        super(observableField, initialValue, fieldIndex);
    }

    /**
     * Returns the original read-only ObservableField.
     */
    @Override
    public ObservableField<DT> getObservableFeature() {
        return (ObservableField<DT>) super.getObservableFeature();
    }

    /**
     * Returns the underlying field data.
     */
    public Field<DT> field() {
        return getObservableFeature().field();
    }

    /**
     * Returns the index of this field in the semantic version.
     */
    public int getFieldIndex() {
        return featureIndex;
    }
}
