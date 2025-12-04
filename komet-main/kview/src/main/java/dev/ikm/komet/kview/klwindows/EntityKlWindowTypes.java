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
package dev.ikm.komet.kview.klwindows;

/**
 * Standard enumeration of window types available in the Komet workspace.
 * <p>
 * This enum implements the {@link EntityKlWindowType} interface, providing
 * built-in window categories with predefined identifier prefixes. Each window type
 * represents a distinct functional category of window used for specific
 * tasks within the application.
 * <p>
 * These window types are automatically registered with the {@link EntityKlWindowType.Registry}
 * during initialization and can be looked up using their enum name, prefix, or
 * string representation.
 *
 * @see EntityKlWindowType
 * @see EntityKlWindowType.Registry
 */
public enum EntityKlWindowTypes implements EntityKlWindowType {

    /**
     * Window type for concept windows.
     */
    CONCEPT("concept_"),

    /**
     * Window type for pattern windows.
     */
    PATTERN("pattern_"),

    /**
     * Window type for pattern-semantic windows.
     */
    GEN_EDITING("gen_editing_"),

    /**
     * Window type for LIDR windows.
     */
    LIDR("lidr_");

    /**
     * The unique identifier prefix for this window type.
     * <p>
     * This prefix is used to generate unique identifiers for windows of this type
     * and to associate persisted window configurations with their appropriate type
     * during workspace restoration.
     */
    private final String prefix;

    /**
     * Constructs a window type with the specified identifier prefix.
     *
     * @param prefix the unique prefix string to use for generating window identifiers
     *               of this type (should end with an underscore for consistency)
     */
    EntityKlWindowTypes(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return name();
    }
}
