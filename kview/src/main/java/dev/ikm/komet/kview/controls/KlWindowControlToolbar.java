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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Skin;

/**
 * The control bar shown at the top of a Knowledge Layout chapter window. It hosts the window's
 * title tab (the drag-dots icon, the {@link #titleProperty() title} and an optional DRAFT chip)
 * on its top row, and below it the coordinate menu, the Publish button, the timeline toggle,
 * the properties toggle and the window close button.
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

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- title
    /**
     * The text shown in the window's title tab, on the toolbar's top row.
     */
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    public StringProperty titleProperty() { return title; }
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }

    // -- draft visible
    /**
     * Controls whether the DRAFT chip is shown next to the title in the window's title tab.
     * Shown while the window is in create mode, i.e. framing a component that doesn't exist yet.
     */
    private final BooleanProperty draftVisible = new SimpleBooleanProperty(this, "draftVisible", false);
    public BooleanProperty draftVisibleProperty() { return draftVisible; }
    public boolean isDraftVisible() { return draftVisible.get(); }
    public void setDraftVisible(boolean value) { draftVisible.set(value); }

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
     * Publish Button                                                          *
     **************************************************************************/

    // -- on publish action
    private final ObjectProperty<Runnable> onPublishAction = new SimpleObjectProperty<>();

    /**
     * Sets the action run when the Publish button is pressed.
     *
     * @param onPublish the action, or {@code null} to clear it
     */
    public void setOnPublishAction(Runnable onPublish) { this.onPublishAction.set(onPublish); }
    public Runnable getOnPublishAction() { return onPublishAction.get(); }

    // -- publish visible
    private final BooleanProperty publishVisible = new SimpleBooleanProperty(this, "publishVisible", false);

    /**
     * Controls whether the Publish button is shown. Both the visibility and the managed state of
     * the underlying button follow this property. Off by default — only windows that adopt the
     * Publish flow turn it on.
     */
    public BooleanProperty publishVisibleProperty() { return publishVisible; }
    public boolean isPublishVisible() { return publishVisible.get(); }
    public void setPublishVisible(boolean value) { publishVisible.set(value); }

    // -- publish disable
    /**
     * The disable state of the Publish button. Callers drive it from whether there is anything
     * to publish — the button starts disabled until a caller says otherwise.
     */
    private final BooleanProperty publishDisable = new SimpleBooleanProperty(this, "publishDisable", true);
    public BooleanProperty publishDisableProperty() { return publishDisable; }
    public boolean isPublishDisable() { return publishDisable.get(); }
    public void setPublishDisable(boolean value) { publishDisable.set(value); }

    // -- publish tooltip
    /**
     * The Publish button's tooltip text. The skin installs the tooltip beside the button rather
     * than on it, so it shows while the button is disabled too — callers set it to say why
     * publishing is unavailable ("Complete the required semantics to publish") as well as the
     * plain enabled text.
     */
    private final StringProperty publishTooltip = new SimpleStringProperty(this, "publishTooltip", "Publish");
    public StringProperty publishTooltipProperty() { return publishTooltip; }
    public String getPublishTooltip() { return publishTooltip.get(); }
    public void setPublishTooltip(String value) { publishTooltip.set(value); }

    /***************************************************************************
     * Timeline toggle                                                         *
     **************************************************************************/

    // -- timeline selected
    private final BooleanProperty timelineSelected = new SimpleBooleanProperty(this, "timelineSelected", false);

    /**
     * The selected state of the timeline (time travel) toggle. The skin binds the toggle button to this
     * property bidirectionally, so callers may read it, set it, bind to it, or subscribe to it to react.
     */
    public BooleanProperty timelineSelectedProperty() { return timelineSelected; }
    public boolean isTimelineSelected() { return timelineSelected.get(); }
    public void setTimelineSelected(boolean value) { timelineSelected.set(value); }

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
     * Field defaults button                                                   *
     **************************************************************************/

    // -- field defaults visible
    private final BooleanProperty fieldDefaultsVisible = new SimpleBooleanProperty(this, "fieldDefaultsVisible", false);

    /**
     * Whether the field-defaults button — which opens the properties panel on the pattern's field
     * defaults — is shown. Off by default; the standard Pattern window turns it on once its pattern
     * exists.
     */
    public BooleanProperty fieldDefaultsVisibleProperty() { return fieldDefaultsVisible; }
    public boolean isFieldDefaultsVisible() { return fieldDefaultsVisible.get(); }
    public void setFieldDefaultsVisible(boolean value) { fieldDefaultsVisible.set(value); }

    // -- on field defaults action
    private final ObjectProperty<Runnable> onFieldDefaultsAction = new SimpleObjectProperty<>();

    /** The action the field-defaults button runs — a one-shot action, the button holds no state. */
    public ObjectProperty<Runnable> onFieldDefaultsActionProperty() { return onFieldDefaultsAction; }
    public void setOnFieldDefaultsAction(Runnable onFieldDefaults) { this.onFieldDefaultsAction.set(onFieldDefaults); }
    public Runnable getOnFieldDefaultsAction() { return onFieldDefaultsAction.get(); }



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

    // -- coordinate visible
    private final BooleanProperty coordinateVisible = new SimpleBooleanProperty(this, "coordinateVisible", true);

    /**
     * Controls whether the coordinate menu is shown. Both the visibility and the managed state of the
     * underlying button follow this property.
     */
    public BooleanProperty coordinateVisibleProperty() { return coordinateVisible; }
    public boolean isCoordinateVisible() { return coordinateVisible.get(); }
    public void setCoordinateVisible(boolean value) { coordinateVisible.set(value); }
}