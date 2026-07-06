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
 * Tab-separated import/export for {@link TableModel} — the spreadsheet-paste (and spreadsheet-copy)
 * lane of the #596 standard. On import the first row is treated as the header; on export tabs and
 * newlines inside cells are folded to spaces (TSV is line/tab oriented and has no escape).
 */
public final class TsvTable {

    private TsvTable() {
    }

    /** Serialises {@code model} to TSV (header first when present; no trailing newline). */
    public static String serialize(TableModel model) {
        int cols = model.columnCount();
        if (cols == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (model.hasHeader()) {
            line(sb, model.headers(), cols);
        }
        for (List<String> row : model.rows()) {
            line(sb, row, cols);
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /** Parses TSV (first row = header) into a model with no explicit alignment. */
    public static TableModel parse(String tsv) {
        List<List<String>> all = new ArrayList<>();
        if (tsv != null) {
            for (String raw : tsv.split("\n", -1)) {
                String s = raw.endsWith("\r") ? raw.substring(0, raw.length() - 1) : raw;
                if (s.isEmpty()) {
                    continue;
                }
                List<String> cells = new ArrayList<>();
                for (String cell : s.split("\t", -1)) {
                    cells.add(cell.strip());
                }
                all.add(cells);
            }
        }
        if (all.isEmpty()) {
            return new TableModel(List.of(), List.of(), List.of());
        }
        List<String> headers = all.get(0);
        List<List<String>> rows = new ArrayList<>(all.subList(1, all.size()));
        return new TableModel(headers, List.of(), rows);
    }

    private static void line(StringBuilder sb, List<String> cells, int cols) {
        for (int i = 0; i < cols; i++) {
            if (i > 0) {
                sb.append('\t');
            }
            String cell = i < cells.size() ? cells.get(i) : "";
            sb.append(cell.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
        }
        sb.append('\n');
    }
}
