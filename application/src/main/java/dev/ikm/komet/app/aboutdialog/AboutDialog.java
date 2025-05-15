package dev.ikm.komet.app.aboutdialog;

import javafx.scene.control.Dialog;
import javafx.stage.Modality;

public class AboutDialog extends Dialog {

    private AboutDialogPane dialogPane;

    public AboutDialog() {
        init();
    }

    private void init() {
        setTitle("About Komet");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        dialogPane = new AboutDialogPane();

        super.setDialogPane(dialogPane);
    }

}
