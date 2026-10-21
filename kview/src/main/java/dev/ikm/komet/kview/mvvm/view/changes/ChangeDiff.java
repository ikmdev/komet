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

import dev.ikm.tinkar.common.id.LongIdCollection;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * What one unpublished change does to its component, field by field: the component's last
 * published version against its unpublished one, each field as a before and an after text.
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
     * Compares a change's unpublished version with its last published one.
     *
     * @param change     the change
     * @param calculator the view the field names and values are described on
     * @return the diff
     */
    public static ChangeDiff of(UnpublishedChange change, ViewCalculator calculator) {
        Entity<?> entity = change.entity();
        EntityVersion after = null;
        EntityVersion before = null;
        for (EntityVersion version : entity.versions()) {
            if (version.uncommitted()) {
                after = version;
            } else if (!version.canceled() && (before == null || version.time() > before.time())) {
                before = version;
            }
        }
        if (after == null) {
            throw new IllegalArgumentException("No unpublished version: " + entity);
        }
        List<FieldDiff> fields = new ArrayList<>();
        fields.add(new FieldDiff("Status", before == null ? null : stateText(before.state()), stateText(after.state())));
        switch (after) {
            case SemanticEntityVersion semantic -> addSemanticFields(fields, (SemanticEntityVersion) before, semantic, calculator);
            case PatternEntityVersion pattern -> addPatternFields(fields, (PatternEntityVersion) before, pattern, calculator);
            default -> { }
        }
        fields.add(new FieldDiff("Module",
                before == null ? null : calculator.getDescriptionTextOrNid(before.moduleNid()),
                calculator.getDescriptionTextOrNid(after.moduleNid())));
        fields.add(new FieldDiff("Path",
                before == null ? null : calculator.getDescriptionTextOrNid(before.pathNid()),
                calculator.getDescriptionTextOrNid(after.pathNid())));
        return new ChangeDiff(change, before, after, List.copyOf(fields));
    }

    /**
     * A semantic's fields, named by the pattern's field meanings on the pattern's latest version
     * on the view. A pattern's fields never change once it is created, so every version of a
     * semantic has one value per field, in the pattern's order.
     */
    private static void addSemanticFields(List<FieldDiff> fields, SemanticEntityVersion before,
                                          SemanticEntityVersion after, ViewCalculator calculator) {
        Latest<PatternEntityVersion> pattern = calculator.latestPatternEntityVersion(after.patternNid());
        if (!pattern.isPresent()) {
            return;
        }
        ImmutableList<? extends FieldDefinitionForEntity> definitions = pattern.get().fieldDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            String name = calculator.getDescriptionTextOrNid(definitions.get(i).meaningNid());
            fields.add(new FieldDiff(name,
                    before == null ? null : valueText(before.fieldValues().get(i), calculator),
                    valueText(after.fieldValues().get(i), calculator)));
        }
    }

    /**
     * A pattern's own fields: what its semantics mean and are for, then each field's meaning and
     * purpose. A pattern's field list is fixed once it is created, so the fields line up by
     * position between versions and only a field's meaning and purpose can differ.
     */
    private static void addPatternFields(List<FieldDiff> fields, PatternEntityVersion before,
                                         PatternEntityVersion after, ViewCalculator calculator) {
        fields.add(new FieldDiff("Meaning",
                before == null ? null : calculator.getDescriptionTextOrNid(before.semanticMeaningNid()),
                calculator.getDescriptionTextOrNid(after.semanticMeaningNid())));
        fields.add(new FieldDiff("Purpose",
                before == null ? null : calculator.getDescriptionTextOrNid(before.semanticPurposeNid()),
                calculator.getDescriptionTextOrNid(after.semanticPurposeNid())));
        ImmutableList<? extends FieldDefinitionForEntity> afterDefinitions = after.fieldDefinitions();
        for (int i = 0; i < afterDefinitions.size(); i++) {
            FieldDefinitionForEntity beforeDefinition = before == null ? null : before.fieldDefinitions().get(i);
            FieldDefinitionForEntity afterDefinition = afterDefinitions.get(i);
            String field = "Field " + (i + 1);
            fields.add(new FieldDiff(field + " meaning",
                    beforeDefinition == null ? null : calculator.getDescriptionTextOrNid(beforeDefinition.meaningNid()),
                    calculator.getDescriptionTextOrNid(afterDefinition.meaningNid())));
            fields.add(new FieldDiff(field + " purpose",
                    beforeDefinition == null ? null : calculator.getDescriptionTextOrNid(beforeDefinition.purposeNid()),
                    calculator.getDescriptionTextOrNid(afterDefinition.purposeNid())));
        }
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
