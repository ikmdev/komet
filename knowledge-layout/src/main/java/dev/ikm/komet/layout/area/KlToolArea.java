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
package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.view.ViewProperties;
import javafx.scene.layout.Region;

/**
 * A {@link KlSupplementalArea} that can be summoned as a self-contained tool window
 * within the Journal workspace.
 *
 * <p>Unlike entity chapter windows, a tool area is <em>not</em> bound to an
 * {@code EntityFacade}. The hosting journal injects the view it should query through
 * {@link #setToolViewProperties(ViewProperties)} and a close callback through
 * {@link #setOnCloseRequest(Runnable)} before the area is shown. Implementations are
 * discovered via {@link java.util.ServiceLoader} as {@link Factory} services and offered
 * to the user as entries on the workspace "+" (add) menu.
 *
 * <p>This is the next-generation, type-safe replacement for contributing a standalone
 * panel through the legacy {@code dev.ikm.komet.framework.KometNodeFactory}, which is only
 * reachable in the classic tab UI rather than the Journal workspace.
 *
 * @param <FX> the JavaFX {@link Region} that backs this area
 */
public interface KlToolArea<FX extends Region> extends KlSupplementalArea<FX> {

    /**
     * Supplies the journal view this tool should query. Called once by the host
     * before the area is shown, so in-process tools resolve against the same
     * coordinate the user sees.
     *
     * @param viewProperties the view properties (coordinate and calculators) of the
     *                        hosting journal window
     */
    void setToolViewProperties(ViewProperties viewProperties);

    /**
     * Sets the callback the tool invokes to request that its host window close
     * (for example from a close control in the tool's own header).
     *
     * @param onCloseRequest the action that closes and removes the host window;
     *                        may be {@code null} to clear a previously set callback
     */
    void setOnCloseRequest(Runnable onCloseRequest);

    /**
     * Factory for {@link KlToolArea} instances, discovered via {@code ServiceLoader}
     * under the service type {@code dev.ikm.komet.layout.area.KlToolArea.Factory}.
     *
     * @param <FX> the JavaFX {@link Region} backing the created area
     * @param <KL> the concrete tool-area type produced by this factory
     */
    interface Factory<FX extends Region, KL extends KlToolArea<FX>>
            extends KlSupplementalArea.Factory<FX, KL> {

        /**
         * Human-readable label for the workspace "+" menu entry that creates this tool.
         *
         * @return the menu label, for example {@code "Claude Assistant"}
         */
        String toolName();
    }
}
