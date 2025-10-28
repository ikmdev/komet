package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

import java.io.ByteArrayOutputStream;

public class DefaultKlImageField extends BaseDefaultKlField<byte[]> {

    private boolean isUpdatingImageControl = false;
    private boolean isUpdatingObservableField = false;

    public DefaultKlImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, boolean isEditable) {
        final Region node = switch (isEditable) {
            case true -> new KLImageControl();
            case false -> new KLReadOnlyImageControl();
        };
        super(observableImageField, observableView, isEditable, node);

        switch (node) {
            case KLImageControl imageControl -> {
                imageControl.setTitle(getTitle());
                // Sync ObservableField and ImageControl
                observableImageField.valueProperty().subscribe(newByteArray -> {
                    if (isUpdatingObservableField) {
                        return;
                    }

                    isUpdatingImageControl = true;
                    imageControl.setImage(KlFieldHelper.newImageFromByteArray(newByteArray));
                    isUpdatingImageControl = false;
                });
                imageControl.imageProperty().subscribe(() -> {
                    if (isUpdatingImageControl) {
                        return;
                    }

                    isUpdatingObservableField = true;
                    if (imageControl.getImage() == null) {
                        //set the field value to empty byte array since we cannot save null value to database.
                        field().valueProperty().set(new ByteArrayOutputStream().toByteArray());
                    } else {
                        byte[] newByteArray = KlFieldHelper.newByteArrayFromImage(imageControl.getImage());
                        field().valueProperty().set(newByteArray);
                    }
                    isUpdatingObservableField = false;
                });            }
            case KLReadOnlyImageControl readOnlyImageControl -> {
                readOnlyImageControl.setTitle(getTitle());
                // Sync ObservableField and ReadOnlyImageControl
                byte[] imageBytes = observableImageField.value();
                readOnlyImageControl.setValue(KlFieldHelper.newImageFromByteArray(imageBytes));
                observableImageField.valueProperty().subscribe(newByteArray -> {
                    readOnlyImageControl.setValue(KlFieldHelper.newImageFromByteArray(newByteArray));
                });
                // Title
                readOnlyImageControl.setTitle(getTitle());
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

}