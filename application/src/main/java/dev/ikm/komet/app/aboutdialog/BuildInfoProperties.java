package dev.ikm.komet.app.aboutdialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class BuildInfoProperties extends Properties {

    private static final Logger LOG = LoggerFactory.getLogger(BuildInfoProperties.class);

    public static final String PROPERTY_FILENMAME = "/buildInfo.properties";
    public static final String MAVERN_VERSION_PROP = "mavenVersion";
    public static final String BUILD_TIME_PROP = "buildTime";

    public BuildInfoProperties() {
        try {
            super.load(BuildInfoProperties.class.getResourceAsStream(PROPERTY_FILENMAME));
        } catch (IOException e) {
            LOG.error("IOException loading properties", e);
        }
    }

    public String getMavenVersion() {
        return getProperty(MAVERN_VERSION_PROP);
    }

    public String getBuildTime() {
        return getProperty(BUILD_TIME_PROP);
    }

}
