package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.CalendarPopupSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * <p>A popup control implementation that shows a date picker and a
 * time picker to the user, in order to define a {@link #localDateProperty() date},
 * a {@link #localTimeProperty() time} and a {@link #zoneOffsetProperty() zone-offset}.
 * </p>
 * <p>This popup is meant to be launched from a {@link KLInstantControl}.
 * </p>
 *
 * @see KLInstantControl
 */
public class CalendarPopup extends PopupControl {

    /**
     * Creates a CalendarPopup
     */
    public CalendarPopup() {
        super();
        getStyleClass().add("calendar-popup");
        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        setAutoHide(false);
        addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (KeyCode.ESCAPE.equals(e.getCode())) {
                hide();
                e.consume();
            }
        });
    }

    /**
     * A property that holds the {@link LocalDate} that represents the selected instant in time
     */
    private final ObjectProperty<LocalDate> localDateProperty = new SimpleObjectProperty<>(this, "localDate");
    public final ObjectProperty<LocalDate> localDateProperty() {
        return localDateProperty;
    }
    public final LocalDate getLocalDate() {
        return localDateProperty.get();
    }
    public final void setLocalDate(LocalDate value) {
        localDateProperty.set(value);
    }

    /**
     * A property that holds the {@link LocalTime} that represents the selected instant in time
     */
    private final ObjectProperty<LocalTime> localTimeProperty = new SimpleObjectProperty<>(this, "localTime");
    public final ObjectProperty<LocalTime> localTimeProperty() {
        return localTimeProperty;
    }
    public final LocalTime getLocalTime() {
        return localTimeProperty.get();
    }
    public final void setLocalTime(LocalTime value) {
        localTimeProperty.set(value);
    }

    /**
     * A property wiht a {@link ZoneOffset} that defines the time-zone offset from Greenwich/ UTC
     */
    private final ObjectProperty<ZoneOffset> zoneOffsetProperty = new SimpleObjectProperty<>(this, "zoneOffset", ZoneOffset.UTC);
    public final ObjectProperty<ZoneOffset> zoneOffsetProperty() {
        return zoneOffsetProperty;
    }
    public final ZoneOffset getZoneOffset() {
        return zoneOffsetProperty.get();
    }
    public final void setZoneOffset(ZoneOffset value) {
        zoneOffsetProperty.set(value);
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarPopupSkin(this);
    }

}
