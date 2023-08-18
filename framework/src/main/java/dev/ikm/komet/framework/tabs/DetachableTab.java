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
package dev.ikm.komet.framework.tabs;

import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * @author amrullah
 */
public class DetachableTab extends Tab implements WindowComponent {
    final KometNode kometNode;
    private BooleanProperty detachable = new SimpleBooleanProperty(true);

    public DetachableTab(KometNode kometNode) {
        super(kometNode.getTitle().getValue(), kometNode.getNode());
        final String longText = "long-text";
        if (kometNode.getTitle().getValue().length() > 50) {
            this.getStyleClass().add(longText);
        }
        this.kometNode = kometNode;
        setGraphic(kometNode.getTitleNode());
        textProperty().bind(kometNode.getTitle());
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 50) {
                getStyleClass().add(longText);
            } else {
                getStyleClass().remove(longText);
            }
        });
        tooltipProperty().setValue(kometNode.makeToolTip());
    }

    public KometNode getKometNode() {
        return kometNode;
    }

    public boolean isDetachable() {
        return detachable.get();
    }

    public void setDetachable(boolean detachable) {
        this.detachable.set(detachable);
    }

    public BooleanProperty detachableProperty() {
        return detachable;
    }

    @Override
    public ObservableViewNoOverride windowView() {
        return kometNode.windowView();
    }

    @Override
    public KometPreferences nodePreferences() {
        return kometNode.nodePreferences();
    }

    @Override
    public ImmutableList<WindowComponent> children() {
        return kometNode.children();
    }

    @Override
    public void saveConfiguration() {
        kometNode.saveConfiguration();
    }

    @Override
    public Node getNode() {
        return this.getContent();
    }

    @Override
    public Class factoryClass() {
        throw new UnsupportedOperationException("Should not be called. TabGroup Reconstructor handles creation of DetachableTabs");
    }


}
