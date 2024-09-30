package dev.ikm.komet.app;

/**
 * Enum representing the different states of the login feature in the application.
 */
public enum LoginFeatureFlag {

    /**
     * Login is enabled only on web platforms.
     */
    ENABLED_WEB_ONLY,

    /**
     * Login is enabled only on desktop platforms.
     */
    ENABLED_DESKTOP_ONLY,

    /**
     * Login is enabled on both desktop and web platforms.
     */
    ENABLED,

    /**
     * Login feature is disabled on all platforms.
     */
    DISABLED
}
