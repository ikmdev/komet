package dev.ikm.komet.kview.klfields.componentsetfield;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.layout.version.field.KlComponentSetField;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.UUID;
import java.util.function.Consumer;

public class DefaultKlComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    /**
     *
     * @param observableComponentSetField
     * @param observableView
     * @param isEditable
     * @param journalTopic This is used for the option to summon the concept window in the specific work space.
     */
    public DefaultKlComponentSetField(ObservableField<IntIdSet> observableComponentSetField, ObservableView observableView, boolean isEditable, UUID journalTopic) {
        final Region node = switch (isEditable) {
            case true -> KLComponentControlFactory
                    .createTypeAheadComponentListControl(observableView.calculator());
            case false -> new KLReadOnlyComponentSetControl();
        };
        super(observableComponentSetField, observableView, isEditable, node);
        switch (node) {
            case KLComponentCollectionControl klComponentCollectionControl -> {
                klComponentCollectionControl.setTitle(getTitle());
                klComponentCollectionControl.valueProperty().bindBidirectional(observableComponentSetField.valueProperty());
            }
            case KLReadOnlyComponentSetControl readOnlyComponentSetControl -> {
                readOnlyComponentSetControl.setTitle(getTitle());

                observableComponentSetField.valueProperty().addListener(observable ->
                        updateReadOnlyIntIdSet(readOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator()));
                updateReadOnlyIntIdSet(readOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator());
                Consumer<Integer> itemConsumer= (nid) -> {
                    EntityHandle.get(nid).ifConcept(conceptEntity -> {
                        EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeConceptWindowEvent(this,
                                MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
                    });
                };
                readOnlyComponentSetControl.setOnPopulateAction(itemConsumer);
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
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
