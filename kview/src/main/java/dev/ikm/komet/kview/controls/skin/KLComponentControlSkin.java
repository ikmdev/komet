package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import dev.ikm.komet.kview.controls.ConceptTile;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Default skin implementation for the {@link KLComponentControl} control
 */
public class KLComponentControlSkin extends SkinBase<KLComponentControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentControl.class);
    private static final String SEARCH_TEXT_VALUE = "search.text.value";
    public static final DataFormat COMPONENT_CONTROL_DRAG_FORMAT;

    static {
        DataFormat dataFormat = DataFormat.lookupMimeType("text/concept-control-format");
        COMPONENT_CONTROL_DRAG_FORMAT = dataFormat == null ? new DataFormat("text/concept-control-format") : dataFormat;
    }

    private final Label titleLabel;
    private final StackPane selectedConceptContainer;
    private final StackPane conceptContainer;
    private final HBox aboutToDropHBox;
    private final HBox aboutToRearrangeHBox;
    private final BorderPane doNotDropBorderPane;
    private final StackPane dragHandleIconContainer;

    private AutoCompleteTextField<EntityProxy> typeAheadSearchField;

    /**
     * Creates a new KLComponentControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLComponentControlSkin(KLComponentControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("editable-title-label");
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.managedProperty().bind(titleLabel.visibleProperty());
        titleLabel.visibleProperty().bind(titleLabel.textProperty().isNotEmpty());

        selectedConceptContainer = new StackPane();
        selectedConceptContainer.getStyleClass().add("selected-concept-container");
        selectedConceptContainer.managedProperty().bind(selectedConceptContainer.visibleProperty());

        aboutToDropHBox = createDragOverAnimation();
        doNotDropBorderPane = createDoNotDropDragOverAnimation();
        aboutToRearrangeHBox = createDragOverAnimation();

        conceptContainer = new StackPane(createSearchBox(), aboutToDropHBox , doNotDropBorderPane);
        conceptContainer.getStyleClass().add("concept-container");
        conceptContainer.managedProperty().bind(conceptContainer.visibleProperty());
        selectedConceptContainer.visibleProperty().bind(conceptContainer.visibleProperty().not());

        dragHandleIconContainer = new StackPane();
        dragHandleIconContainer.getStyleClass().add("drag-handle-icon-container");
        dragHandleIconContainer.visibleProperty().bind(control.showDragHandleProperty());
        dragHandleIconContainer.managedProperty().bind(control.managedProperty());

        getChildren().addAll(titleLabel, selectedConceptContainer, conceptContainer, aboutToRearrangeHBox, dragHandleIconContainer);

        setupDragNDrop();

        registerChangeListener(getSkinnable().entityProperty(), entity -> {
            if (control.isEmpty()) {
               selectedConceptContainer.getChildren().clear();
               conceptContainer.setVisible(true);

               typeAheadSearchField.clear();
            }
        });

        control.entityProperty().addListener((observable, oldValue, newValue) -> {
            if (!control.isEmpty()) {
                addConceptNode(control.getEntity(), control.getComponentNameRenderer());
            }
        });
        if (!control.isEmpty()) {
            addConceptNode(control.getEntity(), control.getComponentNameRenderer());
        }

    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        unregisterChangeListeners(getSkinnable().entityProperty());
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double labelPrefWidth = titleLabel.isManaged() ? titleLabel.prefWidth(-1) : 0;
        double labelPrefHeight = titleLabel.isManaged() ? titleLabel.prefHeight(labelPrefWidth) : 0;
        Insets padding = getSkinnable().getPadding();
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;
        selectedConceptContainer.resizeRelocate(x, y, contentWidth - padding.getRight() - x, contentHeight - padding.getBottom() - y);
        conceptContainer.resizeRelocate(x, y, contentWidth - padding.getRight() - x, contentHeight - padding.getBottom() - y);
        aboutToRearrangeHBox.resizeRelocate(x, y, contentWidth - padding.getRight() - x, contentHeight - padding.getBottom() - y);
    }

    /**
     * There are two type of DND operations:
     * - Drop a concept over an empty KLComponentControl (dragboard string is publicId)
     * - Rearrange non-empty KLComponentControls that belong to a KLComponentSetControl or a
     *   KLComponentListControl (dragboard string is CONTROL_DRAG_KEY)
     */
    private void setupDragNDrop() {
        KLComponentControl control = getSkinnable();
        control.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(COMPONENT_CONTROL_DRAG_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            } else if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                if (isFilterAllowedWhileDragAndDropping(event)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                } else {
                    event.acceptTransferModes(TransferMode.NONE);
                }
            }

            event.consume();
        });

        control.setOnDragEntered(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                conceptContainer.setOpacity(.90);
                if (event.getDragboard().hasContent(COMPONENT_CONTROL_DRAG_FORMAT)) {
                    if (hasAllowedDND(control)) {
                        aboutToRearrangeHBox.setVisible(true);
                    }
                } else if (!isFilterAllowedWhileDragAndDropping(event)) {
                    doNotDropBorderPane.setVisible(true);  // show error message.
                } else {
                    aboutToDropHBox.setVisible(true);
                }
            }
            event.consume();
        });

        control.setOnDragExited(event -> {
            conceptContainer.setOpacity(1);
            aboutToRearrangeHBox.setVisible(false);
            aboutToDropHBox.setVisible(false);
            doNotDropBorderPane.setVisible(false);
            event.consume();
        });

        control.setOnDragDetected(ev -> {
            if (hasAllowedDND(control)) {
                Dragboard dragboard = control.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(COMPONENT_CONTROL_DRAG_FORMAT, "concept-control");
                control.setUserData(control.getEntity().publicId());
                clipboardContent.putString(control.getEntity().toString());
                dragboard.setContent(clipboardContent);
                SnapshotParameters p = new SnapshotParameters();
                p.setTransform(new Scale(10, 10));
                WritableImage snapshot = control.snapshot(p, null);
                dragboard.setDragView(scale(snapshot, (int) (snapshot.getWidth() / 10), (int) (snapshot.getHeight() / 10)));
            }
            ev.consume();
        });

        control.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (event.getDragboard().hasContent(COMPONENT_CONTROL_DRAG_FORMAT) &&
                    event.getGestureSource() instanceof KLComponentControl cc && haveAllowedDND(control, cc)) {
                // reorder components
                if (control.getParent() instanceof KLComponentSetControl componentSetControl) {
                    KLComponentSetControlSkin skin = (KLComponentSetControlSkin) componentSetControl.getSkin();
                    int sourceIndex = skin.getChildren().indexOf(cc);
                    int targetIndex = skin.getChildren().indexOf(control);
                    final Node node = skin.getChildren().remove(sourceIndex);
                    skin.getChildren().add(targetIndex, node);

                    event.setDropCompleted(true);
                    event.consume();
                }
            } else if (dragboard.hasString() && !(event.getGestureSource() instanceof KLComponentControl)) {
                // drop concept
                if (!isFilterAllowedWhileDragAndDropping(event)) {
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }

                try {
                    int nid = extractNid(event);
                    LOG.info("publicId: {}", dragboard.getString());
                    if (nid != Integer.MIN_VALUE) {  //
                        EntityProxy entity = Entity.getFast(nid).toProxy();
                        if (!(control.getParent() instanceof KLComponentSetControl componentSetControl) ||
                                !componentSetControl.getValue().contains(nid)) {
                            control.setEntity(entity);
                            addConceptNode(entity, control.getComponentNameRenderer());

                            event.setDropCompleted(true);
                            event.consume();
                        }
                    }

                } catch (Exception e) {
                    LOG.error("exception: ", e);
                }
            }
        });
    }


