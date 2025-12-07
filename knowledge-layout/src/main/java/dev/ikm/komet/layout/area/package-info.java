/**
 * Defines the type-safe area hierarchy for the Knowledge Layout framework.
 *
 * <h2>Overview</h2>
 * This package provides the <b>Marker-Generic-Concrete (MGC)</b> pattern implementation
 * for field areas. The MGC pattern enables type-safe, extensible UI components while
 * maintaining a controlled, sealed hierarchy.
 *
 * <h2>The Marker-Generic-Concrete (MGC) Pattern</h2>
 * The MGC pattern organizes types into three layers:
 * <ol>
 *   <li><b>Marker (Sealed)</b>: Defines the contract and controls which types are permitted</li>
 *   <li><b>Generic (Non-sealed)</b>: Fixes type parameters for specific data types</li>
 *   <li><b>Concrete</b>: Implementation classes in the {@code layout_engine} package</li>
 * </ol>
 *
 * <h3>Example: Field Area Hierarchy</h3>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ MARKER LAYER (Sealed)                                           │
 * │   KlAreaForFeature&lt;DT, F, FX&gt;                                   │
 * │   - Defines the contract for all feature areas                  │
 * │   - permits: KlAreaForBoolean, KlAreaForString, KlAreaForConcept│
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ GENERIC LAYER (Non-sealed)                                      │
 * │   KlAreaForBoolean&lt;FX&gt;  → DT=Boolean, F=Feature&lt;Boolean&gt;        │
 * │   KlAreaForString&lt;FX&gt;   → DT=String, F=Feature&lt;String&gt;          │
 * │   KlAreaForConcept&lt;FX&gt;  → DT=ConceptFacade, F=Feature&lt;...&gt;      │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ CONCRETE LAYER (in layout_engine.component.area)                │
 * │   BooleanFieldArea extends EditableFieldAreaBlueprint           │
 * │                    implements KlAreaForBoolean&lt;StackPane&gt;       │
 * │   StringFieldArea  extends EditableFieldAreaBlueprint           │
 * │                    implements KlAreaForString&lt;StackPane&gt;        │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Adding a New Field Type</h2>
 * To add support for a new data type (e.g., {@code Integer}, {@code ConceptSet}), follow these steps:
 *
 * <h3>Step 1: Add to the Sealed Hierarchy</h3>
 * Update {@link KlAreaForFeature} to permit your new interface:
 * <pre>{@code
 * public sealed interface KlAreaForFeature<DT, F extends Feature<DT>, FX extends Region>
 *     permits KlAreaForBoolean, KlAreaForString, KlAreaForInteger, ... // Add here
 * }</pre>
 *
 * <h3>Step 2: Create the Generic Interface</h3>
 * Create a new interface in this package (e.g., {@code KlAreaForInteger.java}):
 * <pre>{@code
 * @FullyQualifiedName("Knowledge layout integer field area")
 * @RegularName("Integer field area")
 * @ParentConcept(KlAreaForFeature.class)
 * public non-sealed interface KlAreaForInteger<FX extends Region>
 *         extends KlAreaForFeature<Integer, Feature<Integer>, FX> {
 *
 *     interface Factory<FX extends Region>
 *             extends KlAreaForFeature.Factory<Integer, Feature<Integer>, FX, KlAreaForInteger<FX>> {
 *     }
 * }
 * }</pre>
 *
 * <h3>Step 3: Create the Concrete Implementation</h3>
 * Create a new class in {@code layout_engine.component.area}:
 * <pre>{@code
 * public final class IntegerFieldArea extends EditableFieldAreaBlueprint<Integer, StackPane>
 *         implements KlAreaForInteger<StackPane> {
 *
 *     // Create your control
 *     protected void createControl() { ... }
 *
 *     // Bind/unbind to editable
 *     protected void bindControlToEditable(ObservableField.Editable<Integer> editable) { ... }
 *     protected void unbindControlFromEditable() { ... }
 *
 *     // Factory
 *     public static class Factory implements KlAreaForInteger.Factory<StackPane> { ... }
 * }
 * }</pre>
 *
 * <h3>Step 4: Create the UI Control (if needed)</h3>
 * If no suitable JavaFX control exists, create one in {@code layout_engine.controls}:
 * <pre>{@code
 * public class KlIntegerControl extends Control {
 *     private final ObjectProperty<Integer> value = new SimpleObjectProperty<>();
 *     public ObjectProperty<Integer> valueProperty() { return value; }
 *     // ... title, promptText, skin, etc.
 * }
 * }</pre>
 *
 * <h2>Supported Data Types</h2>
 * <table border="1">
 *   <caption>Current KlAreaForFeature Implementations</caption>
 *   <tr><th>Interface</th><th>Data Type</th><th>Implementation</th><th>Status</th></tr>
 *   <tr><td>{@link KlAreaForBoolean}</td><td>Boolean</td><td>BooleanFieldArea</td><td>✅ Complete</td></tr>
 *   <tr><td>{@link KlAreaForString}</td><td>String</td><td>StringFieldArea</td><td>✅ Complete</td></tr>
 *   <tr><td>{@link KlAreaForConcept}</td><td>ConceptFacade</td><td>-</td><td>⏳ Planned</td></tr>
 *   <tr><td>{@link KlAreaForPattern}</td><td>PatternFacade</td><td>-</td><td>⏳ Planned</td></tr>
 *   <tr><td>{@link KlAreaForPublicId}</td><td>PublicId</td><td>PublicIdArea</td><td>✅ Read-only</td></tr>
 *   <tr><td>KlAreaForInteger</td><td>Integer</td><td>-</td><td>❌ Not yet created</td></tr>
 *   <tr><td>KlAreaForConceptSet</td><td>IntIdSet</td><td>-</td><td>❌ Not yet created</td></tr>
 * </table>
 *
 * <h2>Related Packages</h2>
 * <ul>
 *   <li>{@code layout_engine.blueprint} - Base classes including {@code EditableFieldAreaBlueprint}</li>
 *   <li>{@code layout_engine.component.area} - Concrete implementations</li>
 *   <li>{@code layout_engine.controls} - JavaFX controls for editing</li>
 *   <li>{@code framework.observable} - Observable data model</li>
 * </ul>
 *
 * @see KlAreaForFeature
 * @see KlAreaForBoolean
 * @see KlAreaForString
 * @see dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint
 */
package dev.ikm.komet.layout.area;