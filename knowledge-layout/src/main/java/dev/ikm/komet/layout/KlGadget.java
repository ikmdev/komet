package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.PreferenceProperty;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Highest level Knowledge Layout Component. Some components, such as {@code Window} do
 * not descend from {@code Node}, and this interface enables inclusion of those components
 * in the Knowledge Layout paradigm (i.e. consistent use of factories and preferences, and
 * an ability to serialize, share, and restore a layout.)
 *
 */
public interface KlGadget<T> {
    /**
     * Keys for objects that KlGadgets will store in the properties of their associated
     * JavaFx {@code Node}s.
     *
     */
    enum PropertyKeys {
        VIEW_CALCULATOR
    }

    /**
     * A thread-safe atomic reference to a {@code ViewCalculator} instance used within the knowledge layout system.
     * This instance provides functionalities for evaluating and manipulating various view and coordinate computations.
     * The initial value is set using the default {@code ViewCalculatorWithCache} instance provided by {@code Calculators.View.Default()}.
     * This reference ensures safe updates and access across multiple threads.
     */
    AtomicReference<ViewCalculator> applicationViewCalculatorRef = new AtomicReference<>(Calculators.View.Default());
    /**
     * Enum representing the keys used to manage and access user preferences
     * related to gadgets within the application. This enum defines constants
     * that are essential for storing and retrieving configuration or state
     * information for restoring windows or initializing preferences.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Boolean string representing if the preferences have been initialized.
         */
        INITIALIZED( Boolean.FALSE),

        /**
         * Fully qualified name of the factory class. Used to restore the KlWindow
         * from preferences.
         */
        FACTORY_CLASS(PreferenceProperty.INITIAL_STRING_VALUE),

        /**
         * Represents the name of the specific implementation of a {@link KlWindow}
         * that can be restored from preferences. This key is used to identify
         * and manage restoration of the window's state during application initialization
         * or when reloading user preferences.
         */
        NAME_FOR_RESTORE("");

        /**
         * Represents the default value associated with a preference key.
         * This value provides an initial or fallback configuration used
         * when no other value has been explicitly set or retrieved.
         */
        Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }
    /**
     * Provides an instance of the generic type T associated with the knowledge layout component.
     *
     * @return an instance of type T, representing a specific knowledge layout gadget.
     */
    T klGadget();

    /**
     * Retrieves the concrete class type of the {@link KlFactory} used to create
     * the current {@link KlGadget} implementation.
     *
     * @return a {@link Class} object that represents the class type extending {@link KlFactory},
     *         which is responsible for producing the associated {@link KlGadget}.
     */
    default Class<? extends KlFactory> factoryClass() {
        throw new UnsupportedOperationException("Please implement on the concrete class");
    }

    /**
     * Retrieves a {@code ViewCalculator} instance for the context associated with the implementing class.
     * The {@code ViewCalculator} allows for the evaluation and manipulation of various view and coordinate
     * computations within the associated knowledge layout system.
     *
     * @return a {@code ViewCalculator} instance associated with the current context.
     */
    default ViewCalculator viewCalculatorForContext() {
        return switch (this.klGadget()) {
            case Node node -> viewCalculatorForContext(node);
            case Window window -> viewCalculatorForContext(window);
            case Scene scene -> viewCalculatorForContext(scene);
            default -> applicationViewCalculatorRef.get();
        };
    }
    /**
     * Retrieves a {@code ViewCalculator} instance for the specified {@code Window} context.
     * The {@code ViewCalculator} enables evaluation and manipulation of view-related computations
     * within the associated context. If the {@code Window} properties contain a specific
     * {@code VIEW_CALCULATOR} entry, it is returned. Otherwise, a default {@code ViewCalculator}
     * is used.
     *
     * @param window the {@code Window} context for which a {@code ViewCalculator} is to be retrieved.
     * @return a {@code ViewCalculator} instance associated with the provided {@code Window} context
     *         or a default instance if none is specified in the properties.
     */
    static ViewCalculator viewCalculatorForContext(Window window) {
        if (window.hasProperties() && window.getProperties().containsKey(PropertyKeys.VIEW_CALCULATOR.name())) {
            return (ViewCalculator) window.getProperties().get(PropertyKeys.VIEW_CALCULATOR.name());
        }
        return applicationViewCalculatorRef.get();
    }

    /**
     * Retrieves a {@code ViewCalculator} instance for the specified {@code Node} context.
     * The {@code ViewCalculator} enables evaluation and manipulation of view-related computations
     * within the associated Node context. If the {@code Node} or its hierarchy (including its parent
     * or scene) contains a specific {@code VIEW_CALCULATOR} property, it is returned.
     * Otherwise, the method iterates through the parent hierarchy to locate an appropriate
     * {@code ViewCalculator}.
     *
     * @param node the {@code Node} for which a {@code ViewCalculator} is to be retrieved.
     * @return a {@code ViewCalculator} instance associated with the provided {@code Node} context
     *         or the nearest parent or scene containing a valid {@code VIEW_CALCULATOR} property.
     */
    static ViewCalculator viewCalculatorForContext(Node node) {
        if (node.hasProperties() && node.getProperties().containsKey(PropertyKeys.VIEW_CALCULATOR.name())) {
            return (ViewCalculator) node.getProperties().get(PropertyKeys.VIEW_CALCULATOR.name());
        }
        Node parent = node.getParent();
        if (parent != null) {
            return viewCalculatorForContext(parent);
        }
        Scene scene = parent.getScene();
        if (scene != null) {
            return viewCalculatorForContext(scene);
        }
        return viewCalculatorForContext(node.getParent());

    }

    /**
     * Retrieves a {@code ViewCalculator} instance for the specified {@code Scene} context.
     * The {@code ViewCalculator} enables evaluation and manipulation of view-related computations
     * within the associated scene. If the {@code Scene} properties contain a specific
     * {@code VIEW_CALCULATOR} entry, it is returned. If no such entry exists but the scene has an
     * associated {@code Window}, the method delegates to the {@code viewCalculatorForContext(Window)}.
     * Otherwise, a default {@code ViewCalculator} instance is returned from the application context.
     *
     * @param scene the {@code Scene} context for which a {@code ViewCalculator} is to be retrieved.
     * @return a {@code ViewCalculator} instance associated with the provided {@code Scene} context,
     *         the associated {@code Window} context, or a default instance if neither has a specified
     *         {@code VIEW_CALCULATOR} entry.
     */
    static ViewCalculator viewCalculatorForContext(Scene scene) {
        if (scene.hasProperties() && scene.getProperties().containsKey(PropertyKeys.VIEW_CALCULATOR.name())) {
            return (ViewCalculator) scene.getProperties().get(PropertyKeys.VIEW_CALCULATOR.name());
        }
        if (scene.getWindow() != null) {
            return viewCalculatorForContext(scene.getWindow());
        }
        return applicationViewCalculatorRef.get();
    }
}
