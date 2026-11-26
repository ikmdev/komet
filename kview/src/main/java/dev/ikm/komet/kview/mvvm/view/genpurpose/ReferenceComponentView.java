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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * What the standard Semantic window's "Reference Component" section shows: the component the
 * window's semantic is about.
 *
 * <p>While the semantic doesn't exist yet there is no such component, and the view is where the
 * user chooses one: a component is searched for or dropped, and Confirm hands it to
 * {@link #onConfirmedProperty()}, which creates the semantic about it. From then on the view only
 * shows it: a semantic's reference component cannot be changed.
 *
 * <p>Layout only: the window's controller sets {@link #referenceComponentProperty()} and is told
 * what the user confirmed or asked to open. Nothing here writes to the store.
 */
final class ReferenceComponentView extends VBox {

    private final ViewProperties viewProperties;

    /** Shows the reference component, once the semantic has one. */
    private final KLReadOnlyComponentControl referenceComponentControl = new KLReadOnlyComponentControl();

    /** Where the user chooses the reference component, while the semantic doesn't exist. */
    private final VBox chooser = new VBox();

    /** The component being chosen. */
    private final KLComponentControl componentPicker;

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    /**
     * @param viewProperties the window's view, which the search runs through and names are read by
     */
    ReferenceComponentView(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        getStyleClass().add("semantic-reference");

        componentPicker = KLComponentControlFactory.createComponentControl(viewProperties.calculator());
        componentPicker.setTitle("Reference component");

        Label note = new Label("The reference component cannot be changed once confirmed.");
        note.setWrapText(true);
        note.getStyleClass().add("semantic-reference-note");

        Button confirmButton = new Button("CONFIRM");
        confirmButton.getStyleClass().add("semantic-reference-confirm");
        confirmButton.disableProperty().bind(componentPicker.entityProperty().map(KLComponentControl::isEmpty).orElse(true));
        confirmButton.setOnAction(_ -> getOnConfirmed().accept(componentPicker.getEntity()));
        HBox buttons = new HBox(confirmButton);
        buttons.getStyleClass().add("semantic-reference-buttons");

        chooser.getStyleClass().add("semantic-reference-chooser");
        chooser.getChildren().addAll(componentPicker, note, buttons);

        // A double click opens the component in a window of its own, like the components listed
        // elsewhere in the journal.
        referenceComponentControl.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2
                    && getReferenceComponent() != null) {
                getOnOpenRequested().accept(getReferenceComponent());
            }
        });

        getChildren().addAll(chooser, referenceComponentControl);

        // The one item of its section: across the section's grid, whatever its columns.
        GridPane.setRowIndex(this, 0);
        GridPane.setColumnIndex(this, 0);
        GridPane.setHgrow(this, Priority.ALWAYS);

        referenceComponent.subscribe(this::refresh);
        refresh();
    }

    /*=*************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     ************************************************************************=*/

    /**
     * Shows the reference component, or the chooser while there is none, reading the component's
     * name again through the view: called as the view's coordinate changes.
     */
    void refresh() {
        EntityFacade component = getReferenceComponent();

        boolean chosen = component != null;
        chooser.setVisible(!chosen);
        chooser.setManaged(!chosen);
        referenceComponentControl.setVisible(chosen);
        referenceComponentControl.setManaged(chosen);

        if (chosen) {
            EntityHandle handle = EntityHandle.get(component.nid());
            String kind = handle.isConcept() ? "Concept"
                    : handle.isPattern() ? "Pattern"
                    : handle.isSemantic() ? "Semantic"
                    : "Component";
            referenceComponentControl.setTitle(kind);
            referenceComponentControl.setValue(new ComponentItem(nameOf(component, handle),
                    Identicon.generateIdenticonImage(component.publicId()), component.publicId(), handle.isConcept()));
        }
    }

    /*=*************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     ************************************************************************=*/

    /**
     * The name the component shows under. A semantic has no description of its own, so its name
     * is the one the view spells out from its pattern and reference component.
     */
    private String nameOf(EntityFacade component, EntityHandle handle) {
        var languageCalculator = viewProperties.calculator().languageCalculator();
        if (handle.isSemantic()) {
            return languageCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(component.nid());
        }
        return viewProperties.calculator().getDescriptionTextOrNid(component.nid());
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- reference component
    /**
     * The component the window's semantic is about; null while the semantic doesn't exist, which
     * shows the chooser.
     */
    private final ObjectProperty<EntityFacade> referenceComponent = new SimpleObjectProperty<>(this, "referenceComponent");
    public ObjectProperty<EntityFacade> referenceComponentProperty() { return referenceComponent; }
    public EntityFacade getReferenceComponent() { return referenceComponent.get(); }
    public void setReferenceComponent(EntityFacade value) { referenceComponent.set(value); }

    // -- on confirmed
    /** Called with the component the user chose and confirmed as the new semantic's reference component. */
    private final ObjectProperty<Consumer<EntityFacade>> onConfirmed = new SimpleObjectProperty<>(this, "onConfirmed");
    public ObjectProperty<Consumer<EntityFacade>> onConfirmedProperty() { return onConfirmed; }
    public Consumer<EntityFacade> getOnConfirmed() { return onConfirmed.get(); }
    public void setOnConfirmed(Consumer<EntityFacade> handler) { onConfirmed.set(handler); }

    // -- on open requested
    /** Called with the reference component when the user double-clicks it, to open it in a window of its own. */
    private final ObjectProperty<Consumer<EntityFacade>> onOpenRequested = new SimpleObjectProperty<>(this, "onOpenRequested");
    public ObjectProperty<Consumer<EntityFacade>> onOpenRequestedProperty() { return onOpenRequested; }
    public Consumer<EntityFacade> getOnOpenRequested() { return onOpenRequested.get(); }
    public void setOnOpenRequested(Consumer<EntityFacade> handler) { onOpenRequested.set(handler); }
}
