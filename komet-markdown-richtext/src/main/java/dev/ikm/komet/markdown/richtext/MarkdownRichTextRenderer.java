/*
 * Copyright © 2026 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.markdown.richtext;

import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders CommonMark (with the GFM tables extension) into styled {@link RichParagraph} runs
 * for the incubator {@code RichTextArea}. Block and inline structure is generic; every
 * <em>content</em> text run is delegated to an {@link InlineDecorator}, so consumers can
 * inject inline nodes (e.g. concept chips) without this module depending on them. Fenced code
 * blocks are dispatched by their info string to a {@link BlockRenderer}, so consumers can render
 * a closed menu of block presentations (e.g. a concept tree) as native nodes, with any
 * unrecognised tag falling through to preformatted text. Structural text — table separators,
 * horizontal rules, fenced-code lines — is emitted directly.
 *
 * <p>This renderer is tinkar-free and holds no JavaFX scene state; it only builds the
 * RichTextArea model. Pair the produced paragraphs with {@link MarkdownStyledModel}.
 */
public final class MarkdownRichTextRenderer {

    private static final Parser PARSER = Parser.builder()
            .extensions(List.of(TablesExtension.create()))
            .build();
    private static final String MONO = "monospace";

    private final double base;
    private final String fontFamily;
    private final InlineDecorator decorator;
    private final BlockRenderer blockRenderer;

    /**
     * Equivalent to {@link #MarkdownRichTextRenderer(double, InlineDecorator, BlockRenderer)} with
     * no block renderer, so every fenced code block renders as preformatted text.
     *
     * @param base      base body font size in px; headings and code scale from it
     * @param decorator the inline-run decorator; {@code null} uses {@link InlineDecorator#PLAIN}
     */
    public MarkdownRichTextRenderer(double base, InlineDecorator decorator) {
        this(base, decorator, BlockRenderer.NONE);
    }

    /**
     * Equivalent to {@link #MarkdownRichTextRenderer(double, String, InlineDecorator, BlockRenderer)}
     * with the platform-default body font family.
     *
     * @param base          base body font size in px; headings and code scale from it
     * @param decorator     the inline-run decorator; {@code null} uses {@link InlineDecorator#PLAIN}
     * @param blockRenderer the fenced-block renderer, dispatched by the block's info string;
     *                      {@code null} uses {@link BlockRenderer#NONE}
     */
    public MarkdownRichTextRenderer(double base, InlineDecorator decorator, BlockRenderer blockRenderer) {
        this(base, null, decorator, blockRenderer);
    }

    /**
     * @param base          base body font size in px; headings and code scale from it
     * @param fontFamily    the base body font family applied to prose, headings, lists, and the
     *                      non-code runs of table cells (and inherited by concept chips);
     *                      {@code null} uses the platform default. Code spans and code blocks stay
     *                      monospaced regardless — this only sets the <em>prose</em> family, which
     *                      is a model attribute (not CSS), so it is the only way to give, e.g., a
     *                      print rendering a serif manuscript body.
     * @param decorator     the inline-run decorator; {@code null} uses {@link InlineDecorator#PLAIN}
     * @param blockRenderer the fenced-block renderer, dispatched by the block's info string;
     *                      {@code null} uses {@link BlockRenderer#NONE}
     */
    public MarkdownRichTextRenderer(double base, String fontFamily, InlineDecorator decorator,
                                    BlockRenderer blockRenderer) {
        this.base = base;
        this.fontFamily = fontFamily;
        this.decorator = decorator == null ? InlineDecorator.PLAIN : decorator;
        this.blockRenderer = blockRenderer == null ? BlockRenderer.NONE : blockRenderer;
    }

    /**
     * Combines the configured base font family (when set) onto the caller's base style, so all
     * prose that inherits from it picks up the family. Code runs re-set {@code monospace} on top of
     * the result, so they stay monospaced. Returns the style unchanged when no family is configured.
     */
    private StyleAttributeMap withFamily(StyleAttributeMap baseStyle) {
        if (fontFamily == null) {
            return baseStyle;
        }
        return baseStyle.combine(StyleAttributeMap.builder().setFontFamily(fontFamily).build());
    }

