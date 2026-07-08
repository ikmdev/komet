package dev.ikm.komet.layout.editor.model;

/**
 * The type of a KL Window. There are three standard (application-provided) windows, seeded by
 * {@code StandardEditorWindows}; every window the user creates in the KL Editor is a
 * {@link #SEMANTICS} window — those are opened from a search result via
 * "Open as &lt;name of window&gt;".
 */
public enum EditorWindowType {
    STANDARD_CONCEPT("Standard Concept Window"),
    STANDARD_PATTERN("Standard Pattern Window"),
    STANDARD_SEMANTIC("Standard Semantic Window"),
    SEMANTICS("Semantics Window");

    private final String displayName;

    EditorWindowType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}