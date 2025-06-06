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
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;

/**
 * A non-interactive credentials provider for GitHub that retrieves authentication
 * information from stored preferences.
 * <p>
 * Retrieves GitHub credentials from the application's preference store and provides them
 * to JGit for repository operations. Supports username/password authentication only.
 *
 * @see CredentialsProvider
 * @see CredentialItem
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
        return Lists.immutable.of(items).allSatisfy(this::isItemSupported);
    }

    /**
     * Checks if this provider supports a single credential item.
     *
     * @param item the credential item to verify
     * @return {@code true} if the item is supported
     */
    private boolean isItemSupported(CredentialItem item) {
        return item instanceof CredentialItem.InformationalMessage ||
                item instanceof CredentialItem.Username ||
                item instanceof CredentialItem.Password ||
                isPasswordStringType(item);
    }

    /**
     * Determines if the item is a password StringType credential.
     *
     * @param item the credential item to check
     * @return {@code true} if the item is a password StringType
     */
    private boolean isPasswordStringType(CredentialItem item) {
        return item instanceof CredentialItem.StringType &&
                "Password: ".equals(item.getPromptText());
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final Optional<GitHubPreferences> gitHubPrefsOpt = gitHubPreferencesDao.load();

        // Handle missing preferences
        if (gitHubPrefsOpt.isEmpty()) {
            return handleMissingPreferences(uri, items);
        }

        // Populate credential values from preferences
        final GitHubPreferences preferences = gitHubPrefsOpt.get();
        Lists.immutable.of(items)
                .select(this::isUsernamePasswordItem)
                .forEach(item -> setCredentialValue(item, preferences));

        return checkForUnsupportedItems(uri, items);
    }

    /**
     * Checks if the item requires username or password values from preferences.
     *
     * @param item the credential item to check
     * @return {@code true} if the item is a username or password credential
     */
    private boolean isUsernamePasswordItem(CredentialItem item) {
        return item instanceof CredentialItem.Username ||
                item instanceof CredentialItem.Password;
    }

    /**
     * Handles the case when GitHub preferences are not configured.
     *
     * @param uri the repository URI
     * @param items the credential items to process
     * @return {@code true} if processing can continue
     * @throws UnsupportedCredentialItem if credential items require preferences
     */
    private boolean handleMissingPreferences(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final boolean hasCredentialItems = Lists.immutable.of(items).anySatisfy(this::isUsernamePasswordItem);

        if (hasCredentialItems) {
            throw new UnsupportedCredentialItem(uri, "No GitHub preferences configured");
        }

        return checkForUnsupportedItems(uri, items);
    }

    /**
     * Validates that all credential items are supported.
     *
     * @param uri the repository URI
     * @param items the credential items to validate
     * @return {@code true} if all items are supported
     * @throws UnsupportedCredentialItem if any unsupported items are found
     */
    private boolean checkForUnsupportedItems(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final ImmutableList<CredentialItem> unsupportedItems = Lists.immutable.of(items).reject(this::isItemSupported);

        if (unsupportedItems.notEmpty()) {
            throw new UnsupportedCredentialItem(uri,
                    unsupportedItems.size() + " credential items not supported");
        }

        return true;
    }

    /**
     * Sets the credential value for a supported item using stored preferences.
     *
     * @param item the credential item to populate
     * @param preferences the GitHub preferences containing credential data
     * @throws IllegalArgumentException if the item type is unexpected
     */
    private void setCredentialValue(CredentialItem item, GitHubPreferences preferences) {
        switch (item) {
            case CredentialItem.Username username -> username.setValue(preferences.gitUsername());
            case CredentialItem.Password password -> password.setValue(preferences.gitPassword());
            default -> throw new IllegalArgumentException("Unexpected credential item type: " +
                    item.getClass().getSimpleName());
        }
    }
}