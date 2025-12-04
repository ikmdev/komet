package dev.ikm.komet.layout.component.multi;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlEntityType;
import dev.ikm.komet.layout.window.KlFrameFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.Pane;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * This interface defines a factory for creating instances of {@code KlMultiComponentPane}, which are used
 * to visually manage and present multiple observable entities within a graphical user interface. The created
 * pane is tailored to specific requirements, including a collection of observable entities, a provided view
 * for navigation/coordinate settings, and user preferences.
 *
 * @param <OE> the type of observable entity that extends {@link ObservableEntity}; represents the entities
 *             managed and rendered by the pane created by this factory
 * @param <P> the type of pane used within the {@code KlMultiComponentPane}; typically extends {@link Pane}
 */
public interface KlMultiComponentAreaFactory<OE extends ObservableEntity, P extends Pane> extends KlFrameFactory, KlEntityType<OE> {

    /**
     * Creates an instance of {@code KlMultiComponentPane}, which is used to display and manage multiple
     * observable entities within a graphical user interface, tailored to the provided observable entities,
     * a specific observable view, and user preferences.
     *
     * @param observableEntities the immutable list of observable entities that will be managed by the created pane;
     *                           each entity must extend {@link ObservableEntity}
     * @param observableView     the observable view that provides context, including navigation and coordinate
     *                           settings for the pane
     * @param preferences        user-specific preferences that influence the behavior or appearance
     *                           of the created pane
     * @return an instance of {@code KlMultiComponentPane} configured according to the provided parameters
     */
    KlMultiComponentArea<OE, P> create(ImmutableList<OE> observableEntities,
                                       ObservableView observableView,
                                       KometPreferences preferences);
}