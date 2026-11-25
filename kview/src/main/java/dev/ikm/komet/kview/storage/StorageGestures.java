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
package dev.ikm.komet.kview.storage;

import dev.ikm.komet.actions.KometActionInput;
import dev.ikm.komet.actions.KometActionResult;
import dev.ikm.komet.actions.KometActions;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.layout.storage.KlStorage;
import dev.ikm.komet.layout.storage.KlStoredGroup;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.EntityCountSummary;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.entity.boundary.DataKind;
import dev.ikm.tinkar.entity.scope.Scope;
import dev.ikm.tinkar.entity.scope.ScopeOperations;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The person's gestures for {@link ExportToFile} and {@link ImportFromFile}
 * (IKE-Network/ike-issues#1313). Each runs off the JavaFX thread, and its outcome is shown as a toast
 * and kept as a line in Warnings and Errors. The one question either asks is whether an import
 * replaces what is here already (KEC, 2026-11-22, decision 5).
 */
public final class StorageGestures {

    private static final Logger LOG = LoggerFactory.getLogger(StorageGestures.class);

    /** What exporting is called in the interface. */
    public static final String EXPORT = "Export";
    /** What importing is called in the interface. */
    public static final String IMPORT = "Import";
    /** What deleting is called in the interface. */
    private static final String DELETE = "Delete";
    /** The end of an exported file's name. */
    public static final String SUFFIX = ".komet-export.zip";

    private StorageGestures() {
    }

    /**
     * Asks for a file and exports a group of local data to it. Call on the JavaFX thread.
     *
     * @param from    a node in the window the person asked from
     * @param subject a component the group is found by
     * @param name    the group's name, which the file's name is made from
     * @param edit    the edit coordinate of the person
     * @param done    run on the JavaFX thread once it is done, refused or given up
     */
    public static void export(Node from, PublicId subject, String name, EditCoordinate edit, Runnable done) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(EXPORT + " " + name);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exported data", "*.zip"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setInitialFileName(fileName(name));
        File chosen = chooser.showSaveDialog(window(from));
        if (chosen == null) {
            done.run();
            return;
        }
        run(from, EXPORT, ExportToFile.input(subject, chosen.getName().contains(".") ? chosen.toPath()
                : chosen.toPath().resolveSibling(chosen.getName() + SUFFIX)), edit, done);
    }

    /**
     * A file name made from a group's name.
     *
     * @param name the name
     * @return a name with the characters file systems refuse left out
     */
    static String fileName(String name) {
        String safe = name.replaceAll("[/\\\\:*?\"<>|\\p{Cntrl}]+", " ").replaceAll("\\s+", " ").strip();
        safe = safe.length() > 60 ? safe.substring(0, 60).strip() : safe;
        return (safe.isEmpty() ? "data" : safe) + SUFFIX;
    }

    /**
     * Imports a file that Export wrote, asking first when this knowledge base has the data already.
     * Call on the JavaFX thread.
     *
     * @param from a node in the window the person asked from
     * @param file the file
     * @param edit the edit coordinate of the person
     * @param done run on the JavaFX thread once it is done, refused or given up
     */
    public static void importFile(Node from, File file, EditCoordinate edit, Runnable done) {
        CompletableFuture.supplyAsync(() -> {
            try {
                ImportFromFile.check(file);
                return Optional.<String>empty();
            } catch (ScopeOperations.AlreadyHereException here) {
                return Optional.of(nameHere(ScopeOperations.manifest(file)));
            } catch (IllegalStateException | IllegalArgumentException refused) {
                return Optional.<String>empty();                 // the action says why, in its result
            }
        }).whenComplete((here, failure) -> Platform.runLater(() -> {
            if (failure != null) {
                LOG.warn("Could not read {}", file, failure);
                report(from, IMPORT, false, "Could not read " + file.getName() + ": " + failure.getCause().getMessage());
                done.run();
                return;
            }
            if (here.isPresent() && !replace(from, here.get())) {
                done.run();
                return;
            }
            run(from, IMPORT, ImportFromFile.input(file.toPath(), here.isPresent()), edit, done);
        }));
    }

    /** What this knowledge base calls the data a file has. */
    private static String nameHere(ScopeOperations.Manifest manifest) {
        for (PublicId root : manifest.roots()) {
            if (PrimitiveData.get().hasPublicId(root)) {
                for (KlStoredGroup group : KlStorage.groups()) {
                    if (PublicId.equals(group.root(), root)) {
                        return "“" + group.name() + "”";
                    }
                }
                return "“" + KlStorage.name(PrimitiveData.nid(root)) + "”";
            }
        }
        return "it";
    }

    private static boolean replace(Node from, String name) {
        ButtonType yes = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
        Alert ask = new Alert(Alert.AlertType.WARNING, "Its copy here is removed first, with anything in it that the file does not have.\n"
                + "Nothing else contains that.", yes, ButtonType.CANCEL);
        ask.initOwner(window(from));
        ask.setTitle(IMPORT);
        ask.setHeaderText("This knowledge base already has " + name + ". Replace it?");
        ask.getDialogPane().setId("importReplaceDialog");
        ask.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);          // else a second line of text is cut off
        return ask.showAndWait().filter(answer -> answer == yes).isPresent();
    }

    /**
     * Deletes a group that no plugin answers for, after asking. Call on the JavaFX thread.
     *
     * @param from  a node in the window the person asked from
     * @param group the group
     * @param done  run on the JavaFX thread once it is done, refused or given up
     */
    public static void delete(Node from, KlStoredGroup group, Runnable done) {
        ButtonType yes = new ButtonType(DELETE, ButtonBar.ButtonData.OK_DONE);
        Alert ask = new Alert(Alert.AlertType.WARNING, group.kind() == DataKind.FILE
                ? "It was read in from a file." : "Nothing else contains it.", yes, ButtonType.CANCEL);
        ask.initOwner(window(from));
        ask.setTitle(DELETE);
        ask.setHeaderText("Delete “" + group.name() + "” from this knowledge base?");
        ask.getDialogPane().setId("storageDeleteDialog");
        ask.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        if (ask.showAndWait().filter(answer -> answer == yes).isEmpty()) {
            done.run();
            return;
        }
        CompletableFuture.supplyAsync(() -> ScopeOperations.delete(Scope.of(PrimitiveData.nid(group.root()))))
                .whenComplete((deleted, failure) -> Platform.runLater(() -> {
                    if (failure != null) {
                        Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
                        report(from, DELETE, false, "Not deleted: " + cause.getMessage());
                    } else {
                        report(from, DELETE, true, "Deleted “" + group.name() + "”, " + deleted.components() + " components.");
                    }
                    done.run();
                }));
    }

    /**
     * Runs an action off the JavaFX thread and reports how it went.
     *
     * @param from  a node in the window the person asked from
     * @param title what the gesture is called
     * @param input the action's input
     * @param edit  the edit coordinate of the person
     * @param done  run on the JavaFX thread once it is reported
     */
    private static void run(Node from, String title, KometActionInput input, EditCoordinate edit, Runnable done) {
        CompletableFuture.supplyAsync(() -> KometActions.expect(input.action()).perform(input, Calculators.View.Default(), edit))
                .whenComplete((result, failure) -> Platform.runLater(() -> {
                    if (failure != null) {
                        Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
                        LOG.warn("{} failed", title, cause);
                        report(from, title, false, "Could not " + title.toLowerCase() + ": " + cause.getMessage());
                    } else {
                        report(from, title, result.completed(), result.message());
                    }
                    done.run();
                }));
    }

    /**
     * The size of a group, for a person: its concepts, patterns and semantics, as Storage counts them.
     *
     * @param counts what was written or read
     * @return the size in words
     */
    static String size(EntityCountSummary counts) {
        return String.format("%,d components", counts.conceptCount() + counts.patternCount() + counts.semanticCount());
    }

    /**
     * Shows an outcome as a toast in the window of a node, when it is still shown, and keeps it as a
     * line in Warnings and Errors: a note when it was done, a warning when it was not. Call on the
     * JavaFX thread.
     *
     * @param from    the node
     * @param title   what was done
     * @param done    whether it was
     * @param message the outcome, for the person
     */
    public static void report(Node from, String title, boolean done, String message) {
        LOG.info("{}: {}", title, message);
        AlertStreams.getRoot().dispatch(AlertObject.makeOutcome(done, title, message));
        if (window(from) != null) {
            new Toast(from).show(done ? Toast.Status.SUCCESS : Toast.Status.FAILURE, message);
        }
    }

    private static Window window(Node from) {
        return from == null || from.getScene() == null ? null : from.getScene().getWindow();
    }
}
