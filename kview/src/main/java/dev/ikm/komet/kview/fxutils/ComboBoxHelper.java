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

import javafx.beans.InvalidationListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;
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
    public static void setupComboBoxWithIcon(ComboBox comboBox) {
        comboBox.setCellFactory(lv -> {
            final ListCell<String> cell = new ListCell<>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item != null ? item : null);
                }
            };
            Region icon = new Region();
            Label iconLabel = new Label("", icon);
            //iconLabel.setStyle("-fx-border-color: green; -fx-border-width: 1");
            //iconLabel.setPrefWidth(400);
            icon.getStyleClass().add("icon");
            cell.setGraphic(iconLabel);
            //cell.setStyle("-fx-border-color: red");
            return cell;
        });
    }
}
