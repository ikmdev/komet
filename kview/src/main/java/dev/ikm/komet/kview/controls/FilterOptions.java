package dev.ikm.komet.kview.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FilterOptions {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    public enum OPTION_ITEM {
        STATUS,
        PATH,
        LANGUAGE,
        DESCRIPTION_TYPE
    }

    public record Option(OPTION_ITEM item, String title, String defaultOption, List<String> availableOptions, ObservableList<String> selectedOptions) {

        @Override
        public String title() {
            return resources.getString(title);
        }

        @Override
        public String defaultOption() {
            return resources.getString(defaultOption);
        }

        @Override
        public List<String> availableOptions() {
            return availableOptions.stream().map(resources::getString).toList();
        }

        @Override
        public ObservableList<String> selectedOptions() {
            return selectedOptions;
        }

        @Override
        public String toString() {
            return item + ": " + selectedOptions;
        }

        public boolean isMultiSelectionAllowed() {
            return true;
        }
    }

    private final Option status = new Option(OPTION_ITEM.STATUS, "status.title", "status.option.all",
            List.of("status.option.active", "status.option.inactive", "status.option.withdrawn"),
                    FXCollections.observableArrayList());

    private final Option path = new Option(OPTION_ITEM.PATH, "path.title", "path.option.all",
            List.of("path.option.development", "path.option.master", "path.option.primordial", "path.option.sandbox"),
            FXCollections.observableArrayList());

    private final Option language = new Option(OPTION_ITEM.LANGUAGE, "language.title", "language.option.all",
           List.of("language.option.english", "language.option.spanish", "language.option.italian", "language.option.arabic"),
           FXCollections.observableArrayList());

    private final Option descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.title", "description.option.all",
            List.of("description.option.fqn", "description.option.preferred", "description.option.regular", "description.option.preferredfqn", "description.option.regularfqn"),
            FXCollections.observableArrayList());

    private final List<Option> options;

    public FilterOptions() {
        options = List.of(status, path, language, descriptionType);
    }

    public Option getStatus() {
        return status;
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
        return Objects.equals(status, that.status) &&
                Objects.equals(path, that.path) &&
                Objects.equals(language, that.language) &&
                Objects.equals(descriptionType, that.descriptionType) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, path, language, descriptionType, options);
    }

    @Override
    public String toString() {
        return "FilterOptions{" +
                options.stream().map(Object::toString).collect(Collectors.joining(", ")) +
                "}";
    }
}