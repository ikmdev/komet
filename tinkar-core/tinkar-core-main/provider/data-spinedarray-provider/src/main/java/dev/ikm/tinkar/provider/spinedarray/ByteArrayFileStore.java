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

import dev.ikm.tinkar.collection.store.ByteArrayStore;
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

import static dev.ikm.tinkar.collection.SpineFileUtil.SPINE_PREFIX;

public class ByteArrayFileStore extends SpinedArrayFileStore implements ByteArrayStore {
    private static final Logger LOG = LoggerFactory.getLogger(ByteArrayFileStore.class);

    public ByteArrayFileStore(File directory) {
        super(directory);
    }

    public ByteArrayFileStore(File directory, Semaphore diskSemaphore) {
        super(directory, diskSemaphore);
    }

    @Override
    public Optional<AtomicReferenceArray<byte[]>> get(int spineIndex) {
        String spineKey = SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        if (spineFile.exists()) {
            diskSemaphore.acquireUninterruptibly();
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                int arraySize = dis.readInt();
                byte[][] spineArray = new byte[arraySize][];
                for (int i = 0; i < arraySize; i++) {
                    int valueSize = dis.readInt();
                    if (valueSize != 0) {
                        byte[] value = new byte[valueSize];
                        dis.readFully(value);
                        spineArray[i] = value;
                    } else {
                        spineArray[i] = null;
                    }
                }
                AtomicReferenceArray<byte[]> spine = new AtomicReferenceArray<>(spineArray);
                return Optional.of(spine);

            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } finally {
                diskSemaphore.release();
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<byte[]> spine) {
        String spineKey = SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        diskSemaphore.acquireUninterruptibly();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
            dos.writeInt(spine.length());
            for (int i = 0; i < spine.length(); i++) {
                byte[] value = spine.get(i);
                if (value == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(value.length);
                    dos.write(value);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        } finally {
            diskSemaphore.release();
        }
    }

}
