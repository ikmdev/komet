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
package dev.ikm.komet.framework;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.context.AddToContextMenu;
import dev.ikm.komet.framework.context.AddToContextMenuSimple;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class TopPanelFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TopPanelFactory.class);

    public static TopPanelParts make(ViewProperties viewProperties,
                                     SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                     SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                                     Node centerNode) {
        ViewCalculatorWithCache viewCalculator =
                ViewCalculatorWithCache.getCalculator(viewProperties.nodeView().getValue());
        MenuButton viewPropertiesMenuButton = makeViewMenuButton(viewProperties, activityStreamKeyProperty, optionForActivityStreamKeyProperty);
        Menu activityStreamMenu = new Menu("Activity stream", Icon.ACTIVITY.makeIcon());

        updatePublishOnlyActivityStreamMenu(activityStreamKeyProperty,
                optionForActivityStreamKeyProperty,
                activityStreamMenu);


        activityStreamKeyProperty.addListener((observable,
                                               oldValue,
                                               newValue) -> {
            Platform.runLater(() -> updatePublishOnlyActivityStreamMenu(activityStreamKeyProperty, optionForActivityStreamKeyProperty,
                    activityStreamMenu));
        });
        viewPropertiesMenuButton.getItems().add(activityStreamMenu);

        GridPane gridPane = new GridPane();
        //gridPane.getStyleClass().add("top-panel");
        updateGridPane(gridPane,
                viewPropertiesMenuButton,
                centerNode);
        return new TopPanelParts(gridPane, viewPropertiesMenuButton, activityStreamMenu);
    }

    public static MenuButton makeViewMenuButton(ViewProperties viewProperties,
                                                SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                                SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty) {

        MenuButton viewPropertiesMenuButton = new MenuButton();
        Menu coordinatesMenu = new Menu("Coordinates", Icon.COORDINATES.makeIcon());

        ViewMenuModel viewMenuModel = new ViewMenuModel(viewProperties, viewPropertiesMenuButton, coordinatesMenu);
        viewPropertiesMenuButton.getProperties().put("viewMenuModel", viewMenuModel);
        viewPropertiesMenuButton.getItems().add(coordinatesMenu);
        viewPropertiesMenuButton.setGraphic(Icon.VIEW.makeIcon());


        return viewPropertiesMenuButton;
    }

    private static void updatePublishOnlyActivityStreamMenu(SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                                            SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                                                            Menu activityStreamMenu) {
        activityStreamMenu.getItems().clear();
        MenuItem currentActivityMenuItem = new MenuItem();
        activityStreamMenu.getItems().add(currentActivityMenuItem);

        if (activityStreamKeyProperty.get().equals(ActivityStreams.UNLINKED)) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption(),
                    ActivityStreams.getActivityIcon(activityStreamKeyProperty.get())));
            currentActivityMenuItem.setText("Unlinked");
        } else if (optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.PUBLISH.keyForOption())) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(ActivityStreams.getActivityIcon(activityStreamKeyProperty.get()),
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption()));
            currentActivityMenuItem.setText("Publishing to " + activityStreamKeyProperty.get().getString());
        } else {
            throw new IllegalStateException(optionForActivityStreamKeyProperty.get().toString());
        }
        activityStreamMenu.getItems().add(new SeparatorMenuItem());

        for (PublicIdStringKey<ActivityStream> key : ActivityStreams.KEYS) {
            Menu optionsForStreamMenu = new Menu(key.getString(), ActivityStreams.getActivityIcon(key));
            for (ActivityStreamOption activityStreamOption : ActivityStreamOption.optionsForStream(key)) {
                if (activityStreamOption.equals(ActivityStreamOption.PUBLISH)) {
                    activityStreamMenu.getItems().add(optionsForStreamMenu);
                    MenuItem activityStreamOptionItem = new MenuItem(activityStreamOption.keyForOption().getString(),
                            activityStreamOption.iconForOption());
                    optionsForStreamMenu.getItems().add(activityStreamOptionItem);
                    activityStreamOptionItem.setOnAction(event -> {
                        activityStreamKeyProperty.set(key);
                        optionForActivityStreamKeyProperty.set(activityStreamOption.keyForOption());
                    });
                }
            }
        }
    }

    private static void updateGridPane(GridPane gridPane,
                                       MenuButton viewPropertiesButton,
                                       Node centerNode) {
        gridPane.getChildren().clear();
        gridPane.add(viewPropertiesButton, 0, 0, 2, 1);

        GridPane.setHgrow(centerNode, Priority.ALWAYS);
        GridPane.setVgrow(centerNode, Priority.ALWAYS);
        GridPane.setFillHeight(centerNode, true);
        GridPane.setFillWidth(centerNode, true);
        if (centerNode instanceof Region centerRegion) {
            centerRegion.setMaxWidth(Double.MAX_VALUE);
            centerRegion.setPrefWidth(Double.MAX_VALUE);
            centerRegion.setMaxHeight(Double.MAX_VALUE);
            if (centerRegion instanceof Labeled centerLabeled) {
                centerLabeled.setAlignment(Pos.TOP_LEFT);
            }
        }
        GridPane.setValignment(centerNode, VPos.TOP);

        gridPane.add(centerNode, 2, 0, 4, 2);

    }

    private static void updateActivityStreamMenu(SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                                 SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                                                 Menu activityStreamMenu,
                                                 SimpleBooleanProperty focusOnActivity) {
        activityStreamMenu.getItems().clear();
        MenuItem currentActivityMenuItem = new MenuItem();
        activityStreamMenu.getItems().add(currentActivityMenuItem);

        if (activityStreamKeyProperty.get().equals(ActivityStreams.UNLINKED)) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption(),
                    ActivityStreams.getActivityIcon(activityStreamKeyProperty.get())));
            currentActivityMenuItem.setText("Unlinked");
        } else if (optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.PUBLISH.keyForOption())) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(ActivityStreams.getActivityIcon(activityStreamKeyProperty.get()),
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption()));
            currentActivityMenuItem.setText("Publishing to " + activityStreamKeyProperty.get().getString());
        } else if (optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.SYNCHRONIZE.keyForOption())) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(ActivityStreams.getActivityIcon(activityStreamKeyProperty.get()),
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption()));
            currentActivityMenuItem.setText("Synchronizing with " + activityStreamKeyProperty.get().getString());
        } else if (optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.SUBSCRIBE.keyForOption())) {
            currentActivityMenuItem.setGraphic(Icon.makeIconGroup(ActivityStreams.getActivityIcon(activityStreamKeyProperty.get()),
                    ActivityStreamOption.get(optionForActivityStreamKeyProperty.get()).iconForOption()));
            currentActivityMenuItem.setText("Subscribed to " + activityStreamKeyProperty.get().getString());
        } else {
            throw new IllegalStateException(optionForActivityStreamKeyProperty.get().toString());
        }
        if (focusOnActivity != null) {
            MenuItem focusOnActivityMenuItem = new MenuItem("Focus on activity");
            if (focusOnActivity.get()) {
                focusOnActivityMenuItem.setText("Focus tab on activity");
                focusOnActivityMenuItem.setGraphic(Icon.EYE.makeIcon());
            } else {
                focusOnActivityMenuItem.setText("Don't focus tab on activity");
                focusOnActivityMenuItem.setGraphic(Icon.EYE_SLASH.makeIcon());
            }

            activityStreamMenu.getItems().add(focusOnActivityMenuItem);
        }
        activityStreamMenu.getItems().add(new SeparatorMenuItem());

        for (PublicIdStringKey<ActivityStream> key : ActivityStreams.KEYS) {
            Menu optionsForStreamMenu = new Menu(key.getString(), ActivityStreams.getActivityIcon(key));
            activityStreamMenu.getItems().add(optionsForStreamMenu);
            for (ActivityStreamOption activityStreamOption : ActivityStreamOption.optionsForStream(key)) {
                MenuItem activityStreamOptionItem = new MenuItem(activityStreamOption.keyForOption().getString(),
                        activityStreamOption.iconForOption());
                optionsForStreamMenu.getItems().add(activityStreamOptionItem);
                activityStreamOptionItem.setOnAction(event -> {
                    activityStreamKeyProperty.set(key);
                    optionForActivityStreamKeyProperty.set(activityStreamOption.keyForOption());
                });
            }
        }
    }

    public static Node make(ViewProperties viewProperties,
                            SimpleObjectProperty<EntityFacade> entityFocusProperty,
                            SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                            SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                            boolean focusOnActivity) {

        ViewCalculatorWithCache viewCalculator =
                ViewCalculatorWithCache.getCalculator(viewProperties.nodeView().getValue());

        SimpleBooleanProperty focusOnActivityProperty = new SimpleBooleanProperty(focusOnActivity);
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("top-panel");

        SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty();
        Runnable unlink = () -> {
        };
        AddToContextMenu[] contextMenuProviders = new AddToContextMenu[]{new AddToContextMenuSimple()};

        EntityLabelWithDragAndDrop entityLabel = EntityLabelWithDragAndDrop.make(viewProperties,
                entityFocusProperty, null, selectionIndexProperty, unlink,
                contextMenuProviders);


        MenuButton viewPropertiesMenuButton = makeViewMenuButton(viewProperties, activityStreamKeyProperty, optionForActivityStreamKeyProperty);
        Menu activityStreamMenu = new Menu("Activity stream", Icon.ACTIVITY.makeIcon());
        activityStreamKeyProperty.addListener((observable,
                                               oldValue,
                                               newValue) -> {
            if (newValue != null) {
                ActivityStream newActivityStream = ActivityStreams.get(newValue);
                ImmutableList<EntityFacade> lastDispatch = newActivityStream.lastDispatch();
                int selectionIndex = selectionIndexProperty.get();
                if (selectionIndex > -1 && selectionIndex < lastDispatch.size()) {
                    EntityFacade entityFacade = lastDispatch.get(selectionIndex);
                    entityFocusProperty.set(entityFacade);
                }
            }
            Platform.runLater(() -> updateAll(activityStreamKeyProperty, optionForActivityStreamKeyProperty, gridPane,
                    viewPropertiesMenuButton, activityStreamMenu, focusOnActivityProperty, entityLabel));
        });
        viewPropertiesMenuButton.getItems().add(activityStreamMenu);

        updateAll(activityStreamKeyProperty, optionForActivityStreamKeyProperty, gridPane, viewPropertiesMenuButton,
                activityStreamMenu, focusOnActivityProperty, entityLabel);

        optionForActivityStreamKeyProperty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> updateAll(activityStreamKeyProperty, optionForActivityStreamKeyProperty, gridPane,
                    viewPropertiesMenuButton, activityStreamMenu, focusOnActivityProperty, entityLabel));
        });

        entityFocusProperty.addListener((observable, oldValue, newValue) -> {
            if (focusOnActivityProperty.get()) {
                if (!ScreenInfo.isMousePressed() & !ScreenInfo.mouseWasDragged()) {
                    selectTab(gridPane);
                } else {
                    Timer timer = new Timer();

                    timer.schedule(new TimerTask() {
                        boolean mouseWasDragged = false;

                        @Override
                        public void run() {
                            if (ScreenInfo.isMousePressed()) {
                                // come back later and check. ;
                                //LOG.info("Mouse is pressed. Come back later and check again. ");
                                mouseWasDragged = ScreenInfo.mouseWasDragged();
                                if (mouseWasDragged) {
                                    //LOG.info("Mouse was dragged.");
                                }
                            } else {
                                if (mouseWasDragged || ScreenInfo.mouseWasDragged()) {
                                    // Don't focus if from a drag event.
                                    //LOG.info("No focus change because mouse was dragged.");
                                } else {
                                    Platform.runLater(() -> selectTab(gridPane));
                                }
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    }, 500, 500);
                }
            }
        });
        // show the current activity stream at the top
        return gridPane;
    }

    private static void selectTab(GridPane gridPane) {
        Parent parentNode = gridPane.getParent();
        Node tabContentRegion = null;

        while (parentNode != null && !(parentNode instanceof TabPane)) {
            parentNode = parentNode.getParent();

            try {
                if (parentNode !=null && parentNode.getStyleClass().contains("tab-content-area")) {
                    tabContentRegion = parentNode;
                }else if(parentNode == null) {
                    LOG.error("ALERT! - Parent Node is null, a subscription in details view window has been closed.");
                }
            }catch(Throwable e) {
                LOG.error("Parent Node should never be null...", e);
            }
        }
        if (parentNode != null && parentNode instanceof TabPane tabPane) {
            ObservableList<Tab> tabList = tabPane.getTabs();

            for (Tab t : tabList) {
                if (t.getContent().getParent().equals(tabContentRegion)) {
                    tabPane.getSelectionModel().select(t);
                    break;
                }
            }
        }
    }

    private static void updateFocusOnActivityMenu(SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                                  SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                                                  GridPane gridPane,
                                                  MenuButton viewPropertiesButton,
                                                  Menu activityStreamMenu,
                                                  SimpleBooleanProperty focusOnActivity,
                                                  EntityLabelWithDragAndDrop entityLabel) {
        MenuItem focusOnActivityMenuItem = new MenuItem("Focus on activity");
        focusOnActivityMenuItem.setOnAction(event -> {
            focusOnActivity.set(!focusOnActivity.get());
            Platform.runLater(() -> updateAll(activityStreamKeyProperty, optionForActivityStreamKeyProperty, gridPane,
                    viewPropertiesButton, activityStreamMenu, focusOnActivity, entityLabel));
        });
        if (focusOnActivity.get()) {
            focusOnActivityMenuItem.setText("Don't focus tab on activity");
            focusOnActivityMenuItem.setGraphic(Icon.EYE_SLASH.makeIcon());
        } else {
            focusOnActivityMenuItem.setText("Focus tab on activity");
            focusOnActivityMenuItem.setGraphic(Icon.EYE.makeIcon());
        }

        activityStreamMenu.getItems().add(focusOnActivityMenuItem);
    }

    private static void updateAll(SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                                  SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty,
                                  GridPane gridPane,
                                  MenuButton viewPropertiesButton,
                                  Menu activityStreamMenu,
                                  SimpleBooleanProperty focusOnActivity,
                                  EntityLabelWithDragAndDrop entityLabel) {
        updateGridPane(gridPane,
                viewPropertiesButton,
                entityLabel);
        updateActivityStreamMenu(activityStreamKeyProperty, optionForActivityStreamKeyProperty, activityStreamMenu, focusOnActivity);
        if (focusOnActivity != null) {
            updateFocusOnActivityMenu(activityStreamKeyProperty,
                    optionForActivityStreamKeyProperty,
                    gridPane,
                    viewPropertiesButton,
                    activityStreamMenu,
                    focusOnActivity,
                    entityLabel);
        }

    }

    public static record TopPanelParts(Node topPanel, MenuButton viewPropertiesMenuButton, Menu activityStreamMenu) {

    }
}
