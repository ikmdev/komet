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
package dev.ikm.komet.kview.mvvm.model;

import one.jpro.platform.auth.core.basic.InMemoryUserManager;
import one.jpro.platform.auth.core.basic.UsernamePasswordCredentials;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BasicUserManager extends InMemoryUserManager {

    public BasicUserManager() {
        super();
    }

    public CompletableFuture<Set<UsernamePasswordCredentials>> loadFileAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            final Set<UsernamePasswordCredentials> credentialsSet = ConcurrentHashMap.newKeySet();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                String currentSection = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("#")) {
                        continue; // Skip empty lines and comments
                    }

                    if (line.startsWith("[") && line.endsWith("]")) {
                        currentSection = line.substring(1, line.length() - 1);
                    } else if (currentSection != null) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();

                            if (currentSection.equalsIgnoreCase("users")) {
                                // Parse users: username = password, role1, role2
                                String[] userParts = value.split(",");
                                if (userParts.length >= 1) { // Ensure at least password is present
                                    String password = userParts[0].trim();
                                    Set<String> userRoles = new HashSet<>();
                                    for (int i = 1; i < userParts.length; i++) {
                                        userRoles.add(userParts[i].trim());
                                    }

                                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(key, password);
                                    createUser(credentials, userRoles, Map.of("enabled", Boolean.TRUE));
                                    credentialsSet.add(credentials);
                                }
                            }
                        }
                    }
                }
                return credentialsSet;
            } catch (IOException ex) {
                // Wrap IOException in a RuntimeException to let CompletableFuture handle it
                throw new RuntimeException("Error reading file: " + filePath, ex);
            }
        });
    }
}
