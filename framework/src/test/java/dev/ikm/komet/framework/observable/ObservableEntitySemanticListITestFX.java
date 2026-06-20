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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ObservableEntity#getObservableSemanticList()} against entity data
 * loaded into an ephemeral store from the Tinkar starter-data protobuf file.
 * <p>The accessor is verified to enumerate exactly the semantics that primitive data reports as
 * referencing a component ({@link EntityService#semanticNidsForComponent(int)}), to wrap each as an
 * {@link ObservableSemantic}, and to surface the correct referenced-component back-reference.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableEntitySemanticListITestFX {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableEntitySemanticListITestFX.class);
    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    /**
     * Well-known concepts present in the starter data. Each carries at least description semantics,
     * so the set collectively exercises the non-empty enumeration path.
     */
    private static final ConceptFacade[] SAMPLE_CONCEPTS = {
            TinkarTerm.ENGLISH_LANGUAGE,
            TinkarTerm.DEVELOPMENT_PATH,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.USER,
            TinkarTerm.KOMET_USER
    };

    @BeforeAll
    void setupDatabase() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        LOG.info("Started PrimitiveData with ephemeral store");
    }

    @Test
    @RunOnJavaFXThread
    @Order(1)
    void loadTestData() {
        assertTrue(PB_STARTER_DATA.exists(),
                "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath() +
                        " (copied by maven-dependency-plugin during process-test-resources)");
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA);
        long totalCount = loadProto.compute().getTotalCount();
        LOG.info("{} entities loaded", totalCount);
        assertTrue(totalCount > 0, "Should load entities from protobuf file");
    }

    /**
     * For each sample concept, the observable enumeration must report exactly the semantic nids that
     * primitive data reports as referencing that concept, and every wrapped element must be an
     * {@link ObservableSemantic} whose referenced component is the concept itself. At least one
     * concept must yield a non-empty list so the populated path is actually exercised.
     */
    @Test
    @RunOnJavaFXThread
    @Order(2)
    void observableSemanticListMatchesPrimitiveData() {
        int totalSemantics = 0;
        for (ConceptFacade concept : SAMPLE_CONCEPTS) {
            int conceptNid = concept.nid();

            Set<Integer> expectedNids = new HashSet<>();
            for (int semanticNid : EntityService.get().semanticNidsForComponent(conceptNid)) {
                expectedNids.add(semanticNid);
            }

            ObservableConcept observableConcept = ObservableEntityHandle.getConceptOrThrow(conceptNid);

            Set<Integer> observedNids = new HashSet<>();
            for (ObservableSemantic observableSemantic : observableConcept.getObservableSemanticList()) {
                assertNotNull(observableSemantic, "Enumerated semantic wrapper must not be null");
                assertEquals(conceptNid, observableSemantic.referencedComponentNid(),
                        "Enumerated semantic must reference the originating concept");
                observedNids.add(observableSemantic.nid());
            }

            assertEquals(expectedNids, observedNids,
                    "Observable enumeration must match primitive semanticNidsForComponent for " + concept);
            totalSemantics += observedNids.size();
        }

        assertTrue(totalSemantics > 0,
                "Sample concepts should collectively have at least one referencing semantic");
        LOG.info("Verified observable semantic enumeration over {} semantics", totalSemantics);
    }

    /**
     * A concept with no referencing semantics must yield an empty (non-null) iterable rather than
     * throwing, confirming the previously-stubbed accessor handles the empty case.
     */
    @Test
    @RunOnJavaFXThread
    @Order(3)
    void observableSemanticListIsEmptyWhenNoReferencingSemantics() {
        for (ConceptFacade concept : SAMPLE_CONCEPTS) {
            int conceptNid = concept.nid();
            if (EntityService.get().semanticNidsForComponent(conceptNid).length != 0) {
                continue;
            }
            ObservableConcept observableConcept = ObservableEntityHandle.getConceptOrThrow(conceptNid);
            Iterable<ObservableSemantic> semantics = observableConcept.getObservableSemanticList();
            assertNotNull(semantics, "Empty enumeration must be a non-null iterable");
            assertFalse(semantics.iterator().hasNext(), "Expected no referencing semantics for " + concept);
        }
    }

    @AfterAll
    void tearDownDatabase() {
        PrimitiveData.stop();
        LOG.info("PrimitiveData stopped");
    }
}
