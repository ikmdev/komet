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
package dev.ikm.tinkar.collection;

import java.io.File;
import java.io.IOException;


public class SpineFileUtil {
    public static final String SPINE_COUNT_PREFIX = "spineCount-";
    public static final String SPINE_PREFIX = "spine-";

    public static int readSpineCount(File directory) {
        if (directory.exists()) {
            File[] spineCountFiles = directory.listFiles((pathname) -> {
                return pathname.getName().startsWith(SPINE_COUNT_PREFIX);
            });

            switch (spineCountFiles.length) {
                case 0:
                    //throw new IllegalStateException("spineCount == 0 for: " + directory.getName());
                    return 0;
                case 1:
                    String countStr = spineCountFiles[0].getName().substring(SPINE_COUNT_PREFIX.length());
                    return Integer.parseInt(countStr);
                default:
                    throw new IllegalStateException("spineCount- > 1 for: " + directory.getName());
            }
        }
        return 0;
    }
    public static void writeSpineCount(File directory, int count) throws IOException {
        File[] spineCountFiles = directory.listFiles((pathname) -> pathname.getName().startsWith(SPINE_COUNT_PREFIX));
        for (File countFile: spineCountFiles) {
            countFile.delete();
        }
        String newCountFileName = SPINE_COUNT_PREFIX + count;
        File newCountFile = new File(directory, newCountFileName);
        newCountFile.createNewFile();
    }


    public static File getSpineDirectory(File parentDirectory, int patternNid) {
        File spinedMapDirectory = new File(parentDirectory, Integer.toUnsignedString(patternNid));

        spinedMapDirectory.mkdirs();
        return spinedMapDirectory;
    }
}
