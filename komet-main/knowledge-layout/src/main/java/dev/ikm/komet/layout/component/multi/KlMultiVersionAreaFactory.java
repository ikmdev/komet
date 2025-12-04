package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlVersionType;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.Pane;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * A factory interface for creating instances of {@link KlMultiVersionArea}, a component designed to handle
 * and visually represent multiple observable versions of a specified type. This factory integrates the
 * requirements for observable version management, contextual view configuration, and user preference settings.
 *
 * @param <OV> the type of {@link ObservableVersion} that this factory operates on
 * @param <P>  the type of {@link Pane} used as the visual container for the versions
 */
public interface KlMultiVersionAreaFactory<OV extends ObservableVersion, P extends Pane>
        extends KlFactory<KlWidget>, KlVersionType<OV> {

    /**
     * Creates an instance of {@link KlMultiVersionArea}, which is a component for managing and visually
     * representing multiple observable versions of a specified type. This method integrates the provided
     * observable versions, the contextual view configuration, and user preference settings.
     *
     * @param observableVersions the list of observable versions to be managed and displayed within the created pane
     * @param observableView the contextual view configuration that defines how the versions are interpreted and displayed
     * @param preferences the user preference settings used to further configure the pane's behavior and appearance
     * @return a new instance of {@link KlMultiVersionArea} configured with the given observable versions, view, and preferences
     */
    KlMultiVersionArea<OV, P> create(ImmutableList<OV> observableVersions,
                                     ObservableView observableView,
                                     KometPreferences preferences);
}