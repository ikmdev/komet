package dev.ikm.komet.app.aboutdialog;

import javafx.scene.control.Dialog;
import javafx.stage.Modality;

import static dev.ikm.komet.kview.fxutils.FXUtils.getFocusedWindow;

public class AboutDialog extends Dialog<Void> {

    private AboutDialogPane dialogPane;

    public AboutDialog() {
        init();
    }

    private void init() {
        setTitle("About Komet");

        // set the owner to the focused window so the Windows task bar doesn't show
        // two windows which are the main application and the dialog window
        initOwner(getFocusedWindow());

        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        dialogPane = new AboutDialogPane();

        super.setDialogPane(dialogPane);
    }

}
