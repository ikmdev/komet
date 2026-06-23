package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlToolArea;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout.controls.ViewOptionsPopupHelper;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

/**
 * A {@link dev.ikm.komet.layout.KlCard} whose content is a hosted {@link KlToolArea} — for example the
 * Claude Assistant. It is the card-native replacement for the legacy {@code ToolAreaChapterKlWindow}:
 * the chrome, engine-native drag, per-card coordinate context, bind/unbind lifecycle, and framework
 * save/restore all come from {@link AbstractHostCard}; this class supplies only the tool as content.
 *
 * <p><b>Each tool card is sandboxed.</b> The hosted tool's preferences are allocated <em>under this
 * card's own per-instance preferences node</em> (via {@link KlPreferencesFactory#create(KometPreferences,
 * Class)}), not under a node keyed by the tool-area factory class. Two tool cards of the same kind — two
 * Claude Assistants in one journal — therefore never share a preferences node, so each keeps its own
 * conversation rail and history. (The legacy window used {@code sharedWindowPreferenceFactory(class)},
 * which keyed by factory class and caused every instance to collide on one shared node.)
 *
 * <p>The tool is instantiated once at {@link #realize()} and then observes the live card coordinate; a
 * coordinate-change refresh does not re-create it. On restore the tool reloads from its own node, so its
 * state round-trips with the card through the standard framework save/restore flow.
 */
public final class ToolCard extends AbstractHostCard {

    private static final Logger LOG = LoggerFactory.getLogger(ToolCard.class);

    // Framework-persisted identity of the hosted tool (in this card's own preferences node).
    private static final String TOOL_FACTORY_CLASS_KEY = "toolCard.factoryClass";
    private static final String TOOL_NAME_KEY = "toolCard.toolName";
    private static final String TOOL_AREA_NODE_KEY = "toolCard.toolAreaNode";

    /** Fully-qualified class name of the hosting {@link KlToolArea.Factory}. */
    private String toolFactoryClassName;
    /** Human-readable tool name (the tab title); persisted so the title is correct before realize. */
    private String toolName;
    /** Name of the tool area's preferences node, a child of this card's node (per-instance sandbox). */
    private String toolAreaNodeName;
    /** The hosted tool area; instantiated once at realize. */
    private KlToolArea<?> toolArea;

    private ToolCard(KometPreferences preferences) {
        super(preferences);
    }

