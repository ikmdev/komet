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
 * A username/password pair for authenticating against a Maven repository.
 *
 * <p>{@code password} is a {@code char[]} rather than a {@code String} so callers can clear
 * it after use. Note this means the generated {@link #equals(Object)}/{@link #hashCode()}
 * compare {@code password} by array reference, not content — this type is not intended for
 * use as a set/map key or for content-based equality checks.
 *
 * @param username the username
 * @param password the password
 */
public record Credentials(String username, char[] password) {

    /**
     * Validates that neither component is {@code null}.
     *
     * @throws NullPointerException if {@code username} or {@code password} is {@code null}
     */
    public Credentials {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
    }
}
