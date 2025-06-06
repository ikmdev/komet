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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials;

import dev.ikm.komet.kview.mvvm.model.GitHubPreferences;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;

/**
 * A non-interactive credentials provider for GitHub that retrieves authentication
 * information from stored preferences.
 * <p>
 * This implementation extends JGit's {@link CredentialsProvider} and provides GitHub
 * credentials by loading them from the application's preference store via
 * {@link GitHubPreferencesDao}. It supports username and password authentication for
 * GitHub repositories.
 *
 * @see CredentialsProvider
 * @see GitHubPreferences
 * @see GitHubPreferencesDao
 */
public class GitHubCredentialsProvider extends CredentialsProvider {

    private final GitHubPreferencesDao gitHubPreferencesDao = new GitHubPreferencesDao();

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        final MutableList<CredentialItem> unprocessedItems = Lists.mutable.empty();

        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.InformationalMessage) {
                continue;
            }
            if (item instanceof CredentialItem.Username) {
                continue;
            }
            if (item instanceof CredentialItem.Password) {
                continue;
            }
            if (item instanceof CredentialItem.StringType) {
                if (item.getPromptText().equals("Password: ")) {
                    continue;
                }
            }

            unprocessedItems.add(item);
        }

        return unprocessedItems.isEmpty();
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final MutableList<CredentialItem> unprocessedItems = Lists.mutable.empty();
        final Optional<GitHubPreferences> gitHubPrefsOpt = gitHubPreferencesDao.load();

        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.Username username) {
                gitHubPrefsOpt.ifPresent(gitHubPreferences ->
                        username.setValue(gitHubPreferences.gitUsername()));
            } else if (item instanceof CredentialItem.Password password) {
                gitHubPrefsOpt.ifPresent(gitHubPreferences ->
                        password.setValue(gitHubPreferences.gitPassword()));
            } else {
                unprocessedItems.add(item);
            }
        }

        if (unprocessedItems.isEmpty()) {
            return true;
        }

        throw new UnsupportedCredentialItem(uri, unprocessedItems.size() + " credential items not supported");
    }
}
