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
package dev.ikm.komet.kview.mvvm.view.settings;

import dev.ikm.komet.framework.settings.KometSettings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The application-wide Settings dialog (text size, component glyphs, display language). Opened
 * from {@code File > Settings...} and from the gear at the bottom of the journal sidebar. The
 * values it edits live in {@link KometSettings}, which apply to every window and every database
 * for the current OS user.
 */
public final class SettingsDialog {

    private SettingsDialog() {
    }

    /**
     * Shows the dialog modally over {@code owner} and returns once it is closed.
     *
     * @param owner the window the dialog belongs to, so it is centred over it and shares its
     *              task-bar entry
     */
    public static void show(Window owner) {
        FXMLLoader loader = new FXMLLoader(SettingsDialog.class.getResource("settings-dialog.fxml"));
        loader.setControllerFactory(type -> new SettingsDialogController(KometSettings.get()));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load settings-dialog.fxml", e);
        }
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Settings");
        if (owner instanceof Stage ownerStage) {
            // The same title-bar icon as the window it belongs to (the app icon is set by komet-desktop).
            stage.getIcons().setAll(ownerStage.getIcons());
        }
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}
