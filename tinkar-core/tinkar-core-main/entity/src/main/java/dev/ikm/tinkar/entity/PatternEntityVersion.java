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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.component.PatternVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.list.ImmutableList;

public interface PatternEntityVersion extends EntityVersion, PatternVersion {
    default <T> T getFieldWithMeaning(ConceptFacade fieldMeaning, SemanticEntityVersion version) {
        return (T) version.fieldValues().get(indexForMeaning(fieldMeaning));
    }

    default int indexForMeaning(ConceptFacade meaning) {
        return indexForMeaning(meaning.nid());
    }

    // TODO: should allow more than one index for meaning?
    // TODO: Note the stamp calculator caches these indexes. Consider how to optimize, and eliminate unoptimized calls?
    default int indexForMeaning(int meaningNid) {
        for (int i = 0; i < fieldDefinitions().size(); i++) {
            if (fieldDefinitions().get(i).meaningNid() == meaningNid) {
                return i;
            }
        }
        return -1;
    }

    @Override
    ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions();

    @Override
    default ConceptEntity semanticPurpose() {
        return EntityService.get().getEntityFast(semanticPurposeNid());
    }

    int semanticPurposeNid();

    @Override
    default ConceptEntity semanticMeaning() {
        return EntityService.get().getEntityFast(semanticMeaningNid());
    }

    int semanticMeaningNid();

    default <T> T getFieldWithPurpose(ConceptFacade fieldPurpose, SemanticEntityVersion version) {
        return (T) version.fieldValues().get(indexForPurpose(fieldPurpose));
    }

    default int indexForPurpose(ConceptFacade purpose) {
        return indexForPurpose(purpose.nid());
    }

    // TODO: should allow more than one index for purpose?
    // TODO: Note the stamp calculator caches these indexes. Consider how to optimize, and eliminate unoptimized calls?
    default int indexForPurpose(int purposeNid) {
        for (int i = 0; i < fieldDefinitions().size(); i++) {
            if (fieldDefinitions().get(i).purposeNid() == purposeNid) {
                return i;
            }
        }
        return -1;
    }

}
