package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableNavigationCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.collections.ObservableList;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditCoordinateOptionsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EditCoordinateOptionsUtils.class);
    private static final String DEFAULT_DESCRIPTION_STRING = Integer.toString(Integer.MAX_VALUE);

    public EditCoordinateOptionsUtils() {}

    private Subscription nodeSubscription;
    private Subscription viewSubscription;
    private Subscription optionSubscription;

    private boolean fromView;
    private boolean fromFilter;

    // pass changes from View (typically the nodeView.editCoordinate()) to EditCoordinateOptions (typically the defaultEditCoordinateOptions)
    public void subscribeEditCoordinateOptionsToView(EditCoordinateOptions editCoordinateOptions, ObservableView observableView) {

        // remove previous subscriptions
        unsubscribeView();
        ObservableEditCoordinate observableEditCoordinateForOptionsProperty = editCoordinateOptions.observableEditCoordinateForOptionsProperty();
        EditCoordinateOptions.MainEditCoordinates mainCoordinates = editCoordinateOptions.getMainCoordinates();
//        List<EditCoordinateOptions.LanguageFilterCoordinates> languageCoordinatesList = editCoordinateOptions.getLanguageCoordinatesList();

        // When any coordinate property from the View changes, this subscribers will change immediately the F.O. coordinate property,
        // and the selectedOptions for the related Option, but also directly to the top F.O.
        // observableViewForFilter, so it is safe to add a listener just to this property to get notified of any change
        // in any of its coordinates (that is, options), and refresh the default F.O accordingly.
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {

//                // NAVIGATION
//                viewSubscription = viewSubscription.and(observableNavigationCoordinate.navigationPatternsProperty().subscribe(nav -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    mainCoordinates.getNavigator().selectedOptions().clear();
//                    if (nav != null) {
//                        mainCoordinates.getNavigator().selectedOptions().addAll(nav);
//                    }
//                    observableViewForFilterProperty.navigationCoordinate().navigationPatternsProperty().set(nav);
//                    fromView = false;
//                }));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

//                // STATUS
//                viewSubscription = viewSubscription.and(observableStampCoordinate.allowedStatesProperty().subscribe(stateSet -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    mainCoordinates.getStatus().selectedOptions().clear();
//                    if (stateSet != null) {
//                        mainCoordinates.getStatus().selectedOptions().addAll(stateSet.toEnumSet().stream().toList());
//                    }
//                    observableViewForFilterProperty.stampCoordinate().allowedStatesProperty().set(stateSet);
//                    fromView = false;
//                }));
//
//                // TIME
//                viewSubscription = viewSubscription.and(observableStampCoordinate.timeProperty().subscribe(t -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true; // when user clicks on the titled pane, the time (should be from spinner or dropdown)
//                    mainCoordinates.getTime().selectedOptions().clear();
//                    if (t != null) {
//                        Long time = t.longValue();
//                        LOG.info("Filter Option date time selected {} - epoch millis: {}", new Date(time), time);
//                        mainCoordinates.getTime().selectedOptions().addAll(String.valueOf(time));
//                    }
//                    observableViewForFilterProperty.stampCoordinate().timeProperty().setValue(t);
//                    fromView = false;
//                }));

//                // MODULE
//                viewSubscription = viewSubscription.and(observableStampCoordinate.moduleSpecificationsProperty().subscribe(m -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    mainCoordinates.getModule().selectedOptions().clear();
//                    if (m != null) {
//                        // When the set is empty, it means "all module wildcard", and that implies "Any" is selected
//                        // When the set contains all available modules, it means "all individual modules",
//                        // and that implies "Select All" is selected.
//                        // If it only contains a few of them, "Select All" and "Any" are deselected, and those modules
//                        // are selected.
//                        mainCoordinates.getModule().setAny(m.isEmpty());
//                        mainCoordinates.getModule().selectedOptions().addAll(m.stream().toList());
//                        observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().set(
//                                m.stream().collect(Collectors.toCollection(FXCollections::observableSet)));
//                    } else {
//                        observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().set(null);
//                    }
//                    fromView = false;
//                }));
//                viewSubscription = viewSubscription.and(observableStampCoordinate.excludedModuleSpecificationsProperty().subscribe(e -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    mainCoordinates.getModule().excludedOptions().clear();
//                    if (e != null) {
//                        mainCoordinates.getModule().excludedOptions().addAll(e.stream().toList());
//                        observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().set(
//                                e.stream().collect(Collectors.toCollection(FXCollections::observableSet)));
//                    } else {
//                        observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().set(null);
//                    }
//                    fromView = false;
//                }));
//
//                // PATH
//                viewSubscription = viewSubscription.and(observableStampCoordinate.pathConceptProperty().subscribe(path -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    if (path != null) {
//                        mainCoordinates.getPath().selectedOptions().clear();
//                        mainCoordinates.getPath().selectedOptions().addAll(path);
//                    }
//                    observableViewForFilterProperty.stampCoordinate().pathConceptProperty().set(path);
//                    fromView = false;
//                }));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {

//                // LANGUAGE
//                // todo: support more language coordinates for secondary+ languages
//
//                EditCoordinateOptions.LanguageFilterCoordinates languageFilterCoordinates = languageCoordinatesList.getFirst();
//                viewSubscription = viewSubscription.and(observableLanguageCoordinate.languageConceptProperty().subscribe(lang -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    languageFilterCoordinates.getLanguage().selectedOptions().clear();
//                    if (lang != null) {
//                        languageFilterCoordinates.getLanguage().selectedOptions().addAll(lang);
//                    }
//                    // update dialect
//                    languageFilterCoordinates.getDialect().selectedOptions().clear();
//                    if (TinkarTerm.ENGLISH_LANGUAGE.equals(lang)) {
//                        ObservableList<PatternFacade> list = observableLanguageCoordinate.dialectPatternPreferenceListProperty().get();
//                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
//                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
//                    } else {
//                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().clear();
//                    }
//                    observableViewForFilterProperty.languageCoordinates().getFirst().languageConceptProperty().set(lang);
//                    fromView = false;
//                }));
//
//                viewSubscription = viewSubscription.and(observableLanguageCoordinate.dialectPatternPreferenceListProperty().subscribe(list -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    if (!TinkarTerm.ENGLISH_LANGUAGE.equals(observableLanguageCoordinate.languageConcept())) {
//                        // ignore
//                        return;
//                    }
//                    fromView = true;
//                    languageFilterCoordinates.getDialect().selectedOptions().clear();
//                    if (list != null) {
//                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
//                    }
//                    observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
//                    fromView = false;
//                }));
//
//                viewSubscription = viewSubscription.and(observableLanguageCoordinate.descriptionTypePreferenceListProperty().subscribe(list -> {
//                    if (fromFilter) {
//                        return;
//                    }
//                    fromView = true;
//                    languageFilterCoordinates.getDescriptionType().selectedOptions().clear();
//                    if (list != null) {
//                        languageFilterCoordinates.getDescriptionType().selectedOptions().addAll(list);
//                    }
//                    observableViewForFilterProperty.languageCoordinates().getFirst().descriptionTypePreferenceListProperty().set(list);
//                    fromView = false;
//                }));
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
    public void subscribeViewToFilterOptions(EditCoordinateOptions filterOptions, ObservableView observableView) {
        // remove previous subscriptions
        unsubscribeNodeFilterOptions();

        // get parent menu settings
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {
//                // NAVIGATION
//                nodeSubscription = nodeSubscription.and(
//                    filterOptions.observableViewForFilterProperty().navigationCoordinate().navigationPatternsProperty().subscribe(nav -> {
//                        if (fromView) {
//                            return;
//                        }
//                        SetPropertyWithOverride<PatternFacade> propertyWithOverride = (SetPropertyWithOverride<PatternFacade>) observableNavigationCoordinate.navigationPatternsProperty();
//                        if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getNavigator().isInOverride()) {
//                            // force a reset of the property, so it fires a change event when it gets updated
//                            // to its originalValue, in case parentView and nodeView values are the same
//                            observableNavigationCoordinate.navigationPatternsProperty().set(FXCollections.emptyObservableSet()); // Dummy
//                            propertyWithOverride.removeOverride();
//                        } else if (!observableView.navigationCoordinate().navigationPatternsProperty().get().equals(nav)) {
//                            fromFilter = true;
//                            observableNavigationCoordinate.navigationPatternsProperty().set(nav);
//                            fromFilter = false;
//                        }
//                    }));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

//                // STATUS
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableViewForFilterProperty().stampCoordinate().allowedStatesProperty().subscribe(stateSet -> {
//                            if (fromView) {
//                                return;
//                            }
//                            ObjectPropertyWithOverride<StateSet> propertyWithOverride = (ObjectPropertyWithOverride<StateSet>) observableStampCoordinate.allowedStatesProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getStatus().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableStampCoordinate.allowedStatesProperty().set(StateSet.make()); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableStampCoordinate.allowedStatesProperty().get().equals(stateSet)) {
//                                fromFilter = true;
//                                observableStampCoordinate.allowedStatesProperty().set(stateSet);
//                                fromFilter = false;
//                            }
//                        }));

//                // TIME
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableViewForFilterProperty().stampCoordinate().timeProperty().subscribe(time -> {
//                            if (fromView) {
//                                return;
//                            }
//                            LongPropertyWithOverride propertyWithOverride = (LongPropertyWithOverride) observableStampCoordinate.timeProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getTime().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableStampCoordinate.timeProperty().set(-1L); // Dummy
//                                propertyWithOverride.removeOverride();
//                            } else if (observableStampCoordinate.timeProperty().get() != time.longValue()) {
//                                fromFilter = true;
//                                observableStampCoordinate.timeProperty().set(time.longValue());
//                                fromFilter = false;
//                            }
//                        }));

//                // MODULE
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableEditCoordinateForOptionsProperty().stampCoordinate().moduleSpecificationsProperty().subscribe(m -> {
//                            if (fromView) {
//                                return;
//                            }
//                            SetPropertyWithOverride<ConceptFacade> propertyWithOverride = (SetPropertyWithOverride<ConceptFacade>) observableStampCoordinate.moduleSpecificationsProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getModule().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableStampCoordinate.moduleSpecificationsProperty().set(FXCollections.emptyObservableSet()); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableStampCoordinate.moduleSpecificationsProperty().get().equals(m)) {
//                                fromFilter = true;
//                                observableStampCoordinate.moduleSpecificationsProperty().set(m);
//                                fromFilter = false;
//                            }
//                        }));
//
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableEditCoordinateForOptionsProperty().stampCoordinate().excludedModuleSpecificationsProperty().subscribe(e -> {
//                            if (fromView) {
//                                return;
//                            }
//                            SetPropertyWithOverride<ConceptFacade> propertyWithOverride = (SetPropertyWithOverride<ConceptFacade>) observableStampCoordinate.excludedModuleSpecificationsProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getMainCoordinates().getModule().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableStampCoordinate.excludedModuleSpecificationsProperty().set(FXCollections.emptyObservableSet()); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableStampCoordinate.excludedModuleSpecificationsProperty().get().equals(e)) {
//                                fromFilter = true;
//                                observableStampCoordinate.excludedModuleSpecificationsProperty().set(e);
//                                fromFilter = false;
//                            }
//                        }));
//
//                // PATH
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableEditCoordinateForOptionsProperty().stampCoordinate().pathConceptProperty().subscribe(path -> {
//                            if (fromView) {
//                                return;
//                            }
//                            ObjectPropertyWithOverride<ConceptFacade> propertyWithOverride = (ObjectPropertyWithOverride<ConceptFacade>) observableStampCoordinate.pathConceptProperty();
//                            if (propertyWithOverride.isOverridden() &&
//                                    !filterOptions.getMainCoordinates().getPath().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableStampCoordinate.pathConceptProperty().set(filterOptions.getMainCoordinates().getPath().availableOptions().stream()
//                                        .filter(o -> o.nid() != propertyWithOverride.getValue().nid()).findFirst().orElse(null)); // other path, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableStampCoordinate.pathConceptProperty().get().equals(path)) {
//                                fromFilter = true;
//                                observableStampCoordinate.pathConceptProperty().set(path);
//                                fromFilter = false;
//                            }
//                        }));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {
//
//                // LANGUAGE
//                // todo: more languages
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().languageConceptProperty().subscribe(lang -> {
//                            if (fromView) {
//                                return;
//                            }
//                            ObjectPropertyWithOverride<ConceptFacade> propertyWithOverride = (ObjectPropertyWithOverride<ConceptFacade>) observableLanguageCoordinate.languageConceptProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getLanguage().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableLanguageCoordinate.languageConceptProperty().set(TinkarTerm.LANGUAGE_COORDINATE_NAME); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableLanguageCoordinate.languageConceptProperty().get().equals(lang)) {
//                                fromFilter = true;
//                                observableLanguageCoordinate.languageConceptProperty().set(lang);
//                                fromFilter = false;
//                            }
//                        }));
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().dialectPatternPreferenceListProperty().subscribe(list -> {
//                            if (fromView) {
//                                return;
//                            }
//                            ListPropertyWithOverride<PatternFacade> propertyWithOverride = (ListPropertyWithOverride<PatternFacade>) observableLanguageCoordinate.dialectPatternPreferenceListProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getDialect().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableLanguageCoordinate.dialectPatternPreferenceListProperty().set(FXCollections.emptyObservableList()); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableLanguageCoordinate.dialectPatternPreferenceListProperty().get().equals(list)) {
//                                fromFilter = true;
//                                observableLanguageCoordinate.dialectPatternPreferenceListProperty().set(list);
//                                fromFilter = false;
//                            }
//                        }));
//                nodeSubscription = nodeSubscription.and(
//                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().descriptionTypePreferenceListProperty().subscribe(list -> {
//                            if (fromView) {
//                                return;
//                            }
//                            ListPropertyWithOverride<ConceptFacade> propertyWithOverride = (ListPropertyWithOverride<ConceptFacade>) observableLanguageCoordinate.descriptionTypePreferenceListProperty();
//                            if (propertyWithOverride.isOverridden() && !filterOptions.getLanguageCoordinatesList().getFirst().getDialect().isInOverride()) {
//                                // force a reset of the property, so it fires a change event when it gets updated
//                                // to its originalValue, in case parentView and nodeView values are the same
//                                observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(FXCollections.emptyObservableList()); // Dummy, not null
//                                propertyWithOverride.removeOverride();
//                            } else if (!observableLanguageCoordinate.descriptionTypePreferenceListProperty().get().equals(list)) {
//                                fromFilter = true;
//                                observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(list);
//                                fromFilter = false;
//                            }
//                        }));
            }
        }

        // add Option to observableViewForFilterProperty subscriptions
        addOptionSubscriptions(filterOptions);
    }

    public void unsubscribeNodeFilterOptions() {
        if (nodeSubscription != null) {
            nodeSubscription.unsubscribe();
        }
        nodeSubscription = Subscription.EMPTY;
    }

    // Subscribe Option's selectedOptions observable list to notify F.O. observableViewForFilter related coordinates properties
    private void addOptionSubscriptions(EditCoordinateOptions filterOptions) {
        // remove previous subscriptions
        unsubscribeOptions();

        EditCoordinateOptions.MainEditCoordinates mainCoordinates = filterOptions.getMainCoordinates();
//        List<EditCoordinateOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();

//        // NAVIGATOR
//        optionSubscription = optionSubscription.and(mainCoordinates.getNavigator().selectedOptions().subscribe(() ->
//                updateNavigatorProperty(filterOptions)));
//        updateNavigatorProperty(filterOptions);

//        // STATUS
//        optionSubscription = optionSubscription.and(mainCoordinates.getStatus().selectedOptions().subscribe(() ->
//                updateStatusProperty(filterOptions)));
//        updateStatusProperty(filterOptions);

//        // TIME
//        optionSubscription = optionSubscription.and(mainCoordinates.getTime().selectedOptions().subscribe(() ->
//                updateTimeProperty(filterOptions)));
//        updateTimeProperty(filterOptions);

//        // MODULE
//        optionSubscription = optionSubscription.and(mainCoordinates.getModule().selectedOptions().subscribe(() ->
//                updateModuleProperty(filterOptions)));
//        updateModuleProperty(filterOptions);
//
//        optionSubscription = optionSubscription.and(mainCoordinates.getModule().excludedOptions().subscribe(() ->
//                updateModuleExcProperty(filterOptions)));
//        updateModuleExcProperty(filterOptions);
//

        // AUTHOR FOR CHANGE
        optionSubscription = optionSubscription.and(mainCoordinates.getAuthorForChange().selectedOptions().subscribe(() ->
                updateAuthorForChangeProperty(filterOptions)));
        updateAuthorForChangeProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getAuthorForChange().availableOptions().subscribe(() ->
                System.out.println("=======>  changing available options. here is where the UI needs to update it's available options")));

        // default Module
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultModule().selectedOptions().subscribe(() ->
                updateDefaultModuleProperty(filterOptions)));
        updateDefaultModuleProperty(filterOptions);

        // destination Module
        optionSubscription = optionSubscription.and(mainCoordinates.getDestinationModule().selectedOptions().subscribe(() ->
                updateDestinationModuleProperty(filterOptions)));
        updateDestinationModuleProperty(filterOptions);

        // destination Module
        // default PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultPath().selectedOptions().subscribe(() ->
                updateDefaultPathProperty(filterOptions)));
        updateDefaultPathProperty(filterOptions);

        // promotion PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getPromotionPath().selectedOptions().subscribe(() ->
                updatePromotionPathProperty(filterOptions)));
        updatePromotionPathProperty(filterOptions);


//        // LANGUAGE
//        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getLanguage().selectedOptions().subscribe(() ->
//                updateLangProperty(filterOptions)));
//        updateLangProperty(filterOptions);
//
//        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getDialect().selectedOptions().subscribe(() ->
//                updateDialectProperty(filterOptions)));
//        updateDialectProperty(filterOptions);
//
//        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getDescriptionType().selectedOptions().subscribe(() ->
//                updateDescriptionProperty(filterOptions)));
//        updateDescriptionProperty(filterOptions);
    }

    private void unsubscribeOptions() {
        if (optionSubscription != null) {
            optionSubscription.unsubscribe();
        }
        optionSubscription = Subscription.EMPTY;
    }

//    private void updateNavigatorProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        ObservableList<PatternFacade> selectedOptions = filterOptions.getMainCoordinates().getNavigator().selectedOptions();
//        if (!selectedOptions.isEmpty()) {
//            PatternFacade patternFacade = selectedOptions.getFirst();
//            fromFilter = true;
//            filterOptions.observableViewForFilterProperty().navigationCoordinate().navigationPatternsProperty().set(FXCollections.observableSet(patternFacade));
//            fromFilter = false;
//        }
//    }

//    private void updateStatusProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        List<State> stateList = filterOptions.getMainCoordinates().getStatus().selectedOptions().stream().toList();
//        fromFilter = true;
//        filterOptions.observableViewForFilterProperty().stampCoordinate().allowedStatesProperty().set(StateSet.of(stateList));
//        fromFilter = false;
//    }

//    private void updateTimeProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        ObservableList<String> selectedOptions = filterOptions.getMainCoordinates().getTime().selectedOptions();
//        if (!selectedOptions.isEmpty()) {
//            String time = selectedOptions.getFirst();
//            long t;
//            try {
//                t = Long.parseLong(time);
//            } catch (NumberFormatException e) {
//                t = Long.MAX_VALUE;
//            }
//            fromFilter = true;
//            filterOptions.observableViewForFilterProperty().stampCoordinate().timeProperty().set(t);
//            fromFilter = false;
//        }
//    }

//    private void updateModuleProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        Set<ConceptFacade> includedSet = new HashSet<>();
//        if (!filterOptions.getMainCoordinates().getModule().any()) {
//            includedSet.addAll(filterOptions.getMainCoordinates().getModule().selectedOptions());
//        }
//        fromFilter = true;
//        filterOptions.observableEditCoordinateForOptionsProperty().stampCoordinate().moduleSpecificationsProperty().addAll(includedSet);
//        fromFilter = false;
//    }

//    private void updateModuleExcProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        Set<ConceptFacade> excludedSet = new HashSet<>(filterOptions.getMainCoordinates().getModule().excludedOptions());
//        fromFilter = true;
//        filterOptions.observableEditCoordinateForOptionsProperty().stampCoordinate().excludedModuleSpecificationsProperty().addAll(excludedSet);
//        fromFilter = false;
//    }


    private void updateAuthorForChangeProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getAuthorForChange().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            // TODO: Please verify and test the authorForChangesProperty is the correct way to set.
            filterOptions.observableEditCoordinateForOptionsProperty().authorForChangesProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updateDefaultModuleProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDefaultModule().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().defaultModuleProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updateDestinationModuleProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDestinationModule().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().destinationModuleProperty().set(conceptFacade);
            fromFilter = false;
        }
    }


    private void updateDefaultPathProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDefaultPath().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().defaultPathProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updatePromotionPathProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getPromotionPath().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().promotionPathProperty().set(conceptFacade);
            fromFilter = false;
        }
    }

//    private void updateLangProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        ObservableList<EntityFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getLanguage().selectedOptions();
//        if (!selectedOptions.isEmpty()) {
//            EntityFacade entityFacade = selectedOptions.getFirst();
//            fromFilter = true;
//            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().languageConceptProperty().set((ConceptFacade) entityFacade);
//            fromFilter = false;
//        }
//    }
//
//    private void updateDialectProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        ObservableList<PatternFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getDialect().selectedOptions().stream()
//                .map(e -> (PatternFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
//        if (!selectedOptions.isEmpty()) {
//            fromFilter = true;
//            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().dialectPatternPreferenceListProperty().setValue(selectedOptions);
//            fromFilter = false;
//        }
//    }
//
//    private void updateDescriptionProperty(EditCoordinateOptions filterOptions) {
//        if (fromView) {
//            return;
//        }
//        ObservableList<ConceptFacade> selectedOptions = filterOptions.getLanguageCoordinatesList().getFirst().getDescriptionType().selectedOptions().stream()
//                .map(e -> (ConceptFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
//        if (!selectedOptions.isEmpty()) {
//            fromFilter = true;
//            filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().descriptionTypePreferenceListProperty().setValue(selectedOptions);
//            fromFilter = false;
//        }
//    }
//
//    public static List<ZonedDateTime> getTimesInUse() {
//        SortedSet<ZonedDateTime> sortedSet = new TreeSet<>(Comparator.reverseOrder());
//        PrimitiveData.get().forEachStampNid(nid -> {
//            long time = EntityHandle.get(nid).expectStamp().time();
//            if (time != PREMUNDANE_TIME) {
//                sortedSet.add(Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault()));
//            }
//        });
//        return sortedSet.stream().toList();
//    }

//    private static int findNidForDescription(Navigator navigator, int nid, String description) {
//        return navigator.getChildEdges(nid).stream()
//                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
//                .findFirst()
//                .map(Edge::destinationNid)
//                .orElseThrow();
//    }

//    public static List<EntityFacade> getDescendentsList(Navigator navigator, int parentNid, String description) {
//        int nid = parentNid;
//        for (String s : description.split(", ")) {
//            nid = findNidForDescription(navigator, nid, s);
//        }
//        return navigator.getViewCalculator().descendentsOf(nid).intStream().boxed()
//                .map(i -> (EntityFacade) Entity.getFast(i))
//                .sorted()
//                .toList();
//    }

    public static <T> String getDescription(ViewCalculator viewCalculator, T t) {
        return switch (t) {
            case String value -> value;
            case State value -> viewCalculator == null ?
                    Entity.getFast(value.nid()).description() :
                    getDescriptionTextOrNid(viewCalculator, value.nid());
            case Long value -> String.valueOf(value);
            case EntityFacade value -> {
                if (viewCalculator != null) {
                    yield getDescriptionTextOrNid(viewCalculator, value);
                }
                yield value.description();
            }

            default -> throw new RuntimeException("Unsupported type: " + t.getClass().getName());
        };
    }

    public static String getDescriptionTextOrNid(ViewCalculator viewCalculator, int nid) {
        try {
            return viewCalculator.getDescriptionTextOrNid(nid);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return DEFAULT_DESCRIPTION_STRING;
        }
    }

    public static String getDescriptionTextOrNid(ViewCalculator viewCalculator, EntityFacade entityFacade) {
        try {
            return viewCalculator.getDescriptionTextOrNid(entityFacade);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return DEFAULT_DESCRIPTION_STRING;
        }
    }
}
