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

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import javafx.scene.Node;

/**
 * The refreshed axiom view (ike-issues#639) as a Knowledge-Layout supplemental area: renders the
 * concept's logical definition as a {@link KonceptAxiomTree} (identicon-pill {@code KonceptBadge}s
 * over the historic clause accent bars). Shared base for the stated and inferred variants
 * ({@link StatedKonceptAxiomArea} / {@link InferredKonceptAxiomArea}); the rendering and context
 * injection are inherited from {@link AbstractAxiomArea}.
 */
public abstract class KonceptAxiomTreeArea extends AbstractAxiomArea {

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    protected KonceptAxiomTreeArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    protected KonceptAxiomTreeArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    @Override
    protected Node renderAxioms(ObservableSemanticVersion axiomVersion, PremiseType premiseType,
                                ViewProperties viewProperties) {
        return KonceptAxiomTree.create(axiomVersion, premiseType, viewProperties);
    }
}
