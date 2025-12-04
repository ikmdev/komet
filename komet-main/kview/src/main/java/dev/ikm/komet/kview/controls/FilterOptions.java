package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ObservableViewWithOverride;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.set.ImmutableSet;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;

public class FilterOptions implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    public enum OPTION_ITEM {
        // Main Coordinates
        NAVIGATOR("navigator", ""),
        TYPE("type", ""),
        HEADER("header", ""),
        STATUS("status", "Status"),
        TIME("time", ""),
        MODULE("module", "Module"),
        PATH("path", "Path"),
        KIND_OF("kindof", ""),
        MEMBERSHIP("membership", ""),
        SORT_BY("sortby", ""),
        // Language Coordinates
        LANGUAGE("language", "Model concept, Tinkar Model concept, Language"),
        DIALECT("dialect", ""),
        PATTERN("pattern", ""),
        DESCRIPTION_TYPE("description", "");

        private final String name;
        private final String path;

        OPTION_ITEM(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }

    public static final class Option<T> implements Serializable {

        @Serial
        private static final long serialVersionUID = 0L;

        public enum BUTTON {
            NONE(""),
            ALL(".button.all"),
            ANY(".button.any"),
            EXCLUDING(".button.excluding");

            private final String label;

            BUTTON(String label) {
                this.label = label;
            }

            public String getLabel() {
                return label;
            }
        }

        private final OPTION_ITEM item;
        private final String title;
        private final List<T> availableOptions;
        private final ObservableList<T> selectedOptions;
        private final ObservableList<T> excludedOptions;
        private final boolean multiSelect;
        private boolean any;
        private final EnumSet<BUTTON> buttonType;
        private boolean inOverride;

        public Option(OPTION_ITEM item, String title,
                      List<T> availableOptions, ObservableList<T> selectedOptions, ObservableList<T> excludedOptions,
                      boolean multiSelect, boolean any, EnumSet<BUTTON> buttonType, boolean inOverride) {
            this.item = item;
            this.title = title;
            this.availableOptions = availableOptions;
            this.selectedOptions = selectedOptions;
            this.excludedOptions = excludedOptions;
            this.multiSelect = multiSelect;
            this.any = any;
            this.buttonType = buttonType;
            this.inOverride = inOverride;
        }

        public String title() {
            return resources.getString(title);
        }

        public boolean isMultiSelectionAllowed() {
            return multiSelect;
        }

        public boolean hasAll() {
            return buttonType.contains(BUTTON.ALL);
        }

        public boolean hasAny() {
            return buttonType.contains(BUTTON.ANY);
        }

        public boolean hasExcluding() {
            return buttonType.contains(BUTTON.EXCLUDING);
        }

        public boolean areAllSelected() {
            return selectedOptions.size() == availableOptions.size();
        }

        public boolean hasExclusions() {
            return hasExcluding() && excludedOptions() != null && !excludedOptions().isEmpty();
        }

        public boolean isInOverride() {
            return inOverride;
        }

        public void setInOverride(boolean override) {
            this.inOverride = override;
        }

        public OPTION_ITEM item() {
            return item;
        }

        public List<T> availableOptions() {
            return availableOptions;
        }

        public ObservableList<T> selectedOptions() {
            return selectedOptions;
        }

        public ObservableList<T> excludedOptions() {
            return excludedOptions;
        }

        public boolean any() {
            return any;
        }

        public void setAny(boolean any) {
            this.any = any;
        }

        public EnumSet<BUTTON> buttonType() {
            return buttonType;
        }

        public Option<T> copy() {
            return new Option<>(item, title,
                    new ArrayList<>(availableOptions.stream().toList()),
                    FXCollections.observableArrayList(selectedOptions.stream().toList()),
                    excludedOptions != null ? FXCollections.observableArrayList(excludedOptions.stream().toList()) : null,
                    multiSelect, any, buttonType, inOverride);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option<?> option = (Option<?>) o;
            if (!Objects.equals(item, option.item)) return false;
            if (!Objects.equals(title, option.title)) return false;
            if (any != option.any) return false;
            if (inOverride != option.inOverride) return false;
            if (option.item == OPTION_ITEM.DIALECT || option.item == OPTION_ITEM.DESCRIPTION_TYPE) {
                if (!compareSortedLists(selectedOptions, option.selectedOptions)) return false;
                if (!compareSortedLists(excludedOptions, option.excludedOptions)) return false;
            } else {
                if (!compareLists(selectedOptions, option.selectedOptions)) return false;
                if (!compareLists(excludedOptions, option.excludedOptions)) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, any, selectedOptions, excludedOptions, inOverride);
        }

