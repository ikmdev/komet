/*
 * Copyright © 2026 Integrated Knowledge Management
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

/**
 * Remembers user-chosen table column widths across renderings
 * ({@code IKE-Network/ike-issues#1034}). The renderer identifies each table by a stable key
 * derived from its header row, asks for remembered widths when it builds the table, and reports
 * the full set of column widths when the user finishes a resize drag. Where the widths live is
 * the host's business — the transcript surface stores them in the owning conversation's
 * preferences node — so the renderer stays free of any persistence machinery.
 */
public interface TableColumnWidths {

    /**
     * Returns the remembered column widths for a table, or {@code null} when nothing usable is
     * remembered (never seen, stored under a different column count, or unreadable).
     *
     * @param tableKey    the renderer's stable identity for the table (derived from its header row
     *                    and column count)
     * @param columnCount the number of columns the table is being built with; an implementation
     *                    must return {@code null} rather than an array of any other length
     * @return one width in px per column, or {@code null} to lay the table out from content
     */
    double[] recall(String tableKey, int columnCount);

    /**
     * Remembers the column widths the user has chosen for a table, replacing whatever was
     * remembered before.
     *
     * @param tableKey the renderer's stable identity for the table
     * @param widths   one width in px per column, as laid out at the end of the resize drag
     */
    void remember(String tableKey, double[] widths);
}
