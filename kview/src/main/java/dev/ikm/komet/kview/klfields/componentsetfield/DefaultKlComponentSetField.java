package dev.ikm.komet.kview.klfields.componentsetfield;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLComponentListControl;
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

import java.util.function.Consumer;

public class DefaultKlComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    public DefaultKlComponentSetField(ObservableField<IntIdSet> observableComponentSetField, ObservableView observableView, boolean isEditable) {
        super(observableComponentSetField, observableView, isEditable);
        Parent node;
        if (isEditable) {
            KLComponentListControl<IntIdSet> klComponentListControl = KLComponentControlFactory
                    .createTypeAheadComponentListControl(observableView.calculator());

            klComponentListControl.setTitle(getTitle());
            klComponentListControl.valueProperty().bindBidirectional(observableComponentSetField.valueProperty());
            node = klComponentListControl;
        } else {
            KLReadOnlyComponentSetControl klReadOnlyComponentSetControl = new KLReadOnlyComponentSetControl();

            klReadOnlyComponentSetControl.setTitle(getTitle());

            observableComponentSetField.valueProperty().addListener(observable ->
                    updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator()));
            updateReadOnlyIntIdSet(klReadOnlyComponentSetControl, observableComponentSetField.valueProperty().get(), observableView.calculator());
            Consumer<Integer> itemConsumer= (nid) -> {
                EntityFacade entityFacade = EntityService.get().getEntityFast(nid);
                if (entityFacade instanceof ConceptEntity conceptEntity) {
                    EvtBusFactory.getDefaultEvtBus().publish(JOURNAL_TOPIC, new MakeConceptWindowEvent(this,
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
