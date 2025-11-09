package dev.ikm.komet.framework.observable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * Generic abstract base for type-safe observable version implementations.
 * <p>
 * Provides the generic type parameters and shared implementation for all observable
 * entity version types. This is Layer 2 of the Marker-Generic-Concrete pattern,
 * sitting between the simple {@link ObservableVersion} marker interface and
 * concrete final implementations like {@link ObservableConceptVersion}.
 *
 * <h2>Pattern Structure</h2>
 * <ul>
 *   <li><b>Layer 1 (Marker):</b> {@link ObservableVersion} - Generic-free consumer API</li>
 *   <li><b>Layer 2 (Generic):</b> {@code ObservableEntityVersion<OE, V>} - Type-safe inheritance (this class)</li>
 *   <li><b>Layer 3 (Concrete):</b> {@link ObservableConceptVersion}, etc. - Full type reification</li>
 * </ul>
 *
 * <h2>Symmetry with Entity Hierarchy</h2>
 * <p>This class mirrors {@link ObservableEntity} in structure and purpose:
 * <pre>
 * {@code ObservableEntity<OV>}              ←→  {@code ObservableEntityVersion<OE, V>}
 * ├─ Generic type parameters             ├─ Generic type parameters
 * ├─ Shared version management           ├─ Shared field management
 * └─ Abstract wrap() method              └─ Abstract methods for subclasses
 * </pre>
 *
 * @param <OE> the observable entity type (e.g., {@link ObservableConcept})
 * @param <V> the entity version record type (e.g., {@link ConceptVersionRecord})
 */
