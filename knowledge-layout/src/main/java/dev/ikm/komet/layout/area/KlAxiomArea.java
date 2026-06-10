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
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.terms.EntityFacade;

/**
 * Injection seam and marker for a swappable concept <em>axiom-view</em> provider
 * (ike-issues#644). Every axiom renderer — the classic {@code AxiomView} wrapper, the refreshed
 * Koncept tree (ike-issues#639), or a future one — is a {@link KlSupplementalArea} that also
 * implements this interface.
 *
 * <p>The host window (the new KL concept window, ike-issues#645) discovers the selected provider's
 * {@code KlArea.Factory} via {@code PluggableService}, creates the area, and injects its context
 * — the concept in focus, the {@link ViewProperties} to query, and the {@link PremiseType} — through
 * this interface, with no knowledge of the concrete renderer. This mirrors how
 * {@code SupplementalAreaRenderer} injects {@code AbstractCheckArea}/{@code KlToolArea} context.
 */
public interface KlAxiomArea {

    /**
     * Sets the concept whose axioms are rendered.
     *
     * @param concept the concept to render, or {@code null} to clear
     */
    void setFocusConcept(EntityFacade concept);

    /**
     * Sets the view the renderer queries (descriptions, navigation, axiom resolution).
     *
     * @param viewProperties the view to use
     */
    void setAxiomViewProperties(ViewProperties viewProperties);

    /**
     * Sets the premise whose axioms are shown.
     *
     * @param premiseType the premise type (stated or inferred)
     */
    void setPremiseType(PremiseType premiseType);
}
