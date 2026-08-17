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
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.BreakIterator;

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
 *
 * <p>{@link #setWrapText(boolean)} switches the region to its <em>multi-line</em> mode
 * ({@code IKE-Network/ike-issues#1036}): the full string always shows, wrapping to the allocated
 * width instead of truncating, and the region grows in height ({@code HORIZONTAL} content bias) —
 * ellipsis for the contexts that constrain, wrapping for the contexts that must show the whole
 * name. Single-line ellipsis remains the default.
 */
public final class EllipsisText extends Region {

    /** The single-character ellipsis appended to a truncated string. */
    private static final String ELLIPSIS = "…";

    /** Gap between the end of the string and a trailing node, matching the chip's row spacing. */
    private static final double TRAILING_GAP = 3;

    /**
     * Characters a wrapped line may break <em>after</em>, beside spaces — the semantic-break
     * set (slash, hyphen family, plus): {@code Rhinovirus/Enterovirus} folds after the slash,
     * never mid-word, matching the house semantic-line-break discipline. A word with no such
     * opportunity still force-breaks at the width limit rather than overflowing.
     */
    private static final String BREAK_AFTER = "/-–—+";

    private final Text text = new Text();
    private String fullText = "";
    private boolean wrapText;
    private Node trailingNode;

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
     * Switches between the single-line ellipsis mode (the default) and the multi-line wrap mode
     * ({@code IKE-Network/ike-issues#1036}). Wrapping shows the full string at any allocated
     * width, growing in height instead of truncating; the region's content bias becomes
     * {@code HORIZONTAL} so a host lays its height out from its width.
     *
     * @param wrap {@code true} to wrap the full string across lines; {@code false} to ellipsise
     */
    public void setWrapText(boolean wrap) {
        if (this.wrapText == wrap) {
            return;
        }
        this.wrapText = wrap;
        if (!wrap) {
            text.setWrappingWidth(0);
        }
        text.setText(fullText);
        requestLayout();
    }

    /**
     * Whether the region is in its multi-line wrap mode.
     *
     * @return {@code true} when wrapping, {@code false} when ellipsising (the default)
     */
    public boolean isWrapText() {
        return wrapText;
    }

    /**
     * Whether the displayed string is currently truncated — the gate for an expansion affordance
     * (a click on the ellipsis opens the full, wrapped rendering). Always {@code false} in wrap
     * mode, and before layout has ever constrained the string.
     *
     * @return {@code true} when the display carries less than the full string
     */
    public boolean isEllipsized() {
        return !wrapText && !fullText.equals(text.getText());
    }

    /**
     * Installs (or removes) a node that flows <em>inline after the string's last character</em> —
     * the wrapped badge's definition popout rides here, trailing the final word of the name like
     * a link glyph in running text rather than floating at the region's edge
     * ({@code IKE-Network/ike-issues#1036}). When the node does not fit beside the last line it
     * moves to the start of a new line, and the region's preferred height grows to hold it.
     *
     * @param node the trailing node, or {@code null} to remove the current one
     */
    public void setTrailingNode(Node node) {
        if (this.trailingNode == node) {
            return;
        }
        if (this.trailingNode != null) {
            getChildren().remove(this.trailingNode);
        }
        this.trailingNode = node;
        if (node != null) {
            getChildren().add(node);
        }
        requestLayout();
    }

    /**
     * The node flowing after the string's last character, if any.
     *
     * @return the trailing node, or {@code null}
     */
    public Node getTrailingNode() {
        return trailingNode;
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
     * @return {@code HORIZONTAL} in wrap mode — the region's height follows from its width — and
     *         {@code null} (no bias) in the single-line ellipsis mode, whose height is one line
     *         regardless of width
     */
    @Override
    public Orientation getContentBias() {
        return wrapText ? Orientation.HORIZONTAL : null;
    }

    /**
     * {@inheritDoc}
     *
     * @return zero — the region may shrink to nothing; the string ellipsises as it narrows, or
     *         wraps harder in wrap mode
     */
    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + snappedRightInset();
    }

    /**
     * {@inheritDoc}
     *
     * @return the full string's width in the current font (independent of any currently
     *         ellipsised display, so layout is stable), plus room for the trailing node when one
     *         is installed — so an unconstrained host shows the whole string with its trailer on
     *         one line
     */
    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + measure(fullText) + trailingWidth() + snappedRightInset();
    }

    /** The width the trailing node adds to a line it shares: its preference plus the gap. */
    private double trailingWidth() {
        return trailingNode == null ? 0 : TRAILING_GAP + trailingNode.prefWidth(-1);
    }

    /**
     * {@inheritDoc}
     *
     * @return the same as {@link #computePrefHeight(double)} — a truncated line and a wrapped
     *         block are both incompressible vertically
     */
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    /**
     * {@inheritDoc}
     *
     * @return in the single-line mode, the string's line height in the current font (truncation
     *         never changes the height); in wrap mode, the height of the full string wrapped to
     *         {@code width} (one line when the width is unknown or the string fits it) — in both
     *         cases grown to hold the trailing node, whether it shares the last line or moves to
     *         a new one
     */
    @Override
    protected double computePrefHeight(double width) {
        Text probe = new Text(fullText.isEmpty() ? " " : fullText);
        probe.setFont(text.getFont());
        double inner = -1;
        if (wrapText && width >= 0) {
            inner = width - snappedLeftInset() - snappedRightInset();
            if (inner > 0) {
                // The same semantic wrap the layout applies — never the text engine's own
                // whitespace-only wrapping, which the display no longer uses.
                probe.setText(wrapToWidth(fullText.isEmpty() ? " " : fullText, text.getFont(), inner));
            }
        }
        double height = probe.getLayoutBounds().getHeight();
        if (trailingNode != null) {
            double[] band = endCaretBand(probe);
            double bandHeight = band[2] - band[1];
            double nodeWidth = trailingNode.prefWidth(-1);
            double nodeHeight = trailingNode.prefHeight(-1);
            if (inner <= 0 || band[0] + TRAILING_GAP + nodeWidth <= inner) {
                // Sharing the last line: the band centre carries the node; a node taller than
                // the band pushes the block's bottom down by its overhang.
                height = Math.max(height, (band[1] + band[2]) / 2 + nodeHeight / 2);
            } else {
                // The node opens a new line below the block.
                height = band[2] + Math.max(bandHeight, nodeHeight);
            }
        }
        return snappedTopInset() + height + snappedBottomInset();
    }

    /**
     * The end-of-string caret band of {@code measured}, in its local coordinates — where a
     * trailing node seats. The caret shape is the text engine's own answer for the position
     * after the last character, so it lands at the end of the last <em>wrapped</em> line.
     *
     * @param measured the text to read (its caret position is moved to the end)
     * @return {@code [x, topY, bottomY]} of the end caret
     */
    private static double[] endCaretBand(Text measured) {
        String value = measured.getText();
        measured.setCaretPosition(value == null ? 0 : value.length());
        double x = 0;
        double top = Double.MAX_VALUE;
        double bottom = -Double.MAX_VALUE;
        PathElement[] caret = measured.getCaretShape();
        for (PathElement element : (caret == null ? new PathElement[0] : caret)) {
            if (element instanceof MoveTo move) {
                x = Math.max(x, move.getX());
                top = Math.min(top, move.getY());
                bottom = Math.max(bottom, move.getY());
            } else if (element instanceof LineTo line) {
                x = Math.max(x, line.getX());
                top = Math.min(top, line.getY());
                bottom = Math.max(bottom, line.getY());
            }
        }
        if (top > bottom) {
            // No caret geometry (empty text): a zero band at the origin.
            return new double[] {0, 0, 0};
        }
        return new double[] {x, top, bottom};
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
     * Fits the displayed string to the allocated width and centres it vertically. In the
     * single-line mode the string ellipsises if needed (and the full string is restored when
     * re-widened); in wrap mode the full string always shows, wrapped to the allocated width.
     * Both paths write only when the value differs from the current one, so the layout pass
     * converges.
     */
    @Override
    protected void layoutChildren() {
        double width = getWidth() - snappedLeftInset() - snappedRightInset();
        if (wrapText) {
            // The string wraps to the full allocated width with the SEMANTIC break set
            // (wrapToWidth) — explicit newlines, never the text engine's whitespace-only
            // wrapping. The trailing node shares the last line when it leaves room, and opens
            // a new line otherwise (layoutTrailingNode).
            if (text.getWrappingWidth() != 0) {
                text.setWrappingWidth(0);
            }
            String display = width > 0 ? wrapToWidth(fullText, text.getFont(), width) : fullText;
            if (!display.equals(text.getText())) {
                text.setText(display);
            }
        } else {
            String display = fitToWidth(fullText, text.getFont(),
                    Math.max(0, width - trailingWidth()));
            if (!display.equals(text.getText())) {
                text.setText(display);
            }
        }
        layoutInArea(text, snappedLeftInset(), snappedTopInset(),
                width, getHeight() - snappedTopInset() - snappedBottomInset(),
                0, HPos.LEFT, VPos.CENTER);
        layoutTrailingNode(width);
    }

    /**
     * Seats the trailing node at the end-of-string caret: beside the last line's final character
     * when it fits the allocated width, else at the start of a new line below the block —
     * vertically centred on its line's band either way.
     */
    private void layoutTrailingNode(double width) {
        if (trailingNode == null) {
            return;
        }
        trailingNode.autosize();
        double nodeWidth = trailingNode.getLayoutBounds().getWidth();
        double nodeHeight = trailingNode.getLayoutBounds().getHeight();
        double[] band = endCaretBand(text);
        double bandHeight = band[2] - band[1];
        double x;
        double y;
        if (band[0] + TRAILING_GAP + nodeWidth <= width) {
            x = band[0] + TRAILING_GAP;
            y = (band[1] + band[2]) / 2 - nodeHeight / 2;
        } else {
            x = 0;
            y = band[2] + Math.max(0, (Math.max(bandHeight, nodeHeight) - nodeHeight) / 2);
        }
        trailingNode.relocate(
                snapPositionX(text.getLayoutX() + x),
                snapPositionY(text.getLayoutY() + y));
    }

    /** The width of {@code string} in the region's current font. */
    private double measure(String string) {
        Text probe = new Text(string);
        probe.setFont(text.getFont());
        return probe.getLayoutBounds().getWidth();
    }

    /**
     * Wraps {@code text} to {@code maxWidth} in {@code font} with explicit newlines at the
     * <em>semantic</em> break opportunities: at spaces (the space at the fold is consumed) and
     * after any of {@value #BREAK_AFTER} ({@code Rhinovirus/Enterovirus} folds after the slash).
     * A single token wider than the line with no break opportunity force-breaks at a grapheme
     * boundary rather than overflowing. No non-space character is ever dropped, and each line is
     * the longest that fits — the multi-line badge's own line breaker
     * ({@code IKE-Network/ike-issues#1036}), since the text engine's wrapping knows only
     * whitespace.
     *
     * @param text     the string to wrap; {@code null} is treated as empty
     * @param font     the font the string is measured in
     * @param maxWidth the line width bound in px
     * @return the string with {@code \n} at each fold; the input unchanged when it fits
     */
    public static String wrapToWidth(String text, Font font, double maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int length = text.length();
        int start = 0;
        while (start < length) {
            while (start < length && text.charAt(start) == ' ') {
                start++;
            }
            if (start >= length) {
                break;
            }
            String rest = trimTrailingSpaces(text, start, length);
            Text probe = new Text(rest);
            probe.setFont(font);
            if (probe.getLayoutBounds().getWidth() <= maxWidth) {
                if (!out.isEmpty()) {
                    out.append('\n');
                }
                out.append(rest);
                break;
            }
            int cut = lastFittingBreak(text, start, font, maxWidth);
            if (cut <= start) {
                cut = forceCut(text, start, font, maxWidth);
            }
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(trimTrailingSpaces(text, start, cut));
            start = cut;
        }
        return out.toString();
    }

    /** {@code text[start, end)} with trailing spaces dropped — what a folded line displays. */
    private static String trimTrailingSpaces(String text, int start, int end) {
        while (end > start && text.charAt(end - 1) == ' ') {
            end--;
        }
        return text.substring(start, end);
    }

    /**
     * The largest break position {@code i} in {@code (start, length)} — an index whose preceding
     * character is a space or one of {@value #BREAK_AFTER} — whose line {@code [start, i)}
     * (trailing spaces trimmed) fits {@code maxWidth}; {@code start} when no break position
     * fits.
     */
    private static int lastFittingBreak(String text, int start, Font font, double maxWidth) {
        int best = start;
        for (int i = start + 1; i < text.length(); i++) {
            char before = text.charAt(i - 1);
            if (before != ' ' && BREAK_AFTER.indexOf(before) < 0) {
                continue;
            }
            String line = trimTrailingSpaces(text, start, i);
            if (line.isEmpty()) {
                continue;
            }
            Text probe = new Text(line);
            probe.setFont(font);
            if (probe.getLayoutBounds().getWidth() <= maxWidth) {
                best = i;
            } else {
                break;
            }
        }
        return best;
    }

    /**
     * The forced cut for a token with no fitting break opportunity: the longest grapheme-boundary
     * prefix of {@code [start, …)} that fits, and never less than one grapheme — the line
     * overflows by that grapheme rather than looping.
     */
    private static int forceCut(String text, int start, Font font, double maxWidth) {
        int lo = start + 1;
        int hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            Text candidate = new Text(text.substring(start, mid));
            candidate.setFont(font);
            if (candidate.getLayoutBounds().getWidth() <= maxWidth) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        BreakIterator graphemes = BreakIterator.getCharacterInstance();
        graphemes.setText(text);
        if (!graphemes.isBoundary(lo)) {
            lo = graphemes.preceding(lo);
        }
        if (lo <= start) {
            lo = graphemes.following(start);
            if (lo < 0) {
                lo = text.length();
            }
        }
        return lo;
    }

    /**
     * Truncates {@code text} to fit {@code maxWidth} in {@code font}, appending an ellipsis — the
     * width bound a {@code Label}'s {@code OverrunStyle} would apply, done by hand for a
     * {@link Text} node (which has no overrun). A string that already fits is returned unchanged.
     * The cut never lands inside a grapheme cluster (a surrogate pair, or a base character and its
     * combining marks), so a truncated name never ends in a lone-surrogate replacement glyph.
     *
     * @param text     the string to fit; {@code null} is treated as empty
     * @param font     the font the string is measured in
     * @param maxWidth the maximum width in px
     * @return {@code text} if it fits; its longest grapheme-boundary prefix (plus {@code …}) that
     *         does; or the empty string when not even the ellipsis fits (matching a {@code Label},
     *         which renders nothing at such widths — this region does not clip, so an overflowing
     *         ellipsis would paint outside its bounds)
     */
    public static String fitToWidth(String text, Font font, double maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        Text probe = new Text(text);
        probe.setFont(font);
        if (probe.getLayoutBounds().getWidth() <= maxWidth) {
            return text;
        }
        Text ellipsisProbe = new Text(ELLIPSIS);
        ellipsisProbe.setFont(font);
        if (ellipsisProbe.getLayoutBounds().getWidth() > maxWidth) {
            return "";
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
        if (lo > 0) {
            BreakIterator graphemes = BreakIterator.getCharacterInstance();
            graphemes.setText(text);
            if (!graphemes.isBoundary(lo)) {
                lo = graphemes.preceding(lo);
            }
        }
        return text.substring(0, lo) + ELLIPSIS;
    }
}
