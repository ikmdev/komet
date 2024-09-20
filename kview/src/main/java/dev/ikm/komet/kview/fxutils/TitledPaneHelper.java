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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class TitledPaneHelper {
    private static final int DEFAULT_BUTTON_SPACING = 34; // pixels to offset the right arrow dropdown


    /**
     * pass in the title and arrow css selectors since the behavior will apply to nested TitledPanes
     * if you don't specific the selector more specifically
     * Code credit
     * https://stackoverflow.com/a/55085777
     * @param pane
     */
    public static void putArrowOnRight(TitledPane pane, double buttonSpacing, String titleSelector, String arrowSelector) {
        pane.layout();
        pane.applyCss();
        Region title = (Region) pane.lookup(titleSelector);
        Region arrow = (Region) title.lookup(arrowSelector);
        Text text = (Text) title.lookup(".text");

        arrow.translateXProperty().bind(Bindings.createDoubleBinding(() -> {
            double rightInset = title.getPadding().getRight() + buttonSpacing;
            return title.getWidth() - arrow.getLayoutX() - arrow.getWidth() - rightInset;
        }, title.paddingProperty(), title.widthProperty(), arrow.widthProperty(), arrow.layoutXProperty()));
        arrow.setStyle("-fx-padding: 0.0em 0.0em 0.0em 0.583em;");

        DoubleBinding textGraphicBinding = Bindings.createDoubleBinding(() -> {
            switch (pane.getAlignment()) {
                case TOP_CENTER:
                case CENTER:
                case BOTTOM_CENTER:
                case BASELINE_CENTER:
                    return 0.0;
                default:
                    return -(arrow.getWidth());
            }
        }, arrow.widthProperty(), pane.alignmentProperty());
        text.translateXProperty().bind(textGraphicBinding);

        pane.graphicProperty().addListener((observable, oldGraphic, newGraphic) -> {
            if (oldGraphic != null) {
                oldGraphic.translateXProperty().unbind();
                oldGraphic.setTranslateX(0);
            }
            if (newGraphic != null) {
                newGraphic.translateXProperty().bind(textGraphicBinding);
            }
        });
        if (pane.getGraphic() != null) {
            pane.getGraphic().translateXProperty().bind(textGraphicBinding);
        }
    }

    /**
     * default params wrapper method for putArrowOnRight
     * @param pane
     */
    public static void putArrowOnRight(TitledPane pane) {
        putArrowOnRight(pane, DEFAULT_BUTTON_SPACING, ".title", ".arrow-button");
    }
}
