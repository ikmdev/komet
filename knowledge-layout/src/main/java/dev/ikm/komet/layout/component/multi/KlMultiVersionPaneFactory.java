package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlVersionType;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.component.KlComponentPane;
import dev.ikm.komet.preferences.KometPreferences;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * A factory interface for creating instances of {@link KlComponentPane} that support multiple observable versions.
 *
 * @param <OV> the type parameter that extends {@link ObservableVersion}, representing the specific version type
 *             that this factory interacts with.
 */
public interface KlMultiVersionPaneFactory<OV extends ObservableVersion> extends KlFactory<KlWidget>, KlVersionType<OV> {

    /**
     * Creates an instance of {@link KlMultiVersionPane} tailored to the specified observable versions, observable view,
     * and user preferences. This method constructs a multi-version pane that facilitates layout and comparison
     * of multiple observable versions of the same type.
     *
     * @param observableVersions a list of observable versions to be managed by the multi-version pane
     * @param observableView the observable view providing the context and configurations for the pane
     * @param preferences the user preferences object used to configure the behavior of the multi-version pane
     * @return an instance of {@link KlMultiVersionPane} configured with the provided versions, view, and preferences
     */
    KlMultiVersionPane<OV> create(ImmutableList<OV> observableVersions,
                           ObservableView observableView,
                           KometPreferences preferences);
}