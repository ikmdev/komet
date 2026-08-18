package dev.ikm.komet.layout.editor.model;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A filter on which of a Pattern's semantics are displayed (see
 * {@link EditorPatternModel#getSemanticFilters()}): a semantic passes the filter when its field values
 * match every entry in {@link #getFieldConstraints() fieldConstraints}. For example, the Description
 * Pattern can be filtered so only semantics whose "Description type" field is "Fully qualified name"
 * are shown.
 *
 * <p>Constraints are keyed by the field's index in the Pattern; a field with no entry accepts any
 * value. A Pattern with no filters shows all of its semantics, and a Pattern with several filters
 * shows the semantics passing any one of them.
 *
 * <p>This is the display-side counterpart of {@link EditorPatternRequirement}, which constrains
 * fields the same way but to demand semantics rather than to hide them.
 */
public class EditorPatternSemanticFilter {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternSemanticFilter.class);

    private static final String CONSTRAINT_SEPARATOR = ";";
    private static final String FIELD_VALUE_SEPARATOR = ":";
    private static final String UUID_SEPARATOR = "_";

    /**
     * Whether a semantic holding the passed in field values passes this filter, that is whether every
     * constrained field holds the constraint's concept. A filter with no constraints passes everything.
     *
     * @param fieldValues the field values of the semantic's version, in Pattern field order
     * @return whether the semantic passes this filter
     */
    public boolean matches(ImmutableList<Object> fieldValues) {
        return fieldConstraints.entrySet().stream().allMatch(constraint ->
                constraint.getKey() < fieldValues.size()
                        && fieldValues.get(constraint.getKey()) instanceof EntityFacade fieldConcept
                        && fieldConcept.nid() == constraint.getValue().nid());
    }

    /**
     * Encodes this filter into a single preferences string:
     * {@code [<fieldIndex>:<uuid>[_<uuid>...][;...]]} — the concept of each field constraint is stored
     * by its portable PublicId UUIDs. A filter with no constraints encodes to the empty string.
     */
    public String toPreferenceString() {
        return fieldConstraints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(constraint -> {
                    String uuids = constraint.getValue().publicId().asUuidList().stream()
                            .map(UUID::toString)
                            .collect(Collectors.joining(UUID_SEPARATOR));
                    return constraint.getKey() + FIELD_VALUE_SEPARATOR + uuids;
                })
                .collect(Collectors.joining(CONSTRAINT_SEPARATOR));
    }

    /**
     * Decodes a filter encoded by {@link #toPreferenceString()}. A constraint whose concept is not
     * present in the datastore is dropped with a warning rather than failing the whole load.
     */
    public static EditorPatternSemanticFilter fromPreferenceString(String encoded) {
        EditorPatternSemanticFilter filter = new EditorPatternSemanticFilter();

        for (String constraint : encoded.split(CONSTRAINT_SEPARATOR)) {
            if (constraint.isEmpty()) {
                // A filter with no constraints (one the user hasn't filled in yet) encodes to "".
                continue;
            }

            String[] fieldAndValue = constraint.split(FIELD_VALUE_SEPARATOR);
            int fieldIndex = Integer.parseInt(fieldAndValue[0]);
            UUID[] uuids = Arrays.stream(fieldAndValue[1].split(UUID_SEPARATOR))
                    .map(UUID::fromString)
                    .toArray(UUID[]::new);

            EntityHandle.get(PublicIds.of(uuids))
                    .ifEntity(entity -> filter.getFieldConstraints().put(fieldIndex, entity.toProxy()))
                    .ifAbsent(() -> LOG.warn(
                            "Pattern semantic filter constraint concept not in datastore; dropping it: {}",
                            fieldAndValue[1]));
        }

        return filter;
    }

    /*=============================================================================*
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     *=============================================================================*/

    // -- field constraints
    /**
     * The concept each constrained field must hold for a semantic to be displayed, keyed by the field's
     * index in the Pattern. Fields with no entry accept any value.
     */
    private final ObservableMap<Integer, EntityProxy> fieldConstraints = FXCollections.observableHashMap();
    public ObservableMap<Integer, EntityProxy> getFieldConstraints() { return fieldConstraints; }
}
