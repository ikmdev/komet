package dev.ikm.komet.kview.controls;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class FilterOptionsUtils {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static FilterOptions initializeFilterOptions(ObservableCoordinate<ViewCoordinateRecord> currentView, ViewCalculator calculator) {
        FilterOptions filterOptions = new FilterOptions();

        // get parent menu settings
        for (ObservableCoordinate<?> observableCoordinate : currentView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // populate the TYPE; this isn't in the parent view coordinate
                // it is all set in FilterOptions

                // populate the STATUS
                StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
                List<String> currentStatesStr = currentStates.toEnumSet().stream().map(Enum::name).toList();

                FilterOptions.Option statusOption = filterOptions.getMainCoordinates().getStatus();
                statusOption.selectedOptions().clear();
                statusOption.selectedOptions().addAll(currentStatesStr);

                statusOption.defaultOptions().clear();
                statusOption.defaultOptions().addAll(currentStatesStr);

                // MODULE
                if (!observableStampCoordinate.moduleNids().isEmpty()) {
                    FilterOptions.Option moduleOption = filterOptions.getMainCoordinates().getModule();
                    moduleOption.defaultOptions().clear();
                    observableStampCoordinate.moduleNids().intStream().forEach(moduleNid -> {
                        String moduleStr = calculator.getPreferredDescriptionStringOrNid(moduleNid);
                        System.out.println("moduleStr = " + moduleStr);
                        moduleOption.defaultOptions().add(moduleStr);
                    });
                }

                // populate the PATH
                ConceptFacade currentPath = observableStampCoordinate.pathConceptProperty().getValue();
                String currentPathStr = currentPath.description();

                List<String> defaultSelectedPaths = new ArrayList<>(List.of(currentPathStr));
                FilterOptions.Option pathOption = filterOptions.getMainCoordinates().getPath();
                pathOption.defaultOptions().clear();
                pathOption.defaultOptions().addAll(defaultSelectedPaths);

                pathOption.selectedOptions().clear();
                pathOption.selectedOptions().addAll(defaultSelectedPaths);

                // TIME
                FilterOptions.Option timeOption = filterOptions.getMainCoordinates().getTime();

                Long time = observableStampCoordinate.timeProperty().getValue();
                if (!time.equals(Long.MAX_VALUE) && !time.equals(PREMUNDANE_TIME)) {
                    //FIXME the custom control doesn't support premundane yet
                    Date date = new Date(time);
                    timeOption.defaultOptions().clear();
                    timeOption.selectedOptions().clear();
                    timeOption.selectedOptions().add(SIMPLE_DATE_FORMAT.format(date));
                    timeOption.defaultOptions().addAll(timeOption.selectedOptions());
                }
            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {
                // populate the LANGUAGE
                FilterOptions.Option language = filterOptions.getLanguageCoordinates(0).getLanguage();
                language.defaultOptions().clear();
                String languageStr = calculator.languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(
                        observableLanguageCoordinate.languageConceptProperty().get().nid());
                language.defaultOptions().add(languageStr);
                language.selectedOptions().clear();
                language.selectedOptions().addAll(language.defaultOptions());

                //FIXME description choices don't yet align with parent/classic menu, more discussion needs to happen on
                // how we want to fix this.
                // all set in FilterOptions
            }
        }
        return filterOptions;
    }

    public static FilterOptions copyFilterOptions(FilterOptions filterOptions) {
        FilterOptions copyOfFilterOptions = new FilterOptions();
        filterOptions.getMainCoordinates().getOptions().forEach(option -> {
            copyOfFilterOptions.setOptionForItem(option.item(), option.copy());
        });
        return copyOfFilterOptions;
    }


    public static FilterOptions reloadFilterOptions(FilterOptions.MainCoordinates childOptions,
                                                    ObservableCoordinate<ViewCoordinateRecord> childView, ViewCalculator calculator) {
        FilterOptions filterOptions = new FilterOptions();

        // get parent menu settings
        for (ObservableCoordinate<?> observableCoordinate : childView.getCompositeCoordinates()) {

            if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // populate the TYPE; this isn't in the parent view coordinate
                // it is all set in FilterOptions

                // populate the STATUS
                StateSet currentStates = observableStampCoordinate.allowedStatesProperty().getValue();
                List<String> currentStatesStr = currentStates.toEnumSet().stream().map(Enum::name).toList();

                FilterOptions.Option statusOption = filterOptions.getMainCoordinates().getStatus();
                if (!childOptions.getStatus().isInOverride()) {
                    // override
                    statusOption.selectedOptions().clear();
                    statusOption.selectedOptions().addAll(currentStatesStr);

                    statusOption.defaultOptions().clear();
                    statusOption.defaultOptions().addAll(currentStatesStr);
                    childOptions.getStatus().setInOverride(true);
                } else {
                    // don't override
                    statusOption.selectedOptions().clear();
                    statusOption.selectedOptions().addAll(childOptions.getStatus().selectedOptions());

                    statusOption.defaultOptions().clear();
                    statusOption.defaultOptions().addAll(childOptions.getStatus().defaultOptions());
                    childOptions.getStatus().setInOverride(false);
                }

                // MODULE
//                if (!observableStampCoordinate.moduleNids().isEmpty()) {
//                    FilterOptions.Option moduleOption = filterOptions.getMainCoordinates().getModule();
//                    if (!childOptions.getModule().isInOverride()) {
//                        // override
//                        moduleOption.defaultOptions().clear();
//                        observableStampCoordinate.moduleNids().intStream().forEach(moduleNid -> {
//                            String moduleStr = calculator.getPreferredDescriptionStringOrNid(moduleNid);
//                            System.out.println("moduleStr = " + moduleStr);
//                            moduleOption.defaultOptions().add(moduleStr);
//                        });
//                    } else {
//                        // don't override
//                        moduleOption.selectedOptions().clear();
//                        moduleOption.selectedOptions().addAll(childOptions.getModule().selectedOptions());
//
//                        moduleOption.defaultOptions().clear();
//                        moduleOption.defaultOptions().addAll(childOptions.getModule().defaultOptions());
//                    }
//                }

                // populate the PATH
//                ConceptFacade currentPath = observableStampCoordinate.pathConceptProperty().getValue();
//                String currentPathStr = currentPath.description();
//
//                List<String> defaultSelectedPaths = new ArrayList<>(List.of(currentPathStr));
//                FilterOptions.Option pathOption = filterOptions.getMainCoordinates().getPath();
//                if (!childOptions.getPath().isInOverride()) {
//                    // override
//                    pathOption.defaultOptions().clear();
//                    pathOption.defaultOptions().addAll(defaultSelectedPaths);
//
//                    pathOption.selectedOptions().clear();
//                    pathOption.selectedOptions().addAll(defaultSelectedPaths);
//                } else {
//                    // don't override
//                    pathOption.selectedOptions().clear();
//                    pathOption.selectedOptions().addAll(childOptions.getPath().selectedOptions());
//
//                    pathOption.defaultOptions().clear();
//                    pathOption.defaultOptions().addAll(childOptions.getPath().defaultOptions());
//                }

                // TIME
//                FilterOptions.Option timeOption = filterOptions.getMainCoordinates().getTime();
//                if (!childOptions.getTime().isInOverride()) {
//                    // override
//                    Long time = observableStampCoordinate.timeProperty().getValue();
//                    if (!time.equals(Long.MAX_VALUE) && !time.equals(PREMUNDANE_TIME)) {
//                        Date date = new Date(time);
//                        timeOption.defaultOptions().clear();
//                        timeOption.selectedOptions().clear();
//                        timeOption.selectedOptions().add(SIMPLE_DATE_FORMAT.format(date));
//                        timeOption.defaultOptions().addAll(timeOption.selectedOptions());
//                    }
//                    timeOption.setInOverride(true);
//                } else {
//                    // don't override
//                    timeOption.selectedOptions().clear();
//                    timeOption.selectedOptions().addAll(childOptions.getTime().selectedOptions());
//
//                    timeOption.defaultOptions().clear();
//                    timeOption.defaultOptions().addAll(childOptions.getTime().defaultOptions());
//                    timeOption.setInOverride(false);
//                }
            } /*else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate
                && observableLanguageCoordinate.hasOverrides()) {
                // populate the LANGUAGE
                FilterOptions.Option language = filterOptions.getLanguageCoordinates(0).getLanguage();
                language.defaultOptions().clear();
                String languageStr = calculator.languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(
                        observableLanguageCoordinate.languageConceptProperty().get().nid());
                language.defaultOptions().add(languageStr);
                language.selectedOptions().clear();
                language.selectedOptions().addAll(language.defaultOptions());
                language.setInOverride(true);

                //FIXME description choices don't yet align with parent/classic menu, more discussion needs to happen on
                // how we want to fix this.
                // all set in FilterOptions
            }*/
        }
        return filterOptions;
    }


    public static long getMillis(FilterOptions filterOptions) {
        FilterOptions.Option time = filterOptions.getMainCoordinates().getTime();
        if (time == null || time.selectedOptions().isEmpty()) {
            return -1;
        }

        if (!time.selectedOptions().getFirst().startsWith("Latest")) {
            Date date;
            try {
                date = SIMPLE_DATE_FORMAT.parse(time.selectedOptions().getFirst());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return date.getTime();
        } else {
            return Long.MAX_VALUE;
        }
    }

    public static List<LocalDateTime> getTimesInUse() {
        ImmutableLongList times = StampService.get().getTimesInUse().toReversed();
        return Arrays.stream(times.toArray())
                .filter(time -> time != PREMUNDANE_TIME)
                .boxed()
                .map(time -> DateTimeUtil.epochToZonedDateTime(time).toLocalDateTime())
                .toList();
    }

    private static int findNidForDescription(Navigator navigator, int nid, String description) {
        return navigator.getChildEdges(nid).stream()
                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
                .findFirst()
                .map(Edge::destinationNid)
                .orElseThrow();
    }

    public static List<String> getDescendentsList(Navigator navigator, int parentNid, String description) {
        int nid = parentNid;
        for (String s : description.split(", ")) {
            nid = findNidForDescription(navigator, nid, s);
        }
        return navigator.getViewCalculator().descendentsOf(nid).intStream().boxed()
                .map(i -> Entity.getFast(i).description())
                .sorted()
                .toList();
    }

    public static List<String> stateSetToList(StateSet states) {
        return states.toEnumSet().stream().map(Enum::name).toList();
    }
}
