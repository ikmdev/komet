package dev.ikm.komet.layout.controls;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A reusable, content-independent <b>modal takeover</b> overlay for the knowledge-layout tier — the
 * primitive behind expanding a figure into one rung of its host ladder (a {@link KlDrawer} taken to
 * 100% of a host). Given a target {@link Pane} to fill and a content {@link Region}, it overlays the
 * target with a scrim and the content filling it, owns the fade in/out, and captures {@code Escape} to
 * run {@linkplain #setOnEscape(Runnable) an escape action} — by default collapsing.
 *
 * <p>An overlay fills <em>one</em> target for its whole life: {@code target} is final, and the collapse
 * fade's completion callback runs 140&nbsp;ms after the collapse begins, so a mutable target would let
 * a stale fade remove the overlay from a pane it no longer belongs to. To move a figure between rungs,
 * {@link #detach()} this overlay and expand a fresh one on the next rung, reusing the content node.
 *
 * <p>The overlay is added as an <em>unmanaged</em> child of the target so the target's own layout does
 * not disturb it; it is kept sized to the target by width/height listeners. All methods must be used on
 * the JavaFX application thread.
 */
public final class KlExpansionOverlay {

    private static final Duration FADE = Duration.millis(140);

    private final Pane target;
    private final Region content;
    private final Node anchor;
    private final Runnable onCollapse;
    private final StackPane overlay = new StackPane();
    private final ChangeListener<Number> resizeListener = (obs, old, now) -> sizeToTarget();
    private Runnable onEscape = this::collapse;
    private final EventHandler<KeyEvent> escHandler = e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
            onEscape.run();
            e.consume();
        }
    };
    /** The figure leaving the scene means its expansion is over — tear down rather than orphan. */
    private final ChangeListener<Scene> anchorSceneListener = (obs, was, now) -> {
        if (now == null) {
            dispose();
        }
    };
    /** The scene the Escape filter was installed on, so it is removed from that same scene. */
    private Scene filterScene;
    private boolean showing;

    /**
     * Equivalent to {@link #KlExpansionOverlay(Pane, Region, Node, Runnable)} with no anchor: the
     * overlay is then torn down only by {@link #collapse()}, {@link #detach()} or {@link #dispose()}.
     *
     * @param target     the pane to fill (one rung of the figure's host ladder)
     * @param content    the expanded content to show
     * @param onCollapse run when the overlay collapses; may be {@code null}
     */
    public KlExpansionOverlay(Pane target, Region content, Runnable onCollapse) {
        this(target, content, null, onCollapse);
    }

    /**
     * @param target     the pane to fill (one rung of the figure's host ladder)
     * @param content    the expanded content to show
     * @param anchor     the figure being expanded; when it leaves the scene while expanded the overlay
     *                   disposes itself. May be {@code null}. A rung's pane outlives an individual
     *                   figure, so without an anchor a figure removed mid-expansion (its card unbound,
     *                   its document rebuilt) would leave the overlay showing and its Escape filter
     *                   installed
     * @param onCollapse run when the overlay collapses or disposes — <em>not</em> on {@link #detach()};
     *                   may be {@code null}
     */
    public KlExpansionOverlay(Pane target, Region content, Node anchor, Runnable onCollapse) {
        this.target = Objects.requireNonNull(target, "target is null");
        this.content = Objects.requireNonNull(content, "content is null");
        this.anchor = anchor;
        this.onCollapse = onCollapse == null ? () -> { } : onCollapse;

        overlay.getStyleClass().add("kl-expansion-overlay");
        overlay.setManaged(false);
        // Scrim + a frame of padding, set inline so it renders without a loaded stylesheet.
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.setPadding(new Insets(24));
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        overlay.getChildren().add(content);
        // Escape is handled by a SCENE filter installed on expand (not a node-focus grab) — see expand().
    }

    /**
     * Whether the overlay is currently showing.
     *
     * @return {@code true} while expanded
     */
    public boolean isShowing() {
        return showing;
    }

    /**
     * The action {@code Escape} runs while this overlay is showing. Defaults to {@link #collapse()}; a
     * figure with more than one rung sets it to step down one rung instead.
     *
     * @param action what Escape does; {@code null} restores the default
     */
    public void setOnEscape(Runnable action) {
        this.onEscape = action == null ? this::collapse : action;
    }

    /** Expands: adds the overlay to the target, sizes it to fill, and fades it in. */
    public void expand() {
        if (showing) {
            return;
        }
        showing = true;
        target.getChildren().add(overlay);
        target.widthProperty().addListener(resizeListener);
        target.heightProperty().addListener(resizeListener);
        if (anchor != null) {
            anchor.sceneProperty().addListener(anchorSceneListener);
        }
        // Escape collapses via a SCENE-level filter rather than grabbing node focus onto the overlay:
        // a non-input StackPane holding focus confuses the native print panel's focus hand-back on
        // dismiss (macOS beeps), and it lets the inner controls (nav, print, settings) focus normally.
        filterScene = target.getScene();
        if (filterScene != null) {
            filterScene.addEventFilter(KeyEvent.KEY_PRESSED, escHandler);
        }
        sizeToTarget();
        overlay.setOpacity(0);
        FadeTransition fade = new FadeTransition(FADE, overlay);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /** Collapses: fades the overlay out, removes it, and runs {@code onCollapse}. */
    public void collapse() {
        if (!showing) {
            return;
        }
        showing = false;
        detachListeners();
        FadeTransition fade = new FadeTransition(FADE, overlay);
        fade.setFromValue(overlay.getOpacity());
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            target.getChildren().remove(overlay);
            onCollapse.run();
        });
        fade.play();
    }

    /**
     * Removes the overlay immediately and releases its content — <b>without</b> a fade and without
     * running {@code onCollapse}. The figure is still expanded, merely moving to another rung, and the
     * content node is about to be re-parented into that rung's overlay. Idempotent.
     */
    public void detach() {
        if (!showing) {
            return;
        }
        teardown();
        overlay.getChildren().remove(content);
    }

    /**
     * Tears the overlay down immediately, without the collapse fade, and runs {@code onCollapse} — for
     * when the figure it belongs to has gone away and there is nothing left to animate back to.
     * Idempotent; a no-op unless expanded.
     */
    public void dispose() {
        if (!showing) {
            return;
        }
        teardown();
        onCollapse.run();
    }

    /** Removes the overlay and every listener/filter installed by {@link #expand()}. */
    private void teardown() {
        showing = false;
        detachListeners();
        target.getChildren().remove(overlay);
    }

    /** Removes every listener/filter installed by {@link #expand()}. Safe to call once per expansion. */
    private void detachListeners() {
        target.widthProperty().removeListener(resizeListener);
        target.heightProperty().removeListener(resizeListener);
        if (anchor != null) {
            anchor.sceneProperty().removeListener(anchorSceneListener);
        }
        // Remove the filter from the scene it was added to: by teardown time the target may already
        // have left that scene, so target.getScene() would hand back null and leak the filter.
        if (filterScene != null) {
            filterScene.removeEventFilter(KeyEvent.KEY_PRESSED, escHandler);
            filterScene = null;
        }
    }

    private void sizeToTarget() {
        overlay.resizeRelocate(0, 0, target.getWidth(), target.getHeight());
    }
}
