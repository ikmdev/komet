package dev.ikm.tinkar.integration;

/**
 * JUnit 5 extension that loads tinkar-starter-data into ephemeral store.
 * <p>
 * <b>Store Type:</b> Ephemeral (in-memory)
 * <br>
 * <b>Data Loaded:</b> tinkar-starter-data-reasoned-pb.zip
 * <p>
 * <b>Usage:</b>
 * <pre>
 * {@code
 * @ExtendWith(StarterDataEphemeralProvider.class)
 * class MyIntegrationTest {
 *     // Starter data already loaded and ready
 *
 *     @Test
 *     void testWithRealData() {
 *         ObservableConcept concept = ObservableEntityHandle.getConcept(...);
 *         assertNotNull(concept);
 *     }
 * }
 * }
 * </pre>
 *
 * <b>Override data file if needed:</b>
 * <pre>
 * {@code
 * @ExtendWith(StarterDataEphemeralProvider.class)
 * @WithKeyValueProvider(importPath = "custom-snapshot.zip")
 * class SnapshotTest { }
 * }
 * </pre>
 *
 * @see NewEphemeralKeyValueProvider for empty ephemeral store
 * @see StarterDataSpinedArrayProvider for persistent storage with data
 */
public class StarterDataEphemeralProvider extends NewEphemeralKeyValueProvider {

    @Override
    protected String getImportPath() {
        return "target/data/tinkar-starter-data-reasoned-pb.zip";
    }
}