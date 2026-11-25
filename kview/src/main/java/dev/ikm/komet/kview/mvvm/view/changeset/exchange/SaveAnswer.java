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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * What a save of the change sets came to. One that is not written is not in the folder the exchange sends.
 */
public final class SaveAnswer {

    private SaveAnswer() {
    }

    /**
     * Waits for a save and requires that it was done. The wait is not interrupted: what a cancelled exchange was
     * saving is saved.
     *
     * @param save the save
     * @throws IOException if what was to be saved is not, with the reason
     */
    public static void requireSaved(CompletableFuture<Void> save) throws IOException {
        try {
            save.join();
        } catch (CompletionException failed) {
            Throwable reason = failed;
            while (reason.getCause() != null) {
                reason = reason.getCause();
            }
            throw new IOException("The change set could not be saved: " + text(reason), failed.getCause());
        }
    }

    /** A reason in words. A file is given by its name: the popup has no room for where it is, and the log says. */
    private static String text(Throwable reason) {
        if (reason.getMessage() == null) {
            return reason.getClass().getSimpleName();
        }
        return reason instanceof FileNotFoundException || reason instanceof FileSystemException
                ? reason.getMessage().replaceFirst("^.*[/\\\\]", "") : reason.getMessage();
    }
}
