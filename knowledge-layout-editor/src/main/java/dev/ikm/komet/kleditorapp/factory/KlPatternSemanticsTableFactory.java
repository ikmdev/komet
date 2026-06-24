package dev.ikm.komet.kleditorapp.factory;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kleditorapp.view.control.PatternTableEditorControl;
import dev.ikm.komet.kleditorapp.view.control.KlEditorWindowControlFactory;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.PatternSemanticsTablePresenter;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import dev.ikm.komet.layout.editor.property.TablePatternProperties;

import java.util.Optional;
import java.util.UUID;

public class KlPatternSemanticsTableFactory implements KlPatternSemanticsFactory {

    @Override
    public PatternSemanticsPresenter createJournalControl(EditorPatternModel editorPatternModel, ViewProperties viewProperties,
                                                          ObservableComposer composer, UUID journalTopic) {
        return new PatternSemanticsTablePresenter(editorPatternModel, viewProperties, composer);
    }

    @Override
    public PatternTableEditorControl createEditorControl(EditorPatternModel editorPatternModel) {
        return KlEditorWindowControlFactory.createTablePatternView(editorPatternModel);
    }

    @Override
    public String displayName() {
        return "Table";
    }

    @Override
    public Optional<KlPropertySet> createProperties() {
        return Optional.of(new TablePatternProperties());
    }
}