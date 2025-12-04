package dev.ikm.komet.app.aboutdialog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Year;

public class AboutDialogController {

    private static final Logger LOG = LoggerFactory.getLogger(AboutDialogController.class);

    private static final String DEFAULT_BUILD_VERSION_PROP_VALUE = "${build.version}";

    @FXML
    private Label copyrightYearLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label publishedDateLabel;

    private BuildInfoProperties buildInfoProperties = new BuildInfoProperties();

    @FXML
    public void initialize() {
        copyrightYearLabel.setText(Integer.toString(Year.now().getValue()));

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
