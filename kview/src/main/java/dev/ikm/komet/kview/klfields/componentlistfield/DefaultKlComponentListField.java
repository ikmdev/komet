package dev.ikm.komet.kview.klfields.componentlistfield;

import static dev.ikm.komet.kview.controls.KLComponentControlFactory.createTypeAheadComponentListControl;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.layout.version.field.KlComponentListField;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Component list field implementation supporting both read-only and editable patterns.
 * <p>
 * <b>Editable Pattern (Recommended):</b> Use with {@link ObservableField.Editable} for
 * transaction management, dirty tracking, and save/commit/rollback capabilities.
 * <p>
 * <b>Legacy Pattern:</b> Use with {@link ObservableField} for immediate write-through.
 * <p>
 * When using the editable pattern, changes are cached in the {@link ObservableField.Editable}
 * and do not persist to the database until the parent {@link dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable}
 * is saved and committed via {@link dev.ikm.komet.framework.observable.ObservableComposer}.
 */
public class DefaultKlComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {

    private final ObservableField.Editable<IntIdList> editableField;

    /**
     * Constructor using the editable pattern (recommended).
     * <p>
     * Provides transaction management, dirty tracking, and cached editing.
     * Changes do not persist until the editable version is saved and committed.
     *
     * @param editableField the editable field from an ObservableSemanticVersion.Editable
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     * @param journalTopic used for summoning the concept window in the specific workspace
     */
    public DefaultKlComponentListField(
            ObservableField.Editable<IntIdList> editableField,
            ObservableView observableView,
            ObservableStamp stamp4field,
            UUID journalTopic) {

        Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> createTypeAheadComponentListControl(observableView.calculator());
            case false -> new KLReadOnlyComponentListControl();
        };

        super(editableField.getObservableFeature(), observableView, stamp4field, node);
        this.editableField = editableField;

        switch (node) {
            case KLComponentCollectionControl klComponentCollectionControl -> {
                klComponentCollectionControl.setTitle(getTitle());
                setupEditableBinding(editableField, klComponentCollectionControl);
            }
            case KLReadOnlyComponentListControl klReadOnlyComponentListControl -> {
                klReadOnlyComponentListControl.setTitle(getTitle());
                setupReadOnlyBinding(editableField.getObservableFeature(), klReadOnlyComponentListControl, observableView, journalTopic);
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

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
     * @deprecated Use {@link #DefaultKlComponentListField(ObservableField.Editable, ObservableView, ObservableStamp, UUID)}
     *             with ObservableField.Editable for transaction management
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public DefaultKlComponentListField(
            ObservableField<IntIdList> observableComponentListField,
            ObservableView observableView,
            ObservableStamp stamp4field,
            UUID journalTopic) {

        Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> createTypeAheadComponentListControl(observableView.calculator());
            case false -> new KLReadOnlyComponentListControl();
        };

        super(observableComponentListField, observableView, stamp4field, node);
        this.editableField = null;

        switch (node) {
            case KLComponentCollectionControl klComponentCollectionControl -> {
                klComponentCollectionControl.setTitle(getTitle());
                setupLegacyBinding(observableComponentListField, klComponentCollectionControl);
            }
            case KLReadOnlyComponentListControl klReadOnlyComponentListControl -> {
                klReadOnlyComponentListControl.setTitle(getTitle());
                setupReadOnlyBinding(observableComponentListField, klReadOnlyComponentListControl, observableView, journalTopic);
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    /**
     * Sets up bidirectional binding between editable field and component list control.
     * Uses the ObservableField.Editable pattern for cached editing.
     */
    private void setupEditableBinding(
            ObservableField.Editable<IntIdList> editableField,
            KLComponentCollectionControl control) {

        // Bind control to editable field's editable property (cached changes)
        control.valueProperty().bindBidirectional(editableField.editableValueProperty());
    }

    /**
     * Sets up bidirectional binding using legacy pattern.
     * Changes write through immediately to ObservableField via editableValueProperty().
     * Display reads from the read-only valueProperty().
     */
    private void setupLegacyBinding(
            ObservableField<IntIdList> observableField,
            KLComponentCollectionControl control) {

        // Bind control to observable field's editable property (immediate DB write)
        control.valueProperty().bindBidirectional(observableField.editableValueProperty());
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
        observableField.valueProperty().subscribe(intIdSet -> {
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

    /**
     * Returns the editable field if using the editable pattern.
     *
     * @return the editable field, or null if using legacy pattern
     */
    public ObservableField.Editable<IntIdList> getEditableField() {
        return editableField;
    }

    /**
     * Returns whether this field is using the editable pattern.
     */
    public boolean isUsingEditablePattern() {
        return editableField != null;
    }
}
