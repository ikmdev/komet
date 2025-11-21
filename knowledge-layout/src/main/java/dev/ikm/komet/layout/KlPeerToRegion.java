package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.window.KlRenderView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.ikm.komet.layout.KlPeerable.PropertyKeys.KL_PEER;


/**
 * Represents a class for managing and interacting with the relationship
 * between peer nodes and their associated regions. It provides functionality
 * for depth-first traversal over regions and nodes, as well as handling grid
 * layout configurations for the associated objects. This class also manages
 * the binding and unbinding of knowledge layouts and offers methods for
 * querying hierarchical and layout-related properties.
 */
public interface KlPeerToRegion<FX extends Region> extends KlRestorable {

    Logger LOG = LoggerFactory.getLogger(KlPeerToRegion.class);

    FX fxObject();

    /**
     * Performs a recursive depth-first search to collect {@code KlArea} objects from the given node (which are
     * identified by the {@code KL_PEER} property) and its children. If the given node does not contain a {@code KlArea}, the
     * node's descendants are searched in a depth-first manner. Each branch of the DFS is terminated when
     * a node with a {@code KL_PEER} property is encountered, and the {@code KL_PEER} value object is added
     * to the {@code klChildren} list.
     *
     * @param node        the current node to be processed in the search
     * @param klChildren  a mutable list to collect {@code KlArea} nodes found during the search
     */
    private void dfsKlNodes(Node node, MutableList<KlArea<?>> klChildren) {
        if (node.hasProperties() && node.getProperties().containsKey(KL_PEER)) {
            klChildren.add((KlArea<?>) node.getProperties().get(KL_PEER));
        } else if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                dfsKlNodes(child, klChildren);
            }
        }
    }


    /**
     * Performs a depth-first search to retrieve all child nodes of type {@code KlArea<?>}
     * starting from the given parent area.
     *
     * @param parentArea the parent {@code KlArea} from which to start traversing the child nodes.
     *                   Must not be null.
     *
     * @return an immutable list containing all discovered {@code KlArea} child nodes
     *         encountered during the traversal.
     */
    default ImmutableList<KlArea<?>> dfsKlChildrenNodes(KlArea<?> parentArea) {
        Objects.nonNull(parentArea);
        MutableList<KlArea<?>> klChildren = Lists.mutable.empty();
        for (Node child : parentArea.fxObject().getChildrenUnmodifiable()) {
            dfsKlNodes(child, klChildren);
        }
        return klChildren.toImmutable();
    }

    /**
     * Finds a sibling of the current KlArea that matches the given predicate.
     *
     * @param predicate a Predicate that tests each sibling to determine if it matches
     * @return an Optional containing the matching sibling KlArea, or an empty Optional if no match is found
     */
    default Optional<KlArea<?>> findKlSibling(Predicate<KlArea<?>> predicate) {
        Optional<KlArea<?>> optionalParent = findKlAncestor(klArea -> klArea instanceof KlArea<?>);
        if (optionalParent.isPresent()) {
            // parentsChildren includes the siblings + self.
            ImmutableList<KlArea<?>> parentsChildren = dfsKlChildrenNodes(optionalParent.get());
            ImmutableList<KlArea<?>> siblings = parentsChildren.reject(klArea -> klArea.equals(this));
            for (KlArea<?> klSibling : siblings) {
                if (predicate.test(klSibling)) {
                    return Optional.of(klSibling);
                }
            }
        }
        return Optional.empty();
    }
    /**
     * Finds a sibling or ancestor of the current KlArea that matches the given predicate. Siblings are
     * tested first, followed by ancestors. If no match is found, the method returns an empty Optional.
     *
     * @param predicate the condition to evaluate for finding the appropriate sibling or ancestor
     * @return an Optional containing the matching sibling or ancestor if found, or an empty Optional if no match is found
     */
    default Optional<KlArea<?>> findKlSiblingOrAncestor(Predicate<KlArea<?>> predicate) {
        Optional<KlArea<?>> result = findKlSibling(predicate);
        if (result.isPresent()) {
            return result;
        }
        result = findKlAncestor(predicate);
        if (result.isPresent()) {
            return result;
        }
        return Optional.empty();
    }
    default Optional<KlArea<?>> findKlSelfOrAncestor(Predicate<KlArea<?>> predicate) {
        if (this instanceof KlArea<?> klArea) {
            if (predicate.test(klArea)) {
                return Optional.of(klArea);
            }
        }
        Optional<KlArea<?>> result = findKlAncestor(predicate);
        if (result.isPresent()) {
            return result;
        }
        return Optional.empty();
    }
    /**
     * Finds the first ancestor of type KlArea that matches the given predicate.
     *
     * @param predicate a condition to determine whether a particular KlArea is a valid ancestor
     * @return an Optional containing the first matching KlArea ancestor, or an empty Optional if no match is found
     */
    default Optional<KlArea<?>> findKlAncestor(Predicate<KlArea<?>> predicate) {
        Parent parentNode = fxObject().getParent();
        while (parentNode != null) {
            if (parentNode.hasProperties()
                    && parentNode.getProperties().containsKey(KL_PEER)
                    && predicate.test((KlArea<?>) parentNode.getProperties().get(KL_PEER))) {
                return Optional.of((KlArea<?>) parentNode.getProperties().get(KL_PEER));
            }
            parentNode = parentNode.getParent();
        }
        return Optional.empty();
    }


    /**
     * Performs a depth-first search (DFS) process on the KL area nodes and
     * applies the given action to each node.
     *
     * @param action the action to be performed on each KL area node during the DFS traversal
     */
    default void dfsProcessKlArea(Consumer<KlArea<?>> action) {
        dfsProcessNodesWithKlPeer(fxObject(), action);
    }

    /**
     * Performs a depth-first traversal of the descendants of the current KlArea
     * and executes the specified action on each node that has a KlPeer.
     *
     * @param action the operation to be performed on nodes with a KlPeer during traversal
     */
    default void dfsProcessKlAreaDescendents(Consumer<KlArea<?>> action) {
        Parent parentNode = fxObject();
        parentNode.getChildrenUnmodifiable().forEach(childNode -> dfsProcessNodesWithKlPeer(childNode, action));
    }

    /**
     * Performs a depth-first search (DFS) traversal of the nodes and processes
     * any node that contains a Kl peer by applying the specified action.
     *
     * @param node   the root node to start the DFS traversal from
     * @param action the action to be executed for each Kl peer encountered
     */
    private void dfsProcessNodesWithKlPeer(Node node, Consumer<KlArea<?>> action) {
        if (node.hasProperties() && node.getProperties().containsKey(KlPeerable.PropertyKeys.KL_PEER)) {
            KlArea area = (KlArea) node.getProperties().get(KlPeerable.PropertyKeys.KL_PEER);
            action.accept(area);
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> dfsProcessNodesWithKlPeer(child, action));
        }
    }


    default KnowledgeLayout getMasterLayout() {
        Parent fx = fxObject();
        LOG.debug(this.getClass().getSimpleName() + " FxPeer: " + fx + " parent: " + fx.getParent());
        KnowledgeLayout masterLayout = (KnowledgeLayout) fx.getProperties().get(KlArea.PropertyKeys.MASTER_LAYOUT);
        while (masterLayout == null) {
            if (fx.getParent() != null) {
                fx = fx.getParent();
                masterLayout = (KnowledgeLayout) fx.getProperties().get(KlArea.PropertyKeys.MASTER_LAYOUT);
            } else {
                Scene scene = fx.getScene();
                KlPeerable klPeer = (KlPeerable) scene.getProperties().get(KL_PEER);
                masterLayout = switch (klPeer) {
                    case KlRenderView renderView -> renderView.getMasterLayout();
                    case null -> throw new IllegalStateException("Can't find master layout in scene graph. KL_PEER is null. ");
                    default -> throw new IllegalStateException("Can't find master layout in scene graph. " +
                            "KL_PEER is not a KlRenderView: " + klPeer);
                };
            }
        }
        return masterLayout;
    }

    default void setMasterLayout(KnowledgeLayout masterLayout) {
        properties().put(KlArea.PropertyKeys.MASTER_LAYOUT, masterLayout);
    }


    default ObservableMap<Object, Object> properties() {
        return fxObject().getProperties();
    }

    /**
     * Retrieves a GridLayout instance with the current configuration.
     *
     * @return a configured GridLayout object with the specified column index,
     *         row index, colspan, rowspan, grow priorities, alignments, margins,
     *         and size constraints.
     */
    default AreaGridSettings getAreaLayout() {
        return new AreaGridSettings(
                getFactoryClassName(), getColumnIndex(),
                getRowIndex(),
                getLayoutKeyForArea(), getColspan(),
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
                getFillWidth(),
                getVisible()
        );
    }

    /**
     * Configures the grid layout properties for a component based on the given GridLayout object.
     *
     * @param areaGridSettings the GridLayout object containing the layout configuration
     */
    default void setAreaLayout(AreaGridSettings areaGridSettings) {
        setColumnIndex(areaGridSettings.columnIndex());
        setRowIndex(areaGridSettings.rowIndex());
        setColspan(areaGridSettings.columnSpan());
        setRowspan(areaGridSettings.rowSpan());
        setHgrow(areaGridSettings.hGrow());
        setVgrow(areaGridSettings.vGrow());
        setHalignment(areaGridSettings.hAlignment());
        setValignment(areaGridSettings.vAlignment());
        setMargins(areaGridSettings.margin());
        setMaxHeight(areaGridSettings.maxHeight());
        setMaxWidth(areaGridSettings.maxWidth());
        setPrefHeight(areaGridSettings.preferredHeight());
        setPrefWidth(areaGridSettings.preferredWidth());
        setFillHeight(areaGridSettings.fillHeight());
        setFillWidth(areaGridSettings.fillWidth());
        setVisible(areaGridSettings.visible());
        setLayoutKeyForArea(areaGridSettings.layoutKeyForArea());
        setFactoryClassName(areaGridSettings.areaFactoryClassName());
    }

    /**
     * Sets the column index for the pane in the GridPane layout.
     *
     * @param columnIndex the column index to set for the pane
     */
    default void setColumnIndex(int columnIndex) {
        GridPane.setColumnIndex(fxObject(), columnIndex);
    }

    /**
     * Retrieves the column index for this pane in the GridPane layout.
     *
     * @return the column index of the pane
     */
    default int getColumnIndex() {
        return GridPane.getColumnIndex(fxObject());
    }

    /**
     * Sets the row index for the pane in the GridPane layout.
     *
     * @param rowIndex the row index to set for the pane
     */
    default void setRowIndex(int rowIndex) {
        GridPane.setRowIndex(fxObject(), rowIndex);
    }

    /**
     * Retrieves the row index for this pane in the GridPane layout.
     *
     * @return the row index of the pane
     */
    default int getRowIndex() {
        return GridPane.getRowIndex(fxObject());
    }

    /**
     * Sets the column span for the pane in the GridPane layout.
     *
     * @param colspan the number of columns the pane should span
     */
    default void setColspan(int colspan) {
        GridPane.setColumnSpan(fxObject(), colspan);
    }

    /**
     * Retrieves the column span for the pane in the GridPane layout.
     *
     * @return the number of columns the pane spans
     */
    default int getColspan() {
        return GridPane.getColumnSpan(fxObject());
    }

    /**
     * Sets the row span for the pane in the GridPane layout.
     *
     * @param rowspan the number of rows the pane should span
     */
    default void setRowspan(int rowspan) {
        GridPane.setRowSpan(fxObject(), rowspan);
    }

    /**
     * Retrieves the row span for the pane in the GridPane layout.
     *
     * @return the number of rows the pane spans
     */
    default int getRowspan() {
        return GridPane.getRowSpan(fxObject());
    }

    /**
     * Sets the horizontal grow priority for the pane in the GridPane layout.
     *
     * @param priority the horizontal grow priority to set for the pane
     */
    default void setHgrow(Priority priority) {
        GridPane.setHgrow(fxObject(), priority);
    }

    /**
     * Retrieves the horizontal grow priority for the pane in the GridPane layout.
     *
     * @return the horizontal grow priority of the pane
     */
    default Priority getHgrow() {
        return GridPane.getHgrow(fxObject());
    }

    /**
     * Sets the vertical grow priority for the pane in the GridPane layout.
     *
     * @param priority the vertical grow priority to set for the pane
     */
    default void setVgrow(Priority priority) {
        GridPane.setVgrow(fxObject(), priority);
    }

    /**
     * Retrieves the vertical grow priority for the pane in the GridPane layout.
     *
     * @return the vertical grow priority of the pane
     */
    default Priority getVgrow() {
        return GridPane.getVgrow(fxObject());
    }

    /**
     * Sets the horizontal alignment for the widget within its grid cell.
     *
     * @param hPos the horizontal alignment to apply, specified as an HPos value
     */
    default void setHalignment(HPos hPos) {
        GridPane.setHalignment(fxObject(), hPos);
    }

    /**
     * Retrieves the horizontal alignment of this widget within its grid cell.
     *
     * @return the horizontal alignment represented as an {@code HPos} value.
     */
    default HPos getHalignment() {
        return GridPane.getHalignment(fxObject());
    }

    /**
     * Sets the vertical alignment for this widget within its grid cell in a {@code GridPane} layout.
     *
     * @param vPos the vertical alignment to apply, specified as a {@code VPos} value
     */
    default void setValignment(VPos vPos) {
        GridPane.setValignment(fxObject(), vPos);
    }

    /**
     * Retrieves the vertical alignment of this widget within its grid cell in a {@code GridPane} layout.
     * The alignment is represented as a {@code VPos} value.
     *
     * @return the vertical alignment of the widget within its grid cell.
     */
    default VPos getValignment() {
        return GridPane.getValignment(fxObject());
    }

    /**
     * Determines the visibility status of the associated FX Gadget.
     *
     * @return true if the FX Gadget is visible; false otherwise.
     */
    default boolean getVisible() {
        return fxObject().isVisible();
    }

    /**
     * Sets the visibility of the object.
     *
     * @param visible a boolean value where true makes the object visible,
     *                and false makes it invisible.
     */
    default void setVisible(boolean visible) {
        fxObject().setVisible(visible);
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
        GridPane.setMargin(fxObject(), insets);
    }

    /**
     * Retrieves the margins as insets for the pane in the GridPane layout.
     *
     * The insets determine the amount of space to be applied around the pane.
     *
     * @return the Insets object containing the top, right, bottom, and left margins of the pane
     */
    default Insets getMargins() {
        return GridPane.getMargin(fxObject());
    }


    /**
     * Sets whether the widget should fill its cell's width within the GridPane.
     *
     * @param fillWidth a boolean value where {@code true} means the widget should
     *                  fill the width of its cell, and {@code false} means it should not.
     */
    default void setFillWidth(boolean fillWidth) {
        GridPane.setFillWidth(fxObject(), fillWidth);
    }
    /**
     * Determines whether the widget is configured to fill the available width.
     *
     * @return true if the widget is set to fill its width, otherwise false
     */
    default boolean getFillWidth() {
        Boolean fillWidth = GridPane.isFillWidth(fxObject());
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
        GridPane.setFillHeight(fxObject(), fillHeight);
    }
    /**
     * Determines whether the height of the target widget within the GridPane should
     * be expanded to fill its cell, based on the GridPane's isFillHeight property.
     *
     * @return true if the height of the widget is set to fill its allocated cell space,
     *         false otherwise.
     */
    default boolean getFillHeight() {
        Boolean fillHeight = GridPane.isFillHeight(fxObject());
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
            return Optional.of(region.prefWidthProperty());
        }
        return Optional.empty();
    }

    /**
     * Returns a property that represents the visibility state of the Fx objects.
     *
     * @return a BooleanProperty that holds the visibility state. If true, the object is visible; otherwise, it is not.
     */
    default BooleanProperty visibleProperty() {
        return fxObject().visibleProperty();
    }


    /**
     * Sets the preferred width for the associated region of this widget if the underlying
     * FX gadget is an instance of {@code Region}. Updates the region's preferred width to the
     * specified value.
     *
     * @param prefWidth the preferred width to set for the associated region
     */
    default void setPrefWidth(double prefWidth) {
        if (fxObject() instanceof Region region) {
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
        if (fxObject() instanceof Region region) {
            return region.getPrefWidth();
        }
        return Region.USE_COMPUTED_SIZE;
    }
    /**
     * Retrieves the value associated with the "KL_KEY" preference key from the properties map
     * and casts it to a LayoutKey.
     *
     * @return the LayoutKey object associated with the "KL_KEY" preference key, or null if not present or not of type LayoutKey.
     */
    default LayoutKey.ForArea getLayoutKeyForArea() {
        return (LayoutKey.ForArea) properties().get(KlArea.PreferenceKeys.LAYOUT_KEY.name());
    }
    /**
     * Sets the layout key in the properties map.
     *
     * @param layoutKeyForArea the layout key to be set
     */
    default void setLayoutKeyForArea(LayoutKey.ForArea layoutKeyForArea) {
        properties().put(KlArea.PreferenceKeys.LAYOUT_KEY.name(), layoutKeyForArea);
    }
}
