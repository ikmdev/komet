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
package dev.ikm.komet.framework.view;

import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hermetic check that a description-preference <em>reorder</em> is committed as a single whole value through the
 * coordinate-override layer — the mechanism the View Options popup's {@code commitToView} relies on after the
 * mesh-collapse (ike-issues#666, #692), now that the description-type dimension is held as one whole immutable
 * value in an {@link OverrideOf} rather than a {@code ListPropertyWithOverride} (ike-issues#697).
 * <p>
 * A reorder of the same elements (only the order — the language calculator's preference order — differs) must:
 * <ul>
 *   <li>propagate (the new order wins),</li>
 *   <li>pin the override,</li>
 *   <li>publish exactly one whole-value change and <strong>never an empty-list "dummy" sentinel</strong> — the old
 *       live write-back poked {@code FXCollections.emptyObservableList()} to force a change event; that is gone, so
 *       a reorder must propagate without it, and</li>
 *   <li>revert the override when edited back to the parent order, again with no dummy.</li>
 * </ul>
 * Datastore-free (no {@code FxGet}/{@code StampService}, no datastore): it drives an {@code OverrideOf} over an
 * {@link ImmutableList} directly, exactly as {@code commitToView} does via {@code setValue} on the language
 * coordinate's {@code descriptionTypePreferenceList}. The generic firing/inheritance contract of {@code OverrideOf}
 * itself is proved by {@code OverrideOfTest}; this test pins the description-type intent to that mechanism.
 */
class DescriptionPreferenceReorderTest {

    // Raw concept proxies — no datastore resolution required, only nid identity (mirrors FQN / Regular name types).
    private static final ConceptFacade FQN = EntityProxy.Concept.make(-2_000_001);
    private static final ConceptFacade REGULAR = EntityProxy.Concept.make(-2_000_002);

    @Test
    void reorderCommitsAsWholeValue_neverADummySentinel_andRevertsToParent() {
        SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> parent =
                new SimpleEqualityBasedObjectProperty<>(this, "descriptionTypePreferenceList", Lists.immutable.of(FQN, REGULAR));
        OverrideOf<ImmutableList<ConceptFacade>> override = new OverrideOf<>(parent, this);

        // (a) a fresh override is a pure pass-through to the parent's order.
        assertEquals(Lists.immutable.of(FQN, REGULAR), override.get(), "fresh override inherits the parent order");
        assertFalse(override.isOverridden(), "fresh override is not pinned");

        // Spy: record every value the override publishes, so we can assert no dummy sentinel and a single write.
        List<ImmutableList<ConceptFacade>> published = new ArrayList<>();
        override.addListener((obs, old, val) -> published.add(val));

        // (b) commit a reorder as a whole value — exactly what commitToView does for description-type.
        override.setValue(Lists.immutable.of(REGULAR, FQN));
        assertEquals(Lists.immutable.of(REGULAR, FQN), override.get(), "the reordered order wins");
        assertTrue(override.isOverridden(), "a reorder pins the override");
        assertEquals(1, published.size(), "exactly one whole-value change event — no dummy pre-write");
        assertFalse(published.stream().anyMatch(v -> v != null && v.isEmpty()),
                "no empty-list dummy sentinel is ever published");

        // (c) editing back to the parent order reverts the override (no dummy, whole-value revert to parent).
        override.setValue(Lists.immutable.of(FQN, REGULAR));
        assertFalse(override.isOverridden(), "editing back to the parent order clears the override");
        assertEquals(Lists.immutable.of(FQN, REGULAR), override.get(), "the reverted override tracks the parent");
        assertFalse(published.stream().anyMatch(v -> v != null && v.isEmpty()),
                "still no empty-list dummy sentinel after revert");
    }
}
