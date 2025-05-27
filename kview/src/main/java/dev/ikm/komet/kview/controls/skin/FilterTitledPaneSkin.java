package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class FilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass EXCLUDING_TITLED_PANE = PseudoClass.getPseudoClass("excluding");
    private static final PseudoClass EXCLUDED_OPTION = PseudoClass.getPseudoClass("excluded");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region arrow;
    private final VBox titleBox;
    private final TruncatedTextFlow selectedOption;
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ToggleButton allToggle;
    private final ToggleButton excludingToggle;
    private final HBox togglesBox;
    private final VBox contentBox;
    private final FilterTitledPane control;
    private boolean allSelection = false, singleSelection = false;
    private Subscription subscription;

    public FilterTitledPaneSkin(FilterTitledPane control) {
        super(control);
        this.control = (FilterTitledPane) getSkinnable();

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.setMaxWidth(240);
        selectedOption.getStyleClass().add("option");
        selectedOption.textProperty().bind(control.optionProperty());

        allToggle = new ToggleButton(resources.getString("titled.pane.option.all"), new IconRegion("check"));
        allToggle.getStyleClass().add("all-toggle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        excludingToggle = new ToggleButton(resources.getString("titled.pane.option.excluding"), new IconRegion("check"));
        excludingToggle.getStyleClass().add("exclude-toggle");
        excludingToggle.disableProperty().bind(Bindings.createBooleanBinding(() ->
                control.getSelectedOptions().size() != control.getAvailableOptions().size(),
                control.getSelectedOptions(), control.getAvailableOptions()));

        togglesBox = new HBox(allToggle, spacer, excludingToggle);
        togglesBox.getStyleClass().add("toggles-box");

        titleBox = new VBox(titleLabel, selectedOption, togglesBox);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        Region titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        contentBox = new VBox(separatorRegion);
        contentBox.getStyleClass().add("content-box");
        control.getAvailableOptions().forEach(text ->
                contentBox.getChildren().add(new OptionToggle(text)));

        control.getAvailableOptions().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(text ->
                            contentBox.getChildren().add(new OptionToggle(text)));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(o ->
                            contentBox.getChildren().removeIf(t -> t instanceof OptionToggle toggle &&
                                    toggle.getText().equals(o)));
                }
            }
        });
        control.optionProperty().bind(Bindings.createStringBinding(() -> {
            if (control.isExcluding() && !control.getExcludedOptions().isEmpty()) {
                return MessageFormat.format(resources.getString("titled.pane.option.exclude"),
                            String.join(", ", control.getExcludedOptions()));
            } else {
                if (control.getSelectedOptions().size() == control.getAvailableOptions().size()) {
                    return resources.getString("titled.pane.option.all");
                } else if (control.getSelectedOptions().isEmpty()) {
                    return resources.getString("titled.pane.option.none");
                } else {
                    return String.join(", ", control.getSelectedOptions());
                }
            }
        }, control.getSelectedOptions(), control.getExcludedOptions()));

        control.setContent(contentBox);
        allToggle.disableProperty().bind(Bindings.size(contentBox.getChildren()).lessThanOrEqualTo(1));
        allToggle.setSelected(control.getSelectedOptions().size() == control.getAvailableOptions().size());

        setupTitledPane();
    }

    private void setupTitledPane() {

        subscription = selectedOption.boundsInParentProperty().subscribe(b ->
                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30));

        // if user clicks on All, toggles on/off all:
        subscription = subscription.and(allToggle.selectedProperty().subscribe((_, selected) -> {
            if (!singleSelection) {
                allSelection = true;
                getOptionToggles().forEach(tb -> tb.setSelected(selected));
                if (selected) {
                    getOptionToggles().forEach(tb -> tb.setExcluded(false));
                    excludingToggle.setSelected(false);
                }
                allSelection = false;
            }
        }));
        subscription = subscription.and(excludingToggle.selectedProperty().subscribe((_, selected) -> {
            if (control.isExcluding()) {
                singleSelection = true;
                if (selected) {
                    allToggle.setSelected(false);
                } else {
                    getOptionToggles().forEach(tb -> {
                        tb.setExcluded(false);
                        tb.setSelected(true);
                    });
                    allToggle.setSelected(true);
                }
                singleSelection = false;
            }
        }));

        subscription = subscription.and(excludingToggle.disableProperty().subscribe(d -> {
            if (d) {
                // remove check from toggle
                excludingToggle.setSelected(false);
            }
        }));

        getOptionToggles().forEach(tb -> {
            subscription = subscription.and(tb.selectedProperty().subscribe((_, selected) -> {
                if (!allSelection) {
                    singleSelection = true;
                    // select All if all the toggles are selected
                    allToggle.setSelected(getOptionToggles().allMatch(OptionToggle::isSelected) &&
                            !excludingToggle.isSelected());
                    singleSelection = false;
                }
                if (selected && !control.getSelectedOptions().contains(tb.getText())) {
                    control.getSelectedOptions().add(tb.getText());
                } else if (!selected && !(control.isExcluding() && excludingToggle.isSelected())) {
                    control.getSelectedOptions().remove(tb.getText());
                }
            }));
            subscription = subscription.and(tb.excludedProperty().subscribe((_, excluded) -> {
                if (control.isExcluding()) {
                    if (excluded && !control.getExcludedOptions().contains(tb.getText())) {
                        control.getExcludedOptions().add(tb.getText());
                    } else if (!excluded) {
                        control.getExcludedOptions().remove(tb.getText());
                    }
                }
            }));
            tb.setExcluded(control.getExcludedOptions().contains(tb.getText()));
            tb.setSelected(control.getSelectedOptions().contains(tb.getText()));
        });

        subscription = subscription.and(selectedOption.textProperty().subscribe((_, t) ->
            pseudoClassStateChanged(MODIFIED_TITLED_PANE, !control.getDefaultOption().equals(t))));

        subscription = subscription.and(control.excludingProperty().subscribe(excluding ->
                togglesBox.pseudoClassStateChanged(EXCLUDING_TITLED_PANE, excluding)));
    }

    @Override
    public void dispose() {
        selectedOption.textProperty().unbind();
        control.optionProperty().unbind();
        excludingToggle.disableProperty().unbind();
        allToggle.disableProperty().unbind();
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    private Stream<OptionToggle> getOptionToggles() {
        return contentBox.getChildren().stream()
                .filter(OptionToggle.class::isInstance)
                .map(OptionToggle.class::cast);
    }

    private class OptionToggle extends HBox {

        private final ToggleButton toggleButton;

        public OptionToggle(String text) {
            StackPane checkPane = new StackPane(new IconRegion("cross"));
            checkPane.getStyleClass().add("option-check");

            toggleButton = new ToggleButton(text, new IconRegion("check"));
            toggleButton.getStyleClass().add("option-toggle");
            toggleButton.setMouseTransparent(true);
            if (!control.isMultiSelect()) {
                toggleButton.setToggleGroup(toggleGroup);
            }
            textProperty.bind(toggleButton.textProperty());

            getChildren().addAll(checkPane, toggleButton);
            getStyleClass().add("option-toggle-box");

            setOnMouseClicked(_ -> {
                if (control.isExcluding() && excludingToggle.isSelected()) {
                    setExcluded(!isExcluded());
                }
                setSelected(!isSelected());
            });
        }

        // selectedProperty
        private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected") {
            @Override
            protected void invalidated() {
                toggleButton.setSelected(get());
            }
        };
        public final BooleanProperty selectedProperty() {
           return selectedProperty;
        }
        public final boolean isSelected() {
           return selectedProperty.get();
        }
        public final void setSelected(boolean value) {
            selectedProperty.set(value);
        }

        // textProperty
        private final ReadOnlyStringWrapper textProperty = new ReadOnlyStringWrapper(this, "text");
        public final ReadOnlyStringProperty textProperty() {
           return textProperty.getReadOnlyProperty();
        }
        public final String getText() {
           return textProperty.get();
        }

        // excludedProperty
        private final BooleanProperty excludedProperty = new SimpleBooleanProperty(this, "excluded") {
            @Override
            protected void invalidated() {
                pseudoClassStateChanged(EXCLUDED_OPTION, get());
                toggleButton.setDisable(get());
            }
        };
        public final BooleanProperty excludedProperty() {
           return excludedProperty;
        }
        public final boolean isExcluded() {
           return excludedProperty.get();
        }
        public final void setExcluded(boolean value) {
            excludedProperty.set(value);
        }

    }
}
