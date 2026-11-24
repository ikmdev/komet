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

import dev.ikm.tinkar.entity.scope.SharedFolder;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Syncs a knowledge base's folder of shared data, on a branch of its own in the knowledge base's own
 * remote (IKE-Network/ike-issues#1313; the first question before step 4 of
 * {@code design-2026-11-22-knowledge-shared-local}, settled by KEC on 2026-11-23). Everyone who syncs the
 * knowledge base gets it, with the same login, and a release, which reads the knowledge base's
 * {@code changeSets} folder, cannot see it.
 *
 * <p>The folder becomes a git repository when there is something to send, or something to receive. Its
 * change sets are written once and never changed, and a marker has a name of its own
 * ({@link SharedFolder}), so two people's work merges without a conflict. Should one arise, the folder
 * is put back as it was and nothing is sent.
 */
public final class SharedFolderSync {

    private static final Logger LOG = LoggerFactory.getLogger(SharedFolderSync.class);

    /** The branch of the knowledge base's remote that holds its shared data. */
    public static final String BRANCH = "shared";

    private static final String REMOTE = "origin";
    private static final String LOCAL_REF = "refs/heads/" + BRANCH;
    private static final String REMOTE_REF = "refs/remotes/" + REMOTE + "/" + BRANCH;

    private final Path folder;
    private final String remote;
    private final CredentialsProvider credentials;
    private final PersonIdent person;

    /**
     * Makes the sync of one knowledge base's shared folder.
     *
     * @param folder      the folder of shared data, which may not exist yet
     * @param remote      the knowledge base's remote
     * @param credentials the login the knowledge base's own sync uses; may be {@code null} for none
     * @param person      who commits
     */
    public SharedFolderSync(Path folder, String remote, CredentialsProvider credentials, PersonIdent person) {
        this.folder = folder;
        this.remote = remote;
        this.credentials = credentials;
        this.person = person;
    }

    /**
     * Brings what others shared, deleted or made local: commits what this knowledge base did, then
     * merges the branch.
     *
     * @return whether the folder changed
     * @throws GitAPIException if git fails, or the two cannot be merged
     * @throws IOException     if the folder cannot be read or written
     */
    public boolean receive() throws GitAPIException, IOException {
        boolean remoteHas = remoteHasBranch();
        if (!remoteHas && !Files.isDirectory(folder.resolve(".git"))) {
            return false;                                    // nothing to receive, and nothing sent yet
        }
        try (Git git = open()) {
            commit(git);
            if (!remoteHas) {
                return false;
            }
            git.fetch().setRemote(REMOTE).setRefSpecs(new RefSpec("+" + LOCAL_REF + ":" + REMOTE_REF))
                    .setCredentialsProvider(credentials).call();
            ObjectId before = git.getRepository().resolve(LOCAL_REF);
            ObjectId theirs = git.getRepository().resolve(REMOTE_REF);
            if (theirs == null || theirs.equals(before)) {
                return false;
            }
            if (before == null) {
                // The folder's first commits are theirs: point the branch at them, then write the files.
                git.branchCreate().setName(BRANCH).setStartPoint(REMOTE_REF).call();
                git.reset().setMode(ResetCommand.ResetType.HARD).setRef(LOCAL_REF).call();
                return true;
            }
            MergeResult merged = git.merge().include(theirs).setCommit(false).call();
            if (!merged.getMergeStatus().isSuccessful()) {
                git.reset().setMode(ResetCommand.ResetType.HARD).setRef(before.getName()).call();
                throw new IOException("The shared data could not be merged (" + merged.getMergeStatus() + "); nothing was changed.");
            }
            if (merged.getMergeStatus() == MergeResult.MergeStatus.MERGED_NOT_COMMITTED) {
                // Committed here, as the person and unsigned, whatever git is configured to sign on this computer.
                git.commit().setMessage("Merge the shared data of others").setAuthor(person).setCommitter(person).setSign(false).call();
            }
            return !before.equals(git.getRepository().resolve(LOCAL_REF));
        }
    }

    /**
     * Sends what this knowledge base shared, deleted or made local.
     *
     * @return whether anything was sent
     * @throws GitAPIException if git fails
     * @throws IOException     if the folder cannot be read, or the remote refuses
     */
    public boolean send() throws GitAPIException, IOException {
        if (!Files.isDirectory(folder.resolve(".git")) && !somethingToSend()) {
            return false;
        }
        try (Git git = open()) {
            commit(git);
            ObjectId mine = git.getRepository().resolve(LOCAL_REF);
            if (mine == null || mine.equals(git.getRepository().resolve(REMOTE_REF))) {
                return false;
            }
            for (PushResult result : git.push().setRemote(REMOTE).setRefSpecs(new RefSpec(LOCAL_REF + ":" + LOCAL_REF))
                    .setCredentialsProvider(credentials).call()) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    if (update.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE) {
                        return false;
                    }
                    if (update.getStatus() != RemoteRefUpdate.Status.OK) {
                        throw new IOException("The remote refused the shared data: " + update.getStatus()
                                + (update.getMessage() == null ? "" : ", " + update.getMessage()) + ". Sync again to send it.");
                    }
                }
            }
            return true;
        }
    }

    /** Whether the remote has a branch of shared data. */
    private boolean remoteHasBranch() throws GitAPIException {
        Collection<Ref> refs = Git.lsRemoteRepository().setRemote(remote).setHeads(true).setCredentialsProvider(credentials).call();
        return refs.stream().anyMatch(ref -> ref.getName().equals(LOCAL_REF));
    }

    private boolean somethingToSend() throws IOException {
        return !toAdd().isEmpty();
    }

    /** The folder's repository, made when it is not one yet, with the knowledge base's remote. */
    private Git open() throws GitAPIException, IOException {
        Files.createDirectories(folder);
        Git git = Files.isDirectory(folder.resolve(".git")) ? Git.open(folder.toFile())
                : Git.init().setDirectory(folder.toFile()).setInitialBranch(BRANCH).call();
        StoredConfig config = git.getRepository().getConfig();
        try {
            if (config.getString("remote", REMOTE, "url") == null) {
                git.remoteAdd().setName(REMOTE).setUri(new URIish(remote)).call();
            } else if (!remote.equals(config.getString("remote", REMOTE, "url"))) {
                git.remoteSetUrl().setRemoteName(REMOTE).setRemoteUri(new URIish(remote)).call();
            }
        } catch (URISyntaxException e) {
            git.close();
            throw new IOException("Not an address git can use: " + remote, e);
        }
        return git;
    }

    /** Commits the folder as it stands: its finished change sets and its markers, and what was removed from it. */
    private void commit(Git git) throws GitAPIException, IOException {
        AddCommand add = git.add();
        List<String> files = toAdd();
        if (!files.isEmpty()) {
            files.forEach(add::addFilepattern);
            add.call();
        }
        git.add().setUpdate(true).addFilepattern(".").call();        // what is gone, and nothing new
        if (git.status().call().hasUncommittedChanges()) {
            git.commit().setMessage("Shared data of " + person.getName()).setAuthor(person).setCommitter(person).setSign(false).call();
            LOG.info("Committed the shared data in {}", folder);
        }
    }

    private List<String> toAdd() throws IOException {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(SharedFolder.CHANGE_SET_SUFFIX) || name.endsWith(SharedFolder.MARKER_SUFFIX))
                    .sorted().toList();
        }
    }
}
