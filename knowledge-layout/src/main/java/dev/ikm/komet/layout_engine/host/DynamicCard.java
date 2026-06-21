package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewWithOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout.controls.ViewOptionsPopupHelper;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.ProxyFactory;
import dev.ikm.tinkar.terms.State;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;

/**
 * A {@code DynamicCard} is the {@link dev.ikm.komet.layout.KlCard} whose content is <em>dynamic</em>:
 * its composition is materialized at runtime from a layout authored in the layout editor (an
 * {@link EditorWindowModel}), bound to a reference component and a view coordinate, and re-rendered
 * reactively when that coordinate changes — in contrast to code-composed cards.
 *
 * <p>It realizes a designed layout using only the Knowledge Layout engine: each editor section becomes
 * a {@link TitledPane} over a {@link GridPane}; patterns render through the section's
 * {@link KlPatternSemanticsFactory}; placed supplemental areas materialize via
 * {@link SupplementalAreaRenderer}.
 *
 * <p>The card chrome, the engine-native drag, the per-card coordinate context, the bind/unbind
 * lifecycle, and the framework save/restore plumbing all come from {@link AbstractHostCard}; this class
 * supplies only the <em>content</em> (the realized editor layout) and the editor toolbar (the overridable
 * View coordinate control wired to {@link #getCardViewProperties()} plus the Publish action).
 *
 * <p>This class is the non-component-focused dynamic card and the base for specializations such as
 * {@code DynamicComponentCard}. Subclasses enrich the header via {@link #contributeToHeader} and refresh
 * dynamic header content via {@link #refreshHeader()}.
 */
public class DynamicCard extends AbstractHostCard {

    /** The component the realized layout is about; may be {@code null} for a reference-less layout. */
    private EntityFacade referenceComponent;
    /** Preferences node of the editor-designed layout (the {@link EditorWindowModel}) to realize. */
    private KometPreferences editorWindowPreferences;
    /** Optional callback fired when the card is re-focused on a new component (e.g. via a drop). */
    private Consumer<EntityFacade> onComponentFocused;

    /** Commits this card's edit transaction; disabled unless there are uncommitted changes. */
    private final Button publishButton = new Button("Publish");
    /** Lazily created editing composer required by {@link KlPatternSemanticsFactory#createJournalControl}. */
    private ObservableComposer composer;
    /** The vertical stack of section panes; the card's children grid (one growing column). */
    private final GridPane sectionStack = gridPaneForChildren();

    // Preference keys for the card's own framework-persisted content identity (2a save/restore).
    private static final String REFERENCE_COMPONENT_KEY = "dynamicCard.referenceComponent";
    private static final String EDITOR_LAYOUT_TITLE_KEY = "dynamicCard.editorLayoutTitle";

    protected DynamicCard(KometPreferences preferences) {
        super(preferences);
        initContent();
    }

    protected DynamicCard(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
        initContent();
    }

    /**
     * Installs the scrollable, single-growing-column content stack as the card body content.
     */
    private void initContent() {
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        column.setFillWidth(true);
        sectionStack.getColumnConstraints().setAll(column);
        ScrollPane scroller = new ScrollPane(sectionStack);
        scroller.setFitToWidth(true);
        setCardContent(scroller);
    }

    /*******************************************************************************
     *  Injection                                                                  *
     ******************************************************************************/

    /**
     * Sets the component the realized layout is about.
     *
     * @param referenceComponent the reference component, or {@code null} for a reference-less layout
     */
    public void setReferenceComponent(EntityFacade referenceComponent) {
        this.referenceComponent = referenceComponent;
    }

    /**
     * Sets the preferences node of the editor-designed layout to realize.
     *
     * @param editorWindowPreferences the editor window preferences node
     */
    public void setEditorWindowPreferences(KometPreferences editorWindowPreferences) {
        this.editorWindowPreferences = editorWindowPreferences;
    }

    /**
     * Sets a callback notified when this card is re-focused on a new component (e.g. via a drop on the
     * identity area). The host window typically uses it to update and persist its reference component.
     *
     * @param onComponentFocused the callback, or {@code null}
     */
    public void setOnComponentFocused(Consumer<EntityFacade> onComponentFocused) {
        this.onComponentFocused = onComponentFocused;
    }

