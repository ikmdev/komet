package dev.ikm.komet.preferences;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

public enum PreferencesServiceFinder {
    INSTANCE;

    PreferencesService service;

    PreferencesServiceFinder() {
        Class serviceClass = PreferencesService.class;
        ServiceLoader<PreferencesService> serviceLoader = ServiceLoader.load(serviceClass);
        Optional<PreferencesService> optionalService = serviceLoader.findFirst();
        if (optionalService.isPresent()) {
            this.service = optionalService.get();
        } else {
            throw new NoSuchElementException("No " + serviceClass.getName() +
                    " found by ServiceLoader...");
        }
    }

    public PreferencesService get() {
        return service;
    }
}