    /**
     * Parses {@code markdown} and appends its rendered paragraphs (and the parallel plain-text
     * projection) to {@code out}/{@code plain}, using {@code baseStyle} as the base run style.
     */
    public void render(String markdown, StyleAttributeMap baseStyle,
                       List<RichParagraph> out, List<String> plain) {
        StyleAttributeMap effectiveBase = withFamily(baseStyle);
        Node document = PARSER.parse(markdown == null ? "" : markdown);
        for (Node block = document.getFirstChild(); block != null; block = block.getNext()) {
            renderBlock(block, out, plain, 0, effectiveBase);
        }
    }

    /**
     * Renders non-Markdown text: one paragraph per line, each line still passed through the
     * decorator (so inline nodes are injected even outside Markdown).
     */
    public void renderPlainText(String text, StyleAttributeMap style,
                                List<RichParagraph> out, List<String> plain) {
        StyleAttributeMap effectiveStyle = withFamily(style);
        for (String line : (text == null ? "" : text).split("\n", -1)) {
            RichParagraph.Builder b = RichParagraph.builder();
            decorator.emit(b, line, effectiveStyle);
            out.add(b.build());
            plain.add(line);
        }
    }

    /**
     * Parses {@code markdown} and renders it as an ordered list of {@link DocumentSegment}s — the
     * block-stack projection. Splitting happens only at the <em>top level</em> of the document: a
     * fenced code block whose info string the {@link BlockRenderer} accepts, and a GFM table,
     * each flush the accumulated prose and emit a {@link DocumentSegment.NodeBlock}; every other
     * block (including a fenced block the renderer declines, which stays preformatted text, and
     * any table or fence nested inside a quote or list) accumulates into the current
     * {@link DocumentSegment.ProseRun}. Rendering any block is byte-identical to
     * {@link #render}; only the packaging differs.
     *
     * @param markdown  the CommonMark source; {@code null} is treated as empty
     * @param baseStyle the base run style prose inherits
     * @return the document's segments in order; empty when the source renders to nothing
     */
    public List<DocumentSegment> renderSegments(String markdown, StyleAttributeMap baseStyle) {
        List<DocumentSegment> segments = new ArrayList<>();
        List<RichParagraph> proseOut = new ArrayList<>();
        List<String> prosePlain = new ArrayList<>();
        StyleAttributeMap effectiveBase = withFamily(baseStyle);
        Node document = PARSER.parse(markdown == null ? "" : markdown);
        for (Node block = document.getFirstChild(); block != null; block = block.getNext()) {
            switch (block) {
                case FencedCodeBlock fc -> {
                    List<BlockPiece> pieces = blockRenderer.render(fc.getInfo(), fc.getLiteral(), effectiveBase);
                    if (pieces.isEmpty()) {
                        // Declined tag — preformatted text, part of the flowing prose.
                        emitCodeBlock(fc.getLiteral(), proseOut, prosePlain);
                    } else {
                        flushProse(segments, proseOut, prosePlain);
                        for (BlockPiece piece : pieces) {
                            segments.add(new DocumentSegment.NodeBlock(piece.node(), piece.plainText()));
                        }
                    }
                }
                case TableBlock tb -> {
                    flushProse(segments, proseOut, prosePlain);
                    segments.add(new DocumentSegment.NodeBlock(() -> buildTableNode(tb),
                            GfmTableSerializer.serialize(TableModel.fromCommonMark(tb))));
                }
                default -> renderBlock(block, proseOut, prosePlain, 0, effectiveBase);
            }
        }
        flushProse(segments, proseOut, prosePlain);
        return segments;
    }

    /** Wraps any accumulated prose paragraphs into a {@link DocumentSegment.ProseRun} and clears. */
    private static void flushProse(List<DocumentSegment> segments,
                                   List<RichParagraph> out, List<String> plain) {
        if (!out.isEmpty()) {
            segments.add(new DocumentSegment.ProseRun(out, plain));
            out.clear();
            plain.clear();
        }
    }

