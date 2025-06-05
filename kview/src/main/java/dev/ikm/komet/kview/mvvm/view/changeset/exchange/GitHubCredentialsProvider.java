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

import dev.ikm.komet.kview.mvvm.model.GitHubPreferences;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
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
    public boolean supports(CredentialItem... credentialItems) {
        return true;
    }

    @Override
    public boolean get(URIish urIish, CredentialItem... credentialItems) throws UnsupportedCredentialItem {
        Optional<GitHubPreferences> gitHubPrefsOpt = gitHubPreferencesDao.load();
        if (gitHubPrefsOpt.isPresent()) {
            final GitHubPreferences gitHubPreferences = gitHubPrefsOpt.get();
            final String gitUsername = gitHubPreferences.gitUsername();
            final char[] gitPassword = gitHubPreferences.gitPassword();

            for (CredentialItem credentialItem : credentialItems) {
                switch (credentialItem) {
                    case CredentialItem.Username username -> username.setValue(gitUsername);
                    case CredentialItem.Password password -> password.setValue(gitPassword);
                    default -> throw new IllegalStateException("Unexpected value: " + credentialItem);
                }
            }
            return true;
        }

        return false;
    }
}
