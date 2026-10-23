/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.layout.help;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A help book kept as AsciiDoc resources in the providing plugin's jar: one {@code <topic>.adoc}
 * per topic beside a {@code nav.adoc} that lists them, under a root directory. The contents file
 * follows the Antora navigation convention, one cross reference per list item, nested by the
 * number of markers:
 *
 * <pre>
 * * xref:opening-a-document.adoc[Opening a document]
 * ** xref:kinds-of-data.adoc[]
 * </pre>
 *
 * An item with empty link text takes its topic's document title.
 *
 * <p>The resources are read through the subclass's module. A resource in a package of a named
 * module is encapsulated, so the root is a directory that is not a package, such as one under
 * {@code META-INF}; {@link #defaultRoot(String)} names the conventional one. A subclass that
 * keeps its help elsewhere overrides {@link #open(String)}.
 */
public abstract class KlResourceHelpProvider implements KlHelpProvider {

    /** The contents file of a book, in the book's root. */
    public static final String CONTENTS = "nav.adoc";

    private static final Pattern NAV_ITEM = Pattern.compile("^(\\*+)\\s+xref:([^\\[\\s]+?)(?:\\.adoc)?\\[(.*)]\\s*$");
    private static final Pattern DOCUMENT_TITLE = Pattern.compile("^=\\s+(\\S.*?)\\s*$");

    private final String bookId;
    private final String title;
    private final String root;

    /**
     * Creates a book kept under the conventional root.
     *
     * @param bookId the book's identifier
     * @param title  the book's title
     */
    protected KlResourceHelpProvider(String bookId, String title) {
        this(bookId, title, defaultRoot(bookId));
    }

    /**
     * Creates a book kept under the given root.
     *
     * @param bookId the book's identifier
     * @param title  the book's title
     * @param root   the resource directory holding the book, ending in {@code /}
     */
    protected KlResourceHelpProvider(String bookId, String title, String root) {
        this.bookId = bookId;
        this.title = title;
        this.root = root.endsWith("/") ? root : root + "/";
    }

    /**
     * The conventional root of a book: {@code META-INF/komet-help/<bookId>/}.
     *
     * @param bookId the book's identifier
     * @return its root
     */
    public static String defaultRoot(String bookId) {
        return "META-INF/komet-help/" + bookId + "/";
    }

    @Override
    public final String bookId() {
        return bookId;
    }

    @Override
    public final String title() {
        return title;
    }

    @Override
    public List<Entry> contents() {
        List<Entry> entries = new ArrayList<>();
        Optional<String> nav = read(CONTENTS);
        if (nav.isEmpty()) {
            return entries;
        }
        for (String line : nav.get().split("\\R")) {
            Matcher item = NAV_ITEM.matcher(line.strip());
            if (item.matches()) {
                String topicId = item.group(2);
                String text = item.group(3).strip();
                if (text.isEmpty()) {
                    text = source(topicId).flatMap(KlResourceHelpProvider::documentTitle).orElse(topicId);
                }
                entries.add(new Entry(topicId, text, item.group(1).length()));
            }
        }
        return entries;
    }

    @Override
    public Optional<String> source(String topicId) {
        return read(topicId + ".adoc");
    }

    /**
     * The title line of an AsciiDoc document.
     *
     * @param source the document
     * @return the text of its {@code = Title} line, or empty when it has none
     */
    public static Optional<String> documentTitle(String source) {
        for (String line : source.split("\\R")) {
            Matcher title = DOCUMENT_TITLE.matcher(line);
            if (title.matches()) {
                return Optional.of(title.group(1));
            }
        }
        return Optional.empty();
    }

    /**
     * Opens a resource of the book's module.
     *
     * @param path the resource's path from the module's root
     * @return its stream, or {@code null} when there is no such resource
     * @throws IOException when it cannot be opened
     */
    protected InputStream open(String path) throws IOException {
        return getClass().getModule().getResourceAsStream(path);
    }

    private Optional<String> read(String name) {
        try (InputStream in = open(root + name)) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read help resource " + root + name, e);
        }
    }
}
