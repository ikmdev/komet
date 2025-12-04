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
package dev.ikm.tinkar.common.service;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ByteList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.factory.primitive.LongSets;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.ObjIntConsumer;

public interface PrimitiveDataService {

    int FIRST_NID = Integer.MIN_VALUE + 1;
    byte STAMP_DATA_TYPE = 7;

    ConcurrentHashSet<Integer> canceledStampNids = new ConcurrentHashSet<>();

    static int nidForUuids(ConcurrentMap<UUID, Integer> uuidNidMap, NidGenerator nidGenerator, ImmutableList<UUID> uuidList) {
        switch (uuidList.size()) {
            case 0:
                throw new IllegalStateException("uuidList cannot be empty");
            case 1: {
                return valueOrGenerateAndPut(uuidList.get(0), uuidNidMap, nidGenerator);
            }
        }
        return valueOrGenerateForList(uuidList.toSortedList(), uuidNidMap, nidGenerator);
    }

    static int valueOrGenerateAndPut(UUID uuid,
                                     ConcurrentMap<UUID, Integer> uuidNidMap,
                                     NidGenerator nidGenerator) {
        Integer nid = uuidNidMap.get(uuid);
        if (nid != null) {
            return nid;
        }
        nid = uuidNidMap.computeIfAbsent(uuid, uuidKey -> nidGenerator.newNid());
        return nid;
    }

