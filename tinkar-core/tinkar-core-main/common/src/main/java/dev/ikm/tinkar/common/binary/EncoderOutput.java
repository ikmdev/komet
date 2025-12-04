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
package dev.ikm.tinkar.common.binary;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.bytebuf.ByteBufStrings;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.IntList;

import java.time.Instant;
import java.util.UUID;

public class EncoderOutput {

    private static int defaultCapacity = 10240;

    protected ByteBuf buf;

    public EncoderOutput(ByteBuf buf) {
        this.buf = buf;
    }

    public EncoderOutput(int initialCapacity) {
        this.buf = ByteBufPool.allocate(initialCapacity);
    }

    public EncoderOutput() {
        this.buf = ByteBufPool.allocate(defaultCapacity);
    }

    private void growIfNeeded(int bytesNeeded) {
        // if ByteBuf is instance of ByteBufSlice, then we can't use the writeRemaining to determine
        // allowed bytes. ByteBufSlice is package private, so can only make sure that the ByteBuf is not a subclass
        if (buf.writeRemaining() < bytesNeeded || (buf.getClass() != ByteBuf.class)) {
            int usedBytes = buf.readRemaining();
            int newCapacity = usedBytes + (usedBytes >> 1);
            ByteBuf newBuf = ByteBufPool.allocate(newCapacity);
            newBuf.put(buf);
            buf.recycle();
            buf = newBuf;
        }
    }


    public void writeByteArray(byte[] byteArray) {
        growIfNeeded(byteArray.length + 4);
        buf.writeVarInt(byteArray.length);
        buf.write(byteArray);
    }


    public void writeUuidArray(UUID[] uuidArray) {
        writeLongArray(UuidUtil.asArray(uuidArray));
    }

    public void writeLongArray(long[] longArray) {
        growIfNeeded((longArray.length * 8) + 4);
        buf.writeVarInt(longArray.length);
        for (long value: longArray) {
            buf.writeLong(value);
        }
    }

    public void writeUuid(UUID uuid) {
        growIfNeeded(16);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public void writeString(String string) {
        growIfNeeded(string.length() * 3);
        int tailAtStart = buf.tail();
        buf.writeInt(0); // place for length of bytes for string.
        int byteCount = ByteBufStrings.encodeUtf8(buf.array(), buf.tail(), string);
        buf.tail(tailAtStart);
        buf.writeInt(byteCount);
        buf.moveTail(byteCount);
    }

    public void writeBoolean(boolean v) {
        growIfNeeded(1);
        buf.writeBoolean(v);
    }

    public void writeByte(byte v) {
        growIfNeeded(1);
        buf.writeByte(v);
    }

    public void writeChar(char v) {
        growIfNeeded(3);
        buf.writeChar(v);
    }

    public void writeDouble(double v) {
        growIfNeeded(8);
        buf.writeDouble(v);
    }

    public void writeFloat(float v) {
        growIfNeeded(4);
        buf.writeFloat(v);
    }

    public void writeInt(int v) {
        growIfNeeded(4);
        buf.writeInt(v);
    }

    public void writeLong(long v) {
        growIfNeeded(8);
        buf.writeLong(v);
    }

    public void writeShort(short v) {
        growIfNeeded(2);
        buf.writeShort(v);
    }

    public void writeVarInt(int v) {
        growIfNeeded(5);
        buf.writeVarInt(v);
    }

    public void writeVarLong(long v) {
        growIfNeeded(10);
        buf.writeVarLong(v);
    }

    public void writeNid(int nid) {
        writePublicId(PrimitiveData.publicId(nid));
    }

    public void writePublicId(PublicId publicId) {
        ImmutableList<UUID> uuidList = publicId.asUuidList();
        growIfNeeded(4);
        writeVarInt(uuidList.size());
        uuidList.forEach(this::writeUuid);
    }

    public void writeNidArray(int[] nids) {
        writeNidList(IntLists.immutable.of(nids));
    }

    public void writeNidList(ImmutableIntList nids) {
        growIfNeeded(4);
        buf.writeVarInt(nids.size());
        nids.forEach(this::writeNid);
    }

    public void writeIntIdList(IntIdList intIdList) {
        writeNidArray(intIdList.toArray());
    }

    public void writeInstant(Instant instant) {
        growIfNeeded(12);
        buf.writeLong(instant.getEpochSecond());
        buf.writeInt(instant.getNano());
    }

    public void write(Encodable encodable) {
        this.writeString(encodable.getClass().getName());
        encodable.encode(this);
    }

    public void recycle() {
        buf.recycle();
    }
}
