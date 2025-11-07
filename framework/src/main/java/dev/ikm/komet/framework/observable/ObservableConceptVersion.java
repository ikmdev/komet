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
import org.eclipse.collections.api.list.MutableList;

public final class ObservableConceptVersion extends ObservableVersion<ObservableConcept, ConceptVersionRecord> implements ConceptEntityVersion {
    ObservableConceptVersion(ObservableConcept observableConcept, ConceptVersionRecord conceptVersionRecord) {
        super(observableConcept, conceptVersionRecord);
    }

    @Override
    protected ConceptVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public ConceptVersionRecord getVersionRecord() {
        return version();
    }


    @Override
    public int patternNid() {
        return Binding.Concept.pattern().nid();
    }

    @Override
    public int indexInPattern() {
        return Binding.Concept.versionItemPatternIndex();
    }


    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature> features) {
        // Nothing to add.
    }

    @Override
    public Editable getEditableVersion(ObservableStamp editStamp) {
        return Editable.getOrCreate(getObservableEntity(), this, editStamp);
    }

    /**
     * Editable version wrapper for ObservableConceptVersion.
     * <p>
     * Concepts have minimal fields (just stamp fields), so this class mainly
     * provides the infrastructure for saving and committing concept versions.
     */
    public static final class Editable
            extends ObservableVersion.Editable<ObservableConcept, ObservableConceptVersion, ConceptVersionRecord> {

        private Editable(ObservableConcept observableConcept, ObservableConceptVersion observableVersion, ObservableStamp editStamp) {
            super(observableConcept, observableVersion, editStamp);
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
        public static Editable getOrCreate(ObservableConcept observableConcept, ObservableConceptVersion observableVersion, ObservableStamp editStamp) {
            return ObservableVersion.Editable.getOrCreate(observableConcept, observableVersion, editStamp, Editable::new);
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
