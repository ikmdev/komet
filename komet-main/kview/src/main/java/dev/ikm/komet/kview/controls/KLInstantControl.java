package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLInstantControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;

/**
 * <p>KLInstantControl allows setting or getting a time instant, defined by a {@link #localDateTimeProperty()} and
 * a {@link #zoneOffsetProperty()}, by using a {@link CalendarPopup popup} that shows the initial date and time (or the
 * current ones, if not set) and allows removing the initial value (if set), or setting a new value.
 * </p>
 * <pre><code>
 * KLInstantControl instantControl = new KLInstantControl();
 * instantControl.setTitle("Instant");
 * instantControl.localDateTimeProperty().subscribe(t -> System.out.println("Instant: " + t));
 * </code></pre>
 */
public class KLInstantControl extends Control {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.instant-control");

    /**
     * Creates a KLInstantControl
     */
    public KLInstantControl() {
        getStyleClass().add("instant-control");
    }

    /**
     * A string property that sets the title of the control, if any
     */
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
        return titleProperty;
    }
    public final String getTitle() {
        return titleProperty.get();
    }
    public final void setTitle(String value) {
        titleProperty.set(value);
    }

    /**
     * A string property that sets the prompt of the control
     */
    private final StringProperty promptProperty = new SimpleStringProperty(this, "prompt", resources.getString("prompt.text"));
    public final StringProperty promptProperty() {
        return promptProperty;
    }
    public final String getPrompt() {
        return promptProperty.get();
    }
    public final void setPrompt(String value) {
        promptProperty.set(value);
    }

    /**
     * A property that holds the {@link LocalDateTime} that represents the selected instant in time
     */
    private final ObjectProperty<LocalDateTime> localDateTimeProperty = new SimpleObjectProperty<>(this, "localDateTime");
    public final ObjectProperty<LocalDateTime> localDateTimeProperty() {
        return localDateTimeProperty;
    }
    public final LocalDateTime getLocalDateTime() {
        return localDateTimeProperty.get();
    }
    public final void setLocalDateTime(LocalDateTime value) {
        localDateTimeProperty.set(value);
    }

    /**
     * A property wiht a {@link ZoneOffset} that defines the time-zone offset from Greenwich/ UTC
     */
    private final ObjectProperty<ZoneOffset> zoneOffsetProperty = new SimpleObjectProperty<>(this, "zoneOffset");
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
        return new KLInstantControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("instant-control.css").toExternalForm();
    }
}
