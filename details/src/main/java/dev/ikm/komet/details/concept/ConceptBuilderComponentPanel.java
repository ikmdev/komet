/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
