package dev.ikm.komet.layout.preferences;

import dev.ikm.tinkar.coordinate.Coordinates;

/**
 * An enumeration defining global preference keys with associated default values.
 * Each key in this enum implements the PropertyWithDefault interface, providing
 * a mechanism to retrieve predefined default values for preferences.
 * These keys are primarily used for storing and retrieving application-wide
 * global preferences, with a focus on defaults when specific values are absent.
 */
public enum KlGlobalPreferenceKeys implements PropertyWithDefault {
    /**
     * A key representing the default coordinate for views. This value is used
     * as the default when no specific coordinate preference is defined.
     * It is initialized with a predefined default view coordinate provided by
     * Coordinates.View.DefaultView().
     */
    DEFAULT_VIEW_COORDINATE(Coordinates.View.DefaultView());

    final Object defaultValue;

    KlGlobalPreferenceKeys(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    @Override
    public Object defaultValue() {
        return defaultValue;
    }
}
