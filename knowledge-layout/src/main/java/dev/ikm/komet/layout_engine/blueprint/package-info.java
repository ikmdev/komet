/**
 * Blueprint base classes for building Knowledge Layout areas with JavaFX binding support.
 *
 * <h2>Overview</h2>
 * This package provides abstract base classes ("blueprints") that handle the common
 * plumbing required for Knowledge Layout areas. Blueprints manage:
 * <ul>
 *   <li>Preference persistence and restoration</li>
 *   <li>JavaFX property binding and subscription lifecycle</li>
 *   <li>Context management and lifecycle hooks</li>
 *   <li>Save/revert operations</li>
 * </ul>
 *
 * <h2>Blueprint Hierarchy</h2>
 * <pre>
 * StateAndContextBlueprint&lt;FX&gt;
 *     │
 *     ├── AreaBlueprint&lt;FX extends Region&gt;
 *     │       │
 *     │       ├── FeatureAreaBlueprint&lt;DT, F, FX&gt;
 *     │       │       │
 *     │       │       └── EditableFieldAreaBlueprint&lt;DT, FX&gt;  ← USE THIS FOR EDITABLE FIELDS
 *     │       │
 *     │       ├── FeatureListAreaBlueprint
 *     │       ├── ParentAreaBlueprint
 *     │       └── SupplementalAreaBlueprint
 *     │
 *     └── FxWindow, RenderView
 * </pre>
 *
 * <h2>EditableFieldAreaBlueprint - The Key Class for Field Editing</h2>
 * {@link EditableFieldAreaBlueprint} is the primary base class for creating editable field areas.
 * It handles all the "plumbing" so subclasses can focus purely on the UI control.
 *
 * <h3>What EditableFieldAreaBlueprint Provides</h3>
 * <ul>
 *   <li>Management of {@link dev.ikm.komet.framework.observable.ObservableField.Editable} reference</li>
 *   <li>{@link EditableFieldAreaBlueprint#rebind} mechanism for swapping editables</li>
 *   <li>Automatic subscription cleanup on unbind/rebind</li>
 *   <li>Delegation of {@code save()}/{@code revert()} to the editable</li>
 *   <li>Title derivation from field metadata</li>
 * </ul>
 *
 * <h3>What Subclasses Must Implement</h3>
 * <table border="1">
 *   <caption>Abstract Methods in EditableFieldAreaBlueprint</caption>
 *   <tr><th>Method</th><th>Purpose</th></tr>
 *   <tr><td>{@code createControl()}</td><td>Create and add the UI control to {@code fxObject()}</td></tr>
 *   <tr><td>{@code getControlValueProperty()}</td><td>Return the control's value property for binding</td></tr>
 *   <tr><td>{@code bindControlToEditable(editable)}</td><td>Set up bidirectional binding</td></tr>
 *   <tr><td>{@code unbindControlFromEditable()}</td><td>Remove bindings from previous editable</td></tr>
 *   <tr><td>{@code updateControlTitle(title)}</td><td>Set the control's title/label</td></tr>
 * </table>
 *
 * <h3>Minimal Implementation Example</h3>
 * <pre>{@code
 * public final class IntegerFieldArea extends EditableFieldAreaBlueprint<Integer, StackPane>
 *         implements KlAreaForInteger<StackPane> {
 *
 *     private KlIntegerControl integerControl;
 *
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
 *             editable.editableValueProperty().subscribe((old, newVal) -> {
 *                 if (newVal != null) editable.setValue(newVal);
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
 *     // Factory implementation...
 * }
 * }</pre>
 *
 * <h2>Usage with ObservableComposer</h2>
 * Field areas are designed to work with {@link dev.ikm.komet.framework.observable.ObservableComposer}:
 * <pre>{@code
 * // Create the area
 * IntegerFieldArea integerArea = IntegerFieldArea.create(preferencesFactory);
 *
 * // Get editable fields from ObservableComposer
 * var semanticComposer = composer.composeSemantic(publicId, concept, pattern);
 * ObservableList<ObservableField.Editable<?>> fields =
 *     semanticComposer.getEditableVersion().getEditableFields();
 *
 * // Find the integer field and bind it
 * ObservableField.Editable<Integer> integerEditable =
 *     (ObservableField.Editable<Integer>) fields.get(fieldIndex);
 * integerArea.setEditable(integerEditable);
 *
 * // Add to layout
 * parentPane.getChildren().add(integerArea.fxObject());
 *
 * // Later, when user saves
 * semanticComposer.save();
 * composer.commit();
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │ CONSTRUCTION                                                  │
 * │   1. Super constructor initializes preferences & context      │
 * │   2. createControl() called → subclass creates UI control     │
 * └───────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌───────────────────────────────────────────────────────────────┐
 * │ BINDING (when setEditable() is called)                        │
 * │   1. unbindControlFromEditable() on previous                  │
 * │   2. Clear all editable subscriptions                         │
 * │   3. bindControlToEditable() on new                           │
 * │   4. updateControlTitle() with derived title                  │
 * └───────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌───────────────────────────────────────────────────────────────┐
 * │ EDITING (user interacts with control)                         │
 * │   - Bidirectional binding keeps editable in sync              │
 * │   - Changes cached in editable until save()                   │
 * └───────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌───────────────────────────────────────────────────────────────┐
 * │ SAVE / REVERT                                                 │
 * │   subAreaSave() → delegated to ObservableComposer.commit()    │
 * │   subAreaRevert() → calls editable.reset()                    │
 * └───────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌───────────────────────────────────────────────────────────────┐
 * │ CLEANUP (when area is removed)                                │
 * │   knowledgeLayoutUnbind() → rebind(null) clears subscriptions │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * @see EditableFieldAreaBlueprint
 * @see FeatureAreaBlueprint
 * @see dev.ikm.komet.framework.observable.ObservableField.Editable
 * @see dev.ikm.komet.framework.observable.ObservableComposer
 */
package dev.ikm.komet.layout_engine.blueprint;