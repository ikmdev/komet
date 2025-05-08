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
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Abstract base implementation of the {@link ChapterKlWindow} interface for Komet applications.
 * This class provides core functionality for windows that display content within the Journal
 * window, including state management, layout persistence, and lifecycle events.
 * <p>
 * Chapter windows represent discrete sections of the user interface that require specific
 * {@link ViewProperties} for content rendering and {@link KometPreferences} for storing
 * user configuration. Subclasses define the specific UI components and behavior for
 * different window types.
 *
 * @param <T> A JavaFX {@link Node} subclass that serves as the root container for this window's content.
 */
public abstract class AbstractChapterKlWindow<T extends Node> implements ChapterKlWindow<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractChapterKlWindow.class);

    private final ViewProperties viewProperties;
    protected KometPreferences preferences;
    private Runnable onCloseRunnable;
    protected T paneWindow;

    /**
     * The persistent window state containing position, size, and other window-specific information.
     * This state can be saved to and loaded from preferences.
     */
    private EntityKlWindowState windowState;

    /**
     * Constructs a base chapter window with the specified view properties and preferences.
     * This constructor initializes the fundamental components needed for window operation
     * but does not create the actual UI components, which should be handled by subclasses.
     * <p>
     * The view properties provide contextual information for content rendering, while
     * preferences store user configuration that persists across application sessions.
     *
     * @param viewProperties The {@link ViewProperties} providing contextual information for content rendering.
     * @param preferences    The {@link KometPreferences} for storing and retrieving user configuration.
     */
    public AbstractChapterKlWindow(ViewProperties viewProperties, KometPreferences preferences) {
        this.viewProperties = viewProperties;
        this.preferences = preferences;
    }

    /**
     * Returns the view properties associated with this window, which provide
     * the context for how content should be rendered and how data should be
     * interpreted within the knowledge management system.
     *
     * @return The {@link ViewProperties} for this window.
     */
    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    /**
     * Retrieves the current window state containing position, size, and other window-specific information.
     * The window state represents the configuration that can be persisted to and loaded from preferences.
     *
     * @return The current {@link EntityKlWindowState} of this window, which may be null if not yet initialized.
     */
    public EntityKlWindowState getWindowState() {
        return windowState;
    }

    /**
     * Sets the window state to the specified state object.
     * This method is typically used during initialization or when restoring a window from saved preferences.
     * It does not automatically apply the state to the window's visual component, use
     * {@link #applyWindowState(EntityKlWindowState)}
     * to apply visual changes.
     *
     * @param windowState The {@link EntityKlWindowState} to set as the current window state.
     */
    protected void setWindowState(EntityKlWindowState windowState) {
        this.windowState = windowState;
    }

    @Override
    public void setOnClose(Runnable onClose) {
        this.onCloseRunnable = onClose;
    }

    /**
     * Returns the optional callback to be executed when this window is closed.
     * This allows parent containers or managers to be notified when the window
     * is no longer active.
     *
     * @return An {@link Optional} containing the close callback, if one has been set.
     */
    protected Optional<Runnable> getOnClose() {
        return Optional.ofNullable(onCloseRunnable);
    }

    @Override
    public void save() {
        if (preferences == null) {
            throw new IllegalStateException("Preferences not set");
        }

        windowState = captureWindowState();
        if (windowState.saveToPreferences(preferences)) {
            LOG.debug("Saved state for window: {}", windowState);
        } else {
            LOG.error("Failed to save window state for {} window: {}", getWindowType(), getWindowTopic());
        }
    }

    @Override
    public void revert() {
        if (preferences == null) {
            throw new IllegalStateException("Preferences not set");
        }

        try {
            // Load the saved state from preferences
            if (windowState == null) {
                windowState = EntityKlWindowState.fromPreferences(preferences);
            }

            // Apply the loaded state to the UI component
            if (applyWindowState(windowState)) {
                LOG.info("Reverted window state for {}", getWindowTopic());
            } else {
                LOG.warn("Failed to apply window state for {}", getWindowTopic());
            }
        } catch (Exception ex) {
            LOG.error("Error reverting window state for {}", getWindowTopic(), ex);
        }
    }

    @Override
    public void delete() {
        if (preferences == null) {
            LOG.info("No preferences to delete for window {}", getWindowTopic());
            return;
        }

        try {
            KometPreferences parentPreferences = preferences.parent();
            preferences.flush();
            preferences.removeNode();
            if (parentPreferences != null) {
                parentPreferences.flush();
            }
            preferences = null;
            LOG.info("Deleted window state for {}", getWindowTopic());
        } catch (Exception ex) {
            LOG.error("Error deleting window state for {}", getWindowTopic(), ex);
        }
    }

    @Override
    public T fxGadget() {
        return paneWindow;
    }

    @Override
    public KometPreferences preferences() {
        return preferences;
    }

    /**
     * Captures the current window state, including position, size, and any
     * subclass-specific properties. This state can be saved to preferences
     * for persistence across application sessions.
     * <p>
     * The base implementation captures common window attributes such as position
     * and size. Subclasses should override {@link #captureAdditionalState(EntityKlWindowState)}
     * to add type-specific state information.
     *
     * @return An {@link EntityKlWindowState} object representing this window's current state.
     */
    public EntityKlWindowState captureWindowState() {
        final EntityKlWindowState.Builder builder = EntityKlWindowState.builder()
                .windowId(getWindowTopic())
                .windowType(getWindowType());

        final T gadget = fxGadget();
        if (gadget != null) {
            builder.position(gadget.getLayoutX(), gadget.getLayoutY());

            // Save size if applicable
            if (gadget instanceof Pane pane) {
                builder.size(pane.getWidth(), pane.getHeight());
            } else {
                builder.size(gadget.prefWidth(-1), gadget.prefHeight(-1));
            }
        }

        final EntityKlWindowState state = builder.build();

        // Subclasses should override to add entity-specific information
        captureAdditionalState(state);

        return state;
    }

    /**
     * Applies a previously captured window state to this window, restoring its
     * position, size, and any subclass-specific properties.
     * <p>
     * The base implementation applies common attributes and delegates to
     * {@link #applyAdditionalState(EntityKlWindowState)} for type-specific state restoration.
     *
     * @param state The {@link EntityKlWindowState} to apply to this window.
     * @return {@code true} if the state was successfully applied, {@code false} otherwise.
     */
    public boolean applyWindowState(EntityKlWindowState state) {
        if (state == null) {
            LOG.warn("Cannot apply null window state");
            return false;
        }

        final T gadget = fxGadget();
        if (gadget != null) {
            // Apply position
            gadget.setLayoutX(state.getXPos());
            gadget.setLayoutY(state.getYPos());

            // Apply size if applicable
            if (gadget instanceof Pane pane) {
                pane.setPrefWidth(state.getWidth());
                pane.setPrefHeight(state.getHeight());
            }
        }

        try {
            // Apply additional state
            applyAdditionalState(state);
            return true;
        } catch (Exception e) {
            LOG.error("Error applying window state", e);
            return false;
        }
    }

    /**
     * Extension point for subclasses to capture additional window state beyond the
     * basic position and size information captured by the base implementation.
     * <p>
     * Subclasses should implement this method to add any specialized state data
     * to the provided state object, such as content-specific preferences or
     * view configurations.
     *
     * @param state The {@link EntityKlWindowState} to enhance with additional information.
     */
    protected abstract void captureAdditionalState(EntityKlWindowState state);

    /**
     * Extension point for subclasses to apply additional window state beyond the
     * basic position and size information applied by the base implementation.
     * <p>
     * Subclasses should implement this method to restore any specialized state data
     * from the provided state object, such as content-specific preferences or
     * view configurations.
     *
     * @param state The {@link EntityKlWindowState} containing additional information to apply.
     */
    protected abstract void applyAdditionalState(EntityKlWindowState state);
}