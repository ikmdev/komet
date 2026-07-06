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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip and structural tests for the #596 table-interchange stack — the engine-neutral
 * {@link TableModel} plus the GFM / AsciiDoc / TSV / HTML adapters. These are pure logic (no
 * JavaFX toolkit required).
 */
class TableInterchangeTest {

    private static TableModel sample() {
        return new TableModel(
                List.of("Property", "Value"),
                List.of(TableModel.Alignment.LEFT, TableModel.Alignment.RIGHT),
                List.of(
                        List.of("UUID", "f68ce1c1"),
                        List.of("Parent", "Microbiology")));
    }

    @Test
    void gfmRoundTripPreservesStructureAndAlignment() {
        TableModel original = sample();
        String gfm = GfmTableSerializer.serialize(original);
        assertTrue(gfm.contains(":---"), "left alignment delimiter expected");
        assertTrue(gfm.contains("---:"), "right alignment delimiter expected");

        TableModel parsed = GfmTableParser.parse(gfm);
        assertEquals(original.headers(), parsed.headers());
        assertEquals(original.alignments(), parsed.alignments());
        assertEquals(original.rows(), parsed.rows());
    }

    @Test
    void gfmEscapesAndUnescapesPipes() {
        TableModel model = new TableModel(
                List.of("a", "b"),
                List.of(),
                List.of(List.of("x | y", "z")));
        String gfm = GfmTableSerializer.serialize(model);
        assertTrue(gfm.contains("x \\| y"), "pipe inside a cell must be escaped");

        TableModel parsed = GfmTableParser.parse(gfm);
        assertEquals("x | y", parsed.rows().get(0).get(0), "escaped pipe must round-trip");
    }

    @Test
    void looksLikeTableDistinguishesGfmFromProse() {
        assertTrue(GfmTableParser.looksLikeTable("| a | b |\n| --- | --- |\n| 1 | 2 |"));
        assertFalse(GfmTableParser.looksLikeTable("just a paragraph\nwith two lines"));
        assertFalse(GfmTableParser.looksLikeTable("a\tb\nc\td"));
    }

    @Test
    void tsvRoundTrip() {
        TableModel model = sample();
        String tsv = TsvTable.serialize(model);
        assertTrue(tsv.contains("\t"), "TSV must be tab separated");
        TableModel parsed = TsvTable.parse(tsv);
        assertEquals(model.headers(), parsed.headers());
        assertEquals(model.rows(), parsed.rows());
    }

    @Test
    void htmlRoundTrip() {
        TableModel model = sample();
        String html = HtmlTable.serialize(model);
        assertTrue(html.contains("<thead>"), "header expected in HTML");
        assertTrue(html.contains("text-align:right"), "right alignment style expected");

        TableModel parsed = HtmlTable.parse(html);
        assertEquals(model.headers(), parsed.headers());
        assertEquals(model.rows(), parsed.rows());
        assertEquals(model.alignments(), parsed.alignments(), "alignment must round-trip");
    }

    @Test
    void htmlParsesAlignmentFromExternalMarkup() {
        // Alignment pasted from Word/browser arrives as a text-align style on the cells.
        String html = "<table><tr><th style=\"text-align:center\">H1</th>"
                + "<th style=\"text-align:right\">H2</th></tr>"
                + "<tr><td>a</td><td>b</td></tr></table>";
        TableModel parsed = HtmlTable.parse(html);
        assertEquals(
                List.of(TableModel.Alignment.CENTER, TableModel.Alignment.RIGHT),
                parsed.alignments());
    }

    @Test
    void tsvSingleColumnRoundTrip() {
        TableModel model = new TableModel(
                List.of("Name"),
                List.of(),
                List.of(List.of("Alice"), List.of("Bob")));
        String tsv = TsvTable.serialize(model);
        assertFalse(tsv.contains("\t"), "single-column TSV has no tabs");
        TableModel parsed = TsvTable.parse(tsv);
        assertEquals(List.of("Name"), parsed.headers());
        assertEquals(List.of(List.of("Alice"), List.of("Bob")), parsed.rows());
    }

    @Test
    void htmlEscapesMarkup() {
        TableModel model = new TableModel(
                List.of("h"),
                List.of(),
                List.of(List.of("<b> & </b>")));
        String html = HtmlTable.serialize(model);
        assertTrue(html.contains("&lt;b&gt; &amp; &lt;/b&gt;"), "cell markup must be escaped");
        assertEquals("<b> & </b>", HtmlTable.parse(html).rows().get(0).get(0));
    }

    @Test
    void asciiDocUsesHeaderAndAlignmentSpec() {
        String adoc = AsciiDocTableSerializer.serialize(sample());
        assertTrue(adoc.contains("%header"), "header option expected");
        assertTrue(adoc.contains("cols=\"<,>\""), "left/right cols spec expected");
        assertTrue(adoc.contains("|==="), "delimited block expected");
        assertTrue(adoc.contains("|Property"), "header cell expected");
    }

    @Test
    void fromCommonMarkExtractsHeadersAlignmentRowsAndConceptCodes() {
        String md = "| Property | Code |\n"
                + "| :--- | ---: |\n"
                + "| UUID | f68ce1c1-6a45-5908-9010-55e4837d41c8 |\n";
        org.commonmark.node.Node doc = org.commonmark.parser.Parser.builder()
                .extensions(List.of(org.commonmark.ext.gfm.tables.TablesExtension.create()))
                .build()
                .parse(md);
        org.commonmark.ext.gfm.tables.TableBlock tb =
                (org.commonmark.ext.gfm.tables.TableBlock) doc.getFirstChild();

        TableModel model = TableModel.fromCommonMark(tb);
        assertEquals(List.of("Property", "Code"), model.headers());
        assertEquals(List.of(TableModel.Alignment.LEFT, TableModel.Alignment.RIGHT), model.alignments());
        assertEquals(1, model.rows().size());
        // The concept code survives verbatim as plain cell text (so chips re-derive on render and it
        // round-trips through GFM/Zulip/adoc).
        assertEquals("f68ce1c1-6a45-5908-9010-55e4837d41c8", model.rows().get(0).get(1));
    }
}
