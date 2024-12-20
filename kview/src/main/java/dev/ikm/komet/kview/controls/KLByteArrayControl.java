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

/**
 * <p>KLByteArrayControl allows file attachments. Users can select a file from their file system
 * and upload it to Komet, providing the file matches any of the allowed file extensions set via
 * {@link #fileExtensionsProperty()}, which already include a default set of valid ones, and its
 * size fits within the maximum size allowed by {@link #maxFileSizeProperty()}.
 * </p>
 * <p>Then the file will be saved as byte array, along with its
 * name and size in a {@link FileData} record, and available via {@link #fileDataProperty()}.
 * </p>
 * <p>The progress of the file upload can be monitored enabling {@link #showUploadProgressProperty()},
 * and the upload can be cancelled at any moment, or when it ends, removing the file and its data.</p>
 *
 * <pre><code>
 * KLByteArrayControl byteArrayControl = new KLByteArrayControl();
 * byteArrayControl.setTitle("File upload");
 * byteArrayControl.setShowUploadProgress(true);
 * byteArrayControl.setPrefWidth(300);
 * byteArrayControl.fileDataProperty().subscribe(data -> {
 *     if (data != null) System.out.println("File uploaded " + data.name());
 * });
 * </code></pre>
 */
public class KLByteArrayControl extends Control {

    private static final String FILE_DATA = "file.data";
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.bytearray-control");

    /**
     * Record that stores data from an uploaded file: name, byte array and size
     * @param name a string with the original name of the file, without its path
     * @param data a byte array with the data of the file
     * @param size a long with the size of the file
     */
    public record FileData(String name, byte[] data, long size) {}

    /**
     * Record that defines a valid extension that can be added as a filter to a file browser.
     * @param name a string with the description of the extension
     * @param extensions one or more valid extensions
     */
    public record FileExtensions(String name, String... extensions) {}

    private final List<FileExtensions> defaultExtensionsList = List.of(
            new FileExtensions(resources.getString("file.extension.excel"), "*.xls", "*.xlsx"),
            new FileExtensions(resources.getString("file.extension.csv"), "*.csv"),
            new FileExtensions(resources.getString("file.extension.pdf"), "*.pdf"),
            new FileExtensions(resources.getString("file.extension.image"), "*.jpeg", "*.jpg", "*.bmp", "*.png"),
            new FileExtensions(resources.getString("file.extension.all"), "*.*")
    );

    /**
     * Creates a KLByteArrayControl
     */
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

    /**
     * A string property that sets the title of the control, if any
     */
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

    /**
     * A property with the {@link FileData data} of the uploaded file
     */
    private final ReadOnlyObjectWrapper<FileData> fileDataProperty = new ReadOnlyObjectWrapper<>(this, "fileData");
    public final ReadOnlyObjectProperty<FileData> fileDataProperty() {
        return fileDataProperty.getReadOnlyProperty();
    }
    public final FileData getFileData() {
        return fileDataProperty.get();
    }

    /**
     * A property that defines the maximum file size allowed. By default, 10 MB
     */
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

    /**
     * A property with a list of valid {@link FileExtensions}. By default, the list is initialized with
     * extensions for Excel, CSV, PDF, Image files, and also All files.
     */
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

    /**
     * Boolean property to enable visual monitoring of the upload progress, false by default.
     */
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

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLByteArrayControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLByteArrayControl.class.getResource("bytearray-control.css").toExternalForm();
    }
}
