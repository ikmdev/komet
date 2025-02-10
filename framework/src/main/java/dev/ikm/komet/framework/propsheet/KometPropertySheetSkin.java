/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.propsheet;


import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.panel.axiom.AxiomView;
import dev.ikm.komet.framework.propsheet.editor.IntIdListEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdSetEditor;
import dev.ikm.komet.framework.propsheet.editor.ListEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.*;

import static dev.ikm.komet.framework.StyleClasses.PROP_SHEET_PROPERTY_NAME;

public class KometPropertySheetSkin extends SkinBase<KometPropertySheet> {

    /**************************************************************************
     *
     * Static fieldValues
     *
     **************************************************************************/

    private static final int MIN_COLUMN_WIDTH = 100;

    /**************************************************************************
     *
     * fieldValues
     *
     **************************************************************************/

    private final BorderPane content;
    private final ToolBar toolbar;
    private final SegmentedButton modeButton = ActionUtils.createSegmentedButton(
            new KometPropertySheetSkin.ActionChangeMode(PropertySheet.Mode.NAME),
            new KometPropertySheetSkin.ActionChangeMode(PropertySheet.Mode.CATEGORY)
    );
    private final TextField searchField = TextFields.createClearableTextField();


    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    public KometPropertySheetSkin(final KometPropertySheet control) {
        super(control);

        toolbar = new ToolBar();
        toolbar.managedProperty().bind(toolbar.visibleProperty());
        toolbar.setFocusTraversable(true);

        // property sheet mode
        modeButton.managedProperty().bind(modeButton.visibleProperty());
        modeButton.getButtons().get(getSkinnable().modeProperty().get().ordinal()).setSelected(true);
        toolbar.getItems().add(modeButton);

        // property sheet search
        searchField.setPromptText("Search"); //$NON-NLS-1$
        searchField.setMinWidth(0);
        HBox.setHgrow(searchField, Priority.SOMETIMES);
        searchField.managedProperty().bind(searchField.visibleProperty());
        toolbar.getItems().add(searchField);

        // layout controls
        content = new BorderPane();
        content.setTop(toolbar);

        // Wrap the center content in a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        content.setCenter(scrollPane);

        getChildren().add(content);

        // setup listeners
        registerChangeListener(control.modeProperty(), e -> refreshProperties());
        registerChangeListener(control.propertyEditorFactory(), e -> refreshProperties());
        registerChangeListener(control.titleFilter(), e -> refreshProperties());
        registerChangeListener(searchField.textProperty(), e -> getSkinnable().setTitleFilter(searchField.getText()));
        registerChangeListener(control.modeSwitcherVisibleProperty(), e -> updateToolbar());
        registerChangeListener(control.searchBoxVisibleProperty(), e -> updateToolbar());
        registerChangeListener(control.categoryComparatorProperty(), e -> refreshProperties());

        control.getItems().addListener((ListChangeListener<PropertySheet.Item>) change -> refreshProperties());

        // initialize properly
        refreshProperties();
        updateToolbar();

        // Add event filter to handle scroll events appropriately
        addScrollEventFilter(scrollPane);
    }

    /**
     * Refreshes the property sheet content by rebuilding the property container.
     * This method is called when there are changes in the properties or UI settings.
     */
    private void refreshProperties() {
        Node propertyContent = buildPropertySheetContainer();
        ScrollPane scrollPane = (ScrollPane) content.getCenter();
        scrollPane.setContent(propertyContent);
    }

