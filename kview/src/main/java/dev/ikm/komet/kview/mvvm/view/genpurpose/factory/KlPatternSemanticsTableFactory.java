package dev.ikm.komet.kview.mvvm.view.genpurpose.factory;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternSemanticsPresenter;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.PatternSemanticsTablePresenter;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;

import java.util.UUID;

public class KlPatternSemanticsTableFactory implements KlPatternSemanticsFactory {

    @Override
    public PatternSemanticsPresenter create(EditorPatternModel editorPatternModel, ViewProperties viewProperties,
                                            ObservableComposer composer, UUID journalTopic) {
        return new PatternSemanticsTablePresenter(editorPatternModel, viewProperties, composer, journalTopic);
    }
}