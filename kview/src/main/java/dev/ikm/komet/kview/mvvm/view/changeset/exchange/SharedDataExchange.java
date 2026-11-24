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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.entity.scope.SharedFolder;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.importer.registry.ChangeSetLoad;
import dev.ikm.tinkar.terms.KernelConcept;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

import static dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent.GLOBAL_REFRESH;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;

/**
 * The exchange of a knowledge base's shared data, the part of a sync that is not knowledge
 * (IKE-Network/ike-issues#1313, step 4 of {@code design-2026-11-22-knowledge-shared-local}): the folder is
 * synced ({@link SharedFolderSync}) with the remote and identity of the knowledge base's change sets, what
 * others deleted or made local is taken out of the open knowledge base, and what they shared is loaded.
 * A failure is reported in Warnings and Errors, and the sync of the knowledge goes on.
 */
public final class SharedDataExchange {

    private static final Logger LOG = LoggerFactory.getLogger(SharedDataExchange.class);

    private final Path changeSetFolder;
    private final CredentialsProvider credentials;

    /**
     * @param changeSetFolder the knowledge base's {@code changeSets} folder, a git repository with a remote
     * @param credentials     the login of its sync; may be {@code null} for none
     */
    public SharedDataExchange(Path changeSetFolder, CredentialsProvider credentials) {
        this.changeSetFolder = changeSetFolder.toAbsolutePath().normalize();
        this.credentials = credentials;
    }

    private Path storeRoot() {
        return changeSetFolder.getParent();
    }

    private SharedFolderSync sync() throws IOException {
        try (Git git = Git.open(changeSetFolder.toFile())) {
            StoredConfig config = git.getRepository().getConfig();
            String remote = config.getString("remote", GitTask.REMOTE_NAME, "url");
            if (remote == null) {
                throw new IOException("the knowledge base has no remote");
            }
            return new SharedFolderSync(SharedFolder.of(storeRoot()), remote, credentials,
                    new PersonIdent(config.getString("user", null, "name"), config.getString("user", null, "email")));
        }
    }

    /**
     * Brings the shared data others synced: what they shared is loaded, and what they deleted or made
     * local is removed here, or kept as local data where something of this person's was never synced.
     *
     * @param sending whether the sync also sends, so shared documents are first brought up to date with their files
     */
    public void receive(boolean sending) {
        try {
            if (sending) {
                ServiceLifecycleManager.get().beforeSync();
            }
            saveChangeSets();                                // what was just read in, and what was said here, in finished files
            boolean changed = sync().receive();
            SharedFolder.Applied applied = SharedFolder.apply(SharedFolder.of(storeRoot()), KernelConcept.USER);
            applied.sentences().forEach(sentence -> {
                LOG.info("Shared data: {}", sentence);
                AlertStreams.getRoot().dispatch(AlertObject.makeAdvisory("Shared documents", sentence));
            });
            ChangeSetLoad.Report report = ChangeSetLoad.loadShared(storeRoot(), ChangeSetLoad.Progress.NONE);
            LOG.info("Shared data: {}\n{}", report.summary(), report.markdown());
            if (changed || !applied.quiet() || !report.loaded().isEmpty()) {
                // The calculators first, so what reads its document again reads it with the data just loaded.
                EvtBusFactory.getDefaultEvtBus().publish(CALCULATOR_CACHE_TOPIC, new RefreshCalculatorCacheEvent(this, GLOBAL_REFRESH));
                ServiceLifecycleManager.get().afterSync();
            }
        } catch (GitAPIException | IOException | RuntimeException e) {
            LOG.error("The shared documents were not received", e);
            AlertStreams.getRoot().dispatch(AlertObject.makeError("Shared documents", "They were not received: " + e.getMessage(), e));
        }
    }

    /** Closes the session's change sets, so the folder holds everything written before now. */
    private static void saveChangeSets() {
        for (SaveState state : PrimitiveData.getStatesToSave()) {
            if (state instanceof ChangeSetWriterService) {
                state.save().join();
            }
        }
    }

    /** Sends the shared data of this knowledge base. A failure is reported; the next sync sends it. */
    public void send() {
        try {
            saveChangeSets();
            LOG.info("Shared data: {}", sync().send() ? "sent" : "nothing to send");
        } catch (GitAPIException | IOException | RuntimeException e) {
            LOG.error("The shared documents were not sent", e);
            AlertStreams.getRoot().dispatch(AlertObject.makeError("Shared documents", "They were not sent: " + e.getMessage(), e));
        }
    }
}
