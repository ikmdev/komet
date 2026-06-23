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

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ObservableViewWithOverride#setOverrides} against the starter dataset (per
 * the TESTING standard: composed coordinate behavior is integration-tested, not faked with hermetic
 * records). These exercise the exact persist/restore round-trip the layout-engine Card relies on — capture
 * a view's resolved coordinate while it carries overrides, then re-apply that record onto a <em>fresh</em>
 * override view via {@code setOverrides} — over a real default {@link ViewCoordinateRecord}, across more than
 * one constituent coordinate, and confirm that dimensions which were not pinned keep tracking the parent
 * (the journal cascade).
 *
 * <p>The capture step reads {@code getValue()} (the composite record), which is only fresh while the view is
 * <em>listening</em>; the Card keeps its view listening via its re-render listener, so the source view here
 * attaches an observer to reproduce that condition faithfully — and asserting that the captured record
 * carries the pin is what proves the Card's capture path is sound.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableViewSetOverridesITestFX {

    private static final File STARTER = new File("target/data", "tinkar-starter-data-reasoned-pb.zip");

    @BeforeAll
    void startStore() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    @RunOnJavaFXThread
    @Order(1)
    void loadStarterData() {
        if (STARTER.exists()) {
            LoadEntitiesFromProtobufFile load = new LoadEntitiesFromProtobufFile(STARTER);
            assertTrue(load.compute().getTotalCount() > 0, "starter data should load entities");
        }
    }

    @Test
    @RunOnJavaFXThread
    @Order(2)
    void setOverrides_roundTripsPinsAcrossConstituents_andLeavesUnpinnedDimensionsTracking() {
        ViewCoordinateRecord defaultView = Coordinates.View.DefaultView();
        ObservableViewNoOverride parent = new ObservableViewNoOverride(defaultView);

        // Create overrides the way the View popup does — set dimensions on a child override view — across two
        // different constituent coordinates: the stamp time and the navigation sort-vertices flag.
        ObservableViewWithOverride source = new ObservableViewWithOverride(parent);
        source.addListener((obs, oldValue, newValue) -> { });   // keep it listening, as the Card's view is
        long pinnedTime = defaultView.stampCoordinate().stampPosition().time() - 1000L;
        boolean flippedSort = !source.navigationCoordinate().sortVerticesProperty().get();
        source.stampCoordinate().timeProperty().set(pinnedTime);
        source.navigationCoordinate().sortVerticesProperty().set(flippedSort);
        assertTrue(source.hasOverrides(), "the source view now carries overrides");

        // Capture the resolved coordinate (what the Card persists). Asserting the pin survived the capture
        // proves the Card's getValue()-based capture path is sound.
        ViewCoordinateRecord captured = source.getValue();
        assertEquals(pinnedTime, captured.stampCoordinate().stampPosition().time(),
                "the captured record carries the pinned stamp time");

        // Re-apply onto a FRESH override view (what the Card does at the next bind).
        ObservableViewWithOverride restored = new ObservableViewWithOverride(parent);
        assertFalse(restored.hasOverrides(), "a fresh override view starts with no pins");
        restored.setOverrides(captured);

        // Both pinned dimensions round-tripped — across two different constituent coordinates.
        assertTrue(restored.stampCoordinate().timeProperty().isOverridden(), "stamp time re-pinned");
        assertEquals(pinnedTime, restored.stampCoordinate().timeProperty().get(), "stamp time value restored");
        assertTrue(restored.navigationCoordinate().sortVerticesProperty().isOverridden(),
                "navigation sortVertices re-pinned");
        assertEquals(flippedSort, restored.navigationCoordinate().sortVerticesProperty().get(),
                "navigation sortVertices value restored");

        // A dimension that was NOT pinned stays inherited — it still resolves to the parent (journal).
        assertFalse(restored.logicCoordinate().classifierProperty().isOverridden(),
                "an unpinned dimension is not spuriously pinned by setOverrides");
        assertEquals(parent.logicCoordinate().classifierProperty().get().nid(),
                restored.logicCoordinate().classifierProperty().get().nid(),
                "the unpinned dimension resolves through to the parent");
    }

    @Test
    @RunOnJavaFXThread
    @Order(3)
    void afterSetOverrides_pinWinsWhileUnpinnedDimensionsFollowTheParent() {
        ViewCoordinateRecord defaultView = Coordinates.View.DefaultView();
        ObservableViewNoOverride parent = new ObservableViewNoOverride(defaultView);

        ObservableViewWithOverride source = new ObservableViewWithOverride(parent);
        source.addListener((obs, oldValue, newValue) -> { });
        long pinnedTime = defaultView.stampCoordinate().stampPosition().time() - 5000L;
        source.stampCoordinate().timeProperty().set(pinnedTime);   // pin ONLY the stamp time
        ViewCoordinateRecord captured = source.getValue();

        ObservableViewWithOverride restored = new ObservableViewWithOverride(parent);
        restored.setOverrides(captured);
        boolean originalSort = restored.navigationCoordinate().sortVerticesProperty().get();
        assertFalse(restored.navigationCoordinate().sortVerticesProperty().isOverridden(),
                "navigation sortVertices was not pinned, so it stays inherited");

        // Move the parent (the journal coordinate). The pinned stamp time holds (pin-wins); the unpinned
        // navigation dimension follows the parent, resolving through.
        parent.navigationCoordinate().sortVerticesProperty().set(!originalSort);
        parent.stampCoordinate().timeProperty().set(pinnedTime + 9999L);

        assertEquals(pinnedTime, restored.stampCoordinate().timeProperty().get(),
                "the pinned stamp time is shielded from the parent change (pin-wins)");
        assertEquals(!originalSort, restored.navigationCoordinate().sortVerticesProperty().get(),
                "the unpinned navigation dimension tracks the parent change (cascade preserved)");
    }

    /**
     * The persistence delta round-trip (ike-issues#745): when the parent (journal) coordinate changes
     * <em>between sessions</em>, re-applying a captured override must re-pin only the genuinely-pinned
     * dimensions and let the merely-inherited ones track the NEW parent — not freeze them at their stale
     * captured values. {@link ObservableViewWithOverride#setOverridesFromDelta} achieves this from the
     * captured {@code (getValue(), getOriginalValue())} pair; the old whole-value
     * {@link ObservableViewWithOverride#setOverrides} does not.
     */
    @Test
    @RunOnJavaFXThread
    @Order(4)
    void setOverridesFromDelta_reAppliesOnlyPinnedDimensions_soInheritedOnesTrackAChangedParent() {
        ViewCoordinateRecord defaultView = Coordinates.View.DefaultView();
        ObservableViewNoOverride parent = new ObservableViewNoOverride(defaultView);

        // Pin ONLY the stamp time on the source; navigation sortVertices stays inherited.
        ObservableViewWithOverride source = new ObservableViewWithOverride(parent);
        source.addListener((obs, oldValue, newValue) -> { });   // keep it listening, as the Card's view is
        long pinnedTime = defaultView.stampCoordinate().stampPosition().time() - 7000L;
        boolean inheritedSortAtCapture = source.navigationCoordinate().sortVerticesProperty().get();
        source.stampCoordinate().timeProperty().set(pinnedTime);

        // Capture the delta pair the Card persists: the resolved coordinate AND the inherited parent baseline.
        ViewCoordinateRecord resolved = source.getValue();
        ViewCoordinateRecord baseline = source.getOriginalValue();

        // The journal coordinate CHANGES between sessions: flip the (unpinned) navigation sortVertices on the parent.
        parent.navigationCoordinate().sortVerticesProperty().set(!inheritedSortAtCapture);

        // Re-apply as a delta onto a fresh child of the (now-changed) parent.
        ObservableViewWithOverride restored = new ObservableViewWithOverride(parent);
        restored.setOverridesFromDelta(resolved, baseline);

        // The genuinely-pinned dimension re-pins to its captured value (pin-wins).
        assertTrue(restored.stampCoordinate().timeProperty().isOverridden(), "the pinned stamp time re-pinned");
        assertEquals(pinnedTime, restored.stampCoordinate().timeProperty().get(), "the pinned stamp time value restored");

        // The merely-inherited dimension is NOT frozen as a spurious pin — it tracks the CHANGED parent.
        assertFalse(restored.navigationCoordinate().sortVerticesProperty().isOverridden(),
                "an inherited dimension is not spuriously re-pinned when the parent changed between sessions");
        assertEquals(!inheritedSortAtCapture, restored.navigationCoordinate().sortVerticesProperty().get(),
                "the inherited dimension resolves through to the changed parent (cascade preserved across restore)");

        // Contrast: the whole-value setOverrides WOULD freeze the inherited dimension as a spurious pin against
        // the changed parent — exactly the persistence bug the delta re-apply fixes.
        ObservableViewWithOverride wholeValue = new ObservableViewWithOverride(parent);
        wholeValue.setOverrides(resolved);
        assertTrue(wholeValue.navigationCoordinate().sortVerticesProperty().isOverridden(),
                "whole-value setOverrides freezes the inherited dimension as a spurious pin (the bug delta avoids)");
    }
}
