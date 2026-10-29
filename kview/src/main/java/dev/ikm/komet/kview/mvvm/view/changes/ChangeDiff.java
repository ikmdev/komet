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
package dev.ikm.komet.kview.mvvm.view.changes;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.ObservableChangeChronology;
import dev.ikm.komet.framework.observable.ObservableChangeChronology.FieldChange;
import dev.ikm.komet.framework.observable.ObservableChangeChronology.VersionChange;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.key.FieldDefinitionListItemKey;
import dev.ikm.komet.framework.observable.key.SemanticFieldListItemKey;
import dev.ikm.tinkar.common.id.LongIdCollection;
import dev.ikm.tinkar.common.service.NonExistentValue;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * What one unpublished change does to its component, field by field: the component's last
 * published version against its unpublished one, each field as a before and an after text. An
 * adapter over the component's live change chronology ({@link ObservableChangeChronology}): the
 * head pending {@link VersionChange} says which fields differ and how, and the fields it leaves
 * out are listed unchanged, so the window can show them or hide them.
 *
 * @param change the change in the tray
 * @param before the last published version of the component; null for a new component
 * @param after  the unpublished version
 * @param fields the component's fields, in display order: the status first, then the version's
 *               own fields, then the module and the path
 */
public record ChangeDiff(UnpublishedChange change, EntityVersion before, EntityVersion after, List<FieldDiff> fields) {

    /**
     * One field of the component.
     *
     * @param name   the field's name
     * @param before the field's text in the last published version; null for a new component
     * @param after  the field's text in the unpublished version
     */
    public record FieldDiff(String name, String before, String after) {

        /** Whether the unpublished version changes the field. A new component's fields all do. */
        public boolean changed() {
            return !Objects.equals(before, after);
        }
    }

    /** How many fields the unpublished version changes. */
    public long changedCount() {
        return fields.stream().filter(FieldDiff::changed).count();
    }

    /**
     * The diff of a change's unpublished version, read from the component's live chronology.
     * FX thread only, as the chronology is.
     *
     * @param change     the change
     * @param calculator the view the field names and values are described on
     * @return the diff
     * @throws IllegalArgumentException if the component has no unpublished version
     */
    public static ChangeDiff of(UnpublishedChange change, ViewCalculator calculator) {
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(change.nid());
        VersionChange pending = chronology.changes().stream()
                .filter(versionChange -> versionChange.version().getVersionRecord().uncommitted())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No unpublished version: " + change.entity()));
        return of(change, pending, calculator);
    }

    /**
     * The diff of one pending version change: its version against the version it supersedes.
     *
     * @param change     the change
     * @param pending    the pending version's entry in the component's chronology
     * @param calculator the view the field names and values are described on
     * @return the diff
     */
    public static ChangeDiff of(UnpublishedChange change, VersionChange pending, ViewCalculator calculator) {
        EntityVersion after = pending.version().getVersionRecord();
        EntityVersion before = predecessor(pending, after);
        ImmutableList<FieldChange> changes = pending.changes();
        List<FieldDiff> fields = new ArrayList<>();
        fields.add(stampField(changes, "Status", FeatureKey.VersionFeature.Stamp.Status.class,
                value -> stateText((State) value), before == null ? null : stateText(before.state()), stateText(after.state())));
        switch (after) {
            case SemanticEntityVersion semantic -> addSemanticFields(fields, changes, (SemanticEntityVersion) before, semantic, calculator);
            case PatternEntityVersion pattern -> addPatternFields(fields, changes, (PatternEntityVersion) before, pattern, calculator);
            default -> { }
        }
        fields.add(stampField(changes, "Module", FeatureKey.VersionFeature.Stamp.Module.class,
                value -> valueText(value, calculator),
                before == null ? null : calculator.getDescriptionTextOrNid(before.moduleNid()),
                calculator.getDescriptionTextOrNid(after.moduleNid())));
        fields.add(stampField(changes, "Path", FeatureKey.VersionFeature.Stamp.Path.class,
                value -> valueText(value, calculator),
                before == null ? null : calculator.getDescriptionTextOrNid(before.pathNid()),
                calculator.getDescriptionTextOrNid(after.pathNid())));
        return new ChangeDiff(change, before, after, List.copyOf(fields));
    }

    /**
     * The version the pending one supersedes: the one under the stamp every field change records
     * as prior. Null for a first version, whose prior stamp is the nonexistent stamp, and when the
     * chronology records no field at all.
     */
    private static EntityVersion predecessor(VersionChange pending, EntityVersion after) {
        if (pending.changes().isEmpty()) {
            return null;
        }
        long priorStampNid = pending.changes().getFirst().prior().versionStampNid();
        if (priorStampNid == StampRecord.nonExistentStamp().nid()) {
            return null;
        }
        return Entity.getVersion(after.nid(), priorStampNid).orElseThrow(() ->
                new IllegalStateException("The version superseded is not stored: stamp " + priorStampNid + " of " + after));
    }

    /**
     * A stamp field: from the chronology when the pending version changes it, else the same
     * value on both sides, read from the versions.
     */
    private static FieldDiff stampField(ImmutableList<FieldChange> changes, String name, Class<? extends FeatureKey> keyType,
                                        Function<Object, String> text, String unchangedBefore, String unchangedAfter) {
        FieldChange change = find(changes, fieldChange -> keyType.isInstance(fieldChange.key()));
        if (change == null) {
            return new FieldDiff(name, unchangedBefore, unchangedAfter);
        }
        return new FieldDiff(name, priorText(change, text), text.apply(change.current().value()));
    }

