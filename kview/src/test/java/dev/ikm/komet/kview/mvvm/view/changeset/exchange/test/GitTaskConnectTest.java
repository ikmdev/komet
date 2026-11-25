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

import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Connecting a knowledge base's change sets to a remote: the remote is the configured one, and a remote that cannot
 * be reached changes nothing.
 */
class GitTaskConnectTest {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/git-task-connect";

    @TempDir
    Path root;

    private final AtomicInteger connections = new AtomicInteger();
    private KometPreferences node;
    private GitHubPreferencesDao dao;
    private Path changeSets;
    private Path one;
    private Path two;

    @BeforeEach
    void makeRemotes() throws Exception {
        node = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        dao = GitHubPreferencesDao.backedBy(() -> node);
        changeSets = Files.createDirectories(root.resolve("kb/changeSets"));
        one = bare("one.git");
        two = bare("two.git");
        configure(one, "author");
    }

    @AfterEach
    void removeThrowAwayNode() throws Exception {
        dao.forgetPassword();
        node.removeNode();
    }

    @Test
    void anEmptyRemoteGetsTheMainBranch() throws Exception {
        assertTrue(connect());
        assertEquals(List.of("Initial commit for changeset exchange"), messages(changeSets));
        assertEquals(head(changeSets), main(one));
        assertEquals(url(one), remote());
        assertEquals(1, connections.get());
    }

    @Test
    void connectingAgainMakesNoCommit() throws Exception {
        connect();
        ObjectId before = head(changeSets);
        assertTrue(connect());
        assertEquals(before, head(changeSets));
    }

    @Test
    void aRemoteWithAMainBranchIsConnectedWithNoCommit() throws Exception {
        connect();
        Path other = Files.createDirectories(root.resolve("other/changeSets"));
        assertTrue(connect(other));
        assertNull(head(other), "a pull brings the remote's commits");
    }

    @Test
    void aRemoteThatCannotBeReachedLeavesTheRepositoryAsItWas() throws Exception {
        connect();
        ObjectId before = head(changeSets);
        String readme = Files.readString(changeSets.resolve("README.md"));
        Files.move(one, root.resolve("gone.git"));

        assertFalse(connect());
        assertEquals(before, head(changeSets), "no commit is made");
        assertEquals(readme, Files.readString(changeSets.resolve("README.md")));
        assertEquals(1, connections.get());
    }

    @Test
    void aRemoteThatCannotBeReachedAtFirstIsNotSetAndCommitsNothing() throws Exception {
        configure(root.resolve("nowhere.git"), "author");
        assertFalse(connect());
        assertNull(head(changeSets));
        assertFalse(Files.exists(changeSets.resolve("README.md")));
        assertNull(remote());

        configure(one, "author");
        assertTrue(connect(), "the address corrected, it connects");
        assertEquals(url(one), remote());
    }

    @Test
    void aChangedUrlIsTheRemoteFromThen() throws Exception {
        connect();
        configure(two, "author");
        assertTrue(connect());
        assertEquals(url(two), remote());
        assertEquals(head(changeSets), main(two), "the empty remote gets the branch as it is");
        assertEquals(1, messages(changeSets).size(), "with no commit made for it");
    }

    @Test
    void aChangedUrlThatCannotBeReachedLeavesTheRemote() throws Exception {
        connect();
        configure(root.resolve("nowhere.git"), "author");
        assertFalse(connect());
        assertEquals(url(one), remote());
    }

    @Test
    void aChangedUserNameIsUsedFromThen() throws Exception {
        connect();
        configure(one, "renamed");
        assertTrue(connect());
        try (Git git = Git.open(changeSets.toFile())) {
            StoredConfig config = git.getRepository().getConfig();
            assertEquals("renamed", config.getString("user", null, "name"));
            assertEquals("renamed@example.org", config.getString("user", null, "email"));
        }
    }

    @Test
    void aReadmeThatIsThereIsKept() throws Exception {
        Files.writeString(changeSets.resolve("README.md"), "Written by a person.");
        assertTrue(connect());
        assertEquals("Written by a person.", Files.readString(changeSets.resolve("README.md")));
        assertNotNull(main(one));
    }

    private Path bare(String name) throws Exception {
        Path folder = root.resolve(name);
        Git.init().setBare(true).setDirectory(folder.toFile()).setInitialBranch("main").call().close();
        return folder;
    }

    private static String url(Path bare) {
        return "file://" + bare;
    }

    private void configure(Path bare, String name) throws Exception {
        dao.save(url(bare), name + "@example.org", name, "Not-a-real-token-42".toCharArray());
    }

    private boolean connect() throws Exception {
        return connect(changeSets);
    }

    private boolean connect(Path folder) throws Exception {
        return new GitTask(GitTask.OperationMode.CONNECT, folder, connections::incrementAndGet, dao).call();
    }

    private String remote() throws Exception {
        try (Git git = Git.open(changeSets.toFile())) {
            return git.getRepository().getConfig().getString("remote", GitTask.REMOTE_NAME, "url");
        }
    }

    private static ObjectId head(Path folder) throws Exception {
        try (Git git = Git.open(folder.toFile())) {
            return git.getRepository().resolve("HEAD");
        }
    }

    private static ObjectId main(Path bare) throws Exception {
        try (Git git = Git.open(bare.toFile())) {
            Ref ref = git.getRepository().findRef("refs/heads/main");
            return ref == null ? null : ref.getObjectId();
        }
    }

    private static List<String> messages(Path folder) throws Exception {
        try (Git git = Git.open(folder.toFile())) {
            List<String> messages = new ArrayList<>();
            for (RevCommit commit : git.log().call()) {
                messages.add(commit.getShortMessage());
            }
            return messages;
        }
    }
}
