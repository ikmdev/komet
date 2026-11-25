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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange.test;

import dev.ikm.komet.kview.mvvm.view.changeset.exchange.SaveAnswer;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A save that is not done is not taken for one that is. */
class SaveAnswerTest {

    @Test
    void aSaveThatIsDoneIsTaken() {
        assertDoesNotThrow(() -> SaveAnswer.requireSaved(CompletableFuture.completedFuture(null)));
    }

    @Test
    void aSaveThatIsNotGivesItsReason() {
        IOException full = new IOException("No space left on device");
        IOException said = assertThrows(IOException.class, () -> SaveAnswer.requireSaved(
                CompletableFuture.failedFuture(new UncheckedIOException("The change set x could not be written", full))));
        assertEquals("The change set could not be saved: No space left on device", said.getMessage());
    }

    @Test
    void aFileIsGivenByItsName() {
        IOException said = assertThrows(IOException.class, () -> SaveAnswer.requireSaved(CompletableFuture.failedFuture(
                new FileNotFoundException("/a/knowledge base/changeSets/Author ike-cs.zip.spool (Permission denied)"))));
        assertEquals("The change set could not be saved: Author ike-cs.zip.spool (Permission denied)", said.getMessage());
        said = assertThrows(IOException.class, () -> SaveAnswer.requireSaved(CompletableFuture.failedFuture(
                new NoSuchFileException("C:\\kb\\Author ike-cs.zip.spool", null, "it is gone"))));
        assertEquals("The change set could not be saved: Author ike-cs.zip.spool: it is gone", said.getMessage());
    }

    @Test
    void aReasonWithNoMessageIsNamed() {
        IOException said = assertThrows(IOException.class, () -> SaveAnswer.requireSaved(
                CompletableFuture.failedFuture(new IllegalStateException())));
        assertEquals("The change set could not be saved: IllegalStateException", said.getMessage());
    }
}
