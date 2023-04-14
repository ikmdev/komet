package dev.ikm.komet.preferences;

import com.google.auto.service.AutoService;

import java.util.concurrent.atomic.AtomicReference;

@AutoService({KometPreferences.class})
public class PreferencesServiceFactory {
    protected static final AtomicReference<PreferencesService> provider = new AtomicReference<>();

    public static PreferencesService provider() {
        return provider.updateAndGet(entityProvider -> {
            if (entityProvider == null) {
                return new PreferencesProvider();
            }
            return entityProvider;
        });
    }


}
