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
