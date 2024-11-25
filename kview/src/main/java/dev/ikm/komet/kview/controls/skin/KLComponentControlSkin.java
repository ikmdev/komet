package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class KLComponentControlSkin extends SkinBase<KLComponentControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentControl.class);
    private static final String SEARCH_TEXT_VALUE = "search.text.value";
    private static final DataFormat CONTROL_DRAG_FORMAT;
    static {
        DataFormat dataFormat = DataFormat.lookupMimeType("text/concept-control-format");
        CONTROL_DRAG_FORMAT = dataFormat == null ? new DataFormat("text/concept-control-format") : dataFormat;
    }

    private final Label titleLabel;
    private final StackPane selectedConceptContainer;
    private final StackPane conceptContainer;
    private final HBox aboutToDropHBox;
    private final HBox aboutToRearrangeHBox;

    public KLComponentControlSkin(KLComponentControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.managedProperty().bind(titleLabel.visibleProperty());
        titleLabel.visibleProperty().bind(titleLabel.textProperty().isNotEmpty());

        selectedConceptContainer = new StackPane();
        selectedConceptContainer.getStyleClass().add("selected-concept-container");
        selectedConceptContainer.managedProperty().bind(selectedConceptContainer.visibleProperty());

        aboutToDropHBox = createDragOverAnimation();
        aboutToRearrangeHBox = createDragOverAnimation();
        conceptContainer = new StackPane(createSearchBox(), aboutToDropHBox);
        conceptContainer.getStyleClass().add("concept-container");
        conceptContainer.managedProperty().bind(conceptContainer.visibleProperty());
        selectedConceptContainer.visibleProperty().bind(conceptContainer.visibleProperty().not());
        getChildren().addAll(titleLabel, selectedConceptContainer, conceptContainer, aboutToRearrangeHBox);

        setupDragNDrop();

        registerChangeListener(getSkinnable().entityProperty(), entity -> {
            if (entity == null || entity.getValue() == null) {
               selectedConceptContainer.getChildren().clear();
               conceptContainer.setVisible(true);
           }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        unregisterChangeListeners(getSkinnable().entityProperty());
    }

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
            if (event.getDragboard().hasContent(CONTROL_DRAG_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            } else if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        control.setOnDragEntered(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                conceptContainer.setOpacity(.90);
                if (event.getDragboard().hasContent(CONTROL_DRAG_FORMAT)) {
                    if (hasAllowedDND(control)) {
                        aboutToRearrangeHBox.setVisible(true);
                    }
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
            event.consume();
        });

        control.setOnDragDetected(ev -> {
            if (hasAllowedDND(control)) {
                Dragboard dragboard = control.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(CONTROL_DRAG_FORMAT, "concept-control");
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
            boolean success = false;
            Dragboard dragboard = event.getDragboard();
            if (event.getDragboard().hasContent(CONTROL_DRAG_FORMAT) &&
                    event.getGestureSource() instanceof KLComponentControl cc && haveAllowedDND(control, cc)) {
                // reorder components
                if (control.getParent() instanceof KLComponentSetControl componentSetControl) {
                    KLComponentSetControlSkin skin = (KLComponentSetControlSkin) componentSetControl.getSkin();
                    int sourceIndex = skin.getChildren().indexOf(cc);
                    int targetIndex = skin.getChildren().indexOf(control);
                    final Node node = skin.getChildren().remove(sourceIndex);
                    skin.getChildren().add(targetIndex, node);
                    success = true;
                } else if (control.getParent() instanceof KLComponentListControl componentListControl) {
                    KLComponentListControlSkin skin = (KLComponentListControlSkin) componentListControl.getSkin();
                    int sourceIndex = skin.getChildren().indexOf(cc);
                    int targetIndex = skin.getChildren().indexOf(control);
                    final Node node = skin.getChildren().remove(sourceIndex);
                    skin.getChildren().add(targetIndex, node);
                    success = true;
                }
            } else if (dragboard.hasString() && !(event.getGestureSource() instanceof KLComponentControl)) {
                // drop concept
                try {
                    LOG.info("publicId: {}", dragboard.getString());
                    if (event.getGestureSource() instanceof Node source &&
                            source.getUserData() instanceof PublicId publicId &&
                            publicId.toString().equals(dragboard.getString())) { // TODO: should this be needed? shouldn't we get PublicId from dragboard content?
                        if (control.getEntity() == null) {
                            Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                            if (!(control.getParent() instanceof KLComponentSetControl componentSetControl) ||
                                    !componentSetControl.getEntitiesList().contains(entity)) {
                                control.setEntity(entity);
                                addConceptNode(entity);
                                success = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("exception: ", e);
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean hasAllowedDND(KLComponentControl control) {
        return control != null && control.getEntity() != null &&
                ((control.getParent() instanceof KLComponentSetControl cs && cs.getEntitiesList().size() > 1) ||
                (control.getParent() instanceof KLComponentListControl cl && cl.getEntitiesList().size() > 1));
    }

    private boolean haveAllowedDND(KLComponentControl source, KLComponentControl target) {
        // only allowed if both source and target have the same parent
        return hasAllowedDND(source) && hasAllowedDND(target) &&
                ((source.getParent() instanceof KLComponentSetControl cs1 && target.getParent() instanceof KLComponentSetControl cs2 && cs1 == cs2) ||
                 (source.getParent() instanceof KLComponentListControl cl1 && target.getParent() instanceof KLComponentListControl cl2 && cl1 == cl2));
    }

    private HBox createSearchBox() {
        TextField searchTextField = new TextField();
        searchTextField.getStyleClass().add("concept-text-field");
        searchTextField.setPromptText(getString("textfield.prompt.text"));
        searchTextField.onActionProperty().bind(getSkinnable().onSearchActionProperty());
        searchTextField.textProperty().subscribe(text -> getSkinnable().getProperties().put(SEARCH_TEXT_VALUE, text));
        HBox.setHgrow(searchTextField, Priority.ALWAYS);

        Region searchRegion = new Region();
        searchRegion.getStyleClass().add("concept-search-region");
        Button searchButton = new Button(null, searchRegion);
        searchButton.getStyleClass().add("concept-search-button");
        searchButton.disableProperty().bind(searchTextField.textProperty().isEmpty());
        searchButton.onActionProperty().bind(getSkinnable().onSearchActionProperty());

        Region addRegion = new Region();
        addRegion.getStyleClass().add("concept-add-region");
        addRegion.setOnMouseClicked(e -> {
            if (getSkinnable().getOnAddConceptAction() != null) {
                getSkinnable().getOnAddConceptAction().handle(new ActionEvent());
            }
        });
        HBox.setMargin(addRegion, new Insets(2, 5, 2, 4));
        addRegion.managedProperty().bind(addRegion.visibleProperty());
        addRegion.visibleProperty().bind(getSkinnable().showAddConceptProperty());

        HBox searchBox = new HBox(searchTextField, searchButton, addRegion);
        searchBox.getStyleClass().add("concept-search-box");
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

    private void addConceptNode(Entity<?> entity) {
        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setImage(identicon);
        HBox imageViewWrapper = new HBox();
        imageViewWrapper.setAlignment(Pos.CENTER);
        HBox.setMargin(imageView, new Insets(0, 8, 0 ,8));
        imageViewWrapper.getChildren().add(imageView);

        Label conceptNameLabel = new Label(entity.description());
        conceptNameLabel.getStyleClass().add("selected-concept-description");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("selected-concept-discard-region");
        Button closeButton = new Button(null, buttonRegion);
        closeButton.getStyleClass().add("selected-concept-discard-button");
        closeButton.setOnMouseClicked(event -> {
            if (getSkinnable().getOnRemoveAction() != null) {
                getSkinnable().getOnRemoveAction().handle(new ActionEvent());
            }
        });
        closeButton.setAlignment(Pos.CENTER_RIGHT);

        HBox selectedConcept = new HBox(imageViewWrapper, conceptNameLabel, spacer, closeButton);
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
