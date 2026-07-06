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
package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KlWindowControlToolbarSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Skin;

/**
 * The control bar shown at the top of a Knowledge Layout chapter window. It hosts the
 * coordinate menu, the timeline toggle, the properties toggle and the window close button.
 * <p>
 * The buttons are created and laid out by {@link KlWindowControlToolbarSkin}. Rather than exposing those
 * buttons, the control offers an action-oriented API: callers register <em>what</em> should happen (e.g.
 * {@link #setOnCloseAction(Runnable)}) and observe/drive state through properties (e.g.
 * {@link #propertiesSelectedProperty()}). The coordinate menu is the lone exception — see
 * {@link #getCoordinatesMenuButton()}.
 * <p>
 * All visuals are defined in CSS via the {@value #DEFAULT_STYLE_CLASS} style class (see
 * {@code kview.css}); the control holds no inline styling.
 */
public class KlWindowControlToolbar extends Control {

    public static final String DEFAULT_STYLE_CLASS = "kl-window-control-toolbar";

    public KlWindowControlToolbar() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KlWindowControlToolbarSkin(this);
    }

    // -- on close action
    private ObjectProperty<Runnable> onCloseAction = new SimpleObjectProperty<>();

    /**
     * Sets the action run when the window close button is pressed.
     *
     * @param onClose the action, or {@code null} to clear it
     */
    public void setOnCloseAction(Runnable onClose) { this.onCloseAction.set(onClose); }
    public Runnable getOnCloseAction() { return onCloseAction.get(); }

    // -- properties selected
    private final BooleanProperty propertiesSelected = new SimpleBooleanProperty(this, "propertiesSelected", false);

    /**
     * The selected state of the properties toggle. The skin binds the toggle button to this property
     * bidirectionally, so callers may read it, set it to drive the toggle, bind to it, or subscribe to it
     * to react when the panel is opened or closed.
     */
    public BooleanProperty propertiesSelectedProperty() { return propertiesSelected; }
    public boolean isPropertiesSelected() { return propertiesSelected.get(); }
    public void setPropertiesSelected(boolean value) { propertiesSelected.set(value); }

    /***************************************************************************
     *                                                                         *
     * Timeline toggle                                                         *
     *                                                                         *
     **************************************************************************/

    private final BooleanProperty timelineSelected = new SimpleBooleanProperty(this, "timelineSelected", false);

    /**
     * The selected state of the timeline (time travel) toggle. The skin binds the toggle button to this
     * property bidirectionally, so callers may read it, set it, bind to it, or subscribe to it to react.
     */
    public BooleanProperty timelineSelectedProperty() { return timelineSelected; }
    public boolean isTimelineSelected() { return timelineSelected.get(); }
    public void setTimelineSelected(boolean value) { timelineSelected.set(value); }

    // -- coordinate visible
    private final BooleanProperty coordinateVisible = new SimpleBooleanProperty(this, "coordinateVisible", true);

    /**
     * Controls whether the coordinate menu is shown. Both the visibility and the managed state of the
     * underlying button follow this property.
     */
    public BooleanProperty coordinateVisibleProperty() { return coordinateVisible; }
    public boolean isCoordinateVisible() { return coordinateVisible.get(); }
    public void setCoordinateVisible(boolean value) { coordinateVisible.set(value); }

    // -- timeline visible
    private final BooleanProperty timelineVisible = new SimpleBooleanProperty(this, "timelineVisible", true);

    /**
     * Controls whether the timeline toggle is shown. Both the visibility and the managed state of the
     * underlying button follow this property.
     */
    public BooleanProperty timelineVisibleProperty() { return timelineVisible; }
    public boolean isTimelineVisible() { return timelineVisible.get(); }
    public void setTimelineVisible(boolean value) { timelineVisible.set(value); }

    /***************************************************************************
     * Coordinate menu                                                         *
     **************************************************************************/

    // The coordinate menu is a dynamic popup rather than a simple press action, and its wiring
    // (mouse-press filter, :filter-showing / :filter-set pseudo-classes, dynamically populated items)
    // needs the live button node. The control therefore owns the button instance so callers can reach it
    // before the skin is built; the skin styles and lays it out.
    //
    // TODO: exposing the raw MenuButton leaks the skin's UI to callers. Replace this getter with an
    //  encapsulated, action/popup-oriented API once the coordinate popup wiring is refactored so it no
    //  longer needs the button node directly.
    private final MenuButton coordinatesMenuButton = new MenuButton();

    /**
     * Returns the coordinate menu button so callers can attach the view-coordinate popup behaviour.
     *
     * @return the coordinate menu button
     */
    public MenuButton getCoordinatesMenuButton() {
        return coordinatesMenuButton;
    }
}