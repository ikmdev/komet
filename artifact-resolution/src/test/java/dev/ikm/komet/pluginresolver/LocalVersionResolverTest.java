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

class LocalVersionResolverTest {

    private static final ArtifactCoordinates COORDINATES = new ArtifactCoordinates("network.ike.komet", "example-plugin");

    @Test
    void listsVersionsWithPublishedPomOrJar(@TempDir Path repoRoot) throws IOException {
        Path artifactDir = repoRoot.resolve("network/ike/komet/example-plugin");

        writeFile(artifactDir.resolve("1.0.0/example-plugin-1.0.0.jar"), "jar-bytes");
        writeFile(artifactDir.resolve("1.1.0/example-plugin-1.1.0.pom"), "<project/>");
        // A version directory with no matching pom/jar (e.g. a stray/incomplete download) is not a published version.
        Files.createDirectories(artifactDir.resolve("1.2.0-incomplete"));

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        List<String> versions = resolver.localVersions(COORDINATES);

        assertEquals(List.of("1.0.0", "1.1.0"), versions);
    }

    @Test
    void returnsEmptyWhenArtifactDirectoryDoesNotExist(@TempDir Path repoRoot) {
        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertTrue(resolver.localVersions(COORDINATES).isEmpty());
    }

    @Test
    void artifactDirectoryFollowsMaven2Layout(@TempDir Path repoRoot) {
        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        Path expected = repoRoot.resolve("network/ike/komet/example-plugin");
        assertEquals(expected, resolver.artifactDirectory(COORDINATES));
    }

    @Test
    void availableClassifiersListsClassifiedZipsForOneVersion(@TempDir Path repoRoot) throws IOException {
        Path versionDir = repoRoot.resolve("network/ike/komet/example-plugin/1.0.0");
        writeFile(versionDir.resolve("example-plugin-1.0.0-reasoned-sa.zip"), "zip-bytes");
        writeFile(versionDir.resolve("example-plugin-1.0.0-unreasoned-sa.zip"), "zip-bytes");
        writeFile(versionDir.resolve("example-plugin-1.0.0.pom"), "<project/>");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertEquals(Set.of("reasoned-sa", "unreasoned-sa"), resolver.availableClassifiers(COORDINATES, "1.0.0"));
    }

    @Test
    void availableClassifiersIsEmptyWhenVersionDirectoryDoesNotExist(@TempDir Path repoRoot) {
        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertTrue(resolver.availableClassifiers(COORDINATES, "9.9.9").isEmpty());
    }

