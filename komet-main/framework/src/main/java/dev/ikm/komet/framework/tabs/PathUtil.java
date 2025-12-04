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
package dev.ikm.komet.framework.tabs;

import javafx.scene.shape.*;

/**
 * @author amrullah
 */
class PathUtil {

    public static void generateTabPath(Path path, double startX, double startY, double width, double height) {
        path.getElements().clear();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(startX);
        moveTo.setY(startY);
        path.getElements().add(moveTo);//start
        path.getElements().add(new HLineTo(startX + width));//path width
        path.getElements().add(new VLineTo(startY + height));//path height
        path.getElements().add(new HLineTo(startX));//path bottom left
        path.getElements().add(new VLineTo(startY));//back to start
    }

    public static void generateTabPath(Path path, double tabPos, double width, double height) {
        int tabHeight = 32;
        int start = 2;
        tabPos = Math.max(start, tabPos);
        path.getElements().clear();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(start);
        moveTo.setY(tabHeight);
        path.getElements().add(moveTo);//start

        path.getElements().add(new HLineTo(width));//path width
        path.getElements().add(new VLineTo(height));//path height
        path.getElements().add(new HLineTo(start));//path bottom left
        path.getElements().add(new VLineTo(tabHeight));//back to start

        if (tabPos > 20) {
            path.getElements().add(new MoveTo(tabPos, tabHeight + 5));
            path.getElements().add(new LineTo(Math.max(start, tabPos - 10), tabHeight + 15));
            path.getElements().add(new HLineTo(tabPos + 10));
            path.getElements().add(new LineTo(tabPos, tabHeight + 5));
        } else {
            double tip = Math.max(tabPos, start + 5);
            path.getElements().add(new MoveTo(tip, tabHeight + 5));
            path.getElements().add(new LineTo(tip + 10, tabHeight + 5));
            path.getElements().add(new LineTo(tip, tabHeight + 15));
            path.getElements().add(new VLineTo(tabHeight + 5));
        }
    }
}