/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.view;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.coordinate.edit.Activity;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.VertexSort;
import dev.ikm.tinkar.coordinate.view.VertexSortNaturalOrder;
import dev.ikm.tinkar.coordinate.view.VertexSortNone;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.LongConsumer;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;

public class ViewMenuTask extends TrackingCallable<List<MenuItem>> {
    private static final Logger LOG = LoggerFactory.getLogger(ViewMenuTask.class);
    ViewCalculator viewCalculator;
    ObservableCoordinate observableCoordinate;
    private String whichMenu = "Unknown";

    public ViewMenuTask(ViewCalculator viewCalculator, ObservableCoordinate observableCoordinate, String whichMenu) {
        super(false, true);
        this.viewCalculator = viewCalculator;
        this.observableCoordinate = observableCoordinate;
        updateTitle("Updating View Menu");
        updateProgress(-1, -1);

        if (whichMenu != null) {
            this.whichMenu = whichMenu;
        }

        LOG.info("New ViewMenuTask");
    }

    private static void addSeparator(List<MenuItem> menuItems) {
        if (menuItems.get(menuItems.size() - 1) instanceof SeparatorMenuItem) {
            // already a separator, don't duplicate.
        } else {
            menuItems.add(new SeparatorMenuItem());
        }
    }

    private static void addRemoveOverrides(List<MenuItem> menuItems, ObservableCoordinate observableCoordinate) {
        if (observableCoordinate.hasOverrides()) {
            MenuItem removeOverrides = new MenuItem("Remove overrides");
            menuItems.add(removeOverrides);
            removeOverrides.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.removeOverrides();
                });
                event.consume();
            });
        }
    }

    private static void addChangeItemsForFilter(ViewCalculator viewCalculator, List<MenuItem> menuItems, ObservableStampCoordinate observableCoordinate) {


        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (PublicIdStringKey key : FxGet.pathCoordinates(viewCalculator).keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathForMenu = FxGet.pathCoordinates(viewCalculator).get(key);
            item.setSelected(pathForMenu.pathConceptNid() == observableCoordinate.pathNidForFilter());
            item.setUserData(FxGet.pathCoordinates(viewCalculator).get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.pathConceptProperty().setValue(Entity.getFast(path.pathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForFilter(menuItems, observableCoordinate);

        changeStates(menuItems, "Change filter states", observableCoordinate.allowedStatesProperty());

        addIncludedModulesMenu(menuItems, observableCoordinate, viewCalculator);

        addExcludedModulesMenu(menuItems, observableCoordinate, viewCalculator);

    }

    private static void changeStates(List<MenuItem> menuItems, String menuText, ObservableView observableView) {
        Menu changeAllowedStatusMenu = new Menu(menuText);
        menuItems.add(changeAllowedStatusMenu);
        for (StateSet stateSet : new StateSet[]{StateSet.ACTIVE, StateSet.ACTIVE_AND_INACTIVE, StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN,
                StateSet.INACTIVE, StateSet.WITHDRAWN}) {
            CheckMenuItem item = new CheckMenuItem(stateSet.toUserString());
            item.setSelected(stateSet.equals(observableView.stampCoordinate().allowedStates()));
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableView.setAllowedStates(stateSet);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }

    }

    private static void changeStates(List<MenuItem> menuItems, String menuText, ObjectProperty<StateSet> statusProperty) {
        Menu changeAllowedStatusMenu = new Menu(menuText);
        menuItems.add(changeAllowedStatusMenu);

        for (StateSet statusSet : new StateSet[]{StateSet.ACTIVE, StateSet.ACTIVE_AND_INACTIVE,
                StateSet.ACTIVE_INACTIVE_AND_WITHDRAWN, StateSet.INACTIVE, StateSet.WITHDRAWN}) {
            CheckMenuItem item = new CheckMenuItem(statusSet.toUserString());
            item.setSelected(statusSet.equals(statusProperty.get()));
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    statusProperty.setValue(statusSet);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }
    }

    private static void addChangePositionForManifold(List<MenuItem> menuItems, ObservableView observableView) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> {
                observableView.stampCoordinate().timeProperty().setValue(time);
            });
        });
    }

    private static void addChangePositionForFilter(List<MenuItem> menuItems, ObservableStampCoordinate observableCoordinate) {
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
                String yearString = Integer.toString(year);
                if (time == PREMUNDANE_TIME){
                    yearString = PREMUNDANE;
                }
                Menu yearMenu = new Menu(yearString);
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

    private static void addChangeItemsForEdit(ViewCalculator viewCalculator, List<MenuItem> menuItems, ObservableEditCoordinate observableCoordinate) {
        Menu changeAuthorMenu = new Menu("Change author");
        menuItems.add(changeAuthorMenu);

        IntIdSet authors = viewCalculator.kindOf(TinkarTerm.USER.nid());

        // Create author assemblage
        for (int author : authors.toArray()) {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(author));
            item.setSelected(observableCoordinate.getAuthorNidForChanges() == author);
            changeAuthorMenu.getItems().add(item);
            item.setOnAction(event -> {
                observableCoordinate.authorForChangesProperty().setValue(EntityProxy.Concept.make(author));
                event.consume();
            });
        }
        changeAuthorMenu.getItems().sort((o1, o2) -> NaturalOrder.compareStrings(o1.getText(), o2.getText()));

        Menu changeDefaultModuleMenu = new Menu("Change default module");
        menuItems.add(changeDefaultModuleMenu);
        // Create module assemblage
        for (ConceptFacade module : new ConceptFacade[]{TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.SOLOR_MODULE,
                TinkarTerm.KOMET_MODULE, TinkarTerm.TEST_MODULE, TinkarTerm.TEST_PROMOTION_MODULE}) {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(module));
            item.setSelected(observableCoordinate.getDefaultModuleNid() == module.nid());
            changeDefaultModuleMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.defaultModuleProperty().setValue(module));
                event.consume();
            });
        }

        Menu changeDestinationModuleMenu = new Menu("Change destination module");
        menuItems.add(changeDestinationModuleMenu);
        // Create module assemblage
        for (ConceptFacade module : new ConceptFacade[]{TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.SOLOR_MODULE,
                TinkarTerm.KOMET_MODULE}) {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(module));
            item.setSelected(observableCoordinate.getDestinationModuleNid() == module.nid());
            changeDestinationModuleMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.promotionPathProperty().setValue(module));
                event.consume();
            });
        }


        Menu changePromotionPathMenu = new Menu("Change promotion path");
        menuItems.add(changePromotionPathMenu);

        for (StampPathImmutable path : PathService.get().getPaths()) {
            CheckMenuItem item = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(path.pathConceptNid()));
            item.setSelected(observableCoordinate.getPromotionPathNid() == path.pathConceptNid());
            changePromotionPathMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.promotionPathProperty().setValue(EntityProxy.Concept.make(path.pathConceptNid())));
                event.consume();
            });
        }
    }

    private static void addChangeItemsForNavigation(ViewCalculator viewCalculator,
                                                    List<MenuItem> menuItems,
                                                    ObservableNavigationCoordinate observableCoordinate) {
        Menu changeNavigationMenu = new Menu("Change navigation");
        menuItems.add(changeNavigationMenu);
        for (ImmutableList<PatternFacade> navOption : FxGet.navigationOptions()) {
            StringBuilder menuText = new StringBuilder();
            for (PatternFacade navConcept : navOption) {
                if (menuText.length() > 0) {
                    menuText.append(", ");
                }
                menuText.append(viewCalculator.getPreferredDescriptionStringOrNid(navConcept));
            }
            CheckMenuItem item = new CheckMenuItem(menuText.toString());
            if (navOption.size() == observableCoordinate.navigationPatternNids().size()) {
                boolean foundAll = true;
                for (PatternFacade navPattern : navOption) {
                    if (!observableCoordinate.navigationPatternNids().contains(navPattern.nid())) {
                        foundAll = false;
                    }
                }
                item.setSelected(foundAll);
            }
            if (!item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        ObservableSet<PatternFacade> newSet = FXCollections.observableSet(navOption.toArray(new PatternFacade[navOption.size()]));
                        observableCoordinate.navigationPatternsProperty().setValue(newSet);
                    });
                    event.consume();
                });
            }
            changeNavigationMenu.getItems().add(item);
        }
    }

    private static void addChangeItemsForLogic(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                               ObservableLogicCoordinate observableCoordinate) {
    }

    private static void addChangeItemsForLanguage(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                                  ObservableLanguageCoordinate observableCoordinate, String whichMenu) {

        // Change description preference menu is also being set in the menu in addChangeItemsForView()
        // use common method to create the menu items
        createChangeDescriptionPreferenceMenuItems(viewCalculator, menuItems, observableCoordinate, whichMenu);

        Menu changeLanguageMenu = new Menu("Change language");
        menuItems.add(changeLanguageMenu);
        for (ConceptFacade language : FxGet.allowedLanguages()) {
            CheckMenuItem languageItem = new CheckMenuItem(viewCalculator.getPreferredDescriptionStringOrNid(language));
            changeLanguageMenu.getItems().add(languageItem);
            languageItem.setSelected(language.nid() == observableCoordinate.languageConceptProperty().get().nid());
            languageItem.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.languageConceptProperty().setValue(language));
                event.consume();
            });
        }

        Menu changeDialectOrder = new Menu("Change dialect preference order");
        menuItems.add(changeDialectOrder);
        for (MenuItem menuItem : menuItems) {
            menuItem.getStyleClass().add("menu-item-custom");
        }
        for (ImmutableList<? extends PatternFacade> dialectPreferenceList : FxGet.allowedDialectTypeOrder()) {
            CheckMenuItem dialectOrderItem = new CheckMenuItem(viewCalculator.toEntityString(dialectPreferenceList.castToList(), viewCalculator::toEntityStringOrPublicIdAndNid));
            changeDialectOrder.getItems().add(dialectOrderItem);
            dialectOrderItem.setSelected(observableCoordinate.dialectPatternPreferenceListProperty().getValue().equals(dialectPreferenceList.castToList()));
            dialectOrderItem.setOnAction(event -> {
                ObservableList<PatternFacade> prefList = FXCollections.observableArrayList(dialectPreferenceList.toArray(new PatternFacade[0]));
                Platform.runLater(() -> observableCoordinate.dialectPatternPreferenceListProperty().setValue(prefList));
                event.consume();
            });
        }
    }

    private static void addChangeItemsForView(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                              ObservableView observableView, String whichMenu) {

//        Menu changeActivityMenu = new Menu("Change activity");
//        menuItems.add(changeActivityMenu);
//        for (Activity activity: Activity.values()) {
//            CheckMenuItem activityItem = new CheckMenuItem(activity.toUserString());
//            changeActivityMenu.getItems().add(activityItem);
//            activityItem.setSelected(observableView.getCurrentActivity() == activity);
//            activityItem.setOnAction(event -> {
//                Platform.runLater(() ->
//                        observableView.activityProperty().setValue(activity)
//                );
//                event.consume();
//            });
//        }

        changeStates(menuItems, "Change allowed states", observableView);

        changeStates(menuItems, "Change allowed edge and language states", observableView.stampCoordinate().allowedStatesProperty());

        changeStates(menuItems, "Change allowed vertex states", observableView.navigationCoordinate().vertexStatesProperty());

        var languageCoordinates = observableView.languageCoordinates();

        for (int i = 0; i < languageCoordinates.size(); i++) {
            ObservableLanguageCoordinate languageCoordinate = languageCoordinates.get(i);
            var menuText = "Change language coordinate" +
                    (languageCoordinates.size() > 1 ? " " + i : "");

            Menu languageCoordinateMenu = new Menu(menuText);
            menuItems.add(languageCoordinateMenu);

            // Change description preference menu is also being set in the menu in addChangeItemsForLanguage()
            // use common method to create the menu items
            createChangeDescriptionPreferenceMenuItems(viewCalculator, languageCoordinateMenu.getItems(), languageCoordinate, whichMenu);
        }

        addChangeItemsForNavigation(viewCalculator,
                menuItems,
                observableView.navigationCoordinate());

        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (PublicIdStringKey key : FxGet.pathCoordinates(viewCalculator).keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathCoordinate = FxGet.pathCoordinates(viewCalculator).get(key);
            int pathNid = pathCoordinate.pathConceptNid();
            item.setSelected(pathNid == observableView.stampCoordinate().pathNidForFilter());
            item.setUserData(FxGet.pathCoordinates(viewCalculator).get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableView.setViewPath((path.pathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForManifold(menuItems, observableView);

        Menu changeVertexSortMenu = new Menu("Change sort");
        menuItems.add(changeVertexSortMenu);
        VertexSort[] sorts = new VertexSort[]{VertexSortNaturalOrder.SINGLETON, VertexSortNone.SINGLETON};
        for (VertexSort vertexSort : sorts) {
            CheckMenuItem item = new CheckMenuItem(vertexSort.getVertexSortName());
            item.setSelected(observableView.navigationCoordinate().sortVerticesProperty().equals(vertexSort));
            item.setOnAction(event -> {
                Platform.runLater(() ->
                        observableView.navigationCoordinate().sortVerticesProperty().setValue(vertexSort == VertexSortNaturalOrder.SINGLETON));
                event.consume();
            });
            changeVertexSortMenu.getItems().add(item);
        }

        MenuItem reloadManifoldMenu = new MenuItem("Reload view menu");
        menuItems.add(reloadManifoldMenu);
        reloadManifoldMenu.setOnAction(event -> {
            event.consume();
            Platform.runLater(() -> {
                MenuItem sourceMenu = (MenuItem) event.getSource();
                Menu parentMenu = sourceMenu.getParentMenu();
                if (parentMenu != null) {
                    parentMenu.getItems().clear();
                    TinkExecutor.threadPool().execute(TaskWrapper.make(new ViewMenuTask(viewCalculator, observableView, whichMenu),
                            (List<MenuItem> result) -> parentMenu.getItems().addAll(result)));
                }
            });
        });

    }

    /**
     * Creates the menu items for the Change description preferenence language selections, which includes the
     * Fully Qualified Name and Regular Name
     */
    private static void createChangeDescriptionPreferenceMenuItems(ViewCalculator viewCalculator, List<MenuItem> menuItems, ObservableLanguageCoordinate languageCoordinate, String whichMenu) {
        Menu changeDescriptionPreferenceMenu = new Menu("Change description preference");
        menuItems.add(changeDescriptionPreferenceMenu);

        for (ImmutableList<? extends ConceptFacade> typePreferenceList : FxGet.allowedDescriptionTypeOrder()) {
            CheckMenuItem typeOrderItem = new CheckMenuItem(viewCalculator.toEntityString(typePreferenceList.castToList(), viewCalculator::toEntityStringOrPublicIdAndNid));
            changeDescriptionPreferenceMenu.getItems().add(typeOrderItem);
            typeOrderItem.setSelected(languageCoordinate.descriptionTypePreferenceListProperty().getValue().equals(typePreferenceList.castToList()));
            typeOrderItem.setDisable(typeOrderItem.isSelected());
            typeOrderItem.setOnAction(event -> {

                if (whichMenu.equals("JournalController")) {
                    LOG.debug("JournalController menu");
                }

                ObservableList<ConceptFacade> prefList = FXCollections.observableArrayList(typePreferenceList.toArray(new ConceptFacade[0]));
                Platform.runLater(() ->
                        languageCoordinate.descriptionTypePreferenceListProperty().setValue(prefList)
                );
                event.consume();
            });
        }
    }

    private static boolean makeRecursiveOverrideMenu(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                                     ObservableCoordinate observableCoordinate) {

        if (observableCoordinate.hasOverrides()) {
            Menu overridesMenu = new Menu(viewCalculator.toPreferredEntityStringOrInputString(observableCoordinate.getName()) + " has overrides");
            menuItems.add(overridesMenu);
            for (Property property : observableCoordinate.getBaseProperties()) {
                if (property instanceof PropertyWithOverride) {
                    PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                    if (propertyWithOverride.isOverridden()) {
                        overridesMenu.getItems().add(new MenuItem(getNameAndValueString(viewCalculator, propertyWithOverride)));
                    }
                }
            }
            addRemoveOverrides(menuItems, observableCoordinate);

            for (ObservableCoordinate compositeCoordinate : observableCoordinate.getCompositeCoordinates()) {
                if (makeRecursiveOverrideMenu(viewCalculator, overridesMenu.getItems(),
                        compositeCoordinate)) {
                    addSeparator(menuItems);
                }
            }
            return true;
        }
        return false;
    }

    private static String getNameAndValueString(ViewCalculator viewCalculator, Property<?> baseProperty) {
        String propertyName = getPropertyNameWithOverride(viewCalculator, baseProperty);
        StringBuilder sb = new StringBuilder(propertyName + ": ");
        Object value = baseProperty.getValue();
        if (value instanceof Collection collection) {
            if (collection.isEmpty()) {
                if (propertyName.toLowerCase().startsWith("modules")) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    viewCalculator.toEntityString(StampService.get().getModulesInUse(),
                            viewCalculator::toEntityStringOrPublicIdAndNid,
                            collectionBuilder);
                    sb.append(" (*)\n").append(collectionBuilder);
                } else {
                    viewCalculator.toEntityString(value, viewCalculator::getPreferredDescriptionStringOrNid, sb);
                }
            } else {
                Object obj = collection.iterator().next();
                if (obj instanceof ConceptFacade) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    viewCalculator.toEntityString(value, viewCalculator::toEntityStringOrPublicIdAndNid, collectionBuilder);
                    sb.append("\n").append(collectionBuilder);
                } else {
                    if (collection instanceof Set) {
                        Object[] objects = collection.toArray();
                        Arrays.sort(objects, (o1, o2) ->
                                NaturalOrder.compareStrings(o1.toString(), o2.toString()));
                        sb.append(Arrays.toString(objects));
                    } else {
                        sb.append(collection.toString());
                    }

                }
            }

        } else if (value instanceof Activity) {
            sb.append(((Activity) value).toUserString());
        } else if (value instanceof StateSet) {
            sb.append(((StateSet) value).toUserString());
        } else {
            viewCalculator.toEntityString(value, viewCalculator::getPreferredDescriptionStringOrNid, sb);
        }
        return sb.toString();
    }

    private static String getPropertyNameWithOverride(ViewCalculator viewCalculator, Property<?> baseProperty) {
        if (baseProperty instanceof PropertyWithOverride propertyWithOverride) {
            return propertyWithOverride.getOverrideName(viewCalculator);
        }
        return viewCalculator.toPreferredEntityStringOrInputString(baseProperty.getName());
    }

    @Override
    protected List<MenuItem> compute() throws Exception {
        List<MenuItem> menuItems = new ArrayList<>();
        makeCoordinateDisplayMenu(viewCalculator,
                menuItems,
                observableCoordinate);
        updateTitle("Updated View Menu");
        updateMessage("In " + durationString());
        LOG.info("Updated View Menu in " + durationString());
        return menuItems;
    }

    /**
     * The
     *
     * @param viewCalculator       Used to get preferred concept names
     * @param observableCoordinate The coordinate to make an display menu for.
     */
    private void makeCoordinateDisplayMenu(ViewCalculator viewCalculator,
                                           List<MenuItem> menuItems,
                                           ObservableCoordinate observableCoordinate) {

        makeRecursiveOverrideMenu(viewCalculator, menuItems,
                observableCoordinate);

        for (Property<?> baseProperty : observableCoordinate.getBaseProperties()) {
            menuItems.add(new MenuItem(getNameAndValueString(viewCalculator, baseProperty)));
        }

        updateMessage("Making composite coordinate menu");
        for (ObservableCoordinate<?> compositeCoordinate : observableCoordinate.getCompositeCoordinates()) {
            String propertyName = getPropertyNameWithOverride(viewCalculator, compositeCoordinate);
            Menu compositeMenu = new Menu(propertyName);
            menuItems.add(compositeMenu);
            makeCoordinateDisplayMenu(viewCalculator, compositeMenu.getItems(), compositeCoordinate);
        }

        if (observableCoordinate instanceof ObservableView observableView) {
            addSeparator(menuItems);
            //addRemoveOverrides(menuItems, observableCoordinate);
            addChangeItemsForView(viewCalculator, menuItems, observableView, whichMenu);
        } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {
            addSeparator(menuItems);
            updateMessage("Making change language menu");
            addChangeItemsForLanguage(viewCalculator, menuItems, observableLanguageCoordinate, whichMenu);
        } else if (observableCoordinate instanceof ObservableLogicCoordinate observableLogicCoordinate) {
            //menuItems.add(new SeparatorMenuItem());
            updateMessage("Making change logic menu");
            addChangeItemsForLogic(viewCalculator, menuItems, observableLogicCoordinate);
        } else if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {
            addSeparator(menuItems);
            updateMessage("Making change navigation menu");
            addChangeItemsForNavigation(viewCalculator, menuItems, observableNavigationCoordinate);
        } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {
            addSeparator(menuItems);
            updateMessage("Making change stamp filter menu");
            addChangeItemsForFilter(viewCalculator, menuItems, observableStampCoordinate);
        }
    }
}
