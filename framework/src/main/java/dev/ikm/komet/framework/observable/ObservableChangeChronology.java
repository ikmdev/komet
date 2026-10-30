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
import dev.ikm.komet.framework.observable.key.VersionKey;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.coordinate.stamp.change.FieldChangeRecord;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityText;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * A live, listenable view of one entity's change chronology: what each version of the entity
 * changed against the version it supersedes, kept current as versions arrive, are edited,
 * commit and cancel. There is one chronology per entity, keyed by nid alone and shared by every
 * view of the entity whatever its coordinate, since a {@link ChangeChronology} depends on no
 * coordinate: a semantic's fields are recorded by index against its own pattern, and a view
 * names them through its own calculator ({@link FieldRecord#fieldDefinition}).
 *
 * <h2>One diff algorithm</h2>
 * <p>{@link StampCalculator#changeChronology(Entity)} is the only code that works out
 * predecessors and field differences. On each event this view reruns it on the record the
 * canonical {@link ObservableEntity} holds, not on a fresh read, and reconciles the result into
 * {@link #changes()} by {@link VersionKey}: an entry whose changes are the same keeps the same
 * object, so bindings and selection survive; an entry whose changes differ is replaced; an entry
 * that is gone is removed. Listeners get ordinary JavaFX list changes:
 * <ul>
 *   <li>a new version is an add;</li>
 *   <li>a cancel or withdrawal is a remove;</li>
 *   <li>an edit to a pending version, or a commit, is a replace;</li>
 *   <li>a revision of a semantic's pattern re-announces each entry as a replace with the same
 *       {@link VersionChange}: the records do not change, since fields are recorded by index, but
 *       their names might, so bound views re-label.</li>
 * </ul>
 *
 * <h2>Event sources</h2>
 * <p>{@link ObservableEntity#updateVersions} is the only place an observable changes, and it
 * runs on the FX thread. This view listens to the entity's version list (a new version is an
 * add, a canceled or withdrawn one a removal), to the version property of each uncommitted
 * version (a commit, or an edit to a pending version, is set in place; committed versions never
 * change) and, for a semantic, to its pattern's version list.
 *
 * <h2>Lifetime</h2>
 * <p>Reached through {@link ObservableEntityHandle#getChangeChronologyOrThrow(long)}, next to the
 * snapshot. Held weakly in a cache keyed by nid, like the canonical observables, and holds its
 * {@link ObservableEntity} strongly. FX thread only, like the rest of the package.
 *
 * <p>Unsaved control edits stay on {@link EditableVersion}: the chronology covers what is stored,
 * committed or pending in a transaction.
 */
public final class ObservableChangeChronology {

    /**
     * What one version changed against the version it supersedes.
     *
     * @param key     the version: the entity's nid and the version's stamp
     * @param version the version, the one the canonical observable entity holds
     * @param changes the fields that differ, each named by a feature key
     */
    public record VersionChange(VersionKey key, ObservableVersion version, ImmutableList<FieldChange> changes) {
    }

    /**
     * One field that differs between a version and its predecessor. Both records are kept, so a
     * view can name the field ({@link FieldRecord#fieldDefinition}) and read each side's stamp:
     * the predecessor's stamp is {@code prior().versionStampNid()}, the nonexistent stamp for a
     * first version.
     *
     * @param key     the feature the field is: a stamp field, a pattern version's meaning, purpose
     *                or field definition, or a semantic's field
     * @param prior   the field in the predecessor
     * @param current the field in the version
     */
    public record FieldChange(FeatureKey key, FieldRecord<?> prior, FieldRecord<?> current) {
    }

    /** One chronology per entity, let go when nothing holds it, like the canonical observables. */
    private static final Cache<Long, ObservableChangeChronology> INSTANCES = Caffeine.newBuilder().weakValues().build();

    /** The chronology depends on no coordinate, so any calculator computes it. */
    private static final StampCalculator CALCULATOR = Calculators.Stamp.DevelopmentLatest();

    private final ObservableEntity<?> entity;
    /** A semantic's pattern, held so the pattern listener lives as long as this chronology. */
    private final ObservableEntity<?> pattern;
    private final ObservableList<VersionChange> changes = FXCollections.observableArrayList();
    private final ObservableList<VersionChange> readOnlyChanges = FXCollections.unmodifiableObservableList(changes);
    /** The subscription to each uncommitted version's version property, by stamp. */
    private final MutableLongObjectMap<Subscription> versionSubscriptions = new LongObjectHashMap<>();
    private final ListChangeListener<ObservableEntityVersion<?, ?>> versionListListener = change -> versionsChanged();
    private final ListChangeListener<ObservableEntityVersion<?, ?>> patternVersionListListener = change -> reannounce();

    @SuppressWarnings("unchecked")
    private ObservableChangeChronology(ObservableEntity<?> entity) {
        this.entity = entity;
        this.pattern = entity instanceof ObservableSemantic semantic
                ? ObservableEntityHandle.get(semantic.patternNid()).expectEntity() : null;
        // The entity's list holds this listener and this chronology holds the entity, so the two
        // live and die together; the pattern's list must not hold the chronology.
        ((FeatureList<ObservableEntityVersion<?, ?>>) entity.versionList()).addListener(versionListListener);
        if (pattern != null) {
            ((FeatureList<ObservableEntityVersion<?, ?>>) pattern.versionList())
                    .addListener(new WeakListChangeListener<>(patternVersionListListener));
        }
        versionsChanged();
    }

    /**
     * The chronology of an entity: the one held when there is one, else a new one. Package-private;
     * reach it through {@link ObservableEntityHandle}.
     */
    static ObservableChangeChronology packagePrivateGet(ObservableEntity<?> entity) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("The change chronology is read on the FX thread only");
        }
        // Get-or-create without Caffeine's compute: construction looks up the pattern's observable,
        // and a compute must not reenter the cache. The FX thread is the only caller.
        ObservableChangeChronology chronology = INSTANCES.getIfPresent(entity.nid());
        if (chronology == null) {
            chronology = new ObservableChangeChronology(entity);
            INSTANCES.put(entity.nid(), chronology);
        }
        return chronology;
    }

    /** The entity whose changes these are. */
    public ObservableEntity<?> observableEntity() {
        return entity;
    }

    public long nid() {
        return entity.nid();
    }

    /**
     * The entity's version changes, newest first: the path farthest from the primordial path
     * first, then by time, then by stamp. Read-only; it changes as the entity does.
     */
    public ObservableList<VersionChange> changes() {
        return readOnlyChanges;
    }

    /** The entity's versions came or went: follow the uncommitted ones and rerun the diff. */
    private void versionsChanged() {
        for (long stampNid : versionSubscriptions.keySet().toArray()) {
            if (!entity.versionPropertyMap().containsKey(stampNid)) {
                versionSubscriptions.remove(stampNid).unsubscribe();
            }
        }
        for (ObservableEntityVersion<?, ?> version : (Iterable<ObservableEntityVersion<?, ?>>) entity.versionPropertyMap().values()) {
            if (version.uncommitted() && !versionSubscriptions.containsKey(version.stampNid())) {
                versionSubscriptions.put(version.stampNid(), version.versionProperty().subscribe(this::refresh));
            }
        }
        refresh();
    }

    /**
     * Reruns the diff on the record the observable holds and reconciles the result into
     * {@link #changes()}. A version that committed no longer changes, so its subscription ends.
     */
    private void refresh() {
        Entity<?> held = heldRecord();
        ChangeChronology chronology = CALCULATOR.changeChronology(held);
        long stampVersionPatternNid = nidIfStored(EntityBinding.Stamp.Version.pattern());
        long patternVersionPatternNid = held instanceof PatternEntity<?>
                ? nidIfStored(EntityBinding.Pattern.Version.pattern()) : 0;

        Map<VersionKey, VersionChange> current = new HashMap<>();
        for (VersionChange change : changes) {
            current.put(change.key(), change);
        }
        MutableList<VersionChange> next = Lists.mutable.withInitialCapacity(chronology.changeRecords().size());
        for (VersionChangeRecord record : chronology.changeRecords()) {
            ObservableEntityVersion<?, ?> version = (ObservableEntityVersion<?, ?>) entity.versionPropertyMap().get(record.stampNid());
            if (version == null) {
                throw new IllegalStateException("The held record has a version the observable does not: stamp "
                        + record.stampNid() + " of " + EntityText.diagnostic(held));
            }
            if (!version.uncommitted() && versionSubscriptions.containsKey(record.stampNid())) {
                versionSubscriptions.remove(record.stampNid()).unsubscribe();
            }
            VersionKey key = new VersionKey(held.nid(), record.stampNid());
            ImmutableList<FieldChange> fieldChanges = record.changes().collect(fieldChange ->
                    new FieldChange(keyFor(fieldChange, stampVersionPatternNid, patternVersionPatternNid),
                            fieldChange.priorValue(), fieldChange.currentValue()));
            VersionChange existing = current.get(key);
            next.add(existing != null && existing.version() == version && existing.changes().equals(fieldChanges)
                    ? existing : new VersionChange(key, version, fieldChanges));
        }
        reconcile(next);
    }

    /**
     * The record the observable holds: its entity record with the versions its observable versions
     * hold now. {@link ObservableEntity#updateVersions} sets each version, and the list, before it
     * takes the new entity record, so at the events this chronology listens to the versions are
     * current and the entity record may not be yet.
     */
    @SuppressWarnings("unchecked")
    private Entity<?> heldRecord() {
        MutableList<EntityVersion> versions = Lists.mutable.empty();
        for (ObservableEntityVersion<?, ?> version : (Iterable<ObservableEntityVersion<?, ?>>) entity.versionPropertyMap().values()) {
            versions.add(version.getVersionRecord());
        }
        return switch (entity.entity()) {
            case ConceptRecord concept -> concept.withVersions(versions.collect(version -> (ConceptVersionRecord) version).toImmutable());
            case SemanticRecord semantic -> semantic.withVersions(versions.collect(version -> (SemanticVersionRecord) version).toImmutable());
            case PatternRecord pattern -> pattern.withVersions(versions.collect(version -> (PatternVersionRecord) version).toImmutable());
            case StampRecord stamp -> stamp.withVersions(versions.collect(version -> (StampVersionRecord) version).toImmutable());
            default -> throw new IllegalStateException("Not a concept, semantic, pattern or stamp: " + EntityText.diagnostic(entity.entity()));
        };
    }

    /**
     * Brings {@link #changes} to the given list with the smallest ordinary list changes: entries
     * that are gone are removed, entries at their place that differ are set, entries out of place
     * or new are moved or added.
     */
    private void reconcile(MutableList<VersionChange> next) {
        Map<VersionKey, VersionChange> wanted = new HashMap<>();
        for (VersionChange change : next) {
            wanted.put(change.key(), change);
        }
        for (int index = changes.size() - 1; index >= 0; index--) {
            if (!wanted.containsKey(changes.get(index).key())) {
                changes.remove(index);
            }
        }
        for (int index = 0; index < next.size(); index++) {
            VersionChange change = next.get(index);
            int at = indexOfKey(change.key(), index);
            if (at == index) {
                if (changes.get(index) != change) {
                    changes.set(index, change);
                }
            } else {
                if (at >= 0) {
                    changes.remove(at);
                }
                changes.add(index, change);
            }
        }
    }

    /** The index of the entry with the key at or after {@code from}, or -1 when there is none. */
    private int indexOfKey(VersionKey key, int from) {
        for (int index = from; index < changes.size(); index++) {
            if (changes.get(index).key().equals(key)) {
                return index;
            }
        }
        return -1;
    }

    /** The semantic's pattern was revised: the same entries, announced again so views re-label. */
    private void reannounce() {
        for (int index = 0; index < changes.size(); index++) {
            changes.set(index, changes.get(index));
        }
    }

    /** The pattern's nid when the store holds the pattern, else 0: no field is then recorded against it. */
    private static long nidIfStored(PatternFacade pattern) {
        return PrimitiveData.get().hasPublicId(pattern.publicId()) ? pattern.nid() : 0;
    }

    /**
     * The feature a field change is, from its current record's pattern nid and index:
     * <ul>
     *   <li>the stamp version pattern, indexes 1 to 5: the stamp's status, time, author, module
     *       and path, keyed on the stamp as {@link ObservableStampVersion} keys them;</li>
     *   <li>the pattern version pattern: index 1 the pattern's meaning, 2 its purpose, 3 one of
     *       its field definitions, at the definition's own index;</li>
     *   <li>any other pattern is the semantic's own: the field at that index.</li>
     * </ul>
     */
    private static FeatureKey keyFor(FieldChangeRecord fieldChange, long stampVersionPatternNid, long patternVersionPatternNid) {
        FieldRecord<?> current = fieldChange.currentValue();
        long nid = current.nid();
        long stampNid = current.versionStampNid();
        long patternNid = current.patternNid();
        int index = current.indexInPattern();
        if (patternNid == stampVersionPatternNid) {
            if (index == EntityBinding.Stamp.Version.statusFieldDefinitionIndex()) {
                return FeatureKey.Version.StampStatus(stampNid);
            }
            if (index == EntityBinding.Stamp.Version.timeFieldDefinitionIndex()) {
                return FeatureKey.Version.StampTime(stampNid);
            }
            if (index == EntityBinding.Stamp.Version.authorFieldDefinitionIndex()) {
                return FeatureKey.Version.StampAuthor(stampNid);
            }
            if (index == EntityBinding.Stamp.Version.moduleFieldDefinitionIndex()) {
                return FeatureKey.Version.StampModule(stampNid);
            }
            if (index == EntityBinding.Stamp.Version.pathFieldDefinitionIndex()) {
                return FeatureKey.Version.StampPath(stampNid);
            }
            throw new IllegalStateException("Not a stamp field the chronology records: index " + index + " of " + current);
        }
        if (patternNid == patternVersionPatternNid) {
            if (index == EntityBinding.Pattern.Version.patternMeaningFieldDefinitionIndex()) {
                return FeatureKey.Version.PatternMeaning(nid, stampNid);
            }
            if (index == EntityBinding.Pattern.Version.patternPurposeFieldDefinitionIndex()) {
                return FeatureKey.Version.PatternPurpose(nid, stampNid);
            }
            if (index == EntityBinding.Pattern.Version.fieldDefinitionListFieldDefinitionIndex()) {
                // The definition is the value, on whichever side has one; it carries its own index.
                FieldDefinitionForEntity definition = current.value() instanceof FieldDefinitionForEntity currentDefinition
                        ? currentDefinition : (FieldDefinitionForEntity) fieldChange.priorValue().value();
                return FeatureKey.Version.PatternFieldDefinitionListItem(nid, definition.indexInPattern(),
                        definition.patternNid(), stampNid);
            }
            throw new IllegalStateException("Not a pattern version field the chronology records: index " + index + " of " + current);
        }
        return FeatureKey.Version.SemanticFieldListItem(nid, index, patternNid, stampNid);
    }
}
