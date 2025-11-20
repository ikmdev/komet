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
package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.property.TypedProperty;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.State;

import java.util.List;

/**
 * Type-safe property constants for Stamp form ViewModels using enum-based pattern.
 * <p>
 * Provides compile-time type safety for all STAMP-related properties used in
 * {@link StampFormViewModelBase} and its subclasses. Uses an inner enum for
 * enumeration benefits (iteration, exhaustiveness) while maintaining full type safety
 * through static constants.
 *
 * <h2>Benefits of Enum-Based Typed Properties</h2>
 * <ul>
 *   <li><b>Compile-time safety:</b> Wrong types caught by compiler, not at runtime</li>
 *   <li><b>No unsafe casts:</b> Eliminates {@code (ObservableConcept)} style casts</li>
 *   <li><b>Enumeration:</b> Can iterate over all properties for validation</li>
 *   <li><b>Exhaustiveness:</b> Switch statements checked for completeness</li>
 *   <li><b>Self-documenting:</b> Property type and membership explicit</li>
 *   <li><b>IDE support:</b> Full autocomplete with correct return types</li>
 * </ul>
 *
 * <h2>Usage Pattern 1: Type-Safe Access (Primary)</h2>
 * <pre>{@code
 * // Use static constants - fully type-safe!
 * ObservableConcept module = StampProperties.MODULE.getFrom(viewModel);
 * State status = StampProperties.STATUS.getFrom(viewModel);
 *
 * // Compiler enforces correct types
 * StampProperties.MODULE.setTo(viewModel, observableConcept);
 *
 * // Compile error if wrong type:
 * // StampProperties.MODULE.setTo(viewModel, "string");  // ✗ Won't compile!
 * }</pre>
 *
 * <h2>Usage Pattern 2: Iteration for Validation</h2>
 * <pre>{@code
 * // Validate all STAMP coordinates are set
 * for (StampProperties.Keys key : StampProperties.coordinateKeys()) {
 *     if (!key.property().containsIn(viewModel)) {
 *         throw new ValidationException("Missing: " + key);
 *     }
 * }
 *
 * // Debug: print all set properties
 * for (StampProperties.Keys key : StampProperties.allKeys()) {
 *     if (key.property().containsIn(viewModel)) {
 *         System.out.println(key + " = " + key.property().getFrom(viewModel));
 *     }
 * }
 * }</pre>
 *
 * <h2>Usage Pattern 3: Exhaustive Switching</h2>
 * <pre>{@code
 * public void validate(StampProperties.Keys key) {
 *     switch (key) {
 *         case MODULE -> validateModule();
 *         case PATH -> validatePath();
 *         case STATUS -> validateStatus();
 *         // Compiler ensures all keys covered!
 *     }
 * }
 * }</pre>
 *
 * <h2>Migration from Legacy Properties</h2>
 * <pre>{@code
 * // Before (unsafe):
 * EntityFacade moduleFromForm = getValue(Properties.MODULE);
 * ObservableConcept module = (ObservableConcept) moduleFromForm;  // ⚠️ Unsafe cast!
 *
 * // After (type-safe):
 * ObservableConcept module = StampProperties.MODULE.getFrom(this);  // ✓ Safe!
 * }</pre>
 *
 * @see TypedProperty
 * @see StampFormViewModelBase
 */
public final class StampProperties {

    /**
     * Inner enum providing enumeration and sealed type benefits.
     * <p>
     * Each enum constant wraps a {@link TypedProperty} with the correct type.
     * Type safety is achieved through the static constant wrappers below,
     * while the enum provides iteration and exhaustiveness checking.
     */
    public enum Keys {
        // STAMP coordinate properties
        CURRENT_STAMP(Stamp.class),
        STATUS(State.class),
        AUTHOR(ObservableConcept.class),
        MODULE(ObservableConcept.class),
        PATH(ObservableConcept.class),
        TIME(Long.class),

        // Form state properties
        IS_STAMP_VALUES_THE_SAME_OR_EMPTY(Boolean.class),
        IS_CONFIRMED_OR_SUBMITTED(Boolean.class),

        // Observable lists
        STATUSES(List.class),
        MODULES(List.class),
        PATHS(List.class),

        // UI display properties
        FORM_TITLE(String.class),
        FORM_TIME_TEXT(String.class),
        CLEAR_RESET_BUTTON_TEXT(String.class),
        SUBMIT_BUTTON_TEXT(String.class);

        private final TypedProperty<?> property;

        Keys(Class<?> type) {
            this.property = TypedProperty.of(this.name(), (Class) type);
        }

        /**
         * Returns the underlying TypedProperty for this key.
         */
        public TypedProperty<?> property() {
            return property;
        }

        /**
         * Returns true if this key is a STAMP coordinate (status, author, module, path, time).
         */
        public boolean isStampCoordinate() {
            return this == STATUS || this == AUTHOR || this == MODULE ||
                   this == PATH || this == TIME;
        }
    }

    // ========== Type-Safe Static Constants (Primary Usage) ==========

    /**
     * The current stamp being edited or viewed.
     * <p>
     * Represents the full STAMP coordinate (Status, Time, Author, Module, Path)
     * for the entity version being managed by the form.
     */
    public static final TypedProperty<Stamp> CURRENT_STAMP =
            (TypedProperty<Stamp>) Keys.CURRENT_STAMP.property();

    /**
     * The status component of the STAMP (Active, Inactive, Canceled, Primordial, Withdrawn).
     * <p>
     * User-selected status that determines the lifecycle state of the entity version.
     */
    public static final TypedProperty<State> STATUS =
            (TypedProperty<State>) Keys.STATUS.property();

