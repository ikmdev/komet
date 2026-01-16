package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.komet.layout.component.version.field.KlImageField;
import javafx.scene.image.Image;

public class KlImageFieldFactory implements KlFieldFactory<byte[]> {

    @Override
    public KlField<byte[]> create(ObservableField<byte[]> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlImageField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<byte[]>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<byte[]>> getFieldImplementation() {
        return DefaultKlImageField.class;
    }

    @Override
    public String getName() {
        return "Image Field Factory";
    }

    @Override
    public String getDescription() {
        return "An Image field";
    }
}