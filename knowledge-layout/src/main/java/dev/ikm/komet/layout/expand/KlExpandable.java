package dev.ikm.komet.layout.expand;

import dev.ikm.komet.layout.controls.IconRegion;
import dev.ikm.komet.layout.controls.KlExpansionOverlay;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

/**
 * A capability mixin: a figure that can <b>expand to fill a host</b>. It carries a small expand icon in
 * a configurable corner (hidden until the cursor nears that corner, or the icon takes keyboard focus);
 * activating it does an in-place modal takeover — the {@link #expandedView()} fills the nearest
 * enclosing {@link KlExpansionHost} rung as the sole navigation context.
 *
 * <p>Expansion is a <b>ladder</b>, not a jump: a block figure fills its document, then its card, then
 * the journal window, one rung per activation, with the expanded chrome naming where the next step
 * leads. {@code Escape} steps back down one rung; at the bottom it collapses to the figure's place,
 * restoring scroll ("the grammar of fullscreen video / map expand"). If the figure also implements
 * {@link KlPreferenceEditable}, the chrome shows the standard preferences toggle.
 *
 * <p>Any figure can adopt it — a {@code KlArea}, a plain control, or a document block. Like
 * {@code KlPeerToRegion}, the behaviour lives in {@code default} methods that manipulate
 * {@link #figureNode()} and stash their state in its node properties (no interface state). All methods
 * must be used on the JavaFX application thread.
 */
public interface KlExpandable {

    /** Style class marking the {@code StackPane} that hosts a figure and its expand icon. */
    String HOST_STYLE_CLASS = "kl-expandable-host";

    /** Reveal distance (px) from the corner within which the expand icon fades in. */
    double REVEAL_DISTANCE = 140;

    /** Node-property key under which the active expansion is stashed. */
    enum PropertyKeys {
        /** The {@link ActiveExpansion} while expanded; absent otherwise. */
        EXPANSION
    }

    /**
     * The live state of one expansion, stashed in {@link #figureNode()}'s properties. Mutable, and the
     * <em>same instance</em> for the whole expansion: the overlay's collapse callback identifies its
     * expansion by identity, so climbing a rung mutates this holder rather than replacing it.
     */
    final class ActiveExpansion {
        /** The rungs this figure can fill, nearest first. */
        final List<KlExpansionHost.Rung> ladder;
        /** The chrome (toolbar + expanded view), re-parented from each rung's overlay into the next. */
        final BorderPane chrome;
        /** Holds the expanded view plus the corner contract icon; child 0 is the view itself. */
        final StackPane contentHost;
        /** The "Reduce to…" control, retargeted as the figure descends. */
        final Button reduceButton;
        /** The "Expand further" control, retargeted as the figure climbs. */
        final Button furtherButton;
        /** The overlay filling the current rung. Replaced, never re-targeted, on each step. */
        KlExpansionOverlay overlay;
        /** Index into {@link #ladder} of the rung currently filled. */
        int rungIndex;
        /** Whether this expansion took the window to OS full screen. */
        boolean osFullScreen;
        /** The stage taken to full screen — held so it can be restored even after the figure detaches. */
        Stage fullScreenStage;
        /** Watches the stage, so full screen left by any other means (the green button) is noticed. */
        ChangeListener<Boolean> fullScreenListener;

        ActiveExpansion(List<KlExpansionHost.Rung> ladder, BorderPane chrome, StackPane contentHost,
                        Button reduceButton, Button furtherButton) {
            this.ladder = ladder;
            this.chrome = chrome;
            this.contentHost = contentHost;
            this.reduceButton = reduceButton;
            this.furtherButton = furtherButton;
        }
    }

    /**
     * The collapsed figure — the node placed in the normal layout and decorated with the expand icon.
     *
     * @return the figure region
     */
    Region figureNode();

    /**
     * Builds the expanded content (e.g. a paged document view, or an enlarged tree). Called fresh on
     * each expansion and on {@link #refreshExpandedView()}.
     *
     * @return the expanded content region
     */
    Region expandedView();

    /**
     * The corner the expand icon occupies.
     *
     * @return the corner; defaults to {@link ExpansionCorner#DEFAULT}
     */
    default ExpansionCorner expansionCorner() {
        return ExpansionCorner.DEFAULT;
    }

