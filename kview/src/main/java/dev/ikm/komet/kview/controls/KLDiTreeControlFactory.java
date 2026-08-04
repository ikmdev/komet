package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import javafx.scene.image.Image;

/**
 * Creates {@link KLDiTreeControl}s wired to the rest of the app — the counterpart of
 * {@link KLComponentControlFactory} for the editable axiom tree. The control itself is a pure UI
 * control; controls obtained here resolve concept names and identicons through the given view
 * calculator, and produce inline search slots backed by
 * {@link KLComponentControlFactory#createComponentControl(ViewCalculator)}.
 */
public final class KLDiTreeControlFactory {

    private KLDiTreeControlFactory() {
    }

    /**
     * Creates an editable axiom tree control fully wired against the given view.
     *
     * @param viewCalculator the view to resolve names, identicons and type-ahead search against
     * @return the wired control
     */
    public static KLDiTreeControl create(ViewCalculator viewCalculator) {
        KLDiTreeControl control = new KLDiTreeControl();
        control.setComponentItemResolver(nid -> {
            String description = viewCalculator.getDescriptionTextOrNid(nid);
            EntityHandle entityHandle = EntityHandle.get(nid);
            PublicId publicId = entityHandle.expectEntity().publicId();
            Image identicon = Identicon.generateIdenticonImage(publicId);
            return new ComponentItem(description, identicon, publicId, entityHandle.isConcept());
        });
        control.setDescriptionResolver(viewCalculator::getDescriptionTextOrNid);
        control.setComponentSlotFactory(() -> KLComponentControlFactory.createComponentControl(viewCalculator));
        // Seeds for newly added property sets, mirroring the classic axiom control's
        // AddPropertySet / AddDataPropertySet / AddIntervalPropertySet actions
        // (SnomedIds.concept_model_object_attribute / concept_model_data_attribute).
        control.setPropertySetSeedNid(PrimitiveData.nid(UuidUtil.fromSNOMED("762705008")));
        control.setDataPropertySetSeedNid(PrimitiveData.nid(UuidUtil.fromSNOMED("762706009")));
        return control;
    }
}