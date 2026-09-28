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
package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.PinnedAncestorsLayout;
import dev.ikm.komet.kview.controls.PinnedAncestorsLayout.Metrics;
import dev.ikm.komet.kview.controls.PinnedAncestorsLayout.Result;
import dev.ikm.komet.kview.controls.PinnedAncestorsLayout.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pinned ancestors describe the first row that stays visible below the overlay its own ancestors
 * need, and only the nearest three are pinned until the user asks for all of them.
 */
class PinnedAncestorsLayoutTest {

    private static final double ROW = 26;
    private static final double TOGGLE = 22;
    private static final double INSETS = 3;
    private static final Metrics METRICS = new Metrics(ROW, TOGGLE, INSETS);

    /** Rows of the given depths, one below the other, the first one scrolled out by {@code offset}. */
    private static List<Row> rows(double offset, int... depths) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < depths.length; i++) {
            rows.add(new Row(i * ROW - offset, ROW, depths[i]));
        }
        return rows;
    }

    @Test
    @DisplayName("A top level concept at the top pins nothing")
    void topLevelConceptPinsNothing() {
        Result result = PinnedAncestorsLayout.compute(rows(0, 0, 1, 1, 2), 0, false, METRICS);
        assertEquals(0, result.anchorIndex());
        assertEquals(0, result.shownCount());
        assertEquals(0, result.height());
    }

    @Test
    @DisplayName("The anchor is the first row below the overlay of its own ancestors")
    void anchorIsTheFirstRowBelowItsOwnOverlay() {
        // siblings at depth 2: the overlay takes two rows and the insets, so the two first rows
        // are covered, and the third one still shows
        Result result = PinnedAncestorsLayout.compute(rows(0, 2, 2, 2, 2, 2), 0, false, METRICS);
        assertEquals(2, result.anchorIndex());
        assertEquals(2, result.ancestorCount());
        assertEquals(2, result.shownCount());
        assertFalse(result.toggleVisible());
        assertEquals(INSETS + 2 * ROW, result.height());
    }

    @Test
    @DisplayName("A row that is fully scrolled under the overlay stops being the anchor")
    void anchorChangesWhenTheRowIsFullyCovered() {
        // depth 1: the overlay takes a row and the insets
        List<Row> partiallyCovered = List.of(new Row(-10, ROW, 1), new Row(ROW - 10, ROW, 1), new Row(2 * ROW - 10, ROW, 1));
        assertEquals(1, PinnedAncestorsLayout.compute(partiallyCovered, 0, false, METRICS).anchorIndex());

        List<Row> fullyCovered = List.of(new Row(-25, ROW, 1), new Row(ROW - 25, ROW, 1), new Row(2 * ROW - 25, ROW, 1));
        assertEquals(2, PinnedAncestorsLayout.compute(fullyCovered, 0, false, METRICS).anchorIndex());
    }

    @Test
    @DisplayName("Collapsed, only the nearest three ancestors are pinned, below a toggle")
    void collapsedPinsTheNearestThree() {
        Result result = PinnedAncestorsLayout.compute(rows(0, 6, 6, 6, 6, 6, 6, 6), 0, false, METRICS);
        assertEquals(6, result.ancestorCount());
        assertEquals(3, result.shownCount());
        assertTrue(result.toggleVisible());
        assertEquals(INSETS + TOGGLE + 3 * ROW, result.height());
        assertEquals(3, result.anchorIndex());
    }

    @Test
    @DisplayName("Expanded, all the ancestors are pinned")
    void expandedPinsAllTheAncestors() {
        Result result = PinnedAncestorsLayout.compute(rows(0, 6, 6, 6, 6, 6, 6, 6, 6, 6), 0, true, METRICS);
        assertEquals(6, result.shownCount());
        assertTrue(result.toggleVisible());
        assertEquals(INSETS + TOGGLE + 6 * ROW, result.height());
        assertEquals(6, result.anchorIndex());
    }

    @Test
    @DisplayName("Three ancestors or fewer need no toggle, expanded or not")
    void noToggleUpToThreeAncestors() {
        assertFalse(PinnedAncestorsLayout.compute(rows(0, 3, 3, 3, 3, 3), 0, false, METRICS).toggleVisible());
        assertFalse(PinnedAncestorsLayout.compute(rows(0, 3, 3, 3, 3, 3), 0, true, METRICS).toggleVisible());
    }

    @Test
    @DisplayName("Past the end of a branch, the ancestors are those of the next visible concept")
    void endOfBranchPinsTheAncestorsOfTheNextConcept() {
        // the last three concepts of a branch at depth 3, followed by a top level concept: the three
        // are covered by their own overlay, so the top level concept is the anchor, and nothing is pinned
        Result result = PinnedAncestorsLayout.compute(rows(0, 3, 3, 3, 0, 1), 0, false, METRICS);
        assertEquals(3, result.anchorIndex());
        assertEquals(0, result.shownCount());
    }

    @Test
    @DisplayName("The search for the anchor can start further down")
    void searchStartsFromIndex() {
        Result result = PinnedAncestorsLayout.compute(rows(0, 1, 1, 1, 1), 2, false, METRICS);
        assertEquals(2, result.anchorIndex());
    }

    @Test
    @DisplayName("Without a row that stays visible, nothing is pinned")
    void noVisibleRowPinsNothing() {
        assertEquals(Result.NONE, PinnedAncestorsLayout.compute(rows(0, 6, 6), 0, true, METRICS));
        assertEquals(Result.NONE, PinnedAncestorsLayout.compute(List.of(), 0, false, METRICS));
    }
}
