package dev.ikm.komet.framework.preferences;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;


public class KometPreferencesStage implements ListChangeListener<TreeItem> {
    private static final Logger LOG = LoggerFactory.getLogger(KometPreferencesStage.class);

    private final ObservableList<ConfigurationPreference> configurationPreferences = FXCollections.observableArrayList();
    private final ObservableList<UserPreferenceItems> userPreferenceItems = FXCollections.observableArrayList();

    private RootPreferences rootPreferences;

    private KometPreferencesController kpc;
    private Stage preferencesStage;
    private ViewProperties viewProperties;

    public KometPreferencesStage(ViewProperties viewProperties) {
        //TODO get proper view...
        this.viewProperties = viewProperties;
    }

    public void updatePreferencesTitle(UUID preference, String title) {
        TreeItem<PreferenceGroup> root = kpc.getPreferenceTree().getRoot();
        recursiveUpdate(root, preference, title);
    }

    private void recursiveUpdate(TreeItem<PreferenceGroup> treeItem, UUID preference, String title) {
        if (treeItem.getValue().getTreeItem().getPreferences().name().equals(preference.toString())) {
            treeItem.getValue().groupNameProperty().set(title);
        } else {
            for (TreeItem<PreferenceGroup> child : treeItem.getChildren()) {
                recursiveUpdate(child, preference, title);
            }
        }
    }

    public void resetUserPreferences() {
        try {
            KometPreferences userPreferences = PreferencesService.userPreferences();
            clearNodeAndChildren(userPreferences);
        } catch (BackingStoreException ex) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(ex));
        }
    }

    private void clearNodeAndChildren(KometPreferences node) throws BackingStoreException {
        for (KometPreferences child : node.children()) {
            clearNodeAndChildren(child);
        }
        node.clear();
        node.sync();
    }

    public void loadPreferences() {
        KometPreferences preferences = PreferencesService.configurationPreferences();
        //TODO get proper view...
        setupPreferencesController(viewProperties, preferences);
    }

    private void setupPreferencesController(ViewProperties viewProperties, KometPreferences preferences) {
        if (kpc == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/ikm/komet/framework/preferences/KometPreferences.fxml"));
                Parent root = loader.load();
                this.kpc = loader.getController();
                this.kpc.setViewProperties(viewProperties);
                Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, viewProperties, kpc);
                setupRoot(treeRoot);

                root.setId(UUID.randomUUID()
                        .toString());

                this.preferencesStage = new Stage();
                this.preferencesStage.setTitle("TODO" + " preferences");
//                FxGet.configurationNameProperty().addListener((observable, oldValue, newValue) -> {
//                    this.preferencesStage.setTitle(newValue + " preferences");
//                });
                Scene scene = new Scene(root);

                this.preferencesStage.setScene(scene);
                //TODO get the stylesheets right...
//                scene.getStylesheets()
//                        .add(FxGet.fxConfiguration().getUserCSSURL().toString());
//                scene.getStylesheets()
//                        .add(IconographyHelper.getStyleSheetStringUrl());
            } catch (IOException ex) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(ex));
            }
        }
    }

    private void setupRoot(Optional<PreferencesTreeItem> treeRoot) {
        configurationPreferences.clear();
        userPreferenceItems.clear();
        if (treeRoot.isPresent()) {
            PreferencesTreeItem rootTreeItem = treeRoot.get();
            this.rootPreferences = (RootPreferences) rootTreeItem.getValue();
            recursiveProcess(rootTreeItem);
            this.kpc.setRoot(treeRoot.get());
        }
    }

    private void recursiveProcess(PreferencesTreeItem treeItem) {
        treeItem.getChildren().removeListener(this);
        treeItem.getChildren().addListener(this);
        addPreferenceItem(treeItem.getValue());
        for (TreeItem<PreferenceGroup> child : treeItem.getChildren()) {
            recursiveProcess((PreferencesTreeItem) child);
        }
    }

    public void reloadPreferences() {
        PreferencesService.get().reloadConfigurationPreferences();

        KometPreferences preferences = PreferencesService.configurationPreferences();
        Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, viewProperties, kpc);
        setupRoot(treeRoot);
    }

    public void onChanged(Change<? extends TreeItem> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //permutate
                }
            } else if (c.wasUpdated()) {
                //update item
            } else {
                for (TreeItem remitem : c.getRemoved()) {
                    remitem.getChildren().removeListener(this);
                    removePreferenceItem(remitem.getValue());
                }
                for (TreeItem additem : c.getAddedSubList()) {
                    additem.getChildren().removeListener(this);
                    additem.getChildren().addListener(this);
                    addPreferenceItem(additem.getValue());
                }
            }
        }

    }

    private void removePreferenceItem(Object item) {
        if (item instanceof ConfigurationPreference) {
            configurationPreferences.remove(item);
        } else if (item instanceof UserPreferenceItems) {
            userPreferenceItems.remove(item);
        }
    }

    private void addPreferenceItem(Object item) {
        if (item instanceof ConfigurationPreference) {
            configurationPreferences.add((ConfigurationPreference) item);
        } else if (item instanceof UserPreferenceItems) {
            userPreferenceItems.add((UserPreferenceItems) item);
        }

    }

    public Stage showPreferences() {
        KometPreferences preferences = PreferencesService.configurationPreferences();
        setupPreferencesController(viewProperties, preferences);
        preferencesStage.show();
        preferencesStage.setAlwaysOnTop(true);
        return preferencesStage;
    }

    public void closePreferences() {
        this.preferencesStage.close();
    }

}


