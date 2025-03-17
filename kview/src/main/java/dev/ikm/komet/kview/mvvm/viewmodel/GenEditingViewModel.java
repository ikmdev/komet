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

import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.list.ImmutableList;
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
    public static String FIELD_INDEX = "fieldIndex";
    public static String PATTERN = "pattern";

    // for single semantic editing
    public static String FIELD = "field";

    public GenEditingViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, (UUID) null)
                .addProperty(WINDOW_TOPIC, (UUID) null)
                .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                .addProperty(FIELDS_COLLECTION, new ArrayList<FieldRecord<Object>>()) // Ordered collection of Fields
                .addProperty(REF_COMPONENT, (EntityFacade) null)
                .addProperty(SEMANTIC, (EntityFacade) null)
                .addProperty(PATTERN, (EntityFacade) null)
        ;
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

    /**
     * given a pattern create a default, empty semantic
     * @param pattern existing pattern
     * @return a default, empty semantic
     */
    public EntityFacade createEmptySemantic(EntityFacade pattern) {
        EntityFacade semantic;
        // set up defaults for the initial STAMP on a new Semantic
        State status = State.ACTIVE;
        EntityProxy.Concept author = TinkarTerm.USER;
        EntityProxy.Concept module = TinkarTerm.DEVELOPMENT_MODULE;
        EntityProxy.Concept path = TinkarTerm.DEVELOPMENT_PATH;
        EntityProxy patternProxy = pattern.toProxy();

        ViewCalculator viewCalculator = getViewProperties().calculator();
        PatternVersionRecord patternVersionRecord = (PatternVersionRecord) viewCalculator.latest(pattern).get();

        // create empty semantic using the Composer API
        EntityService.get().beginLoadPhase();
        Composer composer = new Composer("Semantic for %s".formatted(pattern.description()));
        Session session = composer.open(status, author, module, path);

        EntityProxy.Semantic defaultSemantic = EntityProxy.Semantic.make(PublicIds.newRandom());
        session.compose((SemanticAssembler semanticAssembler) -> {
            semanticAssembler
                    .semantic(defaultSemantic)
                    .reference(patternProxy)
                    .pattern((EntityProxy.Pattern) patternProxy)
                    .fieldValues(fieldValues -> {
                        patternVersionRecord.fieldDefinitions().forEach(f -> {
                            if (f.dataTypeNid() == TinkarTerm.COMPONENT_FIELD.nid()) {
                                fieldValues.with(TinkarTerm.ANONYMOUS_CONCEPT);
                            } else if (f.dataTypeNid() == TinkarTerm.STRING_FIELD.nid()
                                    || f.dataTypeNid() == TinkarTerm.STRING.nid()) {
                                fieldValues.with("[Placeholder]");
                            } else if (f.dataTypeNid() == TinkarTerm.FLOAT_FIELD.nid()) {
                                fieldValues.with(0.0);
                            } else if (f.dataTypeNid() == TinkarTerm.BOOLEAN_FIELD.nid()) {
                                fieldValues.with(false);
                            } else if (f.dataTypeNid() == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
                                fieldValues.with(IntIds.list.empty());
                            } else if (f.dataTypeNid() == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                                fieldValues.with(IntIds.set.empty());
                            }
                        });
                    }).attach((USDialect dialect) -> dialect
                            .acceptability(PREFERRED));
        });
        composer.commitSession(session);
        EntityService.get().endLoadPhase();
        semantic = defaultSemantic.toProxy();
        return semantic;
    }


}
