package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The editor tile for a placed supplemental area (a {@code KlSupplementalArea} dropped from the
 * "Controls" palette). A leaf grid node that shows the area's title; grid placement
 * (row/column/span) is provided by {@link GridBaseControl}.
 */
public class SupplementalAreaViewControl extends GridBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "supplemental-area-view";

    private final VBox container = new VBox();
    private final Label titleLabel = new Label();

    SupplementalAreaViewControl() {
        titleLabel.textProperty().bind(title);
        titleLabel.getStyleClass().add("supplemental-area-title");

        container.getChildren().add(titleLabel);
        container.getStyleClass().add("supplemental-area-container");

        getChildren().add(container);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    public void delete() {
        if (getParentSection() != null) {
            getParentSection().getSupplementalAreas().remove(this);
        }
    }

    @Override
    protected void layoutContent(double contentX, double contentY, double contentWidth, double contentHeight) {
        container.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    // -- parent section
    private final ReadOnlyObjectWrapper<SectionViewControl> parentSection = new ReadOnlyObjectWrapper<>();
    public SectionViewControl getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<SectionViewControl> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(SectionViewControl parentSection) { this.parentSection.set(parentSection); }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }
}
