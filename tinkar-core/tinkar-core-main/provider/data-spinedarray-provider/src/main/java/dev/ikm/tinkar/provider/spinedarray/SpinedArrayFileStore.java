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
package dev.ikm.tinkar.provider.spinedarray;

import dev.ikm.tinkar.collection.SpineFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import static dev.ikm.tinkar.collection.SpineFileUtil.SPINE_PREFIX;

public class SpinedArrayFileStore {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayFileStore.class);
    protected final Semaphore diskSemaphore;

    protected final File directory;

    protected int spineSize;

    public SpinedArrayFileStore(File directory) {
        this(directory, new Semaphore(1));
    }

    public SpinedArrayFileStore(File directory, Semaphore diskSemaphore) {
        this.directory = directory;
        this.directory.mkdirs();
        this.spineSize = SpineFileUtil.readSpineCount(directory);
        this.diskSemaphore = diskSemaphore;
    }

    public final void writeSpineCount(int spineCount) {
        try {
            this.spineSize = spineCount;
            SpineFileUtil.writeSpineCount(directory, spineCount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final int getSpineCount() {
        return SpineFileUtil.readSpineCount(directory);
    }

    public final int sizeOnDisk() {
        if (directory == null) {
            return 0;
        }
        File[] files = directory.listFiles((pathname) -> pathname.getName().startsWith(SPINE_PREFIX));
        int size = 0;
        for (File spineFile : files) {
            size = (int) (size + spineFile.length());
        }
        return size;
    }
}
