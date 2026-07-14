package dev.ikm.komet.layout.settings;

import dev.ikm.komet.layout.expand.ExpansionCorner;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-tests {@link ConceptSettingsForm}'s pure reflection helpers (off the FX thread): detecting
 * concept-identity-enum components, reading component values, and rebuilding a record with a single
 * component replaced via its canonical constructor. The FX form itself is smoke-tested in the app.
 */
class ConceptSettingsFormTest {

    /** A settings-record shape: two concept-identity-enum fields and one non-enum field. */
    record Fixture(ExpansionCorner corner, ExpansionCorner second, int count) {
    }

    @Test
    void detectsConceptEnums() {
        assertTrue(ConceptSettingsForm.isConceptEnum(ExpansionCorner.class),
                "a concept-identity value enum is auto-renderable");
        assertFalse(ConceptSettingsForm.isConceptEnum(int.class), "a primitive is not");
        assertFalse(ConceptSettingsForm.isConceptEnum(String.class), "a plain type is not");
    }

    @Test
    void readsComponentValues() {
        Fixture fixture = new Fixture(ExpansionCorner.TOP_LEFT, ExpansionCorner.BOTTOM_RIGHT, 3);
        RecordComponent[] components = Fixture.class.getRecordComponents();
        assertEquals(ExpansionCorner.TOP_LEFT, ConceptSettingsForm.readComponent(fixture, components[0]));
        assertEquals(ExpansionCorner.BOTTOM_RIGHT, ConceptSettingsForm.readComponent(fixture, components[1]));
        assertEquals(3, ConceptSettingsForm.readComponent(fixture, components[2]));
    }

    @Test
    void rebuildReplacesExactlyOneComponent() {
        Fixture original = new Fixture(ExpansionCorner.TOP_LEFT, ExpansionCorner.BOTTOM_RIGHT, 3);
        Fixture rebuilt = ConceptSettingsForm.rebuildWith(original, 0, ExpansionCorner.BOTTOM_LEFT);

        assertEquals(ExpansionCorner.BOTTOM_LEFT, rebuilt.corner(), "the replaced component changed");
        assertEquals(ExpansionCorner.BOTTOM_RIGHT, rebuilt.second(), "other components unchanged");
        assertEquals(3, rebuilt.count(), "other components unchanged");
        assertEquals(new Fixture(ExpansionCorner.TOP_LEFT, ExpansionCorner.BOTTOM_RIGHT, 3), original,
                "the original record is untouched (records are immutable)");
    }
}
