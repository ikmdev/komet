package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.layout.component.version.field.KlBooleanField;
public class DefaultBooleanControl extends KLBooleanControl implements KlBooleanField {

    @Override
    public ObservableField field() {
        return null;
    }
    public boolean isEditable() {
        return false;
    }
}
