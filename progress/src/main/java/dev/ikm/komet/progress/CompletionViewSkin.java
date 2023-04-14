package dev.ikm.komet.progress;


import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.TaskProgressView;
import dev.ikm.komet.framework.concurrent.CompletedTask;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompletionViewSkin<T extends Task<?>> extends
        SkinBase<TaskProgressView<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(CompletionViewSkin.class);
    final ListView<T> listView;
    final ObservableList<T> tasks;

    public CompletionViewSkin(TaskProgressView<T> monitor, ObservableList<T> tasks) {
        super(monitor);
        this.tasks = tasks;
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("box");

        // list view
        this.listView = new ListView<>();
        listView.setPrefSize(500, 400);
        listView.setPlaceholder(new Label("No completed tasks available"));
        listView.setCellFactory(param -> new CompletionViewSkin.TaskCell());
        listView.setFocusTraversable(false);
//        listView.getItems().addListener((ListChangeListener<? super T>)
//                c -> {
//                    while (c.next()) {
//                        LOG.info("Adding: " + c.getAddedSubList());
//                    }
//                });

        Bindings.bindContent(listView.getItems(), tasks);
        borderPane.setCenter(listView);

        getChildren().add(listView);
    }

    class TaskCell extends ListCell<T> {
        private Label titleText;
        private Label messageText;
        private Label completionTime;
        private Button removeButton;

        private T task;
        private BorderPane borderPane;

        public TaskCell() {
            titleText = new Label();
            titleText.getStyleClass().add("task-title");

            messageText = new Label();
            messageText.getStyleClass().add("task-message");

            completionTime = new Label();
            completionTime.getStyleClass().add("task-message");


            FontIcon icon = new FontIcon();
            icon.setIconLiteral("mdi2c-card-remove:16:#52646d");
            icon.setId("remove-completion-node");

            removeButton = new Button("", icon);
            //
            removeButton.getStyleClass().add("task-cancel-button");
            removeButton.setTooltip(new Tooltip("Remove task from completion list"));
            removeButton.setOnAction(evt -> {
                CompletionViewSkin.this.tasks.remove(task);
            });

            VBox vbox = new VBox();
            vbox.setSpacing(4);
            vbox.getChildren().add(titleText);
            vbox.getChildren().add(messageText);
            vbox.getChildren().add(completionTime);

            BorderPane.setAlignment(removeButton, Pos.CENTER);
            BorderPane.setMargin(removeButton, new Insets(0, 0, 0, 4));

            borderPane = new BorderPane();
            borderPane.setCenter(vbox);
            borderPane.setRight(removeButton);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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

            this.task = task;

            if (empty || task == null) {
                getStyleClass().setAll("task-list-cell-empty");
                setGraphic(null);
                titleText.textProperty().unbind();
                messageText.textProperty().unbind();
                completionTime.textProperty().unbind();
                titleText.setText("");
                messageText.setText("");
                completionTime.setText("");
            } else if (task != null) {
                getStyleClass().setAll("task-list-cell");
                titleText.textProperty().bind(task.titleProperty());
                messageText.textProperty().bind(task.messageProperty());
                if (task instanceof CompletedTask completedTask) {
                    completionTime.setText(completedTask.completionTime());
                    completionTime.setVisible(true);
                } else {
                    completionTime.setVisible(false);
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
