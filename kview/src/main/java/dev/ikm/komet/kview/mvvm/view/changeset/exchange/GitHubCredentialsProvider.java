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

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_PASSWORD;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_USERNAME;

public class GitHubCredentialsProvider extends CredentialsProvider {

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
        KometPreferences userPreferences = Preferences.get().getUserPreferences();
        Optional<char[]> optionalPassword = getPassword(userPreferences);
        Optional<String> optionalUser = getUser(userPreferences);

        for (CredentialItem credentialItem : credentialItems) {
            switch (credentialItem) {
                case CredentialItem.Username username ->
                        optionalUser.ifPresentOrElse(username::setValue, () -> username.setValue(urIish.getUser()));
                case CredentialItem.Password password ->
                        optionalPassword.ifPresentOrElse(password::setValue, () -> password.setValue(new char[0]));
                default -> throw new IllegalStateException("Unexpected value: " + credentialItem);
            }
        }

        return optionalUser.isPresent() && optionalPassword.isPresent();

        // Show the GitHub preferences dialog to get the credentials
    }

    Optional<char[]> getPassword(KometPreferences preferences) {
        return preferences.getPassword(GIT_PASSWORD);
    }

    void setPassword(KometPreferences preferences, char[] password) {
        preferences.putPassword(GIT_PASSWORD, password);
    }

    Optional<String> getUser(KometPreferences preferences) {
        return preferences.get(GIT_USERNAME);
    }

    void setUser(KometPreferences preferences, String username) {
        preferences.put(GIT_USERNAME, username);
    }
}
