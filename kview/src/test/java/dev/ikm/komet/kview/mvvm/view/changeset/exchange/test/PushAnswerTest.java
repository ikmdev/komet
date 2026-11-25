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

import dev.ikm.komet.kview.mvvm.view.changeset.exchange.PushAnswer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A push the remote refused is told from one it took: JGit itself throws for neither. */
class PushAnswerTest {

    @TempDir
    Path root;

    private Path remote;
    private Git ada;
    private Git bob;

    @BeforeEach
    void makeRemoteAndTwoClones() throws Exception {
        remote = root.resolve("remote.git");
        Git.init().setBare(true).setDirectory(remote.toFile()).setInitialBranch("main").call().close();
        ada = repository("ada");
        bob = repository("bob");
    }

    @AfterEach
    void close() {
        ada.close();
        bob.close();
    }

    @Test
    void aPushTheRemoteTookIsAccepted() throws Exception {
        commit(ada, "one");
        assertDoesNotThrow(() -> PushAnswer.requireAccepted(push(ada)));
        assertDoesNotThrow(() -> PushAnswer.requireAccepted(push(ada)), "nothing new is no refusal");
    }

    @Test
    void aPushTheRemoteRefusedIsNot() throws Exception {
        commit(ada, "one");
        push(ada);
        commit(bob, "other");
        List<PushResult> results = push(bob);
        assertEquals(RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD,
                results.getFirst().getRemoteUpdate("refs/heads/main").getStatus(), "JGit returned, and nothing was sent");

        IOException refused = assertThrows(IOException.class, () -> PushAnswer.requireAccepted(results));
        assertEquals("The remote refused main (it has commits not pulled yet).", refused.getMessage());
    }

    private Git repository(String name) throws Exception {
        Git git = Git.init().setDirectory(Files.createDirectories(root.resolve(name)).toFile()).setInitialBranch("main").call();
        git.remoteAdd().setName("origin").setUri(new URIish("file://" + remote)).call();
        return git;
    }

    private static void commit(Git git, String message) throws Exception {
        git.commit().setMessage(message).setAllowEmpty(true).setSign(false)
                .setAuthor(message, message + "@example.test").setCommitter(message, message + "@example.test").call();
    }

    private static List<PushResult> push(Git git) throws Exception {
        List<PushResult> results = new ArrayList<>();
        git.push().setRemote("origin").setPushAll().call().forEach(results::add);
        return results;
    }
}
