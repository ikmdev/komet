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
package dev.ikm.komet.kview.klwindows.genediting;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.genediting.GenEditingDetailsController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.ArrayList;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.*;

/**
 * The General Editing Chapter window showing semantic details on the Journal Window's surface as a JavaFX Pane
 * having the ability to resize, drag, close.
 */
public class GenEditingKlWindow extends AbstractEntityChapterKlWindow {

    /**
     * Root container for the FXML UI and its controller.
     */
    private final JFXNode<Pane, GenEditingDetailsController> jfxNode;

    /**
     * Constructs a new editing window for a specific semantic entity.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   the semantic when in edit mode, the pattern when in create mode
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public GenEditingKlWindow(UUID journalTopic, EntityFacade entityFacade, ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        EntityFacade refComponent;
        EntityFacade semanticComponent;
        PatternFacade patternFacade;
        if (entityFacade instanceof PatternFacade) {
            patternFacade = (PatternFacade) entityFacade;
            refComponent = null;
            semanticComponent = null;
        } else {
            semanticComponent = entityFacade;
            SemanticEntity entity = (SemanticEntity) EntityService.get().getEntity(entityFacade.nid()).get();
            patternFacade = entity.pattern().toProxy();
            refComponent = EntityService.get().getEntity(entity.referencedComponentNid()).get();
        }
        Config config = new Config(GenEditingDetailsController.class.getResource("genediting-details.fxml"))
                .updateViewModel("genEditingViewModel", genEditingViewModel ->
                        genEditingViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                                .setPropertyValue(WINDOW_TOPIC, getWindowTopic())
//                                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                                .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                                .setPropertyValue(REF_COMPONENT, refComponent)
                                .setPropertyValue(SEMANTIC, semanticComponent)
                                .setPropertyValue(PATTERN, patternFacade));

        // Create chapter window
        jfxNode = FXMLMvvmLoader.make(config);

        // Getting the concept window pane
        this.paneWindow = jfxNode.node();

        var controller = jfxNode.controller();

        // Calls the remove method to remove and concepts that were closed by the user.
        controller.setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });
    }

    @Override
    public void onShown() {
        jfxNode.controller().putTitlePanesArrowOnRight();
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_EDITING;
    }
}
