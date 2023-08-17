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
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDetachableTab {

    @Test
    public void testDetachableTabForLongText() {
        // Given a detachable node
        // When we create KometNode that has a title longer than 50 characters (actual 55)
        ReadOnlyProperty<String> titleProperty = Mockito.mock(ReadOnlyStringProperty.class);
        when(titleProperty.getValue()).thenReturn("NRCeO8Bi5ZSSqYM0Y3XiTWBosbrMjfiWVLDqqby8htPDleBW1njm8KS");

        KometNode kometNode = Mockito.mock(KometNode.class);
        when(kometNode.getTitle()).thenReturn(titleProperty);
        DetachableTab detachableTab = new DetachableTab(kometNode);

        // Then the style for that kometnode should contain style : 'long-text'
        assert detachableTab.getStyleClass().contains("long-text");
    }

    @Test
    public void testDetachableTabForShortText() {
        // Given a detachable node,
        // When kometnode has a title shorter than 50 characters (actual 25)
        ReadOnlyProperty<String> titleProperty = Mockito.mock(ReadOnlyStringProperty.class);
        when(titleProperty.getValue()).thenReturn("S8014oWaSJR7deST0jATGDmkg");

        KometNode kometNode = Mockito.mock(KometNode.class);
        when(kometNode.getTitle()).thenReturn(titleProperty);
        DetachableTab detachableTab = new DetachableTab(kometNode);

        // Then the style for that KometNode should remove style : 'long-text'
        assert !detachableTab.getStyleClass().contains("long-text");
    }
}
