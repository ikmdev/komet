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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.ikm.tinkar.common.service.TrackingListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenDataStoreDownloadTaskTest {

    private HttpServer server;
    private String baseUrl;
    private ExecutorService executor;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void unpackModeProducesExpectedDirectoryShape(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip(
                "nidToPatternNidMap/.keep", "",
                "nidToByteArrayMap/.keep", "",
                "nidToCitingComponentNidMap/.keep", "");
        server.createContext("/sa/example-plugin-1.0.0-sa.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "sa/example-plugin-1.0.0-sa.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-1.0.0-sa.zip");
        Path destinationDirectory = destinationRoot.resolve("my-store");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.unpackTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationDirectory, MavenDataStoreDownloadTask.AssetVerification.NONE);

        Path result = task.call();

        assertEquals(destinationDirectory, result);
        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToPatternNidMap")));
        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToByteArrayMap")));
        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToCitingComponentNidMap")));
        assertArrayEquals(zipBytes, Files.readAllBytes(cacheFile), "the download should be staged at the local repository cache path");
    }

    @Test
    void unpackRecordsAContentChecksumMatchingTheCurrentOnDiskState(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip(
                "nidToPatternNidMap/.keep", "",
                "nidToByteArrayMap/data.bin", "some-real-content");
        server.createContext("/sa/example-plugin-3.0.0-sa.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "sa/example-plugin-3.0.0-sa.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-3.0.0-sa.zip");
        Path destinationDirectory = destinationRoot.resolve("my-store");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.unpackTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationDirectory, MavenDataStoreDownloadTask.AssetVerification.NONE);

        task.call();

        Optional<String> recorded = MavenDataStoreDownloadTask.existingUnpackedContentSha256(destinationDirectory);
        assertTrue(recorded.isPresent(), "unpack should record a content checksum marker");
        assertEquals(recorded.get(), MavenDataStoreDownloadTask.currentUnpackedContentSha256(destinationDirectory),
                "immediately after unpack, the recorded content checksum should match the actual on-disk state");
    }

    @Test
    void editingAFileAfterUnpackIsDetectableAsContentDrift(@TempDir Path destinationRoot) throws Exception {
        // A flat, un-nested entry — deliberately not sharing a single top-level directory with
        // anything else, so commonTopLevelDirectory's wrapper-stripping never kicks in and this
        // lands at exactly destinationDirectory/data.bin.
        byte[] zipBytes = buildZip("data.bin", "original-content");
        server.createContext("/sa/example-plugin-3.1.0-sa.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "sa/example-plugin-3.1.0-sa.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-3.1.0-sa.zip");
        Path destinationDirectory = destinationRoot.resolve("my-store");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.unpackTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationDirectory, MavenDataStoreDownloadTask.AssetVerification.NONE);
        task.call();
        String recordedContentSha256 = MavenDataStoreDownloadTask.existingUnpackedContentSha256(destinationDirectory).orElseThrow();

        // Simulates local drift entirely independent of the source repository — no re-download,
        // no change to the cached zip or its checksum, just the extracted file itself being
        // altered on disk after the fact (a manual edit, partial write, or corruption).
        Files.writeString(destinationDirectory.resolve("data.bin"), "tampered-content");

        String currentContentSha256 = MavenDataStoreDownloadTask.currentUnpackedContentSha256(destinationDirectory);
        assertTrue(!currentContentSha256.equalsIgnoreCase(recordedContentSha256),
                "editing an extracted file's content should change the recomputed on-disk hash");
    }

    @Test
    void unpackModeStripsASingleWrappingTopLevelDirectory(@TempDir Path destinationRoot) throws Exception {
        // Real shape confirmed against a live published store snapshot
        // (dev.ikm.tinkar.data:loinc:1.0.0-SNAPSHOT, reasoned-sa classifier), 2026-07-21: every
        // entry is nested under a directory named after the artifact, not at the zip's root.
        byte[] zipBytes = buildZip(
                "loinc/nidToPatternNidMap/.keep", "",
                "loinc/nidToByteArrayMap/.keep", "",
                "loinc/nidToCitingComponentNidMap/.keep", "");
        server.createContext("/sa/loinc-1.0.0-reasoned-sa.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "sa/loinc-1.0.0-reasoned-sa.zip");
        Path cacheFile = destinationRoot.resolve("cache/loinc-1.0.0-reasoned-sa.zip");
        Path destinationDirectory = destinationRoot.resolve("my-store");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.unpackTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationDirectory, MavenDataStoreDownloadTask.AssetVerification.NONE);

        task.call();

        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToPatternNidMap")),
                "the wrapping \"loinc\" directory should be stripped, landing content directly at the destination root");
        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToByteArrayMap")));
        assertTrue(Files.isDirectory(destinationDirectory.resolve("nidToCitingComponentNidMap")));
        assertFalse(Files.exists(destinationDirectory.resolve("loinc")),
                "the wrapping directory itself should not appear as a subdirectory of the destination");
    }

    @Test
    void placeAsFileModeProducesExactFilename(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "fake-protobuf-bytes");
        server.createContext("/pb/example-plugin-2.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "pb/example-plugin-2.0.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-2.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-2.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertTrue(Files.exists(destinationFile));
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
    }

    @Test
    void checksumVerificationSucceedsWithTheCorrectSha256(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "checksum-verified-bytes");
        String correctSha256 = sha256Hex(zipBytes);
        server.createContext("/verify/example-plugin-7.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "verify/example-plugin-7.0.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-7.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-7.0.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification =
                new MavenDataStoreDownloadTask.AssetVerification(Optional.of(correctSha256), Optional.empty());
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
    }

    @Test
    void successfulChecksumVerificationIsSignaledInTheTaskMessage(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "checksum-verified-bytes");
        String correctSha256 = sha256Hex(zipBytes);
        server.createContext("/verifymsg/example-plugin-9.5.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "verifymsg/example-plugin-9.5.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-9.5.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-9.5.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification =
                new MavenDataStoreDownloadTask.AssetVerification(Optional.of(correctSha256), Optional.empty());
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        List<String> messages = new ArrayList<>();
        task.addListener(silentListenerCapturingMessages(messages));
        task.call();

        assertTrue(messages.stream().filter(Objects::nonNull).anyMatch(m -> m.contains("checksum verified")),
                "expected a message signaling successful checksum verification, got: " + messages);
    }

    @Test
    void missingChecksumIsSignaledInTheTaskMessageRatherThanSilentlySkipped(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "no-checksum-available-bytes");
        server.createContext("/nochecksum/example-plugin-9.6.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "nochecksum/example-plugin-9.6.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-9.6.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-9.6.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE);

        List<String> messages = new ArrayList<>();
        task.addListener(silentListenerCapturingMessages(messages));
        task.call();

        assertTrue(messages.stream().filter(Objects::nonNull).anyMatch(m -> m.contains("no checksum reported")),
                "expected a message signaling that no checksum was available to verify, got: " + messages);
    }

    private static TrackingListener<Path> silentListenerCapturingMessages(List<String> messages) {
        return new TrackingListener<>() {
            @Override
            public void updateValue(Path result) {
            }

            @Override
            public void updateMessage(String message) {
                messages.add(message);
            }

            @Override
            public void updateTitle(String title) {
            }

            @Override
            public void updateProgress(double workDone, double max) {
            }
        };
    }

    @Test
    void checksumMismatchFailsTheDownloadAndLeavesNoCacheFile(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "tampered-or-corrupted-bytes");
        server.createContext("/verify/example-plugin-8.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "verify/example-plugin-8.0.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-8.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-8.0.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification =
                new MavenDataStoreDownloadTask.AssetVerification(Optional.of("0".repeat(64)), Optional.empty());
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        IOException exception = assertThrows(IOException.class, task::call);

        assertTrue(exception.getMessage().contains("checksum"), "expected a checksum-mismatch message, got: " + exception.getMessage());
        assertFalse(Files.exists(cacheFile), "a checksum-mismatched download must not be left in the local repository cache");
        assertFalse(Files.exists(destinationFile), "a checksum-mismatched download must not be materialized at the destination");
    }

    @Test
    void pomIsFetchedAndCachedAlongsideTheZip(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "zip-bytes");
        byte[] pomBytes = "<project><modelVersion>4.0.0</modelVersion></project>".getBytes(StandardCharsets.UTF_8);
        server.createContext("/pomtest/example-plugin-1.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));
        server.createContext("/pomtest/example-plugin-1.0.0.pom", exchange -> respondBytes(exchange, pomBytes));

        URI downloadUri = URI.create(baseUrl + "pomtest/example-plugin-1.0.0-pb.zip");
        URI pomUri = URI.create(baseUrl + "pomtest/example-plugin-1.0.0.pom");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-1.0.0-pb.zip");
        Path pomCacheFile = destinationRoot.resolve("cache/example-plugin-1.0.0.pom");
        Path destinationFile = destinationRoot.resolve("example-plugin-1.0.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification = new MavenDataStoreDownloadTask.AssetVerification(
                Optional.empty(), Optional.of(new MavenDataStoreDownloadTask.AssetVerification.PomFetch(pomUri, pomCacheFile, Optional.empty())));
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        task.call();

        assertArrayEquals(pomBytes, Files.readAllBytes(pomCacheFile));
    }

    @Test
    void pomFetchFailureIsNonFatalAndTheZipDownloadStillSucceeds(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "zip-bytes");
        server.createContext("/pomtest404/example-plugin-2.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));
        server.createContext("/pomtest404/example-plugin-2.0.0.pom", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });

        URI downloadUri = URI.create(baseUrl + "pomtest404/example-plugin-2.0.0-pb.zip");
        URI pomUri = URI.create(baseUrl + "pomtest404/example-plugin-2.0.0.pom");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-2.0.0-pb.zip");
        Path pomCacheFile = destinationRoot.resolve("cache/example-plugin-2.0.0.pom");
        Path destinationFile = destinationRoot.resolve("example-plugin-2.0.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification = new MavenDataStoreDownloadTask.AssetVerification(
                Optional.empty(), Optional.of(new MavenDataStoreDownloadTask.AssetVerification.PomFetch(pomUri, pomCacheFile, Optional.empty())));
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
        assertFalse(Files.exists(pomCacheFile), "a failed POM fetch should be silently skipped, not fabricated");
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test
    void alreadyCachedArtifactIsMaterializedWithoutAnyNetworkRequest(@TempDir Path destinationRoot) throws Exception {
        // No context registered for this path — if the task made a network request, the
        // server would respond 404 and the task would fail; it must succeed using only the
        // pre-populated cache file.
        byte[] zipBytes = buildZip("entities.pb", "already-cached-bytes");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-9.0.0-pb.zip");
        Files.createDirectories(cacheFile.getParent());
        Files.write(cacheFile, zipBytes);

        URI downloadUri = URI.create(baseUrl + "never-hit/example-plugin-9.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-9.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
    }

    @Test
    void cachedCopyMatchingTheReportedChecksumIsUsedWithoutAnyNetworkRequest(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "still-fresh-bytes");
        String correctSha256 = sha256Hex(zipBytes);
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-9.1.0-pb.zip");
        Files.createDirectories(cacheFile.getParent());
        Files.write(cacheFile, zipBytes);

        // No context registered — a network request here would 404 and fail the task.
        URI downloadUri = URI.create(baseUrl + "never-hit/example-plugin-9.1.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-9.1.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification =
                new MavenDataStoreDownloadTask.AssetVerification(Optional.of(correctSha256), Optional.empty());
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        List<String> messages = new ArrayList<>();
        task.addListener(silentListenerCapturingMessages(messages));
        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
        assertTrue(messages.stream().filter(Objects::nonNull).anyMatch(m -> m.contains("source unchanged")),
                "expected a message signaling the cached copy still matches the repository, got: " + messages);
    }

    @Test
    void cachedCopyNotMatchingTheReportedChecksumIsTreatedAsStaleAndRefetched(@TempDir Path destinationRoot) throws Exception {
        byte[] staleBytes = buildZip("entities.pb", "outdated-local-copy");
        byte[] freshBytes = buildZip("entities.pb", "newly-republished-content");
        String freshSha256 = sha256Hex(freshBytes);
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-9.2.0-pb.zip");
        Files.createDirectories(cacheFile.getParent());
        Files.write(cacheFile, staleBytes);

        server.createContext("/refetch/example-plugin-9.2.0-pb.zip", exchange -> respondBytes(exchange, freshBytes));
        URI downloadUri = URI.create(baseUrl + "refetch/example-plugin-9.2.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-9.2.0-pb.zip");
        MavenDataStoreDownloadTask.AssetVerification verification =
                new MavenDataStoreDownloadTask.AssetVerification(Optional.of(freshSha256), Optional.empty());
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, verification);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(freshBytes, Files.readAllBytes(destinationFile),
                "a stale cache (not matching the repository's current checksum) must be refetched, not silently reused");
        assertArrayEquals(freshBytes, Files.readAllBytes(cacheFile), "the local repository cache itself should be refreshed too");
    }

    @Test
    void materializeProgressReachesCompletionEvenWithNoDownloadPhase(@TempDir Path destinationRoot) throws Exception {
        // A "Local repository" browse never downloads anything — materializing alone must still
        // drive the progress bar, not leave it at whatever it initialized to the whole time.
        byte[] zipBytes = buildZip("entities.pb", "x".repeat(50_000));
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-10.1.0-pb.zip");
        Files.createDirectories(cacheFile.getParent());
        Files.write(cacheFile, zipBytes);

        Path destinationFile = destinationRoot.resolve("example-plugin-10.1.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.localPlaceAsFileTask(cacheFile, destinationFile);

        List<Double> progressValues = new ArrayList<>();
        task.addListener(new TrackingListener<Path>() {
            @Override
            public void updateValue(Path result) {
            }

            @Override
            public void updateMessage(String message) {
            }

            @Override
            public void updateTitle(String title) {
            }

            @Override
            public void updateProgress(double workDone, double max) {
                if (max > 0) {
                    progressValues.add(workDone / max);
                }
            }
        });

        task.call();

        assertFalse(progressValues.isEmpty(), "expected at least one progress update during materialization alone");
        assertEquals(1.0, progressValues.getLast(), 0.0001);
    }

    @Test
    void localTaskMaterializesAnAlreadyCachedArtifactWithNoHttpClientAtAll(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "local-only-bytes");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-10.0.0-pb.zip");
        Files.createDirectories(cacheFile.getParent());
        Files.write(cacheFile, zipBytes);

        Path destinationFile = destinationRoot.resolve("example-plugin-10.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.localPlaceAsFileTask(cacheFile, destinationFile);

        Path result = task.call();

        assertEquals(destinationFile, result);
        assertArrayEquals(zipBytes, Files.readAllBytes(destinationFile));
    }

    @Test
    void localTaskFailsClearlyWhenTheCacheFileIsMissing(@TempDir Path destinationRoot) {
        Path cacheFile = destinationRoot.resolve("cache/does-not-exist-1.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("does-not-exist-1.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.localPlaceAsFileTask(cacheFile, destinationFile);

        assertThrows(IOException.class, task::call);
    }

    @Test
    void cancellationLeavesNoPartialFileAtCacheOrDestination(@TempDir Path destinationRoot) throws Exception {
        CountDownLatch firstChunkSent = new CountDownLatch(1);
        server.createContext("/slow/example-plugin-3.0.0-sa.zip", exchange -> respondSlowly(exchange, firstChunkSent));

        URI downloadUri = URI.create(baseUrl + "slow/example-plugin-3.0.0-sa.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-3.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-3.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE);

        CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);

        assertTrue(firstChunkSent.await(5, TimeUnit.SECONDS), "server never started streaming");
        task.cancel();

        ExecutionException executionException = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertTrue(executionException.getCause() instanceof RuntimeException);
        assertTrue(executionException.getCause().getCause() instanceof CancellationException);
        assertFalse(Files.exists(destinationFile), "no partial file should be left at the destination after cancellation");
        assertFalse(Files.exists(cacheFile), "no partial file should be left at the cache path after cancellation");
    }

    @Test
    void progressAndEtaAreSane(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "x".repeat(50_000));
        server.createContext("/progress/example-plugin-4.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        URI downloadUri = URI.create(baseUrl + "progress/example-plugin-4.0.0-pb.zip");
        Path cacheFile = destinationRoot.resolve("cache/example-plugin-4.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-4.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE);

        List<Double> progressValues = new ArrayList<>();
        task.addListener(new TrackingListener<Path>() {
            @Override
            public void updateValue(Path result) {
            }

            @Override
            public void updateMessage(String message) {
            }

            @Override
            public void updateTitle(String title) {
            }

            @Override
            public void updateProgress(double workDone, double max) {
                if (max > 0) {
                    progressValues.add(workDone / max);
                }
            }
        });

        task.call();

        assertFalse(progressValues.isEmpty(), "expected at least one progress update");
        for (Double fraction : progressValues) {
            assertTrue(fraction >= 0.0 && fraction <= 1.0, "progress fraction out of range: " + fraction);
        }
        assertEquals(1.0, progressValues.getLast(), 0.0001);
        assertTrue(task.estimateTimeRemaining().getSeconds() >= 0);
    }

    @Test
    void downloadAndHeadProbeSendBasicAuthorizationWhenCredentialsProvided(@TempDir Path destinationRoot) throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "authed-bytes");
        AtomicReference<String> downloadAuthHeader = new AtomicReference<>();
        AtomicReference<String> headAuthHeader = new AtomicReference<>();
        server.createContext("/auth/example-plugin-6.0.0-pb.zip", exchange -> {
            if ("HEAD".equals(exchange.getRequestMethod())) {
                headAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            } else {
                downloadAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            }
            respondBytes(exchange, zipBytes);
        });

        URI downloadUri = URI.create(baseUrl + "auth/example-plugin-6.0.0-pb.zip");
        Credentials credentials = new Credentials("build-user", "s3cret".toCharArray());
        String expectedHeader = "Basic " + Base64.getEncoder().encodeToString("build-user:s3cret".getBytes(StandardCharsets.UTF_8));

        OptionalLong size = MavenDataStoreDownloadTask.headContentLength(HttpClient.newHttpClient(), downloadUri, credentials);
        assertEquals(expectedHeader, headAuthHeader.get());
        assertTrue(size.isPresent());

        Path cacheFile = destinationRoot.resolve("cache/example-plugin-6.0.0-pb.zip");
        Path destinationFile = destinationRoot.resolve("example-plugin-6.0.0-pb.zip");
        MavenDataStoreDownloadTask task = MavenDataStoreDownloadTask.placeAsFileTask(HttpClient.newHttpClient(), downloadUri, cacheFile, destinationFile, MavenDataStoreDownloadTask.AssetVerification.NONE, credentials);
        task.call();

        assertEquals(expectedHeader, downloadAuthHeader.get());
    }

    @Test
    void downloadUriFollowsMaven2Layout() {
        ArtifactCoordinates coordinates = new ArtifactCoordinates("network.ike.komet", "example-plugin");
        URI uri = MavenDataStoreDownloadTask.downloadUri("https://repo.example.com/releases", coordinates, "1.0.0", "sa");
        assertEquals("https://repo.example.com/releases/network/ike/komet/example-plugin/1.0.0/example-plugin-1.0.0-sa.zip", uri.toString());
    }

    @Test
    void localRepositoryPathFollowsMaven2Layout(@TempDir Path repoRoot) {
        ArtifactCoordinates coordinates = new ArtifactCoordinates("network.ike.komet", "example-plugin");
        Path path = MavenDataStoreDownloadTask.localRepositoryPath(repoRoot, coordinates, "1.0.0", "sa");
        assertEquals(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0/example-plugin-1.0.0-sa.zip"), path);
    }

    // The version directory a build lives under. A repository search answers with the RESOLVED
    // snapshot build; using that as the directory too requests a path that does not exist — the
    // 404-on-a-present-artifact of ikmdev/komet-desktop#116.

    @Test
    void directoryVersionRewritesAResolvedSnapshotBuildToItsSnapshotDirectory() {
        assertEquals("1.0.0-SNAPSHOT", MavenDataStoreDownloadTask.directoryVersion("1.0.0-20260714.135548-4"));
        assertEquals("1-chronology-builder-SNAPSHOT",
                MavenDataStoreDownloadTask.directoryVersion("1-chronology-builder-20260724.000852-12"));
    }

    @Test
    void directoryVersionLeavesASnapshotVersionUnchanged() {
        assertEquals("1.0.0-SNAPSHOT", MavenDataStoreDownloadTask.directoryVersion("1.0.0-SNAPSHOT"));
    }

    @Test
    void directoryVersionLeavesAReleaseUnchanged() {
        assertEquals("1.0.0", MavenDataStoreDownloadTask.directoryVersion("1.0.0"));
    }

    @Test
    void directoryVersionLeavesADateBasedReleaseUnchanged() {
        // SOLOR publishes date-based release versions; they must not read as a build timestamp.
        assertEquals("20250827", MavenDataStoreDownloadTask.directoryVersion("20250827"));
        assertEquals("2024-04-10+1.0.0", MavenDataStoreDownloadTask.directoryVersion("2024-04-10+1.0.0"));
    }

    @Test
    void directoryVersionRewritesAResolvedSnapshotOfADateQualifiedVersion() {
        // The rxnorm shape: a date-qualified base version with a resolved snapshot build appended.
        assertEquals("2024-04-10+1.0.0-SNAPSHOT",
                MavenDataStoreDownloadTask.directoryVersion("2024-04-10+1.0.0-20260714.140246-2"));
    }

    @Test
    void directoryVersionIsIdempotent() {
        String resolved = MavenDataStoreDownloadTask.directoryVersion("1.0.0-20260714.135548-4");
        assertEquals(resolved, MavenDataStoreDownloadTask.directoryVersion(resolved));
    }

    @Test
    void downloadUriForSnapshotUsesSnapshotDirectoryButResolvedFileVersion() {
        ArtifactCoordinates coordinates = new ArtifactCoordinates("network.ike.komet", "example-plugin");
        URI uri = MavenDataStoreDownloadTask.downloadUri("https://repo.example.com/releases", coordinates,
                "1.0.0-SNAPSHOT", "1.0.0-20260714.135548-4", "reasoned-sa");
        assertEquals("https://repo.example.com/releases/network/ike/komet/example-plugin/1.0.0-SNAPSHOT/"
                + "example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip", uri.toString());
    }

    @Test
    void localRepositoryPathForSnapshotUsesSnapshotDirectoryButResolvedFileVersion(@TempDir Path repoRoot) {
        ArtifactCoordinates coordinates = new ArtifactCoordinates("network.ike.komet", "example-plugin");
        Path path = MavenDataStoreDownloadTask.localRepositoryPath(repoRoot, coordinates,
                "1.0.0-SNAPSHOT", "1.0.0-20260714.135548-4", "reasoned-sa");
        assertEquals(repoRoot.resolve("network/ike/komet/example-plugin/1.0.0-SNAPSHOT/"
                + "example-plugin-1.0.0-20260714.135548-4-reasoned-sa.zip"), path);
    }

    @Test
    void headContentLengthReturnsServerReportedSize() throws Exception {
        byte[] zipBytes = buildZip("entities.pb", "12345");
        server.createContext("/head/example-plugin-5.0.0-pb.zip", exchange -> respondBytes(exchange, zipBytes));

        OptionalLong size = MavenDataStoreDownloadTask.headContentLength(HttpClient.newHttpClient(),
                URI.create(baseUrl + "head/example-plugin-5.0.0-pb.zip"));

        assertTrue(size.isPresent());
        assertEquals(zipBytes.length, size.getAsLong());
    }

    private static byte[] buildZip(String... nameContentPairs) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(buffer)) {
            for (int i = 0; i < nameContentPairs.length; i += 2) {
                zipOutputStream.putNextEntry(new ZipEntry(nameContentPairs[i]));
                zipOutputStream.write(nameContentPairs[i + 1].getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        }
        return buffer.toByteArray();
    }

    private static void respondBytes(HttpExchange exchange, byte[] bytes) throws IOException {
        if ("HEAD".equals(exchange.getRequestMethod())) {
            // com.sun.net.httpserver doesn't set Content-Length from sendResponseHeaders'
            // body-length argument on a HEAD response (no body is written) — set it explicitly.
            exchange.getResponseHeaders().add("Content-Length", String.valueOf(bytes.length));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void respondSlowly(HttpExchange exchange, CountDownLatch firstChunkSent) throws IOException {
        byte[] chunk = "x".repeat(4096).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, (long) chunk.length * 50);
        try (OutputStream out = exchange.getResponseBody()) {
            for (int i = 0; i < 50; i++) {
                out.write(chunk);
                out.flush();
                if (i == 0) {
                    firstChunkSent.countDown();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
