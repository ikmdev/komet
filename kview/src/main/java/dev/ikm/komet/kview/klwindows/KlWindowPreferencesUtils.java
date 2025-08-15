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
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;

import java.util.UUID;

import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_ID;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_TYPE;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNALS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_FOLDER_PREFIX;
import static java.io.File.separator;

/**
 * Interface providing utility methods for managing window preferences in the Komet application.
 */
public interface KlWindowPreferencesUtils {

    /**
     * Converts a UUID to a deterministic 8-character string that is safe to use in file paths
     * across all major platforms (Windows, macOS, Linux).
     *
     * @param uuid The UUID to convert
     * @return A consistent 8-character path-safe string
     */
    static String shortenUUID(UUID uuid) {
        // Combine both parts of the UUID to maintain uniqueness
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        // XOR the two halves to create a single long value
        long combined = msb ^ lsb;

        // Ensure we don't get negative values in our base36 conversion
        combined = Math.abs(combined);

        // Convert to base36 (alphanumeric: 0-9, a-z)
        String base36 = Long.toString(combined, 36);

        // Pad with zeros if needed
        while (base36.length() < 8) {
            base36 = "0" + base36;
        }

        // Take or pad to exactly 8 characters
        if (base36.length() > 8) {
            base36 = base36.substring(0, 8);
        }

        // Ensure the first character is a letter to avoid potential
        // issues with numeric-only filenames or reserved names
        if (Character.isDigit(base36.charAt(0))) {
            // Deterministically transform the first digit to a letter (a-j)
            char firstChar = (char) ('a' + (base36.charAt(0) - '0'));
            base36 = firstChar + base36.substring(1);
        }

        return base36;
    }

    /**
     * Retrieves view properties for a specific journal instance.
     * <p>
     * This utility method creates view properties by loading saved journal settings
     * from application preferences. The method:
     * <ol>
     *   <li>Constructs the preference path for the journal using its UUID</li>
     *   <li>Retrieves the journal's preference node</li>
     *   <li>Creates window settings from these preferences</li>
     *   <li>Extracts and returns overridable view properties</li>
     * </ol>
     * <p>
     * This default implementation is provided for all factory implementations to ensure
     * consistent view property retrieval across different window types. It enables
     * windows to maintain visual and behavioral consistency with their parent journal.
     *
     * @param journalTopic the UUID identifying the journal for which to retrieve view properties
     * @return view properties configured for the specified journal
     * @throws NullPointerException if journalTopic is null
     * @see WindowSettings
     * @see ObservableViewNoOverride#makeOverridableViewProperties()
     */
    static ViewProperties getJournalViewProperties(UUID journalTopic) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final String journalPath = JOURNALS + separator
                + JOURNAL_FOLDER_PREFIX + shortenUUID(journalTopic);
        KometPreferences journalPreferences = appPreferences.node(journalPath);
        WindowSettings journalWindowSettings = new WindowSettings(journalPreferences);
        ObservableViewNoOverride windowView = journalWindowSettings.getView();
        return windowView.makeOverridableViewProperties("KlWindowPreferencesUtils.getJournalViewProperties");
    }

    /**
     * Generates a unique journal directory name based on the journal's UUID.
     * This ensures a consistent identifier for preference storage even if the journal is renamed.
     *
     * @param journalTopic the UUID identifying the journal
     * @return A string in the format "JOURNAL_" + shortened UUID for use as a preference folder name
     */
    static String getJournalDirName(UUID journalTopic) {
        return JOURNAL_FOLDER_PREFIX + shortenUUID(journalTopic);
    }

    /**
     * Retrieves the preferences node specific to a journal instance.
     * The node path is constructed using the journal's unique directory name.
     *
     * @param journalTopic the UUID identifying the journal
     * @return The KometPreferences node for this journal instance
     */
    static KometPreferences getJournalPreferences(UUID journalTopic) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final String path = JOURNALS + separator + getJournalDirName(journalTopic);
        return appPreferences.node(path);
    }

    /**
     * Retrieves the preferences node for a window within a journal, automatically generating a new window ID.
     * <p>
     * This convenience method creates a new UUID for the window and delegates to the three-parameter
     * version of {@code getWindowPreferences}. It's useful when creating a new window instance
     * that doesn't yet have an assigned ID.
     *
     * @param journalTopic the UUID identifying the parent journal
     * @param windowType   the type of the window (e.g., CONCEPT, PATTERN, etc.)
     * @return the KometPreferences node for the newly identified window
     * @see #getWindowPreferences(UUID, UUID, EntityKlWindowType)
     */
    static KometPreferences getWindowPreferences(UUID journalTopic, EntityKlWindowType windowType) {
        return getWindowPreferences(journalTopic, UUID.randomUUID(), windowType);
    }

    /**
     * Retrieves the preferences node for a specific window within a journal.
     * <p>
     * This method constructs the appropriate preference path using shortened UUIDs
     * for both the journal and window topics to ensure path length compatibility
     * across all platforms.
     *
     * @param journalTopic the UUID identifying the parent journal
     * @param windowTopic  the UUID identifying the specific window
     * @param windowType   the type of the window (e.g., CONCEPT, PATTERN, etc.)
     * @return the KometPreferences node for the specified window
     */
    static KometPreferences getWindowPreferences(UUID journalTopic, UUID windowTopic, EntityKlWindowType windowType) {
        final String path = JOURNALS +
                separator + JOURNAL_FOLDER_PREFIX + shortenUUID(journalTopic) +
                separator + windowType.getPrefix() + shortenUUID(windowTopic);
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(path);
        windowPreferences.put(WINDOW_ID, windowTopic.toString());
        windowPreferences.put(WINDOW_TYPE, windowType.toString());
        return windowPreferences;
    }
}