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
import dev.ikm.tinkar.collection.store.IntLongArrayStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IntLongArrayFileStore extends SpinedArrayFileStore implements IntLongArrayStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntLongArrayFileStore.class);

    public IntLongArrayFileStore(File directory) {
        super(directory);
    }

    public IntLongArrayFileStore(File directory, Semaphore diskSemaphore) {
        super(directory, diskSemaphore);
    }

    @Override
    public Optional<AtomicReferenceArray<long[]>> get(int spineIndex) {
        String spineKey = SpineFileUtil.SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        if (!spineFile.exists()) {
            return Optional.empty();
        }
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
            int arraySize = dis.readInt();
            AtomicReferenceArray<long[]> data = new AtomicReferenceArray<>(arraySize);
            for (int i = 0; i < arraySize; i++) {
                int valueSize = dis.readInt();
                if (valueSize != 0) {
                    long[] value = new long[valueSize];
                    for (int j = 0; j < valueSize; j++) {
                        value[j] = dis.readLong();
                    }
                    data.set(i, value);
                }
            }
            return Optional.of(data);
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<long[]> spine) {
        directory.mkdirs();
        String spineKey = SpineFileUtil.SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        diskSemaphore.acquireUninterruptibly();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
            dos.writeInt(spine.length());
            for (int i = 0; i < spine.length(); i++) {
                long[] value = spine.get(i);
                if (value == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(value.length);
                    for (long valueElement : value) {
                        dos.writeLong(valueElement);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            diskSemaphore.release();
        }
    }
}