    /**
     * Re-focuses this card on the given component: it becomes the reference component, the realized
     * layout and header re-render for it, and any {@link #setOnComponentFocused} callback fires (so the
     * host can persist the new focus). No-op-safe before the card is bound.
     *
     * @param component the component to focus
     */
    public void focusComponent(EntityFacade component) {
        setReferenceComponent(component);
        if (isRealized()) {
            refresh();
        }
        if (onComponentFocused != null) {
            onComponentFocused.accept(component);
        }
    }

    /*******************************************************************************
     *  Content + chrome                                                           *
     ******************************************************************************/

    @Override
    protected String cardTitle() {
        return editorWindowPreferences != null ? editorWindowPreferences.name() : "Dynamic card";
    }

    /**
     * Adds the editor card's toolbar controls: the coordinate crosshair (the overridable View control,
     * wired to this card's {@link #getCardViewProperties()}) and the Publish action.
     */
    @Override
    protected void buildToolbarControls(HBox toolBar) {
        MenuButton coordinateButton = new MenuButton();
        coordinateButton.getStyleClass().add("coordinate");
        coordinateButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        coordinateButton.setTooltip(new Tooltip("Coordinates"));
        ViewOptionsPopupHelper.setupViewCoordinateOptionsPopup(getCardViewProperties(),
                FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, fxObject(), coordinateButton, () -> {});

        // Drive the crosshair's orange "overridden" state directly from this card's coordinate — its resolved
        // value differing from the inherited (parent) baseline — rather than the popup's own detection, which
        // does not reflect a restored override. getValue()/getOriginalValue() compose every dimension, so this
        // also catches nested pins (e.g. a language description-type order override).
        ObservableView cardView = getCardViewProperties().nodeView();
        Runnable syncOverrideIndicator = () -> {
            boolean overridden = cardView instanceof ObservableViewWithOverride overrideView
                    && !overrideView.getValue().equals(overrideView.getOriginalValue());
            coordinateButton.getStyleClass().remove("override");
            if (overridden) {
                coordinateButton.getStyleClass().add("override");
            }
        };
        syncOverrideIndicator.run();
        cardView.subscribe(syncOverrideIndicator);

        publishButton.getStyleClass().add("dynamic-card-publish-button");
        publishButton.setOnAction(event -> commitEdits());
        publishButton.setDisable(true);

        toolBar.getChildren().addAll(coordinateButton, publishButton);
    }

    /**
     * Loads the {@link EditorWindowModel} and (re)builds every section into the content stack.
     */
    @Override
    protected void renderContent() {
        Objects.requireNonNull(editorWindowPreferences, "editorWindowPreferences must be set before realize()");
        clearContent();
        final ViewProperties cardViewProperties = getCardViewProperties();
        final ViewCalculator viewCalculator = cardViewProperties.calculator();
        final String title = editorWindowPreferences.name();
        final EditorWindowModel model =
                EditorWindowManager.loadWindowModel(editorWindowPreferences, viewCalculator, title);

        int row = 0;
        row = addSection(model.getMainSection(), row);
        for (EditorSectionModel section : model.getAdditionalSections()) {
            row = addSection(section, row);
        }
        updatePublishState();
    }

    /**
     * Removes all realized content so the card can be rebuilt on a coordinate change.
     */
    @Override
    protected void clearContent() {
        sectionStack.getChildren().clear();
        composer = null;
    }

    /**
     * Commits this card's edit transaction (the lazily-created pattern-editing composer), then
     * re-realizes so committed semantics reload. No-op when there is no active composer.
     */
    private void commitEdits() {
        if (composer != null && composer.hasUncommittedChanges()) {
            composer.commit();
            refresh();
        }
    }

    /**
     * Reflects the current edit transaction in the Publish button: bound to the composer's
     * uncommitted-changes state when a composer exists, otherwise disabled.
     */
    protected void updatePublishState() {
        publishButton.disableProperty().unbind();
        if (composer != null) {
            publishButton.disableProperty().bind(composer.hasUncommittedChangesProperty().not());
        } else {
            publishButton.setDisable(true);
        }
    }

