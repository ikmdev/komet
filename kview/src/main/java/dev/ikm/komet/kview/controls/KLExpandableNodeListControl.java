package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLExpandableNodeListControlSkin;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A custom JavaFX control that displays a collapsible/expandable list of nodes with animated transitions.
 * <p>
 * This control holds a list of nodes (UI components) but only displays a configurable
 * number of them by default. When collapsed, it shows only up to {@link #visibleCountProperty()} items.
 * A toggle button at the bottom allows users to expand the control to view all items or
 * collapse it back to show only the visible count.
 * <p>
 * Key features:
 * <ul>
 *   <li>Configurable number of always-visible items via {@link #visibleCountProperty()}</li>
 *   <li>Animated expand/collapse transitions</li>
 *   <li>CSS styling support through pseudo-classes and style classes</li>
 *   <li>Accessibility support via JavaFX's accessibility framework</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>{@code
 * // Create a control with some items
 * Label label1 = new Label("Item 1");
 * Label label2 = new Label("Item 2");
 * Label label3 = new Label("Item 3");
 * Label label4 = new Label("Item 4");
 *
 * KLExpandableNodeListControl control = new KLExpandableNodeListControl(label1, label2, label3, label4);
 *
 * // Set the number of visible items (default is 3)
 * control.setVisibleCount(2);
 *
 * // Add the control to a layout container
 * vbox.getChildren().add(control);
 * }</pre>
 *
 * @see KLExpandableNodeListControlSkin
 */
@DefaultProperty("items")
public class KLExpandableNodeListControl extends Control {

    /**
     * The default style class applied to this control.
     */
    private static final String DEFAULT_STYLE_CLASS = "expandable-node-list-control";

    /**
     * PseudoClass that is activated when the control is in expanded state.
     */
    private static final PseudoClass PSEUDO_CLASS_EXPANDED = PseudoClass.getPseudoClass("expanded");

    /**
     * PseudoClass that is activated when the control is in collapsed state.
     */
    private static final PseudoClass PSEUDO_CLASS_COLLAPSED = PseudoClass.getPseudoClass("collapsed");

    /**
     * The default number of items that are visible when the control is collapsed.
     */
    private static final int DEFAULT_VISIBLE_COUNT = 3;

    /**
     * Constructs an empty {@code KLExpandableNodeListControl} with default styling.
     * <p>
     * The control is initialized in the collapsed state, showing up to
     * {@link #DEFAULT_VISIBLE_COUNT} items initially.
     */
    public KLExpandableNodeListControl() {
        // Attach a default style class that can be targeted by CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        // initialize pseudo-class state
        pseudoClassStateChanged(PSEUDO_CLASS_COLLAPSED, true);
    }

    /**
     * Constructs a {@code KLExpandableNodeListControl} with the specified items.
     * <p>
     * The control is initialized in the collapsed state, showing up to
     * {@link #DEFAULT_VISIBLE_COUNT} items initially.
     *
     * @param items the initial items (Nodes) to add to this control
     */
    public KLExpandableNodeListControl(Node... items) {
        this();
        getItems().setAll(items);
    }

    // -----------------------------------------------------------------------------------------
    // Items
    // -----------------------------------------------------------------------------------------

    /**
     * The observable list that holds all the items (nodes) contained in this control.
     * <p>
     * This list is exposed via the {@link #getItems()} method and can be modified directly
     * to add or remove items from the control.
     */
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /**
     * Returns the {@link ObservableList} of items (Nodes) contained in this control.
     * <p>
     * Adding or removing items from this list will automatically reflect in the UI,
     * subject to the value of {@link #getVisibleCount()} and {@link #isExpanded()}.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Add a node to the control
     * control.getItems().add(new Label("New Item"));
     *
     * // Remove all items
     * control.getItems().clear();
     * }</pre>
     *
     * @return the observable list of items for this control
     */
    public final ObservableList<Node> getItems() {
        return items;
    }

    // -----------------------------------------------------------------------------------------
    // Visible count
    // -----------------------------------------------------------------------------------------

    /**
     * The property representing the maximum number of visible items when the control is collapsed.
     * <p>
     * This property is lazily initialized with a default value of {@link #DEFAULT_VISIBLE_COUNT}.
     */
    private IntegerProperty visibleCount;

    /**
     * Returns the maximum number of items to display when the control is not expanded.
     * <p>
     * This value determines how many items from the beginning of the {@link #getItems()} list
     * will be visible when the control is in a collapsed state. Additional items beyond this count
     * are only visible when the control is expanded.
     * <p>
     * If the property has not been instantiated yet, the default value
     * ({@link #DEFAULT_VISIBLE_COUNT}) is returned.
     *
     * @return the maximum number of items visible when collapsed
     */
    public final int getVisibleCount() {
        return (visibleCount != null) ? visibleCount.get() : DEFAULT_VISIBLE_COUNT;
    }

    /**
     * Sets the maximum number of items to display when the control is not expanded.
     * <p>
     * This value determines how many items from the beginning of the {@link #getItems()} list
     * will be visible when the control is in a collapsed state. Additional items beyond this count
     * are only visible when the control is expanded.
     *
     * @param value the maximum number of visible items
     */
    public final void setVisibleCount(int value) {
        visibleCountProperty().set(value);
    }

    /**
     * The property representing the maximum number of visible items when
     * the control is collapsed.
     *
     * @return the IntegerProperty tracking the maximum number of visible items
     */
    public final IntegerProperty visibleCountProperty() {
        if (visibleCount == null) {
            visibleCount = new SimpleIntegerProperty(this, "visibleCount", DEFAULT_VISIBLE_COUNT);
        }
        return visibleCount;
    }

    // -----------------------------------------------------------------------------------------
    // Expanded
    // -----------------------------------------------------------------------------------------

    /**
     * The property representing whether this control is in an expanded state.
     * <p>
     * This property is lazily initialized with a default value of {@code false}.
     */
    private BooleanProperty expanded;

    /**
     * Returns {@code true} if this control is in the expanded state (showing all items),
     * or {@code false} if it is collapsed (showing only up to {@link #getVisibleCount()} items).
     * <p>
     * The expanded state is reflected visually with an animated transition between states.
     *
     * @return {@code true} if the control is expanded; {@code false} otherwise
     */
    public final boolean isExpanded() {
        return (expanded != null) && expanded.get();
    }

    /**
     * Sets whether this control should be expanded (showing all items) or collapsed
     * (showing only up to {@link #getVisibleCount()} items).
     * <p>
     * Changing this value will trigger an animated transition between the expanded
     * and collapsed states.
     * <p>
     * This change also updates the CSS pseudo-classes {@link #PSEUDO_CLASS_EXPANDED} and
     * {@link #PSEUDO_CLASS_COLLAPSED} which can be used for styling.
     *
     * @param value {@code true} to expand the control; {@code false} to collapse it
     */
    public final void setExpanded(boolean value) {
        expandedProperty().set(value);
    }

    /**
     * The property indicating whether this control is expanded.
     * <p>
     * When this property is {@code true}, the control shows all items in its
     * {@link #getItems()} list. When it is {@code false}, the control shows only
     * up to {@link #getVisibleCount()} items.
     * <p>
     * Changing this property's value triggers an animated transition between states
     * and updates the CSS pseudo-classes {@link #PSEUDO_CLASS_EXPANDED} and
     * {@link #PSEUDO_CLASS_COLLAPSED} which can be used for styling.
     * <p>
     * If this property is not yet instantiated, it will be created with a default value of {@code false}.
     *
     * @return the expanded BooleanProperty
     */
    public final BooleanProperty expandedProperty() {
        if (expanded == null) {
            expanded = new SimpleBooleanProperty(this, "expanded", false) {

                @Override
                protected void invalidated() {
                    final boolean active = get();
                    pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED, active);
                    pseudoClassStateChanged(PSEUDO_CLASS_COLLAPSED, !active);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
                }
            };
        }
        return expanded;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLExpandableNodeListControlSkin(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return KLExpandableNodeListControl.class.getResource("expandable-node-list-control.css").toExternalForm();
    }
}