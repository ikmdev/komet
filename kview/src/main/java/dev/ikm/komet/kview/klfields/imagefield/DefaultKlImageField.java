package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
                imageControl.setImage(newImageFromByteArray(newByteArray));
                isUpdatingImageControl = false;
            });
            imageControl.imageProperty().subscribe(newImage -> {
                if (isUpdatingImageControl) {
                    return;
                }

                isUpdatingObservableField = true;
                if (newImage == null) {
                    //set the field value to empty byte array since we cannot save null value to database.
                    field().valueProperty().set(new ByteArrayOutputStream().toByteArray());
                    return;
                }
                byte[] newByteArray = newByteArrayFromImage(newImage);
                field().valueProperty().set(newByteArray);
                isUpdatingObservableField = false;
            });

            // Set Editable Control to be the node
            node = imageControl;
        } else {
            KLReadOnlyImageControl readOnlyImageControl = new KLReadOnlyImageControl();
            readOnlyImageControl.setTitle(getTitle());
            // Sync ObservableField and ReadOnlyImageControl
            byte[] imageBytes = observableImageField.value();
            readOnlyImageControl.setValue(newImageFromByteArray(imageBytes));
            observableImageField.valueProperty().subscribe(newByteArray -> {
                readOnlyImageControl.setValue(newImageFromByteArray(newByteArray));
            });
            // Title
            readOnlyImageControl.setTitle(getTitle());
            node = readOnlyImageControl;
        }
        setKlWidget(node);
    }

    private Image newImageFromByteArray(byte[] imageByteArray) {
        //if the image is blank or empty i.e then return null image. This will show prompt image.
        if(imageByteArray.length == 0){
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
        Image image = new Image(bis);
        return image;
    }

    private byte[] newByteArrayFromImage(Image image) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }
}