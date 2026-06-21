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
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.selection.KlSelectionContext;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.layout_engine.field.KlEditableStringField;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * The drawer's editing surface: it renders an editor for the <em>currently selected</em> field, driven by the
 * card's {@link KlSelectionContext selection nexus}. The body publishes the focused canonical
 * {@link ObservableField.Editable} into the nexus; this area observes it and binds a native field editor to it.
 *
 * <p>This is the parity-with-kview "edit the selected one" model, but loosely coupled through the scene-graph
 * nexus and the grounded canonical observable — the editor mutates the same observable everything else holds,
 * and the host card's Publish commits the composer's transaction. The first version renders String fields (via
 * {@link KlEditableStringField}); other field types fall through to a hint until their native editors land.
 */
public class EditingArea extends SupplementalAreaBlueprint {

    private static final String PLACEHOLDER = "Select a field to edit.";

    private final VBox content = new VBox(12);
    private final ScrollPane scroller = new ScrollPane(content);

    private KlSelectionContext selectionContext;
    private ObservableView observableView;
    private KlEditableStringField currentEditor;

    private final ChangeListener<ObservableField.Editable<?>> focusedFieldListener =
            (observable, oldField, newField) -> renderFocusedField(newField);

    {
        content.setPadding(new Insets(12));
        content.setFillWidth(true);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("editing-area-scroll");
        fxObject().setCenter(scroller);
        showPlaceholder();
    }

    /**
     * Constructs an editing area to be restored from the given preferences node.
     *
     * @param preferences the preferences node backing this area
     */
    public EditingArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Constructs a fresh editing area with a provisioned preferences node.
     *
     * @param preferencesFactory the factory that provisions this area's preferences node
     * @param areaFactory        the area factory that produced this area
     */
    public EditingArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Connects this area to the card's selection nexus, so it renders an editor whenever the focused field
     * changes. Called by the host card once its coordinate is available.
     *
     * @param selectionContext the card's selection nexus
     * @param observableView   the card's view, used to title/tooltip the field editor
     */
    public void setSelectionContext(KlSelectionContext selectionContext, ObservableView observableView) {
        if (this.selectionContext != null) {
            this.selectionContext.focusedFieldProperty().removeListener(focusedFieldListener);
        }
        this.selectionContext = selectionContext;
        this.observableView = observableView;
        if (selectionContext != null) {
            selectionContext.focusedFieldProperty().addListener(focusedFieldListener);
            renderFocusedField(selectionContext.getFocusedField());
        } else {
            showPlaceholder();
        }
    }

    /** Renders the editor for the focused field, or the empty-state hint when nothing is selected. */
    private void renderFocusedField(ObservableField.Editable<?> editable) {
        unbindCurrentEditor();
        if (editable == null || observableView == null) {
            showPlaceholder();
            return;
        }
        if (isStringField(editable)) {
            @SuppressWarnings("unchecked")
            ObservableField.Editable<String> stringEditable = (ObservableField.Editable<String>) editable;
            currentEditor = new KlEditableStringField(stringEditable, observableView);
            content.getChildren().setAll(currentEditor.fxObject());
        } else {
            Label hint = new Label("A native editor for this field type isn't available yet.");
            hint.setWrapText(true);
            content.getChildren().setAll(hint);
        }
    }

    /**
     * Whether the editable holds a String value. A {@code null} value is treated as a String (an empty text
     * field). Richer data-type resolution will replace this once non-string editors land.
     */
    private boolean isStringField(ObservableField.Editable<?> editable) {
        Object value = editable.getValue();
        return value == null || value instanceof String;
    }

    /** Shows the empty-state hint. */
    private void showPlaceholder() {
        Label note = new Label(PLACEHOLDER);
        note.setWrapText(true);
        content.getChildren().setAll(note);
    }

    /** Releases the current editor's binding before it is replaced or the area is unbound. */
    private void unbindCurrentEditor() {
        if (currentEditor != null) {
            currentEditor.knowledgeLayoutUnbind();
            currentEditor = null;
        }
    }

    /*******************************************************************************
     *  Framework save / restore — editing is transient, held by the composer.     *
     ******************************************************************************/

    @Override
    protected void subAreaSave() {
        // Nothing to persist: the edited value lives on the canonical observable, committed via the composer.
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // Nothing to restore: the host re-connects the nexus on realize.
    }

    @Override
    protected void subAreaRevert() {
        // Nothing to revert: the composer owns rollback of uncommitted edits.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        if (selectionContext != null) {
            selectionContext.focusedFieldProperty().removeListener(focusedFieldListener);
        }
        unbindCurrentEditor();
    }

    /*******************************************************************************
     *  Factory                                                                    *
     ******************************************************************************/

    /**
     * Returns the factory for {@code EditingArea} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores an {@code EditingArea} from previously stored preferences.
     *
     * @param preferences the preferences node backing the area
     * @return the restored area
     */
    public static EditingArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Creates a new {@code EditingArea} with default grid settings.
     *
     * @param preferencesFactory the factory that provisions the area's preferences node
     * @return the created area
     */
    public static EditingArea create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    /**
     * Factory that produces and restores {@link EditingArea} instances.
     */
    public static class Factory implements SupplementalAreaBlueprint.Factory<EditingArea> {

        @Override
        public EditingArea restore(KometPreferences preferences) {
            return new EditingArea(preferences);
        }

        @Override
        public EditingArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditingArea area = new EditingArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            return area;
        }
    }
}
