package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewWithOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlToolbarItem;
import dev.ikm.komet.layout.KlView;
import dev.ikm.komet.layout.controls.KlDrawer;
import dev.ikm.komet.layout.selection.KlSelectionContext;
import dev.ikm.komet.layout.selection.KlSelectionContextFactory;
import dev.ikm.komet.layout_engine.toolbar.NodeToolbarItem;
import dev.ikm.komet.layout_engine.toolbar.SpacerToolbarItem;
import dev.ikm.komet.layout_engine.toolbar.ToggleToolbarItem;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.layout_engine.component.view.ViewContext;
import dev.ikm.komet.layout_engine.window.DraggableSupport;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code AbstractHostCard} is the shared infrastructure for a "card on a surface": the chrome, the
 * engine-native drag, the per-card coordinate context, the bind/unbind lifecycle, and the framework
 * save/restore plumbing — everything a {@link dev.ikm.komet.layout.KlCard} needs to be a loosely
 * coupled, persistable citizen of a workspace surface, independent of what its <em>content</em> is.
 *
 * <p>It owns:
 * <ul>
 *   <li>a transparent root over a bordered, rounded {@linkplain #body body}, with a floating tab and
 *       an icon toolbar made draggable on the workspace via {@link DraggableSupport};</li>
 *   <li>a real per-card {@link ViewContext} — a live override of whatever context is above the card
 *       in the scene graph (discovered at bind via {@link KlView#context}, never hard-wired) — with
 *       its popup-ready {@link ViewProperties} published for the standard View control;</li>
 *   <li>the {@link #knowledgeLayoutBind()}/{@link #knowledgeLayoutUnbind()} lifecycle, with realization
 *       deferred to bind (the card must be in the composed scene graph for the coordinate cascade to
 *       find its parent), and a clean teardown that avoids the listener leak kview chapter windows
 *       suffer;</li>
 *   <li>framework persistence of the journal topic plus the {@code subArea*} save/revert/restore
 *       hooks, so concrete cards persist their own content identity through the standard flow.</li>
 * </ul>
 *
 * <p><b>Content is the variation point.</b> Concrete cards supply the body content and chrome details
 * through a small set of hooks — {@link #renderContent()}, {@link #clearContent()}, {@link #cardTitle()},
 * {@link #buildToolbarControls(HBox)}, {@link #contributeToHeader(VBox, Region)}, {@link #refreshHeader()},
 * {@link #subCardSave()} / {@link #subCardRestore()} — without re-implementing the host machinery. So a
 * component view, a realized editor layout, and a hosted tool area are all the same kind of citizen,
 * differing only in what fills the body and toolbar.
 *
 * @see DynamicCard DynamicCard — content is a realized editor-designed layout
 */
public abstract class AbstractHostCard extends CardBlueprint {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHostCard.class);

    /** The card's coordinate of record — a live override of the host view (#660-style authority-first). */
    private ViewProperties cardViewProperties;
    /** The card's KL context wrapping {@link #cardViewProperties}; attached as {@code KL_CONTEXT}. */
    private ViewContext cardViewContext;
    /** The override view backing {@link #cardViewProperties}; retained to remove {@link #reRenderListener}. */
    private ObservableView cardView;
    /** Re-render trigger on coordinate change; removed on unbind to avoid a listener leak. */
    private ChangeListener<ViewCoordinateRecord> reRenderListener;
    /**
     * Captured coordinate override — the resolved coordinate ({@code getValue()}) when the card view carries any
     * pin — re-applied as a DELTA against {@link #pendingViewBaseline} at the next bind (see
     * {@link ObservableViewWithOverride#setOverridesFromDelta}), so a per-card override survives a rebind, a newly
     * inserted parent context, and a restart, and so only genuinely-pinned dimensions re-pin — leaving inherited
     * ones tracking the current parent even when it changed between sessions (ike-issues#745).
     */
    private ViewCoordinateRecord pendingViewOverride;

    /**
     * The inherited parent baseline ({@code getOriginalValue()}) captured alongside {@link #pendingViewOverride}.
     * The genuinely-pinned dimensions are exactly those where the override differs from this baseline, so the two
     * together carry the override DELTA without persisting a per-dimension flag (ike-issues#745). Null for a legacy
     * override persisted before the baseline was stored — re-applied via the whole-value
     * {@link ObservableViewWithOverride#setOverrides} fallback.
     */
    private ViewCoordinateRecord pendingViewBaseline;

    /** Journal topic used by content presenters for event coordination. */
    private UUID journalTopic;
    /** Optional close action set by the host; when present, the chrome shows a close control. */
    private Runnable onCloseRequest;
    /** Removes the engine-native window-drag handlers on unbind (avoids a handler leak). */
    private Subscription dragSubscription;

    /** The card header (top of the body); holds the icon toolbar plus subclass content. */
    protected final VBox header = new VBox();
    /** The bordered, rounded card body; its center holds the card content (set by the subclass). */
    protected final BorderPane body = new BorderPane();

    /**
     * Preference key for the card's framework-persisted journal topic. The {@code dynamicCard.} prefix
     * is retained (rather than a generic {@code hostCard.}) so cards persisted before this base was
     * extracted still restore their topic.
     */
    private static final String JOURNAL_TOPIC_KEY = "dynamicCard.journalTopic";
    /** Preference key for the card's framework-persisted per-card coordinate override (a {@link ViewCoordinateRecord}). */
    private static final String VIEW_OVERRIDE_KEY = "dynamicCard.viewOverride";
    /** Preference key for the inherited parent baseline captured with the override, to re-apply it as a delta (ike-issues#745). */
    private static final String VIEW_BASELINE_KEY = "dynamicCard.viewBaseline";

    /**
     * Constructs a host card from a restored preferences node.
     *
     * @param preferences the preferences node backing the card
     */
    protected AbstractHostCard(KometPreferences preferences) {
        super(preferences);
        initContainer();
    }

    /**
     * Constructs a fresh host card with a provisioned preferences node.
     *
     * @param preferencesFactory the factory that provisions the card's preferences node
     * @param areaFactory        the area factory that produced this card
     */
    protected AbstractHostCard(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
        initContainer();
    }

    /**
     * Installs the transparent root, the bordered body, and the header (top of the body). The body's
     * center — the card content — is supplied by the concrete card via {@link #setCardContent(Node)}.
     */
    private void initContainer() {
        fxObject().getStyleClass().add("dynamic-card-root");
        header.getStyleClass().add("dynamic-card-header");
        body.getStyleClass().add("dynamic-card-body");
        body.setTop(header);

        URL stylesheet = AbstractHostCard.class.getResource("dynamic-card.css");
        if (stylesheet != null) {
            fxObject().getStylesheets().add(stylesheet.toExternalForm());
        }

        fxObject().setCenter(body);
    }

    /*******************************************************************************
     *  Injection                                                                  *
     ******************************************************************************/

    /**
     * Sets the card content (the body's center). Concrete cards call this once during construction.
     *
     * @param content the content node
     */
    protected final void setCardContent(Node content) {
        body.setCenter(content);
    }

    /**
     * Sets the journal topic used by content presenters for event coordination.
     *
     * @param journalTopic the journal topic
     */
    public void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }

    /**
     * Returns the journal topic, or {@code null} if not set.
     *
     * @return the journal topic
     */
    protected UUID journalTopic() {
        return journalTopic;
    }

    /**
     * Sets the action invoked when the chrome's close control is activated. When non-null, the header
     * shows a close control; the host typically wires this to its own close/removal logic.
     *
     * @param onCloseRequest the close action, or {@code null} for no close control
     */
    public void setOnCloseRequest(Runnable onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    /**
     * Runs the close action if one is set. The base wires the chrome's close control to this; content
     * (e.g. a hosted tool with its own close control) can route through it too.
     */
    protected final void requestClose() {
        if (onCloseRequest != null) {
            onCloseRequest.run();
        }
    }

    /*******************************************************************************
     *  Drawers                                                                    *
     ******************************************************************************/

    /** Key prefix for each drawer's persisted open state, suffixed by the drawer's add order. */
    private static final String DRAWER_EXPANDED_KEY_PREFIX = "drawer.expanded.";

    /** Drawers added to this card, each with the area it hosts and its persistence key. */
    private final List<DrawerHandle> drawers = new ArrayList<>();

    /** Toolbar items created once (each with its own prefs node) and re-placed when the header rebuilds. */
    private SpacerToolbarItem spacerItem;
    private NodeToolbarItem leadingItem;
    private NodeToolbarItem closeItem;

    /** Overlay created on the first drawer; holds the card content with drawers that slide out beyond its edges. */
    private StackPane drawerContainer;

    private KlSelectionContext selectionContext;

    /** A drawer plus the area it reveals, the preference key for its open state, and its toggle label. */
    private record DrawerHandle(KlDrawer drawer, KlArea<? extends Region> area, String prefKey, String toggleLabel,
                                ToggleToolbarItem toggleItem) {
    }

    /**
     * Adds a slide-out drawer on the given side hosting the given area's content, with a "Properties" toggle.
     *
     * @param side the edge to dock the drawer to
     * @param area the area whose {@link KlArea#fxObject() content} the drawer reveals
     * @return the created drawer
     */
    public KlDrawer addDrawer(Side side, KlArea<? extends Region> area) {
        return addDrawer(side, area, "Properties");
    }

    /**
     * Adds a slide-out drawer on the given side hosting the given area's content, with a labeled toggle. The
     * drawer overlays the card content from that edge; the toggle is added to the card toolbar; the drawer's
     * open state persists with the card; and the hosted area participates in the card's bind/unbind lifecycle.
     *
     * @param side        the edge to dock the drawer to
     * @param area        the area whose {@link KlArea#fxObject() content} the drawer reveals
     * @param toggleLabel the text of the toolbar toggle that opens the drawer
     * @return the created drawer
     */
    public KlDrawer addDrawer(Side side, KlArea<? extends Region> area, String toggleLabel) {
        return addDrawerInternal(side, area.fxObject(), area, toggleLabel);
    }

    /**
     * Adds a slide-out drawer on the given side hosting plain content (no hosted area), with a labeled toggle.
     * Use this for content that is not (yet) a {@link KlArea}; the drawer's open state still persists with the
     * card, but no area lifecycle is bound.
     *
     * @param side        the edge to dock the drawer to
     * @param content     the content region the drawer reveals
     * @param toggleLabel the text of the toolbar toggle that opens the drawer
     * @return the created drawer
     */
    public KlDrawer addDrawer(Side side, Region content, String toggleLabel) {
        return addDrawerInternal(side, content, null, toggleLabel);
    }

    /**
     * Shared drawer wiring: installs the drawer in the overlay on the given edge, records it for persistence
     * and lifecycle, restores its persisted open state, and (when the card is already realized) binds any
     * hosted area and refreshes the header so the toggle appears.
     *
     * @param side        the edge to dock the drawer to
     * @param content     the content region the drawer reveals
     * @param area        the hosted area for lifecycle binding, or {@code null} for plain content
     * @param toggleLabel the text of the toolbar toggle that opens the drawer
     * @return the created drawer
     */
    private KlDrawer addDrawerInternal(Side side, Region content, KlArea<? extends Region> area, String toggleLabel) {
        ensureDrawerContainer();
        KlDrawer drawer = new KlDrawer(side, content);
        drawer.setContent(buildDrawerChrome(content, () -> drawer.setExpanded(false)));
        placeDrawerInContainer(drawer, side);

        String prefKey = DRAWER_EXPANDED_KEY_PREFIX + drawers.size();
        // The toggle is a first-class KlArea item: its own preferences node, lifecycle, save/restore. Created
        // once here (bound for the life of the card); the header rebuild only re-adds its node.
        ToggleToolbarItem toggleItem = ToggleToolbarItem.factory()
                .create(KlPreferencesFactory.create(preferences(), ToggleToolbarItem.class));
        toggleItem.setLabel(toggleLabel);
        toggleItem.setSelected(drawer.expandedProperty());
        toggleItem.knowledgeLayoutBind();
        drawers.add(new DrawerHandle(drawer, area, prefKey, toggleLabel, toggleItem));

        // Restore the persisted open state without animation (a restored card opens its drawer instantly),
        // then enable animation for subsequent user toggles.
        if (preferences() != null) {
            drawer.setAnimated(false);
            drawer.setExpanded(preferences().getBoolean(prefKey, false));
            drawer.setAnimated(true);
        }
        // A user toggle marks the card changed (so the open state persists) and grows or shrinks the card so
        // the drawer slides out beside the content, widening the card, rather than overlaying the content.
        updateDrawerOpenClass(side, drawer.expandedProperty().get());
        drawer.expandedProperty().addListener((obs, wasOpen, isOpen) -> {
            changedProperty().set(true);
            updateDrawerOpenClass(side, isOpen);
        });

        if (isRealized()) {
            // The card is already on screen: bind the hosted area and refresh the header so its toggle appears.
            if (area != null) {
                area.knowledgeLayoutBind();
            }
            buildHeader();
        }
        return drawer;
    }

    /**
     * Lazily wraps the card's content (the body center) in a container that holds the content in the center and
     * a drawer in each edge slot. Placing a drawer <em>beside</em> the content (rather than over it) lets the
     * card widen as the drawer opens, while the header — and its drawer toggles — stay visible above the content.
     */
    private void ensureDrawerContainer() {
        if (drawerContainer == null) {
            drawerContainer = new StackPane();
            drawerContainer.getStyleClass().add("dynamic-card-drawer-overlay");
            // Overlay the drawer over the whole body — navy title bar + content — so a slid-out drawer aligns
            // with the card's title bar (the bar reads as extending across the drawer), not just the content
            // beneath it.
            Node bodyNode = fxObject().getCenter();
            fxObject().setCenter(null);
            if (bodyNode != null) {
                drawerContainer.getChildren().add(bodyNode);
            }
            fxObject().setCenter(drawerContainer);
        }
    }

    /**
     * Adds the drawer to the overlay pinned to its edge, then pushes it <em>outward beyond</em> that edge by its
     * own (animating) extent, so on reveal it slides out as a separate node — never covering the content and
     * never resizing the card. The drawer renders past the card's footprint; raising the card so it sits over a
     * neighbour is the separate {@code #716} concern.
     */
    private void placeDrawerInContainer(KlDrawer drawer, Side side) {
        StackPane.setAlignment(drawer, alignmentForSide(side));
        // The drawer slides out beyond the docked edge (translate by its own size). On the cross axis it fills
        // the body, so a side drawer is as tall as the card (its title bar aligns with the card's, the divider
        // runs the full height) and a top/bottom drawer is as wide.
        switch (side) {
            case RIGHT -> {
                drawer.translateXProperty().bind(drawer.widthProperty());
                drawer.prefHeightProperty().bind(drawerContainer.heightProperty());
                drawer.maxHeightProperty().bind(drawerContainer.heightProperty());
            }
            case LEFT -> {
                drawer.translateXProperty().bind(drawer.widthProperty().negate());
                drawer.prefHeightProperty().bind(drawerContainer.heightProperty());
                drawer.maxHeightProperty().bind(drawerContainer.heightProperty());
            }
            case BOTTOM -> {
                drawer.translateYProperty().bind(drawer.heightProperty());
                drawer.prefWidthProperty().bind(drawerContainer.widthProperty());
                drawer.maxWidthProperty().bind(drawerContainer.widthProperty());
            }
            case TOP -> {
                drawer.translateYProperty().bind(drawer.heightProperty().negate());
                drawer.prefWidthProperty().bind(drawerContainer.widthProperty());
                drawer.maxWidthProperty().bind(drawerContainer.widthProperty());
            }
        }
        drawerContainer.getChildren().add(drawer);
    }

    /** Maps a docked side to the {@link Pos} that pins a drawer to that edge of the overlay. */
    /**
     * Wraps a drawer's content in card-matching chrome: a navy header bar — the drawer's own toolbar, carrying
     * a close control (and, later, drawer tools) — above the content, framed with a divider on the docked edge
     * so the slid-out drawer reads as an extension of the card's title bar rather than a detached panel.
     *
     * @param content the drawer's content
     * @param onClose invoked when the drawer's own close control is pressed
     * @return the chrome wrapping the content
     */
    private Region buildDrawerChrome(Region content, Runnable onClose) {
        // The drawer's own toolbar bar (same navy as the card's title bar). The "PROPERTIES" label lives on the
        // card's toggle — not repeated here — so this bar carries the close control, right-aligned past a spacer.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("close-window");
        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().add("dynamic-card-close-button");
        closeButton.setOnAction(event -> onClose.run());

        HBox drawerHeader = new HBox(spacer, closeButton);
        drawerHeader.setAlignment(Pos.CENTER_LEFT);
        drawerHeader.getStyleClass().add("dynamic-card-drawer-header");

        VBox chrome = new VBox(drawerHeader, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        chrome.getStyleClass().add("dynamic-card-drawer");
        return chrome;
    }

    /**
     * Squares the card's corners on the docked edge while a drawer is open there, so the card and the slid-out
     * drawer meet flush — the rounded corners would otherwise leave a notch between them.
     *
     * @param side the drawer's docked side
     * @param open whether the drawer is open
     */
    private void updateDrawerOpenClass(Side side, boolean open) {
        String openClass = "drawer-open-" + side.name().toLowerCase();
        body.getStyleClass().remove(openClass);
        if (open) {
            body.getStyleClass().add(openClass);
        }
    }

    private static Pos alignmentForSide(Side side) {
        return switch (side) {
            case TOP -> Pos.TOP_LEFT;
            case BOTTOM -> Pos.BOTTOM_LEFT;
            case LEFT -> Pos.TOP_LEFT;
            case RIGHT -> Pos.TOP_RIGHT;
        };
    }

    /** The leading-controls item, wrapping the card's own controls; created once, content refreshed per build. */
    /**
     * The per-card selection nexus, created once and registered on the card's root node so the body (writer)
     * and the drawer (reader) discover it by walking up the scene graph rather than referencing each other.
     *
     * @return the selection context
     */
    protected KlSelectionContext selectionContext() {
        if (selectionContext == null) {
            selectionContext = KlSelectionContextFactory.provider().create();
            fxObject().getProperties().put(KlSelectionContext.class.getName(), selectionContext);
        }
        return selectionContext;
    }

    private NodeToolbarItem leadingItem() {
        if (leadingItem == null) {
            leadingItem = NodeToolbarItem.factory()
                    .create(KlPreferencesFactory.create(preferences(), NodeToolbarItem.class));
        }
        return leadingItem;
    }

    /** The growing spacer item that right-aligns the trailing items; created once. */
    private SpacerToolbarItem spacerItem() {
        if (spacerItem == null) {
            spacerItem = SpacerToolbarItem.factory()
                    .create(KlPreferencesFactory.create(preferences(), SpacerToolbarItem.class));
        }
        return spacerItem;
    }

    /** The close-control item, built once and reused (added to the bar only when a close action is set). */
    private NodeToolbarItem closeItem() {
        if (closeItem == null) {
            Region closeIcon = new Region();
            closeIcon.getStyleClass().add("close-window");
            Button closeButton = new Button();
            closeButton.setGraphic(closeIcon);
            closeButton.getStyleClass().add("dynamic-card-close-button");
            closeButton.setOnAction(event -> requestClose());
            closeItem = NodeToolbarItem.factory()
                    .create(KlPreferencesFactory.create(preferences(), NodeToolbarItem.class));
            closeItem.setNode(closeButton);
        }
        return closeItem;
    }

    /** Binds every drawer's hosted area (if any) into the knowledge-layout lifecycle. */
    private void bindDrawers() {
        for (DrawerHandle handle : drawers) {
            if (handle.area() != null) {
                handle.area().knowledgeLayoutBind();
            }
        }
    }

    /** Unbinds every drawer's hosted area (if any) from the knowledge-layout lifecycle. */
    private void unbindDrawers() {
        for (DrawerHandle handle : drawers) {
            handle.toggleItem().knowledgeLayoutUnbind();
            if (handle.area() != null) {
                handle.area().knowledgeLayoutUnbind();
            }
        }
    }

    /*******************************************************************************
     *  Realization                                                                *
     ******************************************************************************/

    /**
     * Establishes this card's coordinate context, builds the header, and realizes content. Invoked from
     * {@link #knowledgeLayoutBind()} once the card is in the composed scene graph — <em>not</em> at
     * construction, because the card discovers its container's coordinate from the scene graph.
     */
    public void realize() {
        if (cardViewContext == null) {
            establishViewContext();
        }
        buildHeader();
        renderContent();
        bindDrawers();
    }

    /**
     * Establishes this card's coordinate of record as a live override of the host view, publishing its
     * popup-ready {@link ViewProperties} on the context so the standard View control can edit it. A
     * change listener re-realizes the card when the coordinate changes.
     */
    private void establishViewContext() {
        // Loose coupling: discover the container's context by walking the scene graph for the nearest
        // KL_CONTEXT (the journal publishes its JournalKlContext on the workspace pane). The card binds
        // to "whatever context is above me" — chapter, Surface, anything — never a hard-wired host view.
        KlContext parentContext = KlView.context(fxObject());
        this.cardViewContext = ViewContext.createChildOf(this, parentContext, "Dynamic card view");
        this.cardViewProperties = cardViewContext.viewProperties();
        this.cardView = cardViewProperties.nodeView();

        this.reRenderListener = (obs, oldCoordinate, newCoordinate) -> Platform.runLater(() -> {
            if (cardViewContext != null) {
                refresh();
            }
        });
        this.cardView.addListener(reRenderListener);

        // Re-apply any captured/persisted override onto this freshly created child view AFTER attaching the
        // listener — the view must be "listening" for a per-dimension pin to propagate consistently into the
        // composite coordinate (and the override indicators); applying it while the view has no observers leaves
        // the dimension pins and the resolved coordinate out of sync. Re-apply as a DELTA against the captured
        // baseline: only dimensions that were genuinely pinned (override differs from the parent-at-capture)
        // re-pin, so dimensions that were merely inherited keep tracking THIS (possibly changed) parent instead
        // of freezing at the stale captured value — the override survives a rebind, a newly inserted parent
        // context, a restart, AND a journal-coordinate change between sessions (ike-issues#745).
        if (pendingViewOverride != null && cardView instanceof ObservableViewWithOverride overrideView) {
            if (pendingViewBaseline != null) {
                overrideView.setOverridesFromDelta(pendingViewOverride, pendingViewBaseline);
            } else {
                // Legacy override persisted before the baseline was stored — re-apply whole-value.
                overrideView.setOverrides(pendingViewOverride);
            }
        }
    }

    /**
     * Builds the chrome: a draggable tab (nine-dots handle + the card {@linkplain #cardTitle() title})
     * over an icon toolbar carrying the concrete card's {@linkplain #buildToolbarControls(HBox) controls}
     * and — when a close action is set — a close control. Subclasses then contribute further header
     * content via {@link #contributeToHeader}. Finally the whole card is made draggable on the workspace
     * by its tab and toolbar via the engine-native {@link DraggableSupport}.
     */
    private void buildHeader() {
        header.getChildren().clear();

        // Tab: a nine-dots drag handle + the card title.
        Region dragHandle = new Region();
        dragHandle.getStyleClass().add("nine-dots-icon");
        Label titleLabel = new Label(cardTitle());
        titleLabel.getStyleClass().add("dynamic-card-tab-text");
        HBox tab = new HBox(6, dragHandle, titleLabel);
        tab.getStyleClass().add("dynamic-card-tab");
        tab.setAlignment(Pos.CENTER_LEFT);
        tab.setCursor(Cursor.CLOSED_HAND);
        FlowPane tabRow = new FlowPane(tab);   // hugs the tab to its content width at the top-left
        tabRow.getStyleClass().add("dynamic-card-tab-row");

        // Toolbar: a GridPane of KlToolbarItems — the card's controls (leading), a growing spacer, the drawer
        // toggles, then the close control. Each item's node sits in its own column; the spacer's column grows,
        // so the toggles and close are pushed to the trailing edge.
        GridPane toolBar = new GridPane();
        toolBar.getStyleClass().add("dynamic-card-toolbar");
        toolBar.setMaxWidth(Double.MAX_VALUE);

        HBox leadingControls = new HBox(8);
        leadingControls.setAlignment(Pos.CENTER_LEFT);
        buildToolbarControls(leadingControls);
        leadingItem().setNode(leadingControls);

        List<KlToolbarItem<?>> items = new ArrayList<>();
        items.add(leadingItem());
        items.add(spacerItem());
        for (DrawerHandle handle : drawers) {
            items.add(handle.toggleItem());
        }
        if (onCloseRequest != null) {
            items.add(closeItem());
        }
        for (int column = 0; column < items.size(); column++) {
            KlToolbarItem<?> item = items.get(column);
            Region itemNode = item.fxObject();
            GridPane.setColumnIndex(itemNode, column);
            GridPane.setValignment(itemNode, VPos.CENTER);
            // Only the spacer's column grows; the trailing items (toggles, close) are pushed flush to the right.
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHgrow(item instanceof SpacerToolbarItem ? Priority.ALWAYS : Priority.NEVER);
            toolBar.getColumnConstraints().add(constraints);
            toolBar.getChildren().add(itemNode);
        }

        fxObject().setTop(tabRow);   // the tab floats above the bordered body; workspace shows beside it
        header.getChildren().add(toolBar);
        contributeToHeader(header, toolBar);
        refreshHeader();

        // Make the card draggable on the workspace by its tab + toolbar (engine-native drag).
        if (dragSubscription != null) {
            dragSubscription.unsubscribe();
        }
        dragSubscription = DraggableSupport.addDraggableNodes(fxObject(), tab, toolBar);
    }

    /**
     * Re-realizes body and header after a coordinate change or revert.
     */
    protected void refresh() {
        renderContent();
        refreshHeader();
    }

    /*******************************************************************************
     *  Content + chrome hooks (the variation points)                              *
     ******************************************************************************/

    /**
     * Returns the card's tab title.
     *
     * @return the title text
     */
    protected abstract String cardTitle();

    /**
     * Realizes (or re-realizes) the card's body content. Called from {@link #realize()} and on each
     * {@link #refresh()}.
     */
    protected abstract void renderContent();

    /**
     * Removes the realized content so the card can be rebuilt on a coordinate change. Default does
     * nothing; cards with rebuildable content override it.
     */
    protected void clearContent() {
        // No rebuildable content in the base.
    }

    /**
     * Extension point: a concrete card adds its toolbar controls (e.g. a coordinate control, a Publish
     * action) to the icon toolbar. The base then appends a growing spacer and the close control. Default
     * adds nothing (a minimal toolbar).
     *
     * @param toolBar the icon toolbar
     */
    protected void buildToolbarControls(HBox toolBar) {
        // A minimal card has no toolbar controls of its own.
    }

    /**
     * Wires a toolbar coordinate control's orange {@code "override"} state to this card's coordinate of
     * record. The control is lit exactly when the card genuinely pins at least one coordinate dimension
     * relative to its inherited parent — read from the per-dimension override flags via
     * {@link dev.ikm.komet.framework.view.ObservableCoordinate#hasOverrides()}, the single parent-relative
     * predicate the View Options panel dots, the popup's "remove all overrides" control, and this card's
     * persistence capture ({@link #captureViewOverride()}) all already use. The crosshair, the dots, and the
     * persisted state therefore cannot disagree about whether the card overrides its parent.
     *
     * <p>It deliberately does <em>not</em> compare the whole resolved {@code getValue()} against
     * {@code getOriginalValue()}: that record-level value diff lights on phantom record-rebuild artifacts
     * (e.g. the {@code EditCoordinateRecord} {@code destinationModule}/{@code promotionPath} field swap)
     * with no real pin, so the crosshair would read orange while every panel dot stayed dark
     * (IKE-Network/ike-issues#743).
     *
     * @param coordinateButton the toolbar coordinate control whose {@code "override"} style class is driven
     */
    protected void wireCoordinateOverrideIndicator(MenuButton coordinateButton) {
        ObservableView cardView = getCardViewProperties().nodeView();
        Runnable syncOverrideIndicator = () -> {
            coordinateButton.getStyleClass().remove("override");
            if (cardView.hasOverrides()) {
                coordinateButton.getStyleClass().add("override");
            }
        };
        syncOverrideIndicator.run();
        cardView.subscribe(syncOverrideIndicator);
    }

    /**
     * Extension point: subclasses add further header content (identity rows, action buttons). The base
     * has already added the icon toolbar. Default does nothing.
     *
     * @param headerBox the header container (toolbar already added)
     * @param toolBar   the icon toolbar, for adding further controls
     */
    protected void contributeToHeader(VBox headerBox, Region toolBar) {
        // No additional header content for a minimal card.
    }

    /**
     * Extension point: refresh dynamic header content after a coordinate change. Called after each
     * (re)render. Default does nothing.
     */
    protected void refreshHeader() {
        // Nothing dynamic in the base header.
    }

    /*******************************************************************************
     *  Accessors                                                                  *
     ******************************************************************************/

    /**
     * Returns this card's coordinate of record — a live override of the host view — or {@code null}
     * before {@link #realize()} has established the context.
     *
     * @return the card's view properties, or {@code null} if not yet realized
     */
    public ViewProperties getCardViewProperties() {
        return cardViewProperties;
    }

    /**
     * Indicates whether this card's coordinate context has been established (i.e. the card is realized).
     *
     * @return {@code true} once {@link #realize()} has run
     */
    protected boolean isRealized() {
        return cardViewContext != null;
    }

    /*******************************************************************************
     *  Lifecycle                                                                  *
     ******************************************************************************/

    @Override
    public void contextChanged() {
        if (cardViewContext != null) {
            refresh();
        }
    }

    @Override
    public void knowledgeLayoutBind() {
        // Establish the coordinate context and realize content NOW — the card is in the composed scene
        // graph (this runs from the host's onShown), deferred one pulse so composition has settled.
        // Binding here rather than at construction is the layout-lifecycle step kview's chapter windows
        // skip; it is also what lets a restored card bind to the live journal coordinate exactly as a
        // freshly created card does.
        Platform.runLater(() -> {
            try {
                if (cardViewContext == null) {
                    realize();
                }
            } catch (RuntimeException e) {
                // A card's content failed to realize (e.g. a restored card whose plugin is gone). Contain it
                // here so one card cannot crash journal restore on the FX thread; the failure is logged.
                LOG.error("Card failed to realize on bind", e);
            }
            this.lifecycleState.set(LifecycleState.BOUND);
        });
    }

    @Override
    public void knowledgeLayoutUnbind() {
        unbindDrawers();
        teardownViewContext();
        clearContent();
    }

    /**
     * Removes the coordinate-change listener and releases the per-card context, preventing the listener
     * leak that the kview chapter-window binding suffers from.
     */
    private void teardownViewContext() {
        captureViewOverride();   // preserve a live override across rebind (re-applied at the next bind)
        if (dragSubscription != null) {
            dragSubscription.unsubscribe();
            dragSubscription = null;
        }
        if (cardView != null && reRenderListener != null) {
            cardView.removeListener(reRenderListener);
        }
        reRenderListener = null;
        if (cardViewContext != null) {
            cardViewContext.delete();
            cardViewContext = null;
        }
        cardView = null;
        cardViewProperties = null;
    }

    /*******************************************************************************
     *  Framework save / restore                                                   *
     ******************************************************************************/

    @Override
    protected void subAreaSave() {
        captureViewOverride();   // refresh the captured override from the live card view
        if (journalTopic != null) {
            preferences().put(JOURNAL_TOPIC_KEY, journalTopic.toString());
        }
        if (pendingViewOverride != null && pendingViewBaseline != null) {
            preferences().putObject(VIEW_OVERRIDE_KEY, pendingViewOverride);
            preferences().putObject(VIEW_BASELINE_KEY, pendingViewBaseline);
        } else {
            // No override (or no baseline) — drop any stale persisted copy of both.
            preferences().remove(VIEW_OVERRIDE_KEY);
            preferences().remove(VIEW_BASELINE_KEY);
        }
        for (DrawerHandle handle : drawers) {
            preferences().putBoolean(handle.prefKey(), handle.drawer().isExpanded());
        }
        subCardSave();
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        preferences().get(JOURNAL_TOPIC_KEY).ifPresent(topic -> this.journalTopic = UUID.fromString(topic));
        Optional<ViewCoordinateRecord> savedOverride = preferences().getObject(VIEW_OVERRIDE_KEY);
        savedOverride.ifPresent(record -> this.pendingViewOverride = record);
        // The baseline may be absent for a legacy override (pre-#745); establishViewContext then falls back to
        // the whole-value setOverrides re-apply.
        Optional<ViewCoordinateRecord> savedBaseline = preferences().getObject(VIEW_BASELINE_KEY);
        savedBaseline.ifPresent(record -> this.pendingViewBaseline = record);
        subCardRestore();
    }

    /**
     * Captures the card view's current coordinate override as a delta pair — its resolved coordinate
     * ({@link #pendingViewOverride}) plus the inherited parent baseline ({@link #pendingViewBaseline}) — when it
     * carries any pin, or clears both when it carries none. The two together identify exactly the pinned
     * dimensions (those where the resolved value differs from the baseline), so the override re-applies at the
     * next bind as a delta against the current parent (ike-issues#745). No-op while the view is not realized, so
     * a restored-but-not-yet-bound override is not wiped before it can be re-applied.
     */
    private void captureViewOverride() {
        if (cardView instanceof ObservableViewWithOverride overrideView && overrideView.hasOverrides()) {
            this.pendingViewOverride = overrideView.getValue();
            this.pendingViewBaseline = overrideView.getOriginalValue();
        } else if (cardView != null) {
            this.pendingViewOverride = null;
            this.pendingViewBaseline = null;
        }
    }

    @Override
    protected void subAreaRevert() {
        if (cardViewContext != null) {
            refresh();
        }
    }

    /**
     * Extension point: persist the concrete card's content identity to its own preferences node. The
     * base has already persisted the journal topic. Default does nothing.
     */
    protected void subCardSave() {
        // No content identity in the base.
    }

    /**
     * Extension point: restore the concrete card's content identity from its own preferences node. The
     * base has already restored the journal topic. Default does nothing.
     */
    protected void subCardRestore() {
        // No content identity in the base.
    }
}
