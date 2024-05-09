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
package dev.ikm.komet.amplify.data.persistence;

import dev.ikm.komet.amplify.data.om.STAMPDetail;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;

import java.util.UUID;

public class STAMPWriter implements Writer {

    public final PublicId stamp;

    public STAMPWriter(PublicId stamp) {
        this.stamp = stamp;
    }

    public void write(STAMPDetail stampDetail){
        //Create empty version list
        RecordListBuilder<StampVersionRecord> versions = RecordListBuilder.make();

        //Pull out primordial UUID from PublicId
        UUID primordialUUID = stamp.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(stamp);

        //Assign nids for STAMP component Concepts
        int stampNid = EntityService.get().nidForUuids(primordialUUID);
        int statusNid = EntityService.get().nidForPublicId(stampDetail.status());
        int authorNid = EntityService.get().nidForPublicId(stampDetail.author());
        int moduleNid = EntityService.get().nidForPublicId(stampDetail.module());
        int pathNid = EntityService.get().nidForPublicId(stampDetail.path());

        //Create STAMP Chronology
        StampRecord stampRecord = StampRecordBuilder.builder()
                .nid(stampNid)
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions)
                .build();

        //Create STAMP Version
        versions.add(StampVersionRecordBuilder.builder()
                .chronology(stampRecord)
                .stateNid(statusNid)
                .time(stampDetail.time())
                .authorNid(authorNid)
                .moduleNid(moduleNid)
                .pathNid(pathNid)
                .build());

        //Rebuild the StampRecord with the now populated version data
        StampEntity<? extends StampEntityVersion> stampEntity = StampRecordBuilder.builder(stampRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(stampEntity);
    }
}
