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

import static dev.ikm.tinkar.events.EntityVersionChangeEvent.VERSION_UPDATED;
import static dev.ikm.tinkar.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.events.EntityVersionChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO: should be a way of listening for changes to the versions of the entity? Yes, use the versionProperty()...
 *
 * @param <OV>
 */
public abstract sealed class ObservableEntity<OV extends ObservableVersion<?>>
        implements Entity<OV>, ObservableComponent
        permits ObservableConcept, ObservablePattern, ObservableSemantic, ObservableStamp {

    /**
     * Canonical object pool ensuring a single in-memory instance per entity using weak references.
     * <p>
     * When an entity is in memory, all observers operate on the same object instance, ensuring
     * all changes are visible to all observers. Weak references allow automatic cleanup when
     * no observers hold the entity anymore.
     *
     * <h3>Migration from SOFT to WEAK References</h3>
     * <p>
     * <b>Previous Implementation:</b> Used SOFT references via {@code ConcurrentReferenceHashMap}.
     * <p>
     * <b>Current Implementation:</b> Uses WEAK references via Caffeine cache with {@code weakValues()}.
     *
     * <h3>Why WEAK is Superior for This Use Case</h3>
     * <ul>
     *   <li><b>Immediate Cleanup:</b> Entities are GC'd as soon as all observers release them
     *       (no strong references remain). This prevents memory buildup from unused entities.</li>
     *   <li><b>"Flyweight While In Use" Semantics:</b> Shared instance during active use
     *       (UI bound, listeners attached), automatic cleanup when done.</li>
     *   <li><b>Predictable Memory Behavior:</b> No accumulation of unused entities waiting
     *       for memory pressure. Memory is reclaimed promptly when observers are done.</li>
     * </ul>
     *
     * <h3>Why NOT SOFT References</h3>
     * <ul>
     *   <li><b>SOFT references persist too long:</b> They're only cleared when the JVM is
     *       under memory pressure, which may not occur even when entities are no longer needed.</li>
     *   <li><b>Memory Accumulation:</b> Unused entities can accumulate indefinitely if memory
     *       pressure doesn't trigger GC, defeating the purpose of automatic cleanup.</li>
     *   <li><b>Unpredictable Cleanup:</b> Cleanup timing depends on JVM memory state rather
     *       than actual usage, making behavior less deterministic.</li>
     * </ul>
     *
     * <h3>Behavior Guarantee</h3>
     * <p>
     * While any code holds a strong reference to an {@code ObservableEntity} (e.g., stored in
     * a variable, bound to UI, registered as listener), the entity remains in the cache and all
     * code accessing the same nid receives the same instance. When all strong references are
     * released, GC can collect the entity and the cache entry is automatically cleared.
     *
     * <h3>Implementation Notes</h3>
     * <p>
     * Migrated from legacy {@code ConcurrentReferenceHashMap} (2600 lines, pre-Java 8
     * segment-based locking) to modern Caffeine cache (actively maintained, optimized for
     * Java 8+ with better concurrency characteristics and cleaner API).
     *
     * @see com.github.benmanes.caffeine.cache.Cache
     * @see java.lang.ref.WeakReference
     */
    protected static final Cache<Integer, ObservableEntity> SINGLETONS =
            Caffeine.newBuilder()
                    .weakValues()
                    .build();
    private static final EntityChangeSubscriber ENTITY_CHANGE_SUBSCRIBER = new EntityChangeSubscriber();

    static {
        Entity.provider().addSubscriberWithWeakReference(ENTITY_CHANGE_SUBSCRIBER);
    }

    private final FeatureList<OV> versionSetAsList;

    private MutableIntObjectMap<OV> versionPropertyMap = new IntObjectHashMap<>();

    final private AtomicReference<Entity<?>> entityReference;

    /**
     * Saves the uncommited entity version to the DB and fires event (VERSION_UPDATED).
     * it also adds the version to the versionProperty list.
     * @param analogue the entity record
     * @param newVersionRecord entity version record
     */
    public void saveToDB(Entity<?> analogue, EntityVersion newVersionRecord , EntityVersion oldVersionRecord) {
        Entity.provider().putEntity(analogue);
        versionPropertyMap.put(newVersionRecord.stamp().nid(), wrap(newVersionRecord));
        EvtBusFactory.getDefaultEvtBus()
                .publish(VERSION_CHANGED_TOPIC, new EntityVersionChangeEvent(this, VERSION_UPDATED, newVersionRecord));
    }

    ObservableEntity(Entity<?> entity) {
        Entity<?> entityClone = switch (entity) {
            case ConceptRecord conceptEntity -> conceptEntity.analogueBuilder().build();

            case PatternRecord patternEntity -> patternEntity.analogueBuilder().build();

            case SemanticRecord semanticEntity -> semanticEntity.analogueBuilder().build();

            case StampRecord stampEntity -> stampEntity.analogueBuilder().build();

            default -> throw new UnsupportedOperationException("Can't handle: " + entity);
        };
        this.versionSetAsList = FeatureList.makeEmptyList(FeatureKey.Entity.VersionSet(entity.nid()),
                Binding.Component.pattern(), Binding.Component.versionsFieldDefinitionIndex(), this);

        this.entityReference = new AtomicReference<>(entityClone);
        for (EntityVersion version : entity.versions()) {
            OV wrappedVersion = wrap(version);
            versionPropertyMap.put(version.stamp().nid(), wrappedVersion);
            versionSetAsList.add(wrappedVersion);
        }
    }

    protected abstract OV wrap(EntityVersion version);

    public static <OE extends ObservableEntity<OV>, OV extends ObservableVersion<EV>, EV extends EntityVersion>
    ObservableEntitySnapshot<OE, OV> getSnapshot(int nid, ViewCalculator calculator) {
        return get(Entity.getFast(nid)).getSnapshot(calculator);
    }

    public abstract ObservableEntitySnapshot<?,?> getSnapshot(ViewCalculator calculator);

    public static <OE extends ObservableEntity> OE get(Entity<? extends EntityVersion> entity) {
        if (!Platform.isFxApplicationThread()) {
            //Throw exception since we need to get the version using JavaFx thread.
            throw new RuntimeException( "Invalid calling thread.");
        }
        ObservableEntity observableEntity = switch (entity) {
            case ObservableEntity oe -> oe;
            case ConceptEntity conceptEntity -> new ObservableConcept(conceptEntity);
            case PatternEntity patternEntity -> new ObservablePattern(patternEntity);
            case SemanticEntity semanticEntity -> new ObservableSemantic(semanticEntity);
            case StampEntity stampEntity -> new ObservableStamp(stampEntity);
            default -> throw new UnsupportedOperationException("Can't handle: " + entity);
        };
        observableEntity.updateVersions(entity);
        return (OE) observableEntity;
    }

    /**
     * Updates the versions in the versionProperty list.
     * @param newEntity
     */
    private void updateVersions(Entity<? extends EntityVersion> newEntity) {
        // Entities are immutable, so if the entity identities are the same, then the versions are also the same.
        // Versions can never be removed, we are append only. Do not have to check for deletions, just additions or
        // updates.
        if (entityReference.get() != newEntity) {
            final AtomicBoolean changed = new AtomicBoolean(false);
            // Find if there is a changed version...
            for (EntityVersion newVersion: newEntity.versions()) {
                OV oldVersion = versionPropertyMap.get(newVersion.stampNid());
                if (oldVersion == null) {
                    changed.set(true);
                    OV newWrappedVersion = wrap(newVersion);
                    versionPropertyMap.put(newVersion.stampNid(), newWrappedVersion);
                    versionSetAsList.add(oldVersion);
                } else if (oldVersion.version().time() == Long.MAX_VALUE) {
                    changed.set(true);
                    int index = versionSetAsList.indexOf(oldVersion);
                    OV newWrappedVersion = wrap(newVersion);
                    versionPropertyMap.put(newVersion.stampNid(), newWrappedVersion);
                    versionSetAsList.set(index, newWrappedVersion);
                } else if (newVersion.time() != oldVersion.version().time()) {
                    throw new IllegalStateException("Version time mismatch: " + newVersion.time() + " != " + oldVersion.version().time());
                }
                if (changed.get()) {
                    entityReference.set(newEntity);
                }
            }
        }
    }

    public static <OE extends ObservableEntity> OE get(int nid) {
        return get(Entity.getFast(nid));
    }

    protected Entity<?> entity() {
        return entityReference.get();
    }

    public MutableIntObjectMap<OV> versionPropertyMap() {
        return versionPropertyMap;
    }

    @Override
    public ImmutableList<OV> versions() {
        return Lists.immutable.ofAll(versionPropertyMap.values());
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


    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    final AtomicReference<FeatureWrapper> publicIdFeatureReference = new AtomicReference<>();
    private FeatureWrapper getPublicIdFeature() {
        return publicIdFeatureReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makePublicIdFeature());
    }
    private FeatureWrapper makePublicIdFeature() {
        return new FeatureWrapper(this.publicId(),
                Binding.Component.pattern().nid(),
                Binding.Component.publicIdFieldDefinitionIndex(),
                this,
                FeatureKey.Entity.PublicId(this.nid()));
    }

    @Override
    public final ImmutableList<Feature> getFeatures() {
        // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature lists.
        // TODO: Handle changes in StampCalculator.
        MutableList<Feature> features = Lists.mutable.empty();

        // Public ID:
        features.add(getPublicIdFeature());
        // Versions
        features.add(this.versionSetAsList);

        for (OV version : versions()) {
            features.add(version);
        }

        addAdditionalChronologyFeatures(features);

        return features.toImmutable();
    }

    protected abstract void addAdditionalChronologyFeatures(MutableList<Feature> features);

    public Feature<?> getFeature(FeatureKey featureKey) {
        return switch (featureKey) {
            case FeatureKey.ChronologyFeature chronologyFeatureKey -> getFeatures().select(feature -> chronologyFeatureKey.match(feature.featureKey())).getOnly();
            case FeatureKey.VersionFeature versionFeatureKey -> getVersion(versionFeatureKey.stampNid()).get().getFeature(versionFeatureKey);
        };
    }

    public final class EntityFeature implements Feature<ObservableEntity<OV>> {

        private EntityFeature() {
        }

        @Override
        public ReadOnlyProperty<? extends Feature<ObservableEntity<OV>>> featureProperty() {
            return entityFeatureWrapper;
        }

        @Override
        public FeatureKey featureKey() {
            return FeatureKey.Entity.Object(ObservableEntity.this.nid());
        }

        @Override
        public ObservableComponent containingComponent() {
            return ObservableEntity.this;
        }

        @Override
        public int patternNid() {
            return switch (ObservableEntity.this) {
                case ObservableConcept _-> Binding.Concept.pattern().nid();
                case ObservablePattern _-> Binding.Pattern.pattern().nid();
                case ObservableSemantic _-> Binding.Semantic.pattern().nid();
                case ObservableStamp _-> Binding.Stamp.pattern().nid();
            };
        }

        @Override
        public int indexInPattern() {
            // TODO: replace with a new index after adding a new field for the meaning and purpose of the entity class
            return 0;
        }

        @Override
        public String toString() {
            return ObservableEntity.this.getClass().getSimpleName() + " Feature<" + nid() + "> " + PrimitiveData.text(nid());
        }

    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private final ReadOnlyObjectProperty<? extends Feature<ObservableEntity<OV>>> entityFeatureWrapper =
            new ReadOnlyObjectWrapper<>(this, this.getClass().getSimpleName(), new ObservableEntity<OV>.EntityFeature()).getReadOnlyProperty();

    private ReadOnlyProperty<? extends Feature<ObservableEntity<OV>>> featureProperty() {
        return entityFeatureWrapper;
    }

    public Feature<ObservableEntity<OV>> asFeature() {
        return entityFeatureWrapper.getValue();
    }

    private static class EntityChangeSubscriber implements Subscriber<Integer> {

        @Override
        public void onNext(Integer nid) {
            // Do nothing with item, but request another...
            if (SINGLETONS.getIfPresent(nid) != null) {
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(() -> get(nid));
                } else {
                    get(nid);
                }
            }
        }
    }
}
