package dev.ikm.komet.kview.mvvm.view.journal;

import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.window.KlSceneFactory;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.eclipse.collections.api.list.ImmutableList;

public class JournalWindowFactory implements KlWindowFactory {
    @Override
    public ImmutableList<MenuItem> createMenuItems() {
        return null;
    }

    @Override
    public KlWindow create(KometPreferences preferences) {
        FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
        BorderPane journalBorderPane = null;
        return null;
//        try {
//            journalBorderPane = journalLoader.load();
//            JournalController journalController = journalLoader.getController();
//            Scene sourceScene = new Scene(journalBorderPane, 1200, 800);
//            JournalWindow journalWindow = new JournalWindow();
//            journalWindow.setScene(sourceScene);
//            journalWindow.setOnCloseRequest(windowEvent -> {
//                saveJournalWindowsToPreferences();
//                // call shutdown method on the view
//                journalController.shutdown();
//                journalControllersList.remove(journalController);
//                // enable Delete menu option
//                journalWindowSettings.setValue(CAN_DELETE, true);
//                EvtBusFactory.getDefaultEvtBus().publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
//            });
//
//            return journalWindow;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public KlWindow create(KometPreferences preferences, KlSceneFactory sceneFactory) {
        return null;
    }

    @Override
    public WindowType factoryWindowType() {
        return WindowType.JOURNAL;
    }

    @Override
    public Class<? extends KlWidget> klWidgetInterfaceClass() {
        return null;
    }

    @Override
    public Class<?> klWidgetImplementationClass() {
        return null;
    }



//    private void launchJournalViewWindow(PrefX journalWindowSettings) {
//        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
//        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
//
//        WindowSettings windowSettings = new WindowSettings(windowPreferences);
//
//        // stage, loader, controller,
//        Stage journalStageWindow = new Stage();
//        FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
//        JournalController journalController;
//        try {
//            BorderPane journalBorderPane = journalLoader.load();
//            journalController = journalLoader.getController();
//            Scene sourceScene = new Scene(journalBorderPane, 1200, 800);
//
//            // TODO REFACTOR
//            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);
//
//            journalStageWindow.setScene(sourceScene);
//
//            // TODO REFACTOR
//            // if NOT on Mac OS
//            if(System.getProperty("os.name")!=null && !System.getProperty("os.name").toLowerCase().startsWith(OS_NAME_MAC)) {
//                generateMsWindowsMenu(journalBorderPane, journalStageWindow);
//            }
//
//            String journalName;
//            if (journalWindowSettings != null) {
//                // load journal specific window settings
//                journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
//                journalStageWindow.setTitle(journalName);
//                if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
//                    journalStageWindow.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
//                    journalStageWindow.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
//                    journalStageWindow.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
//                    journalStageWindow.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
//                    journalController.recreateConceptWindows(journalWindowSettings);
//                }else{
//                    journalStageWindow.setMaximized(true);
//                }
//            }
//
//            // TODO REFACTOR
//            journalStageWindow.setOnCloseRequest(windowEvent -> {
//                saveJournalWindowsToPreferences();
//                // call shutdown method on the view
//                journalController.shutdown();
//                journalControllersList.remove(journalController);
//                // enable Delete menu option
//                journalWindowSettings.setValue(CAN_DELETE, true);
//                kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
//            });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // TODO REFACTOR ViewModel stuff
//        journalController.setWindowView(windowSettings.getView());
//
//        // Launch windows window pane inside journal view
//        journalStageWindow.setOnShown(windowEvent -> {
//            //TODO: Refactor factory constructor calls below to use PluggableService (make constructors private)
//            KometNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
//            KometNodeFactory searchNodeFactory = new SearchNodeFactory();
//
//            journalController.launchKometFactoryNodes(
//                    journalWindowSettings.getValue(JOURNAL_TITLE),
//                    navigatorNodeFactory,
//                    searchNodeFactory);
//            // load additional panels
//            journalController.loadNextGenReasonerPanel();
//            journalController.loadNextGenSearchPanel();
//        });
//        // disable the delete menu option for a Journal Card.
//        journalWindowSettings.setValue(CAN_DELETE, false);
//
//        // TODO REFACTOR
//        kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
//        journalControllersList.add(journalController);
//
//        journalStageWindow.show();
//    }
}
