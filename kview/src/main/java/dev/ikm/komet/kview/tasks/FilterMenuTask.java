package dev.ikm.komet.kview.tasks;

import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.entity.EntityService;
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

                // populate the TYPE; this isn't in the parent view coordinate
                // it is All | Concepts | Semantics
                filterOptions.getType().selectedOptions().clear();
                filterOptions.getType().selectedOptions().addAll(new ArrayList<>(List.of("Concepts", "Semantics")));
                filterOptions.getType().defaultOptions().clear();
                filterOptions.getType().defaultOptions().addAll(filterOptions.getType().selectedOptions());

                // populate the STATUS
                StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
                List<String> currentStatesStr = currentStates.toEnumSet().stream().map(s -> s.name()).toList();

                filterOptions.getStatus().selectedOptions().clear();
                filterOptions.getStatus().selectedOptions().addAll(currentStatesStr);

                filterOptions.getStatus().availableOptions().clear();
                filterOptions.getStatus().availableOptions().addAll(ALL_STATES);

                filterOptions.getStatus().defaultOptions().clear();
                filterOptions.getStatus().defaultOptions().addAll(currentStatesStr);

                // MODULE
                filterOptions.getModule().defaultOptions().clear();
                observableStampCoordinate.moduleNids().intStream().forEach(moduleNid -> {
                    String moduleStr = viewProperties.calculator().getPreferredDescriptionStringOrNid(moduleNid);
                    filterOptions.getModule().defaultOptions().add(moduleStr);
                });


                // populate the PATH
                ConceptFacade currentPath = observableStampCoordinate.pathConceptProperty().getValue();
                String currentPathStr = currentPath.description();

                List<String> defaultSelectedPaths = new ArrayList(List.of(currentPathStr));
                filterOptions.getPath().defaultOptions().clear();
                filterOptions.getPath().defaultOptions().addAll(defaultSelectedPaths);

                filterOptions.getPath().selectedOptions().clear();
                filterOptions.getPath().selectedOptions().addAll(defaultSelectedPaths);


            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {
                // populate the LANGUAGE
                filterOptions.getLanguage().defaultOptions().clear();
                String languageStr = viewProperties.calculator().languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(
                        observableLanguageCoordinate.languageConceptProperty().get().nid());
                filterOptions.getLanguage().defaultOptions().add(languageStr);
                filterOptions.getLanguage().selectedOptions().clear();
                filterOptions.getLanguage().selectedOptions().addAll(filterOptions.getLanguage().defaultOptions());
            }
            //TODO Type, Description Type, Kind of, Membership, Sort By
        }
        return filterOptions;
    }

}
