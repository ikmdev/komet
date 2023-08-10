import dev.ikm.komet.preferences.PreferencesService;
import dev.ikm.komet.preferences.PreferencesServiceFactory;

module dev.ikm.komet.preferences {
    exports dev.ikm.komet.preferences;
    requires org.slf4j;
    requires java.prefs;
    requires dev.ikm.tinkar.entity;
    requires java.xml;
    requires static com.google.auto.service;

    provides PreferencesService with PreferencesServiceFactory;
    uses PreferencesService;
}