    /**
     * A semantic's fields, named by the pattern's field meanings on the pattern's latest version
     * on the view. A pattern's fields never change once it is created, so every version of a
     * semantic has one value per field, in the pattern's order; the chronology records each
     * field by its index in that pattern.
     */
    private static void addSemanticFields(List<FieldDiff> fields, ImmutableList<FieldChange> changes, SemanticEntityVersion before,
                                          SemanticEntityVersion after, ViewCalculator calculator) {
        Latest<PatternEntityVersion> pattern = calculator.latestPatternEntityVersion(after.patternNid());
        if (!pattern.isPresent()) {
            return;
        }
        ImmutableList<? extends FieldDefinitionForEntity> definitions = pattern.get().fieldDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            int index = i;
            String name = calculator.getDescriptionTextOrNid(definitions.get(i).meaningNid());
            FieldChange change = find(changes, fieldChange ->
                    fieldChange.key() instanceof SemanticFieldListItemKey key && key.index() == index);
            if (change == null) {
                String text = valueText(after.fieldValues().get(i), calculator);
                fields.add(new FieldDiff(name, before == null ? null : text, text));
            } else {
                fields.add(new FieldDiff(name, priorText(change, value -> valueText(value, calculator)),
                        valueText(change.current().value(), calculator)));
            }
        }
    }

    /**
     * A pattern's own fields: what its semantics mean and are for, then each field's meaning and
     * purpose. A pattern's field list is fixed once it is created, so the fields line up by
     * position between versions and only a field's meaning and purpose can differ; the
     * chronology records a changed definition as one change at the definition's index.
     */
    private static void addPatternFields(List<FieldDiff> fields, ImmutableList<FieldChange> changes, PatternEntityVersion before,
                                         PatternEntityVersion after, ViewCalculator calculator) {
        Function<Object, String> concept = value -> valueText(value, calculator);
        FieldChange meaning = find(changes, fieldChange -> fieldChange.key() instanceof FeatureKey.VersionFeature.Pattern.PatternMeaning);
        fields.add(meaning == null
                ? unchanged("Meaning", before != null, calculator.getDescriptionTextOrNid(after.semanticMeaningNid()))
                : new FieldDiff("Meaning", priorText(meaning, concept), concept.apply(meaning.current().value())));
        FieldChange purpose = find(changes, fieldChange -> fieldChange.key() instanceof FeatureKey.VersionFeature.Pattern.PatternPurpose);
        fields.add(purpose == null
                ? unchanged("Purpose", before != null, calculator.getDescriptionTextOrNid(after.semanticPurposeNid()))
                : new FieldDiff("Purpose", priorText(purpose, concept), concept.apply(purpose.current().value())));
        ImmutableList<? extends FieldDefinitionForEntity> afterDefinitions = after.fieldDefinitions();
        for (int i = 0; i < afterDefinitions.size(); i++) {
            int index = i;
            String field = "Field " + (i + 1);
            FieldDefinitionForEntity afterDefinition = afterDefinitions.get(i);
            FieldChange change = find(changes, fieldChange ->
                    fieldChange.key() instanceof FieldDefinitionListItemKey key && key.index() == index);
            FieldDefinitionForEntity beforeDefinition;
            if (change == null) {
                beforeDefinition = before == null ? null : afterDefinition;
            } else {
                beforeDefinition = change.prior().value() instanceof FieldDefinitionForEntity prior ? prior : null;
            }
            fields.add(new FieldDiff(field + " meaning",
                    beforeDefinition == null ? null : calculator.getDescriptionTextOrNid(beforeDefinition.meaningNid()),
                    calculator.getDescriptionTextOrNid(afterDefinition.meaningNid())));
            fields.add(new FieldDiff(field + " purpose",
                    beforeDefinition == null ? null : calculator.getDescriptionTextOrNid(beforeDefinition.purposeNid()),
                    calculator.getDescriptionTextOrNid(afterDefinition.purposeNid())));
        }
    }

    private static FieldDiff unchanged(String name, boolean hasBefore, String text) {
        return new FieldDiff(name, hasBefore ? text : null, text);
    }

    /** The prior value's text; null when the field did not exist before. */
    private static String priorText(FieldChange change, Function<Object, String> text) {
        Object prior = change.prior().value();
        return prior instanceof NonExistentValue ? null : text.apply(prior);
    }

    private static FieldChange find(ImmutableList<FieldChange> changes, Predicate<FieldChange> test) {
        for (FieldChange change : changes) {
            if (test.test(change)) {
                return change;
            }
        }
        return null;
    }

    /** A field value as text: components by their description, the rest as they print. */
    static String valueText(Object value, ViewCalculator calculator) {
        return switch (value) {
            case null -> "";
            case EntityFacade facade -> calculator.getDescriptionTextOrNid(facade.nid());
            case LongIdCollection ids -> {
                StringJoiner joiner = new StringJoiner(", ");
                ids.forEach(nid -> joiner.add(calculator.getDescriptionTextOrNid(nid)));
                yield joiner.toString();
            }
            case byte[] bytes -> "Image (" + bytes.length + " bytes)";
            case Instant instant -> DateTimeUtil.format(instant);
            default -> String.valueOf(value);
        };
    }

    /** "Active", "Inactive", ...: the state's name in sentence case. */
    static String stateText(State state) {
        String name = state.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
