package dev.ikm.komet.layout;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The single source of truth for {@link KlPatternSemanticsFactory} instances. Factories are
 * discovered once via {@link ServiceLoader} and cached, so callers always share the same instances:
 * the display {@code ComboBox} fills its items from {@link #all()} and the model resolves its current
 * factory (default or restored from a stored class name) from {@link #byClassName(String)}. Because
 * both sides use these shared instances, the combo can match its selected value to a list item and
 * render the factory's display name instead of falling back to the class name.
 */
public final class KlPatternSemanticsFactories {

    private static final List<KlPatternSemanticsFactory> FACTORIES =
            ServiceLoader.load(KlPatternSemanticsFactory.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .toList();

    private KlPatternSemanticsFactories() {
    }

    /**
     * The discovered factories, in service-provider declaration order. Suitable for populating the
     * display {@code ComboBox} items.
     */
    public static List<KlPatternSemanticsFactory> all() {
        return FACTORIES;
    }

    /**
     * Resolves the shared factory instance for the given fully qualified class name.
     *
     * @param className the factory's {@link Class#getName() fully qualified class name}
     * @return the matching shared factory, or {@link Optional#empty()} if none is registered
     */
    public static Optional<KlPatternSemanticsFactory> byClassName(String className) {
        return FACTORIES.stream()
                .filter(factory -> factory.getClass().getName().equals(className))
                .findFirst();
    }
}
