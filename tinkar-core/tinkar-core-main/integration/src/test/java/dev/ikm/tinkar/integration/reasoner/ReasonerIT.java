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
package dev.ikm.tinkar.integration.reasoner;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReasonerIT {
    private static final Logger LOG = LoggerFactory.getLogger(ReasonerIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            ReasonerIT.class);
    @BeforeAll
    public void beforeAll() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA);
    }

    @AfterAll
    public void afterAll() {
        TestHelper.stopDatabase();
        // delete temporary database
        FileUtil.recursiveDelete(DATASTORE_ROOT);
    }

    @Test
    public void reasonStarterData() throws Exception {
        AtomicInteger totalConcepts = new AtomicInteger();
        PrimitiveData.get().forEachConceptNid(conceptNid -> totalConcepts.getAndIncrement());
        LOG.info("Initial number of concepts " + totalConcepts.get());
        List<ReasonerService> rss = PluggableService.load(ReasonerService.class).stream().map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparing(ReasonerService::getName)).toList();
        LOG.info("Number of reasoners " + rss.size());
        for (ReasonerService rs : rss) {
            LOG.info("Reasoner service: " + rs);

            rs.init(Calculators.View.Default(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
            rs.setProgressUpdater(null);
            // Extract
            rs.extractData();
            // Load
            rs.loadData();
            // Compute
            rs.computeInferences();
            // Process Results
            ClassifierResults results = rs.processResults(null, false);

            LOG.info("After Size of ConceptSet: " + rs.getReasonerConceptSet().size());
            LOG.info("ClassifierResults: inferred changes size " + results.getConceptsWithInferredChanges().size());
            LOG.info("ClassifierResults: navigation changes size " + results.getConceptsWithNavigationChanges().size());
            LOG.info("ClassifierResults: classificationconcept size " + results.getClassificationConceptSet().size());
        }
        AtomicInteger totalConcepts2 = new AtomicInteger();
        PrimitiveData.get().forEachConceptNid(conceptNid ->
                {totalConcepts2.getAndIncrement();}
        );
        LOG.info("Final number of concepts " + totalConcepts2.get());
    }
}
