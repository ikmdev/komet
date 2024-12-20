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


import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
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
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
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

    JFXNode<Pane, SemanticFieldsController> editFieldsJfxNode;

    @FXML
    private void initialize() {
        clearView();
        setupShowingPanelHandlers();
    }

    private void setupShowingPanelHandlers() {
        showPanelSubscriber = evt -> {
            LOG.info("Show Panel by event type: " + evt.getEventType());
            if (evt.getEventType() == PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS) {
                System.out.println("show edit semantic_fields panel");
                if (editFieldsJfxNode == null) {
                    System.out.println(evt.getSemantic());
                    propertyToggleButtonGroup.selectToggle(addEditButton);
                    Config config = new Config(this.getClass().getResource("semantic-edit-fields.fxml"));
                    config.updateViewModel("semanticFieldsViewModel", (semanticFieldsViewModel) -> {
                        semanticFieldsViewModel
                                .addProperty(WINDOW_TOPIC, propertiesViewModel.getObjectProperty(WINDOW_TOPIC))
                                .addProperty(VIEW_PROPERTIES, propertiesViewModel.getObjectProperty(VIEW_PROPERTIES))
                                .addProperty(SEMANTIC, evt.getSemantic());
                    });
                    editFieldsJfxNode = FXMLMvvmLoader.make(config);
                }
                contentBorderPane.setCenter(editFieldsJfxNode.node());
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(propertiesViewModel.getPropertyValue(WINDOW_TOPIC), PropertyPanelEvent.class, showPanelSubscriber);
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
