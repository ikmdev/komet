package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;

/**
 * The {@code KlProfiles} class provides utility methods for accessing
 * user profile and shared preferences within an application. It defines
 * constants for preference path keys and offers methods to access and
 * create preference factories for user-specific and shared configurations,
 * including layouts and windows.
 */
public class KlProfiles {
    /**
     * Defines the base path for user profile preferences.
     * This constant is used to locate preferences associated with individual users
     * as part of the application configuration or state management.
     */
    public static final String USER_PROFILES = "/profiles/users";
    /**
     * Represents the base path for accessing shared profile-related preferences.
     * This is used as the base identifier within the preference management system
     * to differentiate shared profiles from user-specific or other configuration nodes.
     */
    public static final String SHARED_PROFILE = "/profiles/shared";
    /**
     * A constant representing the path suffix for accessing layout preference nodes.
     * This value is used as a postfix to construct paths or keys related to layouts
     * within the preferences system.
     */
    public static final String LAYOUTS_SUFFIX = "/layouts";
    /**
     * Represents the path suffix used for identifying preferences or configurations
     * related to windows in the system. This constant is typically used
     * in constructing paths or keys for storage and retrieval of window-specific data.
     */
    public static final String WINDOWS_SUFFIX = "/windows";
    /**
     * Represents the shared layout preferences path key for the application.
     * It is constructed as a combination of the shared profile identifier and the layout suffix.
     * This key is used for accessing shared layout preferences in a consistent manner
     * across different components or modules in the system.
     */
    public static final String SHARED_LAYOUTS = SHARED_PROFILE + LAYOUTS_SUFFIX;
    /**
     * Represents the path key for shared window preferences in the application.
     * This constant is derived by appending the {@link #WINDOWS_SUFFIX} to {@link #SHARED_PROFILE}.
     * It is used to manage and access preferences common to all users related to window configurations.
     */
    public static final String SHARED_WINDOWS = SHARED_PROFILE + WINDOWS_SUFFIX;

    /**
     * Provides access to the shared profile preferences node.
     *
     * @return A {@code KometPreferences} instance representing the shared profile preferences.
     */
    public static KometPreferences sharedProfilePreferences() {
        return PreferencesService.configurationPreferences().node(SHARED_PROFILE);
    }

    /**
     * Provides access to the shared layout preferences node.
     *
     * @return A {@code KometPreferences} instance representing the shared layout preferences.
     */
    public static KometPreferences sharedLayoutPreferences() {
        return PreferencesService.configurationPreferences().node(SHARED_LAYOUTS);
    }

    /**
     * Creates a {@code KlPreferencesFactory} for managing shared layout preferences
     * with the given implementation class.
     *
     * @param implementationClass The class for which the preferences factory is intended.
     * @return A {@code KlPreferencesFactory} instance for the shared layout preferences.
     */
    public static KlPreferencesFactory sharedLayoutPreferenceFactory(Class implementationClass) {
        return KlPreferencesFactory.createFactory(sharedLayoutPreferences(), implementationClass);
    }

    /**
     * Provides access to the shared window preferences node.
     *
     * @return A {@code KometPreferences} instance representing the shared window preferences.
     */
    public static KometPreferences sharedWindowPreferences() {
        return PreferencesService.configurationPreferences().node(SHARED_WINDOWS);
    }
    /**
     * Creates a {@code KlPreferencesFactory} for managing shared window preferences
     * with the given implementation class.
     *
     * @param implementationClass The class for which the preferences factory is intended.
     * @return A {@code KlPreferencesFactory} instance for the shared window preferences.
     */
    public static KlPreferencesFactory sharedWindowPreferenceFactory(Class implementationClass) {
        return KlPreferencesFactory.createFactory(sharedWindowPreferences(), implementationClass);
    }

    /**
     * Provides access to the preferences node for a specific user's profile.
     *
     * @param userName The name of the user whose profile preferences are requested.
     * @return A {@code KometPreferences} instance representing the user's profile preferences.
     */
    public static KometPreferences userProfilePreferences(String userName) {
        return PreferencesService.configurationPreferences().node(USER_PROFILES + "/" + userName);
    }
    /**
     * Provides access to the window preferences node for a specific user.
     *
     * @param userName The name of the user whose window preferences are requested.
     * @return A {@code KometPreferences} instance representing the user's window preferences.
     */
    public static KometPreferences userWindowPreferences(String userName) {
        return PreferencesService.configurationPreferences().node(USER_PROFILES + "/" + userName + WINDOWS_SUFFIX);
    }

    /**
     * Creates a {@code KlPreferencesFactory} for managing window preferences of a specified user
     * with the given implementation class.
     *
     * @param userName The name of the user whose window preferences factory needs to be created.
     * @param implementationClass The class for which the preferences factory is intended.
     * @return A {@code KlPreferencesFactory} instance for the specified user's window preferences.
     */
    public static KlPreferencesFactory userWindowPreferenceFactory(String userName, Class implementationClass) {
        return KlPreferencesFactory.createFactory(userWindowPreferences(userName), implementationClass);
    }
    /**
     * Provides access to the layout preferences node for a specific user.
     *
     * @param userName The name of the user whose layout preferences are requested.
     * @return A {@code KometPreferences} instance representing the user's layout preferences.
     */
    public static KometPreferences userLayoutPreferences(String userName) {
        return PreferencesService.configurationPreferences().node(USER_PROFILES + "/" + userName + LAYOUTS_SUFFIX);
    }

    /**
     * Creates a {@code KlPreferencesFactory} for managing layout preferences of a specified user
     * with the given implementation class.
     *
     * @param userName The name of the user whose layout preferences factory needs to be created.
     * @param implementationClass The class for which the preferences factory is intended.
     * @return A {@code KlPreferencesFactory} instance for the specified user's layout preferences.
     */
    public static KlPreferencesFactory userLayoutPreferenceFactory(String userName, Class implementationClass) {
        return KlPreferencesFactory.createFactory(userLayoutPreferences(userName), implementationClass);
    }

}
