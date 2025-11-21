package dev.ikm.komet.kview.klauthoring.readonly.componentlistfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.events.EvtBusFactory;
import javafx.scene.image.Image;

import java.util.*;
import java.util.function.*;

/**
 * Component list field implementation supporting both read-only and editable patterns.
 * <p>
 * <b>Editable Pattern (Recommended):</b> Use with {@link Editable} for
 * transaction management, dirty tracking, and save/commit/rollback capabilities.
 * <p>
 * <b>Legacy Pattern:</b> Use with {@link ObservableField} for immediate write-through.
 * <p>
 * When using the editable pattern, changes are cached in the {@link Editable}
 * and do not persist to the database until the parent {@link dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable}
 * is saved and committed via {@link dev.ikm.komet.framework.observable.ObservableComposer}.
 */
public class KlReadOnlyComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {
    /**
     * Constructor using the legacy pattern (for backward compatibility).
     * <p>
     * Changes write through immediately to the ObservableField.
     * Use the editable constructor for better transaction management.
     *
     * @param observableComponentListField list of the intIdList
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     * @param journalTopic used for summoning the concept window in the specific workspace
     */
    public KlReadOnlyComponentListField(
            ObservableField<IntIdList> observableComponentListField,
            ObservableView observableView,
            ObservableStamp stamp4field,
            UUID journalTopic) {

        KLReadOnlyComponentListControl node = new KLReadOnlyComponentListControl();
        super(observableComponentListField, observableView, stamp4field, node);
        node.setTitle(getTitle());
        setupReadOnlyBinding(observableComponentListField, node, observableView, journalTopic);
    }

    /**
     * Sets up one-way binding for read-only controls.
     */
    private void setupReadOnlyBinding(
            ObservableField<IntIdList> observableField,
            KLReadOnlyComponentListControl control,
            ObservableView observableView,
            UUID journalTopic) {

        // Observable field (read-only) → Read-only control
        observableField.editableValueProperty().subscribe(intIdSet -> {
            control.getItems().clear();
            intIdSet.forEach(nid -> {
                EntityHandle.get(nid).ifPresent(entity -> {
                    Image icon = Identicon.generateIdenticonImage(entity.publicId());

                    String description = observableView.calculator().languageCalculator()
                            .getFullyQualifiedDescriptionTextWithFallbackOrNid(entity.nid());

                    ComponentItem componentItem = new ComponentItem(description, icon, nid);
                    control.getItems().add(componentItem);
                });
            });
        });

        // Set up item click handler
        Consumer<Integer> itemConsumer = (nid) -> {
            EntityHandle itemHandle = EntityHandle.get(nid);
            itemHandle.ifConcept(conceptEntity -> {
                EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeConceptWindowEvent(this,
                        MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
            });
        };
        control.setOnPopulateAction(itemConsumer);
    }
}
