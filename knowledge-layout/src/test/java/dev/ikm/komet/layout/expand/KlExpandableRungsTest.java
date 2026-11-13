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
package dev.ikm.komet.layout.expand;

import dev.ikm.komet.layout.HeadlessFx;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The rungs a figure chooses to use, and what its expansion is tied to: a figure that needs more
 * room than its nearest hosts have opens in the first rung it keeps and collapses from there, and
 * a figure that is rebuilt while expanded stays expanded when its expansion is tied to a host.
 */
class KlExpandableRungsTest {

    private static boolean fxReady;

    @BeforeAll
    static void startFx() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HeadlessFx.select();
            Platform.startup(latch::countDown);
            fxReady = latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            fxReady = true;
        } catch (Throwable cannotStart) {
            fxReady = false;
        }
    }

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

    /** A document in a card in a journal window, as the three hosts a block figure has above it. */
    private static final class Place {
        final Region figure = new Region();
        final StackPane home = new StackPane();
        final BorderPane document = new BorderPane();
        final BorderPane card = new BorderPane();
        final BorderPane window = new BorderPane();

        Place() {
            KlExpansionHost.mark(document, () -> "Document", false);
            KlExpansionHost.mark(card, () -> "Card", false);
            KlExpansionHost.mark(window, () -> "Journal window", true);
            card.setCenter(document);
            window.setCenter(card);
            new Scene(window, 900, 600);
        }

        void place(KlExpandable expandable) {
            home.getChildren().setAll(expandable.withExpandAffordance());
            document.setCenter(home);
        }

        /** The pane the expansion's overlay is in, or null when no overlay is showing. */
        BorderPane filled() {
            for (BorderPane pane : List.of(document, card, window)) {
                if (pane.lookup(".kl-expansion-overlay") != null && pane.getChildren().stream()
                        .anyMatch(child -> child.getStyleClass().contains("kl-expansion-overlay"))) {
                    return pane;
                }
            }
            return null;
        }

        String reduceText() {
            return buttonText(".kl-expansion-reduce");
        }

        String furtherText() {
            return buttonText(".kl-expansion-further");
        }

        /** A button of the expansion's bar, which is in the scene graph once the bar has its skin. */
        private String buttonText(String styleClass) {
            window.applyCss();
            window.layout();
            return window.lookup(styleClass) instanceof Button button ? button.getText() : null;
        }
    }

    /** A figure that uses the rungs and the anchor it is given. */
    private static KlExpandable figure(Place place, boolean topRungOnly, Node anchor) {
        return new KlExpandable() {
            @Override
            public Region figureNode() {
                return place.figure;
            }

            @Override
            public Region expandedView() {
                return new Region();
            }

            @Override
            public List<KlExpansionHost.Rung> expansionRungs(List<KlExpansionHost.Rung> hosts) {
                return topRungOnly && !hosts.isEmpty() ? List.of(hosts.getLast()) : hosts;
            }

            @Override
            public Node expansionAnchor() {
                return anchor == null ? place.figure : anchor;
            }
        };
    }

    @Test
    @DisplayName("A figure that keeps every host opens in the nearest and names the next")
    void everyHostIsARungByDefault() throws InterruptedException {
        onFx(() -> {
            Place place = new Place();
            KlExpandable expandable = figure(place, false, null);
            place.place(expandable);
            expandable.expand();
            assertTrue(expandable.isExpanded());
            assertEquals(place.document, place.filled(), "the nearest host");
            assertEquals("↙ Collapse", place.reduceText());
            assertEquals("Expand further → Card", place.furtherText());
        });
    }

    @Test
    @DisplayName("A figure that keeps only the window opens there, and its Reduce collapses it")
    void aFigureOpensInTheFirstRungItKeeps() throws InterruptedException {
        onFx(() -> {
            Place place = new Place();
            KlExpandable expandable = figure(place, true, null);
            place.place(expandable);
            expandable.expand();
            assertEquals(place.window, place.filled(), "the hosts too small for it are not rungs");
            assertEquals("↙ Collapse", place.reduceText(), "there is no rung beneath to reduce to");
            assertEquals("Expand further", place.furtherText(), "this figure does not ask for full screen");
            expandable.stepDown();
            // collapsing fades for 140 ms; the expansion is over when the fade is
        });
        Thread.sleep(400);
        onFx(() -> { });
    }

    @Test
    @DisplayName("Stepping down from the only rung a figure keeps collapses it to its place")
    void steppingDownFromTheOnlyRungCollapses() throws InterruptedException {
        Place place = new Place();
        AtomicReference<KlExpandable> made = new AtomicReference<>();
        onFx(() -> {
            made.set(figure(place, true, null));
            place.place(made.get());
            made.get().expand();
            made.get().stepDown();
        });
        Thread.sleep(400);
        onFx(() -> {
            assertFalse(made.get().isExpanded());
            assertNull(place.filled(), "no overlay is left in any host");
        });
    }

    @Test
    @DisplayName("An expansion tied to its figure ends when the figure is rebuilt")
    void anExpansionTiedToItsFigureEndsWithIt() throws InterruptedException {
        onFx(() -> {
            Place place = new Place();
            KlExpandable expandable = figure(place, true, null);
            place.place(expandable);
            expandable.expand();
            assertNotNull(place.filled());
            place.home.getChildren().clear();                    // the document draws the block again
            assertFalse(expandable.isExpanded());
            assertNull(place.filled());
        });
    }

    @Test
    @DisplayName("An expansion tied to a host stays when the figure is rebuilt, and ends when the host goes")
    void anExpansionTiedToAHostOutlivesItsFigure() throws InterruptedException {
        onFx(() -> {
            Place place = new Place();
            KlExpandable expandable = figure(place, true, place.document);
            place.place(expandable);
            expandable.expand();
            place.home.getChildren().clear();                    // the document draws the block again
            assertTrue(expandable.isExpanded(), "the expansion is tied to the document's pane, not to the block");
            assertEquals(place.window, place.filled());
            place.card.setCenter(null);                          // the document's pane leaves the window
            assertFalse(expandable.isExpanded());
            assertNull(place.filled());
        });
    }
}
