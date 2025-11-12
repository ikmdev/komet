package dev.ikm.komet.kview.klauthoring.editable.componentsetfield;

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

public class KlEditableComponentSetField extends BaseDefaultKlField<IntIdSet> implements KlComponentSetField {

    /**
     * Creates an editable component set field.
     * @param observableComponentSetField the observable field
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableComponentSetField(ObservableField<IntIdSet> observableComponentSetField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLComponentCollectionControl node = KLComponentControlFactory.createTypeAheadComponentListControl(observableView.calculator());
        super(observableComponentSetField, observableView, stamp4field, node);
        node.setTitle(getTitle());
        node.valueProperty().bindBidirectional(observableComponentSetField.editableValueProperty());

    }

}
