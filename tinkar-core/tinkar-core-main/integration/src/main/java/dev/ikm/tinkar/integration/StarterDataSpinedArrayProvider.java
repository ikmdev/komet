package dev.ikm.tinkar.integration;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that loads tinkar-starter-data into SpinedArray store.
 * <p>
 * <b>Store Type:</b> SpinedArray (persistent, file-based)
 * <br>
 * <b>Data Loaded:</b> tinkar-starter-data-reasoned-pb.zip
 * <br>
 * <b>Storage Location:</b> target/spinedarrays
 */
public class StarterDataSpinedArrayProvider extends NewSpinedArrayKeyValueProvider {

    @Override
    protected Config resolveConfig(ExtensionContext context) {
        Config cfg = super.resolveConfig(context);
        // Ensure we are using NEW spined array and set defaults suitable for starter data
        cfg.controllerName = TestConstants.NEW_SPINED_ARRAY_STORE;
        if (cfg.dataPath == null || cfg.dataPath.isBlank()) {
            cfg.dataPath = "target/spinedarrays";
        }
        if (cfg.importPath == null || cfg.importPath.isBlank()) {
            cfg.importPath = "target/data/tinkar-starter-data-reasoned-pb.zip";
        }
        return cfg;
    }
}