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

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
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
import java.util.Set;

public class IntIntSetFileStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntIntArrayFileStore.class);
    final File patternToElementNidsMapDirectory;
    final File patternToElementNidsMapData;
    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> patternElementNidsMap = ConcurrentHashMap.newMap();

    public IntIntSetFileStore(File patternToElementNidsMapDirectory) {
        this.patternToElementNidsMapDirectory = patternToElementNidsMapDirectory;
        this.patternToElementNidsMapData = new File(patternToElementNidsMapDirectory, "data");
    }

    public void read() throws IOException {
        if (patternToElementNidsMapData.exists()) {
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(patternToElementNidsMapData)))) {
                int patternKeyCount = dis.readInt();
                this.patternElementNidsMap = ConcurrentHashMap.newMap(patternKeyCount);
                for (int patternCount = 0; patternCount < patternKeyCount; patternCount++) {
                    int patternNid = dis.readInt();
                    int elementNidCount = dis.readInt();
                    ConcurrentHashMap<Integer, Integer> elementNidSet = ConcurrentHashMap.newMap(elementNidCount);
                    for (int elementCount = 0; elementCount < elementNidCount; elementCount++) {
                        int elementNid = dis.readInt();
                        elementNidSet.put(elementNid, elementNid);
                    }
                    this.patternElementNidsMap.put(patternNid, elementNidSet);
                }
            }
        }
    }

    public void write() throws IOException {
        patternToElementNidsMapData.getParentFile().mkdirs();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(patternToElementNidsMapData)))) {
            dos.writeInt(patternElementNidsMap.size());
            for (Pair<Integer, ConcurrentHashMap<Integer, Integer>> keyValue : patternElementNidsMap.keyValuesView()) {
                dos.writeInt(keyValue.getOne());
                dos.writeInt(keyValue.getTwo().size());
                for (Integer elementNid : keyValue.getTwo().keySet()) {
                    dos.writeInt(elementNid);
                }
            }
        }
    }


    public boolean addToSet(int patternNid, int elementNid) {
        return null == patternElementNidsMap.getIfAbsentPut(patternNid, integer -> new ConcurrentHashMap<>())
                .put(elementNid, elementNid);
    }

    public Set<Integer> getElementNidsForPatternNid(int patternNid) {
        if (patternElementNidsMap.containsKey(patternNid)) {
            return patternElementNidsMap.get(patternNid).keySet();
        }
        return Set.of();
    }
}