    /**
     * Adds a scroll event filter to the specified {@link ScrollPane} to manage both vertical and
     * horizontal scrolling behavior. This ensures the user cannot scroll beyond the content boundaries.
     *
     * <p>For both vertical and horizontal axes, the filter will:
     * <ul>
     *   <li>Ignore scrolling if the corresponding scrollbar policy is set to {@code NEVER}.</li>
     *   <li>Ignore scrolling when there is no content to scroll or if the viewport size is zero.</li>
     *   <li>Ignore scrolling if the content fits entirely within the viewport (i.e., no scroll is needed).</li>
     *   <li>Ignore negligible scroll deltas (using a small floating-point threshold, {@code EPSILON}).</li>
     *   <li>Consume (block) scroll events that attempt to scroll beyond the top/bottom or left/right edges.</li>
     * </ul>
     *
     * @param scrollPane the {@code ScrollPane} to which this scroll event filter will be added
     */
    private void addScrollEventFilter(ScrollPane scrollPane) {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            Node content = scrollPane.getContent();
            if (content == null) {
                // No content, no scrolling needed
                return;
            }

            // A small threshold for floating-point comparisons
            final double EPSILON = 1e-5;

            // -------------------------------------------------------------
            // VERTICAL SCROLL CHECKS
            // -------------------------------------------------------------

            // 1) Skip if vertical scrollbar is never visible
            if (scrollPane.getVbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER) {

                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                // 2) Skip if viewport is not yet sized or has zero height
                if (viewportHeight > 0) {
                    double contentHeight = content.getLayoutBounds().getHeight();
                    // 3) Skip if content fits entirely within the viewport (no scrolling needed)
                    if (contentHeight > viewportHeight) {
                        double deltaY = event.getDeltaY();
                        // 4) Skip if there's no vertical scroll movement
                        if (Math.abs(deltaY) > EPSILON) {
                            // Retrieve current and extreme scroll values
                            double vValue = scrollPane.getVvalue();
                            double vMin   = scrollPane.getVmin();
                            double vMax   = scrollPane.getVmax();

                            // Determine if we're at the top or bottom
                            boolean atTop    = Math.abs(vValue - vMin) < EPSILON;
                            boolean atBottom = Math.abs(vValue - vMax) < EPSILON;

                            // 5) Consume scroll events that try to scroll beyond the vertical boundary
                            if ((atTop && deltaY > 0) || (atBottom && deltaY < 0)) {
                                event.consume();
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // HORIZONTAL SCROLL CHECKS
            // -------------------------------------------------------------

            // 1) Skip if horizontal scrollbar is never visible
            if (scrollPane.getHbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER) {

                double viewportWidth = scrollPane.getViewportBounds().getWidth();
                // 2) Skip if viewport is not yet sized or has zero width
                if (viewportWidth > 0) {
                    double contentWidth = content.getLayoutBounds().getWidth();
                    // 3) Skip if content fits entirely within the viewport (no scrolling needed)
                    if (contentWidth > viewportWidth) {
                        double deltaX = event.getDeltaX();
                        // 4) Skip if there's no horizontal scroll movement
                        if (Math.abs(deltaX) > EPSILON) {
                            // Retrieve current and extreme scroll values
                            double hValue = scrollPane.getHvalue();
                            double hMin   = scrollPane.getHmin();
                            double hMax   = scrollPane.getHmax();

                            // Determine if we're at the left or right edge
                            boolean atLeft  = Math.abs(hValue - hMin) < EPSILON;
                            boolean atRight = Math.abs(hValue - hMax) < EPSILON;

                            // 5) Consume scroll events that try to scroll beyond the horizontal boundary
                            if ((atLeft && deltaX < 0) || (atRight && deltaX > 0)) {
                                event.consume();
                            }
                        }
                    }
                }
            }
        });
    }

    /**************************************************************************
     *
     * Implementation
     *
     **************************************************************************/

    private void updateToolbar() {
        modeButton.setVisible(getSkinnable().isModeSwitcherVisible());
        searchField.setVisible(getSkinnable().isSearchBoxVisible());

        toolbar.setVisible(modeButton.isVisible() || searchField.isVisible());
    }

    private Node buildPropertySheetContainer() {
        switch (getSkinnable().modeProperty().get()) {
            case CATEGORY: {
                // group by category
                Map<String, List<PropertySheet.Item>> categoryMap = new TreeMap(getSkinnable().getCategoryComparator());
                for (PropertySheet.Item p : getSkinnable().getItems()) {
                    String category = p.getCategory();
                    List<PropertySheet.Item> list = categoryMap.get(category);
                    if (list == null) {
                        list = new ArrayList<>();
                        categoryMap.put(category, list);
                    }
                    list.add(p);
                }

                // WAS: create category-based accordion; KEC want to support more than one pane open.
                VBox categoryPanes = new VBox(2);
                for (String category : categoryMap.keySet()) {
                    KometPropertySheetSkin.PropertyPane props = new KometPropertySheetSkin.PropertyPane(categoryMap.get(category), 0, getSkinnable().isHideLabels());
                    // Only show non-empty categories
                    if (props.getChildrenUnmodifiable().size() > 0) {
                        TitledPane pane = new TitledPane(category, props);
                        pane.setExpanded(true);
                        categoryPanes.getChildren().add(pane);
                    }
                }
                return categoryPanes;
            }

            default:
                return new KometPropertySheetSkin.PropertyPane(getSkinnable().getItems(), 0, getSkinnable().isHideLabels());
        }

    }

    public ViewProperties viewProperties() {
        return getSkinnable().viewProperties;
    }

    /**************************************************************************
     *
     * Overriding public API
     *
     **************************************************************************/

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        content.resizeRelocate(x, y, w, h);
    }

    /**************************************************************************
     *
     * Support classes / enums
     *
     **************************************************************************/

    private class ActionChangeMode extends Action {

        private final Label CATEGORY_IMAGE = Icon.INDENT_INCREASE.makeIcon();
        private final Label NAME_IMAGE = Icon.BY_NAME.makeIcon();

        public ActionChangeMode(PropertySheet.Mode mode) {
            super(""); //$NON-NLS-1$
            setEventHandler(ae -> getSkinnable().modeProperty().set(mode));

            if (mode == PropertySheet.Mode.CATEGORY) {
                setGraphic(CATEGORY_IMAGE);
                setLongText("By Category"); //$NON-NLS-1$
            } else if (mode == PropertySheet.Mode.NAME) {
                setGraphic(NAME_IMAGE);
                setLongText("By Name"); //$NON-NLS-1$
            } else {
                setText("???"); //$NON-NLS-1$
            }
        }

    }


    private class PropertyPane extends GridPane {
        public PropertyPane(List<PropertySheet.Item> properties) {
            this(properties, 0);
        }

        public PropertyPane(List<PropertySheet.Item> properties, int nestingLevel) {
            this(properties, nestingLevel, false);
        }
        public PropertyPane(List<PropertySheet.Item> properties, int nestingLevel, boolean hideLabels) {
            setVgap(5);
            setHgap(5);
            setPadding(new Insets(5, 15, 5, 15 + nestingLevel * 10));
            getStyleClass().add("property-pane"); //$NON-NLS-1$
            setItems(properties, hideLabels);

//            setGridLinesVisible(true);
        }

        public void setItems(List<PropertySheet.Item> properties, boolean hideLabels) {
            getChildren().clear();

            String filter = getSkinnable().titleFilter().get();
            filter = filter == null ? "" : filter.trim().toLowerCase(); //$NON-NLS-1$

            int row = 0;
            final int firstColumn = 0;
            final int secondColumn = 1;
            for (PropertySheet.Item item : properties) {
                Node editor = getEditor(item);

                // filter properties
                String title = item.getName();

                if (!filter.isEmpty() && title.toLowerCase().indexOf(filter) < 0) continue;
                Optional<EntityProxy> optionalProxy = Optional.empty();
                if (title.startsWith("<")) {
                    optionalProxy = ProxyFactory.fromXmlFragmentOptional(title);
                    if (optionalProxy.isPresent()) {
                        title = viewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(optionalProxy.get());
                    }

                }

                // Show labels by default
                if (!hideLabels) {
                    // setup property label
                    Label label = new Label(title + ": ");
                    label.setAlignment(Pos.CENTER_RIGHT);
                    label.setTextAlignment(TextAlignment.RIGHT);
                    label.setWrapText(true);
                    label.setMinWidth(MIN_COLUMN_WIDTH);
                    label.getStyleClass().add(PROP_SHEET_PROPERTY_NAME.toString());

                    // show description as a tooltip
                    String description = item.getDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        label.setTooltip(new Tooltip(description));
                    } else if (optionalProxy.isPresent()) {
                        String fullyQualifiedName = viewProperties().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(optionalProxy.get());
                        label.setTooltip(new Tooltip(fullyQualifiedName));
                    }

                    GridPane.setHalignment(label, HPos.RIGHT);
                    item.getPropertyEditorClass().ifPresent((Class aClass) -> {
                        if (aClass == AxiomView.class ||
                                aClass == ListEditor.class ||
                                aClass == IntIdListEditor.class ||
                                aClass == IntIdSetEditor.class) {
                            label.setMaxWidth(100);
                            GridPane.setValignment(label, VPos.TOP);
                        }
                    });
                    add(label, firstColumn, row);

                    // setup property editor

                    if (editor instanceof Region) {
                        ((Region) editor).setMinWidth(MIN_COLUMN_WIDTH);
                        ((Region) editor).setMaxWidth(Double.MAX_VALUE);
                    }
                    label.setLabelFor(editor);

                } // show labels by default.

                int editorColumn = hideLabels ? firstColumn : secondColumn; // make editor first column when labels are hid.
                add(editor, editorColumn, row);
                GridPane.setHgrow(editor, Priority.ALWAYS);

                //TODO add support for recursive properties

                row++;
            }

        }

        @SuppressWarnings("unchecked")
        private Node getEditor(PropertySheet.Item item) {
            @SuppressWarnings("rawtypes")
            PropertyEditor editor = getSkinnable().getPropertyEditorFactory().call(item);
            if (editor == null) {
                editor = new AbstractPropertyEditor<Object, TextField>(item, new TextField(), true) {
                    {
                        getEditor().setEditable(false);
                        getEditor().setDisable(true);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected ObservableValue<Object> getObservableValue() {
                        return (ObservableValue<Object>) (Object) getEditor().textProperty();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void setValue(Object value) {
                        getEditor().setText(value == null ? "" : value.toString()); //$NON-NLS-1$
                    }
                };
            } else if (!item.isEditable()) {
                editor.getEditor().setDisable(true);
            }
            editor.setValue(item.getValue());
            return editor.getEditor();
        }
    }
}
