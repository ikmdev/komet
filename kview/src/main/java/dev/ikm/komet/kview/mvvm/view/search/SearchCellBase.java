package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static dev.ikm.komet.kview.controls.KometIcon.IconValue.KL_EDITABLE_VIEW;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;

public abstract class SearchCellBase extends ListCell {
    protected final ViewProperties viewProperties;
    protected final UUID journalTopic;
    protected final ObservableViewNoOverride observableViewNoOverride;

    private final ContextMenu contextMenu = new ContextMenu();

    private final HashMap<String, MenuItem> windowTitleToMenuItem = new HashMap<>();

    public SearchCellBase(ViewProperties viewProperties, UUID journalTopic, ObservableViewNoOverride observableViewNoOverride) {
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;
        this.observableViewNoOverride = observableViewNoOverride;

        initContextMenu();
    }

    private void initContextMenu() {
        contextMenu.getStyleClass().add("search-context-menu-container");

        // Populate concept
        MenuItem populateConcept = new MenuItem("Populate concept");
        populateConcept.setMnemonicParsing(false);
        populateConcept.getStyleClass().add("search-context-menu-item");
        populateConcept.setGraphic(makePaddedRegion("populate-icon", 16));
        populateConcept.setOnAction(this::onPopulateConcept);

        // Open in Concept Navigator
        MenuItem openInConceptNavigator = new MenuItem("Open in Concept Navigator");
        openInConceptNavigator.setMnemonicParsing(false);
        openInConceptNavigator.getStyleClass().add("search-context-menu-item");
        openInConceptNavigator.setGraphic(makePaddedRegion("populate-icon", 16));
        openInConceptNavigator.setOnAction(this::onOpenInConceptNavigator);

        // Send to Journal
        MenuItem sendToJournal = new MenuItem("Send to Journal");
        sendToJournal.setMnemonicParsing(false);
        sendToJournal.getStyleClass().add("search-context-menu-item");
        sendToJournal.setGraphic(makePaddedRegion("arrow-icon", 16));

        // Send to Chapter
        MenuItem sendToChapter = new MenuItem("Send to Chapter");
        sendToChapter.setMnemonicParsing(false);
        sendToChapter.getStyleClass().add("search-context-menu-item");
        sendToChapter.setGraphic(makePaddedRegion("arrow-icon", 16));

        // SeparatorMenuItem
        SeparatorMenuItem optionLine = new SeparatorMenuItem();
        optionLine.setMnemonicParsing(false);
        optionLine.getStyleClass().add("search-option-menu-line");

        // Copy
        MenuItem copy = new MenuItem("Copy");
        copy.setMnemonicParsing(false);
        copy.getStyleClass().add("search-context-menu-item");
        copy.setGraphic(makeCopySvg());

        // Save to Favorites
        MenuItem saveToFavorites = new MenuItem("Save to Favorites");
        saveToFavorites.setMnemonicParsing(false);
        saveToFavorites.getStyleClass().add("search-context-menu-item");
        saveToFavorites.setGraphic(makePaddedRegion("favorites-icon", 16));

        SeparatorMenuItem klWindowsMenuSeparator = new SeparatorMenuItem();

        contextMenu.getItems().addAll(
                populateConcept,
                openInConceptNavigator,
                sendToJournal,
                sendToChapter,
                optionLine,
                copy,
                saveToFavorites,
                klWindowsMenuSeparator
        );

        setOnContextMenuRequested(mouseEvent -> {
            contextMenu.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        });

        // KL Window Options
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);

        klWindowsMenuSeparator.setVisible(!editorWindows.isEmpty());

