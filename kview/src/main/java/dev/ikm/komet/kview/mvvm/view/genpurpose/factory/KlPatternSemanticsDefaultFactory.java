package dev.ikm.komet.kview.mvvm.view.genpurpose.factory;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternSemanticsDefaultPresenter;
import dev.ikm.komet.kview.mvvm.view.genpurpose.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.UUID;

public class KlPatternSemanticsDefaultFactory implements KlPatternSemanticsFactory {

    @Override
    public PatternSemanticsPresenter create(EditorPatternModel editorPatternModel, ViewProperties viewProperties,
                                            ObservableComposer composer, UUID journalTopic) {
        return new PatternSemanticsDefaultPresenter(editorPatternModel, viewProperties, composer, journalTopic);
    }
}