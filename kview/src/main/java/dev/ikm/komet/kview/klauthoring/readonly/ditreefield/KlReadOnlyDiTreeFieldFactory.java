package dev.ikm.komet.kview.klauthoring.readonly.ditreefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlDirectedTreeField;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;

/**
 * A factory class for creating instances of {@link KlReadOnlyDiTreeField}.
 */
public class KlReadOnlyDiTreeFieldFactory implements KlFieldFactory<DiTreeEntity> {

    /**
     * Creates an instance of KlReadOnlyDiTreeField.
     * @param observableField The observable field containing the DiTreeEntity logical definition
     * @param observableView The observable view context
     * @param stamp4field The observable stamp providing versioning information
     * @return An instance of KlField&lt;DiTreeEntity&gt;
     */
    @Override
    public KlField<DiTreeEntity> create(ObservableField<DiTreeEntity> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlReadOnlyDiTreeField(observableField, observableView, stamp4field);
    }

    /**
     * Axiom editing is not supported yet: in edit mode the field is shown as the same read-only
     * axiom tree, following the precedent of
     * {@link dev.ikm.komet.kview.klauthoring.readonly.stringfield.KlReadOnlyStringFieldFactory}.
     * See {@link dev.ikm.komet.kview.klfields.KlFieldHelper}.
     * @param observableFieldEditable - observable editable object.
     * @param observableView view coordinates.
     * @param stamp4field current stamp if any.
     * @return KlField representing a read-only axiom tree for screens attempting to display the
     *         field in edit mode.
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
        return KlReadOnlyDiTreeField.class;
    }

    @Override
    public String getName() {
        return "Read-Only DiTree Field Factory";
    }

    @Override
    public String getDescription() {
        return "A read-only DiTree field factory for displaying logical definitions as an axiom tree.";
    }
}