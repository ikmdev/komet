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
package dev.ikm.komet.amplify.commons;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.amplify.commons.ViewportHelper.clipChildren;

/**
 * Slide out tray helper is a utility class to create JavaFX slide out tray capability.
 * e.g. Pressing the search button on a sidebar control will slide out a tray panel allowing the user to search.
 */
public class SlideOutTrayHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SlideOutTrayHelper.class);
    private SlideOutTrayHelper() {}

    /**
     * The ability to setup a slide out tray pane that will contain only one child node (nodePanel).
     * @param nodePanel The pane to be added to the slide out tray pane.
     * @param trayPane The tray pane that will hold a single child node.
     */
    public static void setupSlideOutTrayPane(Pane nodePanel, Pane trayPane) {
        double width = nodePanel.getWidth();
        trayPane.getChildren().add(nodePanel);
        clipChildren(trayPane, 0);
        nodePanel.setLayoutX(-width);
        trayPane.setMaxWidth(0);
    }

    /**
     * Perform an animated slide out effect with the tray pane.
     * @param trayPane A tray pane consisting of one child (the panel to display)
     */
    public static void slideOut(Pane trayPane) {
        Pane currentViewPane = (Pane) trayPane.getChildren().get(0);
        double width = currentViewPane.getBoundsInLocal().getWidth();
        currentViewPane.setLayoutX(width);

        clipChildren(trayPane, 0);
        currentViewPane.setLayoutX(-width);
        trayPane.setMaxWidth(0);

        KeyValue xCoord = new KeyValue(currentViewPane.layoutXProperty(), 0, Interpolator.EASE_IN);
        KeyValue slideOutWidth = new KeyValue(trayPane.maxWidthProperty(), width, Interpolator.EASE_IN);
        EventHandler<ActionEvent> onFinish = actionEvent -> {
            LOG.debug("slide out complete");
            currentViewPane.layoutXProperty().set(0);
            trayPane.setMaxWidth(-1);
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(250), onFinish, slideOutWidth, xCoord);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    /**
     * Perform an animated slide in effect with the tray pane.
     * @param trayPane A tray pane consisting of one child (the panel to display)
     */
    public static void slideIn(Pane trayPane) {
        Node currentViewPane = trayPane.getChildren().get(0);
        double width = currentViewPane.getBoundsInLocal().getWidth();
        trayPane.maxWidthProperty().set(width);
        currentViewPane.layoutXProperty().set(0);
        KeyValue xCoord = new KeyValue(currentViewPane.layoutXProperty(), -width);
        KeyValue slideOutWidth = new KeyValue(trayPane.maxWidthProperty(), 0);
        EventHandler<ActionEvent> onFinish = actionEvent -> {
            LOG.debug("slide in complete ");
            currentViewPane.layoutXProperty().set(-width);
            trayPane.maxWidthProperty().set(0);
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(250), onFinish, slideOutWidth, xCoord);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    /**
     * Perform an animated slide out effect with the tray pane beside (right side) the owning pane.
     * @param trayPane A tray pane consisting of one child (the panel to display)
     * @param owningPanel The owning panel in display will have the tray pane attached to its right side.
     */
    public static void slideOut(Pane trayPane, Pane owningPanel) {
        Node propPane = trayPane.getChildren().get(0);
        double width = propPane.getBoundsInLocal().getWidth();

        // in the negative position left of view port
        propPane.layoutXProperty().set(-width);
        trayPane.maxWidthProperty().set(0);
        KeyValue xCoord = new KeyValue(propPane.layoutXProperty(), 0, Interpolator.EASE_IN);
        DoubleProperty prefWidth = owningPanel.prefWidthProperty();
        KeyValue detailsPaneWidth = new KeyValue(prefWidth, prefWidth.get() + width, Interpolator.EASE_IN);
        KeyValue slideOutWidth = new KeyValue(trayPane.maxWidthProperty(), width, Interpolator.EASE_IN);
        EventHandler<ActionEvent> onFinish = actionEvent -> {
            propPane.layoutXProperty().set(0);
            trayPane.setMaxWidth(-1);
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(250), onFinish, detailsPaneWidth, slideOutWidth, xCoord);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }
    public static boolean isOpen(Pane trayPane) {
        return !isClosed(trayPane);
    }
    public static boolean isClosed(Pane trayPane) {
        return trayPane.maxWidthProperty().get() == 0;
    }
    /**
     * Perform an animated slide in effect with the tray pane beside (right side) the owning pane.
     * @param trayPane A tray pane consisting of one child (the panel to display)
     * @param owningPanel The owning panel in display will have the tray pane attached to its right side.
     */
    public static void slideIn(Pane trayPane, Pane owningPanel) {

        Node propPane = trayPane.getChildren().get(0);
        double width = propPane.getBoundsInLocal().getWidth();

        trayPane.maxWidthProperty().set(width);
        propPane.layoutXProperty().set(0);
        KeyValue xCoord = new KeyValue(propPane.layoutXProperty(), -width);

        DoubleProperty prefWidth = owningPanel.prefWidthProperty();
        KeyValue detailsPaneWidth = new KeyValue(prefWidth, prefWidth.get() - width, Interpolator.EASE_IN);

        KeyValue slideOutWidth = new KeyValue(trayPane.maxWidthProperty(), 0);
        EventHandler<ActionEvent> onFinish = actionEvent -> {
            LOG.info("slide in complete ");
            propPane.layoutXProperty().set(-width);
            trayPane.maxWidthProperty().set(0);
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(250), onFinish, detailsPaneWidth, slideOutWidth, xCoord);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();

    }
}