//    TODO: Refactor code below to detect a drag event and to extract a nid (int) from a ui cell or node. The outside
//          caller (user of this control) should be able to supply a dictionary to determine if a user is permitted to
//          drag item into this control. For now the following functions detect if the dnd item is from a source for
//          backwards compatibility.
//          e.g., This control should allow a user to drag a concept from classic concept navigator.

    /**
     * Returns true if the user is allowed to drag and drop an item (source) to this control (destination).
     * @param dragEvent event A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns true if the user is allowed to drag and drop an item (source) to this control (destination).
     */
    private boolean isFilterAllowedWhileDragAndDropping(DragEvent dragEvent) {
        // Detect classic concept navigator to drag and drop concepts
        boolean classicConceptNavigatorDnD = isFromClassicConceptNav(dragEvent); // and the allowed filter returns true

        // Detect classic search to drag and drop components (concepts and semantics)
        boolean classicSearchDnD = isFromClassicSearch(dragEvent);

        // Detect new concept navigator to drag and drop concepts
        boolean nextGenConceptNavigatorDnD = isFromNexGenConceptNav(dragEvent);

        // Existing checks for next gen search navigator items to drag.
        boolean nextGenSearchDnD =  isADragAndDropInfo(dragEvent);

        return classicConceptNavigatorDnD || classicSearchDnD || nextGenConceptNavigatorDnD || nextGenSearchDnD;
    }

    /**
     * Returns true if user dragged item contains a DragAndDropInfo record. Record will contain a concept from
     * the next gen search or pattern/semantic navigator, otherwise false.
     * @param dragEvent A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns true if user dragged a concept from the next gen search or pattern/semantic navigator, otherwise false.
     */
    private boolean isADragAndDropInfo(DragEvent dragEvent) {
        // Existing checks for next gen search navigator and pattern nav items to drag.
        boolean isValidDnD =  dragEvent.getGestureSource() instanceof Node source // and the gesture source is a Node
                && source.getUserData() instanceof DragAndDropInfo dropInfo // whose user data is DragAndDropInfoEntityProxy
                && dropInfo.publicId() != null // with a non-null publicId
                && getSkinnable().getComponentAllowedFilter().test(dropInfo.publicId()); // and the allowed filter returns true
        return isValidDnD;
    }

    /**
     * TODO: Refactor the allow dnd code and the extract nid code to be outside of this component.
     * Returns a nid (int) representing a unique database identifier of a given entity. A component's nid is extracted from
     * the drag event. When return a zero (the entity is invalid).
     * @param dragEvent A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns a nid (int) representing a unique database identifier of a given entity. A component's nid is extracted from
     */
    private int extractNid(DragEvent dragEvent) {
        if (isADragAndDropInfo(dragEvent)) {
            DragAndDropInfo dropInfo = (DragAndDropInfo) ((Node)dragEvent.getGestureSource()).getUserData();
            return EntityService.get().nidForPublicId(dropInfo.publicId());
        }
        if (isFromClassicConceptNav(dragEvent)) {
            TreeCell treeCell = (TreeCell) dragEvent.getGestureSource();  // and the gesture source is a TreeCell Node
            EntityFacade entityFacade = (EntityFacade) treeCell.getTreeItem().getValue();
            return entityFacade.nid();
        }
        if (isFromClassicSearch(dragEvent)) {
            SearchResultCell searchResultCell = (SearchResultCell) dragEvent.getGestureSource();
            Object value = searchResultCell.getTreeItem().getValue();
            if (value instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                return nidTextRecord.nid();
            } else if (value instanceof LatestVersionSearchResult latestVersionSearchResult) {
                return latestVersionSearchResult.latestVersion().get().nid();
            }
        }
        if (isFromNexGenConceptNav(dragEvent)) {
            ConceptTile conceptTile = (ConceptTile) dragEvent.getGestureSource();
            return conceptTile.getConcept().getValue().nid();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Returns true if user dragged a concept from the classic concept navigator, otherwise false.
     * @param dragEvent A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns true if user dragged a concept from the classic concept navigator, otherwise false.
     */
    private boolean isFromClassicConceptNav(DragEvent dragEvent) {
        // Detect classic concept navigator to drag and drop concepts
        boolean isValidDnD = dragEvent.getGestureSource() instanceof TreeCell source // and the gesture source is a TreeCell Node
                && source.getTreeItem().getValue() instanceof EntityFacade conceptFacade // whose tree item is an entity facade
                && getSkinnable().getComponentAllowedFilter().test(conceptFacade.publicId()); // and the allowed filter returns true
        return isValidDnD;
    }

    /**
     * Returns true if user dragged a component from the classic search controller, otherwise false.
     * @param dragEvent A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns true if user dragged a component from the classic search controller, otherwise false.
     */
    private boolean isFromClassicSearch(DragEvent dragEvent) {
        // Detect classic concept navigator to drag and drop concepts
        boolean isValidDnD = dragEvent.getGestureSource() instanceof SearchResultCell source // and the gesture source is a TreeCell Node
                &&
                (source.getTreeItem().getValue() instanceof SearchPanelController.NidTextRecord ||
                        source.getTreeItem().getValue()  instanceof LatestVersionSearchResult); // whose tree item is LatestVersionSearchResult

        return isValidDnD;
    }

    /**
     * Returns true if user dragged a concept from the new concept navigator, otherwise false.
     * @param dragEvent A drag event {@link DragEvent} from a JavaFX {@link Node}.
     * @return Returns true if user dragged a concept from the new concept navigator, otherwise false.
     */
    private boolean isFromNexGenConceptNav(DragEvent dragEvent) {
        boolean isValidDnD = dragEvent.getGestureSource() instanceof ConceptTile source // and the gesture source is a ConceptTile
                && source.getConcept().getValue() instanceof ConceptRecord conceptRecord // whose value is a concept record
                && getSkinnable().getComponentAllowedFilter().test(conceptRecord.publicId());
        return isValidDnD;
    }

    private boolean hasAllowedDND(KLComponentControl control) {
        return control != null && control.getEntity() != null &&
                ((control.getParent() instanceof KLComponentSetControl cs && cs.getValue().size() > 1)
                    ||  (control.getParent() instanceof KLComponentListControl cl && cl.getValue().size() > 1)
                );
    }

    private boolean haveAllowedDND(KLComponentControl source, KLComponentControl target) {
        // only allowed if both source and target have the same parent
        return hasAllowedDND(source) && hasAllowedDND(target) &&
                ((source.getParent() instanceof KLComponentSetControl cs1 && target.getParent() instanceof KLComponentSetControl cs2 && cs1 == cs2));
    }

    private HBox createSearchBox() {
        KLComponentControl control = getSkinnable();

        typeAheadSearchField = new AutoCompleteTextField<>();

        StackPane typeAheadSearchFieldContainer = new StackPane();
        typeAheadSearchFieldContainer.getStyleClass().add("search-field-container");

        typeAheadSearchField.getStyleClass().add("concept-text-field");
        typeAheadSearchField.textProperty().subscribe(text -> getSkinnable().getProperties().put(SEARCH_TEXT_VALUE, text));

        // Type ahead setup
        typeAheadSearchField.valueProperty().subscribe(() -> {
            if (control.getComponentAllowedFilter().test(typeAheadSearchField.getValue().publicId())) {
                control.setEntity(typeAheadSearchField.getValue());
            }
        });
        typeAheadSearchField.completerProperty().bind(getSkinnable().completerProperty());
        typeAheadSearchField.converterProperty().bind(getSkinnable().typeAheadStringConverterProperty());
        typeAheadSearchField.suggestionsNodeFactoryProperty().bind(getSkinnable().suggestionsNodeFactoryProperty());
        typeAheadSearchField.getPopupStyleClasses().add("component-popup");
        typeAheadSearchField.setSuggestionsNodeHeight(41);

        // search prompt
        Label searchPrompt = new Label(getString("textfield.prompt.text"));
        searchPrompt.getStyleClass().add("search-prompt");

        searchPrompt.visibleProperty().bind(
                typeAheadSearchField.focusedProperty().not()
                        .and
                (typeAheadSearchField.textProperty().isEmpty())
        );

        searchPrompt.setMouseTransparent(true);

        typeAheadSearchFieldContainer.getChildren().addAll(typeAheadSearchField, searchPrompt);

        HBox.setHgrow(typeAheadSearchFieldContainer, Priority.ALWAYS);

        Region searchRegion = new Region();
        searchRegion.getStyleClass().add("concept-filter-region");
        Button filterButton = new Button(null, searchRegion);
        filterButton.getStyleClass().add("concept-filter-button");

        HBox searchBox = new HBox(typeAheadSearchFieldContainer, filterButton);
        searchBox.getStyleClass().add("concept-search-box");

        typeAheadSearchField.setSkin(new AutoCompleteTextFieldSkin<>(typeAheadSearchField) {

            @Override
            protected double getPopupWidth() {
                return searchBox.getWidth() - searchBox.getInsets().getLeft() - searchBox.getInsets().getRight() - 1;
            }
        });


        return searchBox;
    }

    private HBox createDragOverAnimation() {
        Region iconRegion = new Region();
        iconRegion.getStyleClass().add("concept-drag-and-drop-icon");
        Label dragAndDropLabel = new Label(getString("textfield.drag.text"), iconRegion);
        HBox aboutToDropHBox = new HBox(dragAndDropLabel);
        aboutToDropHBox.setAlignment(Pos.CENTER);
        aboutToDropHBox.getStyleClass().add("concept-drop-area");
        aboutToDropHBox.managedProperty().bind(aboutToDropHBox.visibleProperty());
        aboutToDropHBox.setVisible(false);
        return aboutToDropHBox;
    }

    /***
     * This method show the error message to let user know tht the value is duplicate.
     * @return hbox
     */
    private BorderPane createDoNotDropDragOverAnimation(){
        // Initialize the borderpane
        BorderPane borderPane = new BorderPane();
        StackPane stackPane = new StackPane();
        //add stackPane to right of borderPane.
        borderPane.setRight(stackPane);

        Region iconRegion = new Region();
        iconRegion.getStyleClass().add("concept-donot-drag-and-drop-icon");
        // add Region/Icon to the stackpane
        stackPane.getChildren().add(iconRegion);

        Label doNotDragAndDropLabel = new Label(getString("textfield.donot.drag.text"));
        doNotDragAndDropLabel.getStyleClass().add("error-msg-label");
        //Add label to the center of borderpane.
        borderPane.setCenter(doNotDragAndDropLabel);

        borderPane.getStyleClass().add("concept-donot-drop-area");
        borderPane.managedProperty().bind(borderPane.visibleProperty());
        borderPane.setVisible(false);
        return borderPane;
    }


    private void addConceptNode(EntityProxy entity, Function<EntityProxy, String> componentNameLabelFunction) {
        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView();
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setImage(identicon);
        HBox imageViewWrapper = new HBox();
        imageViewWrapper.setAlignment(Pos.CENTER);
        imageViewWrapper.getChildren().add(imageView);
        imageViewWrapper.getStyleClass().add("image-view-container");

        Label componentNameLabel = new Label(componentNameLabelFunction.apply(entity));

        componentNameLabel.getStyleClass().add("selected-concept-description");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane dragHandleIcon = new StackPane();
        dragHandleIcon.getStyleClass().add("drag-handle-icon");
        dragHandleIconContainer.getChildren().add(dragHandleIcon);

        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("selected-concept-discard-region");
        Button closeButton = new Button(null, buttonRegion);
        closeButton.getStyleClass().add("selected-concept-discard-button");
        closeButton.setOnMouseClicked(event -> {
            if (getSkinnable().getOnRemoveAction() != null) {
                getSkinnable().getOnRemoveAction().handle(new ActionEvent());
            }
            typeAheadSearchField.requestFocus();
        });
        closeButton.setAlignment(Pos.CENTER_RIGHT);

        HBox selectedConcept = new HBox(imageViewWrapper, componentNameLabel, spacer, dragHandleIconContainer, closeButton);
        selectedConcept.getStyleClass().add("concept-selected-entity-box");
        selectedConcept.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(selectedConceptContainer, new Insets(8));

        selectedConceptContainer.getChildren().add(selectedConcept);
        conceptContainer.setVisible(false);
    }

    private Image scale(Image source, int targetWidth, int targetHeight) {
        ImageView imageView = new ImageView(source);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        return imageView.snapshot(null, null);
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-control").getString(key);
    }
}
