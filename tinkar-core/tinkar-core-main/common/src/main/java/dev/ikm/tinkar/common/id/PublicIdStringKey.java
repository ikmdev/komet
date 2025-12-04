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
package dev.ikm.tinkar.common.id;


import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.util.text.NaturalOrder;

/**
 * Used when developer wants to have a key with a unique public identifier, but want to
 * have a potentially changeable string associated with that key for user comprehension
 * of intended objects associated with the key.
 * <p>
 * https://www.honeybadger.io/blog/uuids-and-ulids/
 * <p>
 * https://www.getuniqueid.com/cuid
 * <p>
 * T is the class this is a key for, to help code comprehension
 */
public class PublicIdStringKey<T> implements PublicIdWithString<PublicIdStringKey> {

    final PublicId publicId;
    String string;

    public PublicIdStringKey(PublicId publicId, String string) {
        this.publicId = publicId;
        this.string = string;
    }

    public static PublicIdStringKey make(String string) {
        return new PublicIdStringKey(PublicIds.newRandom(), string);
    }

    @Decoder
    public static PublicIdStringKey decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return new PublicIdStringKey(PublicIds.of(in.readUuidArray()), in.readString());
        }
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeUuidArray(this.publicId.asUuidArray());
        out.writeString(this.string);
    }

    @Override
    public int compareTo(PublicIdStringKey o) {
        int comparison = NaturalOrder.compareStrings(this.string, o.getString());
        if (comparison != 0) {
            return comparison;
        }
        return publicId.compareTo(o.getPublicId());
    }

    public String getString() {
        return string;
    }

    public PublicId getPublicId() {
        return publicId;
    }

    @Override
    public int hashCode() {
        return publicId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicIdStringKey that = (PublicIdStringKey) o;
        return publicId.equals(that.getPublicId());
    }

    @Override
    public String toString() {
        return string;
    }

    public void updateString(String string) {
        this.string = string;
    }


}
