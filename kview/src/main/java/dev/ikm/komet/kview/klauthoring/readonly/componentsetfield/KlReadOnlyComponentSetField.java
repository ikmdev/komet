package dev.ikm.komet.kview.klauthoring.readonly.componentsetfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.*;
import java.util.function.*;

public class KlReadOnlyComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    /**
     * Creates a read-only component set field.
     * @param observableComponentSetField the observable field
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     * @param journalTopic used for summoning the concept window in the specific workspace
     */
    public KlReadOnlyComponentSetField(ObservableField<IntIdSet> observableComponentSetField, ObservableView observableView, ObservableStamp stamp4field, UUID journalTopic) {
        final KLReadOnlyComponentSetControl node = new KLReadOnlyComponentSetControl();
        super(observableComponentSetField, observableView, stamp4field, node);
        node.setTitle(getTitle());

        observableComponentSetField.editableValueProperty().addListener(observable ->
                updateReadOnlyIntIdSet(node, observableComponentSetField.editableValueProperty().get(), observableView.calculator()));

        updateReadOnlyIntIdSet(node, observableComponentSetField.valueProperty().get(), observableView.calculator());
        Consumer<Integer> itemConsumer= (nid) -> {
            EntityHandle.get(nid).ifConcept(conceptEntity -> {
                EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeConceptWindowEvent(this,
                        MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
            });
        };
        node.setOnPopulateAction(itemConsumer);
    }

    private void updateReadOnlyIntIdSet(KLReadOnlyComponentSetControl klReadOnlyComponentSetControl, IntIdSet newIntIdSet,
                                        ViewCalculator viewCalculator) {
        klReadOnlyComponentSetControl.getItems().clear();
        newIntIdSet.forEach(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

            String description = viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());

            ComponentItem componentItem = new ComponentItem(description, icon, nid);
            klReadOnlyComponentSetControl.getItems().add(componentItem);
        });
    }
}
