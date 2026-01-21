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
package dev.ikm.komet.framework.view;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;

import java.util.List;

public class ViewMenuModel {
    private ViewProperties viewProperties;
    private Control baseControlToShowOverride;
    private Shape baseControlGraphic;
    private Menu coordinateMenu;
    private MenuButton coordinateMenuButton;
    private String oldFill = null;
    private final ChangeListener<ViewCoordinateRecord> viewChangedListener = this::viewCoordinateChanged;
    // whichMenu added to help with debugging
    private String whichMenu = "Unknown";

    /**
     * Default true to style button for classic Komet. if using the new constructor using a MenuButton it will be set to false.
     */
    private boolean isClassicViewCoordMenuButton = true;

    public ViewMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride) {
        this(viewProperties, baseControlToShowOverride, new Menu("Coordinates", Icon.VIEW.makeIcon()), null);
    }


    public ViewMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride, Menu coordinateMenu, String whichMenu) {
        this.coordinateMenu = coordinateMenu;
        this.coordinateMenuButton = null;
        initialize(viewProperties, baseControlToShowOverride, whichMenu);
    }

    public ViewMenuModel(ViewProperties viewProperties, MenuButton coordinateMenuButton, String whichMenu) {
        this.coordinateMenu = null;
        this.coordinateMenuButton = coordinateMenuButton;

        // set to the new styling for next gen.
        isClassicViewCoordMenuButton = false;
        initialize(viewProperties, coordinateMenuButton, whichMenu);
    }

    /**
     * TODO: New figma designs will replace these next gen komet view coordinate (bullseye) context menu popups.
     * Classic Komet's view coordinate filter options (starburst icon).
     * @param viewProperties ViewProperties containing view coordinates and view calculators.
     * @param baseControlToShowOverride The starburst combobox control to indicate using the color Orange showing user is overriding filter options.
     * @param whichMenu The classic menu options to update or ignore when override is set so parent won't update child menus.
     */
    private void initialize(ViewProperties viewProperties, Control baseControlToShowOverride, String whichMenu) {
        this.viewProperties = viewProperties;
        this.viewProperties.nodeView().addListener(this.viewChangedListener);
        ViewCalculatorWithCache viewCalculator = ViewCalculatorWithCache.getCalculator(this.viewProperties.nodeView().getValue());
        FxGet.pathCoordinates(viewCalculator).addListener((MapChangeListener<PublicIdStringKey, StampPathImmutable>) change ->
                updateCoordinateMenu(isClassicViewCoordMenuButton)
        );

        this.baseControlToShowOverride = baseControlToShowOverride;
        if (isClassicViewCoordMenuButton) {
            if (baseControlToShowOverride instanceof Labeled) {
                Node graphic = ((Labeled) this.baseControlToShowOverride).getGraphic();
                if (graphic instanceof AnchorPane) {
                    Node childZero = ((AnchorPane) graphic).getChildren().get(0);
                    this.baseControlGraphic = (Shape) childZero;
                } else {
                    baseControlGraphic = null;
                }
            } else {
                this.baseControlGraphic = null;
            }
        }

        if (whichMenu != null) {
            this.whichMenu = whichMenu;
        }

        updateCoordinateMenu(isClassicViewCoordMenuButton);
    }

    /**
     * @Deprecated Use psuedo state styling in CSS instead. Please look at ChapterWindowHelper for an example of using psuedo states.
     * Change color to indicate it's been overwritten.
     */
    private void styleNextGenMenuButton() {
        if (this.viewProperties.nodeView().hasOverrides()) {
            this.baseControlToShowOverride.getStyleClass().remove("override");
            this.baseControlToShowOverride.getStyleClass().add("override");
        } else {
            this.baseControlToShowOverride.getStyleClass().remove("override");
        }
    }

    private void styleClassicMenuButton() {
        if (this.viewProperties.nodeView().hasOverrides()) {
            if (this.baseControlGraphic != null) {
                if (this.oldFill == null) {
                    this.oldFill = this.baseControlGraphic.getFill().toString().replace("0x", "#");
                }
                this.baseControlGraphic.setStyle("-fx-font-family: 'Material Design Icons'; -fx-font-size: 18.0; -icons-color: #ff9100;");
            } else {
                this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, #ff9100;");
            }
        } else {
            if (this.baseControlGraphic != null) {
                this.baseControlGraphic.setStyle("-fx-font-family: 'Material Design Icons'; -fx-font-size: 18.0; -icons-color: " +
                        this.oldFill + ";");
            } else {
                this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;");
            }
        }
    }
    public void updateCoordinateMenu(boolean isClassicViewCoordinate) {
        if (isClassicViewCoordinate) {
            styleClassicMenuButton();
        } else {
            styleNextGenMenuButton();
        }

        Platform.runLater(() -> {
            List<MenuItem> menuItems = null;

            if (this.coordinateMenu != null) {
                menuItems = coordinateMenu.getItems();
            } else if (coordinateMenuButton != null) {
                menuItems = coordinateMenuButton.getItems();
            }

            if (menuItems != null) {
                ViewCalculatorWithCache viewCalculator = ViewCalculatorWithCache.getCalculator(this.viewProperties.nodeView().getValue());
                var viewMenuTask = new ViewMenuTask(viewCalculator, viewProperties.nodeView(), whichMenu);

                final List<MenuItem> finalMenuItems = menuItems;
                TinkExecutor.threadPool().execute(TaskWrapper.make(viewMenuTask,
                        (List<MenuItem> result) -> {
                            Platform.runLater(() -> {
                                finalMenuItems.clear();
                                finalMenuItems.addAll(result);
                            });
                        }));
            }
        });
    }

    public Menu getCoordinateMenu() {
        return coordinateMenu;
    }

    private void viewCoordinateChanged(ObservableValue<? extends ViewCoordinateRecord> observable,
                                       ViewCoordinateRecord oldValue,
                                       ViewCoordinateRecord newValue) {
        updateCoordinateMenu(this.isClassicViewCoordMenuButton);

    }


}
