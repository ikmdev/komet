package dev.ikm.komet.framework.observable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ikm.tinkar.component.FieldDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public sealed class ObservableFeature<DT> implements Feature<DT>, Field<DT>
        permits ObservableField {
    private final ReadOnlyObjectWrapper<Field<DT>> fieldProperty = new ReadOnlyObjectWrapper<>();
    public final BooleanProperty refreshProperties = new SimpleBooleanProperty(false);
    private ObservableComponent containingComponent;
    private ReadOnlyObjectWrapper<DT> valueProperty = new ReadOnlyObjectWrapper<>();
    // Mutable property for UI binding that triggers database writes
    private SimpleObjectProperty<DT> editableValueProperty = null;
    public final boolean writeOnEveryChange;
    public final FeatureKey featureKey;
    private final ReadOnlyProperty<? extends Feature<DT>> featureProperty = new ReadOnlyObjectWrapper<>(this).getReadOnlyProperty();

    public ObservableFeature(FeatureKey featureKey, Field<DT> attribute, ObservableComponent containingComponent, boolean writeOnEveryChange) {
        this.featureKey = featureKey;
        this.containingComponent = containingComponent;
        this.writeOnEveryChange = writeOnEveryChange;
        this.fieldProperty.set(attribute);
        if (attribute != null) {
            valueProperty.set(attribute.value());
        }
        refreshProperties.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                writeToDatabase(value());
            }
        });
    }

    public ObservableFeature(FeatureKey featureKey, Field<DT> attribute, ObservableComponent containingComponent) {
        this(featureKey, attribute, containingComponent, true);
    }

    /**
     * Package-private method to update value internally. Only called from updateVersions() flow.
     */
    void setValueInternal(DT newValue) {
        if (newValue != null) {
            valueProperty.set(newValue);
            fieldProperty.set(field().with(newValue));
            // Also update the editable property if it exists, without triggering the listener
            if (editableValueProperty != null) {
                editableValueProperty.set(newValue);
            }
        }
    }

    @Override
    public int patternNid() {
        return field().patternNid();
    }

    @Override
    public DT value() {
        return valueProperty.getValue();
    }

    public ReadOnlyObjectProperty<DT> valueProperty() {
        return valueProperty.getReadOnlyProperty();
    }

    /**
     * Returns a mutable property for UI binding that triggers database writes when changed.
     * This property should be used for bidirectional bindings in editable UI controls.
     * Changes to this property will automatically trigger writeToDatabase() and eventually
     * update the readonly valueProperty() through the EvtBus event flow.
     */
    public ObjectProperty<DT> editableValueProperty() {
        if (editableValueProperty == null) {
            editableValueProperty = new SimpleObjectProperty<>(value());
            editableValueProperty.addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    if (writeOnEveryChange && !refreshProperties.get()) {
                        writeToDatabase(newValue);
                    }
                }
            });
        }
        return editableValueProperty;
    }

    public FieldRecord<DT> field() {
        return (FieldRecord<DT>) fieldProperty.get();
    }

    @Override
    public int indexInPattern() {
        return field().indexInPattern();
    }

    public ObservableComponent containingComponent() {
        return containingComponent;
    }

    @Override
    public PatternFacade pattern() {
        return EntityProxy.Pattern.make(field().patternNid());
    }

    @Override
    public ReadOnlyProperty<? extends Feature<DT>> featureProperty() {
        return this.featureProperty;
    }

    @Override
    public FieldDefinition fieldDefinition(StampCalculator stampCalculator) {
        PatternEntity<PatternEntityVersion> pattern = Entity.getFast(field().patternNid());
        return stampCalculator.latestPatternEntityVersion(pattern).get().fieldDefinitions().get(field().indexInPattern());
    }

    public ReadOnlyObjectProperty<Field<DT>> fieldProperty() {
        return fieldProperty.getReadOnlyProperty();
    }

    /**
     * Package-private method to write to database. Only called from updateVersions() flow or explicit triggers.
     */
    void writeToDatabase(Object newValue) {
        StampRecord stamp = Entity.getStamp(field().versionStampNid());
        // Get current version
        SemanticVersionRecord version = Entity.getVersionFast(field().nid(), field().versionStampNid());
        SemanticRecord semantic = Entity.getFast(field().nid());
        MutableList fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
        fieldsForNewVersion.set(indexInPattern(), newValue);

        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create a new version.
            SemanticVersionRecord newVersion = version.with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

            SemanticRecord analogue = semantic.with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            SemanticVersionRecord newVersion = version.withFieldValues(fieldsForNewVersion.toImmutable());
            // if a version with the same stamp as newVersion exists, that version will be removed
            // prior to adding the new version so you don't get duplicate versions with the same stamp.
            SemanticRecord analogue = semantic.with(newVersion).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    @Override
    public final FeatureKey featureKey() {
        return featureKey;
    }

    /**
     * Base class for editable features that cache field changes for GUI editing.
     * <p>
     * Provides the same symmetric API as {@link ObservableFeature}, but for editable scenarios
     * where changes are cached in an {@link ObservableVersion.Editable} until save() or commit().
     * <p>
     * <b>Symmetry with ObservableFeature:</b>
     * <ul>
     *   <li>ObservableFeature → read-only, immediate DB writes</li>
     *   <li>ObservableFeature.Editable → editable, cached changes</li>
     * </ul>
     * <p>
     * Subclasses:
     * <ul>
     *   <li>{@link ObservableField.Editable} - for semantic version fields</li>
     * </ul>
     * <p>
     * Note: {@link ObservableFeatureDefinition.Editable} is NOT a subclass of ObservableFeature.Editable
     * because {@link ObservableFeatureDefinition} does not extend {@link ObservableFeature} - it directly
     * implements the Feature interface. However, ObservableFeatureDefinition.Editable provides the same
     * API surface for consistency.
     *
     * @param <DT> the data type of the feature value
     */
    public abstract static sealed class Editable<DT>
            permits ObservableField.Editable {

        /**
         * Composite key for caching editable features.
         */
        record EditableFeatureKey(int editableVersionNid, int stampNid, int featureIndex) {}

        /**
         * Caffeine cache with weak values for canonical editable feature instances.
         * Ensures one editable feature per (editableVersion, stamp, featureIndex) combination.
         */
        private static final Cache<EditableFeatureKey, Editable<?>> EDITABLE_FEATURE_CACHE =
                Caffeine.newBuilder()
                        .weakValues()
                        .build();

        protected final ObservableFeature<DT> observableFeature;
        protected final SimpleObjectProperty<DT> editableValueProperty;
        protected final int featureIndex;

        /**
         * Package-private constructor.
         *
         * @param observableFeature the read-only feature to wrap
         * @param initialValue the initial value for the editable property
         * @param featureIndex the index of this feature in its container
         */
        Editable(ObservableFeature<DT> observableFeature, DT initialValue, int featureIndex) {
            this.observableFeature = observableFeature;
            this.featureIndex = featureIndex;
            this.editableValueProperty = new SimpleObjectProperty<>(this, "value", initialValue);
        }

        /**
         * Gets or creates a canonical editable feature.
         * Ensures the same editable feature instance is returned for the same key.
         */
        @SuppressWarnings("unchecked")
        static <DT, OEF extends Editable<DT>> OEF getOrCreate(
                EditableFeatureKey key,
                EditableFeatureFactory<DT, OEF> factory) {
            return (OEF) EDITABLE_FEATURE_CACHE.get(key, k -> factory.create());
        }

        /**
         * Returns the original read-only ObservableFeature.
         */
        public ObservableFeature<DT> getObservableFeature() {
            return observableFeature;
        }

        /**
         * Returns the editable property for GUI binding.
         * Changes to this property are cached and don't immediately affect the database.
         */
        public SimpleObjectProperty<DT> editableValueProperty() {
            return editableValueProperty;
        }

        /**
         * Returns the current cached value.
         */
        public DT getValue() {
            return editableValueProperty.get();
        }

        /**
         * Sets the cached value.
         */
        public void setValue(DT value) {
            editableValueProperty.set(value);
        }

        /**
         * Returns the field definition for this feature.
         */
        public FieldDefinition fieldDefinition(StampCalculator calculator) {
            return observableFeature.fieldDefinition(calculator);
        }

        /**
         * Returns the index of this feature in its container.
         */
        public int getFeatureIndex() {
            return featureIndex;
        }

        /**
         * Returns whether this editable feature has unsaved changes.
         */
        public boolean isDirty() {
            DT currentValue = editableValueProperty.get();
            DT originalValue = observableFeature.value();

            if (currentValue == null && originalValue == null) {
                return false;
            }
            if (currentValue == null || originalValue == null) {
                return true;
            }
            return !currentValue.equals(originalValue);
        }

        /**
         * Resets the editable value to match the original read-only feature.
         */
        public void reset() {
            editableValueProperty.set(observableFeature.value());
        }

        /**
         * Factory interface for creating editable features.
         */
        @FunctionalInterface
        interface EditableFeatureFactory<DT, OEF extends Editable<DT>> {
            OEF create();
        }
    }
}
