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
package dev.ikm.komet.framework.window;

import javafx.beans.property.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

public class WindowSettings {
    private static final Logger LOG = LoggerFactory.getLogger(WindowSettings.class);

    private static final HashSet<String> windowIds = new HashSet<>();


    private final SimpleBooleanProperty enableLeftPaneProperty = new SimpleBooleanProperty(this, TinkarTerm.ENABLE_LEFT_PANE.toXmlFragment(), true);
    private final SimpleBooleanProperty enableCenterPaneProperty = new SimpleBooleanProperty(this, TinkarTerm.ENABLE_CENTER_PANE.toXmlFragment(), true);
    private final SimpleBooleanProperty enableRightPaneProperty = new SimpleBooleanProperty(this, TinkarTerm.ENABLE_RIGHT_PANE.toXmlFragment(), true);

    private final SimpleStringProperty windowNameProperty
            = new SimpleStringProperty(this, TinkarTerm.WINDOW_CONFIGURATION_NAME.toXmlFragment());

    private final SimpleStringProperty leftTabPreferencesProperty =
            new SimpleStringProperty(this, TinkarTerm.LEFT_PANE_DAFAULTS.toXmlFragment());

    private final SimpleStringProperty centerTabPreferencesProperty =
            new SimpleStringProperty(this, TinkarTerm.CENTER_PANE_DEFAULTS.toXmlFragment());

    private final SimpleStringProperty rightTabPreferencesProperty =
            new SimpleStringProperty(this, TinkarTerm.RIGHT_PANE_DEFAULTS.toXmlFragment());

    private final SimpleDoubleProperty xLocationProperty =
            new SimpleDoubleProperty(this, TinkarTerm.WINDOW_X_POSITION.toXmlFragment());

    private final SimpleDoubleProperty yLocationProperty =
            new SimpleDoubleProperty(this, TinkarTerm.WINDOW_Y_POSITION.toXmlFragment());

    private final SimpleDoubleProperty heightProperty =
            new SimpleDoubleProperty(this, TinkarTerm.WINDOW_HEIGHT.toXmlFragment());

    private final SimpleDoubleProperty widthProperty =
            new SimpleDoubleProperty(this, TinkarTerm.WINDOW_WIDTH.toXmlFragment());

