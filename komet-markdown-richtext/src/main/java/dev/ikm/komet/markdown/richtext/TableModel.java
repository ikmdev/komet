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

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.Code;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The canonical, engine-neutral table representation for the #596 table-interchange standard:
 * an optional header row, a per-column alignment, and zero or more body rows. Each cell holds its
 * <em>inline source text</em> (plain text, with any concept references carried verbatim as plain
 * codes) — the lowest common denominator that round-trips losslessly across GFM, AsciiDoc, TSV,
 * and HTML.
 *
 * <p>Everything parses <em>to</em> a {@code TableModel} and serialises <em>from</em> one
 * (see {@link GfmTableSerializer}, {@link AsciiDocTableSerializer}, {@link TsvTable},
 * {@link HtmlTable}). Rendering for display walks the CommonMark AST directly so it can style
 * inline runs and inject concept chips; the model is the interchange currency, and supplies the
 * GFM plain-text projection used on copy. Column/row spanning and nested blocks are deliberately
 * out of scope (GFM/Zulip interop limit).
 *
 * <p>Instances are immutable; defensive copies are taken on construction.
 */
public final class TableModel {

    /** Per-column horizontal alignment, mirroring the GFM delimiter row. */
    public enum Alignment {
        /** No explicit alignment ({@code ---}). */
        NONE,
        /** Left ({@code :---}). */
        LEFT,
        /** Centre ({@code :--:}). */
        CENTER,
        /** Right ({@code ---:}). */
        RIGHT
    }

    private final List<String> headers;
    private final List<Alignment> alignments;
    private final List<List<String>> rows;

    /**
     * @param headers    header cell texts (empty list = headerless table); a copy is taken
     * @param alignments per-column alignment; padded/truncated to the column count
     * @param rows       body rows of cell texts; a deep copy is taken
     */
    public TableModel(List<String> headers, List<Alignment> alignments, List<List<String>> rows) {
        this.headers = List.copyOf(headers == null ? List.of() : headers);
        List<List<String>> copied = new ArrayList<>();
        if (rows != null) {
            for (List<String> r : rows) {
                copied.add(List.copyOf(r == null ? List.of() : r));
            }
        }
        this.rows = List.copyOf(copied);

        int cols = columnCount(this.headers, this.rows);
        List<Alignment> a = new ArrayList<>();
        for (int i = 0; i < cols; i++) {
            a.add(alignments != null && i < alignments.size() && alignments.get(i) != null
                    ? alignments.get(i) : Alignment.NONE);
        }
        this.alignments = List.copyOf(a);
    }

    private static int columnCount(List<String> headers, List<List<String>> rows) {
        int cols = headers.size();
        for (List<String> r : rows) {
            cols = Math.max(cols, r.size());
        }
        return cols;
    }

    /** Header cell texts (empty when the table has no header). */
    public List<String> headers() {
        return headers;
    }

    /** True when the table has a header row. */
    public boolean hasHeader() {
        return !headers.isEmpty();
    }

    /** Per-column alignment, exactly {@link #columnCount()} entries. */
    public List<Alignment> alignments() {
        return alignments;
    }

    /** Body rows of cell texts. */
    public List<List<String>> rows() {
        return rows;
    }

    /** The number of columns (max across header and body rows). */
    public int columnCount() {
        return alignments.size();
    }

    // ---- CommonMark AST → model -------------------------------------------

    /**
     * Builds a model from a parsed GFM {@link TableBlock}. Cell text is the plain concatenation of
     * the cell's inline literals (concept codes survive verbatim); alignment comes from the GFM
     * delimiter row.
     *
     * @param tb the parsed table block (never null)
     * @return the canonical model
     */
    public static TableModel fromCommonMark(TableBlock tb) {
        List<String> headers = new ArrayList<>();
        List<Alignment> alignments = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        for (Node section = tb.getFirstChild(); section != null; section = section.getNext()) {
            if (section instanceof TableHead head) {
                TableRow hr = firstRow(head);
                if (hr != null) {
                    for (Node cell = hr.getFirstChild(); cell != null; cell = cell.getNext()) {
                        if (cell instanceof TableCell tc) {
                            headers.add(cellText(tc));
                            alignments.add(map(tc.getAlignment()));
                        }
                    }
                }
            } else if (section instanceof TableBody body) {
                for (Node rowNode = body.getFirstChild(); rowNode != null; rowNode = rowNode.getNext()) {
                    if (rowNode instanceof TableRow tr) {
                        List<String> row = new ArrayList<>();
                        for (Node cell = tr.getFirstChild(); cell != null; cell = cell.getNext()) {
                            if (cell instanceof TableCell tc) {
                                row.add(cellText(tc));
                            }
                        }
                        rows.add(row);
                    }
                }
            }
        }
        return new TableModel(headers, alignments, rows);
    }

    private static TableRow firstRow(Node section) {
        for (Node n = section.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof TableRow tr) {
                return tr;
            }
        }
        return null;
    }

    private static Alignment map(TableCell.Alignment a) {
        if (a == null) {
            return Alignment.NONE;
        }
        return switch (a) {
            case LEFT -> Alignment.LEFT;
            case CENTER -> Alignment.CENTER;
            case RIGHT -> Alignment.RIGHT;
        };
    }

    /** Plain-text of a cell's inline content (Text/Code literals; line breaks → space). */
    static String cellText(Node cell) {
        StringBuilder sb = new StringBuilder();
        appendText(cell, sb);
        return sb.toString().strip();
    }

    private static void appendText(Node node, StringBuilder sb) {
        for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
            switch (c) {
                case Text t -> sb.append(t.getLiteral());
                case Code code -> sb.append(code.getLiteral());
                case SoftLineBreak ignored -> sb.append(' ');
                case HardLineBreak ignored -> sb.append(' ');
                default -> appendText(c, sb);
            }
        }
    }
}
