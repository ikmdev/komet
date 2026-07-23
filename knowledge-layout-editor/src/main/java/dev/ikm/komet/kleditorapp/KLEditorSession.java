package dev.ikm.komet.kleditorapp;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Represents the KL Editor Session. This singleton holds the state of the session, for example, whether one
 * has started, is ending or ended. It is also the single owner of the list of Patterns that exist in the
 * Window the session is about: the list is populated from the Window when the session starts, kept in sync
 * with Patterns and Sections being added to or removed from the Window while it's edited, and cleared when
 * the session ends.
 */
public class KLEditorSession {
    private static KLEditorSession INSTANCE = new KLEditorSession();

    private KLEditorSession() { }

    /**
     * Starts a session for editing the given Window. Populates the existing patterns list with the
     * Patterns currently in the Window and starts tracking Patterns being added and removed.
     *
     * @param window the Window this session is about
     */
    public static void startSession(EditorWindowModel window) {
        INSTANCE.currentWindow = window;

        INSTANCE.existingPatterns.setAll(window.getAllPatterns());

        window.getMainSection().getPatterns().addListener(INSTANCE.sectionPatternsListener);
        for (EditorSectionModel section : window.getAdditionalSections()) {
            section.getPatterns().addListener(INSTANCE.sectionPatternsListener);
        }
        window.getAdditionalSections().addListener(INSTANCE.additionalSectionsListener);

        INSTANCE.setSessionState(SessionState.STARTED);
    }

    public static KLEditorSession getInstance() {
        return INSTANCE;
    }

    public void endSession() {
        setSessionState(SessionState.ENDING);

        currentWindow.getMainSection().getPatterns().removeListener(sectionPatternsListener);
        for (EditorSectionModel section : currentWindow.getAdditionalSections()) {
            section.getPatterns().removeListener(sectionPatternsListener);
        }
        currentWindow.getAdditionalSections().removeListener(additionalSectionsListener);
        currentWindow = null;

        existingPatterns.clear();

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

    // -- current window
    /**
     * The Window the currently started session is about.
     */
    private EditorWindowModel currentWindow;
    public EditorWindowModel getCurrentWindow() { return currentWindow; }

    // -- existing patterns
    private final ObservableList<EditorPatternModel> existingPatterns = FXCollections.observableArrayList();
    /**
     * The list of existing created Patterns in the currently started Session.
     */
    private final ObservableList<EditorPatternModel> readonlyExistingPatterns = FXCollections.unmodifiableObservableList(existingPatterns);
    public ObservableList<EditorPatternModel> getExistingPatterns() { return readonlyExistingPatterns; }

    /**
     * Keeps the existing patterns list in sync with Patterns being added to or removed from a Section
     * of the current Window.
     */
    private final ListChangeListener<EditorPatternModel> sectionPatternsListener = change -> {
        while (change.next()) {
            existingPatterns.addAll(change.getAddedSubList());
            existingPatterns.removeAll(change.getRemoved());
        }
    };

    /**
     * Tracks Sections being added to or removed from the current Window, so that their Patterns are
     * added to or removed from the existing patterns list and tracked from then on.
     */
    private final ListChangeListener<EditorSectionModel> additionalSectionsListener = change -> {
        while (change.next()) {
            for (EditorSectionModel section : change.getAddedSubList()) {
                existingPatterns.addAll(section.getPatterns());
                section.getPatterns().addListener(sectionPatternsListener);
            }
            for (EditorSectionModel section : change.getRemoved()) {
                existingPatterns.removeAll(section.getPatterns());
                section.getPatterns().removeListener(sectionPatternsListener);
            }
        }
    };

    public enum SessionState {
        NOT_STARTED,
        STARTED,
        ENDING,
        ENDED
    }
}