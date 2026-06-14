package dev.ikm.komet.kleditorapp.factory;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.PatternSemanticsStandardPresenter;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.EditorWindowBaseControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;

import java.util.UUID;

public class KlPatternSemanticsStandardFactory implements KlPatternSemanticsFactory {

    @Override
    public PatternSemanticsPresenter createJournalControl(EditorPatternModel editorPatternModel, ViewProperties viewProperties,
                                                          ObservableComposer composer, UUID journalTopic) {
        return new PatternSemanticsStandardPresenter(editorPatternModel, viewProperties, composer, journalTopic);
    }

    @Override
    public EditorWindowBaseControl createEditorControl() {
        return null;
    }

    @Override
    public String displayName() {
        return "Standard";
    }
}