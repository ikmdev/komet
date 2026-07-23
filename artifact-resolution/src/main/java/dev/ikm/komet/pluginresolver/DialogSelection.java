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

/**
 * A remembered "Add from Maven" dialog configuration for one download flow (e.g. a SpinedArray
 * store snapshot vs. a protobuf changeset) — what the dialog restores on a repeat visit, except
 * the password itself, which stays in {@link RepositoryCredentialStore}.
 *
 * <p>Deliberately does <em>not</em> carry the last-used groupId/artifactId. The dialog opens with
 * empty coordinates by design: it must never name — or, with Download enabled, offer — an
 * artifact the user hasn't chosen in this session. Only the repository (and, via
 * {@link RepositoryCredentialStore}, its credentials) is restored, since that's configuration
 * rather than a claim about what's being downloaded.
 *
 * @param repositoryUrl the last-used repository URL
 */
public record DialogSelection(String repositoryUrl) {
}
