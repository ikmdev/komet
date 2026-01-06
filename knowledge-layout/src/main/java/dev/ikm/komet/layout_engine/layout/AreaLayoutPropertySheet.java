package dev.ikm.komet.layout_engine.layout;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;
import javafx.util.Subscription;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;
import java.util.ServiceLoader;

public class AreaLayoutPropertySheet implements MapChangeListener {
    /*
        https://stackoverflow.com/questions/24238858/property-sheet-example-with-use-of-a-propertyeditor-controlsfx
     */

    SimpleIntegerProperty columnIndex = new SimpleIntegerProperty(this, "Column index", AreaGridSettings.DEFAULT.columnIndex());
    SimpleIntegerProperty rowIndex = new SimpleIntegerProperty(this, "Row index", AreaGridSettings.DEFAULT.rowIndex());
    SimpleIntegerProperty columnSpan = new SimpleIntegerProperty(this, "Column span", AreaGridSettings.DEFAULT.columnSpan());
    SimpleIntegerProperty rowSpan = new SimpleIntegerProperty(this, "Row span", AreaGridSettings.DEFAULT.rowSpan());
    SimpleObjectProperty<Priority> hGrow = new SimpleObjectProperty<>(this, "Horizontal grow", AreaGridSettings.DEFAULT.hGrow());
    SimpleObjectProperty<Priority> vGrow = new SimpleObjectProperty<>(this, "Vertical grow", AreaGridSettings.DEFAULT.vGrow());
    SimpleObjectProperty<HPos> hAlighment = new SimpleObjectProperty<>(this, "Horizontal alignment", AreaGridSettings.DEFAULT.hAlignment());
    SimpleObjectProperty<VPos> vAlignment = new SimpleObjectProperty<>(this, "Vertical alignment", AreaGridSettings.DEFAULT.vAlignment());

    SimpleDoubleProperty marginTop = new SimpleDoubleProperty(this, "Margin top", AreaGridSettings.DEFAULT.margin().getTop());
    SimpleDoubleProperty marginRight = new SimpleDoubleProperty(this, "Margin right", AreaGridSettings.DEFAULT.margin().getRight());
    SimpleDoubleProperty marginBottom = new SimpleDoubleProperty(this, "Margin bottom", AreaGridSettings.DEFAULT.margin().getBottom());
    SimpleDoubleProperty marginLeft = new SimpleDoubleProperty(this, "Margin left", AreaGridSettings.DEFAULT.margin().getLeft());

    SimpleDoubleProperty maxHeight = new SimpleDoubleProperty(this, "Max height", AreaGridSettings.DEFAULT.maxHeight());
    SimpleDoubleProperty maxWidth = new SimpleDoubleProperty(this, "Max width", AreaGridSettings.DEFAULT.maxWidth());
    SimpleDoubleProperty preferredHeight = new SimpleDoubleProperty(this, "Preferred height", AreaGridSettings.DEFAULT.preferredHeight());
    SimpleDoubleProperty preferredWidth = new SimpleDoubleProperty(this, "Preferred width", AreaGridSettings.DEFAULT.preferredWidth());
    SimpleBooleanProperty fillHeight = new SimpleBooleanProperty(this, "Fill height", AreaGridSettings.DEFAULT.fillHeight());
    SimpleBooleanProperty fillWidth = new SimpleBooleanProperty(this, "Fill width", AreaGridSettings.DEFAULT.fillWidth());
    SimpleBooleanProperty visible = new SimpleBooleanProperty(this, "Visible", AreaGridSettings.DEFAULT.visible());
    SimpleStringProperty areaFactoryClassName = new SimpleStringProperty(this, "Area factory", AreaGridSettings.DEFAULT.areaFactoryClassName());
    final LayoutKey.ForArea klKey;
    final PropertySheet propertySheet = new PropertySheet();
    final KlArea<Region> klArea;
    final AreaGridSettings initialAreaLayout;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final MapChangeListener<Object, Object> mapChangeListener = this::onChanged;

