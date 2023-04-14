/*
 * Copyright (C) 2015 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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