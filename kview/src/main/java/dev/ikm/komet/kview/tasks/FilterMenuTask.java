package dev.ikm.komet.kview.tasks;

import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * task to translate the current state of view coordinates in the view properties
 * into FilterOptions so that the coordinates can be set on a next gen filter
 *
 */
public class FilterMenuTask extends TrackingCallable {

    private static final Logger LOG = LoggerFactory.getLogger(FilterMenuTask.class);

    private ViewProperties viewProperties;

    private static final List<String> ALL_STATES = StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN.toEnumSet().stream().map(s -> s.name()).toList();

    public FilterMenuTask(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    @Override
    protected FilterOptions compute() throws Exception {
        FilterOptions filterOptions = new FilterOptions();

        // get parent menu settings
        ObservableCoordinate parentView = viewProperties.parentView();
        for (ObservableCoordinate<?> observableCoordinate : parentView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // populate the STATUS
                StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
                List<String> currentStatesStr = currentStates.toEnumSet().stream().map(s -> s.name()).toList();

                filterOptions.getStatus().selectedOptions().clear();
                filterOptions.getStatus().selectedOptions().addAll(currentStatesStr);

                filterOptions.getStatus().availableOptions().clear();
                filterOptions.getStatus().availableOptions().addAll(ALL_STATES);

                filterOptions.getStatus().defaultOptions().clear();
                filterOptions.getStatus().defaultOptions().addAll(currentStatesStr);

                // populate the PATH
                ConceptFacade currentPath = observableStampCoordinate.pathConceptProperty().getValue();
                String currentPathStr = currentPath.description();

                List<String> defaultSelectedPaths = new ArrayList(List.of(currentPathStr));
                filterOptions.getPath().defaultOptions().clear();
                filterOptions.getPath().defaultOptions().addAll(defaultSelectedPaths);

                filterOptions.getPath().selectedOptions().clear();
                filterOptions.getPath().selectedOptions().addAll(defaultSelectedPaths);
            }
            //TODO Type, Module, Language, Description Type, Kind of, Membership, Sort By
        }
        return filterOptions;
    }

}
