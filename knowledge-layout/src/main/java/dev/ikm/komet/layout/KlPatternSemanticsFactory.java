package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.editor.EditorWindowBaseControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.property.KlPropertySet;

import java.util.Optional;
import java.util.UUID;

/**
 * A stateless singleton factory for a pattern's semantics. The canonical instances are discovered
 * once via {@link java.util.ServiceLoader} and shared through {@link KlPatternSemanticsFactories};
 * resolve factories from there (including by stored class name) rather than instantiating them, so
 * that every reference to a given factory is the same object.
 */
public interface KlPatternSemanticsFactory {
    PatternSemanticsPresenter createJournalControl(EditorPatternModel editorPatternModel, ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic);

    EditorWindowBaseControl createEditorControl(EditorPatternModel editorPatternModel);

    String displayName();

    /**
     * Creates a fresh set of the configurable properties unique to this factory (for example a
     * table factory's "show header" and "show grid lines"). A new instance is returned on each call
     * because the values are per-pattern state, while the factory itself is a stateless singleton.
     * The returned set shows up in the editor's properties pane, is persisted with the pattern, and
     * is bound to by the journal control at render time.
     *
     * @return a new property set for this factory, or {@link Optional#empty()} if the factory has no
     *         extra configurable properties
     */
    default Optional<KlPropertySet> createProperties() {
        return Optional.empty();
    }
}