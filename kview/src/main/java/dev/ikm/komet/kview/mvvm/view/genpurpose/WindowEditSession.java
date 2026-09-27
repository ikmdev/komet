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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 * What a general-purpose KL window is editing: the component it frames and whether that
 * component exists yet. Shared by the window's controller, its properties panel and the panel's
 * forms, and free of any scene graph.
 *
 * <p>A window opened from the Journal's "+" button frames a component that doesn't exist yet:
 * it starts in {@link FormMode#CREATE create mode} with no component, gets one — still
 * uncommitted — when the user authors the first semantic, and {@link #enterEditMode() enters
 * edit mode} once that component is committed. A window opened on an existing component
 * ("Open as ...") starts in edit mode with it.
 */
public final class WindowEditSession {

    private final ViewProperties viewProperties;

    /**
     * @param viewProperties the window's derived coordinate, which every edit reads and writes through
     * @param component      the component the window was opened on; null to create a new one
     */
    public WindowEditSession(ViewProperties viewProperties, EntityFacade component) {
        this.viewProperties = viewProperties;
        this.mode = new SimpleObjectProperty<>(component == null ? FormMode.CREATE : FormMode.EDIT);
        this.component = new ReadOnlyObjectWrapper<>(component);
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public boolean isCreateMode() {
        return mode.get() == FormMode.CREATE;
    }

    /** The window's component now exists: it was committed. */
    public void enterEditMode() {
        mode.set(FormMode.EDIT);
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- form mode
    /**
     * Whether the window's component exists yet ({@link FormMode#EDIT}) or is still being created
     * ({@link FormMode#CREATE}).
     */
    private final ObjectProperty<FormMode> mode;
    public ObjectProperty<FormMode> modeProperty() {
        return mode;
    }
    public FormMode getMode() {
        return mode.get();
    }

    // -- component
    /**
     * The component the window frames: the one it was opened on, or the one created while in
     * create mode (uncommitted until its first commit); null while none exists.
     */
    private final ReadOnlyObjectWrapper<EntityFacade> component;
    public ReadOnlyObjectProperty<EntityFacade> componentProperty() {
        return component.getReadOnlyProperty();
    }
    public EntityFacade getComponent() {
        return component.get();
    }
    /** The component the window creates in create mode, as soon as it exists (uncommitted). */
    void setComponent(EntityFacade component) {
        this.component.set(component);
    }
}
