package dev.ikm.komet.kview.mvvm.model;

/**
 * This helps disambiguate between a concept and pattern,
 * if we just put the public id in the user data during
 * drag-n-drop, then you can make a concept out of a pattern
 * and vice versa
 */
public enum DragAndDropType {
    CONCEPT,
    PATTERN,
    SEMANTIC,
    STAMP
}
