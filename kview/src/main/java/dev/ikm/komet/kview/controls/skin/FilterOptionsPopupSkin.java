package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.ResourceBundle;

public class FilterOptionsPopupSkin implements Skin<FilterOptionsPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    private final FilterOptionsPopup control;
    private final VBox root;

    private final Accordion accordion;

    private final Subscription subscription;
    private Subscription filterSubscription;

    public FilterOptionsPopupSkin(FilterOptionsPopup control) {
        this.control = control;

        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> getSkinnable().hide());

        Label title = new Label(resources.getString("header.title"));
        title.getStyleClass().add("title");

        StackPane filterPane = new StackPane(new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("region");
        filterPane.setOnMouseClicked(_ -> {
        });

        HBox headerBox = new HBox(closePane, title, filterPane);
        headerBox.getStyleClass().add("header-box");

        FilterOptions filterOptions = new FilterOptions();
        FilterTitledPane statusFilterTitledPane = setupTitledPane(filterOptions.getStatus());
        FilterTitledPane pathFilterTitledPane = setupTitledPane(filterOptions.getPath());
        FilterTitledPane languageFilterTitledPane = setupTitledPane(filterOptions.getLanguage());
        FilterTitledPane descriptionFilterTitledPane = setupTitledPane(filterOptions.getDescription());

        accordion = new Accordion(
                statusFilterTitledPane,
                pathFilterTitledPane,
                languageFilterTitledPane,
                descriptionFilterTitledPane);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button applyButton = new Button(resources.getString("button.apply"));
        applyButton.setOnAction(_ -> {
            accordion.setExpandedPane(null);
            FilterOptions currentFilterOptions = new FilterOptions();
            accordion.getPanes().stream()
                    .map(FilterTitledPane.class::cast)
                    .forEach(pane -> {
                        if (pane.getUserData() instanceof FilterOptions.Option option) {
                            currentFilterOptions.getOptionForItem(option.item())
                                    .selectedOptions().setAll(FilterOptions.fromString(pane.getOption()));
                        }
                    });
            control.setFilterOptions(currentFilterOptions);
        });
        StackPane region = new StackPane(new IconRegion("icon", "filter"));
        region.getStyleClass().add("region");
        Button saveButton = new Button(resources.getString("button.save"), region);
        saveButton.setOnAction(_ -> System.out.println("Save"));
        Button revertButton = new Button(resources.getString("button.revert"));
        revertButton.setOnAction(_ -> {
            accordion.setExpandedPane(null);
            control.setFilterOptions(FilterOptions.defaultOptions());
        });
        VBox bottomBox = new VBox(applyButton, saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, accordion, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        subscription = control.filterOptionsProperty().subscribe(this::setupFilter);
    }

    private void setupFilter(FilterOptions filterOptions) {
        // changes from titledPane control:
        filterSubscription = Subscription.EMPTY;
        accordion.getPanes().stream()
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    filterSubscription = filterSubscription.and(pane.optionProperty().subscribe(s -> {
                        if (s != null && pane.getUserData() instanceof FilterOptions.Option option) {
                            option.selectedOptions().setAll(FilterOptions.fromString(s));
                        }
                    }));
                });

        // pass popup control options to titledPane controls
        accordion.getPanes().stream()
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    if (pane.getUserData() instanceof FilterOptions.Option option) {
                        pane.setOption(String.join(", ",
                                filterOptions.getOptionForItem(option.item()).selectedOptions()));
                    }
                });
    }

    @Override
    public FilterOptionsPopup getSkinnable() {
        return control;
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        if (filterSubscription != null) {
            filterSubscription.unsubscribe();
        }
    }

    private FilterTitledPane setupTitledPane(FilterOptions.Option option) {
        FilterTitledPane titledPane = new FilterTitledPane();
        titledPane.setUserData(option);
        titledPane.setTitle(option.title());
        titledPane.setDefaultOption(option.defaultOption());
        titledPane.setMultiSelect(option.isMultiSelectionAllowed());
        titledPane.setOptions(option.availableOptions());
        titledPane.setExpanded(false);
        return titledPane;
    }
}
