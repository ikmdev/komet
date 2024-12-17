package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLByteArrayControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.List;
import java.util.ResourceBundle;

public class KLByteArrayControl extends Control {

    private static final String FILE_DATA = "file.data";
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.bytearray-control");

    public record FileData(String name, byte[] data, long size) {}

    public record FileExtensions(String name, String... extensions) {}

    private final List<FileExtensions> defaultExtensionsList = List.of(
            new FileExtensions(resources.getString("file.extension.excel"), "*.xls", "*.xlsx"),
            new FileExtensions(resources.getString("file.extension.csv"), "*.csv"),
            new FileExtensions(resources.getString("file.extension.pdf"), "*.pdf"),
            new FileExtensions(resources.getString("file.extension.image"), "*.jpeg", "*.jpg", "*.bmp", "*.png"),
            new FileExtensions(resources.getString("file.extension.all"), "*.*")
    );

    public KLByteArrayControl() {
        getStyleClass().add("bytearray-control");
        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded() && FILE_DATA.equals(change.getKey())) {
                if (change.getValueAdded() instanceof FileData value) {
                    fileDataProperty.set(value);
                }
                getProperties().remove(FILE_DATA);
            }
        });
    }

    // titleProperty
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
        return titleProperty;
    }
    public final String getTitle() {
        return titleProperty.get();
    }
    public final void setTitle(String value) {
        titleProperty.set(value);
    }

    // fileDataProperty
    private final ReadOnlyObjectWrapper<FileData> fileDataProperty = new ReadOnlyObjectWrapper<>(this, "fileData");
    public final ReadOnlyObjectProperty<FileData> fileDataProperty() {
        return fileDataProperty.getReadOnlyProperty();
    }
    public final FileData getFileData() {
        return fileDataProperty.get();
    }

    // maxFileSizeProperty
    private final LongProperty maxFileSizeProperty = new SimpleLongProperty(this, "maxFileSize", 5 * 1024 * 1024);
    public final LongProperty maxFileSizeProperty() {
        return maxFileSizeProperty;
    }
    public final long getMaxFileSize() {
        return maxFileSizeProperty.get();
    }
    public final void setMaxFileSize(long value) {
        maxFileSizeProperty.set(value);
    }

    // fileExtensionsProperty
    private final ObjectProperty<List<FileExtensions>> fileExtensionsProperty = new SimpleObjectProperty<>(this, "fileExtensions", defaultExtensionsList);
    public final ObjectProperty<List<FileExtensions>> fileExtensionsProperty() {
        return fileExtensionsProperty;
    }
    public final List<FileExtensions> getFileExtensions() {
        return fileExtensionsProperty.get();
    }
    public final void setFileExtensions(List<FileExtensions> value) {
        fileExtensionsProperty.set(value);
    }

    // showUploadProgressProperty
    private final BooleanProperty showUploadProgressProperty = new SimpleBooleanProperty(this, "showUploadProgress");
    public final BooleanProperty showUploadProgressProperty() {
       return showUploadProgressProperty;
    }
    public final boolean isShowUploadProgress() {
       return showUploadProgressProperty.get();
    }
    public final void setShowUploadProgress(boolean value) {
        showUploadProgressProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLByteArrayControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLByteArrayControl.class.getResource("bytearray-control.css").toExternalForm();
    }
}
