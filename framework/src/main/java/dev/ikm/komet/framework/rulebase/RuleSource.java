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
package dev.ikm.komet.framework.rulebase;

import java.util.Objects;
import java.util.UUID;

/**
 * Rules written while Komet runs: the Java source of one class of rules, with the identity and
 * the name it is listed under.
 *
 * <p>The source is written as Komet's own rule classes are: one public class, each rule a
 * {@code void} method, each of its conditions a {@code boolean} method named in the rule's
 * annotation. The {@link RuleService} compiles it when it is put in use, against the types
 * Komet itself is built on.
 *
 * @param id   the identity, which stays the same as the source is edited
 * @param name the name the rules are listed under
 * @param java the Java source text
 */
public record RuleSource(UUID id, String name, String java) {

    /**
     * Refuses a rule source that lacks an identity, a name or its text.
     *
     * @param id   the identity, which stays the same as the source is edited
     * @param name the name the rules are listed under
     * @param java the Java source text
     */
    public RuleSource {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(java, "java");
    }
}
