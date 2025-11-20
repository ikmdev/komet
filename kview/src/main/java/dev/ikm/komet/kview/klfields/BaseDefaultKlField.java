package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.tinkar.component.FeatureDefinition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.util.Subscription;

import java.util.*;
import java.util.function.*;

public abstract class BaseDefaultKlField<T> implements KlField<T> {
    protected ObservableField<T> observableField;
    protected ObservableField.Editable<T> observableFieldEditable;
    protected final ObservableView observableView;

    protected final Region fxObject;

    protected ObservableStamp stamp4field;

    protected final Tooltip tooltip = new Tooltip();

    private final String title;

    private List<Subscription> fieldEditableSubscriptions;

    public BaseDefaultKlField(ObservableField<T> observableField, ObservableView observableView,
                              ObservableStamp stamp4field, Region fxObject) {
        this.observableField = observableField;
        this.observableFieldEditable = null;
        this.observableView = observableView;
        this.stamp4field = stamp4field;
        this.fxObject = fxObject;

        FeatureDefinition featureDefinition = field().definition(observableView.calculator());

        title = observableView.getDescriptionTextOrNid(featureDefinition.meaningNid()) + ":";

        setFxPeer(fxObject);

        tooltip.setText(observableView.getDescriptionTextOrNid(featureDefinition.purposeNid()));
    }

    public BaseDefaultKlField(ObservableField.Editable<T> observableFieldEditable, ObservableView observableView,
                              ObservableStamp stamp4field, Region fxObject) {
        this.observableField = observableFieldEditable.getObservableFeature();
        this.observableFieldEditable = observableFieldEditable;
        this.observableView = observableView;
        this.stamp4field = stamp4field;
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

    @Override
    public ObservableField.Editable<T> fieldEditable() {
        return observableFieldEditable;
    }

    // -- title
    public String getTitle() { return title; }

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

    /**
     * Returns the list of JavaFX Subscriptions (aka change listeners) bound to the custom ui control.
     * While most controls have one subscription some can have many (e.g., see image field).
     * @return A list of Subscriptions.
     */
    protected List<Subscription> getFieldEditableSubscriptions() {
        if (fieldEditableSubscriptions == null) {
            fieldEditableSubscriptions = new ArrayList<>();
        }
        return fieldEditableSubscriptions;
    }

    protected void unsubscribeFieldSubscriptions() {
        if (fieldEditableSubscriptions != null) {
            fieldEditableSubscriptions.forEach(Subscription::unsubscribe);
            fieldEditableSubscriptions = null;
        }
    }

    /**
     * Unsubscribe all change listeners and swap out previous with new {@link ObservableField.Editable} instance.
     * @param fieldEditable A new field editable to be swapped out.
     */
    protected void replaceObservableFieldEditable(ObservableField.Editable<T> fieldEditable) {
        if (fieldEditable == null) {
            throw new RuntimeException("A fieldEditable must not be null ");
        }
        if (this.observableFieldEditable == null || this.observableFieldEditable != fieldEditable ) {
            unsubscribeFieldSubscriptions();
            this.observableFieldEditable = fieldEditable;
        }
    }

//    /**
//     * A convenience method to perform an action when the fieldEditable().editableValueProperty() is changed.
//     * A JavaFX Subscription is added to a list to be later unsubscribed.
//     * Warning: According to JavaFX docs, when using subscribe
//     * with the Consumer parameter, the listener is invoked. Other subscribe methods are also available won't invoke
//     * listeners (BiConsumer and Runnable do not get invoked initially) .
//     * @param newValueConsumer - Callers code block to perform action on change.
//     * @return A Subscription or change listener when field value changes.
//     */
//    @Override
//    public Subscription doOnEditableValuePropertyChange(Consumer<Optional<T>> newValueConsumer) {
//        Subscription subscription = KlField.super.doOnEditableValuePropertyChange(newValueConsumer);
//        // Add to list of subscriptions.
//        getFieldEditableSubscriptions().add(subscription);
//        return subscription;
//    }

    @Override
    public Subscription doOnEditableValuePropertyChange(BiConsumer<Optional<T>, Optional<T>> changeValueConsumer) {
        Subscription subscription = KlField.super.doOnEditableValuePropertyChange(changeValueConsumer);

        // Add to list of subscriptions.
        getFieldEditableSubscriptions().add(subscription);
        return subscription;
    }

    @Override
    public Subscription doOnEditableValuePropertyChange(Runnable codeBlock) {
        Subscription subscription = KlField.super.doOnEditableValuePropertyChange(codeBlock);

        // Add to list of subscriptions.
        getFieldEditableSubscriptions().add(subscription);
        return subscription;
    }
    /**
     * Most editable UI controls will contain a valueProperty to perform a bidirectional bind to
     * the fieldEditable().editableValueProperty() and a change listener to update the fieldEditable().setValue().
     * More complex controls may not call this method in favor of custom binding behavior.
     * @param valueProperty - UI controls that contain a valueProperty() method to be bi directionally bound and Editable to be
     *                      notified to update ObservableSemantic/Version field (uncommitted).
     * @param newFieldEditable A new field editable {@link ObservableField.Editable} instance.
     */
    protected void rebindValueProperty(Property<T> valueProperty, ObservableField.Editable<T> newFieldEditable) {
        // Unbind previous fieldEditable() both directions editValueProperty <-> uiControl.entityValueProperty
        valueProperty
                .unbindBidirectional(
                        fieldEditable()
                                .editableValueProperty());

        // if already the same ignore.
        replaceObservableFieldEditable(newFieldEditable);

        // bind bidirectionally the ui control and editableValueProperty.
//        FeatureDefinition featureDefinition = field().definition(observableView.calculator());
//        Object defaultValue = KlFieldHelper.defaultFieldValue(featureDefinition);
//        Object previousValue = valueProperty.orElse((T) defaultValue).getValue();
//        fieldEditable().editableValueProperty().set((T) previousValue);
//        fieldEditable().setValue((T) previousValue);

        valueProperty
                .bindBidirectional(
                        fieldEditable().editableValueProperty());
//        // needed to reset previous value, as binding overwrites the valueProperty.
//        valueProperty.setValue((T) previousValue);

        // Add new subscription (change listeners on property changes)
        // based on fieldEditable().editableValueProperty().
        doOnEditableValuePropertyChange((_, newValueOpt) ->
                newValueOpt.ifPresent(newValue ->
                        fieldEditable().setValue(newValue)));
    }

    @Override
    public Region fxObject() {
        return fxObject;
    }
}