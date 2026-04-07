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

import dev.ikm.tinkar.service.proto.SearchSortOption;
import dev.ikm.tinkar.service.proto.TinkarConceptSearchWithSortRequest;
import dev.ikm.tinkar.service.proto.TinkarConceptSearchWithSortResponse;
import dev.ikm.tinkar.service.proto.TinkarSearchServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Manages a gRPC channel to a running tinkar-core service and exposes
 * concept-search operations. Configured via system properties:
 * <ul>
 *   <li>{@code komet.grpc.host} – hostname (default: {@code localhost})</li>
 *   <li>{@code komet.grpc.port} – port number (default: {@code 9090})</li>
 * </ul>
 * Call {@link #initialize(String, int)} once at startup, then access via {@link #get()}.
 */
public class GrpcSearchClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcSearchClient.class);

    private static volatile GrpcSearchClient instance;

    private final ManagedChannel channel;
    private final TinkarSearchServiceGrpc.TinkarSearchServiceBlockingStub stub;

    private GrpcSearchClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = TinkarSearchServiceGrpc.newBlockingStub(channel);
        LOG.info("gRPC client initialised → {}:{}", host, port);
    }

    /**
     * Creates and registers the singleton client.
     *
     * @param host gRPC server hostname
     * @param port gRPC server port
     */
    public static void initialize(String host, int port) {
        instance = new GrpcSearchClient(host, port);
    }

    /** Returns {@code true} when the client has been initialised. */
    public static boolean isAvailable() {
        return instance != null;
    }

    /** Returns the singleton client, or {@code null} if not yet initialised. */
    public static GrpcSearchClient get() {
        return instance;
    }

    /**
     * Calls {@code TinkarSearchService.ConceptSearchWithSort} on the remote service.
     *
     * @param query      free-text search string
     * @param maxResults maximum number of results to return
     * @param sortBy     sort order for results
     * @return the response from the server
     */
    public TinkarConceptSearchWithSortResponse conceptSearchWithSort(
            String query, int maxResults, SearchSortOption sortBy) {

        TinkarConceptSearchWithSortRequest request = TinkarConceptSearchWithSortRequest.newBuilder()
                .setQuery(query)
                .setMaxResults(maxResults)
                .setSortBy(sortBy)
                .build();
        return stub.conceptSearchWithSort(request);
    }

    @Override
    public void close() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            LOG.info("gRPC channel shut down");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            channel.shutdownNow();
        }
    }
}
