package dev.ikm.komet.layout.editor.property;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * The configurable properties specific to the Table pattern factory. This is the canonical example
 * of a {@link KlPropertySet}: it declares plain JavaFX properties and the framework does the rest —
 * the editor renders a control per property, the values are persisted, and the journal table control
 * binds to these same properties at render time.
 *
 * <p>The {@link KlProperty} annotations here are optional. Without them the labels would be derived
 * from the property names ("headerVisible" → "Header Visible"); they are shown to illustrate the
 * escape hatch for custom labels.
 */
public class TablePatternProperties extends KlPropertySet {
    // -- header visible
    @KlProperty(label = "Show header")
    private final BooleanProperty headerVisible = new SimpleBooleanProperty(true);
    public BooleanProperty headerVisibleProperty() { return headerVisible; }
    public boolean isHeaderVisible() { return headerVisible.get(); }
    public void setHeaderVisible(boolean headerVisible) { this.headerVisible.set(headerVisible); }

    // -- grid lines visible
    @KlProperty(label = "Show grid lines")
    private final BooleanProperty gridLinesVisible = new SimpleBooleanProperty(false);
    public BooleanProperty gridLinesVisibleProperty() { return gridLinesVisible; }
    public boolean isGridLinesVisible() { return gridLinesVisible.get(); }
    public void setGridLinesVisible(boolean gridLinesVisible) { this.gridLinesVisible.set(gridLinesVisible); }
}