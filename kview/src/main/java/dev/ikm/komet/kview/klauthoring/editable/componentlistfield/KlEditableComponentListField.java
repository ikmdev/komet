package dev.ikm.komet.kview.klauthoring.editable.componentlistfield;

import static dev.ikm.komet.kview.controls.KLComponentControlFactory.createTypeAheadComponentListControl;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentListField;
import dev.ikm.tinkar.common.id.IntIdList;
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
public class KlEditableComponentListField extends BaseDefaultKlField<IntIdList> implements KlComponentListField {


    /**
     * Constructor using the editable pattern (recommended).
     * <p>
     * Provides transaction management, dirty tracking, and cached editing.
     * Changes do not persist until the editable version is saved and committed.
     *
     * @param editableField the editable field from an ObservableSemanticVersion.Editable
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableComponentListField(
            Editable<IntIdList> editableField,
            ObservableView observableView,
            ObservableStamp stamp4field) {

        KLComponentCollectionControl node = createTypeAheadComponentListControl(observableView.calculator());
        super(editableField, observableView, stamp4field, node);
        node.setTitle(getTitle());
        setupEditableBinding(editableField, node);
    }
    /**
     * Sets up bidirectional binding between editable field and component list control.
     * Uses the ObservableField.Editable pattern for cached editing.
     */
    private void setupEditableBinding(
            Editable<IntIdList> editableField,
            KLComponentCollectionControl control) {

        // Bind control to editable field's editable property (cached changes)
        control.valueProperty().bindBidirectional(editableField.editableValueProperty());
        editableField
                .editableValueProperty()
                .subscribe(newValue -> {
            if (newValue != null) {
                editableField.setValue(newValue);
            }
        });
    }
}
