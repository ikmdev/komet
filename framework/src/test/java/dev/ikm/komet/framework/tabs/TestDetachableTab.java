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
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDetachableTab {
    DetachableTab detachableTab;
    SimpleStringProperty titleProperty = new SimpleStringProperty();

    @BeforeEach
    public void setUp() {
        titleProperty.set("NRCeO8Bi5ZSSqYM0Y3XiTWBosbrMjfiWVLDqqby8htPDleBW1njm8KS");

        KometNode kometNode = new StubKometNode(titleProperty);
        detachableTab = new DetachableTab(kometNode);
        detachableTab.textProperty().unbind();
    }

    @Test
    public void testDetachableTabForLongText() {
        assert detachableTab.getStyleClass().contains("long-text");
    }

    @Test
    public void testDetachableTabForShortText() {
        titleProperty.set("Dummy dummy dummy dummy");
        detachableTab.textProperty().setValue("Dummy dummy dummy dummy");
        assert !detachableTab.getStyleClass().contains("long-text");
    }

    @Test
    public void testForRenderingShortText() {
        titleProperty.set("Dummy dummy dummy dummy");
        detachableTab.textProperty().setValue("Dummy dummy dummy dummy");
        assert !detachableTab.getStyleClass().contains("long-text");
    }

    @Test
    public void testForRenderingLongText() {
        titleProperty.set(
                "Dummy dummy dummy dummy dummy dummy dummy  dummy dummy dummy dummy dummy dummy dummy");
        detachableTab.textProperty().setValue(
                "Dummy dummy dummy dummy dummy dummy dummy  dummy dummy dummy dummy dummy dummy dummy");
        assert detachableTab.getStyleClass().contains("long-text");
    }

    @Test
    public void testIsDetachable() {
        assertTrue(detachableTab.isDetachable());
    }

    @Test
    public void testGetKometNode() {
        assertNotNull(detachableTab.getKometNode());
    }

    @Test
    public void testSetDetachable() {
        detachableTab.setDetachable(false);
        assertFalse(detachableTab.isDetachable());
    }

    @Test
    public void testDetachableProperty() {
        assertTrue(detachableTab.detachableProperty().get());
    }

    @Test
    public void testNodePreferences() {
        assertNull(detachableTab.nodePreferences());
    }

    @Test
    public void testSaveConfiguration() {
        detachableTab.saveConfiguration();
        assertNotNull(detachableTab);
    }

    @Test
    public void testGetNode() {
        assertNull(detachableTab.getNode());
    }

    /**
     * Minimal stub providing only what DetachableTab's constructor needs.
     */
    private static class StubKometNode implements KometNode {
        private final SimpleStringProperty title;

        StubKometNode(SimpleStringProperty title) {
            this.title = title;
        }

        @Override public ReadOnlyProperty<String> getTitle() { return title; }
        @Override public Node getTitleNode() { return null; }
        @Override public ReadOnlyProperty<String> toolTipTextProperty() { return new SimpleStringProperty(); }
        @Override public Tooltip makeToolTip() { return null; }
        @Override public ViewProperties getViewProperties() { return null; }
        @Override public ActivityStream getActivityStream() { return null; }
        @Override public SimpleObjectProperty<PublicIdStringKey<ActivityStreamOption>> optionForActivityStreamKeyProperty() { return null; }
        @Override public SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty() { return null; }
        @Override public Node getNode() { return null; }
        @Override public ObjectProperty<Node> getMenuIconProperty() { return null; }
        @Override public void close() { }
        @Override public void setNodeSelectionMethod(Runnable nodeSelectionMethod) { }
        @Override public void savePreferences() { }
        @Override public void revertPreferences() { }
        @Override public Node getMenuIconGraphic() { return null; }
        @Override public KometPreferences getNodePreferences() { return null; }
        @Override public ObservableViewNoOverride windowView() { return null; }
        @Override public KometPreferences nodePreferences() { return null; }
        @Override public ImmutableList children() { return null; }
        @Override public void saveConfiguration() { }
        @Override public Class factoryClass() { return null; }
    }
}
