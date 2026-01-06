package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.LongConsumer;


public class MenuChangeItemsForStampTask implements Callable<MenuItem>, ScopedValue.CallableOp<MenuItem, Exception> {
    @Override
    public MenuItem call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();
        Menu changeMenu = new Menu("Change stamp");
        addChangeItemsForStamp(viewCalculator, changeMenu.getItems(), observableView.stampCoordinate());
        return changeMenu;
    }


    private static void addChangeItemsForStamp(ViewCalculator viewCalculator, List<MenuItem> menuItems, ObservableStampCoordinate observableCoordinate) {

        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (PublicIdStringKey key : FxGet.pathCoordinates(viewCalculator).keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathForMenu = FxGet.pathCoordinates(viewCalculator).get(key);
            item.setSelected(pathForMenu.pathConceptNid() == observableCoordinate.pathNidForFilter());
            item.setDisable(item.isSelected());
            item.setUserData(FxGet.pathCoordinates(viewCalculator).get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.pathConceptProperty().setValue(Entity.getFast(path.pathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }
        //TODO: add back or replace
        //addChangePositionForStamp(menuItems, observableCoordinate);

        ViewMenuFactory.addChangeStates(menuItems, "Change allowed states", observableCoordinate.allowedStatesProperty());

        addIncludedModulesMenu(menuItems, observableCoordinate, viewCalculator);

        addExcludedModulesMenu(menuItems, observableCoordinate, viewCalculator);

    }


    private static void addChangePositionForStamp(List<MenuItem> menuItems, ObservableStampCoordinate observableCoordinate) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> observableCoordinate.timeProperty().setValue(time));
        });
    }

    private static void addChangePositionMenu(List<MenuItem> menuItems, LongConsumer setPosition) {
        Menu changePositionMenu = new Menu("Change position");

        menuItems.add(changePositionMenu);
        MenuItem latestItem = new MenuItem("latest");
        changePositionMenu.getItems().add(latestItem);
        latestItem.setOnAction(event -> {
            Platform.runLater(() -> {
                setPosition.accept(Long.MAX_VALUE);
            });
            event.consume();
        });

        ImmutableLongList times = StampService.get().getTimesInUse().toReversed();

        MutableIntObjectMap<Menu> yearMenuMap = IntObjectMaps.mutable.empty();
        for (long time : times.toArray()) {
            LocalDateTime localTime = DateTimeUtil.epochToZonedDateTime(time).toLocalDateTime();
            Menu aYearMenu = yearMenuMap.getIfAbsentPutWithKey(localTime.getYear(), (int year) -> {
                Menu yearMenu = new Menu(Integer.toString(year));
                changePositionMenu.getItems().add(yearMenu);
                yearMenu.getItems().add(new Menu("Jan"));
                yearMenu.getItems().add(new Menu("Feb"));
                yearMenu.getItems().add(new Menu("Mar"));
                yearMenu.getItems().add(new Menu("Apr"));
                yearMenu.getItems().add(new Menu("May"));
                yearMenu.getItems().add(new Menu("Jun"));
                yearMenu.getItems().add(new Menu("Jul"));
                yearMenu.getItems().add(new Menu("Aug"));
                yearMenu.getItems().add(new Menu("Sep"));
                yearMenu.getItems().add(new Menu("Oct"));
                yearMenu.getItems().add(new Menu("Nov"));
                yearMenu.getItems().add(new Menu("Dec"));
                return yearMenu;
            });
            Menu monthMenu = (Menu) aYearMenu.getItems().get(localTime.getMonthValue() - 1);
            MenuItem positionMenu = new MenuItem(
                    localTime.getDayOfMonth() + DateTimeUtil.getDayOfMonthSuffix(localTime.getDayOfMonth()) +
                            " " + DateTimeUtil.EASY_TO_READ_TIME_FORMAT.format(DateTimeUtil.epochToZonedDateTime(time)));
            monthMenu.getItems().add(positionMenu);
            positionMenu.setOnAction(event -> {
                Platform.runLater(() -> setPosition.accept(time));
                event.consume();
            });
        }

        yearMenuMap.values().forEach(yearMenu -> {
            ArrayList<MenuItem> toRemove = new ArrayList<>();
            for (MenuItem monthMenu : yearMenu.getItems()) {
                if (((Menu) monthMenu).getItems().isEmpty()) {
                    toRemove.add(monthMenu);
                }
            }
            yearMenu.getItems().removeAll(toRemove);
        });
    }

    private static void addIncludedModulesMenu(List<MenuItem> menuItems,
                                               ObservableStampCoordinate observableCoordinate,
                                               ViewCalculator viewCalculator) {
        Menu addIncludedModulesMenu = new Menu("Change included modules");
        menuItems.add(addIncludedModulesMenu);
        CheckMenuItem allModulesItem = new CheckMenuItem("all module wildcard");
        allModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().isEmpty());
        if (allModulesItem.isSelected()) {
            allModulesItem.setDisable(true);
        }
        addIncludedModulesMenu.getItems().add(allModulesItem);
        allModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.moduleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem allIndividualModulesItem = new CheckMenuItem("all individual modules");

        allIndividualModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().containsAll(
                StampService.get().getModulesInUse().castToSet()));
        if (allIndividualModulesItem.isSelected()) {
            allIndividualModulesItem.setDisable(true);
        }

        addIncludedModulesMenu.getItems().add(allIndividualModulesItem);
        allIndividualModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                ObservableSet<ConceptFacade> newSet = FXCollections.observableSet();
                newSet.addAll(StampService.get().getModulesInUse().castToSet());
                observableCoordinate.moduleSpecificationsProperty().setValue(newSet);
            });
            event.consume();
        });

        StampService.get().getModulesInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(moduleConcept));
            item.setSelected(observableCoordinate.moduleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.moduleSpecificationsProperty().remove(moduleConcept);
                    });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.moduleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            addIncludedModulesMenu.getItems().add(item);
        });
    }

    private static void addExcludedModulesMenu(List<MenuItem> menuItems,
                                               ObservableStampCoordinate observableCoordinate,
                                               ViewCalculator viewCalculator) {
        Menu excludedModulesMenu = new Menu("Change excluded modules");
        menuItems.add(excludedModulesMenu);
        CheckMenuItem noExclusionsWildcard = new CheckMenuItem("no exclusions wildcard");
        noExclusionsWildcard.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().isEmpty());
        if (noExclusionsWildcard.isSelected()) {
            noExclusionsWildcard.setDisable(true);
        }

        excludedModulesMenu.getItems().add(noExclusionsWildcard);
        noExclusionsWildcard.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.excludedModuleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem excludeAllIndividualModulesItem = new CheckMenuItem("exclude all individual modules");

        excludeAllIndividualModulesItem.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().containsAll(
                StampService.get().getModulesInUse().castToSet()));
        excludedModulesMenu.getItems().add(excludeAllIndividualModulesItem);
        if (excludeAllIndividualModulesItem.isSelected()) {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.excludedModuleSpecificationsProperty().clear();
                });
                event.consume();
            });
        } else {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    ObservableSet<ConceptFacade> newSet = FXCollections.observableSet();
                    newSet.addAll(StampService.get().getModulesInUse().castToSet());
                    observableCoordinate.excludedModuleSpecificationsProperty().setValue(newSet);
                });
                event.consume();
            });
        }
        StampService.get().getModulesInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(moduleConcept));
            item.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.excludedModuleSpecificationsProperty().remove(moduleConcept);
                    });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.excludedModuleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            excludedModulesMenu.getItems().add(item);
        });
    }



}