package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.SearchControlSkin;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class SearchControl extends Control {

    public SearchControl() {

        getStyleClass().add("search-control");
        getStylesheets().add(getUserAgentStylesheet());
    }

    // promptTextProperty
    private final StringProperty promptTextProperty = new SimpleStringProperty(this, "promptText");
    public final StringProperty promptTextProperty() {
        return promptTextProperty;
    }
    public final String getPromptText() {
        return promptTextProperty.get();
    }
    public final void setPromptText(String value) {
        promptTextProperty.set(value);
    }

    // textProperty
    private final StringProperty textProperty = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
        return textProperty;
    }
    public final String getText() {
        return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
    }

    // onActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onActionProperty;
    }
    public final EventHandler<ActionEvent> getOnAction() {
        return onActionProperty.get();
    }
    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty.set(value);
    }

    // onFilterActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty = new SimpleObjectProperty<>(this, "onFilterAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty() {
        return onFilterActionProperty;
    }
    public final EventHandler<ActionEvent> getOnFilterAction() {
        return onFilterActionProperty.get();
    }
    public final void setOnFilterAction(EventHandler<ActionEvent> value) {
        onFilterActionProperty.set(value);
    }

    // filterSetProperty
    private final BooleanProperty filterSetProperty = new SimpleBooleanProperty(this, "filterSet");
    public final BooleanProperty filterSetProperty() {
        return filterSetProperty;
    }
    public final boolean isFilterSet() {
        return filterSetProperty.get();
    }
    public final void setFilterSet(boolean value) {
        filterSetProperty.set(value);
    }

    // activationProperty
    private final DoubleProperty activationProperty = new SimpleDoubleProperty(this, "activation", 500);
    public final DoubleProperty activationProperty() {
        return activationProperty;
    }
    public final double getActivation() {
        return activationProperty.get();
    }
    public final void setActivation(double value) {
        activationProperty.set(value);
    }

    public record SearchResult(ConceptFacade parentConcept, ConceptFacade concept, String highlight) {}

    // resultsProperty
    private final ObservableList<SearchResult> resultsProperty = FXCollections.observableArrayList();
    public final ObservableList<SearchResult> resultsProperty() {
       return resultsProperty;
    }

    // resultsPlaceholderProperty
    private final StringProperty resultsPlaceholderProperty = new SimpleStringProperty(this, "resultsPlaceholder");
    public final StringProperty resultsPlaceholderProperty() {
        return resultsPlaceholderProperty;
    }
    public final String getResultsPlaceholder() {
        return resultsPlaceholderProperty.get();
    }
    public final void setResultsPlaceholder(String value) {
        resultsPlaceholderProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return SearchControl.class.getResource("search-control.css").toExternalForm();
    }
}