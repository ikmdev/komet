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

/**
 * The <em>locked</em> geometry of the STAMP kind sigil (ike-issues#638, design note
 * {@code dev-component-badge}): a point-up pentagon with one "reading" dot per axis plus a centre
 * hub — a five-dimensional radar that reads as provenance, not a clock (time is only one of the
 * five stamp dimensions) and not a busy rubber-stamp.
 *
 * <p>These are pure constants with <strong>no JavaFX dependency</strong> so the same numbers drive
 * every medium — the JavaFX {@link StampSigil}, the adoc SVG badge, and the Zulip / email PNG — once
 * this moves into the shared {@code koncept-core} module (ike-issues#623). The coordinates are in a
 * <em>y-down</em> (screen / SVG) unit space centred at the origin, so {@link #VERTICES}{@code [0]}
 * = {@code (0, -1)} is the top point.
 *
 * <p>A <em>filled</em> radar reading of the real five field values (status · time · author · module
 * · path) is reserved for the <em>expanded</em> stamp chip; the inline sigil shows the fixed
 * asymmetric dots defined here, not live values.
 */
public final class StampSigilGeometry {

    private StampSigilGeometry() {
    }

    /**
     * The five pentagon vertices as unit vectors (radius 1) from the centre, point-up, y-down:
     * {@code V0(0,-1) V1(0.951,-0.309) V2(0.588,0.809) V3(-0.588,0.809) V4(-0.951,-0.309)}.
     * Each is also the unit direction of axis {@code i}.
     */
    public static final double[][] VERTICES = {
            {0.0, -1.0},
            {0.951, -0.309},
            {0.588, 0.809},
            {-0.588, 0.809},
            {-0.951, -0.309}
    };

    /**
     * The radius (0..1) of the single "reading" dot on each axis {@code V0..V4} — an asymmetric
     * reading so the figure is recognisable, not symmetric: {@code [0.78, 0.48, 0.86, 0.56, 0.66]}.
     */
    public static final double[] AXIS_DOT_RADII = {0.78, 0.48, 0.86, 0.56, 0.66};

    /** Axis-dot radius in unit space ({@code ≈ 0.10}); floor it in px so it is not a speck. */
    public static final double DOT_RADIUS = 0.10;

    /** Centre hub-dot radius in unit space ({@code ≈ 0.12}). */
    public static final double HUB_RADIUS = 0.12;

    /** Pentagon outline stroke width in pixels ({@code ≈ 1.4}, non-scaling, round joins). */
    public static final double STROKE_WIDTH_PX = 1.4;

    /** The single gray ({@value}) used for the outline, the dots, and the hub (metadata/provenance). */
    public static final String COLOR = "#888780";

    /** The number of pentagon axes / vertices / reading dots. */
    public static final int AXIS_COUNT = 5;
}
