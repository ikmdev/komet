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

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.layout.Region;

/**
 * The JavaFX rendering of the STAMP {@link ComponentKind} sigil — the point-up pentagon with one
 * reading dot per axis and a centre hub, drawn from the locked {@link StampSigilGeometry}.
 *
 * <p>This is the live adapter of the geometry; the same numbers render the adoc SVG badge and the
 * Zulip / email PNG. The node is a fixed square sized for an inline ({@value #DEFAULT_SIZE}px)
 * instance — the dots are floored so they stay visible rather than scaling to specks at small sizes.
 *
 * @see StampSigilGeometry
 * @see ComponentKind#STAMP
 */
public final class StampSigil extends Region {

    /** Default inline edge length (px), in the locked ~16–18px range. */
    public static final double DEFAULT_SIZE = 16.0;

    /** Fraction of the half-edge used as the unit radius, leaving a margin for the stroke. */
    private static final double RADIUS_FRACTION = 0.92;

    /** Minimum dot radius (px) so a dot never renders as a sub-pixel speck. */
    private static final double MIN_DOT_RADIUS_PX = 1.0;

    /**
     * Creates a stamp sigil at the {@value #DEFAULT_SIZE}px inline size.
     */
    public StampSigil() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a stamp sigil sized to {@code sizePx} square.
     *
     * @param sizePx the edge length in pixels
     */
    public StampSigil(double sizePx) {
        Color gray = Color.web(StampSigilGeometry.COLOR);
        double center = sizePx / 2.0;
        double unitRadius = (sizePx / 2.0) * RADIUS_FRACTION;

        Polygon pentagon = new Polygon();
        for (double[] vertex : StampSigilGeometry.VERTICES) {
            pentagon.getPoints().addAll(center + vertex[0] * unitRadius, center + vertex[1] * unitRadius);
        }
        pentagon.setFill(Color.TRANSPARENT);
        pentagon.setStroke(gray);
        pentagon.setStrokeWidth(StampSigilGeometry.STROKE_WIDTH_PX);
        pentagon.setStrokeType(StrokeType.INSIDE);
        pentagon.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Group shapes = new Group(pentagon);

        double dotRadius = Math.max(StampSigilGeometry.DOT_RADIUS * unitRadius, MIN_DOT_RADIUS_PX);
        for (int axis = 0; axis < StampSigilGeometry.AXIS_COUNT; axis++) {
            double[] direction = StampSigilGeometry.VERTICES[axis];
            double reading = StampSigilGeometry.AXIS_DOT_RADII[axis] * unitRadius;
            shapes.getChildren().add(new Circle(
                    center + direction[0] * reading, center + direction[1] * reading, dotRadius, gray));
        }

        // Keep the hub proportionally larger than the dots (the locked 0.12 vs 0.10 ratio) even
        // after the dot radius is floored at small inline sizes, so the hub stays distinct — the
        // "A · uniform small dots + hub" decision (ike-issues#638).
        double hubRadius = dotRadius * (StampSigilGeometry.HUB_RADIUS / StampSigilGeometry.DOT_RADIUS);
        shapes.getChildren().add(new Circle(center, center, hubRadius, gray));

        // Unmanaged so the Region leaves the shapes at their geometric (pixel) coordinates rather
        // than laying the Group out.
        shapes.setManaged(false);
        getChildren().add(shapes);

        setMinSize(sizePx, sizePx);
        setPrefSize(sizePx, sizePx);
        setMaxSize(sizePx, sizePx);
        getStyleClass().add("stamp-sigil");
    }
}
