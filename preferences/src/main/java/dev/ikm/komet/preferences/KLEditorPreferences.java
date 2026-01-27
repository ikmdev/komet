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
     * Root of KL Editor 'App' preferences.
     */
    public static final String KL_EDITOR_APP = "kl-editor-app";

    /**
     * Section Reference Component
     */
    public static final String KL_REFERENCE_COMPONENT = "kl-reference-component";

    public enum ListKey {
        /**
         * List of Patterns in a Section
         */
        PATTERN_LIST,
        /**
         * List of Fields in a Pattern
         */
        FIELDS_LIST
    };

    public class GridLayoutKey {
        /**
         * The number of columns to use for the GridPane like control.
         */
        public static String KL_GRID_NUMBER_COLUMNS = "kl-grid-number-columns";

        /**
         * The grid node column index.
         */
        public static String KL_GRID_COLUMN_INDEX = "kl-grid-node-column-index";

        /**
         * The grid node row index.
         */
        public static String KL_GRID_ROW_INDEX = "kl-grid-node-row-index";

        /**
         * The grid node column span.
         */
        public static String KL_GRID_COLUMN_SPAN = "kl-grid-node-column-span";
    }

    public class PatternKey {
        public static String PATTERN_TITLE_VISIBLE = "pattern-title-visible";
    }
}