public abstract sealed class ObservableEntityVersion<OE extends ObservableChronology, V extends EntityVersion>
        implements EntityVersion, ObservableComponent, ObservableVersion
        permits ObservableConceptVersion, ObservablePatternVersion,
                ObservableSemanticVersion, ObservableStampVersion {

    protected final ReadOnlyObjectWrapper<EntityVersion> versionProperty = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<State> stateProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyLongWrapper timeProperty = new ReadOnlyLongWrapper();
    private final ReadOnlyObjectWrapper<ConceptFacade> authorProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ConceptFacade> moduleProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ConceptFacade> pathProperty = new ReadOnlyObjectWrapper<>();

    private final Supplier<ReadOnlyProperty<Feature<ObservableVersion>>> featurePropertySupplier =
            StableValue.supplier(() -> new ReadOnlyObjectWrapper<>(this.getObservableEntity(),
                    this.getClass().getSimpleName(),
                    (Feature<ObservableVersion>) this).getReadOnlyProperty());


    private final OE observableEntity;

    ObservableEntityVersion(OE observableEntity, V entityVersion) {
        this.observableEntity = observableEntity;
        setVersionInternal(entityVersion);
    }

    /**
     * Package-private method to update version data. Only called from updateVersions() flow.
     */
    void setVersionInternal(EntityVersion entityVersion) {
        versionProperty.set(entityVersion);
        stateProperty.set(entityVersion.state());
        timeProperty.set(entityVersion.time());
        authorProperty.set(EntityHandle.get(entityVersion.authorNid()).expectConcept("Getting author nid: "));
        moduleProperty.set(EntityHandle.get(entityVersion.moduleNid()).expectConcept("Getting module nid: " + entityVersion.moduleNid() + " "));
        pathProperty.set(EntityHandle.get(entityVersion.pathNid()).expectConcept("Getting path nid: "));
    }

    /**
     * Returns the observable entity (chronology) containing this version.
     * <p>
     * Implements the marker interface method with concrete return type.
     */
    @Override
    public final OE getObservableEntity() {
        return observableEntity;
    }
    // TODO: replace with JEP 502: Stable Values when finalized, or for testing.
    AtomicInteger versionIndex = new AtomicInteger(-1);

    @Override
    public FeatureKey featureKey() {
        return FeatureKey.Entity.Version(nid(), stampNid());
    }

    @Override
    public ObservableComponent containingComponent() {
        return getObservableEntity();
    }

    public int nid() {
        return versionProperty.get().nid();
    }

    public V version() {
        return (V) versionProperty.getValue();
    }

    public ReadOnlyObjectProperty<? extends EntityVersion> versionProperty() {
        return versionProperty.getReadOnlyProperty();
    }

    @Override
    public Entity entity() {
        return version().entity();
    }

    @Override
    public int stampNid() {
        return version().stampNid();
    }

    @Override
    public Entity chronology() {
        return version().chronology();
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return stateProperty.getReadOnlyProperty();
    }

    public ReadOnlyLongProperty timeProperty() {
        return timeProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<ConceptFacade> authorProperty() {
        return authorProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<ConceptFacade> moduleProperty() {
        return moduleProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<ConceptFacade> pathProperty() {
        return pathProperty.getReadOnlyProperty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersionRecord().stampNid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ObservableVersion observableVersion) {
            return getVersionRecord().equals(observableVersion.getVersionRecord());
        }
        return false;
    }

    public abstract V getVersionRecord();

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + getVersionRecord().toString();
    }

    @Override
    public ReadOnlyProperty<? extends Feature<ObservableVersion>> featureProperty() {
        return this.featurePropertySupplier.get();
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionStampReference = new AtomicReference<>();
    private FeatureWrapper getVersionStampFeature() {
        return versionStampReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionStampFeature());
    }
    private FeatureWrapper makeVersionStampFeature() {
        FeatureKey locator = FeatureKey.Version.VersionStamp(this.nid(), this.stampNid());
        ObservableStamp stamp = ObservableEntityHandle.getStampOrThrow(stampNid());
        return new FeatureWrapper(stamp, Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.stampFieldDefinitionIndex(),this, locator);
    }

    @Override
    public final ImmutableList<Feature<?>> getFeatures() {
        // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature lists.
        MutableList<Feature<?>> features = Lists.mutable.empty();
        addAdditionalVersionFeatures(features);

        if (this instanceof ObservableStampVersion) {
            // if this is an ObservableStampVersion, the fields will be added as part of addAdditionalVersionFeatures.
        } else {
            // Add the stamp features. Other layouts may choose to handle the stamp fields differently.
            // TODO: question if we should include the stamp fields here for convenience, or have the developer specifically retrieve them if wanted.
            ObservableStamp stamp = ObservableEntityHandle.getStampOrThrow(stampNid());
            ObservableStampVersion stampEntityVersion = stamp.lastVersion();
            stampEntityVersion.addAdditionalVersionFeatures( features);
        }
        // Add the feature for the stamp itself.
        features.add(getVersionStampFeature());

        return features.toImmutable();
    }

    protected abstract void addAdditionalVersionFeatures(MutableList<Feature<?>> features);

    public Feature<?> getFeature(FeatureKey.VersionFeature versionFeatureKey) {
        return getFeatures().select(feature -> versionFeatureKey.match(feature.featureKey())).getOnly();
    }

    //TODO: added temporarily to support intermediate state of KL merge with main.
    //TODO: Need to eliminate author for changes and make sure editing is performed correctly.
    private ConceptFacade authorForChanges;

    public void setAuthorForChanges(ConceptFacade authorForChanges){
        this.authorForChanges = authorForChanges;
    }
    public ConceptFacade getAuthorForChanges(){
        return this.authorForChanges;
    }

    /**
     * Returns the canonical editable version for this ObservableVersion with the specified stamp.
     * <p>
     * The same stamp will always return the same editable version instance, allowing multiple
     * GUI components to bind to and edit the same working copy. Different stamps (e.g., for
     * different authors) will return different editable versions.
     * <p>
     * Uses {@link ObservableStamp} instead of immutable StampEntity because the stamp can change
     * from uncommitted to committed state during the editing lifecycle.
     * <p>
     * The editable version caches all field changes until either:
     * <ul>
     *   <li>{@link Editable#save()} - writes uncommitted version to database</li>
     *   <li>{@link Editable#commit()} - commits transaction and writes committed version</li>
     * </ul>
     *
     * @param editStamp the observable stamp to use for this editable version (typically identifies the author)
     * @return the canonical editable version for this stamp
     */
    public abstract EditableVersion getEditableVersion(ObservableStamp editStamp);

    /**
     * Base class for editable versions that cache field changes for GUI editing.
     * <p>
     * Implements {@link EditableVersion} marker interface to provide clean API usage.
     *
     * @param <OE> the observable entity type
     * @param <OV> the observable version type
     * @param <V> the entity version record type
     */
    public abstract static sealed class Editable<OE extends ObservableEntity<?>,
            OV extends ObservableEntityVersion, V extends EntityVersion>
            implements EditableVersion  // ← Only implement EditableVersion

            permits ObservableConceptVersion.Editable, ObservablePatternVersion.Editable,
            ObservableSemanticVersion.Editable, ObservableStampVersion.Editable {

        protected final OE observableEntity;
        protected final OV observableVersion;
        protected final ObservableStamp editStamp;
        protected V workingVersion;
        protected Transaction transaction;

        /**
         * Package-private constructor. Use ObservableVersion.getEditableVersion(stamp) to create instances.
         */
        Editable(OE observableEntity, OV observableVersion, ObservableStamp editStamp) {
            this.observableEntity = observableEntity;
            this.observableVersion = observableVersion;
            this.editStamp = editStamp;
            this.workingVersion = (V) observableVersion.getVersionRecord();
            this.transaction = null;
        }

        /**
         * Returns the original ObservableVersion being edited.
         * <p>
         * Implements {@link EditableVersion#getObservableVersion()}.
         */
        @Override
        public OV getObservableVersion() {
            return observableVersion;
        }

        /**
         * Returns the observable edit stamp for this editable version.
         * <p>
         * Implements {@link EditableVersion#getEditStamp()}
         */
        @Override
        public ObservableStamp getEditStamp() {
            return editStamp;
        }

        /**
         * Returns the current working version with all cached changes.
         * <p>
         * This is the version that will be saved/committed.
         */
        public V getWorkingVersion() {
            return workingVersion;
        }

        /**
         * Returns whether this editable version has unsaved changes.
         * <p>
         * Implements {@link EditableVersion#isDirty()}.
         */
        @Override
        public boolean isDirty() {
            return !workingVersion.equals(observableVersion.getVersionRecord());
        }

        /**
         * Saves the current working version as an uncommitted version to the database.
         * <p>
         * Implements {@link EditableVersion#save()}.
         */
        @Override
        public void save() {
            if (!isDirty()) {
                return;
            }

            // Create uncommitted stamp if needed
            if (transaction == null) {
                transaction = Transaction.make();
            }

            // Get or create uncommitted stamp
            StampEntity uncommittedStamp = transaction.getStampForEntities(
                    editStamp.lastVersion().state(),
                    editStamp.lastVersion().authorNid(),
                    editStamp.lastVersion().moduleNid(),
                    editStamp.lastVersion().pathNid(),
                    observableVersion.entity()
            );

            // Create new version with uncommitted stamp
            V newVersion = createVersionWithStamp(workingVersion, uncommittedStamp.nid());
            Entity<?> analogue = createAnalogue(newVersion);

            // Save to database - this will trigger update back to observable entity
            V oldVersion = (V) observableVersion.getVersionRecord();
            observableEntity.saveToDB(analogue, newVersion, oldVersion);

            // Update working version
            workingVersion = newVersion;
        }

        /**
         * Commits the transaction and writes the committed version to the database.
         * <p>
         * Implements {@link EditableVersion#commit()}.
         */
        @Override
        public void commit() {
            if (transaction != null) {
                transaction.commit();

                // After commit, create committed version
                StampEntity committedStamp = Entity.getStamp(editStamp.nid());
                V committedVersion = createVersionWithStamp(workingVersion, committedStamp.nid());
                Entity<?> analogue = createAnalogue(committedVersion);

                // Save committed version
                V oldVersion = workingVersion;
                observableEntity.saveToDB(analogue, committedVersion, oldVersion);

                workingVersion = committedVersion;
                transaction = null;
            }
        }

        /**
         * Discards all cached changes and reverts to the original version.
         * <p>
         * Implements {@link EditableVersion#reset()}.
         */
        @Override
        public void reset() {
            workingVersion = (V) observableVersion.getVersionRecord();
            if (transaction != null) {
                transaction.cancel();
                transaction = null;
            }
        }

        /**
         * Returns the observable chronology containing this editable version.
         * <p>
         * <b>Note:</b> Currently returns the entity containing the version.
         * This is a forward-compatible implementation for potential future
         * entity-level editing features.
         */
        public ObservableEntity<?> getObservableChronology() {
            return observableEntity;
        }

        /**
         * Creates a new version with the specified stamp nid.
         * Subclasses must implement this to provide type-specific version creation.
         */
        protected abstract V createVersionWithStamp(V version, int stampNid);

        /**
         * Creates an analogue (entity record) containing the specified version.
         * Subclasses must implement this to provide type-specific entity creation.
         */
        protected abstract Entity<?> createAnalogue(V version);

        /**
         * Factory interface for creating editable versions.
         */
        @FunctionalInterface
        interface EditableVersionFactory<OC extends ObservableEntity<? extends EntityVersion>,
                OV extends ObservableEntityVersion,
                V extends EntityVersion,
                OEV extends ObservableEntityVersion.Editable<OC, OV, V>> {
            OEV create(OC observableChronology, OV observableVersion, ObservableStamp editStamp);
        }
    }

    /**
     * Composite key combining component nid and stamp nid for cache lookup.
     * <p>
     * This record ensures that each combination of component (entity) and stamp
     * maps to exactly one canonical editable version instance.
     *
     * @param nid the nid of the component (entity) whose version is being edited
     * @param stampNid the nid of the ObservableStamp (typically identifying the author)
     */
    record EditableVersionKey(int nid, int stampNid) {}

    /**
     * Caffeine cache with weak values ensuring canonical editable versions.
     * <p>
     * Uses weak references so that editable versions are automatically garbage collected
     * when no longer referenced by GUI components or application code, preventing memory leaks
     * from unused editing sessions.
     * <p>
     * The cache is thread-safe and lock-free, avoiding the need for synchronized blocks
     * while maintaining the canonical instance guarantee.
     */
    private static final Cache<EditableVersionKey, EditableVersion>
            EDITABLE_VERSION_CACHE =
            Caffeine.newBuilder()
                    .weakValues()
                    .build();

    /**
     * Gets or creates the canonical editable version for the given observable version and stamp.
     * <p>
     * <b>Canonical Instance Guarantee:</b> For any given combination of ObservableVersion and ObservableStamp,
     * this method will always return the same Editable instance. Multiple calls with the same
     * stamp will return the exact same object reference.
     * <p>
     * This ensures that all GUI components binding to the same stamp for editing share the same working copy,
     * preventing inconsistent state.
     * <p>
     * <b>Thread-Safe and Lock-Free:</b> Uses Caffeine's atomic get-or-create operation, avoiding the need
     * for synchronized blocks while maintaining the canonical instance guarantee under concurrent access.
     *
     * @param observableVersion the ObservableVersion to create an editable version for
     * @param editStamp the ObservableStamp identifying the editor (typically the author)
     * @param mappingFunction "maps" the key to a new instance if needed
     * @return the canonical editable version for this version+stamp combination
     */
    static <OE extends ObservableEntity<? extends EntityVersion>,
            OV extends ObservableEntityVersion,
            V extends EntityVersion,
            OEV extends ObservableEntityVersion.Editable<OE, OV, V>>

    OEV getOrCreate(OE observableEntity,
                    OV observableVersion,
                    ObservableStamp editStamp,
                    Editable.EditableVersionFactory<OE, OV, V, OEV> mappingFunction) {
        // Create a composite key using the component's nid (not version nid - versions don't have their own nid)
        // Caffeine's get() with mapping function is atomic and thread-safe
        // If the key exists, returns the existing value
        // If the key doesn't exist, calls the mappingFunction function exactly once and caches the result
        return (OEV) EDITABLE_VERSION_CACHE.get(new EditableVersionKey(observableVersion.nid(), editStamp.nid()),
                k -> mappingFunction.create(observableEntity, observableVersion, editStamp));
    }
}

