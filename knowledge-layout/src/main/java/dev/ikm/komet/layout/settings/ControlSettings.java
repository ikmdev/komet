package dev.ikm.komet.layout.settings;

import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.binary.Encodable;

import java.util.Objects;

/**
 * A typed, reusable seam for persisting and restoring the settings of a single embeddable
 * KL-tier control, backed by the existing {@link KometPreferences} object rail.
 * <p>
 * A control's settings are modelled as one whole {@link Encodable} value (a {@code @RecordBuilder}
 * record with a {@code DEFAULT} constant, in the {@link dev.ikm.komet.layout.area.AreaGridSettings}
 * style) rather than a field-by-field explosion of primitives. {@code ControlSettings} pairs that
 * value type with a preference-key enum that is <em>both</em>:
 * <ul>
 *   <li>an {@link Enum} — a valid {@link KometPreferences} key, and</li>
 *   <li>a {@link PropertyWithDefault} — a {@code publicId()}-bearing concept binding carrying the
 *       control's default value (via {@link PropertyWithDefault#defaultValue()}).</li>
 * </ul>
 * The dual bound is enforced at compile time, so a settings key cannot be a bare string and always
 * carries a concept identity — the convention the whole per-control settings mechanism relies on.
 * <p>
 * This class is deliberately thin: the reusable asset is the <em>convention</em>
 * (record + {@code Encodable} · {@link PropertyWithDefault} key enum · a {@code KlDrawer} editor ·
 * {@code subCardSave}/{@code subCardRestore}), not a framework. There is no registry and no
 * reflection. See {@code package-info} for the full recipe and the first consumer.
 *
 * @param <K> the preference-key enum type — an {@link Enum} that is also a {@link PropertyWithDefault}
 * @param <T> the {@link Encodable} settings value type persisted whole under the key
 */
public record ControlSettings<K extends Enum<K> & PropertyWithDefault, T extends Encodable>(
        K key, T defaultValue) {

    /**
     * Creates a settings binding.
     *
     * @param key          the non-null preference key (also the settings' concept identity)
     * @param defaultValue the non-null value returned when nothing has been persisted yet
     */
    public ControlSettings {
        Objects.requireNonNull(key, "key is null");
        Objects.requireNonNull(defaultValue, "defaultValue is null");
    }

    /**
     * Creates a settings binding.
     *
     * @param key          the preference key (also the settings' concept identity)
     * @param defaultValue the value returned when nothing has been persisted yet
     * @param <K>          the preference-key enum type
     * @param <T>          the {@link Encodable} settings value type
     * @return a new {@code ControlSettings} pairing the key with its default value
     */
    public static <K extends Enum<K> & PropertyWithDefault, T extends Encodable> ControlSettings<K, T> of(
            K key, T defaultValue) {
        return new ControlSettings<>(key, defaultValue);
    }

    /**
     * Loads the persisted settings value, or {@link #defaultValue()} if nothing has been stored
     * yet under {@link #key()}.
     *
     * @param preferences the control's preferences node (e.g. {@code AbstractHostCard.preferences()})
     * @return the restored settings value, never {@code null}
     */
    public T load(KometPreferences preferences) {
        return preferences.getObject(key, defaultValue);
    }

    /**
     * Persists the settings value whole under {@link #key()}. The caller is responsible for the
     * enclosing {@code flush()} on its save cycle (the lifecycle already flushes in
     * {@code subCardSave}).
     *
     * @param preferences the control's preferences node
     * @param value       the settings value to persist
     */
    public void save(KometPreferences preferences, T value) {
        preferences.putObject(key, value);
    }
}
