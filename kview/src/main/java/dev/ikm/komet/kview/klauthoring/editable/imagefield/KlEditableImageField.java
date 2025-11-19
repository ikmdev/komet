package dev.ikm.komet.kview.klauthoring.editable.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import javafx.util.Subscription;

import java.io.ByteArrayOutputStream;

/**
 * Image field implementation supporting both read-only and editable patterns.
 * <p>
 * <b>Editable Pattern (Recommended):</b> Use with {@link Editable} for
 * transaction management, dirty tracking, and save/commit/rollback capabilities.
 * <p>
 * <b>Legacy Pattern:</b> Use with {@link ObservableField} for immediate write-through.
 * <p>
 * When using the editable pattern, changes are cached in the {@link Editable}
 * and do not persist to the database until the parent {@link dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable}
 * is saved and committed via {@link dev.ikm.komet.framework.observable.ObservableComposer}.
 */
public class KlEditableImageField extends BaseDefaultKlField<byte[]> {

    private boolean isUpdatingImageControl = false;
    private boolean isUpdatingProperty = false;

    /**
     * Constructor using the editable pattern (recommended).
     * <p>
     * Provides transaction management, dirty tracking, and cached editing.
     * Changes do not persist until the editable version is saved and committed.
     *
     * @param observableFieldEditable the editable field from an ObservableSemanticVersion.Editable
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public KlEditableImageField(
            Editable<byte[]> observableFieldEditable,
            ObservableView observableView,
            ObservableStamp stamp4field) {

        final KLImageControl node = new KLImageControl();

        super(observableFieldEditable, observableView, stamp4field, node);
        node.setTitle(getTitle());

        rebind(observableFieldEditable);
    }

    /**
     * Sets up bidirectional binding between editable field and image control.
     * Uses the ObservableField.Editable pattern for cached editing.
     * @deprecated Use rebind() method instead.
     */
    private void setupEditableBinding(
            Editable<byte[]> editableField,
            KLImageControl imageControl) {

        // Editable field → Image control
        Subscription editableSub = editableField.editableValueProperty().subscribe(newByteArray -> {
            if (isUpdatingProperty) {
                return;
            }
            isUpdatingImageControl = true;
            imageControl.setImage(KlFieldHelper.newImageFromByteArray(newByteArray));
            isUpdatingImageControl = false;
        });
        getFieldEditableSubscriptions().add(editableSub);

        // Image control → Editable field
        Subscription imageSub = imageControl.imageProperty().subscribe(() -> {
            if (isUpdatingImageControl) {
                return;
            }
            isUpdatingProperty = true;

            byte[] newByteArray = imageControl.getImage() == null
                ? new ByteArrayOutputStream().toByteArray()
                : KlFieldHelper.newByteArrayFromImage(imageControl.getImage());

            editableField.setValue(newByteArray);
            isUpdatingProperty = false;
        });
        getFieldEditableSubscriptions().add(imageSub);

        // Set initial value
        imageControl.setImage(KlFieldHelper.newImageFromByteArray(editableField.getValue()));
    }

    @Override
    public void rebind(ObservableField.Editable<byte[]> newFieldEditable) {
        KLImageControl imageControl = (KLImageControl) fxObject();
        // if already the same ignore.
        replaceObservableFieldEditable(newFieldEditable);


        // Add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty().
        doOnEditableValuePropertyChange(newValueOpt ->
                newValueOpt.ifPresent(newValue -> {
                    if (isUpdatingProperty) {
                        return;
                    }
                    isUpdatingImageControl = true;
                    imageControl.setImage(KlFieldHelper.newImageFromByteArray(newValue));
                    isUpdatingImageControl = false;
                })
        );
        // Image control → Editable field
        Subscription imageSub = imageControl.imageProperty().subscribe(() -> {
            if (isUpdatingImageControl) {
                return;
            }
            isUpdatingProperty = true;

            byte[] newByteArray = imageControl.getImage() == null
                    ? new ByteArrayOutputStream().toByteArray()
                    : KlFieldHelper.newByteArrayFromImage(imageControl.getImage());

            fieldEditable().setValue(newByteArray);
            isUpdatingProperty = false;
        });
        getFieldEditableSubscriptions().add(imageSub);
        // Set initial value
        imageControl.setImage(KlFieldHelper.newImageFromByteArray(newFieldEditable.getValue()));
    }
}