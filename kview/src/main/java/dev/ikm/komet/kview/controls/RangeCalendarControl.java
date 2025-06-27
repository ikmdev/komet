package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.RangeCalendarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>A control implementation that shows a calendar to the user,
 * in order to define a {@link #dateProperty() date}, if {@link MODE#DATE} is set,
 * or one or more {@link DateRange date ranges}, if {@link MODE#RANGE} is set.
 * </p>
 *
 */
public class RangeCalendarControl extends Control {

    public static final String DEFAULT_DATE_PATTERN = "MM/dd/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final String CAN_ADD_NEW_RANGE_KEY = "CAN_ADD_NEW_RANGE";

    public enum MODE {
        DATE,
        RANGE
    }

    private RangeCalendarSkin rangeCalendarSkin;

    /**
     * Creates a CalendarPopup
     */
    public RangeCalendarControl() {
        super();
        getStyleClass().add("range-calendar");

        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded() && CAN_ADD_NEW_RANGE_KEY.equals(change.getKey())) {
                canAddNewRangeProperty.set((Boolean) getProperties().get(CAN_ADD_NEW_RANGE_KEY));
                getProperties().remove(CAN_ADD_NEW_RANGE_KEY);
            }
        });
    }

    // modeProperty
    private final ObjectProperty<MODE> modeProperty = new SimpleObjectProperty<>(this, "mode", MODE.DATE);
    public final ObjectProperty<MODE> modeProperty() {
       return modeProperty;
    }
    public final MODE getMode() {
       return modeProperty.get();
    }
    public final void setMode(MODE value) {
        modeProperty.set(value);
    }

    // dateProperty
    private final ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>(this, "date");
    public final ObjectProperty<LocalDate> dateProperty() {
       return dateProperty;
    }
    public final LocalDate getDate() {
       return dateProperty.get();
    }
    public final void setDate(LocalDate value) {
        dateProperty.set(value);
    }

    // dateRangeListProperty
    private final ObservableList<DateRange> dateRangeList = FXCollections.observableArrayList();
    public final ObservableList<DateRange> dateRangeList() {
       return dateRangeList;
    }

    // canAddNewRangeProperty
    private final ReadOnlyBooleanWrapper canAddNewRangeProperty = new ReadOnlyBooleanWrapper(this, "canAddNewRange");
    public final ReadOnlyBooleanProperty canAddNewRangeProperty() {
       return canAddNewRangeProperty.getReadOnlyProperty();
    }
    public final boolean canAddNewRange() {
       return canAddNewRangeProperty.get();
    }

    public void addRange(boolean isExcluding) {
        if (rangeCalendarSkin != null) {
            rangeCalendarSkin.createDateRangeFields(isExcluding);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        rangeCalendarSkin = new RangeCalendarSkin(this);
        return rangeCalendarSkin;
    }

}
