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
package dev.ikm.komet.framework.controls;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TextAreaReadOnly extends TextArea {
    private static final Logger LOG = LoggerFactory.getLogger(TextAreaReadOnly.class);

    TextAreaSkinNoScroller textAreaSkinNoScroller;

    public TextAreaReadOnly() {
        setSkin(new TextAreaSkinNoScroller(this));
        this.textAreaSkinNoScroller = (TextAreaSkinNoScroller) this.getSkin();
        this.textAreaSkinNoScroller.getContentView().heightProperty().addListener((observable, oldValue, newValue) -> {
            setTheHeight(newValue.doubleValue());
        });
        setEditable(false);
        setWrapText(true);
        setPrefRowCount(4);

        this.textProperty().addListener((observable, oldValue, newValue) -> {
            double height = textAreaSkinNoScroller.computePrefHeight(getWidth());
            setTheHeight(height);
        });

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            double height = textAreaSkinNoScroller.computePrefHeight(newValue.doubleValue());
            setTheHeight(height);
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            selectRange(0, 0);
        });

        addEventFilter(
                MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() > 2)
                            LOG.info("ClickCount: " + event.getClickCount());
                    }
                }
        );
    }

    private void setTheHeight(Double height) {
        setPrefHeight(height);
        setMaxHeight(height);
        setHeight(height);
    }


}
