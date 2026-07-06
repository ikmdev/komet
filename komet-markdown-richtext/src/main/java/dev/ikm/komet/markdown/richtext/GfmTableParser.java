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

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a GFM pipe table (header row, delimiter/alignment row, body rows) into a
 * {@link TableModel}. The inverse of {@link GfmTableSerializer}; together they give the lossless
 * structural round-trip the #596 interchange standard requires. Leading/trailing pipes are
 * optional and {@code \|} escapes are honoured.
 */
public final class GfmTableParser {

    private GfmTableParser() {
    }

    /**
     * True if {@code text} contains a GFM table delimiter line (a row of {@code ---}/{@code :--:}
     * cells) — a cheap test for choosing the GFM parser over TSV on paste.
     */
    public static boolean looksLikeTable(String text) {
        if (text == null) {
            return false;
        }
        for (String line : text.split("\n", -1)) {
            if (isDelimiterRow(splitRow(line))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses {@code gfm} into a model. Returns an empty model when the text is not a recognisable
     * GFM table (no delimiter row).
     */
    public static TableModel parse(String gfm) {
        List<String> lines = new ArrayList<>();
        if (gfm != null) {
            for (String raw : gfm.split("\n", -1)) {
                String s = raw.strip();
                if (!s.isEmpty()) {
                    lines.add(s);
                }
            }
        }
        // Find the delimiter row; the line above it is the header.
        int delim = -1;
        for (int i = 1; i < lines.size(); i++) {
            if (isDelimiterRow(splitRow(lines.get(i)))) {
                delim = i;
                break;
            }
        }
        if (delim < 1) {
            return new TableModel(List.of(), List.of(), List.of());
        }
        List<String> headers = splitRow(lines.get(delim - 1));
        List<TableModel.Alignment> alignments = parseAlignments(splitRow(lines.get(delim)));
        List<List<String>> rows = new ArrayList<>();
        for (int i = delim + 1; i < lines.size(); i++) {
            rows.add(splitRow(lines.get(i)));
        }
        return new TableModel(headers, alignments, rows);
    }

    /** Split one pipe-table line into trimmed, unescaped cells (outer pipes optional). */
    static List<String> splitRow(String line) {
        List<String> cells = new ArrayList<>();
        if (line == null) {
            return cells;
        }
        String s = line.strip();
        if (s.isEmpty()) {
            return cells;
        }
        StringBuilder cur = new StringBuilder();
        boolean any = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == '|' || next == '\\') {
                    cur.append(next);
                    i++;
                    continue;
                }
                cur.append(c);
            } else if (c == '|') {
                cells.add(cur.toString().strip());
                cur.setLength(0);
                any = true;
            } else {
                cur.append(c);
            }
        }
        cells.add(cur.toString().strip());
        // Drop the empty leading/trailing cells produced by outer pipes.
        if (any) {
            if (!cells.isEmpty() && cells.get(0).isEmpty()) {
                cells.remove(0);
            }
            if (!cells.isEmpty() && cells.get(cells.size() - 1).isEmpty()) {
                cells.remove(cells.size() - 1);
            }
        }
        return cells;
    }

    private static boolean isDelimiterRow(List<String> cells) {
        if (cells.isEmpty()) {
            return false;
        }
        for (String cell : cells) {
            if (!cell.matches(":?-+:?")) {
                return false;
            }
        }
        return true;
    }

    private static List<TableModel.Alignment> parseAlignments(List<String> cells) {
        List<TableModel.Alignment> aligns = new ArrayList<>();
        for (String cell : cells) {
            boolean left = cell.startsWith(":");
            boolean right = cell.endsWith(":");
            aligns.add(left && right ? TableModel.Alignment.CENTER
                    : right ? TableModel.Alignment.RIGHT
                    : left ? TableModel.Alignment.LEFT
                    : TableModel.Alignment.NONE);
        }
        return aligns;
    }
}
