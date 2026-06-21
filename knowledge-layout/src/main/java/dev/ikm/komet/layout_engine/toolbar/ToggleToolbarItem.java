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
package dev.ikm.komet.layout_engine.toolbar;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.ToolbarItemBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A {@link dev.ikm.komet.layout.KlToolbarItem} that presents a text label and a sliding on/off switch — the
 * classic "PROPERTIES" toggle, now a first-class area. It reflects and drives an injected
 * {@link BooleanProperty} (for example a drawer's {@code expanded} state): clicking the switch flips it, an
 * external change animates the thumb. The observing listener is installed in {@link #knowledgeLayoutBind()} and
 * removed in {@link #knowledgeLayoutUnbind()}, so it never leaks onto the bound property. Its label persists
 * through the standard area save/restore.
 */
public final class ToggleToolbarItem extends ToolbarItemBlueprint<HBox> {

    private static final String LABEL_KEY = "toggleItem.label";

    private static final double TRACK_WIDTH = 34;
    private static final double TRACK_HEIGHT = 18;
    private static final double THUMB_RADIUS = 7;
    private static final double OFF_X = 3;
    private static final double ON_X = TRACK_WIDTH - 2 * THUMB_RADIUS - 3;

    private final Label labelNode = new Label();
    private final Rectangle track = new Rectangle(TRACK_WIDTH, TRACK_HEIGHT);
    private final Circle thumb = new Circle(THUMB_RADIUS);
    private final ChangeListener<Boolean> stateListener = (obs, wasSelected, isSelected) -> applyState(isSelected, true);
    private BooleanProperty selected;

    {
        labelNode.getStyleClass().add("kl-toolbar-toggle-label");
        labelNode.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        track.setArcWidth(TRACK_HEIGHT);
        track.setArcHeight(TRACK_HEIGHT);
        track.setFill(trackFill(false));
        thumb.setFill(Color.WHITE);
        thumb.setTranslateX(OFF_X);

        StackPane control = new StackPane(track, thumb);
        control.setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);
        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        control.setCursor(Cursor.HAND);
        control.setOnMouseClicked(event -> {
            if (selected != null) {
                selected.set(!selected.get());
            }
        });

        fxObject().setSpacing(6);
        fxObject().setAlignment(Pos.CENTER_LEFT);
        fxObject().getStyleClass().add("kl-toolbar-toggle");
        fxObject().getChildren().addAll(labelNode, control);
    }

    private ToggleToolbarItem(KometPreferences preferences) {
        super(preferences, new HBox());
    }

    private ToggleToolbarItem(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new HBox());
    }

    private static Color trackFill(boolean on) {
        return on ? Color.web("#5b8def") : Color.web("#c4c8cf");
    }

    private void applyState(boolean on, boolean animate) {
        track.setFill(trackFill(on));
        if (animate) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(120), thumb);
            slide.setToX(on ? ON_X : OFF_X);
            slide.play();
        } else {
            thumb.setTranslateX(on ? ON_X : OFF_X);
        }
    }

    /**
     * Sets the toggle's label text (rendered uppercase).
     *
     * @param label the label text
     */
    public void setLabel(String label) {
        labelNode.setText(label == null ? "" : label.toUpperCase());
    }

    /**
     * Injects the boolean this toggle reflects and drives, updating the switch to its current value.
     *
     * @param selected the boolean property
     */
    public void setSelected(BooleanProperty selected) {
        this.selected = selected;
        applyState(selected.get(), false);
    }

    @Override
    public void knowledgeLayoutBind() {
        if (selected != null) {
            selected.addListener(stateListener);
        }
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        if (selected != null) {
            selected.removeListener(stateListener);
        }
    }

    @Override
    protected void subAreaSave() {
        preferences().put(LABEL_KEY, labelNode.getText());
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        preferences().get(LABEL_KEY).ifPresent(labelNode::setText);
    }

    @Override
    protected void subAreaRevert() {
        // The reflected boolean (and thus the visual) is owned elsewhere; nothing to revert here.
    }

    /**
     * Returns a factory for {@code ToggleToolbarItem} instances.
     *
     * @return a new {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores a {@code ToggleToolbarItem} from previously stored preferences.
     *
     * @param preferences the preferences node backing the item
     * @return the restored item
     */
    public static ToggleToolbarItem restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Factory that produces and restores {@link ToggleToolbarItem} instances.
     */
    public static final class Factory implements ToolbarItemBlueprint.Factory<HBox, ToggleToolbarItem> {

        @Override
        public ToggleToolbarItem restore(KometPreferences preferences) {
            return new ToggleToolbarItem(preferences);
        }

        @Override
        public ToggleToolbarItem create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ToggleToolbarItem item = new ToggleToolbarItem(preferencesFactory, this);
            item.setAreaLayout(areaGridSettings.with(this.getClass()));
            return item;
        }
    }
}
