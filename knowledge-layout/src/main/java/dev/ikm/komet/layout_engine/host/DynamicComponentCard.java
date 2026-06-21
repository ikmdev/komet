package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.dnd.ClipboardHelper;
import dev.ikm.komet.framework.dnd.DragAndDropHelper;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.ProxyFactory;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A {@link DynamicCard} specialized to <em>view a component</em>, presented with the classic Komet
 * concept-window chrome — kview-free. Its header carries:
 * <ul>
 *   <li>the component's LifeHash {@link Identicon} and its <em>view-generated</em> name (large title);</li>
 *   <li>the Komet id (public id) and any external identifiers (e.g. SCTID), resolved through the
 *       identifier pattern under the card's coordinate;</li>
 *   <li>the latest committed STAMP — status, last updated, author, module, path.</li>
 * </ul>
 * over the realized layout body, with the inherited functional toolbar (View coordinate control +
 * Publish). Every label re-resolves on coordinate change via {@link #refreshHeader()}.
 *
 * <p>The identifier and STAMP reads are delegated to the coordinate-bound
 * {@link dev.ikm.komet.framework.observable.read.SnapshotReads} on the component's
 * {@code ObservableEntitySnapshot} (the snapshot carries the coordinate, so the reads take no
 * argument). The card therefore carries no kview dependency and no read logic of its own.
 */
public final class DynamicComponentCard extends DynamicCard {

    private final ImageView identiconView = new ImageView();
    private final Label titleLabel = new Label();
    private final VBox identifiersBox = new VBox();
    /** STAMP block (right of the identity); hidden when there is no concept in focus. */
    private VBox stampBlock;

    private final Label statusLabel = new Label();
    private final Label lastUpdatedLabel = new Label();
    private final Label authorLabel = new Label();
    private final Label moduleLabel = new Label();
    private final Label pathLabel = new Label();

    /** Most-recent-first component-focus history, capped at {@link #MAX_HISTORY}, persisted in prefs. */
    private static final int MAX_HISTORY = 15;
    private static final String HISTORY_KEY = "dynamicCard.componentHistory";
    private final List<EntityFacade> componentHistory = new ArrayList<>();

    {
        // A right-side "Properties" drawer with placeholder content (the editing area replaces it
        // incrementally). Added at construction so it is present before the card realizes; its toggle is
        // contributed to the toolbar when the header builds, and its open state persists with the card.
        addDrawer(Side.RIGHT, buildPropertiesPlaceholder(), "Properties");
    }

    private DynamicComponentCard(KometPreferences preferences) {
        super(preferences);
    }

    private DynamicComponentCard(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    @Override
    protected void contributeToHeader(VBox headerBox, HBox toolBar) {
        identiconView.setFitWidth(40);
        identiconView.setFitHeight(40);
        identiconView.setPreserveRatio(true);
        // Right-click the identicon for this card's component-focus history (revert to an earlier item).
        identiconView.setOnContextMenuRequested(this::showHistoryMenu);
        titleLabel.getStyleClass().add("dynamic-component-card-title");
        // Ellipsize (don't wrap) when the title is wider than the area; ELLIPSIS is the default overrun.
        titleLabel.setWrapText(false);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // The identity row (identicon + name) is both a drag SOURCE for the reference component (dragged
        // as a unit with a combined identicon+text drag image, like the axiom-tree entity rows) and a drop
        // TARGET: dropping a component dragged from the navigator re-focuses the card on it.
        // A fixed, bordered slot so the identicon area stays visible even when empty — it is the placeholder
        // you drag a concept onto. When focused, the identicon fills it.
        StackPane identiconSlot = new StackPane(identiconView);
        identiconSlot.setMinSize(40, 40);
        identiconSlot.setPrefSize(40, 40);
        identiconSlot.getStyleClass().add("dynamic-component-card-identicon-slot");
        HBox titleRow = new HBox(8, identiconSlot, titleLabel);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setCursor(Cursor.OPEN_HAND);
        new DragAndDropHelper(titleRow,
                this::getReferenceComponent,                        // drag this component out
                this::acceptComponentDrop,                          // drop a component in → re-focus the card
                dragEvent -> getReferenceComponent() != null,
                this::canAcceptComponentDrop);

        identifiersBox.getStyleClass().add("dynamic-component-card-ids");

        VBox identityLeft = new VBox(4, titleRow, identifiersBox);

        this.stampBlock = new VBox(
                stampRow("Status:", statusLabel),
                stampRow("Last Updated:", lastUpdatedLabel),
                stampRow("Author:", authorLabel),
                stampRow("Module:", moduleLabel),
                stampRow("Path:", pathLabel));
        stampBlock.getStyleClass().add("dynamic-component-card-stamp");

        // The identity column grows (so the title has a bound to ellipsize against); the STAMP block
        // keeps its preferred width on the right.
        HBox identityRow = new HBox(identityLeft, stampBlock);
        HBox.setHgrow(identityLeft, Priority.ALWAYS);
        identityRow.getStyleClass().add("dynamic-component-card-identity");
        headerBox.getChildren().add(identityRow);
    }

    /**
     * Builds the placeholder content for the right "Properties" drawer. The editing controls arrive
     * incrementally; for now this is a simple labeled panel so the slide-out is visible and usable.
     *
     * @return the placeholder content region
     */
    private Region buildPropertiesPlaceholder() {
        Label heading = new Label("Properties");
        heading.getStyleClass().add("dynamic-component-card-properties-heading");
        Label note = new Label("Editing controls will appear here.");
        note.setWrapText(true);
        VBox panel = new VBox(8, heading, note);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(320);
        panel.getStyleClass().add("dynamic-component-card-properties");
        // A framed surface so the drawer reads as a distinct panel beside the card content.
        panel.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 1; "
                + "-fx-background-radius: 4; -fx-border-radius: 4;");
        return panel;
    }

    /**
     * Builds a STAMP row: a bold field label (e.g. {@code "Status:"}) followed by its value label,
     * matching the classic detail-window STAMP presentation.
     *
     * @param fieldName  the bold field label text
     * @param valueLabel the value label, populated in {@link #refreshHeader()}
     * @return the assembled row
     */
    private HBox stampRow(String fieldName, Label valueLabel) {
        Label fieldLabel = new Label(fieldName);
        fieldLabel.getStyleClass().add("dynamic-component-card-stamp-label");
        return new HBox(4, fieldLabel, valueLabel);
    }

    /**
     * Builds a copyable identifier row: the identifier text plus a copy-to-clipboard control that is
     * revealed on hover, with a tooltip showing the full text — mirroring the classic public-id control
     * but kview-free (Komet framework {@link ClipboardHelper}).
     *
     * @param displayText the text shown (e.g. {@code "Komet ID: …"})
     * @param copyValue   the value placed on the clipboard when the copy control is activated
     * @return the assembled row
     */
    private HBox copyableIdRow(String displayText, String copyValue) {
        Label label = new Label(displayText);
        label.getStyleClass().add("dynamic-component-card-id");
        Tooltip.install(label, new Tooltip(displayText));

        Region copyIcon = new Region();
        copyIcon.getStyleClass().add("dynamic-card-copy-icon");
        Button copyButton = new Button();
        copyButton.setGraphic(copyIcon);
        copyButton.getStyleClass().add("dynamic-card-copy-button");
        copyButton.setOnAction(event -> ClipboardHelper.copyToClipboard(copyValue));
        Tooltip.install(copyButton, new Tooltip("Copy to clipboard"));

        HBox row = new HBox(6, label, copyButton);
        row.setAlignment(Pos.CENTER_LEFT);
        // Reveal the copy control only while hovering the row; it still reserves space, so no layout shift.
        copyButton.visibleProperty().bind(row.hoverProperty());
        return row;
    }

    /** Whether a drag carries a droppable component (concept, semantic, or pattern proxy). */
    private boolean canAcceptComponentDrop(DragEvent event) {
        Set<DataFormat> types = event.getDragboard().getContentTypes();
        return KometClipboard.containsAny(types, KometClipboard.CONCEPT_TYPES)
                || KometClipboard.containsAny(types, KometClipboard.SEMANTIC_TYPES)
                || KometClipboard.containsAny(types, KometClipboard.PATTERN_TYPES);
    }

    /** Re-focuses this card on the component carried by a drop (e.g. dragged from the navigator). */
    private void acceptComponentDrop(Dragboard dragboard) {
        EntityFacade dropped = entityFromDragboard(dragboard);
        if (dropped != null) {
            focusComponent(dropped);
        }
    }

    /** Extracts the dropped component proxy from a Komet dragboard, or {@code null} if none. */
    private EntityFacade entityFromDragboard(Dragboard dragboard) {
        if (dragboard.hasContent(KometClipboard.KOMET_CONCEPT_PROXY)) {
            return ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_CONCEPT_PROXY));
        } else if (dragboard.hasContent(KometClipboard.KOMET_SEMANTIC_PROXY)) {
            return ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_SEMANTIC_PROXY));
        } else if (dragboard.hasContent(KometClipboard.KOMET_PATTERN_PROXY)) {
            return ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_PATTERN_PROXY));
        }
        return null;
    }

    @Override
    protected void refreshHeader() {
        final EntityFacade component = getReferenceComponent();
        final ViewProperties viewProperties = getCardViewProperties();
        if (component == null || viewProperties == null) {
            // Keep the identicon slot + name row visible as the drop target; only their values are blank, and
            // the STAMP block is hidden, so an empty card clearly shows where to drop a concept.
            titleLabel.setText("No concept in focus");
            identiconView.setImage(null);
            identifiersBox.getChildren().clear();
            clearStamp();
            stampBlock.setVisible(false);
            stampBlock.setManaged(false);
            return;
        }
        stampBlock.setVisible(true);
        stampBlock.setManaged(true);
        final ViewCalculator calculator = viewProperties.calculator();

        titleLabel.setText(calculator.getDescriptionTextOrNid(component.nid()));
        recordHistory(component);
        identifiersBox.getChildren().clear();
        if (component.publicId() != null) {
            identiconView.setImage(Identicon.generateIdenticonImage(component.publicId()));
            ImmutableList<UUID> uuids = component.publicId().asUuidList();
            if (!uuids.isEmpty()) {
                UUID kometId = uuids.getFirst();
                identifiersBox.getChildren().add(copyableIdRow("Komet ID: " + kometId, kometId.toString()));
            }
        }

        final ObservableEntitySnapshot<?, ?> snapshot;
        try {
            snapshot = ObservableEntityHandle.get(component.nid()).expectEntity().getSnapshot(calculator);
        } catch (RuntimeException noVersionUnderCoordinate) {
            // No version of the component passes the current view coordinate — leave external ids/STAMP blank.
            clearStamp();
            return;
        }
        for (String identifier : snapshot.externalIdentifiers()) {
            // externalIdentifiers() returns "Source: value"; copy just the value (e.g. the SCTID number).
            String copyValue = identifier.contains(": ")
                    ? identifier.substring(identifier.indexOf(": ") + 2)
                    : identifier;
            identifiersBox.getChildren().add(copyableIdRow(identifier, copyValue));
        }

        final StampEntity<?> stamp = snapshot.latestCommittedStamp();
        if (stamp == null) {
            clearStamp();
            return;
        }
        final State state = stamp.state();
        statusLabel.setText(state == null ? "" : calculator.getPreferredDescriptionTextWithFallbackOrNid(state.nid()));
        lastUpdatedLabel.setText(TimeUtils.toShortDateString(stamp.time()));
        final ConceptFacade author = stamp.author();
        authorLabel.setText(author == null ? "" : calculator.getPreferredDescriptionTextWithFallbackOrNid(author.nid()));
        final ConceptFacade module = stamp.module();
        moduleLabel.setText(module == null ? "" : calculator.getPreferredDescriptionTextWithFallbackOrNid(module.nid()));
        final ConceptFacade path = stamp.path();
        pathLabel.setText(path == null ? "" : calculator.getPreferredDescriptionTextWithFallbackOrNid(path.nid()));
    }

    private void clearStamp() {
        statusLabel.setText("");
        lastUpdatedLabel.setText("");
        authorLabel.setText("");
        moduleLabel.setText("");
        pathLabel.setText("");
    }

    /** Records the focused component at the front of the history (dedup), capped at {@link #MAX_HISTORY}. */
    private void recordHistory(EntityFacade component) {
        if (component == null) {
            return;
        }
        componentHistory.removeIf(existing -> existing.nid() == component.nid());
        componentHistory.addFirst(component);
        while (componentHistory.size() > MAX_HISTORY) {
            componentHistory.removeLast();
        }
    }

    /** Shows the component-focus history as a context menu; selecting an item re-focuses the card on it. */
    private void showHistoryMenu(ContextMenuEvent event) {
        final ContextMenu menu = new ContextMenu();
        final ViewProperties viewProperties = getCardViewProperties();
        final ViewCalculator calculator = viewProperties != null ? viewProperties.calculator() : null;
        for (EntityFacade item : componentHistory) {
            final String name = calculator != null
                    ? calculator.getDescriptionTextOrNid(item.nid()) : String.valueOf(item.nid());
            final MenuItem menuItem = new MenuItem(name);
            if (item.publicId() != null) {
                final ImageView icon = new ImageView(Identicon.generateIdenticonImage(item.publicId()));
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);
                menuItem.setGraphic(icon);
            }
            menuItem.setOnAction(action -> focusComponent(item));
            menu.getItems().add(menuItem);
        }
        if (menu.getItems().isEmpty()) {
            final MenuItem empty = new MenuItem("No history");
            empty.setDisable(true);
            menu.getItems().add(empty);
        }
        menu.show(identiconView, event.getScreenX(), event.getScreenY());
    }

    @Override
    protected void subCardSave() {
        super.subCardSave();
        // Persist the component-focus history by public id (durable across datastores), not nids.
        preferences().putComponentList(HISTORY_KEY, componentHistory);
    }

    @Override
    protected void subCardRestore() {
        super.subCardRestore();
        componentHistory.clear();
        componentHistory.addAll(preferences().getEntityList(HISTORY_KEY, new EntityFacade[0]));
    }

    /**
     * Restores a {@code DynamicComponentCard} shell from previously stored preferences.
     *
     * @param preferences the preferences node backing the card
     * @return the restored card
     */
    public static DynamicComponentCard restore(KometPreferences preferences) {
        return new Factory().restore(preferences);
    }

    /**
     * Creates a {@code DynamicComponentCard} shell for the given component with its inputs injected.
     * Realization is deferred to {@link #knowledgeLayoutBind()} (invoked by the host window after the
     * card joins the composed scene graph), so the card never binds its context at construction.
     *
     * @param preferencesFactory      the factory that provisions the card's preferences node
     * @param editorWindowPreferences the preferences node of the editor-designed layout to realize
     * @param referenceComponent      the component this card views
     * @param journalTopic            the journal topic for presenter event coordination
     * @return the created card shell (realized later, at bind)
     */
    public static DynamicComponentCard create(KlPreferencesFactory preferencesFactory,
                                              KometPreferences editorWindowPreferences,
                                              EntityFacade referenceComponent,
                                              UUID journalTopic) {
        DynamicComponentCard card = new Factory().create(preferencesFactory);
        card.setEditorWindowPreferences(editorWindowPreferences);
        card.setReferenceComponent(referenceComponent);
        card.setJournalTopic(journalTopic);
        return card;
    }

    /**
     * Factory that produces and restores {@link DynamicComponentCard} instances.
     */
    public static final class Factory implements dev.ikm.komet.layout_engine.blueprint.CardBlueprint.Factory<DynamicComponentCard> {

        @Override
        public DynamicComponentCard restore(KometPreferences preferences) {
            return new DynamicComponentCard(preferences);
        }

        @Override
        public DynamicComponentCard create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            DynamicComponentCard card = new DynamicComponentCard(preferencesFactory, this);
            card.setAreaLayout(areaGridSettings.with(this.getClass()));
            return card;
        }
    }
}