    static int valueOrGenerateForList(ListIterable<UUID> sortedUuidList,
                                      ConcurrentMap<UUID, Integer> uuidNidMap,
                                      NidGenerator nidGenerator) {
        boolean missingMap = false;
        int foundValue = Integer.MIN_VALUE;

        for (UUID uuid : sortedUuidList) {
            Integer nid = uuidNidMap.get(uuid);
            if (nid == null) {
                missingMap = true;
            } else {
                if (foundValue == Integer.MIN_VALUE) {
                    foundValue = nid;
                } else {
                    if (foundValue != nid) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Multiple nids for: ");
                        sb.append(sortedUuidList);
                        sb.append(" first value: ").append(foundValue);
                        sb.append(" second value: ").append(nid);
                        throw new IllegalStateException(sb.toString());
                    }
                }
            }
        }
        if (!missingMap) {
            return foundValue;
        }
        if (foundValue == Integer.MIN_VALUE) {
            foundValue = valueOrGenerateAndPut(sortedUuidList.get(0), uuidNidMap, nidGenerator);
        }
        for (UUID uuid : sortedUuidList) {
            uuidNidMap.put(uuid, foundValue);
        }
        return foundValue;
    }

    static int nidForUuids(ConcurrentMap<UUID, Integer> uuidNidMap, NidGenerator nidGenerator, UUID... uuids) {
        switch (uuids.length) {
            case 0:
                throw new IllegalStateException("uuidList cannot be empty");
            case 1:
                return valueOrGenerateAndPut(uuids[0], uuidNidMap, nidGenerator);
        }
        Arrays.sort(uuids);
        return valueOrGenerateForList(Lists.immutable.of(uuids), uuidNidMap, nidGenerator);
    }

    /**
     * Merge bytes from concurrently created entities. Method is idempotent.
     * Versions will not be duplicated as a result of calling method multiple times.
     * <p>
     * Used for map.merge functions in concurrent maps.
     *
     * @param oldBytes
     * @param newBytes
     * @return
     */
    static byte[] merge(byte[] oldBytes, byte[] newBytes) {
        if (oldBytes == null) {
            return newBytes;
        }
        if (newBytes == null) {
            return oldBytes;
        }
        if (Arrays.equals(oldBytes, newBytes)) {
            return oldBytes;
        }
        try {
            MutableSet<ByteList> byteArraySet = Sets.mutable.empty();
            MutableIntList stampList = IntLists.mutable.withInitialCapacity(16);
            byte entityFormat = newBytes[8];
            addToSet(newBytes, byteArraySet, stampList, entityFormat);
            addToSet(oldBytes, byteArraySet, stampList, entityFormat);
            MutableList<ByteList> byteArrayList = byteArraySet.toList();

            byteArrayList.sort((o1, o2) -> {
                int minSize = Math.min(o1.size(), o2.size());
                for (int i = 0; i < minSize; i++) {
                    if (o1.get(i) != o2.get(i)) {
                        return Integer.compare(o1.get(i), o2.get(i));
                    }
                }
                return Integer.compare(o1.size(), o2.size());
            });
            // Remove canceled versions here
            if (byteArrayList.size() > 2) {
                MutableList<ByteList> chronologyByteLists = Lists.mutable.empty();
                MutableIntList indexesToRemove = IntLists.mutable.empty();
                for (int i = 0; i < byteArrayList.size(); i++) {
                    ByteList versionBytes = byteArrayList.get(i);
                    byte versionToken = versionBytes.get(0);
                    switch (versionToken) {
                        /*
                            CONCEPT_CHRONOLOGY((byte) 1, ConceptChronology.class),
                            PATTERN_CHRONOLOGY((byte) 2, PatternChronology.class),
                            SEMANTIC_CHRONOLOGY((byte) 3, SemanticChronology.class),
                            STAMP(STAMP_DATA_TYPE, Stamp.class)
                        */
                        case 1, 2, 3, STAMP_DATA_TYPE-> chronologyByteLists.add(versionBytes);

                        /*
                            CONCEPT_VERSION((byte) 4, ConceptVersion.class),
                            PATTERN_VERSION((byte) 5, PatternVersion.class),
                            SEMANTIC_VERSION((byte) 6, SemanticVersion.class),
                            STAMP_VERSION((byte) 25, Stamp.class)
                         */
                        case 4, 5, 6, 25 -> {
                            int stampNid = ((versionBytes.get(1) & 0xFF) << 24) |
                                    ((versionBytes.get(2) & 0xFF) << 16) |
                                    ((versionBytes.get(3) & 0xFF) << 8) |
                                    ((versionBytes.get(4) & 0xFF) << 0);
                            if (PrimitiveData.get().isCanceledStampNid(stampNid)) {
                                // Garbage collection for canceled versions...
                                indexesToRemove.add(i);
                            }
                        }
                        default -> {
                            // Leave all versions. Need to retain canceled version if component is a stamp.
                        }
                    }
                }
                indexesToRemove.reverseThis().forEach(index -> byteArrayList.remove(index));

                // UUIDs were added...
                if (chronologyByteLists.size() > 1) {
                    // need to merge into one record for the chronology...
                    MutableSet<UUID> uuids = Sets.mutable.empty();
                    for (ByteList chronologyByteList : chronologyByteLists) {
                        MutableLongList longList = LongLists.mutable.empty();
                        ByteBuf chronologyBytes = ByteBuf.wrapForReading(chronologyByteList.toArray());
                        chronologyBytes.readByte(); // EntityType token
                        chronologyBytes.readInt(); // Entity nid
                        longList.add(chronologyBytes.readLong()); // Entity most significant bits
                        longList.add(chronologyBytes.readLong()); // Entity least significant bits
                        int additionalUuidLongs = chronologyBytes.readByte(); // Additional UUID longs...
                        for (int i = 0; i < additionalUuidLongs; i++) {
                            longList.add(chronologyBytes.readLong());
                        }
                        uuids.addAll(UuidUtil.toList(longList.toArray()).castToList());
                    }
                    ImmutableList<UUID> uuidList = uuids.toImmutableList();
                    ByteBuf chronologyBytes = ByteBuf.wrapForReading(chronologyByteLists.get(0).toArray());
                    ByteBuf writeBuf = ByteBufPool.allocate(16 * uuidList.size() + chronologyBytes.array().length);
                    writeBuf.writeByte(chronologyBytes.readByte()); // EntityType token
                    writeBuf.writeInt(chronologyBytes.readInt()); // Entity nid
                    chronologyBytes.readLong(); // Discard msb
                    chronologyBytes.readLong(); // Discard lsb
                    int discardAdditionalUuidLongs = chronologyBytes.readByte();
                    for (int i = 0; i < discardAdditionalUuidLongs; i++) {
                        chronologyBytes.readLong();
                    }

                    // write the new UUIDs.
                    writeBuf.writeLong(uuidList.get(0).getMostSignificantBits());
                    writeBuf.writeLong(uuidList.get(0).getLeastSignificantBits());
                    int additionalUuidLongs = uuidList.size() * 2 - 2;
                    writeBuf.writeByte((byte) additionalUuidLongs);
                    for (int uuidIndex = 1; uuidIndex < uuidList.size(); uuidIndex++) {
                        writeBuf.writeLong(uuidList.get(uuidIndex).getMostSignificantBits());
                        writeBuf.writeLong(uuidList.get(uuidIndex).getLeastSignificantBits());
                    }
                    while (chronologyBytes.canRead()) {
                        writeBuf.writeByte(chronologyBytes.readByte());
                    }
                    byteArrayList.removeAll(chronologyByteLists);
                    byteArrayList.add(0, ByteLists.immutable.of(writeBuf.asArray()));
                }
            }

            ByteBuf byteBuf = ByteBufPool.allocate(oldBytes.length + newBytes.length);
            byteBuf.writeInt(byteArrayList.size());
            boolean first = true;
            for (ByteList byteArray : byteArrayList) {
                if (first) {
                    // Add 4 to have room for the number of versions.
                    // Add 1 for the entity format token
                    byteBuf.writeInt(byteArray.size() + 5);
                    byteBuf.writeByte(entityFormat);
                    byteArray.forEach(b -> {
                        byteBuf.put(b);
                    });
                    // write the number of versions...
                    byteBuf.writeInt(byteArrayList.size() - 1);
                    first = false;
                } else {
                    byteBuf.writeInt(byteArray.size());
                    byteArray.forEach(b -> {
                        byteBuf.put(b);
                    });
                }
            }
            return byteBuf.asArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param bytes
     * @param byteArraySet
     * @param stampsInSet  represents the stamps already added to the set. If two versions with the same stamp are being merged,
     *                     the newer version must be the merged version, as it represents a newer edit. There is an assumption that
     *                     the edits of a single version under a single stamp value are sequential, not concurrent.
     * @throws IOException
     */
    private static void addToSet(byte[] bytes, MutableSet<ByteList> byteArraySet, MutableIntList stampsInSet,
                                 byte entityFormat) throws IOException {
        ByteBuf readBuf = ByteBuf.wrapForReading(bytes);
        boolean stampDataType = bytes[9] == STAMP_DATA_TYPE;
        int arrayCount = readBuf.readInt();
        for (int i = 0; i < arrayCount; i++) {
            int arraySize = readBuf.readInt();
            if (i == 0) {
                byte localEntityFormat = readBuf.readByte();
                if (localEntityFormat != entityFormat) {
                    throw new IllegalStateException("All entities should be the same format. Found: " + entityFormat + " != " + localEntityFormat);
                }
                // The first array is the chronicle, and has a field for the number of versions...
                // Add one for the entityFormat token.
                byte[] newArray = new byte[arraySize - 5];
                readBuf.read(newArray);
                byteArraySet.add(ByteLists.immutable.of(newArray));
                int versionCount = readBuf.readInt();
                if (versionCount != arrayCount - 1) {
                    throw new IllegalStateException("Malformed data. versionCount: " +
                            versionCount + " arrayCount: " + arrayCount);
                }
                // Version count is not included as the version count may change as a result of merge.
                // It must be added back in after sorting unique versions.
            } else {
                byte[] newArray = new byte[arraySize];
                readBuf.read(newArray);
                if (stampDataType) {
                    byteArraySet.add(ByteLists.immutable.of(newArray));
                } else {
                    int stampNid = ((newArray[1] & 0xFF) << 24) |
                            ((newArray[2] & 0xFF) << 16) |
                            ((newArray[3] & 0xFF) << 8) |
                            ((newArray[4] & 0xFF) << 0);
                    if (stampsInSet.contains(stampNid)) {
                        // Don't add, a newer version already exists (assuming addToSet is called in order of newest to oldest bytearray)
                        // There should be no concurrent editing on versions with the same stamp.
                    } else {
                        byteArraySet.add(ByteLists.immutable.of(newArray));
                        stampsInSet.add(stampNid);
                    }
                }
            }
        }
    }

    default boolean isCanceledStampNid(int stampNid) {
        return canceledStampNids.contains(stampNid);
    }

    static long[] mergeCitations(long[] citation1, long[] citation2) {
        if (citation1 == null) {
            return citation2;
        }
        if (citation2 == null) {
            return citation1;
        }
        if (Arrays.equals(citation1, citation2)) {
            return citation1;
        }
        MutableLongSet citationSet = LongSets.mutable.of(citation1);
        citationSet.addAll(citation2);
        return citationSet.toSortedArray();
    }

    long writeSequence();

    void close();

    default int nidForPublicId(PublicId publicId) {
        return nidForUuids(publicId.asUuidArray());
    }

    int nidForUuids(UUID... uuids);

    int nidForUuids(ImmutableList<UUID> uuidList);

    boolean hasUuid(UUID uuid);

    boolean hasPublicId(PublicId publicId);

    void forEach(ObjIntConsumer<byte[]> action);

    void forEachParallel(ObjIntConsumer<byte[]> action);

    void forEachParallel(ImmutableIntList nids, ObjIntConsumer<byte[]> action);

    byte[] getBytes(int nid);

    /**
     * If the specified nid (native identifier -- an int) is not already associated
     * with a value or is associated with null, associates it with the given non-null value.
     * Otherwise, replaces the associated value with the results of a remapping function
     * (the provider provides remapping function), or removes if the result is {@code null}.
     * This method may be of use when combining multiple mapped values for a nid.
     * For example, merging multiple versions of an entity, where each version is represented as a
     * byte[].
     *
     * Defaults to an activity of DataActivity.SYNCHRONIZABLE_EDIT.
     *
     * @param nid                    native identifier (an int) with which the resulting value is to be associated
     * @param patternNid
     * @param referencedComponentNid if the bytes are for a semantic, the referenced component nid,
     *                               otherwise Integer.MAX_VALUE.
     * @param value                  the non-null value to be merged with the existing value
     *                               associated with the nid or, if no existing value or a null value
     *                               is associated with the nid, to be associated with the nid
     * @param sourceObject           object that is the source of the bytes to merge.
     * @return the new value associated with the specified nid, or null if no
     * value is associated with the nid
     */
    default byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject) {
        return this.merge(nid, patternNid, referencedComponentNid, value, sourceObject, DataActivity.SYNCHRONIZABLE_EDIT);
    }

    /**
     * If the specified nid (native identifier -- an int) is not already associated with a value or is associated
     * with null, associates it with the given non-null value. Otherwise, replaces the associated value with the
     * results of a remapping function (the provider provides remapping function), or removes if the result is
     * null. This method may be of use when combining multiple mapped values for a nid. For example, merging multiple
     * versions of an entity, where each version is represented as a byte[].
     *
     * @param nid Native identifier (an int) with which the resulting value is to be associated.
     * @param patternNid Pattern native identifier.
     * @param referencedComponentNid If the bytes are for a semantic, the referenced component nid,
     *                               otherwise Integer.MAX_VALUE.
     * @param value The non-null value to be merged with the existing value
     *              associated with the nid or, if no existing value or a null value
     *              is associated with the nid, to be associated with the nid.
     * @param sourceObject Object that is the source of the bytes to merge.
     * @param activity The data activity performed, classifying the type of database (and therefore change set) write.
     * @return The new value associated with the specified nid, or null if no
     *         value is associated with the nid.
     */
    byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject, DataActivity activity);

    PrimitiveDataSearchResult[] search(String query, int maxResultSize) throws Exception;

    CompletableFuture<Void> recreateLuceneIndex() throws Exception;

    /**
     * @param patternNid
     * @return
     */
    default int[] semanticNidsOfPattern(int patternNid) {
        MutableIntList intList = IntLists.mutable.empty();
        forEachSemanticNidOfPattern(patternNid, nid -> intList.add(nid));
        return intList.toArray();
    }

    void forEachSemanticNidOfPattern(int patternNid, IntProcedure procedure);

    void forEachPatternNid(IntProcedure procedure);

    void forEachConceptNid(IntProcedure procedure);

    void forEachStampNid(IntProcedure procedure);

    void forEachSemanticNid(IntProcedure procedure);

    default int[] semanticNidsForComponent(int componentNid) {
        MutableIntList intList = IntLists.mutable.empty();
        forEachSemanticNidForComponent(componentNid, nid -> intList.add(nid));
        return intList.toArray();
    }

    void forEachSemanticNidForComponent(int componentNid, IntProcedure procedure);

    default int[] semanticNidsForComponentOfPattern(int componentNid, int patternNid) {
        MutableIntList intList = IntLists.mutable.empty();
        forEachSemanticNidForComponentOfPattern(componentNid, patternNid, nid -> intList.add(nid));
        return intList.toArray();
    }

    void forEachSemanticNidForComponentOfPattern(int componentNid, int patternNid, IntProcedure procedure);

    default void addCanceledStampNid(int stampNid) {
        canceledStampNids.add(stampNid);
    }

    /**
     * @return user-friendly name for this data service
     */
    String name();

    enum RemoteOperations {
        NID_FOR_UUIDS(1),
        GET_BYTES(2),
        MERGE(3);

        public final byte token;

        RemoteOperations(int token) {
            this.token = (byte) token;
        }

        public static RemoteOperations fromToken(byte token) {
            switch (token) {
                case 1:
                    return NID_FOR_UUIDS;
                case 2:
                    return GET_BYTES;
                case 3:
                    return MERGE;
                default:
                    throw new UnsupportedOperationException("Can't handle token: " + token);
            }
        }
    }

    class CacheProvider implements CachingService {

        @Override
        public void reset() {
            canceledStampNids.clear();
        }
    }

}
