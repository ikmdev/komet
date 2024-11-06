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
package dev.ikm.komet.app.util;

import fr.brouillard.oss.cssfx.CSSFX;
import fr.brouillard.oss.cssfx.api.URIToPathConverter;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for managing and applying CSS stylesheets in JavaFX applications.
 * <p>
 * This class provides static methods to add CSS stylesheets to JavaFX {@link Scene} objects.
 * It first attempts to load CSS files from the local file system for development purposes
 * and falls back to loading them from the classpath resources for production environments.
 * Additionally, it integrates with CSSFX to enable live-reloading of CSS files during development.
 * </p>
 * <p>
 * Supported CSS files are declared within the {@link CssFile} enum. To include additional CSS files,
 * declare new enum constants in {@link CssFile} and ensure they are referenced appropriately in the methods.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * import static dev.ikm.komet.app.util.CssFile.*;
 * import dev.ikm.komet.app.util.CssUtils;
 * import javafx.scene.Scene;
 * import javafx.scene.layout.BorderPane;
 *
 * // ...
 *
 * BorderPane root = new BorderPane();
 * Scene scene = new Scene(root, 800, 600);
 *
 * // Apply CSS stylesheets using the CssFile enum
 * CssUtils.addStylesheets(scene, KOMET_CSS, KVIEW_CSS);
 *
 * // Set up and show the stage
 * primaryStage.setScene(scene);
 * primaryStage.show();
 * }</pre>
 *
 * @see CssFile
 */
public final class CssUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CssUtils.class);

    /**
     * The name of the application project module. Used to determine the working directory.
     */
    private static final String APPLICATION_PROJECT_NAME = "application";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CssUtils() {
        throw new UnsupportedOperationException("CssUtils class should not be instantiated.");
    }

    /**
     * Adds the specified CSS files to the given JavaFX {@link Scene}. This method attempts to load the CSS files
     * from the local file system first, which is suitable for development environments where CSS files may change frequently.
     * If the CSS files are not found in the local file system, it falls back to loading them from the classpath resources,
     * which is appropriate for production environments. Additionally, this method sets up CSSFX to enable live-reloading
     * of CSS files when changes are detected in the local file system.
     *
     * @param scene    the JavaFX {@link Scene} to which the stylesheets will be added
     * @param cssFiles a variable number of {@link CssFile} enums representing the CSS files to be added
     * @throws NullPointerException if the {@code scene} parameter is {@code null}
     */
    public static void addStylesheets(Scene scene, CssFile... cssFiles) {
        Objects.requireNonNull(scene, "The scene parameter cannot be null.");

        if (cssFiles == null || cssFiles.length == 0) {
            LOG.warn("No CSS files provided to addStylesheets.");
            return;
        }

        final Path workingDirPath = determineWorkingDirectory();
        final List<String> cssUris = new ArrayList<>();
        final List<CssFile> loadedFromFileSystemList = new ArrayList<>();

        for (CssFile cssFile : cssFiles) {
            Path cssPath = cssFile.resolveAbsolutePath(workingDirPath);
            LOG.debug("Attempting to load CSS '{}' from path: {}", cssFile.getFileName(), cssPath);

            if (Files.exists(cssPath)) {
                String cssUri = cssPath.toUri().toString();
                cssUris.add(cssUri);
                loadedFromFileSystemList.add(cssFile);
                LOG.info("Loaded CSS '{}' from local file system: {}", cssFile.getFileName(), cssUri);
            } else {
                LOG.warn("CSS file '{}' not found at local file system path '{}'", cssFile.getFileName(), cssPath);
                loadFromResource(cssFile, cssUris);
            }
        }

        if (!cssUris.isEmpty()) {
            scene.getStylesheets().addAll(cssUris);
            LOG.info("Added {} stylesheet(s) to the scene.", cssUris.size());
        } else {
            LOG.warn("No CSS stylesheets were added to the scene.");
        }

        if (!loadedFromFileSystemList.isEmpty()) {
            setupCssMonitor(loadedFromFileSystemList.toArray(new CssFile[0]), workingDirPath);
        }
    }

    /**
     * Determines the working directory based on the current system property {@code user.dir}.
     * If the working directory corresponds to the application module, it moves up one directory level.
     * This adjustment is useful when running the application from within an IDE or specific project structure.
     *
     * @return the {@link Path} representing the determined working directory
     */
    private static Path determineWorkingDirectory() {
        Path workingDirPath = Paths.get(System.getProperty("user.dir"));
        LOG.info("Working directory: {}", workingDirPath);

        if (workingDirPath.getFileName().toString().equalsIgnoreCase(APPLICATION_PROJECT_NAME)) {
            // Running from the application module, move up one level
            Path parent = workingDirPath.getParent();
            if (parent != null) {
                workingDirPath = parent;
                LOG.info("Adjusted working directory to parent: {}", workingDirPath);
            } else {
                LOG.warn("Cannot move up from working directory '{}'. Using as is.", workingDirPath);
            }
        }

        return workingDirPath;
    }

    /**
     * Loads the specified CSS file from the class loader resources and adds its URI to the provided list.
     * This method is used as a fallback when the CSS file is not found in the local file system.
     *
     * @param cssFile the {@link CssFile} enum representing the CSS file to load
     * @param cssUris the list to which the CSS URI will be added if the resource is found
     */
    private static void loadFromResource(CssFile cssFile, List<String> cssUris) {
        String resourcePath = cssFile.getResourcePath();

        // Attempt to retrieve the resource URL using the class loader
        URL resourceUrl = CssUtils.class.getClassLoader().getResource(resourcePath);
        if (resourceUrl != null) {
            String cssResourceUri = resourceUrl.toExternalForm();
            cssUris.add(cssResourceUri);
            LOG.info("Loaded CSS '{}' from class loader resource: {}", cssFile.getFileName(), cssResourceUri);
        } else {
            LOG.error("CSS resource '{}' not found in class loader.", resourcePath);
        }
    }

    /**
     * Sets up CSSFX to monitor changes in the specified CSS files that were loaded from the local file system.
     * This enables live-reloading of CSS stylesheets during development, allowing for immediate visual feedback
     * when CSS files are modified.
     *
     * @param cssFiles   the array of {@link CssFile} enums that were loaded from the file system
     * @param workingDir the working directory {@link Path} used to resolve the CSS file paths
     */
    private static void setupCssMonitor(CssFile[] cssFiles, Path workingDir) {
        final URIToPathConverter myCssConverter = uri -> {
            for (CssFile cssFile : cssFiles) {
                if (uri.endsWith(cssFile.getFileName())) { // More precise matching
                    Path cssPath = cssFile.resolvePathForMonitoring(workingDir);
                    if (Files.exists(cssPath)) {
                        LOG.debug("CSSFX will monitor changes for: {}", cssPath);
                        return cssPath;
                    } else {
                        LOG.warn("CSSFX could not find the path to monitor for CSS file '{}': {}", cssFile.getFileName(), cssPath);
                    }
                }
            }
            return null;
        };

        try {
            CSSFX.addConverter(myCssConverter).start();
            LOG.info("CSSFX has been initialized for live-reloading of CSS files.");
        } catch (Exception e) {
            LOG.error("Failed to initialize CSSFX: {}", e.getMessage(), e);
        }
    }
}
