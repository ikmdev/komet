package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.KlFieldUtils;
import javafx.scene.Parent;

import java.io.ByteArrayOutputStream;

public class DefaultKlImageField extends BaseDefaultKlField<byte[]> {

    private boolean isUpdatingImageControl = false;
    private boolean isUpdatingObservableField = false;

    public DefaultKlImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, boolean isEditable) {
        super(observableImageField, observableView, isEditable);
        Parent node;
        if (isEditable) {
            KLImageControl imageControl = new KLImageControl();
            imageControl.setTitle(getTitle());
            // Sync ObservableField and ImageControl
            observableImageField.valueProperty().subscribe(newByteArray -> {
                if (isUpdatingObservableField) {
                    return;
                }

                isUpdatingImageControl = true;
                imageControl.setImage(KlFieldUtils.newImageFromByteArray(newByteArray));
                isUpdatingImageControl = false;
            });
            imageControl.imageProperty().subscribe(() -> {
                if (isUpdatingImageControl) {
                    return;
                }

                isUpdatingObservableField = true;
                if (imageControl.getImage() == null) {
                    //set the field value to empty byte array since we cannot save null value to database.
                    field().valueProperty().set(new ByteArrayOutputStream().toByteArray());
                } else {
                    byte[] newByteArray = KlFieldUtils.newByteArrayFromImage(imageControl.getImage());
                    field().valueProperty().set(newByteArray);
                }
                isUpdatingObservableField = false;
            });

            // Set Editable Control to be the node
            node = imageControl;
        } else {
            KLReadOnlyImageControl readOnlyImageControl = new KLReadOnlyImageControl();
            readOnlyImageControl.setTitle(getTitle());
            // Sync ObservableField and ReadOnlyImageControl
            byte[] imageBytes = observableImageField.value();
            readOnlyImageControl.setValue(KlFieldUtils.newImageFromByteArray(imageBytes));
            observableImageField.valueProperty().subscribe(newByteArray -> {
                readOnlyImageControl.setValue(KlFieldUtils.newImageFromByteArray(newByteArray));
            });
            // Title
            readOnlyImageControl.setTitle(getTitle());
            node = readOnlyImageControl;
        }
        setKlWidget(node);
    }

}