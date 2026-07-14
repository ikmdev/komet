/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.markdown.richtext;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;

/**
 * The <b>manual half of the {@code #789} spike</b> — NOT a test. Launch {@link #main} and verify by
 * eye the one thing headless asserts cannot: that the view refreshes correctly on the ad-hoc
 * {@code insertChip → fireChangeEvent} path, and that editing feels right around a live inline node.
 *
 * <p>Checklist:
 * <ol>
 *   <li>Type prose — ordinary editing works (the {@code replace()} path through our hooks).</li>
 *   <li>Click "Insert chip" — a pill appears <em>at the caret</em>, mid-text, and the caret lands
 *       after it (the ad-hoc-mutation refresh: the unverified assumption).</li>
 *   <li>Backspace immediately after the chip — the whole chip vanishes atomically.</li>
 *   <li>Arrow keys step over the chip as one character; selection sweeps across it and
 *       select-then-type replaces it together with the selected text.</li>
 *   <li>Enter before/after a chip splits the line without disturbing it.</li>
 *   <li>The token label at the bottom mirrors {@code toTokenText()} — the send-extraction form.</li>
 * </ol>
 */
public final class ConceptChipSpikeHarness extends Application {

    private static final String TOKEN = "k:sctid=73211009[Diabetes mellitus]";

    private final ConceptChipTextModel model = new ConceptChipTextModel();

    @Override
    public void start(Stage stage) {
        RichTextArea area = new RichTextArea(model);
        area.setWrapText(true);

        Button insert = new Button("Insert chip at caret");
        insert.setOnAction(e -> {
            TextPos caret = area.getCaretPosition();
            TextPos at = caret == null ? TextPos.ZERO : caret;
            TextPos after = model.insertChip(at, TOKEN, ConceptChipSpikeHarness::chipNode);
            area.select(after);
            area.requestFocus();
        });

        Label tokenText = new Label();
        Button showTokens = new Button("toTokenText()");
        showTokens.setOnAction(e -> {
            tokenText.setText(model.toTokenText().replace("\n", " ⏎ "));
            dumpChips(area); // on-demand scene diagnostic while testing interactively
        });

        HBox bar = new HBox(8, insert, showTokens, tokenText);
        bar.setPadding(new Insets(8));

        // The window is on a shared screen: it must explain itself without the chat transcript.
        Label instructions = new Label("Spike checklist — please poke: type prose · Insert chip at caret ·"
                + " Backspace right after a chip must delete the whole pill · arrows/selection treat it as"
                + " one character · Enter before/after it splits the line · toTokenText() shows the k: form"
                + " (and dumps chip nodes to the terminal).");
        instructions.setWrapText(true);
        instructions.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        instructions.setPadding(new Insets(6, 8, 6, 8));

        BorderPane root = new BorderPane(area);
        root.setTop(bar);
        root.setBottom(instructions);
        stage.setScene(new Scene(root, 720, 360));
        stage.setTitle("#789 spike — ConceptChipTextModel — interactive checklist (see bottom)");
        stage.show();

        // Self-demonstration of THE unverified assumption: seed a chip through the ad-hoc
        // insertChip → fireChangeEvent path only after the view is live, so what appears on screen
        // is the refresh behaving — not a model pre-populated before the area attached. The seed is
        // model-driven with explicit positions (no caret dependence) and guarded to run once.
        javafx.application.Platform.runLater(() -> {
            if (seeded) {
                return;
            }
            seeded = true;
            TextPos end = area.appendText("Evaluate  for similar tests.");
            System.out.println("seed: appended through " + end);
            TextPos afterChip = model.insertChip(TextPos.ofLeading(0, 9), TOKEN,
                    ConceptChipSpikeHarness::chipNode);
            System.out.println("seed: chip inserted, caret belongs at " + afterChip
                    + ", tokenText=" + model.toTokenText());
            area.select(afterChip);
            area.requestFocus();
            javafx.animation.PauseTransition settle =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            settle.setOnFinished(ev -> dumpChips(area));
            settle.play();
        });
    }

    /** Guards the live seed against any double invocation. */
    private boolean seeded;

    private static final java.util.concurrent.atomic.AtomicInteger CHIP_BUILDS =
            new java.util.concurrent.atomic.AtomicInteger();

    /** A stand-in chip: the real one is KonceptChips' identicon pill, supplied by the plugin. */
    private static Node chipNode() {
        int n = CHIP_BUILDS.incrementAndGet();
        System.out.println("chipNode build #" + n);
        Label chip = new Label("Diabetes mellitus");
        chip.getStyleClass().add("spike-chip");
        chip.setStyle("-fx-background-color: #e9eff6; -fx-background-radius: 9;"
                + " -fx-border-color: #9dbbd8; -fx-border-radius: 9;"
                + " -fx-padding: 0 6 0 6; -fx-font-size: 11;");
        return chip;
    }

    /** Dumps every live chip node and its parent chain — the decisive view-side diagnostic. */
    private static void dumpChips(RichTextArea area) {
        var chips = area.lookupAll(".spike-chip");
        System.out.println("dump: " + chips.size() + " chip node(s) in the scene, "
                + CHIP_BUILDS.get() + " built");
        int i = 0;
        for (Node chip : chips) {
            StringBuilder chain = new StringBuilder("dump: chip[" + (i++) + "]@"
                    + Integer.toHexString(System.identityHashCode(chip)));
            for (Node p = chip.getParent(); p != null && !(p instanceof RichTextArea); p = p.getParent()) {
                chain.append(" <- ").append(p.getClass().getSimpleName())
                        .append('@').append(Integer.toHexString(System.identityHashCode(p)));
            }
            System.out.println(chain);
        }
    }

    /**
     * Launches the harness.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        launch(args);
    }
}