    // ---- Block rendering ---------------------------------------------------

    private void renderBlock(Node block, List<RichParagraph> out, List<String> plain,
                             int listDepth, StyleAttributeMap baseStyle) {
        switch (block) {
            case Heading h -> {
                RichParagraph.Builder b = RichParagraph.builder();
                StyleAttributeMap hs = baseStyle.combine(
                        StyleAttributeMap.builder().setBold(true).setFontSize(headingSize(h.getLevel())).build());
                appendInlines(b, h.getFirstChild(), hs);
                emit(b, plainOf(h), out, plain);
            }
            case Paragraph p -> {
                RichParagraph.Builder b = RichParagraph.builder();
                appendInlines(b, p.getFirstChild(), baseStyle);
                emit(b, plainOf(p), out, plain);
            }
            case BlockQuote q -> {
                StyleAttributeMap qs = baseStyle.combine(StyleAttributeMap.builder().setItalic(true).build());
                for (Node c = q.getFirstChild(); c != null; c = c.getNext()) {
                    renderBlock(c, out, plain, listDepth, qs);
                }
            }
            case FencedCodeBlock fc -> emitFencedBlock(fc, out, plain, baseStyle);
            case IndentedCodeBlock ic -> emitCodeBlock(ic.getLiteral(), out, plain);
            case BulletList bl -> {
                for (Node item = bl.getFirstChild(); item != null; item = item.getNext()) {
                    renderListItem(item, "•  ", out, plain, listDepth, baseStyle);
                }
            }
            case OrderedList ol -> {
                int n = 1;
                for (Node item = ol.getFirstChild(); item != null; item = item.getNext()) {
                    renderListItem(item, (n++) + ".  ", out, plain, listDepth, baseStyle);
                }
            }
            case ThematicBreak ignored -> emit(RichParagraph.builder().addSegment("─".repeat(10), baseStyle),
                    "─".repeat(10), out, plain);
            case TableBlock tb -> renderTable(tb, out, plain, baseStyle);
            default -> {
                String text = plainOf(block);
                if (!text.isBlank()) {
                    RichParagraph.Builder b = RichParagraph.builder();
                    decorator.emit(b, text, baseStyle);
                    emit(b, text, out, plain);
                }
            }
        }
    }

