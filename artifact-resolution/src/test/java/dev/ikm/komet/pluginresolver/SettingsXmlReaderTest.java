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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsXmlReaderTest {

    @Test
    void parsesMirrorsRepositoriesAndPlaintextServers(@TempDir Path dir) throws IOException {
        Path settingsXml = writeSettings(dir, """
                <settings>
                    <localRepository>/custom/repo</localRepository>
                    <mirrors>
                        <mirror>
                            <id>central-mirror</id>
                            <url>https://mirror.example.com/repo</url>
                            <mirrorOf>*</mirrorOf>
                        </mirror>
                    </mirrors>
                    <servers>
                        <server>
                            <id>internal-nexus</id>
                            <username>build-user</username>
                            <password>plain-secret</password>
                        </server>
                    </servers>
                    <repositories>
                        <repository>
                            <id>top-level-repo</id>
                            <url>https://repo.example.com/releases</url>
                        </repository>
                    </repositories>
                </settings>
                """);

        SettingsXmlReader.Settings settings = SettingsXmlReader.read(settingsXml);

        assertEquals(Path.of("/custom/repo"), settings.localRepository().orElseThrow());
        assertEquals(1, settings.mirrors().size());
        assertEquals(new SettingsXmlReader.Mirror("central-mirror", "https://mirror.example.com/repo", "*"),
                settings.mirrors().getFirst());
        assertEquals(1, settings.servers().size());
        assertEquals("build-user", settings.servers().getFirst().username());
        assertEquals("plain-secret", settings.servers().getFirst().plaintextPassword());
        assertEquals(1, settings.repositories().size());
        assertEquals(new SettingsXmlReader.RemoteRepositoryDescriptor("top-level-repo", "https://repo.example.com/releases"),
                settings.repositories().getFirst());
    }

    @Test
    void encryptedServerPasswordIsReportedAsAbsentNotDecrypted(@TempDir Path dir) throws IOException {
        Path settingsXml = writeSettings(dir, """
                <settings>
                    <servers>
                        <server>
                            <id>encrypted-server</id>
                            <username>someone</username>
                            <password>{COQLCE6DU6GtcS5P=}</password>
                        </server>
                    </servers>
                </settings>
                """);

        SettingsXmlReader.Settings settings = SettingsXmlReader.read(settingsXml);

        assertEquals(1, settings.servers().size());
        assertEquals("someone", settings.servers().getFirst().username());
        assertNull(settings.servers().getFirst().plaintextPassword());
    }

    @Test
    void missingSectionsYieldEmptyLists(@TempDir Path dir) throws IOException {
        Path settingsXml = writeSettings(dir, "<settings/>");

        SettingsXmlReader.Settings settings = SettingsXmlReader.read(settingsXml);

        assertTrue(settings.localRepository().isEmpty());
        assertTrue(settings.mirrors().isEmpty());
        assertTrue(settings.servers().isEmpty());
        assertTrue(settings.repositories().isEmpty());
    }

    @Test
    void includesRepositoriesFromActiveByDefaultAndExplicitlyActiveProfilesOnly(@TempDir Path dir) throws IOException {
        Path settingsXml = writeSettings(dir, """
                <settings>
                    <activeProfiles>
                        <activeProfile>explicitly-active</activeProfile>
                    </activeProfiles>
                    <profiles>
                        <profile>
                            <id>default-active</id>
                            <activation>
                                <activeByDefault>true</activeByDefault>
                            </activation>
                            <repositories>
                                <repository>
                                    <id>default-active-repo</id>
                                    <url>https://repo.example.com/default-active</url>
                                </repository>
                            </repositories>
                        </profile>
                        <profile>
                            <id>explicitly-active</id>
                            <repositories>
                                <repository>
                                    <id>explicit-repo</id>
                                    <url>https://repo.example.com/explicit</url>
                                </repository>
                            </repositories>
                        </profile>
                        <profile>
                            <id>inactive</id>
                            <repositories>
                                <repository>
                                    <id>inactive-repo</id>
                                    <url>https://repo.example.com/inactive</url>
                                </repository>
                            </repositories>
                        </profile>
                    </profiles>
                </settings>
                """);

        SettingsXmlReader.Settings settings = SettingsXmlReader.read(settingsXml);

        assertEquals(2, settings.repositories().size());
        assertTrue(settings.repositories().stream().anyMatch(r -> r.id().equals("default-active-repo")));
        assertTrue(settings.repositories().stream().anyMatch(r -> r.id().equals("explicit-repo")));
        assertTrue(settings.repositories().stream().noneMatch(r -> r.id().equals("inactive-repo")));
    }

    private static Path writeSettings(Path dir, String xml) throws IOException {
        Path settingsXml = dir.resolve("settings.xml");
        Files.writeString(settingsXml, xml);
        return settingsXml;
    }
}
