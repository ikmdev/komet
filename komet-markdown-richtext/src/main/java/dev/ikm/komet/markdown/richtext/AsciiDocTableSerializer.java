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
 * Serialises a {@link TableModel} to an AsciiDoc {@code |===} table — the docs/adoc target of the
 * #596 standard. Per-column alignment maps to the {@code cols=} specifier ({@code <} left,
 * {@code ^} centre, {@code >} right); the header (when present) uses the {@code %header} option.
 * GFM → AsciiDoc is the clean mechanical transform #596 calls for.
 */
public final class AsciiDocTableSerializer {

    private AsciiDocTableSerializer() {
    }

    /** Serialises {@code model} to an AsciiDoc table block (no trailing newline). */
    public static String serialize(TableModel model) {
        int cols = model.columnCount();
        if (cols == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (model.hasHeader()) {
            sb.append("%header,");
        }
        sb.append("cols=\"").append(colsSpec(model.alignments())).append("\"]\n");
        sb.append("|===\n");
        if (model.hasHeader()) {
            sb.append(rowLine(model.headers(), cols)).append('\n');
            sb.append('\n');
        }
        for (List<String> row : model.rows()) {
            sb.append(rowLine(row, cols)).append('\n');
        }
        sb.append("|===");
        return sb.toString();
    }

    private static String colsSpec(List<TableModel.Alignment> aligns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aligns.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(switch (aligns.get(i)) {
                case CENTER -> "^";
                case RIGHT -> ">";
                case LEFT, NONE -> "<";
            });
        }
        return sb.toString();
    }

    private static String rowLine(List<String> cells, int cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            sb.append('|').append(escape(cell));
            if (i < cols - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /** Escapes a cell for an AsciiDoc table: pipes become {@code \|}; newlines fold to spaces. */
    static String escape(String cell) {
        if (cell == null || cell.isEmpty()) {
            return "";
        }
        return cell.replace("|", "\\|")
                .replace("\r\n", " ")
                .replace('\n', ' ')
                .replace('\r', ' ');
    }
}
