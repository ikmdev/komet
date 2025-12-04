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
package dev.ikm.komet.kview.fxutils;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * A class containing utility methods for common ComboBox creation / setup
 */
public class ComboBoxHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ComboBoxHelper.class);
    private static final String DEFAULT_CHECK_MARK_ICON_REGION = "check-mark";

    /**
     * set up a comboBox for ConceptEntities
     * @param comboBox combo box for concepEntities
     * @param listener invalidation listener
     * @param displayText a function that applies how to render the display text
     * @param <T> type ConceptEntity
     */
    public static <T> void setupComboBox(ComboBox comboBox, InvalidationListener listener, Function<T, String> displayText) {
        comboBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T conceptEntity) {
                return displayText.apply(conceptEntity);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(new Callback<>() {

            /**
             * @param param The single argument upon which the returned value should be
             *              determined.
             * @return
             */
            @Override
            public ListCell<T> call(Object param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(T conceptEntity, boolean b) {
                        super.updateItem(conceptEntity, b);
                        if (conceptEntity != null) {
                            setText(displayText.apply(conceptEntity));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        // register invalidation listener
        comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    /**
     * style a comboBox with a custom graphic icon
     * @param comboBox comboBox we are setting up
     */
    public static <T> void setupComboBoxWithCheckMarkIcon(ComboBox<T> comboBox, Function<T, String> displayText) {
        setupComboBoxWithIcon(comboBox, displayText, DEFAULT_CHECK_MARK_ICON_REGION);
    }

    /**
     * style a comboBox with a custom graphic icon
     * @param comboBox comboBox we are setting up
     * @param iconStyleClass A style class representing a Region of an svg for '-fx-shape' attribute.
     */
    public static <T> void setupComboBoxWithIcon(ComboBox<T> comboBox, Function<T, String> displayText, String iconStyleClass) {
        comboBox.setConverter(new StringConverter<T>() {

            @Override
            public String toString(T domainObject) {
                return displayText.apply(domainObject);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });

        // workaround to hide scroll bar. Odd thing happens on the initial popup a vertical scrollbar appears.
        // TODO: might be another way to fix by using CSS to fix this issue or examine height of popup (ListView).
        Platform.runLater(() -> {
            final Node scrollBar = comboBox.lookup(".scroll-bar:vertical");
            scrollBar.setStyle("-fx-pref-width: 0; -fx-padding: 0;");
            scrollBar.applyCss();
            scrollBar.setVisible(false);
            scrollBar.setOpacity(0);
            scrollBar.setScaleX(0);
        });

        // Code to create a HBox with label and region using an SVG shape.
        comboBox.setCellFactory(lv -> {
            final ListCell<T> cell = new ListCell<>() {
                @Override
                public void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item != null ? displayText.apply(item) : null);

                    // Create an HBox (Label, Region)
                    // set List Cell's graphic as the HBox
                    if (!empty) {
                        if (this.isSelected()) {
                            this.getStyleClass().add("check-mark-selected");

                            // Label of text horizontal grow
                            Label contentLabel = new Label(displayText.apply(item));
                            contentLabel.setTextFill(Color.WHITE);
                            HBox.setHgrow(contentLabel, Priority.ALWAYS);
                            contentLabel.setMaxWidth(Double.MAX_VALUE);

                            // Create a check mark graphic
                            Region iconGraphic = new Region();
                            iconGraphic.getStyleClass().add(iconStyleClass);
                            HBox customListCell = new HBox(contentLabel, iconGraphic);
                            setGraphic(customListCell);
                        } else {
                            this.getStyleClass().remove("check-mark-selected");
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            };
            return cell;
        });
    }
}
