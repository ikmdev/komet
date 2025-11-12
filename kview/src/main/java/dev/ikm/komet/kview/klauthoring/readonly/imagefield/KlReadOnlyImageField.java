package dev.ikm.komet.kview.klauthoring.readonly.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableField.Editable;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.kview.klfields.KlFieldHelper;

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
public class KlReadOnlyImageField extends BaseDefaultKlField<byte[]> {

    private final Editable<byte[]> editableField;
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
    public KlReadOnlyImageField(
            Editable<byte[]> editableField,
            ObservableView observableView,
            ObservableStamp stamp4field) {

        final KLReadOnlyImageControl node = new KLReadOnlyImageControl();

        super(editableField.getObservableFeature(), observableView, stamp4field, node);
        this.editableField = editableField;
        node.setTitle(getTitle());
        setupReadOnlyBinding(editableField.getObservableFeature(), node);
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
     * @deprecated Use {@link #KlReadOnlyImageField(Editable, ObservableView, ObservableStamp)}
     *             with ObservableField.Editable for transaction management
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public KlReadOnlyImageField(ObservableField<byte[]> observableImageField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLReadOnlyImageControl node = new KLReadOnlyImageControl();

        super(observableImageField, observableView, stamp4field, node);
        this.editableField = null;
        node.setTitle(getTitle());
        setupReadOnlyBinding(editableField.getObservableFeature(), node);
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
    public Editable<byte[]> getEditableField() {
        return editableField;
    }

    /**
     * Returns whether this field is using the editable pattern.
     */
    public boolean isUsingEditablePattern() {
        return editableField != null;
    }
}