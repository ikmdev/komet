package dev.ikm.komet.kview.controls.skin;

import static dev.ikm.komet.kview.controls.RangeCalendarControl.DATE_FORMATTER;
import static dev.ikm.komet.kview.controls.RangeCalendarControl.DEFAULT_DATE_PATTERN;
import dev.ikm.komet.kview.controls.DateFilterTitledPane;
import dev.ikm.komet.kview.controls.DateRange;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.RangeCalendarControl;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DateFilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region arrow;
    private final VBox titleBox;
    private final TruncatedTextFlow selectedOption;
    private final ComboBox<String> comboBox;
    private final VBox contentBox;
    private final DateFilterTitledPane control;
    private Subscription subscription;
    private ScrollPane scrollPane;
    private RangeCalendarControl calendarControl;
    private FilterOptions.Option currentOption;

    public DateFilterTitledPaneSkin(DateFilterTitledPane control) {
        super(control);
        this.control = control;

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.setMaxWidth(240);
        selectedOption.getStyleClass().add("option");

        comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("date-combo-box");

        titleBox = new VBox(titleLabel, selectedOption, comboBox);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        Region titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        contentBox = new VBox();
        contentBox.getStyleClass().add("content-box");

        control.setContent(contentBox);

        titleRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!control.isExpanded()) {
                boolean active = !comboBox.isVisible() || comboBox.localToScene(comboBox.getBoundsInLocal()).contains(e.getSceneX(), e.getSceneY());
                control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, active);
                e.consume();
            }
        });

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

        currentOption = control.getOption().copy();
        selectedOption.setText(getOptionText(currentOption));

        // fill combobox only once
        if (comboBox.getItems().isEmpty()) {
            comboBox.setItems(FXCollections.observableArrayList(currentOption.availableOptions()));
            comboBox.getSelectionModel().select(0);
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
                if (!(pane instanceof DateFilterTitledPane)) {
                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
                }
            }));
        }

        subscription = subscription.and(control.heightProperty().subscribe(_ -> {
            if (control.isExpanded()) {
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        }));

        subscription = subscription.and(comboBox.showingProperty().subscribe((_, showing) -> {
            if (showing && !contentBox.getChildren().isEmpty() && !control.isExpanded()) {
                comboBox.hide();
                control.setExpanded(true);
            }
        }));

        subscription = subscription.and(comboBox.getSelectionModel().selectedIndexProperty().subscribe((_, value) -> {
            if (comboBox.isShowing()) {
                // reset
                currentOption = new FilterOptions().getDate();
            }

            if (value.intValue() == 0) {
                contentBox.getChildren().clear();
                calendarControl = null;
                if (comboBox.isShowing()) {
                    control.setExpanded(false);
                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
                    control.setMode(DateFilterTitledPane.MODE.LATEST);
                }
            } else {
                if (value.intValue() == 1) {
                    createSpecificDatePane(currentOption);
                } else {
                    createDateRangePane(currentOption);
                }
                if (comboBox.isShowing()) {
                    control.setExpanded(true);
                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
                }
            }
            selectedOption.setText(getOptionText(currentOption));
        }));

        // confirm changes
        subscription = subscription.and(control.expandedProperty().subscribe((_, expanded) -> {
            if (!expanded) {
                currentOption.selectedOptions().clear();
                currentOption.excludedOptions().clear();
                if (calendarControl != null && calendarControl.getDate() != null) {
                    currentOption.selectedOptions().add(DATE_FORMATTER.format(calendarControl.getDate()));
                } else if (calendarControl != null && !calendarControl.dateRangeList().isEmpty()) {
                    calendarControl.dateRangeList().stream()
                            .filter(r -> !r.exclude())
                            .forEach(dr -> currentOption.selectedOptions().add(dr.toString()));
                    calendarControl.dateRangeList().stream()
                            .filter(DateRange::exclude)
                            .forEach(dr -> currentOption.excludedOptions().add(dr.toString()));
                }
                control.setOption(currentOption.copy());
            }
        }));

        subscription = subscription.and(control.optionProperty().subscribe((_, _) -> setupTitledPane()));

        List<String> selectedOptions = control.getOption().selectedOptions();
        List<String> excludedOptions = control.getOption().excludedOptions();
        if (containsDateRange(selectedOptions) || containsDateRange(excludedOptions)) {
            control.setMode(DateFilterTitledPane.MODE.DATE_RANGE_LIST);
        } else if (containsDate(selectedOptions) && (excludedOptions == null || excludedOptions.isEmpty())) {
            control.setMode(DateFilterTitledPane.MODE.SINGLE_DATE);
        } else {
            control.setMode(DateFilterTitledPane.MODE.LATEST);
        }

        subscription = subscription.and(control.modeProperty().subscribe(mode ->
                comboBox.getSelectionModel().select(mode.ordinal())));
    }

    @Override
    public void dispose() {
        selectedOption.textProperty().unbind();
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    private void createSpecificDatePane(FilterOptions.Option option) {
        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        calendarControl = new RangeCalendarControl();
        contentBox.getChildren().setAll(separatorRegion, calendarControl);

        if (containsDate(option.selectedOptions())) {
            try {
                LocalDate date = LocalDate.parse(option.selectedOptions().getFirst(), DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
                calendarControl.setDate(date);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        control.setMode(DateFilterTitledPane.MODE.SINGLE_DATE);
    }

    private void createDateRangePane(FilterOptions.Option option) {
        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        calendarControl = new RangeCalendarControl();
        calendarControl.setMode(RangeCalendarControl.MODE.RANGE);
        Button additionButton = new Button(resources.getString("date.range.additional.button"));
        additionButton.getStyleClass().add("additional");
        additionButton.setOnAction(_ -> calendarControl.addRange(false));

        Button excludeButton = new Button(resources.getString("date.range.exclude.button"));
        excludeButton.getStyleClass().add("exclude");
        excludeButton.setOnAction(_ -> calendarControl.addRange(true));

        VBox bottomBox = new VBox(additionButton, excludeButton);
        bottomBox.getStyleClass().add("bottom-box");

        bottomBox.disableProperty().bind(calendarControl.canAddNewRangeProperty().not());
        contentBox.getChildren().setAll(separatorRegion, calendarControl, bottomBox);

        if (!option.selectedOptions().isEmpty() || !option.excludedOptions().isEmpty()) {
            try {
                List<DateRange> dateRanges = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger();
                dateRanges.addAll(option.selectedOptions().stream()
                        .map(s -> DateRange.of(counter.getAndIncrement(), s, false).orElse(null))
                        .filter(Objects::nonNull)
                        .toList());
                dateRanges.addAll(option.excludedOptions().stream()
                        .map(s -> DateRange.of(counter.getAndIncrement(), s, true).orElse(null))
                        .filter(Objects::nonNull)
                        .toList());
                calendarControl.dateRangeList().setAll(dateRanges);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        control.setMode(DateFilterTitledPane.MODE.DATE_RANGE_LIST);
    }

    private String getOptionText(FilterOptions.Option option) {
        if (option == null) {
            return null;
        }
        if (calendarControl != null && calendarControl.getDate() != null) {
            return MessageFormat.format(resources.getString("date.option.specific"),
                    DATE_FORMATTER.format(calendarControl.getDate()));
        } else if (calendarControl != null && !calendarControl.dateRangeList().isEmpty()) {
            String including = calendarControl.dateRangeList().stream()
                    .filter(r -> !r.exclude())
                    .map(dr -> MessageFormat.format(resources.getString("date.range.text"), dr.startDate(), dr.endDate()))
                    .collect(Collectors.joining(", "));
            String excluding = calendarControl.dateRangeList().stream()
                    .filter(DateRange::exclude)
                    .map(dr -> MessageFormat.format(resources.getString("date.range.text"), dr.startDate(), dr.endDate()))
                    .collect(Collectors.joining(", "));
            if (excluding.isEmpty()) {
                return MessageFormat.format(resources.getString("date.option.range"), including);
            } else {
                return MessageFormat.format(resources.getString("date.option.range.excluding"), including, excluding);
            }
        } else {
            return String.join(", ", option.defaultOptions());
        }
    }

    private boolean containsDate(List<String> options) {
        return options != null && options.size() == 1 && containsDate(options.getFirst());
    }

    private boolean containsDate(String option) {
        return option != null && !option.isEmpty() && !option.contains(":");
    }

    private boolean containsDateRange(List<String> options) {
        return options != null && !options.isEmpty() && options.stream().allMatch(o -> o.contains(":"));
    }

}
