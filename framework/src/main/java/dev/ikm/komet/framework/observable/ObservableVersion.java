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
     *   <li>{@link ObservableEditableVersion#save()} - writes uncommitted version to database</li>
     *   <li>{@link ObservableEditableVersion#commit()} - commits transaction and writes committed version</li>
     * </ul>
     *
     * @param editStamp the observable stamp to use for this editable version (typically identifies the author)
     * @return the canonical editable version for this stamp
     */
    public abstract ObservableEditableVersion<?, ?, ?> getEditableVersion(StampEntity editStamp);
}
