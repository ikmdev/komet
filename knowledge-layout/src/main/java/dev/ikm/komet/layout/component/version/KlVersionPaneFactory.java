package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlVersionType;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.preferences.KometPreferences;

public interface KlVersionPaneFactory <OV extends ObservableVersion> extends KlFactory<KlWidget>, KlVersionType<OV> {

    KlVersionPane<OV> create(OV observableVersion,
                           ObservableView observableView,
                           KometPreferences preferences);
}
