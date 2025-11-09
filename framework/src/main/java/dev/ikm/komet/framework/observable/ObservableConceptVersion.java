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

import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import org.eclipse.collections.api.list.MutableList;


/**
 * Concrete observable concept version - fully type-reified, no generic parameters.
 * <p>
 * This is Layer 3 (Concrete) of the MGC pattern for concept versions.
 * All generic types are resolved to concrete types.
 *
 * <h2>MGC Pattern Layers for Concept Versions</h2>
 * <ul>
 *   <li><b>Layer 1:</b> {@link ObservableVersion} - Marker interface</li>
 *   <li><b>Layer 2:</b> {@link ObservableEntityVersion} - Generic abstract class</li>
 *   <li><b>Layer 3:</b> {@code ObservableConceptVersion} - Concrete final class (this class)</li>
 * </ul>
 */
public final class ObservableConceptVersion 
        extends ObservableEntityVersion<ObservableConcept, ConceptVersionRecord> 
        implements ConceptEntityVersion, ObservableVersion {
    
    ObservableConceptVersion(ObservableConcept observableConcept, ConceptVersionRecord conceptVersionRecord) {
        super(observableConcept, conceptVersionRecord);
    }

    @Override
    public ConceptVersionRecord getVersionRecord() {
        return version();
    }


    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature<?>> features) {
        // Nothing to add.
    }

    /**
     * Type-safe accessor for the containing concept entity.
     * <p>
     * Overrides the generic method with specific return type.
     */
    public ObservableConcept getObservableConcept() {
        return getObservableEntity();
    }

    @Override
    public Editable getEditableVersion(ObservableStamp editStamp, Transaction transaction) {
        return Editable.getOrCreate(getObservableEntity(), this, editStamp, transaction);
    }

    /**
     * Editable version wrapper for ObservableConceptVersion.
     * <p>
     * Implements {@link EditableVersion} marker
     * interface through the base {@link ObservableEntityVersion.Editable} class.
     * <p>
     * Concepts have minimal fields (just stamp fields), so this class mainly
     * provides the infrastructure for saving and committing concept versions.
     * 
     * <h2>Usage Example</h2>
     * <pre>{@code
     * // Get editable - implements EditableVersion marker
     * EditableVersion editable = conceptVersion.getEditableVersion(editStamp);
     * 
     * // Type-safe casting when needed
     * if (editable instanceof ObservableConceptVersion.Editable ce) {
     *     // Access concept-specific methods
     * }
     * 
     * // Or pattern matching
     * switch (editable) {
     *     case ObservableConceptVersion.Editable ce -> handleConcept(ce);
     *     default -> handleOther(editable);
     * }
     * }</pre>
     */
    public static final class Editable
            extends ObservableEntityVersion.Editable<ObservableConcept, ObservableConceptVersion, ConceptVersionRecord> implements EditableVersion {
        // Already implements EditableVersion and EditableChronology via parent!

        private Editable(ObservableConcept observableConcept, ObservableConceptVersion observableVersion, ObservableStamp editStamp, Transaction transaction) {
            super(observableConcept, observableVersion, editStamp, transaction);
        }

        /**
         * Gets or creates the canonical editable concept version for the given stamp.
         * <p>
         * Returns the exact same instance for multiple calls with the same stamp, ensuring
         * a single canonical editable version per ObservableStamp.
         *
         * @param observableVersion the ObservableConceptVersion to edit
         * @param editStamp the ObservableStamp (typically identifying the author)
         * @return the canonical editable concept version for this stamp
         */
        public static Editable getOrCreate(ObservableConcept observableConcept, ObservableConceptVersion observableVersion, ObservableStamp editStamp, Transaction transaction) {
            return ObservableEntityVersion.getOrCreate(observableConcept, observableVersion, editStamp, transaction, Editable::new);
        }

        @Override
        protected ConceptVersionRecord createVersionWithStamp(ConceptVersionRecord version, int stampNid) {
            return version.withStampNid(stampNid);
        }

        @Override
        protected Entity<?> createAnalogue(ConceptVersionRecord version) {
            return version.chronology().with(version).build();
        }
    }
}
