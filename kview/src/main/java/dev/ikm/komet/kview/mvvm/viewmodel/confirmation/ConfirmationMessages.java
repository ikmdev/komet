package dev.ikm.komet.kview.mvvm.viewmodel.confirmation;

/**
 * The confirmation messages that the confirmation pane can display.
 */
public enum ConfirmationMessages {

    NO_SELECTION_MADE_SEMANTIC(new ConfirmationText("No Selection Made", ConfirmationText.SEMANTIC_DEFAULT_MESSAGE)),
    SEMANTIC_DETAILS_ADDED(new ConfirmationText("Semantic Details Added", ConfirmationText.SEMANTIC_DEFAULT_MESSAGE)),

    NO_SELECTION_MADE_PATTERN(new ConfirmationText("No Selection Made", ConfirmationText.PATTERN_DEFAULT_MESSAGE)),
    PATTERN_DEFINITION_ADDED(new ConfirmationText("Pattern Definition Added", "")),
    FULLY_QUALIFIED_NAME_ADDED(new ConfirmationText("Fully Qualified Name Added", "")),
    OTHER_NAME_ADDED(new ConfirmationText("Other Name Added", "")),
    DEFINITION_ADDED(new ConfirmationText("Definition Added", "")),
    CONTINUE_EDITING_FIELDS(new ConfirmationText("Continue Editing Fields?", ""));

    private ConfirmationText confirmationText;

    ConfirmationMessages(ConfirmationText confirmationText) {
        this.confirmationText = confirmationText;
    }

    ConfirmationText getConfirmationText() {
        return confirmationText;
    }

}
