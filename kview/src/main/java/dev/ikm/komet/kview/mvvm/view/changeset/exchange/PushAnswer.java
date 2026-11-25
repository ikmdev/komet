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

import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.io.IOException;

/**
 * What a remote answered a push. JGit reports a refused branch in the push's result and throws nothing, so a push
 * that returned may have sent nothing.
 */
public final class PushAnswer {

    private static final String BRANCHES = "refs/heads/";

    private PushAnswer() {
    }

    /**
     * Requires that the remote took every branch pushed to it.
     *
     * @param results what the push returned
     * @throws IOException if the remote refused a branch, with the branch, the reason and what the remote said of it
     */
    public static void requireAccepted(Iterable<PushResult> results) throws IOException {
        for (PushResult result : results) {
            for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                if (update.getStatus() != RemoteRefUpdate.Status.OK && update.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE) {
                    String said = result.getMessages().strip().replaceAll("\\s+", " ");
                    throw new IOException("The remote refused %s (%s)%s".formatted(branch(update), reason(update),
                            said.isEmpty() ? "." : ": " + said));
                }
            }
        }
    }

    private static String branch(RemoteRefUpdate update) {
        String name = update.getRemoteName();
        return name.startsWith(BRANCHES) ? name.substring(BRANCHES.length()) : name;
    }

    private static String reason(RemoteRefUpdate update) {
        if (update.getStatus() == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD
                || update.getStatus() == RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED) {
            return "it has commits not pulled yet";
        }
        return update.getMessage() == null || update.getMessage().isBlank() ? update.getStatus().toString() : update.getMessage();
    }
}
