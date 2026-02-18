package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.EditCoordinateOptions;
import dev.ikm.komet.kview.controls.EditCoordinateOptionsUtils;
import dev.ikm.komet.kview.controls.EditCoordinateTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.util.*;
import java.util.stream.*;

public class EditCoordinateTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.edit-coordinate-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass EXCLUDING_BUTTON_TITLED_PANE = PseudoClass.getPseudoClass("excluding");
    private static final PseudoClass ANY_BUTTON_TITLED_PANE = PseudoClass.getPseudoClass("any");
    private static final PseudoClass EXCLUDED_OPTION = PseudoClass.getPseudoClass("excluded");
    private static final PseudoClass SINGLE_SELECT_OPTION = PseudoClass.getPseudoClass("single-select");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region titleRegion;
    private final Region arrow;
    private final VBox titleBox;
    private final TruncatedTextFlow selectedOption;
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ToggleButton allToggle;
    private final ToggleButton anyToggle;
    private final ToggleGroup buttonToggleGroup;
    private final ToggleButton excludingToggle;
    private final HBox togglesBox;
    private final VBox contentBox;
    private final EditCoordinateTitledPane control;
    private boolean allSelection = false, singleSelection = false;
    private ScrollPane scrollPane;
    private Subscription subscription;

    public EditCoordinateTitledPaneSkin(EditCoordinateTitledPane control) {
        super(control);
        this.control = control;

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.getStyleClass().add("option");

        buttonToggleGroup = new ToggleGroup();
        allToggle = new ToggleButton(null, new IconRegion("check"));
        allToggle.getStyleClass().add("all-toggle");
        allToggle.setToggleGroup(toggleGroup);

        anyToggle = new ToggleButton(null, new IconRegion("check"));
        anyToggle.getStyleClass().add("any-toggle");
        anyToggle.setToggleGroup(buttonToggleGroup);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        excludingToggle = new ToggleButton(null, new IconRegion("check"));
        excludingToggle.getStyleClass().add("exclude-toggle");

        togglesBox = new HBox(allToggle, anyToggle, spacer, excludingToggle);
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

        Parent parent = control.getParent();
        while (!(parent instanceof ScrollPane)) {
            parent = parent.getParent();
        }

        scrollPane = (ScrollPane) parent;

        setupTitledPane();
    }

    private void setupTitledPane() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Subscription.EMPTY;

        EditCoordinateOptions.Option option = control.getOption();
        if (option == null) {
            return;
        }
        boolean multiSelectionAllowed = option.isMultiSelectionAllowed();
        control.pseudoClassStateChanged(SINGLE_SELECT_OPTION, !multiSelectionAllowed);

        EditCoordinateOptions.Option currentOption = option.copy();
        // whenever the navigator changes, update the option text
        subscription = subscription.and(control.navigatorProperty().subscribe(_ ->
                selectedOption.setText(getOptionText(currentOption))));

        // add toggles only once
        if (contentBox.getChildren().size() == 1) {
            option.availableOptions().forEach(o ->
                    contentBox.getChildren().add(new OptionToggle<>(o)));
        }
        setupToggleBox(currentOption);

        subscription = subscription.and(selectedOption.boundsInParentProperty().subscribe(b ->
                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30)));

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

        if (currentOption.hasAll()) {
            // if user clicks on All, toggles on/off all toggles:
            subscription = subscription.and(allToggle.selectedProperty().subscribe((_, selected) -> {
                if (!singleSelection) {
                    allSelection = true;
                    getOptionToggles().forEach(tb -> tb.setSelected(selected));
                    if (selected) {
                        getOptionToggles().forEach(tb -> tb.setExcluded(false));
                        excludingToggle.setSelected(false);
                        anyToggle.setSelected(false);
                    } else if (currentOption.hasAny() && !anyToggle.isSelected() &&
                            (getOptionToggles().allMatch(OptionToggle::isSelected) || getOptionToggles().noneMatch(OptionToggle::isSelected))) {
                        // when Select All is deselected, select Any, if all toggles or none are selected
                        anyToggle.setSelected(true);
                    }
                    allSelection = false;
                }
                excludingToggle.setDisable(!(currentOption.areAllSelected() || anyToggle.isSelected()));
            }));
        }

        if (currentOption.hasAny()) {
            // if user clicks on Any, disable/enable all toggles:
            subscription = subscription.and(anyToggle.selectedProperty().subscribe((_, selected) -> {
                currentOption.setAny(selected);
                getOptionToggles().forEach(tb -> tb.setDisable(selected));
                if (selected) {
                    getOptionToggles().forEach(tb -> tb.setExcluded(false));
                    excludingToggle.setSelected(false);
                    singleSelection = true;
                    allToggle.setSelected(false);
                    singleSelection = false;
                } else {
                    if (getOptionToggles().allMatch(OptionToggle::isSelected)) {
                        singleSelection = true;
                        allToggle.setSelected(true);
                        singleSelection = false;
                    } else if (getOptionToggles().noneMatch(OptionToggle::isSelected)) {
                        // don't allow Any being deselected, and all toggles are deselected
                        anyToggle.setSelected(true);
                    }
                }
                excludingToggle.setDisable(!(currentOption.areAllSelected() || anyToggle.isSelected()));
            }));
        }

        if (currentOption.hasExcluding()) {
            subscription = subscription.and(excludingToggle.selectedProperty().subscribe(selected -> {
                singleSelection = true;
                if (selected) {
                    // when excluding toggle is selected, enable all toggles and
                    // disable All/Any toggles
                    getOptionToggles().forEach(tb -> {
                        tb.setDisable(false);
                        tb.setSelected(true);
                    });
                    allToggle.setDisable(true);
                    anyToggle.setDisable(true);
                } else {
                    // when excluding toggle is deselected, select and include all toggles
                    // disabling them if Any was selected
                    getOptionToggles().forEach(tb -> {
                        tb.setExcluded(false);
                        tb.setSelected(true);
                        tb.setDisable(anyToggle.isSelected());
                    });
                    // enable All/Any toggles
                    allToggle.setDisable(false);
                    anyToggle.setDisable(false);
                    if (!allToggle.isSelected() && !anyToggle.isSelected()) {
                        allToggle.setSelected(true);
                    }
                }
                singleSelection = false;
            }));
        }

        subscription = subscription.and(excludingToggle.disableProperty().subscribe((_, disabled) -> {
            if (disabled) {
                // when disabled, remove check from toggle
                excludingToggle.setSelected(false);
            }
        }));

        if (currentOption.hasAny()) {
            if (currentOption.any()) {
                allToggle.setSelected(false);
                anyToggle.setSelected(true);
            } else {
                anyToggle.setSelected(false);
                singleSelection = true;
                allToggle.setSelected(currentOption.areAllSelected());
                singleSelection = false;
            }
        } else {
            allToggle.setSelected(currentOption.areAllSelected() && !currentOption.hasExclusions() && multiSelectionAllowed);
        }
        excludingToggle.setSelected(currentOption.hasExclusions());
        excludingToggle.setDisable(!(currentOption.areAllSelected() || anyToggle.isSelected()));
        togglesBox.pseudoClassStateChanged(EXCLUDING_BUTTON_TITLED_PANE, currentOption.hasExcluding());
        togglesBox.pseudoClassStateChanged(ANY_BUTTON_TITLED_PANE, currentOption.hasAny());

        getOptionToggles().forEach(tb -> {
            subscription = subscription.and(tb.selectedProperty().subscribe((_, selected) -> {
                if (!allSelection && multiSelectionAllowed) {
                    singleSelection = true;
                    // select All if all the toggles are selected
                    allToggle.setSelected(getOptionToggles().allMatch(OptionToggle::isSelected) &&
                            !excludingToggle.isSelected() && (!currentOption.hasAny() || !anyToggle.isSelected()));
                    if (allToggle.isSelected()) {
                        anyToggle.setSelected(false);
                    } else if (currentOption.hasAny() && !anyToggle.isSelected() && getOptionToggles().noneMatch(OptionToggle::isSelected)) {
                        // select Any if all the toggles are deselected
                        anyToggle.setSelected(true);
                    }
                    singleSelection = false;
                }
                if (selected && !currentOption.selectedOptions().contains(tb.getT())) {
                    if (multiSelectionAllowed) {
                        currentOption.selectedOptions().add(tb.getT());
                        currentOption.selectedOptions().sort(Comparator.comparing(this::getDescription));
                    } else {
                        currentOption.selectedOptions().clear();
                        currentOption.selectedOptions().add(tb.getT());
                    }
                } else if (!selected && !(currentOption.hasExcluding() && excludingToggle.isSelected())) {
                    currentOption.selectedOptions().remove(tb.getT());
                }
                excludingToggle.setDisable(!(currentOption.areAllSelected() || anyToggle.isSelected()));
            }));
            subscription = subscription.and(tb.excludedProperty().subscribe((_, excluded) -> {
                if (currentOption.hasExcluding()) {
                    if (excluded && !currentOption.excludedOptions().contains(tb.getT())) {
                        currentOption.excludedOptions().add(tb.getT());
                        currentOption.excludedOptions().sort(Comparator.comparing(this::getDescription));
                    } else if (!excluded) {
                        currentOption.excludedOptions().remove(tb.getT());
                    }
                }
            }));
            tb.setExcluded(currentOption.hasExcluding() && currentOption.excludedOptions().contains(tb.getT()));
            tb.setSelected(currentOption.selectedOptions().contains(tb.getT()) && !tb.isExcluded());
        });

        // confirm changes, and set again titledPane
        subscription = subscription.and(control.optionProperty().subscribe((_, _) -> setupTitledPane()));
        subscription = subscription.and(control.expandedProperty().subscribe((_, expanded) -> {
            if (!expanded) {
                updateModifiedState(currentOption);
                control.setOption(currentOption.copy());
                selectedOption.setText(getOptionText(currentOption));
            }
        }));
        updateModifiedState(currentOption);
    }

    private <T> void updateModifiedState(EditCoordinateOptions.Option<T> currentOption) {
        boolean modified = !Objects.equals(currentOption, control.getDefaultOption());
        pseudoClassStateChanged(MODIFIED_TITLED_PANE, currentOption.isInOverride() || modified);
        if (modified && !currentOption.isInOverride()) {
            currentOption.setInOverride(true);
        }
    }

    private <T> void setupToggleBox(EditCoordinateOptions.Option<T> currentOption) {
        String name = currentOption.item().getName();
        boolean empty = contentBox.getChildren().size() <= 1;
        allToggle.setDisable(empty);
        if (currentOption.hasAll()) {
            allToggle.setText(resources.getString(name + EditCoordinateOptions.Option.BUTTON.ALL.getLabel()));
        }
        anyToggle.setDisable(empty);
        if (currentOption.hasAny()) {
            anyToggle.setText(resources.getString(name + EditCoordinateOptions.Option.BUTTON.ANY.getLabel()));
        }
        excludingToggle.setDisable(empty);
        if (currentOption.hasExcluding()) {
            excludingToggle.setText(resources.getString(name + EditCoordinateOptions.Option.BUTTON.EXCLUDING.getLabel()));
        }
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

    private Stream<OptionToggle> getOptionToggles() {
        return contentBox.getChildren().stream()
                .filter(OptionToggle.class::isInstance)
                .map(OptionToggle.class::cast);
    }

    private <T> String getDescription(T t) {
        return EditCoordinateOptionsUtils.getDescription(control.getNavigator() == null ? null : control.getNavigator().getViewCalculator(), t);
    }

    private <T> String getOptionText(EditCoordinateOptions.Option<T> option) {
        if (option == null) {
            return null;
        }
        String name = option.item().getName();
        if (option.hasExclusions()) {
            String any = resources.getString(name + ".label." + (option.hasAny() && option.any() ? "any" : "all"));

            return MessageFormat.format(resources.getString(name + ".label.exclude"),
                    any, String.join(", ", option.excludedOptions().stream().map(this::getDescription).toList()));
        } else {
            if (option.areAllSelected() || (option.hasAny() && option.any())) {
                return resources.getString(name + ".label." + (option.hasAny() && option.any() ? "any" : "all"));
            }
            if (option.selectedOptions().isEmpty()) {
                return resources.getString(name + ".label.none");
            }
            return String.join(", ", option.selectedOptions().stream().map(this::getDescription).toList());
        }
    }

    private class OptionToggle<T> extends HBox {

        private final ToggleButton toggleButton;

        public OptionToggle(T t) {
            StackPane checkPane = new StackPane(new IconRegion("cross"));
            checkPane.getStyleClass().add("option-check");

            toggleButton = new ToggleButton(getDescription(t), new IconRegion("check"));
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
            tProperty.set(t);

            getChildren().addAll(checkPane, toggleButton);
            getStyleClass().add("option-toggle-box");

            setOnMouseClicked(e -> {
                if (control.getOption().hasExcluding() && excludingToggle.isSelected()) {
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

        // tProperty
        private final ReadOnlyObjectWrapper<T> tProperty = new ReadOnlyObjectWrapper<>(this, "t");
        public final ReadOnlyObjectProperty<T> tProperty() {
           return tProperty.getReadOnlyProperty();
        }
        public final T getT() {
           return tProperty.get();
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
