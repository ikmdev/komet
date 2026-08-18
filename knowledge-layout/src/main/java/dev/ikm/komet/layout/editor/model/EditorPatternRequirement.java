package dev.ikm.komet.layout.editor.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * A refinement of a required Pattern (see {@link EditorGridNodeModel#requiredProperty()}): at least
 * {@link #minCountProperty() minCount} semantics of the Pattern must exist whose field values match
 * the requirement's {@link #getFieldConstraints() fieldConstraints}. For example, a Description
 * Pattern can require at least one semantic whose "Description type" field is "Fully qualified name".
 *
 * <p>A Pattern with no requirement models keeps the plain required meaning: at least one semantic of
 * any kind.
 */
public class EditorPatternRequirement extends EditorSemanticConstraintBase {

    /**
     * Encodes this requirement into a single preferences string:
     * {@code <minCount>[;<fieldIndex>:<uuid>[_<uuid>...]]...} — the minimum count followed by the
     * encoded field constraints.
     */
    public String toPreferenceString() {
        String constraints = constraintsToPreferenceString();
        return constraints.isEmpty()
                ? String.valueOf(getMinCount())
                : getMinCount() + CONSTRAINT_SEPARATOR + constraints;
    }

    /**
     * Decodes a requirement encoded by {@link #toPreferenceString()}.
     */
    public static EditorPatternRequirement fromPreferenceString(String encoded) {
        EditorPatternRequirement requirement = new EditorPatternRequirement();

        int endOfMinCount = encoded.indexOf(CONSTRAINT_SEPARATOR);
        if (endOfMinCount < 0) {
            requirement.setMinCount(Integer.parseInt(encoded));
        } else {
            requirement.setMinCount(Integer.parseInt(encoded.substring(0, endOfMinCount)));
            requirement.loadConstraintsFromPreferenceString(encoded.substring(endOfMinCount + 1));
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
}
