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
package dev.ikm.tinkar.coordinate.edit;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public enum Activity implements Encodable {

    // @TODO probably should have concepts with descriptions rather than depending on the user strings here...
    VIEWING("Viewing"),
    DEVELOPING("Developing"),
    PROMOTING("Promoting"),
    MODULARIZING("Modularizing");

    private String userString;

    Activity(String userString) {
        this.userString = userString;
    }

    @Decoder
    public static Activity decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return Activity.valueOf(in.readString());
        }
    }


    // Using a static method rather than a constructor eliminates the need for
    // a readResolve method, but allows the implementation to decide how
    // to handle special cases.

    public String toUserString() {
        return this.userString;
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeString(name());
    }
}
