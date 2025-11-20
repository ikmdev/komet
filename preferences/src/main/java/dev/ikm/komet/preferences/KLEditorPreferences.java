package dev.ikm.komet.preferences;

public class KLEditorPreferences {
    /**
     * Key for the list of created KL Windows.
     */
    public static final String KL_EDITOR_WINDOWS = "KL-EDITOR-WINDOWS";

    /**
     * Key for the main section inside the KL Window
     */
    public static final String KL_MAIN_SECTION = "KL-MAIN-SECTION";

    /**
     * Key for the list of additional Sections inside the KL Window
     */
    public static final String KL_ADDITIONAL_SECTIONS = "KL-ADDITIONAL-SECTIONS";

    /**
     * Key for the list Patterns inside a Section.
     */
    public static final String KL_PATTERNS = "KL-PATTERNS";

    /**
     * Root of KL Editor 'App' preferences.
     */
    public static final String KL_EDITOR_APP = "kl-editor-app";

    public enum PatternKey {
        /**
         * List of Patterns in a Section
         */
        PATTERN_LIST
    };
}