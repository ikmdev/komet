package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.ConceptControl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class ConceptControlSkin extends SkinBase<ConceptControl> {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptControl.class);
    private static final String SEARCH_TEXT_VALUE = "search.text.value";

    private final Label titleLabel;
    private final StackPane selectedConceptContainer;
    private final StackPane conceptContainer;
    private final HBox aboutToDropHBox;

    public ConceptControlSkin(ConceptControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());

        selectedConceptContainer = new StackPane();
        selectedConceptContainer.getStyleClass().add("selected-concept-container");
        selectedConceptContainer.managedProperty().bind(selectedConceptContainer.visibleProperty());

        aboutToDropHBox = createDragOverAnimation();
        conceptContainer = new StackPane(createSearchBox(), aboutToDropHBox);
        conceptContainer.getStyleClass().add("concept-container");
        conceptContainer.managedProperty().bind(conceptContainer.visibleProperty());
        selectedConceptContainer.visibleProperty().bind(conceptContainer.visibleProperty().not());
        getChildren().addAll(titleLabel, selectedConceptContainer, conceptContainer);

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
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        Insets padding = getSkinnable().getPadding();
        titleLabel.resizeRelocate(contentX + padding.getLeft(), contentY + padding.getTop(), labelPrefWidth, labelPrefHeight);
        selectedConceptContainer.resizeRelocate(contentX, contentY + labelPrefHeight, contentWidth, contentHeight - padding.getBottom());
        conceptContainer.resizeRelocate(contentX + padding.getLeft(), contentY + padding.getTop() + labelPrefHeight, contentWidth, contentHeight - padding.getBottom());
    }

    private void setupDragNDrop() {
        ConceptControl control = getSkinnable();
        control.setOnDragOver(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        control.setOnDragEntered(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasString()) {
                conceptContainer.setOpacity(.90);
            }
            aboutToDropHBox.setVisible(true);
            event.consume();
        });

        control.setOnDragExited(event -> {
            conceptContainer.setOpacity(1);
            aboutToDropHBox.setVisible(false);
            event.consume();
        });

        control.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                try {
                    LOG.info("publicId: {}", dragboard.getString());
                    if (event.getGestureSource() instanceof Node source &&
                            source.getUserData() instanceof PublicId publicId &&
                            publicId.toString().equals(dragboard.getString())) { // TODO: should this be needed? shouldn't we get PublicId from dragboard content?
                        if (control.getEntity() == null) {
                            Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                            control.setEntity(entity);
                            addConceptNode(entity);
                        }
                        success = true;
                    }
                } catch (Exception e) {
                    LOG.error("exception: ", e);
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
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
        closeButton.setOnMouseClicked(event -> getSkinnable().setEntity(null));
        closeButton.setAlignment(Pos.CENTER_RIGHT);

        HBox selectedConcept = new HBox(imageViewWrapper, conceptNameLabel, spacer, closeButton);
        selectedConcept.getStyleClass().add("concept-selected-entity-box");
        selectedConcept.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(selectedConceptContainer, new Insets(8));

        selectedConceptContainer.getChildren().add(selectedConcept);
        conceptContainer.setVisible(false);
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-control").getString(key);
    }
}
