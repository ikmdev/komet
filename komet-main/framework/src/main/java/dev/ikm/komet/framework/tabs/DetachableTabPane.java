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
package dev.ikm.komet.framework.tabs;


import dev.ikm.komet.framework.ScreenInfo;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Original version by amrullah. Extended and modified for Java 16 and Komet by KEC.
 *
 * @author amrullah
 * 
 */
class DetachableTabPane extends TabPane {
    private static final Logger LOG = LoggerFactory.getLogger(DetachableTabPane.class);
    private static final Path path = new Path();
    private static final DetachableTabPathModel pathModel = new DetachableTabPathModel(path);
    private static final DataFormat DATA_FORMAT = new DataFormat("dragAwareTab");
    /**
     * hold reference to the source of drag event. We can't use
     * event.getGestureSource() because it is null when the target on a different
     * stage
     */
    private static DetachableTabPane DRAG_SOURCE;
    private static int DRAG_SOURCE_INDEX = -1;
    private static DetachableTab DRAGGED_TAB;
    private static double DRAG_TAB_WIDTH = 400;
    private static double DRAG_CONTENT_WIDTH = 400;
    private static double DRAG_CONTENT_HEIGHT = 700;
    private StringProperty scope = new SimpleStringProperty("");
    private Pos pos = null;
    private int dropIndex = 0;
    private List<Double> lstTabPoint = new ArrayList<>();
    private boolean closeIfEmpty = false;
    private TabGroup detachableStack = null;
    private Button btnTop;
    private Button btnRight;
    private Button btnBottom;
    private Button btnLeft;
    private StackPane dockPosIndicator;
    private GridPane posGrid;
    private Callback<TabGroup, Scene> sceneFactory = new Callback<TabGroup, Scene>() {

        @Override
        public Scene call(TabGroup p) {

            return new Scene(new SplitPane(p), DetachableTabPane.DRAG_CONTENT_WIDTH, DetachableTabPane.DRAG_CONTENT_HEIGHT);
        }
    };
    private TabGroupFactory tabGroupFactory = new TabGroupFactory() {
        @Override
        protected void init(TabGroup a) {
            a.setMaxWidth(Double.MAX_VALUE);
        }

    };
    private Callback<Stage, Window> stageOwnerFactory = new Callback<Stage, Window>() {

        @Override
        public Window call(Stage p) {
            if (DetachableTabPane.this.getScene() == null) {
                AlertStreams.getRoot().dispatch(AlertObject.makeWarning("Unable to get parent stage.", "DetachableTabPane.this.getScene() is null."));
                return null;
            }
            return DetachableTabPane.this.getScene().getWindow();
        }
    };

    DetachableTabPane() {
        super();
        getStyleClass().add("detachable-w-pane");
        setMaxWidth(Double.MAX_VALUE);
        attachListeners();
    }


    protected void setDetachableStack(TabGroup detachableStack) {
        this.detachableStack = detachableStack;
    }

    private void initDropButton() {
        btnTop = new Button("", new FontIcon());
        btnTop.setId("drop-top-button");
        btnTop.getStyleClass().add("drop-top");
        btnRight = new Button("", new FontIcon());
        btnRight.setId("drop-right-button");
        btnRight.getStyleClass().add("drop-right");
        btnBottom = new Button("", new FontIcon());
        btnBottom.setId("drop-bottom-button");
        btnBottom.getStyleClass().add("drop-bottom");
        btnLeft = new Button("", new FontIcon());
        btnLeft.setId("drop-left-button");
        btnLeft.getStyleClass().add("drop-left");
        posGrid = new GridPane();
        posGrid.add(btnTop, 1, 0);
        posGrid.add(btnRight, 2, 1);
        posGrid.add(btnBottom, 1, 2);
        posGrid.add(btnLeft, 0, 1);
        posGrid.getStyleClass().add("dock-pos-indicator");
        dockPosIndicator = new StackPane();
        dockPosIndicator.getChildren().add(posGrid);
        dockPosIndicator.setLayoutX(100);
        dockPosIndicator.setLayoutY(100);
    }

    /**
     * Get drag scope id
     *
     * @return
     */
    String getScope() {
        return scope.get();
    }

    /**
     * Set scope id. Only TabPane having the same scope that could be drop
     * target. Default is empty string. So the default behavior is this TabPane
     * could receive tab from empty scope DragAwareTabPane
     *
     * @param scope
     */
    void setScope(String scope) {
        this.scope.set(scope);
    }

