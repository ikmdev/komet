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
package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlSupplementalArea;
import dev.ikm.komet.layout.area.KlToolArea;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorSupplementalAreaModel;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.layout_engine.blueprint.AbstractCheckArea;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders <em>placed supplemental areas</em> — the pluggable-area capability of the Knowledge
 * Layout, demonstrated here as the runtime half of "plugging capability into the layout".
 *
 * <p>A layout author drops a supplemental area (any {@link KlSupplementalArea}) into a section in
 * the editor; it is persisted only by its {@code KlSupplementalArea.Factory} class name plus grid
 * placement. At render time this capability materializes the live area <em>generically</em> from
 * that factory — discovered cross-layer via {@link PluggableService} — and injects the host's
 * context: the {@link ViewProperties} the window queries and, for check areas, the concept in
 * focus.
 *
 * <p>The point: no host (and no per-plugin) code needs to know about any specific area type.
 * Contributing a new {@code KlSupplementalArea.Factory} from a plugin is, by itself, enough for it
 * to be placeable in the editor and rendered here. A host window (for example the journal's general
 * purpose window) only delegates one call — {@link #renderInto}.
 */
public final class SupplementalAreaRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(SupplementalAreaRenderer.class);

    private SupplementalAreaRenderer() {
    }

    /**
     * Renders every supplemental area placed in the given section into the target grid, at each
     * area's saved row / column / span. Failures to materialize one area are logged and skipped so
     * a single bad plugin cannot break the whole window.
     *
     * @param section        the section whose placed areas should be rendered; may be {@code null}
     * @param targetGrid     the grid to add the area nodes to; may be {@code null}
     * @param viewProperties the host view the areas should query
     * @param focus          the concept a check area should evaluate; may be {@code null}
     */
    public static void renderInto(EditorSectionModel section, GridPane targetGrid,
                                  ViewProperties viewProperties, EntityFacade focus) {
        if (section == null || targetGrid == null) {
            return;
        }
        for (EditorSupplementalAreaModel model : section.getSupplementalAreas()) {
            try {
                KlSupplementalArea<?> area = materialize(model, viewProperties, focus);
                Region node = area.fxObject();
                GridPane.setRowIndex(node, model.getRowIndex());
                GridPane.setColumnIndex(node, model.getColumnIndex());
                GridPane.setColumnSpan(node, Math.max(1, model.getColumnSpan()));
                GridPane.setVgrow(node, Priority.ALWAYS);
                targetGrid.getChildren().add(node);
            } catch (RuntimeException e) {
                LOG.error("Could not render supplemental area {}", model.getAreaFactoryClassName(), e);
            }
        }
    }

    /**
     * Materializes a single placed supplemental area into a live, context-injected
     * {@link KlSupplementalArea} from its persisted factory class name.
     *
     * @param model          the placed-area model (carries the factory class name and placement)
     * @param viewProperties the host view the area should query
     * @param focus          the concept a check area should evaluate; may be {@code null}
     * @return the live area
     * @throws RuntimeException if the factory cannot be loaded or instantiated
     */
    public static KlSupplementalArea<?> materialize(EditorSupplementalAreaModel model,
                                                    ViewProperties viewProperties, EntityFacade focus) {
        String factoryClassName = model.getAreaFactoryClassName();
        try {
            KlArea.Factory factory = (KlArea.Factory) PluggableService.forName(factoryClassName)
                    .getDeclaredConstructor().newInstance();
            KlPreferencesFactory preferencesFactory =
                    KlProfiles.sharedWindowPreferenceFactory(factory.getClass());
            KlSupplementalArea<?> area = (KlSupplementalArea<?>) factory.create(preferencesFactory);

            // Inject the host context through the generic knowledge-layout seams a placed area may
            // implement. No host or per-plugin code is required — this is the whole extension point.
            if (area instanceof KlToolArea<?> toolArea) {
                toolArea.setToolViewProperties(viewProperties);
            }
            if (area instanceof AbstractCheckArea checkArea) {
                checkArea.setCheckViewProperties(viewProperties);
                checkArea.setFocus(focus);
            }
            return area;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not materialize supplemental area: " + factoryClassName, e);
        }
    }
}
