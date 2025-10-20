package dev.ikm.komet.kleditorapp;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * A Cell that renders each item in the Pattern Browser List.
 */
public class PatternBrowserCell extends ListCell<Entity<EntityVersion>> {
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
    protected void updateItem(Entity<EntityVersion> entity, boolean empty) {
        super.updateItem(entity, empty);

        if (!isEmpty() && entity != null) {
            titleLabel.setText(retrieveDisplayName(entity.toProxy()));
            Image identiconImage = Identicon.generateIdenticonImage(entity.publicId());
            identiconImageView.setImage(identiconImage);

            Latest<PatternEntityVersion> optionalLatestPattern = viewCalculator.latest(entity.nid());
            optionalLatestPattern.ifPresentOrElse(latestPattern -> {
                StampEntity stamp = latestPattern.stamp();

                String statusText = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stamp.stateNid());
                statusLabel.setText(statusText);

                // Last updated
                long stampTime = stamp.time();
                lastUpdatedLabel.setText("Last updated: ");
                lastUpdatedTextLabel.setText(TimeUtils.toShortDateString(stampTime));
            }, () -> {
                lastUpdatedLabel.setText("");
                statusLabel.setText("");
                lastUpdatedTextLabel.setText("");
            });

        } else {
            titleLabel.setText("");
            identiconImageView.setImage(null);
            statusLabel.setText("");
            lastUpdatedLabel.setText("");
            lastUpdatedTextLabel.setText("");
        }
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }
}