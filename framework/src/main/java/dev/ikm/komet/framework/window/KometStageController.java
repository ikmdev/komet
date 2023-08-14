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
package dev.ikm.komet.framework.window;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.tabs.TabGroup;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewMenuTask;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;


/**
 * Root node of scene is given a UUID for unique identification.
 *
 * 
 */
public class KometStageController implements SaveState {

    private static final Logger LOG = LoggerFactory.getLogger(KometStageController.class);
    private final ImageView vanityImage = new ImageView();
    ArrayList<TabPane> tabPanes = new ArrayList<>();
    @FXML  // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML                                                                          // fx:id="bottomBorderBox"
    private HBox bottomBorderBox;                  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="editorButtonBar"
    private ButtonBar editorButtonBar;                  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="windowSplitPane"
    private SplitPane windowSplitPane;       // Value injected by FXMLLoader
    @FXML
    private BorderPane leftBorderPane;
    @FXML
    private BorderPane centerBorderPane;
    @FXML
    private BorderPane rightBorderPane;
    @FXML                                                                          // fx:id="editToolBar"
    private ToolBar editToolBar;                      // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="statusMessage"
    private Label statusMessage;                    // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="vanityBox"
    private Button vanityBox;                        // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="topGridPane"
    private GridPane topGridPane;                      // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="classifierMenuButton"
    private MenuButton classifierMenuButton;             // Value injected by FXMLLoader

    @FXML
    private Label pathLabel;

    @FXML
    private Menu windowCoordinates;

    @FXML
    private MenuButton viewPropertiesButton;

    //private ChangeListener<Boolean> focusChangeListener = this::handleFocusEvents;
    private Window window;
    private List<MenuButton> newTabMenuButtons = new ArrayList<>(5);
    private WindowSettings windowSettings;

    private KometPreferences nodePreferences;
    private TabGroup leftDetachableTabPane;
    private TabGroup centerDetachableTabPane;
    private TabGroup rightDetachableTabPane;

    private Node getTabPaneFromIndex(int index) {
        switch (index) {
            case 0:
                return leftDetachableTabPane;
            case 1:
                return centerDetachableTabPane;
            case 2:
                return rightDetachableTabPane;
            default:
                throw new ArrayIndexOutOfBoundsException("Tab pane index is: " + index);
        }
    }

    /**
     * When the button action event is triggered, refresh the user CSS file.
     *
     * @param event the action event.
     */
    @FXML
    public void handleRefreshUserCss(ActionEvent event) {
        try {
            // "Feature" to make css editing/testing easy in the dev environment.
            File cssSourceFile = new File("../framework/src/main/resources/dev/ikm/komet/framework/graphics/komet.css");
            if (cssSourceFile.exists()) {
                Scene scene = vanityBox.getScene();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssSourceFile.toURI().toURL().toString());
                LOG.info("Updated komet.css: " + cssSourceFile.getAbsolutePath());
            } else {
                LOG.info("File not found for komet.css: " + cssSourceFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // TODO: Raise an alert
            e.printStackTrace();
        }

    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert bottomBorderBox != null :
                "fx:id=\"bottomBorderBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editorButtonBar != null :
                "fx:id=\"editorButtonBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert centerBorderPane != null :
                "fx:id=\"centerBorderPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editToolBar != null :
                "fx:id=\"editToolBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert leftBorderPane != null :
                "fx:id=\"leftBorderPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert rightBorderPane != null :
                "fx:id=\"rightBorderPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert statusMessage != null :
                "fx:id=\"statusMessage\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert vanityBox != null : "fx:id=\"vanityBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert topGridPane != null :
                "fx:id=\"topGridPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert classifierMenuButton != null :
                "fx:id=\"classifierMenuButton\" was not injected: check your FXML file 'KometStageScene.fxml'.";

        //windowCoordinates.setGraphic(Icon.COORDINATES.getStyledIconographic());

        viewPropertiesButton.setGraphic(new FontIcon());
        viewPropertiesButton.setId("view-coordinates");
        //classifierMenuButton.setGraphic(Icon.ICON_CLASSIFIER1.getIconographic());
        classifierMenuButton.getItems().clear();
        classifierMenuButton.getItems().addAll(getTaskMenuItems());

        Image image = new Image(KometStageController.class.getResourceAsStream("/dev/ikm/komet/framework/images/viewer-logo-b@2.png"));
        vanityImage.setImage(image);
        vanityImage.setFitHeight(36);
        vanityImage.setPreserveRatio(true);
        vanityImage.setSmooth(true);
        vanityImage.setCache(true);
        vanityBox.setGraphic(vanityImage);


        topGridPane.setStyle("-fx-border-color: transparent");
        topGridPane.setOnDragDropped((DragEvent event) -> {
            event.setDropCompleted(true);
        });
        topGridPane.setOnDragOver((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.MOVE);
            topGridPane.setStyle("-fx-border-color: -komet-blue-color");
            event.consume();
        });

        topGridPane.setOnDragEntered((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.MOVE);
            topGridPane.setStyle("-fx-border-color: -komet-blue-color");
            event.consume();
        });

