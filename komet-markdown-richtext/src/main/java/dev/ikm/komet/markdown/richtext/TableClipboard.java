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

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Optional;

/**
 * Clipboard integration for the #596 table-interchange standard. <b>Copy</b> places two flavors:
 * {@code text/plain} = GFM (markdown apps, Zulip, our renderer) and {@code text/html} = an HTML
 * {@code <table>} (rich paste into Word/email/browser). <b>Paste</b> prefers {@code text/plain},
 * parsing it as GFM when it looks like a table and otherwise as TSV (spreadsheet paste), and falls
 * back to the {@code text/html} table. Lossless within our stack; graceful degradation elsewhere.
 *
 * <p>Must be called on the JavaFX application thread.
 */
public final class TableClipboard {

    private TableClipboard() {
    }

    /** Copies {@code model} to the system clipboard as both GFM ({@code text/plain}) and HTML. */
    public static void copy(TableModel model) {
        ClipboardContent content = new ClipboardContent();
        content.putString(GfmTableSerializer.serialize(model));
        content.putHtml(HtmlTable.serialize(model));
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Reads a table from the system clipboard, preferring {@code text/plain} (GFM, else TSV) and
     * falling back to {@code text/html}.
     *
     * @return the parsed model, or empty when the clipboard holds no recognisable table
     */
    public static Optional<TableModel> paste() {
        Clipboard cb = Clipboard.getSystemClipboard();
        if (cb.hasString()) {
            String s = cb.getString();
            if (GfmTableParser.looksLikeTable(s)) {
                return nonEmpty(GfmTableParser.parse(s));
            }
            // TSV (spreadsheet paste). A tab marks multi-column data; a bare newline marks a
            // single-column copy (which has no tabs) — both are valid TSV.
            if (s != null && (s.indexOf('\t') >= 0 || s.indexOf('\n') >= 0)) {
                Optional<TableModel> tsv = nonEmpty(TsvTable.parse(s));
                if (tsv.isPresent()) {
                    return tsv;
                }
            }
        }
        if (cb.hasHtml()) {
            return nonEmpty(HtmlTable.parse(cb.getHtml()));
        }
        return Optional.empty();
    }

    private static Optional<TableModel> nonEmpty(TableModel model) {
        return model.columnCount() == 0 ? Optional.empty() : Optional.of(model);
    }
}
