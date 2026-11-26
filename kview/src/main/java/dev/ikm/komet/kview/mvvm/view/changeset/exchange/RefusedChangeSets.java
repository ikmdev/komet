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

import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.importer.registry.ChangeSetLoad;

import java.nio.file.Path;
import java.util.List;

/**
 * What a Pull or a Sync tells a person of the change sets the knowledge base refused: the files, at the end of its
 * last message, and why each was refused, in an advisory in Warnings and Errors. A refusal does not fail the
 * operation: what did load is in the knowledge base, a Sync still sends this person's change sets, and a refused
 * file stays in its folder (IKE-Network/ike-issues#1406). An open of the knowledge base gives the same advisory for
 * the files it refuses, at every open that refuses them.
 */
public final class RefusedChangeSets {

    /** How many files the last message names before it counts the rest, so it keeps to the popup's few lines. */
    private static final int NAMED = 2;

    private RefusedChangeSets() {
    }

    /**
     * Names the refused files for the end of an operation's last message: {@code 1 change set refused: a ike-cs.zip.},
     * {@code 2 change sets refused: a ike-cs.zip and b ike-cs.zip.}, or, past two, the first two and how many more.
     *
     * @param refused the refused files; not empty
     * @return the sentence, ending with a full stop
     */
    static String sentence(List<ChangeSetLoad.Result> refused) {
        int count = refused.size();
        List<String> named = refused.stream().limit(NAMED).map(result -> name(result.file())).toList();
        StringBuilder sentence = new StringBuilder().append(count)
                .append(count == 1 ? " change set refused: " : " change sets refused: ");
        if (count <= NAMED) {
            sentence.append(String.join(" and ", named));
        } else {
            sentence.append(String.join(", ", named)).append(" and ").append(count - NAMED).append(" more");
        }
        return sentence.append('.').toString();
    }

    /**
     * Records in Warnings and Errors, as an advisory, which files were refused and why. Nothing is recorded when
     * none was.
     *
     * @param title     what was refused, in a few words
     * @param which     which change sets they are, after "change set(s)": {@code brought by this Pull}, or
     *                  {@code found when it opened}
     * @param storeRoot the knowledge base's root, to say whether the files are tried again when it next opens
     * @param refused   the refused files
     */
    public static void advise(String title, String which, Path storeRoot, List<ChangeSetLoad.Result> refused) {
        if (refused.isEmpty()) {
            return;
        }
        boolean one = refused.size() == 1;
        StringBuilder description = new StringBuilder("The knowledge base refused ")
                .append(one ? "1 change set " : refused.size() + " change sets ").append(which)
                .append(one ? ": what it contains" : ": what they contain").append(" is not in the knowledge base.");
        for (ChangeSetLoad.Result result : refused) {
            description.append("\n- ").append(name(result.file())).append(": ")
                    .append(result.reason().orElse("no reason was given"));
        }
        if (triedAgainAtOpen(storeRoot, refused)) {
            description.append(one ? "\nIt stays in its folder and is tried again when the knowledge base next opens."
                    : "\nThey stay in their folders and are tried again when the knowledge base next opens.");
        }
        AlertStreams.getRoot().dispatch(AlertObject.makeAdvisory(title, description.toString()));
    }

    /** Whether each file is in a folder a knowledge base loads when it opens. */
    private static boolean triedAgainAtOpen(Path storeRoot, List<ChangeSetLoad.Result> refused) {
        List<Path> folders = ChangeSetLoad.folders(storeRoot).stream().map(RefusedChangeSets::normal).toList();
        return refused.stream().allMatch(result -> folders.contains(normal(result.file()).getParent()));
    }

    private static Path normal(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static String name(Path file) {
        return file.getFileName().toString();
    }
}
