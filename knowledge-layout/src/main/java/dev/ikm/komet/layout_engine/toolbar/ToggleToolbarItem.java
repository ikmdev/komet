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

import dev.ikm.komet.layout.KlToolbarItem;
import dev.ikm.komet.layout.area.AreaGridSettings;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A {@link KlToolbarItem} that presents a text label and a sliding on/off switch bound to a
 * {@link BooleanProperty} — the classic "PROPERTIES" toggle, now factory-produced. It reflects and drives the
 * property: clicking the switch flips it, and an external change animates the thumb. The observing listener is
 * installed in {@link #bind()} and removed in {@link #unbind()}, so a toolbar can be rebuilt without leaking
 * listeners onto the bound property.
 */
public final class ToggleToolbarItem implements KlToolbarItem {

    private static final double TRACK_WIDTH = 34;
    private static final double TRACK_HEIGHT = 18;
    private static final double THUMB_RADIUS = 7;
    private static final double OFF_X = 3;
    private static final double ON_X = TRACK_WIDTH - 2 * THUMB_RADIUS - 3;

    private final HBox node = new HBox(6);
    private final AreaGridSettings placement;
    private final BooleanProperty selected;
    private final Rectangle track = new Rectangle(TRACK_WIDTH, TRACK_HEIGHT);
    private final Circle thumb = new Circle(THUMB_RADIUS);
    private final ChangeListener<Boolean> stateListener;

    private ToggleToolbarItem(String label, BooleanProperty selected, AreaGridSettings placement) {
        this.selected = selected;
        this.placement = placement;

        Label text = new Label(label.toUpperCase());
        text.getStyleClass().add("kl-toolbar-toggle-label");
        text.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        track.setArcWidth(TRACK_HEIGHT);
        track.setArcHeight(TRACK_HEIGHT);
        track.setFill(trackFill(selected.get()));
        thumb.setFill(Color.WHITE);
        thumb.setTranslateX(selected.get() ? ON_X : OFF_X);

        StackPane control = new StackPane(track, thumb);
        control.setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);
        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        control.setCursor(Cursor.HAND);
        control.setOnMouseClicked(event -> this.selected.set(!this.selected.get()));

        node.setAlignment(Pos.CENTER_LEFT);
        node.getStyleClass().add("kl-toolbar-toggle");
        node.getChildren().addAll(text, control);

        // Reflect external changes (installed at bind, removed at unbind).
        this.stateListener = (obs, wasSelected, isSelected) -> {
            track.setFill(trackFill(isSelected));
            TranslateTransition slide = new TranslateTransition(Duration.millis(120), thumb);
            slide.setToX(isSelected ? ON_X : OFF_X);
            slide.play();
        };
    }

    private static Color trackFill(boolean on) {
        return on ? Color.web("#5b8def") : Color.web("#c4c8cf");
    }

    @Override
    public Node toolbarNode() {
        return node;
    }

    @Override
    public AreaGridSettings placement() {
        return placement;
    }

    @Override
    public void bind() {
        selected.addListener(stateListener);
    }

    @Override
    public void unbind() {
        selected.removeListener(stateListener);
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
     * Factory that produces {@link ToggleToolbarItem} instances.
     */
    public static final class Factory implements KlToolbarItem.Factory<ToggleToolbarItem> {

        @Override
        public AreaGridSettings defaultPlacement() {
            return AreaGridSettings.DEFAULT;
        }

        /**
         * Creates a toggle item with the default placement.
         *
         * @param label    the label text (rendered uppercase)
         * @param selected the boolean the toggle reflects and drives
         * @return the created item
         */
        public ToggleToolbarItem create(String label, BooleanProperty selected) {
            return create(label, selected, defaultPlacement());
        }

        /**
         * Creates a toggle item with the given placement.
         *
         * @param label     the label text (rendered uppercase)
         * @param selected  the boolean the toggle reflects and drives
         * @param placement the grid placement within the toolbar
         * @return the created item
         */
        public ToggleToolbarItem create(String label, BooleanProperty selected, AreaGridSettings placement) {
            return new ToggleToolbarItem(label, selected, placement);
        }
    }
}
