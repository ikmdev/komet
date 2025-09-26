package dev.ikm.komet.dev;

import com.jpro.webapi.WebAPI;
import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppMenu;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

class Starter {
    public static void main(String[] args) {

        System.out.println("Hello, Komet Dev!");

        System.setProperty("dev_author", "KOMET user");

        AppMenu.devMenuFactory = Starter::createDevMenu;

        if(!Boolean.getBoolean("jpro.isbrowser")) {
            App.main(args);
        }
    }

    static Menu createDevMenu(Parent node) {
        Menu devMenu = new Menu("Dev");

        MenuItem reloadMenuItem = new MenuItem("Scenic View");
        reloadMenuItem.setOnAction(actionEvent -> {
            var stage2 = new Stage();
            if(WebAPI.isBrowser()) {
                WebAPI.getWebAPI(node.getScene()).openStageAsPopup(stage2);
            }
            ScenicView.show(node, stage2);
        });
        // Add shortcut Ctrl+Shift+S to open Scenic View
        KeyCombination scenicViewKeyCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        reloadMenuItem.setAccelerator(scenicViewKeyCombo);
        devMenu.getItems().add(reloadMenuItem);
        return devMenu;
    }
}