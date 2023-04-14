package dev.ikm.komet.details.concept;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import dev.ikm.komet.framework.observable.ObservableCompoundVersion;
import dev.ikm.komet.framework.view.ViewProperties;

public class ConceptBuilderComponentPanel
        extends Pane {

    final ViewProperties viewProperties;
    final ObservableCompoundVersion observableCompoundVersion;
    final boolean independentCommit;
    final StringProperty conceptText;

    public ConceptBuilderComponentPanel(ViewProperties viewProperties, ObservableCompoundVersion observableCompoundVersion, boolean independentCommit, StringProperty conceptText) {
        this.viewProperties = viewProperties;
        this.observableCompoundVersion = observableCompoundVersion;
        this.independentCommit = independentCommit;
        this.conceptText = conceptText;
    }

    public void setCommitHandler(EventHandler<ActionEvent> eventHandler) {
        throw new UnsupportedOperationException();
    }

    public void setCancelHandler(EventHandler<ActionEvent> eventHandler) {
        throw new UnsupportedOperationException();
    }
}
