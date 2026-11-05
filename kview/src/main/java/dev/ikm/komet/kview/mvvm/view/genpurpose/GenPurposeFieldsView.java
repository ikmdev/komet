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

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.fxutils.CssHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/**
 * The edit-fields form of a general-purpose KL window's properties panel, built in code: a title
 * bar on top, the field editors stacked in a scrolling column in the middle, and the Cancel,
 * Clear/Reset and Submit buttons along the bottom. Layout only: the column shows one editor per
 * item of {@link #getFields()}, each made by the {@link #fieldNodeFactoryProperty() field node
 * factory}, and {@link GenPurposeFieldsController} supplies the fields, the factory, the texts and
 * the button handlers through this class's properties without ever seeing the controls themselves.
 */
public final class GenPurposeFieldsView extends BorderPane {

    private final Label formTitleLabel = new Label();
    private final VBox editFieldsVBox = new VBox();
    private final Button cancelButton = new Button("Cancel");
    private final Button clearOrResetFormButton = new Button();
    private final Button submitButton = new Button();

    /** The fields shown in the column, in order. */
    private final ObservableList<ObservableField.Editable<?>> fields = FXCollections.observableArrayList();

    public GenPurposeFieldsView() {
        getStyleClass().add("sub-container");
        getStylesheets().add(CssHelper.defaultStyleSheet());
        setPrefWidth(500);

        formTitleLabel.getStyleClass().add("lidr-device-title-label");
        formTitleLabel.textProperty().bind(formTitle);
        HBox titleBar = new HBox(formTitleLabel);
        titleBar.getStyleClass().add("fields-title-bar");
        BorderPane.setAlignment(titleBar, Pos.CENTER);
        setTop(titleBar);

        editFieldsVBox.getStyleClass().add("controls-container");
        editFieldsVBox.setSpacing(8);
        ScrollPane scrollPane = new ScrollPane(editFieldsVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane.setAlignment(scrollPane, Pos.CENTER);
        BorderPane.setMargin(scrollPane, new Insets(12));
        setCenter(scrollPane);

        // The spacer pushes Clear/Reset and Submit to the right of Cancel.
        Region spacer = new Region();
        spacer.setMaxWidth(Double.MAX_VALUE);
        spacer.setPrefWidth(150);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        for (Button button : new Button[] {cancelButton, clearOrResetFormButton, submitButton}) {
            button.setMinWidth(Region.USE_PREF_SIZE);
            button.setMnemonicParsing(false);
        }
        clearOrResetFormButton.textProperty().bind(clearOrResetButtonText);
        clearOrResetFormButton.disableProperty().bind(clearOrResetDisabled);
        submitButton.getStyleClass().add("primary");
        submitButton.textProperty().bind(submitButtonText);
        submitButton.disableProperty().bind(submitDisabled);
        HBox buttonBar = new HBox(cancelButton, spacer, clearOrResetFormButton, submitButton);
        buttonBar.getStyleClass().add("button-bar");
        setBottom(buttonBar);

        cancelButton.setOnAction(_ -> getOnCancel().run());
        clearOrResetFormButton.setOnAction(_ -> getOnClearOrResetForm().run());
        submitButton.setOnAction(_ -> getOnSubmit().run());

        fields.subscribe(this::rebuildFieldColumn);
    }

    /*=========================================================================*
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     *=========================================================================*/

    /** The fields shown in the column, in order; the column follows changes to the list. */
    public ObservableList<ObservableField.Editable<?>> getFields() {
        return fields;
    }

    /** Moves the keyboard focus to the Cancel button, away from the field editors. */
    public void focusCancelButton() {
        cancelButton.requestFocus();
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- form title
    /** The title shown above the fields. */
    private final StringProperty formTitle = new SimpleStringProperty(this, "formTitle", "Pattern Fields");
    public String getFormTitle() { return formTitle.get(); }
    public StringProperty formTitleProperty() { return formTitle; }
    public void setFormTitle(String title) { this.formTitle.set(title); }

    // -- field node factory
    /** Makes the editor shown for a field of {@link #getFields()}. */
    private final ObjectProperty<Function<ObservableField.Editable<?>, Node>> fieldNodeFactory = new SimpleObjectProperty<>(this, "fieldNodeFactory");
    public Function<ObservableField.Editable<?>, Node> getFieldNodeFactory() { return fieldNodeFactory.get(); }
    public ObjectProperty<Function<ObservableField.Editable<?>, Node>> fieldNodeFactoryProperty() { return fieldNodeFactory; }
    public void setFieldNodeFactory(Function<ObservableField.Editable<?>, Node> factory) { this.fieldNodeFactory.set(factory); }

    // -- clear or reset button text
    /** What the Clear/Reset button reads: "Clear form" or "Reset form", by the window's mode. */
    private final StringProperty clearOrResetButtonText = new SimpleStringProperty(this, "clearOrResetButtonText", "Clear form");
    public String getClearOrResetButtonText() { return clearOrResetButtonText.get(); }
    public StringProperty clearOrResetButtonTextProperty() { return clearOrResetButtonText; }
    public void setClearOrResetButtonText(String text) { this.clearOrResetButtonText.set(text); }

    // -- clear or reset disabled
    /** Whether the Clear/Reset button is disabled. */
    private final BooleanProperty clearOrResetDisabled = new SimpleBooleanProperty(this, "clearOrResetDisabled", false);
    public boolean isClearOrResetDisabled() { return clearOrResetDisabled.get(); }
    public BooleanProperty clearOrResetDisabledProperty() { return clearOrResetDisabled; }
    public void setClearOrResetDisabled(boolean disabled) { this.clearOrResetDisabled.set(disabled); }

    // -- submit button text
    /** What the Submit button reads: "Submit", or "Publish" for the defaults form. */
    private final StringProperty submitButtonText = new SimpleStringProperty(this, "submitButtonText", "Submit");
    public String getSubmitButtonText() { return submitButtonText.get(); }
    public StringProperty submitButtonTextProperty() { return submitButtonText; }
    public void setSubmitButtonText(String text) { this.submitButtonText.set(text); }

    // -- submit disabled
    /** Whether the Submit button is disabled. */
    private final BooleanProperty submitDisabled = new SimpleBooleanProperty(this, "submitDisabled", false);
    public boolean isSubmitDisabled() { return submitDisabled.get(); }
    public BooleanProperty submitDisabledProperty() { return submitDisabled; }
    public void setSubmitDisabled(boolean disabled) { this.submitDisabled.set(disabled); }

    // -- on cancel
    /** Called when the Cancel button is pressed. */
    private final ObjectProperty<Runnable> onCancel = new SimpleObjectProperty<>(this, "onCancel");
    public Runnable getOnCancel() { return onCancel.get(); }
    public ObjectProperty<Runnable> onCancelProperty() { return onCancel; }
    public void setOnCancel(Runnable handler) { this.onCancel.set(handler); }

    // -- on clear or reset form
    /** Called when the Clear/Reset button is pressed. */
    private final ObjectProperty<Runnable> onClearOrResetForm = new SimpleObjectProperty<>(this, "onClearOrResetForm");
    public Runnable getOnClearOrResetForm() { return onClearOrResetForm.get(); }
    public ObjectProperty<Runnable> onClearOrResetFormProperty() { return onClearOrResetForm; }
    public void setOnClearOrResetForm(Runnable handler) { this.onClearOrResetForm.set(handler); }

    // -- on submit
    /** Called when the Submit button is pressed. */
    private final ObjectProperty<Runnable> onSubmit = new SimpleObjectProperty<>(this, "onSubmit");
    public Runnable getOnSubmit() { return onSubmit.get(); }
    public ObjectProperty<Runnable> onSubmitProperty() { return onSubmit; }
    public void setOnSubmit(Runnable handler) { this.onSubmit.set(handler); }

    /*=========================================================================*
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     *=========================================================================*/

    /** Shows one editor per field, one under the other with a separator between. */
    private void rebuildFieldColumn() {
        editFieldsVBox.getChildren().clear();
        for (int i = 0; i < fields.size(); i++) {
            editFieldsVBox.getChildren().add(getFieldNodeFactory().apply(fields.get(i)));
            if (i < fields.size() - 1) {
                editFieldsVBox.getChildren().add(createSeparator());
            }
        }
    }

    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        separator.setFocusTraversable(false);
        return separator;
    }
}
