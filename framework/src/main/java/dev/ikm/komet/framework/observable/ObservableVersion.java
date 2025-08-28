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

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract sealed class ObservableVersion<V extends EntityVersion>
        implements EntityVersion, ObservableComponent
        permits ObservableConceptVersion, ObservablePatternVersion, ObservableSemanticVersion, ObservableStampVersion {
    protected final SimpleObjectProperty<V> versionProperty = new SimpleObjectProperty<>();

    private final EntityVersion entityVersion;

    final SimpleObjectProperty<State> stateProperty = new SimpleObjectProperty<>();
    final SimpleLongProperty timeProperty = new SimpleLongProperty();
    final SimpleObjectProperty<ConceptFacade> authorProperty = new SimpleObjectProperty<>();
    final SimpleObjectProperty<ConceptFacade> moduleProperty = new SimpleObjectProperty<>();
    final SimpleObjectProperty<ConceptFacade> pathProperty = new SimpleObjectProperty<>();


    ObservableVersion(V entityVersion) {
        versionProperty.set(entityVersion);
        this.entityVersion = entityVersion;
        stateProperty.set(entityVersion.state());
        timeProperty.set(entityVersion.time());
        authorProperty.set(Entity.provider().getEntityFast(entityVersion.authorNid()));
        moduleProperty.set(Entity.provider().getEntityFast(entityVersion.moduleNid()));
        pathProperty.set(Entity.provider().getEntityFast(entityVersion.pathNid()));
        addListeners();
    }

    public abstract ImmutableMap<FieldCategory, ObservableField> getObservableFields();

    protected void addListeners() {
        stateProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                if (version().uncommitted()) {
                    Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                        transaction.forEachStampInTransaction(stampUuid -> {
                            StampEntity newStamp = updateUncommitedTransactionStampValue(stampUuid, newValue, version().author(), version().module(), version().path());
                            versionProperty.set(withStampNid(newStamp.nid()));
                        });
                    }, () -> {
                        createNewTransaction(newValue, version().author(), version().module(), version().path());
                    });
                } else {
                    createNewTransaction(newValue, version().author(), version().module(), version().path());
                }
            }
        });

        timeProperty.addListener((observable, oldValue, newValue) -> {
            // TODO when to update the chronology with new record? At commit time? Automatically with reactive stream for commits?
            if (version().uncommitted()) {
                Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                    StampEntity newStamp = transaction.getStamp(version().state(), newValue.longValue(), version().authorNid(), version().moduleNid(), version().pathNid());
                    versionProperty.set(withStampNid(newStamp.nid()));
                }, () -> {
                    throw new IllegalStateException("No transaction for uncommitted version: " + version());
                });
            } else {
                //Are we allowed to change the time and have it autosave ?
                throw new IllegalStateException("Version is already committed, cannot change value.");
            }
        });

        authorProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue !=null ){
                if (version().uncommitted()) {
                    Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                        transaction.forEachStampInTransaction(stampUuid -> {
                            StampEntity newStamp = updateUncommitedTransactionStampValue(stampUuid, version().state(), newValue, version().module(), version().path());
                            versionProperty.set(withStampNid(newStamp.nid()));
                        });
                    }, () -> {
                        createNewTransaction(version().state(), newValue, version().module(), version().path());
                    });
                } else {
                    createNewTransaction(version().state(), newValue, version().module(), version().path());
                }
            }

        });

        moduleProperty.addListener((observable, oldValue, newValue) -> {
            if( newValue != null){
                if (version().uncommitted()) {
                    Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                        transaction.forEachStampInTransaction(stampUuid -> {
                            StampEntity newStamp = updateUncommitedTransactionStampValue(stampUuid, version().state(), version().author(), newValue, version().path());
                            versionProperty.set(withStampNid(newStamp.nid()));
                        });
                    }, () -> {
                        createNewTransaction(version().state(), version().author(), newValue, version().path());
                    });
                } else {
                    createNewTransaction(version().state(), version().author(), newValue, version().path());
                }
            }

        });

        pathProperty.addListener((observable, oldValue, newValue) -> {
            if( newValue != null){
                if (version().uncommitted()) {
                    Transaction.forVersion(version()).ifPresentOrElse(transaction -> {
                        transaction.forEachStampInTransaction(stampUuid -> {
                           StampEntity newStamp = updateUncommitedTransactionStampValue(stampUuid, version().state(), version().author(), version().module(), newValue);
                           versionProperty.set(withStampNid(newStamp.nid()));
                        });
                    }, () -> {
                        createNewTransaction(version().state(), version().author(), version().module(), newValue);
                    });
                } else {
                    createNewTransaction(version().state(), version().author(), version().module(), newValue);
                }
            }
        });
    }

    private StampEntity updateUncommitedTransactionStampValue(UUID stampUuid, State state, ConceptFacade author, ConceptFacade module, ConceptFacade path) {
        Optional<StampEntity> optionalStamp = Entity.get(PrimitiveData.nid(stampUuid));
        if (optionalStamp.isEmpty()) {
            StampEntity stamp = StampRecord.make(stampUuid, state, version().time(), author.publicId(), module.publicId(), path.publicId());
            Entity.provider().putEntity(stamp);
            return stamp;
        }
        return optionalStamp.get();
    }

    private void createNewTransaction(State state, ConceptFacade author, ConceptFacade module, ConceptFacade path) {
        Transaction t = Transaction.make();
        // newStamp already written to the entity store.
        StampEntity<?> newStamp = t.getStampForEntities(state, author.nid(), module.nid(), path.nid(), entity());
        // Create new version...
        versionProperty.set(withStampNid(newStamp.nid()));
    }

    public V version() {
        return versionProperty.getValue();
    }

    public EntityVersion getEntityVersion(){
        return this.entityVersion;
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
}
