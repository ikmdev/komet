package dev.ikm.komet.kview.mvvm.viewmodel.confirmation;

/**
 * Contains the confirmation title and message text for display on the confirmation pane.
 * @param title
 * @param message
 */
record ConfirmationText(String title, String message) {

    static final String SEMANTIC_DEFAULT_MESSAGE = "Make a selection in the view to edit the Semantic.";
    static final String PATTERN_DEFAULT_MESSAGE = "Make a selection in the view to edit the Pattern.";

}
