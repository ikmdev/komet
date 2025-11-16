package dev.ikm.komet.kview.klauthoring.editable.componentsetfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlComponentSetField;
import dev.ikm.tinkar.common.id.IntIdSet;

import java.util.*;
import java.util.function.*;

public class KlEditableComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    /**
     * Creates an editable component set field.
     * @param observableComponentSetFieldEditable the observable editable field
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableComponentSetField(ObservableField.Editable<IntIdSet> observableComponentSetFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        final KLComponentCollectionControl node = KLComponentControlFactory.createTypeAheadComponentListControl(observableView.calculator());
        super(observableComponentSetFieldEditable, observableView, stamp4field, node);
        node.setTitle(getTitle());
        node.valueProperty().bindBidirectional(observableComponentSetFieldEditable.editableValueProperty());
        // Listen for changes in the control and update the observable field (is this the right way to do this?)
        observableComponentSetFieldEditable
                .editableValueProperty()
                .subscribe(newValue -> {
            if (newValue != null) {
                observableComponentSetFieldEditable.setValue(newValue);
            }
        });
    }

}
