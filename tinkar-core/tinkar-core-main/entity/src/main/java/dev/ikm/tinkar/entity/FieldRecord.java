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
import dev.ikm.tinkar.component.PatternVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * TODO: create an entity data type that combines concept and FieldDataType like the Status enum?
 * @param <DT>
 * @param nid
 * @param versionStampNid
 */
@RecordBuilder
public record FieldRecord<DT>(DT value, int nid, int versionStampNid,
                              int patternNid, int indexInPattern)
        implements Field<DT>, FieldRecordBuilder.With {


    public FieldRecord {
        Validator.notZero(nid);
        Validator.notZero(versionStampNid);
        Validator.notZero(patternNid);
        if (indexInPattern < 0) throw new IllegalStateException("Index must be >= 0");
    }

    @Override
    public FieldDefinition fieldDefinition(StampCalculator stampCalculator) {
        PatternEntity<PatternEntityVersion> patternEntity = Entity.getFast(patternNid());
        Latest<PatternEntityVersion> patternVersion = stampCalculator.latest(patternEntity);
        return patternVersion.get().fieldDefinitions().get(indexInPattern());
    }

    public FieldRecord<DT> with(DT value) {
        return withValue(value);
    }


    @Override
    public String toString() {
        return "FieldRecord{value: " + value +
                ", for entity: " + PrimitiveData.textWithNid(nid) +
                " of version: " + Entity.getStamp(versionStampNid).lastVersion().describe() +
                " in pattern: " + Entity.getFast(patternNid()) +
                " with index: " + indexInPattern() +
                '}';
    }

}
