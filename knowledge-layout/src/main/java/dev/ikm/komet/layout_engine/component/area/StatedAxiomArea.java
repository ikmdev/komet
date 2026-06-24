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
 * The classic axiom view for a concept's <b>stated</b> (author-asserted) logical definition, as a
 * Knowledge-Layout supplemental area (ike-issues#644). It is a distinct class from
 * {@link InferredAxiomArea} so the layout editor lists the two as separate "Stated Axiom Area" and
 * "Inferred Axiom Area" items — the editor palette labels each area by its class name, not its
 * factory. All rendering is inherited from {@link ClassicAxiomArea}; this variant fixes the premise
 * to {@link PremiseType#STATED}.
 */
public final class StatedAxiomArea extends ClassicAxiomArea {

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    public StatedAxiomArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    public StatedAxiomArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Discoverable factory for the stated axiom view, registered via
     * {@code provides dev.ikm.komet.layout.area.KlSupplementalArea.Factory}.
     */
    public static final class Factory implements SupplementalAreaBlueprint.Factory<StatedAxiomArea> {

        /**
         * Restores a {@link StatedAxiomArea} from preferences.
         *
         * @param preferences the preferences node
         * @return the restored area
         */
        @Override
        public StatedAxiomArea restore(KometPreferences preferences) {
            StatedAxiomArea area = new StatedAxiomArea(preferences);
            area.setPremiseType(PremiseType.STATED);
            return area;
        }

        /**
         * Creates a {@link StatedAxiomArea} with the given layout.
         *
         * @param preferencesFactory factory for the area's preferences node
         * @param areaGridSettings   the area's grid layout
         * @return the created area
         */
        @Override
        public StatedAxiomArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            StatedAxiomArea area = new StatedAxiomArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            area.setPremiseType(PremiseType.STATED);
            return area;
        }
    }
}
