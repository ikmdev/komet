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
package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

/**
 * Default skin for {@link KlWindowControlToolbar}. Builds the whole control bar — the coordinate menu
 * and timeline toggle on the leading edge, a growing spacer, then the {@code PROPERTY} label, properties
 * toggle, a vertical separator and the close button on the trailing edge — and binds each piece to the
 * control's state and action hooks.
 * <p>
 * All visuals come from CSS (see the {@code .kl-window-control-toolbar} and {@code .concept-header-control}
 * rules in {@code kview.css}); the skin sets only style classes and layout constraints, never inline style.
 */
public class KlWindowControlToolbarSkin extends SkinBase<KlWindowControlToolbar> {

    public KlWindowControlToolbarSkin(KlWindowControlToolbar control) {
        super(control);

        // Coordinate menu button
        MenuButton coordinatesMenuButton = control.getCoordinatesMenuButton();
        coordinatesMenuButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        coordinatesMenuButton.getStyleClass().add("coordinate");
        coordinatesMenuButton.setTooltip(new Tooltip("Coordinates"));
        coordinatesMenuButton.visibleProperty().bind(control.coordinateVisibleProperty());
        coordinatesMenuButton.managedProperty().bind(control.coordinateVisibleProperty());

        // Timeline (time travel) toggle.
        ToggleButton timelineToggleButton = new ToggleButton();
        timelineToggleButton.setMnemonicParsing(false);
        timelineToggleButton.getStyleClass().add("timeline");
        Region timelineIcon = new Region();
        timelineIcon.getStyleClass().addAll("icon", "timeline-icon");
        timelineToggleButton.setGraphic(timelineIcon);
        timelineToggleButton.setTooltip(new Tooltip("Time Travel"));
        timelineToggleButton.visibleProperty().bind(control.timelineVisibleProperty());
        timelineToggleButton.managedProperty().bind(control.timelineVisibleProperty());
        timelineToggleButton.selectedProperty().bindBidirectional(control.timelineSelectedProperty());

        Region spacer = new Region();
        spacer.setMinWidth(10);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text propertyLabel = new Text("PROPERTIES");
        propertyLabel.setStrokeType(StrokeType.OUTSIDE);
        propertyLabel.setStrokeWidth(0.0);
        propertyLabel.getStyleClass().add("properties-toggle");

        // Properties toggle, drawn as a toggle switch (body + knob).
        ToggleButton propertiesToggleButton = new ToggleButton();
        propertiesToggleButton.setMnemonicParsing(false);
        Rectangle toggleBody = new Rectangle(32, 20);
        toggleBody.setArcWidth(5);
        toggleBody.setArcHeight(5);
        toggleBody.setStrokeType(StrokeType.INSIDE);
        toggleBody.getStyleClass().add("toggle-switch-body");
        Ellipse toggleKnob = new Ellipse(10, 10, 8, 8);
        toggleKnob.setStrokeType(StrokeType.INSIDE);
        toggleKnob.getStyleClass().add("property-toggle-switch");
        propertiesToggleButton.setGraphic(new Group(toggleBody, toggleKnob));
        propertiesToggleButton.selectedProperty().bindBidirectional(control.propertiesSelectedProperty());

        Separator separator = new Separator(Orientation.VERTICAL);
        separator.getStyleClass().add("thin-vertical-separator");

        Button closeButton = new Button();
        closeButton.setMnemonicParsing(false);
        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("close-window");
        closeButton.setGraphic(closeIcon);
        closeButton.setOnAction(event -> {
            Runnable onClose = control.getOnCloseAction();
            if (onClose != null) {
                onClose.run();
            }
        });

        HBox container = new HBox();
        container.getStyleClass().addAll("concept-header-control", "rounded-upper-right-only");
        container.getChildren().addAll(
                coordinatesMenuButton,
                timelineToggleButton,
                spacer,
                propertyLabel,
                propertiesToggleButton,
                separator,
                closeButton);

        getChildren().add(container);
    }
}