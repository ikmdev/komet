package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.kview.controls.EditCoordinateOptionsPopup;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout.controls.IconRegion;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class EditCoordinateOptionsPopupSkin implements Skin<EditCoordinateOptionsPopup> {
    private static final Logger LOG = LoggerFactory.getLogger(EditCoordinateOptionsPopupSkin.class);

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.edit-coordinate-options");

    private final EditCoordinateOptionsPopup control;
    private final VBox root;
    private Subscription subscription;

    public EditCoordinateOptionsPopupSkin(EditCoordinateOptionsPopup control) {
        this.control = control;
        subscription = Subscription.EMPTY;

        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> getSkinnable().hide());

        Label title = new Label(resources.getString("control.title"));
        title.getStyleClass().add("title");

        HBox headerBox = new HBox(closePane, title);
        headerBox.getStyleClass().add("header-box");

        // Create inline search fields for each edit coordinate property
        ObservableEditCoordinate editCoordinate = control.getViewProperties().parentView().editCoordinate();

        VBox fieldsBox = new VBox(8,
                createReadOnlyAuthorField(editCoordinate),
                createField("DEFAULT MODULE", editCoordinate.defaultModuleProperty()),
                createField("DESTINATION MODULE", editCoordinate.destinationModuleProperty()),
                createField("DEFAULT PATH", editCoordinate.defaultPathProperty()),
                createField("PROMOTION PATH", editCoordinate.promotionPathProperty())
        );
        fieldsBox.getStyleClass().add("content-box");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button revertButton = new Button(resources.getString("button.revert"));
        revertButton.setOnAction(_ -> {
            // Revert all fields to the current edit coordinate values
            // (the controls are already bound, so hiding and re-showing will refresh)
            getSkinnable().hide();
        });

        VBox bottomBox = new VBox(revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, fieldsBox, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        // The CSS lives with the relocated control in knowledge-layout (dev.ikm.komet.layout.controls), so
        // anchor the lookup on that module's FilterOptionsPopup, like LangEditCoordinateTitledPaneSkin (ike-issues#758).
        root.getStylesheets().add(FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        control.setOnShown(_ -> {});
    }

    /**
     * Creates a labeled KLComponentControl field that is bound to an edit coordinate property.
     * The user can clear the current value and type to search for a replacement.
     */
    private VBox createField(String labelText, ObjectProperty<ConceptFacade> coordinateProperty) {
        Label label = new Label(labelText);
        label.getStyleClass().add("title-label");
        // The shared filter-options-popup.css has no rule for these content labels, so they render faint;
        // pin a legible light fill (matching the header title) against the dark popup body (ike-issues#754).
        label.setStyle("-fx-text-fill: #E1E8F1;");

        KLComponentControl componentControl = KLComponentControlFactory.createComponentControl(
                control.getViewProperties().calculator());

        // Set the current value from the edit coordinate, and track future changes
        ConceptFacade currentValue = coordinateProperty.get();
        if (currentValue != null) {
            componentControl.setEntity(currentValue.toProxy());
        }
        subscription = subscription.and(coordinateProperty.subscribe((_, newValue) -> {
            if (newValue != null) {
                EntityProxy current = componentControl.getEntity();
                if (current == null || KLComponentControl.isEmpty(current) || current.nid() != newValue.nid()) {
                    componentControl.setEntity(newValue.toProxy());
                }
            }
        }));

        // When user selects a new concept via inline search, update the edit coordinate
        subscription = subscription.and(componentControl.entityProperty().subscribe((_, newEntity) -> {
            if (newEntity != null && !KLComponentControl.isEmpty(newEntity) && newEntity instanceof ConceptFacade newConcept) {
                coordinateProperty.setValue(newConcept);
                LOG.info("Edit coordinate {} updated to: {}", labelText, newConcept);
            }
        }));

        // When user removes the concept, clear the field (but don't clear the coordinate)
        componentControl.setOnRemoveAction(_ -> componentControl.setEntity(null));

        VBox fieldBox = new VBox(2, label, componentControl);
        fieldBox.getStyleClass().add("edit-coordinate-field");
        return fieldBox;
    }

    /**
     * Builds the READ-ONLY author field. The edit-coordinate author is the logged-in user, set at login
     * (ike-issues#754); it must not be edited here — to change it, log in as a different user. So the resolved
     * author is shown as plain text rather than an editable search control.
     */
    private VBox createReadOnlyAuthorField(ObservableEditCoordinate editCoordinate) {
        Label label = new Label("AUTHOR for CHANGES");
        label.getStyleClass().add("title-label");
        label.setStyle("-fx-text-fill: #E1E8F1;");

        Label authorValue = new Label();
        authorValue.getStyleClass().add("edit-coordinate-readonly-value");
        authorValue.setStyle("-fx-text-fill: #E1E8F1; -fx-font-size: 14; -fx-padding: 6 0 6 2;");
        updateAuthorText(authorValue, editCoordinate.authorForChangesProperty().get());
        subscription = subscription.and(editCoordinate.authorForChangesProperty().subscribe((_, newAuthor) ->
                updateAuthorText(authorValue, newAuthor)));

        VBox fieldBox = new VBox(2, label, authorValue);
        fieldBox.getStyleClass().add("edit-coordinate-field");
        return fieldBox;
    }

    private void updateAuthorText(Label authorValue, ConceptFacade author) {
        if (author == null) {
            authorValue.setText("—");
        } else {
            authorValue.setText(control.getViewProperties().calculator()
                    .getPreferredDescriptionTextWithFallbackOrNid(author.nid()));
        }
    }

    @Override
    public EditCoordinateOptionsPopup getSkinnable() {
        return control;
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
