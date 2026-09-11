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
package dev.ikm.komet.framework.settings;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;

/**
 * Application-wide user settings: the values edited in the Settings dialog.
 *
 * <p>These apply to every Komet window and to every database, for the current OS user, so they
 * are stored in {@link PreferencesService#userPreferences()} — the per-user tier backed by the JDK
 * preferences store — rather than in the per-database configuration preferences. Keys use the
 * reverse-DNS convention already used by other per-user settings in the workspace.
 *
 * <p>Each setting is a JavaFX property so that windows can subscribe and react as they are wired
 * up. The text size takes effect through {@link TextSizeStylesheet}, installed once at start-up;
 * the glyph and language settings are persisted by the dialog and wired in separately.
 */
public final class KometSettings {

    private static final Logger LOG = LoggerFactory.getLogger(KometSettings.class);

    private static final String KEY_PREFIX = "dev.ikm.komet.settings.";
    static final String TEXT_SIZE_KEY = KEY_PREFIX + "textSize";
    static final String SHOW_KIND_SIGIL_KEY = KEY_PREFIX + "showKindSigil";
    static final String SHOW_DEFINITION_STATUS_KEY = KEY_PREFIX + "showDefinitionStatus";
    static final String SHOW_MULTIPLE_PARENTS_KEY = KEY_PREFIX + "showMultipleParents";
    static final String DISPLAY_LANGUAGE_KEY = KEY_PREFIX + "displayLanguage";

    /** Lazily created so the preferences service is only looked up on first use. */
    private static final class Holder {
        private static final KometSettings INSTANCE = new KometSettings(PreferencesService.userPreferences());
    }

    private final KometPreferences preferences;

    /** The settings for the current OS user. */
    public static KometSettings get() {
        return Holder.INSTANCE;
    }

    /**
     * Settings read from and written to {@code preferences} instead of the user tier. For tests
     * and tools that must not touch the user's real settings.
     */
    public static KometSettings backedBy(KometPreferences preferences) {
        return new KometSettings(preferences);
    }

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    private KometSettings(KometPreferences preferences) {
        this.preferences = preferences;

        textSize.set(readEnum(TEXT_SIZE_KEY, TextSize.DEFAULT));
        showKindSigil.set(preferences.getBoolean(SHOW_KIND_SIGIL_KEY, true));
        showDefinitionStatus.set(preferences.getBoolean(SHOW_DEFINITION_STATUS_KEY, true));
        showMultipleParents.set(preferences.getBoolean(SHOW_MULTIPLE_PARENTS_KEY, true));
        displayLanguage.set(readEnum(DISPLAY_LANGUAGE_KEY, DisplayLanguage.SYSTEM_DEFAULT));

        textSize.subscribe(() -> store(TEXT_SIZE_KEY, textSize.get().name()));
        showKindSigil.subscribe(() -> store(SHOW_KIND_SIGIL_KEY, showKindSigil.get()));
        showDefinitionStatus.subscribe(() -> store(SHOW_DEFINITION_STATUS_KEY, showDefinitionStatus.get()));
        showMultipleParents.subscribe(() -> store(SHOW_MULTIPLE_PARENTS_KEY, showMultipleParents.get()));
        displayLanguage.subscribe(() -> store(DISPLAY_LANGUAGE_KEY, displayLanguage.get().name()));
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- text size
    /**
     * The text-size step applied to every window.
     */
    private final ObjectProperty<TextSize> textSize = new SimpleObjectProperty<>(this, "textSize");
    public final ObjectProperty<TextSize> textSizeProperty() {
        return textSize;
    }
    public final TextSize getTextSize() {
        return textSize.get();
    }
    public final void setTextSize(TextSize value) {
        textSize.set(value);
    }

    // -- show kind sigil
    /**
     * Whether the component-kind sigil (D / S / P / ? letter, stamp pentagon) is drawn beside names.
     */
    private final BooleanProperty showKindSigil = new SimpleBooleanProperty(this, "showKindSigil");
    public final BooleanProperty showKindSigilProperty() {
        return showKindSigil;
    }
    public final boolean isShowKindSigil() {
        return showKindSigil.get();
    }
    public final void setShowKindSigil(boolean value) {
        showKindSigil.set(value);
    }

    // -- show definition status
    /**
     * Whether the definition-status glyph (defined, primitive, root) is drawn beside concept names.
     */
    private final BooleanProperty showDefinitionStatus = new SimpleBooleanProperty(this, "showDefinitionStatus");
    public final BooleanProperty showDefinitionStatusProperty() {
        return showDefinitionStatus;
    }
    public final boolean isShowDefinitionStatus() {
        return showDefinitionStatus.get();
    }
    public final void setShowDefinitionStatus(boolean value) {
        showDefinitionStatus.set(value);
    }

    // -- show multiple parents
    /**
     * Whether the multiple-parents fork is appended to the status glyph. It only ever appears on
     * top of the definition status, so it has no effect while {@link #showDefinitionStatusProperty()}
     * is false.
     */
    private final BooleanProperty showMultipleParents = new SimpleBooleanProperty(this, "showMultipleParents");
    public final BooleanProperty showMultipleParentsProperty() {
        return showMultipleParents;
    }
    public final boolean isShowMultipleParents() {
        return showMultipleParents.get();
    }
    public final void setShowMultipleParents(boolean value) {
        showMultipleParents.set(value);
    }

    // -- display language
    /**
     * The language of Komet's own user interface.
     */
    private final ObjectProperty<DisplayLanguage> displayLanguage = new SimpleObjectProperty<>(this, "displayLanguage");
    public final ObjectProperty<DisplayLanguage> displayLanguageProperty() {
        return displayLanguage;
    }
    public final DisplayLanguage getDisplayLanguage() {
        return displayLanguage.get();
    }
    public final void setDisplayLanguage(DisplayLanguage value) {
        displayLanguage.set(value);
    }

    /*=*************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     ************************************************************************=*/

    /**
     * Reads an enum-valued setting by constant name. The stored value comes from an external store
     * that an older or newer Komet may have written, so a name this build does not know falls back
     * to the default rather than failing.
     */
    private <E extends Enum<E>> E readEnum(String key, E defaultValue) {
        String stored = preferences.get(key, defaultValue.name());
        return Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants())
                .filter(constant -> constant.name().equals(stored))
                .findFirst()
                .orElse(defaultValue);
    }

    private void store(String key, String value) {
        preferences.put(key, value);
        flush();
    }

    private void store(String key, boolean value) {
        preferences.putBoolean(key, value);
        flush();
    }

    /** Pushes the change to the OS-level store now, so it survives an abrupt exit. */
    private void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LOG.warn("Unable to flush user settings to the preferences store", e);
        }
    }
}
