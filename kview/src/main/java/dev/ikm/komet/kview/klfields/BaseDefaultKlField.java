package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;

public abstract class BaseDefaultKlField<T> implements KlField<T> {
    protected final ObservableField<T> observableField;
    protected final ObservableView observableView;

    protected ObjectProperty<Parent> klWidget = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Tooltip.install(get(), tooltip);
        }
    };

    protected final boolean isEditable;

    protected final Tooltip tooltip = new Tooltip();

    private final String title;

    public BaseDefaultKlField(ObservableField<T> observableField, ObservableView observableView, boolean isEditable) {
        this.observableField = observableField;
        this.observableView = observableView;

        this.isEditable = isEditable;

        title = field().field().meaning().description() + ":";

        tooltip.setText(observableView.getDescriptionTextOrNid(observableField.purposeNid()));
    }

    protected void updateTooltipText() {
        tooltip.setText(observableView.getDescriptionTextOrNid(observableField.purposeNid()));
    }

    // -- field
    @Override
    public ObservableField<T> field() {
        return observableField;
    }

    // -- title
    public String getTitle() { return title; }

    // -- klWidget
    protected void setKlWidget(Parent klWidget) { this.klWidget.set(klWidget); }

    @Override
    public Parent klWidget() {
        return klWidget.get();
    }
}