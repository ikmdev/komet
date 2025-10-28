package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

public abstract class BaseDefaultKlField<T> implements KlField<T> {
    protected final ObservableField<T> observableField;
    protected final ObservableView observableView;

    protected final Region fxObject;

    protected final boolean isEditable;

    protected final Tooltip tooltip = new Tooltip();

    private final String title;

    public BaseDefaultKlField(ObservableField<T> observableField, ObservableView observableView,
                              boolean isEditable, Region fxObject) {
        this.observableField = observableField;
        this.observableView = observableView;
        this.isEditable = isEditable;
        this.fxObject = fxObject;

        FeatureDefinition featureDefinition = field().definition(observableView.calculator());

        title = observableView.getDescriptionTextOrNid(featureDefinition.meaningNid()) + ":";

        setFxPeer(fxObject);

        tooltip.setText(observableView.getDescriptionTextOrNid(featureDefinition.purposeNid()));
    }

    protected void updateTooltipText() {
        tooltip.setText(observableView.getDescriptionTextOrNid(observableField.definition(observableView.calculator()).purposeNid()));
    }

    // -- on edit action
    private ObjectProperty<Runnable> onEditAction = new SimpleObjectProperty<>();
    public Runnable getOnEditAction() { return onEditAction.get(); }
    public ObjectProperty<Runnable> onEditActionProperty() { return onEditAction; }
    public void setOnEditAction(Runnable onEditAction) { this.onEditAction.set(onEditAction); }

    // -- field
    @Override
    public ObservableField<T> field() {
        return observableField;
    }

    // -- title
    public String getTitle() { return title; }

    @Override
    public Region fxObject() {
        return this.fxObject;
    }

    @Override
    public void save() {
        // TODO: implement saving to preferences
    }

    @Override
    public void restoreFromPreferencesOrDefaults() {

    }

    @Override
    public void knowledgeLayoutUnbind() {

    }

    @Override
    public void knowledgeLayoutBind() {

    }

}