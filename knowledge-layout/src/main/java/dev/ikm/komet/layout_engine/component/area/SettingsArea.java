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
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * A {@link dev.ikm.komet.layout.KlArea} that presents user settings — font family, font size, and a masked
 * password — as drawer content. It is the simplest content payload for the {@code KlDrawer}: it owns its own
 * controls and round-trips their values through the standard area save/restore lifecycle, so a restored card
 * brings its settings back.
 *
 * <p>The font and size are stored as plain preference values; the password is stored through the existing
 * {@link KometPreferences#putPassword(String, char[]) putPassword} / {@link KometPreferences#getPassword(String)
 * getPassword} mechanism (obfuscated at rest), never as clear text. A live preview label reflects the chosen
 * font and size within the area; applying the font to a wider scope (the host card or journal) is a separate
 * concern layered on top of this area.
 */
public class SettingsArea extends SupplementalAreaBlueprint {

    /** Preference key for the selected font family. */
    private static final String FONT_FAMILY_KEY = "settings.fontFamily";
    /** Preference key for the selected font size. */
    private static final String FONT_SIZE_KEY = "settings.fontSize";
    /** Preference key for the masked password (stored via {@code putPassword}). */
    private static final String PASSWORD_KEY = "settings.password";

    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 72;
    private static final int DEFAULT_FONT_SIZE = 14;

    private final ComboBox<String> fontFamilyBox = new ComboBox<>();
    private final Spinner<Integer> fontSizeSpinner =
            new Spinner<>(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE);
    private final PasswordField passwordField = new PasswordField();
    private final Label preview = new Label("The quick brown fox 0123");

    {
        fontFamilyBox.getItems().setAll(Font.getFamilies());
        fontFamilyBox.setValue(Font.getDefault().getFamily());
        fontSizeSpinner.setEditable(true);
        passwordField.setPromptText("password");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(12));
        form.addRow(0, new Label("Font"), fontFamilyBox);
        form.addRow(1, new Label("Size"), fontSizeSpinner);
        form.addRow(2, new Label("Password"), passwordField);

        VBox content = new VBox(12, form, preview);
        content.setPadding(new Insets(12));
        fxObject().setCenter(content);

        fontFamilyBox.valueProperty().addListener((obs, oldValue, newValue) -> applyPreview());
        fontSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> applyPreview());
        applyPreview();
    }

    /**
     * Constructs a settings area to be restored from the given preferences node.
     *
     * @param preferences the preferences node backing this area
     */
    public SettingsArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Constructs a fresh settings area with a provisioned preferences node.
     *
     * @param preferencesFactory the factory that provisions this area's preferences node
     * @param areaFactory        the area factory that produced this area
     */
    public SettingsArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /** Applies the currently selected font family and size to the preview label. */
    private void applyPreview() {
        String family = fontFamilyBox.getValue();
        int size = fontSizeSpinner.getValue() == null ? DEFAULT_FONT_SIZE : fontSizeSpinner.getValue();
        preview.setFont(Font.font(family, size));
    }

    /**
     * Returns the currently selected font family, or {@code null} if none is selected.
     *
     * @return the font family
     */
    public String getSelectedFontFamily() {
        return fontFamilyBox.getValue();
    }

    /**
     * Returns the currently selected font size.
     *
     * @return the font size in points
     */
    public int getSelectedFontSize() {
        return fontSizeSpinner.getValue() == null ? DEFAULT_FONT_SIZE : fontSizeSpinner.getValue();
    }

    /**
     * Selects the given font family.
     *
     * @param family the font family
     */
    public void setSelectedFontFamily(String family) {
        fontFamilyBox.setValue(family);
    }

    /**
     * Sets the font size, clamped to the supported range.
     *
     * @param size the font size in points
     */
    public void setSelectedFontSize(int size) {
        int clamped = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, size));
        fontSizeSpinner.getValueFactory().setValue(clamped);
    }

    /**
     * Sets the password held by the masked field. The value is never rendered in clear text and is
     * persisted only through {@code putPassword}.
     *
     * @param password the password characters, or {@code null} to clear
     */
    public void setPassword(char[] password) {
        passwordField.setText(password == null ? "" : new String(password));
    }

    /*******************************************************************************
     *  Framework save / restore                                                   *
     ******************************************************************************/

    @Override
    protected void subAreaSave() {
        if (fontFamilyBox.getValue() != null) {
            preferences().put(FONT_FAMILY_KEY, fontFamilyBox.getValue());
        }
        preferences().put(FONT_SIZE_KEY, String.valueOf(getSelectedFontSize()));
        String password = passwordField.getText();
        if (password != null && !password.isEmpty()) {
            preferences().putPassword(PASSWORD_KEY, password.toCharArray());
        }
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        preferences().get(FONT_FAMILY_KEY).ifPresent(fontFamilyBox::setValue);
        preferences().get(FONT_SIZE_KEY).ifPresent(value -> {
            try {
                fontSizeSpinner.getValueFactory().setValue(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                // A non-numeric stored value is treated as absent; the spinner keeps its default.
            }
        });
        preferences().getPassword(PASSWORD_KEY).ifPresent(chars -> passwordField.setText(new String(chars)));
        applyPreview();
    }

    @Override
    protected void subAreaRevert() {
        subAreaRestoreFromPreferencesOrDefault();
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // No external bindings to release.
    }

    /*******************************************************************************
     *  Factory                                                                    *
     ******************************************************************************/

    /**
     * Returns the factory for {@code SettingsArea} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores a {@code SettingsArea} from previously stored preferences.
     *
     * @param preferences the preferences node backing the area
     * @return the restored area
     */
    public static SettingsArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Creates a new {@code SettingsArea} with the supplied grid settings.
     *
     * @param preferencesFactory the factory that provisions the area's preferences node
     * @param areaGridSettings   the grid placement settings for the area
     * @return the created area
     */
    public static SettingsArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    /**
     * Creates a new {@code SettingsArea} with default grid settings.
     *
     * @param preferencesFactory the factory that provisions the area's preferences node
     * @return the created area
     */
    public static SettingsArea create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    /**
     * Factory that produces and restores {@link SettingsArea} instances.
     */
    public static class Factory implements SupplementalAreaBlueprint.Factory<SettingsArea> {

        @Override
        public SettingsArea restore(KometPreferences preferences) {
            return new SettingsArea(preferences);
        }

        @Override
        public SettingsArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            SettingsArea area = new SettingsArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            return area;
        }
    }
}
