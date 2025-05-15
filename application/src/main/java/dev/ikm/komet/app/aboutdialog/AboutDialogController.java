package dev.ikm.komet.app.aboutdialog;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Year;
import java.util.ResourceBundle;

public class AboutDialogController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(AboutDialogController.class);

    @FXML
    private Label copyrightYearLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label publishedDateLabel;

    public AboutDialogController() {
        // TODO read properties file to get version and build date
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("initialize()");

        copyrightYearLabel.setText("" + Year.now().getValue());
    }

}
