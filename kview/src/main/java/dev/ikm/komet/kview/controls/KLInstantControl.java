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

public class KLInstantControl extends Control {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.instant-control");

    public KLInstantControl() {
        getStyleClass().add("instant-control");
    }

    // titleProperty
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

    // promptProperty
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

    // localDateTimeProperty
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

    // zoneOffsetProperty
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLInstantControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("instant-control.css").toExternalForm();
    }
}
