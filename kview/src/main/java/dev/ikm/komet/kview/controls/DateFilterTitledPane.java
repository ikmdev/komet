package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.DateFilterTitledPaneSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DateFilterTitledPane extends TitledPane {

    public DateFilterTitledPane() {
        getStyleClass().add("date-filter-titled-pane");
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

    // optionProperty
    private final StringProperty optionProperty = new SimpleStringProperty(this, "option");
    public final StringProperty optionProperty() {
        return optionProperty;
    }
    public final String getOption() {
        return optionProperty.get();
    }
    public final void setOption(String value) {
        optionProperty.set(value);
    }

    // defaultOptionProperty
    private final StringProperty defaultOptionProperty = new SimpleStringProperty(this, "defaultOption");
    public final StringProperty defaultOptionProperty() {
        return defaultOptionProperty;
    }
    public final String getDefaultOption() {
        return defaultOptionProperty.get();
    }
    public final void setDefaultOption(String value) {
        defaultOptionProperty.set(value);
    }

    // specificDateProperty
    private final ObjectProperty<LocalDate> specificDateProperty = new SimpleObjectProperty<>(this, "specificDate");
    public final ObjectProperty<LocalDate> specificDateProperty() {
        return specificDateProperty;
    }
    public final LocalDate getSpecificDate() {
        return specificDateProperty.get();
    }
    public final void setSpecificDate(LocalDate value) {
        specificDateProperty.set(value);
    }

    // includedDateRangeListProperty
    private final ObjectProperty<List<DateRange>> includedDateRangeListProperty = new SimpleObjectProperty<>(this, "includedDateRangeList", new ArrayList<>());
    public final ObjectProperty<List<DateRange>> includedDateRangeListProperty() {
        return includedDateRangeListProperty;
    }
    public final List<DateRange> getIncludedDateRangeList() {
        return includedDateRangeListProperty.get();
    }
    public final void setIncludedDateRangeList(List<DateRange> value) {
        includedDateRangeListProperty.set(value);
    }

    // excludedDateRangeListProperty
    private final ObjectProperty<List<DateRange>> excludedDateRangeListProperty = new SimpleObjectProperty<>(this, "excludedDateRangeList", new ArrayList<>());
    public final ObjectProperty<List<DateRange>> excludedDateRangeListProperty() {
        return excludedDateRangeListProperty;
    }
    public final List<DateRange> getExcludedDateRangeList() {
        return excludedDateRangeListProperty.get();
    }
    public final void setExcludedDateRangeList(List<DateRange> value) {
        excludedDateRangeListProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateFilterTitledPaneSkin(this);
    }
}
