/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.controls;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A single-line {@link Text} that ellipsises to the width layout allocates it — the overrun a
 * {@code Label} provides, for a {@code Text} node. A {@code Label} cannot strike through (there is
 * no {@code -fx-strikethrough} for it), and a bare {@code Text} cannot shrink below its string's
 * width; this region provides both: the inner {@code Text} carries the real strikethrough and CSS
 * fill, while the region reports a min width of zero and re-fits the displayed string on every
 * layout pass ({@code IKE-Network/ike-issues#855}).
 *
 * <p>The preferred width is always the <em>full</em> string's width, so an unconstrained host sizes
 * to the whole name and a constrained host shrinks it — the string ellipsises when narrowed and is
 * restored when re-widened. Style the inner {@link #textNode()} (CSS style class, fill,
 * strikethrough); set the string and font here.
 */
public final class EllipsisText extends Region {

    /** The single-character ellipsis appended to a truncated string. */
    private static final String ELLIPSIS = "…";

    private final Text text = new Text();
    private String fullText = "";

    /**
     * Creates an empty ellipsising text region.
     */
    public EllipsisText() {
        // TOP origin gives the Text box-like bounds so layoutInArea can centre it vertically.
        text.setTextOrigin(VPos.TOP);
        getChildren().add(text);
    }

    /**
     * The inner {@link Text} node — the target for CSS style classes, fill and strikethrough. Its
     * string is managed by this region (the fitted, possibly ellipsised form of {@link #getText()});
     * set the string via {@link #setText(String)}, not on the node.
     *
     * @return the inner text node (never {@code null})
     */
    public Text textNode() {
        return text;
    }

    /**
     * Sets the full string to display. The inner node carries the full string immediately (so an
     * un-laid-out region still reads its text); layout re-fits it, ellipsising only if the
     * allocated width requires it.
     *
     * @param value the full string; {@code null} is treated as empty
     */
    public void setText(String value) {
        this.fullText = value == null ? "" : value;
        text.setText(fullText);
        requestLayout();
    }

    /**
     * The full (un-ellipsised) string.
     *
     * @return the string set via {@link #setText(String)}, never {@code null}
     */
    public String getText() {
        return fullText;
    }

    /**
     * Sets the font the string is measured and rendered in.
     *
     * @param font the font to render the string in
     */
    public void setFont(Font font) {
        text.setFont(font);
        requestLayout();
    }

    /**
     * {@inheritDoc}
     *
     * @return zero — the region may shrink to nothing; the string ellipsises as it narrows
     */
    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + snappedRightInset();
    }

    /**
     * {@inheritDoc}
     *
     * @return the full string's width in the current font, so an unconstrained host shows the whole
     *         string (independent of any currently ellipsised display, so layout is stable)
     */
    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + measure(fullText) + snappedRightInset();
    }

    /**
     * {@inheritDoc}
     *
     * @return the string's line height in the current font (truncation never changes the height)
     */
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    /**
     * {@inheritDoc}
     *
     * @return the string's line height in the current font (truncation never changes the height)
     */
    @Override
    protected double computePrefHeight(double width) {
        Text probe = new Text(fullText.isEmpty() ? " " : fullText);
        probe.setFont(text.getFont());
        return snappedTopInset() + probe.getLayoutBounds().getHeight() + snappedBottomInset();
    }

    /**
     * {@inheritDoc}
     *
     * @return the vertically-centred inner text's baseline, so a baseline-aligning host seats the
     *         string on its line
     */
    @Override
    public double getBaselineOffset() {
        return text.getLayoutY() + text.getBaselineOffset();
    }

    /**
     * Fits the displayed string to the allocated width (ellipsising if needed — and restoring the
     * full string when re-widened) and centres it vertically. The fitted string is set only when it
     * differs from the current display, so the layout pass converges.
     */
    @Override
    protected void layoutChildren() {
        double width = getWidth() - snappedLeftInset() - snappedRightInset();
        String display = fitToWidth(fullText, text.getFont(), width);
        if (!display.equals(text.getText())) {
            text.setText(display);
        }
        layoutInArea(text, snappedLeftInset(), snappedTopInset(),
                width, getHeight() - snappedTopInset() - snappedBottomInset(),
                0, HPos.LEFT, VPos.CENTER);
    }

    /** The width of {@code string} in the region's current font. */
    private double measure(String string) {
        Text probe = new Text(string);
        probe.setFont(text.getFont());
        return probe.getLayoutBounds().getWidth();
    }

    /**
     * Truncates {@code text} to fit {@code maxWidth} in {@code font}, appending an ellipsis — the
     * width bound a {@code Label}'s {@code OverrunStyle} would apply, done by hand for a
     * {@link Text} node (which has no overrun). A string that already fits is returned unchanged.
     *
     * @param text     the string to fit
     * @param font     the font the string is measured in
     * @param maxWidth the maximum width in px
     * @return {@code text} if it fits, else its longest prefix (plus {@code …}) that does
     */
    public static String fitToWidth(String text, Font font, double maxWidth) {
        Text probe = new Text(text);
        probe.setFont(font);
        if (probe.getLayoutBounds().getWidth() <= maxWidth) {
            return text;
        }
        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            Text candidate = new Text(text.substring(0, mid) + ELLIPSIS);
            candidate.setFont(font);
            if (candidate.getLayoutBounds().getWidth() <= maxWidth) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return text.substring(0, lo) + ELLIPSIS;
    }
}
