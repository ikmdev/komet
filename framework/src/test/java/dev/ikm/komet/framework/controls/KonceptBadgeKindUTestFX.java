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
import javafx.css.PseudoClass;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the component-kind sigil slot and the contextual alarm on {@link KonceptBadge}
 * (ike-issues#638), using presentation-only badges (no store) with explicit {@link #setKind}.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeKindUTestFX {

    private static final PseudoClass ALARM = PseudoClass.getPseudoClass("alarm");

    /** A presentation-only badge for a made-up concept (no view to resolve a real kind). */
    private static KonceptBadge badge() {
        return new KonceptBadge(PublicIds.newRandom(), "SOME CONCEPT");
    }

    /** The badge's sigil slot (its leading child). */
    private static HBox sigilBox(KonceptBadge badge) {
        return (HBox) badge.getChildrenUnmodifiable().get(0);
    }

    @Test
    void aPresentationBadgeIsABareConcept() {
        KonceptBadge badge = badge();
        assertEquals(ComponentKind.CONCEPT, badge.getKind());
        assertFalse(badge.isConceptViolation(), "a concept badge carries no sigil");
        assertTrue(sigilBox(badge).getChildrenUnmodifiable().isEmpty(), "no sigil node for a concept");
        assertFalse(sigilBox(badge).isManaged(), "the empty sigil slot collapses");
    }

    @Test
    void stampShowsThePentagonSigil() {
        KonceptBadge badge = badge();
        badge.setKind(ComponentKind.STAMP);
        assertEquals(ComponentKind.STAMP, badge.getKind());
        assertTrue(badge.isConceptViolation());
        assertInstanceOf(StampSigil.class, sigilBox(badge).getChildrenUnmodifiable().get(0),
                "a stamp's sigil is the pentagon, not a letter");
    }

    @Test
    void letterKindsShowTheirColouredGlyph() {
        KonceptBadge badge = badge();

        badge.setKind(ComponentKind.DESCRIPTION);
        Text d = assertInstanceOf(Text.class, sigilBox(badge).getChildrenUnmodifiable().get(0));
        assertEquals("D", d.getText());
        assertEquals(Color.web(ComponentKind.DESCRIPTION.colorHex()), d.getFill(),
                "the sigil colour is the kind's own colour (data)");

        badge.setKind(ComponentKind.SEMANTIC);
        assertEquals("S", ((Text) sigilBox(badge).getChildrenUnmodifiable().get(0)).getText());

        badge.setKind(ComponentKind.UNKNOWN);
        assertEquals("?", ((Text) sigilBox(badge).getChildrenUnmodifiable().get(0)).getText());
    }

    @Test
    void settingKindBackToConceptClearsTheSigil() {
        KonceptBadge badge = badge();
        badge.setKind(ComponentKind.PATTERN);
        assertFalse(sigilBox(badge).getChildrenUnmodifiable().isEmpty());

        badge.setKind(ComponentKind.CONCEPT);
        assertTrue(sigilBox(badge).getChildrenUnmodifiable().isEmpty());
        assertFalse(badge.isConceptViolation());
    }

    @Test
    void alarmTriggersOnlyForANonConceptInAConceptExpectingContext() {
        KonceptBadge badge = badge();

        // Concept-expecting context, but a bare concept → no alarm.
        badge.setConceptExpected(true);
        badge.setKind(ComponentKind.CONCEPT);
        assertFalse(badge.getPseudoClassStates().contains(ALARM));

        // A non-concept in that context → alarm.
        badge.setKind(ComponentKind.STAMP);
        assertTrue(badge.getPseudoClassStates().contains(ALARM), "a sigil in a concept slot is the alarm");

        // Switching back to concept clears it.
        badge.setKind(ComponentKind.CONCEPT);
        assertFalse(badge.getPseudoClassStates().contains(ALARM));

        // In a mixed context (not concept-expecting) the same sigil is merely informative — no alarm.
        badge.setConceptExpected(false);
        badge.setKind(ComponentKind.DESCRIPTION);
        assertFalse(badge.getPseudoClassStates().contains(ALARM));
    }
}
