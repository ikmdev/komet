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
package dev.ikm.tinkar.common.util.uuid;

//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.common.id.PublicId;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UuidT5Generator.
 *
 * 
 */
public class UuidT5Generator {

    /**
     * The Constant ENCODING.
     */
    public static final String ENCODING = "UTF-8";
    public static final String DIGEST = "SHA-1";
    /**
     * The Constant STAMP_NAMESPACE.
     */
    public static final UUID STAMP_NAMESPACE = UUID.fromString("2801b388-44aa-11eb-b378-0242ac130002");
    /**
     * The Constant PATH_ID_FROM_FS_DESC.
     */
    public static final UUID PATH_ID_FROM_FS_DESC = UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66");
    /**
     * The Constant REL_GROUP_NAMESPACE.
     */
    public static final UUID REL_GROUP_NAMESPACE = UUID.fromString("8972fef0-ad53-11df-94e2-0800200c9a66");
    /**
     * The Constant USER_FULLNAME_NAMESPACE.
     */
    public static final UUID USER_FULLNAME_NAMESPACE = UUID.fromString("cad85220-1ed4-11e1-8bc2-0800200c9a66");
    /**
     * The Constant TAXONOMY_COORDINATE_NAMESPACE.
     */
    public static final UUID TAXONOMY_COORDINATE_NAMESPACE = UUID.fromString("c58dcdb6-185b-11e5-b60b-1697f925ec7b");
    /**
     * The Constant REL_ADAPTOR_NAMESPACE.
     */
    public static final UUID REL_ADAPTOR_NAMESPACE = UUID.fromString("9cb2bf66-1863-11e5-b60b-1697f925ec7");
    /**
     * The Constant AUTHOR_TIME_ID.
     */
    public static final UUID AUTHOR_TIME_ID = UUID.fromString("c6915290-30fc-11e1-b86c-0800200c9a66");
    public static final UUID SINGLE_SEMANTIC_FOR_RC_UUID = UUID.fromString("97c14234-205f-11eb-adc1-0242ac120002");

    /**
     * Utility classes, which are collections of static members, are not meant to be instantiated.
     * Even abstract utility classes, which can be extended, should not have public constructors.
     */
    private UuidT5Generator() {
    }


    //~--- get methods ---------------------------------------------------------

    public static UUID singleSemanticUuid(PublicId patternId, PublicId referencedComponentId) {
        return singleSemanticUuid(patternId.asUuidArray(), referencedComponentId.asUuidArray());
    }

    /**
     * Generate a UUID for a semantic set that has one semantic per referenced component.
     * Sorts the pattern uuids and the referenced component uuids (separately) to ensure that order of
     * UUID presentation does not matter.
     *
     * @param patternUuids
     * @param referencedComponentIds
     * @return the generated uuid
     */
    public static UUID singleSemanticUuid(UUID[] patternUuids, UUID[] referencedComponentIds) {
        Arrays.sort(patternUuids);
        Arrays.sort(referencedComponentIds);
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(patternUuids));
        builder.append(Arrays.toString(referencedComponentIds));
        return get(SINGLE_SEMANTIC_FOR_RC_UUID, builder.toString());
    }

    /**
     * Generates a reproducible UUID from a namespace and a name.
     *
     * @param namespace the namespace
     * @param name      the name
     * @return the generated uuid
     */
    public static UUID get(UUID namespace, String name) {
        return getUuidWithEncoding(namespace, name, ENCODING);
    }

    /**
     * Generates a reproducible UUID from a namespace and a name in a given encoding.
     *
     * @param namespace
     * @param name
     * @param encoding
     * @return the generated uuid
     */
    public static UUID getUuidWithEncoding(UUID namespace, String name, String encoding) {
        try {
            final MessageDigest sha1Algorithm = getDigest(DIGEST);

            // Generate the digest.
            sha1Algorithm.reset();

            if (namespace != null) {
                sha1Algorithm.update(UuidUtil.getRawBytes(namespace));
            }

            sha1Algorithm.update(name.getBytes(encoding));

            final byte[] sha1digest = sha1Algorithm.digest();

            sha1digest[6] &= 0x0f;  /* clear version */
            sha1digest[6] |= 0x50;  /* set to version 5 */
            sha1digest[8] &= 0x3f;  /* clear variant */
            sha1digest[8] |= 0x80;  /* set to IETF variant */

            long msb = 0;
            long lsb = 0;

            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (sha1digest[i] & 0xff);
            }

            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (sha1digest[i] & 0xff);
            }

            return new UUID(msb, lsb);
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    public static MessageDigest getDigest(String instance) {
        try {
            return MessageDigest.getInstance(instance);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static UUID fromPublicIds(PublicId id1, PublicId id2) {
        StringBuilder builder = new StringBuilder();
        addPublicId(builder, id1);
        addPublicId(builder, id2);
        return UuidT5Generator.get(builder.toString());
    }

    private static void addPublicId(StringBuilder builder, PublicId publicId) {
        UUID[] publicUuids = publicId.asUuidArray();
        Arrays.sort(publicUuids);
        for (UUID uuid : publicUuids) {
            builder.append(uuid.toString());
        }
    }

    /**
     * Gets a generated UUID from a name with a null namespace.
     *
     * @param name the name
     * @return the uuid
     */
    public static UUID get(String name) {
        return get(null, name);
    }

    public static UUID fromPublicIds(UUID namespace, PublicId... publicIds) {
        StringBuilder builder = new StringBuilder();
        for (PublicId publicId : publicIds) {
            addPublicId(builder, publicId);
        }
        return UuidT5Generator.get(namespace, builder.toString());
    }

    public static UUID forTransaction(UUID transactionId, PublicId stateId, long time, PublicId authorId, PublicId moduleId, PublicId pathId) {
        StringBuilder builder = new StringBuilder();
        addPublicId(builder, stateId);
        addPublicId(builder, authorId);
        addPublicId(builder, moduleId);
        addPublicId(builder, pathId);
        builder.append(time);

        return UuidT5Generator.get(transactionId, builder.toString());
    }

    /**
     * Same as {@link #get(UUID, String)} but with an optional consumer, which will get a call with the fed in name and resulting UUID.
     *
     * @param namespace
     * @param name
     * @param consumer  optional callback for debug / UUID generation tracking.
     * @return
     */
    public static UUID get(UUID namespace, String name, BiConsumer<String, UUID> consumer) {
        UUID temp = get(namespace, name);
        if (consumer != null) {
            consumer.accept(name, temp);
        }
        return temp;
    }

}
