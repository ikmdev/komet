package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.KlPatternSemanticsFactories;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

public class PatternPropertiesPane extends GridNodePropertiesPane<EditorPatternModel> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-properties";

    private final VBox patternMainContainer = new VBox();

    private final TextField titleTextField;
    private final TextField identifierTextField;
    private final ToggleSwitch titleVisibleTSwitch;

    private final ToggleSwitch requiredTSwitch;
    private final ToggleSwitch allowNewSemanticsTSwitch;
    private final PatternRequirementsView requirementsView = new PatternRequirementsView();

    private final PatternSemanticFiltersView semanticFiltersView = new PatternSemanticFiltersView();

    private final ComboBox<KlPatternSemanticsFactory> displayComboBox;

    // Factory-specific properties section, rebuilt from the model whenever the factory changes.
    private final Separator factoryPropertiesSeparator = new Separator();
    private final Label factoryPropertiesTitleLabel = new Label("PROPERTIES");
    private final VBox factoryPropertiesSection = new VBox();
    private Subscription factoryPropertiesSubscription;

    public PatternPropertiesPane() {
        super(true);

        // Title container: the title and its Visible toggle form one group, the identifier another,
        // with a wider gap between the two so the toggle reads as belonging to the title
        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().addAll("sub-section", "title-container");
        titleContainer.setSpacing(16);

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

        VBox titleGroup = new VBox(titleLabel, titleTextField, titleVisibleTSwitch);
        titleGroup.getStyleClass().add("title-group");
        titleGroup.setSpacing(4);

        VBox identifierGroup = new VBox(identifierLabel, identifierTextField);
        identifierGroup.getStyleClass().add("identifier-group");
        identifierGroup.setSpacing(4);

        titleContainer.getChildren().addAll(titleGroup, identifierGroup);

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // Positioning container
        VBox positioningContainer = new VBox();
        positioningContainer.getStyleClass().addAll("sub-section", "positioning-container");
        positioningContainer.setSpacing(4);
        positioningContainer.getChildren().addAll(
                positioningLabel,
                positioningGridPane
        );

        // Interaction container
        VBox interactionContainer = new VBox();
        interactionContainer.getStyleClass().addAll("sub-section", "interaction-container");
        interactionContainer.setSpacing(4);

        // "INTERACTION" label
        Label interactionTitleLabel = new Label("INTERACTION");
        interactionTitleLabel.getStyleClass().add("group-title");

        // Display GridPane
        GridPane displayGridPane = new PropertyGridPane();
        displayGridPane.setVgap(4);

        // - Rows sized like the property-set rows below, so the two sections space their rows alike
        for (int i = 0; i < 2; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            displayGridPane.getRowConstraints().add(row);
        }

        // - "Display" label in grid
        Label displayLabel = new Label("Display:");
        displayGridPane.add(displayLabel, 0, 0);

        // - "Display" ComboBox in grid
        displayComboBox = new ComboBox<>();
        displayComboBox.setCellFactory(_ -> createDisplayCell());
        displayComboBox.setButtonCell(createDisplayCell());
        displayGridPane.add(displayComboBox, 1, 0);

        populateDisplayComboBox();

        // - "Add semantics in edit mode" row in grid, laid out like the property-set toggles (label
        // column, control column). When off, new semantics of the Pattern can only be added while
        // the window is in create mode in the Journal; existing semantics stay editable.
        Label allowNewSemanticsLabel = new Label("Add semantics in edit mode:");
        displayGridPane.add(allowNewSemanticsLabel, 0, 1);

        allowNewSemanticsTSwitch = new ToggleSwitch();
        allowNewSemanticsTSwitch.setSelected(true);
        allowNewSemanticsTSwitch.getStyleClass().add("allow-new-semantics");
        displayGridPane.add(allowNewSemanticsTSwitch, 1, 1);

        interactionContainer.getChildren().addAll(
                interactionTitleLabel,
                displayGridPane
        );

        // Separator
        Separator separator3 = new Separator();
        separator3.setPrefWidth(200);

        // Properties container
        VBox propertiesContainer = new VBox();
        propertiesContainer.getStyleClass().addAll("sub-section", "properties-container");
        propertiesContainer.setSpacing(4);

        // Factory-specific properties section
        factoryPropertiesSeparator.setPrefWidth(200);
        factoryPropertiesTitleLabel.getStyleClass().add("group-title");
        factoryPropertiesSection.getStyleClass().add("factory-properties-section");
        factoryPropertiesSection.setSpacing(8);

        propertiesContainer.getChildren().addAll(
                factoryPropertiesTitleLabel,
                factoryPropertiesSection
        );

        // Data properties container
        VBox dataPropertiesContainer = new VBox();
        dataPropertiesContainer.getStyleClass().addAll("sub-section", "data-properties-container");
        dataPropertiesContainer.setSpacing(4);

        // "DATA PROPERTIES" label
        Label dataPropertiesTitleLabel = new Label("DATA PROPERTIES");
        dataPropertiesTitleLabel.getStyleClass().add("group-title");

        // Required row: label on the left edge, toggle on the right edge
        Label requiredLabel = new Label("Required:");

        Region requiredSpacer = new Region();
        HBox.setHgrow(requiredSpacer, Priority.ALWAYS);

        requiredTSwitch = new ToggleSwitch();
        requiredTSwitch.setSelected(false);
        requiredTSwitch.getStyleClass().add("required");

        HBox requiredRow = new HBox(requiredLabel, requiredSpacer, requiredTSwitch);
        requiredRow.getStyleClass().add("required-row");
        requiredRow.setAlignment(Pos.CENTER_LEFT);

        // Requirement refinements, shown only while the pattern is required
        requirementsView.visibleProperty().bind(requiredTSwitch.selectedProperty());
        requirementsView.managedProperty().bind(requirementsView.visibleProperty());

        dataPropertiesContainer.getChildren().addAll(
                dataPropertiesTitleLabel,
                requiredRow,
                requirementsView
        );

        // Separator between data properties and visible semantics
        Separator dataPropertiesSeparator = new Separator();
        dataPropertiesSeparator.setPrefWidth(200);

        // Visible semantics container: which of the Pattern's semantics are displayed
        VBox visibleSemanticsContainer = new VBox();
        visibleSemanticsContainer.getStyleClass().addAll("sub-section", "visible-semantics-container");
        visibleSemanticsContainer.setSpacing(4);

        Label visibleSemanticsTitleLabel = new Label("VISIBLE SEMANTICS");
        visibleSemanticsTitleLabel.getStyleClass().add("group-title");

        visibleSemanticsContainer.getChildren().addAll(
                visibleSemanticsTitleLabel,
                semanticFiltersView
        );

        // Separator between visible semantics and positioning
        Separator visibleSemanticsSeparator = new Separator();
        visibleSemanticsSeparator.setPrefWidth(200);

        patternMainContainer.getChildren().addAll(
                titleContainer,
                separator,
                dataPropertiesContainer,
                dataPropertiesSeparator,
                visibleSemanticsContainer,
                visibleSemanticsSeparator,
                positioningContainer,
                separator3,
                interactionContainer,
                factoryPropertiesSeparator,
                propertiesContainer
        );

        setContent(patternMainContainer);

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
            titleTextField.textProperty().unbindBidirectional(previouslyShownModel.titleProperty());
            identifierTextField.textProperty().unbindBidirectional(previouslyShownModel.identifierProperty());
            titleVisibleTSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.titleVisibleProperty());
            requiredTSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.requiredProperty());
            allowNewSemanticsTSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.allowNewSemanticsProperty());
            displayComboBox.valueProperty().unbindBidirectional(previouslyShownModel.factoryProperty());

            if (factoryPropertiesSubscription != null) {
                factoryPropertiesSubscription.unsubscribe();
            }
        }

        titleTextField.textProperty().bindBidirectional(currentlyShownModel.titleProperty());
        titleVisibleTSwitch.selectedProperty().bindBidirectional(currentlyShownModel.titleVisibleProperty());
        requiredTSwitch.selectedProperty().bindBidirectional(currentlyShownModel.requiredProperty());
        allowNewSemanticsTSwitch.selectedProperty().bindBidirectional(currentlyShownModel.allowNewSemanticsProperty());
        requirementsView.setPattern(currentlyShownModel);
        semanticFiltersView.setPattern(currentlyShownModel);

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
        // Use the shared factory instances so the combo's selected value (set by the model from the
        // same registry) matches a list item and renders its display name rather than the class name.
        displayComboBox.getItems().setAll(KlPatternSemanticsFactories.all());
    }
}