    /**
     * The author component of the STAMP - who made the change.
     * <p>
     * Represents the user or system that created this entity version.
     * Always an {@link ObservableConcept} representing the author.
     */
    public static final TypedProperty<ConceptEntity<?>> AUTHOR =
            (TypedProperty<ConceptEntity<?>>) Keys.AUTHOR.property();

    /**
     * The module component of the STAMP - organizational context.
     * <p>
     * Represents the module or organizational unit responsible for this change.
     * Always an {@link ObservableConcept} that is a descendant of TinkarTerm.MODULE.
     */
    public static final TypedProperty<ConceptEntity<?>> MODULE =
            (TypedProperty<ConceptEntity<?>>) Keys.MODULE.property();

    /**
     * The path component of the STAMP - development/classification context.
     * <p>
     * Represents the development path or classification branch for this change.
     * Always an {@link ObservableConcept} that is a descendant of TinkarTerm.PATH.
     */
    public static final TypedProperty<ConceptEntity<?>> PATH =
            (TypedProperty<ConceptEntity<?>>) Keys.PATH.property();

    /**
     * The time component of the STAMP - when the change was made.
     * <p>
     * Epoch milliseconds representing the timestamp of this entity version.
     */
    public static final TypedProperty<Long> TIME =
            (TypedProperty<Long>) Keys.TIME.property();

    /**
     * Whether the STAMP values in the form match the current STAMP.
     * <p>
     * Used for validation - if true, the form values haven't changed and
     * submission should be prevented (no point creating identical STAMP).
     */
    public static final TypedProperty<Boolean> IS_STAMP_VALUES_THE_SAME_OR_EMPTY =
            (TypedProperty<Boolean>) Keys.IS_STAMP_VALUES_THE_SAME_OR_EMPTY.property();

    /**
     * Whether the user has confirmed or submitted the form.
     * <p>
     * Signals that the form has been successfully processed and can be closed.
     */
    public static final TypedProperty<Boolean> IS_CONFIRMED_OR_SUBMITTED =
            (TypedProperty<Boolean>) Keys.IS_CONFIRMED_OR_SUBMITTED.property();

    /**
     * Observable list of available status values for selection.
     * <p>
     * Contains all {@link State} enum values (Active, Inactive, etc.)
     */
    public static final TypedProperty<List> STATUSES =
            (TypedProperty<List>) Keys.STATUSES.property();

    /**
     * Observable list of available modules for selection.
     * <p>
     * Contains all concepts that are descendants of TinkarTerm.MODULE.
     */
    public static final TypedProperty<List> MODULES =
            (TypedProperty<List>) Keys.MODULES.property();

    /**
     * Observable list of available paths for selection.
     * <p>
     * Contains all concepts that are descendants of TinkarTerm.PATH.
     */
    public static final TypedProperty<List> PATHS =
            (TypedProperty<List>) Keys.PATHS.property();

    /**
     * The title text displayed at the top of the form.
     * <p>
     * Dynamic title that changes based on form state (e.g., "New Concept Version"
     * vs "Latest Concept Version").
     */
    public static final TypedProperty<String> FORM_TITLE =
            (TypedProperty<String>) Keys.FORM_TITLE.property();

    /**
     * The formatted time text displayed in the form.
     * <p>
     * Human-readable representation of the STAMP timestamp, or "Uncommitted"
     * for unsaved changes.
     */
    public static final TypedProperty<String> FORM_TIME_TEXT =
            (TypedProperty<String>) Keys.FORM_TIME_TEXT.property();

    /**
     * The text label for the clear/reset button.
     * <p>
     * Changes based on form mode: "CLEAR" for create forms, "RESET" for edit forms.
     */
    public static final TypedProperty<String> CLEAR_RESET_BUTTON_TEXT =
            (TypedProperty<String>) Keys.CLEAR_RESET_BUTTON_TEXT.property();

    /**
     * The text label for the submit button.
     * <p>
     * Changes based on form mode: "SUBMIT", "CONFIRM", etc.
     */
    public static final TypedProperty<String> SUBMIT_BUTTON_TEXT =
            (TypedProperty<String>) Keys.SUBMIT_BUTTON_TEXT.property();

    // ========== Enumeration Support Methods ==========

    /**
     * Returns all property keys.
     *
     * @return array of all Keys
     */
    public static Keys[] allKeys() {
        return Keys.values();
    }

    /**
     * Returns only the STAMP coordinate keys (status, author, module, path, time).
     * <p>
     * Use for validating that all required STAMP coordinates are set.
     *
     * @return array of coordinate Keys
     */
    public static Keys[] coordinateKeys() {
        return new Keys[] {
            Keys.STATUS, Keys.AUTHOR, Keys.MODULE, Keys.PATH, Keys.TIME
        };
    }

    /**
     * Returns only UI display property keys.
     *
     * @return array of display Keys
     */
    public static Keys[] displayKeys() {
        return new Keys[] {
            Keys.FORM_TITLE, Keys.FORM_TIME_TEXT,
            Keys.CLEAR_RESET_BUTTON_TEXT, Keys.SUBMIT_BUTTON_TEXT
        };
    }

    /**
     * Private constructor prevents instantiation.
     * <p>
     * This is a utility class with only static constants - no instances needed.
     */
    private StampProperties() {
        throw new AssertionError("StampProperties is a utility class and cannot be instantiated");
    }
}
