package dev.ikm.komet.kview.klauthoring.editable.imagefield;

import static dev.ikm.komet.kview.klfields.KlFieldHelper.newByteArrayFromImage;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForImage;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * TODO: Work in progress: finalize the bidirectional binding between Image and byte[]
 * An editable area for Image fields.
 * <p>
 * This area uses {@link KLImageControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableImageFieldArea imageArea = EditableImageFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<Image> editable = ...;
 *
 * // Connect the area to the editable
 * imageArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(imageArea.fxObject());
 * }</pre>
 */
public final class EditableImageFieldArea extends EditableFieldAreaBlueprint<byte[], KLImageControl>
        implements KlAreaForImage<KLImageControl> {
    Property<byte[]> byteArrayProperty;
    private boolean isUpdatingImageControl = false;
    private boolean isUpdatingProperty = false;
    /**
     * Constructor for restoring from preferences.
     */
    public EditableImageFieldArea(KometPreferences preferences) {
        super(preferences, new KLImageControl());
        // set imageProperty
        byteArrayProperty = getControlValueProperty();
        bindImageAndBytesBidirectional(fxObject().imageProperty(), (ObjectProperty<byte[]>) byteArrayProperty);
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableImageFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForImage.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLImageControl());
    }

    @Override
    protected void createControl() {
        KLImageControl imageControl = new KLImageControl();
        setFxPeer(imageControl);
    }

    @Override
    protected Property<byte[]> getControlValueProperty() {
        if (byteArrayProperty == null) {
            byteArrayProperty = new SimpleObjectProperty<>();
            if (fxObject().getImage() != null) {
                byteArrayProperty.setValue(KlFieldHelper.newByteArrayFromImage(fxObject().getImage()));
            } else {
                byteArrayProperty.setValue(new byte[0]);
            }
        }
        return byteArrayProperty;
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<byte[]> editable) {
//        getFxPeer()
//                .valueProperty()
//                .bindBidirectional(editable.editableValueProperty());
//
//        addEditableSubscription(
//                editable.editableValueProperty().subscribe((oldVal, newVal) -> {
//                    if (newVal != null && !newVal.equals(oldVal)) {
//                        editable.setValue(newVal);
//                    }
//                })
//        );getFxPeer()
//                .valueProperty()
//                .bindBidirectional(editable.editableValueProperty());
//
//        addEditableSubscription(
//                editable.editableValueProperty().subscribe((oldVal, newVal) -> {
//                    if (newVal != null && !newVal.equals(oldVal)) {
//                        editable.setValue(newVal);
//                    }
//                })
//        );
    }

    @Override
    protected void unbindControlFromEditable() {
        if (getEditable() != null) {
            byteArrayProperty.unbindBidirectional(getEditable().editableValueProperty());
        }
    }

//    public void rebind(ObservableField.Editable<byte[]> newFieldEditable) {
//        KLImageControl imageControl = (KLImageControl) fxObject();
//        // if already the same ignore.
//        replaceObservableFieldEditable(newFieldEditable);
//
//
//        // Add new subscription (change listeners on property changes)
//        // based on fieldEditable().editableValueProperty().
//        doOnEditableValuePropertyChange((_, newValueOpt) ->
//                newValueOpt.ifPresent(newValue -> {
//                    if (isUpdatingProperty) {
//                        return;
//                    }
//                    isUpdatingImageControl = true;
//                    imageControl.setImage(KlFieldHelper.newImageFromByteArray(newValue));
//                    isUpdatingImageControl = false;
//                })
//        );
//        // Image control → Editable field
//        Subscription imageSub = imageControl.imageProperty().subscribe(() -> {
//            if (isUpdatingImageControl) {
//                return;
//            }
//            isUpdatingProperty = true;
//
//            byte[] newByteArray = imageControl.getImage() == null
//                    ? new ByteArrayOutputStream().toByteArray()
//                    : KlFieldHelper.newByteArrayFromImage(imageControl.getImage());
//
//            fieldEditable().setValue(newByteArray);
//            isUpdatingProperty = false;
//        });
//        getFieldEditableSubscriptions().add(imageSub);
//        // Set initial value
//        imageControl.setImage(KlFieldHelper.newImageFromByteArray(newFieldEditable.getValue()));
//    }
    @Override
    protected void updateControlTitle(String title) {
        getFxPeer().setTitle(title);
    }

    // --- Factory Methods ---

    public static Factory factory() {
        return new Factory();
    }

    public static EditableImageFieldArea restore(KometPreferences preferences) {
        return EditableImageFieldArea.factory().restore(preferences);
    }

    public static EditableImageFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableImageFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableImageFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableImageFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForImage.Factory<KLImageControl> {

        public Factory() {}

        @Override
        public EditableImageFieldArea restore(KometPreferences preferences) {
            return new EditableImageFieldArea(preferences);
        }

        @Override
        public EditableImageFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableImageFieldArea area = new EditableImageFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableImageFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableImageFieldArea area = new EditableImageFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }

    // Utility method to access imageControl
    public static void bindImageAndBytesBidirectional(ObjectProperty<Image> imageProp, ObjectProperty<byte[]> bytesProp) {

        // Listener to update bytesProp when imageProp changes
        imageProp.subscribe(newVal -> {
            if (newVal == null) {
                if (bytesProp.get() == null) { // Prevent infinite loop check
                    bytesProp.set(new byte[0]);
                }
                return;
            }
            byte[] bytes = newByteArrayFromImage(newVal);
            if (!java.util.Arrays.equals(bytesProp.get(), bytes)) { // Prevent infinite loop check
                bytesProp.set(bytes);
            }
        });

        // Listener to update imageProp when bytesProp changes
        bytesProp.subscribe(newVal -> {
            if (newVal == null || newVal.length == 0) {
                if (imageProp.get() != null) { // Prevent infinite loop check
                    imageProp.set(null);
                }
                return;
            }
            // if bytesProp is null or empty, set imageProp to null
            if (!java.util.Arrays.equals(bytesProp.get(), newVal)) { // Prevent infinite loop check
                if (newVal != null) {
                    bytesProp.set(newVal);
                }
            }
            // bytesProp changed, update imageProp
            if (bytesProp.get() != null && bytesProp.get().length == 0) {
                if (imageProp.get() != null) {
                    imageProp.set(null);
                }
                return;
            }
        });
    }
}