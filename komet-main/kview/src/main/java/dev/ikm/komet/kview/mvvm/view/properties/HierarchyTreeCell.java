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
package dev.ikm.komet.kview.mvvm.view.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HierarchyTreeCell extends TreeCell<ConceptTreeItemRecord> {
    private static PseudoClass ADDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("added");
    private static PseudoClass RETIRED_PSEUDO_CLASS = PseudoClass.getPseudoClass("retired");
    private static PseudoClass EDITED_PSEUDO_CLASS = PseudoClass.getPseudoClass("edited");
    private static PseudoClass MAIN_CONCEPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("concept");
    private BooleanProperty added;
    private BooleanProperty retired;
    private BooleanProperty edited;
    private BooleanProperty concept;

    public HierarchyTreeCell() {
        concept = new SimpleBooleanProperty(false);
        concept.addListener(e -> pseudoClassStateChanged(MAIN_CONCEPT_PSEUDO_CLASS, concept.get()));
        added = new SimpleBooleanProperty(false);
        added.addListener(e -> pseudoClassStateChanged(ADDED_PSEUDO_CLASS, added.get()));
        retired = new SimpleBooleanProperty(false);
        retired.addListener(e -> pseudoClassStateChanged(RETIRED_PSEUDO_CLASS, retired.get()));
        edited = new SimpleBooleanProperty(false);
        edited.addListener(e -> pseudoClassStateChanged(EDITED_PSEUDO_CLASS, edited.get()));

        this.getStyleClass().add("hierarchy-tree-cell");
    }
    public void clearAllStates() {
        setConcept(false);
        setAdded(false);
        setEdited(false);
        setRetired(false);
    }

    public boolean isConcept() {
        return concept.get();
    }

    public BooleanProperty conceptProperty() {
        return concept;
    }

    public void setConcept(boolean concept) {
        this.concept.set(concept);
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

    @Override
    protected void updateItem(ConceptTreeItemRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setText(null);
            setGraphic(createTreeCellGraphic(item.conceptTitle(), item.date(), item.transaction(), item.states()));
            clearAllStates();
            if (item.states() != null && item.states().length > 0) {
                for (int i = 0; i < item.states().length; i++) {
                    String state = item.states()[i];
                    switch (state){
                        case "added" -> setAdded(true);
                        case "edited" -> setEdited(true);
                        case "retired" -> setRetired(true);
                        case "concept" -> setConcept(true);
                    }
                }
            }
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    /**
     *  [disclosure] [icon] [concept text] [date badge]
     * @param conceptTitle
     * @param date
     * @return
     */
    private HBox createTreeCellGraphic(String conceptTitle, String date, String transaction, String ...states) {

        HBox treeItemDisplay = new HBox();
        treeItemDisplay.setAlignment(Pos.CENTER_LEFT);
        treeItemDisplay.setMaxWidth(Double.MAX_VALUE);
        treeItemDisplay.setPrefWidth(200.0);
        treeItemDisplay.setSpacing(5.0);
        treeItemDisplay.getStyleClass().add("node-cell");
        HBox.setHgrow(treeItemDisplay, Priority.ALWAYS);

        // Concept circle
        Region icon = new Region();
        HBox.setMargin(icon, new Insets(0,0,3, 0));
        icon.getStyleClass().addAll("icon", "circle");
        treeItemDisplay.getChildren().add(icon);

        // Label
        Label conceptTitleLabel = new Label(conceptTitle);
        conceptTitleLabel.setPrefWidth(255.0);
        conceptTitleLabel.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(conceptTitleLabel, new Tooltip(conceptTitle));
        conceptTitleLabel.getStyleClass().add("title-text");
        HBox.setHgrow(conceptTitleLabel, Priority.ALWAYS);
        treeItemDisplay.getChildren().add(conceptTitleLabel);

        // Transaction badge
//        Label transactionLabel = new Label(transaction);
//        if (states != null && states.length > 0) {
//            transactionLabel.getStyleClass().addAll(states);
//        }
//        transactionLabel.setPrefSize(16, 58);
//        HBox.setHgrow(transactionLabel, Priority.ALWAYS);
//        treeItemDisplay.getChildren().add(transactionLabel);

        // Date badge
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().addAll("date-text");
        if (states != null && states.length > 0) {
            dateLabel.getStyleClass().addAll(states);
        }
        dateLabel.setPrefSize(16, 58);
        HBox.setHgrow(dateLabel, Priority.ALWAYS);
        treeItemDisplay.getChildren().add(dateLabel);


        return treeItemDisplay;
    }
}
