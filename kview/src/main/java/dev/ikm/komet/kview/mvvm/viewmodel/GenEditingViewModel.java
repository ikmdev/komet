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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class GenEditingViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(GenEditingViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";
    public static String WINDOW_TOPIC = "windowTopic";
    public static String FIELDS_COLLECTION = "fieldsCollection";
    public static String SEMANTIC = "semantic";
    public static String REF_COMPONENT = "referenceComponent";

    public GenEditingViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, (UUID) null)
                .addProperty(WINDOW_TOPIC, (UUID) null)
                .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                .addProperty(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                .addProperty(REF_COMPONENT, (EntityFacade) null)
                .addProperty(SEMANTIC, (EntityFacade) null);
    }

    public void loadPatternValues(){
    }

    public boolean createSemantic() {
        return false;
    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(VIEW_PROPERTIES);
    }

//    public void updateStamp() {
//        EntityFacade patternFacade = getPropertyValue(PATTERN);
//        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
//
//        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);
//
//        Stamp stamp = stampCalculator.latest(patternFacade).get().stamp();
//        stampViewModel.setValue(STATUS, stamp.state());
//        stampViewModel.setValue(TIME, stamp.time());
//        stampViewModel.setValue(AUTHOR, stamp.author());
//        stampViewModel.setValue(MODULE, stamp.module());
//        stampViewModel.setValue(PATH, stamp.path());
//    }



}