    @Test
    void localVersionsRecognizesAClassifierOnlyVersionWithNoPomOrJar(@TempDir Path repoRoot) throws IOException {
        // MavenDataStoreDownloadTask caches only the classified zip, never a .pom — a version
        // downloaded that way must still be recognized as present.
        writeFile(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0/example-plugin-1.0.0-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertEquals(List.of("1.0.0"), resolver.localVersions(COORDINATES));
    }

    @Test
    void localVersionsRecognizesASnapshotClassifierOnlyVersionWithNoPomOrJar(@TempDir Path repoRoot) throws IOException {
        writeFile(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0-SNAPSHOT/example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertEquals(List.of("1.0.0-SNAPSHOT"), resolver.localVersions(COORDINATES));
    }

    @Test
    void availableClassifiersHandlesSnapshotFileNamingShape(@TempDir Path repoRoot) throws IOException {
        // The version directory keeps the -SNAPSHOT name, but real files inside it are named
        // with the resolved timestamp — confirmed against a live Nexus snapshot repository.
        Path versionDir = repoRoot.resolve("network/ike/komet/example-plugin/1.0.0-SNAPSHOT");
        writeFile(versionDir.resolve("example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip"), "zip-bytes");
        writeFile(versionDir.resolve("example-plugin-1.0.0-20260714.135548-4-unreasoned-sa.zip"), "zip-bytes");
        writeFile(versionDir.resolve("example-plugin-1.0.0-20260714.135548-4.pom"), "<project/>");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertEquals(Set.of("reasoned-sa", "unreasoned-sa"), resolver.availableClassifiers(COORDINATES, "1.0.0-SNAPSHOT"));
    }

    @Test
    void resolveSnapshotFileVersionFindsTheResolvedTimestampedVersion(@TempDir Path repoRoot) throws IOException {
        Path versionDir = repoRoot.resolve("network/ike/komet/example-plugin/1.0.0-SNAPSHOT");
        writeFile(versionDir.resolve("example-plugin-1.0.0-20260714.135548-4-reasoned-pb.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertEquals(java.util.Optional.of("1.0.0-20260714.135548-4"),
                resolver.resolveSnapshotFileVersion(COORDINATES, "1.0.0-SNAPSHOT"));
    }

    @Test
    void resolveSnapshotFileVersionIsEmptyWhenDirectoryHasNoMatchingFiles(@TempDir Path repoRoot) {
        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertTrue(resolver.resolveSnapshotFileVersion(COORDINATES, "1.0.0-SNAPSHOT").isEmpty());
    }

    @Test
    void handlesRealBaseVersionsThatAreNotPlainDottedDecimals(@TempDir Path repoRoot) throws IOException {
        // Real shapes confirmed against live artifacts, 2026-07-22: dev.ikm.tinkar.data:rxnorm's
        // base version is "2024-04-10+1.0.0" (embedded date, plus sign — no artifactId prefix),
        // and dev.ikm.tinkar.data:loinc's is "loinc-2.82+1.0.0" (artifactId-prefixed on top of
        // that). Neither fits a plain \d+(\.\d+)* dotted-decimal shape.
        ArtifactCoordinates rxnorm = new ArtifactCoordinates("dev.ikm.tinkar.data", "rxnorm");
        Path rxnormVersionDir = repoRoot.resolve("dev/ikm/tinkar/data/rxnorm/2024-04-10+1.0.0-SNAPSHOT");
        writeFile(rxnormVersionDir.resolve("rxnorm-2024-04-10+1.0.0-20260714.140246-2-reasoned-sa.zip"), "zip-bytes");

        ArtifactCoordinates loinc = new ArtifactCoordinates("dev.ikm.tinkar.data", "loinc");
        Path loincVersionDir = repoRoot.resolve("dev/ikm/tinkar/data/loinc/loinc-2.82+1.0.0-SNAPSHOT");
        writeFile(loincVersionDir.resolve("loinc-loinc-2.82+1.0.0-20260714.135548-1-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertEquals(Set.of("reasoned-sa"), resolver.availableClassifiers(rxnorm, "2024-04-10+1.0.0-SNAPSHOT"));
        assertEquals(java.util.Optional.of("2024-04-10+1.0.0-20260714.140246-2"),
                resolver.resolveSnapshotFileVersion(rxnorm, "2024-04-10+1.0.0-SNAPSHOT"));
        assertEquals(List.of("2024-04-10+1.0.0-SNAPSHOT"), resolver.localVersions(rxnorm));

        assertEquals(Set.of("reasoned-sa"), resolver.availableClassifiers(loinc, "loinc-2.82+1.0.0-SNAPSHOT"));
        assertEquals(java.util.Optional.of("loinc-2.82+1.0.0-20260714.135548-1"),
                resolver.resolveSnapshotFileVersion(loinc, "loinc-2.82+1.0.0-SNAPSHOT"));
        assertEquals(List.of("loinc-2.82+1.0.0-SNAPSHOT"), resolver.localVersions(loinc));
    }

    private static final List<String> SA_CANDIDATES = List.of("reasoned-sa", "unreasoned-sa", "spined-array", "sa");

    @Test
    void compatibleVersionsKeepsOnlyVersionsWithACandidateClassifier(@TempDir Path repoRoot) throws IOException {
        Path artifactDir = repoRoot.resolve("network/ike/komet/example-plugin");
        // Compatible: has a reasoned-sa zip.
        writeFile(artifactDir.resolve("1.0.0/example-plugin-1.0.0-reasoned-sa.zip"), "zip-bytes");
        // Not compatible: only a PB classifier, which isn't among the SA candidates.
        writeFile(artifactDir.resolve("1.1.0/example-plugin-1.1.0-reasoned-pb.zip"), "zip-bytes");
        // Not compatible: a plain pom/jar release with no classified zip at all.
        writeFile(artifactDir.resolve("1.2.0/example-plugin-1.2.0.pom"), "<project/>");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertEquals(List.of("1.0.0"), resolver.compatibleVersions(COORDINATES, SA_CANDIDATES));
    }

    @Test
    void compatibleVersionsPreservesLexicographicOrderAndDropsNonMatchingInTheMiddle(@TempDir Path repoRoot) throws IOException {
        Path artifactDir = repoRoot.resolve("network/ike/komet/example-plugin");
        writeFile(artifactDir.resolve("1.0.0/example-plugin-1.0.0-unreasoned-sa.zip"), "zip-bytes");
        // A PB-only version sitting between the two compatible ones must be dropped, not reordered.
        writeFile(artifactDir.resolve("1.1.0/example-plugin-1.1.0-reasoned-pb.zip"), "zip-bytes");
        writeFile(artifactDir.resolve("1.2.0/example-plugin-1.2.0-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertEquals(List.of("1.0.0", "1.2.0"), resolver.compatibleVersions(COORDINATES, SA_CANDIDATES));
    }

    @Test
    void compatibleVersionsMatchesASnapshotByItsResolvedFileNaming(@TempDir Path repoRoot) throws IOException {
        // The -SNAPSHOT directory keeps its name while the zip carries a resolved timestamp;
        // compatibleVersions must still recognize the classifier and keep the version.
        writeFile(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0-SNAPSHOT/example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertEquals(List.of("1.0.0-SNAPSHOT"), resolver.compatibleVersions(COORDINATES, SA_CANDIDATES));
    }

    @Test
    void compatibleVersionsIsEmptyWhenNoVersionHasACandidateClassifier(@TempDir Path repoRoot) throws IOException {
        writeFile(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0/example-plugin-1.0.0-reasoned-pb.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertTrue(resolver.compatibleVersions(COORDINATES, SA_CANDIDATES).isEmpty());
    }

    @Test
    void compatibleVersionsIsEmptyForAnEmptyCandidateList(@TempDir Path repoRoot) throws IOException {
        writeFile(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0/example-plugin-1.0.0-reasoned-sa.zip"), "zip-bytes");

        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);

        assertTrue(resolver.compatibleVersions(COORDINATES, List.of()).isEmpty());
    }

    @Test
    void compatibleVersionsIsEmptyWhenArtifactDirectoryDoesNotExist(@TempDir Path repoRoot) {
        LocalVersionResolver resolver = new LocalVersionResolver(repoRoot);
        assertTrue(resolver.compatibleVersions(COORDINATES, SA_CANDIDATES).isEmpty());
    }

    private static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
