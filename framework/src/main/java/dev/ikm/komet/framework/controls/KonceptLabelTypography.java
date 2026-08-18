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
 * How a {@link KonceptBadge} renders its label ({@code IKE-Network/ike-issues#1050}): true small
 * caps — the ike-issues#855 treatment, the default — or plain text in the label's natural case,
 * either at regular or bold weight. A value object so a surface can carry one typography and hand
 * it to every chip it builds; surfaces that never set one render the {@link #DEFAULT}.
 *
 * <p>Typography governs the label (and the sizes that ride it: the kind sigil, the status
 * cluster); it never moves the identicon, which rides the ambient font size alone.
 *
 * @param smallCaps {@code true} renders the label in the bundled true small-caps family (with the
 *                  shrunken all-caps fallback when the family is absent); {@code false} renders
 *                  plain text — the platform default family, natural case, no transform
 * @param bold      {@code true} requests bold weight. JavaFX resolves the request against the
 *                  registered faces (it never synthesizes weight), so small-caps bold degrades to
 *                  the family's registered face until a bold small-caps face is bundled
 */
public record KonceptLabelTypography(boolean smallCaps, boolean bold) {

    /** Today's rendering: true small caps at regular weight. */
    public static final KonceptLabelTypography DEFAULT = new KonceptLabelTypography(true, false);
}
