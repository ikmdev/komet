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
package dev.ikm.tinkar.common.util.io;

import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

/**
 * {@link FileUtil}.
 */
public class FileUtil {
    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    //~--- methods -------------------------------------------------------------

    public static Optional<String> readFile(File file) {
        return readFile(file.toPath());
    }

    /**
     * Read file.
     *
     * @param path the path for the file.
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Optional<String> readFile(Path path) {
        try {
            return Optional.ofNullable(Files.readString(path));
        } catch (IOException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            return Optional.empty();
        }
    }

    /**
     * Recursive delete.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void recursiveDelete(File file) {
        if ((file == null) || !file.exists()) {
            return;
        }
        try {
            Files.walk(file.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
    }

    public void writeFile(File file, String content) {
        writeFile(file.toPath(), content);
    }

    public void writeFile(Path path, String content) {
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}