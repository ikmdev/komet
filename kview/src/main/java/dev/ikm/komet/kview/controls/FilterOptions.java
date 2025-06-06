package dev.ikm.komet.kview.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterOptions {

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

    public record Option(OPTION_ITEM item, String title, String defaultOption, ObservableList<String> availableOptions,
                         ObservableList<String> selectedOptions, ObservableList<String> excludedOptions) {

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
    }

    private final Option sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", "sortby.option.all",
            FXCollections.observableArrayList(),
            FXCollections.observableArrayList(), null);

    private final Option status = new Option(OPTION_ITEM.STATUS, "status.title", "status.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList(), null);

    private final Option module = new Option(OPTION_ITEM.MODULE, "module.title", "module.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList(), null);

    private final Option path = new Option(OPTION_ITEM.PATH, "path.title", "path.option.all",
            FXCollections.observableArrayList(),
            FXCollections.observableArrayList(), null);

    private final Option language = new Option(OPTION_ITEM.LANGUAGE, "language.title", "language.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList(), null);

    private final Option descriptionType;
    {
        List<String> descriptionTypeOptions = Stream.of(
                "description.option.fqn", "description.option.preferred", "description.option.regular",
                "description.option.preferredfqn", "description.option.regularfqn")
                .map(resources::getString)
                .toList();
        descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.title", "description.option.all",
                FXCollections.observableArrayList(descriptionTypeOptions),
                FXCollections.observableArrayList(), null);
    }

    private final Option kindOf;
    {
        List<String> kindOfOptions = Stream.of(
                "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                        "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                        "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                .map(resources::getString)
                .toList();
        kindOf = new Option(OPTION_ITEM.KIND_OF, "kindof.title", "kindof.option.all",
                FXCollections.observableArrayList(kindOfOptions),
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList());
    }

    private final Option membership;
    {
        List<String> membershipOptions = Stream.of(
                "membership.option.member1", "membership.option.member2", "membership.option.member3",
                        "membership.option.member4", "membership.option.member5")
                .map(resources::getString)
                .toList();
        membership = new Option(OPTION_ITEM.MEMBERSHIP, "membership.title", "membership.option.all",
                FXCollections.observableArrayList(membershipOptions),
                FXCollections.observableArrayList(), null);
    }

    private final Option date;
    {
        List<String> dateOptions = Stream.of("date.item1", "date.item2", "date.item3")
                .map(resources::getString)
                .toList();
        date = new Option(OPTION_ITEM.DATE, "date.title", "date.option.all",
                FXCollections.observableArrayList(dateOptions),
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList());
    }

    private final List<Option> options;

    public FilterOptions() {
        options = List.of(sortBy, status, module, path, language,
                descriptionType, kindOf, membership, date);
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