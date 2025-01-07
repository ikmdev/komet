package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlVersionType;
import dev.ikm.komet.layout.KlWidgetFactory;
import dev.ikm.komet.preferences.KometPreferences;

public interface KlVersionPaneFactory <OV extends ObservableVersion> extends KlWidgetFactory, KlVersionType<OV> {

    KlVersionPane<OV> create(OV observableVersion,
                           ObservableView observableView,
                           KometPreferences preferences);
}
