package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region arrow;
    private final VBox titleBox;
    private final TruncatedTextFlow selectedOption;
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ToggleButton allToggle;
    private final VBox contentBox;
    private boolean allSelection = false, singleSelection = false, collapsing = false;
    private Subscription subscription;

    public FilterTitledPaneSkin(FilterTitledPane control) {
        super(control);

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.setMaxWidth(240);
        selectedOption.getStyleClass().add("option");
        selectedOption.textProperty().bindBidirectional(control.optionProperty());

        allToggle = new ToggleButton(resources.getString("titled.pane.option.all"), new IconRegion("check"));
        allToggle.getStyleClass().add("all-toggle");

        titleBox = new VBox(titleLabel, selectedOption, allToggle);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        Region titleRegion = (Region) control.lookup(".title");
        selectedOption.boundsInParentProperty().subscribe(b ->
                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30));

        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        contentBox = new VBox(separatorRegion);
        contentBox.getStyleClass().add("content-box");
        control.getOptions().forEach(o -> {
            ToggleButton toggle = new ToggleButton(o, new IconRegion("check"));
            toggle.getStyleClass().add("option-toggle");
            if (!control.isMultiSelect()) {
                toggle.setToggleGroup(toggleGroup);
            }
            contentBox.getChildren().add(toggle);
        });
        control.setContent(contentBox);

        setupTitledPane();
    }

    private void setupTitledPane() {

        // if user clicks on All, toggles on/off all:
        subscription = allToggle.selectedProperty().subscribe(selected -> {
            if (!singleSelection) {
                allSelection = true;
                contentBox.getChildren().stream()
                        .filter(ToggleButton.class::isInstance)
                        .map(ToggleButton.class::cast)
                        .forEach(tb -> tb.setSelected(!selected));
                allSelection = false;
            }
        });

        contentBox.getChildren().stream()
                .filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .forEach(tb -> {
                    subscription = subscription.and(tb.selectedProperty().subscribe(_ -> {
                        if (!allSelection) {
                            singleSelection = true;
                            // select All if none of the toggles are selected
                            allToggle.setSelected(contentBox.getChildren().stream()
                                    .filter(ToggleButton.class::isInstance)
                                    .map(ToggleButton.class::cast).noneMatch(ToggleButton::isSelected));
                            singleSelection = false;
                        }
                    }));
                    tb.setSelected(!allToggle.isSelected());
                });

        subscription = subscription.and(getSkinnable().expandedProperty().subscribe(expanded -> {
            if (expanded) {
                allToggle.setSelected(allToggle.getText().equals(selectedOption.getText()));
            } else {
                // when the titledPane is collapsed:
                collapsing = true;
                if (allToggle.isSelected() || contentBox.getChildren().stream()
                        .filter(ToggleButton.class::isInstance)
                        .map(ToggleButton.class::cast).allMatch(ToggleButton::isSelected)) {
                    selectedOption.setText(allToggle.getText());
                } else {
                    selectedOption.setText(contentBox.getChildren().stream()
                            .filter(ToggleButton.class::isInstance)
                            .map(ToggleButton.class::cast)
                            .filter(ToggleButton::isSelected)
                            .map(ToggleButton::getText)
                            .collect(Collectors.joining(", ")));
                }
                collapsing = false;
            }
        }));
        subscription = subscription.and(selectedOption.textProperty().subscribe((_, t) -> {
            if (!collapsing) {
                // when the popup control sets a new option
                setupTitledPane();
            }
            pseudoClassStateChanged(MODIFIED_TITLED_PANE, !((FilterTitledPane) getSkinnable()).getDefaultOption().equals(t));
        }));
    }

    @Override
    public void dispose() {
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

}
