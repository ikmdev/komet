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

import java.util.Optional;

/**
 * Stores {@link Credentials} for Maven repositories, keyed by repository id.
 *
 * <p>This is Komet's own credential store, independent of {@code ~/.m2/settings.xml} —
 * it never depends on Maven's pluggable master-source ecosystem (a local passphrase file,
 * {@code gpg-agent}, the 1Password CLI, ...) at runtime, since that varies per developer
 * machine and would make repository resolution non-deterministic across machines. See
 * {@link SettingsXmlReader} for the (structural-only, no-decryption) alternative that reads
 * {@code settings.xml} directly.
 */
public interface RepositoryCredentialStore {

    /**
     * Looks up stored credentials for a repository.
     *
     * @param repositoryId the repository id to look up
     * @return the stored credentials, or empty if none are stored for this repository
     */
    Optional<Credentials> get(String repositoryId);

    /**
     * Stores credentials for a repository, replacing any previously stored credentials for
     * the same repository id.
     *
     * @param repositoryId the repository id to store credentials under
     * @param credentials the credentials to store
     */
    void put(String repositoryId, Credentials credentials);

    /**
     * Removes any stored credentials for a repository. A no-op if none are stored.
     *
     * @param repositoryId the repository id to remove credentials for
     */
    void remove(String repositoryId);
}
