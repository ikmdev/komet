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

import java.util.List;

/**
 * A plugin-discoverable provider of rule classes for the rule engine.
 *
 * <p>Implementations are discovered by the {@link RuleService} provider via
 * {@code PluggableService.load(RuleProvider.class)}, and the rule classes they
 * return are compiled into the rule knowledge alongside the built-in rules. This
 * lets a plugin contribute rules — and therefore the rule-driven context-menu
 * actions, worthiness decisions, and other behaviour the engine produces —
 * without modifying the engine or requiring a new komet release.
 *
 * <p>Each returned class must be an annotated rule class in the engine's DSL
 * (an Evrete annotated class extending the shared {@code RulesBase}), and the
 * contributing module must be compiled with {@code -parameters} (Evrete binds
 * facts to rule-method parameters by name). The rule package must be opened
 * <em>unqualified</em> ({@code opens <pkg>;}, or an {@code open module}) so the
 * engine can read the rule reflectively via {@code MethodHandles}: a qualified
 * {@code opens <pkg> to org.evrete.dsl.java} is <em>not</em> sufficient and fails
 * at runtime with {@code IllegalAccessException} when the rule method is unreflected.
 * A provider is registered in its {@code module-info} with
 * {@code provides dev.ikm.komet.framework.rulebase.RuleProvider with ...}.
 */
public interface RuleProvider {

    /**
     * The rule classes this provider contributes to the rule knowledge.
     *
     * @return the annotated rule classes to add; never null (may be empty)
     */
    List<Class<?>> ruleClasses();
}
