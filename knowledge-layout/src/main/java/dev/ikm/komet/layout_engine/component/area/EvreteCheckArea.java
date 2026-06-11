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

import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.check.CheckResult;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.AbstractCheckArea;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.stream.Collectors;

/**
 * A check area backed by the Komet rules engine ({@code RuleService} / Evrete).
 *
 * <p>When run, it presents the focused item to the rules engine as a
 * {@link Topic#COMPONENT_FOCUSED} observation and inspects the resulting
 * {@link Consequence consequences} — the corrective actions the rules suggest for that item.
 * The verdict mapping is:
 * <ul>
 *   <li><b>PASS</b> — the engine returned no consequences (nothing flagged).</li>
 *   <li><b>FAIL</b> — the engine suggested one or more actions; the detail line names the rules
 *       that fired.</li>
 * </ul>
 *
 * <p>This rides the existing framework SPI {@link RuleService#get()} (provided by
 * {@code EvreteRulesService} in the {@code dev.ikm.komet.rules} module); no Evrete dependency is
 * introduced here, and the provider is discovered at runtime.
 */
public class EvreteCheckArea extends AbstractCheckArea {

    private static final String CHECK_TITLE = "Rules check";
    private static final String KNOWLEDGE_BASE_NAME = "Komet rules";

    private EvreteCheckArea(KometPreferences preferences) {
        super(preferences);
        init();
    }

    private EvreteCheckArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
        init();
    }

    private void init() {
        setCheckTitle(CHECK_TITLE);
        fxObject().setId("EvreteCheckArea");
    }

    @Override
    protected CheckResult evaluate(EntityFacade item, ViewProperties viewProperties) {
        if (viewProperties == null) {
            return CheckResult.error("No view available to run rules.");
        }
        Latest<EntityVersion> latest = viewProperties.calculator().latest(item.nid());
        if (!latest.isPresent()) {
            return CheckResult.unknown("No version of the focused item is visible in this view.");
        }
        ObservationRecord observation =
                new ObservationRecord(Topic.COMPONENT_FOCUSED, latest.get(), Measures.present());
        ImmutableList<Consequence<?>> consequences = RuleService.get().execute(
                KNOWLEDGE_BASE_NAME,
                Lists.immutable.of(observation),
                viewProperties,
                viewProperties.nodeView().editCoordinate());

        if (consequences.isEmpty()) {
            return CheckResult.pass("No rule findings.");
        }
        String rules = consequences.stream()
                .map(Consequence::ruleMethod)
                .distinct()
                .collect(Collectors.joining(", "));
        return CheckResult.fail(consequences.size() + " rule finding(s): " + rules);
    }

    /**
     * Returns a new factory for this area.
     *
     * @return a {@link Factory}
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Restores an area from preferences.
     *
     * @param preferences the preferences node
     * @return the restored area
     */
    public static EvreteCheckArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    /**
     * Creates a new area with the given grid settings.
     *
     * @param preferencesFactory factory for the area's preferences node
     * @param areaGridSettings   the grid placement for the new area
     * @return the new area
     */
    public static EvreteCheckArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    /**
     * {@code ServiceLoader}-discoverable factory for {@link EvreteCheckArea}.
     */
    public static class Factory implements SupplementalAreaBlueprint.Factory<EvreteCheckArea> {

        /**
         * Restores an area from preferences.
         *
         * @param preferences the preferences node
         * @return the restored area
         */
        @Override
        public EvreteCheckArea restore(KometPreferences preferences) {
            return new EvreteCheckArea(preferences);
        }

        /**
         * Creates a new area with the given grid settings.
         *
         * @param preferencesFactory factory for the area's preferences node
         * @param areaGridSettings   the grid placement for the new area
         * @return the new area
         */
        @Override
        public EvreteCheckArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EvreteCheckArea area = new EvreteCheckArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            return area;
        }
    }
}
