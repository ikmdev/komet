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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML {@code <table>} import/export for {@link TableModel} — the {@code text/html} clipboard flavor
 * of the #596 standard (rich paste into/from Word, email, browsers). Serialisation is exact;
 * parsing is a tolerant tag scan (no nested-table or rowspan/colspan support, matching the
 * deliberately flat #596 model).
 */
public final class HtmlTable {

    private static final Pattern TABLE = Pattern.compile("<table\\b[^>]*>(.*?)</table>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ROW = Pattern.compile("<tr\\b[^>]*>(.*?)</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CELL = Pattern.compile("<(t[hd])\\b([^>]*)>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ALIGN = Pattern.compile("text-align\\s*:\\s*(left|center|right)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    private HtmlTable() {
    }

    /** Serialises {@code model} to an HTML {@code <table>} (thead + tbody; alignment via style). */
    public static String serialize(TableModel model) {
        int cols = model.columnCount();
        if (cols == 0) {
            return "<table></table>";
        }
        StringBuilder sb = new StringBuilder("<table>");
        if (model.hasHeader()) {
            sb.append("<thead><tr>");
            for (int i = 0; i < cols; i++) {
                cellTag(sb, "th", get(model.headers(), i), align(model.alignments(), i));
            }
            sb.append("</tr></thead>");
        }
        sb.append("<tbody>");
        for (List<String> row : model.rows()) {
            sb.append("<tr>");
            for (int i = 0; i < cols; i++) {
                cellTag(sb, "td", get(row, i), align(model.alignments(), i));
            }
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    /** Parses the first {@code <table>} found in {@code html} into a model (best effort). */
    public static TableModel parse(String html) {
        if (html == null) {
            return new TableModel(List.of(), List.of(), List.of());
        }
        Matcher tm = TABLE.matcher(html);
        if (!tm.find()) {
            return new TableModel(List.of(), List.of(), List.of());
        }
        String body = tm.group(1);
        List<String> headers = new ArrayList<>();
        List<TableModel.Alignment> alignments = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        Matcher rm = ROW.matcher(body);
        boolean first = true;
        while (rm.find()) {
            String rowHtml = rm.group(1);
            List<String> cells = new ArrayList<>();
            List<TableModel.Alignment> rowAligns = new ArrayList<>();
            boolean rowIsHeader = false;
            Matcher cm = CELL.matcher(rowHtml);
            while (cm.find()) {
                if (cm.group(1).equalsIgnoreCase("th")) {
                    rowIsHeader = true;
                }
                cells.add(unescape(stripTags(cm.group(3))).strip());
                rowAligns.add(alignFromStyle(cm.group(2)));
            }
            if (cells.isEmpty()) {
                continue;
            }
            if (first && (rowIsHeader || headers.isEmpty())) {
                headers.addAll(cells);
                alignments = rowAligns;
                first = false;
            } else {
                if (alignments.isEmpty()) {
                    alignments = rowAligns;
                }
                rows.add(cells);
                first = false;
            }
        }
        return new TableModel(headers, alignments, rows);
    }

    /** Reads the {@code text-align} value from a cell tag's attributes, or NONE. */
    private static TableModel.Alignment alignFromStyle(String attrs) {
        if (attrs == null) {
            return TableModel.Alignment.NONE;
        }
        Matcher m = ALIGN.matcher(attrs);
        if (!m.find()) {
            return TableModel.Alignment.NONE;
        }
        return switch (m.group(1).toLowerCase(Locale.ROOT)) {
            case "left" -> TableModel.Alignment.LEFT;
            case "center" -> TableModel.Alignment.CENTER;
            case "right" -> TableModel.Alignment.RIGHT;
            default -> TableModel.Alignment.NONE;
        };
    }

    private static void cellTag(StringBuilder sb, String tag, String text, String alignStyle) {
        sb.append('<').append(tag);
        if (alignStyle != null) {
            sb.append(" style=\"text-align:").append(alignStyle).append('"');
        }
        sb.append('>').append(escape(text)).append("</").append(tag).append('>');
    }

    private static String align(List<TableModel.Alignment> aligns, int i) {
        TableModel.Alignment a = i < aligns.size() ? aligns.get(i) : TableModel.Alignment.NONE;
        return switch (a) {
            case CENTER -> "center";
            case RIGHT -> "right";
            case LEFT -> "left";
            case NONE -> null;
        };
    }

    private static String get(List<String> cells, int i) {
        return i < cells.size() ? cells.get(i) : "";
    }

    private static String stripTags(String s) {
        return TAG.matcher(s).replaceAll("");
    }

    static String escape(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String unescape(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }
}
