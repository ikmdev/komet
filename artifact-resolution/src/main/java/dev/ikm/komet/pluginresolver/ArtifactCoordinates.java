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

import java.util.Objects;

/**
 * A Maven groupId:artifactId coordinate, identifying an artifact independent of version.
 *
 * @param groupId the Maven group id, e.g. {@code "network.ike.komet"}
 * @param artifactId the Maven artifact id, e.g. {@code "komet-claude-plugin"}
 */
public record ArtifactCoordinates(String groupId, String artifactId) {

    /**
     * Validates that neither component is {@code null}.
     *
     * @throws NullPointerException if {@code groupId} or {@code artifactId} is {@code null}
     */
    public ArtifactCoordinates {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(artifactId, "artifactId");
    }

    /**
     * The {@code groupId} with {@code .} replaced by the platform path separator, used to
     * locate this artifact's directory under a local or remote Maven repository root
     * (e.g. {@code network/ike/komet}).
     *
     * @return the groupId rewritten as a repository-relative path
     */
    public String groupPath() {
        return groupId.replace('.', '/');
    }

    /**
     * The {@code groupId:artifactId} display form.
     *
     * @return the colon-joined groupId and artifactId
     */
    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }
}
