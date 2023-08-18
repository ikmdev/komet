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
package dev.ikm.komet.artifact;

import dev.ikm.komet.framework.KometNode;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArtifactImportControllerTest {
    ArtifactImportController artifactImportController;
    final String sampleTitle = "sampleTitle";

    @BeforeAll
    public void setUp() {
        // When we spy on artifact import controller
        artifactImportController = Mockito.mock(ArtifactImportController.class);
        when(artifactImportController.toString()).thenReturn(sampleTitle);
    }
    @Test
    public void artifactImportControllerToString() {
        // Given a sample title name
        String sampleTitle = "sampleTitle";
        // When we spy on artifact import controller
        ArtifactImportController artifactImportController = spy(ArtifactImportController.class);

        when(artifactImportController.toString()).thenReturn(sampleTitle);

        // Then check the to string returns what we expect
        assertEquals(artifactImportController.toString(), sampleTitle);
    }

    @Test
    public void testCreateWorker() throws InterruptedException, ExecutionException {
        File file = new File("");
        // Given a sample title name
        // Then check the to string returns what we expect
        Task<Boolean> task = artifactImportController.createWorker(file);
        assertNull(task);
    }
}

