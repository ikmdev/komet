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
package dev.ikm.komet.framework.graphics;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks {@link LayoutWatchdog} against the case that taught it: a text field an {@code HBox}
 * squeezes to its insets, whose skin moves its text on every layout and asks for the next one.
 */
@ExtendWith(JavaFXThreadExtension.class)
class LayoutWatchdogUTestFX {

    private static final long SAMPLE_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

    /** A field beside a label that will not give way, in a row narrower than the two. */
    private static Parent row(double width, boolean guarded) {
        TextField field = new TextField("100000");
        field.setId("price");
        field.setPrefColumnCount(8);
        if (guarded) {
            field.setMinWidth(Region.USE_PREF_SIZE);
        }
        Label title = new Label("A title wider than the row has room for, which keeps its width");
        title.setMinWidth(Region.USE_PREF_SIZE);
        HBox row = new HBox(8, title, field);
        StackPane root = new StackPane(row);
        new Scene(root, width, 80);
        return root;
    }

    private static <T> T onFx(Callable<T> body) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(body.call());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }

    @Test
    void aFieldSqueezedToItsInsetsNeverSettles() throws Exception {
        List<String> unsettled = onFx(() -> LayoutWatchdog.unsettled(row(300, false), 300, 80));
        assertEquals(1, unsettled.size(), unsettled::toString);
        assertTrue(unsettled.getFirst().contains("TextField#price"), unsettled::toString);
    }

    @Test
    void aFieldHeldAtItsWidthSettles() throws Exception {
        assertEquals(List.of(), onFx(() -> LayoutWatchdog.unsettled(row(300, true), 300, 80)));
    }

    @Test
    void aFieldWithRoomSettles() throws Exception {
        assertEquals(List.of(), onFx(() -> LayoutWatchdog.unsettled(row(900, false), 900, 80)));
    }

    @Test
    void aWatchedWindowNamesTheNodeOnce() throws Exception {
        BlockingQueue<String> reports = new LinkedBlockingQueue<>();
        Stage stage = onFx(() -> {
            Subscription watching = LayoutWatchdog.install(SAMPLE_NANOS, 3, 2, reports::add);
            Stage shown = new Stage();
            shown.setScene(row(300, false).getScene());
            shown.setOnHidden(event -> watching.unsubscribe());
            shown.show();
            return shown;
        });
        try {
            String report = reports.poll(10, TimeUnit.SECONDS);
            assertNotNull(report, "the loop is named");
            assertTrue(report.contains("TextField#price"), report);
            assertNull(reports.poll(1, TimeUnit.SECONDS), "and named once");
        } finally {
            onFx(() -> {
                stage.hide();
                return null;
            });
        }
    }

    @Test
    void aLabelThatChangesNowAndThenIsNotNamed() throws Exception {
        BlockingQueue<String> reports = new LinkedBlockingQueue<>();
        Label clock = new Label("0");
        Timeline ticking = new Timeline(new KeyFrame(Duration.millis(40), event -> clock.setText(clock.getText() + "0")));
        ticking.setCycleCount(Animation.INDEFINITE);
        Stage stage = onFx(() -> {
            Subscription watching = LayoutWatchdog.install(SAMPLE_NANOS, 3, 10, reports::add);
            Stage shown = new Stage();
            shown.setScene(new Scene(new StackPane(clock), 300, 80));
            shown.setOnHidden(event -> watching.unsubscribe());
            shown.show();
            ticking.play();
            return shown;
        });
        try {
            assertNull(reports.poll(2, TimeUnit.SECONDS), "two or three layouts a look is no loop");
        } finally {
            onFx(() -> {
                ticking.stop();
                stage.hide();
                return null;
            });
        }
    }
}
