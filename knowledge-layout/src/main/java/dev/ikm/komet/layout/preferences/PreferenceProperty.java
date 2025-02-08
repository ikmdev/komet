package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlObject;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.edit.Activity;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

/**
 * The abstract class PreferenceProperty provides a base implementation for managing and binding
 * preference-related properties of various data types. It extends the {@code Property} interface
 * and implements the {@code ClassConceptBinding} interface to associate properties with
 * semantic concepts defined by the binding.
 *
 * This class is designed to work generically with different property implementations (e.g.,
 * {@code SimpleStringProperty}, {@code SimpleIntegerProperty}) through its generic type
 * parameters {@code T} and {@code I}. It also provides utility methods for creating
 * specific types of preference properties, such as String, Integer, Boolean, and Double preferences.
 *
 * @param <T> the type of the value contained in this property
 * @param <I> the type of the property implementation extending {@code Property<T>}
 */
public abstract class PreferenceProperty<T, I extends Property<T>> implements Property<T>, ClassConceptBinding {
    /**
     * Represents the initial default value for a string property in the context of {@code PreferenceProperty}.
     * This constant is used to signify that a string property has not been explicitly initialized with
     * a meaningful value and retains its uninitialized state.
     */
    public static final String INITIAL_STRING_VALUE = "uninitialized";
    /**
     * A constant value representing the initial state of a boolean property.
     * It is set to {@code false} by default, indicating the standard initial value
     * for boolean-based properties within the {@code PreferenceProperty} class.
     * This default value can be used as a starting point for initializing or resetting
     * boolean properties where required.
     */
    public static final boolean INITIAL_BOOLEAN_VALUE = false;

    /**
     * Represents the initial default value for integer properties in the {@code PreferenceProperty} class.
     * This constant is initialized to {@code Integer.MIN_VALUE}, indicating an unassigned or default state
     * for integer-based preferences. Notice that zero was not chosen, as a zero default can be the source of
     * bugs caused by improper initialization. {@code Integer.MIN_VALUE} is more likely to be an exceptional case than
     * zero.
     */
    public static final int INITIAL_INTEGER_VALUE = Integer.MIN_VALUE;
    /**
     * Represents the initial default value for double-type {@code PreferenceProperty} instances.
     * This value is set to {@code Double.NaN}, indicating that no valid numerical value has been
     * explicitly assigned yet. Notice that zero was not chosen, as a zero default can be the source of
     * bugs caused by improper initialization. {@code Double.NaN} is more likely to be an exceptional case than
     * zero.
     */
    public static final double INITIAL_DOUBLE_VALUE = Double.NaN;

    /**
     * Represents the initial default value for an {@link Encodable} property in the {@code PreferenceProperty} class.
     * This value is set to {@code Activity.DEVELOPING}, indicating a predetermined initial state
     * for encodable preference properties.
     *
     * It is used within the context of preference properties to establish a starting
     * or default value that can later be modified or further processed as needed.
     *
     * The constant is immutable and shared across all instances of the {@code PreferenceProperty} class.
     */
    public static final Encodable INITIAL_ENCODABLE_VALUE = Activity.DEVELOPING;

    /**
     * Represents the implementation instance of type {@code I} used internally
     * by the {@code PreferenceProperty}. This instance defines the specific implementation
     * details associated with the property and serves as the core mechanism for property
     * behavior and state handling.
     *
     * The {@code implInstance} is initialized during the construction of the {@code PreferenceProperty}
     * and remains immutable throughout its lifecycle to ensure consistency and thread safety.
     */
    final I implInstance;
    /**
     * Represents the {@code ClassConceptBinding} associated with this {@code PreferenceProperty}.
     * This binding provides a mechanism to specify and utilize the concept mapping
     * for the property in the context of its usage.
     *
     * The {@code binding} is used to initialize and bind the property with a specific
     * concept, enabling consistent and reusable property management.
     *
     * This is a final field and is assigned during the construction of the
     * {@code PreferenceProperty} instance.
     */
    final ClassConceptBinding binding;

    /**
     * Constructs an instance of {@code PreferenceProperty} with the given implementation instance
     * and class concept binding.
     *
     * @param implInstance the instance of type {@code I} representing the implementation of the property
     * @param binding the {@code ClassConceptBinding} associated with this property, specifying its binding concept
     */
    protected PreferenceProperty(I implInstance, ClassConceptBinding binding) {
        this.implInstance = implInstance;
        this.binding = binding;
    }

    /**
     * Retrieves the implementation instance associated with this {@code PreferenceProperty}.
     *
     * @return the implementation instance of type {@code I} associated with this property
     */
    public final I impl() {
        return implInstance;
    }

