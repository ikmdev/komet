package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.DateRange;
import dev.ikm.komet.kview.controls.RangeCalendarControl;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.DateCellSkin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static dev.ikm.komet.kview.controls.RangeCalendarControl.DATE_FORMATTER;
import static dev.ikm.komet.kview.controls.RangeCalendarControl.DEFAULT_DATE_PATTERN;

/**
 * Default skin implementation for the {@link RangeCalendarControl} control
 */
public class RangeCalendarSkin implements Skin<RangeCalendarControl> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.range-calendar");

    private static final List<String> DATE_PATTERN_LIST = List.of(DEFAULT_DATE_PATTERN, "M/d/yyyy", "MMddyyyy",
            "MM.dd.yyyy", "M.d.yyyy", "MM-dd-yyyy", "M-d-yyyy");
    private static final String CAN_ADD_NEW_RANGE_KEY = "CAN_ADD_NEW_RANGE";

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass EXCLUDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("excluded");
    private static final PseudoClass START_RANGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("start");
    private static final PseudoClass IN_RANGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("intermediate");
    private static final PseudoClass END_RANGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("end");

    private final DatePicker datePicker;
    private final RangeCalendarControl control;
    private final VBox root;
    private final Node popupContent;
    private final Label monthYearLabel;

    private Subscription subscription;
    private Subscription calendarSubscription;

    private int currentIndex;
    private boolean currentExcluding;
    private boolean swapping;
    private final BooleanProperty canAddNewRange = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            control.getProperties().put(CAN_ADD_NEW_RANGE_KEY, get());
        }
    };

    /**
     * Creates a new CalendarSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public RangeCalendarSkin(RangeCalendarControl control) {
        this.control = control;

        datePicker = new DatePicker();
        datePicker.setDayCellFactory(_ -> new DateCell() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new CalendarCellSkin(this);
            }

            private static class CalendarCellSkin extends DateCellSkin {

                private final Region circle;

                public CalendarCellSkin(DateCell cell) {
                    super(cell);

                    circle = new Region();
                    circle.getStyleClass().add("calendar-cell-circle");

                    getChildren().addFirst(circle);
                }

                @Override
                protected void layoutChildren(double x, double y, double w, double h) {
                    super.layoutChildren(x, y, w, h);
                    if (!getChildren().contains(circle)) {
                        getChildren().addFirst(circle);
                    }
                    circle.resizeRelocate(x - snappedLeftInset(), y - snappedTopInset(),
                            circle.prefWidth(h), circle.prefHeight(w));
                }
            }
        });

        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        datePicker.setShowWeekNumbers(false);
        popupContent = datePickerSkin.getPopupContent();
        popupContent.getStyleClass().add("calendar-node");
        monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add("month-year-label");
        subscription = monthYearLabel.textProperty().subscribe(_ -> updateSelection());

        subscription = subscription.and(control.sceneProperty().subscribe(s -> {
            if (s != null) {
                if (popupContent.lookup(".month-year-pane") instanceof BorderPane bp) {
                    HBox monthSpinner = (HBox) bp.getLeft();
                    HBox yearSpinner = (HBox) bp.getRight();
                    bp.setCenter(adaptMonthYearPane(monthSpinner, yearSpinner));
                    monthSpinner.setManaged(false);
                    monthSpinner.setVisible(false);
                    yearSpinner.setManaged(false);
                    yearSpinner.setVisible(false);
                }
                popupContent.lookupAll(".day-name-cell").stream()
                        .filter(DateCell.class::isInstance)
                        .map(DateCell.class::cast)
                        .forEach(l -> l.textProperty().subscribe(t -> {
                            if (t.length() == 3) {
                                l.setText(t.substring(0, t.startsWith("T") ? 2 : 1)); // UK/US only
                            }
                        }));
            }
        }));
        subscription = subscription.and(datePicker.valueProperty().subscribe((_, v) -> {
            if (v == null) {
                return;
            }
            if (control.getMode() == RangeCalendarControl.MODE.DATE) {
                control.setDate(v);
            }
            updateSelection();
        }));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox calendarBox = new VBox(popupContent);
        calendarBox.getStyleClass().add("calendar-box");

        root = new VBox(calendarBox);
        root.getStyleClass().add("range-calendar");
        root.getStylesheets().add(RangeCalendarControl.class.getResource("range-calendar.css").toExternalForm());

        subscription = subscription.and(control.modeProperty().subscribe(m -> {
            if (calendarSubscription != null) {
                calendarSubscription.unsubscribe();
            }
            root.getChildren().removeIf(HBox.class::isInstance);
            calendarSubscription = Subscription.EMPTY;
            if (m == RangeCalendarControl.MODE.DATE) {
                createDateFields();
            } else {
                calendarSubscription = calendarSubscription.and(control.dateRangeList().subscribe(this::updateSelection));
                createDateRangeFields(0, false);
            }
        }));

        canAddNewRange.bind(Bindings.createBooleanBinding(() -> {
            if (control.getMode() == RangeCalendarControl.MODE.DATE) {
                return false;
            }
            return control.dateRangeList().size() == root.getChildren().stream().filter(HBox.class::isInstance).count();
        }, control.dateRangeList(), control.modeProperty(), root.getChildren()));
    }

    /** {@inheritDoc} */
    @Override
    public RangeCalendarControl getSkinnable() {
        return control;
    }

    /** {@inheritDoc} */
    @Override
    public Node getNode() {
        return root;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        if (calendarSubscription != null) {
            calendarSubscription.unsubscribe();
        }
    }

    private HBox adaptMonthYearPane(HBox monthSpinner, HBox yearSpinner) {
        Button backYearButton = (Button) yearSpinner.getChildren().getFirst();
        backYearButton.getGraphic().getStyleClass().setAll("most-left-arrow-calendar");
        Label monthLabel = (Label) monthSpinner.getChildren().get(1);
        Label yearLabel = (Label) yearSpinner.getChildren().get(1);
        monthYearLabel.textProperty().bind(Bindings.format("%s %s", monthLabel.textProperty(), yearLabel.textProperty()));
        Button forwardYearButton = (Button) yearSpinner.getChildren().getLast();
        forwardYearButton.getGraphic().getStyleClass().setAll("most-right-arrow-calendar");

        Button backMonthButton = (Button) monthSpinner.getChildren().getFirst();
        backMonthButton.getGraphic().getStyleClass().setAll("left-arrow-calendar");
        Button forwardMonthButton = (Button) monthSpinner.getChildren().getLast();
        forwardMonthButton.getGraphic().getStyleClass().setAll("right-arrow-calendar");
        HBox yearMonthSpinner = new HBox(backYearButton, backMonthButton,
                monthYearLabel,
                forwardMonthButton, forwardYearButton);
        yearMonthSpinner.getStyleClass().add("spinner");
        return yearMonthSpinner;
    }

    private void createDateFields() {
        currentIndex = 0;
        currentExcluding = false;
        Label dateLabel = new Label(resources.getString("date.label"));
        dateLabel.getStyleClass().add("date-label");
        DateTextField dateField = new DateTextField(false);
        HBox.setHgrow(dateField, Priority.ALWAYS);
        HBox dateBox = new HBox(dateLabel, dateField);
        dateBox.getStyleClass().add("date-box");
        control.dateProperty().bindBidirectional(dateField.dateProperty());
        root.getChildren().addFirst(dateBox);
    }

    public void createDateRangeFields(boolean excluding) {
        int index = control.dateRangeList().size();
        createDateRangeFields(index, excluding);
    }

    private void createDateRangeFields(int index, boolean excluding) {
        currentIndex = index;
        currentExcluding = excluding;

        Label dateFromLabel = new Label(resources.getString("range.from.label"));
        dateFromLabel.getStyleClass().addAll("date-label", "range");
        DateTextField dateFromField = new DateTextField(true);
        Label dateToLabel = new Label(resources.getString("range.to.label"));
        dateToLabel.getStyleClass().addAll("date-label", "range");
        DateTextField dateToField = new DateTextField(true);
        HBox dateBox = new HBox(dateFromLabel, dateFromField, dateToLabel, dateToField);
        dateBox.getStyleClass().add("date-box");
        if (excluding) {
            dateBox.getStyleClass().add("excluding");
        }
        final ObjectProperty<DateRange> currentDateRange = new SimpleObjectProperty<>();
        currentDateRange.bind(Bindings.createObjectBinding(() -> {
            if (swapping) {
                return null;
            }
            LocalDate fromDate = dateFromField.getDate();
            LocalDate toDate = dateToField.getDate();
            if (toDate != null) {
                datePicker.setValue(toDate);
                if (fromDate != null && fromDate.isAfter(toDate)) {
                    swapping = true;
                    dateFromField.setDate(toDate);
                    swapping = false;
                    dateToField.setDate(fromDate);
                }
            }
            return new DateRange(index, fromDate, toDate, excluding);
        }, dateFromField.dateProperty(), dateToField.dateProperty()));

        calendarSubscription = calendarSubscription.and(currentDateRange.subscribe((_, dr) -> {
            if (dr != null && dr.isValid()) {
                ObservableList<DateRange> ranges = control.dateRangeList();
                if (ranges.size() <= dr.index()) {
                    ranges.add(dr);
                } else {
                    ranges.set(dr.index(), dr);
                }
            }
        }));
        calendarSubscription = calendarSubscription.and(datePicker.valueProperty().subscribe((o, v) -> {
            if (index == currentIndex && !swapping && v != null) {
                if (control.dateRangeList().stream().anyMatch(dr -> dr.contains(v))) {
                    swapping = true;
                    datePicker.setValue(o);
                    swapping = false;
                    return;
                }
                if (dateFromField.getDate() == null) {
                    dateFromField.setDate(v);
                } else {
                    // lock when range is set. Changes to this range can only be made from the field editors only
                    popupContent.lookup(".calendar-grid").setMouseTransparent(true);
                    dateToField.setDate(v);
                }
            }
        }));
        root.getChildren().add(index, dateBox);
        // unlock
        popupContent.lookup(".calendar-grid").setMouseTransparent(false);
        updateSelection();
    }

    private void updateSelection() {
        LocalDate selectedDate = datePicker.getValue();
        List<DateCell> cells = popupContent.lookupAll(".day-cell").stream()
                .filter(DateCell.class::isInstance)
                .map(DateCell.class::cast)
                .toList();
        cells.forEach(d -> {
            d.pseudoClassStateChanged(EXCLUDED_PSEUDO_CLASS, currentExcluding);
            d.pseudoClassStateChanged(START_RANGE_PSEUDO_CLASS, false);
            d.pseudoClassStateChanged(IN_RANGE_PSEUDO_CLASS, false);
            d.pseudoClassStateChanged(END_RANGE_PSEUDO_CLASS, false);
            d.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
        });
        cells.forEach(d -> {
            if (selectedDate != null && selectedDate.isEqual(d.getItem())) {
                d.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
            }
        });

        ObservableList<DateRange> ranges = control.dateRangeList();
        for (DateRange dateRange : ranges) {
            cells.forEach(d -> {
                LocalDate item = d.getItem();
                if (dateRange.startDate().isEqual(item)) {
                    d.pseudoClassStateChanged(START_RANGE_PSEUDO_CLASS, true);
                    d.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
                    d.pseudoClassStateChanged(EXCLUDED_PSEUDO_CLASS, dateRange.exclude());
                }
                if (dateRange.inRange(item)) {
                    d.pseudoClassStateChanged(IN_RANGE_PSEUDO_CLASS, true);
                    d.pseudoClassStateChanged(EXCLUDED_PSEUDO_CLASS, dateRange.exclude());
                }
                if (dateRange.endDate().isEqual(item)) {
                    d.pseudoClassStateChanged(END_RANGE_PSEUDO_CLASS, true);
                    d.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
                    d.pseudoClassStateChanged(EXCLUDED_PSEUDO_CLASS, dateRange.exclude());
                }
            });
        }
    }

    private class DateTextField extends TextField {

        public DateTextField(boolean isRange) {
            setPromptText(resources.getString(isRange ? "range.prompt" : "date.prompt"));
            getStyleClass().add("date-field");
            if (isRange) {
                getStyleClass().add("range");
            }
            setOnAction(_ -> processUserInput());
            focusedProperty().subscribe(f -> {
                if (!f) {
                    processUserInput();
                }
            });
        }

        // dateProperty
        private final ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>(this, "date") {
            @Override
            protected void invalidated() {
                LocalDate localDate = get();
                pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, localDate != null);
                setText(localDate == null ? null : DATE_FORMATTER.format(localDate));
            }
        };
        public final ObjectProperty<LocalDate> dateProperty() {
           return dateProperty;
        }
        public final LocalDate getDate() {
           return dateProperty.get();
        }
        public final void setDate(LocalDate value) {
            dateProperty.set(value);
        }

        private void processUserInput() {
            parseDate(getText()).ifPresentOrElse(v -> {
                setDate(v);
                swapping = true;
                datePicker.setValue(v);
                swapping = false;
            }, () -> {
                // if new text can't be parsed as a date, restore existing previous date, if any
                if (getText() != null && !getText().isEmpty() && getDate() != null) {
                    setText(DATE_FORMATTER.format(getDate()));
                } else {
                    setText(null);
                }
            });
        }

        private Optional<LocalDate> parseDate(String date) {
            if (date != null && !date.isEmpty()) {
                for (String pattern : DATE_PATTERN_LIST) {
                    try {
                        return Optional.of(LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern)));
                    } catch (DateTimeParseException dtpe) {
                        // Ignore
                    }
                }
            }
            return Optional.empty();
        }

    }
}
