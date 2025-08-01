package dev.ikm.komet.kview.klfields.componentsetfield;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.Parent;
import javafx.scene.image.Image;

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
        super(observableComponentSetField, observableView, isEditable);
        Parent node;
        if (isEditable) {
            KLComponentCollectionControl<IntIdSet> klComponentCollectionControl = KLComponentControlFactory
                    .createTypeAheadComponentListControl(observableView.calculator());

            klComponentCollectionControl.setTitle(getTitle());
            klComponentCollectionControl.valueProperty().bindBidirectional(observableComponentSetField.valueProperty());
            node = klComponentCollectionControl;
        } else {
            KLReadOnlyComponentSetControl klReadOnlyComponentSetControl = new KLReadOnlyComponentSetControl();

            klReadOnlyComponentSetControl.setTitle(getTitle());

            observableComponentSetField.valueProperty().addListener(observable ->
                    updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator()));
            updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator());
            Consumer<Integer> itemConsumer= (nid) -> {
                EntityFacade entityFacade = EntityService.get().getEntityFast(nid);
                if (entityFacade instanceof ConceptEntity conceptEntity) {
                    EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeConceptWindowEvent(this,
                            MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
                }
            };
            klReadOnlyComponentSetControl.setOnPopulateAction(itemConsumer);

            node = klReadOnlyComponentSetControl;
        }

        setKlWidget(node);
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
