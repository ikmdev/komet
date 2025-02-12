package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class DefaultKlImageField extends BaseDefaultKlField<byte[]> {

    public DefaultKlImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, boolean isEditable) {
        super(observableImageField, observableView, isEditable);

        Node node;
        if (isEditable) {
            KLImageControl imageControl = new KLImageControl();

            byte[] imageBytes = observableImageField.value();

            // Convert byte array to Image and set it on the control
            imageControl.setImage(newImageFromByteArray(imageBytes));

            node = imageControl;
        } else {
            KLReadOnlyImageControl readOnlyImageControl = new KLReadOnlyImageControl();

            byte[] imageBytes = observableImageField.value(); // Your byte array here

            // Convert byte array to Image and set it on the control
            readOnlyImageControl.setImage(newImageFromByteArray(imageBytes));
            observableImageField.valueProperty().subscribe(newByteArray -> {
                readOnlyImageControl.setImage(newImageFromByteArray(imageBytes));
            });

            // Title
            readOnlyImageControl.setTitle(getTitle());

            node = readOnlyImageControl;
        }
        setKlWidget(node);
    }

    private Image newImageFromByteArray(byte[] imageByteArray) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
        Image image = new Image(bis);
        return image;
    }
}