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
package dev.ikm.komet.kview.mvvm.viewmodel.test;

import dev.ikm.komet.kview.mvvm.model.GitHubPreferences;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The git configuration of the change set exchange is kept per knowledge base and is there at the next launch; the
 * password is never written to preferences, which a person may push to a remote.
 *
 * <p>The knowledge bases under test are throw-away preferences nodes, removed afterwards, so no knowledge base of
 * whoever runs the build is touched.
 */
class GitHubPreferencesDaoTest {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/change-set-exchange";
    private static final String URL = "https://example.org/team/change-sets.git";
    private static final String PASSWORD = "Not-a-real-token-42";

    private KometPreferences root;
    private KometPreferences knowledgeBase;
    private GitHubPreferencesDao dao;

    @BeforeEach
    void freshKnowledgeBase() {
        root = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        knowledgeBase = root.node("one");
        dao = daoOver(knowledgeBase);
        dao.forgetPassword();
    }

    @AfterEach
    void removeThrowAwayNode() throws BackingStoreException {
        dao.forgetPassword();
        root.removeNode();
    }

    private static GitHubPreferencesDao daoOver(KometPreferences node) {
        return GitHubPreferencesDao.backedBy(() -> node);
    }

    private static GitHubPreferences preferences() {
        return new GitHubPreferences(URL, "author@example.org", "author", PASSWORD.toCharArray());
    }

    @Test
    void aKnowledgeBaseNeverConnectedHasNoConfiguration() {
        assertEquals("", dao.storedUrl());
        assertEquals("", dao.storedEmail());
        assertEquals("", dao.storedUsername());
        assertFalse(dao.validate());
        assertTrue(dao.load().isEmpty());
    }

    @Test
    void theConfigurationIsThereAtTheNextLaunchAndOnlyThePasswordIsAskedForAgain() throws BackingStoreException {
        dao.save(preferences());
        assertTrue(dao.validate());

        dao.forgetPassword();                                // as quitting does
        GitHubPreferencesDao nextLaunch = daoOver(knowledgeBase);
        assertEquals(URL, nextLaunch.storedUrl());
        assertEquals("author@example.org", nextLaunch.storedEmail());
        assertEquals("author", nextLaunch.storedUsername());
        assertFalse(nextLaunch.validate());
        assertTrue(nextLaunch.load().isEmpty());
    }

    @Test
    void thePasswordIsNotWrittenToPreferences() throws BackingStoreException {
        dao.save(preferences());
        assertEquals(Set.of("git-url", "git-email", "git-username"), Set.of(knowledgeBase.keys()));
        assertEquals(0, knowledgeBase.childrenNames().length);
        assertArrayEquals(PASSWORD.toCharArray(), dao.load().orElseThrow().gitPassword());
    }

    @Test
    void everyInstanceInARunningKometHasThePassword() throws BackingStoreException {
        dao.save(preferences());
        assertArrayEquals(PASSWORD.toCharArray(), daoOver(knowledgeBase).load().orElseThrow().gitPassword());
    }

    @Test
    void clearingALoadedPasswordLeavesTheKeptOne() throws BackingStoreException {
        dao.save(preferences());
        Arrays.fill(dao.load().orElseThrow().gitPassword(), '\0');
        assertArrayEquals(PASSWORD.toCharArray(), dao.load().orElseThrow().gitPassword());
    }

    @Test
    void eachKnowledgeBaseHasItsOwnConfiguration() throws BackingStoreException {
        GitHubPreferencesDao other = daoOver(root.node("two"));
        dao.save(preferences());
        assertEquals("", other.storedUrl());

        other.save("https://example.org/other/change-sets.git", "author@example.org", "author", PASSWORD.toCharArray());
        assertEquals(URL, dao.storedUrl());
        assertEquals("https://example.org/other/change-sets.git", other.storedUrl());
    }
}
