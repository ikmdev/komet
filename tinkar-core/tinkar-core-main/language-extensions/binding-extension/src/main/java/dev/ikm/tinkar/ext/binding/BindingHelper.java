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
package dev.ikm.tinkar.ext.binding;

import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import org.eclipse.collections.api.factory.Lists;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to provide common Tinkar calculator based data retrieval capabilities
 */
public class BindingHelper {

    private final LanguageCalculator languageCalculator;
    private final StampCalculator stampCalculator;
    private final Function<String, String> specialCharacterHandler;


    public BindingHelper(LanguageCalculator languageCalculator, StampCalculator stampCalculator, Function<String, String> specialCharacterHandler) {
        this.languageCalculator = languageCalculator;
        this.stampCalculator = stampCalculator;
        this.specialCharacterHandler = specialCharacterHandler;
    }

    public BindingHelper() {
        this(LanguageCalculatorWithCache.getCalculator(
                Coordinates.Stamp.DevelopmentLatest(),
                Lists.mutable.of(Coordinates.Language.UsEnglishFullyQualifiedName()).toImmutableList()),
                StampCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatest()),
                fqn -> {
                    Matcher matcher = Pattern.compile("[^a-zA-Z0-9\\s]").matcher(fqn);
                    while(matcher.find()) {
                        String s = matcher.group();
                        fqn = fqn.replace(s, "");
                    }
                    return fqn;
                });
    }

    /**
     * Get text of Tinkar component
     * @param nid Native Identifier of Tinkar component
     * @return Text of Tinkar component
     */
    public String getText(int nid) {
        return specialCharacterHandler.apply(languageCalculator.getDescriptionText(nid).orElse(""));
    }

    /**
     * Create Text for component and format it to align to Java style static variable naming
     * @param nid Native Identifier of Tinkar component
     * @return Variable Name (java style)
     */
    public String createVariableName(int nid) {
        return getText(nid).replace(" ", "_").toUpperCase().replace("(", "").replace(")", "");
    }

    /**
     * Create Public Id string of an Entity
     * @param entity Tinkar Entity
     * @return Formatted Public Id string to be used in Java style declaration
     */
    public String createPublicId(Entity<? extends EntityVersion> entity) {
        return entity.idString().replace("[", "").replace("]", "");
    }

    /**
     * Get Pattern Field Definitions for Pattern
     * @param nid Native Identifier
     * @return List of Pattern Definitions
     */
    public List<? extends FieldDefinitionForEntity> getPatternFieldDefinitions(int nid) {
        PatternEntityVersion latestVersion = stampCalculator.latestPatternEntityVersion(nid).get();
        return latestVersion.fieldDefinitions().stream().toList();
    }

}
