package dev.ikm.tinkar.integration;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that initializes an empty ephemeral (in-memory) store.
 * <p>
 * <b>Store Type:</b> Ephemeral (in-memory)
 * <br>
 * <b>Data Loaded:</b> None
 * <p>
 * <b>Usage:</b>
 * <pre>
 * {@code
 * @ExtendWith(NewEphemeralKeyValueProvider.class)
 * class MyUnitTest {
 *     // Empty ephemeral store ready to use
 * }
 * }
 * </pre>
 *
 * <b>Override if needed:</b>
 * <pre>
 * {@code
 * @ExtendWith(NewEphemeralKeyValueProvider.class)
 * @WithKeyValueProvider(importPath = "custom-data.zip")
 * class CustomTest {
 *     // Loads custom data into ephemeral store
 * }
 * }
 * </pre>
 *
 * @see StarterDataEphemeralProvider to load tinkar-starter-data
 * @see NewSpinedArrayKeyValueProvider for persistent storage
 */
public class NewEphemeralKeyValueProvider extends KeyValueProviderExtension {

    @Override
    protected Config resolveConfig(ExtensionContext context) {
        // First check if test class has annotation override
        Config cfg = super.resolveConfig(context);

        // Apply subclass defaults if not overridden
        if (cfg.controllerName == null || cfg.controllerName.equals("default")) {
            cfg.controllerName = TestConstants.LOAD_EPHEMERAL_STORE;
        }

        // Allow subclasses to add import path
        String subclassImportPath = getImportPath();
        if (subclassImportPath != null && !subclassImportPath.isEmpty()
                && (cfg.importPath == null || cfg.importPath.isEmpty())) {
            cfg.importPath = subclassImportPath;
        }

        return cfg;
    }

    /**
     * Override in subclass to specify data to import.
     * @return path/pattern to import, or null for no imports
     */
    protected String getImportPath() {
        return null; // No data by default
    }
}