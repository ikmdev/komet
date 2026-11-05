package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klfields.KlFieldType;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.tinkar.common.id.LongIdCollection;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A section's candidate reference components as display text.
 *
 * <p>A section anchored on a reference pattern offers that pattern's semantics as its reference
 * component. Those semantics all share the same referenced component and pattern, so the label
 * they get everywhere else ("Module in Test Performed Pattern") is the same for every one of them.
 * What tells them apart is their field values, so this class turns each candidate's fields into
 * display text, in the order the layout author set or, failing that, the order that best tells
 * the candidates apart: the closed combobox shows the first field's value, a dropdown option
 * leads with the first {@value #OPTION_FIELD_COUNT} values, and the reference strip lists every
 * field.
 */
final class ReferenceComponentSummaries {

    /** A field of a candidate as display text. */
    record FieldText(String name, String value) {}

    /** How many fields a dropdown option leads with. */
    static final int OPTION_FIELD_COUNT = 2;

    private ReferenceComponentSummaries() {
    }

    /**
      * Per candidate (by nid), its fields as display text, in the order the layout author set for
     * the section (see {@link EditorSectionModel#getReferenceFieldOrder()}, pattern order for the
     * fields it leaves out) or, with no order set, the order that best tells the candidates apart
     * ({@link AutomaticFieldOrder}). A candidate that is not a semantic, or that the view
     * holds no version of, gets an empty list.
     */
    static Map<Long, List<FieldText>> summarize(List<? extends EntityFacade> candidates, List<Integer> preferredFieldOrder,
                                                 ViewProperties viewProperties) {
        ViewCalculator calculator = viewProperties.calculator();
        Map<Long, List<FieldText>> inPatternOrder = new LinkedHashMap<>();
        int fieldCount = 0;
        for (EntityFacade candidate : candidates) {
            List<FieldText> fields = fieldTexts(candidate, calculator);
            inPatternOrder.put(candidate.nid(), fields);
            fieldCount = Math.max(fieldCount, fields.size());
        }

        List<Integer> order;
        if (preferredFieldOrder.isEmpty()) {
            List<List<String>> valuesPerCandidate = new ArrayList<>();
            for (List<FieldText> fields : inPatternOrder.values()) {
                if (!fields.isEmpty()) {
                    valuesPerCandidate.add(fields.stream().map(field -> field == null ? "" : field.value()).toList());
                }
            }
            order = AutomaticFieldOrder.rank(valuesPerCandidate, fieldCount);
        } else {
            order = EditorSectionModel.fieldIndexesInOrder(preferredFieldOrder, fieldCount);
        }

        Map<Long, List<FieldText>> summaries = new HashMap<>();
        inPatternOrder.forEach((nid, fields) -> {
            List<FieldText> ordered = new ArrayList<>();
            for (int index : order) {
                if (index < fields.size() && fields.get(index) != null) {
                    ordered.add(fields.get(index));
                }
            }
            summaries.put(nid, ordered);
        });
        return summaries;
    }

    /** The one-line text a dropdown option leads with: the first fields' values, dot-separated. */
    static String optionText(List<FieldText> summary) {
        StringJoiner text = new StringJoiner(" · ");
        for (FieldText field : summary.subList(0, Math.min(summary.size(), OPTION_FIELD_COUNT))) {
            text.add(field.value());
        }
        return text.toString();
    }

    /** The one value the closed combobox shows for the candidate: its first field's. Empty for a candidate without fields. */
    static String buttonValue(List<FieldText> summary) {
        return summary.isEmpty() ? "" : summary.getFirst().value();
    }

    /**
     * The candidate's fields as display text in pattern order, null for a field of a type that has
     * no text form so that the list lines up with the pattern's definitions. The values are those
     * of the version the window renders: the latest for the view through the semantic's observable
     * snapshot, the way the presenters resolve it (see
     * AbstractPatternSemanticsPresenter.latestVersionForView).
     */
    private static List<FieldText> fieldTexts(EntityFacade candidate, ViewCalculator calculator) {
        if (!(candidate instanceof SemanticEntity<?> semanticEntity)) {
            return List.of();
        }
        ObservableSemantic observableSemantic = ObservableEntityHandle.get(semanticEntity.publicId())
                .asSemantic().orElseThrow(() -> new IllegalArgumentException(
                        "Entity is not a semantic: " + semanticEntity.publicId()));
        Latest<ObservableSemanticVersion> latest = observableSemantic.getSnapshot(calculator).getLatestVersion();
        if (latest.isAbsent()) {
            return List.of();
        }
        ObservableSemanticVersion version = latest.get();
        Latest<PatternEntityVersion> latestPattern = calculator.latest(version.patternNid());
        if (latestPattern.isAbsent()) {
            return List.of();
        }

        ImmutableList<? extends FieldDefinitionForEntity> definitions = latestPattern.get().fieldDefinitions();
        ImmutableList<Object> values = version.fieldValues();
        int fieldCount = Math.min(definitions.size(), values.size());
        List<FieldText> fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            FieldDefinitionForEntity definition = definitions.get(i);
            String value = valueText(values.get(i), definition.dataTypeNid(), calculator);
            if (value == null) {
                fields.add(null);
                continue;
            }
            String name = describe(definition.meaningNid(), calculator);
            fields.add(new FieldText(name, value));
        }
        return fields;
    }

    /**
     * The value as the user reads it in the window, blank for a value not set; null for a type
     * that has no text form (e.g. a definition tree).
     */
    private static String valueText(Object value, long dataTypeNid, ViewCalculator calculator) {
        KlFieldType fieldType = KlFieldType.of(dataTypeNid).orElse(null);
        if (fieldType == null) {
            return null;
        }
        if (value == null) {
            return "";
        }
        return switch (fieldType) {
            case COMPONENT -> value instanceof EntityFacade entity ? describe(entity.nid(), calculator) : String.valueOf(value);
            case C_LIST, C_SET -> {
                StringJoiner text = new StringJoiner(", ");
                ((LongIdCollection) value).forEach(nid -> text.add(describe(nid, calculator)));
                yield text.toString();
            }
            case IMAGE -> "Image";
            case STRING, INTEGER, FLOAT, BOOLEAN -> String.valueOf(value);
        };
    }

    private static String describe(long nid, ViewCalculator calculator) {
        return calculator.languageCalculator().getDescriptionTextOrNid(nid);
    }
}
