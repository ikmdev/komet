package dev.ikm.tinkar.integration;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that opens an existing SpinedArray store and ensures
 * tinkar-starter-data is loaded (useful when a store exists but needs data).
 * <p>
 * Store Type: SpinedArray (persistent, file-based)
 * Data Loaded: tinkar-starter-data-reasoned-pb.zip
 * Storage Location (default): target/spinedarrays
 * <p>
 * You can still override behavior on a per-test basis using {@link WithKeyValueProvider}
 * for custom {@code dataPath}, {@code cleanOnStart}, or {@code importPath}.
 */
public class StarterDataOpenSpinedArrayProvider extends OpenSpinedArrayKeyValueProvider {

    @Override
    protected Config resolveConfig(ExtensionContext context) {
        Config cfg = super.resolveConfig(context);
        // Ensure we are using OPEN spined array and set friendly defaults for starter data
        cfg.controllerName = TestConstants.OPEN_SPINED_ARRAY_STORE;
        if (cfg.dataPath == null || cfg.dataPath.isBlank()) {
            cfg.dataPath = "target/spinedarrays";
        }
        if (cfg.importPath == null || cfg.importPath.isBlank()) {
            cfg.importPath = "target/data/tinkar-starter-data-reasoned-pb.zip";
        }
        return cfg;
    }
}
