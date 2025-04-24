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

import static dev.ikm.komet.framework.events.EntityVersionChangeEvent.VERSION_UPDATED;
import static dev.ikm.komet.framework.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import dev.ikm.komet.framework.events.EntityVersionChangeEvent;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO: should be a way of listening for changes to the versions of the entity? Yes, use the versionProperty()...
 *
 * @param <O>
 * @param <V>
 */
public abstract sealed class ObservableEntity<O extends ObservableVersion<V>, V extends EntityVersion>
        implements Entity<O>, ObservableComponent
        permits ObservableConcept, ObservablePattern, ObservableSemantic, ObservableStamp {

    /**
     * The concurrent reference map whose reference type is SOFT.
     * TODO revisit reference type (WEAK vs SOFT)
     */
    protected static final ConcurrentReferenceHashMap<Integer, ObservableEntity> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.SOFT,
                    ConcurrentReferenceHashMap.ReferenceType.SOFT);
    private final EntityChangeSubscriber ENTITY_CHANGE_SUBSCRIBER = new EntityChangeSubscriber();

    {
        Entity.provider().addSubscriberWithWeakReference(ENTITY_CHANGE_SUBSCRIBER);
    }

    final SimpleListProperty<O> versionProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    final private AtomicReference<Entity<V>> entityReference;

    /**
     * Saves the uncommited entity version to the DB and fires event (VERSION_UPDATED).
     * it also adds the version to the versionProperty list.
     * @param analogue the entity record
     * @param newVersionRecord entity version record
     */
    public void saveToDB(Entity<?> analogue, EntityVersion newVersionRecord ) {
        Entity.provider().putEntity(analogue);
        versionProperty.add(wrap((V)newVersionRecord));
        EvtBusFactory.getDefaultEvtBus()
                .publish(VERSION_CHANGED_TOPIC, new EntityVersionChangeEvent(this, VERSION_UPDATED, newVersionRecord));
    }

    ObservableEntity(Entity<V> entity) {
        Entity<V> entityClone = switch (entity) {
            case ConceptRecord conceptEntity -> (Entity<V>) conceptEntity.analogueBuilder().build();

            case PatternRecord patternEntity -> (Entity<V>) patternEntity.analogueBuilder().build();

            case SemanticRecord semanticEntity -> (Entity<V>) semanticEntity.analogueBuilder().build();

            case StampRecord stampEntity -> (Entity<V>) stampEntity.analogueBuilder().build();

            default -> throw new UnsupportedOperationException("Can't handle: " + entity);
        };

        this.entityReference = new AtomicReference<>(entityClone);
        for (V version : entity.versions()) {
            versionProperty.add(wrap(version));
        }
    }

    protected abstract O wrap(V version);

    public abstract ImmutableMap<FieldCategory, ObservableField> getObservableFields();

    public static <OE extends ObservableEntity<OV, EV>, OV extends ObservableVersion<EV>, EV extends EntityVersion>
    ObservableEntitySnapshot<OE, OV, EV> getSnapshot(int nid, ViewCalculator calculator) {
        return get(Entity.getFast(nid)).getSnapshot(calculator);
    }

    public abstract ObservableEntitySnapshot<?,?,?> getSnapshot(ViewCalculator calculator);

    public static <OE extends ObservableEntity> OE get(Entity<? extends EntityVersion> entity) {
        if (entity instanceof ObservableEntity) {
            ObservableEntity observableEntity = (ObservableEntity) entity;
            updateVersions(observableEntity.entity(), observableEntity);
            return (OE) entity;
        }

        ObservableEntity observableEntity = SINGLETONS.computeIfAbsent(entity.nid(), publicId ->
                switch (entity) {
                    case ConceptEntity conceptEntity -> new ObservableConcept(conceptEntity);
                    case PatternEntity patternEntity -> new ObservablePattern(patternEntity);
                    case SemanticEntity semanticEntity -> new ObservableSemantic(semanticEntity);
                    case StampEntity stampEntity -> new ObservableStamp(stampEntity);
                    default -> throw new UnsupportedOperationException("Can't handle: " + entity);
                });
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateVersions(entity, observableEntity));
        } else {
            updateVersions(entity, observableEntity);
        }
        return (OE) observableEntity;
    }

    /**
     * updates the versions in the versionProperty list.
     * @param entity
     * @param observableEntity
     */
    public static void updateVersions(Entity<? extends EntityVersion> entity, ObservableEntity observableEntity) {
        if (!((Entity) observableEntity.entityReference.get()).versions().equals(entity.versions())) {
            observableEntity.entityReference.set(entity);
            observableEntity.versionProperty.clear();
            for (EntityVersion version : entity.versions().stream().sorted((v1, v2) ->
                    Long.compare(v1.stamp().time(), v2.stamp().time())).toList()) {
                observableEntity.versionProperty.add(observableEntity.wrap(version));
            }
        }
    }

    public static <OE extends ObservableEntity> OE get(int nid) {
        return get(Entity.getFast(nid));
    }

    protected Entity<V> entity() {
        return entityReference.get();
    }

    public ObservableList<O> versionProperty() {
        return versionProperty;
    }

    @Override
    public ImmutableList<O> versions() {
        return Lists.immutable.ofAll(versionProperty);
    }

    @Override
    public byte[] getBytes() {
        return entityReference.get().getBytes();
    }

    @Override
    public FieldDataType entityDataType() {
        return entityReference.get().entityDataType();
    }

    @Override
    public FieldDataType versionDataType() {
        return entityReference.get().versionDataType();
    }

    @Override
    public int nid() {
        return entityReference.get().nid();
    }

    @Override
    public long mostSignificantBits() {
        return entityReference.get().mostSignificantBits();
    }

    @Override
    public long leastSignificantBits() {
        return entityReference.get().leastSignificantBits();
    }

    @Override
    public long[] additionalUuidLongs() {
        return entityReference.get().additionalUuidLongs();
    }

    public Iterable<ObservableSemantic> getObservableSemanticList() {
        throw new UnsupportedOperationException();
    }

    private class EntityChangeSubscriber implements Subscriber<Integer> {

        @Override
        public void onNext(Integer nid) {
            // Do nothing with item, but request another...
            if (SINGLETONS.containsKey(PrimitiveData.publicId(nid))) {
                Platform.runLater(() -> {
                    get(Entity.getFast(nid));
                });

            }
        }
    }
}