    /**
     * Realizes one editor section as a titled pane over a grid and adds it to the stack.
     *
     * @param section the section model
     * @param row     the row in the section stack to place this section
     * @return the next free row
     */
    private int addSection(EditorSectionModel section, int row) {
        final ViewProperties cardViewProperties = getCardViewProperties();
        GridPane sectionGrid = new GridPane();
        applyColumns(sectionGrid, Math.max(1, section.numberColumnsProperty().get()));

        renderPatterns(section, sectionGrid);
        SupplementalAreaRenderer.renderInto(section, sectionGrid, cardViewProperties, referenceComponent);

        TitledPane sectionPane = new TitledPane();
        sectionPane.textProperty().bind(section.nameProperty());
        sectionPane.setExpanded(!section.isStartCollapsed());
        sectionPane.setContent(sectionGrid);
        sectionPane.setMaxWidth(Double.MAX_VALUE);

        GridPane.setColumnIndex(sectionPane, 0);
        GridPane.setRowIndex(sectionPane, row);
        GridPane.setHgrow(sectionPane, Priority.ALWAYS);
        sectionStack.getChildren().add(sectionPane);
        return row + 1;
    }

    /**
     * Configures a section grid with {@code columns} equal-width growing columns.
     *
     * @param grid    the section grid
     * @param columns the number of columns
     */
    private void applyColumns(GridPane grid, int columns) {
        grid.getColumnConstraints().clear();
        for (int i = 0; i < columns; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHgrow(Priority.ALWAYS);
            constraints.setPercentWidth(100.0 / columns);
            grid.getColumnConstraints().add(constraints);
        }
    }

    /**
     * Renders each pattern in the section using its {@link KlPatternSemanticsFactory} and, when a
     * reference component is present, loads that component's semantics for the pattern read-only.
     *
     * @param section     the section model
     * @param sectionGrid the grid to place pattern views into
     */
    private void renderPatterns(EditorSectionModel section, GridPane sectionGrid) {
        final ViewProperties cardViewProperties = getCardViewProperties();
        List<EditorPatternModel> patterns = section.getPatterns();
        if (patterns == null || patterns.isEmpty()) {
            // A section may legitimately hold only supplemental areas and no patterns.
            return;
        }
        for (EditorPatternModel pattern : patterns) {
            KlPatternSemanticsFactory factory = pattern.getFactory();
            PatternSemanticsPresenter presenter =
                    factory.createJournalControl(pattern, cardViewProperties, composer(), journalTopic());

            if (referenceComponent != null) {
                EntityService.get().forEachSemanticForComponentOfPattern(
                        referenceComponent.nid(), pattern.getNid(),
                        (SemanticEntity<SemanticEntityVersion> semantic) -> presenter.addNewSemantic(semantic));
            }

            Node view = presenter.getView();
            GridPane.setRowIndex(view, pattern.getRowIndex());
            GridPane.setColumnIndex(view, pattern.getColumnIndex());
            GridPane.setColumnSpan(view, Math.max(1, pattern.getColumnSpan()));
            GridPane.setVgrow(view, Priority.ALWAYS);
            sectionGrid.getChildren().add(view);
        }
    }

    /**
     * Lazily creates the editing composer required to construct pattern presenters, seeded from the
     * card coordinate's edit coordinate.
     *
     * @return the composer for this realization pass
     */
    protected ObservableComposer composer() {
        if (composer == null) {
            final ViewProperties cardViewProperties = getCardViewProperties();
            ConceptFacade author = cardViewProperties.nodeView().editCoordinate().getAuthorForChanges();
            ConceptFacade module = cardViewProperties.nodeView().editCoordinate().getDefaultModule();
            ConceptFacade path = cardViewProperties.nodeView().editCoordinate().getDefaultPath();
            composer = ObservableComposer.create(cardViewProperties.calculator(), State.ACTIVE,
                    author, module, path, "DynamicCard");
        }
        return composer;
    }

    /**
     * Returns the component the realized layout is about, or {@code null}.
     *
     * @return the reference component
     */
    public EntityFacade getReferenceComponent() {
        return referenceComponent;
    }

