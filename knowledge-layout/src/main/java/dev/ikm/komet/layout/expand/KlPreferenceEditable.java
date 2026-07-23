package dev.ikm.komet.layout.expand;

import dev.ikm.komet.layout.settings.ConceptSettingsForm;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.scene.layout.Region;

import java.util.function.Supplier;

/**
 * A composing capability: a figure that exposes its stored preferences for editing through the common
 * mechanism. When a {@link KlExpandable} figure also implements this, its full-surface chrome shows a
 * standard <b>preferences toggle</b> that reveals {@link #preferenceEditor()}.
 *
 * <p>The default editor is a generic {@link ConceptSettingsForm} auto-generated from the settings
 * record — zero boilerplate. An area supplies {@link #settings()} (the current value, read live) and
 * {@link #applySettings(Record)} (persist + apply), and may override {@link #preferenceEditor()} to
 * customize the generated form (e.g. {@code ConceptSettingsForm.of(settings(), this::applySettings)
 * .hide("someField").augment(customControl)}).
 *
 * @param <T> the settings record type (an {@link Encodable} {@link Record} of concept-identity enums)
 */
public interface KlPreferenceEditable<T extends Record & Encodable> {

    /**
     * Supplies the current settings value (read live so edits compose).
     *
     * @return a supplier of the current settings record
     */
    Supplier<T> settings();

    /**
     * Persists and applies an edited settings value.
     *
     * @param updated the new settings record
     */
    void applySettings(T updated);

    /**
     * The editor shown by the preferences toggle. The default is a generic {@link ConceptSettingsForm}
     * over {@link #settings()}; override to customize (hide/augment fields) or supply a bespoke editor.
     *
     * @return the preferences editor region
     */
    default Region preferenceEditor() {
        return ConceptSettingsForm.of(settings(), this::applySettings);
    }
}
