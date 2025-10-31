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
import org.eclipse.collections.api.list.MutableList;

public final class ObservableConceptVersion extends ObservableVersion<ConceptVersionRecord> implements ConceptEntityVersion {
    ObservableConceptVersion(ConceptVersionRecord conceptVersionRecord) {
        super(conceptVersionRecord);
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
    public ObservableConcept getObservableEntity() {
        return ObservableEntity.get(nid());
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
    public ObservableEditableConceptVersion getEditableVersion(ObservableStamp editStamp) {
        return ObservableEditableConceptVersion.getOrCreate(this, editStamp);
    }
}
