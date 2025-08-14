package dev.ikm.komet.app;

import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.preferences.KometPreferencesStage;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.KometStageController;
import dev.ikm.komet.framework.window.MainWindowRecord;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.navigator.pattern.PatternNavigatorFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.komet.progress.CompletionNodeFactory;
import dev.ikm.komet.progress.ProgressNodeFactory;
import dev.ikm.komet.search.SearchNodeFactory;
import dev.ikm.komet.table.TableNodeFactory;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.App.*;
import static dev.ikm.komet.app.util.CssFile.KOMET_CSS;
import static dev.ikm.komet.app.util.CssUtils.addStylesheets;
import static dev.ikm.komet.framework.KometNodeFactory.KOMET_NODES;
import static dev.ikm.komet.framework.window.WindowSettings.Keys.*;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNALS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class AppClassicKomet {

    private static final Logger LOG = LoggerFactory.getLogger(AppGithub.class);

    private Stage classicKometStage;

    private final App app;

    /**
     * An entry point to launch the newer UI panels.
     */
    private MenuItem createJournalViewMenuItem;

    public AppClassicKomet(App app) {
        this.app = app;
    }

    void launchClassicKomet() throws IOException, BackingStoreException {
        if (IS_DESKTOP) {
            // If already launched bring to the front
            if (classicKometStage != null && classicKometStage.isShowing()) {
                classicKometStage.show();
                classicKometStage.toFront();
                return;
            }
        }

        classicKometStage = new Stage();
        classicKometStage.getIcons().setAll(app.appIcon);

        //Starting up preferences and getting configurations
        Preferences.start();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        boolean appInitialized = appPreferences.getBoolean(App.AppKeys.APP_INITIALIZED, false);
        if (appInitialized) {
            LOG.info("Restoring configuration preferences.");
        } else {
            LOG.info("Creating new configuration preferences.");
        }

        MainWindowRecord mainWindowRecord = MainWindowRecord.make();

        BorderPane kometRoot = mainWindowRecord.root();
        KometStageController controller = mainWindowRecord.controller();

        //Loading/setting the Komet screen
        Scene kometScene = new Scene(kometRoot, 1800, 1024);
        addStylesheets(kometScene, KOMET_CSS);

        // if NOT on macOS
        if (!IS_MAC) {
            app.appMenu.generateMsWindowsMenu(kometRoot, classicKometStage);
        }

        classicKometStage.setScene(kometScene);

        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        boolean mainWindowInitialized = windowPreferences.getBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, false);
        controller.setup(windowPreferences, classicKometStage);
        classicKometStage.setTitle("Komet");

        if (!mainWindowInitialized) {
            controller.setLeftTabs(makeDefaultLeftTabs(controller.windowView()), 0);
            controller.setCenterTabs(makeDefaultCenterTabs(controller.windowView()), 0);
            controller.setRightTabs(makeDefaultRightTabs(controller.windowView()), 1);
            windowPreferences.putBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, true);
            appPreferences.putBoolean(App.AppKeys.APP_INITIALIZED, true);
        } else {
            // Restore nodes from preferences.
            windowPreferences.get(LEFT_TAB_PREFERENCES).ifPresent(leftTabPreferencesName ->
                    restoreTab(windowPreferences, leftTabPreferencesName, controller.windowView(),
                            controller::leftBorderPaneSetCenter));
            windowPreferences.get(CENTER_TAB_PREFERENCES).ifPresent(centerTabPreferencesName ->
                    restoreTab(windowPreferences, centerTabPreferencesName, controller.windowView(),
                            controller::centerBorderPaneSetCenter));
            windowPreferences.get(RIGHT_TAB_PREFERENCES).ifPresent(rightTabPreferencesName ->
                    restoreTab(windowPreferences, rightTabPreferencesName, controller.windowView(),
                            controller::rightBorderPaneSetCenter));
        }
        //Setting X and Y coordinates for location of the Komet stage
        classicKometStage.setX(controller.windowSettings().xLocationProperty().get());
        classicKometStage.setY(controller.windowSettings().yLocationProperty().get());
        classicKometStage.setHeight(controller.windowSettings().heightProperty().get());
        classicKometStage.setWidth(controller.windowSettings().widthProperty().get());
        classicKometStage.show();

        if (IS_BROWSER) {
            app.webAPI.openStageAsTab(classicKometStage);
        }

        app.appMenu.kometPreferencesStage = new KometPreferencesStage(controller.windowView().makeOverridableViewProperties());

        windowPreferences.sync();
        appPreferences.sync();

        if (createJournalViewMenuItem != null) {
            createJournalViewMenuItem.setDisable(false);
            KeyCombination newJournalKeyCombo = new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN);
            createJournalViewMenuItem.setAccelerator(newJournalKeyCombo);
            KometPreferences journalPreferences = appPreferences.node(JOURNALS);
        }
    }

    private void restoreTab(KometPreferences windowPreferences, String tabPreferenceNodeName, ObservableViewNoOverride windowView, Consumer<Node> nodeConsumer) {
        LOG.info("Restoring from: " + tabPreferenceNodeName);
        KometPreferences itemPreferences = windowPreferences.node(KOMET_NODES + tabPreferenceNodeName);
        itemPreferences.get(WindowComponent.WindowComponentKeys.FACTORY_CLASS).ifPresent(factoryClassName -> {
            try {
                Class<?> objectClass = Class.forName(factoryClassName);
                Class<? extends Annotation> annotationClass = Reconstructor.class;
                Object[] parameters = new Object[]{windowView, itemPreferences};
                WindowComponent windowComponent = (WindowComponent) Encodable.decode(objectClass, annotationClass, parameters);
                nodeConsumer.accept(windowComponent.getNode());

            } catch (Exception e) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
        });
    }

    private static ImmutableList<DetachableTab> makeDefaultLeftTabs(ObservableViewNoOverride windowView) {
        GraphNavigatorNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
        KometNode navigatorNode1 = navigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab navigatorNode1Tab = new DetachableTab(navigatorNode1);


        PatternNavigatorFactory patternNavigatorNodeFactory = new PatternNavigatorFactory();

        KometNode patternNavigatorNode2 = patternNavigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab patternNavigatorNode1Tab = new DetachableTab(patternNavigatorNode2);

        return Lists.immutable.of(navigatorNode1Tab, patternNavigatorNode1Tab);
    }

    private static ImmutableList<DetachableTab> makeDefaultCenterTabs(ObservableViewNoOverride windowView) {
        DetailsNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        KometNode detailsNode1 = detailsNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab detailsNode1Tab = new DetachableTab(detailsNode1);
        // TODO: setting up tab graphic, title, and tooltip needs to be standardized by the factory...
        detailsNode1Tab.textProperty().bind(detailsNode1.getTitle());
        detailsNode1Tab.tooltipProperty().setValue(detailsNode1.makeToolTip());

        KometNode detailsNode2 = detailsNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode2Tab = new DetachableTab(detailsNode2);

        KometNode detailsNode3 = detailsNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode3Tab = new DetachableTab(detailsNode3);

        ListNodeFactory listNodeFactory = new ListNodeFactory();
        KometNode listNode = listNodeFactory.create(windowView,
                ActivityStreams.LIST, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab listNodeNodeTab = new DetachableTab(listNode);

        TableNodeFactory tableNodeFactory = new TableNodeFactory();
        KometNode tableNode = tableNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab tableNodeTab = new DetachableTab(tableNode);

        return Lists.immutable.of(detailsNode1Tab, detailsNode2Tab, detailsNode3Tab, listNodeNodeTab, tableNodeTab);
    }

    private static ImmutableList<DetachableTab> makeDefaultRightTabs(ObservableViewNoOverride windowView) {
        SearchNodeFactory searchNodeFactory = new SearchNodeFactory();
        KometNode searchNode = searchNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab newSearchTab = new DetachableTab(searchNode);

        ProgressNodeFactory progressNodeFactory = new ProgressNodeFactory();
        KometNode kometNode = progressNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab progressTab = new DetachableTab(kometNode);

        CompletionNodeFactory completionNodeFactory = new CompletionNodeFactory();
        KometNode completionNode = completionNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab completionTab = new DetachableTab(completionNode);

        return Lists.immutable.of(newSearchTab, progressTab, completionTab);
    }
}
