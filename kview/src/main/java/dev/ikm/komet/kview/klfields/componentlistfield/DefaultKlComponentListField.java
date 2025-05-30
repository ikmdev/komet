package dev.ikm.komet.kview.klfields.componentlistfield;

import static dev.ikm.komet.kview.controls.KLComponentControlFactory.createTypeAheadComponentListControl;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.image.Image;

import java.util.function.Consumer;

public class DefaultKlComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {

    public DefaultKlComponentListField(ObservableField<IntIdList> observableComponentListField, ObservableView observableView, boolean isEditable) {
        super(observableComponentListField, observableView, isEditable);
        Parent node;
        ObjectProperty<IntIdList> observableProperty = observableComponentListField.valueProperty();
        if (isEditable) {
            KLComponentListControl<IntIdList> klComponentListControl = createTypeAheadComponentListControl(observableView.calculator());

            klComponentListControl.setTitle(getTitle());
            klComponentListControl.valueProperty().bindBidirectional(observableProperty);

            node = klComponentListControl;
        } else {
            KLReadOnlyComponentListControl klReadOnlyComponentListControl = new KLReadOnlyComponentListControl();

            klReadOnlyComponentListControl.setTitle(getTitle());

            observableComponentListField.valueProperty().subscribe(intIdSet -> {
                klReadOnlyComponentListControl.getItems().clear();
                intIdSet.forEach(nid -> {
                    if (nid != 0) {
                        EntityProxy entityProxy = EntityProxy.make(nid);
                        Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

                        String description = observableView.calculator().languageCalculator()
                                .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());

                        ComponentItem componentItem = new ComponentItem(description, icon, nid);
                        klReadOnlyComponentListControl.getItems().add(componentItem);
                    }
                });
            });
            Consumer<Integer> itemConsumer= (nid) -> {
                EntityFacade entityFacade = EntityService.get().getEntityFast(nid);
                if (entityFacade instanceof ConceptEntity conceptEntity) {
                    EvtBusFactory.getDefaultEvtBus().publish(JOURNAL_TOPIC, new MakeConceptWindowEvent(this,
                            MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
                }
            };
            klReadOnlyComponentListControl.setOnPopulateAction(itemConsumer);

            node = klReadOnlyComponentListControl;
        }
        setKlWidget(node);
    }
}
