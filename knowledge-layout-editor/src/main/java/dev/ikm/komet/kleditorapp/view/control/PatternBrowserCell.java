package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.kleditorapp.view.PatternBrowserItem;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A Cell that renders each item in the Pattern Browser List.
 */
public class PatternBrowserCell extends ListCell<PatternBrowserItem> {
    private static final Logger LOG = LoggerFactory.getLogger(PatternBrowserCell.class);

    public static final DataFormat KL_EDITOR_VERSION_PROXY = new DataFormat("kl-editor/komet-pattern-version-proxy");

    private final HBox mainContainer;
    private final VBox contentContainer;
    private final BorderPane subContentContainer;
    private final HBox lastUpdatedContainer;

    private final ImageView identiconImageView;
    private final Label titleLabel;
    private final Label statusLabel;
    private final Label lastUpdatedLabel;
    private final Label lastUpdatedTextLabel;

    private ViewCalculator viewCalculator;

    private PatternEntityVersion currentPatternEntityVersion;

    public PatternBrowserCell(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        mainContainer = new HBox();
        contentContainer = new VBox();
        subContentContainer = new BorderPane();
        lastUpdatedContainer = new HBox();

        identiconImageView = new ImageView();

        titleLabel = new Label();
        statusLabel = new Label();
        lastUpdatedLabel = new Label();
        lastUpdatedTextLabel = new Label();

        mainContainer.getChildren().setAll(identiconImageView, contentContainer);
        contentContainer.getChildren().setAll(titleLabel, subContentContainer);
        subContentContainer.setLeft(statusLabel);
        subContentContainer.setRight(lastUpdatedContainer);

        HBox.setHgrow(contentContainer, Priority.ALWAYS);

        lastUpdatedContainer.getChildren().addAll(lastUpdatedLabel, lastUpdatedTextLabel);

        setGraphic(mainContainer);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        setUpDragAndDrop();

        // CSS
        mainContainer.getStyleClass().add("main-container");
        contentContainer.getStyleClass().add("content-container");
        subContentContainer.getStyleClass().add("sub-content-container");
        titleLabel.getStyleClass().add("title-label");
        statusLabel.getStyleClass().add("status-label");
        lastUpdatedContainer.getStyleClass().add("last-updated-container");
        lastUpdatedTextLabel.getStyleClass().add("last-updated-label");
    }

    @Override
    protected void updateItem(PatternBrowserItem patternBrowserItem, boolean empty) {
        super.updateItem(patternBrowserItem, empty);

        if (!isEmpty()) {
            Latest<PatternEntityVersion> optionalLatestPattern = viewCalculator.latest(patternBrowserItem.getNid());
            optionalLatestPattern.ifPresentOrElse(latestPattern -> {
                // Title
                titleLabel.setText(patternBrowserItem.getTitle());

                // Identicon
                Image identiconImage = Identicon.generateIdenticonImage(patternBrowserItem.getPublicId());
                identiconImageView.setImage(identiconImage);

                currentPatternEntityVersion = latestPattern;

                StampEntity stamp = latestPattern.stamp();
                // Status
                String statusText = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stamp.stateNid());
                statusLabel.setText(statusText);

                // Last updated
                long stampTime = stamp.time();
                lastUpdatedLabel.setText("Last updated: ");
                lastUpdatedTextLabel.setText(TimeUtils.toShortDateString(stampTime));
            }, () -> {
                clearCellsContent();
            });
        } else {
            clearCellsContent();
        }
    }

    private void clearCellsContent() {
        currentPatternEntityVersion = null;

        titleLabel.setText("");
        identiconImageView.setImage(null);
        statusLabel.setText("");
        lastUpdatedLabel.setText("");
        lastUpdatedTextLabel.setText("");
    }

    private void setUpDragAndDrop() {
        // Set up the drag detection event handler
        setOnDragDetected(mouseEvent -> {
            if (currentPatternEntityVersion == null) {
                return;
            }

            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);

            // Create the content to be placed on the dragboard
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.put(KL_EDITOR_VERSION_PROXY, currentPatternEntityVersion.nid());

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(this);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(clipboardContent);

            // Log the drag event details for debugging or auditing
            LOG.info("Drag detected on Pattern Browser cell: " + mouseEvent.toString());

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }
}