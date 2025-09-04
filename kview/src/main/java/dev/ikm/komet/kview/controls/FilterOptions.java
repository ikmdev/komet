package dev.ikm.komet.kview.controls;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final class Option implements Serializable {
        @Serial
        private static final long serialVersionUID = 0L;

        private final OPTION_ITEM item;
        private final String title;
        private final List<String> defaultOptions;
        private final List<String> availableOptions;
        private final List<String> selectedOptions;
        private final List<String> excludedOptions;
        private final boolean multiSelect;
        private boolean any;
        private final EnumSet<BUTTON> buttonType;
        private boolean inOverride;

        public Option(OPTION_ITEM item, String title, List<String> defaultOptions, List<String> availableOptions,
                      List<String> selectedOptions, List<String> excludedOptions, boolean multiSelect, boolean any, EnumSet<BUTTON> buttonType, boolean inOverride) {
            this.item = item;
            this.title = title;
            this.defaultOptions = defaultOptions;
            this.availableOptions = availableOptions;
            this.selectedOptions = selectedOptions;
            this.excludedOptions = excludedOptions;
            this.multiSelect = multiSelect;
            this.any = any;
            this.buttonType = buttonType;
            this.inOverride = inOverride;
        }

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

        public String title() {
            return resources.getString(title);
        }

        public List<String> defaultOptions() {
            return defaultOptions;
        }

        @Override
        public String toString() {
            return "> " + item + ": " + (any ? "Any of: " : (multiSelect ? "All of: " : "")) + selectedOptions +
                    (excludedOptions == null || excludedOptions.isEmpty() ? "" : " - " + excludedOptions) +
                    " from av: " + availableOptions + " def: " + defaultOptions + (inOverride ? " *OV*" : "");
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
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

        public Option copy() {
            return new Option(item, title,
                    new ArrayList<>(defaultOptions.stream().toList()),
                    new ArrayList<>(availableOptions.stream().toList()),
                    new ArrayList<>(selectedOptions.stream().toList()),
                    excludedOptions != null ? new ArrayList<>(excludedOptions.stream().toList()) : null,
                    multiSelect, any, buttonType, inOverride);
        }

        public OPTION_ITEM item() {
            return item;
        }

        public List<String> availableOptions() {
            return availableOptions;
        }

        public List<String> selectedOptions() {
            return selectedOptions;
        }

        public List<String> excludedOptions() {
            return excludedOptions;
        }

        public boolean multiSelect() {
            return multiSelect;
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
    }

    private final EnumSet<Option.BUTTON> noneSet = EnumSet.of(Option.BUTTON.NONE);
    private final EnumSet<Option.BUTTON> allSet = EnumSet.of(Option.BUTTON.ALL);
    private final EnumSet<Option.BUTTON> allExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.EXCLUDING);
    private final EnumSet<Option.BUTTON> allAnyExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.ANY, Option.BUTTON.EXCLUDING);

    public interface Coordinates {}

    public class MainCoordinates implements Coordinates {

        // MainCoordinates

        private Option navigator;

        {
            List<String> navigatorOptions = Stream.of(
                            "navigator.option.stated", "navigator.option.inferred")
                    .map(resources::getString)
                    .toList();
            navigator = new Option(OPTION_ITEM.NAVIGATOR, "navigator.title", new ArrayList<>(List.of(navigatorOptions.getFirst())),
                    new ArrayList<>(navigatorOptions), new ArrayList<>(List.of(navigatorOptions.getFirst())), null, false, false, noneSet, false);
        }

        private Option type;
        {
            List<String> typeOptions = Stream.of(
                            "type.option.concepts", "type.option.semantics")
                    .map(resources::getString)
                    .toList();
            type = new Option(OPTION_ITEM.TYPE, "type.title", new ArrayList<>(typeOptions),
                    new ArrayList<>(typeOptions), new ArrayList<>(typeOptions), null, true, false, allSet, false);
        }

        private Option header = new Option(OPTION_ITEM.HEADER, "header.title", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), null, false, false, allSet, false);

        private Option status = new Option(OPTION_ITEM.STATUS, "status.title", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), null, true, false, allSet, false);

        private Option time;
        {
            List<String> dateOptions = Stream.of("time.item1", "time.item2", "time.item3")
                    .map(resources::getString)
                    .toList();
            time = new Option(OPTION_ITEM.TIME, "time.title", new ArrayList<>(List.of(dateOptions.getFirst())),
                    new ArrayList<>(dateOptions), new ArrayList<>(List.of(dateOptions.getFirst())), new ArrayList<>(), false, false, noneSet, false);
        }

        private Option module = new Option(OPTION_ITEM.MODULE, "module.title", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true, false, allAnyExcludingSet, false);

        private Option path = new Option(OPTION_ITEM.PATH, "path.title", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), null, false, false, noneSet, false);

        private Option kindOf;
        {
            List<String> kindOfOptions = Stream.of(
                            "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                            "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                            "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                    .map(resources::getString)
                    .toList();
            kindOf = new Option(OPTION_ITEM.KIND_OF, "kindof.title", new ArrayList<>(kindOfOptions),
                    new ArrayList<>(kindOfOptions), new ArrayList<>(kindOfOptions), new ArrayList<>(), true, false, allExcludingSet, false);
        }

        private Option membership;
        {
            List<String> membershipOptions = Stream.of(
                            "membership.option.member1", "membership.option.member2", "membership.option.member3",
                            "membership.option.member4", "membership.option.member5")
                    .map(resources::getString)
                    .toList();
            membership = new Option(OPTION_ITEM.MEMBERSHIP, "membership.title", new ArrayList<>(membershipOptions),
                    new ArrayList<>(membershipOptions), new ArrayList<>(membershipOptions), null, true, false, allSet, false);
        }

        private Option sortBy;
        {
            List<String> sortByOptions = Stream.of(
                            "sortby.option.relevant", "sortby.option.alphabetical", "sortby.option.groupedby")
                    .map(resources::getString)
                    .toList();
            sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", new ArrayList<>(List.of(sortByOptions.getFirst())),
                    new ArrayList<>(sortByOptions), new ArrayList<>(List.of(sortByOptions.getFirst())), null, false, false, allSet, false);
        }

        private final List<Option> options;

        MainCoordinates() {
            options = new ArrayList<>(List.of(
                    navigator, type, header, status, time, module, path, kindOf, membership, sortBy
            ));
        }

        public Option getNavigator() {
            return navigator;
        }

        public Option getType() {
            return type;
        }

        public Option getHeader() {
            return header;
        }

        public Option getStatus() {
            return status;
        }

        public Option getTime() {
            return time;
        }

        public Option getModule() {
            return module;
        }

        public Option getPath() {
            return path;
        }

        public Option getKindOf() {
            return kindOf;
        }

        public Option getMembership() {
            return membership;
        }

        public Option getSortBy() {
            return sortBy;
        }

        public List<Option> getOptions() {
            return options;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MainCoordinates that = (MainCoordinates) o;
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

    public class LanguageCoordinates implements Coordinates, Comparable {

        // Language Coordinates
        private Option language = new Option(OPTION_ITEM.LANGUAGE, "language.option.title", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false, false, noneSet, false);

        private Option dialect;
        {
            List<String> dialectOptions = Stream.of(
                            "dialect.option.item1", "dialect.option.item2",
                            "dialect.option.item3", "dialect.option.item4", "dialect.option.item5")
                    .map(resources::getString)
                    .toList();
            dialect = new Option(OPTION_ITEM.DIALECT, "dialect.option.title", new ArrayList<>(dialectOptions),
                    new ArrayList<>(dialectOptions), new ArrayList<>(dialectOptions), null, true, false, noneSet, false);
        }

        private Option pattern;
        {
            List<String> patternOptions = Stream.of(
                            "pattern.option.item1", "pattern.option.item2",
                            "pattern.option.item3", "pattern.option.item4")
                    .map(resources::getString)
                    .toList();
            pattern = new Option(OPTION_ITEM.PATTERN, "pattern.option.title", new ArrayList<>(List.of(patternOptions.getFirst())),
                    new ArrayList<>(patternOptions), new ArrayList<>(List.of(patternOptions.getFirst())), null, false, false, noneSet, false);
        }

        private Option descriptionType;
        {
            List<String> descriptionTypeOptions = Stream.of(
                            //FIXME, the parent/classic menu uses FxGET::allowedDescriptionTypeOrder and hard codes FQN
                            // and Regular Name as options... as new designs for Language+Dialect get implemented
                            // we will additionally need to address this setting as well
                            "description.option.fqn", "description.option.preferred", "description.option.regular",
                            "description.option.preferredfqn", "description.option.regularfqn")
                    .map(resources::getString)
                    .toList();
            descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.option.title", new ArrayList<>(descriptionTypeOptions),
                    new ArrayList<>(descriptionTypeOptions), new ArrayList<>(descriptionTypeOptions), null, true, false, noneSet, false);
        }

        private final List<Option> options;
        private final int ordinal;

        LanguageCoordinates(int ordinal) {
            this.ordinal = ordinal;
            options = new ArrayList<>(List.of(
                    language, dialect, pattern, descriptionType
            ));
        }

        public Option getLanguage() {
            return language;
        }

        public Option getDialect() {
            return dialect;
        }

        public Option getPattern() {
            return pattern;
        }

        public Option getDescriptionType() {
            return descriptionType;
        }

        public List<Option> getOptions() {
            return options;
        }

        public int getOrdinal() {
            return ordinal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LanguageCoordinates that = (LanguageCoordinates) o;
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

        public LanguageCoordinates copy() {
            LanguageCoordinates languageCoordinates = new LanguageCoordinates(ordinal);
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
            if (o instanceof LanguageCoordinates lang) {
                return this.ordinal - lang.ordinal;
            }
            return 1;
        }
    }

    private final MainCoordinates mainCoordinates;
    private final List<LanguageCoordinates> languageCoordinatesList;

    public FilterOptions() {
        mainCoordinates = new MainCoordinates();

        languageCoordinatesList = new ArrayList<>(); // at least one
        languageCoordinatesList.add(new LanguageCoordinates(0));
    }

    public MainCoordinates getMainCoordinates() {
        return mainCoordinates;
    }

    public List<LanguageCoordinates> getLanguageCoordinatesList() {
        return languageCoordinatesList;
    }

    public LanguageCoordinates getLanguageCoordinates(int ordinal) {
        if (ordinal < 0 || ordinal >= languageCoordinatesList.size()) {
            Thread.dumpStack();
        }
        return getLanguageCoordinatesList().get(ordinal);
    }

    public LanguageCoordinates addLanguageCoordinates() {
        LanguageCoordinates languageCoordinates = new LanguageCoordinates(languageCoordinatesList.size());
        languageCoordinatesList.add(languageCoordinates);
        return languageCoordinates;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return mainCoordinates.options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public Option getLangOptionForItem(int ordinal, OPTION_ITEM item) {
        return languageCoordinatesList.get(ordinal).options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case NAVIGATOR -> mainCoordinates.navigator = option;
            case TYPE -> mainCoordinates.type = option;
            case HEADER -> mainCoordinates.header = option;
            case STATUS -> mainCoordinates.status = option;
            case TIME -> mainCoordinates.time = option;
            case MODULE -> mainCoordinates.module = option;
            case PATH -> mainCoordinates.path = option;
            case KIND_OF -> mainCoordinates.kindOf = option;
            case MEMBERSHIP -> mainCoordinates.membership = option;
            case SORT_BY -> mainCoordinates.sortBy = option;
        }
        mainCoordinates.options.clear();
        mainCoordinates.options.addAll(List.of(mainCoordinates.navigator, mainCoordinates.type,
                mainCoordinates.header, mainCoordinates.status,
                mainCoordinates.time, mainCoordinates.module,
                mainCoordinates.path, mainCoordinates.kindOf,
                mainCoordinates.membership, mainCoordinates.sortBy));
    }

    public void setLangCoordinates(int ordinal, LanguageCoordinates value) {
        LanguageCoordinates languageCoordinates = languageCoordinatesList.get(ordinal);
        languageCoordinates.language = value.language;
        languageCoordinates.dialect = value.dialect;
        languageCoordinates.pattern = value.pattern;
        languageCoordinates.descriptionType = value.descriptionType;
        languageCoordinates.options.clear();
        languageCoordinates.options.addAll(List.of(languageCoordinates.language, languageCoordinates.dialect,
                        languageCoordinates.pattern, languageCoordinates.descriptionType));
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