    /**
     * Whether this figure may step beyond the top rung into OS full screen. Off by default: full screen
     * takes a figure out of the application's own navigation context, which suits a large tree or a
     * paged document and never suits a small table.
     *
     * @return {@code true} if the top rung's chrome may offer full screen
     */
    default boolean osFullScreenCapable() {
        return false;
    }

    /**
     * Called after the figure collapses fully, so an adopter can restore focus/scroll to its place.
     * Not called when stepping between rungs. No-op by default.
     */
    default void onCollapsed() {
        // adopters override to restore scroll position, etc.
    }

    /**
     * The expand icon's margin from the figure's edges. The icon must never sit on top of one of the
     * figure's own controls: when the figure is a {@link ScrollPane}, its visible scroll bars are added
     * to this margin automatically, so override only to clear other edge furniture (a fixed header,
     * a status strip).
     *
     * @return the base margin for the icon; 6px on every edge by default
     */
    default Insets expandIconInsets() {
        return new Insets(6);
    }

    /**
     * Wraps {@link #figureNode()} in a host that carries the hover-reveal expand icon, and returns the
     * host to place in the layout (in place of the raw figure). Call once.
     *
     * @return a {@link StackPane} hosting the figure plus its expand affordance
     */
    default Region withExpandAffordance() {
        Region figure = figureNode();
        StackPane host = new StackPane(figure);
        host.getStyleClass().add(HOST_STYLE_CLASS);

        Button expandIcon = cornerIcon(host, "kl-expand-icon", "expand", "⛶");
        // Tab reaches the icon and reveals it, so a figure is expandable without a mouse.
        expandIcon.setFocusTraversable(true);
        expandIcon.setOpacity(0);
        // Opacity does not affect picking in JavaFX: a hidden icon would still swallow a click, or the
        // press that starts a chip drag, anywhere in the figure's corner. Make it pickable only once it
        // has faded in — but never while it is pressed. Dragging off the figure fires MOUSE_EXITED and
        // fades the icon out mid-press; going mouse-transparent then would strand the gesture.
        expandIcon.mouseTransparentProperty().bind(
                expandIcon.opacityProperty().lessThan(0.05).and(expandIcon.pressedProperty().not()));
        expandIcon.setOnAction(e -> expand());
        ExpansionCorner corner = expansionCorner();
        Insets iconInsets = expandIconInsets();
        StackPane.setAlignment(expandIcon, corner.pos());
        StackPane.setMargin(expandIcon, iconInsets);
        host.getChildren().add(expandIcon);

        installHoverReveal(host, expandIcon, corner);
        keepClearOfScrollBars(expandIcon, figure, corner, iconInsets);
        return host;
    }

