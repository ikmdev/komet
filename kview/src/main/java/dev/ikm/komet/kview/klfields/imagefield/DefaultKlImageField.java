package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlImageField;
import javafx.scene.Parent;
import javafx.scene.image.Image;

public class DefaultKlImageField extends BaseDefaultKlField<Image> implements KlImageField {
    // TODO: For now we are using only one instance of each Image Data type control so we can bind them together
    private static KLImageControl imageControl = new KLImageControl();
    private static KLReadOnlyImageControl readOnlyImageControl = new KLReadOnlyImageControl();

    public DefaultKlImageField(ObservableField<Image> observableFloatField, ObservableView observableView, boolean isEditable) {
        super(observableFloatField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            imageControl.setTitle("Image"); //TODO: for now the title is hardcoded but we need to get it from the ObservableField

            node = imageControl;
        } else {
            readOnlyImageControl.setTitle("Image"); //TODO: for now the title is hardcoded but we need to get it from the ObservableField
            readOnlyImageControl.imageFileProperty().bind(imageControl.imageFileProperty()); //TODO: this should later be bound to the ObservableField

            node = readOnlyImageControl;
        }
        setKlWidget(node);
    }
}