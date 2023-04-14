package dev.ikm.komet.framework.graphics;

import javafx.scene.text.Font;

public class LoadFonts {
    public static boolean load() {
        Font.loadFont(getFontUrl("OpenSans-Bold.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-BoldItalic.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-ExtraBold.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-ExtraBoldItalic.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-Italic.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-Light.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-LightItalic.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-Regular.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-SemiBold.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSans-SemiBoldItalic.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSansCondensed-Bold.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSansCondensed-Light.ttf"), 10);
        Font.loadFont(getFontUrl("OpenSansCondensed-LightItalic.ttf"), 10);
        return true;
    }

    public static String getFontUrl(String file) {
        return LoadFonts.class.getResource("/dev/ikm/komet/framework/graphics/fonts/" + file).toString();
    }}
