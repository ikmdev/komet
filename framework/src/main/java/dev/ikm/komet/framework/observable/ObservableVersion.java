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


public abstract sealed class ObservableVersion<OE extends ObservableEntity<?>, V extends EntityVersion>
        implements EntityVersion, ObservableComponent, Feature<ObservableVersion<OE, ?>>
        permits ObservableConceptVersion, ObservablePatternVersion, ObservableSemanticVersion, ObservableStampVersion {
    protected final ReadOnlyObjectWrapper<EntityVersion> versionProperty = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<State> stateProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyLongWrapper timeProperty = new ReadOnlyLongWrapper();
    private final ReadOnlyObjectWrapper<ConceptFacade> authorProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ConceptFacade> moduleProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ConceptFacade> pathProperty = new ReadOnlyObjectWrapper<>();

     private final Supplier<ReadOnlyProperty<Feature<ObservableVersion<OE, ?>>>> featurePropertySupplier =
        StableValue.supplier(() -> new ReadOnlyObjectWrapper<>(this.getObservableEntity(),
                this.getClass().getSimpleName(),
                (Feature<ObservableVersion<OE, ?>>) this).getReadOnlyProperty());


    private final OE observableEntity;

    ObservableVersion(OE observableEntity, V entityVersion) {
        this.observableEntity = observableEntity;
        setVersionInternal(entityVersion);
    }

    /**
     * Package-private method to update version data. Only called from updateVersions() flow.
     */
    void setVersionInternal(V entityVersion) {
        versionProperty.set(entityVersion);
        stateProperty.set(entityVersion.state());
        timeProperty.set(entityVersion.time());
        authorProperty.set(EntityHandle.get(entityVersion.authorNid()).expectConcept("Getting author nid: "));
        moduleProperty.set(EntityHandle.get(entityVersion.moduleNid()).expectConcept("Getting module nid: " + entityVersion.moduleNid() + " "));
        pathProperty.set(EntityHandle.get(entityVersion.pathNid()).expectConcept("Getting path nid: "));
    }

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

    protected abstract V withStampNid(int stampNid);

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
    public ReadOnlyProperty<? extends Feature<ObservableVersion<OE, ?>>> featureProperty() {
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
        return new FeatureWrapper(stamp.asFeature(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.stampFieldDefinitionIndex(),this, locator);
    }

    @Override
    public final ImmutableList<Feature> getFeatures() {
        // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature lists.
        MutableList<Feature> features = Lists.mutable.empty();
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

    protected abstract void addAdditionalVersionFeatures(MutableList<Feature> features);

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
    public abstract Editable<?, ?, ?> getEditableVersion(ObservableStamp editStamp);

    /**
     * Base class for editable versions that cache field changes for GUI editing.
     * <p>
     * An Editable wraps an ObservableVersion and provides:
     * <ul>
     *   <li>Cached field editing - changes accumulate without immediate database writes</li>
     *   <li>Exactly ONE canonical editable version per ObservableStamp - same stamp always returns same instance</li>
     *   <li>Multiple editable versions per ObservableVersion - different stamps (authors) can have separate edits</li>
     *   <li>save() - writes uncommitted version to database</li>
     *   <li>commit() - commits the transaction and writes committed version</li>
     * </ul>
     * <p>
     * <b>Canonical Reference Guarantee:</b> For any given ObservableStamp, there exists exactly ONE
     * Editable. Calling {@code getEditableVersion(stamp)} multiple times with the
     * same stamp will always return the same instance, ensuring all GUI components editing with that
     * stamp share the same working copy.
     * <p>
     * <b>Multiple Authors Support:</b> A single ObservableVersion can have multiple editable versions,
     * each associated with a different ObservableStamp (typically representing different authors).
     * This allows concurrent editing by different users without conflicts.
     * <p>
     * Uses {@link ObservableStamp} instead of immutable StampEntity because the stamp
     * can change from uncommitted to committed state during the editing lifecycle.
     *
     * @param <OV> the observable version type
     * @param <V> the entity version record type
     */
    public abstract static sealed class Editable<OE extends ObservableEntity<OV>,
            OV extends ObservableVersion<OE, V>, V extends EntityVersion>

            permits ObservableConceptVersion.Editable, ObservablePatternVersion.Editable,
                    ObservableSemanticVersion.Editable, ObservableStampVersion.Editable {

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
        private static final Cache<EditableVersionKey, Editable<?, ?, ?>> EDITABLE_VERSION_CACHE =
                Caffeine.newBuilder()
                        .weakValues()
                        .build();

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
            this.workingVersion = observableVersion.version();
            this.transaction = null;
        }

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
         * @param factory factory to create new instances if needed
         * @return the canonical editable version for this version+stamp combination
         */
        @SuppressWarnings("unchecked")
        static <OE extends ObservableEntity<OV>, OV extends ObservableVersion<OE, V>, V extends EntityVersion, OEV extends Editable<OE, OV, V>>

        OEV getOrCreate(OE observableEntity, OV observableVersion, ObservableStamp editStamp, EditableVersionFactory<OE, OV, V, OEV> factory) {
            // Create composite key using the component's nid (not version nid - versions don't have their own nid)
            EditableVersionKey key = new EditableVersionKey(observableVersion.nid(), editStamp.nid());

            // Caffeine's get() with mapping function is atomic and thread-safe
            // If the key exists, returns the existing value
            // If the key doesn't exist, calls the mapping function exactly once and caches the result
            return (OEV) EDITABLE_VERSION_CACHE.get(key, k -> factory.create(observableEntity, observableVersion, editStamp));
        }

        /**
         * Returns the original ObservableVersion being edited.
         */
        public OV getObservableVersion() {
            return observableVersion;
        }

        /**
         * Returns the observable edit stamp for this editable version.
         * The stamp may change from uncommitted to committed during the editing lifecycle.
         */
        public ObservableStamp getEditStamp() {
            return editStamp;
        }

        /**
         * Returns the current working version with all cached changes.
         */
        public V getWorkingVersion() {
            return workingVersion;
        }

        /**
         * Returns whether this editable version has unsaved changes.
         */
        public boolean isDirty() {
            return !workingVersion.equals(observableVersion.version());
        }

        /**
         * Saves the current working version as an uncommitted version to the database.
         * The uncommitted version will be reflected back to the observable entity.
         */
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
            V oldVersion = observableVersion.version();
            observableEntity.saveToDB(analogue, newVersion, oldVersion);

            // Update working version
            workingVersion = newVersion;
        }

        /**
         * Commits the transaction and writes the committed version to the database.
         * This finalizes all changes and makes them permanent.
         */
        public void commit() {
            if (transaction != null) {
                transaction.commit();

                // After commit, create committed version
                StampEntity committedStamp = Entity.getStamp(editStamp.nid());
                V committedVersion = createVersionWithStamp(workingVersion, committedStamp.nid());
                Entity<?> analogue = createAnalogue(committedVersion);

                // Save committed version
                V oldVersion = workingVersion;
                observableVersion.getObservableEntity().saveToDB(analogue, committedVersion, oldVersion);

                workingVersion = committedVersion;
                transaction = null;
            }
        }

        /**
         * Discards all cached changes and reverts to the original version.
         */
        public void reset() {
            workingVersion = observableVersion.version();
            if (transaction != null) {
                transaction.cancel();
                transaction = null;
            }
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
        interface EditableVersionFactory<OE extends ObservableEntity<OV>, OV extends ObservableVersion<OE, V>,
                V extends EntityVersion, OEV extends Editable<OE, OV, V>> {
            OEV create(OE observableEntity, OV observableVersion, ObservableStamp editStamp);
        }
    }
}
