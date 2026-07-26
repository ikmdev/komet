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
package dev.ikm.komet.kview.klfields.test;

import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import javafx.scene.image.Image;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Byte-array fields share the image editor but need not hold an image at all
 * (IKE-Network/ike-issues#924): a byte-array field carrying arbitrary bytes — the
 * IKE starter set's ByteArray loud default is UTF-8 text — must render as "no
 * image", never crash the editing window.
 */
class KlFieldHelperImageTest {

    private static final byte[] NOT_AN_IMAGE = "Default ByteArray value".getBytes(StandardCharsets.UTF_8);

    // KlFieldHelper's static initializer resolves nids, so a live (ephemeral,
    // empty) store must be running before the class loads.
    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("klfield-image-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    @DisplayName("Undecodable bytes yield null, not an errored Image the editor trips over")
    void newImageFromUndecodableBytesIsNull() {
        assertNull(KlFieldHelper.newImageFromByteArray(NOT_AN_IMAGE));
    }

    @Test
    @DisplayName("Empty bytes yield null — the editor's established no-image value")
    void newImageFromEmptyBytesIsNull() {
        assertNull(KlFieldHelper.newImageFromByteArray(new byte[0]));
    }

    @Test
    @DisplayName("An errored Image round-trips to the empty byte[] no-image value, not an exception")
    void newByteArrayFromErroredImageIsEmpty() {
        Image errored = new Image(new ByteArrayInputStream(NOT_AN_IMAGE));
        assertTrue(errored.isError(), "precondition: JavaFX answers a non-null errored Image for garbage bytes");
        assertEquals(0, KlFieldHelper.newByteArrayFromImage(errored).length);
    }
}
