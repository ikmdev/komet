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

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Measures how much wider a part of the scene has to be for the text showing in it to be read
 * in full: labels that end in an ellipsis and text flows that wrap onto more than one line.
 */
public final class TextFitUtils {

    private TextFitUtils() {}

    /**
     * Finds the text under a node that is the furthest from fitting on one line.
     *
     * @param root the node whose visible text is measured
     * @return the width that text is missing, or 0 when all the text fits
     */
    public static double getWidthShortfall(Node root) {
        if (!root.isVisible()) {
            return 0;
        }
        double shortfall = 0;
        if (root instanceof TextFlow textFlow) {
            return getTextFlowShortfall(textFlow);
        }
        if (root instanceof Labeled labeled) {
            shortfall = getLabeledShortfall(labeled);
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                shortfall = Math.max(shortfall, getWidthShortfall(child));
            }
        }
        return shortfall;
    }

    /** What a label's text lost to its ellipsis. */
    private static double getLabeledShortfall(Labeled labeled) {
        String fullText = labeled.getText();
        if (fullText == null || fullText.isEmpty()) {
            return 0;
        }
        for (Node child : labeled.getChildrenUnmodifiable()) {
            if (child instanceof Text shown && shown.isVisible() && !fullText.equals(shown.getText())) {
                return Math.max(0, Math.ceil(getTextWidth(fullText, shown.getFont()) - shown.getLayoutBounds().getWidth()));
            }
        }
        return 0;
    }

    /** What a text flow is short of to hold its content on one line. */
    private static double getTextFlowShortfall(TextFlow textFlow) {
        double lineWidth = textFlow.snappedLeftInset() + textFlow.snappedRightInset();
        for (Node child : textFlow.getChildrenUnmodifiable()) {
            if (!child.isManaged()) {
                continue;
            }
            lineWidth += child instanceof Text text ? getTextWidth(text.getText(), text.getFont()) : child.prefWidth(-1);
        }
        return Math.max(0, Math.ceil(lineWidth - textFlow.getWidth()));
    }

    private static double getTextWidth(String string, Font font) {
        Text text = new Text(string);
        text.setFont(font);
        return text.getLayoutBounds().getWidth();
    }
}
