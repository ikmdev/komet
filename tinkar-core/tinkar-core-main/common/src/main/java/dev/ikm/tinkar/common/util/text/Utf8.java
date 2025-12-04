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
package dev.ikm.tinkar.common.util.text;

import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufStrings;

import java.nio.charset.StandardCharsets;

public class Utf8 {

    public static void encode(ByteBuf buf, String string) {
        int headAtStart = buf.head();
        buf.writeInt(0); // place for length of bytes for string.
        int byteCount = ByteBufStrings.encodeUtf8(buf.array(), buf.tail(), string);
        buf.head(headAtStart);
        buf.writeInt(byteCount);
        buf.moveHead(byteCount);
    }

    public static String decode(ByteBuf buf) {
        int byteCount = buf.readInt();
        String decoded = new String(buf.array(), buf.head(), byteCount, StandardCharsets.UTF_8);
        buf.moveHead(byteCount);
        return decoded;
    }

}