    private ToolCard(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Sets the tool-area factory whose product this card hosts.
     *
     * @param toolAreaFactory the discovered tool-area factory
     */
    public void setToolAreaFactory(KlToolArea.Factory<?, ?> toolAreaFactory) {
        this.toolFactoryClassName = toolAreaFactory.getClass().getName();
        this.toolName = toolAreaFactory.toolName();
    }

    @Override
    protected String cardTitle() {
        return toolName != null ? toolName : "Tool";
    }

    @Override
    protected void renderContent() {
        // The hosted tool is instantiated once and observes the live card coordinate thereafter; a
        // coordinate-change refresh must NOT re-create it (that would discard its conversation state).
        if (toolArea == null) {
            instantiateTool();
        }
    }

    /**
     * Adds the tool card's toolbar control: the coordinate crosshair (the overridable View control wired to
     * this card's coordinate of record), so the hosted tool's view — the one its queries resolve against —
     * is visible and overridable, consistent with the tiles, the journal, and the editor cards.
     */
    @Override
    protected void buildToolbarControls(HBox toolBar) {
        MenuButton coordinateButton = new MenuButton();
        coordinateButton.getStyleClass().add("coordinate");
        coordinateButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        coordinateButton.setTooltip(new Tooltip("Coordinates"));
        ViewOptionsPopupHelper.setupViewCoordinateOptionsPopup(getCardViewProperties(),
                FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, fxObject(), coordinateButton, () -> { });

        // Light the crosshair from the card's per-dimension override flags (hasOverrides), the same
        // parent-relative predicate the panel dots and the persistence capture use, so the indicators agree.
        wireCoordinateOverrideIndicator(coordinateButton);

        toolBar.getChildren().add(coordinateButton);
    }

    /**
     * Instantiates (or restores) the hosted tool area into this card's own per-instance preferences node,
     * hands it the card coordinate and a close callback, and installs it as the card content.
     */
    private void instantiateTool() {
        if (toolFactoryClassName == null) {
            LOG.warn("ToolCard has no tool-area factory class; nothing to host");
            return;
        }
        final KlToolArea.Factory<?, ?> factory;
        try {
            factory = resolveFactory(toolFactoryClassName);
        } catch (RuntimeException e) {
            // The hosting tool's plugin is no longer present (removed, renamed, or migrated — e.g. an old
            // Claude Assistant tool window persisted before the Assistant became a ClaudeCard). Degrade to a
            // placeholder instead of crashing journal restore; closing the window removes it from the workspace.
            LOG.warn("Tool area '{}' is no longer available; showing a placeholder", toolFactoryClassName, e);
            setCardContent(toolUnavailablePlaceholder());
            return;
        }
        if (toolAreaNodeName != null && nodeExists(toolAreaNodeName)) {
            // Restore: re-create the tool from its own per-instance node (its rail/history reloads).
            this.toolArea = factory.restore(preferences().node(toolAreaNodeName));
        } else {
            // Fresh: allocate the tool a node UNDER this card's own per-instance node — never a shared
            // per-class node — so two tool cards of the same kind never collide.
            final KlPreferencesFactory toolPreferencesFactory =
                    KlPreferencesFactory.create(preferences(), factory.getClass());
            this.toolArea = factory.create(toolPreferencesFactory);
            this.toolAreaNodeName = toolArea.preferences().name();
        }
        this.toolName = factory.toolName();
        toolArea.setToolViewProperties(getCardViewProperties());
        toolArea.setOnCloseRequest(this::requestClose);
        setCardContent(toolArea.fxObject());
        LOG.info("ToolCard hosting {} in node {}", toolName, toolAreaNodeName);
    }

    /** Body shown when the hosting tool's plugin can no longer be resolved (removed/renamed/migrated). */
    private static Node toolUnavailablePlaceholder() {
        Label label = new Label("This tool is no longer available.\nClose this window to remove it.");
        label.setWrapText(true);
        label.setPadding(new Insets(24));
        return label;
    }

    /** Whether a child node of this card's preferences node exists (treats a backing-store error as absent). */
    private boolean nodeExists(String nodeName) {
        try {
            return preferences().nodeExists(nodeName);
        } catch (BackingStoreException e) {
            return false;
        }
    }

    /** Resolves a {@link KlToolArea.Factory} by class name across module layers (spans the plugin layer). */
    private static KlToolArea.Factory<?, ?> resolveFactory(String factoryClassName) {
        try {
            final Class<?> factoryClass = PluggableService.forName(factoryClassName);
            return (KlToolArea.Factory<?, ?>) factoryClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to resolve tool-area factory " + factoryClassName, e);
        }
    }

    /**
     * Indicates whether this card has its hosted-tool identity (the tool-area factory class), mirroring
     * {@code DynamicCard.isContentRestored()} so a host can detect an empty card node after a revert.
     *
     * @return {@code true} if the tool factory is known
     */
    public boolean isContentRestored() {
        return toolFactoryClassName != null;
    }

    /*******************************************************************************
     *  Framework save / restore                                                   *
     ******************************************************************************/

    @Override
    protected void subCardSave() {
        if (toolFactoryClassName != null) {
            preferences().put(TOOL_FACTORY_CLASS_KEY, toolFactoryClassName);
        }
        if (toolName != null) {
            preferences().put(TOOL_NAME_KEY, toolName);
        }
        if (toolAreaNodeName != null) {
            preferences().put(TOOL_AREA_NODE_KEY, toolAreaNodeName);
        }
    }

    @Override
    protected void subCardRestore() {
        preferences().get(TOOL_FACTORY_CLASS_KEY).ifPresent(value -> this.toolFactoryClassName = value);
        preferences().get(TOOL_NAME_KEY).ifPresent(value -> this.toolName = value);
        preferences().get(TOOL_AREA_NODE_KEY).ifPresent(value -> this.toolAreaNodeName = value);
    }

    /*******************************************************************************
     *  Factory                                                                    *
     ******************************************************************************/

    /**
     * Creates a {@code ToolCard} shell hosting the given tool-area factory's product. Realization (the
     * tool's instantiation into this card's per-instance node) is deferred to {@link #knowledgeLayoutBind()}.
     *
     * @param preferencesFactory the factory that provisions the card's preferences node
     * @param toolAreaFactory    the discovered tool-area factory to host
     * @param journalTopic       the journal topic for event coordination
     * @return the created card shell (realized later, at bind)
     */
    public static ToolCard create(KlPreferencesFactory preferencesFactory,
                                  KlToolArea.Factory<?, ?> toolAreaFactory,
                                  UUID journalTopic) {
        ToolCard card = new Factory().create(preferencesFactory);
        card.setToolAreaFactory(toolAreaFactory);
        card.setJournalTopic(journalTopic);
        return card;
    }

    /**
     * Restores a {@code ToolCard} shell from previously stored preferences. The hosted tool is
     * re-instantiated from its per-instance node by a subsequent {@link #realize()}.
     *
     * @param preferences the preferences node backing the card
     * @return the restored card
     */
    public static ToolCard restore(KometPreferences preferences) {
        return new Factory().restore(preferences);
    }

    /**
     * Factory that produces and restores {@link ToolCard} instances.
     */
    public static final class Factory implements CardBlueprint.Factory<ToolCard> {

        @Override
        public ToolCard restore(KometPreferences preferences) {
            return new ToolCard(preferences);
        }

        @Override
        public ToolCard create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ToolCard card = new ToolCard(preferencesFactory, this);
            card.setAreaLayout(areaGridSettings.with(this.getClass()));
            return card;
        }
    }
}