    /**
     * Expands the figure into the nearest enclosing host rung: builds the chrome (Close, the optional
     * preferences toggle, and "Expand further") over {@link #expandedView()}, and takes that rung over.
     * No-op if already expanded, or if the figure has no host ancestor larger than itself.
     */
    default void expand() {
        Region figure = figureNode();
        if (isExpanded()) {
            return;
        }
        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(figure);
        if (ladder.isEmpty()) {
            return; // no host to fill
        }
        BorderPane chrome = new BorderPane();
        chrome.getStyleClass().add("kl-expanded-content");
        chrome.setStyle("-fx-background-color: -fx-background;"); // opaque surface over the scrim
        ExpansionCorner corner = expansionCorner();

        // The corner that expanded the figure contracts it again: the click is its own inverse, and it
        // is where the reader's hand already is. Persistently visible, unlike the collapsed figure's icon
        // — nothing here competes with it for attention, and stepping back must be discoverable.
        StackPane contentHost = new StackPane(expandedView());
        Button contractIcon = cornerIcon(contentHost, "kl-contract-icon", "contract", "⤡");
        contractIcon.setOnAction(e -> stepDown());
        StackPane.setAlignment(contractIcon, corner.pos());
        StackPane.setMargin(contractIcon, new Insets(12));
        contentHost.getChildren().add(contractIcon);
        chrome.setCenter(contentHost);

        Button close = new Button("Close");
        close.getStyleClass().add("kl-expansion-close");
        ToolBar bar = new ToolBar(close);
        bar.getStyleClass().add("kl-expansion-bar");
        if (this instanceof KlPreferenceEditable<?> editable) {
            ToggleButton prefsToggle = new ToggleButton("Preferences");
            Region editor = editable.preferenceEditor();
            prefsToggle.setOnAction(e -> chrome.setRight(prefsToggle.isSelected() ? editor : null));
            bar.getItems().add(prefsToggle);
        }
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        // Reduce and expand sit side by side, each naming where it leads. Escape is a shortcut for the
        // former, never the only way to it.
        Button reduceButton = new Button();
        reduceButton.getStyleClass().add("kl-expansion-reduce");
        Button furtherButton = new Button();
        furtherButton.getStyleClass().add("kl-expansion-further");
        bar.getItems().addAll(spring, reduceButton, furtherButton);
        chrome.setTop(bar);

        ActiveExpansion active = new ActiveExpansion(ladder, chrome, contentHost, reduceButton, furtherButton);
        close.setOnAction(e -> collapse());
        reduceButton.setOnAction(e -> stepDown());
        furtherButton.setOnAction(e -> expandFurther());
        figure.getProperties().put(PropertyKeys.EXPANSION, active);

        active.overlay = overlayFor(figure, ladder.get(0).fillPane(), active);
        updateChrome(active);
        active.overlay.expand();
    }

    /**
     * Climbs one rung — or, at the top of a full-screen-capable ladder, toggles OS full screen.
     */
    default void expandFurther() {
        if (!(figureNode().getProperties().get(PropertyKeys.EXPANSION) instanceof ActiveExpansion active)) {
            return;
        }
        if (active.osFullScreen) {
            exitFullScreen(active);
            return;
        }
        if (active.rungIndex >= active.ladder.size() - 1) {
            enterFullScreen(active);
            return;
        }
        active.rungIndex++;
        moveToCurrentRung(active);
    }

    /**
     * Steps down one level: out of OS full screen if the figure took the window there, otherwise down a
     * rung; at the bottom rung, collapses the figure back into its place.
     *
     * <p>Full screen is a step of its own because {@code Escape} reaches us <em>and</em> JavaFX: Glass
     * exits full screen on the exit key and then falls through to dispatch the same key into the scene
     * ({@code GlassViewEventHandler}, {@code case PRESS} → {@code stage.exitFullScreen()} → {@code
     * NOBREAK} → {@code scene.sceneListener.keyEvent}). Without this, one Escape would both leave full
     * screen and drop a rung the reader never asked to leave.
     */
    default void stepDown() {
        if (!(figureNode().getProperties().get(PropertyKeys.EXPANSION) instanceof ActiveExpansion active)) {
            return;
        }
        if (active.osFullScreen) {
            exitFullScreen(active);
            return;
        }
        if (active.rungIndex == 0) {
            collapse();
            return;
        }
        active.rungIndex--;
        moveToCurrentRung(active);
    }

    /** Collapses the figure fully, whichever rung it fills — leaving OS full screen on the way out. */
    default void collapse() {
        if (figureNode().getProperties().get(PropertyKeys.EXPANSION) instanceof ActiveExpansion active) {
            if (active.osFullScreen) {
                exitFullScreen(active);
            }
            active.overlay.collapse();
        }
    }

    /**
     * Whether the figure is currently expanded into some rung.
     *
     * @return {@code true} while expanded
     */
    default boolean isExpanded() {
        // Property-based, not overlay.isShowing(): an expansion that is mid-collapse-fade still counts
        // as expanded, so a re-click during the fade is a no-op rather than stacking a second overlay.
        return figureNode().getProperties().get(PropertyKeys.EXPANSION) instanceof ActiveExpansion;
    }

    /** If expanded, rebuilds the expanded content from {@link #expandedView()} (e.g. after a data change). */
    default void refreshExpandedView() {
        if (figureNode().getProperties().get(PropertyKeys.EXPANSION) instanceof ActiveExpansion active
                && active.overlay.isShowing()) {
            // Replace only the view; the contract icon is the host's other child and must survive.
            active.contentHost.getChildren().set(0, expandedView());
        }
    }

