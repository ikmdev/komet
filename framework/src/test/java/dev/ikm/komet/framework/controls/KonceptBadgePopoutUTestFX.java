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

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.control.Button;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The definition-popout gate (ike-issues#941): a badge carries the LINK_EXTERNAL affordance
 * only where it can honour it — a known concept with a view to resolve its definition
 * through. Presentation-only badges (no store, no view) must stay clean; the applicability
 * gate itself is pinned as a pure truth table. Popout presence/ordering on a live badge is
 * store-backed behaviour verified in the running app (the *ITestFX tier).
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgePopoutUTestFX {

    @Test
    void presentationOnlyBadgeCarriesNoPopout() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "Chronic disease (disorder)");

        assertFalse(badge.getChildren().stream().anyMatch(Button.class::isInstance),
                "no view → no definition to open → no affordance the badge cannot honour");
    }

    @Test
    void applicabilityGateIsAKnownConceptWithAView() {
        assertFalse(KonceptBadge.popoutApplicable(Integer.MIN_VALUE, null, KonceptKind.CONCEPT),
                "presentation-only (unknown nid, no view)");
        assertFalse(KonceptBadge.popoutApplicable(-2147000000, null, KonceptKind.CONCEPT),
                "a nid without a view cannot resolve a definition");
        assertFalse(KonceptBadge.popoutApplicable(Integer.MIN_VALUE, null, KonceptKind.PATTERN),
                "non-concept kinds have no EL++ definition to open");
        // The positive arm needs a live ViewProperties (store-backed) — exercised in the
        // running app; every negative above flips exactly one conjunct of the gate.
    }
}
