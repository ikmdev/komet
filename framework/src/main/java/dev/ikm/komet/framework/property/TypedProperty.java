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
package dev.ikm.komet.framework.property;

import org.carlfx.cognitive.viewmodel.ViewModel;

/**
 * Type-safe property wrapper for Cognitive ViewModel properties.
 * <p>
 * Provides compile-time type safety for property access while delegating to the underlying
 * Cognitive framework's property system. This is a specialized version of
 * {@link dev.ikm.tinkar.common.util.TypedKey} designed specifically for the MVVM pattern
 * and Cognitive ViewModels.
 *
 * <h2>Why TypedProperty Exists: The ViewModel Property Problem</h2>
 * <p>
 * Cognitive ViewModels use a dynamic property system with string keys and Object values:
 * <pre>{@code
 * // Traditional approach - unsafe and error-prone:
 * public class StampViewModel extends FormViewModel {
 *     enum Properties { MODULE, PATH, STATUS }
 *
 *     public void save() {
 *         // Unsafe casts everywhere:
 *         EntityFacade moduleFromForm = (EntityFacade) getValue(MODULE);
 *         ObservableConcept module = (ObservableConcept) moduleFromForm;  // ⚠️ Unsafe!
 *
 *         // Redundant conversions:
 *         EntityFacade canonical = EntityHandle.get(moduleFromForm)
 *             .asEntity()
 *             .map(entity -> (EntityFacade) entity)  // ⚠️ Pointless!
 *             .orElse(moduleFromForm);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * {@code TypedProperty} eliminates these problems:
 * <pre>{@code
 * // Type-safe approach - clean and correct:
 * public class StampViewModel extends FormViewModel {
 *     public static final TypedProperty<ObservableConcept> MODULE =
 *         TypedProperty.of("MODULE", ObservableConcept.class);
 *
 *     public void save() {
 *         ObservableConcept module = MODULE.getFrom(this);  // ✓ Type-safe, no casts!
 *         // Use directly - already canonical
 *     }
 * }
 * }</pre>
 *
 * <h2>Benefits</h2>
 * <ul>
 *   <li><b>Compile-time safety:</b> Type errors caught by compiler, not at runtime</li>
 *   <li><b>IDE support:</b> Full autocomplete with correct types</li>
 *   <li><b>Self-documenting:</b> Property type is explicit in the declaration</li>
 *   <li><b>Eliminates unsafe casts:</b> No more {@code (ObservableConcept)} style casts</li>
 *   <li><b>Eliminates redundant conversions:</b> Store canonical types directly</li>
 *   <li><b>Runtime validation:</b> Additional safety check if type mismatch occurs</li>
 *   <li><b>Zero framework changes:</b> Works with existing Cognitive ViewModel</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Define typed property constants (usually in a dedicated class)
 * public static final TypedProperty<ObservableConcept> MODULE =
 *     TypedProperty.of("MODULE", ObservableConcept.class);
 * public static final TypedProperty<State> STATUS =
 *     TypedProperty.of("STATUS", State.class);
 *
 * // Type-safe access - compiler enforces types!
 * ObservableConcept module = MODULE.getFrom(viewModel);
 * MODULE.setTo(viewModel, observableConcept);
 *
 * // Compile error - wrong type:
 * // MODULE.setTo(viewModel, "string");  // ✗ Won't compile!
 * }</pre>
 *
 * <h2>When to Use TypedProperty vs Alternatives</h2>
 * <table border="1" cellpadding="5">
 * <caption>Choosing the Right Property Type</caption>
 * <tr>
 *   <th>Situation</th>
 *   <th>Use This</th>
 *   <th>Rationale</th>
 * </tr>
 * <tr>
 *   <td>ViewModel properties (UI layer)</td>
 *   <td>{@code TypedProperty<T>}</td>
 *   <td>Integrates with Cognitive framework</td>
 * </tr>
 * <tr>
 *   <td>General Map-based storage</td>
 *   <td>{@link dev.ikm.tinkar.common.util.TypedKey}</td>
 *   <td>Framework-agnostic, more general</td>
 * </tr>
 * <tr>
 *   <td>Observable entity properties</td>
 *   <td>JavaFX Property wrappers</td>
 *   <td>Built-in observable support</td>
 * </tr>
 * <tr>
 *   <td>Fixed ViewModel structure</td>
 *   <td>Regular fields with getters/setters</td>
 *   <td>More efficient than dynamic properties</td>
 * </tr>
 * </table>
 *
 * <h2>Architectural Context</h2>
 * <p>
 * {@code TypedProperty} is part of a layered type-safe property system:
 * <ul>
 *   <li><b>tinkar-common:</b> {@link dev.ikm.tinkar.common.util.TypedKey} - Generic map-based type safety</li>
 *   <li><b>komet-framework:</b> {@code TypedProperty} - ViewModel-specific specialization (this class)</li>
 *   <li><b>Application layer:</b> Domain-specific constants (e.g., {@code StampProperties})</li>
 * </ul>
 * <p>
 * Use {@code TypedProperty} when working with Cognitive ViewModels in the UI layer.
 * Use {@code TypedKey} for general-purpose maps, caches, or configuration.
 *
 * <h2>Migration from Raw Properties</h2>
 * <pre>{@code
 * // Before (unsafe):
 * Object value = viewModel.getPropertyValue("MODULE");
 * ObservableConcept module = (ObservableConcept) value;  // Unsafe cast!
 *
 * // After (type-safe):
 * ObservableConcept module = MODULE.getFrom(viewModel);  // Type-safe!
 * }</pre>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li><b>Create constant classes:</b> Define all properties in dedicated classes (e.g., {@code StampProperties})</li>
 *   <li><b>Store canonical types:</b> Store {@code ObservableConcept} instead of {@code EntityFacade}</li>
 *   <li><b>Use descriptive names:</b> Property names should clearly indicate purpose and type</li>
 *   <li><b>Document ownership:</b> Clarify which ViewModel manages which properties</li>
 *   <li><b>Group related properties:</b> Organize by feature or domain</li>
 * </ul>
 *
 * @param <T> the type of value this property holds
 * @see dev.ikm.tinkar.common.util.TypedKey for general-purpose type-safe map keys
 */
