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
package dev.ikm.komet.kview.klwindows.lidr;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.lidr.mvvm.view.details.LidrDetailsController;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

import java.util.UUID;

import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.DEVICE_ENTITY;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;

/**
 * Represents a window for LIDR (Logical Instrumentation, Devices, and Records).
 */
public class LidrKlWindow extends AbstractEntityChapterKlWindow {

    /**
     * Creates a new LIDR window.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param deviceConcept  an optional entity facade representing a device, or null for creation mode.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public LidrKlWindow(UUID journalTopic, EntityFacade deviceConcept,
                        ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, deviceConcept, viewProperties, preferences);

        // Prefetch modules and paths for the view
        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel
                .setPropertyValue(PATHS_PROPERTY, fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId()), true)
                .setPropertyValue(MODULES_PROPERTY, fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId()), true);

        // In create mode, set up LIDR view model for injection
        ValidationViewModel lidrViewModel = new LidrViewModel()
                .setPropertyValue(CONCEPT_TOPIC, getWindowTopic())
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel);

        String mode;
        if (deviceConcept == null) {
            mode = CREATE;
        } else {
            mode = VIEW;
            lidrViewModel.setPropertyValue(DEVICE_ENTITY, deviceConcept);
        }
        lidrViewModel.setPropertyValue(MODE, mode);
        lidrViewModel.save(true); // xfer to model values.

        Config lidrConfig = new Config(LidrDetailsController.class.getResource("lidr-details.fxml"))
                .addNamedViewModel(new NamedVm("lidrViewModel", lidrViewModel));

        JFXNode<Pane, LidrDetailsController> lidrJFXNode = FXMLMvvmLoader.make(lidrConfig);
        lidrJFXNode.controller().updateView();

        // Getting the concept window pane
        this.paneWindow = lidrJFXNode.node();

        // Calls the remove method to remove and concepts that were closed by the user.
        lidrJFXNode.controller().setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.LIDR;
    }
}
