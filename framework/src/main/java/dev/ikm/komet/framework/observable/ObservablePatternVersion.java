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
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public final class ObservablePatternVersion
        extends ObservableVersion<PatternVersionRecord>
        implements PatternEntityVersion {
    private static final Logger LOG = LoggerFactory.getLogger(ObservablePatternVersion.class);

    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Pattern purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Pattern meaning");
    final ImmutableList<ObservableFeatureDefinition> observableFieldDefinitions;

    ObservablePatternVersion(PatternVersionRecord patternVersionRecord) {
        super(patternVersionRecord);
        purposeProperty.set(Entity.getFast(patternVersionRecord.semanticPurposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(Entity.getFast(patternVersionRecord.semanticMeaningNid()));
        meaningProperty.addListener(this::meaningChanged);
        MutableList<ObservableFeatureDefinition> mutableFieldDefinitions = Lists.mutable.ofInitialCapacity(patternVersionRecord.fieldDefinitions().size());
        for (FieldDefinitionRecord fieldDefinition : patternVersionRecord.fieldDefinitions()) {
            mutableFieldDefinitions.add(new ObservableFeatureDefinition(fieldDefinition, this,
                    FeatureKey.Version.PatternFieldDefinitionListItem(nid(), indexInPattern(), patternNid(), stampNid())));
        }
        this.observableFieldDefinitions = mutableFieldDefinitions.toImmutable();
    }

    @Override
    public ObservablePattern getObservableEntity() {
        return ObservableEntity.get(nid());
    }


    @Override
    public int patternNid() {
        return Binding.Pattern.pattern().nid();
    }

    @Override
    public int indexInPattern() {
        return Binding.Pattern.versionItemDefinitionIndex();
    }

    private void purposeChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.PURPOSE, observableValue, newValue);
    }

    private void meaningChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.MEANING, observableValue, newValue);
    }

    private void handleChange(FIELDS field, ObservableValue<? extends EntityFacade> observableValue, EntityFacade newValue) {
        StampRecord stamp = Entity.getStamp(getVersionRecord().stampNid());
        PatternVersionRecord version = getVersionRecord();
        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create new version...
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.with()
                        .semanticMeaningNid(meaningProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
                case PURPOSE -> version.with()
                        .semanticPurposeNid(purposeProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.withSemanticMeaningNid(meaningProperty.get().nid());
                case PURPOSE -> version.withSemanticPurposeNid(purposeProperty.get().nid());
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    public SimpleObjectProperty<EntityFacade> purposeProperty() {
        return purposeProperty;
    }

    public SimpleObjectProperty<EntityFacade> meaningProperty() {
        return meaningProperty;
    }

    @Override
    protected PatternVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public PatternVersionRecord getVersionRecord() {
        return version();
    }

    @Override
    public ImmutableList<ObservableFeatureDefinition> fieldDefinitions() {
        return this.observableFieldDefinitions;
    }

    @Override
    public int semanticPurposeNid() {
        return version().semanticPurposeNid();
    }

    @Override
    public int semanticMeaningNid() {
        return version().semanticMeaningNid();
    }

    enum FIELDS {PURPOSE, MEANING}


    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<Feature> patternMeaningFieldReference = new AtomicReference<>();
    private Feature getPatternMeaningFeature() {
        return patternMeaningFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makePatternMeaningFeature());
    }
    private Feature makePatternMeaningFeature() {
        FeatureKey locator = FeatureKey.Version.PatternMeaning(this.nid(), this.stampNid());
        return new FeatureWrapper(this.meaningProperty, Binding.Pattern.Version.pattern().nid(),
                Binding.Pattern.Version.patternMeaningFieldDefinitionIndex(), this, locator);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<Feature> patternPurposeFieldReference = new AtomicReference<>();
    private Feature getPatternPurpose() {
        return patternPurposeFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makePatternPurposeFeature());
    }
    private Feature makePatternPurposeFeature() {
        FeatureKey locator = FeatureKey.Version.PatternPurpose(this.nid(), this.stampNid()) ;
        return new FeatureWrapper(this.purposeProperty, Binding.Pattern.Version.pattern().nid(),
                Binding.Pattern.Version.patternPurposeFieldDefinitionIndex(),this, locator);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<Feature> fieldDefinitionListReference = new AtomicReference<>();
    private Feature getFieldDefinitionListFeature() {
        return fieldDefinitionListReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeFieldDefinitionListFeature());
    }
    private Feature makeFieldDefinitionListFeature() {
        FeatureKey locator = FeatureKey.Version.PatternFieldDefinitionList(this.nid(), this.stampNid()) ;
        return FeatureList.makeWithBackingList(this.observableFieldDefinitions, locator, Binding.Pattern.Version.pattern(), Binding.Pattern.Version.fieldDefinitionListFieldDefinitionIndex(), this);
    }

    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature> features) {
        // Pattern purpose
        features.add(getPatternPurpose());
        // Pattern meaning
        features.add(getPatternMeaningFeature());
        // Pattern FieldDefinitionList
        features.add(getFieldDefinitionListFeature());
        // Pattern FieldDefinitionListItems

        for (ObservableFeatureDefinition featureDefinition : observableFieldDefinitions) {
            features.add(featureDefinition);
        }
    }
}
