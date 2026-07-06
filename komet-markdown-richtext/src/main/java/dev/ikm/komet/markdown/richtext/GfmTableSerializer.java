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

import java.util.List;

/**
 * Serialises a {@link TableModel} to a GFM pipe table — the #596 interchange / clipboard format
 * ({@code text/plain}). GFM is the lowest common denominator: it renders natively in Zulip and the
 * daily digest, round-trips through {@link GfmTableParser}, and is what this module also uses as the
 * plain-text projection of a rendered table (so RichTextArea copy yields GFM).
 *
 * <p>Pipes inside cells are escaped ({@code \|}) and embedded newlines are folded to spaces, since
 * GFM tables are line-oriented.
 */
public final class GfmTableSerializer {

    private GfmTableSerializer() {
    }

    /** Serialises {@code model} to GFM pipe-table text (no trailing newline). */
    public static String serialize(TableModel model) {
        int cols = model.columnCount();
        if (cols == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (model.hasHeader()) {
            row(sb, model.headers(), cols);
            delimiter(sb, model.alignments(), cols);
        } else {
            // Headerless: GFM requires a header row, so emit a blank one to stay valid.
            row(sb, List.of(), cols);
            delimiter(sb, model.alignments(), cols);
        }
        for (List<String> r : model.rows()) {
            row(sb, r, cols);
        }
        // Drop the trailing newline.
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static void row(StringBuilder sb, List<String> cells, int cols) {
        sb.append('|');
        for (int i = 0; i < cols; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            sb.append(' ').append(escape(cell)).append(' ').append('|');
        }
        sb.append('\n');
    }

    private static void delimiter(StringBuilder sb, List<TableModel.Alignment> aligns, int cols) {
        sb.append('|');
        for (int i = 0; i < cols; i++) {
            TableModel.Alignment a = i < aligns.size() ? aligns.get(i) : TableModel.Alignment.NONE;
            sb.append(' ').append(switch (a) {
                case LEFT -> ":---";
                case CENTER -> ":--:";
                case RIGHT -> "---:";
                case NONE -> "---";
            }).append(' ').append('|');
        }
        sb.append('\n');
    }

    /** Escapes a cell for GFM: pipes become {@code \|}; newlines fold to spaces. */
    static String escape(String cell) {
        if (cell == null || cell.isEmpty()) {
            return "";
        }
        return cell.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r\n", " ")
                .replace('\n', ' ')
                .replace('\r', ' ');
    }
}
