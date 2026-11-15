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
package dev.ikm.komet.framework.controls;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * A mark drawn as a tile: a filled square with rounded corners and a sign in white strokes on it.
 *
 * <p>A kind sigil is a coloured letter and a status mark a coloured glyph, both of the weight of
 * the text they stand in. A tile is heavier than either and of a shape neither has, so what it
 * marks is seen before the text is read, and is told from them where colour is not, as on a page
 * printed in gray. It is drawn, not set in a face, so it is the same wherever it is shown.
 */
public final class MarkTile {

    /** The style class every tile has. */
    public static final String STYLE_CLASS = "mark-tile";

    /** The complex concept's sign: an upright and a level stroke that cross, the disclosure's plus. */
    private static final double[][] COMPLEX_CONCEPT = {{0.5, 0.2, 0.5, 0.8}, {0.2, 0.5, 0.8, 0.5}};

    private MarkTile() {
    }

    /**
     * A tile.
     *
     * @param size    the tile's edge, in px
     * @param fillHex the tile's colour, as a web hex string
     * @param strokes the sign's strokes, each as {@code x1, y1, x2, y2} in parts of the edge
     * @return the tile, of a fixed size
     */
    public static Pane of(double size, String fillHex, double[][] strokes) {
        Rectangle tile = new Rectangle(size, size, Color.web(fillHex));
        tile.setArcWidth(size * 0.4);
        tile.setArcHeight(size * 0.4);
        Pane pane = new Pane(tile);
        double width = Math.max(1.3, size * 0.13);
        for (double[] stroke : strokes) {
            Line line = new Line(stroke[0] * size, stroke[1] * size, stroke[2] * size, stroke[3] * size);
            line.setStroke(Color.WHITE);
            line.setStrokeWidth(width);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
            pane.getChildren().add(line);
        }
        pane.getStyleClass().add(STYLE_CLASS);
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
        return pane;
    }

    /**
     * The complex concept's tile, in the colour of {@link KonceptStatus#COMPLEX}.
     *
     * @param size the tile's edge, in px
     * @return the tile
     */
    public static Pane complexConcept(double size) {
        return of(size, KonceptStatus.COMPLEX_COLOR_HEX, COMPLEX_CONCEPT);
    }
}
