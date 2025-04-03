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

    public DefaultKlImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, boolean isEditable) {
        super(observableImageField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLImageControl imageControl = new KLImageControl();

            byte[] imageBytes = observableImageField.value();

            imageControl.setTitle(getTitle());

            // Sync ObservableField and ImageControl
            imageControl.setImage(newImageFromByteArray(imageBytes));
            imageControl.imageProperty().subscribe(newImage -> {
                if (newImage == null) {
                    return;
                }

                byte[] newByteArray = newByteArrayFromImage(newImage);
                field().valueProperty().set(newByteArray);
            });

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