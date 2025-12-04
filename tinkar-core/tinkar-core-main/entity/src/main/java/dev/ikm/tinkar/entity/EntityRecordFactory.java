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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.component.Chronology;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.ConceptChronology;
import dev.ikm.tinkar.component.ConceptVersion;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.component.Pattern;
import dev.ikm.tinkar.component.PatternChronology;
import dev.ikm.tinkar.component.PatternVersion;
import dev.ikm.tinkar.component.Semantic;
import dev.ikm.tinkar.component.SemanticChronology;
import dev.ikm.tinkar.component.SemanticVersion;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.component.graph.DiGraph;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.location.PlanarPoint;
import dev.ikm.tinkar.component.location.SpatialPoint;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.*;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;
import static dev.ikm.tinkar.component.FieldDataType.COMPONENT_ID_LIST;
import static dev.ikm.tinkar.component.FieldDataType.SEMANTIC_CHRONOLOGY;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EntityRecordFactory {
    private static final Logger LOG = LoggerFactory.getLogger(EntityRecordFactory.class);
    public static final byte ENTITY_FORMAT_VERSION = 1;
    public static final int DEFAULT_ENTITY_SIZE = 32767;
    public static final int DEFAULT_VERSION_SIZE = 16384;
    public static volatile int MAX_ENTITY_SIZE = DEFAULT_ENTITY_SIZE;
    public static volatile int MAX_VERSION_SIZE = DEFAULT_VERSION_SIZE;

    public static byte[] getBytes(Entity<? extends EntityVersion> entity) {
        // TODO: write directly to a single ByteBuf, rather that the approach below.
        boolean complete = false;
        while (!complete) {
            try {
                ByteBuf byteBuf = ByteBufPool.allocate(MAX_ENTITY_SIZE);
                // one byte for version...
                //byte[0]
                byteBuf.writeByte(ENTITY_FORMAT_VERSION);
                //byte[1]
                byteBuf.writeByte(entity.entityDataType().token); //ensure that the chronicle byte array sorts first.
                //byte[2-5]
                byteBuf.writeInt(entity.nid());
                //byte[6-13]
                byteBuf.writeLong(entity.mostSignificantBits());
                //byte[14-21]
                byteBuf.writeLong(entity.leastSignificantBits());

                long[] additionalUuidLongs = entity.additionalUuidLongs();
                if (additionalUuidLongs == null) {
                    //byte[22]
                    byteBuf.writeByte((byte) 0);
                } else {
                    //byte[22]
                    byteBuf.writeByte((byte) additionalUuidLongs.length);

                    for (int i = 0; i < additionalUuidLongs.length; i++) {
                        //byte[23 + (8*i) -> byte[30 + (8*i)]
                        byteBuf.writeLong(additionalUuidLongs[i]);
                    }
                }
                switch (entity) {
                    case SemanticEntity semanticEntity:
                        byteBuf.writeInt(semanticEntity.referencedComponentNid());
                        byteBuf.writeInt(semanticEntity.patternNid());
                        break;
                    case ConceptRecord conceptEntity:
                        // No additional fieldValues for concept records.
                        break;
                    case PatternEntity patternEntity:
                        // no additional fieldValues
                        break;
                    case StampEntity stampEntity:
                        // no additional fieldValues
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + entity);
                }
                //finishEntityWrite(byteBuf);
                byteBuf.writeInt(entity.versions().size());

                int chronicleArrayCount = entity.versions().size() + 1;
                int chronicleFieldIndex = 0;
                byte[][] entityArray = new byte[chronicleArrayCount][];
                entityArray[chronicleFieldIndex++] = byteBuf.asArray();
                for (EntityVersion version : entity.versions()) {
                    entityArray[chronicleFieldIndex++] = getBytes(version);
                }
                int totalSize = 0;
                totalSize += 4; // Integer for the number of arrays
                for (byte[] arrayBytes : entityArray) {
                    totalSize += 4; // integer for size of array
                    totalSize += arrayBytes.length;
                }
                ByteBuf finalByteBuf = ByteBufPool.allocate(totalSize);
                finalByteBuf.writeInt(entityArray.length);
                for (byte[] arrayBytes : entityArray) {
                    finalByteBuf.writeInt(arrayBytes.length);
                    finalByteBuf.write(arrayBytes);
                }
                return finalByteBuf.asArray();
            } catch (ArrayIndexOutOfBoundsException e) {
                MAX_ENTITY_SIZE = MAX_ENTITY_SIZE * 2;
                LOG.info(e.getMessage() + " will increase entity size to " + MAX_ENTITY_SIZE);
            }
        }
        throw new IllegalStateException("Should never reach here. ");
    }

    public static byte[] getBytes(EntityVersion entityVersion) {
        // TODO: write directly to a single ByteBuf, rather that the approach below.
        boolean complete = false;
        while (!complete) {
            try {
                ByteBuf byteBuf = ByteBufPool.allocate(MAX_VERSION_SIZE);
                if (entityVersion.versionDataType().token == 0) {
                    throw new IllegalStateException("Version type token cannot be zero... " + entityVersion);
                }
                byteBuf.writeByte(entityVersion.versionDataType().token); //ensure that the chronicle byte array sorts first.
                byteBuf.writeInt(entityVersion.stampNid());
                switch (entityVersion) {
                    case ConceptEntityVersion conceptEntityVersion:
                        // no additional data
                        break;
                    case PatternVersionRecord patternVersionRecord:
                        byteBuf.writeInt(patternVersionRecord.semanticPurposeNid());
                        byteBuf.writeInt(patternVersionRecord.semanticMeaningNid());
                        byteBuf.writeInt(patternVersionRecord.fieldDefinitions().size());
                        for (FieldDefinitionRecord field : patternVersionRecord.fieldDefinitions()) {
                            byteBuf.writeInt(field.dataTypeNid());
                            byteBuf.writeInt(field.purposeNid());
                            byteBuf.writeInt(field.meaningNid());
                        }
                        break;
                    case SemanticEntityVersion semanticEntityVersion:
                        byteBuf.writeInt(semanticEntityVersion.fieldValues().size());
                        for (Object field : semanticEntityVersion.fieldValues()) {
                            writeField(byteBuf, field);
                        }
                        break;
                    case StampEntityVersion stampEntityVersion:
                        byteBuf.writeInt(stampEntityVersion.stateNid());
                        byteBuf.writeLong(stampEntityVersion.time());
                        byteBuf.writeInt(stampEntityVersion.authorNid());
                        byteBuf.writeInt(stampEntityVersion.moduleNid());
                        byteBuf.writeInt(stampEntityVersion.pathNid());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + entityVersion);
                }
                //writeVersionFields(byteBuf);
                return byteBuf.asArray();
            } catch (ArrayIndexOutOfBoundsException e) {
                MAX_VERSION_SIZE = MAX_VERSION_SIZE * 2;
                LOG.info(e.getMessage() + " will increase version size to " + MAX_VERSION_SIZE);
            }
        }
        throw new IllegalStateException("Should never reach here. ");
    }

    /**
     * The purpose of this class is to write a field with a byte buffer.
     * @param writeBuf
     * @param field
     */
    public static void writeField(ByteBuf writeBuf, Object field) {
        if (field == null) {
            throw new IllegalArgumentException("Field value cannot be null. Semantic field values must be initialized before serialization.");
        }
        switch (field) {
            case Boolean booleanField ->
                    writeTokenAndField(writeBuf, FieldDataType.BOOLEAN, () -> writeBuf.writeBoolean(booleanField));
            case Float floatField ->
                    writeTokenAndField(writeBuf, FieldDataType.FLOAT, () -> writeBuf.writeFloat(floatField));
            case Double doubleField ->
                    writeTokenAndField(writeBuf, FieldDataType.FLOAT, () -> writeBuf.writeFloat(doubleField.floatValue()));
            case byte[] byteArrayField ->
                    writeTokenAndField(writeBuf, FieldDataType.BYTE_ARRAY, () -> {
                        writeBuf.writeInt(byteArrayField.length);
                        writeBuf.write(byteArrayField);
                    });
            case Integer integerField ->
                    writeTokenAndField(writeBuf, FieldDataType.INTEGER, () -> writeBuf.writeInt(integerField));
            case Long longField ->
                    writeTokenAndField(writeBuf, FieldDataType.LONG, () -> writeBuf.writeLong(longField));
			case BigDecimal decimalField -> writeTokenAndField(writeBuf, FieldDataType.DECIMAL, () -> {
				byte[] bytes = decimalField.toString().getBytes(UTF_8);
				writeBuf.writeInt(bytes.length);
				writeBuf.write(bytes);
			});
            case Instant instantField ->
                    writeTokenAndField(writeBuf, FieldDataType.INSTANT, () -> {
                        writeBuf.writeLong(instantField.getEpochSecond());
                        writeBuf.writeInt(instantField.getNano());
                    });
            case String stringField ->
                    writeTokenAndField(writeBuf, FieldDataType.STRING, () -> {
                        byte[] bytes = stringField.getBytes(UTF_8);
                        writeBuf.writeInt(bytes.length);
                        writeBuf.write(bytes);
                    });
            case ConceptFacade conceptField ->
                    writeTokenAndField(writeBuf, FieldDataType.CONCEPT, () -> writeBuf.writeInt(conceptField.nid()));
            case Concept conceptField ->
                    writeTokenAndField(writeBuf, FieldDataType.CONCEPT, () -> writeBuf.writeInt(Entity.nid(conceptField)));
            case SemanticFacade semanticField ->
                    writeTokenAndField(writeBuf, FieldDataType.SEMANTIC, () -> writeBuf.writeInt(semanticField.nid()));
            case Semantic semanticField ->
                    writeTokenAndField(writeBuf, FieldDataType.SEMANTIC, () -> writeBuf.writeInt(Entity.nid(semanticField)));
            case PatternFacade patternField ->
                    writeTokenAndField(writeBuf, FieldDataType.PATTERN, () -> writeBuf.writeInt(patternField.nid()));
            case Pattern patternField ->
                    writeTokenAndField(writeBuf, FieldDataType.PATTERN, () -> writeBuf.writeInt(Entity.nid(patternField)));
            case EntityFacade entityField ->
                    writeTokenAndField(writeBuf, FieldDataType.IDENTIFIED_THING, () -> writeBuf.writeInt(entityField.nid()));
            case Component componentField ->
                    writeTokenAndField(writeBuf, FieldDataType.IDENTIFIED_THING, () -> writeBuf.writeInt(Entity.nid(componentField)));
            case DiTreeEntity diTreeEntityField ->
                    writeTokenAndField(writeBuf, FieldDataType.DITREE, () ->
                            writeBuf.write(diTreeEntityField.getBytes()));
            case PlanarPoint planarPointField ->
                    writeTokenAndField(writeBuf, FieldDataType.PLANAR_POINT, () -> {
                        writeBuf.writeByte(FieldDataType.PLANAR_POINT.token);
                        writeBuf.writeFloat(planarPointField.x());
                        writeBuf.writeFloat(planarPointField.y());
                    });
            case SpatialPoint spatialPointField ->
                    writeTokenAndField(writeBuf, FieldDataType.SPATIAL_POINT, () -> {
                        writeBuf.writeInt((int) spatialPointField.x());
                        writeBuf.writeFloat(spatialPointField.y());
                        writeBuf.writeFloat(spatialPointField.z());
                    });
            case IntIdList intIdListField ->
                    writeTokenAndField(writeBuf, COMPONENT_ID_LIST, () -> {
                        writeBuf.writeInt(intIdListField.size());
                        intIdListField.forEach(id -> writeBuf.writeInt(id));
                    });
            case IntIdSet intIdSetField ->
                    writeTokenAndField(writeBuf, FieldDataType.COMPONENT_ID_SET, () -> {
                        writeBuf.writeInt(intIdSetField.size());
                        intIdSetField.forEach(id -> writeBuf.writeInt(id));
                    });
            case PublicId publicId ->
                    writeTokenAndField(writeBuf, FieldDataType.IDENTIFIED_THING, () ->
                            writeBuf.writeInt(Entity.nid(publicId)));
            case PublicIdList publicIdListField -> {
                    MutableIntList nidList = IntLists.mutable.withInitialCapacity(publicIdListField.size());
                    publicIdListField.forEach(publicId -> {
                        nidList.add(PrimitiveData.get().nidForPublicId((PublicId) publicId));
                    });
                    writeBuf.writeByte(COMPONENT_ID_LIST.token);
                    writeBuf.writeInt(nidList.size());
                    nidList.forEach(id -> writeBuf.writeInt(id));
            }
            case PublicIdSet publicIdSetField -> {
                MutableIntList nidSet = IntLists.mutable.withInitialCapacity(publicIdSetField.size());
                publicIdSetField.forEach(publicId -> {
                    nidSet.add(PrimitiveData.get().nidForPublicId((PublicId) publicId));
                });
                writeBuf.writeByte(FieldDataType.COMPONENT_ID_SET.token);
                writeBuf.writeInt(nidSet.size());
                nidSet.forEach(id -> writeBuf.writeInt(id));
            }
            default -> throw new IllegalStateException("Unexpected value: %s of class: %s".formatted(field, field.getClass()));
        }
    }
    public static void writeTokenAndField(ByteBuf writeBuf, FieldDataType fieldDataType, Runnable writer) {
        writeBuf.writeByte(fieldDataType.token);
        writer.run();
    }

    public static void writeField(ByteBuf writeBuf, EntityFacade entityField) {
        writeBuf.writeByte(FieldDataType.IDENTIFIED_THING.token);
        writeBuf.writeInt(entityField.nid());
    }

    public static void writeField(ByteBuf writeBuf, Component componentField) {
        writeBuf.writeByte(FieldDataType.IDENTIFIED_THING.token);
        writeBuf.writeInt(Entity.nid(componentField));
    }

    public static void writeField(ByteBuf writeBuf, Semantic semanticField) {
        writeBuf.writeByte(FieldDataType.SEMANTIC.token);
        if (semanticField instanceof ComponentWithNid) {
            writeBuf.writeInt(((ComponentWithNid) semanticField).nid());
        } else {
            writeBuf.writeInt(Entity.nid(semanticField));
        }
    }

    public static void writeField(ByteBuf writeBuf, Pattern patternField) {
        writeBuf.writeByte(FieldDataType.PATTERN.token);
        if (patternField instanceof ComponentWithNid) {
            writeBuf.writeInt(((ComponentWithNid) patternField).nid());
        } else {
            writeBuf.writeInt(Entity.nid(patternField));
        }
    }

    private static long[] processAdditionalUuids(ImmutableList<UUID> componentUuids) {
        if (componentUuids.size() > 1) {
            long[] additionalUuidLongs = new long[(componentUuids.size() - 1) * 2];
            for (int listIndex = 1; listIndex < componentUuids.size(); listIndex++) {
                int additionalUuidIndex = listIndex - 1;
                additionalUuidLongs[additionalUuidIndex * 2] = componentUuids.get(listIndex).getMostSignificantBits();
                additionalUuidLongs[additionalUuidIndex * 2 + 1] = componentUuids.get(listIndex).getLeastSignificantBits();
            }
            return additionalUuidLongs;
        }
        return null;
    }

    public static void collectUuids(byte[] data, ConcurrentHashMap<Integer, ConcurrentHashSet<Integer>> patternElementNidsMap,
                                    ConcurrentHashMap<UUID, Integer> uuidToNidMap) {
        ByteBuf buf = ByteBuf.wrapForReading(data);
        // bytes starts with number of arrays (int = 4 bytes), then size of first array (int = 4 bytes), then type token
        int numberOfArrays = buf.readInt();
        int sizeOfFirstArray = buf.readInt();
        byte formatVersion = buf.readByte();
        FieldDataType fieldDataType = FieldDataType.fromToken(buf.readByte());
        if (formatVersion != ENTITY_FORMAT_VERSION) {
            throw new IllegalStateException("Unsupported entity format version: " + formatVersion);
        }
        int nid = buf.readInt();
        long mostSignificantBits = buf.readLong();
        long leastSignificantBits = buf.readLong();
        uuidToNidMap.put(new UUID(mostSignificantBits, leastSignificantBits), nid);
        int additionalUuidLongCount = buf.readByte();
        long[] additionalUuidLongs = null;

        if (additionalUuidLongCount > 0) {
            additionalUuidLongs = new long[additionalUuidLongCount];
            for (int i = 0; i < additionalUuidLongs.length; i++) {
                additionalUuidLongs[i] = buf.readLong();
            }
            for (int i = 0; i < additionalUuidLongCount; i += 2) {
                uuidToNidMap.put(new UUID(additionalUuidLongs[i], additionalUuidLongs[i + 1]), nid);
            }
        }

        if (fieldDataType == SEMANTIC_CHRONOLOGY) {
            int referencedComponentNid = buf.readInt();
            int patternNid = buf.readInt();
            int versionCount = buf.readInt();
            patternElementNidsMap.getIfAbsentPut(patternNid, integer -> new ConcurrentHashSet())
                    .add(nid);
        }
    }

    /**
     *
     * @param data
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    public static <T extends Entity<V>, V extends EntityVersion> T make(byte[] data) {
        // TODO change to use DecoderInput instead of ByteBuf directly.
        // TODO remove the parts where it computes size.
        ByteBuf buf = ByteBuf.wrapForReading(data);
        // bytes starts with number of arrays (int = 4 bytes), then size of first array (int = 4 bytes), then type token
        int numberOfArrays = buf.readInt();
        int sizeOfFirstArray = buf.readInt();
        byte formatVersion = buf.readByte();
        return make(buf, formatVersion);
    }

    /**
     *
     * @param readBuf
     * @param entityFormatVersion
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    public static <T extends Entity<V>, V extends EntityVersion> T make(ByteBuf readBuf, byte entityFormatVersion) {
        FieldDataType fieldDataType = FieldDataType.fromToken(readBuf.readByte());
        return make(readBuf, entityFormatVersion, fieldDataType);
    }

    /**
     *
     * @param readBuf
     * @param entityFormatVersion
     * @param fieldDataType
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    public static <T extends Entity<V>, V extends EntityVersion> T make(ByteBuf readBuf, byte entityFormatVersion, FieldDataType fieldDataType) {

        if (entityFormatVersion != ENTITY_FORMAT_VERSION) {
            throw new IllegalStateException("Unsupported entity format version: " + entityFormatVersion);
        }
        int nid = readBuf.readInt();
        long mostSignificantBits = readBuf.readLong();
        long leastSignificantBits = readBuf.readLong();

        int additionalUuidLongCount = readBuf.readByte();
        long[] additionalUuidLongs = null;

        if (additionalUuidLongCount > 0) {
            additionalUuidLongs = new long[additionalUuidLongCount];
            for (int i = 0; i < additionalUuidLongs.length; i++) {
                additionalUuidLongs[i] = readBuf.readLong();
            }
        }
        int versionCount = -1;
        return switch (fieldDataType) {
            case CONCEPT_CHRONOLOGY -> {
                versionCount = readBuf.readInt();
                RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();
                ConceptRecord conceptRecord = new ConceptRecord(mostSignificantBits, leastSignificantBits,
                        additionalUuidLongs, nid, versions);
                for (int i = 0; i < versionCount; i++) {
                    ConceptVersionRecord version = (ConceptVersionRecord) makeVersion(readBuf, entityFormatVersion, conceptRecord);
                    if (!PrimitiveData.get().isCanceledStampNid(version.stampNid())) {
                        versions.add(version);
                    }
                }
                yield (T) conceptRecord;
            }

            case SEMANTIC_CHRONOLOGY -> {
                int referencedComponentNid = readBuf.readInt();
                int patternNid = readBuf.readInt();
                versionCount = readBuf.readInt();
                RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();
                SemanticRecord semanticRecord = new SemanticRecord(mostSignificantBits, leastSignificantBits,
                        additionalUuidLongs, nid, patternNid, referencedComponentNid,
                        versions);
                for (int i = 0; i < versionCount; i++) {
                    SemanticVersionRecord version = (SemanticVersionRecord) makeVersion(readBuf, entityFormatVersion, semanticRecord);
                    if (!PrimitiveData.get().isCanceledStampNid(version.stampNid())) {
                        versions.add(version);
                    }
                }
                versions.build();
                yield (T) semanticRecord;
            }

            case PATTERN_CHRONOLOGY -> {
                // no additional fieldValues for pattern.
                versionCount = readBuf.readInt();
                RecordListBuilder<PatternVersionRecord> versions = RecordListBuilder.make();
                PatternRecord patternRecord = new PatternRecord(mostSignificantBits, leastSignificantBits,
                        additionalUuidLongs, nid, versions);
                for (int i = 0; i < versionCount; i++) {
                    PatternVersionRecord version = (PatternVersionRecord) makeVersion(readBuf, entityFormatVersion, patternRecord);
                    if (!PrimitiveData.get().isCanceledStampNid(version.stampNid())) {
                        versions.add(version);
                    }
                }
                versions.build();
                yield (T) patternRecord;
            }

            case STAMP -> {
                // no additional fieldValues for stamp
                versionCount = readBuf.readInt();
                RecordListBuilder<StampVersionRecord> versions = RecordListBuilder.make();
                StampRecord stampRecord = new StampRecord(mostSignificantBits, leastSignificantBits,
                        additionalUuidLongs, nid, versions);
                for (int i = 0; i < versionCount; i++) {
                    versions.add((StampVersionRecord) makeVersion(readBuf, entityFormatVersion, stampRecord));
                }
                versions.build();
                yield (T) stampRecord;
            }

            default -> throw new IllegalStateException("Unexpected fieldDataType: " + fieldDataType);
        };
    }

    private static EntityVersion makeVersion(ByteBuf readBuf, byte formatVersion, Entity<? extends EntityVersion> entity) {
        // bytes used by this version. Not used by this way of reading the data,
        // but is used for merge functions for concurrent write of versions using CAS...
        int bytesInVersion = readBuf.readInt();
        byte token = readBuf.readByte();
        int stampNid = readBuf.readInt();
        if (entity.versionDataType().token != token) {
            // f88e125b-b054-566f-bd72-a150df58e1d9 = Tinkar base model component pattern
            // It is the description for the membership pattern for "path"
            StringBuilder sb = new StringBuilder("Processing: " + entity.entityToString());
            sb = sb.append(" Wrong token type: ");
            sb.append(FieldDataType.fromToken(token));
            sb.append(" ");
            sb.append(token);
            sb.append(" expecting ");
            sb.append(entity.versionDataType());
            sb.append(" ");
            sb.append(entity.versionDataType().token);
            sb.append(" processing ");
            sb.append(entity.getClass().getSimpleName());
            sb.append(" ");
            sb.append(entity.publicId());
            if (entity.versionDataType().token == 6) {

//                int fieldCount = readBuf.readInt();
//                RecordListBuilder<Object> fields = RecordListBuilder.make();
//                for (int i = 0; i < fieldCount; i++) {
//                    FieldDataType dataType = FieldDataType.fromToken(readBuf.readByte());
//                    fields.add(readDataType(readBuf, dataType, formatVersion));
//                }
//                fields.build();
//
//
//                sb.append(new SemanticVersionRecord(null, stampNid, fields));
            }

            String exceptionMessage = sb.toString();
            System.err.println(exceptionMessage);
            throw new IllegalStateException(exceptionMessage);
        }

        return switch (entity) {
            case ConceptRecord conceptRecord -> new ConceptVersionRecord(conceptRecord, stampNid);
            case SemanticRecord semanticRecord -> {
                int fieldCount = readBuf.readInt();
                RecordListBuilder<Object> fields = RecordListBuilder.make();
                for (int i = 0; i < fieldCount; i++) {
                    FieldDataType dataType = FieldDataType.fromToken(readBuf.readByte());
                    fields.add(readFieldData(readBuf, dataType, formatVersion));
                }
                fields.build();
                yield new SemanticVersionRecord(semanticRecord, stampNid, fields);
            }
            case PatternRecord patternRecord -> {
                int semanticPurposeNid = readBuf.readInt();
                int semanticMeaningNid = readBuf.readInt();
                int fieldCount = readBuf.readInt();
                MutableList<FieldDefinitionRecord> fieldDefinitionForEntities = Lists.mutable.ofInitialCapacity(fieldCount);
                for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
                    fieldDefinitionForEntities.add(new FieldDefinitionRecord(readBuf.readInt(),
                            readBuf.readInt(), readBuf.readInt(), stampNid, patternRecord.nid(), fieldIndex));
                }

                PatternVersionRecord patternVersionRecord = new PatternVersionRecord(patternRecord, stampNid,
                        semanticPurposeNid, semanticMeaningNid, fieldDefinitionForEntities.toImmutable());
                // make field definition list mutable in the record?
                yield patternVersionRecord;
            }
            case StampRecord stampRecord -> {
                int stateNid = readBuf.readInt();
                long time = readBuf.readLong();
                int authorNid = readBuf.readInt();
                int moduleNid = readBuf.readInt();
                int pathNid = readBuf.readInt();
                yield new StampVersionRecord(stampRecord, stateNid, time, authorNid, moduleNid, pathNid);
            }
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    public static Object readFieldData(ByteBuf readBuf, FieldDataType dataType, byte formatVersion) {
        return switch (dataType) {
            case BOOLEAN -> readBuf.readBoolean();
            case FLOAT -> readBuf.readFloat();
            case BYTE_ARRAY -> readBytes(readBuf);
            case INTEGER -> readBuf.readInt();
            case STRING -> new String(readBytes(readBuf), UTF_8);
            case DITREE -> DiTreeEntity.make(readBuf, formatVersion);
            case DIGRAPH -> DiGraphEntity.make(readBuf, formatVersion);
            case CONCEPT -> EntityProxy.Concept.make(readBuf.readInt());
            case SEMANTIC -> EntityProxy.Semantic.make(readBuf.readInt());
            case PATTERN -> EntityProxy.Pattern.make(readBuf.readInt());
            case IDENTIFIED_THING -> EntityProxy.make(readBuf.readInt());
            case INSTANT -> Instant.ofEpochSecond(readBuf.readLong(), readBuf.readInt());
            case PLANAR_POINT -> new PlanarPoint(readBuf.readInt(), readBuf.readInt());
            case SPATIAL_POINT -> new SpatialPoint(readBuf.readInt(), readBuf.readInt(), readBuf.readInt());
            case COMPONENT_ID_LIST -> IntIds.list.of(readIntArray(readBuf));
            case COMPONENT_ID_SET -> IntIds.set.of(readIntArray(readBuf));
            case LONG -> readBuf.readLong();
            case DECIMAL -> new BigDecimal(new String(readBytes(readBuf), UTF_8));
            default -> throw new UnsupportedOperationException("Can't handle field read of type: " + dataType);
        };
    }

    private static byte[] readBytes(ByteBuf readBuf) {
        int length = readBuf.readInt();
        byte[] bytes = new byte[length];
        readBuf.read(bytes);
        return bytes;
    }

    static protected int[] readIntArray(ByteBuf readBuf) {
        int size = readBuf.readInt();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = readBuf.readInt();
        }
        return array;
    }

    public static Object externalToInternalObject(Object externalObject) {
        return switch (externalObject) {
            // no conversion
            case Boolean booleanField -> booleanField;
            case Float floatField -> floatField;
            case byte[] byteField -> byteField;
            case Integer integerField -> integerField;
            case String stringField -> stringField.strip();
            case Instant instantField -> instantField;
            case PlanarPoint planarPointField -> planarPointField;
            case SpatialPoint spatialPointField -> spatialPointField;
            case BigDecimal bigDecimalField -> bigDecimalField;
            case IntIdSet intIdSet -> intIdSet;
            case IntIdList intIdList -> intIdList;
            // conversions
            case Concept conceptField -> EntityProxy.Concept.make(Entity.nid(conceptField));
            case Semantic semanticField -> EntityProxy.Semantic.make(Entity.nid(semanticField));
            case Pattern patternField -> EntityProxy.Pattern.make(Entity.nid(patternField));
            case Component componentField -> EntityProxy.make(Entity.nid(componentField));
            case PublicId publicId -> EntityProxy.make(Entity.nid(publicId));
            case DiTree diTreeField -> DiTreeEntity.make(diTreeField);
            case DiGraph diGraphField -> DiGraphEntity.make(diGraphField);
            case PublicIdSet publicIdSetField -> {
                MutableIntSet idSet = IntSets.mutable.withInitialCapacity(publicIdSetField.size());
                publicIdSetField.forEach(publicId -> {
                    if (publicId == null) {
                        throw new IllegalStateException("PublicId cannot be null");
                    }
                    idSet.add(Entity.nid((PublicId) publicId));
                });
                yield IntIds.set.ofAlreadySorted(idSet.toSortedArray());
            }
            case PublicIdList publicIdListField -> {
                MutableIntList idList = IntLists.mutable.withInitialCapacity(publicIdListField.size());
                publicIdListField.forEach(publicId -> {
                    idList.add(Entity.nid((PublicId) publicId));
                });
                yield IntIds.list.of(idList.toArray());
            }

            default -> throw new IllegalStateException("Unexpected value: " + externalObject);
        };
    }
}