        @Override
        public String toString() {
            return "> " + item + ": " + (any ? "Any of: " : (multiSelect ? "All of: " : "")) + selectedOptions.stream().map(s -> s instanceof EntityFacade ef ? ef.description() : s).toList() +
                    (excludedOptions == null || excludedOptions.isEmpty() ? "" : " - " + excludedOptions.stream().map(s -> s instanceof EntityFacade ef ? ef.description() : s).toList()) +
                    " from av: " + availableOptions.stream().map(s -> s instanceof EntityFacade ef ? ef.description() : s).toList() + (inOverride ? " *OV*" : "");
        }

    }

    private final EnumSet<Option.BUTTON> noneSet = EnumSet.of(Option.BUTTON.NONE);
    private final EnumSet<Option.BUTTON> allSet = EnumSet.of(Option.BUTTON.ALL);
    private final EnumSet<Option.BUTTON> allExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.EXCLUDING);
    private final EnumSet<Option.BUTTON> allAnyExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.ANY, Option.BUTTON.EXCLUDING);

    public interface FilterCoordinates {}

    public class MainFilterCoordinates implements FilterCoordinates {

        // MainCoordinates

        private Option<PatternFacade> navigator;

        {
            List<PatternFacade> navigatorOptions = FxGet.navigationOptions().stream().map(l -> l.get(0)).toList();
            navigator = new Option<>(OPTION_ITEM.NAVIGATOR, "navigator.title",
                    new ArrayList<>(navigatorOptions), FXCollections.observableArrayList(navigatorOptions.getFirst()), null, false, false, noneSet, false);
        }

        private Option<String> type;
        {
            List<String> typeOptions = Stream.of(
                            "type.option.concepts", "type.option.semantics")
                    .map(resources::getString)
                    .toList();
            type = new Option<>(OPTION_ITEM.TYPE, "type.title",
                    new ArrayList<>(typeOptions), FXCollections.observableArrayList(typeOptions), null, true, false, allSet, false);
        }

        private Option<String> header = new Option<>(OPTION_ITEM.HEADER, "header.title",
                new ArrayList<>(), FXCollections.observableArrayList(), null, false, false, allSet, false);

        private Option<State> status;
        {
            List<State> ALL_STATES = StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN.toEnumSet().stream().toList();

            status = new Option<>(OPTION_ITEM.STATUS, "status.title",
                    new ArrayList<>(ALL_STATES), FXCollections.observableArrayList(ALL_STATES), null, true, false, allSet, false);
        }

        private Option<String> time;
        {
            List<String> dateOptions = Stream.of("time.item1", "time.item2", "time.item3") // , "time.item4" Range not supported yet
                    .map(resources::getString)
                    .toList();
            time = new Option<>(OPTION_ITEM.TIME, "time.title",
                    new ArrayList<>(dateOptions), FXCollections.observableArrayList(dateOptions.getFirst()), FXCollections.observableArrayList(), false, false, noneSet, false);
        }

        private Option<ConceptFacade> module;

        {
            List<ConceptFacade> modules = StampService.get().getModulesInUse().stream().toList();
            module = new Option<>(OPTION_ITEM.MODULE, "module.title",
                    new ArrayList<>(modules), FXCollections.observableArrayList(), FXCollections.observableArrayList(), true, false, allAnyExcludingSet, false);
        }

        private Option<ConceptFacade> path = new Option<>(OPTION_ITEM.PATH, "path.title",
                new ArrayList<>(), FXCollections.observableArrayList(), null, false, false, noneSet, false);

        private Option<String> kindOf;
        {
            List<String> kindOfOptions = Stream.of(
                            "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                            "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                            "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                    .map(resources::getString)
                    .toList();
            kindOf = new Option<>(OPTION_ITEM.KIND_OF, "kindof.title",
                    new ArrayList<>(kindOfOptions), FXCollections.observableArrayList(kindOfOptions), FXCollections.observableArrayList(), true, false, allExcludingSet, false);
        }

        private Option<String> membership;
        {
            List<String> membershipOptions = Stream.of(
                            "membership.option.member1", "membership.option.member2", "membership.option.member3",
                            "membership.option.member4", "membership.option.member5")
                    .map(resources::getString)
                    .toList();
            membership = new Option<>(OPTION_ITEM.MEMBERSHIP, "membership.title",
                    new ArrayList<>(membershipOptions), FXCollections.observableArrayList(membershipOptions), null, true, false, allSet, false);
        }

        private Option<String> sortBy;
        {
            List<String> sortByOptions = Stream.of(
                            "sortby.option.relevant", "sortby.option.alphabetical", "sortby.option.groupedby")
                    .map(resources::getString)
                    .toList();
            sortBy = new Option<>(OPTION_ITEM.SORT_BY, "sortby.title",
                    new ArrayList<>(sortByOptions), FXCollections.observableArrayList(List.of(sortByOptions.getFirst())), null, false, false, allSet, false);
        }

        private final List<Option> options;

        MainFilterCoordinates() {
            options = new ArrayList<>(List.of(
                    navigator, type, header, status, time, module, path, kindOf, membership, sortBy
            ));
        }

        public Option<PatternFacade> getNavigator() {
            return navigator;
        }

        public Option<String> getType() {
            return type;
        }

        public Option<String> getHeader() {
            return header;
        }

        public Option<State> getStatus() {
            return status;
        }

        public Option<String> getTime() {
            return time;
        }

        public Option<ConceptFacade> getModule() {
            return module;
        }

        public Option<ConceptFacade> getPath() {
            return path;
        }

        public Option<String> getKindOf() {
            return kindOf;
        }

        public Option<String> getMembership() {
            return membership;
        }

        public Option<String> getSortBy() {
            return sortBy;
        }

        public List<Option> getOptions() {
            return options;
        }

        public MainFilterCoordinates copy() {
            MainFilterCoordinates copy = new MainFilterCoordinates();
            copy.navigator = navigator.copy();
            copy.type = type.copy();
            copy.header = header.copy();
            copy.status = status.copy();
            copy.time = time.copy();
            copy.module = module.copy();
            copy.path = path.copy();
            copy.kindOf = kindOf.copy();
            copy.membership = membership.copy();
            copy.sortBy = sortBy.copy();
            copy.options.clear();
            copy.options.addAll(List.of(copy.navigator, copy.type,
                    copy.header, copy.status,
                    copy.time, copy.module,
                    copy.path, copy.kindOf,
                    copy.membership, copy.sortBy));
            return copy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MainFilterCoordinates that = (MainFilterCoordinates) o;
            return Objects.equals(navigator, that.navigator) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(header, that.header) &&
                    Objects.equals(status, that.status) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(module, that.module) &&
                    Objects.equals(path, that.path) &&
                    Objects.equals(kindOf, that.kindOf) &&
                    Objects.equals(membership, that.membership) &&
                    Objects.equals(sortBy, that.sortBy) &&
                    Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    navigator, type, header, status, time, module, path, kindOf, membership, sortBy,
                    options);
        }

        @Override
        public String toString() {
            return "MainOptions{\n" +
                    options.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n")) +
                    "\n}";
        }
    }

    public class LanguageFilterCoordinates implements FilterCoordinates, Comparable {

        // Language Coordinates
        private Option<EntityFacade> language;
        {
            List<EntityFacade> languageOptions = FxGet.allowedLanguages().stream().map(lang -> (EntityFacade) lang).toList();
            language = new Option<>(OPTION_ITEM.LANGUAGE, "language.option.title",
                    new ArrayList<>(languageOptions), FXCollections.observableArrayList(List.of(languageOptions.getFirst())), FXCollections.observableArrayList(), false, false, noneSet, false);
        }

        private Option<EntityFacade> dialect;
        {
            List<EntityProxy> dialectPattern = List.of(TinkarTerm.US_DIALECT_PATTERN, TinkarTerm.GB_DIALECT_PATTERN);
            dialect = new Option<>(OPTION_ITEM.DIALECT, "dialect.option.title",
                    new ArrayList<>(dialectPattern), FXCollections.observableArrayList(dialectPattern), null, true, false, noneSet, false);
        }

        private Option<EntityFacade> pattern;
        {
            EntityFacade patternOption = TinkarTerm.DESCRIPTION_PATTERN;
            pattern = new Option<>(OPTION_ITEM.PATTERN, "pattern.option.title",
                    new ArrayList<>(List.of(patternOption)), FXCollections.observableArrayList(List.of(patternOption)), null, false, false, noneSet, false);
        }

        private Option<EntityFacade> descriptionType;
        {
            List<EntityFacade> descriptionTypeOptions = List.of(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
            descriptionType = new Option<>(OPTION_ITEM.DESCRIPTION_TYPE, "description.option.title",
                    new ArrayList<>(descriptionTypeOptions), FXCollections.observableArrayList(descriptionTypeOptions), null, true, false, noneSet, false);
        }

        private final List<Option<EntityFacade>> options;
        private final int ordinal;

        LanguageFilterCoordinates(int ordinal) {
            this.ordinal = ordinal;
            options = new ArrayList<>(List.of(
                    language, dialect, pattern, descriptionType
            ));
        }

        public Option<EntityFacade> getLanguage() {
            return language;
        }

        public Option<EntityFacade> getDialect() {
            return dialect;
        }

        public Option<EntityFacade> getPattern() {
            return pattern;
        }

        public Option<EntityFacade> getDescriptionType() {
            return descriptionType;
        }

        public List<Option<EntityFacade>> getOptions() {
            return options;
        }

        public int getOrdinal() {
            return ordinal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LanguageFilterCoordinates that = (LanguageFilterCoordinates) o;
            if (ordinal != that.ordinal) {
                return false;
            }
            if (!Objects.equals(language, that.language)) {
                return false;
            }
            if (!Objects.equals(dialect, that.dialect)) {
                return false;
            }
            if (!Objects.equals(pattern, that.pattern)) {
                return false;
            }
            if (!Objects.equals(descriptionType, that.descriptionType)) {
                return false;
            }
            if (!Objects.equals(options, that.options)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    language, dialect, pattern, descriptionType, ordinal,
                    options);
        }

        @Override
        public String toString() {
            return "LangOptions[" + ordinal + "] {\n" +
                    options.stream()
                            .map(option -> "> " + option.toString())
                            .collect(Collectors.joining("\n")) +
                    "\n}";
        }

        public LanguageFilterCoordinates copy() {
            LanguageFilterCoordinates languageCoordinates = new LanguageFilterCoordinates(ordinal);
            languageCoordinates.language = language.copy();
            languageCoordinates.dialect = dialect.copy();
            languageCoordinates.pattern = pattern.copy();
            languageCoordinates.descriptionType = descriptionType.copy();
            languageCoordinates.options.clear();
            languageCoordinates.options.addAll(List.of(
                    languageCoordinates.language, languageCoordinates.dialect,
                    languageCoordinates.pattern, languageCoordinates.descriptionType
            ));
            return languageCoordinates;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof LanguageFilterCoordinates lang) {
                return this.ordinal - lang.ordinal;
            }
            return 1;
        }
    }

    private MainFilterCoordinates mainCoordinates;
    private List<LanguageFilterCoordinates> languageCoordinatesList;

    private final ObservableView observableViewForFilter;

    public FilterOptions() {
        mainCoordinates = new MainFilterCoordinates();

        languageCoordinatesList = new ArrayList<>(); // at least one
        languageCoordinatesList.add(new LanguageFilterCoordinates(0));

        observableViewForFilter = new ObservableViewNoOverride(Coordinates.View.DefaultView())
                .makeOverridableViewProperties().nodeView();
    }

    public ObservableView observableViewForFilterProperty() {
        return observableViewForFilter;
    }

    public MainFilterCoordinates getMainCoordinates() {
        return mainCoordinates;
    }

    public List<LanguageFilterCoordinates> getLanguageCoordinatesList() {
        return languageCoordinatesList;
    }

    public LanguageFilterCoordinates getLanguageCoordinates(int ordinal) {
        if (ordinal < 0 || ordinal >= languageCoordinatesList.size()) {
            Thread.dumpStack();
        }
        return getLanguageCoordinatesList().get(ordinal);
    }

    public LanguageFilterCoordinates addLanguageCoordinates() {
        LanguageFilterCoordinates languageCoordinates = new LanguageFilterCoordinates(languageCoordinatesList.size());
        languageCoordinatesList.add(languageCoordinates);
        return languageCoordinates;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return mainCoordinates.options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public Option<EntityFacade> getLangOptionForItem(int ordinal, OPTION_ITEM item) {
        return languageCoordinatesList.get(ordinal).options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case NAVIGATOR -> mainCoordinates.navigator = (Option<PatternFacade>) option;
            case TYPE -> mainCoordinates.type = (Option<String>) option;
            case HEADER -> mainCoordinates.header = (Option<String>) option;
            case STATUS -> mainCoordinates.status = (Option<State>) option;
            case TIME -> mainCoordinates.time = (Option<String>) option;
            case MODULE -> mainCoordinates.module = (Option<ConceptFacade>) option;
            case PATH -> mainCoordinates.path = (Option<ConceptFacade>) option;
            case KIND_OF -> mainCoordinates.kindOf = (Option<String>) option;
            case MEMBERSHIP -> mainCoordinates.membership = (Option<String>) option;
            case SORT_BY -> mainCoordinates.sortBy = (Option<String>) option;
        }
        mainCoordinates.options.clear();
        mainCoordinates.options.addAll(List.of(mainCoordinates.navigator, mainCoordinates.type,
                mainCoordinates.header, mainCoordinates.status,
                mainCoordinates.time, mainCoordinates.module,
                mainCoordinates.path, mainCoordinates.kindOf,
                mainCoordinates.membership, mainCoordinates.sortBy));
    }

    public void setLangCoordinates(int ordinal, LanguageFilterCoordinates value) {
        LanguageFilterCoordinates languageCoordinates = languageCoordinatesList.get(ordinal);
        languageCoordinates.language = value.language;
        languageCoordinates.dialect = value.dialect;
        languageCoordinates.pattern = value.pattern;
        languageCoordinates.descriptionType = value.descriptionType;
        languageCoordinates.options.clear();
        languageCoordinates.options.addAll(List.of(languageCoordinates.language, languageCoordinates.dialect,
                        languageCoordinates.pattern, languageCoordinates.descriptionType));
    }

    public FilterOptions copy() {
        FilterOptions copy = new FilterOptions();
        copy.mainCoordinates = mainCoordinates.copy();
        copy.languageCoordinatesList = new ArrayList<>(languageCoordinatesList.stream()
                .map(LanguageFilterCoordinates::copy)
                .toList());
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterOptions that = (FilterOptions) o;
        if (!Objects.equals(mainCoordinates, that.mainCoordinates)) {
            return false;
        }
        if (!compareLists(languageCoordinatesList, that.languageCoordinatesList)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainCoordinates, languageCoordinatesList);
    }

    @Override
    public String toString() {
        return "FilterOptions{ " +
                mainCoordinates + "\n" + languageCoordinatesList.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",\n")) +
                "}";
    }

    static boolean compareLists(List<?> list1, List<?> list2) {
        if (list1 == null && list2 != null) return false;
        if (list1 != null && list2 == null) return false;
        if (list1 != null && list1.size() != list2.size()) return false;
        return list1 == null ||
                list1.stream().sorted().toList().equals(list2.stream().sorted().toList());
    }

    static boolean compareSortedLists(List<?> list1, List<?> list2) {
        if (list1 == null && list2 != null) return false;
        if (list1 != null && list2 == null) return false;
        if (list1 != null && list1.size() != list2.size()) return false;
        return list1 == null ||
                list1.stream().toList().equals(list2.stream().toList());
    }

}