package dev.ikm.komet.kview.controls;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
        TYPE("type", ""),
        HEADER("header", ""),
        STATUS("status", "Status"),
        MODULE("module", "Module"),
        PATH("path", "Path"),
        LANGUAGE("language", "Model concept, Tinkar Model concept, Language"),
        DESCRIPTION_TYPE("description", ""),
        KIND_OF("kindof", ""),
        MEMBERSHIP("membership", ""),
        SORT_BY("sortby", ""),
        DATE("date", "");

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

        public Option(OPTION_ITEM item, String title, List<String> defaultOptions, List<String> availableOptions,
                      List<String> selectedOptions, List<String> excludedOptions, boolean multiSelect, boolean any, EnumSet<BUTTON> buttonType) {
            this.item = item;
            this.title = title;
            this.defaultOptions = defaultOptions;
            this.availableOptions = availableOptions;
            this.selectedOptions = selectedOptions;
            this.excludedOptions = excludedOptions;
            this.multiSelect = multiSelect;
            this.any = any;
            this.buttonType = buttonType;
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
            return item + ": " + (any ? "Any of: " : "All of: ") + selectedOptions +
                    (excludedOptions == null || excludedOptions.isEmpty() ? "" : " - " + excludedOptions);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            if (!Objects.equals(item, option.item)) return false;
            if (!Objects.equals(title, option.title)) return false;
            if (any != option.any) return false;
            if (!compareLists(selectedOptions, option.selectedOptions)) return false;
            if (!compareLists(excludedOptions, option.excludedOptions)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, selectedOptions, excludedOptions);
        }

        public Option copy() {
            return new Option(item, title,
                    new ArrayList<>(defaultOptions.stream().toList()),
                    new ArrayList<>(availableOptions.stream().toList()),
                    new ArrayList<>(selectedOptions.stream().toList()),
                    excludedOptions != null ? new ArrayList<>(excludedOptions.stream().toList()) : null,
                    multiSelect, any, buttonType);
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

    private Option type;
    {
        List<String> typeOptions = Stream.of(
                "type.option.concepts", "type.option.semantics")
                .map(resources::getString)
                .toList();
        type = new Option(OPTION_ITEM.TYPE, "type.title", new ArrayList<>(),
            typeOptions, new ArrayList<>(), null, true, false, allSet);
    }

    private Option header = new Option(OPTION_ITEM.HEADER, "header.title", new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), null, false, false, allSet);

    private Option status = new Option(OPTION_ITEM.STATUS, "status.title", new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), null, true, false, allSet);

    private Option module = new Option(OPTION_ITEM.MODULE, "module.title", new ArrayList<>(Arrays.asList("Any")),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true, false, allAnyExcludingSet);

    private Option path = new Option(OPTION_ITEM.PATH, "path.title", new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), null, false, false, noneSet);

    private Option language = new Option(OPTION_ITEM.LANGUAGE, "language.title", new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), null, false, false, allSet);

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
        descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.title", new ArrayList<>(Arrays.asList("All")),
                descriptionTypeOptions, new ArrayList<>(), null, true, false, allSet);
    }

    private Option kindOf;
    {
        List<String> kindOfOptions = Stream.of(
                "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                        "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                        "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                .map(resources::getString)
                .toList();
        kindOf = new Option(OPTION_ITEM.KIND_OF, "kindof.title", new ArrayList<>(Arrays.asList("All")),
                kindOfOptions, new ArrayList<>(), new ArrayList<>(), true, false, allExcludingSet);
    }

    private Option membership;
    {
        List<String> membershipOptions = Stream.of(
                "membership.option.member1", "membership.option.member2", "membership.option.member3",
                        "membership.option.member4", "membership.option.member5")
                .map(resources::getString)
                .toList();
        membership = new Option(OPTION_ITEM.MEMBERSHIP, "membership.title", new ArrayList<>(Arrays.asList("All")),
                membershipOptions, new ArrayList<>(), null, true, false, allSet);
    }

    private Option sortBy;
    {
        List<String> typeOptions = Stream.of(
                        "sortby.option.relevant", "sortby.option.alphabetical", "sortby.option.groupedby")
                .map(resources::getString)
                .toList();
        sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", new ArrayList<>(),
                typeOptions, new ArrayList<>(), null, false, false, allSet);
    }

    private Option date;
    {
        List<String> dateOptions = Stream.of("date.item1", "date.item2", "date.item3")
                .map(resources::getString)
                .toList();
        date = new Option(OPTION_ITEM.DATE, "date.title", new ArrayList<>(Arrays.asList("Latest")),
                dateOptions, new ArrayList<>(), new ArrayList<>(), true, false, noneSet);
    }

    private final List<Option> options;

    public FilterOptions() {
        options = new ArrayList<>(List.of(
                type, header, status, module, path, language,
                descriptionType, kindOf, membership, sortBy, date));
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

    public Option getModule() {
        return module;
    }

    public Option getPath() {
        return path;
    }

    public Option getLanguage() {
        return language;
    }

    public Option getDescription() {
        return descriptionType;
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

    public Option getDate() {
        return date;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case TYPE -> type = option;
            case HEADER -> header = option;
            case STATUS -> status = option;
            case MODULE -> module = option;
            case PATH -> path = option;
            case LANGUAGE -> language = option;
            case DESCRIPTION_TYPE -> descriptionType = option;
            case KIND_OF -> kindOf = option;
            case MEMBERSHIP -> membership = option;
            case SORT_BY -> sortBy = option;
            case DATE -> date = option;
        }
        options.clear();
        options.addAll(List.of(
                type, header, status, module, path, language,
                descriptionType, kindOf, membership, sortBy, date));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterOptions that = (FilterOptions) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(header, that.header) &&
                Objects.equals(status, that.status) &&
                Objects.equals(module, that.module) &&
                Objects.equals(path, that.path) &&
                Objects.equals(language, that.language) &&
                Objects.equals(descriptionType, that.descriptionType) &&
                Objects.equals(kindOf, that.kindOf) &&
                Objects.equals(membership, that.membership) &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(date, that.date) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type, header, status, module, path, language,
                descriptionType, kindOf, membership, sortBy, date,
                options);
    }

    @Override
    public String toString() {
        return "FilterOptions{" +
                options.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}";
    }

    static boolean compareLists(List<String> list1, List<String> list2) {
        if (list1 == null && list2 != null) return false;
        if (list1 != null && list2 == null) return false;
        if (list1 != null && list1.size() != list2.size()) return false;
        return list1 == null ||
                list1.stream().sorted().toList().equals(list2.stream().sorted().toList());
    }

}