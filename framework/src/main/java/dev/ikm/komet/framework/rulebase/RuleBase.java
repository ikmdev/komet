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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.komet.framework.performance.StatementStore;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class RuleBase {
    private static final Logger LOG = LoggerFactory.getLogger(RuleBase.class);

    private static RuleBase singleton;

    private final ImmutableList<Rule> rules;

    private RuleBase() {
        try (ScanResult scanResult =                // Assign scanResult in try-with-resources
                     new ClassGraph()                    // Create a new ClassGraph instance
                             //.verbose()                      // If you want to enable logging to stderr
                             .enableAllInfo()                // Scan classes, methods, fieldValues, annotations
                             .acceptPackages("dev.ikm.komet.framework.rulebase")      // Scan dev.ikm.komet.framework.rulebase and subpackages
                             .scan()) {                      // Perform the scan and return a ScanResult
            // Use the ScanResult within the try block, e.g.
            ClassInfoList rulesClassInfoList = scanResult.getClassesWithAnnotation(RuntimeRule.class);
            MutableList<Rule> mutableRules = Lists.mutable.empty();
            rulesClassInfoList.loadClasses().forEach(aClass -> {
                try {
                    mutableRules.add((Rule) aClass.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    LOG.error("Error loading rules.", e);
                }
            });
            this.rules = mutableRules.toImmutable();
        }
    }

    public static ImmutableList<Consequence<?>> execute(StatementStore statementStore, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        if (RuleBase.singleton == null) {
            RuleBase.singleton = new RuleBase();
        }
        MutableList<Consequence<?>> consequences = Lists.mutable.empty();
        singleton.rules.forEach(rule -> {
            consequences.addAll(rule.execute(statementStore, viewCalculator, editCoordinate).castToList());
        });
        return consequences.toImmutable();
    }
}
