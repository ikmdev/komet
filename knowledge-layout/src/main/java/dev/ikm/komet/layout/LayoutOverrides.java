package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

/// Goal: A reproducible key and layout process for a location within a graph of KlWidgets.
///
/// Challenges:
/// 1. The scene graph may not be complete at the time the Grid Location is computed?
/// 2. We need to find—and subscribe to—parent observable properties.
/// 3. The layout must be serializable and be able to define and restore custom overrides
public class LayoutOverrides implements Encodable {
    private static final Logger LOG = LoggerFactory.getLogger(LayoutOverrides.class);

    /**
     * TODO: This caching strategy will need to be revised for a multi-user environment to keep
     * layout specific to the user, and to handle memory better.
     */
    private final ConcurrentHashMap<LayoutKey.ForArea, AreaGridSettings> layoutOverrides = new ConcurrentHashMap<>();

    private final UUID id;

    private final KometPreferences preferences;

    private LayoutOverrides(UUID id, KometPreferences preferences) {
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(preferences, "preferences is null");
        this.id = id;
        this.preferences = preferences;
        LOG.info("\n\nCreated 1 {} for: {} \n\n", this, preferences.name());
    }
    private LayoutOverrides(KometPreferences preferences) {
        Objects.requireNonNull(preferences, "preferences is null");
        this.id = UUID.randomUUID();
        this.preferences = preferences;
        LOG.info("\n\nCreated 2 {} for: {} \n\n", this, preferences.name());
    }

    private LayoutOverrides(DecoderInput in) {
        this.preferences = KometPreferencesContext.get();
        if (preferences == null) {
            LOG.error("No preferences context set.");
        }
        Objects.requireNonNull(preferences, "preferences is null");
        this.id = in.readUuid();
        int size = in.readVarInt();
        for (int i = 0; i < size; i++) {
            layoutOverrides.put(in.decode(), in.decode());
        }
    }

    public static LayoutOverrides make(UUID overrideMapUuid, KometPreferences preferences) {
        return new LayoutOverrides(overrideMapUuid, preferences);
    }

    public static LayoutOverrides make(KometPreferences preferences) {
        return new LayoutOverrides(preferences);
    }

    public void addOverride(LayoutKey.ForArea layoutKeyProperty, AreaGridSettings layoutSettings) {
        layoutOverrides.put(layoutKeyProperty, layoutSettings);
        LOG.info("\n\nAdded override for: {}, in map id: {} with settings: {}\n\n", layoutKeyProperty, id, layoutSettings);
        // TODO: More than ultimately needed if we had a reliable means of saving the overrides. Layout overrides shoult be small in volume.
        save();
    }

    public void save() {
         preferences.putObject(KlArea.PreferenceKeys.LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES, this);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
        LOG.info("\n\nSaved layout overrides for {} at {}\n\n", preferences.name(), id);
    }

    public static LayoutOverrides restore(KometPreferences preferences) {
        KometPreferencesContext.set(preferences);
        try {
            LayoutOverrides overrides = preferences.getObject(KlArea.PreferenceKeys.LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES, new LayoutOverrides(preferences));
            LOG.info("\n\nRestored {} for: {}, with overrides: {} \n\n", overrides.toString(), preferences.name(), overrides.layoutOverrides.size() );
            return overrides;
        } finally {
            KometPreferencesContext.clear();
        }
    }

    public AreaGridSettings getOrDefault(AreaGridSettings defaultLayout) {
        Objects.nonNull(defaultLayout);
        if (layoutOverrides.containsKey(defaultLayout.layoutKeyForArea())) {
            return layoutOverrides.get(defaultLayout.layoutKeyForArea());
        }
        return defaultLayout;
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeUuid(id);
        out.writeVarInt(layoutOverrides.size());
        layoutOverrides.forEach((key, value) -> {
            out.write(key);
            out.write(value);
        });
    }

    @Decoder
    public static LayoutOverrides decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            // if special handling for particular versions, add case condition.
            default -> new LayoutOverrides(in);
        };
    }

    public class KometPreferencesContext {
        private static final ThreadLocal<KometPreferences> threadLocalPreferences = new ThreadLocal<>();

        public static void set(KometPreferences prefs) {
            threadLocalPreferences.set(prefs);
        }

        public static KometPreferences get() {
            return threadLocalPreferences.get();
        }

        public static void clear() {
            threadLocalPreferences.remove();
        }
    }

}
