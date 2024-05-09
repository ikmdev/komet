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
package dev.ikm.komet.amplify.lidr.viewmodels;

import dev.ikm.komet.amplify.data.om.STAMPDetail;
import dev.ikm.komet.amplify.data.persistence.STAMPWriter;
import dev.ikm.komet.amplify.lidr.om.LidrRecord;
import dev.ikm.komet.amplify.mvvm.ValidationViewModel;
import dev.ikm.komet.amplify.mvvm.validator.ValidationMessage;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static dev.ikm.komet.amplify.lidr.om.DataModelHelper.*;
import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;


public class ViewModelHelper {
    public static final String VIEW_PROPERTIES = "viewProperties";

    private static final Logger LOG = LoggerFactory.getLogger(ViewModelHelper.class);
    // TODO: Access LIDR PublicIds in a more maintainable way



    public static String findDescrNameText(PublicId publicId) {
        return findDescrNameText(publicId, "");
    }
    public static String findDescrNameText(PublicId publicId, String defaultValue) {
        if (publicId == null) return defaultValue;
        Optional<Entity> entity = EntityService.get().getEntity(publicId.asUuidArray());
        Optional<String> stringOptional = viewPropertiesNode().calculator().getRegularDescriptionText(entity.get().nid());
        return stringOptional.orElse(defaultValue);
    }

    public static PublicId addNewLidrRecord(LidrRecord lidrRecord, PublicId device, ValidationViewModel stampViewModel) {
        if (device == null || lidrRecord == null || stampViewModel == null) {
            throw new RuntimeException("Error Unable to create a LIDR record to the database. lidr record = " + lidrRecord);
        }
        // Generate a new Stamp
        STAMPDetail stampDetail = toStampDetail(stampViewModel);

        // Create a stamp into the database.
        PublicId newStampPublicId = PublicIds.newRandom();
        STAMPWriter stampWriter = new STAMPWriter(newStampPublicId);
        stampWriter.write(stampDetail);

        // Lidr record is written to database. It needs a device and stamp entity.
        return write(lidrRecord, device, newStampPublicId);
    }

    public static STAMPDetail toStampDetail(ValidationViewModel stampViewModel) {
        stampViewModel.save();
        if (stampViewModel.hasErrorMsgs()) {
            StringBuilder sb = new StringBuilder();
            for(ValidationMessage message: stampViewModel.getValidationMessages()) {
                sb.append(message.interpolate(stampViewModel) + "\n");
            }
            throw new RuntimeException("Error(s) with validation message(s)\n" + sb);
        }
        State status = stampViewModel.getValue(STATUS_PROPERTY);
        PublicId statusPublicId = status != null ? status.publicId() : TinkarTerm.ACTIVE_STATE.publicId();
        Concept author = stampViewModel.getValue(AUTHOR_PROPERTY);
        PublicId authorPublicId = author != null ? author.publicId() : TinkarTerm.USER.publicId();
        Long time = stampViewModel.getValue(TIME_PROPERTY);
        long epochMillis = time == null ? System.currentTimeMillis() : time;
        Concept module = stampViewModel.getValue(MODULE_PROPERTY);
        PublicId modulePublicId = module != null ? module.publicId() : TinkarTerm.DEVELOPMENT_MODULE.publicId();
        Concept path = stampViewModel.getValue(PATH_PROPERTY);
        PublicId pathPublicId = path != null ? path.publicId() : TinkarTerm.DEVELOPMENT_PATH.publicId();

        return new STAMPDetail(statusPublicId, epochMillis, authorPublicId, modulePublicId, pathPublicId);

    }

}
