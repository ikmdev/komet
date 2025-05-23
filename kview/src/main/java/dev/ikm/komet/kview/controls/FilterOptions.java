package dev.ikm.komet.kview.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
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
        DESCRIPTION_TYPE("");

        private final String path;

        OPTION_ITEM(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public record Option(OPTION_ITEM item, String title, String defaultOption, ObservableList<String> availableOptions,
                         ObservableList<String> selectedOptions) {

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
            return item + ": " + selectedOptions;
        }

        public boolean isMultiSelectionAllowed() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            return Objects.equals(title, option.title) &&
                    item == option.item &&
                    Objects.equals(selectedOptions, option.selectedOptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, selectedOptions);
        }
    }

    private final Option sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", "sortby.option.all",
            FXCollections.observableArrayList(),
            FXCollections.observableArrayList());

    private final Option status = new Option(OPTION_ITEM.STATUS, "status.title", "status.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList());

    private final Option module = new Option(OPTION_ITEM.MODULE, "module.title", "module.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList());

    private final Option path = new Option(OPTION_ITEM.PATH, "path.title", "path.option.all",
            FXCollections.observableArrayList(),
            FXCollections.observableArrayList());

    private final Option language = new Option(OPTION_ITEM.LANGUAGE, "language.title", "language.option.all",
                FXCollections.observableArrayList(),
                FXCollections.observableArrayList());

    private final Option descriptionType;
    {
        List<String> descriptionTypeOptions = Stream.of(
                "description.option.fqn", "description.option.preferred", "description.option.regular",
                "description.option.preferredfqn", "description.option.regularfqn")
                .map(resources::getString)
                .toList();
        descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.title", "description.option.all",
                FXCollections.observableArrayList(descriptionTypeOptions),
                FXCollections.observableArrayList());
    }

    private final List<Option> options;

    public FilterOptions() {
        options = List.of(sortBy, status, module, path, language, descriptionType);
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

    public List<Option> getOptions() {
        return options;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public static FilterOptions defaultOptions() {
        FilterOptions filterOptions = new FilterOptions();
        filterOptions.getOptions().forEach(o -> o.selectedOptions().setAll(o.defaultOption()));
        return filterOptions;
    }

    public static List<String> fromString(String options) {
        if (options == null) {
            return List.of();
        }
        return Arrays.asList(options.split(", "));
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
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortBy, status, path, language, descriptionType, options);
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