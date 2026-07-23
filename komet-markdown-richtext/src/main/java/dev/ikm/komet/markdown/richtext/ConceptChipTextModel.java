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

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import jfx.incubator.scene.control.richtext.StyleResolver;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.DataFormatHandler;
import jfx.incubator.scene.control.richtext.model.PlainTextFormatHandler;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledInput;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * An <b>editable</b> {@link StyledTextModel} whose paragraphs mix plain text with atomic inline
 * <em>chips</em> — live nodes carrying an interchange token — so a compose surface can hold
 * "evaluate ⟨chip⟩ for similar tests" with the chip editable inline
 * ({@code IKE-Network/ike-issues#789}).
 *
 * <p><b>Why this class exists.</b> The incubator's editable {@code RichTextModel} is text-only, and
 * {@code StyledTextModel.replace(...)} is {@code final} with a segment switch that silently drops
 * {@code INLINE_NODE} — so a chip cannot ride the normal edit path. The view side needs nothing:
 * caret shapes, selection, and {@code VFlow} rendering are already inline-node aware in JavaFX 26.
 * The design therefore splits the edit paths:
 * <ul>
 *   <li><b>Text editing</b> (typing, backspace, delete, Enter) flows through the inherited
 *       {@code replace(...)} into the abstract hooks implemented here — a chip occupies exactly
 *       <b>one character</b> of offset space (matching {@code StyledSegment.ofInlineNode}'s
 *       {@code getTextLength() == 1}), so deleting "the character" at a chip's position removes the
 *       chip atomically, for free, from every keyboard path.</li>
 *   <li><b>Chip insertion</b> goes through {@link #insertChip}, which mutates storage directly and
 *       announces the one-character insertion via the public
 *       {@link #fireChangeEvent(TextPos, TextPos, int, int, int)} — sidestepping the node-blind
 *       {@code replace(...)}.</li>
 * </ul>
 *
 * <p>The chip's token (an id-bearing {@code k:} interchange string supplied by the caller — this
 * module stays tinkar-free) is what the chip <em>means</em> in text form: {@link #toTokenText()}
 * projects the whole content with each chip as its token, which is the send-extraction / clipboard
 * form.
 *
 * <p><b>Spike status ({@code #789} phase 1):</b> editing, chips, line breaks, and removal are
 * implemented and tested; character-level styling ({@code applyStyle}) and paragraph attributes are
 * deferred to the MVP and currently no-ops. Not thread-safe; JavaFX application thread only once
 * attached to a control.
 */
public final class ConceptChipTextModel extends StyledTextModel {

    /** One inline atom: a run of styled text, or an atomic chip occupying one character. */
    private sealed interface Seg permits TextSeg, ChipSeg {
    }

    private record TextSeg(String text, StyleAttributeMap attrs) implements Seg {
    }

    /**
     * An atomic chip. Mutable where the view forces it: the model retains the most recently
     * materialized node and the current selection state, so a selection change can restyle the
     * live chip and a re-render inherits the state.
     */
    private static final class ChipSeg implements Seg {
        /** The chip's interchange projection (e.g. {@code k:uuid=…[Label]}). */
        private final String token;
        /** Builds the live chip node; called by the view per render. */
        private final Supplier<Node> factory;
        /** The most recently materialized node, restyled on selection changes. */
        private Node liveNode;
        /** Whether the chip currently lies within the selection. */
        private boolean selected;

        ChipSeg(String token, Supplier<Node> factory) {
            this.token = token;
            this.factory = factory;
        }

        String token() {
            return token;
        }
    }

    private final List<Para> paragraphs = new ArrayList<>();

    /** The pseudo-class the default selection handler toggles on chip nodes. */
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    /**
     * Restyles a chip node when its selection state changes (and on materialization). The default
     * toggles the {@code :selected} pseudo-class; a host whose chips are inline-styled (where CSS
     * cannot override) supplies its own painter.
     */
    private BiConsumer<Node, Boolean> chipSelectionHandler =
            (node, selected) -> node.pseudoClassStateChanged(SELECTED, selected);

    /** Creates an empty, writable model: one empty paragraph. */
    public ConceptChipTextModel() {
        paragraphs.add(new Para());
        // Paste of ordinary text works via the incubator's plain-text import; COPY is overridden by
        // the higher-priority token handler so a selection containing chips leaves as interchange
        // (prose + k: tokens), not as the one-space placeholders the default segment walk would emit.
        // The base class's RTF/HTML export flavors are removed for the same reason: they render a
        // chip as its one-space segment text, so a rich-clipboard paste would silently lose identity.
        removeDataFormatHandler(DataFormat.RTF, true, false);
        removeDataFormatHandler(DataFormat.HTML, true, false);
        registerDataFormatHandler(PlainTextFormatHandler.getInstance(), true, true, 0);
        registerDataFormatHandler(new TokenTextExportHandler(), true, false, 10);
    }

    /*******************************************************************************
     *  The chip seam (#789): direct mutation + fireChangeEvent                    *
     ******************************************************************************/

    /**
     * Inserts an atomic chip at {@code pos}, bypassing the node-blind {@code replace(...)}: storage
     * is mutated directly and the view is refreshed via the public one-character
     * {@code fireChangeEvent}.
     *
     * @param pos         the insertion position (clamped into the document)
     * @param token       the chip's interchange token, e.g. {@code k:uuid=…[Label]}
     * @param nodeFactory builds the live chip node; invoked by the view on each render
     * @return the position immediately after the inserted chip (where the caret belongs)
     */
    public TextPos insertChip(TextPos pos, String token, Supplier<Node> nodeFactory) {
        Objects.requireNonNull(token, "token is null");
        Objects.requireNonNull(nodeFactory, "nodeFactory is null");
        int index = Math.clamp(pos.index(), 0, paragraphs.size() - 1);
        Para para = paragraphs.get(index);
        int offset = Math.clamp(pos.offset(), 0, para.length());
        para.insertChip(offset, new ChipSeg(token, nodeFactory));
        TextPos at = TextPos.ofLeading(index, offset);
        fireChangeEvent(at, at, 1, 0, 0);
        // The base class's undo records replay through the node-blind final replace() with the
        // positions they captured: after this one-character mutation every earlier record is off by
        // one, and a replayed range containing the chip would silently destroy it. Until chip ops
        // get real undo records (the #789 "full" phase), a chip insert invalidates undo history —
        // an honest gap instead of corruption.
        clearUndoRedo();
        return TextPos.ofLeading(index, offset + 1);
    }

    /**
     * Inserts unstyled {@code text} at {@code pos}, announces the edit, and returns the position just
     * after it — for a caller composing a run of text and chips in one gesture (e.g. a multi-concept
     * drop that joins chips with grammatical separators). Like {@link #insertChip}, it invalidates
     * the undo history rather than leave a stale record over the mutated offsets.
     *
     * @param pos  where to insert
     * @param text the text to insert; {@code null} or empty is a no-op
     * @return the position immediately after the inserted text (or {@code pos} unchanged)
     */
    public TextPos insertText(TextPos pos, String text) {
        if (text == null || text.isEmpty()) {
            return pos;
        }
        int index = Math.clamp(pos.index(), 0, paragraphs.size() - 1);
        Para para = paragraphs.get(index);
        int offset = Math.clamp(pos.offset(), 0, para.length());
        para.insertText(offset, text, StyleAttributeMap.EMPTY);
        TextPos at = TextPos.ofLeading(index, offset);
        fireChangeEvent(at, at, text.length(), 0, 0);
        clearUndoRedo();
        return TextPos.ofLeading(index, offset + text.length());
    }

    /**
     * The document with each chip projected as its interchange token — the send-extraction form: a
     * message composed here serializes to prose plus id-bearing tokens, which the transcript
     * re-renders as chips.
     *
     * @return the token-text projection, paragraphs joined with {@code \n}
     */
    public String toTokenText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            for (Seg seg : paragraphs.get(i).segs) {
                switch (seg) {
                    case TextSeg t -> sb.append(t.text());
                    case ChipSeg c -> sb.append(c.token());
                }
            }
        }
        return sb.toString();
    }

    /**
     * Sets the painter invoked when a chip's selection state changes (and when the view
     * materializes a chip, so re-rendered cells come back correctly styled).
     *
     * @param handler the painter; {@code null} restores the default {@code :selected} pseudo-class
     */
    public void setChipSelectionHandler(BiConsumer<Node, Boolean> handler) {
        this.chipSelectionHandler = handler != null ? handler
                : (node, selected) -> node.pseudoClassStateChanged(SELECTED, selected);
    }

    /**
     * Reflects the control's selection onto the chips: a chip is selected exactly when its
     * one-character position lies inside {@code [anchor, caret)} (either order). The view's own
     * highlight paints <em>behind</em> the text, so an opaque chip must carry its own selected
     * look — the token-field convention — via the selection handler.
     *
     * @param anchor one end of the selection; {@code null} clears chip selection
     * @param caret  the other end; {@code null} clears chip selection
     */
    public void updateSelection(TextPos anchor, TextPos caret) {
        TextPos start = anchor;
        TextPos end = caret;
        if (start != null && end != null && start.compareTo(end) > 0) {
            TextPos swap = start;
            start = end;
            end = swap;
        }
        for (int i = 0; i < paragraphs.size(); i++) {
            int offset = 0;
            for (Seg seg : paragraphs.get(i).segs) {
                if (seg instanceof ChipSeg chip) {
                    boolean inRange = start != null && end != null
                            && TextPos.ofLeading(i, offset).compareTo(start) >= 0
                            && TextPos.ofLeading(i, offset).compareTo(end) < 0;
                    if (chip.selected != inRange) {
                        chip.selected = inRange;
                        if (chip.liveNode != null) {
                            chipSelectionHandler.accept(chip.liveNode, inRange);
                        }
                    }
                }
                offset += seg instanceof TextSeg t ? t.text().length() : 1;
            }
        }
    }

    /** The chips' current selection states in document order — for the range-math tests. */
    List<Boolean> chipSelectionStates() {
        List<Boolean> states = new ArrayList<>();
        for (Para para : paragraphs) {
            for (Seg seg : para.segs) {
                if (seg instanceof ChipSeg chip) {
                    states.add(chip.selected);
                }
            }
        }
        return states;
    }

    /**
     * The token-text projection of {@code [start, end)} only — what a copy of that selection puts
     * on the clipboard: prose verbatim, each chip as its interchange token, paragraphs joined with
     * {@code \n}. Offsets count a chip as one character, exactly as the view does.
     *
     * @param start the selection start (clamped)
     * @param end   the selection end (clamped)
     * @return the selection as interchange text
     */
    public String exportTokenText(TextPos start, TextPos end) {
        // A selection made bottom-up arrives reversed; normalize like replace() does.
        if (start.compareTo(end) > 0) {
            TextPos swap = start;
            start = end;
            end = swap;
        }
        StringBuilder sb = new StringBuilder();
        int fromIndex = Math.clamp(start.index(), 0, paragraphs.size() - 1);
        int toIndex = Math.clamp(end.index(), 0, paragraphs.size() - 1);
        for (int i = fromIndex; i <= toIndex; i++) {
            if (i > fromIndex) {
                sb.append('\n');
            }
            Para para = paragraphs.get(i);
            int from = i == fromIndex ? Math.clamp(start.offset(), 0, para.length()) : 0;
            int to = i == toIndex ? Math.clamp(end.offset(), 0, para.length()) : para.length();
            int segStart = 0;
            for (Seg seg : para.segs) {
                int segLen = seg instanceof TextSeg t ? t.text().length() : 1;
                int segEnd = segStart + segLen;
                if (segEnd > from && segStart < to) {
                    switch (seg) {
                        case TextSeg t -> sb.append(t.text(),
                                Math.max(0, from - segStart), Math.min(segLen, to - segStart));
                        // A chip is atomic: any overlap with the selection copies its whole token.
                        case ChipSeg c -> sb.append(c.token());
                    }
                }
                segStart = segEnd;
            }
        }
        return sb.toString();
    }

    /**
     * Empties the model back to one empty paragraph, announcing the removal of everything — the
     * compose surface's post-send reset.
     */
    public void clear() {
        TextPos end = getDocumentEnd();
        paragraphs.clear();
        paragraphs.add(new Para());
        fireChangeEvent(TextPos.ZERO, end, 0, 0, 0);
        // The sent message is gone; an undo record replayed into the emptied document would
        // resurrect fragments of it at clamped positions. A send is not an edit to step back over.
        clearUndoRedo();
    }

    /**
     * Whether the document holds no text and no chips.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return paragraphs.size() == 1 && paragraphs.getFirst().length() == 0;
    }

    /**
     * The number of chips in the document.
     *
     * @return the chip count
     */
    public int chipCount() {
        int count = 0;
        for (Para para : paragraphs) {
            for (Seg seg : para.segs) {
                if (seg instanceof ChipSeg) {
                    count++;
                }
            }
        }
        return count;
    }

    /*******************************************************************************
     *  StyledTextModel contract                                                   *
     ******************************************************************************/

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public int size() {
        return paragraphs.size();
    }

    @Override
    public String getPlainText(int index) {
        return paragraphs.get(index).plainText();
    }

    @Override
    public RichParagraph getParagraph(int index) {
        RichParagraph.Builder builder = RichParagraph.builder();
        for (Seg seg : paragraphs.get(index).segs) {
            switch (seg) {
                case TextSeg t -> builder.addSegment(t.text(), t.attrs());
                case ChipSeg c -> builder.addInlineNode(() -> materialize(c));
            }
        }
        return builder.build();
    }

    /**
     * Materializes a chip for the view, remembering the live node and re-applying the chip's
     * current selection state — a cell rebuilt mid-selection must come back styled selected.
     */
    private Node materialize(ChipSeg chip) {
        Node node = chip.factory.get();
        chip.liveNode = node;
        chipSelectionHandler.accept(node, chip.selected);
        return node;
    }

    @Override
    protected int insertTextSegment(int index, int offset, String text, StyleAttributeMap attrs) {
        paragraphs.get(index).insertText(offset, text, attrs);
        return text.length();
    }

    @Override
    protected void insertLineBreak(int index, int offset) {
        if (index >= size()) {
            paragraphs.add(new Para());
        } else {
            Para tail = paragraphs.get(index).split(offset);
            paragraphs.add(index + 1, tail);
        }
    }

    @Override
    protected void removeRange(TextPos start, TextPos end) {
        int ix = start.index();
        Para first = paragraphs.get(ix);
        if (ix == end.index()) {
            first.removeSpan(start.offset(), end.offset());
        } else {
            Para last = paragraphs.get(end.index());
            last.removeSpan(0, end.offset());
            first.removeSpan(start.offset(), Integer.MAX_VALUE);
            first.append(last);
            int count = end.index() - ix;
            for (int i = 0; i < count; i++) {
                paragraphs.remove(ix + 1);
            }
        }
    }

    @Override
    protected void insertParagraph(int index, Supplier<javafx.scene.layout.Region> generator) {
        // Block-level regions have no place in a compose line; same stance as RichTextModel.
        throw new UnsupportedOperationException();
    }

    @Override
    protected void applyStyle(int index, int start, int end, StyleAttributeMap attrs, boolean merge) {
        // Spike: character styling is deferred to the MVP; typing carries its attrs through
        // insertTextSegment, so plain composition needs nothing here.
    }

    @Override
    protected void applyParagraphStyle(int index, StyleAttributeMap paragraphAttrs) {
        // Spike: paragraph attributes deferred to the MVP.
    }

    @Override
    protected void setParagraphStyle(int index, StyleAttributeMap paragraphAttrs) {
        // Spike: paragraph attributes deferred to the MVP.
    }

    @Override
    public StyleAttributeMap getStyleAttributeMap(StyleResolver resolver, TextPos pos) {
        return StyleAttributeMap.EMPTY;
    }

    /*******************************************************************************
     *  Paragraph storage                                                          *
     ******************************************************************************/

    /**
     * One paragraph: an ordered run of {@link Seg}s with all offset math counting a chip as one
     * character. Text segments are kept coalesced where insertion allows, so typing does not
     * fragment storage.
     */
    private static final class Para {
        private final List<Seg> segs = new ArrayList<>();

        int length() {
            int len = 0;
            for (Seg seg : segs) {
                len += switch (seg) {
                    case TextSeg t -> t.text().length();
                    case ChipSeg c -> 1;
                };
            }
            return len;
        }

        String plainText() {
            StringBuilder sb = new StringBuilder();
            for (Seg seg : segs) {
                switch (seg) {
                    // A chip projects as one space in plain text, matching the view's
                    // one-character StyledSegment.ofInlineNode convention.
                    case TextSeg t -> sb.append(t.text());
                    case ChipSeg c -> sb.append(' ');
                }
            }
            return sb.toString();
        }

        void insertText(int offset, String text, StyleAttributeMap attrs) {
            int segStart = 0;
            for (int i = 0; i < segs.size(); i++) {
                Seg seg = segs.get(i);
                int segLen = seg instanceof TextSeg t ? t.text().length() : 1;
                if (seg instanceof TextSeg t && offset <= segStart + segLen) {
                    // Inside (or at either edge of) a text run: splice into the run, keeping
                    // storage coalesced under ordinary typing.
                    int at = offset - segStart;
                    String joined = t.text().substring(0, at) + text + t.text().substring(at);
                    segs.set(i, new TextSeg(joined, t.attrs()));
                    return;
                }
                if (offset == segStart) {
                    // At a chip boundary with no preceding text run to splice into.
                    segs.add(i, new TextSeg(text, attrs));
                    return;
                }
                segStart += segLen;
            }
            segs.add(new TextSeg(text, attrs)); // at the very end (or the paragraph is empty)
        }

        void insertChip(int offset, ChipSeg chip) {
            int segStart = 0;
            for (int i = 0; i < segs.size(); i++) {
                Seg seg = segs.get(i);
                int segLen = seg instanceof TextSeg t ? t.text().length() : 1;
                if (offset == segStart) {
                    segs.add(i, chip);
                    return;
                }
                if (seg instanceof TextSeg t && offset < segStart + segLen) {
                    // Inside a text run: split it around the chip.
                    int at = offset - segStart;
                    segs.set(i, new TextSeg(t.text().substring(0, at), t.attrs()));
                    segs.add(i + 1, chip);
                    segs.add(i + 2, new TextSeg(t.text().substring(at), t.attrs()));
                    return;
                }
                segStart += segLen;
            }
            segs.add(chip);
        }

        /** Removes {@code [start, end)}; a chip inside the span is dropped whole (it is one char). */
        void removeSpan(int start, int end) {
            List<Seg> kept = new ArrayList<>();
            int segStart = 0;
            for (Seg seg : segs) {
                int segLen = seg instanceof TextSeg t ? t.text().length() : 1;
                int segEnd = segStart + segLen;
                if (segEnd <= start || segStart >= end) {
                    kept.add(seg); // wholly outside the span
                } else if (seg instanceof TextSeg t) {
                    // Partially covered text run: keep the outside pieces.
                    String before = segStart < start ? t.text().substring(0, start - segStart) : "";
                    String after = segEnd > end ? t.text().substring(end - segStart) : "";
                    if (!before.isEmpty()) {
                        kept.add(new TextSeg(before, t.attrs()));
                    }
                    if (!after.isEmpty()) {
                        kept.add(new TextSeg(after, t.attrs()));
                    }
                }
                // A chip overlapping the span is dropped: atomic delete.
                segStart = segEnd;
            }
            segs.clear();
            segs.addAll(coalesce(kept));
        }

        /** Splits at {@code offset}; this paragraph keeps {@code [0, offset)}, the tail gets the rest. */
        Para split(int offset) {
            Para tail = new Para();
            List<Seg> head = new ArrayList<>();
            int segStart = 0;
            for (Seg seg : segs) {
                int segLen = seg instanceof TextSeg t ? t.text().length() : 1;
                int segEnd = segStart + segLen;
                if (segEnd <= offset) {
                    head.add(seg);
                } else if (segStart >= offset) {
                    tail.segs.add(seg);
                } else if (seg instanceof TextSeg t) {
                    int at = offset - segStart;
                    head.add(new TextSeg(t.text().substring(0, at), t.attrs()));
                    tail.segs.add(new TextSeg(t.text().substring(at), t.attrs()));
                }
                segStart = segEnd;
            }
            segs.clear();
            segs.addAll(head);
            return tail;
        }

        void append(Para other) {
            segs.addAll(other.segs);
            List<Seg> joined = coalesce(new ArrayList<>(segs));
            segs.clear();
            segs.addAll(joined);
        }

        /** Merges adjacent text runs with equal attributes; drops empty text runs. */
        private static List<Seg> coalesce(List<Seg> in) {
            List<Seg> out = new ArrayList<>(in.size());
            for (Seg seg : in) {
                if (seg instanceof TextSeg t && t.text().isEmpty()) {
                    continue;
                }
                if (!out.isEmpty()
                        && out.getLast() instanceof TextSeg prev
                        && seg instanceof TextSeg next
                        && Objects.equals(prev.attrs(), next.attrs())) {
                    out.set(out.size() - 1, new TextSeg(prev.text() + next.text(), prev.attrs()));
                } else {
                    out.add(seg);
                }
            }
            return out;
        }
    }

    /**
     * Plain-text COPY as interchange: a selection leaves the model as prose plus {@code k:} tokens
     * via {@link #exportTokenText}, replacing the incubator default whose segment walk would emit a
     * single space per chip. Export only — import (paste) stays with the standard plain-text handler.
     */
    private static final class TokenTextExportHandler extends DataFormatHandler {
        TokenTextExportHandler() {
            super(DataFormat.PLAIN_TEXT);
        }

        @Override
        public Object copy(StyledTextModel model, StyleResolver resolver, TextPos start, TextPos end) {
            return ((ConceptChipTextModel) model).exportTokenText(start, end);
        }

        @Override
        public void save(StyledTextModel model, StyleResolver resolver, TextPos start, TextPos end,
                         OutputStream out) throws IOException {
            out.write(((ConceptChipTextModel) model).exportTokenText(start, end)
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        @Override
        public StyledInput createStyledInput(String text, StyleAttributeMap attr) {
            throw new UnsupportedOperationException("export-only handler; paste uses the plain handler");
        }
    }
}
