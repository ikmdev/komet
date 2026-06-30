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

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies depth-independent coordinate override nesting (ike-issues#663).
 * <p>
 * A three-level chain — {@code KB (NoOverride) → Journal (WithOverride) → Inner (WithOverride)} —
 * built on the stamp coordinate (the same {@code …WithOverride} machinery the view coordinate
 * composes) exercises override-on-override nesting (formerly blocked by a guard in every
 * {@code …WithOverride} constructor) and {@code pin-wins} retention: a pinned override survives an
 * ancestor change even when it comes to coincide with the new parent value.
 * <p>
 * Assertions read the override <em>resolution</em> — {@code timeProperty().get()} resolving through
 * the nested overrides — rather than the observation-driven record cache ({@code time()}), so the
 * test is hermetic: it needs no datastore and no active observers, and is order-independent in the
 * full suite. (Record propagation to observers is the view coordinate's {@code overriddenBaseChanged}
 * path — registered unconditionally — and is exercised at runtime.)
 */
class ObservableViewCascadeTest {

    /** A minimal, datastore-free stamp coordinate at the given time (raw path nid, no modules). */
    private static StampCoordinateRecord stampAt(long time) {
        return StampCoordinateRecord.make(
                StateSet.ACTIVE,
                StampPositionRecord.make(time, 1),
                IntIds.set.empty(),
                IntIds.set.empty(),
                IntIds.list.empty());
    }

    @Test
    void overrideMayWrapOverride_andResolvesThroughTheChain() {
        ObservableStampCoordinateNoOverride kb = new ObservableStampCoordinateNoOverride(stampAt(1000L));
        // #663: these two nestings previously threw "Cannot override an overridden Coordinate."
        ObservableStampCoordinateWithOverride journal = new ObservableStampCoordinateWithOverride(kb);
        ObservableStampCoordinateWithOverride inner = new ObservableStampCoordinateWithOverride(journal);

        // (a) a freshly created level is a pure pass-through: inner resolves through journal to KB.
        assertEquals(1000L, inner.timeProperty().get(), "fresh inner should inherit KB's time");
        assertFalse(journal.hasOverrides(), "fresh journal has no overrides");
        assertFalse(inner.hasOverrides(), "fresh inner has no overrides");

        // (b) a KB change is seen through journal at inner's non-overridden dimension.
        kb.timeProperty().set(2000L);
        assertEquals(2000L, inner.timeProperty().get(), "KB time change should resolve through to inner");

        // (c) pin journal's time: inner resolves to the pin, and a later KB change cannot move it.
        journal.timeProperty().set(5000L);
        assertTrue(journal.hasOverrides(), "journal is now pinned");
        assertEquals(5000L, inner.timeProperty().get(), "inner inherits journal's pin");
        kb.timeProperty().set(3000L);
        assertEquals(5000L, inner.timeProperty().get(), "journal's pin shields inner from the KB change");

        // (d) pin inner; it survives an ancestor change that makes it coincide with the parent.
        inner.timeProperty().set(7000L);
        assertTrue(inner.timeProperty().isOverridden(), "inner is now pinned");
        journal.timeProperty().set(7000L); // ancestor now equals inner's pin
        assertEquals(7000L, inner.timeProperty().get(), "inner's pin persists (pin-wins)");
        assertTrue(inner.timeProperty().isOverridden(),
                "inner's override flag survives coincidence with the ancestor");

        // (e) an explicit revert on inner falls back to the effective parent (journal).
        inner.timeProperty().removeOverride();
        assertFalse(inner.timeProperty().isOverridden(), "inner reverted");
        assertEquals(journal.timeProperty().get(), inner.timeProperty().get(),
                "reverted inner tracks journal's effective value");
    }

    /**
     * {@code setOverrides} (the re-apply side of persist/restore) pins only the dimensions that differ from
     * the parent and leaves the matching ones inherited, and a pinned dimension wins over a later parent
     * change. Exercised on the stamp {@code WithOverride} — the same machinery the view coordinate composes.
     */
    @Test
    void setOverrides_pinsOnlyTheDelta_andPinWinsOverParentChange() {
        ObservableStampCoordinateNoOverride kb = new ObservableStampCoordinateNoOverride(stampAt(1000L));
        ObservableStampCoordinateWithOverride child = new ObservableStampCoordinateWithOverride(kb);
        assertFalse(child.timeProperty().isOverridden(), "a fresh child inherits");

        // Re-apply a coordinate whose only delta from the parent is the time (path + modules match).
        child.setOverrides(stampAt(5000L));
        assertTrue(child.timeProperty().isOverridden(), "the differing dimension (time) is pinned");
        assertEquals(5000L, child.timeProperty().get(), "the pinned time is applied");
        assertFalse(child.moduleSpecificationsProperty().isOverridden(),
                "a dimension equal to the parent (empty modules) stays inherited — not spuriously pinned");

        kb.timeProperty().set(2000L);
        assertEquals(5000L, child.timeProperty().get(), "the pin shields the child from the parent change");
    }

    /**
     * {@code setOverrides} applying the parent's own value to a dimension does <em>not</em> pin it — the
     * dimension keeps tracking the parent (the cascade is preserved for everything not genuinely overridden).
     */
    @Test
    void setOverrides_valueEqualToParentLeavesDimensionInheriting() {
        ObservableStampCoordinateNoOverride kb = new ObservableStampCoordinateNoOverride(stampAt(1000L));
        ObservableStampCoordinateWithOverride child = new ObservableStampCoordinateWithOverride(kb);

        child.setOverrides(stampAt(1000L)); // equal to the parent → must not become a pin
        assertFalse(child.timeProperty().isOverridden(), "a value equal to the parent is not an override");

        kb.timeProperty().set(3000L);
        assertEquals(3000L, child.timeProperty().get(), "the non-pinned dimension tracks the parent");
    }

    /**
     * {@code setOverrides} re-applied with a dimension value equal to the parent clears a pre-existing pin on
     * that dimension — so re-applying a captured override never strands a stale pin.
     */
    @Test
    void setOverrides_clearsAPreviouslyPinnedDimensionWhenReappliedEqualToParent() {
        ObservableStampCoordinateNoOverride kb = new ObservableStampCoordinateNoOverride(stampAt(1000L));
        ObservableStampCoordinateWithOverride child = new ObservableStampCoordinateWithOverride(kb);

        child.timeProperty().set(5000L); // pin the time
        assertTrue(child.timeProperty().isOverridden(), "time is pinned");

        child.setOverrides(stampAt(1000L)); // re-apply equal to the parent → drops the pin
        assertFalse(child.timeProperty().isOverridden(), "re-applying the parent value drops the pin");
        assertEquals(1000L, child.timeProperty().get(), "and the dimension resolves to the parent again");
    }
}
