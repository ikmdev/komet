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
package dev.ikm.tinkar.coordinate.language.calculator;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

public interface LanguageCalculatorDelegate extends LanguageCalculator {
    @Override
    default ImmutableList<LanguageCoordinateRecord> languageCoordinateList() {
        return languageCalculator().languageCoordinateList();
    }

    @Override
    default ImmutableList<SemanticEntity> getDescriptionsForComponent(int componentNid) {
        return languageCalculator().getDescriptionsForComponent(componentNid);
    }

    @Override
    default ImmutableList<SemanticEntityVersion> getDescriptionsForComponentOfType(int componentNid, int descriptionTypeNid) {
        return languageCalculator().getDescriptionsForComponentOfType(componentNid, descriptionTypeNid);
    }

    @Override
    default Optional<String> getRegularDescriptionText(int entityNid) {
        return languageCalculator().getRegularDescriptionText(entityNid);
    }

    @Override
    default Optional<String> getSemanticText(int nid) {
        return languageCalculator().getSemanticText(nid);
    }

    @Override
    default Optional<String> getDescriptionTextForComponentOfType(int entityNid, int descriptionTypeNid) {
        return languageCalculator().getDescriptionTextForComponentOfType(entityNid, descriptionTypeNid);
    }

    @Override
    default Optional<String> getDescriptionText(int componentNid) {
        return languageCalculator().getDescriptionText(componentNid);
    }

    @Override
    default Optional<String> getUserText() {
        return languageCalculator().getUserText();
    }

    @Override
    default Latest<SemanticEntityVersion> getSpecifiedDescription(ImmutableList<SemanticEntity> descriptionList) {
        return languageCalculator().getSpecifiedDescription(descriptionList);
    }

    @Override
    default Optional<String> getTextFromSemanticVersion(SemanticEntityVersion semanticEntityVersion) {
        return languageCalculator().getTextFromSemanticVersion(semanticEntityVersion);
    }

    @Override
    default Latest<SemanticEntityVersion> getSpecifiedDescription(ImmutableList<SemanticEntity> descriptionList, IntIdList descriptionTypePriority) {
        return languageCalculator().getSpecifiedDescription(descriptionList, descriptionTypePriority);
    }

    LanguageCalculator languageCalculator();
}
