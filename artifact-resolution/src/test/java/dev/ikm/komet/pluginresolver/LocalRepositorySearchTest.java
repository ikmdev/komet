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
package dev.ikm.komet.pluginresolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalRepositorySearchTest {

    @Test
    void groupIdColonQueryScopesToExactGroupAndMatchesArtifactSubstring(@TempDir Path repoRoot) throws IOException {
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "snomedct-international", "1.0.0");
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "loinc", "1.0.0");
        writeArtifact(repoRoot, "dev/ikm/komet", "komet-claude-plugin", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "dev.ikm.tinkar.data:snomed", List.of());

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international")), results);
    }

    @Test
    void blankArtifactPartOfColonQueryMatchesEntireGroup(@TempDir Path repoRoot) throws IOException {
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "snomedct-international", "1.0.0");
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "loinc", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "dev.ikm.tinkar.data:", List.of());

        assertEquals(Set.of(
                new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international"),
                new ArtifactCoordinates("dev.ikm.tinkar.data", "loinc")
        ), results);
    }

    @Test
    void freeTextQueryWalksWholeRepositoryAndMatchesGroupOrArtifact(@TempDir Path repoRoot) throws IOException {
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "snomedct-international", "1.0.0");
        writeArtifact(repoRoot, "dev/ikm/komet", "komet-claude-plugin", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "snomed", List.of());

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-international")), results);
    }

    @Test
    void nonArtifactDirectoriesAreNotMistakenForArtifacts(@TempDir Path repoRoot) throws IOException {
        // A groupId path segment directory (e.g. "tinkar" in dev/ikm/tinkar/data) has no
        // <name>-<version>.pom/.jar of its own and must not be reported as an artifact.
        writeArtifact(repoRoot, "dev/ikm/tinkar/data", "snomedct-international", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "tinkar", List.of());

        assertTrue(results.stream().noneMatch(c -> c.artifactId().equals("tinkar") || c.artifactId().equals("data")));
    }

    @Test
    void findsAClassifierOnlyArtifactDownloadedWithNoAccompanyingPom(@TempDir Path repoRoot) throws IOException {
        Path versionDir = repoRoot.resolve("dev/ikm/tinkar/data/loinc/1.0.0-SNAPSHOT");
        Files.createDirectories(versionDir);
        Files.writeString(versionDir.resolve("loinc-1.0.0-20260714.135548-4-reasoned-sa.zip"), "zip-bytes");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "loinc", List.of());

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "loinc")), results);
    }

    @Test
    void findsAnArtifactWhoseSnapshotBaseVersionIsNotAPlainDottedDecimal(@TempDir Path repoRoot) throws IOException {
        // Real shape confirmed against a live artifact, 2026-07-22: dev.ikm.tinkar.data:rxnorm's
        // base version is "2024-04-10+1.0.0" — a search for "rxnorm" with SA classifier
        // candidates must still find it, not silently miss it the way an over-narrow snapshot
        // filename pattern (assuming a plain \d+(\.\d+)* base version) previously did.
        Path versionDir = repoRoot.resolve("dev/ikm/tinkar/data/rxnorm/2024-04-10+1.0.0-SNAPSHOT");
        Files.createDirectories(versionDir);
        Files.writeString(versionDir.resolve("rxnorm-2024-04-10+1.0.0-20260714.140246-2-reasoned-sa.zip"), "zip-bytes");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "rxnorm",
                List.of("reasoned-sa", "unreasoned-sa", "spined-array", "sa"));

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "rxnorm")), results);
    }

    @Test
    void classifierCandidatesExcludeArtifactsWithNoCompatibleClassifier(@TempDir Path repoRoot) throws IOException {
        writeArtifactWithClassifier(repoRoot, "dev/ikm/tinkar/data", "rxnorm", "1.0.0", "reasoned-sa");
        // rxnorm-integration only publishes a plain (unclassified) jar/pom — no SA variant at all.
        writeArtifact(repoRoot, "dev/ikm/maven", "rxnorm-integration", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "rxnorm", List.of("reasoned-sa", "unreasoned-sa"));

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "rxnorm")), results);
    }

    @Test
    void classifierCandidatesFallBackToARarerCompatibleClassifier(@TempDir Path repoRoot) throws IOException {
        writeArtifactWithClassifier(repoRoot, "dev/ikm/tinkar/data", "snomedct-starter-data", "1.0.0", "spined-array");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "snomedct",
                List.of("reasoned-sa", "unreasoned-sa", "spined-array", "sa"));

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.tinkar.data", "snomedct-starter-data")), results);
    }

    @Test
    void emptyClassifierCandidatesListSkipsTheClassifierFilter(@TempDir Path repoRoot) throws IOException {
        writeArtifact(repoRoot, "dev/ikm/maven", "rxnorm-integration", "1.0.0");

        Set<ArtifactCoordinates> results = LocalRepositorySearch.search(repoRoot, "rxnorm", List.of());

        assertEquals(Set.of(new ArtifactCoordinates("dev.ikm.maven", "rxnorm-integration")), results);
    }

    @Test
    void returnsEmptyForBlankQuery(@TempDir Path repoRoot) {
        assertTrue(LocalRepositorySearch.search(repoRoot, "  ", List.of()).isEmpty());
    }

    @Test
    void returnsEmptyWhenRepositoryRootDoesNotExist(@TempDir Path repoRoot) {
        assertTrue(LocalRepositorySearch.search(repoRoot.resolve("does-not-exist"), "anything", List.of()).isEmpty());
    }

    private static void writeArtifact(Path repoRoot, String groupPath, String artifactId, String version) throws IOException {
        Path versionDir = repoRoot.resolve(groupPath).resolve(artifactId).resolve(version);
        Files.createDirectories(versionDir);
        Files.writeString(versionDir.resolve(artifactId + "-" + version + ".pom"), "<project/>");
    }

    private static void writeArtifactWithClassifier(Path repoRoot, String groupPath, String artifactId, String version,
                                                      String classifier) throws IOException {
        Path versionDir = repoRoot.resolve(groupPath).resolve(artifactId).resolve(version);
        Files.createDirectories(versionDir);
        Files.writeString(versionDir.resolve(artifactId + "-" + version + "-" + classifier + ".zip"), "zip-bytes");
    }
}
