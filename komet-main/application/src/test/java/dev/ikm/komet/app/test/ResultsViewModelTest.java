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
package dev.ikm.komet.app.test;


import dev.ikm.komet.kview.data.schema.STAMPDetail;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultsViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsViewModelTest.class);

    //@BeforeAll
    public static void setUpBefore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/snomed+loinc+lidr_int_2024-05-02_reasoned");
        CachingService.clearAll();
        LOG.info("Setup Ephemeral Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");

        PrimitiveData.start();
    }

    //@AfterAll
    public static void tearDownAfter() {
        PrimitiveData.stop();
    }

    public static void main(String[] args) {
        PublicId createdPublicId = PublicIds.newRandom();
        String owlExpression = ViewModelHelper.generateOwlResultConformanceExpression(ViewModelHelper.generateResultConformanceValueMap(createdPublicId, ORDINAL_CONCEPT.publicId()));
        System.out.println(owlExpression);

        Platform.startup(() ->{
            setUpBefore();
            try {
                ResultsViewModelTest testHarness = new ResultsViewModelTest();

                // Testing writers
                PublicId resultConfQuPublicId = testHarness.createResultConformanceQualitativeWithWriters();
                testHarness.displayResultConformanceQualitative(resultConfQuPublicId);

            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
    }

    public PublicId createResultConformanceQualitativeWithWriters() {
        ResultsViewModel resultsViewModel = new ResultsViewModel();
        resultsViewModel
                .setPropertyValue(SCALE_TYPE, ORDINAL_CONCEPT)
                .setPropertyValue(DATA_RESULTS_TYPE, QUALITATIVE_CONCEPT)
                .setPropertyValue(RESULTS_NAME, "Test Result conformance qualitative entry");
        List<EntityFacade> allowableResults = resultsViewModel.getObservableList(ALLOWABLE_RESULTS);
        allowableResults.add(DETECTED_CONCEPT);
        allowableResults.add(NOT_DETECTED_CONCEPT);

        STAMPDetail stampDetail = new STAMPDetail(State.ACTIVE.publicId(),
                System.currentTimeMillis(),
                TinkarTerm.USER.publicId(),
                TinkarTerm.DEVELOPMENT_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId());
        resultsViewModel.save();
        if (resultsViewModel.hasErrorMsgs()) {
            resultsViewModel.getValidationMessages().forEach(vMsg -> {
                LOG.error("Error: msg Type: %s errorcode: %s, msg: %s".formatted(vMsg.messageType(), vMsg.errorCode(), vMsg.interpolate(resultsViewModel)) );
            });
            throw new RuntimeException("Missing fields");
        }
        PublicId resultConformanceId = ViewModelHelper.createQualitativeResultConcept(resultsViewModel, stampDetail);

        System.out.println("Created a ResultConformance record " + resultConformanceId);
        return resultConformanceId;
    }

    public void displayResultConformanceQualitative(PublicId resultConformanceId) {
        List<PublicId> allowedResultIds = Searcher.getAllowedResultsFromResultConformance(resultConformanceId);

        allowedResultIds.forEach(publicId -> {
            String name = ViewModelHelper.findDescrNameText(publicId);
            System.out.println(name);

        });
        assertEquals(2, allowedResultIds.size(), "Should be two allowed results.");
    }

}
