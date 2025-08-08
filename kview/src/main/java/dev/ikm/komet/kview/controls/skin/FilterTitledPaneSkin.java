package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class FilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass EXCLUDING_TITLED_PANE = PseudoClass.getPseudoClass("excluding");
    private static final PseudoClass EXCLUDED_OPTION = PseudoClass.getPseudoClass("excluded");
    private static final PseudoClass SINGLE_SELECT_OPTION = PseudoClass.getPseudoClass("single-select");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region titleRegion;
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
    private ScrollPane scrollPane;
    private Subscription subscription;

    public FilterTitledPaneSkin(FilterTitledPane control) {
        super(control);
        this.control = control;

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.setMaxWidth(240);
        selectedOption.getStyleClass().add("option");

        allToggle = new ToggleButton(resources.getString("titled.pane.option.all"), new IconRegion("check"));
        allToggle.getStyleClass().add("all-toggle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        excludingToggle = new ToggleButton(resources.getString("titled.pane.option.excluding"), new IconRegion("check"));
        excludingToggle.getStyleClass().add("exclude-toggle");

        togglesBox = new HBox(allToggle, spacer, excludingToggle);
        togglesBox.getStyleClass().add("toggles-box");

        titleBox = new VBox(titleLabel, selectedOption, togglesBox);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        contentBox = new VBox(separatorRegion);
        contentBox.getStyleClass().add("content-box");

        control.setContent(contentBox);
        allToggle.disableProperty().bind(Bindings.size(contentBox.getChildren()).lessThanOrEqualTo(1));

        if (control.getParent() instanceof Accordion accordion) {
            Parent parent = accordion.getParent();
            while (!(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }

            scrollPane = (ScrollPane) parent;
        }

        setupTitledPane();
    }

    private void setupTitledPane() {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        boolean multiSelectionAllowed = control.getOption().isMultiSelectionAllowed();
        control.pseudoClassStateChanged(SINGLE_SELECT_OPTION, !multiSelectionAllowed);

        FilterOptions.Option currentOption = control.getOption().copy();
        selectedOption.setText(getOptionText(currentOption));

        // add toggles only once
        if (contentBox.getChildren().size() == 1) {
            control.getOption().availableOptions().forEach(text ->
                    contentBox.getChildren().add(new OptionToggle(text)));
        }

        subscription = selectedOption.boundsInParentProperty().subscribe(b ->
                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30));

        subscription = subscription.and(selectedOption.textProperty().subscribe(text -> {
            List<String> defaultOptions = currentOption.defaultOptions();
            if (defaultOptions.isEmpty()) {
                defaultOptions.add(currentOption.availableOptions().getFirst());
            }
            pseudoClassStateChanged(MODIFIED_TITLED_PANE, !text.isEmpty() && !text.equals(String.join(", ", defaultOptions)));
        }));

        if (control.getParent() instanceof Accordion accordion) {
            subscription = subscription.and(accordion.expandedPaneProperty().subscribe(pane -> {
                if (pane == null) {
                    scrollPane.setVvalue(scrollPane.getVmin());
                }
            }));
        }

        subscription = subscription.and(control.heightProperty().subscribe((_, _) -> {
            if (control.isExpanded()) {
                double accordionHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double minY = control.getBoundsInParent().getMinY();
                double maxY = control.getBoundsInParent().getMaxY() + titleRegion.getBoundsInParent().getHeight();
                double delta = Math.min(maxY - viewportHeight, minY);
                if (maxY > viewportHeight) {
                    scrollPane.setVvalue(scrollPane.getVmax() * delta / (accordionHeight - viewportHeight));
                }
            }
        }));

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
            excludingToggle.setDisable(currentOption.selectedOptions().size() != currentOption.availableOptions().size());
        }));
        subscription = subscription.and(excludingToggle.selectedProperty().subscribe((_, selected) -> {
            if (currentOption.isExcluding()) {
                singleSelection = true;
                if (selected) {
                    // when excluding toggle is selected, deselect all toggle
                    allToggle.setSelected(false);
                } else {
                    // when excluding toggle is deselected, select and include all toggles
                    getOptionToggles().forEach(tb -> {
                        tb.setExcluded(false);
                        tb.setSelected(true);
                    });
                    allToggle.setSelected(true);
                }
                singleSelection = false;
            }
        }));

        subscription = subscription.and(excludingToggle.disableProperty().subscribe((_, disabled) -> {
            if (disabled) {
                // when disabled, remove check from toggle
                excludingToggle.setSelected(false);
            }
        }));

        allToggle.setSelected(currentOption.selectedOptions().size() == currentOption.availableOptions().size() &&
                (!currentOption.isExcluding() || currentOption.excludedOptions().isEmpty()) && multiSelectionAllowed);
        excludingToggle.setSelected(currentOption.isExcluding() && !currentOption.excludedOptions().isEmpty());
        excludingToggle.setDisable(currentOption.selectedOptions().size() != currentOption.availableOptions().size());
        togglesBox.pseudoClassStateChanged(EXCLUDING_TITLED_PANE, currentOption.isExcluding());

        getOptionToggles().forEach(tb -> {
            subscription = subscription.and(tb.selectedProperty().subscribe((_, selected) -> {
                if (!allSelection && multiSelectionAllowed) {
                    singleSelection = true;
                    // select All if all the toggles are selected
                    allToggle.setSelected(getOptionToggles().allMatch(OptionToggle::isSelected) &&
                            !excludingToggle.isSelected());
                    singleSelection = false;
                }
                if (selected && !currentOption.selectedOptions().contains(tb.getText())) {
                    if (multiSelectionAllowed) {
                        addAndSort(currentOption.selectedOptions(), tb.getText());
                    } else {
                        currentOption.selectedOptions().clear();
                        currentOption.selectedOptions().add(tb.getText());
                    }
                } else if (!selected && !(currentOption.isExcluding() && excludingToggle.isSelected())) {
                    currentOption.selectedOptions().remove(tb.getText());
                }
                excludingToggle.setDisable(currentOption.selectedOptions().size() != currentOption.availableOptions().size());
            }));
            subscription = subscription.and(tb.excludedProperty().subscribe((_, excluded) -> {
                if (currentOption.isExcluding()) {
                    if (excluded && !currentOption.excludedOptions().contains(tb.getText())) {
                        addAndSort(currentOption.excludedOptions(), tb.getText());
                    } else if (!excluded) {
                        currentOption.excludedOptions().remove(tb.getText());
                    }
                }
            }));
            tb.setExcluded(currentOption.isExcluding() && currentOption.excludedOptions().contains(tb.getText()));
            tb.setSelected(currentOption.selectedOptions().contains(tb.getText()) && !tb.isExcluded());
        });

        // confirm changes, and set again titledPane
        subscription = subscription.and(control.optionProperty().subscribe((_, _) -> setupTitledPane()));
        subscription = subscription.and(control.expandedProperty().subscribe((_, expanded) -> {
            if (!expanded) {
                control.setOption(currentOption.copy());
                selectedOption.setText(getOptionText(currentOption));
            }
        }));

    }

    @Override
    public void dispose() {
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

    private String getOptionText(FilterOptions.Option option) {
        if (option == null) {
            return null;
        }
        if (option.excludedOptions() != null && !option.excludedOptions().isEmpty()) {
            return MessageFormat.format(resources.getString("titled.pane.option.exclude"),
                    String.join(", ", option.excludedOptions()));
        } else {
            if (option.selectedOptions().size() == option.availableOptions().size()) {
                return resources.getString("titled.pane.option.all");
            } else if (option.selectedOptions().isEmpty()) {
                return resources.getString("titled.pane.option.none");
            } else {
                return String.join(", ", option.selectedOptions());
            }
        }
    }

    private void addAndSort(List<String> list, String value) {
        list.add(value);
        list.sort(Comparator.naturalOrder());
    }

    private class OptionToggle extends HBox {

        private final ToggleButton toggleButton;

        public OptionToggle(String text) {
            StackPane checkPane = new StackPane(new IconRegion("cross"));
            checkPane.getStyleClass().add("option-check");

            toggleButton = new ToggleButton(text, new IconRegion("check"));
            toggleButton.getStyleClass().add("option-toggle");
            toggleButton.setMouseTransparent(true);
            if (!control.getOption().isMultiSelectionAllowed()) {
                toggleButton.setToggleGroup(toggleGroup);
                toggleButton.selectedProperty().subscribe(selected -> {
                    if (!selected) {
                        setSelected(false);
                    }
                });
            }
            textProperty.bind(toggleButton.textProperty());

            getChildren().addAll(checkPane, toggleButton);
            getStyleClass().add("option-toggle-box");

            setOnMouseClicked(e -> {
                if (control.getOption().isExcluding() && excludingToggle.isSelected()) {
                    setExcluded(!isExcluded());
                }
                if (control.getOption().isMultiSelectionAllowed() || !isSelected()) {
                    setSelected(!isSelected());
                }
                e.consume();
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
