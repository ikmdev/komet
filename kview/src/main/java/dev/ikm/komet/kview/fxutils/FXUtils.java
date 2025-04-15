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
package dev.ikm.komet.kview.fxutils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Window;

import java.util.Optional;

public class FXUtils {


    /**
     * Given a Text node including its CSS styling this function can determine the bounds.
     * This allows you to create callouts and predetermine the width and height of a Text node.
     * @param origText - A Text node to measure.
     * @return Bounds The bounds of a Text Node.
     */
    public static Bounds textFontMetricsBounds(final Text origText) {
        Text text = new Text(origText.getText());
        text.getStyleClass().addAll(origText.getStyleClass());
        text.setStyle(origText.getStyle());

        new Scene(new Group(text));
        text.applyCss();
        return text.getLayoutBounds();
    }

    /**
     * Find the bounds of a node relative to a parent levels up the hiearchy. For example
     * A Parent(Pane) can have a child (HBox) that contains a Node (Button).
     * <pre>
     *     Bounds b = localToParent(targetNode, 2);
     *     double x = b.getMinX();
     *     double y = b.getMinY();
     * </pre>
     * @param node The target node to find out it's bounds relative to a parent up the graph.
     * @param levelsUp How many levels up
     * @return
     */
    public static Bounds localToParent(Node node, int levelsUp) {
        Bounds bounds = node.getBoundsInParent();
        Parent parent = node.getParent();

        for (int i = 0; i < levelsUp; i++) {
            bounds = parent.localToParent(bounds);
            parent = parent.getParent();
        }
        return bounds;
    }

    /**
     * Fits an ImageView to the given bounds if the Image width or height doesn't fit
     * within those bounds. Otherwise, sets the ImageView to the original dimensions of
     * its Image.
     *
     * @param imageView the ImageView we want to update.
     * @param maxImageWidth the maximum image bounds width.
     * @param maxImageHeight the maximum image bounds height.
     */
    public static void fitImageToBounds(ImageView imageView, int maxImageWidth, int maxImageHeight) {
        Image image = imageView.getImage();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (imageWidth > maxImageWidth || imageHeight > maxImageHeight) {
            imageView.setFitWidth(maxImageWidth);
            imageView.setFitHeight(maxImageHeight);
        } else {
            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageHeight);
        }
    }

    public static Bounds localToParent(Node node, Parent targetParent) {
        Bounds bounds = node.getBoundsInParent();
        Parent parent = node.getParent();

        while (!targetParent.equals(parent)) {
            bounds = parent.localToParent(bounds);
            parent = parent.getParent();
        }
        return bounds;
    }

    public static Point2D centerPointRelativeToParent(Circle circle, Pane targetParent) {
        Bounds bounds = localToParent(circle, targetParent);
        return new Point2D(bounds.getCenterX(), bounds.getCenterY());
    }

    public static Optional<Parent> findParent(Node child, String styleClass) {
        Parent parent = child.getParent();
        if (parent == null){
            return Optional.empty();
        } else if(parent.getStyleClass().contains(styleClass)) {
            return Optional.of(parent);
        } else {
            return findParent(parent, styleClass);
        }

    }

    /**
     * Retrieves the currently focused {@link Window}.
     * <p>
     * This method iterates through all available windows and returns the first window
     * that is currently focused. If no window is focused, it returns {@code null}.
     *
     * @return the focused {@link Window}, or {@code null} if no window is currently focused.
     */
    public static Window getFocusedWindow() {
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }
}