        for (String windowTitle : editorWindows) {
            KometIcon icon = KometIcon.create(KL_EDITABLE_VIEW);
            MenuItem windowMenuItem = new MenuItem("Open as " + windowTitle, icon);
            windowMenuItem.getStyleClass().add("search-context-menu-item");
            windowTitleToMenuItem.put(windowTitle, windowMenuItem);
            contextMenu.getItems().add(windowMenuItem);

            windowMenuItem.setOnAction(actionEvent -> onOpenAsKLWindow(actionEvent, windowTitle));

            windowTitleToMenuItem.put(windowTitle, windowMenuItem);
        }
    }

    protected abstract void onPopulateConcept(ActionEvent actionEvent);

    protected abstract void onOpenInConceptNavigator(ActionEvent actionEvent);

    protected abstract void onOpenAsKLWindow(ActionEvent actionEvent, String windowTitle);

    private static Region makePaddedRegion(String styleClass, double rightPadding) {
        Region r = new Region();
        r.getStyleClass().add(styleClass);
        r.setPadding(new Insets(0, rightPadding, 0, 0)); // <Insets right="16.0" />
        return r;
    }

    private static SVGPath makeCopySvg() {
        SVGPath svg = new SVGPath();
        svg.setContent(
                "M9.43837 6.99586L9.43837 8.56292L11.4092 8.56292L11.4092 10.5614L12.9763 10.5614L12.9763 8.56292L15.0024 8.56292L15.0024 6.99586L12.9763 6.99586L12.9763 4.99741L11.4092 4.99741L11.4092 6.99586L9.43837 6.99586Z " +
                        "M1.6665 15.1645L1.6665 9.36525C1.66649 8.96838 1.66649 8.62428 1.68968 8.34039C1.71421 8.04011 1.76854 7.73908 1.91648 7.44873C2.13637 7.01718 2.48722 6.66633 2.91877 6.44644C3.20912 6.2985 3.51015 6.24417 3.81043 6.21964C4.09431 6.19645 4.4384 6.19645 4.83526 6.19646L6.19639 6.19646L6.19639 4.83534C6.19638 4.43846 6.19637 4.0944 6.21956 3.81051C6.24409 3.51023 6.29842 3.2092 6.44636 2.91885C6.66625 2.4873 7.01711 2.13644 7.44865 1.91656C7.739 1.76862 8.04003 1.71429 8.34031 1.68976C8.62423 1.66656 8.96834 1.66657 9.36528 1.66658L15.1643 1.66658C15.5612 1.66657 15.9053 1.66656 16.1892 1.68976C16.4895 1.71429 16.7906 1.76862 17.0809 1.91656C17.5125 2.13644 17.8633 2.4873 18.0832 2.91885C18.2311 3.2092 18.2855 3.51023 18.31 3.81051C18.3332 4.09443 18.3332 4.43854 18.3332 4.83547L18.3332 10.6345C18.3332 11.0314 18.3332 11.3755 18.31 11.6594C18.2855 11.9597 18.2311 12.2608 18.0832 12.5511C17.8633 12.9826 17.5125 13.3335 17.0809 13.5534C16.7906 13.7013 16.4895 13.7557 16.1892 13.7802C15.9054 13.8034 15.5613 13.8034 15.1644 13.8034L13.8033 13.8034L13.8033 15.1645C13.8033 15.5614 13.8033 15.9055 13.7801 16.1893C13.7556 16.4896 13.7013 16.7906 13.5533 17.081C13.3334 17.5125 12.9826 17.8634 12.551 18.0833C12.2607 18.2312 11.9596 18.2855 11.6594 18.3101C11.3755 18.3333 11.0314 18.3333 10.6345 18.3333L4.83527 18.3333C4.4384 18.3333 4.09432 18.3333 3.81043 18.3101C3.51015 18.2855 3.20912 18.2312 2.91877 18.0833C2.48723 17.8634 2.13637 17.5125 1.91648 17.081C1.76854 16.7906 1.71421 16.4896 1.68968 16.1893C1.66649 15.9054 1.66649 15.5613 1.6665 15.1645ZM3.25154 16.0617C3.23418 15.8493 3.23357 15.5696 3.23357 15.1338L3.23357 9.39593C3.23357 8.9601 3.23418 8.68046 3.25154 8.468C3.26816 8.26457 3.29614 8.19275 3.31274 8.16016C3.38239 8.02348 3.49352 7.91235 3.6302 7.8427C3.66279 7.8261 3.73462 7.79811 3.93804 7.78149C4.1505 7.76414 4.43014 7.76352 4.86597 7.76352L6.19639 7.76352L6.19639 10.6346C6.19638 11.0314 6.19637 11.3756 6.21956 11.6594C6.2441 11.9597 6.29842 12.2608 6.44636 12.5511C6.66625 12.9826 7.01711 13.3335 7.44865 13.5534C7.739 13.7013 8.04003 13.7557 8.34031 13.7802C8.6242 13.8034 8.96831 13.8034 9.36518 13.8034L12.2362 13.8034L12.2362 15.1338C12.2362 15.5696 12.2356 15.8493 12.2183 16.0617C12.2016 16.2651 12.1737 16.337 12.1571 16.3696C12.0874 16.5062 11.9763 16.6174 11.8396 16.687C11.807 16.7036 11.7352 16.7316 11.5318 16.7482C11.3193 16.7656 11.0397 16.7662 10.6038 16.7662L4.86597 16.7662C4.43014 16.7662 4.1505 16.7656 3.93804 16.7482C3.73462 16.7316 3.66279 16.7036 3.6302 16.687C3.49352 16.6174 3.38239 16.5062 3.31274 16.3696C3.29614 16.337 3.26816 16.2651 3.25154 16.0617ZM7.78142 11.5318C7.76406 11.3194 7.76345 11.0397 7.76345 10.6039L7.76345 4.86605C7.76345 4.43022 7.76406 4.15058 7.78142 3.93812C7.79804 3.73469 7.82602 3.66287 7.84262 3.63028C7.91227 3.4936 8.0234 3.38247 8.16009 3.31282C8.19267 3.29622 8.2645 3.26823 8.46792 3.25161C8.68039 3.23426 8.96003 3.23365 9.39585 3.23365L15.1337 3.23364C15.5695 3.23364 15.8492 3.23426 16.0616 3.25161C16.2651 3.26823 16.3369 3.29622 16.3695 3.31282C16.5062 3.38247 16.6173 3.4936 16.6869 3.63028C16.7035 3.66287 16.7315 3.73469 16.7481 3.93812C16.7655 4.15058 16.7661 4.43022 16.7661 4.86605L16.7661 10.6039C16.7661 11.0397 16.7655 11.3194 16.7481 11.5318C16.7315 11.7353 16.7035 11.8071 16.6869 11.8397C16.6173 11.9764 16.5062 12.0875 16.3695 12.1571C16.3369 12.1737 16.2651 12.2017 16.0616 12.2183C15.8492 12.2357 15.5695 12.2363 15.1337 12.2363L9.39586 12.2363C8.96003 12.2363 8.68039 12.2357 8.46792 12.2183C8.2645 12.2017 8.19267 12.1737 8.16009 12.1571C8.0234 12.0875 7.91227 11.9764 7.84262 11.8397C7.82602 11.8071 7.79804 11.7353 7.78142 11.5318Z"
        );

        svg.setFillRule(javafx.scene.shape.FillRule.EVEN_ODD);
        svg.setFill(Color.WHITE);

        return svg;
    }
}