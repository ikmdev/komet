package dev.ikm.komet.kview.klfields.editable;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.control.Tooltip;

public class DefaultEditableKlComponentField implements EditableKlComponentField {
    private ObservableField<Object> field = new ObservableField<>(null);
    private KLComponentControl node;

    @Override
    public ObservableField<Object> field() {
        return field;
    }

    @Override
    public KLComponentControl klWidget() {
        if (node == null) {
            KLComponentControl componentControl = new KLComponentControl();

            componentControl.entityProperty().addListener((observable, oldValue, newValue) -> {
                // if changed update value.
                Entity entity = newValue;
                field().valueProperty().set(newValue);
                componentControl.setTitle(field().field().meaning().description());
                componentControl.setTooltip(new Tooltip(field().field().purpose().description()));
            });

            field().fieldProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Entity entity = (Entity) newValue.value();
                    Entity<?> entity2 = EntityService.get().getEntityFast(entity.nid());
                    componentControl.entityProperty().set(entity2);
                    componentControl.setTitle(newValue.meaning().description());
                    componentControl.setTooltip(new Tooltip(newValue.purpose().description()));
                }
            });
            EntityProxy entity = (EntityProxy) field().value();
            Entity<?> entity2 = EntityService.get().getEntityFast(entity.nid());
            field().valueProperty().set(entity2);
            componentControl.entityProperty().set(entity2);
            componentControl.setTitle(field().meaning().description());
            componentControl.setTooltip(new Tooltip(field().purpose().description()));

            node = componentControl;
        }
        return node;
    }
}
