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
package dev.ikm.komet.framework;


import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.coordinate.logic.calculator.LogicCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorDelegate;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.KometNode.PreferenceKey.ACTIVITY_STREAM_OPTION_KEY;

public abstract class ExplorationNodeAbstract implements KometNode, Subscriber<ImmutableList<EntityFacade>>, ViewCalculatorDelegate {

    /**
     * The key for the current activity stream that this node is associated with.
     */
    protected final SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty = new SimpleObjectProperty<>();
    /**
     * The activity stream option (PUBLISH, SUBSCRIBE, SYNCHRONIZE) associated with the current activity stream. .
     */
    protected final SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty = new SimpleObjectProperty<>();
    protected final SimpleStringProperty titleProperty = new SimpleStringProperty(getDefaultTitle());
    protected final SimpleStringProperty toolTipTextProperty = new SimpleStringProperty("");
    protected final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(getMenuIconGraphic());
    protected final ViewProperties viewProperties;
    protected final KometPreferences nodePreferences;
    protected HBox titleNode = new HBox(2);
    private Runnable nodeSelectionMethod = () -> {
    }; // default to an empty operation.

    {
        titleNode.alignmentProperty().setValue(Pos.CENTER);
        titleNode.getChildren().add(getMenuIconGraphic());
        activityStreamKeyProperty.addListener((observable, oldActivityStreamKey, newActivityStreamKey) -> {
            titleNode.getChildren().clear();
            titleNode.getChildren().add(getMenuIconGraphic());
            if (newActivityStreamKey != null) {
                if (showActivityStreamIcon()) {
                    if (!ActivityStreams.UNLINKED.equals(newActivityStreamKey)) {
                        ActivityStream activityStream = ActivityStreams.get(newActivityStreamKey);
                        titleNode.getChildren().add(activityStream.getStreamIcon());
                    }
                }
            }
        });
    }

    public ExplorationNodeAbstract(ViewProperties viewProperties, KometPreferences nodePreferences) {
        this.viewProperties = viewProperties;
        this.nodePreferences = nodePreferences;
        PublicIdStringKey<ActivityStream> activityStreamKey = this.nodePreferences.getObject(
                KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, ActivityStreams.UNLINKED);
        this.activityStreamKeyProperty.setValue(activityStreamKey);

        PublicIdStringKey<ActivityStreamOption> activityStreamOptionKey = this.nodePreferences.getObject(
                ACTIVITY_STREAM_OPTION_KEY, ActivityStreamOption.PUBLISH.keyForOption());
        this.optionForActivityStreamKeyProperty.setValue(activityStreamOptionKey);

        updateActivityStream(null, activityStreamKey);

        this.optionForActivityStreamKeyProperty.addListener((observable, oldValue, newValue) -> {
            PublicIdStringKey<ActivityStream> currentActivityStreamKey = activityStreamKeyProperty.get();
            updateActivityStream(currentActivityStreamKey, currentActivityStreamKey);
        });
        this.activityStreamKeyProperty.addListener((observable, oldValue, newValue) -> {
            updateActivityStream(oldValue, newValue);
        });

        // TODO the title label...
        //this.titleLabel = new EntityLabelWithDragAndDrop();
    }

