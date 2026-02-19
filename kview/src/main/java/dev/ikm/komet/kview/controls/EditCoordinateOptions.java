package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.*;

public class EditCoordinateOptions implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.edit-coordinate-options");

    public enum OPTION_ITEM {
        // Main Coordinates
        AUTHOR_FOR_CHANGE("author_for_change", "Author"),
        DEFAULT_MODULE("default_module", "Module"),
        DESTINATION_MODULE("dest_module", "Module"),
        DEFAULT_PATH("default_path", "Path"),
        PROMOTION_PATH("promotion_path", "Path");

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
        private final ObservableList<T> availableOptions;
        private final ObservableList<T> selectedOptions;
        private final ObservableList<T> excludedOptions;
        private final boolean multiSelect;
        private boolean any;
        private final EnumSet<BUTTON> buttonType;
        private boolean inOverride;

        public Option(OPTION_ITEM item, String title,
                      ObservableList<T> availableOptions, ObservableList<T> selectedOptions, ObservableList<T> excludedOptions,
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

        public ObservableList<T> availableOptions() {
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
                    FXCollections.observableArrayList(availableOptions.stream().toList()),
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
            if (!compareLists(selectedOptions, option.selectedOptions)) return false;
            if (!compareLists(excludedOptions, option.excludedOptions)) return false;
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

    public interface EditCoordinates {}

    public class MainEditCoordinates implements EditCoordinates {

        // MainCoordinates
        private Option<ConceptFacade> authorForChange = new Option<>(OPTION_ITEM.AUTHOR_FOR_CHANGE, "author_for_change.title",
                FXCollections.observableArrayList(), FXCollections.observableArrayList(), null, false, false, noneSet, false);

        private Option<ConceptFacade> defaultModule = new Option<>(OPTION_ITEM.DEFAULT_MODULE, "default_module.title",
                FXCollections.observableArrayList(), FXCollections.observableArrayList(), null, false, false, noneSet, false);

        private Option<ConceptFacade> destinationModule = new Option<>(OPTION_ITEM.DESTINATION_MODULE, "dest_module.title",
                FXCollections.observableArrayList(), FXCollections.observableArrayList(), null, false, false, noneSet, false);


        private Option<ConceptFacade> defaultPath = new Option<>(OPTION_ITEM.DEFAULT_PATH, "default_path.title",
                FXCollections.observableArrayList(), FXCollections.observableArrayList(), null, false, false, noneSet, false);

        private Option<ConceptFacade> promotionPath = new Option<>(OPTION_ITEM.PROMOTION_PATH, "promotion_path.title",
                FXCollections.observableArrayList(), FXCollections.observableArrayList(), null, false, false, noneSet, false);


        private final List<Option> options;

        MainEditCoordinates() {
            options = new ArrayList<>(List.of(
                    authorForChange,
                    defaultModule,
                    destinationModule,
                    defaultPath,
                    promotionPath
            ));
        }

        public Option<ConceptFacade> getAuthorForChange() {
            return authorForChange;
        }

        public Option<ConceptFacade> getDefaultModule() {
            return defaultModule;
        }
        public Option<ConceptFacade> getDestinationModule() {
            return destinationModule;
        }

        public Option<ConceptFacade> getDefaultPath() {
            return defaultPath;
        }
        public Option<ConceptFacade> getPromotionPath() {
            return promotionPath;
        }

        public List<Option> getOptions() {
            return options;
        }

        public MainEditCoordinates copy() {
            MainEditCoordinates copy = new MainEditCoordinates();
            copy.authorForChange = authorForChange.copy();
            copy.defaultModule = defaultModule.copy();
            copy.destinationModule = destinationModule.copy();
            copy.defaultPath = defaultPath.copy();
            copy.promotionPath = promotionPath.copy();
            copy.options.clear();
            copy.options.addAll(List.of(
                    copy.authorForChange,
                    copy.defaultModule,
                    copy.destinationModule,
                    copy.defaultPath,
                    copy.promotionPath
            ));
            return copy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MainEditCoordinates that = (MainEditCoordinates) o;
            return
                    Objects.equals(authorForChange, that.authorForChange) &&
//                    Objects.equals(header, that.header) &&
                    Objects.equals(defaultModule, that.defaultModule) &&
                    Objects.equals(destinationModule, that.destinationModule) &&
                    Objects.equals(defaultPath, that.defaultPath) &&
                    Objects.equals(promotionPath, that.promotionPath) &&
                    Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    authorForChange,
                    defaultModule,
                    destinationModule,
                    defaultPath,
                    promotionPath,
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

    private MainEditCoordinates mainCoordinates;

    private final ObservableEditCoordinate observableEditCoordinateForOptions;

    /**
     * Constructor for FilterOptions (aka view coordinate options)
     *
     * @param parentViewCoordinate the parent view coordinate.
     */
    public EditCoordinateOptions(ObservableEditCoordinate parentViewCoordinate) {
        this.observableEditCoordinateForOptions = parentViewCoordinate;
//        this.parentViewCoordinate = parentViewCoordinate;
        mainCoordinates = new MainEditCoordinates();
    }

    public ObservableEditCoordinate observableEditCoordinateForOptionsProperty() {
        return observableEditCoordinateForOptions;
    }

    public MainEditCoordinates getMainCoordinates() {
        return mainCoordinates;
    }


    public Option getOptionForItem(OPTION_ITEM item) {
        return mainCoordinates.options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case AUTHOR_FOR_CHANGE -> mainCoordinates.authorForChange = (Option<ConceptFacade>) option;
            case DEFAULT_MODULE -> mainCoordinates.defaultModule = (Option<ConceptFacade>) option;
            case DESTINATION_MODULE -> mainCoordinates.destinationModule = (Option<ConceptFacade>) option;
            case DEFAULT_PATH -> mainCoordinates.defaultPath = (Option<ConceptFacade>) option;
            case PROMOTION_PATH -> mainCoordinates.promotionPath = (Option<ConceptFacade>) option;
        }
        mainCoordinates.options.clear();
        mainCoordinates.options.addAll(List.of(
                mainCoordinates.authorForChange,
                mainCoordinates.defaultModule,
                mainCoordinates.destinationModule,
                mainCoordinates.defaultPath,
                mainCoordinates.promotionPath
        ));
    }

    public EditCoordinateOptions copy() {
        EditCoordinateOptions copy = new EditCoordinateOptions(this.observableEditCoordinateForOptions);
        copy.mainCoordinates = mainCoordinates.copy();
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditCoordinateOptions that = (EditCoordinateOptions) o;
        if (!Objects.equals(mainCoordinates, that.mainCoordinates)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainCoordinates
        );
    }

    @Override
    public String toString() {
        return "FilterOptions{ " +
                mainCoordinates + "\n" +
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