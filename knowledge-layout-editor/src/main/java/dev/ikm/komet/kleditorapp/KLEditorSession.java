package dev.ikm.komet.kleditorapp;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents the KL Editor Session. This singleton holds the state of the session, for example, whether one
 * has started, is endind or ended.
 */
public class KLEditorSession {
    private static KLEditorSession INSTANCE = new KLEditorSession();

    private KLEditorSession() { }

    public static void startSession() {
        Bindings.bindContent(INSTANCE.existingPatterns, EditorPatternModel.getExistingPatterns());
        INSTANCE.setSessionState(SessionState.STARTED);
    }

    public static KLEditorSession getInstance() {
        return INSTANCE;
    }

    public void endSession() {
        setSessionState(SessionState.ENDING);
        EditorPatternModel.impl_cleanUpExistingPatternsList();
        setSessionState(SessionState.ENDED);
    }

    // -- session state
    /**
     * The state of the session.
     */
    private final ObjectProperty<SessionState> sessionState = new SimpleObjectProperty<>(SessionState.NOT_STARTED);
    public SessionState getSessionState() { return sessionState.get(); }
    public ObjectProperty<SessionState> sessionStateProperty() { return sessionState; }
    public void setSessionState(SessionState sessionState) { this.sessionState.set(sessionState); }

    // -- existing patterns
    private final ObservableList<EditorPatternModel> existingPatterns = FXCollections.observableArrayList();
    /**
     * The list of existing created Patterns in the currently started Session.
     */
    private final ObservableList<EditorPatternModel> readonlyExistingPatterns = FXCollections.unmodifiableObservableList(existingPatterns);
    public ObservableList<EditorPatternModel> getExistingPatterns() { return readonlyExistingPatterns; }


    public enum SessionState {
        NOT_STARTED,
        STARTED,
        ENDING,
        ENDED
    }
}