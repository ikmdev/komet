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
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public abstract sealed class ObservableVersion<V extends EntityVersion>
        implements EntityVersion, ObservableComponent, Feature<ObservableVersion<?>>
        permits ObservableConceptVersion, ObservablePatternVersion, ObservableSemanticVersion, ObservableStampVersion {
    protected final SimpleObjectProperty<V> versionProperty = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<State> stateProperty = new SimpleObjectProperty<>();
    private final SimpleLongProperty timeProperty = new SimpleLongProperty();
    private final SimpleObjectProperty<ConceptFacade> authorProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ConceptFacade> moduleProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ConceptFacade> pathProperty = new SimpleObjectProperty<>();

    // Replace with JEP 502 (Stable Values) when available.
    private final AtomicReference<ReadOnlyProperty<Feature<ObservableVersion<?>>>> featurePropertyReference = new AtomicReference<>();




    ObservableVersion(V entityVersion) {
        versionProperty.set(entityVersion);
        stateProperty.set(entityVersion.state());
        timeProperty.set(entityVersion.time());
        authorProperty.set(Entity.provider().getEntityFast(entityVersion.authorNid()));
        moduleProperty.set(Entity.provider().getEntityFast(entityVersion.moduleNid()));
        pathProperty.set(Entity.provider().getEntityFast(entityVersion.pathNid()));
        addListeners();
    }

    public abstract ObservableEntity<? extends ObservableVersion> getObservableEntity();

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

    protected void addListeners() {
        stateProperty.addListener((observable, oldValue, newValue) -> {
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(newValue, version().time(),
                            version().authorNid(), version().moduleNid(), version().pathNid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });

        timeProperty.addListener((observable, oldValue, newValue) -> {
            // TODO when to update the chronology with new record? At commit time? Automatically with reactive stream for commits?
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(version().state(), newValue.longValue(),
                            version().authorNid(), version().moduleNid(), version().pathNid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });

        authorProperty.addListener((observable, oldValue, newValue) -> {
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(version().state(), version().time(),
                            newValue.nid(), version().moduleNid(), version().pathNid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });

        moduleProperty.addListener((observable, oldValue, newValue) -> {
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(version().state(), version().time(),
                            version().authorNid(), newValue.nid(), version().pathNid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });

        pathProperty.addListener((observable, oldValue, newValue) -> {
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(version().state(), version().time(),
                            version().authorNid(), version().moduleNid(), newValue.nid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });
    }

    public V version() {
        return versionProperty.getValue();
    }

    protected abstract V withStampNid(int stampNid);

    public ObjectProperty<V> versionProperty() {
        return versionProperty;
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

    public ObjectProperty<State> stateProperty() {
        return stateProperty;
    }

    public LongProperty timeProperty() {
        return timeProperty;
    }

    public ObjectProperty<ConceptFacade> authorProperty() {
        return authorProperty;
    }

    public ObjectProperty<ConceptFacade> moduleProperty() {
        return moduleProperty;
    }

    public ObjectProperty<ConceptFacade> pathProperty() {
        return pathProperty;
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
    public ReadOnlyProperty<? extends Feature<ObservableVersion<?>>> featureProperty() {
        // Replace with JEP 502 (Stable Values) when available.
        return this.featurePropertyReference.updateAndGet(old -> old == null ?
                new ReadOnlyObjectWrapper<>(this.getObservableEntity(), this.getClass().getSimpleName(), (Feature<ObservableVersion<?>>) this).getReadOnlyProperty(): old);
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
        ObservableStamp stamp = ObservableEntity.get(stampNid());
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
            ObservableStamp stamp = ObservableEntity.get(stampNid());
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
}
