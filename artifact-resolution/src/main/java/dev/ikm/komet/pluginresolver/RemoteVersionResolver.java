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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lists artifact versions published on a remote Maven repository, by fetching and parsing
 * that repository's {@code maven-metadata.xml} for the artifact over HTTP(S). Uses only the
 * JDK's built-in {@link HttpClient} — no Maven Resolver/Aether dependency.
 */
public final class RemoteVersionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteVersionResolver.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /**
     * The full result of listing an artifact's published versions.
     *
     * @param versions every published version, in the order {@code maven-metadata.xml} lists
     *         them (oldest first, by Maven convention)
     * @param mostRecent the version {@code maven-metadata.xml} itself declares as most recent
     *         — its {@code <release>} tag, falling back to {@code <latest>}, falling back to
     *         the last entry in {@code versions} if neither tag is present. Preferring the
     *         declared tag over sorting version strings ourselves matters here since versions
     *         in this codebase mix schemes (semver, date-based like {@code 20251008}) that
     *         don't share a single correct sort order.
     */
    public record VersionListing(List<String> versions, Optional<String> mostRecent) {
    }

    private final HttpClient httpClient;

    /**
     * Creates a resolver using a default {@link HttpClient}.
     */
    public RemoteVersionResolver() {
        this(HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build());
    }

    /**
     * Creates a resolver using the given {@link HttpClient}, for testing against a fixture
     * server or to reuse a caller-managed client instance.
     *
     * @param httpClient the HTTP client to issue requests with
     * @throws NullPointerException if {@code httpClient} is {@code null}
     */
    public RemoteVersionResolver(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /**
     * Lists the versions of {@code coordinates} published under {@code repositoryBaseUrl},
     * with no authentication.
     *
     * @param repositoryBaseUrl the repository's base URL (e.g. {@code "https://repo.maven.apache.org/maven2/"})
     * @param coordinates the artifact to look up
     * @return the published versions, in the order {@code maven-metadata.xml} lists them
     *         (oldest first, by Maven convention); empty if the repository has no metadata
     *         for this artifact (a 404 response)
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if either argument is {@code null}
     */
    public List<String> remoteVersions(String repositoryBaseUrl, ArtifactCoordinates coordinates)
            throws IOException, InterruptedException {
        return remoteVersionListing(repositoryBaseUrl, coordinates, Optional.empty()).versions();
    }

    /**
     * Lists the versions of {@code coordinates} published under {@code repositoryBaseUrl},
     * authenticating with HTTP Basic auth using {@code credentials}.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @param credentials the credentials to authenticate with
     * @return the published versions, in the order {@code maven-metadata.xml} lists them
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if any argument is {@code null}
     */
    public List<String> remoteVersions(String repositoryBaseUrl, ArtifactCoordinates coordinates, Credentials credentials)
            throws IOException, InterruptedException {
        Objects.requireNonNull(credentials, "credentials");
        return remoteVersionListing(repositoryBaseUrl, coordinates, Optional.of(credentials)).versions();
    }

    /**
     * As {@link #remoteVersions(String, ArtifactCoordinates)}, but also reports which version
     * is most recent (see {@link VersionListing#mostRecent}), with no authentication.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @return the full version listing
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if either argument is {@code null}
     */
    public VersionListing remoteVersionListing(String repositoryBaseUrl, ArtifactCoordinates coordinates)
            throws IOException, InterruptedException {
        return remoteVersionListing(repositoryBaseUrl, coordinates, Optional.empty());
    }

    /**
     * As {@link #remoteVersionListing(String, ArtifactCoordinates)}, authenticating with HTTP
     * Basic auth using {@code credentials}.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @param credentials the credentials to authenticate with
     * @return the full version listing
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if any argument is {@code null}
     */
    public VersionListing remoteVersionListing(String repositoryBaseUrl, ArtifactCoordinates coordinates, Credentials credentials)
            throws IOException, InterruptedException {
        Objects.requireNonNull(credentials, "credentials");
        return remoteVersionListing(repositoryBaseUrl, coordinates, Optional.of(credentials));
    }

    private VersionListing remoteVersionListing(String repositoryBaseUrl, ArtifactCoordinates coordinates,
                                                 Optional<Credentials> credentials) throws IOException, InterruptedException {
        Objects.requireNonNull(repositoryBaseUrl, "repositoryBaseUrl");
        Objects.requireNonNull(coordinates, "coordinates");

        URI metadataUri = metadataUri(repositoryBaseUrl, coordinates);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(metadataUri)
                .timeout(REQUEST_TIMEOUT)
                .GET();
        credentials.ifPresent(creds -> requestBuilder.header("Authorization", BasicAuth.header(creds)));

        HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 404) {
            LOG.debug("No maven-metadata.xml for {} at {}", coordinates, metadataUri);
            return new VersionListing(List.of(), Optional.empty());
        }
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP " + response.statusCode() + " fetching " + metadataUri);
        }
        try (InputStream body = response.body()) {
            return parseVersions(body, metadataUri);
        }
    }

    private static URI metadataUri(String repositoryBaseUrl, ArtifactCoordinates coordinates) {
        String base = repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
        return URI.create(base + coordinates.groupPath() + "/" + coordinates.artifactId() + "/maven-metadata.xml");
    }

    private static VersionListing parseVersions(InputStream metadataXml, URI source) throws IOException {
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(metadataXml);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse maven-metadata.xml from " + source, e);
        }
        document.getDocumentElement().normalize();

        List<String> versions = new ArrayList<>();
        NodeList versionNodes = document.getElementsByTagName("version");
        for (int i = 0; i < versionNodes.getLength(); i++) {
            Node node = versionNodes.item(i);
            if (node instanceof Element element && "versions".equals(parentTagName(element))) {
                versions.add(element.getTextContent().trim());
            }
        }

        Optional<String> mostRecent = directChildText(document, "release")
                .or(() -> directChildText(document, "latest"))
                .or(() -> versions.isEmpty() ? Optional.empty() : Optional.of(versions.getLast()));

        return new VersionListing(List.copyOf(versions), mostRecent);
    }

    /**
     * The text of the first {@code <tagName>} element found that's a direct child of
     * {@code <versioning>} — {@code <release>} and {@code <latest>} both live there, and this
     * avoids accidentally matching a same-named element anywhere else in the document.
     */
    private static Optional<String> directChildText(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && "versioning".equals(parentTagName(element))) {
                String text = element.getTextContent().trim();
                if (!text.isBlank()) {
                    return Optional.of(text);
                }
            }
        }
        return Optional.empty();
    }

    private static String parentTagName(Element element) {
        Node parent = element.getParentNode();
        return parent instanceof Element parentElement ? parentElement.getTagName() : "";
    }

    /**
     * Resolves a {@code -SNAPSHOT} version (e.g. {@code "1.0.0-SNAPSHOT"}) to the concrete,
     * uniquely-timestamped file version (e.g. {@code "1.0.0-20260714.135548-4"}) actually used
     * in that snapshot's published filenames — needed because a Nexus-hosted snapshot repository
     * publishes each deploy under a unique timestamp, not the literal {@code -SNAPSHOT} suffix:
     * the version <em>directory</em> stays {@code 1.0.0-SNAPSHOT}, but every file inside it (and
     * the component's own {@code version} field, as seen by
     * {@code NexusSearchClient.classifiersFor}) uses the resolved value. Fetches and parses that
     * version's own {@code maven-metadata.xml} (at {@code <groupPath>/<artifactId>/<version>/
     * maven-metadata.xml}, distinct from the artifact-level one {@link #remoteVersionListing}
     * reads), with no authentication.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @param snapshotVersion the {@code -SNAPSHOT} version to resolve
     * @return the resolved file version, or empty if this repository has no snapshot metadata
     *         for this version (a 404 response — most likely {@code snapshotVersion} isn't
     *         actually a published snapshot line)
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if any argument is {@code null}
     */
    public Optional<String> resolveSnapshotFileVersion(String repositoryBaseUrl, ArtifactCoordinates coordinates,
                                                         String snapshotVersion) throws IOException, InterruptedException {
        return resolveSnapshotFileVersion(repositoryBaseUrl, coordinates, snapshotVersion, Optional.empty());
    }

    /**
     * As {@link #resolveSnapshotFileVersion(String, ArtifactCoordinates, String)}, authenticating
     * with {@code credentials}.
     *
     * @param repositoryBaseUrl the repository's base URL
     * @param coordinates the artifact to look up
     * @param snapshotVersion the {@code -SNAPSHOT} version to resolve
     * @param credentials the credentials to authenticate with
     * @return the resolved file version, or empty if no snapshot metadata is published
     * @throws IOException if the request fails, or the response is not valid {@code maven-metadata.xml}
     * @throws InterruptedException if the request is interrupted
     * @throws NullPointerException if any argument is {@code null}
     */
    public Optional<String> resolveSnapshotFileVersion(String repositoryBaseUrl, ArtifactCoordinates coordinates,
                                                         String snapshotVersion, Credentials credentials)
            throws IOException, InterruptedException {
        Objects.requireNonNull(credentials, "credentials");
        return resolveSnapshotFileVersion(repositoryBaseUrl, coordinates, snapshotVersion, Optional.of(credentials));
    }

    private Optional<String> resolveSnapshotFileVersion(String repositoryBaseUrl, ArtifactCoordinates coordinates,
                                                          String snapshotVersion, Optional<Credentials> credentials)
            throws IOException, InterruptedException {
        Objects.requireNonNull(repositoryBaseUrl, "repositoryBaseUrl");
        Objects.requireNonNull(coordinates, "coordinates");
        Objects.requireNonNull(snapshotVersion, "snapshotVersion");

        URI metadataUri = versionMetadataUri(repositoryBaseUrl, coordinates, snapshotVersion);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(metadataUri).timeout(REQUEST_TIMEOUT).GET();
        credentials.ifPresent(creds -> requestBuilder.header("Authorization", BasicAuth.header(creds)));

        HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 404) {
            LOG.debug("No snapshot maven-metadata.xml for {} {} at {}", coordinates, snapshotVersion, metadataUri);
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP " + response.statusCode() + " fetching " + metadataUri);
        }
        try (InputStream body = response.body()) {
            return parseSnapshotFileVersion(body, metadataUri, snapshotVersion);
        }
    }

    private static URI versionMetadataUri(String repositoryBaseUrl, ArtifactCoordinates coordinates, String version) {
        String base = repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
        return URI.create(base + coordinates.groupPath() + "/" + coordinates.artifactId() + "/" + version + "/maven-metadata.xml");
    }

    private static Document parseXmlDocument(InputStream metadataXml, URI source) throws IOException {
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(metadataXml);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse maven-metadata.xml from " + source, e);
        }
        document.getDocumentElement().normalize();
        return document;
    }

    private static Optional<String> parseSnapshotFileVersion(InputStream metadataXml, URI source, String snapshotVersion) throws IOException {
        Document document = parseXmlDocument(metadataXml, source);

        Optional<String> timestamp = childText(document, "timestamp", "snapshot");
        Optional<String> buildNumber = childText(document, "buildNumber", "snapshot");
        if (snapshotVersion.endsWith("-SNAPSHOT") && timestamp.isPresent() && buildNumber.isPresent()) {
            String base = snapshotVersion.substring(0, snapshotVersion.length() - "-SNAPSHOT".length());
            return Optional.of(base + "-" + timestamp.get() + "-" + buildNumber.get());
        }

        // Fallback for metadata that omits the generic <snapshot> block — take any one
        // <snapshotVersions>/<snapshotVersion>/<value>; a single atomic deploy publishes every
        // classifier under the same resolved file version.
        NodeList valueNodes = document.getElementsByTagName("value");
        for (int i = 0; i < valueNodes.getLength(); i++) {
            Node node = valueNodes.item(i);
            if (node instanceof Element element && "snapshotVersion".equals(parentTagName(element))) {
                String text = element.getTextContent().trim();
                if (!text.isBlank()) {
                    return Optional.of(text);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The text of the first {@code <tagName>} element whose parent is {@code <parentTagName>} —
     * a narrower version of {@link #directChildText} for elements nested one level deeper than
     * {@code <versioning>} (e.g. {@code <timestamp>}/{@code <buildNumber>}, both children of
     * {@code <snapshot>}).
     */
    private static Optional<String> childText(Document document, String tagName, String parentTagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && parentTagName.equals(parentTagName(element))) {
                String text = element.getTextContent().trim();
                if (!text.isBlank()) {
                    return Optional.of(text);
                }
            }
        }
        return Optional.empty();
    }
}
