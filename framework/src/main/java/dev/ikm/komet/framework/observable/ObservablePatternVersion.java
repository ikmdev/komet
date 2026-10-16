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

import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Concrete observable pattern version - fully type-reified, no generic parameters.
 * <p>This is Layer 3 (Concrete) of the MGC pattern for pattern versions.
 */
public final class ObservablePatternVersion
        extends ObservableEntityVersion<ObservablePattern, PatternVersionRecord>
        implements PatternEntityVersion, ObservableVersion {

    private static final Logger LOG = LoggerFactory.getLogger(ObservablePatternVersion.class);

    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Pattern purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Pattern meaning");
    /**
     * The field definitions of the version this wrapper holds now. Rebuilt whenever the record is
     * replaced in place ({@link #setVersionInternal}): the canonical observable of a pattern
     * outlives its versions' records, and an uncommitted version saved again in its transaction,
     * as a pattern given its fields after it was created is, keeps its stamp and its wrapper.
     */
    private volatile ImmutableList<ObservableFeatureDefinition> observableFieldDefinitions;

    /** True while the properties follow a replaced record, so their listeners write nothing. */
    private boolean followingRecord;

    ObservablePatternVersion(ObservablePattern observablePattern, PatternVersionRecord patternVersionRecord) {
        super(observablePattern, patternVersionRecord);
        purposeProperty.set(EntityHandle.getConceptOrThrow(patternVersionRecord.semanticPurposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(EntityHandle.getConceptOrThrow(patternVersionRecord.semanticMeaningNid()));
        meaningProperty.addListener(this::meaningChanged);
        this.observableFieldDefinitions = fieldDefinitionsOf(patternVersionRecord);
    }

    @Override
    void setVersionInternal(EntityVersion entityVersion) {
        super.setVersionInternal(entityVersion);
        if (meaningProperty == null) {
            return; // the superclass constructor: this version's own state is set by its constructor
        }
        PatternVersionRecord record = (PatternVersionRecord) entityVersion;
        followingRecord = true;
        try {
            purposeProperty.set(EntityHandle.getConceptOrThrow(record.semanticPurposeNid()));
            meaningProperty.set(EntityHandle.getConceptOrThrow(record.semanticMeaningNid()));
        } finally {
            followingRecord = false;
        }
        this.observableFieldDefinitions = fieldDefinitionsOf(record);
        fieldDefinitionListReference.set(null); // the feature list is built over the definitions
    }

    private ImmutableList<ObservableFeatureDefinition> fieldDefinitionsOf(PatternVersionRecord record) {
        MutableList<ObservableFeatureDefinition> definitions = Lists.mutable.ofInitialCapacity(record.fieldDefinitions().size());
        for (FieldDefinitionRecord fieldDefinition : record.fieldDefinitions()) {
            definitions.add(new ObservableFeatureDefinition(fieldDefinition, this,
                    FeatureKey.Version.PatternFieldDefinitionListItem(nid(), fieldDefinition.indexInPattern(), fieldDefinition.patternNid(), record.stampNid())));
        }
        return definitions.toImmutable();
    }

    private void purposeChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.PURPOSE, observableValue, newValue);
    }

    private void meaningChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.MEANING, observableValue, newValue);
    }

    private void handleChange(FIELDS field, ObservableValue<? extends EntityFacade> observableValue, EntityFacade newValue) {
        if (followingRecord) {
            return;
        }
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
    public PatternVersionRecord getVersionRecord() {
        return version();
    }

    @Override
    public ImmutableList<ObservableFeatureDefinition> fieldDefinitions() {
        return this.observableFieldDefinitions;
    }

    @Override
    public long semanticPurposeNid() {
        return version().semanticPurposeNid();
    }

    @Override
    public long semanticMeaningNid() {
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
        return new FeatureWrapper(this.meaningProperty, EntityBinding.Pattern.Version.pattern().nid(),
                EntityBinding.Pattern.Version.patternMeaningFieldDefinitionIndex(), this, locator);
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
        return new FeatureWrapper(this.purposeProperty, EntityBinding.Pattern.Version.pattern().nid(),
                EntityBinding.Pattern.Version.patternPurposeFieldDefinitionIndex(),this, locator);
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
        return FeatureList.makeWithBackingList(this.observableFieldDefinitions, locator, EntityBinding.Pattern.Version.pattern(),
                EntityBinding.Pattern.Version.fieldDefinitionListFieldDefinitionIndex(), this);
    }

    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature<?>> features) {
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

    @Override
    public Editable getEditableVersion(ObservableStamp editStamp, Transaction transaction) {
        return ObservableEntityVersion.getOrCreate(getObservableEntity(), this, editStamp, transaction, Editable::new);
    }
    /**
     * Type-safe accessor for the containing pattern entity.
     */
    public ObservablePattern getObservablePattern() {
        return getObservableEntity();
    }
    /**
     * Editable version wrapper for ObservablePatternVersion.
     * <p>     * Implements {@link EditableVersion} marker
     * interface through the base {@link ObservableEntityVersion.Editable} class.
     * <p>     * Provides editable properties for pattern purpose and meaning that can be
     * bound to GUI components. Changes are cached until save() or commit() is called.
     * 
     * <h2>Pattern-Specific Editable Properties</h2>
     * <p>In addition to the standard {@link EditableVersion} operations, this class
     * provides pattern-specific editable properties:
     * <ul>
     *   <li>{@link #getPurposeProperty()} - Editable semantic purpose</li>
     *   <li>{@link #getMeaningProperty()} - Editable semantic meaning</li>
     * </ul>
     * 
     * <h2>Usage Example</h2>
     * <pre>{@code
     * // Get editable - implements EditableVersion marker
     * EditableVersion editable = patternVersion.getEditableVersion(editStamp);
     * 
     * // Pattern matching to access pattern-specific properties
     * switch (editable) {
     *     case ObservablePatternVersion.Editable pe -> {
     *         // Bind pattern-specific fields
     *         purposeComboBox.valueProperty().bindBidirectional(
     *             pe.getPurposeProperty());
     *         meaningComboBox.valueProperty().bindBidirectional(
     *             pe.getMeaningProperty());
     *     }
     *     default -> bindGenericFields(editable);
     * }
     * }</pre>
     */
    public static final class Editable
            extends ObservableEntityVersion.Editable<ObservablePattern, ObservablePatternVersion, PatternVersionRecord>
            implements EditableVersion {
        // Already implements EditableVersion and EditableChronology via parent!
        
        private final SimpleObjectProperty<EntityFacade> editablePurposeProperty;
        private final SimpleObjectProperty<EntityFacade> editableMeaningProperty;

        Editable(ObservablePattern observablePattern, ObservablePatternVersion observableVersion, ObservableStamp editStamp, Transaction transaction) {
            super(observablePattern, observableVersion, editStamp, transaction);

            // Initialize editable properties
            this.editablePurposeProperty = new SimpleObjectProperty<>(
                    this,
                    "purpose",
                    EntityHandle.getConceptOrThrow(observableVersion.semanticPurposeNid())
            );

            this.editableMeaningProperty = new SimpleObjectProperty<>(
                    this,
                    "meaning",
                    EntityHandle.getConceptOrThrow(observableVersion.semanticMeaningNid())
            );

            // Add listeners to update working version when properties change
            editablePurposeProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withSemanticPurposeNid(newValue.nid());
                }
            });

            editableMeaningProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withSemanticMeaningNid(newValue.nid());
                }
            });
        }

        /**
         * Returns the editable purpose property for GUI binding.
         * <p>         * This property is specific to pattern versions and provides direct
         * bidirectional binding support for UI controls.
         */
        public SimpleObjectProperty<EntityFacade> getPurposeProperty() {
            return editablePurposeProperty;
        }
        
        /**
         * Returns the editable meaning property for GUI binding.
         * <p>         * This property is specific to pattern versions and provides direct
         * bidirectional binding support for UI controls.
         */
        public SimpleObjectProperty<EntityFacade> getMeaningProperty() {
            return editableMeaningProperty;
        }

        /**
         * Sets the field definitions for this pattern version.
         * <p>         * Each {@link FieldDefinitionRecord} defines a field's meaning, purpose, data type,
         * and position within the pattern. This method replaces all existing field definitions.
         *
         * @param fieldDefinitions the immutable list of field definitions
         */
        public void setFieldDefinitions(ImmutableList<FieldDefinitionRecord> fieldDefinitions) {
            workingVersion = workingVersion.withFieldDefinitions(fieldDefinitions);
        }

        @Override
        protected PatternVersionRecord createVersionWithStamp(PatternVersionRecord version, long stampNid) {
            return version.withStampNid(stampNid);
        }

        @Override
        protected Entity<?> createAnalogue(PatternVersionRecord version) {
            // Build on the entity as stored now: the version's chronology is the entity as it was
            // when the version was made, and a canonical observable outlives later writes to it.
            PatternRecord chronology = EntityHandle.get(version.nid()).orNull() instanceof PatternRecord stored
                    ? stored : version.chronology();
            return chronology.with(version).build();
        }

        @Override
        public void reset() {
            super.reset();
            // Reset properties to original values
            editablePurposeProperty.set(EntityHandle.getConceptOrThrow(observableVersion.semanticPurposeNid()));
            editableMeaningProperty.set(EntityHandle.getConceptOrThrow(observableVersion.semanticMeaningNid()));
        }
    }
}
