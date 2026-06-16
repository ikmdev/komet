package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.ServiceLoader;

public class PatternPropertiesPane extends GridNodePropertiesPane<EditorPatternModel> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-properties";

    private final VBox patternMainContainer = new VBox();

    private final TextField titleTextField;
    private final TextField identifierTextField;
    private final ToggleSwitch titleVisibleTSwitch;

    private final ComboBox<KlPatternSemanticsFactory> displayComboBox;

    // Factory-specific properties section, rebuilt from the model whenever the factory changes.
    private final Separator factoryPropertiesSeparator = new Separator();
    private final Label factoryPropertiesTitleLabel = new Label("PROPERTIES");
    private final VBox factoryPropertiesSection = new VBox();
    private Subscription factoryPropertiesSubscription;

    public PatternPropertiesPane() {
        super(true);

        // Section name container
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setSpacing(4);

        Label titleLabel = new Label("Pattern Title:");
        titleTextField = new TextField();

        Label identifierLabel = new Label("Identifier:");
        identifierTextField = new TextField();

        // Visible
        titleVisibleTSwitch = new ToggleSwitch();
        titleVisibleTSwitch.setText("Visible");
        titleVisibleTSwitch.setSelected(false);
        titleVisibleTSwitch.getStyleClass().add("title-visible");

        titleTextField.editableProperty().bind(titleVisibleTSwitch.selectedProperty());

        titleContainer.getChildren().addAll(
                titleLabel,
                titleTextField,
                titleVisibleTSwitch,
                identifierLabel,
                identifierTextField
        );

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // "INTERACTION" label
        Label interactionTitleLabel = new Label("INTERACTION");
        interactionTitleLabel.getStyleClass().add("group-title");

        // Display GridPane
        GridPane displayGridPane = new GridPane();
        displayGridPane.setHgap(8);
        displayGridPane.setVgap(8);

        // - Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(10);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(10);
        col2.setHgrow(Priority.ALWAYS);

        displayGridPane.getColumnConstraints().addAll(col1, col2);

        // - "Display" label in grid
        Label displayLabel = new Label("Display");
        GridPane.setHalignment(displayLabel, HPos.RIGHT);
        displayGridPane.add(displayLabel, 0, 0);

        // - "Display" ComboBox in grid
        displayComboBox = new ComboBox<>();
        displayComboBox.setCellFactory(_ -> createDisplayCell());
        displayComboBox.setButtonCell(createDisplayCell());
        displayComboBox.setMaxWidth(Double.MAX_VALUE);
        displayGridPane.add(displayComboBox, 1, 0);

        populateDisplayComboBox();

        // Separator
        Separator separator3 = new Separator();
        separator3.setPrefWidth(200);

        // Factory-specific properties section
        factoryPropertiesSeparator.setPrefWidth(200);
        factoryPropertiesTitleLabel.getStyleClass().add("group-title");
        factoryPropertiesSection.getStyleClass().add("factory-properties-section");
        factoryPropertiesSection.setSpacing(8);

        patternMainContainer.getChildren().addAll(
                titleContainer,
                separator,
                positioningLabel,
                positioningGridPane,
                separator3,
                interactionTitleLabel,
                displayGridPane,
                factoryPropertiesSeparator,
                factoryPropertiesTitleLabel,
                factoryPropertiesSection
        );

        mainContainer.setCenter(patternMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        patternMainContainer.getStyleClass().add("pattern-main-container");
    }

    private static ListCell<KlPatternSemanticsFactory> createDisplayCell() {
        return new ListCell<>() {
            {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            protected void updateItem(KlPatternSemanticsFactory item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.displayName());
                }
            }
        };
    }

    @Override
    protected void doInit() {
        super.doInit();

        if (previouslyShownModel != null) {
            titleVisibleTSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.titleVisibleProperty());
            displayComboBox.valueProperty().unbindBidirectional(previouslyShownModel.factoryProperty());

            if (factoryPropertiesSubscription != null) {
                factoryPropertiesSubscription.unsubscribe();
            }
        }

        titleTextField.setText(currentlyShownModel.getTitle());
        titleVisibleTSwitch.selectedProperty().bindBidirectional(currentlyShownModel.titleVisibleProperty());

        // Identifier
        identifierTextField.textProperty().bindBidirectional(currentlyShownModel.identifierProperty());

        // Display
        displayComboBox.valueProperty().bindBidirectional(currentlyShownModel.factoryProperty());

        // Factory-specific properties — rebuild now and whenever the factory (and thus its property
        // set) changes. subscribe(Consumer) fires immediately with the current set.
        factoryPropertiesSubscription = currentlyShownModel.factoryPropertiesProperty()
                .subscribe(this::rebuildFactoryProperties);
    }

    private void rebuildFactoryProperties(KlPropertySet propertySet) {
        factoryPropertiesSection.getChildren().clear();

        boolean hasProperties = propertySet != null && !propertySet.discoverProperties().isEmpty();

        factoryPropertiesSeparator.setVisible(hasProperties);
        factoryPropertiesSeparator.setManaged(hasProperties);
        factoryPropertiesTitleLabel.setVisible(hasProperties);
        factoryPropertiesTitleLabel.setManaged(hasProperties);
        factoryPropertiesSection.setVisible(hasProperties);
        factoryPropertiesSection.setManaged(hasProperties);

        if (hasProperties) {
            factoryPropertiesSection.getChildren().add(KlPropertySetEditor.create(propertySet));
        }
    }

    private void populateDisplayComboBox() {
        ServiceLoader<KlPatternSemanticsFactory> loader = ServiceLoader.load(KlPatternSemanticsFactory.class);
        for (KlPatternSemanticsFactory factory : loader) {
            displayComboBox.getItems().add(factory);
        }
    }
}