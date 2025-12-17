package dev.ikm.komet.layout_engine.component.area;


import dev.ikm.komet.framework.observable.FeatureList;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlAreaForListOfVersions;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureListAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListView;
import javafx.util.Subscription;

import java.util.concurrent.atomic.AtomicReference;



public final class SimpleVersionList
        extends FeatureListAreaBlueprint<ObservableVersion, FeatureList<ObservableVersion>>
        implements KlAreaForListOfVersions<ListView<ObservableVersion>> {

    AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);


    private final SimpleObjectProperty<KlAreaForListOfVersions.VersionsAndSelection> versionAndSelectionProperty = new SimpleObjectProperty<>();

    {
        versionAndSelectionProperty.set(new KlAreaForListOfVersions.VersionsAndSelection(fxObject().getItems(),
                fxObject().getSelectionModel().getSelectedItems()));
        transientSubscriptions.get().and(fxObject().getItems().subscribe(this::updateVersionsAndSelection));
        transientSubscriptions.get().and(fxObject().getSelectionModel().getSelectedItems().subscribe(this::updateVersionsAndSelection));
    }

    private SimpleVersionList(KometPreferences preferences) {
        super(preferences);
    }

    private SimpleVersionList(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    protected void updateVersionsAndSelection() {
        Platform.runLater(() -> {
            versionAndSelectionProperty.set(new KlAreaForListOfVersions.VersionsAndSelection(fxObject().getItems(),
                    fxObject().getSelectionModel().getSelectedItems()));
        });
    }

    @Override
    public ReadOnlyObjectProperty<KlAreaForListOfVersions.VersionsAndSelection> versionsAndSelectionProperty() {
        return versionAndSelectionProperty;
    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }

    public static Factory factory() {
        return new Factory();
    }

    public static SimpleVersionList restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static SimpleVersionList create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    public static SimpleVersionList create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    public static final class Factory implements KlAreaForListOfVersions.Factory<ListView<ObservableVersion>> {
        public Factory() {
        }

        @Override
        public SimpleVersionList create(KlPreferencesFactory preferencesFactory) {
            return create(preferencesFactory, defaultAreaGridSettings());
        }

        @Override
        public SimpleVersionList create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            SimpleVersionList area = new SimpleVersionList(preferencesFactory, areaGridSettings.makeAreaFactory());
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public SimpleVersionList restore(KometPreferences preferences) {
            SimpleVersionList simpleVersionList = new SimpleVersionList(preferences);
            return simpleVersionList;
        }
    }

}
