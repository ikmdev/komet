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
     * Key for the KL Window's preferred (view) width in pixels.
     */
    public static final String KL_WINDOW_PREF_WIDTH = "kl-window-pref-width";

    /**
     * Key for the KL Window's preferred (view) height in pixels.
     */
    public static final String KL_WINDOW_PREF_HEIGHT = "kl-window-pref-height";

    /**
     * Key for whether the Coordinate control-bar icon is shown in the KL Window header.
     */
    public static final String KL_WINDOW_COORDINATE_VISIBLE = "kl-window-coordinate-visible";

    /**
     * Key for whether the Timeline control-bar icon is shown in the KL Window header.
     */
    public static final String KL_WINDOW_TIMELINE_VISIBLE = "kl-window-timeline-visible";

    /**
     * Key for the KL Window's type (an {@code EditorWindowType} name): one of the three standard
     * windows or a user-created Semantics Window.
     */
    public static final String KL_WINDOW_TYPE = "kl-window-type";

    /**
     * Root of KL Editor 'App' preferences.
     */
    public static final String KL_EDITOR_APP = "kl-editor-app";

    /**
     * Folder (under {@link #KL_EDITOR_APP}) holding the windows created by the user in the KL Editor.
     */
    public static final String KL_USER_WINDOWS_DIR = "user-windows";

    /**
     * Folder (under {@link #KL_EDITOR_APP}) holding the application-provided standard windows
     * (Concept Window, Pattern Window, Semantic Window).
     */
    public static final String KL_STANDARD_WINDOWS_DIR = "standard-windows";

    /**
     * Section Reference Component
     */
    public static final String KL_REFERENCE_COMPONENT = "kl-reference-component";

    /**
     * Key for the list of supplemental areas placed in a Section (a list of area ids).
     */
    public static final String KL_SUPPLEMENTAL_AREAS = "KL-SUPPLEMENTAL-AREAS";

    /**
     * Key for a placed supplemental area's factory class name — the runtime
     * {@code KlSupplementalArea.Factory} used to restore the area.
     */
    public static final String KL_AREA_FACTORY_CLASS_NAME = "kl-area-factory-class-name";

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

    /**
     * Keys for data properties of things placed in a Section (patterns, supplemental areas, ...) — as
     * opposed to how they are laid out in the grid.
     */
    public class DataPropertyKey {
        /**
         * Whether the node is required (must be filled out when the window is opened in the Journal in
         * create mode).
         */
        public static String KL_REQUIRED = "kl-required";
    }

    public class PatternKey {
        public static String PATTERN_TITLE_VISIBLE = "pattern-title-visible";
        public static String PATTERN_SEMANTICS_FACTORY = "pattern-semantics-factory";
    }
}