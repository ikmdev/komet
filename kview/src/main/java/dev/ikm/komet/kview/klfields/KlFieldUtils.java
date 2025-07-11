package dev.ikm.komet.kview.klfields;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KlFieldUtils {
    public static Image newImageFromByteArray(byte[] imageByteArray) {
        // If the image is blank or empty then return null
        if(imageByteArray.length == 0){
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
        Image image = new Image(bis);
        return image;
    }

    public static byte[] newByteArrayFromImage(Image image) {
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