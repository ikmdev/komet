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
package dev.ikm.tinkar.integration.coordinate;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

import static dev.ikm.tinkar.terms.TinkarTerm.PATH_ORIGINS_PATTERN;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoordinatesIT {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinatesIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            CoordinatesIT.class);

    @BeforeAll
    static void beforeAll() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @Test
    @Order(2)
    void countPathOrigins() {
        // There are 4 Paths and 3 PathOrigins:
        //      MasterPath originates from DevelopmentPath
        //      DevelopmentPath originates from SandboxPath
        //      SandboxPath originates from PrimordialPath
        //      PrimordialPath is, by definition, the origin and therefore does not have a PathOrigin
        int expectedPathOriginsCount = 3;
        Assertions.assertEquals(expectedPathOriginsCount, PrimitiveData.get().semanticNidsOfPattern(PATH_ORIGINS_PATTERN.nid()).length);
    }

    @Test
    @Order(3)
    void pathOrigins() {
        for (int pathNid : PrimitiveData.get().semanticNidsOfPattern(PATH_ORIGINS_PATTERN.nid())) {
            SemanticEntity originSemantic = EntityService.get().getEntityFast(pathNid);
            Entity pathEntity = EntityService.get().getEntityFast(originSemantic.referencedComponentNid());
            ImmutableSet<StampPositionRecord> origin = PathService.get().getPathOrigins(originSemantic.referencedComponentNid());
            LOG.info("Path '" + PrimitiveData.text(pathEntity.nid()) + "' has an origin of: " + origin);
        }
    }

    @Test
    @Order(4)
    void computeLatest() {
        LOG.info("computeLatest()");

        StampCoordinateRecord developmentLatestFilter = Coordinates.Stamp.DevelopmentLatest();
        LOG.info("development latest filter '" + developmentLatestFilter);
        ConceptEntity englishLanguage = Entity.getFast(TinkarTerm.ENGLISH_LANGUAGE.nid());
        StampCalculatorWithCache calculator = StampCalculatorWithCache.getCalculator(developmentLatestFilter);
        Latest<ConceptEntityVersion> latest = calculator.latest(englishLanguage);
        LOG.info("Latest computed: '" + latest);

        Entity.provider().forEachSemanticForComponent(TinkarTerm.ENGLISH_LANGUAGE.nid(), semanticEntity -> {
            LOG.info(semanticEntity.toString() + "\n");
            for (int acceptibilityNid : EntityService.get().semanticNidsForComponentOfPattern(semanticEntity.nid(), TinkarTerm.US_DIALECT_PATTERN.nid())) {
                LOG.info("  Acceptability US: \n    " + EntityService.get().getEntityFast(acceptibilityNid));
            }
        });
        Entity.provider().forEachSemanticForComponent(TinkarTerm.NECESSARY_SET.nid(), semanticEntity -> {
            LOG.info(semanticEntity.toString() + "\n");
            for (int acceptibilityNid : EntityService.get().semanticNidsForComponentOfPattern(semanticEntity.nid(), TinkarTerm.US_DIALECT_PATTERN.nid())) {
                LOG.info("  Acceptability US: \n    " + EntityService.get().getEntityFast(acceptibilityNid));
            }
        });
    }

    @Test
    @Order(5)
    void names() {
        LOG.info("names()");
        LanguageCoordinateRecord usFqn = Coordinates.Language.UsEnglishFullyQualifiedName();
        LanguageCalculatorWithCache usFqnCalc = LanguageCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatest(), Lists.immutable.of(usFqn));
        LOG.info("fqn: " + usFqnCalc.getDescriptionText(TinkarTerm.NECESSARY_SET) + "\n");
        LOG.info("reg: " + usFqnCalc.getRegularDescriptionText(TinkarTerm.NECESSARY_SET) + "\n");
        LOG.info("def: " + usFqnCalc.getDefinitionDescriptionText(TinkarTerm.NECESSARY_SET) + "\n");
    }

    @Test
    @Order(6)
    void navigate() {
        LOG.info("navigate()");
        ViewCalculator viewCalculator = Calculators.View.Default();
        IntIdList children = viewCalculator.childrenOf(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        StringBuilder sb = new StringBuilder("Focus: [");
        Optional<String> optionalName = viewCalculator.getRegularDescriptionText(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(TinkarTerm.DESCRIPTION_ACCEPTABILITY.nid()));
        sb.append("]\nchildren: [");
        for (int childNid : children.toArray()) {
            optionalName = viewCalculator.getRegularDescriptionText(childNid);
            optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(childNid));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]\nparents: [");
        IntIdList parents = viewCalculator.parentsOf(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        for (int parentNid : parents.toArray()) {
            optionalName = viewCalculator.getRegularDescriptionText(parentNid);
            optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(parentNid));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]\n");
        LOG.info(sb.toString());
    }

    @Test
    @Order(7)
    void sortedNavigate() {
        LOG.info("sortedNavigate()");
        ViewCalculator viewCalculator = Calculators.View.Default();
        IntIdList children = viewCalculator.sortedChildrenOf(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        StringBuilder sb = new StringBuilder("Focus: [");
        Optional<String> optionalName = viewCalculator.getRegularDescriptionText(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(TinkarTerm.DESCRIPTION_ACCEPTABILITY.nid()));
        sb.append("]\nsorted children: [");
        for (int childNid : children.toArray()) {
            optionalName = viewCalculator.getRegularDescriptionText(childNid);
            optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(childNid));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]\nsorted parents: [");
        IntIdList parents = viewCalculator.sortedParentsOf(TinkarTerm.DESCRIPTION_ACCEPTABILITY);
        for (int parentNid : parents.toArray()) {
            optionalName = viewCalculator.getRegularDescriptionText(parentNid);
            optionalName.ifPresentOrElse(name -> sb.append(name), () -> sb.append(parentNid));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]\n");
        LOG.info(sb.toString());
    }
}
