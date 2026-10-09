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
package dev.ikm.komet.progress;


import dev.ikm.komet.framework.concurrent.TaskWrapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.TaskProgressView;
import org.kordamp.ikonli.javafx.FontIcon;

public class ProgressViewSkin<T extends Task<?>> extends
        SkinBase<TaskProgressView<T>> {

    public ProgressViewSkin(TaskProgressView<T> monitor) {
        super(monitor);

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("box");

        // list view
        ListView<T> listView = new ListView<>();
        listView.setPrefSize(500, 400);
        listView.setPlaceholder(new Label("No tasks running"));
        listView.setCellFactory(param -> new ProgressViewSkin.TaskCell());
        listView.setFocusTraversable(false);

        Bindings.bindContent(listView.getItems(), monitor.getTasks());
        borderPane.setCenter(listView);

        getChildren().add(listView);
    }

    class TaskCell extends ListCell<T> {
        private ProgressBar progressBar;
        private Label titleText;
        private Label messageText;
        /** The task's time: elapsed, and about how long remains when the task can say. */
        private Label timeText;
        /** Refreshes the time once a second while the task runs; the task pushes nothing for it. */
        private final Timeline ticker = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> tick()));
        private final ChangeListener<Boolean> stopWhenDone = (running, was, isRunning) -> {
            if (!isRunning) {
                ticker.stop();
                tick();
            }
        };
        private Button cancelButton;

        private T task;
        private BorderPane borderPane;

        public TaskCell() {
            titleText = new Label();
            titleText.getStyleClass().add("task-title");

            messageText = new Label();
            messageText.getStyleClass().add("task-message");

            timeText = new Label();
            timeText.getStyleClass().add("task-message");
            timeText.setMinWidth(Region.USE_PREF_SIZE);
            timeText.setTooltip(new Tooltip());
            // The title gives way before the time does: a truncated file name is in the tooltip,
            // a truncated estimate is not.
            titleText.setMinWidth(0);
            titleText.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titleText, Priority.ALWAYS);
            ticker.setCycleCount(Animation.INDEFINITE);

            progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setMaxHeight(8);
            progressBar.getStyleClass().add("task-progress-bar");


            FontIcon icon = new FontIcon();
            icon.setIconLiteral("mdi2c-cancel:16:#52646d");
            icon.setId("cancel-task-icon");
            cancelButton = new Button("", icon);
            cancelButton.getStyleClass().add("task-cancel-button");
            cancelButton.setTooltip(new Tooltip("Cancel Task"));
            cancelButton.setOnAction(evt -> {
                if (task != null) {
                    task.cancel();
                }
            });

            VBox vbox = new VBox();
            vbox.setSpacing(4);
            vbox.getChildren().add(new HBox(8, titleText, timeText));
            vbox.getChildren().add(progressBar);
            vbox.getChildren().add(messageText);

            BorderPane.setAlignment(cancelButton, Pos.CENTER);
            BorderPane.setMargin(cancelButton, new Insets(0, 0, 0, 4));

            borderPane = new BorderPane();
            borderPane.setCenter(vbox);
            borderPane.setRight(cancelButton);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        /** Shows the task's time in the title line's corner, "0:22 · ~6 min left", the sentence as its tooltip. */
        private void tick() {
            if (task instanceof TaskWrapper<?> wrapper) {
                timeText.setText(wrapper.trackingCallable().timeTextCompact());
                timeText.getTooltip().setText(wrapper.trackingCallable().timeText());
            } else {
                timeText.setText("");
            }
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);

            /*
             * I have no idea why this is necessary but it won't work without
             * it. Shouldn't the updateItem method be enough?
             */
            if (index == -1) {
                setGraphic(null);
                getStyleClass().setAll("task-list-cell-empty");
            }
        }

        @Override
        protected void updateItem(T task, boolean empty) {
            super.updateItem(task, empty);

            if (this.task != null) {
                this.task.runningProperty().removeListener(stopWhenDone);
            }
            ticker.stop();
            this.task = task;

            if (empty || task == null) {
                timeText.setText("");
                getStyleClass().setAll("task-list-cell-empty");
                setGraphic(null);
            } else if (task != null) {
                getStyleClass().setAll("task-list-cell");
                progressBar.progressProperty().bind(task.progressProperty());
                titleText.textProperty().bind(task.titleProperty());
                messageText.textProperty().bind(task.messageProperty());
                cancelButton.disableProperty().bind(
                        Bindings.not(task.runningProperty()));
                tick();
                if (task instanceof TaskWrapper<?>) {
                    task.runningProperty().addListener(stopWhenDone);
                    if (task.isRunning()) {
                        ticker.play();
                    }
                }

                Callback<T, Node> factory = getSkinnable().getGraphicFactory();
                if (factory != null) {
                    Node graphic = factory.call(task);
                    if (graphic != null) {
                        BorderPane.setAlignment(graphic, Pos.CENTER);
                        BorderPane.setMargin(graphic, new Insets(0, 4, 0, 0));
                        borderPane.setLeft(graphic);
                    }
                } else {
                    /*
                     * Really needed. The application might have used a graphic
                     * factory before and then disabled it. In this case the border
                     * pane might still have an old graphic in the left position.
                     */
                    borderPane.setLeft(null);
                }

                setGraphic(borderPane);
            }
        }
    }
}
