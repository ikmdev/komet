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
package dev.ikm.komet.framework.panel.pattern;

import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservableFieldDefinition;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.panel.ComponentVersionIsFinalPanel;
import dev.ikm.komet.framework.propsheet.FieldDefinitionRecord;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.text.NaturalOrder;

public class PatternVersionPanel extends ComponentVersionIsFinalPanel<ObservablePatternVersion> {

    public static final String SEMANTIC_INFO = "Semantic info";

    public PatternVersionPanel(ObservablePatternVersion version, ViewProperties viewProperties) {
        super(version, viewProperties);
    }

    @Override
    protected Node makeCenterNode(ObservablePatternVersion version, ViewProperties viewProperties) {
        KometPropertySheet propertySheet = new KometPropertySheet(viewProperties);
        propertySheet.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, version.uncommitted());
        // Referenced component meaning:
        FieldDefinitionRecord semanticMeaningField = new FieldDefinitionRecord(version.meaningProperty(),
                "The meaning of a semantic with this pattern",
                "Semantic meaning", version);
        propertySheet.getItems().add(SheetItem.make(semanticMeaningField, SEMANTIC_INFO, viewProperties));
        // Referenced component purpose:
        FieldDefinitionRecord semanticPurposeField = new FieldDefinitionRecord(version.purposeProperty(),
                "The purpose of a semantic with this pattern",
                "Semantic purpose ", version);
        propertySheet.getItems().add(SheetItem.make(semanticPurposeField, SEMANTIC_INFO, viewProperties));
        // Add the field definitions.

        int i = 1;
        for (ObservableFieldDefinition fieldDef : version.fieldDefinitions()) {
            String categoryName = "Field " + i + ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(fieldDef.meaning());

            FieldDefinitionRecord fieldDataTypeField = new FieldDefinitionRecord(fieldDef.dataTypeProperty(),
                    "Specify the data type of this field for semantics of this pattern",
                    "Data type", version);
            propertySheet.getItems().add(SheetItem.make(fieldDataTypeField, categoryName, viewProperties));
            FieldDefinitionRecord fieldPurposeField = new FieldDefinitionRecord(fieldDef.purposeProperty(),
                    "Specify the purpose of this field for semantics of this pattern",
                    "Purpose", version);
            propertySheet.getItems().add(SheetItem.make(fieldPurposeField, categoryName, viewProperties));
            FieldDefinitionRecord fieldMeaningField = new FieldDefinitionRecord(fieldDef.meaningProperty(),
                    "Specify the meaning of this field for semantics of this pattern",
                    "Meaning", version);
            propertySheet.getItems().add(SheetItem.make(fieldMeaningField, categoryName, viewProperties));
            i++;
        }
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setCategoryComparator((o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            }
            if (o1.equals(SEMANTIC_INFO)) {
                return -1;
            }
            if (o2.equals(SEMANTIC_INFO)) {
                return 1;
            }
            return NaturalOrder.compareStrings(o1, o2);
        });
        return propertySheet;
    }
}