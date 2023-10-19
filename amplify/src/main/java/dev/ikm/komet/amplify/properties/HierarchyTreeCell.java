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
package dev.ikm.komet.amplify.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;

public class HierarchyTreeCell extends TreeCell {
    private static PseudoClass ADDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("added");
    private static PseudoClass RETIRED_PSEUDO_CLASS = PseudoClass.getPseudoClass("retired");
    private static PseudoClass EDITED_PSEUDO_CLASS = PseudoClass.getPseudoClass("edited");
    private BooleanProperty added;
    private BooleanProperty retired;
    private BooleanProperty edited;

    public HierarchyTreeCell() {
        added = new SimpleBooleanProperty(false);
        added.addListener(e -> pseudoClassStateChanged(ADDED_PSEUDO_CLASS, added.get()));
        retired = new SimpleBooleanProperty(false);
        retired.addListener(e -> pseudoClassStateChanged(RETIRED_PSEUDO_CLASS, added.get()));
        edited = new SimpleBooleanProperty(false);
        edited.addListener(e -> pseudoClassStateChanged(EDITED_PSEUDO_CLASS, added.get()));

        this.getStyleClass().add("hierarchy-tree-cell");
    }

    public boolean isAdded() {
        return added.get();
    }

    public BooleanProperty addedProperty() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added.set(added);
    }

    public boolean isRetired() {
        return retired.get();
    }

    public BooleanProperty retiredProperty() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired.set(retired);
    }

    public boolean isEdited() {
        return edited.get();
    }

    public BooleanProperty editedProperty() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited.set(edited);
    }
}
