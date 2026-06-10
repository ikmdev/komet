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

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.logic.PremiseType;

/**
 * The refreshed (KonceptBadge) axiom view for a concept's <b>stated</b> logical definition, as a
 * layout-editor supplemental area (ike-issues#639). A distinct class from
 * {@link InferredKonceptAxiomArea} so the editor lists them as separate items (the palette labels
 * each area by its class name). Rendering is inherited from {@link KonceptAxiomTreeArea}; this
 * variant fixes the premise to {@link PremiseType#STATED}.
 */
public final class StatedKonceptAxiomArea extends KonceptAxiomTreeArea {

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    public StatedKonceptAxiomArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    public StatedKonceptAxiomArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Discoverable factory for the stated KonceptBadge axiom view, registered via
     * {@code provides dev.ikm.komet.layout.area.KlSupplementalArea.Factory}.
     */
    public static final class Factory implements SupplementalAreaBlueprint.Factory<StatedKonceptAxiomArea> {

        /**
         * Restores a {@link StatedKonceptAxiomArea} from preferences.
         *
         * @param preferences the preferences node
         * @return the restored area
         */
        @Override
        public StatedKonceptAxiomArea restore(KometPreferences preferences) {
            StatedKonceptAxiomArea area = new StatedKonceptAxiomArea(preferences);
            area.setPremiseType(PremiseType.STATED);
            return area;
        }

        /**
         * Creates a {@link StatedKonceptAxiomArea} with the given layout.
         *
         * @param preferencesFactory factory for the area's preferences node
         * @param areaGridSettings   the area's grid layout
         * @return the created area
         */
        @Override
        public StatedKonceptAxiomArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            StatedKonceptAxiomArea area = new StatedKonceptAxiomArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            area.setPremiseType(PremiseType.STATED);
            return area;
        }
    }
}
