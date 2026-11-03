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

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Names a node that lays itself out over and over while nobody touches the window. Such a node
 * shows nothing on screen: it only keeps the JavaFX thread and the renderer busy, as a text field
 * squeezed narrower than its caret did, moving its text back and forth on every layout and asking
 * for the next one.
 *
 * <p>The watchdog says so in the log and does nothing else. It cannot tell a loop from a slow
 * animation with certainty, and the cure is always to fix the node, so it never throttles pulses
 * or holds a layout back.
 *
 * <p>A node is watched by whether its {@code needsLayout} flag keeps changing, not by whether the
 * flag is set: a node can keep the flag with no layout coming, when it asks for one while its
 * parent is laying out, and that costs nothing. A window at rest has no pulses, so this costs
 * nothing there either; under a running animation it walks the scene once a second.
 */
public final class LayoutWatchdog {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutWatchdog.class);

    /** How long one look at a scene lasts. */
    private static final long SAMPLE_NANOS = TimeUnit.SECONDS.toNanos(1);
    /** The looks in a row, with no input, that a node must stay busy through before it is named. */
    private static final int SAMPLES_TO_REPORT = 5;
    /** The layouts in one look that make a node busy: more than a clock or a status line asks for. */
    private static final int LAYOUTS_PER_SAMPLE = 10;
    /** The ancestors of a node its path gives, enough to place it without reading the whole scene. */
    private static final int PATH_LENGTH = 8;
    /** The passes a scene is given to settle before it is judged, for layouts that take more than one. */
    private static final int PASSES_TO_SETTLE = 8;
    /** The passes a scene is judged over. */
    private static final int PASSES_JUDGED = 4;

    private LayoutWatchdog() {
    }

    /**
     * Starts watching the windows already showing and every window shown from now on. Call once,
     * on the FX thread.
     *
     * @return a subscription that stops the watching
     */
    public static Subscription install() {
        Installer installer = new Installer(SAMPLE_NANOS, SAMPLES_TO_REPORT, LAYOUTS_PER_SAMPLE,
                path -> LOG.warn("{} is being laid out continuously with no input; it is keeping the JavaFX "
                        + "thread and the renderer busy. Look for a size or position it changes while laying out.", path));
        return installer::uninstall;
    }

    /** As {@link #install()}, with the timing and the report a test sets. */
    static Subscription install(long sampleNanos, int samplesToReport, int layoutsPerSample, Consumer<String> report) {
        return new Installer(sampleNanos, samplesToReport, layoutsPerSample, report)::uninstall;
    }

    /**
     * Lays a scene's root out until it should have settled, and returns the path of every node
     * still being laid out; empty when the scene has come to rest. For a test: a node on this list
     * would keep a running Komet busy for as long as it shows. Call on the FX thread, on the root
     * of a scene; no window is needed.
     *
     * @param width  the width to give the root, as a window would
     * @param height the height to give the root
     */
    public static List<String> unsettled(Parent root, double width, double height) {
        root.resize(width, height);
        for (int pass = 0; pass < PASSES_TO_SETTLE; pass++) {
            pulse(root);
        }
        Look look = new Look(root);
        for (int pass = 0; pass < PASSES_JUDGED; pass++) {
            pulse(root);
        }
        return List.copyOf(look.end(PASSES_JUDGED));
    }

    /**
     * What a pulse does to a root. Reading its bounds matters: a node tells its parent of a change
     * of bounds only when the old ones have been read, so without this a loop stops by itself.
     */
    private static void pulse(Parent root) {
        root.applyCss();
        root.layout();
        root.getBoundsInParent();
    }

    /** The deepest nodes under a root that need layout. */
    private static List<Parent> deepestNeedingLayout(Parent root) {
        List<Parent> deepest = new ArrayList<>();
        collect(root, deepest);
        return deepest;
    }

    private static boolean collect(Parent parent, List<Parent> deepest) {
        boolean below = false;
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent inner && collect(inner, deepest)) {
                below = true;
            }
        }
        if (below) {
            return true;
        }
        if (parent.isNeedsLayout()) {
            deepest.add(parent);
            return true;
        }
        return false;
    }

    /** A node as its last ancestors read, each by its class and its id if it has one. */
    private static String path(Node node) {
        Deque<String> names = new ArrayDeque<>();
        for (Node at = node; at != null && names.size() < PATH_LENGTH; at = at.getParent()) {
            Class<?> type = at.getClass();
            while (type.getSimpleName().isEmpty()) {
                type = type.getSuperclass();
            }
            names.addFirst(at.getId() == null ? type.getSimpleName() : type.getSimpleName() + "#" + at.getId());
        }
        return String.join(" > ", names);
    }

    /** One look at a root: counts how often the nodes needing layout at its start change that flag. */
    private static final class Look {

        private final Map<Parent, Integer> changes = new IdentityHashMap<>();
        private final List<Subscription> subscriptions = new ArrayList<>();

        Look(Parent root) {
            for (Parent node : deepestNeedingLayout(root)) {
                changes.put(node, 0);
                subscriptions.add(node.needsLayoutProperty().subscribe((was, is) -> changes.merge(node, 1, Integer::sum)));
            }
        }

        /** Ends the look, and gives the path of each node laid out at least this many times in it. */
        Set<String> end(int layouts) {
            subscriptions.forEach(Subscription::unsubscribe);
            Set<String> busy = new LinkedHashSet<>();
            // a layout clears the flag and the next request sets it: two changes for each
            changes.forEach((node, count) -> {
                if (count >= 2 * layouts - 1) {
                    busy.add(path(node));
                }
            });
            return busy;
        }
    }

    /** Watches each window's scene for as long as the window is open. */
    private static final class Installer {

        private final long sampleNanos;
        private final int samplesToReport;
        private final int layoutsPerSample;
        private final Consumer<String> report;
        private final Map<Window, Watch> watches = new HashMap<>();
        private final ListChangeListener<Window> onWindows = change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::forget);
                change.getAddedSubList().forEach(this::track);
            }
        };

        Installer(long sampleNanos, int samplesToReport, int layoutsPerSample, Consumer<String> report) {
            this.sampleNanos = sampleNanos;
            this.samplesToReport = samplesToReport;
            this.layoutsPerSample = layoutsPerSample;
            this.report = report;
            Window.getWindows().forEach(this::track);
            Window.getWindows().addListener(onWindows);
        }

        private void track(Window window) {
            if (!(window instanceof PopupWindow)) {
                watches.put(window, new Watch(window));
            }
        }

        private void forget(Window window) {
            Watch watch = watches.remove(window);
            if (watch != null) {
                watch.stop();
            }
        }

        private void uninstall() {
            Window.getWindows().removeListener(onWindows);
            watches.values().forEach(Watch::stop);
            watches.clear();
        }

        /** Takes a look at a window's scene on the pulses that come, and names what stays busy. */
        private final class Watch {

            private final Subscription onScene;
            private final Runnable onPulse = this::pulse;
            private final EventHandler<InputEvent> onInput = event -> touched = true;
            /** What has been named, so a node is named once however long it runs. */
            private final Set<String> reported = new HashSet<>();
            private Scene scene;
            private Look look;
            private long lookStart;
            private boolean touched;
            private Set<String> suspects = Set.of();
            private int samples;

            Watch(Window window) {
                onScene = window.sceneProperty().subscribe(this::watch);
            }

            private void watch(Scene next) {
                if (scene != null) {
                    scene.removePreLayoutPulseListener(onPulse);
                    scene.removeEventFilter(InputEvent.ANY, onInput);
                }
                scene = next;
                endLook();
                if (scene != null) {
                    scene.addPreLayoutPulseListener(onPulse);
                    scene.addEventFilter(InputEvent.ANY, onInput);
                }
            }

            private void pulse() {
                long now = System.nanoTime();
                if (look == null || now - lookStart >= sampleNanos) {
                    judge(now);
                    look = new Look(scene.getRoot());
                    lookStart = now;
                    touched = false;
                }
            }

            private void judge(long now) {
                // pulses that stopped between two looks are no continuous layout
                boolean adjacent = look != null && now - lookStart < 2 * sampleNanos;
                Set<String> busy = look == null ? Set.of() : look.end(layoutsPerSample);
                look = null;
                if (touched || !adjacent || busy.isEmpty()) {
                    suspects = Set.of();
                    samples = 0;
                    return;
                }
                Set<String> staying = new LinkedHashSet<>(busy);
                if (samples > 0) {
                    staying.retainAll(suspects);
                }
                suspects = staying;
                samples = staying.isEmpty() ? 0 : samples + 1;
                if (samples >= samplesToReport) {
                    staying.stream().filter(reported::add).forEach(report);
                }
            }

            private void endLook() {
                if (look != null) {
                    look.end(layoutsPerSample);
                    look = null;
                }
                suspects = Set.of();
                samples = 0;
            }

            void stop() {
                watch(null);
                onScene.unsubscribe();
            }
        }
    }
}
