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

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.Validator;
import dev.ikm.tinkar.component.FieldDefinition;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record FieldDefinitionRecord(int dataTypeNid, int purposeNid, int meaningNid, int patternVersionStampNid,
                                    int patternNid, int indexInPattern)
        implements FieldDefinitionForEntity, FieldDefinitionRecordBuilder.With, FieldDefinition {

    public FieldDefinitionRecord {
        Validator.notZero(dataTypeNid);
        Validator.notZero(purposeNid);
        Validator.notZero(meaningNid);
        Validator.notZero(patternVersionStampNid);
        Validator.notZero(patternNid);
    }
    public FieldDefinitionRecord(FieldDefinition fieldDefinition, PatternEntityVersion patternVersion, int indexInPattern) {
        this(Entity.nid(fieldDefinition.dataType()),
                Entity.nid(fieldDefinition.purpose()),
                Entity.nid(fieldDefinition.meaning()),
                patternVersion.stampNid(),
                patternVersion.nid(),
                indexInPattern
        );
    }

    public FieldDefinitionRecord(FieldDefinition fieldDefinition, int patternVersionStampNid, int patternNid, int indexInPattern) {
        this(Entity.nid(fieldDefinition.dataType()),
                Entity.nid(fieldDefinition.purpose()),
                Entity.nid(fieldDefinition.meaning()),
                patternVersionStampNid,
                patternNid,
                indexInPattern
        );
    }

    @Override
    public String toString() {
        return "FieldDefinitionRecord{" +
                "dataType: " + PrimitiveData.text(dataTypeNid) +
                ", purpose: " + PrimitiveData.text(purposeNid) +
                ", meaning: " + PrimitiveData.text(meaningNid) +
                ", for pattern version: " + Entity.getStamp(patternVersionStampNid).lastVersion().describe() +
                ", for pattern: " + PrimitiveData.text(patternNid) + " [" + indexInPattern +
                "]}";
    }

}
