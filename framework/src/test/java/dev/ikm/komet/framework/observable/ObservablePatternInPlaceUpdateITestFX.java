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
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A pattern created and then given its fields inside one transaction, as
 * {@code PatternDefinitionSeeder} creates its patterns on a store that lacks them. Every caller of
 * a nid shares one canonical observable, so the pattern's observable is made when the pattern has
 * no fields yet; the save keeps the uncommitted stamp, and the observable updates its version in
 * place. The version must then follow the record it now holds: a semantic composed of the pattern
 * gets one editable field per field definition, not the none the first record had.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(JavaFXThreadExtension.class)
class ObservablePatternInPlaceUpdateITestFX {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    @BeforeAll
    void setupDatabase() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    void teardownDatabase() {
        PrimitiveData.stop();
    }

    @Test
    @RunOnJavaFXThread
    void aPatternGivenItsFieldsInItsTransactionComposesSemanticsWithThem() {
        ViewCalculator calculator = Calculators.View.Default();
        ObservableComposer composer = ObservableComposer.builder()
                .viewCalculator(calculator)
                .author(KernelConcept.USER)
                .module(KometConcept.DEVELOPMENT_MODULE)
                .path(KernelConcept.DEVELOPMENT_PATH)
                .defaultState(State.ACTIVE)
                .transactionComment("pattern given its fields in its transaction")
                .build();

        PublicId patternId = PublicIds.newRandom();
        long patternNid = writePatternWithTwoFields(composer, patternId, KometConcept.MEANING, KometConcept.PURPOSE);

        var observablePattern = ObservableEntityHandle.get(patternNid).expectPattern();
        ObservablePatternVersion latest = observablePattern.getSnapshot(calculator).getLatestVersion().get();
        assertEquals(2, latest.fieldDefinitions().size(), "the shared observable follows the saved record");
        assertEquals(KometConcept.MEANING.nid(), latest.meaningProperty().get().nid(), "and its meaning");

        var semanticComposer = composer.composeSemantic(PublicIds.newRandom(), KernelConcept.USER,
                EntityProxy.Pattern.make(patternId));
        assertEquals(2, semanticComposer.getEditableVersion().getEditableFields().size(),
                "a semantic of the pattern has one editable field per field definition");
        composer.cancel();
    }

    /** What the seeder does: create the pattern, then give it its fields, in one transaction. */
    private static long writePatternWithTwoFields(ObservableComposer composer, PublicId patternId,
                                                  ConceptFacade meaning, ConceptFacade purpose) {
        var patternComposer = composer.composePattern(patternId);
        var editable = patternComposer.getEditableVersion();
        editable.getMeaningProperty().set(meaning);
        editable.getPurposeProperty().set(purpose);
        long patternNid = PrimitiveData.nid(patternId);
        long stampNid = editable.getEditStamp().nid();
        MutableList<FieldDefinitionRecord> definitions = Lists.mutable.empty();
        for (int index = 0; index < 2; index++) {
            definitions.add(new FieldDefinitionRecord(KernelConcept.COMPONENT_FIELD.nid(), purpose.nid(),
                    meaning.nid(), stampNid, patternNid, index));
        }
        editable.setFieldDefinitions(definitions.toImmutable());
        patternComposer.save();
        return patternNid;
    }
}
