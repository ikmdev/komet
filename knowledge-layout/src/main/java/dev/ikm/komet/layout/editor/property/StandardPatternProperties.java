package dev.ikm.komet.layout.editor.property;

import dev.ikm.komet.layout.editor.model.ParentGridModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The configurable properties specific to the Standard pattern factory.
 *
 * <p>The number of grid columns is a Standard-pattern concept (the Table factory lays its fields out
 * as table columns instead), so it lives here rather than on every pattern. By implementing
 * {@link ParentGridModel} this property set is itself the parent grid the Standard pattern's fields
 * are positioned within: the field models bind their {@code parentGrid} to it, the editor control
 * binds its column count to it, and it is the value the grid engine and standard journal control
 * read. A pattern whose factory is not Standard exposes no such property set and therefore no column
 * count.
 */
public class StandardPatternProperties extends KlPropertySet implements ParentGridModel {

    // -- number columns
    @KlProperty(label = "Column(s)", intChoices = {1, 2, 3})
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public int getNumberColumns() { return numberColumns.get(); }
    public void setNumberColumns(int numberColumns) { this.numberColumns.set(numberColumns); }

    // -- separator visible
    /**
     * Whether a horizontal separator is drawn between consecutive semantics of the pattern. Turning
     * it off gives a more compact list, which is what an author wants when each semantic renders a
     * single untitled field and the separators dominate the content.
     */
    @KlProperty(label = "Separator")
    private final BooleanProperty separatorVisible = new SimpleBooleanProperty(true);
    public BooleanProperty separatorVisibleProperty() { return separatorVisible; }
    public boolean isSeparatorVisible() { return separatorVisible.get(); }
    public void setSeparatorVisible(boolean separatorVisible) { this.separatorVisible.set(separatorVisible); }
}