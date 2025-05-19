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

    private static final String DEFAULT_BUILD_VERSION_PROP_VALUE = "${build.version}";

    @FXML
    private Label copyrightYearLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label publishedDateLabel;

    private BuildInfoProperties buildInfoProperties = new BuildInfoProperties();

    public AboutDialogController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("initialize()");

        copyrightYearLabel.setText("" + Year.now().getValue());

        var mavenVersion = buildInfoProperties.getMavenVersion();
        var buildVersion = buildInfoProperties.getBuildVersion();
        var buildTime = buildInfoProperties.getBuildTime();

        if (buildVersion.equals(DEFAULT_BUILD_VERSION_PROP_VALUE)) {
            versionLabel.setText(mavenVersion);
        } else {
            versionLabel.setText(buildVersion);
        }

        publishedDateLabel.setText(buildTime);
    }

}
