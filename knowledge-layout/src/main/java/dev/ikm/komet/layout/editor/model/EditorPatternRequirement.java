package dev.ikm.komet.layout.editor.model;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A refinement of a required Pattern (see {@link EditorGridNodeModel#requiredProperty()}): at least
 * {@link #minCountProperty() minCount} semantics of the Pattern must exist whose field values match
 * every entry in {@link #getFieldConstraints() fieldConstraints}. For example, a Description Pattern
 * can require at least one semantic whose "Description type" field is "Fully qualified name".
 *
 * <p>Constraints are keyed by the field's index in the Pattern; a field with no entry accepts any
 * value. A Pattern with no requirement models keeps the plain required meaning: at least one
 * semantic of any kind.
 */
public class EditorPatternRequirement {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternRequirement.class);

    private static final String CONSTRAINT_SEPARATOR = ";";
    private static final String FIELD_VALUE_SEPARATOR = ":";
    private static final String UUID_SEPARATOR = "_";

    /**
     * Encodes this requirement into a single preferences string:
     * {@code <minCount>[;<fieldIndex>:<uuid>[_<uuid>...]]...} — the concept of each field
     * constraint is stored by its portable PublicId UUIDs.
     */
    public String toPreferenceString() {
        StringBuilder encoded = new StringBuilder(String.valueOf(getMinCount()));
        fieldConstraints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(constraint -> {
                    String uuids = constraint.getValue().publicId().asUuidList().stream()
                            .map(UUID::toString)
                            .collect(Collectors.joining(UUID_SEPARATOR));
                    encoded.append(CONSTRAINT_SEPARATOR)
                            .append(constraint.getKey())
                            .append(FIELD_VALUE_SEPARATOR)
                            .append(uuids);
                });
        return encoded.toString();
    }

    /**
     * Decodes a requirement encoded by {@link #toPreferenceString()}. A constraint whose concept is
     * not present in the datastore is dropped with a warning rather than failing the whole load.
     */
    public static EditorPatternRequirement fromPreferenceString(String encoded) {
        EditorPatternRequirement requirement = new EditorPatternRequirement();

        String[] parts = encoded.split(CONSTRAINT_SEPARATOR);
        requirement.setMinCount(Integer.parseInt(parts[0]));

        for (int i = 1; i < parts.length; i++) {
            String[] fieldAndValue = parts[i].split(FIELD_VALUE_SEPARATOR);
            int fieldIndex = Integer.parseInt(fieldAndValue[0]);
            UUID[] uuids = Arrays.stream(fieldAndValue[1].split(UUID_SEPARATOR))
                    .map(UUID::fromString)
                    .toArray(UUID[]::new);

            EntityHandle.get(PublicIds.of(uuids))
                    .ifEntity(entity -> requirement.getFieldConstraints().put(fieldIndex, entity.toProxy()))
                    .ifAbsent(() -> LOG.warn(
                            "Pattern requirement constraint concept not in datastore; dropping it: {}",
                            fieldAndValue[1]));
        }

        return requirement;
    }

    /*=============================================================================*
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     *=============================================================================*/

    // -- min count
    /**
     * The minimum number of semantics matching this requirement's field constraints.
     */
    private final IntegerProperty minCount = new SimpleIntegerProperty(1);
    public int getMinCount() { return minCount.get(); }
    public IntegerProperty minCountProperty() { return minCount; }
    public void setMinCount(int minCount) { this.minCount.set(minCount); }

    // -- field constraints
    /**
     * The concept each constrained field must hold, keyed by the field's index in the Pattern.
     * Fields with no entry accept any value.
     */
    private final ObservableMap<Integer, EntityProxy> fieldConstraints = FXCollections.observableHashMap();
    public ObservableMap<Integer, EntityProxy> getFieldConstraints() { return fieldConstraints; }
}