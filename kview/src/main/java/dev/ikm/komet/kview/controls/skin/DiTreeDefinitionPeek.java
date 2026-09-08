package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLReadOnlyDiTreeControl;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * <p>The hover definition peek of the axiom tree: resting the pointer on a concept chip for
 * {@link #SHOW_DELAY} opens a transient {@link PopOver} under the chip with that concept's stated
 * definition — the KL counterpart of the classic axiom view's {@code LINK_EXTERNAL} popout
 * ({@code AxiomPopover}), without a button on every chip.</p>
 *
 * <p>The popover's content is a nested {@link KLReadOnlyDiTreeControl} sharing the owner's
 * resolvers, so its chips carry the same peek: hovering a concept inside a definition opens the
 * next definition from it, and a chain of peeks stacks, each popover hanging from the chip that
 * opened it. A popover stays while the pointer is on its chip or on it, while a peek opened from
 * inside it is showing, or while it is pinned; otherwise it leaves {@link #HIDE_GRACE} after the
 * pointer left, so crossing the gap between chip and popover does not close it. Escape closes the
 * deepest popover of the chain; the chip leaving the scene (a rebuild, an inline edit swapping it
 * out) or a drag starting on it closes its popover at once.</p>
 */
final class DiTreeDefinitionPeek {

    /** How long the pointer rests on a chip before its definition shows. */
    static final Duration SHOW_DELAY = Duration.millis(500);

    /** How long the pointer may be away from chip and popover before the popover hides. */
    static final Duration HIDE_GRACE = Duration.millis(300);

    /** Distance between the chip's bottom edge and the popover's arrow tip, in px. */
    private static final double OWNER_GAP = 2;

    /**
     * Node-properties key under which a nested (peek) control carries the peek that opened it,
     * so the peeks of its chips can hold it open while they show.
     */
    private static final String PARENT_PEEK_KEY = "ditree-definition-peek-parent";

    /** The tree control's stylesheet, which also styles the popover frame and content. */
    private static final String STYLESHEET =
            KLReadOnlyDiTreeControl.class.getResource("read-only-ditree-control.css").toExternalForm();

    private final Node chip;
    private final int conceptNid;
    private final KLReadOnlyDiTreeControl owner;
    private final PauseTransition showTimer = new PauseTransition(SHOW_DELAY);
    private final PauseTransition hideTimer = new PauseTransition(HIDE_GRACE);

    private PopOver popover;
    private Region content;
    private boolean pinned;
    /** Peeks opened from chips inside this popover, in opening order. */
    private final List<DiTreeDefinitionPeek> openChildren = new ArrayList<>();
    private Scene keyScene;
    private final EventHandler<KeyEvent> escapeFilter = this::onKeyPressed;

    /**
     * Installs the peek on a chip: the behaviour lives as long as the chip's listeners do.
     *
     * @param chip       the chip whose hover opens the peek
     * @param conceptNid the concept the chip renders, whose definition the peek shows
     * @param owner      the tree control the chip belongs to — the source of the resolvers
     */
    static void install(Node chip, int conceptNid, KLReadOnlyDiTreeControl owner) {
        new DiTreeDefinitionPeek(chip, conceptNid, owner);
    }

    private DiTreeDefinitionPeek(Node chip, int conceptNid, KLReadOnlyDiTreeControl owner) {
        this.chip = chip;
        this.conceptNid = conceptNid;
        this.owner = owner;

        showTimer.setOnFinished(e -> show());
        hideTimer.setOnFinished(e -> hideIfIdle());

        chip.hoverProperty().subscribe(() -> {
            if (chip.isHover()) {
                hideTimer.stop();
                if (popover == null && owner.getDefinitionResolver() != null) {
                    showTimer.playFromStart();
                }
            } else {
                showTimer.stop();
                scheduleHide();
            }
        });
        // A press means the chip is being used (context menu, click-to-edit, drag start): a
        // definition popping up on top of that would be in the way.
        chip.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> showTimer.stop());
        chip.addEventFilter(MouseEvent.DRAG_DETECTED, e -> hideNow());
        chip.sceneProperty().subscribe(scene -> {
            if (scene == null) {
                hideNow();
            }
        });
    }

    /*=========================================================================*
     * Showing                                                                 *
     *=========================================================================*/

    private void show() {
        if (popover != null || chip.getScene() == null) {
            return;
        }
        IntFunction<DiTreeEntity> definitionResolver = owner.getDefinitionResolver();
        DiTreeEntity definition = definitionResolver.apply(conceptNid);
        if (definition == null) {
            return;
        }

        KLReadOnlyDiTreeControl nested = new KLReadOnlyDiTreeControl();
        nested.setCompactMode(true);
        nested.setTitleVisible(false);
        nested.setComponentItemResolver(owner.getComponentItemResolver());
        nested.setDescriptionResolver(owner.getDescriptionResolver());
        nested.setDefinitionResolver(definitionResolver);
        nested.setRootConceptNid(conceptNid);
        nested.setValue(definition);
        nested.getProperties().put(PARENT_PEEK_KEY, this);

        content = createContent(nested);
        popover = new PopOver(content);
        // Hover is read off the whole frame (root), which includes the arrow: the content
        // alone leaves the pointer "outside" while it crosses the arrow on its way in.
        Region frame = popover.getRoot();
        frame.hoverProperty().subscribe(() -> {
            if (frame.isHover()) {
                hideTimer.stop();
            } else {
                scheduleHide();
            }
        });
        popover.setDetachable(false);
        popover.setHeaderAlwaysVisible(false);
        popover.setCloseButtonEnabled(false);
        popover.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        // Hiding is the hover rule's (and the pin's) job, not the focus's: an auto-hiding popup
        // would close when a peek opened from inside it takes a click.
        popover.setAutoHide(false);
        popover.setOnHidden(e -> onHidden());
        // The popup's scene root is a plain pane behind the rounded frame, and the owner
        // window's scene-level stylesheet (komet.css, `.root { white }`) reaches popup scene
        // roots: painted white, it squares off the frame's corners and cuts its shadow.
        popover.getScene().getRoot().setStyle("-fx-background-color: transparent;");
        // The frame (the skin's border path) is styled from the control's stylesheet under the
        // .popover.ditree-peek selector: the ControlsFX default strokes it with a top-to-bottom
        // gradient that fades the top corners out.
        popover.getStyleClass().add("ditree-peek");
        popover.getRoot().getStylesheets().add(STYLESHEET);

        parentPeek().ifPresent(parent -> parent.childShown(this));
        keyScene = chip.getScene();
        keyScene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        // Just below the chip, never over it: the default (arrow tip 4px inside the owner)
        // lands the popup window under the pointer, which un-hovers the chip and closes the
        // popover it just opened — an open/close loop. The hover grace covers the gap.
        popover.show(chip, -OWNER_GAP);
        // Escape also has to work while the popover itself holds the focus (after a click in it).
        popover.getScene().addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
    }

    /**
     * The popover's content: a toolbar (premise label, pin, close) over the nested definition
     * tree, the tree capped in height and scrolling beyond it.
     */
    private Region createContent(KLReadOnlyDiTreeControl nested) {
        Label kindLabel = new Label("STATED DEFINITION");
        kindLabel.getStyleClass().add("ditree-peek-kind");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region pinIcon = new Region();
        pinIcon.getStyleClass().add("ditree-peek-pin-icon");
        ToggleButton pinButton = new ToggleButton("", pinIcon);
        pinButton.getStyleClass().addAll("ditree-peek-button", "ditree-peek-pin-button");
        pinButton.setFocusTraversable(false);
        Tooltip.install(pinButton, new Tooltip("Keep open"));
        pinButton.selectedProperty().subscribe(() -> {
            pinned = pinButton.isSelected();
            if (pinned) {
                hideTimer.stop();
            } else {
                scheduleHide();
            }
        });

        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("ditree-peek-close-icon");
        Button closeButton = new Button("", closeIcon);
        closeButton.getStyleClass().addAll("ditree-peek-button", "ditree-peek-close-button");
        closeButton.setFocusTraversable(false);
        Tooltip.install(closeButton, new Tooltip("Close"));
        closeButton.setOnAction(e -> hideNow());

        HBox toolbar = new HBox(kindLabel, spacer, pinButton, closeButton);
        toolbar.getStyleClass().add("ditree-peek-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(nested);
        scrollPane.getStyleClass().add("ditree-peek-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox box = new VBox(toolbar, scrollPane);
        box.getStyleClass().add("ditree-peek-popover");
        box.getStylesheets().add(STYLESHEET);
        return box;
    }

    /*=========================================================================*
     * Hiding                                                                  *
     *=========================================================================*/

    /** Starts the grace period, after which the popover hides unless something holds it. */
    private void scheduleHide() {
        if (popover != null && !pinned) {
            hideTimer.playFromStart();
        }
    }

    private void hideIfIdle() {
        if (popover == null || pinned || chip.isHover() || popover.getRoot().isHover() || !openChildren.isEmpty()) {
            return;
        }
        popover.hide();
    }

    private void hideNow() {
        showTimer.stop();
        hideTimer.stop();
        if (popover != null) {
            popover.hide();
        }
    }

    /** Hides the deepest popover of the chain this peek heads. */
    private void hideDeepest() {
        if (openChildren.isEmpty()) {
            hideNow();
        } else {
            openChildren.getLast().hideDeepest();
        }
    }

    private void onHidden() {
        keyScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        keyScene = null;
        // Hiding a popup hides the popups it owns, so the child peeks are on their way out
        // too; drop them here rather than wait, so a parent asking "any open children?" is
        // answered by the time its own grace period ends.
        openChildren.clear();
        popover = null;
        content = null;
        pinned = false;
        parentPeek().ifPresent(parent -> parent.childHidden(this));
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && popover != null) {
            hideDeepest();
            event.consume();
        }
    }

    /*=========================================================================*
     * Chaining                                                                *
     *=========================================================================*/

    private Optional<DiTreeDefinitionPeek> parentPeek() {
        return Optional.ofNullable(owner.getProperties().get(PARENT_PEEK_KEY))
                .map(DiTreeDefinitionPeek.class::cast);
    }

    private void childShown(DiTreeDefinitionPeek child) {
        openChildren.add(child);
        hideTimer.stop();
    }

    private void childHidden(DiTreeDefinitionPeek child) {
        openChildren.remove(child);
        if (openChildren.isEmpty()) {
            scheduleHide();
        }
    }
}
