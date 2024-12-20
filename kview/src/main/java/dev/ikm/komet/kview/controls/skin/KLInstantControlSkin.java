package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.CalendarPopup;
import dev.ikm.komet.kview.controls.KLInstantControl;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Default skin implementation for the {@link KLInstantControl} control
 */
public class KLInstantControlSkin extends SkinBase<KLInstantControl> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy, HH:mm a O");
    private final Label titleLabel;
    private final HBox calendarBox;

    /**
     * Creates a new KLInstantControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLInstantControlSkin(KLInstantControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.managedProperty().bind(titleLabel.visibleProperty());
        titleLabel.visibleProperty().bind(titleLabel.textProperty().isNotEmpty());

        Label calendarLabel = new Label();
        calendarLabel.getStyleClass().add("calendar-label");
        calendarLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (control.getLocalDateTime() == null || control.getZoneOffset() == null) {
                return control.getPrompt();
            }
            return formatter.format(control.getLocalDateTime().atOffset(control.getZoneOffset()));
        }, control.promptProperty(), control.localDateTimeProperty(), control.zoneOffsetProperty()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region calendarRegion = new Region();
        calendarRegion.getStyleClass().add("add-date-time-calendar");
        StackPane calendarPane = new StackPane(calendarRegion);
        calendarPane.getStyleClass().add("calendar-pane");
        calendarPane.setOnMouseClicked(e -> {
            CalendarPopup calendarPopup = new CalendarPopup();
            LocalDateTime localDateTime = control.getLocalDateTime();
            calendarPopup.setLocalDate(localDateTime != null ? localDateTime.toLocalDate() : null);
            calendarPopup.setLocalTime(localDateTime != null ? localDateTime.toLocalTime() : null);
            calendarPopup.setZoneOffset(control.getZoneOffset());

            calendarPopup.setOnHiding(ev -> {
                if (calendarPopup.getLocalDate() != null && calendarPopup.getLocalTime() != null) {
                    control.setLocalDateTime(LocalDateTime.of(calendarPopup.getLocalDate(), calendarPopup.getLocalTime()));
                } else {
                    control.setLocalDateTime(null);
                }
                control.setZoneOffset(calendarPopup.getZoneOffset());
            });
            calendarPopup.show(control.getScene().getWindow());
        });

        calendarBox = new HBox(calendarLabel, spacer, calendarPane);
        calendarBox.getStyleClass().add("calendar-box");

        getChildren().addAll(titleLabel, calendarBox);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;
        double spacing = 10;
        double boxPrefWidth = contentWidth - padding.getLeft() - padding.getRight();
        double boxPrefHeight = calendarBox.prefHeight(boxPrefWidth);
        calendarBox.resizeRelocate(x, y + spacing, boxPrefWidth, boxPrefHeight);
    }
}
