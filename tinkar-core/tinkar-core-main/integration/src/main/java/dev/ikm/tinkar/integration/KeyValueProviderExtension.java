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
package dev.ikm.tinkar.integration;

import com.github.benmanes.caffeine.cache.Cache;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.entity.EntityRecordFactory;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.provider.entity.EntityProvider;
import dev.ikm.tinkar.provider.ephemeral.constants.EphemeralStoreControllerName;
import dev.ikm.tinkar.provider.spinedarray.constants.SpinedArrayControllerNames;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JUnit 5 extension that initializes the Tinkar key-value provider for tests.
 * <p>
 * It prevents the common "No provider. Call Select provider prior to get()" error by
 * ensuring the provider is selected and started before any tests that access entities run.
 * <p>
 * Usage options (hybrid approach):
 * <ul>
 *   <li>Annotation-driven: annotate test classes with {@link WithKeyValueProvider} to configure
 *   controller, data path, clean-on-start, and optional imports.</li>
 *   <li>Type-safe subclasses: use predefined providers (e.g., {@link NewSpinedArrayKeyValueProvider},
 *   {@link OpenSpinedArrayKeyValueProvider}, {@link StarterDataSpinedArrayProvider}, etc.) for
 *   discoverable, IDE-friendly defaults. You can still refine behavior by adding
 *   {@link WithKeyValueProvider} on the test class.</li>
 * </ul>
 * <p>
 * Configuration precedence (highest to lowest):
 * <ol>
 *   <li>Test class-level {@link WithKeyValueProvider} annotation</li>
 *   <li>Predefined provider subclass via {@link #resolveConfig(ExtensionContext)} override</li>
 *   <li>Sensible defaults (controller = automatic NEW/OPEN selection; dataPath = "target/key-value-store")</li>
 * </ol>
 * <p>
 * Defaults and smart behavior:
 * <ul>
 *   <li>If {@code controllerName="default"} or empty, a Spined Array controller is selected automatically:
 *     OPEN if {@code dataPath} exists and has content; NEW otherwise.</li>
 *   <li>For non-ephemeral controllers, {@code dataPath} defaults to {@code target/key-value-store}.</li>
 *   <li>{@code cleanOnStart=true} removes any existing files under {@code dataPath} prior to startup.</li>
 *   <li>{@code importPath} supports single file, glob(s), and comma-separated patterns.</li>
 * </ul>
 */
public class KeyValueProviderExtension implements BeforeAllCallback, AfterAllCallback {
    private static final Logger LOG = LoggerFactory.getLogger(KeyValueProviderExtension.class);
    private static final Object LOCK = new Object();
    private static int referenceCount = 0;
    
    // Add tracking for JVM reuse detection
    private static final String JVM_INIT_MARKER = "tinkar.jvm.initialized";
    private static final AtomicBoolean JVM_ALREADY_USED = new AtomicBoolean(false);
    private static final Set<String> INITIALIZED_TEST_CLASSES = ConcurrentHashMap.newKeySet();
    
    /**
     * Configuration container resolved from {@link WithKeyValueProvider} or defaults.
     */
    protected static class Config {
        String controllerName;
        String dataPath;
        boolean cleanOnStart;
        String importPath;

        Config(String controllerName, String dataPath, boolean cleanOnStart, String importPath) {
            this.controllerName = controllerName;
            this.dataPath = dataPath;
            this.cleanOnStart = cleanOnStart;
            this.importPath = importPath;
        }
    }

    /**
     * Override to supply a configuration programmatically (for backward-compatible concrete extensions).
     * Default implementation reads {@link WithKeyValueProvider} annotation or uses sensible defaults.
     */
    protected Config resolveConfig(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        // First, check if the test class itself has @WithKeyValueProvider
        WithKeyValueProvider testAnnotation = testClass.getAnnotation(WithKeyValueProvider.class);

        // Second, check if the extension class has @WithKeyValueProvider (for predefined providers)
        WithKeyValueProvider providerAnnotation = this.getClass().getAnnotation(WithKeyValueProvider.class);

        // Test annotation overrides provider annotation
        WithKeyValueProvider effective = testAnnotation != null ? testAnnotation : providerAnnotation;

        if (effective == null) {
            // No annotation found - use defaults
            return new Config("default", "", false, "");
        }

        // If test has annotation, it can override provider's settings
        String controllerName = testAnnotation != null && !testAnnotation.controllerName().isEmpty()
                ? testAnnotation.controllerName()
                : (providerAnnotation != null ? providerAnnotation.controllerName() : "default");

        String dataPath = testAnnotation != null && !testAnnotation.dataPath().isEmpty()
                ? testAnnotation.dataPath()
                : (providerAnnotation != null ? providerAnnotation.dataPath() : "");

        boolean cleanOnStart = testAnnotation != null
                ? testAnnotation.cleanOnStart()
                : (providerAnnotation != null && providerAnnotation.cleanOnStart());

        String importPath = testAnnotation != null && !testAnnotation.importPath().isEmpty()
                ? testAnnotation.importPath()
                : (providerAnnotation != null ? providerAnnotation.importPath() : "");

        return new Config(controllerName, dataPath, cleanOnStart, importPath);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Config cfg = resolveConfig(context);
        String testClassName = context.getRequiredTestClass().getName();

        synchronized (LOCK) {
            // SAFETY CHECK 1: Detect JVM reuse across test classes
            detectUnsafeJVMReuse(context, testClassName);
            
            // SAFETY CHECK 2: Verify Maven failsafe configuration
            verifyFailsafeConfiguration();
            
            // SAFETY CHECK 3: Check for static state pollution
            checkStaticStatePollution();
            
            referenceCount++;
            if (referenceCount == 1) {
                // Mark this JVM as having been initialized
                System.setProperty(JVM_INIT_MARKER, "true");
                JVM_ALREADY_USED.set(true);
                
                // Determine controller and data path defaults
                applyDefaultsAndEnvironment(cfg);
                LOG.info("Initializing Tinkar provider for tests with controller: {}", cfg.controllerName);
                
                // Set data store root for non-ephemeral controllers
                if (!isEphemeralController(cfg.controllerName)) {
                    if (cfg.cleanOnStart) {
                        cleanDirectory(cfg.dataPath);
                    }
                    // Ensure directory exists for NEW controller case
                    File dataRoot = new File(cfg.dataPath);
                    if (!dataRoot.exists()) {
                        boolean created = dataRoot.mkdirs();
                        if (!created) {
                            LOG.warn("Failed to create data directory: {}", dataRoot);
                        }
                    }
                    System.setProperty(ServiceKeys.DATA_STORE_ROOT.name(), dataRoot.getAbsolutePath());
                }

                // Clean up any stale lock files from previous test runs
                cleanupStaleLocks();

                // Clear any existing caches
                CachingService.clearAll();

                // Select and start provider
                PrimitiveData.selectControllerByName(cfg.controllerName);

                // Only start if not already running
                if (!PrimitiveData.running()) {
                    PrimitiveData.start();
                    LOG.info("{} provider started successfully.", cfg.controllerName);
                } else {
                    LOG.info("{} provider already running, skipping start", cfg.controllerName);
                }

                // Import any specified protobuf files
                importIfRequested(cfg);
            } else {
                LOG.info("Provider already initialized (reference count: {})", referenceCount);
            }
            
            // Track this test class
            INITIALIZED_TEST_CLASSES.add(testClassName);
        }
    }

    /**
     * Detects if this JVM has been reused for multiple test classes.
     * This is UNSAFE for Tinkar tests due to static state pollution.
     */
    private void detectUnsafeJVMReuse(ExtensionContext context, String testClassName) {
        String previousInit = System.getProperty(JVM_INIT_MARKER);
        
        if (previousInit != null && referenceCount == 0) {
            // JVM was used before but reference count is 0 = JVM reuse across test classes
            String error = String.format(
                "UNSAFE TEST CONFIGURATION DETECTED!\n" +
                "════════════════════════════\n" +
                "Test class '%s' \nis running in a JVM that was previously\n" +
                "used by another test class. This causes static state pollution.\n" +
                "\n" +
                "Previously initialized test classes:\n" +
                "%s\n" +
                "\n" +
                "WHY THIS IS DANGEROUS:\n" +
                "- Static caches (EntityProvider.ENTITY_CACHE, etc.) retain old data\n" +
                "- ByteBuf pools contain residual data from previous tests\n" +
                "- EntityRecordFactory.MAX_ENTITY_SIZE may be modified\n" +
                "- Data store locks may be held\n" +
                "- This causes 'Wrong token type' errors and data corruption\n" +
                "\n" +
                "REQUIRED FIX:\n" +
                "Add to your pom.xml <maven-failsafe-plugin> configuration:\n" +
                "  <configuration>\n" +
                "    <forkCount>1</forkCount>\n" +
                "    <reuseForks>false</reuseForks>  <!-- CRITICAL: Fresh JVM per test class -->\n" +
                "  </configuration>\n" +
                "\n" +
                "Or ensure your test class name ends in *IT.java or *ITestFX.java\n" +
                "to match the safe configuration in your parent POM.\n" +
                "════════════════════════════",
                testClassName,
                String.join("\n  - ", INITIALIZED_TEST_CLASSES)
            );
            
            LOG.error(error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * Verifies that Maven Failsafe is configured correctly for safe test execution.
     */
    private void verifyFailsafeConfiguration() {
        // Check system properties set by Maven Surefire/Failsafe
        String reuseForks = System.getProperty("failsafe.reuseForks");
        String forkNumber = System.getProperty("surefire.forkNumber");
        
        // If we can detect the configuration, verify it's safe
        if (reuseForks != null && !"false".equalsIgnoreCase(reuseForks)) {
            String warning = String.format(
                "POTENTIALLY UNSAFE TEST CONFIGURATION!\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "Maven Failsafe property 'failsafe.reuseForks' = '%s'\n" +
                "\n" +
                "RECOMMENDED: Set to 'false' to prevent JVM reuse between test classes.\n" +
                "\n" +
                "Current fork number: %s\n" +
                "═══════════════════════════════════════════════════════════════",
                reuseForks,
                forkNumber
            );
            LOG.warn(warning);
        }
        
        // Log the fork configuration for diagnostics
        LOG.info("Test execution environment: forkNumber={}, reuseForks={}, test.class={}", 
            forkNumber, reuseForks, System.getProperty("test.class"));
    }

    /**
     * Checks for evidence of static state pollution from previous test runs.
     */
    private void checkStaticStatePollution() {
        List<String> pollutionWarnings = new ArrayList<>();
        
        // Check EntityRecordFactory static state
        int currentMaxEntitySize = EntityRecordFactory.MAX_ENTITY_SIZE;
        int currentMaxVersionSize = EntityRecordFactory.MAX_VERSION_SIZE;
        
        if (currentMaxEntitySize != EntityRecordFactory.DEFAULT_ENTITY_SIZE) {
            pollutionWarnings.add(String.format(
                "EntityRecordFactory.MAX_ENTITY_SIZE = %d (expected %d)",
                currentMaxEntitySize, EntityRecordFactory.DEFAULT_ENTITY_SIZE
            ));
        }
        
        if (currentMaxVersionSize != EntityRecordFactory.DEFAULT_VERSION_SIZE) {
            pollutionWarnings.add(String.format(
                "EntityRecordFactory.MAX_VERSION_SIZE = %d (expected %d)",
                currentMaxVersionSize, EntityRecordFactory.DEFAULT_VERSION_SIZE
            ));
        }
        
        // Check if EntityProvider caches have content
        try {
            EntityService service = EntityService.get();
            if (service instanceof EntityProvider provider) {
                // Use reflection to check cache sizes (they're private)
                Field entityCacheField = EntityProvider.class.getDeclaredField("ENTITY_CACHE");
                entityCacheField.setAccessible(true);
                Cache<?, ?> entityCache = (Cache<?, ?>) entityCacheField.get(null);
                long cacheSize = entityCache.estimatedSize();
                
                if (cacheSize > 0) {
                    pollutionWarnings.add(String.format(
                        "EntityProvider.ENTITY_CACHE contains %d entries (expected 0)",
                        cacheSize
                    ));
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not check EntityProvider cache state: {}", e.getMessage());
        }
        
        // Check if PrimitiveData is already running
        if (PrimitiveData.running()) {
            pollutionWarnings.add("PrimitiveData is already running (expected: not started yet)");
        }
        
        // Report any pollution detected
        if (!pollutionWarnings.isEmpty() && referenceCount == 0) {
            String warning = String.format(
                "STATIC STATE POLLUTION DETECTED!\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "Evidence of previous test execution in this JVM:\n\n" +
                "%s\n\n" +
                "This indicates JVM reuse between test classes, which is UNSAFE.\n" +
                "═══════════════════════════════════════════════════════════════",
                String.join("\n  - ", pollutionWarnings)
            );
            LOG.error(warning);
            throw new IllegalStateException(warning);
        }
    }

    private void applyDefaultsAndEnvironment(Config cfg) {
        // Default data path for non-ephemeral controllers
        if (cfg.dataPath == null || cfg.dataPath.isBlank()) {
            cfg.dataPath = "target/key-value-store";
        }

        // Resolve controller if default
        if (cfg.controllerName == null || cfg.controllerName.equalsIgnoreCase("default")) {
            // If import requested but ephemeral is desired? Keep spinedarray by default.
            File dir = new File(cfg.dataPath);
            boolean exists = dir.exists() && dir.isDirectory() && dir.list() != null && dir.list().length > 0;
            cfg.controllerName = exists ? SpinedArrayControllerNames.OPEN_CONTROLLER_NAME
                    : SpinedArrayControllerNames.NEW_CONTROLLER_NAME;
        }
    }

    private boolean isEphemeralController(String controllerName) {
        return EphemeralStoreControllerName.NEW_CONTROLLER_NAME.equals(controllerName);
    }

    private void cleanDirectory(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) return;
        Path path = Path.of(pathStr);
        if (!Files.exists(path)) return;
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.warn("Failed to clean directory: {}", pathStr, e);
        }
    }

    private void importIfRequested(Config cfg) {
        if (cfg.importPath == null || cfg.importPath.isBlank()) return;
        List<String> patterns = new ArrayList<>();
        for (String token : Arrays.asList(cfg.importPath.split(","))) {
            String t = token.trim();
            if (!t.isEmpty()) patterns.add(t);
        }
        List<Path> filesToImport = new ArrayList<>();
        for (String pattern : patterns) {
            Path p = Path.of(pattern);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                filesToImport.add(p.toAbsolutePath());
                continue;
            }
            // Treat as glob. Base directory is either explicit parent or project root
            Path baseDir = p.getParent() != null ? p.getParent() : Path.of(".");
            String glob = p.getFileName() != null ? p.getFileName().toString() : "*";
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
            try {
                if (Files.exists(baseDir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
                        for (Path child : stream) {
                            if (Files.isRegularFile(child) && matcher.matches(child.getFileName())) {
                                filesToImport.add(child.toAbsolutePath());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOG.warn("Failed expanding glob pattern: {}", pattern, e);
            }
        }
        if (filesToImport.isEmpty()) {
            LOG.info("No import files matched for pattern(s): {}", cfg.importPath);
            return;
        }
        filesToImport.sort(null);
        for (Path file : filesToImport) {
            try {
                LOG.info("Importing protobuf file: {}", file);
                new LoadEntitiesFromProtobufFile(file.toFile()).compute();
            } catch (Exception e) {
                LOG.warn("Failed to import file: {}", file, e);
            }
        }
    }

    /**
     * Removes stale Lucene lock files from previous test runs.
     * This prevents LockObtainFailedException when tests don't shut down cleanly.
     */
    private void cleanupStaleLocks() {
        try {
            // Try to get the configured data root, but don't set a default yet
            String dataRootPath = System.getProperty(ServiceKeys.DATA_STORE_ROOT.name());

            if (dataRootPath == null) {
                // Check common test locations
                File targetDir = new File("target/spinedarrays");
                if (targetDir.exists()) {
                    dataRootPath = targetDir.getAbsolutePath();
                    LOG.info("Using detected data root: {}", dataRootPath);
                } else {
                    LOG.debug("No data root configured yet, skipping lock cleanup");
                    return;
                }
            }

            File dataRoot = new File(dataRootPath);
            Path lucenePath = Path.of(dataRoot.getPath(), "lucene");
            Path lockFile = lucenePath.resolve("write.lock");

            if (Files.exists(lockFile)) {
                LOG.info("Removing stale Lucene lock file: {}", lockFile);
                try {
                    Files.delete(lockFile);
                    LOG.info("Successfully removed lock file");
                } catch (IOException e) {
                    LOG.warn("Failed to delete lock file, will attempt forced cleanup", e);
                    // Try to forcibly delete
                    lockFile.toFile().delete();
                }
            } else {
                LOG.debug("No lock file found at: {}", lockFile);
            }
        } catch (Exception e) {
            LOG.warn("Failed to clean up stale lock files (this may cause test failures)", e);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        synchronized (LOCK) {
            referenceCount--;
            LOG.info("KeyValueProviderExtension.afterAll - referenceCount now: {}", referenceCount);
            
            // If this was the last test class in this JVM, log final state
            if (referenceCount == 0) {
                LOG.info("All test classes completed in this JVM fork. Final state:");
                LOG.info("  - Initialized test classes: {}", INITIALIZED_TEST_CLASSES);
                LOG.info("  - EntityRecordFactory.MAX_ENTITY_SIZE: {}", EntityRecordFactory.MAX_ENTITY_SIZE);
                LOG.info("  - EntityRecordFactory.MAX_VERSION_SIZE: {}", EntityRecordFactory.MAX_VERSION_SIZE);
                LOG.info("Provider will stop when JVM exits.");
            }
        }
    }
}