    /**
     * Moves the expansion to {@code active.rungIndex}: the outgoing overlay is detached (no fade, no
     * collapse callback — the figure is still expanded) and a fresh overlay takes over the new rung with
     * the same chrome. Overlays are never re-targeted; see {@link KlExpansionOverlay}.
     */
    private void moveToCurrentRung(ActiveExpansion active) {
        active.overlay.detach();
        active.overlay = overlayFor(figureNode(), active.ladder.get(active.rungIndex).fillPane(), active);
        updateChrome(active);
        active.overlay.expand();
    }

    /** A fresh overlay filling {@code pane}, wired to step down on Escape and clean up on collapse. */
    private KlExpansionOverlay overlayFor(Region figure, Pane pane, ActiveExpansion mine) {
        KlExpansionOverlay overlay = new KlExpansionOverlay(pane, mine.chrome, figure, () -> {
            // Clear the state only if it still refers to THIS expansion — a quick re-open during the
            // collapse fade may already have installed a newer one, whose state we must not delete.
            if (figure.getProperties().get(PropertyKeys.EXPANSION) == mine) {
                // dispose() (the figure left the scene) reaches here without passing through collapse(),
                // so a figure torn down while full screen would otherwise strand the window there.
                if (mine.osFullScreen) {
                    exitFullScreen(mine);
                }
                figure.getProperties().remove(PropertyKeys.EXPANSION);
            }
            onCollapsed();
        });
        overlay.setOnEscape(this::stepDown);
        return overlay;
    }

    /** Retargets both stepping controls at the steps either side of where the figure now is. */
    private void updateChrome(ActiveExpansion active) {
        int next = active.rungIndex + 1;
        if (active.osFullScreen) {
            active.furtherButton.setText("Expand further");
            active.furtherButton.setDisable(true);
        } else if (next < active.ladder.size()) {
            active.furtherButton.setText("Expand further → " + active.ladder.get(next).name());
            active.furtherButton.setDisable(false);
        } else if (osFullScreenCapable() && active.ladder.get(active.rungIndex).osFullScreenBeyond()) {
            active.furtherButton.setText("Full screen");
            active.furtherButton.setDisable(false);
        } else {
            active.furtherButton.setText("Expand further");
            active.furtherButton.setDisable(true);
        }

        // The inverse, always available and always named: full screen is just the top step of the same
        // ladder, not a special case with its own vocabulary.
        if (active.osFullScreen) {
            active.reduceButton.setText("Exit full screen");
        } else if (active.rungIndex > 0) {
            active.reduceButton.setText("↙ Reduce to " + active.ladder.get(active.rungIndex - 1).name());
        } else {
            active.reduceButton.setText("↙ Collapse");
        }
    }

