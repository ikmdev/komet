package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.view.ListPropertyWithOverride;
import dev.ikm.komet.framework.view.LongPropertyWithOverride;
import dev.ikm.komet.framework.view.ObjectPropertyWithOverride;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableNavigationCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.SetPropertyWithOverride;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Subscription;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;

public class FilterOptionsUtils {

    public FilterOptionsUtils() {}

    private Subscription nodeSubscription;
    private Subscription viewSubscription;
    private Subscription optionSubscription;

    private boolean fromView;
    private boolean fromFilter;

    // pass changes from View (typically the nodeView) to FilterOptions (typically the defaultFilterOptions)
    public void subscribeFilterOptionsToView(FilterOptions filterOptions, ObservableView observableView) {

        // remove previous subscriptions
        unsubscribeView();

        ObservableView observableViewForFilterProperty = filterOptions.observableViewForFilterProperty();
        FilterOptions.MainFilterCoordinates mainCoordinates = filterOptions.getMainCoordinates();
        List<FilterOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();

        // When any coordinate property from the View changes, this subscribers will change immediately the F.O. coordinate property,
        // and the selectedOptions for the related Option, but also directly to the top F.O.
        // observableViewForFilter, so it is safe to add a listener just to this property to get notified of any change
        // in any of its coordinates (that is, options), and refresh the default F.O accordingly.
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {

                // NAVIGATION
                viewSubscription = viewSubscription.and(observableNavigationCoordinate.navigationPatternsProperty().subscribe(nav -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getNavigator().selectedOptions().clear();
                    if (nav != null) {
                        mainCoordinates.getNavigator().selectedOptions().addAll(nav);
                    }
                    observableViewForFilterProperty.navigationCoordinate().navigationPatternsProperty().set(nav);
                    fromView = false;
                }));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // STATUS
                viewSubscription = viewSubscription.and(observableStampCoordinate.allowedStatesProperty().subscribe(stateSet -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getStatus().selectedOptions().clear();
                    if (stateSet != null) {
                        mainCoordinates.getStatus().selectedOptions().addAll(stateSet.toEnumSet().stream().toList());
                    }
                    observableViewForFilterProperty.stampCoordinate().allowedStatesProperty().set(stateSet);
                    fromView = false;
                }));

