package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.CalendarPopup;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.Subscription;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link CalendarPopup} control
 */
public class CalendarPopupSkin implements Skin<CalendarPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.instant-control");

    private final CalendarPopup control;
    private final BorderPane root;

    private final ObjectProperty<LocalDate> selectedLocalDateProperty;
    private final ObjectProperty<LocalTime> selectedLocalTimeProperty;
    private Subscription subscription;

    /**
     * Creates a new CalendarPopupSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public CalendarPopupSkin(CalendarPopup control) {
        this.control = control;

        selectedLocalDateProperty = new SimpleObjectProperty<>();
        selectedLocalTimeProperty = new SimpleObjectProperty<>();

        // left side
        DatePicker datePicker = new DatePicker(control.getLocalDate() != null ? control.getLocalDate() : LocalDate.now());
        subscription = datePicker.valueProperty().subscribe(selectedLocalDateProperty::set);
        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        datePicker.setShowWeekNumbers(false);
        final Node popupContent = datePickerSkin.getPopupContent();
        popupContent.getStyleClass().add("calendar-node");
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
                                l.setText(t.substring(0, 1));
                            }
                        }));
            }
        }));

        // center side
        Region line = new Region();
        line.getStyleClass().add("calendar-line");
        VBox.setVgrow(line, Priority.ALWAYS);

        VBox centerBox = new VBox(line);
        centerBox.getStyleClass().add("center-box");

        // right side
        LocalTime localTime = control.getLocalTime() != null ? control.getLocalTime() : LocalTime.now();

        TextField hourField = new TextField();
        hourField.getStyleClass().add("hour-field");
        hourField.setTextFormatter(new TextFormatter<>(new IntegerRangeStringConverter(0, 23), localTime.getHour()));

        Label colonLabel = new Label(":");
        colonLabel.getStyleClass().add("colon-label");

        TextField minuteField = new TextField();
        minuteField.getStyleClass().add("minute-field");
        minuteField.setTextFormatter(new TextFormatter<>(new IntegerRangeStringConverter(0, 59), localTime.getMinute()));

        selectedLocalTimeProperty.bind(Bindings.createObjectBinding(
                () -> LocalTime.of((int) hourField.getTextFormatter().getValue(), (int) minuteField.getTextFormatter().getValue()),
                hourField.getTextFormatter().valueProperty(), minuteField.getTextFormatter().valueProperty()));

        HBox timeBox = new HBox(hourField, colonLabel, minuteField);
        timeBox.getStyleClass().add("time-box");

        ZoneOffset zoneOffset = control.getZoneOffset() != null ? control.getZoneOffset() : OffsetDateTime.now().getOffset();

        Label zoneLabel = new Label("UTC" + (ZoneOffset.UTC.equals(zoneOffset) ? "" : zoneOffset.toString()));
        zoneLabel.getStyleClass().add("zone-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button applyButton = new Button(resources.getString("apply.button.text"));
        applyButton.getStyleClass().add("apply-button");
        applyButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectedLocalDateProperty.get().equals(control.getLocalDate()) && selectedLocalTimeProperty.get().equals(control.getLocalTime()),
                selectedLocalDateProperty, selectedLocalTimeProperty));
        applyButton.setOnAction(e -> {
            control.setLocalDate(selectedLocalDateProperty.get());
            control.setLocalTime(selectedLocalTimeProperty.get());
            control.setZoneOffset(zoneOffset);
            control.hide();
        });

        Button removeButton = new Button(resources.getString("remove.button.text"));
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> control.hide());
        removeButton.managedProperty().bind(removeButton.visibleProperty());
        removeButton.setVisible(control.getLocalDate() != null && control.getLocalTime() != null && control.getZoneOffset() != null);
        removeButton.setOnAction(e -> {
            control.setLocalDate(null);
            control.setLocalTime(null);
            control.setZoneOffset(null);
            control.hide();
        });
        Button cancelButton = new Button(resources.getString("cancel.button.text"));
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setOnAction(e -> control.hide());

        VBox buttonsBox = new VBox(applyButton, removeButton, cancelButton);
        buttonsBox.getStyleClass().add("buttons-box");

        VBox localTimeBox = new VBox(timeBox, zoneLabel, spacer, buttonsBox);
        localTimeBox.getStyleClass().add("calendar-time-box");

        root = new BorderPane(centerBox);
        root.setLeft(popupContent);
        root.setRight(localTimeBox);
        root.getStyleClass().add("calendar-popup");
        root.getStylesheets().add(CalendarPopup.class.getResource("calendar-popup.css").toExternalForm());
    }

    /** {@inheritDoc} */
    @Override
    public CalendarPopup getSkinnable() {
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
    }

    private HBox adaptMonthYearPane(HBox monthSpinner, HBox yearSpinner) {
        Button backYearButton = (Button) yearSpinner.getChildren().getFirst();
        backYearButton.getGraphic().getStyleClass().setAll("most-left-arrow-calendar");
        Label monthLabel = (Label) monthSpinner.getChildren().get(1);
        Label yearLabel = (Label) yearSpinner.getChildren().get(1);
        Label monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add("month-year-label");
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

    static class IntegerRangeStringConverter extends StringConverter<Integer> {

        private final int min;
        private final int max;

        public IntegerRangeStringConverter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString(Integer value) {
            return String.format("%02d", value);
        }

        @Override
        public Integer fromString(String value) {
            int integer = Integer.parseInt(value);
            if (integer > max || integer < min) {
                throw new IllegalArgumentException();
            }
            return integer;
        }

    }
}
