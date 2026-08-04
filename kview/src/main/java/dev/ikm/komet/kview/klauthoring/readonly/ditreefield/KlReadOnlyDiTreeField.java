package dev.ikm.komet.kview.klauthoring.readonly.ditreefield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyDiTreeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlDirectedTreeField;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.scene.image.Image;

/**
 * Read-only field for {@link DiTreeEntity} values — logical definitions such as the field of the
 * EL++ stated and inferred terminological axiom patterns. Renders the definition as an axiom tree
 * ({@link KLReadOnlyDiTreeControl}) whose concepts are component chips, resolving names and
 * identicons through the field's view.
 */
public class KlReadOnlyDiTreeField extends BaseDefaultKlField<DiTreeEntity> implements KlDirectedTreeField<DiTreeEntity> {

    public KlReadOnlyDiTreeField(ObservableField<DiTreeEntity> observableDiTreeField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLReadOnlyDiTreeControl control = new KLReadOnlyDiTreeControl();
        super(observableDiTreeField, observableView, stamp4field, control);

        control.setComponentItemResolver(nid -> {
            String description = observableView.getDescriptionTextOrNid(nid);
            EntityHandle entityHandle = EntityHandle.get(nid);
            PublicId publicId = entityHandle.expectEntity().publicId();
            Image identicon = Identicon.generateIdenticonImage(publicId);
            return new ComponentItem(description, identicon, publicId, entityHandle.isConcept());
        });
        control.setDescriptionResolver(observableView::getDescriptionTextOrNid);
        control.setTitle(getTitle());
        int semanticNid = observableDiTreeField.field().nid();
        control.setRootConceptNid(EntityHandle.getSemanticOrThrow(semanticNid).referencedComponentNid());
        control.valueProperty().bind(observableDiTreeField.editableValueProperty());
    }
}