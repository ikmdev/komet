package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.kview.controls.EditCoordinateOptionsPopup;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout.controls.IconRegion;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.ViewCoordinateHelper;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
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

        // The author is read-only (login-driven); module and path are sorted drop-downs of the available
        // concepts — modules = descendants of MODULE, paths = leaf descendants of PATH (ike-issues#754).
        ObservableEditCoordinate editCoordinate = control.getViewProperties().parentView().editCoordinate();

        ViewCalculator navCalculator = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(
                control.getViewProperties(), TinkarTerm.STATED_NAVIGATION_PATTERN.nid());
        List<ConceptEntity> sortedModules = sortedByName(
                DataModelHelper.fetchDescendentsOfConcept(navCalculator, TinkarTerm.MODULE.publicId()), navCalculator);
        List<ConceptEntity> sortedPaths = sortedByName(
                DataModelHelper.fetchLeafDescendentsOfConcept(navCalculator, TinkarTerm.PATH.publicId()), navCalculator);

        VBox fieldsBox = new VBox(8,
                createReadOnlyAuthorField(editCoordinate),
                createDropDownField("DEFAULT MODULE", editCoordinate.defaultModuleProperty(), sortedModules),
                createDropDownField("DESTINATION MODULE", editCoordinate.destinationModuleProperty(), sortedModules),
                createDropDownField("DEFAULT PATH", editCoordinate.defaultPathProperty(), sortedPaths),
                createDropDownField("PROMOTION PATH", editCoordinate.promotionPathProperty(), sortedPaths)
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
     * Creates a labeled drop-down field for an edit-coordinate property: a sorted ComboBox of the available
     * concepts (module or path), with the current value pre-selected and selection writing back to the
     * coordinate property (ike-issues#754).
     */
    private VBox createDropDownField(String labelText, ObjectProperty<ConceptFacade> coordinateProperty,
                                     List<ConceptEntity> items) {
        Label label = new Label(labelText);
        label.getStyleClass().add("title-label");
        label.setStyle("-fx-text-fill: #E1E8F1;");

        ViewCalculator calculator = control.getViewProperties().calculator();
        ComboBox<ConceptEntity> comboBox = new ComboBox<>(FXCollections.observableArrayList(items));
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ConceptEntity concept) {
                return concept == null ? "" : calculator.getPreferredDescriptionTextWithFallbackOrNid(concept.nid());
            }

            @Override
            public ConceptEntity fromString(String string) {
                return null;
            }
        });

        ConceptFacade currentValue = coordinateProperty.get();
        if (currentValue != null) {
            items.stream().filter(e -> e.nid() == currentValue.nid()).findFirst().ifPresent(comboBox::setValue);
        }
        subscription = subscription.and(coordinateProperty.subscribe((_, newValue) -> {
            if (newValue == null) {
                comboBox.setValue(null);
            } else if (comboBox.getValue() == null || comboBox.getValue().nid() != newValue.nid()) {
                items.stream().filter(e -> e.nid() == newValue.nid()).findFirst().ifPresent(comboBox::setValue);
            }
        }));
        subscription = subscription.and(comboBox.valueProperty().subscribe((_, newEntity) -> {
            if (newEntity != null && (coordinateProperty.get() == null || coordinateProperty.get().nid() != newEntity.nid())) {
                coordinateProperty.setValue(newEntity);
                LOG.info("Edit coordinate {} updated to: {}", labelText, newEntity);
            }
        }));

        VBox fieldBox = new VBox(2, label, comboBox);
        fieldBox.getStyleClass().add("edit-coordinate-field");
        return fieldBox;
    }

    private List<ConceptEntity> sortedByName(java.util.Set<ConceptEntity> concepts, ViewCalculator calculator) {
        return concepts.stream()
                .sorted(Comparator.comparing(c -> calculator.getPreferredDescriptionTextWithFallbackOrNid(c.nid())))
                .toList();
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
