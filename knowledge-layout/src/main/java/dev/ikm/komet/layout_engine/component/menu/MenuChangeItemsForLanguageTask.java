package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.concurrent.Callable;

import static dev.ikm.komet.layout_engine.component.menu.ViewMenuFactory.indexToWord;


public class MenuChangeItemsForLanguageTask implements Callable<MenuItem>, ScopedValue.CallableOp<MenuItem, Exception> {

    @Override
    public MenuItem call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();


        Menu changeLanguageMenu = new Menu("Change language");
        for (int i = 0; i < observableView.languageCoordinates().size(); i++) {
            Menu changeMenu = addChangeItemsForLanguage(observableView.languageCoordinates().get(i),
                    indexToWord(i) + " coordinate ", viewCalculator);
            changeLanguageMenu.getItems().add(changeMenu);
        }

        return changeLanguageMenu;
    }


    private static Menu addChangeItemsForLanguage(ObservableLanguageCoordinate observableCoordinate,
                                                  String menuName, ViewCalculator viewCalculator
                                                  ) {
        Menu changeMenu = new Menu(menuName);
        Menu changeTypeOrder = new Menu("Change description preference");
        changeMenu.getItems().add(changeTypeOrder);
        for (ImmutableList<? extends ConceptFacade> typePreferenceList : FxGet.allowedDescriptionTypeOrder()) {
            CheckMenuItem typeOrderItem = new CheckMenuItem(viewCalculator.toEntityString(typePreferenceList.castToList(), viewCalculator::toEntityStringOrPublicIdAndNid));
            changeTypeOrder.getItems().add(typeOrderItem);
            typeOrderItem.setSelected(observableCoordinate.descriptionTypePreferenceListProperty().getValue().equals(typePreferenceList.castToList()));
            typeOrderItem.setDisable(typeOrderItem.isSelected());
            typeOrderItem.setOnAction(event -> {
                ObservableList<ConceptFacade> prefList = FXCollections.observableArrayList(typePreferenceList.toArray(new ConceptFacade[0]));
                Platform.runLater(() ->
                        observableCoordinate.descriptionTypePreferenceListProperty().setValue(prefList)
                );
                event.consume();
            });
        }

        Menu changeLanguageMenu = new Menu("Change language");
        changeMenu.getItems().add(changeLanguageMenu);
        for (ConceptFacade language : FxGet.allowedLanguages()) {
            CheckMenuItem languageItem = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(language));
            changeLanguageMenu.getItems().add(languageItem);
            languageItem.setSelected(language.nid() == observableCoordinate.languageConceptProperty().get().nid());
            languageItem.setDisable(languageItem.isSelected());
            languageItem.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.languageConceptProperty().setValue(language));
                event.consume();
            });
        }

        Menu changeDialectOrder = new Menu("Change dialect preference order");
        changeMenu.getItems().add(changeDialectOrder);
        for (MenuItem menuItem : changeMenu.getItems()) {
            menuItem.getStyleClass().add("menu-item-custom");
        }
        for (ImmutableList<? extends PatternFacade> dialectPreferenceList : FxGet.allowedDialectTypeOrder()) {
            CheckMenuItem dialectOrderItem = new CheckMenuItem(viewCalculator.toEntityString(dialectPreferenceList.castToList(), viewCalculator::toEntityStringOrPublicIdAndNid));
            changeDialectOrder.getItems().add(dialectOrderItem);
            dialectOrderItem.setSelected(observableCoordinate.dialectPatternPreferenceListProperty().getValue().equals(dialectPreferenceList.castToList()));
            if (dialectOrderItem.isSelected()) {
                dialectOrderItem.setDisable(true);
            }
            dialectOrderItem.setOnAction(event -> {
                ObservableList<PatternFacade> prefList = FXCollections.observableArrayList(dialectPreferenceList.toArray(new PatternFacade[0]));
                Platform.runLater(() -> observableCoordinate.dialectPatternPreferenceListProperty().setValue(prefList));
                event.consume();
            });
        }
        return changeMenu;
    }

}
