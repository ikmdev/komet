/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.markdown.richtext;

import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.ContentChange;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@code #789} spike assertions for {@link ConceptChipTextModel}: mixed text+chip storage with
 * chip-as-one-character offset math, atomic chip removal through the ordinary
 * {@code removeRange} path (what every keyboard delete reduces to), line-break splitting, and the
 * {@code insertChip → fireChangeEvent} announcement. All headless — the model is pure data; the
 * chip node supplier is never invoked (the view path is exercised by the manual spike harness and
 * the MVP smoke).
 */
class ConceptChipTextModelTest {

    private static final String TOKEN_A = "k:uuid=e07f8c60-1234-1234-1234-1234567890ab[Chronic disease]";
    private static final Supplier<Node> NEVER = () -> {
        throw new AssertionError("the chip node supplier must not be invoked headless");
    };
    private static final StyleAttributeMap PLAIN = StyleAttributeMap.EMPTY;

    private static ConceptChipTextModel modelWith(String text) {
        ConceptChipTextModel model = new ConceptChipTextModel();
        model.insertTextSegment(0, 0, text, PLAIN);
        return model;
    }

    @Test
    void startsAsOneEmptyParagraph() {
        ConceptChipTextModel model = new ConceptChipTextModel();
        assertTrue(model.isWritable());
        assertEquals(1, model.size());
        assertEquals("", model.getPlainText(0));
        assertEquals(0, model.chipCount());
    }

