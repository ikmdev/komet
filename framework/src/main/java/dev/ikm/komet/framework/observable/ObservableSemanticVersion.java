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
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.concurrent.atomic.AtomicReference;

public final class ObservableSemanticVersion
        extends ObservableVersion<ObservableSemantic, SemanticVersionRecord>
        implements SemanticEntityVersion {
    ObservableSemanticVersion(ObservableSemantic observableSemantic, SemanticVersionRecord semanticVersionRecord) {
        super(observableSemantic, semanticVersionRecord);
    }

    @Override
    protected SemanticVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public SemanticEntity entity() {
        return version().entity();
    }

    @Override
    public SemanticEntity chronology() {
        return version().chronology();
    }

    @Override
    public SemanticVersionRecord getVersionRecord() {
        return version();
    }

    @Override
    public PatternEntity pattern() {
        return EntityHandle.getPatternOrThrow(patternNid());
    }

    @Override
    public int patternNid() {
        return version().patternNid();
    }

    @Override
    public int indexInPattern() {
        return Binding.Semantic.versionItemDefinitionIndex();
    }

    @Override
    public ImmutableList<Object> fieldValues() {
        return version().fieldValues();
    }

    @Override
    public ImmutableList<ObservableField> fields() {
        ObservableField[] fieldArray = new ObservableField[fieldValues().size()];
        for (int indexInPattern = 0; indexInPattern < fieldArray.length; indexInPattern++) {
            Object value = fieldValues().get(indexInPattern);

            FeatureKey.VersionFeature.Semantic.FieldListItem featureKey =
                    FeatureKey.Version.SemanticFieldListItem(nid(), indexInPattern, patternNid(), stampNid());

            fieldArray[indexInPattern] = new ObservableField(featureKey, new FieldRecord(value, featureKey.nid(), featureKey.stampNid(), featureKey.patternNid(), featureKey.index()), this);
        }
        return Lists.immutable.of(fieldArray);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<Feature> fieldListReference = new AtomicReference<>();
    private Feature getFieldListFeature() {
        return fieldListReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeFieldListFeature());
    }
    private Feature makeFieldListFeature() {
        FeatureKey locator = FeatureKey.Version.SemanticFieldList(this.nid(), stampNid());
        return FeatureList.makeWithBackingList(this.fields(), locator, Binding.Semantic.Version.pattern(), Binding.Semantic.Version.semanticFieldsDefinitionIndex(), this);
    }

    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature> features) {
        features.add(getFieldListFeature());
        for (ObservableField field : fields()) {
            features.add(field);
        }
    }

    @Override
    public ObservableEditableSemanticVersion getEditableVersion(StampEntity editStamp) {
        return ObservableEditableSemanticVersion.getOrCreate(getObservableEntity(), this, editStamp);
    }
}
