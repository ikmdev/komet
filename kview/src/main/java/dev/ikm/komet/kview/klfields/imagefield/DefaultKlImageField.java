package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import javafx.scene.layout.Region;

import java.io.ByteArrayOutputStream;

/**
 * Image field implementation supporting both read-only and editable patterns.
 * <p>
 * <b>Editable Pattern (Recommended):</b> Use with {@link ObservableField.Editable} for
 * transaction management, dirty tracking, and save/commit/rollback capabilities.
 * <p>
 * <b>Legacy Pattern:</b> Use with {@link ObservableField} for immediate write-through.
 * <p>
 * When using the editable pattern, changes are cached in the {@link ObservableField.Editable}
 * and do not persist to the database until the parent {@link dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable}
 * is saved and committed via {@link dev.ikm.komet.framework.observable.ObservableComposer}.
 */
public class DefaultKlImageField extends BaseDefaultKlField<byte[]> {

    private final ObservableField.Editable<byte[]> editableField;
    private boolean isUpdatingImageControl = false;
    private boolean isUpdatingProperty = false;

    /**
     * Constructor using the editable pattern (recommended).
     * <p>
     * Provides transaction management, dirty tracking, and cached editing.
     * Changes do not persist until the editable version is saved and committed.
     *
     * @param editableField the editable field from an ObservableSemanticVersion.Editable
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     */
    public DefaultKlImageField(
            ObservableField.Editable<byte[]> editableField,
            ObservableView observableView,
            ObservableStamp stamp4field) {

        final Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> new KLImageControl();
            case false -> new KLReadOnlyImageControl();
        };

        super(editableField.getObservableFeature(), observableView, stamp4field, node);
        this.editableField = editableField;

        switch (node) {
            case KLImageControl imageControl -> {
                imageControl.setTitle(getTitle());
                setupEditableBinding(editableField, imageControl);
            }
            case KLReadOnlyImageControl readOnlyImageControl -> {
                readOnlyImageControl.setTitle(getTitle());
                setupReadOnlyBinding(editableField.getObservableFeature(), readOnlyImageControl);
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    /**
     * Constructor using the legacy pattern (for backward compatibility).
     * <p>
     * Changes write through immediately to the ObservableField.
     * Use the editable constructor for better transaction management.
     *
     * @param observableImageField the observable field
     * @param observableView the view context
     * @param stamp4field the stamp for UI state determination
     * @deprecated Use {@link #DefaultKlImageField(ObservableField.Editable, ObservableView, ObservableStamp)}
     *             with ObservableField.Editable for transaction management
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public DefaultKlImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, ObservableStamp stamp4field) {
        final Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> new KLImageControl();
            case false -> new KLReadOnlyImageControl();
        };
        super(observableImageField, observableView, stamp4field, node);
        this.editableField = null;

        switch (node) {
            case KLImageControl imageControl -> {
                imageControl.setTitle(getTitle());
                setupLegacyBinding(observableImageField, imageControl);
            }
            case KLReadOnlyImageControl readOnlyImageControl -> {
                readOnlyImageControl.setTitle(getTitle());
                setupReadOnlyBinding(observableImageField, readOnlyImageControl);
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    /**
     * Sets up bidirectional binding between editable field and image control.
     * Uses the ObservableField.Editable pattern for cached editing.
     */
    private void setupEditableBinding(
            ObservableField.Editable<byte[]> editableField,
            KLImageControl imageControl) {

        // Editable field → Image control
        editableField.editableValueProperty().subscribe(newByteArray -> {
            if (isUpdatingProperty) {
                return;
            }
            isUpdatingImageControl = true;
            imageControl.setImage(KlFieldHelper.newImageFromByteArray(newByteArray));
            isUpdatingImageControl = false;
        });

        // Image control → Editable field
        imageControl.imageProperty().subscribe(() -> {
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

        // Set initial value
        imageControl.setImage(KlFieldHelper.newImageFromByteArray(editableField.getValue()));
    }

    /**
     * Sets up bidirectional binding using legacy pattern.
     * Changes write through immediately to ObservableField via editableValueProperty().
     * Display reads from the read-only valueProperty().
     */
    private void setupLegacyBinding(
            ObservableField<byte[]> observableField,
            KLImageControl imageControl) {

        // Observable field (read-only) → Image control
        observableField.valueProperty().subscribe(newByteArray -> {
            if (isUpdatingProperty) {
                return;
            }
            isUpdatingImageControl = true;
            imageControl.setImage(KlFieldHelper.newImageFromByteArray(newByteArray));
            isUpdatingImageControl = false;
        });

        // Image control → Observable field (write to editable property, triggers DB write)
        imageControl.imageProperty().subscribe(() -> {
            if (isUpdatingImageControl) {
                return;
            }
            isUpdatingProperty = true;

            byte[] newByteArray = imageControl.getImage() == null
                ? new ByteArrayOutputStream().toByteArray()
                : KlFieldHelper.newByteArrayFromImage(imageControl.getImage());

            // Write to editableValueProperty which triggers database write
            observableField.editableValueProperty().set(newByteArray);
            isUpdatingProperty = false;
        });

        // Set initial value from read-only property
        imageControl.setImage(KlFieldHelper.newImageFromByteArray(observableField.value()));
    }

    /**
     * Sets up one-way binding for read-only controls.
     */
    private void setupReadOnlyBinding(
            ObservableField<byte[]> observableField,
            KLReadOnlyImageControl readOnlyImageControl) {

        // Observable field → Read-only control
        observableField.valueProperty().subscribe(newByteArray -> {
            readOnlyImageControl.setValue(KlFieldHelper.newImageFromByteArray(newByteArray));
        });

        // Set initial value
        byte[] imageBytes = observableField.value();
        readOnlyImageControl.setValue(KlFieldHelper.newImageFromByteArray(imageBytes));
    }

    /**
     * Returns the editable field if using the editable pattern.
     *
     * @return the editable field, or null if using legacy pattern
     */
    public ObservableField.Editable<byte[]> getEditableField() {
        return editableField;
    }

    /**
     * Returns whether this field is using the editable pattern.
     */
    public boolean isUsingEditablePattern() {
        return editableField != null;
    }
}