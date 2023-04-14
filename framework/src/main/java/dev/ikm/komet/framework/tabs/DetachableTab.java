/*
 * Copyright (C) 2013 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package dev.ikm.komet.framework.tabs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.preferences.KometPreferences;

/**
 * @author amrullah
 */
public class DetachableTab extends Tab implements WindowComponent {
    final KometNode kometNode;
    private BooleanProperty detachable = new SimpleBooleanProperty(true);

    public DetachableTab(KometNode kometNode) {
        super(kometNode.getTitle().getValue(), kometNode.getNode());
        this.kometNode = kometNode;
        setGraphic(kometNode.getTitleNode());
        textProperty().bind(kometNode.getTitle());
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