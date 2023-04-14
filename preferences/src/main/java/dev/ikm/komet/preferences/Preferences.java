package dev.ikm.komet.preferences;

public class Preferences {
    private Preferences() {
    }

    public static PreferencesService get() {
        return PreferencesProvider.singleton;
    }

    public static void start() {
        PreferencesProvider.singleton.start();
    }

    public static void stop() {
        PreferencesProvider.singleton.stop();
    }
}
