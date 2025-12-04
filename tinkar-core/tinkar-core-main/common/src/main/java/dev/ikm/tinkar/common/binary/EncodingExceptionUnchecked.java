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

public class EncodingExceptionUnchecked extends RuntimeException {

    public EncodingExceptionUnchecked(String message) {
        super(message);
    }

    public EncodingExceptionUnchecked(Throwable cause) {
        super(cause);
    }

    public static EncodingExceptionUnchecked makeWrongVersionException(int expected, DecoderInput in) {
        return new EncodingExceptionUnchecked("Wrong encoding version. Expected: " + expected + " found: " + in.encodingFormatVersion());
    }

    public static EncodingExceptionUnchecked makeWrongVersionException(int lowerBound, int upperBound, DecoderInput in) {
        return new EncodingExceptionUnchecked("Wrong encoding version. Expected version between [" +
                lowerBound + ", " + upperBound + "] found: " + in.encodingFormatVersion());
    }
}
