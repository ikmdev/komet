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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Structural-only reader for a Maven {@code settings.xml} file: mirrors, repositories, and
 * server credentials whose password is stored in plain text.
 *
 * <p>Deliberately does <strong>not</strong> decrypt server passwords wrapped in Maven's
 * {@code {...}} encrypted-value marker — doing so would require depending on whichever
 * master-source a given developer machine happens to have configured (a local passphrase
 * file, {@code gpg-agent}, the 1Password CLI, ...), which varies per machine and would make
 * repository-credential resolution non-deterministic. A server whose password is encrypted
 * is reported with a {@code null} {@link ServerCredential#plaintextPassword()} — callers
 * should fall back to a {@link RepositoryCredentialStore} for that repository id, not attempt
 * decryption.
 */
public final class SettingsXmlReader {

    private SettingsXmlReader() {
    }

    /**
     * A {@code <mirror>} entry.
     *
     * @param id the mirror id
     * @param url the mirror's repository URL
     * @param mirrorOf the {@code mirrorOf} pattern (e.g. {@code "*"}, {@code "central"})
     */
    public record Mirror(String id, String url, String mirrorOf) {
    }

    /**
     * A {@code <repository>} entry, from either the top-level list or an active profile.
     *
     * @param id the repository id
     * @param url the repository's base URL
     */
    public record RemoteRepositoryDescriptor(String id, String url) {
    }

    /**
     * A {@code <server>} entry. {@code plaintextPassword} is {@code null} when the entry
     * has no password, or when its password is Maven-encrypted (see class Javadoc).
     *
     * @param id the server id, matched against a repository's id
     * @param username the username, or {@code null} if absent
     * @param plaintextPassword the password, or {@code null} if absent or encrypted
     */
    public record ServerCredential(String id, String username, String plaintextPassword) {
    }

    /**
     * The structural contents of a {@code settings.xml} file.
     *
     * @param localRepository the {@code <localRepository>} override, if present
     * @param mirrors all {@code <mirror>} entries
     * @param repositories all {@code <repository>} entries from the top level and from
     *         active profiles (default-active, or listed in {@code <activeProfiles>})
     * @param servers all {@code <server>} entries
     */
    public record Settings(Optional<Path> localRepository, List<Mirror> mirrors,
                            List<RemoteRepositoryDescriptor> repositories, List<ServerCredential> servers) {
    }

    /**
     * Parses a {@code settings.xml} file.
     *
     * @param settingsXmlPath path to the {@code settings.xml} file
     * @return the parsed structural contents
     * @throws IOException if the file cannot be read or is not well-formed XML
     * @throws NullPointerException if {@code settingsXmlPath} is {@code null}
     */
    public static Settings read(Path settingsXmlPath) throws IOException {
        Objects.requireNonNull(settingsXmlPath, "settingsXmlPath");
        Document document = parse(settingsXmlPath);
        Element root = document.getDocumentElement();

        Optional<Path> localRepository = firstChildText(root, "localRepository").map(Path::of);
        List<Mirror> mirrors = readMirrors(root);
        List<ServerCredential> servers = readServers(root);
        List<RemoteRepositoryDescriptor> repositories = readActiveRepositories(root);

        return new Settings(localRepository, mirrors, repositories, servers);
    }

    private static Document parse(Path settingsXmlPath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Harden against XXE: no external entities, no DOCTYPE.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream in = Files.newInputStream(settingsXmlPath)) {
                Document document = builder.parse(in);
                document.getDocumentElement().normalize();
                return document;
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse " + settingsXmlPath, e);
        }
    }

    private static List<Mirror> readMirrors(Element root) {
        List<Mirror> mirrors = new ArrayList<>();
        for (Element mirror : childElements(firstChildElement(root, "mirrors"), "mirror")) {
            mirrors.add(new Mirror(
                    firstChildText(mirror, "id").orElse(null),
                    firstChildText(mirror, "url").orElse(null),
                    firstChildText(mirror, "mirrorOf").orElse(null)));
        }
        return List.copyOf(mirrors);
    }

    private static List<ServerCredential> readServers(Element root) {
        List<ServerCredential> servers = new ArrayList<>();
        for (Element server : childElements(firstChildElement(root, "servers"), "server")) {
            String id = firstChildText(server, "id").orElse(null);
            String username = firstChildText(server, "username").orElse(null);
            String password = firstChildText(server, "password").orElse(null);
            servers.add(new ServerCredential(id, username, isEncrypted(password) ? null : password));
        }
        return List.copyOf(servers);
    }

    private static boolean isEncrypted(String password) {
        if (password == null) {
            return false;
        }
        String trimmed = password.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    private static List<RemoteRepositoryDescriptor> readActiveRepositories(Element root) {
        Set<String> activeProfileIds = new HashSet<>();
        for (Element activeProfile : childElements(firstChildElement(root, "activeProfiles"), "activeProfile")) {
            activeProfileIds.add(activeProfile.getTextContent().trim());
        }

        List<RemoteRepositoryDescriptor> repositories = new ArrayList<>();
        repositories.addAll(readRepositories(firstChildElement(root, "repositories")));

        for (Element profile : childElements(firstChildElement(root, "profiles"), "profile")) {
            String profileId = firstChildText(profile, "id").orElse(null);
            boolean activeByDefault = firstChildElement(profile, "activation")
                    .flatMap(activation -> firstChildText(activation, "activeByDefault"))
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            boolean explicitlyActive = profileId != null && activeProfileIds.contains(profileId);
            if (activeByDefault || explicitlyActive) {
                repositories.addAll(readRepositories(firstChildElement(profile, "repositories")));
            }
        }
        return List.copyOf(repositories);
    }

    private static List<RemoteRepositoryDescriptor> readRepositories(Optional<Element> repositoriesElement) {
        List<RemoteRepositoryDescriptor> repositories = new ArrayList<>();
        for (Element repository : childElements(repositoriesElement, "repository")) {
            repositories.add(new RemoteRepositoryDescriptor(
                    firstChildText(repository, "id").orElse(null),
                    firstChildText(repository, "url").orElse(null)));
        }
        return repositories;
    }

    private static Optional<Element> firstChildElement(Node parent, String tagName) {
        if (parent == null) {
            return Optional.empty();
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && tagName.equals(element.getTagName())) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> firstChildText(Node parent, String tagName) {
        return firstChildElement(parent, tagName).map(Node::getTextContent).map(String::trim);
    }

    private static List<Element> childElements(Optional<Element> parent, String tagName) {
        if (parent.isEmpty()) {
            return List.of();
        }
        List<Element> matches = new ArrayList<>();
        NodeList children = parent.get().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && tagName.equals(element.getTagName())) {
                matches.add(element);
            }
        }
        return matches;
    }
}
