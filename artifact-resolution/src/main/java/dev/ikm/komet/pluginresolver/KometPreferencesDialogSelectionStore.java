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
package dev.ikm.komet.pluginresolver;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;

import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

/**
 * {@link DialogSelectionStore} backed by {@link KometPreferences}, one child node per flow key.
 */
public final class KometPreferencesDialogSelectionStore implements DialogSelectionStore {

    private static final String NODE_NAME = "dev.ikm.komet.pluginresolver.dialogSelections";
    private static final String REPOSITORY_URL_KEY = "repositoryUrl";

    private final KometPreferences selectionsNode;

    /**
     * Creates a store backed by the current process's user preferences
     * ({@code PreferencesService.get().getUserPreferences()}).
     */
    public KometPreferencesDialogSelectionStore() {
        this(PreferencesService.get().getUserPreferences());
    }

    /**
     * Creates a store backed by an explicit {@link KometPreferences} root, for testing against
     * a real (non-default) preferences node.
     *
     * @param userPreferences the preferences node to store dialog selections under
     * @throws NullPointerException if {@code userPreferences} is {@code null}
     */
    public KometPreferencesDialogSelectionStore(KometPreferences userPreferences) {
        Objects.requireNonNull(userPreferences, "userPreferences");
        this.selectionsNode = userPreferences.node(NODE_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @param flowKey {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException if {@code flowKey} is {@code null}
     */
    @Override
    public Optional<DialogSelection> get(String flowKey) {
        Objects.requireNonNull(flowKey, "flowKey");
        KometPreferences flowNode = selectionsNode.node(flowKey);
        Optional<String> repositoryUrl = flowNode.get(REPOSITORY_URL_KEY);
        if (repositoryUrl.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DialogSelection(repositoryUrl.get()));
    }

    /**
     * {@inheritDoc}
     *
     * @param flowKey {@inheritDoc}
     * @param selection {@inheritDoc}
     * @throws NullPointerException if either argument is {@code null}
     * @throws CredentialStoreException if the underlying preferences store cannot be synced
     */
    @Override
    public void put(String flowKey, DialogSelection selection) {
        Objects.requireNonNull(flowKey, "flowKey");
        Objects.requireNonNull(selection, "selection");
        KometPreferences flowNode = selectionsNode.node(flowKey);
        flowNode.put(REPOSITORY_URL_KEY, selection.repositoryUrl());
        try {
            flowNode.sync();
        } catch (BackingStoreException e) {
            throw new CredentialStoreException("Failed to persist dialog selection for flow " + flowKey, e);
        }
    }
}
