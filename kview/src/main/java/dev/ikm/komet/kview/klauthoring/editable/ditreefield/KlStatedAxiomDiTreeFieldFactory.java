package dev.ikm.komet.kview.klauthoring.editable.ditreefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.klauthoring.readonly.ditreefield.KlReadOnlyDiTreeField;
import dev.ikm.komet.layout.version.field.KlDirectedTreeField;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;

/**
 * A factory class for creating instances of {@link KlStatedAxiomDiTreeField} — the inline-editing
 * axiom tree used in the window body for stated logical definitions.
 */
public class KlStatedAxiomDiTreeFieldFactory implements KlFieldFactory<DiTreeEntity> {

    /**
     * Creates an instance of KlStatedAxiomDiTreeField.
     * @param observableField The observable field containing the stated DiTreeEntity logical definition
     * @param observableView The observable view context
     * @param stamp4field The observable stamp providing versioning information
     * @return An instance of KlField&lt;DiTreeEntity&gt; rendering an inline-editing axiom tree
     */
    @Override
    public KlField<DiTreeEntity> create(ObservableField<DiTreeEntity> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlStatedAxiomDiTreeField(observableField, observableView, stamp4field);
    }

    /**
     * Axiom editing happens inline in the window body only, like the classic axiom control: in
     * edit mode (properties panel) the definition shows as the read-only axiom tree, following
     * the {@link dev.ikm.komet.kview.klauthoring.readonly.stringfield.KlReadOnlyStringFieldFactory}
     * precedent.
     */
    @Override
    public KlField<DiTreeEntity> create(ObservableField.Editable<DiTreeEntity> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlReadOnlyDiTreeField(observableFieldEditable.getObservableFeature(), observableView, stamp4field);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends KlField<DiTreeEntity>> getFieldInterface() {
        // KlDirectedTreeField is generic, so its class literal is raw and needs this cast.
        return (Class<? extends KlField<DiTreeEntity>>) (Class<?>) KlDirectedTreeField.class;
    }

    @Override
    public Class<? extends KlField<DiTreeEntity>> getFieldImplementation() {
        return KlStatedAxiomDiTreeField.class;
    }

    @Override
    public String getName() {
        return "Stated Axiom DiTree Field Factory";
    }

    @Override
    public String getDescription() {
        return "An inline-editing DiTree field factory for stated logical definitions, committing each applied edit.";
    }
}