    /** Takes the figure's window to OS full screen, if this figure opted in and the top rung allows it. */
    private void enterFullScreen(ActiveExpansion active) {
        if (!osFullScreenCapable() || !active.ladder.get(active.rungIndex).osFullScreenBeyond()) {
            return;
        }
        Stage stage = stageOf();
        if (stage == null) {
            return;
        }
        // Never stack a second watcher on a re-entry.
        if (active.fullScreenListener != null && active.fullScreenStage != null) {
            active.fullScreenStage.fullScreenProperty().removeListener(active.fullScreenListener);
        }
        active.osFullScreen = true;
        active.fullScreenStage = stage;
        // The reader can also leave full screen by the window's own controls — the green button, a swipe,
        // Ctrl-Cmd-F. Notice that, so the chrome and the Escape step stay honest about where the figure
        // is. The watcher removes ITSELF on that exit: every path that would otherwise clean it up is
        // guarded on osFullScreen, which this listener has just cleared, so it would outlive the
        // expansion it captures — pinned to a journal window that outlives every card.
        active.fullScreenListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean was, Boolean isFullScreen) {
                if (isFullScreen) {
                    return;
                }
                Stage watched = active.fullScreenStage;
                if (watched != null) {
                    watched.fullScreenProperty().removeListener(this);
                }
                active.fullScreenStage = null;
                active.fullScreenListener = null;
                active.osFullScreen = false;
                updateChrome(active);
            }
        };
        stage.fullScreenProperty().addListener(active.fullScreenListener);
        stage.setFullScreen(true);
        updateChrome(active);
    }

    /**
     * Leaves OS full screen, if this expansion took the window there. Idempotent. Uses the stage captured
     * on the way in rather than the figure's current scene, so a figure torn down while full screen still
     * hands the window back.
     */
    private void exitFullScreen(ActiveExpansion active) {
        active.osFullScreen = false;
        Stage stage = active.fullScreenStage;
        if (stage != null) {
            if (active.fullScreenListener != null) {
                stage.fullScreenProperty().removeListener(active.fullScreenListener);
            }
            // Glass may already have exited full screen for us on the exit key; setting it again is a
            // no-op, and doing it unconditionally means no other exit path can leave the window stranded.
            stage.setFullScreen(false);
        }
        active.fullScreenStage = null;
        active.fullScreenListener = null;
        updateChrome(active);
    }

    /** The {@link Stage} showing this figure, or {@code null} if it is not in a staged scene. */
    private Stage stageOf() {
        Scene scene = figureNode().getScene();
        return scene != null && scene.getWindow() instanceof Stage stage ? stage : null;
    }

    /**
     * Keeps the icon clear of a scrolling figure's scroll bars: when {@code figure} is a
     * {@link ScrollPane}, the icon's margin grows by whichever of its bars share the icon's corner, and
     * shrinks back when they hide. The viewport shrinks exactly when a bar appears, so its bounds are
     * the signal to re-inset; the skin listener covers the first layout, before which no scroll-bar node
     * exists to measure. A non-scrolling figure keeps the base margin.
     */
    private static void keepClearOfScrollBars(Node icon, Region figure, ExpansionCorner corner, Insets base) {
        if (!(figure instanceof ScrollPane scrollPane)) {
            return;
        }
        Runnable reinset = () -> StackPane.setMargin(icon, marginClearing(scrollPane, corner, base));
        scrollPane.viewportBoundsProperty().addListener((obs, was, now) -> reinset.run());
        scrollPane.skinProperty().addListener((obs, was, now) -> reinset.run());
        reinset.run();
    }

    /** The icon margin for {@code corner} given the scroll pane's own currently visible scroll bars. */
    private static Insets marginClearing(ScrollPane scrollPane, ExpansionCorner corner, Insets base) {
        double verticalBarWidth = 0;
        double horizontalBarHeight = 0;
        for (Node node : scrollPane.lookupAll(".scroll-bar")) {
            if (!(node instanceof ScrollBar bar) || !bar.isVisible() || !ownedBy(bar, scrollPane)) {
                continue;
            }
            if (bar.getOrientation() == Orientation.VERTICAL) {
                // Fall back to the preferred size: on the layout pass that first reveals a bar, the
                // viewport bounds change before the bar itself is resized, so its width still reads 0.
                verticalBarWidth = Math.max(verticalBarWidth, Math.max(bar.getWidth(), bar.prefWidth(-1)));
            } else {
                horizontalBarHeight = Math.max(horizontalBarHeight,
                        Math.max(bar.getHeight(), bar.prefHeight(-1)));
            }
        }
        return corner.insetsClearing(base, verticalBarWidth, horizontalBarHeight);
    }

    /**
     * Whether {@code bar} is one of {@code scrollPane}'s own scroll bars rather than one belonging to a
     * nested scrolling figure in its content. {@code lookupAll} searches the whole subtree, so a table
     * block's horizontal bar would otherwise displace the enclosing surface's icon.
     */
    private static boolean ownedBy(ScrollBar bar, ScrollPane scrollPane) {
        for (Node parent = bar.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrollPane owner) {
                return owner == scrollPane;
            }
        }
        return false;
    }

    /**
     * Fades the expand icon in with the cursor's proximity to its corner, and holds it visible while the
     * icon itself has keyboard focus.
     *
     * <p>Only the <b>innermost</b> figure under the cursor reveals. Figures nest (a table inside a
     * document surface) and {@code MOUSE_MOVED} bubbles to every enclosing host, so without this test
     * hovering a table would light its own icon and the document's at once. The picked target is the
     * same for every handler on the bubble path, so exactly one host recognises itself as nearest.
     *
     * <p>The reveal keys on the icon's own focus rather than the host's {@code focusWithin}: a node's
     * {@code focusWithin} is true whenever any <em>descendant</em> holds focus, so a surface whose
     * content is focusable (a prose block) would show its icon permanently. A pressed icon holds its
     * opacity — a press that drags off the figure must not make the control under the user's finger
     * disappear, and its pickability is tied to that opacity.
     */
    private static void installHoverReveal(StackPane host, Node icon, ExpansionCorner corner) {
        host.setOnMouseMoved(e -> {
            if (revealIsPinned(icon)) {
                return;
            }
            // The icon is drawn over the figure, so it can lie above a nested figure. Hovering it
            // directly must still reveal it, or it could never be clicked there.
            if (icon.getBoundsInParent().contains(e.getX(), e.getY())) {
                icon.setOpacity(1);
                return;
            }
            if (!(e.getTarget() instanceof Node picked) || nearestExpandableHost(picked) != host) {
                icon.setOpacity(0); // an inner figure owns the reveal here
                return;
            }
            icon.setOpacity(cornerProximity(host, corner, e.getX(), e.getY()));
        });
        host.setOnMouseExited(e -> {
            if (!revealIsPinned(icon)) {
                icon.setOpacity(0);
            }
        });
        icon.focusedProperty().addListener((obs, was, focused) -> {
            if (!icon.isPressed()) {
                icon.setOpacity(focused ? 1 : 0);
            }
        });
        // A press ending outside the icon leaves it revealed; settle it once the gesture is over.
        icon.pressedProperty().addListener((obs, was, pressed) -> {
            if (!pressed && !icon.isFocused() && !icon.isHover()) {
                icon.setOpacity(0);
            }
        });
    }

    /**
     * A corner affordance button carrying an {@link IconRegion} drawn from {@code expansion.css}: four
     * corner brackets pointing out to expand, in to contract.
     *
     * <p>The stylesheet is applied to {@code styleRoot} so the icon renders wherever the mixin is
     * adopted, with no requirement on the host to have loaded anything. If the stylesheet cannot be
     * found the button falls back to {@code fallbackGlyph} and an inline style, so a packaging mistake
     * degrades to a visible control rather than an invisible one.
     */
    private static Button cornerIcon(Parent styleRoot, String buttonClass, String iconClass, String fallbackGlyph) {
        Button button = new Button();
        button.getStyleClass().add(buttonClass);
        URL stylesheet = KlExpandable.class.getResource("expansion.css");
        if (stylesheet == null) {
            button.setText(fallbackGlyph);
            button.setStyle("-fx-background-radius: 4; -fx-background-color: rgba(0,0,0,0.55);"
                    + " -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 1 6 3 6; -fx-cursor: hand;");
            return button;
        }
        String url = stylesheet.toExternalForm();
        if (!styleRoot.getStylesheets().contains(url)) {
            styleRoot.getStylesheets().add(url);
        }
        button.setGraphic(new IconRegion("icon", iconClass));
        return button;
    }

    /** The innermost expandable host at or above {@code from}, or {@code null} if none. */
    private static Node nearestExpandableHost(Node from) {
        for (Node node = from; node != null; node = node.getParent()) {
            if (node.getStyleClass().contains(HOST_STYLE_CLASS)) {
                return node;
            }
        }
        return null;
    }

    /** Whether the icon must stay revealed regardless of the cursor: it is focused, or mid-press. */
    private static boolean revealIsPinned(Node icon) {
        return icon.isFocused() || icon.isPressed();
    }

    /** Opacity 0..1 for the icon given the cursor's distance from the configured corner. */
    private static double cornerProximity(Region host, ExpansionCorner corner, double x, double y) {
        Pos pos = corner.pos();
        double cornerX = pos.getHpos() == HPos.LEFT ? 0 : host.getWidth();
        double cornerY = pos.getVpos() == VPos.TOP ? 0 : host.getHeight();
        double distance = Math.hypot(x - cornerX, y - cornerY);
        return Math.max(0, Math.min(1, (REVEAL_DISTANCE - distance) / REVEAL_DISTANCE));
    }
}
