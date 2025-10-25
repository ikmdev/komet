package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableVersion;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Objects;

/**
 * Represents an interface for managing areas dedicated to observable versions and their selection state.
 * This interface extends the {@code KlListArea} and provides additional functionality for handling
 * version-related constructs in JavaFX regions.
 *
 * @param <FX> the type of JavaFX region associated with this area, extending {@code Region}
 */
public non-sealed interface KlAreaForListOfVersions<FX extends Region>
        extends KlAreaForListOfFeatures<ObservableVersion<?>, FX> {
    /**
     * A record that encapsulates information about observable versions and the subset
     * of those versions currently selected. This record holds immutable lists so that
     * use of the versions and selection is side-effect-free.
     * <p>
     * This record is designed to be used in contexts where version management is crucial,
     * such as selecting or displaying multiple versions of entities in a user interface.
     *
     * @param versions          the complete list of {@code ObservableVersion} instances,
     *                          representing all available versions in a given context
     * @param selectedVersions  the subset of {@code ObservableVersion} instances currently
     *                          selected or highlighted from the complete list
     */
    record VersionsAndSelection(ImmutableList<ObservableVersion<?>> versions,
                                ImmutableList<ObservableVersion<?>> selectedVersions) {

        public VersionsAndSelection {
            Objects.requireNonNull(versions, "versions list cannot be null");
            Objects.requireNonNull(selectedVersions, "selectedVersions list cannot be null");
        }

        /**
         * Convenient constructor that takes ObservableLists and converts them to immutable lists.
         *
         * @param observableVersions the observable list of all versions
         * @param observableSelectedVersions the observable list of selected versions
         * @throws NullPointerException if either parameter is null
         */
        public VersionsAndSelection(ObservableList<ObservableVersion<?>> observableVersions,
                                    ObservableList<ObservableVersion<?>> observableSelectedVersions) {
            this(Lists.immutable.withAll(Objects.requireNonNull(observableVersions, "observableVersions cannot be null")),
                 Lists.immutable.withAll(Objects.requireNonNull(observableSelectedVersions, "observableSelectedVersions cannot be null")));
        }

    }


    /**
     * Retrieves a read-only property representing a {@code VersionsAndSelection} instance.
     * This property encapsulates information about the observable list of all versions
     * and the subset of those versions currently selected, ensuring immutability and
     * side-effect-free usage.
     *
     * @return a {@code ReadOnlyObjectProperty} containing a {@code VersionsAndSelection}
     *         instance, representing the complete list of versions and their selected subset.
     */
    ReadOnlyObjectProperty<VersionsAndSelection> versionsAndSelectionProperty();

    /**
     * Represents a specialized factory interface within the Knowledge Layout (KL) framework
     * for creating and managing instances of list areas that handle observable version-specific
     * elements and their associated JavaFX regions.
     * <p>
     * This factory extends the `KlListArea.Factory` interface by specifying the concrete types
     * for its generic parameters. Specifically, it binds the element type to an `ObservableVersion`,
     * the JavaFX region type to a type extending `Region`, and the associated list area type
     * to a `KlListOfVersionArea`.
     *
     * @param <FX> the type of JavaFX region associated with the list area, extending {@code Region}.
     */
    interface Factory<FX extends Region>
            extends KlAreaForListOfFeatures.Factory<ObservableVersion<?>, FX, KlAreaForListOfVersions<FX>> {


    }
}
