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

import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.Transaction;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public final class ObservableField<DT> extends ObservableFeature<DT> {

    public ObservableField(FeatureKey featureKey, Field<DT> attribute, ObservableSemanticVersion containingVersion, boolean writeOnEveryChange) {
        super(featureKey, attribute, containingVersion, writeOnEveryChange);
    }

    public ObservableField(FeatureKey featureKey, Field<DT> attribute, ObservableSemanticVersion containingVersion) {
        this(featureKey, attribute, containingVersion, false);
    }

    /**
     * Editable field wrapper providing cached editing for semantic version fields.
     * <p>
     * Symmetric counterpart to {@link ObservableField}:
     * <ul>
     *   <li>ObservableField - read-only, writes directly to DB</li>
     *   <li>ObservableField.Editable - editable, caches changes in ObservableSemanticVersion.Editable</li>
     * </ul>
     * <p>
     * Editable fields are obtained from {@link ObservableSemanticVersion.Editable#getEditableFields()}
     * and provide JavaFX properties that can be bound to GUI controls. Changes accumulate until
     * the parent editable version's save() or commit() is called.
     *
     * @param <DT> the data type of the field value
     */
    public static final class Editable<DT> extends ObservableFeature.Editable<DT> {

        /**
         * Package-private constructor. Use {@link ObservableSemanticVersion.Editable#getEditableFields()}.
         *
         * @param observableField the read-only field to wrap
         * @param initialValue the initial value
         * @param fieldIndex the index of this field in the semantic version
         */
        Editable(ObservableField<DT> observableField, DT initialValue, int fieldIndex) {
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
}