        topGridPane.setOnDragExited((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.MOVE);
            topGridPane.setStyle("-fx-border-color: transparent");
            event.consume();
        });
    }

    private List<MenuItem> getTaskMenuItems() {
        ArrayList<MenuItem> items = new ArrayList<>();
        return items;
    }

    @Override
    public void save() {
        try {
            this.windowSettings.dividerPositionsProperty().set(this.windowSplitPane.getDividerPositions());
            if (this.leftBorderPane.getCenter() != null && this.leftBorderPane.getCenter() instanceof WindowComponent windowComponent) {
                this.windowSettings.leftTabPreferencesProperty().set(windowComponent.nodePreferences().name());
            } else {
                this.windowSettings.leftTabPreferencesProperty().set("null");
            }
            if (this.centerBorderPane.getCenter() != null && this.centerBorderPane.getCenter() instanceof WindowComponent windowComponent) {
                this.windowSettings.centerTabPreferencesProperty().set(windowComponent.nodePreferences().name());
            } else {
                this.windowSettings.centerTabPreferencesProperty().set("null");
            }
            if (this.rightBorderPane.getCenter() != null && this.rightBorderPane.getCenter() instanceof WindowComponent windowComponent) {
                this.windowSettings.rightTabPreferencesProperty().set(windowComponent.nodePreferences().name());
            } else {
                this.windowSettings.rightTabPreferencesProperty().set("null");
            }

            this.windowSettings.save();
            saveChildren(this.leftBorderPane);
            saveChildren(this.centerBorderPane);
            saveChildren(this.rightBorderPane);
            this.nodePreferences.sync();
        } catch (BackingStoreException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
    }

    private static void saveChildren(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof WindowComponent windowComponent) {
                windowComponent.saveConfiguration();
            }
        }
    }

    void handleCloseRequest(WindowEvent event) {
        //stage.focusedProperty().removeListener(this.focusChangeListener);
        //MenuProvider.handleCloseRequest(event);
    }

    public WindowSettings windowSettings() {
        return windowSettings;
    }

    public void setup(KometPreferences nodePreferences) {
        this.nodePreferences = nodePreferences;
        this.window = topGridPane.getScene().getWindow();
        this.windowSettings = new WindowSettings(nodePreferences);

        ViewCalculatorWithCache viewCalculator = ViewCalculatorWithCache.getCalculator(windowSettings.getView().toViewCoordinateRecord());

        this.leftDetachableTabPane = TabGroup.create(windowSettings.getView(), TabGroup.REMOVAL.DISALLOW);
        this.centerDetachableTabPane = TabGroup.create(windowSettings.getView(), TabGroup.REMOVAL.DISALLOW);
        this.rightDetachableTabPane = TabGroup.create(windowSettings.getView(), TabGroup.REMOVAL.DISALLOW);

        leftBorderPane.setCenter(this.leftDetachableTabPane);
        centerBorderPane.setCenter(this.centerDetachableTabPane);
        rightBorderPane.setCenter(this.rightDetachableTabPane);

        this.window.setX(this.windowSettings.xLocationProperty().getValue());
        this.windowSettings.xLocationProperty().bind(this.window.xProperty());

        this.window.setY(this.windowSettings.yLocationProperty().getValue());
        this.windowSettings.yLocationProperty().bind(this.window.yProperty());

        this.window.setHeight(this.windowSettings.heightProperty().getValue());
        this.windowSettings.heightProperty().bind(this.window.heightProperty());

        this.window.setWidth(this.windowSettings.widthProperty().getValue());
        this.windowSettings.widthProperty().bind(this.window.widthProperty());

        this.windowSplitPane.setDividerPositions(this.windowSettings.dividerPositionsProperty().getValue());

        TinkExecutor.threadPool().execute(TaskWrapper.make(new ViewMenuTask(viewCalculator, windowSettings.getView()),
                (List<MenuItem> result) -> {
                    windowCoordinates.getItems().addAll(result);
                }));

        windowSettings.getView().addListener((observable, oldValue, newValue) -> {
            windowCoordinates.getItems().clear();
            TinkExecutor.threadPool().execute(TaskWrapper.make(new ViewMenuTask(viewCalculator, windowSettings.getView()),
                    (List<MenuItem> result) -> windowCoordinates.getItems().addAll(result)));
        });

        PrimitiveData.getStatesToSave().add(this);
    }

    public void setLeftTabs(ImmutableList<DetachableTab> tabs, int selectedIndexInTab) {
        setupTabs(tabs, selectedIndexInTab, this.leftDetachableTabPane);
    }

    private void setupTabs(ImmutableList<DetachableTab> tabs, int selectedIndexInTab, TabGroup tabGroup) {
        for (DetachableTab tab : tabs) {
            tabGroup.getTabs().add(tab);
        }
        if (selectedIndexInTab > -1 || selectedIndexInTab < tabs.size()) {
            tabGroup.getSelectionModel().select(tabs.get(selectedIndexInTab));
        }
    }

    public void setCenterTabs(ImmutableList<DetachableTab> tabs, int selectedIndexInTab) {
        setupTabs(tabs, selectedIndexInTab, this.centerDetachableTabPane);
    }

    public void setRightTabs(ImmutableList<DetachableTab> tabs, int selectedIndexInTab) {
        setupTabs(tabs, selectedIndexInTab, this.rightDetachableTabPane);
    }

    public ObservableViewNoOverride windowView() {
        return this.windowSettings.getView();
    }

    public void leftBorderPaneSetCenter(Node centerNode) {
        this.leftBorderPane.setCenter(centerNode);
    }

    public void centerBorderPaneSetCenter(Node centerNode) {
        this.centerBorderPane.setCenter(centerNode);
    }

    public void rightBorderPaneSetCenter(Node centerNode) {
        this.rightBorderPane.setCenter(centerNode);
    }

    public enum WindowKeys {
        WINDOW_INITIALIZED,
        FACTORY_CLASS,
        TAB_PANE_INDEX,
        INDEX_IN_TAB_PANE,
    }
}
