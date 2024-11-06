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

import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility interface for managing and applying CSS stylesheets.
 * <p>
 * This interface provides static methods to add CSS stylesheets to JavaFX scenes.
 * It attempts to load CSS files from the local file system for development purposes
 * and falls back to the JRT (Java Runtime) file system for production environments.
 * Additionally, it integrates with CSSFX to enable live-reloading of CSS files during development.
 * </p>
 * <p>
 * Supported CSS files are declared as constants within this interface. To add more CSS files,
 * declare additional constants and update the relevant methods accordingly.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * import dev.ikm.komet.app.util.CssUtils;
 * import javafx.scene.Scene;
 * import javafx.scene.layout.BorderPane;
 *
 * // ...
 *
 * BorderPane root = new BorderPane();
 * Scene scene = new Scene(root, 800, 600);
 *
 * // Apply CSS stylesheets
 * CssUtils.addStylesheets(scene, CssUtils.KOMET_CSS, CssUtils.KVIEW_CSS);
 *
 * // Set up and show the stage
 * primaryStage.setScene(scene);
 * primaryStage.show();
 * }</pre>
 */
public interface CssUtils {

    Logger LOG = LoggerFactory.getLogger(CssUtils.class);

    // Constants for CSS file names
    String KOMET_CSS = "komet.css";
    String KVIEW_CSS = "kview.css";

    /**
     * Adds the specified CSS files to the given JavaFX Scene. It attempts to load them from the local file system
     * for development mode and falls back to the JRT file system for production mode. Also sets up CSSFX for live-reloading.
     *
     * @param scene    The JavaFX Scene to which the stylesheets will be added.
     * @param cssFiles Variable number of CSS file names to be added.
     */
    static void addStylesheets(Scene scene, String... cssFiles) {
        final String workingDir = System.getProperty("user.dir");
        Path workingDirPath = Paths.get(workingDir);

        if (workingDirPath.getFileName().toString().equals("application")) {
            // Running from the application module, move up one level
            workingDirPath = workingDirPath.getParent();
        }

        final Path frameworkResourcesDir = workingDirPath.resolve("framework/src/main/resources");
        final Path kviewResourcesDir = workingDirPath.resolve("kview/src/main/resources");

        List<String> cssUris = new ArrayList<>();
        boolean loadedFromFileSystem = false;

        for (String cssFile : cssFiles) {
            Path cssPath = determineCssPath(cssFile, frameworkResourcesDir, kviewResourcesDir);

            if (cssPath != null && Files.exists(cssPath)) {
                String cssUri = cssPath.toUri().toString();
                cssUris.add(cssUri);
                loadedFromFileSystem = true;
                LOG.info("Loaded CSS from local file system: {}", cssUri);
            } else {
                // Attempt to load from JRT (production mode)
                String moduleName = getModuleNameForCss(cssFile);
                String resourcePath = getResourcePathForCss(cssFile);

                if (moduleName != null && resourcePath != null) {
                    try {
                        FileSystem jrtFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
                        Path cssResourcePathInJrt = jrtFileSystem.getPath("/modules", moduleName, resourcePath);
                        if (Files.exists(cssResourcePathInJrt)) {
                            String cssResourceUri = cssResourcePathInJrt.toUri().toString();
                            cssUris.add(cssResourceUri);
                            LOG.info("Loaded CSS from JRT file system: {}", cssResourceUri);
                        } else {
                            LOG.warn("CSS file '{}' not found in JRT file system at '{}'", cssFile, cssResourcePathInJrt);
                        }
                    } catch (FileSystemNotFoundException e) {
                        LOG.warn("Error accessing JRT file system for CSS file '{}': {}", cssFile, e.getMessage());
                    }
                } else {
                    LOG.warn("No module mapping found for CSS file '{}'", cssFile);
                }
            }
        }

        if (!cssUris.isEmpty()) {
            scene.getStylesheets().addAll(cssUris);
        }

        if (loadedFromFileSystem) {
            setupCssMonitor(cssFiles, frameworkResourcesDir, kviewResourcesDir);
        }
    }

    /**
     * Determines the file system path of the CSS file based on its name.
     *
     * @param cssFile               The name of the CSS file.
     * @param frameworkResourcesDir The framework resources directory.
     * @param kviewResourcesDir     The kview resources directory.
     * @return The Path to the CSS file, or null if the file name is unrecognized.
     */
    private static Path determineCssPath(String cssFile, Path frameworkResourcesDir, Path kviewResourcesDir) {
        return switch (cssFile) {
            case KOMET_CSS -> frameworkResourcesDir.resolve(
                    Paths.get("dev", "ikm", "komet", "framework", "graphics", KOMET_CSS));
            case KVIEW_CSS -> kviewResourcesDir.resolve(
                    Paths.get("dev", "ikm", "komet", "kview", "mvvm", "view", KVIEW_CSS));
            // Add more cases here for additional CSS files
            default -> {
                LOG.warn("Unknown CSS file '{}', unable to determine path", cssFile);
                yield null;
            }
        };
    }

    /**
     * Returns the module name associated with the given CSS file.
     *
     * @param cssFile The name of the CSS file.
     * @return The module name, or null if the file name is unrecognized.
     */
    private static String getModuleNameForCss(String cssFile) {
        return switch (cssFile) {
            case KOMET_CSS -> "dev.ikm.komet.framework";
            case KVIEW_CSS -> "dev.ikm.komet.kview";
            // Add more cases here for additional CSS files
            default -> {
                LOG.warn("Unknown CSS file '{}', no module mapping", cssFile);
                yield null;
            }
        };
    }

    /**
     * Returns the resource path within the module for the given CSS file.
     *
     * @param cssFile The name of the CSS file.
     * @return The resource path, or null if the file name is unrecognized.
     */
    private static String getResourcePathForCss(String cssFile) {
        return switch (cssFile) {
            case KOMET_CSS -> "dev/ikm/komet/framework/graphics/" + KOMET_CSS;
            case KVIEW_CSS -> "dev/ikm/komet/kview/mvvm/view/" + KVIEW_CSS;
            // Add more cases here for additional CSS files
            default -> {
                LOG.warn("Unknown CSS file '{}', no resource path mapping", cssFile);
                yield null;
            }
        };
    }

    /**
     * Sets up CSSFX to monitor CSS changes in development mode.
     *
     * @param cssFiles              The list of CSS file names.
     * @param frameworkResourcesDir The framework resources directory.
     * @param kviewResourcesDir     The kview resources directory.
     */
    private static void setupCssMonitor(String[] cssFiles, Path frameworkResourcesDir, Path kviewResourcesDir) {
        final URIToPathConverter myCssConverter = uri -> {
            for (String cssFile : cssFiles) {
                if (uri.contains(cssFile)) {
                    Path cssPath = switch (cssFile) {
                        case KOMET_CSS: yield frameworkResourcesDir.resolve(
                                Paths.get("dev", "ikm", "komet", "framework", "graphics", KOMET_CSS));
                        case KVIEW_CSS: yield kviewResourcesDir.resolve(
                                Paths.get("dev", "ikm", "komet", "kview", "mvvm", "view", KVIEW_CSS));
                        // Add more cases here for additional CSS files
                        default:
                            LOG.warn("Unknown CSS file '{}', unable to set up CSSFX converter", cssFile);
                            yield null;
                    };

                    if (cssPath != null && Files.exists(cssPath)) {
                        return cssPath;
                    }
                }
            }
            return null;
        };

        CSSFX.addConverter(myCssConverter).start();
    }
}
