package dev.ikm.komet.kview.klauthoring.editable.componentlistfield;

import static dev.ikm.komet.kview.controls.KLComponentControlFactory.createComponentListControl;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;

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
public class KlEditableComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {


    /**
     * Constructor using the editable pattern (recommended).
     * <p>
     * Provides transaction management, dirty tracking, and cached editing.
     * Changes do not persist until the editable version is saved and committed.
     *
     * @param observableFieldEditable the editable field from an ObservableSemanticVersion.Editable
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableComponentListField(
            Editable<IntIdList> observableFieldEditable,
            ObservableView observableView,
            ObservableStamp stamp4field) {

        KLComponentCollectionControl node = createComponentListControl(observableView.calculator());
        super(observableFieldEditable, observableView, stamp4field, node);
        node.setTitle(getTitle());
        //setupEditableBinding(editableField, node);
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(node.valueProperty(), observableFieldEditable);

        // set title
        node.setTitle(getTitle());
    }

    /**
     * Unbinds UI control's and the prior ObservableField.Editable. Next, rebinds and updates
     * properties with new JavaFX subscriptions (change listeners).
     *
     * @param newFieldEditable A new ObservableField.Editable instance.
     */
    @Override
    public void rebind(ObservableField.Editable<IntIdList> newFieldEditable) {
        // Obtain UI control
        KLComponentCollectionControl uiControl = (KLComponentCollectionControl) fxObject();

        // Unbind both directions editValueProperty <-> uiControl.entityValueProperty
        // Assign new observable field and unsubscribe all previous subscriptions.
        // Rebind bi-directionally UI Control <-> editableValueProperty.
        // Re-add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty()
        // bi directionally bind editable UI control to the Editable.editableValueProperty().
        rebindValueProperty(uiControl.valueProperty(), newFieldEditable);
    }
}