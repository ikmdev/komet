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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The behavior matrix for {@link OverrideOf} — the one override property that replaces the four
 * {@code *PropertyWithOverride} variants (ike-issues#697). This test IS the safety proof for the
 * collapse: every cell that the cascade relies on is asserted on a fired <em>event</em>, not just a
 * {@code get()} value (a {@code get()}-only check is exactly what passes while the screen stays stale).
 *
 * <p>Datastore-free: it drives {@code OverrideOf} over a plain {@link SimpleEqualityBasedObjectProperty}
 * parent with simple value types ({@code String}, and an immutable {@code List} to exercise the
 * list-as-a-single-value dimension), so it needs no {@code FxGet}/{@code StampService}/datastore.
 */
class OverrideOfTest {

    private static SimpleEqualityBasedObjectProperty<String> parent(String value) {
        return new SimpleEqualityBasedObjectProperty<>(new Object(), "dimension", value);
    }

    // --- inheritance ---------------------------------------------------------------------------------

    @Test
    void notOverridden_inheritsParentValue() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        assertEquals("A", override.get(), "a fresh override inherits the parent value");
        assertFalse(override.isOverridden(), "a fresh override is not pinned");
        assertEquals("A", override.getOriginalValue(), "getOriginalValue is the parent value");
    }

    /** THE cascade-critical cell: a parent change must fire THIS property's change event when not pinned. */
    @Test
    void notOverridden_parentChange_firesChildChangeEvent() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);

        AtomicInteger fires = new AtomicInteger();
        List<String> seen = new ArrayList<>();
        override.addListener((obs, oldV, newV) -> { fires.incrementAndGet(); seen.add(newV); });

        parent.setValue("B");

        assertEquals(1, fires.get(), "an inherited (parent) change fires exactly one child change event");
        assertEquals(List.of("B"), seen, "the child publishes the new inherited value");
        assertEquals("B", override.get(), "and resolves to it");
    }

    @Test
    void notOverridden_parentChange_firesInvalidation() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        AtomicInteger invalidations = new AtomicInteger();
        override.addListener(obs -> invalidations.incrementAndGet());
        parent.setValue("B");
        assertEquals(1, invalidations.get(), "an inherited change invalidates the child");
    }

    // --- pinning (pin-wins) --------------------------------------------------------------------------

    @Test
    void setOverride_pins_returnsPinned_andFiresOnce() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        AtomicInteger fires = new AtomicInteger();
        override.addListener((obs, oldV, newV) -> fires.incrementAndGet());

        override.setValue("X");

        assertTrue(override.isOverridden(), "setting a value distinct from the parent pins an override");
        assertEquals("X", override.get(), "and resolves to the pinned value");
        assertEquals(1, fires.get(), "pinning fires exactly one change");
    }

    /** Pin-wins: while pinned, parent changes are silent and the pinned value stands. */
    @Test
    void overridden_parentChange_isSilent() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        override.setValue("X");

        AtomicInteger fires = new AtomicInteger();
        override.addListener((obs, oldV, newV) -> fires.incrementAndGet());

        parent.setValue("B");

        assertEquals(0, fires.get(), "a pinned override does not fire on a parent change (pin-wins)");
        assertEquals("X", override.get(), "and keeps its pinned value");
        assertTrue(override.isOverridden(), "and stays pinned");
    }

    @Test
    void setEqualToParent_isNotAnOverride() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        override.setValue("A"); // equals the inherited value
        assertFalse(override.isOverridden(), "pinning the inherited value is not an override");
        assertEquals("A", override.get());
    }

    // --- removing the override -----------------------------------------------------------------------

    @Test
    void removeOverride_revertsToParent_andFires() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        override.setValue("X");

        AtomicInteger fires = new AtomicInteger();
        List<String> seen = new ArrayList<>();
        override.addListener((obs, oldV, newV) -> { fires.incrementAndGet(); seen.add(newV); });

        override.removeOverride();

        assertFalse(override.isOverridden(), "removeOverride drops the pin");
        assertEquals("A", override.get(), "and reverts to the inherited value");
        assertEquals(1, fires.get(), "and publishes the revert");
        assertEquals(List.of("A"), seen);
    }

    /** The cell ObjectPropertyWithOverride missed: clearing the pin by SETTING the parent value must fire. */
    @Test
    void overridden_thenSetToParentValue_revertsAndFires() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        override.setValue("X");

        AtomicInteger fires = new AtomicInteger();
        override.addListener((obs, oldV, newV) -> fires.incrementAndGet());

        override.setValue("A"); // set to the inherited value -> clears the pin, effective value X -> A

        assertFalse(override.isOverridden(), "setting the inherited value clears the pin");
        assertEquals("A", override.get());
        assertEquals(1, fires.get(), "and fires because the effective value changed from X to A");
    }

    @Test
    void setNull_clearsOverride() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        override.setValue("X");
        override.setValue(null);
        assertFalse(override.isOverridden(), "null clears the override");
        assertEquals("A", override.get());
    }

    // --- listener symmetry / no leak -----------------------------------------------------------------

    @Test
    void removedListener_doesNotFire_afterParentChange() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        AtomicInteger fires = new AtomicInteger();
        var listener = (javafx.beans.value.ChangeListener<String>) (obs, oldV, newV) -> fires.incrementAndGet();
        override.addListener(listener);
        override.removeListener(listener);

        parent.setValue("B"); // would have fired the removed listener if it leaked

        assertEquals(0, fires.get(), "a removed change listener is not invoked (no retained subscription)");
    }

    @Test
    void removedInvalidationListener_doesNotFire() {
        var parent = parent("A");
        var override = new OverrideOf<>(parent, this);
        AtomicInteger fires = new AtomicInteger();
        javafx.beans.InvalidationListener listener = obs -> fires.incrementAndGet();
        override.addListener(listener);
        override.removeListener(listener);
        parent.setValue("B");
        assertEquals(0, fires.get(), "a removed invalidation listener is not invoked");
    }

    // --- list-as-value: order significance -----------------------------------------------------------

    /** A list dimension is held as one immutable list value; a reorder is a genuine change and fires. */
    @Test
    void listValue_reorderIsAGenuineOverride_andFires() {
        var parent = new SimpleEqualityBasedObjectProperty<List<String>>(new Object(), "preferenceList", List.of("FQN", "Regular"));
        var override = new OverrideOf<>(parent, this);

        AtomicInteger fires = new AtomicInteger();
        List<List<String>> seen = new ArrayList<>();
        override.addListener((obs, oldV, newV) -> { fires.incrementAndGet(); seen.add(newV); });

        override.setValue(List.of("Regular", "FQN")); // same elements, different order

        assertTrue(override.isOverridden(), "an order-significant reorder pins an override");
        assertEquals(List.of("Regular", "FQN"), override.get(), "the new order wins");
        assertEquals(1, fires.get(), "a reorder fires exactly one change (order-sensitive equality)");
        assertEquals(List.of(List.of("Regular", "FQN")), seen);
    }

    @Test
    void listValue_inheritedReorder_firesWhenNotOverridden() {
        var parent = new SimpleEqualityBasedObjectProperty<List<String>>(new Object(), "preferenceList", List.of("FQN", "Regular"));
        var override = new OverrideOf<>(parent, this);
        AtomicInteger fires = new AtomicInteger();
        override.addListener((obs, oldV, newV) -> fires.incrementAndGet());

        parent.setValue(List.of("Regular", "FQN")); // a parent-level reorder cascades down

        assertEquals(1, fires.get(), "an inherited list reorder fires the child (the cascade for list dimensions)");
        assertEquals(List.of("Regular", "FQN"), override.get());
        assertFalse(override.isOverridden(), "and does not pin — the child still inherits");
    }
}
