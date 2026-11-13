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
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The expand icon of a figure: hidden until the pointer is near its corner by default, as a video
 * player's is, and drawn always for a figure that asks for that, where a reader must see that the
 * figure can be made larger.
 */
class KlExpandIconTest {

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

    /** A figure 400 by 300, whose icon is drawn always when it says so. */
    private static KlExpandable figure(boolean alwaysShown) {
        Region figure = new Region();
        figure.setPrefSize(400, 300);
        return new KlExpandable() {
            @Override
            public Region figureNode() {
                return figure;
            }

            @Override
            public Region expandedView() {
                return new Region();
            }

            @Override
            public boolean expandIconAlwaysShown() {
                return alwaysShown;
            }

            @Override
            public ExpansionCorner expansionCorner() {
                return alwaysShown ? ExpansionCorner.TOP_RIGHT : ExpansionCorner.DEFAULT;
            }

            @Override
            public Insets expandIconInsets() {
                return alwaysShown ? Insets.EMPTY : new Insets(6);
            }

            @Override
            public ExpansionCorner contractionCorner() {
                // the figure with the icon at its top names another corner for the way back
                return alwaysShown ? ExpansionCorner.BOTTOM_RIGHT : KlExpandable.super.contractionCorner();
            }
        };
    }

    /** A figure in a pane of its own, in a window it can expand into. */
    private static BorderPane windowWith(KlExpandable expandable) {
        // the pane a figure stands in is its home, and not a place it expands into: the window around it is
        BorderPane window = new BorderPane(new StackPane(expandable.withExpandAffordance()));
        KlExpansionHost.mark(window, () -> "Window", false);
        new Scene(window, 600, 400);
        return window;
    }

    private static Button icon(Region host) {
        return (Button) host.lookup(".kl-expand-icon");
    }

    @Test
    @DisplayName("By default the icon is hidden, takes no click while hidden, and is at the bottom right")
    void theIconIsHiddenUntilThePointerIsNearByDefault() throws InterruptedException {
        onFx(() -> {
            Region host = figure(false).withExpandAffordance();
            Button icon = icon(host);
            assertEquals(0, icon.getOpacity(), 1e-9);
            assertTrue(icon.isMouseTransparent(), "a hidden icon does not take a click meant for the figure");
            assertEquals(Pos.BOTTOM_RIGHT, StackPane.getAlignment(icon));
            assertFalse(icon.getStyleClass().contains(KlExpandable.SHOWN_STYLE_CLASS));
            assertEquals("Expand", icon.getTooltip().getText());
        });
    }

    @Test
    @DisplayName("A figure that asks for it has its icon drawn always, in the corner it names, large enough to click")
    void aFigureCanHaveItsIconDrawnAlways() throws InterruptedException {
        onFx(() -> {
            Region host = figure(true).withExpandAffordance();
            new Scene(new BorderPane(host), 600, 400);
            host.getParent().applyCss();
            host.getParent().layout();
            Button icon = icon(host);
            assertEquals(1, icon.getOpacity(), 1e-9, "drawn with no pointer near it");
            assertFalse(icon.isMouseTransparent());
            assertTrue(icon.getStyleClass().contains(KlExpandable.SHOWN_STYLE_CLASS));
            assertEquals(Pos.TOP_RIGHT, StackPane.getAlignment(icon));
            assertEquals("Expand", icon.getTooltip().getText());
            assertTrue(icon.getWidth() >= 24 && icon.getHeight() >= 24,
                    "a pointer target of 24 by 24 or more: " + icon.getWidth() + " by " + icon.getHeight());
            Bounds at = icon.getBoundsInParent();
            assertEquals(host.getWidth(), at.getMaxX(), 0.5, "at the right edge");
            assertEquals(0, at.getMinY(), 0.5, "at the top edge");
        });
    }

    @Test
    @DisplayName("The contract icon of an expanded figure is in the corner it was expanded from, unless the figure names another")
    void theContractIconIsWhereTheFigureSays() throws InterruptedException {
        onFx(() -> {
            KlExpandable byDefault = figure(false);
            BorderPane window = windowWith(byDefault);
            byDefault.expand();
            assertTrue(byDefault.isExpanded());
            assertEquals(Pos.BOTTOM_RIGHT, StackPane.getAlignment(window.lookup(".kl-contract-icon")),
                    "the corner that expanded the figure contracts it again");

            KlExpandable named = figure(true);
            BorderPane other = windowWith(named);
            named.expand();
            assertEquals(Pos.TOP_RIGHT, StackPane.getAlignment(icon((Region) named.figureNode().getParent())));
            assertEquals(Pos.BOTTOM_RIGHT, StackPane.getAlignment(other.lookup(".kl-contract-icon")),
                    "a figure may name another corner for the way back");
        });
    }

    @Test
    @DisplayName("A pointer's click on an icon that is drawn always expands the figure, with the pointer having been nowhere near it")
    void aPointerClickOnAnAlwaysDrawnIconExpands() throws InterruptedException {
        // a robot on a real screen would move the person's pointer
        assumeTrue("Monocle".equals(System.getProperty("glass.platform")), "a pointer of the test's own");
        AtomicReference<KlExpandable> made = new AtomicReference<>();
        onFx(() -> {
            KlExpandable expandable = figure(true);
            made.set(expandable);
            // the pane a figure stands in is its home, and not a place it expands into: the window around it is
            BorderPane window = new BorderPane(new StackPane(expandable.withExpandAffordance()));
            KlExpansionHost.mark(window, () -> "Window", false);
            Stage stage = new Stage();
            stage.setScene(new Scene(window, 600, 400));
            stage.show();
        });
        Thread.sleep(300);
        onFx(() -> {
            Button icon = icon((Region) made.get().figureNode().getParent());
            Bounds on = icon.localToScreen(icon.getBoundsInLocal());
            Robot robot = new Robot();
            robot.mouseMove(on.getCenterX(), on.getCenterY());
            robot.mousePress(MouseButton.PRIMARY);
            robot.mouseRelease(MouseButton.PRIMARY);
        });
        Thread.sleep(300);
        onFx(() -> assertTrue(made.get().isExpanded(), "the click reached the icon"));
    }
}
