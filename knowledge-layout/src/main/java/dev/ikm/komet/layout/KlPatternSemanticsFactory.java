package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;

import java.util.UUID;

public interface KlPatternSemanticsFactory {
    PatternSemanticsPresenter create(EditorPatternModel editorPatternModel, ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic);

    String displayName();
}