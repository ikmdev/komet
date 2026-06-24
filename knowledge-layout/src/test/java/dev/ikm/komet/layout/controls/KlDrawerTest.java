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
package dev.ikm.komet.layout.controls;

import dev.ikm.komet.layout.controls.skin.KlDrawerSkin;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Smoke tests for {@link KlDrawer} and its skin. They exercise the control on the JavaFX application
 * thread with animation disabled so the reveal snaps and the preferred-size geometry can be asserted
 * deterministically. If the JavaFX toolkit cannot start (a headless environment), the behavioral tests
 * skip rather than fail.
 */
class KlDrawerTest {

    private static boolean fxReady;

    @BeforeAll
    static void startFx() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            fxReady = latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            fxReady = true;
        } catch (Throwable cannotStart) {
            fxReady = false;
        }
    }

    /** Runs {@code body} on the FX application thread and rethrows any failure it raises. */
    private static void onFx(Runnable body) throws InterruptedException {
        assumeTrue(fxReady, "JavaFX toolkit unavailable");
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                body.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task did not complete");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    private static Region fixedContent(double prefWidth, double prefHeight) {
        Region region = new Region();
        region.setPrefSize(prefWidth, prefHeight);
        return region;
    }

    @Test
    void apiContracts() throws InterruptedException {
        onFx(() -> {
            KlDrawer drawer = new KlDrawer();
            assertEquals(Side.RIGHT, drawer.getSide(), "default side is RIGHT");
            assertFalse(drawer.isExpanded(), "default is collapsed");

            drawer.setSide(Side.LEFT);
            assertTrue(drawer.isHorizontalAxis(), "LEFT is a horizontal-axis side");
            drawer.setSide(Side.RIGHT);
            assertTrue(drawer.isHorizontalAxis(), "RIGHT is a horizontal-axis side");
            drawer.setSide(Side.TOP);
            assertFalse(drawer.isHorizontalAxis(), "TOP is a vertical-axis side");
            drawer.setSide(Side.BOTTOM);
            assertFalse(drawer.isHorizontalAxis(), "BOTTOM is a vertical-axis side");

            drawer.open();
            assertTrue(drawer.isExpanded());
            drawer.close();
            assertFalse(drawer.isExpanded());
            drawer.toggle();
            assertTrue(drawer.isExpanded());

            Region content = fixedContent(100, 50);
            drawer.setContent(content);
            assertSame(content, drawer.getContent());
            drawer.setContent((Region) null);
            assertNull(drawer.getContent());
        });
    }

    @Test
    void horizontalAxisReservesWidthOnlyWhenOpen() throws InterruptedException {
        onFx(() -> {
            KlDrawer drawer = new KlDrawer(Side.RIGHT, fixedContent(200, 100));
            drawer.setAnimated(false);
            drawer.setSkin(new KlDrawerSkin(drawer));

            drawer.setExpanded(false);
            assertEquals(0.0, drawer.prefWidth(-1), 0.5, "a closed horizontal drawer reserves no width");
            assertEquals(100.0, drawer.prefHeight(-1), 0.5, "the cross (main) axis is the content height");

            drawer.setExpanded(true);
            assertEquals(200.0, drawer.prefWidth(-1), 0.5, "an open horizontal drawer reserves the content width");
            assertEquals(100.0, drawer.prefHeight(-1), 0.5, "the cross axis is unchanged by opening");
        });
    }

    @Test
    void verticalAxisReservesHeightOnlyWhenOpen() throws InterruptedException {
        onFx(() -> {
            KlDrawer drawer = new KlDrawer(Side.BOTTOM, fixedContent(200, 100));
            drawer.setAnimated(false);
            drawer.setSkin(new KlDrawerSkin(drawer));

            drawer.setExpanded(false);
            assertEquals(0.0, drawer.prefHeight(-1), 0.5, "a closed vertical drawer reserves no height");
            assertEquals(200.0, drawer.prefWidth(-1), 0.5, "the cross (main) axis is the content width");

            drawer.setExpanded(true);
            assertEquals(100.0, drawer.prefHeight(-1), 0.5, "an open vertical drawer reserves the content height");
        });
    }

    @Test
    void contentStaysAttachedAcrossToggles() throws InterruptedException {
        onFx(() -> {
            Region content = fixedContent(200, 100);
            KlDrawer drawer = new KlDrawer(Side.RIGHT, content);
            drawer.setAnimated(false);
            drawer.setSkin(new KlDrawerSkin(drawer));

            assertTrue(drawer.getChildrenUnmodifiable().contains(content), "content attached after skinning");
            drawer.open();
            assertTrue(drawer.getChildrenUnmodifiable().contains(content), "still attached when open");
            drawer.close();
            assertTrue(drawer.getChildrenUnmodifiable().contains(content),
                    "still attached when closed (clipped, not removed)");
            drawer.open();
            assertTrue(drawer.getChildrenUnmodifiable().contains(content), "still attached after reopen");
        });
    }
}
