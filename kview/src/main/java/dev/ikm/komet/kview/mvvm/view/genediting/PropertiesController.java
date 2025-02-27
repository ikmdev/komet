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


import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @InjectViewModel
    private SimpleViewModel propertiesViewModel;

    Subscriber<PropertyPanelEvent> showPanelSubscriber;

    Subscriber<GenEditingEvent> genEditingEventSubscriber;

    JFXNode<Pane, SemanticFieldsController> editFieldsJfxNode;



    @FXML
    private void initialize() {
        clearView();
        setupShowingPanelHandlers();
    }

    private void setupShowingPanelHandlers() {
        Config config = new Config(this.getClass().getResource("semantic-edit-fields.fxml"));
        config.updateViewModel("semanticFieldsViewModel", (semanticFieldsViewModel) -> {
            semanticFieldsViewModel
                    .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, propertiesViewModel.getObjectProperty(CURRENT_JOURNAL_WINDOW_TOPIC))
                    .addProperty(WINDOW_TOPIC, propertiesViewModel.getObjectProperty(WINDOW_TOPIC))
                    .addProperty(VIEW_PROPERTIES, propertiesViewModel.getObjectProperty(VIEW_PROPERTIES))
                    .addProperty(SEMANTIC, propertiesViewModel.getObjectProperty(SEMANTIC))
                    .addProperty(FIELD_INDEX, -1);
        });
        editFieldsJfxNode = FXMLMvvmLoader.make(config);

        showPanelSubscriber = evt -> {
            LOG.info("Show Panel by event type: " + evt.getEventType());
            propertyToggleButtonGroup.selectToggle(addEditButton);
            contentBorderPane.setCenter(editFieldsJfxNode.node());
            ValidationViewModel semanticFieldsViewModel = (ValidationViewModel) editFieldsJfxNode
                    .getViewModel("semanticFieldsViewModel").get();
            if (evt.getEventType() == PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS) {
                semanticFieldsViewModel.setPropertyValue(FIELD_INDEX, -1);
            } else if (evt.getEventType() == PropertyPanelEvent.SHOW_EDIT_SINGLE_SEMANTIC_FIELD) {
                semanticFieldsViewModel.setPropertyValue(FIELD_INDEX, evt.getObservableFieldIndex());
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(propertiesViewModel.getPropertyValue(WINDOW_TOPIC), PropertyPanelEvent.class, showPanelSubscriber);

        genEditingEventSubscriber = evt -> {
            LOG.info("Publish event type: " + evt.getEventType());
            Config closePropertiesConfig = new Config(this.getClass().getResource("close-properties.fxml"))
                    .addNamedViewModel(new NamedVm("propertiesViewModel", propertiesViewModel));

            JFXNode<Pane, ClosePropertiesController> closePropsJfxNode = FXMLMvvmLoader.make(closePropertiesConfig);
            contentBorderPane.setCenter(closePropsJfxNode.node());
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(propertiesViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, genEditingEventSubscriber);
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