    /**
     * Indicates whether this card has its content identity — the editor-designed layout to realize. A
     * host window checks this after a framework {@code revert()} to decide whether a migration fallback
     * (seeding from the legacy window-state copy) is needed for an empty card node.
     *
     * @return {@code true} if the card's editor layout is set
     */
    public boolean isContentRestored() {
        return editorWindowPreferences != null;
    }

    /*******************************************************************************
     *  Framework save / restore                                                   *
     ******************************************************************************/

    @Override
    protected void subCardSave() {
        // Persist the card's content identity to its own preferences node (the framework save path), so
        // the card carries its component + designed layout itself rather than via the kview window-state
        // shadow copy. This is what makes the inherited save()/changed/revert machinery actually live.
        if (referenceComponent != null) {
            preferences().put(REFERENCE_COMPONENT_KEY, referenceComponent.toXmlFragment());
        }
        if (editorWindowPreferences != null) {
            preferences().put(EDITOR_LAYOUT_TITLE_KEY, editorWindowPreferences.name());
        }
    }

    @Override
    protected void subCardRestore() {
        // Load the card's content identity from its own preferences node, when present. No-clobber: an
        // absent key leaves any already-injected value intact — safe during the migration off the kview
        // window-state copy and before the restore path is fully switched onto the framework.
        preferences().get(REFERENCE_COMPONENT_KEY).ifPresent(xml ->
                this.referenceComponent = ProxyFactory.fromXmlFragment(xml));
        preferences().get(EDITOR_LAYOUT_TITLE_KEY).ifPresent(title ->
                this.editorWindowPreferences = KometPreferencesImpl.getConfigurationRootPreferences()
                        .node(KL_EDITOR_APP).node(title));
    }

    /*******************************************************************************
     *  Factory                                                                    *
     ******************************************************************************/

    /**
     * Returns the factory for {@code DynamicCard} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores a {@code DynamicCard} shell from previously stored preferences. The realized content
     * is rebuilt by a subsequent {@link #realize()} once the host view, editor layout, and reference
     * component are re-injected.
     *
     * @param preferences the preferences node backing the card
     * @return the restored card
     */
    public static DynamicCard restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Creates a {@code DynamicCard} shell with its inputs injected. Realization (coordinate context +
     * header + content) is intentionally <em>deferred</em> to {@link #knowledgeLayoutBind()}, which the
     * host window invokes once the card is in the composed scene graph — the card must not bind its
     * context at construction.
     *
     * @param preferencesFactory      the factory that provisions the card's preferences node
     * @param editorWindowPreferences the preferences node of the editor-designed layout to realize
     * @param referenceComponent      the component the layout is about, or {@code null}
     * @param journalTopic            the journal topic for presenter event coordination
     * @return the created card shell (realized later, at bind)
     */
    public static DynamicCard create(KlPreferencesFactory preferencesFactory,
                                     KometPreferences editorWindowPreferences,
                                     EntityFacade referenceComponent,
                                     UUID journalTopic) {
        DynamicCard card = factory().create(preferencesFactory);
        card.setEditorWindowPreferences(editorWindowPreferences);
        card.setReferenceComponent(referenceComponent);
        card.setJournalTopic(journalTopic);
        return card;
    }

    /**
     * Creates a new {@code DynamicCard} shell with the supplied grid settings.
     *
     * @param preferencesFactory the factory that provisions the card's preferences node
     * @param areaGridSettings   the grid placement settings for the card
     * @return the created card shell
     */
    public static DynamicCard create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    /**
     * Creates a new {@code DynamicCard} shell with default grid settings.
     *
     * @param preferencesFactory the factory that provisions the card's preferences node
     * @return the created card shell
     */
    public static DynamicCard create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    /**
     * Factory that produces and restores {@link DynamicCard} instances.
     */
    public static final class Factory implements CardBlueprint.Factory<DynamicCard> {

        @Override
        public DynamicCard restore(KometPreferences preferences) {
            return new DynamicCard(preferences);
        }

        @Override
        public DynamicCard create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            DynamicCard card = new DynamicCard(preferencesFactory, this);
            card.setAreaLayout(areaGridSettings.with(this.getClass()));
            return card;
        }
    }
}
