/**
 * Custom JavaFX controls for editing Tinkar field values.
 *
 * <h2>Overview</h2>
 * This package provides specialized JavaFX {@link javafx.scene.control.Control} implementations
 * for editing various data types used in Tinkar semantics. Each control follows a consistent
 * pattern with:
 * <ul>
 *   <li>A {@code titleProperty()} for the field label</li>
 *   <li>A {@code valueProperty()} (or type-specific equivalent) for bidirectional binding</li>
 *   <li>A {@code promptTextProperty()} for placeholder/hint text</li>
 *   <li>An embedded {@link javafx.scene.control.Skin} implementation</li>
 * </ul>
 *
 * <h2>Control Naming Convention</h2>
 * Controls follow the pattern {@code Kl<DataType>Control}:
 * <ul>
 *   <li>{@link KlBooleanControl} - For {@code Boolean} values</li>
 *   <li>{@link KlStringControl} - For {@code String} values</li>
 *   <li>{@code KlIntegerControl} - For {@code Integer} values (to be created)</li>
 *   <li>{@code KlConceptControl} - For {@code ConceptFacade} values (to be created)</li>
 * </ul>
 *
 * <h2>Creating a New Control</h2>
 *
 * <h3>Step 1: Define the Control Class</h3>
 * <pre>{@code
 * public class KlIntegerControl extends Control {
 *
 *     public KlIntegerControl() {
 *         getStyleClass().add("kl-integer-control");
 *     }
 *
 *     // -- title
 *     private final StringProperty title = new SimpleStringProperty(this, "title");
 *     public StringProperty titleProperty() { return title; }
 *     public String getTitle() { return title.get(); }
 *     public void setTitle(String value) { title.set(value); }
 *
 *     // -- value (use ObjectProperty<Integer> to allow null)
 *     private final ObjectProperty<Integer> value = new SimpleObjectProperty<>(this, "value", 0);
 *     public ObjectProperty<Integer> valueProperty() { return value; }
 *     public Integer getValue() { return value.get(); }
 *     public void setValue(Integer value) { this.value.set(value); }
 *
 *     // -- prompt text
 *     private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "Enter number");
 *     public StringProperty promptTextProperty() { return promptText; }
 *     public String getPromptText() { return promptText.get(); }
 *     public void setPromptText(String value) { promptText.set(value); }
 *
 *     @Override
 *     protected Skin<?> createDefaultSkin() {
 *         return new KlIntegerControlSkin(this);
 *     }
 * }
 * }</pre>
 *
 * <h3>Step 2: Define the Skin</h3>
 * <pre>{@code
 * public class KlIntegerControlSkin extends SkinBase<KlIntegerControl> {
 *
 *     private final VBox mainContainer = new VBox();
 *     private final Label titleLabel = new Label();
 *     private final Spinner<Integer> spinner = new Spinner<>();
 *
 *     public KlIntegerControlSkin(KlIntegerControl control) {
 *         super(control);
 *
 *         mainContainer.getChildren().addAll(titleLabel, spinner);
 *         getChildren().add(mainContainer);
 *
 *         // Title binding
 *         titleLabel.textProperty().bind(control.titleProperty());
 *         titleLabel.getStyleClass().add("kl-editable-title-label");
 *
 *         // Spinner setup
 *         spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
 *             Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
 *         spinner.setEditable(true);
 *
 *         // Bidirectional sync
 *         control.valueProperty().addListener((obs, old, newVal) -> {
 *             if (newVal != null) spinner.getValueFactory().setValue(newVal);
 *         });
 *         spinner.valueProperty().addListener((obs, old, newVal) -> {
 *             if (newVal != null) control.setValue(newVal);
 *         });
 *
 *         // CSS
 *         mainContainer.getStyleClass().add("kl-main-container");
 *     }
 * }
 * }</pre>
 *
 * <h3>Step 3: Create CSS (Optional)</h3>
 * Place in {@code resources/dev/ikm/komet/layout_engine/controls/integer-control.css}:
 * <pre>{@code
 * .kl-integer-control {
 *     -fx-padding: 5px;
 * }
 * .kl-integer-control .kl-editable-title-label {
 *     -fx-font-weight: bold;
 * }
 * .kl-integer-control .kl-main-container {
 *     -fx-spacing: 5px;
 * }
 * }</pre>
 *
 * <h2>Control Design Patterns</h2>
 *
 * <h3>Property Pattern</h3>
 * All controls use JavaFX properties for clean binding:
 * <pre>{@code
 * // In your field area:
 * myControl.valueProperty().bindBidirectional(editable.editableValueProperty());
 * }</pre>
 *
 * <h3>Null Handling</h3>
 * Use {@code ObjectProperty<T>} instead of primitive properties to support null values:
 * <pre>{@code
 * // Good - supports null
 * private final ObjectProperty<Boolean> value = new SimpleObjectProperty<>(Boolean.FALSE);
 *
 * // Avoid for nullable fields
 * private final BooleanProperty value = new SimpleBooleanProperty(false);
 * }</pre>
 *
 * <h3>Skin Separation</h3>
 * Keep visual logic in the Skin, not the Control:
 * <ul>
 *   <li><b>Control:</b> Properties, behavior, CSS loading</li>
 *   <li><b>Skin:</b> Layout, child nodes, event handling</li>
 * </ul>
 *
 * <h2>Available Controls</h2>
 * <table border="1">
 *   <caption>KL Controls</caption>
 *   <tr><th>Control</th><th>Data Type</th><th>Inner Widget</th></tr>
 *   <tr><td>{@link KlBooleanControl}</td><td>Boolean</td><td>ComboBox&lt;Boolean&gt;</td></tr>
 *   <tr><td>{@link KlStringControl}</td><td>String</td><td>TextField</td></tr>
 *   <tr><td>KlIntegerControl</td><td>Integer</td><td>Spinner&lt;Integer&gt;</td></tr>
 *   <tr><td>KlConceptControl</td><td>ConceptFacade</td><td>ComboBox + Search</td></tr>
 *   <tr><td>KlConceptSetControl</td><td>IntIdSet</td><td>ListView + Add/Remove</td></tr>
 * </table>
 *
 * @see KlBooleanControl
 * @see KlStringControl
 * @see dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint
 */
package dev.ikm.komet.layout_engine.controls;