package dev.ikm.komet.layout.controls;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableNavigationCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.*;

public class FilterOptionsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FilterOptionsUtils.class);
    private static final String DEFAULT_DESCRIPTION_STRING = Integer.toString(Integer.MAX_VALUE);

    public FilterOptionsUtils() {}

    private Subscription nodeSubscription;
    private Subscription viewSubscription;

    private boolean fromView;
    private boolean fromFilter;

    // The window's editable override view; the popup commits whole values onto it on Apply (see commitToView).
    private ObservableView committedNodeView;

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
                    fromView = true; // when user clicks on the titled pane, the time (should be from spinner or dropdown)
                    mainCoordinates.getTime().selectedOptions().clear();
                    if (t != null) {
                        Long time = t.longValue();
                        LOG.info("Filter Option date time selected {} - epoch millis: {}", new Date(time), time);
                        mainCoordinates.getTime().selectedOptions().addAll(String.valueOf(time));
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
                        // When the set is empty, it means "all module wildcard", and that implies "Any" is selected
                        // When the set contains all available modules, it means "all individual modules",
                        // and that implies "Select All" is selected.
                        // If it only contains a few of them, "Select All" and "Any" are deselected, and those modules
                        // are selected.
                        mainCoordinates.getModule().setAny(m.isEmpty());
                        mainCoordinates.getModule().selectedOptions().addAll(m.castToSet());
                        observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().setValue(m);
                    } else {
                        observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().set(null);
                    }
                    fromView = false;
                }));
                viewSubscription = viewSubscription.and(observableStampCoordinate.excludedModuleSpecificationsProperty().subscribe(e -> {
                    if (fromFilter) {
                        return;
                    }
                    fromView = true;
                    mainCoordinates.getModule().excludedOptions().clear();
                    if (e != null) {
                        mainCoordinates.getModule().excludedOptions().addAll(e.castToSet());
                        observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().setValue(e);
                    } else {
                        observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().set(null);
                    }
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
                        ImmutableList<PatternFacade> list = observableLanguageCoordinate.dialectPatternPreferenceListProperty().get();
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list.castToList());
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
                    } else {
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().setValue(Lists.immutable.<PatternFacade>empty());
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
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list.castToList());
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
                        languageFilterCoordinates.getDescriptionType().selectedOptions().addAll(list.castToList());
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

    // The popup's edits are committed onto the nodeView only on explicit Apply (see commitToView) — one whole-value
    // setValue per dimension, never the live preview mesh that forced the //Dummy pokes (ike-issues#666, #692, #697).
    // Here we only remember the commit target; the display comes from projectFromView, not a live preview view.
    public void subscribeViewToFilterOptions(FilterOptions filterOptions, ObservableView observableView) {
        // remove previous subscriptions
        unsubscribeNodeFilterOptions();
        this.committedNodeView = observableView;
    }

    /// Commit the popup's current UI selections ({@code current} — the authoritative state rebuilt from the panes)
    /// onto the window's nodeView as whole values: one {@code setValue}/{@code setAll} per facet. Each override
    /// property reverts to its parent automatically when the committed value equals the parent's. Invoked on Apply.
    /// This is the whole-value envelope the hierarchical menu has always used — no live same-value reconciliation,
    /// no {@code //Dummy} sentinels — so it never depends on the framework's change-event edge cases
    /// (ike-issues#666, #692).
    public void commitToView(FilterOptions current) {
        if (current == null || committedNodeView == null) {
            return;
        }
        FilterOptions.MainFilterCoordinates main = current.getMainCoordinates();
        ObservableStampCoordinate stamp = committedNodeView.stampCoordinate();
        fromFilter = true;
        try {
            // STATUS
            stamp.allowedStatesProperty().setValue(StateSet.of(main.getStatus().selectedOptions().stream().toList()));

            // TIME (skip when nothing is chosen)
            ObservableList<String> timeOptions = main.getTime().selectedOptions();
            if (!timeOptions.isEmpty()) {
                long time;
                try {
                    time = Long.parseLong(timeOptions.getFirst());
                } catch (NumberFormatException e) {
                    time = Long.MAX_VALUE;
                }
                stamp.timeProperty().setValue(time);
            }

            // MODULE (whole set; empty == "all modules" wildcard) and EXCLUDED MODULE — whole-value replace, so a
            // deselection actually takes (the old addAll accumulated onto the inherited value).
            Set<ConceptFacade> includedModules = new HashSet<>();
            if (!main.getModule().any()) {
                includedModules.addAll(main.getModule().selectedOptions());
            }
            stamp.moduleSpecificationsProperty().setValue(Sets.immutable.ofAll(includedModules));
            stamp.excludedModuleSpecificationsProperty().setValue(
                    Sets.immutable.ofAll(main.getModule().excludedOptions()));

            // PATH (skip when nothing is chosen)
            ObservableList<ConceptFacade> pathOptions = main.getPath().selectedOptions();
            if (!pathOptions.isEmpty() && pathOptions.getFirst() != null) {
                stamp.pathConceptProperty().setValue(pathOptions.getFirst());
            }

            // NAVIGATION (whole set of selected patterns; skip when nothing is chosen)
            ObservableList<PatternFacade> navOptions = main.getNavigator().selectedOptions();
            if (!navOptions.isEmpty()) {
                committedNodeView.navigationCoordinate().navigationPatternsProperty()
                        .setValue(FXCollections.observableSet(navOptions.toArray(new PatternFacade[0])));
            }

            // LANGUAGE (first coordinate)
            FilterOptions.LanguageFilterCoordinates lang = current.getLanguageCoordinatesList().getFirst();
            ObservableLanguageCoordinate nodeLang = committedNodeView.languageCoordinates().getFirst();
            ObservableList<EntityFacade> langOptions = lang.getLanguage().selectedOptions();
            if (!langOptions.isEmpty() && langOptions.getFirst() != null) {
                nodeLang.languageConceptProperty().setValue((ConceptFacade) langOptions.getFirst());
            }
            nodeLang.dialectPatternPreferenceListProperty().setValue(
                    Lists.immutable.fromStream(
                            lang.getDialect().selectedOptions().stream().map(e -> (PatternFacade) e)));
            // The description-type ORDER is the meaning (the language calculator's preference order), so a reorder
            // commits as a whole-value setValue and propagates (ike-issues#666).
            nodeLang.descriptionTypePreferenceListProperty().setValue(
                    Lists.immutable.fromStream(
                            lang.getDescriptionType().selectedOptions().stream().map(e -> (ConceptFacade) e)));
        } finally {
            fromFilter = false;
        }
    }

    /// Projects the window's nodeView (its resolved coordinate — inherited values plus applied overrides) into the
    /// popup's option model: each facet's {@code selectedOptions} is read fresh from the nodeView, in order. The
    /// available options come from the navigator separately. This is the faithful, stateless read the popup
    /// rebuilds on each show — the nodeView is the single source of truth, so the display cannot drift from it
    /// (ike-issues#681). Inverse of {@link #commitToView}.
    public void projectFromView(FilterOptions filterOptions) {
        projectFromView(filterOptions, committedNodeView);
    }

    /// Projects the given {@code view}'s resolved coordinate into the option model. Called with the window's
    /// nodeView for the displayed values, and with the inherited parent ({@code getParentViewCoordinate()}) for
    /// each pane's baseline — so a pane's override dot appears exactly when its nodeView value differs from the
    /// value it inherits (ike-issues#681).
    public void projectFromView(FilterOptions filterOptions, ObservableView view) {
        if (filterOptions == null || view == null) {
            return;
        }
        FilterOptions.MainFilterCoordinates main = filterOptions.getMainCoordinates();
        ObservableStampCoordinate stamp = view.stampCoordinate();

        // STATUS
        main.getStatus().selectedOptions().setAll(stamp.allowedStatesProperty().get().toEnumSet().stream().toList());

        // TIME
        main.getTime().selectedOptions().setAll(List.of(String.valueOf(stamp.timeProperty().get())));

        // MODULE (empty set == "all modules" wildcard) and EXCLUDED MODULE. "Any module" is the EMPTY wildcard —
        // an empty selection with any=true — NOT every module enumerated. Both the display (nodeView) and the
        // baseline (parent) project through here, so empty == empty and no spurious override dot appears
        // (ike-issues#681).
        ImmutableSet<ConceptFacade> modules = stamp.moduleSpecificationsProperty().get();
        boolean anyModule = modules == null || modules.isEmpty();
        main.getModule().setAny(anyModule);
        main.getModule().selectedOptions().setAll(anyModule ? List.of() : modules.toList());
        ImmutableSet<ConceptFacade> excluded = stamp.excludedModuleSpecificationsProperty().get();
        main.getModule().excludedOptions().setAll(excluded == null ? List.of() : excluded.toList());

        // PATH
        ConceptFacade path = stamp.pathConceptProperty().get();
        main.getPath().selectedOptions().setAll(path == null ? List.of() : List.of(path));

        // NAVIGATION
        Set<PatternFacade> navPatterns = view.navigationCoordinate().navigationPatternsProperty().get();
        main.getNavigator().selectedOptions().setAll(navPatterns == null ? List.of() : navPatterns.stream().toList());

        // LANGUAGE (first coordinate)
        FilterOptions.LanguageFilterCoordinates langFilter = filterOptions.getLanguageCoordinatesList().getFirst();
        ObservableLanguageCoordinate viewLang = view.languageCoordinates().getFirst();
        ConceptFacade langConcept = viewLang.languageConceptProperty().get();
        langFilter.getLanguage().selectedOptions().setAll(langConcept == null ? List.of() : List.of((EntityFacade) langConcept));
        langFilter.getDialect().selectedOptions().setAll(
                viewLang.dialectPatternPreferenceListProperty().get().castToList().stream().map(p -> (EntityFacade) p).toList());
        langFilter.getDescriptionType().selectedOptions().setAll(
                viewLang.descriptionTypePreferenceListProperty().get().castToList().stream().map(c -> (EntityFacade) c).toList());
    }

    /// True when the window's nodeView currently carries any override (vs its inherited parent) — drives the
    /// Revert button, which removes those overrides.
    public boolean viewHasOverrides() {
        return committedNodeView != null && committedNodeView.hasOverrides();
    }

    /// Removes every override on the window's nodeView so it resolves back to its inherited parent (Revert).
    public void revertViewToInherited() {
        if (committedNodeView != null) {
            committedNodeView.removeOverrides();
        }
    }

    public void unsubscribeNodeFilterOptions() {
        if (nodeSubscription != null) {
            nodeSubscription.unsubscribe();
        }
        nodeSubscription = Subscription.EMPTY;
    }

    public static List<ZonedDateTime> getTimesInUse() {
        SortedSet<ZonedDateTime> sortedSet = new TreeSet<>(Comparator.reverseOrder());
        PrimitiveData.get().forEachStampNid(nid -> {
            EntityHandle handle = EntityHandle.get(nid);
            if (handle.isAbsent()) {
                return;
            }
            long time = handle.expectStamp().time();
            if (time != PREMUNDANE_TIME) {
                sortedSet.add(Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault()));
            }
        });
        return sortedSet.stream().toList();
    }

    private static int findNidForDescription(FilterOptionsNavigator navigator, int nid, String description) {
        return navigator.getChildEdges(nid).stream()
                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
                .findFirst()
                .map(Edge::destinationNid)
                .orElseThrow();
    }

    public static List<EntityFacade> getDescendentsList(FilterOptionsNavigator navigator, int parentNid, String description) {
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
