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
package dev.ikm.komet.amplify.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.amplify.commons.CssHelper.genText;

/**
 * The properties window providing tabs of Edit, Hierarchy, History, and Comments.
 * This controller is associated with the view file history-change-selection.fxml.
 */
public class PropertiesController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesController.class);
    protected static final String HISTORY_CHANGE_FXML_FILE = "history-change-selection.fxml";
    @FXML
    private SVGPath commentsButton;

    @FXML
    private ToggleButton editButton;

    @FXML
    private ToggleButton historyButton;

    @FXML
    private ToggleButton navigateButton;

    @FXML
    private ToggleGroup propertyToggleButtonGroup;

    @FXML
    private BorderPane contentBorderPane;

    private Pane historyTabsBorderPane;
    private HistoryChangeController historyChangeController;

    private Pane editBorderPane = new StackPane(genText("Edit Concept"));
    private Pane navigatorPane = new StackPane(genText("Navigator Pane"));
    private Pane commentsPane = new StackPane(genText("Comments Pane"));
    private ViewProperties viewProperties;
    private EntityFacade entityFacade;
    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() throws IOException {
        clearView();
        // Load History tabs View Panel (FXML & Controller)
        FXMLLoader loader = new FXMLLoader(getClass().getResource(HISTORY_CHANGE_FXML_FILE));
        this.historyTabsBorderPane = loader.load();
        this.historyChangeController = loader.getController();

        // Programmatically change CSS Theme
        String styleSheet = defaultStyleSheet();
        this.historyTabsBorderPane.getStylesheets().add(styleSheet);

        // initially a default selected tab and view is shown
        updateDefaultSelectedViews();
    }

    private void updateDefaultSelectedViews() {
        // default to selected tab (History)
        Toggle tab = propertyToggleButtonGroup.getSelectedToggle();
        if (editButton.equals(tab)) {
            contentBorderPane.setCenter(editBorderPane);
        } else if (navigateButton.equals(tab)) {
            contentBorderPane.setCenter(navigatorPane);
        } else if (historyButton.equals(tab)) {
            contentBorderPane.setCenter(historyTabsBorderPane);
        } else if (commentsButton.equals(tab)) {
            contentBorderPane.setCenter(commentsPane);
        }
    }
    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade){
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }
    public void updateView() {
        this.historyChangeController.updateModel(viewProperties, entityFacade);
        this.historyChangeController.updateView();
    }

    @FXML
    private void showEditView(ActionEvent event) {
        event.consume();
        LOG.info("Show Edit View " + event);
        contentBorderPane.setCenter(editBorderPane);
    }
    @FXML
    private void showNavigatorView(ActionEvent event) {
        event.consume();
        LOG.info("Show Navigator View " + event);
        contentBorderPane.setCenter(navigatorPane);
    }
    @FXML
    private void showHistoryView(ActionEvent event) {
        event.consume();
        LOG.info("Show History View " + event);
        contentBorderPane.setCenter(historyTabsBorderPane);
    }
    @FXML
    private void showCommentsView(ActionEvent event) {
        event.consume();
        LOG.info("Show Comments View " + event);
        contentBorderPane.setCenter(commentsPane);

    }

    public HistoryChangeController getHistoryChangeController() {
        return historyChangeController;
    }

    public void clearView() {
    }
}