                // TIME
                viewSubscription = viewSubscription.and(observableStampCoordinate.timeProperty().subscribe(t -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getTime().selectedOptions().clear();
                    if (t != null) {
                        Long time = t.longValue();
                        if (!time.equals(Long.MAX_VALUE) && !time.equals(PREMUNDANE_TIME)) {
                            //FIXME the custom control doesn't support premundane yet
                            mainCoordinates.getTime().selectedOptions().addAll(String.valueOf(time));
                        } else if (time.equals(Long.MAX_VALUE)) {
                            mainCoordinates.getTime().selectedOptions().addAll(mainCoordinates.getTime().availableOptions().getFirst());
                        }
                    }
                    observableViewForFilterProperty.stampCoordinate().timeProperty().setValue(t);
                    fromView = false;
                }));

                // MODULE
                viewSubscription = viewSubscription.and(observableStampCoordinate.moduleSpecificationsProperty().subscribe(m -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getModule().selectedOptions().clear();
                    if (m != null) {
                        mainCoordinates.getModule().selectedOptions().addAll(m);
                    }
                    observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().set(m);
                    fromView = false;
                }));
                viewSubscription = viewSubscription.and(observableStampCoordinate.excludedModuleSpecificationsProperty().subscribe(e -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getModule().excludedOptions().clear();
                    if (e != null) {
                        mainCoordinates.getModule().excludedOptions().addAll(e);
                    }
                    observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().set(e);
                    fromView = false;
                }));

                // PATH
                viewSubscription = viewSubscription.and(observableStampCoordinate.pathConceptProperty().subscribe(path -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    if (path != null) {
                        mainCoordinates.getPath().selectedOptions().clear();
                        mainCoordinates.getPath().selectedOptions().addAll(path);
                    }
                    observableViewForFilterProperty.stampCoordinate().pathConceptProperty().set(path);
                    fromView = false;
                }));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {

                // LANGUAGE
                // todo: support more language coordinates for secondary+ languages

                FilterOptions.LanguageFilterCoordinates languageFilterCoordinates = languageCoordinatesList.getFirst();
                viewSubscription = viewSubscription.and(observableLanguageCoordinate.languageConceptProperty().subscribe(lang -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    languageFilterCoordinates.getLanguage().selectedOptions().clear();
                    if (lang != null) {
                        languageFilterCoordinates.getLanguage().selectedOptions().addAll(lang);
                    }
                    // update dialect
                    languageFilterCoordinates.getDialect().selectedOptions().clear();
                    if (TinkarTerm.ENGLISH_LANGUAGE.equals(lang)) {
                        ObservableList<PatternFacade> list = observableLanguageCoordinate.dialectPatternPreferenceListProperty().get();
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
                    } else {
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().clear();
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().languageConceptProperty().set(lang);
                    fromView = false;
                }));

                viewSubscription = viewSubscription.and(observableLanguageCoordinate.dialectPatternPreferenceListProperty().subscribe(list -> {
                    if (fromFilter) {
                        return;
                    }
                    if (!TinkarTerm.ENGLISH_LANGUAGE.equals(observableLanguageCoordinate.languageConcept())) {
                        // ignore
                        return;
                    }
                    fromView = true;
                    languageFilterCoordinates.getDialect().selectedOptions().clear();
                    if (list != null) {
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
                    fromView = false;
                }));

                viewSubscription = viewSubscription.and(observableLanguageCoordinate.descriptionTypePreferenceListProperty().subscribe(list -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    languageFilterCoordinates.getDescriptionType().selectedOptions().clear();
                    if (list != null) {
                        languageFilterCoordinates.getDescriptionType().selectedOptions().addAll(list);
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().descriptionTypePreferenceListProperty().set(list);
                    fromView = false;
                }));
            }
        }
    }

    private void unsubscribeView() {
        if (viewSubscription != null) {
            viewSubscription.unsubscribe();
        }
        viewSubscription = Subscription.EMPTY;
    }

    // pass changes from FilterOptions to view (typically the nodeView)
    public void subscribeViewToFilterOptions(FilterOptions filterOptions, ObservableView observableView) {
        // remove previous subscriptions
        unsubscribeNodeFilterOptions();

        // add Option to observableViewForFilterProperty subscriptions
        addOptionSubscriptions(filterOptions);

        // get parent menu settings
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {
                // NAVIGATION
                nodeSubscription = nodeSubscription.and(
                    filterOptions.observableViewForFilterProperty().navigationCoordinate().navigationPatternsProperty().subscribe(nav -> {
                        if (fromView) {
                            return;
                        }
                        SetPropertyWithOverride<PatternFacade> propertyWithOverride = (SetPropertyWithOverride<PatternFacade>) observableNavigationCoordinate.navigationPatternsProperty();
                        if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getNavigator().isInOverride()) {
                            // force a reset of the property, so it fires a change event when it gets updated
                            // to its originalValue, in case parentView and nodeView values are the same
                            observableNavigationCoordinate.navigationPatternsProperty().set(FXCollections.emptyObservableSet()); // Dummy
                            propertyWithOverride.removeOverride();
                        } else if (!observableView.navigationCoordinate().navigationPatternsProperty().get().equals(nav)) {
                            fromFilter = true;
                            observableNavigationCoordinate.navigationPatternsProperty().set(nav);
                            fromFilter = false;
                        }
                    }));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // STATUS
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().allowedStatesProperty().subscribe(stateSet -> {
                            if (fromView) {
                                return;
                            }
                            ObjectPropertyWithOverride<StateSet> propertyWithOverride = (ObjectPropertyWithOverride<StateSet>) observableStampCoordinate.allowedStatesProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getStatus().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableStampCoordinate.allowedStatesProperty().set(StateSet.make()); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableStampCoordinate.allowedStatesProperty().get().equals(stateSet)) {
                                fromFilter = true;
                                observableStampCoordinate.allowedStatesProperty().set(stateSet);
                                fromFilter = false;
                            }
                        }));

                // TIME
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().timeProperty().subscribe(time -> {
                            if (fromView) {
                                return;
                            }
                            LongPropertyWithOverride propertyWithOverride = (LongPropertyWithOverride) observableStampCoordinate.timeProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getTime().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableStampCoordinate.timeProperty().set(-1L); // Dummy
                                propertyWithOverride.removeOverride();
                            } else if (observableStampCoordinate.timeProperty().get() != time.longValue()) {
                                fromFilter = true;
                                observableStampCoordinate.timeProperty().set(time.longValue());
                                fromFilter = false;
                            }
                        }));

                // MODULE
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().moduleSpecificationsProperty().subscribe(m -> {
                            if (fromView) {
                                return;
                            }
                            SetPropertyWithOverride<ConceptFacade> propertyWithOverride = (SetPropertyWithOverride<ConceptFacade>) observableStampCoordinate.moduleSpecificationsProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getModule().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableStampCoordinate.moduleSpecificationsProperty().set(FXCollections.emptyObservableSet()); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableStampCoordinate.moduleSpecificationsProperty().get().equals(m)) {
                                fromFilter = true;
                                observableStampCoordinate.moduleSpecificationsProperty().set(m);
                                fromFilter = false;
                            }
                        }));

                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().excludedModuleSpecificationsProperty().subscribe(e -> {
                            if (fromView) {
                                return;
                            }
                            SetPropertyWithOverride<ConceptFacade> propertyWithOverride = (SetPropertyWithOverride<ConceptFacade>) observableStampCoordinate.excludedModuleSpecificationsProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getModule().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableStampCoordinate.excludedModuleSpecificationsProperty().set(FXCollections.emptyObservableSet()); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableStampCoordinate.excludedModuleSpecificationsProperty().get().equals(e)) {
                                fromFilter = true;
                                observableStampCoordinate.excludedModuleSpecificationsProperty().set(e);
                                fromFilter = false;
                            }
                        }));

                // PATH
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().pathConceptProperty().subscribe(path -> {
                            if (fromView) {
                                return;
                            }
                            ObjectPropertyWithOverride<ConceptFacade> propertyWithOverride = (ObjectPropertyWithOverride<ConceptFacade>) observableStampCoordinate.pathConceptProperty();
                            if (propertyWithOverride.isOverridden() &&
                                    !filterOptions.getMainCoordinates().getPath().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableStampCoordinate.pathConceptProperty().set(filterOptions.getMainCoordinates().getPath().availableOptions().stream()
                                        .filter(o -> o.nid() != propertyWithOverride.getValue().nid()).findFirst().orElse(null)); // other path, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableStampCoordinate.pathConceptProperty().get().equals(path)) {
                                fromFilter = true;
                                observableStampCoordinate.pathConceptProperty().set(path);
                                fromFilter = false;
                            }
                        }));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {

                // LANGUAGE
                // todo: more languages
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().languageConceptProperty().subscribe(lang -> {
                            if (fromView) {
                                return;
                            }
                            ObjectPropertyWithOverride<ConceptFacade> propertyWithOverride = (ObjectPropertyWithOverride<ConceptFacade>) observableLanguageCoordinate.languageConceptProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getLanguage().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableLanguageCoordinate.languageConceptProperty().set(TinkarTerm.LANGUAGE_COORDINATE_NAME); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableLanguageCoordinate.languageConceptProperty().get().equals(lang)) {
                                fromFilter = true;
                                observableLanguageCoordinate.languageConceptProperty().set(lang);
                                fromFilter = false;
                            }
                        }));
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().dialectPatternPreferenceListProperty().subscribe(list -> {
                            if (fromView) {
                                return;
                            }
                            ListPropertyWithOverride<PatternFacade> propertyWithOverride = (ListPropertyWithOverride<PatternFacade>) observableLanguageCoordinate.dialectPatternPreferenceListProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getDialect().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableLanguageCoordinate.dialectPatternPreferenceListProperty().set(FXCollections.emptyObservableList()); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableLanguageCoordinate.dialectPatternPreferenceListProperty().get().equals(list)) {
                                fromFilter = true;
                                observableLanguageCoordinate.dialectPatternPreferenceListProperty().set(list);
                                fromFilter = false;
                            }
                        }));
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().descriptionTypePreferenceListProperty().subscribe(list -> {
                            if (fromView) {
                                return;
                            }
                            ListPropertyWithOverride<ConceptFacade> propertyWithOverride = (ListPropertyWithOverride<ConceptFacade>) observableLanguageCoordinate.descriptionTypePreferenceListProperty();
                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getDialect().isInOverride()) {
                                // force a reset of the property, so it fires a change event when it gets updated
                                // to its originalValue, in case parentView and nodeView values are the same
                                observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(FXCollections.emptyObservableList()); // Dummy, not null
                                propertyWithOverride.removeOverride();
                            } else if (!observableLanguageCoordinate.descriptionTypePreferenceListProperty().get().equals(list)) {
                                fromFilter = true;
                                observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(list);
                                fromFilter = false;
                            }
                        }));
            }
        }
    }

    public void unsubscribeNodeFilterOptions() {
        if (nodeSubscription != null) {
            nodeSubscription.unsubscribe();
        }
        nodeSubscription = Subscription.EMPTY;
    }

    // Subscribe Option's selectedOptions observable list to notify F.O. observableViewForFilter related coordinates properties
    private void addOptionSubscriptions(FilterOptions filterOptions) {
        // remove previous subscriptions
        unsubscribeOptions();

        ObservableView observableViewForFilter = filterOptions.observableViewForFilterProperty();
        FilterOptions.MainFilterCoordinates mainCoordinates = filterOptions.getMainCoordinates();
        List<FilterOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();

        // NAVIGATOR
        optionSubscription = optionSubscription.and(mainCoordinates.getNavigator().selectedOptions().subscribe(() ->
                updateNavigatorProperty(filterOptions)));
        updateNavigatorProperty(filterOptions);

        // STATUS
        optionSubscription = optionSubscription.and(mainCoordinates.getStatus().selectedOptions().subscribe(() ->
                updateStatusProperty(filterOptions)));
        updateStatusProperty(filterOptions);

        // TIME
        optionSubscription = optionSubscription.and(mainCoordinates.getTime().selectedOptions().subscribe(() ->
                updateTimeProperty(filterOptions)));
        updateTimeProperty(filterOptions);

        // MODULE
        optionSubscription = optionSubscription.and(mainCoordinates.getModule().selectedOptions().subscribe(() ->
                updateModuleProperty(filterOptions)));
        updateModuleProperty(filterOptions);

        optionSubscription = optionSubscription.and(mainCoordinates.getModule().excludedOptions().subscribe(() ->
                updateModuleExcProperty(filterOptions)));
        updateModuleExcProperty(filterOptions);

        // PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getPath().selectedOptions().subscribe(() ->
                updatePathProperty(filterOptions)));
        updatePathProperty(filterOptions);

        // LANGUAGE
        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getLanguage().selectedOptions().subscribe(() ->
                updateLangProperty(filterOptions)));
        updateLangProperty(filterOptions);

        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getDialect().selectedOptions().subscribe(() ->
                updateDialectProperty(filterOptions)));
        updateDialectProperty(filterOptions);

        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getDescriptionType().selectedOptions().subscribe(() ->
                updateDescriptionProperty(filterOptions)));
        updateDescriptionProperty(filterOptions);
    }

    private void unsubscribeOptions() {
        if (optionSubscription != null) {
            optionSubscription.unsubscribe();
        }
        optionSubscription = Subscription.EMPTY;
    }

    private void updateNavigatorProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<PatternFacade> selectedOptions = filterOptions.getMainCoordinates().getNavigator().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            PatternFacade patternFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().navigationCoordinate().navigationPatternsProperty().set(FXCollections.observableSet(patternFacade));
            fromFilter = false;
        }
    }

    private void updateStatusProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        List<State> stateList = filterOptions.getMainCoordinates().getStatus().selectedOptions().stream().toList();
        fromFilter = true;
        filterOptions.observableViewForFilterProperty().stampCoordinate().allowedStatesProperty().set(StateSet.of(stateList));
        fromFilter = false;
    }

    private void updateTimeProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<String> selectedOptions = filterOptions.getMainCoordinates().getTime().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            String time = selectedOptions.getFirst();
            long t;
            try {
                t = Long.parseLong(time);
            } catch (NumberFormatException e) {
                t = Long.MAX_VALUE;
            }
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().stampCoordinate().timeProperty().set(t);
            fromFilter = false;
        }
    }

    private void updateModuleProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        Set<ConceptFacade> includedSet = new HashSet<>(filterOptions.getMainCoordinates().getModule().selectedOptions());
        fromFilter = true;
        filterOptions.observableViewForFilterProperty().stampCoordinate().moduleSpecificationsProperty().addAll(includedSet);
        fromFilter = false;
    }

    private void updateModuleExcProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        Set<ConceptFacade> excludedSet = new HashSet<>(filterOptions.getMainCoordinates().getModule().excludedOptions());
        fromFilter = true;
        filterOptions.observableViewForFilterProperty().stampCoordinate().excludedModuleSpecificationsProperty().addAll(excludedSet);
        fromFilter = false;
    }

    private void updatePathProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getPath().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().stampCoordinate().pathConceptProperty().set(conceptFacade);
            fromFilter = false;
        }
    }

    private void updateLangProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<EntityFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getLanguage().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            EntityFacade entityFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().languageConceptProperty().set((ConceptFacade) entityFacade);
            fromFilter = false;
        }
    }

    private void updateDialectProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<PatternFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getDialect().selectedOptions().stream()
                .map(e -> (PatternFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
        if (!selectedOptions.isEmpty()) {
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().dialectPatternPreferenceListProperty().setValue(selectedOptions);
            fromFilter = false;
        }
    }

    private void updateDescriptionProperty(FilterOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getDescriptionType().selectedOptions().stream()
                .map(e -> (ConceptFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
        if (!selectedOptions.isEmpty()) {
            fromFilter = true;
            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().descriptionTypePreferenceListProperty().setValue(selectedOptions);
            fromFilter = false;
        }
    }

    public static List<ZonedDateTime> getTimesInUse() {
        ImmutableLongList times = StampService.get().getTimesInUse().toReversed();
        return Arrays.stream(times.toArray())
                .filter(time -> time != PREMUNDANE_TIME)
                .boxed()
                .map(DateTimeUtil::epochToZonedDateTime)
                .toList();
    }

    private static int findNidForDescription(Navigator navigator, int nid, String description) {
        return navigator.getChildEdges(nid).stream()
                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
                .findFirst()
                .map(Edge::destinationNid)
                .orElseThrow();
    }

    public static List<EntityFacade> getDescendentsList(Navigator navigator, int parentNid, String description) {
        int nid = parentNid;
        for (String s : description.split(", ")) {
            nid = findNidForDescription(navigator, nid, s);
        }
        return navigator.getViewCalculator().descendentsOf(nid).intStream().boxed()
                .map(i -> (EntityFacade) Entity.getFast(i))
                .sorted()
                .toList();
    }

    public static <T> String getDescription(ViewCalculator viewCalculator, T t) {
        return switch (t) {
            case String value -> value;
            case State value -> viewCalculator == null ?
                    Entity.getFast(value.nid()).description() :
                    viewCalculator.getPreferredDescriptionTextOrNid(value.nid());
            case Long value -> String.valueOf(value);
            case EntityFacade value -> viewCalculator == null ?
                    value.description() : viewCalculator.getPreferredDescriptionTextOrNid(value);
            default -> throw new RuntimeException("Unsupported type: " + t.getClass().getName());
        };
    }
}