    final Subscription subscription = Subscription.EMPTY;

    PropertySheet.Item[] items = {
            new PropertyWrapper(columnIndex),
            new PropertyWrapper(rowIndex),
            new PropertyWrapper(columnSpan),
            new PropertyWrapper(rowSpan),
            new PropertyWrapper(hGrow),
            new PropertyWrapper(vGrow),
            new PropertyWrapper(hAlighment),
            new PropertyWrapper(vAlignment),
            new PropertyWrapper(marginTop),
            new PropertyWrapper(marginRight),
            new PropertyWrapper(marginBottom),
            new PropertyWrapper(marginLeft),
            new PropertyWrapper(maxHeight),
            new PropertyWrapper(maxWidth),
            new PropertyWrapper(preferredHeight),
            new PropertyWrapper(preferredWidth),
            new PropertyWrapper(fillHeight),
            new PropertyWrapper(fillWidth),
            new PropertyWrapper(visible),
            new PropertyWrapper(areaFactoryClassName)
    };

    public AreaGridSettings layoutRecord() {
        return new AreaGridSettings(
                areaFactoryClassName.get().getClass().getName(), columnIndex.intValue(),
                rowIndex.intValue(),
                klKey, columnSpan.intValue(),
                rowSpan.intValue(),
                hGrow.get(),
                vGrow.get(),
                hAlighment.get(),
                vAlignment.get(),
                new Insets(marginTop.get(), marginRight.get(), marginBottom.get(), marginLeft.get()),
                maxHeight.get(),
                maxWidth.get(),
                preferredHeight.get(),
                preferredWidth.get(),
                fillHeight.get(),
                fillWidth.get(),
                visible.get()
        );
    }


    public AreaLayoutPropertySheet(KlArea<? extends Region> klArea) {
        this.klArea = (KlArea<Region>) klArea;
        this.initialAreaLayout = klArea.getAreaLayout();
        propertySheet.getItems().addAll(items);
        propertySheet.setMode(PropertySheet.Mode.NAME);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);
        // Set initial values
        columnIndex.setValue(this.klArea.getColumnIndex());
        rowIndex.setValue(this.klArea.getRowIndex());
        columnSpan.setValue(this.klArea.getColspan());
        rowSpan.setValue(this.klArea.getRowspan());
        hGrow.setValue(this.klArea.getHgrow());
        vGrow.setValue(this.klArea.getVgrow());
        hAlighment.setValue(this.klArea.getHalignment());
        vAlignment.setValue(this.klArea.getValignment());
        marginTop.setValue(this.klArea.getMargins().getTop());
        marginRight.setValue(this.klArea.getMargins().getRight());
        marginBottom.setValue(this.klArea.getMargins().getBottom());
        marginLeft.setValue(this.klArea.getMargins().getLeft());
        maxHeight.setValue(this.klArea.getMaxHeight());
        maxWidth.setValue(this.klArea.getMaxWidth());
        preferredHeight.setValue(this.klArea.getPrefHeight());
        preferredWidth.setValue(this.klArea.getPrefWidth());
        fillHeight.setValue(this.klArea.getFillHeight());
        fillWidth.setValue(this.klArea.getFillWidth());
        visible.setValue(this.klArea.getVisible());
        klKey = this.klArea.getLayoutKeyForArea();
        areaFactoryClassName.setValue(this.klArea.getFactoryClassName());
        // subscribe klWidget to GadgetLayoutPropertySheet
        subscription
                .and(columnIndex.subscribe(newValue -> this.klArea.setColumnIndex(newValue.intValue())))
                .and(rowIndex.subscribe(newValue -> this.klArea.setRowIndex(newValue.intValue())))
                .and(columnSpan.subscribe(newValue -> this.klArea.setColspan(newValue.intValue())))
                .and(rowSpan.subscribe(newValue -> this.klArea.setRowspan(newValue.intValue())))
                .and(hGrow.subscribe(newValue -> this.klArea.setHgrow(newValue)))
                .and(vGrow.subscribe(newValue -> this.klArea.setVgrow(newValue)))
                .and(hAlighment.subscribe(newValue -> this.klArea.setHalignment(newValue)))
                .and(vAlignment.subscribe(newValue -> this.klArea.setValignment(newValue)))
                .and(marginTop.subscribe(newValue -> this.klArea.setMargins(new Insets(newValue.doubleValue(), marginRight.get(), marginBottom.get(), marginLeft.get()))))
                .and(marginRight.subscribe(newValue -> this.klArea.setMargins(new Insets(marginTop.get(), newValue.doubleValue(), marginBottom.get(), marginLeft.get()))))
                .and(marginBottom.subscribe(newValue -> this.klArea.setMargins(new Insets(marginTop.get(), marginRight.get(), newValue.doubleValue(), marginLeft.get()))))
                .and(marginLeft.subscribe(newValue -> this.klArea.setMargins(new Insets(marginTop.get(), marginRight.get(), marginBottom.get(), newValue.doubleValue()))))
                .and(maxHeight.subscribe(newValue -> this.klArea.setMaxHeight(newValue.doubleValue())))
                .and(maxWidth.subscribe(newValue -> this.klArea.setMaxWidth(newValue.doubleValue())))
                .and(preferredHeight.subscribe(newValue -> this.klArea.setPrefHeight(newValue.doubleValue())))
                .and(preferredWidth.subscribe(newValue -> this.klArea.setPrefWidth(newValue.doubleValue())))
                .and(fillHeight.subscribe(newValue -> this.klArea.setFillHeight(newValue)))
                .and(fillWidth.subscribe(newValue -> this.klArea.setFillWidth(newValue)))
                .and(visible.subscribe(newValue -> this.klArea.setVisible(newValue)))
                .and(areaFactoryClassName.subscribe(newValue -> this.klArea.setFactoryClassName(newValue.getClass().getName())));

