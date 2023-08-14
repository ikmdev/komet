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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.ProxyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TabSpecification {
    public final ConceptFacade tabSpecification;
    public final UUID preferencesNodeName;

    public TabSpecification(ConceptFacade tabSpecification, UUID preferencesNodeName) {
        this.tabSpecification = tabSpecification;
        this.preferencesNodeName = preferencesNodeName;
    }

    public static ObservableList<TabSpecification> fromStringList(List<String> inputList) {
        ObservableList<TabSpecification> newList = FXCollections.observableArrayList();
        for (String input : inputList) {
            newList.add(fromExternalString(input));
        }
        return newList;
    }

    public static TabSpecification fromExternalString(String externalString) {
        UUID preferenceNodeName = UUID.fromString(externalString.substring(0, 36));
        // Start at 37 to skip the "|" in the external string.
        ConceptFacade conceptSpecification = ProxyFactory.fromXmlFragment(externalString.substring(37));
        return new TabSpecification(conceptSpecification, preferenceNodeName);
    }

    public static List<String> toStringList(List<TabSpecification> inputList) {
        ArrayList<String> newList = new ArrayList<>(inputList.size());
        for (TabSpecification input : inputList) {
            newList.add(input.toExternalString());
        }
        return newList;
    }

    public String toExternalString() {
        return this.preferencesNodeName.toString() + "|" + tabSpecification.toXmlFragment();
    }

    @Override
    public int hashCode() {
        return Objects.hash(tabSpecification, preferencesNodeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabSpecification that = (TabSpecification) o;
        return Objects.equals(tabSpecification, that.tabSpecification) &&
                Objects.equals(preferencesNodeName, that.preferencesNodeName);
    }

    @Override
    public String toString() {
        return "TabSpecification{" + tabSpecification +
                ", preferencesNodeName='" + preferencesNodeName + '\'' +
                '}';
    }
}
