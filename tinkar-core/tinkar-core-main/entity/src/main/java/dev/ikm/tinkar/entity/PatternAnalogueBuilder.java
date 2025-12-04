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

import dev.ikm.tinkar.component.FieldDefinition;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public record PatternAnalogueBuilder(PatternRecord analogue,
                                     RecordListBuilder<PatternVersionRecord> analogVersions) {
    /**
     * If there is a version with the same stamp as versionToAdd, it will be removed prior to adding the
     * new version so you don't get duplicate versions with the same stamp.
     *
     * @param versionToAdd
     * @return
     */
    public PatternAnalogueBuilder with(PatternEntityVersion versionToAdd) {
        return add(versionToAdd);
    }

    /**
     * If there is a version with the same stamp as versionToAdd, it will be removed prior to adding the
     * new version so you don't get duplicate versions with the same stamp.
     *
     * @param versionToAdd
     * @return
     */
    public PatternAnalogueBuilder add(PatternEntityVersion versionToAdd) {
        remove(versionToAdd);
        int fieldDefinitionCount = versionToAdd.fieldDefinitions().size();
        MutableList<FieldDefinitionRecord> fieldDefinitionRecords = Lists.mutable.ofInitialCapacity(versionToAdd.fieldDefinitions().size());
        for (int i = 0; i < fieldDefinitionCount; i++) {
            FieldDefinition fieldDefinition = versionToAdd.fieldDefinitions().get(i);
            if (fieldDefinition instanceof FieldDefinitionRecord fieldDefinitionRecord) {
                fieldDefinitionRecords.add(fieldDefinitionRecord);
            } else {
                fieldDefinitionRecords.add(new FieldDefinitionRecord(Entity.nid(fieldDefinition.dataType()),
                        Entity.nid(fieldDefinition.purpose()),
                        Entity.nid(fieldDefinition.meaning()),
                        versionToAdd.stampNid(), versionToAdd.entity().nid(), i));
            }
        }
        analogVersions.add(new PatternVersionRecord(analogue, versionToAdd.stampNid(),
                versionToAdd.semanticPurposeNid(), versionToAdd.semanticMeaningNid(), fieldDefinitionRecords.toImmutable()));
        return this;
    }

    /**
     * Removal is based on equivalence of stampNid, not based on a deep equals.
     *
     * @param versionToRemove
     * @return
     */
    public PatternAnalogueBuilder remove(PatternEntityVersion versionToRemove) {
        analogVersions.removeIf(patternVersionRecord -> patternVersionRecord.stampNid() == versionToRemove.stampNid());
        return this;
    }

    /**
     * Removal is based on equivalence of stampNid, not based on a deep equals.
     *
     * @param versionToRemove
     * @return
     */
    public PatternAnalogueBuilder without(PatternEntityVersion versionToRemove) {
        return remove(versionToRemove);
    }

    public PatternRecord build() {
        analogVersions.build();
        return analogue;
    }
}
