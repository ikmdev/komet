package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.editor.EditorWindowBaseControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;

import java.util.UUID;

public interface KlPatternSemanticsFactory {
    PatternSemanticsPresenter createJournalControl(EditorPatternModel editorPatternModel, ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic);

    EditorWindowBaseControl createEditorControl();

    String displayName();
}