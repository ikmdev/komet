package dev.ikm.komet.kview.tasks;

import static dev.ikm.komet.kview.controls.FilterOptions.OPTION_ITEM.PATH;
import static dev.ikm.komet.kview.controls.FilterOptions.OPTION_ITEM.STATUS;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * task to translate the current state of view coordinates in the view properties
 * into FilterOptions so that the coordinates can be set on a next gen filter
 *
 */
public class FilterMenuTask extends TrackingCallable { // List<String> ??

    private static final Logger LOG = LoggerFactory.getLogger(FilterMenuTask.class);

    private ViewProperties viewProperties;

    private static final List<String> ALL_STATES = StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN.toEnumSet().stream().map(s -> s.name()).toList();

    public FilterMenuTask(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    @Override
    protected FilterOptions compute() throws Exception {
        FilterOptions filterOptions = new FilterOptions();

        // populate the STATUS
        ObservableCoordinate parentView = viewProperties.parentView();
        for (ObservableCoordinate<?> observableCoordinate : parentView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {
                // get parent menu settings
                StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
                List<String> currentStatesStr = currentStates.toEnumSet().stream().map(s -> s.name()).toList();

                filterOptions.getStatus().selectedOptions().clear();
                filterOptions.getStatus().selectedOptions().addAll(currentStatesStr);

                filterOptions.getStatus().availableOptions().clear();
                filterOptions.getStatus().availableOptions().addAll(ALL_STATES);
            }
            //TODO Type, Module, Path, Language, Description Type, Kind of, Membership, Sort By, Date
        }
        return filterOptions;
    }

//        @Override
//        protected Map<FilterOptions.OPTION_ITEM, List<String>> compute() throws Exception {
//            //        // populate the STATUS
//            Map<FilterOptions.OPTION_ITEM, List<String>> filterOptions = new HashMap<>();
//            ObservableCoordinate parentView = viewProperties.parentView();
//            for (ObservableCoordinate<?> observableCoordinate : parentView.getCompositeCoordinates()) {
//                if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {
//                    StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
//                    List<String> currentStatesStr = currentStates.toEnumSet().stream().map(s -> s.name()).toList();
//                    filterOptions.put(STATUS, currentStatesStr);
//                }
//            }
//            return filterOptions;
//        }
}
