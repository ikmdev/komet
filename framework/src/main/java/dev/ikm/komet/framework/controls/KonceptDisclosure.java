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
package dev.ikm.komet.framework.controls;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells a {@link KonceptBadge} that a concept has more to its definition than its EL++ axioms
 * can state, so the badge leads with {@link KonceptStatus#COMPLEX}. A plugin provides this, because
 * the framework does not know where such a definition is kept.
 */
public interface KonceptDisclosure {

    /**
     * Whether a concept's definition goes beyond its EL++ axioms in this view.
     *
     * @param nid        the concept
     * @param calculator the view
     * @return {@code true} when there is more to see inside
     */
    boolean discloses(long nid, ViewCalculator calculator);

    /**
     * Whether any provider says a concept has more to its definition. A provider that fails
     * counts as no: a rendering must never fail because a status could not be determined.
     *
     * @param nid        the concept
     * @param calculator the view
     * @return {@code true} when a provider says so
     */
    static boolean any(long nid, ViewCalculator calculator) {
        for (KonceptDisclosure provider : Providers.loaded()) {
            try {
                if (provider.discloses(nid, calculator)) {
                    return true;
                }
            } catch (RuntimeException e) {
                // the concept keeps the status its axioms give it
            }
        }
        return false;
    }

    /** The providers, loaded once. A badge asks for every row of every list, so this cannot walk the plugins each time. */
    final class Providers {

        private static List<KonceptDisclosure> loaded;

        private Providers() {
        }

        private static synchronized List<KonceptDisclosure> loaded() {
            if (loaded == null) {
                try {
                    List<KonceptDisclosure> found = new ArrayList<>();
                    PluggableService.load(KonceptDisclosure.class).forEach(found::add);
                    loaded = found;
                } catch (RuntimeException | java.util.ServiceConfigurationError e) {
                    return List.of();         // the plugin layer is not up yet: ask again next time
                }
            }
            return loaded;
        }
    }
}
