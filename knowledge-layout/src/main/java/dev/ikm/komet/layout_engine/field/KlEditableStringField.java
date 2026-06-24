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
package dev.ikm.komet.layout_engine.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlStringField;
import dev.ikm.tinkar.component.FeatureDefinition;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A knowledge-layout-native editable string field: a labelled {@link TextField} bound bidirectionally to a
 * canonical {@link ObservableField.Editable}'s value, with the field's meaning as the title and its purpose as
 * the tooltip. It is the extracted essence of kview's editable-string field — the bind, not the widget — so
 * the layout engine can author a string field without reaching up into kview (the kview widget and its base
 * class were the only kview-specific parts).
 *
 * <p>Because the observable is canonical, the bound {@code editableValueProperty()} is the same instance the
 * rest of the UI holds: a keystroke here updates that one observable, the composer's transaction tracks it, and
 * every other view of the field reflects it — no value is pushed anywhere by hand.
 */
public class KlEditableStringField implements KlStringField {

    private final ObservableView observableView;
    private final ObservableField.Editable<String> fieldEditable;
    private final TextField textField = new TextField();
    private final VBox fxObject;

    /**
     * Builds an editable string field bound to the given canonical editable observable.
     *
     * @param fieldEditable  the canonical editable field to bind to (its value is shared, never copied)
     * @param observableView the view used to resolve the field's meaning (title) and purpose (tooltip)
     */
    public KlEditableStringField(ObservableField.Editable<String> fieldEditable, ObservableView observableView) {
        this.fieldEditable = fieldEditable;
        this.observableView = observableView;

        FeatureDefinition featureDefinition =
                fieldEditable.getObservableFeature().definition(observableView.calculator());
        Label titleLabel = new Label(observableView.getDescriptionTextOrNid(featureDefinition.meaningNid()) + ":");
        titleLabel.getStyleClass().add("kl-field-title");
        textField.setTooltip(new Tooltip(observableView.getDescriptionTextOrNid(featureDefinition.purposeNid())));

        this.fxObject = new VBox(4, titleLabel, textField);
        this.fxObject.getStyleClass().add("kl-editable-string-field");
        setFxPeer(fxObject);

        // The whole contract: bind the control to the canonical editable's value. editableValueProperty IS the
        // cached uncommitted value the composer commits, so there is no write-through (setValue would only
        // re-set this same property), and the editable is canonical, so there is no rebind/swap to manage.
        textField.textProperty().bindBidirectional(fieldEditable.editableValueProperty());
    }

    @Override
    public ObservableField<String> field() {
        return fieldEditable.getObservableFeature();
    }

    @Override
    public ObservableField.Editable<String> fieldEditable() {
        return fieldEditable;
    }

    @Override
    public Region fxObject() {
        return fxObject;
    }

    @Override
    public void save() {
        // Editing is transient; the value lives on the canonical observable, committed via the composer.
    }

    @Override
    public void restoreFromPreferencesOrDefaults() {
        // Nothing persisted by this field; the bound observable carries its value.
    }

    @Override
    public void knowledgeLayoutBind() {
        // Bound at construction so the field is live as soon as it is placed.
    }

    @Override
    public void knowledgeLayoutUnbind() {
        textField.textProperty().unbindBidirectional(fieldEditable.editableValueProperty());
    }
}
