package dev.ikm.komet.kview.controls;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
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
        SORT_BY(""),
        STATUS("Status"),
        MODULE("Module"),
        PATH("Path"),
        LANGUAGE("Model concept, Tinkar Model concept, Language"),
        DESCRIPTION_TYPE(""),
        KIND_OF(""),
        MEMBERSHIP(""),
        DATE("");

        private final String path;

        OPTION_ITEM(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public record Option(OPTION_ITEM item, String title, String defaultOption, List<String> availableOptions,
                         List<String> selectedOptions, List<String> excludedOptions) implements Serializable {

        @Override
        public String title() {
            return resources.getString(title);
        }

        @Override
        public String defaultOption() {
            return resources.getString(defaultOption);
        }

        @Override
        public String toString() {
            return item + ": " + selectedOptions +
                    (excludedOptions == null || excludedOptions.isEmpty() ? "" : " - " + excludedOptions);
        }

        public boolean isMultiSelectionAllowed() {
            return true;
        }

        public boolean isExcluding() {
            return excludedOptions != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            if (!Objects.equals(item, option.item)) return false;
            if (!Objects.equals(title, option.title)) return false;
            if (selectedOptions == null && option.selectedOptions != null) return false;
            if (selectedOptions != null && option.selectedOptions == null) return false;
            if (selectedOptions != null &&
                    selectedOptions.size() != option.selectedOptions.size()) return false;
            if (selectedOptions != null &&
                    !selectedOptions.stream().sorted().toList().equals(
                            option.selectedOptions.stream().sorted().toList())) return false;
            if (excludedOptions == null && option.excludedOptions != null) return false;
            if (excludedOptions != null && option.excludedOptions == null) return false;
            if (excludedOptions != null &&
                    excludedOptions.size() != option.excludedOptions.size()) return false;
            return excludedOptions == null ||
                    excludedOptions.stream().sorted().toList().equals(
                            option.excludedOptions.stream().sorted().toList());
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, selectedOptions, excludedOptions);
        }

        public Option copy() {
            return new Option(item, title, defaultOption,
                    new ArrayList<>(availableOptions.stream().toList()),
                    new ArrayList<>(selectedOptions.stream().toList()),
                    excludedOptions != null ? new ArrayList<>(excludedOptions.stream().toList()) : null);
        }
    }

    private Option sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", "sortby.option.all",
            new ArrayList<>(), new ArrayList<>(), null);

    private Option status = new Option(OPTION_ITEM.STATUS, "status.title", "status.option.all",
            new ArrayList<>(), new ArrayList<>(), null);

    private Option module = new Option(OPTION_ITEM.MODULE, "module.title", "module.option.all",
            new ArrayList<>(), new ArrayList<>(), null);

    private Option path = new Option(OPTION_ITEM.PATH, "path.title", "path.option.all",
            new ArrayList<>(), new ArrayList<>(), null);

    private Option language = new Option(OPTION_ITEM.LANGUAGE, "language.title", "language.option.all",
            new ArrayList<>(), new ArrayList<>(), null);

    private Option descriptionType;
    {
        List<String> descriptionTypeOptions = Stream.of(
                "description.option.fqn", "description.option.preferred", "description.option.regular",
                "description.option.preferredfqn", "description.option.regularfqn")
                .map(resources::getString)
                .toList();
        descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.title", "description.option.all",
                descriptionTypeOptions, new ArrayList<>(), null);
    }

    private Option kindOf;
    {
        List<String> kindOfOptions = Stream.of(
                "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                        "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                        "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                .map(resources::getString)
                .toList();
        kindOf = new Option(OPTION_ITEM.KIND_OF, "kindof.title", "kindof.option.all",
                kindOfOptions, new ArrayList<>(), new ArrayList<>());
    }

    private Option membership;
    {
        List<String> membershipOptions = Stream.of(
                "membership.option.member1", "membership.option.member2", "membership.option.member3",
                        "membership.option.member4", "membership.option.member5")
                .map(resources::getString)
                .toList();
        membership = new Option(OPTION_ITEM.MEMBERSHIP, "membership.title", "membership.option.all",
                membershipOptions, new ArrayList<>(), null);
    }

    private Option date;
    {
        List<String> dateOptions = Stream.of("date.item1", "date.item2", "date.item3")
                .map(resources::getString)
                .toList();
        date = new Option(OPTION_ITEM.DATE, "date.title", "date.option.all",
                dateOptions, new ArrayList<>(), new ArrayList<>());
    }

    private final List<Option> options;

    public FilterOptions() {
        options = new ArrayList<>(List.of(sortBy, status, module, path, language, descriptionType, kindOf, membership, date));
    }

    public Option getSortBy() {
        return sortBy;
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
            case SORT_BY -> sortBy = option;
            case STATUS -> status = option;
            case MODULE -> module = option;
            case PATH -> path = option;
            case LANGUAGE -> language = option;
            case DESCRIPTION_TYPE -> descriptionType = option;
            case KIND_OF -> kindOf = option;
            case MEMBERSHIP -> membership = option;
            case DATE -> date = option;
        }
        options.clear();
        options.addAll(List.of(sortBy, status, module, path, language, descriptionType, kindOf, membership, date));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterOptions that = (FilterOptions) o;
        return Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(status, that.status) &&
                Objects.equals(path, that.path) &&
                Objects.equals(language, that.language) &&
                Objects.equals(descriptionType, that.descriptionType) &&
                Objects.equals(kindOf, that.kindOf) &&
                Objects.equals(membership, that.membership) &&
                Objects.equals(date, that.date) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortBy, status, path, language, descriptionType, kindOf, membership, date, options);
    }

    @Override
    public String toString() {
        return "FilterOptions{" +
                options.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}";
    }
}