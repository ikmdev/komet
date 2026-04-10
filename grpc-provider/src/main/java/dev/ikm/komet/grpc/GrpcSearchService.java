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
package dev.ikm.komet.grpc;

import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.provider.search.SearchService;
import dev.ikm.tinkar.service.proto.SearchSortOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link SearchService} that delegates search calls to a remote
 * tinkar-core gRPC service via {@link GrpcSearchClient}.
 *
 * <p>This is the plug-in point for gRPC-backed search in Komet. When Komet is started
 * with {@code -Dkomet.grpc.port}, this service is activated via {@link #initialize}.
 * Downstream code (e.g., {@code NextGenSearchController}) checks {@link #isActive()} and
 * calls {@link #searchGrouped} or {@link #searchFlat} instead of the local Lucene path.
 *
 * <p>The {@link #search} method satisfies the {@link SearchService} contract but returns
 * an empty array — all meaningful results come through the typed methods that carry
 * grouped/semantic structure back from the service.
 */
public class GrpcSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcSearchService.class);

    private static volatile GrpcSearchService INSTANCE;

    /**
     * Sort options that mirror the UI sort buttons and map 1-to-1 to the
     * proto {@link SearchSortOption} enum.
     */
    public enum SortOption {
        TOP_COMPONENT,
        TOP_COMPONENT_ALPHA,
        SEMANTIC,
        SEMANTIC_ALPHA
    }

    /**
     * A top-level (grouped) search result — one per matching concept.
     *
     * @param publicId           stable UUIDs identifying the concept
     * @param fullyQualifiedName FQN of the concept
     * @param active             whether the concept is currently active
     * @param topScore           highest relevance score among child semantics
     * @param matchingSemantics  child semantic matches
     */
    public record GroupedResult(
            List<String> publicId,
            String fullyQualifiedName,
            boolean active,
            float topScore,
            List<MatchingSemantic> matchingSemantics) {}

    /**
     * A flat semantic search result (SEMANTIC sort modes) — one per matched semantic.
     *
     * @param publicId           stable UUIDs identifying the concept
     * @param fullyQualifiedName FQN of the concept
     * @param highlightedText    matched text with {@code <B>…</B>} markup
     * @param active             whether the concept is currently active
     * @param score              relevance score
     */
    public record SemanticResult(
            List<String> publicId,
            String fullyQualifiedName,
            String highlightedText,
            boolean active,
            float score) {}

    /**
     * A single semantic match within a {@link GroupedResult}.
     *
     * @param highlightedText matched text with {@code <B>…</B>} markup
     * @param plainText       plain text without HTML markup
     * @param score           relevance score
     */
    public record MatchingSemantic(String highlightedText, String plainText, float score) {}

    private GrpcSearchService() {}

    /**
     * Activates gRPC search mode by initializing the underlying {@link GrpcSearchClient}.
     * Must be called once at startup before any search calls.
     */
    public static void initialize(String host, int port) {
        GrpcSearchClient.initialize(host, port);
        INSTANCE = new GrpcSearchService();
        LOG.info("GrpcSearchService initialized → {}:{}", host, port);
    }

    /**
     * Returns {@code true} when gRPC mode has been initialized and is ready.
     */
    public static boolean isActive() {
        return INSTANCE != null && GrpcSearchClient.isAvailable();
    }

    /**
     * Returns the active singleton, or throws if not initialized.
     */
    public static GrpcSearchService get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("GrpcSearchService not initialized — pass -Dkomet.grpc.port to activate");
        }
        return INSTANCE;
    }

    /**
     * Performs a search returning grouped results (TOP_COMPONENT modes).
     * The sort option controls both ordering and grouping behaviour server-side.
     */
    public List<GroupedResult> searchGrouped(String query, int maxResults, SortOption sortOption) {
        SearchSortOption protoSort = toProtoSort(sortOption);
        var response = GrpcSearchClient.get().conceptSearchWithSort(query, maxResults, protoSort);
        return response.getGroupedResultsList().stream()
                .map(g -> new GroupedResult(
                        g.getPublicIdList(),
                        g.getFullyQualifiedName(),
                        g.getActive(),
                        g.getTopScore(),
                        g.getMatchingSemanticsList().stream()
                                .map(m -> new MatchingSemantic(
                                        m.getHighlightedText(), m.getPlainText(), m.getScore()))
                                .toList()))
                .toList();
    }

    /**
     * Performs a search returning flat semantic results (SEMANTIC modes).
     */
    public List<SemanticResult> searchFlat(String query, int maxResults, SortOption sortOption) {
        SearchSortOption protoSort = toProtoSort(sortOption);
        var response = GrpcSearchClient.get().conceptSearchWithSort(query, maxResults, protoSort);
        return response.getResultsList().stream()
                .map(r -> new SemanticResult(
                        r.getPublicIdList(),
                        r.getFullyQualifiedName(),
                        r.getHighlightedText(),
                        r.getActive(),
                        r.getScore()))
                .toList();
    }

    // --- SearchService contract ---

    /**
     * Not meaningful in gRPC mode — indexing is handled server-side.
     */
    @Override
    public void index(Object object) {
        LOG.debug("GrpcSearchService.index() called — no-op in gRPC mode");
    }

    /**
     * Not meaningful in gRPC mode — index commits are handled server-side.
     */
    @Override
    public void commit() throws IOException {
        LOG.debug("GrpcSearchService.commit() called — no-op in gRPC mode");
    }

    /**
     * Satisfies the {@link SearchService} contract. Returns raw Lucene-style results by
     * delegating to {@link #searchFlat} and converting to {@link PrimitiveDataSearchResult}.
     * NIDs are 0 since the local entity store is ephemeral; callers that need rich display
     * should use {@link #searchGrouped} or {@link #searchFlat} directly.
     */
    @Override
    public PrimitiveDataSearchResult[] search(String query, int maxResultSize) {
        List<SemanticResult> flat = searchFlat(query, maxResultSize, SortOption.SEMANTIC);
        return flat.stream()
                .map(r -> new PrimitiveDataSearchResult(0, 0, 0, 0, r.score(), r.highlightedText()))
                .toArray(PrimitiveDataSearchResult[]::new);
    }

    /**
     * Not meaningful in gRPC mode — index recreation is handled server-side.
     */
    @Override
    public CompletableFuture<Void> recreateIndex() {
        LOG.debug("GrpcSearchService.recreateIndex() called — no-op in gRPC mode");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String name() {
        return "GrpcSearchService";
    }

    private static SearchSortOption toProtoSort(SortOption sortOption) {
        return switch (sortOption) {
            case TOP_COMPONENT -> SearchSortOption.TOP_COMPONENT;
            case TOP_COMPONENT_ALPHA -> SearchSortOption.TOP_COMPONENT_ALPHA;
            case SEMANTIC -> SearchSortOption.SEMANTIC;
            case SEMANTIC_ALPHA -> SearchSortOption.SEMANTIC_ALPHA;
        };
    }
}
