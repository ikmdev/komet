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

import dev.ikm.komet.kview.mvvm.view.changeset.exchange.SharedFolderSync;
import dev.ikm.tinkar.entity.scope.SharedFolder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two knowledge bases' folders of shared data, synced through a branch of their own in one remote
 * (IKE-Network/ike-issues#1313, step 4 of {@code design-2026-11-22-knowledge-shared-local}).
 */
class SharedFolderSyncTest {

    private static final UUID MODULE = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @TempDir
    Path root;

    private Path ada;
    private Path bob;
    private String remote;

    @BeforeEach
    void makeRemote() throws Exception {
        Path bare = root.resolve("remote.git");
        Git.init().setBare(true).setDirectory(bare.toFile()).setInitialBranch("main").call().close();
        remote = bare.toUri().toString();
        ada = root.resolve("ada/sharedChangeSets");
        bob = root.resolve("bob/sharedChangeSets");
    }

    @Test
    void withNothingSharedNothingIsMade() throws Exception {
        assertFalse(of(ada, "Ada").receive());
        assertFalse(of(ada, "Ada").send());
        assertFalse(Files.exists(ada.resolve(".git")), "no repository until there is something to send or receive");
        assertEquals(List.of(), branches());
    }

    @Test
    void whatOneSharesTheOtherReceivesOnTheirOwnBranchAlone() throws Exception {
        Path shared = changeSet(ada, "a.adoc", MODULE, "1");
        Files.writeString(ada.resolve("a.adoc [" + MODULE + "] x ike-cs.zip.spool"), "a journal being written, never sent");
        assertTrue(of(ada, "Ada").send());
        assertFalse(of(ada, "Ada").send(), "sent once");
        assertEquals(List.of("refs/heads/shared"), branches(), "on a branch of its own, and no other");

        assertTrue(of(bob, "Bob").receive());
        assertEquals(List.of(shared.getFileName().toString()), names(bob));
        assertFalse(of(bob, "Bob").receive(), "nothing new");
        assertFalse(of(bob, "Bob").send(), "and nothing to send back");
    }

    @Test
    void aChangedRemoteIsSentWhatTheEarlierOneHas() throws Exception {
        changeSet(ada, "a.adoc", MODULE, "1");
        of(ada, "Ada").send();
        Path moved = root.resolve("moved.git");
        Git.init().setBare(true).setDirectory(moved.toFile()).setInitialBranch("main").call().close();
        remote = moved.toUri().toString();

        assertFalse(of(ada, "Ada").receive(), "the new remote has nothing yet");
        assertTrue(of(ada, "Ada").send());
        assertEquals(List.of("refs/heads/shared"), branches());
        assertFalse(of(ada, "Ada").send(), "sent once");
    }

    @Test
    void bothAddAtOnceAndBothHaveAll() throws Exception {
        changeSet(ada, "a.adoc", MODULE, "1");
        of(ada, "Ada").send();
        of(bob, "Bob").receive();
        changeSet(ada, "a.adoc", MODULE, "2");
        changeSet(bob, "a.adoc", MODULE, "3");
        of(ada, "Ada").send();
        assertTrue(of(bob, "Bob").receive(), "merged with what Bob had not sent");
        assertTrue(of(bob, "Bob").send());
        assertTrue(of(ada, "Ada").receive());
        assertEquals(names(bob), names(ada));
        assertEquals(3, names(ada).size());
    }

    @Test
    void aDeletionGoesWithItsMarkerAndWhatWasNeverSentStays() throws Exception {
        Path first = changeSet(ada, "a.adoc", MODULE, "1");
        changeSet(ada, "b.adoc", OTHER, "1");
        of(ada, "Ada").send();
        of(bob, "Bob").receive();
        Path own = changeSet(bob, "a.adoc", MODULE, "bobs");

        Files.delete(first);
        SharedFolder.mark(ada, MODULE, "a.adoc", SharedFolder.Act.DELETED, "Ada", Instant.parse("2026-11-23T18:00:00Z"));
        of(ada, "Ada").receive();
        assertTrue(of(ada, "Ada").send());

        assertTrue(of(bob, "Bob").receive());
        assertEquals(1, SharedFolder.removals(bob).size(), "the marker came");
        assertEquals(List.of(own), SharedFolder.changeSets(bob).get(MODULE), "only what Bob never sent is left of it");
        assertEquals(1, SharedFolder.changeSets(bob).get(OTHER).size(), "and the other document stays");
    }

    @Test
    void twoMarkersForOneDocumentAreNoConflict() throws Exception {
        Path first = changeSet(ada, "a.adoc", MODULE, "1");
        of(ada, "Ada").send();
        of(bob, "Bob").receive();
        for (Path folder : List.of(ada, bob)) {
            Files.delete(folder.resolve(first.getFileName()));
            SharedFolder.mark(folder, MODULE, "a.adoc", SharedFolder.Act.DELETED, folder == ada ? "Ada" : "Bob", Instant.now());
        }
        of(ada, "Ada").receive();
        of(ada, "Ada").send();
        assertTrue(of(bob, "Bob").receive());
        of(bob, "Bob").send();
        assertEquals(2, SharedFolder.removals(bob).size());
        assertTrue(SharedFolder.changeSets(bob).isEmpty());
    }

    @Test
    void whatTwoPeopleSharedBeforeTheyEverSyncedIsKept() throws Exception {
        changeSet(ada, "a.adoc", MODULE, "1");
        of(ada, "Ada").send();
        Path bobs = changeSet(bob, "b.adoc", OTHER, "1");
        assertTrue(of(bob, "Bob").receive(), "Bob's folder was no repository yet");
        assertTrue(Files.exists(bobs), "and what Bob had is still there");
        assertEquals(2, names(bob).size());
        assertTrue(of(bob, "Bob").send());
        assertTrue(of(ada, "Ada").receive());
        assertEquals(names(bob), names(ada));
    }

    @Test
    void aPushTheRemoteRefusesSaysToSyncAgainAndLosesNothing() throws Exception {
        changeSet(ada, "a.adoc", MODULE, "1");
        of(ada, "Ada").send();
        of(bob, "Bob").receive();
        changeSet(ada, "a.adoc", MODULE, "2");
        of(ada, "Ada").send();
        Path mine = changeSet(bob, "a.adoc", MODULE, "3");
        java.io.IOException refused = org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class, () -> of(bob, "Bob").send());
        assertTrue(refused.getMessage().contains("Sync again"), refused.getMessage());
        assertTrue(Files.exists(mine));
        of(bob, "Bob").receive();
        assertTrue(of(bob, "Bob").send());
    }

    private SharedFolderSync of(Path folder, String who) {
        return new SharedFolderSync(folder, remote, null, new PersonIdent(who, who.toLowerCase(java.util.Locale.ROOT) + "@example.test"));
    }

    private static Path changeSet(Path folder, String name, UUID module, String which) throws Exception {
        Files.createDirectories(folder);
        return Files.writeString(folder.resolve(name + " [" + module + "] Author " + which + " ike-cs.zip"), which);
    }

    private static List<String> names(Path folder) throws Exception {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString()).filter(name -> name.endsWith("ike-cs.zip")).sorted().toList();
        }
    }

    private List<String> branches() throws Exception {
        return Git.lsRemoteRepository().setRemote(remote).setHeads(true).call().stream().map(Ref::getName).sorted().toList();
    }
}
