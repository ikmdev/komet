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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Enum representing supported CSS files along with their associated metadata.
 * <p>
 * Each enum constant encapsulates the CSS file name, module name, resource path within the module
 * and the base directory for resources.
 * </p>
 * <p>
 * To add a new CSS file, declare a new enum constant with the appropriate metadata.
 * Ensure that the resource path uses forward slashes (/) and matches the actual file location.
 * </p>
 *
 * @see CssUtils
 */
public enum CssFile {

    KOMET_CSS(
            "komet.css",
            "dev.ikm.komet.framework",
            "dev/ikm/komet/framework/graphics/",
            Paths.get("framework", "src", "main", "resources")
    ),

    KVIEW_CSS(
            "kview.css",
            "dev.ikm.komet.kview",
            "dev/ikm/komet/kview/mvvm/view/",
            Paths.get("kview", "src", "main", "resources")
    );

    /**
     * The name of the application project module. Used to determine the working directory.
     */
    private static final String APPLICATION_PROJECT_NAME = "application";

    private final String fileName;
    private final String moduleName;
    private final String resourcePath;
    private final Path resourceBaseDir;

    /**
     * Constructs a CssFile enum constant with the specified metadata.
     *
     * @param fileName        the name of the CSS file
     * @param moduleName      the name of the module where the CSS file resides
     * @param resourcePath    the resource path within the module
     * @param resourceBaseDir the base directory within the project for resources
     */
    CssFile(String fileName, String moduleName, String resourcePath, Path resourceBaseDir) {
        this.fileName = fileName;
        this.moduleName = moduleName;
        this.resourcePath = resourcePath;
        this.resourceBaseDir = resourceBaseDir;
    }

    /**
     * Gets the CSS file name.
     *
     * @return the CSS file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the module name associated with the CSS file.
     *
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Gets the resource path within the module for the CSS file.
     *
     * @return the resource path
     */
    public String getResourcePath() {
        return resourcePath + fileName;
    }

    /**
     * Gets the relative path from the resources directory to the CSS file.
     *
     * @return the relative Path
     */
    public Path getRelativePath() {
        return Path.of(getResourcePath());
    }

    /**
     * Gets the base directory for resources within the project.
     *
     * @return the resource base directory
     */
    public Path getResourceBaseDir() {
        return resourceBaseDir;
    }

    /**
     * Resolves the absolute path to the CSS file based on the given working directory.
     *
     * @param workingDir the working directory Path
     * @return the absolute Path to the CSS file
     */
    public Path resolveAbsolutePath(Path workingDir) {
        final Path partialPath = (workingDir.toString().contains(APPLICATION_PROJECT_NAME)) ?
                workingDir.resolveSibling(getResourceBaseDir()) : workingDir.resolve(getResourceBaseDir());
        return partialPath.resolve(getRelativePath());
    }

    /**
     * Resolves the absolute path to the CSS file for monitoring based on the given working directory.
     *
     * @param workingDir the working directory Path
     * @return the absolute Path to the CSS file for monitoring
     */
    public Path resolvePathForMonitoring(Path workingDir) {
        return resolveAbsolutePath(workingDir);
    }
}
