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

import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.commonmark.ext.gfm.tables.TableBlock;
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

import java.util.List;

/**
 * Renders CommonMark (with the GFM tables extension) into styled {@link RichParagraph} runs
 * for the incubator {@code RichTextArea}. Block and inline structure is generic; every
 * <em>content</em> text run is delegated to an {@link InlineDecorator}, so consumers can
 * inject inline nodes (e.g. concept chips) without this module depending on them. Structural
 * text — table separators, horizontal rules, fenced-code lines — is emitted directly.
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
    private final InlineDecorator decorator;

    /**
     * @param base      base body font size in px; headings and code scale from it
     * @param decorator the inline-run decorator; {@code null} uses {@link InlineDecorator#PLAIN}
     */
    public MarkdownRichTextRenderer(double base, InlineDecorator decorator) {
        this.base = base;
        this.decorator = decorator == null ? InlineDecorator.PLAIN : decorator;
    }

    /**
     * Parses {@code markdown} and appends its rendered paragraphs (and the parallel plain-text
     * projection) to {@code out}/{@code plain}, using {@code baseStyle} as the base run style.
     */
    public void render(String markdown, StyleAttributeMap baseStyle,
                       List<RichParagraph> out, List<String> plain) {
        Node document = PARSER.parse(markdown == null ? "" : markdown);
        for (Node block = document.getFirstChild(); block != null; block = block.getNext()) {
            renderBlock(block, out, plain, 0, baseStyle);
        }
    }

    /**
     * Renders non-Markdown text: one paragraph per line, each line still passed through the
     * decorator (so inline nodes are injected even outside Markdown).
     */
    public void renderPlainText(String text, StyleAttributeMap style,
                                List<RichParagraph> out, List<String> plain) {
        for (String line : (text == null ? "" : text).split("\n", -1)) {
            RichParagraph.Builder b = RichParagraph.builder();
            decorator.emit(b, line, style);
            out.add(b.build());
            plain.add(line);
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
            case FencedCodeBlock fc -> emitCodeBlock(fc.getLiteral(), out, plain);
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

    private void renderTable(TableBlock tb, List<RichParagraph> out, List<String> plain,
                             StyleAttributeMap baseStyle) {
        for (Node section = tb.getFirstChild(); section != null; section = section.getNext()) {
            boolean header = section instanceof TableHead;
            StyleAttributeMap rowStyle = header
                    ? baseStyle.combine(StyleAttributeMap.builder().setBold(true).build())
                    : baseStyle;
            for (Node rowNode = section.getFirstChild(); rowNode != null; rowNode = rowNode.getNext()) {
                if (!(rowNode instanceof TableRow row)) {
                    continue;
                }
                RichParagraph.Builder b = RichParagraph.builder();
                StringBuilder p = new StringBuilder();
                boolean firstCell = true;
                for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
                    if (!(cell instanceof TableCell)) {
                        continue;
                    }
                    if (!firstCell) {
                        b.addSegment("  |  ", baseStyle);
                        p.append("  |  ");
                    }
                    firstCell = false;
                    appendInlines(b, cell.getFirstChild(), rowStyle);
                    p.append(plainOf(cell));
                }
                emit(b, p.toString(), out, plain);
                if (header) {
                    emit(RichParagraph.builder().addSegment("─".repeat(10), baseStyle),
                            "─".repeat(10), out, plain);
                }
            }
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
                case Image img -> decorator.emit(b, "[image"
                        + (img.getDestination() == null || img.getDestination().isBlank()
                        ? "" : ": " + img.getDestination()) + "]", baseStyle);
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
