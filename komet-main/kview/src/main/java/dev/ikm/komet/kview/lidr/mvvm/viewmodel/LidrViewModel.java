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
package dev.ikm.komet.kview.lidr.mvvm.viewmodel;

import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class LidrViewModel extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(LidrViewModel.class);

    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String DEVICE_ENTITY = "deviceEntity";
    public static String MANUFACTURER_ENTITY = "mfgEntity";
    public static String STAMP_VIEW_MODEL = "stampViewModel";
    public static String RESULT_INTERPRETATIONS = "resultInterpretations"; // Analyte groups added.


    public LidrViewModel() {
        super(); // addProperty(MODE, VIEW); By default
        addProperty(CONCEPT_TOPIC, (UUID) null)
                .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(DEVICE_ENTITY, (EntityFacade) null)
                .addProperty(MANUFACTURER_ENTITY, (EntityFacade) null)
                .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                .addProperty(RESULT_INTERPRETATIONS, (List<SemanticEntity>) null);
    }

    /**
     * Validates the view model and if there are no errors, save to the database.
     * Adds semantic (result) to Device.
     *
     * @param editCoordinate
     * @return
     */
    public boolean addResult(EditCoordinateRecord editCoordinate) {
        save(); // View Model xfer values. does not save to the database but validates data and then copies data from properties to model values.

        // Validation errors will not create record.
        if (hasErrorMsgs()) {
            return false;
        }

        // stamp exists and is populated?
        StampViewModel stampViewModel = getValue(STAMP_VIEW_MODEL);
        if (stampViewModel != null) {
            stampViewModel.save(); // View Model xfer values
            if (hasErrorMsgs()) {
                return false;
            }
        } else {
            return false;
        }

//        Entity.provider().putEntity(conceptRecord);
//
//        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
//        TinkExecutor.threadPool().submit(commitTransactionTask);
//        // place inside as current Concept
//        setValue(CURRENT_ENTITY, conceptFacade);
//        setPropertyValue(CURRENT_ENTITY, conceptFacade);
//        setValue(MODE, EDIT);
//        setPropertyValue(MODE, EDIT);
        return true;
    }
}