        // Subscribe GadgetLayoutPropertySheet to klWidget

        this.klArea.maxHeightPropertyOptional().ifPresent(maxHeightProperty ->
                subscription.and(maxHeightProperty.subscribe(newValue -> maxHeightProperty.set(newValue.doubleValue()))));
        this.klArea.maxWidthPropertyOptional().ifPresent(maxWidthProperty ->
                subscription.and(maxWidthProperty.subscribe(newValue -> maxWidthProperty.set(newValue.doubleValue()))));
        this.klArea.prefHeightPropertyOptional().ifPresent(preferredHeightProperty ->
                subscription.and(preferredHeightProperty.subscribe(newValue -> preferredHeightProperty.set(newValue.doubleValue()))));
        this.klArea.prefWidthPropertyOptional().ifPresent(preferredWidthProperty ->
                subscription.and(preferredWidthProperty.subscribe(newValue -> preferredWidthProperty.set(newValue.doubleValue()))));
        subscription.and(this.klArea.visibleProperty().subscribe(newValue -> visible.set(newValue)));

        //NOTE: using a listener for the Observable map instead of just an invalidation listener...
        this.klArea.properties().addListener(mapChangeListener);
        switch (this.klArea.fxObject()) {
            case Region node -> node.parentProperty().subscribe(this::parentChanged);
        }
        this.klArea.fxObject().getScene().getWindow().setOnCloseRequest(this::onCloseRequest);

    }

    /**
     * Handles the close request event for the window associated with the klWidget.
     * This method ensures that resources and listeners are properly unsubscribed
     * to avoid memory leaks or unintended behavior after the window is closed.
     *
     * @param windowEvent the {@code WindowEvent} triggered when a close request occurs
     */
    private void onCloseRequest(WindowEvent windowEvent) {
        LayoutKey.ForArea layoutKey = this.klArea.getLayoutKeyForArea();
        AreaGridSettings newSettings = this.layoutRecord();
        if (!this.initialAreaLayout.equals(newSettings)) {
            this.klArea.getMasterLayout().layoutOverrides().addOverride(layoutKey, newSettings);
        }
        unsubscribe();
    }


    /**
     * Handles changes to the parent property of a Node. This method is invoked when the parent
     * of the associated {@code klWidget} changes. If the Node is detached from its parent (oldParent
     * is not null and newParent is null), any associated resources or listeners are unsubscribed
     * to prevent memory leaks or unintended behavior.
     *
     * @param oldParent the previous parent of the Node, may be null if the Node had no parent before
     * @param newParent the new parent of the Node, may be null if the Node has been detached from its parent
     */
    private void parentChanged(Parent oldParent, Parent newParent) {
        if (oldParent != null && newParent == null) {
            this.unsubscribe();
        }
    }

    /**
     * Unsubscribes and removes listeners associated with the klWidget.
     * This method is used to clean up resources and event listeners to avoid memory leaks
     * or unintended behavior when the klWidget is no longer needed or its lifecycle ends.
     *
     * Specifically:
     * - Removes the mapChangeListener from the klWidget's properties.
     * - Unsubscribes the existing subscription, stopping any further events or updates.
     */
    private void unsubscribe() {
        klArea.properties().removeListener(mapChangeListener);
        subscription.unsubscribe();
    }

    /**
     * Handles changes to the map of properties and updates specific layout-related attributes
     * based on the detected changes. This method listens for added properties in the map
     * and performs corresponding updates to the property values.
     *
     * @param change An instance of {@link Change} that represents a change
     *               in the map of properties. This object contains details about the key-value
     *               pair that was added, removed, or modified.
     */
    @Override
    public void onChanged(Change change) {
        if (change.wasAdded()) {
            Object key = change.getKey();
            Object value = change.getValueAdded();
            if (key instanceof String && value instanceof Object) {
                String propertyName = (String) key;
                Object propertyValue = value;
                switch (propertyName) {
                    case "gridpane-column" -> columnIndex.setValue((Integer) propertyValue);
                    case "gridpane-row" -> rowIndex.setValue((Integer) propertyValue);
                    case "gridpane-column-span" -> columnSpan.setValue((Integer) propertyValue);
                    case "gridpane-row-span" -> rowSpan.setValue((Integer) propertyValue);
                    case "gridpane-halignment" -> hAlighment.setValue((HPos) propertyValue);
                    case "gridpane-valignment" -> vAlignment.setValue((VPos) propertyValue);
                    case "gridpane-margin" -> {
                        marginTop.setValue(((Insets) propertyValue).getTop());
                        marginRight.setValue(((Insets) propertyValue).getRight());
                        marginBottom.setValue(((Insets) propertyValue).getBottom());
                        marginLeft.setValue(((Insets) propertyValue).getLeft());
                    }
                    case "gridpane-hgrow" -> hGrow.setValue((Priority) propertyValue);
                    case "gridpane-vgrow" -> vGrow.setValue((Priority) propertyValue);
                    case "gridpane-fill-height" ->  fillHeight.setValue((Boolean) propertyValue);
                    case "gridpane-fill-width" ->  fillWidth.setValue((Boolean) propertyValue);
                }
            }
        }
    }

    /**
     * Retrieves the associated {@code PropertySheet} instance used for configuring
     * and managing layout properties in a grid-based system.
     *
     * @return the {@code PropertySheet} associated with this object, providing
     *         access to layout property configuration and customization.
     */
    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    /**
     * A wrapper class for a {@link Property}, implementing the
     * {@link PropertySheet.Item} interface to enable integration
     * with the ControlsFX PropertySheet. The wrapper allows custom handling and representation
     * of properties within a property sheet.
     *
     * This class provides functionalities for accessing property metadata such as name,
     * value, description, category, type, and editability. Additionally, it supports
     * observation of the property's value and custom handling for property editors.
     */
    class PropertyWrapper implements PropertySheet.Item {
        Property wrappedProperty;

        public PropertyWrapper(Property<?> wrappedProperty) {
            this.wrappedProperty = wrappedProperty;
        }

        public Property<?> getWrappedProperty() {
            return wrappedProperty;
        }

        @Override
        public String getName() {
            return wrappedProperty.getName();
        }

        @Override
        public Object getValue() {
            return wrappedProperty.getValue();
        }

        @Override
        public void setValue(Object value) {
            wrappedProperty.setValue(value);
        }

        @Override
        public String getDescription() {
            return getName();
        }

        @Override
        public String getCategory() {
            return "Layout";
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.ofNullable(wrappedProperty);
        }

        @Override
        public Class<?> getType() {
            return switch (wrappedProperty) {
                case StringProperty _ -> String.class;
                case IntegerProperty _ -> Integer.class;
                case DoubleProperty _ -> Double.class;
                case BooleanProperty _ -> Boolean.class;
                case ObjectProperty objectProperty when objectProperty.get() instanceof Insets -> Insets.class;
                case ObjectProperty objectProperty when objectProperty.get() instanceof Priority -> Priority.class;
                case ObjectProperty objectProperty when objectProperty.get() instanceof HPos -> HPos.class;
                case ObjectProperty objectProperty when objectProperty.get() instanceof VPos -> VPos.class;
                default -> Object.class;
            };
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            if (wrappedProperty == areaFactoryClassName) {
                return Optional.of(CustomAreaFactoryEditor.class);
            }
            return Optional.empty();
        }
    }

    public static class CustomAreaFactoryEditor implements PropertyEditor<String> {

        private final ComboBox<KlArea.Factory> comboBox;

        public CustomAreaFactoryEditor(PropertySheet.Item item) {
            ServiceLoader<KlArea.Factory> loader = PluggableService.load(KlArea.Factory.class);
            MutableList<KlArea.Factory> services = Lists.mutable.empty();
            for (KlArea.Factory service : loader) {
                services.add(service);
            }

            Optional<KlArea.Factory> currentFactory = services.select(factory ->
                    factory.getClass().getName().equals(item.getValue())).getFirstOptional();

            this.comboBox = new ComboBox<>();

            this.comboBox.setCellFactory(listView -> new ListCell<KlArea.Factory>() {
                @Override
                protected void updateItem(KlArea.Factory factory, boolean empty) {
                    super.updateItem(factory, empty);
                    if (empty || factory == null) {
                        setText(null);
                    } else {
                        setText(factory.factoryName());

                    }
                }
            });
            // Also customize the cell shown when not dropped down (button cell)
            comboBox.setButtonCell(new ListCell<KlArea.Factory>() {
                @Override
                protected void updateItem(KlArea.Factory factory, boolean empty) {
                    super.updateItem(factory, empty);
                    if (empty || factory == null) {
                        setText(null);
                    } else {
                        setText(factory.factoryName());
                        Tooltip tooltip = new Tooltip(factory.productName() + ": " + factory.productDescription());
                        comboBox.setTooltip(tooltip);

                    }
                }
            });


            this.comboBox.getItems().addAll(services);
            currentFactory.ifPresent(this.comboBox::setValue);

            this.comboBox.valueProperty().addListener((obs, old, newValue) ->
                    item.setValue(newValue.getClass().getName()));
        }

        @Override
        public Node getEditor() {
            return new HBox(comboBox);
        }

        @Override
        public String getValue() {
            return comboBox.getValue().getClass().getName();
        }

        @Override
        public void setValue(String value) {
            for (int i = 0; i < comboBox.getItems().size(); i++) {
                if (comboBox.getItems().get(i).getClass().getName().equals(value)) {
                    comboBox.getSelectionModel().select(i);
                    break;
                }
            }
         }
    }

}
