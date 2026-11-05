package dev.ikm.komet.kview.mvvm.view.genpurpose.test;

import dev.ikm.komet.kview.mvvm.view.genpurpose.AutomaticFieldOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The automatic order of a section's reference fields: the fields whose values best tell the
 * semantics apart first ({@link AutomaticFieldOrder#rank(List, int)}).
 */
class AutomaticFieldOrderTest {

    @Test
    @DisplayName("Nothing to tell apart keeps pattern order")
    void noSemanticsOrOneIsPatternOrder() {
        assertEquals(List.of(0, 1, 2), AutomaticFieldOrder.rank(List.of(), 3));
        assertEquals(List.of(0, 1, 2), AutomaticFieldOrder.rank(List.of(List.of("a", "b", "c")), 3));
    }

    @Test
    @DisplayName("Identical semantics keep pattern order")
    void identicalSemanticsArePatternOrder() {
        List<List<String>> rows = List.of(List.of("a", "b"), List.of("a", "b"));
        assertEquals(List.of(0, 1), AutomaticFieldOrder.rank(rows, 2));
    }

    @Test
    @DisplayName("The one field that differs comes first")
    void differingFieldLeads() {
        List<List<String>> rows = List.of(
                List.of("same", "x", "same"),
                List.of("same", "y", "same"),
                List.of("same", "z", "same"));
        assertEquals(List.of(1, 0, 2), AutomaticFieldOrder.rank(rows, 3));
    }

    @Test
    @DisplayName("The field with more distinct values leads one with fewer")
    void moreDistinctValuesLead() {
        List<List<String>> rows = List.of(
                List.of("p", "1"),
                List.of("p", "2"),
                List.of("q", "3"));
        assertEquals(List.of(1, 0), AutomaticFieldOrder.rank(rows, 2));
    }

    @Test
    @DisplayName("The second field is the one that splits what the first left tied")
    void secondFieldSplitsTheRemainingTie() {
        // Every field splits the four in two at first, so field 0 leads as the earliest. It leaves
        // {A,B} and {C,D} tied; field 1 only repeats that split, while field 2 tells both pairs apart.
        List<List<String>> rows = List.of(
                List.of("left", "1", "i"),
                List.of("left", "1", "ii"),
                List.of("right", "2", "i"),
                List.of("right", "2", "ii"));
        assertEquals(List.of(0, 2, 1), AutomaticFieldOrder.rank(rows, 3));
    }

    @Test
    @DisplayName("A blank value is a value: set versus not set tells semantics apart")
    void blankIsAValue() {
        List<List<String>> rows = List.of(List.of("", "same"), List.of("filled", "same"));
        assertEquals(List.of(0, 1), AutomaticFieldOrder.rank(rows, 2));
    }

    @Test
    @DisplayName("A short row and a null entry count as blank")
    void shortRowsAndNullsAreBlank() {
        List<List<String>> rows = List.of(
                Arrays.asList("same", null),
                List.of("same"),
                List.of("same", "set"));
        assertEquals(List.of(1, 0), AutomaticFieldOrder.rank(rows, 2));
    }

    @Test
    @DisplayName("Ties between equally distinguishing fields go to pattern order")
    void tiesGoToPatternOrder() {
        List<List<String>> rows = List.of(List.of("a", "x"), List.of("b", "y"));
        assertEquals(List.of(0, 1), AutomaticFieldOrder.rank(rows, 2));
    }
}