    /**
     * Creates a new {@code PreferencePropertyString} instance for the given {@code KlObject} and {@code ClassConceptBinding}.
     * This method initializes a string property associated with the provided klObject and binding.
     *
     * @param klObject  the {@code KlObject} instance associated with the preference property
     * @param binding the {@code ClassConceptBinding} used to define bindings and initialize the property
     * @return a new instance of {@code PreferencePropertyString} configured with the given klObject and binding
     */
    public static PreferencePropertyString stringProp(KlObject klObject, ClassConceptBinding binding) {
        return PreferencePropertyString.create(klObject, binding);
    }

    /**
     * Creates a new {@code PreferencePropertyInteger} instance for the given {@code KlObject} and {@code ClassConceptBinding}.
     * This method initializes an integer preference property associated with the specified klObject and binding.
     *
     * @param klObject the {@code KlObject} instance associated with the preference property
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property
     * @return a new instance of {@code PreferencePropertyInteger} configured with the given gadget and binding
     */
    public static PreferencePropertyInteger integerProp(KlObject klObject, ClassConceptBinding binding) {
        return PreferencePropertyInteger.create(klObject, binding);
    }

    /**
     * Creates a new {@code PreferencePropertyBoolean} instance for the specified {@code KlObject} and {@code ClassConceptBinding}.
     * This method initializes a Boolean preference property associated with the given klObject and binding.
     *
     * @param klObject the {@code KlObject} instance associated with the preference property
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property
     * @return a new instance of {@code PreferencePropertyBoolean} configured with the given klObject and binding
     */
    public static PreferencePropertyBoolean booleanProp(KlObject klObject, ClassConceptBinding binding) {
        return PreferencePropertyBoolean.create(klObject, binding);
    }

    /**
     * Creates a new {@code PreferencePropertyDouble} instance for the specified {@code KlObject}
     * and {@code ClassConceptBinding}. This method initializes a double preference
     * property associated with the provided klObject and binding.
     *
     * @param klObject the {@code KlObject} instance associated with the preference property
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property
     * @return a new instance of {@code PreferencePropertyDouble} configured with the given klObject and binding
     */
    public static PreferencePropertyDouble doubleProp(KlObject klObject, ClassConceptBinding binding) {
        return PreferencePropertyDouble.create(klObject, binding);
    }

    /**
     * Creates a new {@code PreferencePropertyObject} instance for the given {@code KlObject}
     * and {@code ClassConceptBinding}. This method initializes a preference property object
     * configured with the specified klObject and binding.
     *
     * @param klObject the {@code KlObject} instance associated with the preference property.
     * @param binding  the {@code ClassConceptBinding} used to define bindings and initialize the property.
     * @return a new instance of {@code PreferencePropertyObject} configured with the given klObject and binding.
     */
    public static PreferencePropertyObject objectProp(KlObject klObject, ClassConceptBinding binding) {
        return PreferencePropertyObject.create(klObject, binding);
    }

    @Override
    public void bind(ObservableValue<? extends T> observable) {
        this.implInstance.bind(observable);
    }

    @Override
    public void unbind() {
        this.implInstance.unbind();
    }

    @Override
    public boolean isBound() {
        return this.implInstance.isBound();
    }

    @Override
    public void setValue(T value) {
        this.implInstance.setValue(value);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        this.implInstance.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        this.implInstance.removeListener(listener);
    }

    @Override
    public T getValue() {
        return this.implInstance.getValue();
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        this.implInstance.bindBidirectional(other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        this.implInstance.unbindBidirectional(other);
    }

    @Override
    public Object getBean() {
        return this.implInstance.getBean();
    }

    /**
     * Retrieves the name associated with this {@code PreferenceProperty}.
     * This method checks if the associated bean is an instance of {@code KlObject},
     * and if a public ID exists for the binding. If so, it attempts to fetch
     * the description text using the associated klObject's view calculator.
     * If no valid description is retrieved or the conditions are not met,
     * the name from the implementation instance is returned.
     *
     * @return the name of this property, determined by the description text
     *         of the public ID if available, or by the implementation instance.
     */
    @Override
    public String getName() {
        if (this.getBean() != null && this.getBean() instanceof KlGadget gadget) {
            if (PrimitiveData.get().hasPublicId(this.binding.publicId())) {
                //TODO need calculator to accept publicIds in addition to nids...
                Optional<String> optionalText = gadget.viewForContext().getDescriptionText(
                        PrimitiveData.nid(this.binding.publicId()));
                if (optionalText.isPresent()) {
                        return optionalText.get();
                }
            }
        }
        return this.implInstance.getName();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        this.implInstance.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        this.implInstance.removeListener(listener);
    }
}