    /**
     * Scope property. Only TabPane having the same scope that could be drop
     * target.
     *
     * @return
     */
    StringProperty scopeProperty() {
        return scope;
    }

    private void attachListeners() {

        /**
         * This listener detects when the TabPane is shown. Then it will call
         * initiateDragGesture. It because the lookupAll call in that method only
         * works if the stage containing this instance is already shown.
         */
        sceneProperty().addListener((ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
            if (t == null && t1 != null) {
                if (getScene().getWindow() != null) {
                    Platform.runLater(() -> {
                        initiateDragGesture(true);
                    });
                } else {
                    getScene().windowProperty().addListener((ObservableValue<? extends Window> ov1, Window t2, Window t3) -> {
                        if (t2 == null && t3 != null) {
                            t3.addEventHandler(WindowEvent.WINDOW_SHOWN, (t4) -> {
                                initiateDragGesture(true);
                            });
                        }
                    });
                }
            }
        });

        this.addEventHandler(DragEvent.ANY, (DragEvent event) -> {
            try {
                if (DRAG_SOURCE == null) {
                    return;
                }
                if (event.getEventType() == DragEvent.DRAG_OVER) {
                    if (DetachableTabPane.this.scope.get().equals(DRAG_SOURCE.getScope())) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        repaintPath(event, 1);
                    }
                    event.consume();
                } else if (event.getEventType() == DragEvent.DRAG_EXITED) {
                    if (DetachableTabPane.this.getSkin() instanceof TabPaneSkin sp) {
                        sp.getChildren().remove(path);
                        sp.getChildren().remove(dockPosIndicator);
                        DetachableTabPane.this.requestLayout();
                    }
                } else if (event.getEventType() == DragEvent.DRAG_ENTERED) {
                    if (!DetachableTabPane.this.scope.get().equals(DRAG_SOURCE.getScope())) {
                        return;
                    }
                    calculateTabPoints();
                    if (dockPosIndicator == null) {
                        initDropButton();
                    }
                    double layoutX = DetachableTabPane.this.getWidth() / 2;
                    double layoutY = DetachableTabPane.this.getHeight() / 2;
                    dockPosIndicator.setLayoutX(layoutX);
                    dockPosIndicator.setLayoutY(layoutY);
                    if (DetachableTabPane.this.getSkin() instanceof TabPaneSkin sp) {
                        if (!sp.getChildren().contains(path)) {
                            if (!getTabs().isEmpty()) {
                                sp.getChildren().add(dockPosIndicator);
                            }
                            repaintPath(event, 2);
                            sp.getChildren().add(path);
                        }
                    }
                } else if (event.getEventType() == DragEvent.DRAG_DROPPED) {
                    if (pos != null) {
                        adjacent();
                        event.setDropCompleted(true);
                        event.consume();
                        return;
                    }
                    if (DRAG_SOURCE != null && DRAG_SOURCE != DetachableTabPane.this) {
                        final Tab selectedtab = DRAGGED_TAB;
                        DetachableTabPane.this.getTabs().add(dropIndex, selectedtab);
                        Platform.runLater(() -> DetachableTabPane.this.getSelectionModel().select(selectedtab));
                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(DRAG_SOURCE == DetachableTabPane.this);
                        final Tab selectedtab = DRAGGED_TAB;
                        int currentSelectionIndex = getTabs().indexOf(selectedtab);
                        if (dropIndex == currentSelectionIndex) {
                            return;
                        }
                        getTabs().add(dropIndex, selectedtab);
                        Platform.runLater(() -> DetachableTabPane.this.getSelectionModel().select(selectedtab));
                    }
                    if (event.isDropCompleted()) {
                        event.getDragboard().setContent(null);
                    }
                    event.consume();
                }
            } catch (Exception ex) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(ex));
            }
        });

        getTabs().addListener((ListChangeListener.Change<? extends Tab> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    if (getScene() != null && getScene().getWindow() != null) {
                        if (getScene().getWindow().isShowing()) {
                            Platform.runLater(() -> {
                                clearGesture();
                                initiateDragGesture(true);
                                /**
                                 * We need to use timer to wait until the
                                 * tab-add-animation finish
                                 */
                                futureCalculateTabPoints();
                            });
                        }
                    }
                } else if (change.wasRemoved()) {
                    /**
                     * We need to use timer to give the system some time to remove
                     * the tab from TabPaneSkin.
                     */
                    futureCalculateTabPoints();

                    if (DRAG_SOURCE == null) {
                        //it means we are not dragging
                        if (getScene() != null && getScene().getWindow() instanceof TabStage) {
                            TabStage stage = (TabStage) getScene().getWindow();
                            closeStageIfNeeded(stage);
                        }
                        if (getTabs().isEmpty()) {
                            removeFromParent(DetachableTabPane.this);
                        }
                    }
                }
            }
        });

    }

    private Optional<SplitPane> findParentSplitPane(DetachableTabPane control) {
        if (control.getParent() == null) return null;
        Set<Node> lstSplitpane = control.getScene().getRoot().lookupAll(".split-pane");
        SplitPane parentSplitPane = null;
        for (Node node : lstSplitpane) {
            if (node instanceof SplitPane splitPane) {
                for (Node child : splitPane.getItems()) {
                    if (child instanceof TabGroup tabGroup) {
                        if (tabGroup.tabPane() == control) {
                            parentSplitPane = splitPane;
                            break;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(parentSplitPane);
    }

    private void adjacent() {
        final Tab selectedtab = DRAGGED_TAB;

        if (getParent() == null) {
            //it means the tabpane is the root of the scene.
            Scene scene = getScene();
            StackPane wrapper = new StackPane();
            wrapper.getChildren().add(this);
            scene.setRoot(wrapper);
        }

        Parent parent = this.detachableStack.getParent();

        Orientation requestedOrientation = Orientation.HORIZONTAL;
        if (pos == Pos.BOTTOM_CENTER || pos == Pos.TOP_CENTER) {
            requestedOrientation = Orientation.VERTICAL;
        }

        int requestedIndex = 0;
        Optional<SplitPane> optionalSplitPane = findParentSplitPane(DetachableTabPane.this);
        if (optionalSplitPane.isPresent() && requestedOrientation == optionalSplitPane.get().getOrientation()) {
            requestedIndex = optionalSplitPane.get().getItems().indexOf(DetachableTabPane.this.detachableStack);
        }
        if (pos == Pos.CENTER_RIGHT || pos == Pos.BOTTOM_CENTER) {
            requestedIndex++;
        }

        if (optionalSplitPane.isEmpty()) {
            SplitPane targetSplitPane = new SplitPane();
            targetSplitPane.setMaxWidth(Double.MAX_VALUE);
            targetSplitPane.setOrientation(requestedOrientation);

            if (parent instanceof Pane parentPane) {
                int index = parentPane.getChildren().indexOf(DetachableTabPane.this.detachableStack);
                if (parentPane instanceof BorderPane borderPane &&
                        borderPane.getCenter() == DetachableTabPane.this.detachableStack) {
                    parentPane.getChildren().remove(DetachableTabPane.this.detachableStack);
                    borderPane.setCenter(targetSplitPane);
                } else {
                    parentPane.getChildren().remove(DetachableTabPane.this.detachableStack);
                    parentPane.getChildren().add(index, targetSplitPane);
                }
                targetSplitPane.getItems().add(DetachableTabPane.this.detachableStack);
                TabGroup tabGroup = tabGroupFactory.create(this.detachableStack);
                tabGroup.getTabs().add(selectedtab);
                targetSplitPane.getItems().add(requestedIndex, tabGroup);
            }

        } else {
            SplitPane targetSplitPane = optionalSplitPane.get();
            if (targetSplitPane.getItems().size() == 1) {
                targetSplitPane.setOrientation(requestedOrientation);
            }
            if (targetSplitPane.getOrientation() == requestedOrientation) {
                TabGroup tabGroup = tabGroupFactory.create(this.detachableStack);
                tabGroup.getTabs().add(selectedtab);
                targetSplitPane.getItems().add(requestedIndex, tabGroup);
                int itemCount = targetSplitPane.getItems().size();
                double[] dividerPos = new double[itemCount];
                dividerPos[0] = 1d / itemCount;
                for (int i = 1; i < dividerPos.length; i++) {
                    dividerPos[i] = dividerPos[i - 1] + dividerPos[0];
                }
                targetSplitPane.setDividerPositions(dividerPos);
            } else {
                int indexTabPane = targetSplitPane.getItems().indexOf(DetachableTabPane.this.detachableStack);
                targetSplitPane.getItems().remove(DetachableTabPane.this.detachableStack);
                SplitPane innerSplitpane = new SplitPane();
                innerSplitpane.setMaxWidth(Double.MAX_VALUE);
                targetSplitPane.getItems().add(indexTabPane, innerSplitpane);
                innerSplitpane.setOrientation(requestedOrientation);
                innerSplitpane.getItems().add(DetachableTabPane.this.detachableStack);
                TabGroup tabGroup = tabGroupFactory.create(this.detachableStack);
                tabGroup.getTabs().add(selectedtab);
                innerSplitpane.getItems().add(requestedIndex, tabGroup);
            }
        }
    }

    private void futureCalculateTabPoints() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // calculateTabPoints() was encountering updates to list on multiple threads.
                Platform.runLater(() -> {
                    calculateTabPoints();
                    timer.cancel();
                    timer.purge();
                });
            }
        }, 1000);
    }

    /**
     * The lookupAll call in this method only works if the stage containing this
     * instance is already shown.
     */
    private void initiateDragGesture(boolean retryOnFailed) {
        Node tabheader = getTabHeaderArea();
        if (tabheader == null) {
            if (retryOnFailed) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        initiateDragGesture(false);
                        timer.cancel();
                        timer.purge();
                    }
                }, 500);

            }
            return;
        }
        Set<Node> tabs = tabheader.lookupAll(".tab");
        if (tabs.isEmpty() && !getTabs().isEmpty()) {
            AlertStreams.getRoot().dispatch(AlertObject.makeWarning("Failed to initiate drag gesture.", "There are no tabs."));
        }
        for (Node node : tabs) {
            addGesture(this, node);
        }
    }

    private Node getTabHeaderArea() {
        Node tabheader = null;
        for (Node node : this.getChildrenUnmodifiable()) {
            if (node.getStyleClass().contains("tab-header-area")) {
                tabheader = node;
                break;
            }
        }
        return tabheader;
    }

    private void calculateTabPoints() {
        lstTabPoint.clear();
        lstTabPoint.add(0d);
        Node tabheader = getTabHeaderArea();
        if (tabheader == null) return;
        Set<Node> tabs = tabheader.lookupAll(".tab");
        Point2D inset = DetachableTabPane.this.localToScene(0, 0);
        for (Node node : tabs) {
            Point2D point = node.localToScene(0, 0);
            Bounds bound = node.getLayoutBounds();
            lstTabPoint.add(point.getX() + bound.getWidth() - inset.getX());
        }
        //LOG.atInfo().log("tab points " + Arrays.deepToString(lstTabPoint.toArray()));
    }

    private void repaintPath(DragEvent event, int source) {
        boolean hasTab = !getTabs().isEmpty();
        if (hasTab && btnLeft.contains(btnLeft.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            pathModel.refresh(0, 0, DetachableTabPane.this.getWidth() / 2, DetachableTabPane.this.getHeight());
            pos = Pos.CENTER_LEFT;
        } else if (hasTab && btnRight.contains(btnRight.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            double pathWidth = DetachableTabPane.this.getWidth() / 2;
            pathModel.refresh(pathWidth, 0, pathWidth, DetachableTabPane.this.getHeight());
            pos = Pos.CENTER_RIGHT;
        } else if (hasTab && btnTop.contains(btnTop.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            pathModel.refresh(0, 0, getWidth(), getHeight() / 2);
            pos = Pos.TOP_CENTER;
        } else if (hasTab && btnBottom.contains(btnBottom.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            double pathHeight = getHeight() / 2;
            pathModel.refresh(0, pathHeight, getWidth(), getHeight() - pathHeight);
            pos = Pos.BOTTOM_CENTER;
        } else {
            pos = null;
            double tabpos = -1;
            for (int i = 1; i < lstTabPoint.size(); i++) {
                if (event.getX() < lstTabPoint.get(i)) {
                    tabpos = lstTabPoint.get(i - 1);
                    dropIndex = i - 1;
                    break;
                }
            }
            if (tabpos == -1) {
                int index = lstTabPoint.size() - 1;
                dropIndex = getTabs().size();
                if (index > -1) {
                    tabpos = lstTabPoint.get(index);
                }
            }
            // 35 to support arrow alignment because of a menubutton to add new tabs, which is 35 pixels wide.
            if (tabpos < 35) {
                tabpos = 35;
            }
//			LOG.atInfo().log("drop index: " + dropIndex);
            pathModel.refresh(tabpos, DetachableTabPane.this.getWidth(), DetachableTabPane.this.getHeight());
        }
    }

    private void clearGesture() {
        Node tabheader = getTabHeaderArea();
        if (tabheader == null) return;
        Set<Node> tabs = tabheader.lookupAll(".tab");
        for (Node node : tabs) {
            node.setOnDragDetected(null);
            node.setOnDragDone(null);
        }
    }

    private void addGesture(final TabPane tabPane, final Node node) {
        node.setOnDragDetected((MouseEvent e) -> {
            DetachableTab tab = (DetachableTab) tabPane.getSelectionModel().getSelectedItem();
            if (tab instanceof DetachableTab && !((DetachableTab) tab).isDetachable()) {
                return;
            }
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(node.snapshot(null, null));
            DetachableTabPane.DRAG_TAB_WIDTH = db.getDragView().getWidth();
            DetachableTabPane.DRAG_CONTENT_WIDTH = DetachableTabPane.this.getWidth();
            DetachableTabPane.DRAG_CONTENT_HEIGHT = DetachableTabPane.this.getHeight();
            Map<DataFormat, Object> dragContent = new HashMap<>();
            dragContent.put(DATA_FORMAT, "test");
            DetachableTabPane.DRAG_SOURCE = DetachableTabPane.this;
            DetachableTabPane.DRAG_SOURCE_INDEX = getTabs().indexOf(tab);
            DRAGGED_TAB = tab;
            getTabs().remove(DRAGGED_TAB);
            db.setContent(dragContent);
            e.consume();
        });

        node.setOnDragDone((DragEvent event) -> {
            if (event.isAccepted()) {
                if (DRAGGED_TAB != null && DRAGGED_TAB.getTabPane() == null) {
                    DetachableTab tab = DRAGGED_TAB;
                    new TabStage(tab, new Point2D(ScreenInfo.getMouseX() - 35, ScreenInfo.getMouseY() - 50));
                }
                if (DRAG_SOURCE.getScene() != null && DRAG_SOURCE.getScene().getWindow() instanceof TabStage) {
                    TabStage stage = (TabStage) DRAG_SOURCE.getScene().getWindow();
                    closeStageIfNeeded(stage);
                }
                if (DRAG_SOURCE.getTabs().isEmpty()) {
                    removeFromParent(DRAG_SOURCE);
                }
                DetachableTabPane.DRAG_SOURCE = null;
                DRAGGED_TAB = null;
            } else {
                // put back...
                DetachableTabPane.DRAG_SOURCE.getTabs().add(DRAG_SOURCE_INDEX, DRAGGED_TAB);
                DetachableTabPane.DRAG_SOURCE.getSelectionModel().select(DRAG_SOURCE_INDEX);
            }
            event.consume();
        });
    }

    private void closeStageIfNeeded(TabStage stage) {
        Set<Node> setNode = stage.getScene().getRoot().lookupAll(".tab-pane");
        boolean empty = true;
        for (Node nodeTabpane : setNode) {
            if (nodeTabpane instanceof DetachableTabPane) {
                if (!((DetachableTabPane) nodeTabpane).getTabs().isEmpty()) {
                    empty = false;
                    break;
                }
            }
        }

        if (empty) {
            //there is a case where lookup .tab-pane style doesn't return all TabPane. So we need to lookup by SplitPane and scan through it
            Set<Node> setSplitpane = stage.getScene().getRoot().lookupAll(".split-pane");
            for (Node nodeSplitpane : setSplitpane) {
                if (nodeSplitpane instanceof SplitPane) {
                    SplitPane asplitpane = (SplitPane) nodeSplitpane;
                    for (Node child : asplitpane.getItems()) {
                        if (child instanceof DetachableTabPane) {
                            DetachableTabPane dtp = (DetachableTabPane) child;
                            if (!dtp.getTabs().isEmpty()) {
                                empty = false;
                                break;
                            }
                        }
                    }
                }
                if (!empty) {
                    break;
                }
            }
        }
        if (empty) {
            stage.close();
        }
    }

    protected void removeFromParent() {
        removeFromParent(this);
    }

    protected void removeFromParent(DetachableTabPane tabPaneToRemove) {
        Optional<SplitPane> optionalSplitPane = findParentSplitPane(tabPaneToRemove);
        if (optionalSplitPane.isPresent()) {
            SplitPane sp = optionalSplitPane.get();
            if (!tabPaneToRemove.isCloseIfEmpty()) {
                DetachableTabPane sibling = findSibling(sp, tabPaneToRemove);
                if (sibling == null) {
                    return;
                }
                List<Tab> lstTab = new ArrayList(sibling.getTabs());
                sibling.getTabs().clear();
                tabPaneToRemove.getTabs().setAll(lstTab);
                tabPaneToRemove = sibling;
            }
            if (tabPaneToRemove.getParent() instanceof TabGroup) {
                sp.getItems().remove(tabPaneToRemove.getParent());
            } else {
                sp.getItems().remove(tabPaneToRemove);
            }
        }
    }

    private DetachableTabPane findSibling(SplitPane sp, DetachableTabPane tabPaneToRemove) {
        for (Node sibling : sp.getItems()) {
            if (tabPaneToRemove != sibling
                    && sibling instanceof DetachableTabPane
                    && tabPaneToRemove.getScope().equals(((DetachableTabPane) sibling).getScope())) {
                return (DetachableTabPane) sibling;
            }
        }
        for (Node sibling : sp.getItems()) {
            if (sibling instanceof SplitPane) {
                return findSibling((SplitPane) sibling, tabPaneToRemove);
            }
        }
        return null;
    }

    /**
     * Getter for {@link #setSceneFactory(javafx.util.Callback)}
     *
     * @return
     */
    Callback<TabGroup, Scene> getSceneFactory() {
        return this.sceneFactory;
    }

    /**
     * Set factory to generate the Scene. Default SceneFactory is provided and it
     * will generate a scene with TabPane as root node. Call this method if you
     * need to have a custom scene
     * <p>
     *
     * @param sceneFactory
     */
    void setSceneFactory(Callback<TabGroup, Scene> sceneFactory) {
        this.sceneFactory = sceneFactory;
    }

    /**
     * Getter for {@link #setStageOwnerFactory(javafx.util.Callback)}
     *
     * @return
     */
    Callback<Stage, Window> getStageOwnerFactory() {
        return stageOwnerFactory;
    }

    /**
     * By default, the stage owner is the stage that own the first TabPane. For
     * example, detaching a Tab will open a new Stage. The new stage owner is the
     * stage of the TabPane. Detaching a tab from the new stage will open another
     * stage. Their owner are the same which is the stage of the first TabPane.
     * <p>
     *
     * @param stageOwnerFactory
     */
    void setStageOwnerFactory(Callback<Stage, Window> stageOwnerFactory) {
        this.stageOwnerFactory = stageOwnerFactory;
    }

    boolean isCloseIfEmpty() {
        return closeIfEmpty;
    }

    void setCloseIfEmpty(boolean closeIfEmpty) {
        this.closeIfEmpty = closeIfEmpty;
    }

    TabGroupFactory getTabGroupFactory() {
        return tabGroupFactory;
    }

    /**
     * Factory object to create new DetachableTabPane. We can extends
     * {@link TabGroupFactory} and set it to this method when custom
     * initialization is needed. For example when we want to set different
     * TabClosingPolicy.
     *
     * @param tabGroupFactory
     */
    void setTabGroupFactory(TabGroupFactory tabGroupFactory) {
        if (tabGroupFactory == null) {
            throw new IllegalArgumentException("tabGroupFactory cannot null");
        }
        this.tabGroupFactory = tabGroupFactory;
    }

    private class TabStage extends Stage {

        private final TabGroup tabGroup;

        public TabStage(final DetachableTab oldTab, Point2D eventLocation) {
            super();
            DetachableTab newTab = new DetachableTab(oldTab.kometNode);

            tabGroup = tabGroupFactory.create(DetachableTabPane.this.detachableStack);
            initOwner(stageOwnerFactory.call(this));
            Scene scene = sceneFactory.call(tabGroup);

            scene.getStylesheets().addAll(DetachableTabPane.this.getScene().getStylesheets());
            setScene(scene);

            setX(eventLocation.getX() - (DRAG_TAB_WIDTH / 2));
            setY(eventLocation.getY());
            if (getOwner() instanceof Stage owner) {
                setTitle(owner.getTitle() + " sidecar");
            }
            show();
            tabGroup.getTabs().add(newTab);

            tabGroup.getSelectionModel().select(newTab);
            if (newTab.getContent() instanceof Parent) {
                ((Parent) newTab.getContent()).requestLayout();
            }

        }
    }
}
