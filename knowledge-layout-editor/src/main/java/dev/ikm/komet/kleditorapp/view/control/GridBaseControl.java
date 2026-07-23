package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import dev.ikm.komet.layout.editor.EditorWindowBaseControl;

public abstract class GridBaseControl extends EditorWindowBaseControl {

    // "*" shown in the top-right corner while this grid node is marked required. Owned by the base so
    // every grid node — including future pattern representations — gets it without any extra wiring.
    private final Label requiredIndicator = new Label("*");

    protected GridBaseControl() {
        // Init GridPane properties with the existing default values
        GridPane.setColumnIndex(this, getColumnIndex());
        GridPane.setRowIndex(this, getRowIndex());
        GridPane.setColumnSpan(this, getColumnSpan());

        requiredIndicator.getStyleClass().add("required-indicator");
        requiredIndicator.setManaged(false);
        requiredIndicator.visibleProperty().bind(required);
        // Render on top of the (later-added) content regardless of child order.
        requiredIndicator.setViewOrder(-1);
        getChildren().add(requiredIndicator);
    }

    /**
     * Lays out the representation-specific content within the given content box (the control's bounds
     * minus its insets). The required "*" overlay is positioned by this base class, so subclasses never
     * deal with it — a new representation only implements this method.
     */
    protected abstract void layoutContent(double contentX, double contentY, double contentWidth, double contentHeight);

    @Override
    protected final void layoutChildren() {
        double leftInsets = snappedLeftInset();
        double rightInsets = snappedRightInset();
        double topInsets = snappedTopInset();
        double bottomInsets = snappedBottomInset();
        double width = getWidth();
        double height = getHeight();

        layoutContent(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);

        // Overlay the required "*" in the top-right corner (fine placement handled via CSS padding).
        double indicatorWidth = requiredIndicator.prefWidth(-1);
        double indicatorHeight = requiredIndicator.prefHeight(indicatorWidth);
        requiredIndicator.resizeRelocate(width - rightInsets - indicatorWidth, topInsets, indicatorWidth, indicatorHeight);
    }

    // -- required
    private final BooleanProperty required = new SimpleBooleanProperty();
    public boolean isRequired() { return required.get(); }
    public BooleanProperty requiredProperty() { return required; }
    public void setRequired(boolean value) { required.set(value); }

    // -- column Index
    private final IntegerProperty columnIndex = new SimpleIntegerProperty(0) {
        @Override
        protected void invalidated() {
            GridPane.setColumnIndex(GridBaseControl.this, get());
        }
    };
    public int getColumnIndex() { return columnIndex.get(); }
    public IntegerProperty columnIndexProperty() { return columnIndex; }
    public void setColumnIndex(int index) { columnIndex.set(index); }

    // -- row index
    private final IntegerProperty rowIndex = new SimpleIntegerProperty(0) {
        @Override
        protected void invalidated() {
            GridPane.setRowIndex(GridBaseControl.this, get());
        }
    };
    public int getRowIndex() { return rowIndex.get(); }
    public IntegerProperty rowIndexProperty() { return rowIndex; }
    public void setRowIndex(int index) { rowIndex.set(index); }

    // -- column span
    private final IntegerProperty columnSpan = new SimpleIntegerProperty(1){
        @Override
        protected void invalidated() {
            GridPane.setColumnSpan(GridBaseControl.this, get());
        }
    };
    public int getColumnSpan() { return columnSpan.get(); }
    public IntegerProperty columnSpanProperty() { return columnSpan; }
    public void setColumnSpan(int index) { columnSpan.set(index); }

    // -- row span
    private final IntegerProperty rowSpan = new SimpleIntegerProperty(1){
        @Override
        protected void invalidated() {
            GridPane.setRowSpan(GridBaseControl.this, get());
        }
    };
    public int getRowSpan() { return rowSpan.get(); }
    public IntegerProperty rowSpanProperty() { return rowSpan; }
    public void setRowSpan(int span) { this.rowSpan.set(span); }
}