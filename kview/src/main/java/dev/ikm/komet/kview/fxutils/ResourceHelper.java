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
package dev.ikm.komet.kview.fxutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for obtaining resources based on a string path that can be the following path types: absolute, relative, and parent relative.
 * For example: absolute is prefixed with a '/'. Relative is a file or a subdirectory. A parent relative path is using the ../ convention to
 * walk up the directory tree.
 */
public class ResourceHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceHelper.class);
    public enum PathType {
        ABSOLUTE,
        RELATIVE,
        PARENT_RELATIVE,
        UNKNOWN
    }
    public static PathType determinePathType(String path) {
        if (path != null) {
            if (path.startsWith("/")) {
                return PathType.ABSOLUTE;
            }
            if (path.startsWith("../")) {
                return PathType.PARENT_RELATIVE;
            }
            return PathType.RELATIVE;
        }
        return PathType.UNKNOWN;
    }

    /**
     * Return a class package name as an absolute file path. e.g. com.foo.bar.MyClass becomes /com/foo/bar
     * This helper method helps find resources in the same module.
     * @param clazz Class package namespace to be converted.
     * @return String of the package namespace as an absolute path.
     */
    public static String obtainPackageAsPath(Class<?> clazz) {
        return "/" + clazz.getPackageName().replaceAll("\\.", "/");
    }

    /**
     * Based on a package namespace (class) and path string it can determine the following:
     * If a class is foo.bar.hello.MyClass the scenarios can exist to locate a resource.
     * <pre>
     *     1. absolute path - /foo/bar/baz/test.txt
     *     2. relative path - bar/test.txt becomes /foo/bar/test.txt
     *     3. parent relative path - ../../fiz/test.txt becomes /foo/fiz/test.txt
     * </pre>
     * @param pathString an absolute path, relative path or a parent relative path.
     * @param clazz The class the file is located in a namespace.
     * @return A resolved path string.
     */
    public static String toAbsolutePath(String pathString, Class<?> clazz) {
        // is absolute, relative path or parent relative path
        PathType pathType = determinePathType(pathString);

        // if unknown just throw Runtime
        if (pathType == PathType.UNKNOWN) {
            throw new RuntimeException("pathString is not a valid URL. Class used: " + clazz);
        }

        // if absolute just return URL string.
        if (pathType == PathType.ABSOLUTE) {
            return pathString;
        }

        // if relative path obtain absolute path and concatenate with path
        if (pathType == PathType.RELATIVE) {
            return obtainPackageAsPath(clazz) + "/" + pathString;
        }

        // if parent relative path (to walk up directories)
        int parentPathCount = numberOfOccurrence("../", pathString);
        String parentPath = obtainPackageAsPath(clazz);
        String childPath = pathString;

        // walk up directory and remove (../) from pathString.
        for (int i=0; i<parentPathCount; i++) {
            parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"));
            childPath = childPath.substring(3); // 3 characters (../)
        }
        return parentPath + "/" + childPath;

    }
    public static int numberOfOccurrence(String substr, String fullString) {
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1) {
            lastIndex = fullString.indexOf(substr,lastIndex);
            if(lastIndex != -1){
                count ++;
                lastIndex += substr.length();
            }
        }
        return count;
    }
    public static void main(String[] args) {
        int numberOfOccurrence = numberOfOccurrence("../", "../../hello");
        PathType pathType1 = determinePathType("/sss");
        PathType pathType2 = determinePathType("../");
        PathType pathType3 = determinePathType("hello/");
        PathType pathType4 = determinePathType(null);
        LOG.info("%s %s %s %s".formatted(pathType1, pathType2, pathType3, pathType4));
        LOG.info(String.valueOf(numberOfOccurrence));
        String url1 = toAbsolutePath("abc.txt", ResourceHelper.class);
        String url2 = toAbsolutePath("/dev/ikm/komet/kview/utils/abc.txt", ResourceHelper.class);
        String url3 = toAbsolutePath("../../abc.txt", ResourceHelper.class);
        String url4 = toAbsolutePath("../abc.txt", ResourceHelper.class);
        LOG.info("1:%s\n2:%s \n3:%s \n4:%s".formatted(url1, url2, url3, url4));
        // test "../details/kview-details.fxml"

        String url5 = toAbsolutePath("../details/kview-details.fxml", ResourceHelper.class);
        LOG.info("Url 5 test. " + url5);
    }
}
