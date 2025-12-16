/**
 * Concrete implementations of Knowledge Layout field areas.
 *
 * <h2>Overview</h2>
 * This package contains the concrete implementations of field areas that users
 * interact with to edit Tinkar entity field values. Each area:
 * <ul>
 *   <li>Extends {@link dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint}</li>
 *   <li>Implements a type-specific interface from {@code layout.area} (e.g., {@link dev.ikm.komet.layout.area.KlAreaForBoolean})</li>
 *   <li>Uses a custom control from {@code layout_engine.controls}</li>
 *   <li>Provides a static {@code Factory} for creation and restoration</li>
 * </ul>
 *
 * <h2>Architecture Summary</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  layout.area (Interfaces)           layout_engine.blueprint (Base)      │
 * │  ┌─────────────────────┐           ┌───────────────────────────────┐    │
 * │  │ KlAreaForBoolean    │           │ EditableFieldAreaBlueprint    │    │
 * │  │ KlAreaForString     │           │   - rebind()                  │    │
 * │  │ KlAreaForInteger    │           │   - subscription management   │    │
 * │  │ ...                 │           │   - save/revert delegation    │    │
 * │  └─────────────────────┘           └───────────────────────────────┘    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                    │                               │
 *                    └───────────┬───────────────────┘
 *                                ▼
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  layout_engine.component.area (Concrete - THIS PACKAGE)                 │
 * │  ┌─────────────────────────────────────────────────────────────────┐    │
 * │  │ BooleanFieldArea                                                │    │
 * │  │   extends EditableFieldAreaBlueprint&lt;Boolean, StackPane&gt;        │    │
 * │  │   implements KlAreaForBoolean&lt;StackPane&gt;                        │    │
 * │  │   uses: KlBooleanControl                                        │    │
 * │  └─────────────────────────────────────────────────────────────────┘    │
 * │  ┌─────────────────────────────────────────────────────────────────┐    │
 * │  │ StringFieldArea                                                 │    │
 * │  │   extends EditableFieldAreaBlueprint&lt;String, StackPane&gt;         │    │
 * │  │   implements KlAreaForString&lt;StackPane&gt;                         │    │
 * │  │   uses: KlStringControl                                         │    │
 * │  └─────────────────────────────────────────────────────────────────┘    │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Implementation Checklist</h2>
 * When creating a new field area, ensure you:
 * <ol>
 *   <li>✅ Extend {@code EditableFieldAreaBlueprint<DT, StackPane>}</li>
 *   <li>✅ Implement the appropriate {@code KlAreaFor<Type><StackPane>} interface</li>
 *   <li>✅ Create the UI control in {@code createControl()}</li>
 *   <li>✅ Return the control's value property in {@code getControlValueProperty()}</li>
 *   <li>✅ Implement bidirectional binding in {@code bindControlToEditable()}</li>
 *   <li>✅ Implement unbinding in {@code unbindControlFromEditable()}</li>
 *   <li>✅ Set title in {@code updateControlTitle()}</li>
 *   <li>✅ Add context menu setup in instance initializer</li>
 *   <li>✅ Provide both constructors (preferences restore & factory create)</li>
 *   <li>✅ Implement the static {@code Factory} class</li>
 *   <li>✅ Provide static convenience methods: {@code factory()}, {@code restore()}, {@code create()}</li>
 * </ol>
 *
 * <h2>Complete Implementation Template</h2>
 * <pre>{@code
 * public final class IntegerFieldArea extends EditableFieldAreaBlueprint<Integer, StackPane>
 *         implements KlAreaForInteger<StackPane> {
 *
 *     private KlIntegerControl integerControl;
 *
 *     // Instance initializer for context menu
 *     {
 *         fxObject().setOnContextMenuRequested(event ->
 *             LayoutContextMenu.makeContextMenu(this).show(fxObject(), event.getScreenX(), event.getScreenY()));
 *     }
 *
 *     // Constructor for restoring from preferences
 *     public IntegerFieldArea(KometPreferences preferences) {
 *         super(preferences, new StackPane());
 *     }
 *
 *     // Constructor for creating new
 *     public IntegerFieldArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
 *         super(preferencesFactory, areaFactory, new StackPane());
 *     }
 *
 *     @Override
 *     protected void createControl() {
 *         integerControl = new KlIntegerControl();
 *         fxObject().getChildren().add(integerControl);
 *     }
 *
 *     @Override
 *     protected Property<Integer> getControlValueProperty() {
 *         return integerControl.valueProperty();
 *     }
 *
 *     @Override
 *     protected void bindControlToEditable(ObservableField.Editable<Integer> editable) {
 *         integerControl.valueProperty().bindBidirectional(editable.editableValueProperty());
 *         addEditableSubscription(
 *             editable.editableValueProperty().subscribe((oldVal, newVal) -> {
 *                 if (newVal != null && !newVal.equals(oldVal)) {
 *                     editable.setValue(newVal);
 *                 }
 *             })
 *         );
 *     }
 *
 *     @Override
 *     protected void unbindControlFromEditable() {
 *         if (getEditable() != null) {
 *             integerControl.valueProperty().unbindBidirectional(getEditable().editableValueProperty());
 *         }
 *     }
 *
 *     @Override
 *     protected void updateControlTitle(String title) {
 *         integerControl.setTitle(title);
 *     }
 *
 *     // --- Static Factory Methods ---
 *
 *     public static Factory factory() {
 *         return new Factory();
 *     }
 *
 *     public static IntegerFieldArea restore(KometPreferences preferences) {
 *         return factory().restore(preferences);
 *     }
 *
 *     public static IntegerFieldArea create(KlPreferencesFactory preferencesFactory,
 *                                           AreaGridSettings areaGridSettings) {
 *         return factory().create(preferencesFactory, areaGridSettings);
 *     }
 *
 *     public static IntegerFieldArea create(KlPreferencesFactory preferencesFactory) {
 *         return factory().create(preferencesFactory);
 *     }
 *
 *     // --- Factory Implementation ---
 *
 *     public static class Factory implements KlAreaForInteger.Factory<StackPane> {
 *
 *         public Factory() {}
 *
 *         @Override
 *         public IntegerFieldArea restore(KometPreferences preferences) {
 *             return new IntegerFieldArea(preferences);
 *         }
 *
 *         @Override
 *         public IntegerFieldArea create(KlPreferencesFactory preferencesFactory,
 *                                        AreaGridSettings areaGridSettings) {
 *             IntegerFieldArea area = new IntegerFieldArea(preferencesFactory, this);
 *             area.setAreaLayout(areaGridSettings);
 *             return area;
 *         }
 *
 *         @Override
 *         public IntegerFieldArea create(KlPreferencesFactory preferencesFactory) {
 *             IntegerFieldArea area = new IntegerFieldArea(preferencesFactory, this);
 *             area.setAreaLayout(defaultAreaGridSettings());
 *             return area;
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // 1. Create the area
 * IntegerFieldArea integerArea = IntegerFieldArea.create(preferencesFactory);
 *
 * // 2. Get editable from ObservableComposer
 * var composer = ObservableComposer.builder()
 *     .viewCalculator(viewCalc).author(author).module(module).path(path).build();
 * var semanticComposer = composer.composeSemantic(publicId, concept, pattern);
 * var fields = semanticComposer.getEditableVersion().getEditableFields();
 * ObservableField.Editable<Integer> integerEditable =
 *     (ObservableField.Editable<Integer>) fields.get(index);
 *
 * // 3. Connect area to editable
 * integerArea.setEditable(integerEditable);
 *
 * // 4. Add to parent layout
 * gridPane.add(integerArea.fxObject(), col, row);
 *
 * // 5. User edits value...
 *
 * // 6. Save when ready
 * semanticComposer.save();
 * composer.commit();
 * }</pre>
 *
 * @see BooleanFieldArea
 * @see StringFieldArea
 * @see dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint
 * @see dev.ikm.komet.framework.observable.ObservableComposer
 */
package dev.ikm.komet.layout_engine.component.area;