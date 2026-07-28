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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A lightweight "does this repository URL accept these credentials" probe — a plain
 * {@code HEAD} request against the repository's base URL, since Nexus (and Central) both
 * require whatever auth the repository needs on any request to it, including the bare base
 * URL, so no specific artifact path needs to be known in advance.
 */
public final class RepositoryConnectionTester {

    private RepositoryConnectionTester() {
    }

    /**
     * Probes {@code repositoryBaseUrl} without credentials.
     *
     * @param httpClient the HTTP client to probe with
     * @param repositoryBaseUrl the repository's base URL
     * @return {@code true} if the server responded with a 2xx status
     * @throws IOException if the request fails outright (connection refused, DNS failure, ...)
     * @throws InterruptedException if the request is interrupted
     */
    public static boolean testConnection(HttpClient httpClient, String repositoryBaseUrl) throws IOException, InterruptedException {
        return testConnection(httpClient, repositoryBaseUrl, null);
    }

    /**
     * As {@link #testConnection(HttpClient, String)}, authenticating with {@code credentials}.
     *
     * @param httpClient the HTTP client to probe with
     * @param repositoryBaseUrl the repository's base URL
     * @param credentials the credentials to authenticate with, or {@code null} for none
     * @return {@code true} if the server responded with a 2xx status
     * @throws IOException if the request fails outright (connection refused, DNS failure, ...)
     * @throws InterruptedException if the request is interrupted
     */
    public static boolean testConnection(HttpClient httpClient, String repositoryBaseUrl, Credentials credentials)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(repositoryBaseUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (credentials != null) {
            requestBuilder.header("Authorization", BasicAuth.header(credentials));
        }
        HttpResponse<Void> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.discarding());
        int statusCode = response.statusCode();
        return statusCode >= 200 && statusCode < 300;
    }
}