    private void renderListItem(Node item, String prefix, List<RichParagraph> out, List<String> plain,
                                int listDepth, StyleAttributeMap baseStyle) {
        if (!(item instanceof ListItem)) {
            renderBlock(item, out, plain, listDepth, baseStyle);
            return;
        }
        String indent = "  ".repeat(Math.max(0, listDepth));
        boolean first = true;
        for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Paragraph p) {
                RichParagraph.Builder b = RichParagraph.builder();
                addSegment(b, indent + (first ? prefix : "   "), baseStyle);
                appendInlines(b, p.getFirstChild(), baseStyle);
                emit(b, indent + (first ? prefix : "   ") + plainOf(p), out, plain);
                first = false;
            } else {
                renderBlock(child, out, plain, listDepth + 1, baseStyle);
            }
        }
    }

    /**
     * Renders a GFM table as a single paragraph holding one inline {@link GridPane} (built lazily
     * when the paragraph is laid out), with chip-aware {@link TextFlow} cells. The parallel
     * plain-text projection is the table's GFM serialization, so a RichTextArea copy yields the
     * #596 interchange format.
     */
    private void renderTable(TableBlock tb, List<RichParagraph> out, List<String> plain,
                             StyleAttributeMap baseStyle) {
        out.add(RichParagraph.builder().addInlineNode(() -> buildTableNode(tb)).build());
        plain.add(GfmTableSerializer.serialize(TableModel.fromCommonMark(tb)));
    }

    private GridPane buildTableNode(TableBlock tb) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("md-table");
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setMaxWidth(Double.MAX_VALUE);

        int row = 0;
        int columnCount = 0;
        for (Node section = tb.getFirstChild(); section != null; section = section.getNext()) {
            if (section instanceof TableHead head) {
                row = renderTableSection(head, grid, row, true);
                columnCount = Math.max(columnCount, columnsInFirstRow(head));
            } else if (section instanceof TableBody body) {
                row = renderTableSection(body, grid, row, false);
                columnCount = Math.max(columnCount, columnsInFirstRow(body));
            }
        }
        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            // SOMETIMES + a fill default lets columns share the available width and lets each
            // cell's TextFlow wrap rather than overflow a narrow host (e.g. KlSupplementalArea).
            cc.setHgrow(Priority.SOMETIMES);
            cc.setFillWidth(true);
            // Floor every column: a TextFlow cell reports a near-zero min width, so without this a
            // short-label column sitting next to long-content columns gets starved down to a single
            // character and wraps its label vertically. The floor only binds in that pathological
            // case; otherwise columns stay content-proportional.
            cc.setMinWidth(base * 6);
            grid.getColumnConstraints().add(cc);
        }
        // Right-click → copy the table to the clipboard in the #596 interchange format
        // (text/plain GFM + text/html), so it pastes losslessly into Zulip/adoc and richly elsewhere.
        MenuItem copyItem = new MenuItem("Copy table");
        copyItem.setOnAction(ignored -> TableClipboard.copy(TableModel.fromCommonMark(tb)));
        ContextMenu menu = new ContextMenu(copyItem);
        grid.setOnContextMenuRequested(e -> menu.show(grid, e.getScreenX(), e.getScreenY()));
        return grid;
    }

    private int renderTableSection(Node section, GridPane grid, int startRow, boolean header) {
        int row = startRow;
        for (Node rowNode = section.getFirstChild(); rowNode != null; rowNode = rowNode.getNext()) {
            if (!(rowNode instanceof TableRow tr)) {
                continue;
            }
            int col = 0;
            for (Node cellNode = tr.getFirstChild(); cellNode != null; cellNode = cellNode.getNext()) {
                if (!(cellNode instanceof TableCell tc)) {
                    continue;
                }
                TextFlow cell = buildCellFlow(tc, header);
                grid.add(cell, col, row);
                GridPane.setHgrow(cell, Priority.SOMETIMES);
                col++;
            }
            row++;
        }
        return row;
    }

    private static int columnsInFirstRow(Node section) {
        int n = 0;
        for (Node r = section.getFirstChild(); r != null; r = r.getNext()) {
            if (r instanceof TableRow tr) {
                for (Node c = tr.getFirstChild(); c != null; c = c.getNext()) {
                    if (c instanceof TableCell) {
                        n++;
                    }
                }
                return n;
            }
        }
        return n;
    }

    /**
     * Builds a table cell as a wrapping {@link TextFlow} whose text runs and concept chips come
     * from the same {@link InlineDecorator} used for flowing text — so identicon chips render
     * inside cells, not as stray text.
     */
    private TextFlow buildCellFlow(TableCell tc, boolean header) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add(header ? "md-table-header" : "md-table-cell");
        flow.setPadding(new Insets(3, 8, 3, 8));
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setTextAlignment(alignmentOf(tc));
        // Inline gridlines so the table reads as a grid even without an external stylesheet
        // (the assistant transcript's RichTextArea doesn't load markdown.css). Adjacent cells'
        // half-px borders overlap into ~1px rules; the header row gets a light fill.
        flow.setStyle("-fx-border-color: #d0d7de; -fx-border-width: 0.5;"
                + (header ? " -fx-background-color: #f6f8fa;" : ""));
        appendCellInlines(flow, tc.getFirstChild(), new CellStyle(header, false, false, false));
        return flow;
    }

    /** Active inline style while walking a table cell (built explicitly to avoid reading back the
     *  incubator {@code StyleAttributeMap}). */
    private record CellStyle(boolean bold, boolean italic, boolean strike, boolean mono) {
        CellStyle withBold() {
            return new CellStyle(true, italic, strike, mono);
        }

        CellStyle withItalic() {
            return new CellStyle(bold, true, strike, mono);
        }

        CellStyle withMono() {
            return new CellStyle(bold, italic, strike, true);
        }
    }

    private void appendCellInlines(TextFlow flow, Node first, CellStyle style) {
        for (Node n = first; n != null; n = n.getNext()) {
            switch (n) {
                case Text t -> emitCellText(flow, t.getLiteral(), style);
                case StrongEmphasis s -> appendCellInlines(flow, s.getFirstChild(), style.withBold());
                case Emphasis e -> appendCellInlines(flow, e.getFirstChild(), style.withItalic());
                case Code c -> emitCellText(flow, c.getLiteral(), style.withMono());
                case SoftLineBreak ignored -> flow.getChildren().add(new javafx.scene.text.Text(" "));
                case HardLineBreak ignored -> flow.getChildren().add(new javafx.scene.text.Text(" "));
                case Link l -> appendCellInlines(flow, l.getFirstChild(), style);
                case Image img -> flow.getChildren().add(imageNode(img.getDestination()));
                default -> emitCellText(flow, plainOf(n), style);
            }
        }
    }

    /**
     * Routes a cell text run through the decorator, materialising text pieces as styled
     * {@code Text} nodes and node pieces (chips) directly into the flow. Unlike the flowing-text
     * path (which can defer a node via {@code addInlineNode}), a {@link TextFlow} cell needs real
     * child nodes, so node pieces are materialised here — when the table's own {@code GridPane}
     * supplier runs (already deferred until the table is laid out). A null node (a misbehaving
     * decorator) is skipped rather than added, since {@code TextFlow} rejects null children.
     */
    private void emitCellText(TextFlow flow, String text, CellStyle style) {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (InlinePiece piece : decorator.decorate(text, cellStyleAttr(style))) {
            switch (piece) {
                case InlinePiece.TextRun tr -> {
                    if (tr.text() != null && !tr.text().isEmpty()) {
                        flow.getChildren().add(cellTextNode(tr.text(), style));
                    }
                }
                case InlinePiece.NodeRun nr -> {
                    javafx.scene.Node node = nr.node().get();
                    if (node != null) {
                        flow.getChildren().add(node);
                    }
                }
            }
        }
    }

    private javafx.scene.text.Text cellTextNode(String text, CellStyle style) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(text);
        FontWeight weight = style.bold() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = style.italic() ? FontPosture.ITALIC : FontPosture.REGULAR;
        t.setFont(Font.font(style.mono() ? MONO : fontFamily, weight, posture, base));
        t.setStrikethrough(style.strike());
        return t;
    }

    private StyleAttributeMap cellStyleAttr(CellStyle style) {
        StyleAttributeMap.Builder b = StyleAttributeMap.builder().setFontSize(base);
        if (style.bold()) {
            b.setBold(true);
        }
        if (style.italic()) {
            b.setItalic(true);
        }
        if (style.mono()) {
            b.setFontFamily(MONO);
        } else if (fontFamily != null) {
            b.setFontFamily(fontFamily);
        }
        return b.build();
    }

    private static TextAlignment alignmentOf(TableCell tc) {
        TableCell.Alignment a = tc.getAlignment();
        if (a == null) {
            return TextAlignment.LEFT;
        }
        return switch (a) {
            case LEFT -> TextAlignment.LEFT;
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
        };
    }

    /** A deferred, size-capped inline image; falls back to an empty node if the URL is unusable. */
    private ImageView imageNode(String url) {
        try {
            if (url == null || url.isBlank()) {
                return new ImageView();
            }
            ImageView view = new ImageView(new javafx.scene.image.Image(url, true));
            view.setPreserveRatio(true);
            view.setFitWidth(base * 24);
            return view;
        } catch (RuntimeException e) {
            return new ImageView();
        }
    }

    /**
     * Dispatches a fenced code block to the {@link BlockRenderer} by its info string. If the
     * renderer returns pieces, each becomes one atomic inline-node paragraph (like a GFM table)
     * with its plain-text projection; otherwise the block falls through to preformatted rendering,
     * so an unrecognised or absent tag never regresses.
     */
    private void emitFencedBlock(FencedCodeBlock fc, List<RichParagraph> out, List<String> plain,
                                 StyleAttributeMap baseStyle) {
        List<BlockPiece> pieces = blockRenderer.render(fc.getInfo(), fc.getLiteral(), baseStyle);
        if (pieces.isEmpty()) {
            emitCodeBlock(fc.getLiteral(), out, plain);
            return;
        }
        for (BlockPiece piece : pieces) {
            out.add(RichParagraph.builder().addInlineNode(piece.node()).build());
            plain.add(piece.plainText());
        }
    }

    private void emitCodeBlock(String literal, List<RichParagraph> out, List<String> plain) {
        String code = literal == null ? "" : literal;
        while (code.endsWith("\n")) {
            code = code.substring(0, code.length() - 1);
        }
        StyleAttributeMap mono = StyleAttributeMap.builder().setFontFamily(MONO).setFontSize(base).build();
        for (String line : code.split("\n", -1)) {
            out.add(RichParagraph.builder().addSegment(line.isEmpty() ? " " : line, mono).build());
            plain.add(line);
        }
    }

    private double headingSize(int level) {
        return switch (level) {
            case 1 -> base + 6;
            case 2 -> base + 4;
            case 3 -> base + 2;
            default -> base + 1;
        };
    }

    // ---- Inline rendering --------------------------------------------------

    private void appendInlines(RichParagraph.Builder b, Node first, StyleAttributeMap baseStyle) {
        for (Node n = first; n != null; n = n.getNext()) {
            switch (n) {
                case Text t -> decorator.emit(b, t.getLiteral(), baseStyle);
                case StrongEmphasis s -> appendInlines(b, s.getFirstChild(),
                        baseStyle.combine(StyleAttributeMap.builder().setBold(true).build()));
                case Emphasis e -> appendInlines(b, e.getFirstChild(),
                        baseStyle.combine(StyleAttributeMap.builder().setItalic(true).build()));
                case Code c -> decorator.emit(b, c.getLiteral(),
                        baseStyle.combine(StyleAttributeMap.builder().setFontFamily(MONO).build()));
                case SoftLineBreak ignored -> decorator.emit(b, " ", baseStyle);
                case HardLineBreak ignored -> decorator.emit(b, " ", baseStyle);
                case Link l -> {
                    appendInlines(b, l.getFirstChild(), baseStyle);
                    if (l.getDestination() != null && !l.getDestination().isBlank()) {
                        decorator.emit(b, " (" + l.getDestination() + ")", baseStyle);
                    }
                }
                case Image img -> b.addInlineNode(() -> imageNode(img.getDestination()));
                default -> decorator.emit(b, plainOf(n), baseStyle);
            }
        }
    }

    private static void addSegment(RichParagraph.Builder b, String text, StyleAttributeMap style) {
        if (text.isEmpty()) {
            return;
        }
        if (style == null || style.isEmpty()) {
            b.addSegment(text);
        } else {
            b.addSegment(text, style);
        }
    }

    // ---- Plain-text projection (for copy / accessibility) ------------------

    private static String plainOf(Node node) {
        StringBuilder sb = new StringBuilder();
        appendPlain(node, sb);
        return sb.toString();
    }

    private static void appendPlain(Node node, StringBuilder sb) {
        for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
            switch (c) {
                case Text t -> sb.append(t.getLiteral());
                case Code code -> sb.append(code.getLiteral());
                case SoftLineBreak ignored -> sb.append(' ');
                case HardLineBreak ignored -> sb.append(' ');
                default -> appendPlain(c, sb);
            }
        }
    }

    private static void emit(RichParagraph.Builder b, String plain,
                             List<RichParagraph> out, List<String> plains) {
        out.add(b.build());
        plains.add(plain);
    }
}
