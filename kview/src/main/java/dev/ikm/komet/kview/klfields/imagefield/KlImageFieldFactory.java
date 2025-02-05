package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.komet.layout.component.version.field.KlImageField;
import javafx.scene.image.Image;

public class KlImageFieldFactory implements KlFieldFactory<Image> {

    @Override
    public KlField<Image> create(ObservableField<Image> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlImageField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<Image>> getFieldInterface() {
        return KlImageField.class;
    }

    @Override
    public Class<? extends KlField<Image>> getFieldImplementation() {
        return DefaultKlImageField.class;
    }

    @Override
    public String getName() {
        return "Image Field Factory";
    }

    @Override
    public String getDescription() {
        return "A Image field";
    }
}