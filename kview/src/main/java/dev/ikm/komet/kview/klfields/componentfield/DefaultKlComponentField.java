package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

public class DefaultKlComponentField extends BaseDefaultKlField<EntityProxy> {

    public DefaultKlComponentField(ObservableField<EntityProxy> observableComponentField, ObservableView observableView, boolean isEditable) {
        super(observableComponentField, observableView, isEditable);

        Node node;
        if (isEditable) {
            KLComponentControl componentControl = new KLComponentControl();

            // title
            componentControl.setTitle(field().meaning().description());

            // entity
            EntityProxy entity = field().value();
            componentControl.setEntity(entity);

            componentControl.entityProperty().subscribe(newEntity -> {
                field().valueProperty().set(newEntity);
                componentControl.setTitle(field().field().meaning().description());
                updateTooltipText();
            });

            node = componentControl;
        } else {
            KLReadOnlyComponentControl readOnlyComponentControl = new KLReadOnlyComponentControl();

            // text
            ObjectProperty<EntityProxy> valueProperty = observableComponentField.valueProperty();
            String description = valueProperty.get().description();
            readOnlyComponentControl.setText(description);

            // title
            String title = observableView.calculator().languageCalculator().getDescriptionText(observableComponentField.meaningNid()).orElse("Blank Title");
            readOnlyComponentControl.setTitle(title);

            // icon
            readOnlyComponentControl.setIcon(Identicon.generateIdenticonImage(observableComponentField.purpose().publicId()));

            node = readOnlyComponentControl;
        }

        setKlWidget(node);
    }
}
