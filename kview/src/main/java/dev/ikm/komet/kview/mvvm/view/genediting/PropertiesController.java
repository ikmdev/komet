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
package dev.ikm.komet.kview.mvvm.view.genediting;


import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;

public class PropertiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    private static final String EDIT_FIELD = "Edit Field";
    private static final String ADD_FIELD = "Add Field";

    @FXML
    private ToggleButton addEditButton;

    @FXML
    private ToggleButton historyButton;

    @FXML
    private ToggleButton instancesButton;

    @FXML
    private ToggleGroup propertyToggleButtonGroup;

    @FXML
    private BorderPane contentBorderPane;

    /////////// Private variables
    /**
     * Show the current edit window.
     */
    public enum PaneProperties {
        PROPERTY_PANE_OPEN,
    }
    private Pane currentEditPane;

    private Pane closePropsPane;
    private ClosePropertiesController closePropertiesController;

    @InjectViewModel
    private GenEditingViewModel genEditingViewModel;


    Subscriber<PropertyPanelEvent> showPanelSubscriber;

    Subscriber<GenEditingEvent> genEditingEventSubscriber;

    JFXNode<Pane, SemanticFieldsController> editFieldsJfxNode;

    JFXNode<Pane, ReferenceComponentController> referenceComponentJfxNode;

    @FXML
    private void initialize() {
        clearView();
        setupShowingPanelHandlers();
        setupShowReferencePanelHandlers();
    }

    //Refers to Add Reference Component
    private void setupShowReferencePanelHandlers() {
        Config addReferenceConfig = new Config(this.getClass().getResource("reference-component.fxml"))
                .addNamedViewModel(new NamedVm("genEditingViewModel", genEditingViewModel));
        referenceComponentJfxNode = FXMLMvvmLoader.make(addReferenceConfig);
    }

    private void setupShowingPanelHandlers() {
        Config config = new Config(this.getClass().getResource("semantic-edit-fields.fxml"))
            .addNamedViewModel(new NamedVm("genEditingViewModel", genEditingViewModel));

        editFieldsJfxNode = FXMLMvvmLoader.make(config);

        Config closePropertiesConfig = new Config(this.getClass().getResource("close-properties.fxml"))
                .addNamedViewModel(new NamedVm("genEditingViewModel", genEditingViewModel));
        JFXNode<Pane, ClosePropertiesController> closePropsJfxNode = FXMLMvvmLoader.make(closePropertiesConfig);
        closePropsPane = closePropsJfxNode.node();
        closePropertiesController = closePropsJfxNode.controller();

        genEditingEventSubscriber = evt -> {
            LOG.info("Publish event type: " + evt.getEventType());

            // "Semantic Details Added" is displayed when form values are Submitted when in CREATE mode
            // "Semantic Details Changed" is displayed when form values are Submitted when in EDIT mode

            if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
                closePropertiesController.showSemanticDetailsAdded();
            } else {
                closePropertiesController.showSemanticDetailsChanged();
            }

            contentBorderPane.setCenter(closePropsPane);
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, genEditingEventSubscriber);

        showPanelSubscriber = evt -> {
            LOG.info("Show Panel by event type: " + evt.getEventType());
            propertyToggleButtonGroup.selectToggle(addEditButton);

            ValidationViewModel semanticFieldsViewModel = (ValidationViewModel) editFieldsJfxNode
                    .getViewModel("genEditingViewModel").get();
            if (evt.getEventType() == PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS) {
                genEditingViewModel.setPropertyValue(FIELD_INDEX, -1);
                contentBorderPane.setCenter(editFieldsJfxNode.node());
            } else if (evt.getEventType() == PropertyPanelEvent.SHOW_EDIT_SINGLE_SEMANTIC_FIELD) {
                genEditingViewModel.setPropertyValue(FIELD_INDEX, evt.getObservableFieldIndex());
                contentBorderPane.setCenter(editFieldsJfxNode.node());
            } else if (evt.getEventType() == PropertyPanelEvent.SHOW_ADD_REFERENCE_SEMANTIC_FIELD) {
                genEditingViewModel.setPropertyValue(FIELD_INDEX, evt.getObservableFieldIndex());
                contentBorderPane.setCenter(referenceComponentJfxNode.node());
            } else if (evt.getEventType() == PropertyPanelEvent.NO_SELECTION_MADE_PANEL) {
                // change the heading on the top of the panel
                genEditingViewModel.setPropertyValue(FIELD_INDEX, -1);

                // "No Selection Made" is displayed on initial creation of Semantic
                closePropertiesController.showNoSelectionMadeToEditSemanticElement();
                contentBorderPane.setCenter(closePropsPane);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                PropertyPanelEvent.class, showPanelSubscriber);


    }


    @FXML
    private void showAddEditView(ActionEvent event) {
        LOG.info("Show Add/Edit View " + event);
        event.consume();
        this.addEditButton.setSelected(true);
//        contentBorderPane.setCenter(currentEditPane);
    }

    @FXML
    private void showInstances(ActionEvent actionEvent) {
        LOG.info("Show Instances " + actionEvent);
//        contentBorderPane.setCenter(instancesPane);
    }

    @FXML
    private void showHistoryView(ActionEvent event) {
        LOG.info("Show Pattern History");
        this.historyButton.setSelected(true);
//        contentBorderPane.setCenter(historyPane);
    }

    public void clearView() {
    }

}
