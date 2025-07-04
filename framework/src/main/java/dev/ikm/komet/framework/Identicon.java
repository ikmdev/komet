/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework;

import com.sparrowwallet.toucan.LifeHash;
import com.sparrowwallet.toucan.LifeHashVersion;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Identicon {
    /*

    https://github.com/bryc/code/wiki/Identicons

    https://barro.github.io/2018/02/avatars-identicons-and-hash-visualization/

    https://www.puls200.de/?p=316

    https://web.archive.org/web/20200430104323/http://scott.sherrillmix.com:80/blog/blogger/wp_identicon/

    https://blog.codinghorror.com/identicons-for-net/

    http://web.archive.org/web/20070208033427/http://www.docuverse.com/blog/donpark/2004/05/03/secure-ui-9-block-phishmarks

    http://www.levitated.net/daily/lev9block.html

    https://github-wiki-see.page/m/bryc/code/wiki/Identicons

    https://github.com/davidhampgonsalves/Contact-Identicons

     */

    public static ImageView generateIdenticon(PublicId publicId, int image_width, int image_height) {

        Image identicon = generateIdenticonImage(publicId);

        ImageView finalImageView = new ImageView(identicon);
        //Scale image to the size you want
        double width = identicon.getWidth();
        double height = identicon.getHeight();

//        Affine at = new Affine();
//        Scale scale = at.scale(image_width / width, image_height / height);
//        finalImageView.getTransforms().add(scale);

        finalImageView.setFitWidth(image_width);
        finalImageView.setFitHeight(image_height);

        return finalImageView;
    }

    public static Image generateIdenticonImage(PublicId publicId) {
        return generateIdenticonImageLifeHash(publicId, LifeHashVersion.DETAILED);
    }

    public static Image generateIdenticonImageOldVersion(PublicId publicId) {
        int width = 5;
        int height = 5;

        int publicIdHash = publicId.publicIdHash();
        int redHash = (byte) Math.abs((byte) (publicIdHash >> 24));
        int greenHash = (byte) Math.abs((byte) (publicIdHash >> 16));
        int blueHash = (byte) Math.abs((byte) (publicIdHash >> 8));
        byte[] hash2 = new byte[]{(byte) Math.abs((byte) (publicIdHash >> 24)),
                (byte) Math.abs((byte) (publicIdHash >> 16)),
                (byte) Math.abs((byte) (publicIdHash >> 8))};
        redHash = redHash < 0 ? (byte) redHash & 0xff : redHash;
        greenHash = greenHash < 0 ? (byte) greenHash & 0xff : greenHash;
        blueHash = blueHash < 0 ? (byte) blueHash & 0xff : blueHash;

        WritableImage identicon = new WritableImage(width, height);
        PixelWriter raster = identicon.getPixelWriter();

        Color background = Color.rgb(255, 255, 255, 0);
        Color foreground = Color.rgb(redHash, greenHash, blueHash, 1);

        for (int x = 0; x < width; x++) {
            //Enforce horizontal symmetry
            int i = x < 3 ? x : 4 - x;
            for (int y = 0; y < height; y++) {
                Color pixelColor;
                //toggle pixels based on bit being on/off
                if ((hash2[i] >> y & 1) == 1)
                    pixelColor = foreground;
                else
                    pixelColor = background;
                raster.setColor(x, y, pixelColor);
            }
        }
        return identicon;
    }

    public static Image generateIdenticonImageLifeHash(PublicId publicId, LifeHashVersion lifeHashVersion) {
        LifeHash.Image lifeHashImage = LifeHash.makeFromUTF8(publicId.idString(), lifeHashVersion, 1, false);

        // This code creates an additional BufferedImage so the code below should be better in terms of performance
        // although more complex
//        BufferedImage awtImage = LifeHash.getBufferedImage(lifeHashImage);
//        return SwingFXUtils.toFXImage(awtImage, null);

        WritableImage writableImage = new WritableImage(lifeHashImage.width(), lifeHashImage.height());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < lifeHashImage.height(); y++) {
            for (int x = 0; x < lifeHashImage.width(); x++) {
                int offset = (y * lifeHashImage.width() + x) * (lifeHashImage.hasAlpha() ? 4 : 3);
                int r = lifeHashImage.colors().get(offset) & 0xFF;
                int g = lifeHashImage.colors().get(offset + 1) & 0xFF;
                int b = lifeHashImage.colors().get(offset + 2) & 0xFF;
                if(lifeHashImage.hasAlpha()) {
                    double a = (lifeHashImage.colors().get(offset + 3) & 0xFF) / 255.0;
                    pixelWriter.setColor(x, y, Color.rgb(r, g, b, a));
                } else {
                    pixelWriter.setColor(x, y, Color.rgb(r, g, b));
                }
            }
        }
        return writableImage;
    }

    // use 10 x 10 squares...

    Shape shape1(double x, double y, Color fill) {
        Rectangle rectangle = new Rectangle(x, y, 10, 5);
        rectangle.setFill(fill);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    Shape shape2(double x, double y, Color fill) {
        Rectangle rectangle = new Rectangle(x, y, 10, 10);
        rectangle.setFill(fill);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    Shape shape3(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 0.0,
                x + 10.0, y + 10.0,
                x + 10.0, y + 0.0,
                x + 0.0, y + 0.0,
        });
        return polygon;
    }

    Shape shape4(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 5.0,
                x + 10.0, y + 0.0,
                x + 10.0, y + 10.0,
                x + 0.0, y + 5.0,
        });
        return polygon;
    }

    Shape shape5(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 5.0,
                x + 5.0, y + 0.0,
                x + 10.0, y + 5.0,
                x + 5.0, y + 10.0,
                x + 0.0, y + 5.0,
        });
        return polygon;
    }

    Shape shape6(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 0.0,
                x + 10.0, y + 5.0,
                x + 10.0, y + 10.0,
                x + 5.0, y + 10.0,
                x + 0.0, y + 0.0,
        });
        return polygon;
    }

    Group shape7(double x, double y, Color fill) {
        Polygon polygon1 = new Polygon();
        polygon1.setFill(fill);
        polygon1.setStroke(Color.BLACK);
        polygon1.getPoints().addAll(new Double[]{
                x + 0.0, y + 5.0,
                x + 5.0, y + 2.5,
                x + 5.0, y + 7.5,
                x + 0.0, y + 5.0,
        });

        Polygon polygon2 = new Polygon();
        polygon2.setFill(fill);
        polygon2.setStroke(Color.BLACK);
        polygon2.getPoints().addAll(new Double[]{
                x + 5.0, y + 2.5,
                x + 10.0, y + 0,
                x + 10, y + 5,
                x + 5.0, y + 2.5,
        });

        Polygon polygon3 = new Polygon();
        polygon3.setFill(fill);
        polygon3.setStroke(Color.BLACK);
        polygon3.getPoints().addAll(new Double[]{
                x + 5.0, y + 7.5,
                x + 10.0, y + 5.0,
                x + 10, y + 10.0,
                x + 5.0, y + 7.5,
        });
        Group group = new Group(polygon1, polygon2, polygon3);

        return group;
    }

    Shape shape8(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 0.0,
                x + 10.0, y + 5.0,
                x + 5.0, y + 10.0,
                x + 0.0, y + 0.0,
        });
        return polygon;
    }

    Shape shape9(double x, double y, Color fill) {
        Rectangle rectangle = new Rectangle(x + 2.5, y + 2.5, 5, 5);
        rectangle.setFill(fill);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    Group shape10(double x, double y, Color fill) {
        Polygon polygon1 = new Polygon();
        polygon1.setFill(fill);
        polygon1.setStroke(Color.BLACK);
        polygon1.getPoints().addAll(new Double[]{
                x + 5.0, y + 0.0,
                x + 10.0, y + 0.0,
                x + 5.0, y + 5.0,
                x + 5.0, y + 0.0,
        });

        Polygon polygon2 = new Polygon();
        polygon2.setFill(fill);
        polygon2.setStroke(Color.BLACK);
        polygon2.getPoints().addAll(new Double[]{
                x + 0.0, y + 5.0,
                x + 5.0, y + 5.0,
                x + 10.0, y + 0.0,
                x + 0.0, y + 5.0,
        });
        Group group = new Group(polygon1, polygon2);
        return group;
    }

    Shape shape11(double x, double y, Color fill) {
        Rectangle rectangle = new Rectangle(x, y, 5, 5);
        rectangle.setFill(fill);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    Shape shape12(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 0.0,
                x + 5.0, y + 5.0,
                x + 10.0, y + 0.0,
                x + 0.0, y + 0.0,
        });
        return polygon;
    }

    Shape shape13(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 5.0, y + 5.0,
                x + 10.0, y + 0.0,
                x + 10.0, y + 10.0,
                x + 5.0, y + 5.0,
        });
        return polygon;
    }

    Shape shape14(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 5.0, y + 0.0,
                x + 5.0, y + 5.0,
                x + 0.0, y + 5.0,
                x + 5.0, y + 0.0,
        });
        return polygon;
    }

    Shape shape15(double x, double y, Color fill) {
        Polygon polygon = new Polygon();
        polygon.setFill(fill);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(new Double[]{
                x + 0.0, y + 0.0,
                x + 5.0, y + 0.0,
                x + 0.0, y + 5.0,
                x + 0.0, y + 0.0,
        });
        return polygon;
    }

}
