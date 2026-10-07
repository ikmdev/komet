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
package dev.ikm.komet.kview.data.persistence;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;

public class ConceptWriter {

    private final PublicId stamp;

    public ConceptWriter(PublicId stamp) {
        this.stamp = stamp;
    }

    public void write(PublicId concept){
        //Create empty version list
        RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();

        //Every UUID of the PublicId goes into the record header
        PublicIdentifierRecord identifier = PublicIdentifierRecord.make(concept);

        //Assign nid for Concept
        long conceptNid = EntityService.get().nidForPublicId(concept);
        long stampNid = EntityService.get().nidForPublicId(stamp);

        //Create Concept Chronology
        ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                .nid(conceptNid)
                .leastSignificantBits(identifier.leastSignificantBits())
                .mostSignificantBits(identifier.mostSignificantBits())
                .additionalUuidLongs(identifier.additionalUuidLongs())
                .versions(versions)
                .build();

        //Create Concept Version
        versions.add(ConceptVersionRecordBuilder.builder()
                .chronology(conceptRecord)
                .stampNid(stampNid)
                .build());

        //Rebuild the ConceptRecord with the now populated version data
        ConceptEntity<? extends ConceptEntityVersion> conceptEntity = ConceptRecordBuilder.builder(conceptRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(conceptEntity);
    }
}
