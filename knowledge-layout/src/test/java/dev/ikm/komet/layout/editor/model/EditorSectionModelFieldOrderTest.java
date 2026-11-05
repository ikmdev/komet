package dev.ikm.komet.layout.editor.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * How a Section's reference field order preference is applied to a pattern's fields
 * ({@link EditorSectionModel#fieldIndexesInOrder(List, int)}).
 */
class EditorSectionModelFieldOrderTest {

    @Test
    @DisplayName("No preference keeps pattern order")
    void emptyPreferenceIsPatternOrder() {
        assertEquals(List.of(0, 1, 2, 3), EditorSectionModel.fieldIndexesInOrder(List.of(), 4));
    }

    @Test
    @DisplayName("Preferred fields lead, the rest follow in pattern order")
    void preferredFieldsLead() {
        assertEquals(List.of(2, 0, 1, 3), EditorSectionModel.fieldIndexesInOrder(List.of(2, 0), 4));
    }

    @Test
    @DisplayName("A full preference is used as is")
    void fullPreferenceIsUsedAsIs() {
        assertEquals(List.of(3, 1, 0, 2), EditorSectionModel.fieldIndexesInOrder(List.of(3, 1, 0, 2), 4));
    }

    @Test
    @DisplayName("Indexes the pattern has no field at, and repeats, are ignored")
    void staleAndRepeatedIndexesAreIgnored() {
        assertEquals(List.of(1, 0, 2), EditorSectionModel.fieldIndexesInOrder(List.of(1, 7, -1, 1), 3));
    }

    @Test
    @DisplayName("A pattern without fields orders nothing")
    void noFields() {
        assertEquals(List.of(), EditorSectionModel.fieldIndexesInOrder(List.of(0), 0));
    }
}
