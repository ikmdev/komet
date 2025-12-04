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

import dev.ikm.tinkar.component.*;
import dev.ikm.tinkar.schema.StampChronology;
import io.activej.bytebuf.ByteBuf;

public class EntityFactory {

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
        // bytes starts with number of arrays (int = 4 bytes), then size of first array (int = 4 bytes), then type token, -1 since index starts at 0...
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
        switch (fieldDataType) {
            case CONCEPT_CHRONOLOGY:
                return (T) EntityRecordFactory.make(readBuf, entityFormatVersion, fieldDataType);

            case SEMANTIC_CHRONOLOGY:
                return (T) EntityRecordFactory.make(readBuf, entityFormatVersion, fieldDataType);

            case PATTERN_CHRONOLOGY:
                return (T) EntityRecordFactory.make(readBuf, entityFormatVersion, fieldDataType);

            case STAMP:
                return (T) EntityRecordFactory.make(readBuf, entityFormatVersion, fieldDataType);

            default:
                throw new UnsupportedOperationException("Can't handle fieldDataType: " + fieldDataType);

        }
    }

    public static StampEntity makeStamp(byte[] data) {
        return makeStamp(ByteBuf.wrapForReading(data));
    }

    public static StampEntity makeStamp(ByteBuf readBuf) {
        int numberOfArrays = readBuf.readInt();
        int sizeOfFirstArray = readBuf.readInt();
        byte entityFormatVersion = readBuf.readByte();
        FieldDataType fieldDataType = FieldDataType.fromToken(readBuf.readByte());
        switch (fieldDataType) {
            case STAMP:
                //return StampEntity.make(readBuf, entityFormatVersion);

            default:
                throw new UnsupportedOperationException("Can't handle fieldDataType: " + fieldDataType);

        }
    }

    public static StampEntity makeStamp(Stamp stamp) {
        throw new UnsupportedOperationException("Can't makeStamp: " + stamp);
        //return StampEntity.make(stampDTO);
    }
}