    @Test
    void typingSplicesIntoACoalescedRun() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertTextSegment(0, 7, " these", PLAIN); // "compare| with" → "compare these with"
        assertEquals("compare these with", model.getPlainText(0));
        assertEquals("compare these with", model.toTokenText());
    }

    @Test
    void insertChipOccupiesOneCharacterAndProjectsItsToken() {
        ConceptChipTextModel model = modelWith("compare with");
        TextPos after = model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER);

        assertEquals(1, model.chipCount());
        assertEquals(TextPos.ofLeading(0, 9), after, "the caret lands just after the one-char chip");
        assertEquals("compare " + " " + "with", model.getPlainText(0), "a chip is one space in plain text");
        assertEquals("compare " + TOKEN_A + "with", model.toTokenText(), "the token is the interchange projection");
    }

    @Test
    void insertChipAnnouncesAOneCharacterEditViaFireChangeEvent() {
        ConceptChipTextModel model = modelWith("ab");
        List<ContentChange> seen = new ArrayList<>();
        model.addListener(new StyledTextModel.Listener() {
            @Override
            public void onContentChange(ContentChange change) {
                seen.add(change);
            }
        });

        model.insertChip(TextPos.ofLeading(0, 1), TOKEN_A, NEVER);

        assertEquals(1, seen.size(), "exactly one change event per chip insert");
        ContentChange change = seen.getFirst();
        assertTrue(change.isEdit());
        assertEquals(TextPos.ofLeading(0, 1), change.getStart());
        assertEquals(TextPos.ofLeading(0, 1), change.getEnd());
        assertEquals(1, change.getCharsAddedTop(), "the chip is one character to the view");
        assertEquals(0, change.getLinesAdded());
        assertEquals(0, change.getCharsAddedBottom());
    }

    @Test
    void backspaceRemovesTheChipAtomically() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER);

        // Backspace after the chip is exactly removeRange(chip, chip+1) through the normal edit path.
        model.removeRange(TextPos.ofLeading(0, 8), TextPos.ofLeading(0, 9));

        assertEquals(0, model.chipCount(), "the chip deletes as a unit");
        assertEquals("compare with", model.getPlainText(0), "the text coalesces back around it");
        assertEquals("compare with", model.toTokenText());
    }

    @Test
    void editingAdjacentTextLeavesTheChipIntact() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER); // "compare ⟨chip⟩with"

        model.removeRange(TextPos.ofLeading(0, 0), TextPos.ofLeading(0, 8)); // delete "compare "
        assertEquals(1, model.chipCount());
        assertEquals(" with", model.getPlainText(0));

        model.insertTextSegment(0, 1, " it", PLAIN); // type just after the chip
        assertEquals(TOKEN_A + " itwith", model.toTokenText());
    }

    @Test
    void lineBreakSplitsAroundTheChip() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER); // "compare ⟨chip⟩with"

        model.insertLineBreak(0, 9); // split just after the chip

        assertEquals(2, model.size());
        assertEquals("compare " + TOKEN_A, model.toTokenText().lines().findFirst().orElseThrow());
        assertEquals(1, model.chipCount(), "the chip stays whole in the first paragraph");
        assertEquals("with", model.getPlainText(1));
    }

    @Test
    void clearResetsToOneEmptyParagraphAndAnnouncesIt() {
        ConceptChipTextModel model = modelWith("some text");
        model.insertChip(TextPos.ofLeading(0, 4), TOKEN_A, NEVER);
        List<ContentChange> seen = new ArrayList<>();
        model.addListener(change -> seen.add(change));

        model.clear();

        assertTrue(model.isEmpty());
        assertEquals(1, model.size());
        assertEquals(0, model.chipCount());
        assertEquals(1, seen.size());
        assertEquals(TextPos.ZERO, seen.getFirst().getStart(), "the whole document was removed");
    }

    @Test
    void isEmptyOnlyWhenNoTextAndNoChips() {
        ConceptChipTextModel model = new ConceptChipTextModel();
        assertTrue(model.isEmpty());
        model.insertChip(TextPos.ZERO, TOKEN_A, NEVER);
        assertTrue(!model.isEmpty(), "a chip alone is content");
        model.clear();
        assertTrue(model.isEmpty());
    }

    @Test
    void selectionExportCopiesChipsAsWholeTokens() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER); // "compare ⟨chip⟩with"

        // A selection sweeping partway into text on both sides of the chip.
        String copied = model.exportTokenText(TextPos.ofLeading(0, 4), TextPos.ofLeading(0, 11));
        assertEquals("are " + TOKEN_A + "wi", copied, "text clips at the bounds; the chip copies whole");

        // A selection that only grazes the chip still carries the full token (atomicity).
        assertEquals(TOKEN_A, model.exportTokenText(TextPos.ofLeading(0, 8), TextPos.ofLeading(0, 9)));

        // A bottom-up (reversed) selection normalizes rather than throwing.
        assertEquals("are " + TOKEN_A + "wi",
                model.exportTokenText(TextPos.ofLeading(0, 11), TextPos.ofLeading(0, 4)));
    }

    @Test
    void insertTextSplicesPlainRunsAndInterleavesWithChips() {
        ConceptChipTextModel model = new ConceptChipTextModel();
        TextPos afterFirst = model.insertText(TextPos.ZERO, "compare ");
        assertEquals(TextPos.ofLeading(0, 8), afterFirst, "the returned pos is just after the text");

        TextPos afterChip = model.insertChip(afterFirst, TOKEN_A, NEVER);
        model.insertText(afterChip, " and more");

        assertEquals("compare " + TOKEN_A + " and more", model.toTokenText(),
                "text and chips interleave in one composed run");
        assertEquals(TextPos.ZERO, model.insertText(TextPos.ZERO, ""), "empty text is a no-op");
    }

    @Test
    void selectionStateFollowsTheRangeChipAsOneCharacter() {
        ConceptChipTextModel model = modelWith("compare with");
        model.insertChip(TextPos.ofLeading(0, 8), TOKEN_A, NEVER); // chip at offset 8

        model.updateSelection(TextPos.ofLeading(0, 4), TextPos.ofLeading(0, 11));
        assertEquals(List.of(true), model.chipSelectionStates(), "a sweep across the chip selects it");

        model.updateSelection(TextPos.ofLeading(0, 9), TextPos.ofLeading(0, 12));
        assertEquals(List.of(false), model.chipSelectionStates(), "a range starting after it does not");

        model.updateSelection(TextPos.ofLeading(0, 11), TextPos.ofLeading(0, 4));
        assertEquals(List.of(true), model.chipSelectionStates(), "reversed (bottom-up) ranges normalize");

        model.updateSelection(null, null);
        assertEquals(List.of(false), model.chipSelectionStates(), "clearing the selection deselects");
    }

    @Test
    void crossParagraphRemovalMergesAndKeepsOutsideChips() {
        ConceptChipTextModel model = modelWith("first line");
        model.insertChip(TextPos.ofLeading(0, 0), TOKEN_A, NEVER); // chip at the very start
        model.insertLineBreak(0, 6);                                // "⟨chip⟩first" / " line"

        // Remove from after "⟨chip⟩f" on line 0 through " l" on line 1.
        model.removeRange(TextPos.ofLeading(0, 2), TextPos.ofLeading(1, 2));

        assertEquals(1, model.size(), "paragraphs merge");
        assertEquals(1, model.chipCount(), "a chip outside the span survives");
        assertEquals(TOKEN_A + "fine", model.toTokenText());
    }
}
