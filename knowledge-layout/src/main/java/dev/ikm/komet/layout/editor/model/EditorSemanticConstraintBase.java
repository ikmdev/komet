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
 * Selects semantics of a Pattern by their field values: a semantic matches when every entry in
 * {@link #getFieldConstraints() fieldConstraints} holds, that is when each constrained field holds
 * the constraint's concept. Constraints are keyed by the field's index in the Pattern, and a field
 * with no entry accepts any value — so a constraint with no entries matches every semantic.
 *
 * <p>The two things authored in the KL editor that select semantics this way extend it:
 * {@link EditorPatternRequirement} demands matching semantics before a component can be created,
 * and {@link EditorPatternSemanticFilter} shows only matching semantics. This class carries what
 * they share — the constrained fields, the matching, and the encoding of the constraints into a
 * preferences string.
 */
public abstract class EditorSemanticConstraintBase {
    private static final Logger LOG = LoggerFactory.getLogger(EditorSemanticConstraintBase.class);

    /** Separates one field constraint from the next in a preferences string. */
    protected static final String CONSTRAINT_SEPARATOR = ";";
    private static final String FIELD_VALUE_SEPARATOR = ":";
    private static final String UUID_SEPARATOR = "_";

    /**
     * Whether a semantic holding the passed in field values matches, that is whether every
     * constrained field holds the constraint's concept.
     *
     * @param fieldValues the field values of the semantic's version, in Pattern field order
     * @return whether the semantic matches
     */
    public boolean matches(ImmutableList<Object> fieldValues) {
        return fieldConstraints.entrySet().stream().allMatch(constraint ->
                constraint.getKey() < fieldValues.size()
                        && fieldValues.get(constraint.getKey()) instanceof EntityFacade fieldConcept
                        && fieldConcept.nid() == constraint.getValue().nid());
    }

    /**
     * Encodes the field constraints as {@code <fieldIndex>:<uuid>[_<uuid>...]} entries separated by
     * {@link #CONSTRAINT_SEPARATOR} — the concept of each constraint is stored by its portable
     * PublicId UUIDs, and no constraints encode to the empty string. Subclasses build their own
     * preference string around this.
     *
     * @return the field constraints encoded for preferences
     */
    protected String constraintsToPreferenceString() {
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
     * Restores the field constraints from a string encoded by {@link #constraintsToPreferenceString()}.
     * A constraint whose concept is not present in the datastore is dropped with a warning rather
     * than failing the whole load.
     *
     * @param encoded the encoded field constraints, empty when there are none
     */
    protected void loadConstraintsFromPreferenceString(String encoded) {
        for (String constraint : encoded.split(CONSTRAINT_SEPARATOR)) {
            if (constraint.isEmpty()) {
                // No constraints at all: split yields a single empty entry rather than nothing.
                continue;
            }

            String[] fieldAndValue = constraint.split(FIELD_VALUE_SEPARATOR);
            int fieldIndex = Integer.parseInt(fieldAndValue[0]);
            UUID[] uuids = Arrays.stream(fieldAndValue[1].split(UUID_SEPARATOR))
                    .map(UUID::fromString)
                    .toArray(UUID[]::new);

            EntityHandle.get(PublicIds.of(uuids))
                    .ifEntity(entity -> fieldConstraints.put(fieldIndex, entity.toProxy()))
                    .ifAbsent(() -> LOG.warn(
                            "Field constraint concept not in datastore; dropping it: {}", fieldAndValue[1]));
        }
    }

    /*=============================================================================*
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     *=============================================================================*/

    // -- field constraints
    /**
     * The concept each constrained field must hold, keyed by the field's index in the Pattern.
     * Fields with no entry accept any value.
     */
    private final ObservableMap<Integer, EntityProxy> fieldConstraints = FXCollections.observableHashMap();
    public ObservableMap<Integer, EntityProxy> getFieldConstraints() { return fieldConstraints; }
}