    private final SimpleIntegerProperty leftTabSelectionProperty = new SimpleIntegerProperty(this, "left tab selection", 0);
    private final SimpleIntegerProperty centerTabSelectionProperty = new SimpleIntegerProperty(this, "center tab selection", 0);
    private final SimpleIntegerProperty rightTabSelectionProperty = new SimpleIntegerProperty(this, "right tab selection", 0);
    private final BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);

    private final SimpleObjectProperty<double[]> dividerPositionsProperty = new SimpleObjectProperty<>(this, "divider positions", new double[]{0.2504, 0.7504});
    private final ObservableViewNoOverride observableViewForWindow = new ObservableViewNoOverride(Coordinates.View.DefaultView());
    private final KometPreferences preferencesNode;
    private final BooleanProperty initialized = new SimpleBooleanProperty(this, Keys.INITIALIZED.toString());

    /**
     * This constructor is called by reflection when reconstituting a window from the preferences.
     *
     * @param preferencesNode
     */
    public WindowSettings(KometPreferences preferencesNode) {
        this.preferencesNode = preferencesNode;
        WindowSettings.windowIds.add(preferencesNode.name());
        if (this.preferencesNode.hasKey(Keys.INITIALIZED)) {
            revertFields();
        } else {
            // Set defaults.
            this.windowNameProperty.set(preferencesNode.get(Keys.WINDOW_NAME, "New window 1"));
            setDefaultLocationAndSize();
        }

        windowNameProperty.addListener((observable, oldValue, newValue) -> {
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    String uuidStr = (String) stage.getScene().getProperties().get(Keys.WINDOW_UUID_STR);
                    if (preferencesNode.name().equals(uuidStr)) {
                        stage.setTitle(newValue + ": " + PrimitiveData.get().name());
                    }
                }
            }
        });
    }

    protected void revertFields() {
        setDefaultLocationAndSize();
        this.observableViewForWindow.setValue(preferencesNode.getObject(Keys.VIEW_COORDINATE_FOR_WINDOW, Coordinates.View.DefaultView()));

        this.windowNameProperty.set(this.preferencesNode.get(Keys.WINDOW_NAME, windowNameProperty.getValue()));
        this.enableLeftPaneProperty.set(this.preferencesNode.getBoolean(Keys.ENABLE_LEFT_PANE, true));
        this.enableCenterPaneProperty.set(this.preferencesNode.getBoolean(Keys.ENABLE_CENTER_PANE, true));
        this.enableRightPaneProperty.set(this.preferencesNode.getBoolean(Keys.ENABLE_RIGHT_PANE, true));

        this.leftTabPreferencesProperty.set(this.preferencesNode.get(Keys.LEFT_TAB_PREFERENCES, null));

        this.centerTabPreferencesProperty.set(this.preferencesNode.get(Keys.CENTER_TAB_PREFERENCES, null));

        this.rightTabPreferencesProperty.set(this.preferencesNode.get(Keys.RIGHT_TAB_PREFERENCES, null));

        this.leftTabSelectionProperty.set(this.preferencesNode.getInt(Keys.LEFT_TAB_SELECTION, leftTabSelectionProperty.get()));
        this.centerTabSelectionProperty.set(this.preferencesNode.getInt(Keys.CENTER_TAB_SELECTION, centerTabSelectionProperty.get()));
        this.rightTabSelectionProperty.set(this.preferencesNode.getInt(Keys.RIGHT_TAB_SELECTION, rightTabSelectionProperty.get()));

        this.dividerPositionsProperty.set(this.preferencesNode.getDoubleArray(Keys.DIVIDER_POSITIONS, this.dividerPositionsProperty.get()));
    }

    private void setDefaultLocationAndSize() {
        this.xLocationProperty.setValue(this.preferencesNode.getDouble(Keys.X_LOC, 40.0));
        this.yLocationProperty.setValue(this.preferencesNode.getDouble(Keys.Y_LOC, 40.0));
        this.heightProperty.setValue(this.preferencesNode.getDouble(Keys.HEIGHT, 1024));
        this.widthProperty.setValue(this.preferencesNode.getDouble(Keys.WIDTH, 1800));
        this.dividerPositionsProperty.setValue(new double[]{0.2504, 0.7504});
    }

    public static String getWindowName(String prefix, String nodeName) {
        windowIds.add(nodeName);
        if (windowIds.size() > 1) {
            int foundCount = 0;
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle() != null && stage.getTitle().startsWith(prefix)) {
                        foundCount++;
                    }
                }
            }
            if (foundCount > 0) {
                return prefix + " " + foundCount;
            }
        }
        return prefix;
    }

    public final void save() {
        try {
            initialized.set(true);
            preferencesNode.putBoolean(Keys.INITIALIZED, initialized.get());
            preferencesNode.putEnum(preferencesNode.getNodeType());
            saveFields();
            preferencesNode.sync();
            preferencesNode.flush();
            LOG.info("Saved window settings: (" + xLocationProperty.get() + ", " + yLocationProperty.get() + ")");
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void saveFields() {
        saveLocationAndFocus();
        this.preferencesNode.putObject(Keys.VIEW_COORDINATE_FOR_WINDOW, getView().getValue());
        this.preferencesNode.put(Keys.LEFT_TAB_PREFERENCES, leftTabPreferencesProperty.getValue());
        this.preferencesNode.put(Keys.CENTER_TAB_PREFERENCES, centerTabPreferencesProperty.getValue());
        this.preferencesNode.put(Keys.RIGHT_TAB_PREFERENCES, rightTabPreferencesProperty.getValue());
        this.preferencesNode.putBoolean(Keys.ENABLE_LEFT_PANE, this.enableLeftPaneProperty.get());
        this.preferencesNode.putBoolean(Keys.ENABLE_CENTER_PANE, this.enableCenterPaneProperty.get());
        this.preferencesNode.putBoolean(Keys.ENABLE_RIGHT_PANE, this.enableRightPaneProperty.get());
    }

    public void saveLocationAndFocus() {
        this.preferencesNode.putDouble(Keys.X_LOC, this.xLocationProperty.doubleValue());
        this.preferencesNode.putDouble(Keys.Y_LOC, this.yLocationProperty.doubleValue());
        this.preferencesNode.putDouble(Keys.HEIGHT, this.heightProperty.doubleValue());
        this.preferencesNode.putDouble(Keys.WIDTH, this.widthProperty.doubleValue());
        this.preferencesNode.putInt(Keys.LEFT_TAB_SELECTION, leftTabSelectionProperty.get());
        this.preferencesNode.putInt(Keys.CENTER_TAB_SELECTION, centerTabSelectionProperty.get());
        this.preferencesNode.putInt(Keys.RIGHT_TAB_SELECTION, rightTabSelectionProperty.get());
        this.preferencesNode.putDoubleArray(Keys.DIVIDER_POSITIONS, dividerPositionsProperty.get());
        try {
            this.preferencesNode.sync();
        } catch (BackingStoreException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
    }

    public ObservableViewNoOverride getView() {
        return this.observableViewForWindow;
    }

    public UUID getWindowUuid() {
        return UUID.fromString(this.preferencesNode.name());
    }


    public SimpleObjectProperty<double[]> dividerPositionsProperty() {
        return dividerPositionsProperty;
    }

    public boolean showDelete() {
        return true;
    }

    public StringProperty getWindowName() {
        return windowNameProperty;
    }

    public SimpleDoubleProperty xLocationProperty() {
        return xLocationProperty;
    }

    public SimpleDoubleProperty yLocationProperty() {
        return yLocationProperty;
    }

    public SimpleDoubleProperty heightProperty() {
        return heightProperty;
    }

    public SimpleDoubleProperty widthProperty() {
        return widthProperty;
    }

    public SimpleBooleanProperty enableLeftPaneProperty() {
        return this.enableLeftPaneProperty;
    }

    public SimpleBooleanProperty enableCenterPaneProperty() {
        return this.enableCenterPaneProperty;
    }

    public SimpleBooleanProperty enableRightPaneProperty() {
        return this.enableRightPaneProperty;
    }

    public SimpleIntegerProperty leftTabSelectionProperty() {
        return this.leftTabSelectionProperty;
    }

    public SimpleIntegerProperty centerTabSelectionProperty() {
        return this.centerTabSelectionProperty;
    }

    public SimpleIntegerProperty rightTabSelectionProperty() {
        return this.rightTabSelectionProperty;
    }

    public boolean isFocusOwner() {
        return this.preferencesNode.getBoolean(Keys.IS_FOCUS_OWNER, false);
    }

    public void setFocusOwner(boolean focusOwner) {
        this.preferencesNode.putBoolean(Keys.IS_FOCUS_OWNER, focusOwner);
    }

    public boolean isPaneEnabled(int paneIndex) {
        switch (paneIndex) {
            case 0:
                return enableLeftPaneProperty.get();
            case 1:
                return enableCenterPaneProperty.get();
            case 2:
                return enableRightPaneProperty.get();
            default:
                return false;
        }
    }

    public SimpleStringProperty leftTabPreferencesProperty() {
        return leftTabPreferencesProperty;
    }

    public SimpleStringProperty centerTabPreferencesProperty() {
        return centerTabPreferencesProperty;
    }

    public SimpleStringProperty rightTabPreferencesProperty() {
        return rightTabPreferencesProperty;
    }

    private void validateChange(Object oldValue, Object newValue) {
        if (oldValue != newValue) {
            if (newValue != null) {
                if (!newValue.equals(oldValue)) {
                    changed.set(true);
                }
            } else {
                changed.set(true);
            }
        }
    }

    public enum Keys {
        INITIALIZED,
        LEFT_TAB_PREFERENCES,
        CENTER_TAB_PREFERENCES,
        RIGHT_TAB_PREFERENCES,
        X_LOC,
        Y_LOC,
        HEIGHT,
        WIDTH,
        MANIFOLD,
        /**
         * The window uuid str is the name of the preference node associated with this window...
         */
        WINDOW_UUID_STR,
        WINDOW_NAME,
        ENABLE_LEFT_PANE,
        ENABLE_CENTER_PANE,
        ENABLE_RIGHT_PANE,
        LEFT_TAB_SELECTION,
        CENTER_TAB_SELECTION,
        RIGHT_TAB_SELECTION,
        DIVIDER_POSITIONS,
        IS_FOCUS_OWNER,

        VIEW_COORDINATE_FOR_WINDOW
    }
}
