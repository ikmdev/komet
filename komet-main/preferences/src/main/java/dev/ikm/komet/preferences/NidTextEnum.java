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
package dev.ikm.komet.preferences;

import java.util.Optional;

public enum NidTextEnum {

    NID_TEXT,
    SEMANTIC_ENTITY;

    /**
     * Returns the enum constant of this type with the specified name.
     * The string must match exactly an identifier used to declare an
     * enum constant in this type.
     *
     * @param name the name of the enum constant to be returned.
     * @return Optional containing the enum constant if found, or empty Optional if not found.
     */
    public static Optional<NidTextEnum> fromString(String name) {
        try {
            return Optional.of(valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return Optional.empty();
        }
    }
}