    protected void updateActivityStream(PublicIdStringKey<ActivityStream> oldValue,
                                        PublicIdStringKey<ActivityStream> newValue) {

        if (oldValue != null) {
            ActivityStream activityStream = ActivityStreams.get(oldValue);
            if (activityStream != null) {
                activityStream.removeSubscriber(this);
            }
        }
        if (newValue != null) {
            ActivityStream activityStream = ActivityStreams.get(newValue);
            if (activityStream != null) {
                activityStream.removeSubscriber(this);
            }
        }

        if (this.optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.SUBSCRIBE.keyForOption()) ||
                this.optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.SYNCHRONIZE.keyForOption())) {
            this.getActivityStream().addSubscriberWithWeakReference(this);
        }

        if (this.optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.PUBLISH.keyForOption()) ||
                this.optionForActivityStreamKeyProperty.get().equals(ActivityStreamOption.SYNCHRONIZE.keyForOption())) {
            // Dispatch is handled dynamically, no need for static setup.
        }
    }

    public ExplorationNodeAbstract() {
        this.viewProperties = null;
        this.nodePreferences = null;
    }

    @Override
    public final ViewCalculator viewCalculator() {
        return viewProperties.calculator();
    }

    @Override
    public LogicCalculator logicCalculator() {
        return viewProperties.calculator();
    }

    @Override
    public NavigationCalculator navigationCalculator() {
        return viewProperties.calculator();
    }

    @Override
    public ViewCoordinateRecord viewCoordinateRecord() {
        return viewProperties.nodeView().getOriginalValue();
    }

    /**
     * Subclasses can override if it does not want activity stream icon shown in title node.
     *
     * @return
     */
    protected boolean showActivityStreamIcon() {
        return true;
    }

    public abstract String getDefaultTitle();


    @Override
    public void onNext(ImmutableList<EntityFacade> items) {
        Platform.runLater(() -> handleActivity(items));
    }

    public abstract void handleActivity(ImmutableList<EntityFacade> entities);

    public void dispatchActivity(ImmutableList<EntityFacade> entities) {
        getActivityStream().dispatch(entities);
    }

    protected final Runnable getNodeSelectionMethod() {
        return nodeSelectionMethod;
    }

    @Override
    public final void setNodeSelectionMethod(Runnable nodeSelectionMethod) {
        this.nodeSelectionMethod = nodeSelectionMethod;
    }

    @Override
    public final void savePreferences() {
        nodePreferences.put(WindowComponentKeys.INITIALIZED, "true");
        nodePreferences.put(WindowComponentKeys.FACTORY_CLASS, factoryClass().getName());
        nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, this.activityStreamKeyProperty.get());
        nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_OPTION_KEY, this.optionForActivityStreamKeyProperty.get());

        saveAdditionalPreferences();
        try {
            nodePreferences.sync();
        } catch (BackingStoreException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
    }

    @Override
    public final void revertPreferences() {
        this.activityStreamKeyProperty.set(nodePreferences.getObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY,
                ActivityStreams.UNLINKED));
        this.optionForActivityStreamKeyProperty.set(nodePreferences.getObject(KometNode.PreferenceKey.ACTIVITY_STREAM_OPTION_KEY,
                ActivityStreamOption.PUBLISH.keyForOption()));
        revertAdditionalPreferences();
    }

    public abstract void revertAdditionalPreferences();

    @Override
    public Node getMenuIconGraphic() {
        Label menuIcon = new Label("", new FontIcon());
        menuIcon.setId(getStyleId());
        return menuIcon;
    }

    public abstract String getStyleId();

    @Override
    public KometPreferences getNodePreferences() {
        return nodePreferences;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public final StringPropertyBase getTitle() {
        return this.titleProperty;
    }

    @Override
    public Node getTitleNode() {
        return titleNode;
    }

    @Override
    public final ReadOnlyProperty<String> toolTipTextProperty() {
        return this.toolTipTextProperty;
    }

    @Override
    public Tooltip makeToolTip() {
        Tooltip tooltip = new Tooltip(toolTipTextProperty().getValue());
        tooltip.textProperty().bind(toolTipTextProperty());
        return tooltip;
    }

    @Override
    public final ViewProperties getViewProperties() {
        return this.viewProperties;
    }

    @Override
    public final ActivityStream getActivityStream() {
        return ActivityStreams.get(activityStreamKeyProperty.get());
    }

    @Override
    public SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty() {
        return optionForActivityStreamKeyProperty;
    }

    @Override
    public final SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty() {
        return activityStreamKeyProperty;
    }

    @Override
    public final Scene getScene() {
        return getNode().getScene();
    }

    @Override
    public final SimpleObjectProperty<Node> getMenuIconProperty() {
        return menuIconProperty;
    }

    @Override
    public ObservableViewNoOverride windowView() {
        return viewProperties.parentView();
    }

    @Override
    public KometPreferences nodePreferences() {
        return nodePreferences;
    }

    @Override
    public ImmutableList<WindowComponent> children() {
        return Lists.immutable.empty();
    }

    @Override
    public final void saveConfiguration() {
        savePreferences();
    }

    protected abstract void saveAdditionalPreferences();
}
