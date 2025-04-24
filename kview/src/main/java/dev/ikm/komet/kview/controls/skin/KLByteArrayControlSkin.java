package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLByteArrayControl;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link KLByteArrayControl} control
 */
public class KLByteArrayControlSkin extends SkinBase<KLByteArrayControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLByteArrayControl.class);
    private static final String FILE_DATA = "file.data";
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    private final Label titleLabel;
    private final StackPane fileContainer;
    private final Label messageLabel;
    private final Button addFileButton;
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Label iconLabel;
    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.bytearray-control");

    private final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>(this, "file");
    private final BooleanProperty onProgressProperty = new SimpleBooleanProperty(this, "onProgress");

    private Task<KLByteArrayControl.FileData> task;

    /**
     * Creates a new KLByteArrayControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLByteArrayControlSkin(KLByteArrayControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("editable-title-label");

        progressBar = new ProgressBar();
        progressBar.managedProperty().bind(progressBar.visibleProperty());
        progressBar.setVisible(false);

        progressLabel = new Label();
        progressLabel.getStyleClass().add("progress-label");
        progressLabel.managedProperty().bind(progressLabel.visibleProperty());
        progressLabel.visibleProperty().bind(progressBar.visibleProperty());

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.managedProperty().bind(messageLabel.visibleProperty());
        messageLabel.visibleProperty().bind(messageLabel.textProperty().isNotEmpty());

        Region icon = new Region();
        icon.getStyleClass().add("file-icon");
        icon.managedProperty().bind(icon.visibleProperty());
        icon.visibleProperty().bind(fileProperty.isNotNull());

        iconLabel = new Label();
        iconLabel.getStyleClass().add("file-icon-label");
        iconLabel.managedProperty().bind(icon.visibleProperty());
        iconLabel.visibleProperty().bind(fileProperty.isNotNull());

        Group iconGroup = new Group(icon, iconLabel);
        iconGroup.getStyleClass().add("file-icon-group");
        iconGroup.managedProperty().bind(iconGroup.visibleProperty());
        iconGroup.visibleProperty().bind(fileProperty.isNotNull());

        Region cancelButton = new Region();
        cancelButton.getStyleClass().add("cancel-button");
        StackPane.setAlignment(cancelButton, Pos.TOP_RIGHT);
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        cancelButton.visibleProperty().bind(onProgressProperty.or(fileProperty.isNotNull()));
        cancelButton.setOnMouseClicked(e -> {
            // TODO: Add confirmation dialog?
            if (task != null && task.isRunning()) {
                LOG.debug("Cancelling file upload");
                task.cancel();
            } else if (fileProperty.get() != null) {
                LOG.debug("Removing uploaded file");
                messageLabel.setText(null);
                iconLabel.setText(null);
                fileProperty.set(null);
            }
            control.requestLayout();
        });

        fileContainer = new StackPane(progressBar, progressLabel, iconGroup, cancelButton);
        fileContainer.getStyleClass().add("file-container");
        fileContainer.managedProperty().bind(fileContainer.visibleProperty());
        fileContainer.visibleProperty().bind(onProgressProperty.or(fileProperty.isNotNull()));

        addFileButton = new Button(resources.getString("button.text"));
        addFileButton.getStyleClass().add("add-file-button");
        addFileButton.disableProperty().bind(fileProperty.isNotNull());
        addFileButton.setOnAction(e -> {
            if (onProgressProperty.get()) {
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(resources.getString("files.title"));
            control.getFileExtensions().stream()
                    .map(fe -> new FileChooser.ExtensionFilter(fe.name(), fe.extensions()))
                    .forEach(ef -> fileChooser.getExtensionFilters().add(ef));
            File uploadFile = fileChooser.showOpenDialog(addFileButton.getScene().getWindow());
            LOG.debug("Upload file: {}", uploadFile);
            if (uploadFile != null) {
                messageLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
                task = uploadFile(uploadFile.toPath(), getSkinnable().isShowUploadProgress());
                onProgressProperty.bind(task.runningProperty());
                if (getSkinnable().isShowUploadProgress()) {
                    progressBar.setVisible(true);
                    progressBar.progressProperty().bind(task.progressProperty());
                    progressLabel.textProperty().bind(Bindings.format("%.1f%%", task.progressProperty().multiply(100)));
                }
                messageLabel.textProperty().bind(task.messageProperty());
                task.setOnCancelled(ev -> {
                    LOG.debug("Task was cancelled");
                    onProgressProperty.unbind();
                    progressBar.progressProperty().unbind();
                    progressBar.setVisible(false);
                    progressLabel.textProperty().unbind();
                    messageLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
                    fileProperty.set(null);
                    iconLabel.setText(null);
                    getSkinnable().getProperties().put(FILE_DATA, null);
                });
                task.setOnFailed(ev -> {
                    LOG.error("Task failed: {}", task.getException(), task.getException());
                    onProgressProperty.unbind();
                    progressBar.progressProperty().unbind();
                    progressBar.setVisible(false);
                    progressLabel.textProperty().unbind();
                    messageLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
                    fileProperty.set(null);
                    iconLabel.setText(null);
                    getSkinnable().getProperties().put(FILE_DATA, null);
                });
                task.setOnSucceeded(ev -> {
                    LOG.debug("Task succeeded, length of uploaded file: {}", task.getValue() != null ? task.getValue().size() : 0);
                    onProgressProperty.unbind();
                    progressBar.progressProperty().unbind();
                    progressBar.setVisible(false);
                    progressLabel.textProperty().unbind();
                    messageLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, task.getValue() == null);
                    messageLabel.textProperty().unbind();
                    if (task.getValue() != null) {
                        String name = uploadFile.getName();
                        getSkinnable().getProperties().put(FILE_DATA, task.getValue());
                        fileProperty.set(uploadFile);
                        messageLabel.setText(name);
                        iconLabel.setText(getFileNameExtension(name).orElse(null));
                    }
                });
                LOG.debug("Start upload task");
                new Thread(task).start();
            }
        });
        getChildren().addAll(titleLabel, fileContainer, messageLabel, addFileButton);
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        double spacing = 5;
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;
        if (fileContainer.isVisible()) {
            double containerHeight = fileContainer.prefHeight(-1);
            fileContainer.resizeRelocate(x, y, contentWidth, containerHeight);
            y += containerHeight + spacing;
        }
        double buttonPrefWidth = addFileButton.prefWidth(-1);
        double buttonPrefHeight = addFileButton.prefHeight(buttonPrefWidth);
        double buttonX = contentWidth - buttonPrefWidth - padding.getRight();
        if (messageLabel.isVisible()) {
            double messageWidth = messageLabel.prefWidth(-1);
            double labelWidth = Math.min(messageWidth, buttonX - spacing);
            double messageHeight = messageLabel.prefHeight(labelWidth);
            messageLabel.resizeRelocate(x, y, labelWidth, messageHeight);
        }
        addFileButton.resizeRelocate(buttonX, y, buttonPrefWidth, buttonPrefHeight);
    }

    private Task<KLByteArrayControl.FileData> uploadFile(Path fileSource, boolean showProgress) {
        return new Task<>() {
            @Override
            protected KLByteArrayControl.FileData call() throws Exception {
                LOG.debug("Uploading file: {}", fileSource);
                if (!Files.exists(Objects.requireNonNull(fileSource))) {
                    LOG.error("Error, file: {} doesn't exist", fileSource);
                    updateMessage("fileSource does not exist");
                    return null;
                }

                long size;
                try {
                    size = Files.size(fileSource);
                    LOG.debug("File size: {} MB", toMB(size));
                    if (size > getSkinnable().getMaxFileSize()) {
                        LOG.error("Error: file size {} is too big", toMB(size));
                        updateMessage(MessageFormat.format(resources.getString("error.max.size.text"),
                                toMB(size), toMB(getSkinnable().getMaxFileSize())));
                        return null;
                    }
                } catch (IOException ex) {
                    LOG.error("Error finding file size: {}", fileSource, ex);
                    updateMessage(MessageFormat.format(resources.getString("error.io.text"), ex.getMessage()));
                    return null;
                }

                byte[] byteArray;
                updateProgress(0, size);
                LOG.debug("Start file upload");
                updateMessage(MessageFormat.format(resources.getString("uploading.file.text"), fileSource.getFileName()));
                try (FileInputStream fis = new FileInputStream(fileSource.toFile());
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int length;
                    long accum = 0L;
                    while ((length = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, length);
                        accum += length;
                        updateProgress(accum, size);
                        if (task.isCancelled()) {
                            updateMessage(resources.getString("error.cancel.text"));
                            return null;
                        } else if (showProgress) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                        }
                    }
                    byteArray = bos.toByteArray();
                    LOG.debug("File upload done successfully");
                    return new KLByteArrayControl.FileData(fileSource.getFileName().toString(), byteArray, size);
                } catch (Exception e) {
                    LOG.error("Error uploading file: {}", fileSource, e);
                    updateMessage(MessageFormat.format(resources.getString("error.io.text"), e.getMessage()));
                }
                return null;
            }
        };
    }

    private static String toMB(long sizeInBytes) {
        return String.format("%.2f", ((double) (sizeInBytes) / (1024d * 1024d)));
    }

    private static Optional<String> getFileNameExtension(String fileName) {
        int lastIndex = Objects.requireNonNull(fileName).lastIndexOf('.');
        if (lastIndex == -1) {
            return Optional.empty();
        }
        String extension = fileName.substring(lastIndex + 1);
        return Optional.of(extension.toUpperCase(Locale.ROOT));
    }

}