public final class TypedProperty<T> {
    private final String key;
    private final Class<T> type;

    /**
     * Private constructor - use {@link #of(String, Class)} factory method.
     *
     * @param key the property key for underlying ViewModel storage
     * @param type the expected type of values for this property
     */
    private TypedProperty(String key, Class<T> type) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Property type cannot be null");
        }
        this.key = key;
        this.type = type;
    }

    /**
     * Creates a typed property with the specified key and type.
     * <p>
     * This is the primary factory method for creating TypedProperty instances.
     * Typically used to define property constants in a dedicated class.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * public static final TypedProperty<ObservableConcept> MODULE =
     *     TypedProperty.of("MODULE", ObservableConcept.class);
     * }</pre>
     *
     * @param <T> the type of value this property holds
     * @param key the property key (must not be null or blank)
     * @param type the expected value type (must not be null)
     * @return a new TypedProperty instance
     * @throws IllegalArgumentException if key is null/blank or type is null
     */
    public static <T> TypedProperty<T> of(String key, Class<T> type) {
        return new TypedProperty<>(key, type);
    }

    /**
     * Gets the typed value from the specified ViewModel.
     * <p>
     * Returns the value with compile-time type safety. Performs a runtime type check
     * and throws ClassCastException if the stored value doesn't match the expected type.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * ObservableConcept module = MODULE.getFrom(this);
     * }</pre>
     *
     * @param vm the ViewModel containing the property
     * @return the typed property value, or null if not set
     * @throws ClassCastException if stored value doesn't match expected type
     */
    public T getFrom(ViewModel vm) {
        Object value = vm.getPropertyValue(key);
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException(
                "Property '" + key + "' expected " + type.getSimpleName() +
                " but found " + value.getClass().getSimpleName()
            );
        }
        return type.cast(value);
    }

    /**
     * Sets the typed value in the specified ViewModel.
     * <p>
     * Provides compile-time type safety through generic parameter. Performs runtime
     * validation to ensure type correctness before storing.
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * MODULE.setTo(this, observableConcept);
     * }</pre>
     *
     * @param vm the ViewModel to store the property in
     * @param value the value to store (must match expected type)
     * @throws ClassCastException if value doesn't match expected type
     */
    public void setTo(ViewModel vm, T value) {
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException(
                "Property '" + key + "' expects " + type.getSimpleName() +
                " but received " + value.getClass().getSimpleName()
            );
        }
        vm.setPropertyValue(key, value);
    }

    /**
     * Returns the property key used for underlying storage.
     *
     * @return the property key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the expected type of values for this property.
     *
     * @return the property value type
     */
    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TypedProperty[" + key + ": " + type.getSimpleName() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TypedProperty<?> other)) return false;
        return key.equals(other.key) && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + type.hashCode();
    }
}
