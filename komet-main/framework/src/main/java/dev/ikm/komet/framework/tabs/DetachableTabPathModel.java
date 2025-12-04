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

import javafx.scene.shape.Path;

/**
 * @author amrullah
 */
class DetachableTabPathModel {

    private final Path path;
    private double tabPos;
    private double width;
    private double height;
    private double startX;
    private double startY;

    public DetachableTabPathModel(Path path) {
        this.path = path;
        this.path.getStyleClass().add("drop-path");
    }

    void refresh(double startX, double startY, double width, double height) {
        boolean regenerate = this.tabPos != -1
                || this.width != width
                || this.height != height
                || this.startX != startX
                || this.startY != startY;
        this.tabPos = -1;
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        if (regenerate) {
            PathUtil.generateTabPath(path, startX + 2, startY + 2, width - 4, height - 4);
        }
    }

    void refresh(double tabPos, double width, double height) {
        boolean regenerate = this.tabPos != tabPos
                || this.width != width
                || this.height != height;
        this.tabPos = tabPos;
        this.width = width;
        this.height = height;
        startX = 0;
        startY = 0;
        if (regenerate) {
            PathUtil.generateTabPath(path, tabPos, width - 2, height - 2);
        }
    }
}