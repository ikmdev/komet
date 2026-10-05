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
package dev.ikm.komet.framework;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * No hash collection in Komet's main code is keyed by a public id. Public ids are equal when
 * they share any UUID, and {@code PublicId.hashCode} is, by design, not consistent with that: equal
 * public ids may hash apart. Within a store, key by nid; a sorted collection ({@code TreeSet},
 * {@code TreeMap}) is fine when "the same UUIDs" is the question. See {@code PublicId}'s javadoc.
 * <p>
 * The check reads the source: a hash collection's key type is erased from a method's bytecode.
 * A deliberate exception is marked on its line: {@code // public-id-hash-key: <reason>}.
 */
class PublicIdHashKeyGuardTest {
    // The same check as tinkar-core's (dev.ikm.tinkar.integration.integrity), over this repository.


    /** A hashed set, map or cache whose element or key type is a public id. */
    static final Pattern HASHED_BY_PUBLIC_ID = Pattern.compile(
            "\\b(Set|HashSet|LinkedHashSet|Map|HashMap|LinkedHashMap|ConcurrentMap|ConcurrentHashMap"
                    + "|MutableSet|ImmutableSet|MutableMap|ImmutableMap|UnifiedSet|UnifiedMap"
                    + "|Cache|LoadingCache|AsyncCache)\\s*<\\s*(\\?\\s+extends\\s+)?PublicId\\b");
    static final String EXCEPTION_MARK = "public-id-hash-key:";

    @Test
    void noHashCollectionIsKeyedByAPublicId() throws IOException {
        Path repository = repositoryRoot();
        List<String> found = new ArrayList<>();
        int scanned = 0;
        try (Stream<Path> files = Files.walk(repository)) {
            for (Path file : files.filter(PublicIdHashKeyGuardTest::isMainSource).toList()) {
                scanned++;
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String trimmed = line.strip();
                    if (trimmed.startsWith("*") || trimmed.startsWith("//") || line.contains(EXCEPTION_MARK)) {
                        continue;
                    }
                    if (HASHED_BY_PUBLIC_ID.matcher(line).find()) {
                        found.add(repository.relativize(file) + ":" + (i + 1) + ": " + trimmed);
                    }
                }
            }
        }
        assertTrue(scanned > 100, "the main sources were found under " + repository);
        assertEquals(List.of(), found, "hash collections keyed by a public id; key by nid, or mark a"
                + " deliberate exception with // " + EXCEPTION_MARK + " <reason>");
    }

    static boolean isMainSource(Path file) {
        String path = file.toString().replace('\\', '/');
        return path.endsWith(".java") && path.contains("/src/main/java/") && !path.contains("/target/");
    }

    /** The repository root: the nearest ancestor of the working directory holding a {@code .git}. */
    static Path repositoryRoot() {
        Path directory = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (directory != null && !Files.exists(directory.resolve(".git"))) {
            directory = directory.getParent();
        }
        if (directory == null) {
            throw new UncheckedIOException(new IOException("No repository root above " + System.getProperty("user.dir")));
        }
        return directory;
    }
}
