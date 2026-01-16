package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Optional;
import java.util.UUID;

/**
 * The {@code KlWidget} interface defines a contract for a customizable widget
 * that integrates with a scene graph and provides various layout and styling
 * functionalities. It extends {@code KlGadget} and genericizes the
 * {@code Node} class, representing the underlying JavaFX node.
 *
 * @param <FX> the type of {@code Node} that this widget extends or encapsulates.
 */

public non-sealed interface KlWidget<FX extends Parent> extends KlGadget<FX> {

    /**
     * Enumeration for defining preference keys used in a GridLayout configuration. Each key is
     * associated with a default value, which can be used when the specific property is not explicitly set.
     *
     * This enum implements the PropertyWithDefault interface, allowing for retrieval of default values
     * associated with each specific preference key.
     *
     * The keys and their defaults represent different layout properties such as column index, row index,
     * column and row spans, growth behavior, alignments, margins, dimensions, and fill behaviors,
     * commonly used in grid-based layouts.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        COLUMN_INDEX(GridLayout.DEFAULT.columnIndex()),
        ROW_INDEX(GridLayout.DEFAULT.rowIndex()),
        COLUMN_SPAN(GridLayout.DEFAULT.columnSpan()),
        ROW_SPAN(GridLayout.DEFAULT.rowSpan()),
        H_GROW(GridLayout.DEFAULT.hGrow()),
        V_GROW(GridLayout.DEFAULT.vGrow()),
        H_ALIGNMENT(GridLayout.DEFAULT.hAlignment()),
        V_ALIGNMENT(GridLayout.DEFAULT.vAlignment()),
        MARGIN(GridLayout.DEFAULT.margin()),
        MAX_HEIGHT(GridLayout.DEFAULT.maxHeight()),
        MAX_WIDTH(GridLayout.DEFAULT.maxWidth()),
        PREFERRED_HEIGHT(GridLayout.DEFAULT.preferredHeight()),
        PREFERRED_WIDTH(GridLayout.DEFAULT.preferredWidth()),
        FILL_HEIGHT(GridLayout.DEFAULT.fillHeight()),
        FILL_WIDTH(GridLayout.DEFAULT.fillWidth());

        final Object defaultValue;
        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }
        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }
    }
    default KometPreferences preferences() {
        // TODO eliminate this after refactoring existing KlWidgets to support KlGadget, and factories with preferences.
        throw new UnsupportedOperationException("Please override and implement...");
    }
    /**
     * Retrieves the widget representation for this KlWidget.
     *
     * @return The widget instance, represented by the specific implementation of the KlWidget.
     */
    default FX fxGadget() {
        return klWidget();
    }

    /**
     * Retrieves the scene graph node that presents this KlWidget.
     *
     * @param <SGN> The type of the scene graph node extending {@code Node}.
     * @return The scene graph node instance.
     */
    default <SGN extends Parent> SGN klWidget() {
        return (SGN) this;
    }

    default ObservableMap<Object, Object> properties() {
        return klWidget().getProperties();
    }

    /**
     * Retrieves a GridLayout instance with the current configuration.
     *
     * @return a configured GridLayout object with the specified column index,
     *         row index, colspan, rowspan, grow priorities, alignments, margins,
     *         and size constraints.
     */
    default GridLayout getGridLayout() {
        return new GridLayout(
                getColumnIndex(),
                getRowIndex(),
                getColspan(),
                getRowspan(),
                getHgrow(),
                getVgrow(),
                getHalignment(),
                getValignment(),
                getMargins(),
                getMaxHeight(),
                getMaxWidth(),
                getPrefHeight(),
                getPrefWidth(),
                getFillHeight(),
                getFillWidth()
        );
    }

    /**
     * Configures the grid layout properties for a component based on the given GridLayout object.
     *
     * @param gridLayout the GridLayout object containing the layout configuration
     */
    default void setGridLayout(GridLayout gridLayout) {
        setColumnIndex(gridLayout.columnIndex());
        setRowIndex(gridLayout.rowIndex());
        setColspan(gridLayout.columnSpan());
        setRowspan(gridLayout.rowSpan());
        setHgrow(gridLayout.hGrow());
        setVgrow(gridLayout.vGrow());
        setHalignment(gridLayout.hAlignment());
        setValignment(gridLayout.vAlignment());
        setMargins(gridLayout.margin());
        setMaxHeight(gridLayout.maxHeight());
        setMaxWidth(gridLayout.maxWidth());
        setPrefHeight(gridLayout.preferredHeight());
        setPrefWidth(gridLayout.preferredWidth());
        setFillHeight(gridLayout.fillHeight());
        setFillWidth(gridLayout.fillWidth());
    }

    /**
     * Sets the column index for the pane in the GridPane layout.
     *
     * @param columnIndex the column index to set for the pane
     */
    default void setColumnIndex(int columnIndex) {
        GridPane.setColumnIndex(klWidget(), columnIndex);
    }

    /**
     * Retrieves the column index for this pane in the GridPane layout.
     *
     * @return the column index of the pane
     */
    default int getColumnIndex() {
        return GridPane.getColumnIndex(klWidget());
    }

    /**
     * Sets the row index for the pane in the GridPane layout.
     *
     * @param rowIndex the row index to set for the pane
     */
    default void setRowIndex(int rowIndex) {
        GridPane.setRowIndex(klWidget(), rowIndex);
    }

    /**
     * Retrieves the row index for this pane in the GridPane layout.
     *
     * @return the row index of the pane
     */
    default int getRowIndex() {
        return GridPane.getRowIndex(klWidget());
    }

    /**
     * Sets the column span for the pane in the GridPane layout.
     *
     * @param colspan the number of columns the pane should span
     */
    default void setColspan(int colspan) {
        GridPane.setColumnSpan(klWidget(), colspan);
    }

    /**
     * Retrieves the column span for the pane in the GridPane layout.
     *
     * @return the number of columns the pane spans
     */
    default int getColspan() {
        return GridPane.getColumnSpan(klWidget());
    }

    /**
     * Sets the row span for the pane in the GridPane layout.
     *
     * @param rowspan the number of rows the pane should span
     */
    default void setRowspan(int rowspan) {
        GridPane.setRowSpan(klWidget(), rowspan);
    }

    /**
     * Retrieves the row span for the pane in the GridPane layout.
     *
     * @return the number of rows the pane spans
     */
    default int getRowspan() {
        return GridPane.getRowSpan(klWidget());
    }

    /**
     * Sets the horizontal grow priority for the pane in the GridPane layout.
     *
     * @param priority the horizontal grow priority to set for the pane
     */
    default void setHgrow(Priority priority) {
        GridPane.setHgrow(klWidget(), priority);
    }

    /**
     * Retrieves the horizontal grow priority for the pane in the GridPane layout.
     *
     * @return the horizontal grow priority of the pane
     */
    default Priority getHgrow() {
        return GridPane.getHgrow(klWidget());
    }

    /**
     * Sets the vertical grow priority for the pane in the GridPane layout.
     *
     * @param priority the vertical grow priority to set for the pane
     */
    default void setVgrow(Priority priority) {
        GridPane.setVgrow(klWidget(), priority);
    }

    /**
     * Retrieves the vertical grow priority for the pane in the GridPane layout.
     *
     * @return the vertical grow priority of the pane
     */
    default Priority getVgrow() {
        return GridPane.getVgrow(klWidget());
    }

    /**
     * Sets the horizontal alignment for the widget within its grid cell.
     *
     * @param hPos the horizontal alignment to apply, specified as an HPos value
     */
    default void setHalignment(HPos hPos) {
        GridPane.setHalignment(klWidget(), hPos);
    }

    /**
     * Retrieves the horizontal alignment of this widget within its grid cell.
     *
     * @return the horizontal alignment represented as an {@code HPos} value.
     */
    default HPos getHalignment() {
        return GridPane.getHalignment(klWidget());
    }

    /**
     * Sets the vertical alignment for this widget within its grid cell in a {@code GridPane} layout.
     *
     * @param vPos the vertical alignment to apply, specified as a {@code VPos} value
     */
    default void setValignment(VPos vPos) {
        GridPane.setValignment(klWidget(), vPos);
    }

    /**
     * Retrieves the vertical alignment of this widget within its grid cell in a {@code GridPane} layout.
     * The alignment is represented as a {@code VPos} value.
     *
     * @return the vertical alignment of the widget within its grid cell.
     */
    default VPos getValignment() {
        return GridPane.getValignment(klWidget());
    }

    /**
     * Sets the margins for the pane in the GridPane layout.
     *
     * @param top the amount of space to be applied to the top of the pane
     * @param right the amount of space to be applied to the right of the pane
     * @param bottom the amount of space to be applied to the bottom of the pane
     * @param left the amount of space to be applied to the left of the pane
     */
    default void setMargins(double top, double right, double bottom, double left) {
        setMargins(new Insets(top, right, bottom, left));
    }

    /**
     * Sets the margins from the insets for the pane in the GridPane layout.
     *
     * @param insets the Insets object containing the top, right, bottom, and left margins
     */
    default void setMargins(Insets insets) {
        GridPane.setMargin(klWidget(), insets);
    }

    /**
     * Retrieves the margins as insets for the pane in the GridPane layout.
     *
     * The insets determine the amount of space to be applied around the pane.
     *
     * @return the Insets object containing the top, right, bottom, and left margins of the pane
     */
    default Insets getMargins() {
        return GridPane.getMargin(klWidget());
    }


    /**
     * Sets whether the widget should fill its cell's width within the GridPane.
     *
     * @param fillWidth a boolean value where {@code true} means the widget should
     *                  fill the width of its cell, and {@code false} means it should not.
     */
    default void setFillWidth(boolean fillWidth) {
        GridPane.setFillWidth(klWidget(), fillWidth);
    }
    /**
     * Determines whether the widget is configured to fill the available width.
     *
     * @return true if the widget is set to fill its width, otherwise false
     */
    default boolean getFillWidth() {
        Boolean fillWidth = GridPane.isFillWidth(klWidget());
        if (fillWidth == null) {
            return true;
        }
        return fillWidth;
    }

    /**
     * Sets whether the widget should fill the available vertical space in its layout container.
     *
     * @param fillHeight a boolean indicating whether the widget should fill the vertical space (true) or not (false)
     */
    default void setFillHeight(boolean fillHeight) {
        GridPane.setFillHeight(klWidget(), fillHeight);
    }
    /**
     * Determines whether the height of the target widget within the GridPane should
     * be expanded to fill its cell, based on the GridPane's isFillHeight property.
     *
     * @return true if the height of the widget is set to fill its allocated cell space,
     *         false otherwise.
     */
    default boolean getFillHeight() {
        Boolean fillHeight = GridPane.isFillHeight(klWidget());
        if (fillHeight == null) {
            return true;
        }
        return fillHeight;
    }

    /**
     * Retrieves the maxHeight property of the associated Region, if present.
     *
     * @return an Optional containing the maxHeight DoubleProperty of the Region if the fxGadget is an instance of Region;
     *         otherwise, an empty Optional
     */
    default Optional<DoubleProperty> maxHeightPropertyOptional() {
        if (fxGadget() instanceof Region region) {
            return Optional.of(region.maxHeightProperty());
        }
        return Optional.empty();
    }

    /**
     * Sets the maximum height for this widget's associated region,
     * if the underlying FX gadget is an instance of {@code Region}.
     *
     * @param maxHeight the maximum height value to set for the associated region
     */
    default void setMaxHeight(double maxHeight) {
        if (fxGadget() instanceof Region region) {
            region.setMaxHeight(maxHeight);
        }
    }

    /**
     * Retrieves the maximum height for the underlying region associated with this widget.
     * If the underlying FX gadget is an instance of {@code Region}, the maximum height
     * specific to that region is returned. Otherwise, the default value for using the
     * computed size is returned.
     *
     * @return the maximum height of the region if applicable, otherwise the value
     *         {@code Region.USE_COMPUTED_SIZE}.
     */
    default double getMaxHeight() {
        if (fxGadget() instanceof Region region) {
            return region.getMaxHeight();
        }
        return Region.USE_COMPUTED_SIZE;
    }

    /**
     * Retrieves the optional maxWidth property of the current fxGadget if it is an instance of Region.
     *
     * @return An Optional containing the maxWidth property as a DoubleProperty if the fxGadget is a Region, or an empty Optional if not.
     */
    default Optional<DoubleProperty> maxWidthPropertyOptional() {
        if (fxGadget() instanceof Region region) {
            return Optional.of(region.maxWidthProperty());
        }
        return Optional.empty();
    }

    /**
     * Sets the maximum width for this widget's associated region, if the underlying FX
     * gadget is an instance of {@code Region}.
     *
     * @param maxWidth the maximum width value to set for the associated region
     */
    default void setMaxWidth(double maxWidth) {
        if (fxGadget() instanceof Region region) {
            region.setMaxWidth(maxWidth);
        }
    }

    /**
     * Retrieves the maximum width for the underlying region associated with this widget.
     * If the underlying FX gadget is an instance of {@code Region}, the maximum width
     * specific to that region is returned. Otherwise, the default value for using the
     * computed size is returned.
     *
     * @return the maximum width of the region if applicable, otherwise the value
     *         {@code Region.USE_COMPUTED_SIZE}.
     */
    default double getMaxWidth() {
        if (fxGadget() instanceof Region region) {
            return region.getMaxWidth();
        }
        return Region.USE_COMPUTED_SIZE;
    }

    /**
     * Retrieves the preferred height property of the underlying JavaFX Region
     * if the fxGadget is an instance of Region.
     *
     * @return an Optional containing the preferred height property as a DoubleProperty
     *         if the fxGadget is an instance of Region, otherwise an empty Optional.
     */
    default Optional<DoubleProperty> prefHeightPropertyOptional() {
        if (fxGadget() instanceof Region region) {
            return Optional.of(region.prefHeightProperty());
        }
        return Optional.empty();
    }

    /**
     * Sets the preferred height for this widget's associated region.
     * If the underlying FX gadget is an instance of {@code Region}, the preferred height
     * of the region is updated to the specified value.
     *
     * @param prefHeight the preferred height to set for the associated region
     */
    default void setPrefHeight(double prefHeight) {
        if (fxGadget() instanceof Region region) {
            region.setPrefHeight(prefHeight);
        }
    }

    /**
     * Retrieves the preferred height of the associated region.
     * If the underlying FX gadget is an instance of {@code Region}, the preferred height
     * specific to that region is returned. Otherwise, the value {@code Region.USE_COMPUTED_SIZE} is returned.
     *
     * @return the preferred height of the region if applicable, otherwise the value {@code Region.USE_COMPUTED_SIZE}.
     */
    default double getPrefHeight() {
        if (fxGadget() instanceof Region region) {
            return region.getPrefHeight();
        }
        return Region.USE_COMPUTED_SIZE;
    }
    /**
     * Retrieves the optional `DoubleProperty` that represents the preferred width of the underlying FX gadget,
     * if the FX gadget is an instance of `Region`.
     *
     * @return an `Optional` containing the preferred width property if the FX gadget is a `Region`, otherwise an empty `Optional`
     */
    default Optional<DoubleProperty> prefWidthPropertyOptional() {
        if (fxGadget() instanceof Region region) {
            return Optional.of(region.prefWidthProperty());
        }
        return Optional.empty();
    }

    /**
     * Sets the preferred width for the associated region of this widget if the underlying
     * FX gadget is an instance of {@code Region}. Updates the region's preferred width to the
     * specified value.
     *
     * @param prefWidth the preferred width to set for the associated region
     */
    default void setPrefWidth(double prefWidth) {
        if (fxGadget() instanceof Region region) {
            region.setPrefWidth(prefWidth);
        }
    }

    /**
     * Retrieves the preferred width of the associated region.
     * If the underlying FX gadget is an instance of {@code Region}, the preferred width
     * specific to that region is returned. Otherwise, the value {@code Region.USE_COMPUTED_SIZE} is returned.
     *
     * @return the preferred width of the region if applicable, otherwise the value {@code Region.USE_COMPUTED_SIZE}.
     */
    default double getPrefWidth() {
        if (fxGadget() instanceof Region region) {
            return region.getPrefWidth();
        }
        return Region.USE_COMPUTED_SIZE;
    }

    /**
     * Retrieves the unique identifier for this KlWidget. Note that the UUID for the
     * KlWidget is independent of whatever entity it may contain at a particular instant. And the
     * UUID will not change across the life of this Knowledge Layout Component.
     *
     * @return the UUID representing the unique identifier of the KlWidget.
     * @deprecated use klObjectId() instead.
     */
    @Deprecated
    default UUID klWidgetId() {
        return klObjectId();
    }

}
