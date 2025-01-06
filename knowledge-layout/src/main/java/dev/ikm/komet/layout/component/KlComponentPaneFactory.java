package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.preferences.KometPreferences;

public interface KlComponentPaneFactory<OE extends ObservableEntity> extends KlFactory, KlEntityType<OE> {

    KlComponentPane<OE> create(OE observableEntity,
                           ObservableView observableView,
                           KometPreferences preferences);
}
