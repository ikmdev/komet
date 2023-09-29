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

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.change.FieldChangeRecord;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResultsLeafHash;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Set;
import java.util.function.Function;

import static dev.ikm.komet.amplify.commons.CssHelper.toWebColor;
import static dev.ikm.tinkar.common.util.Symbols.HEAVY_TRIANGLE_HEADED_RIGHTWARDS_ARROW;
import static dev.ikm.tinkar.terms.TinkarTerm.*;

/**
 * Displays change chronology based on an entity's versions. For example A concept was created and updated over time.
 * A JavaFX Controller associated to the view file change-list-item.fxml.
 */
public class ChangeListItemController {
    @FXML
    private Text commentValueLabel;

    @FXML
    private Region extensionVline;

    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label stampAuthorLabel;

    @FXML
    private Label stampTimeLabel;

    @FXML
    private Label transactionLabel;

    private Tooltip moduleAndPath = new Tooltip();

    /**
     * Semantic chronology will include Status but ignore Time, Author Module and Path.
     */
    static private Set<Integer> EXCLUDE_SEMANTIC_TERM_NIDS = Set.of(
            TIME_FOR_VERSION.nid(),
            AUTHOR_FOR_VERSION.nid(),
            MODULE_FOR_VERSION.nid()
    );

    /**
     * Concept chronology will include Status, Module, Path but ignore Time, Author,
     */
    static private Set<Integer> EXCLUDE_CONCEPT_TERM_NIDS = Set.of(
            TIME_FOR_VERSION.nid(),
            AUTHOR_FOR_VERSION.nid()
    );

    /**
     * Up to three colors to choose from.
     */
    public static final Color[] COLORS_FOR_EXTENSIONS = new Color[]{
            Color.web("#0085FF"), /* Blue */
            Color.web("#956AFF"), /* Purple */
            Color.web("#FF6252"), /* Red */
    };

    private Color extensionColor= COLORS_FOR_EXTENSIONS[0];

    @FXML
    public void initialize()  {
        stampTimeLabel.setTooltip(moduleAndPath);
    }
    public Color getExtensionColor() {
        return extensionColor;
    }
    public void setExtensionVLineColor(Color color) {
        extensionColor = color;
        String backgroundColor = "-fx-background-color: transparent, %s;";
        extensionVline.setStyle(backgroundColor.formatted(toWebColor(color)));
    }

    public void updateView(final ViewProperties viewProperties, final int entityNid, final VersionChangeRecord versionChangeRecord) {
        ViewCalculator viewCalculator = viewProperties.calculator();

        // Get STAMP
        StampEntity stampForChange = Entity.getStamp(versionChangeRecord.stampNid());

        // Time format
        stampTimeLabel.setText(DateTimeUtil.format(stampForChange.time()));

        // Module
        String moduleName = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampForChange.moduleNid());
        String pathName   = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampForChange.pathNid());
        moduleAndPath.setText("Module: %s \nPath: %s".formatted(moduleName, pathName));

        // Author
        stampAuthorLabel.setText(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampForChange.authorNid()));

        boolean showPriorValue = false;
        StringBuilder sb = new StringBuilder();
        Entity referencedEntity = EntityService.get().getEntityFast(entityNid);
        // Identicon
        Image identicon = Identicon.generateIdenticonImage(referencedEntity.publicId());
        identiconImageView.setImage(identicon);

        // An entity can be a Concept or Semantic (Chronology). An Axiom is subtype of Semantic.
        boolean isItAConcept = referencedEntity instanceof ConceptRecord;
        boolean isItASemantic   = referencedEntity instanceof SemanticRecord;
        boolean isItAnAxiom      = false;
        boolean newlyCreated = stampForChange.moduleNid() == PRIMORDIAL_MODULE.nid();

        // Is the field a concept as a datatype.
        Function<Integer, Boolean> isDataTypeConceptField = dataTypeNid ->
                dataTypeNid == CONCEPT_FIELD.nid() || dataTypeNid == COMPONENT_FIELD.nid();

        for (FieldChangeRecord fieldChange: versionChangeRecord.changes()) {
            // default format function
            Function<Object, String> formatFunction = value -> String.valueOf(value);

            // Field Change's current value
            FieldRecord currentFieldRecord = fieldChange.currentValue();
            FieldRecord   priorFieldRecord = fieldChange.priorValue();
            Object         priorFieldValue = priorFieldRecord.value();
            // Indicate previous value was an unitialized entity.
            newlyCreated = priorFieldValue == State.PRIMORDIAL || UNINITIALIZED_COMPONENT.equals(priorFieldValue);

            // detect if it's a field value of DiTreeEntity
            isItAnAxiom = currentFieldRecord.value() instanceof DiTreeEntity;

            // Current Field definition
            FieldDefinitionForEntity currentFieldDefinition = currentFieldRecord.fieldDefinition();
            // Current value's field definition's datatype nid
            int dataTypeNid = currentFieldDefinition.dataTypeNid();
            // Current value's field definition's meaning nid
            int meaningNid = currentFieldDefinition.meaningNid();

            // Include field definition and value, otherwise skip to be ignored when displaying.
            boolean includeFieldDefinition = !(isItAConcept && EXCLUDE_CONCEPT_TERM_NIDS.contains(meaningNid)
                    || isItASemantic && EXCLUDE_SEMANTIC_TERM_NIDS.contains(meaningNid));

            if (isDataTypeConceptField.apply(dataTypeNid)) {
                // preferred description of concept or component.
                formatFunction = value -> switch (value) {
                    case ConceptFacade conceptFacade -> viewCalculator.getPreferredDescriptionStringOrNid(conceptFacade);
                    default -> value.toString();
                };
            }

            // Output Type Field label. eg Text:
            if (includeFieldDefinition) {
                sb.append(viewCalculator.getPreferredDescriptionStringOrNid(meaningNid)).append(": ");
            }

            // Output the prior value
            if (showPriorValue) {
                sb.append(formatFunction.apply(priorFieldRecord.value())).append(" ").append(HEAVY_TRIANGLE_HEADED_RIGHTWARDS_ARROW).append(" ");
            }

            // Output the Value. e.g. Case significance: Case insensitive.
            if (includeFieldDefinition) {
                sb.append(formatFunction.apply(currentFieldRecord.value()));
                sb.append("\n");
            }

            // Output changes to Axioms (directed tree entities)
            if (currentFieldRecord.value() instanceof DiTreeEntity currentTree &&
                    fieldChange.priorValue().value() instanceof DiTreeEntity priorTree) {
                SemanticEntity theSemantic = (SemanticEntity) referencedEntity;
                IsomorphicResultsLeafHash<?> isomorphicResult = new IsomorphicResultsLeafHash(currentTree, priorTree, theSemantic.referencedComponentNid());
                try {
                    isomorphicResult.call();
                    sb.append("\n");
                    if (!isomorphicResult.getAdditionalVertexRoots().isEmpty())  {
                        sb.append("\nAdditions: \n\n");
                        isomorphicResult.getAdditionalVertexRoots().forEach((EntityVertex additionRoot) -> {
                            sb.append("  ").append(isomorphicResult.getReferenceTree().fragmentToString(additionRoot));
                            sb.append("\n");
                        });
                    }
                    if (!isomorphicResult.getDeletedVertexRoots().isEmpty()) {
                        sb.append("\nDeletions: \n\n");
                        isomorphicResult.getDeletedVertexRoots().forEach((EntityVertex deletionRoot) -> {
                            sb.append("  ").append(isomorphicResult.getComparisonTree().fragmentToString(deletionRoot));
                            sb.append("\n");
                        });
                    }
                } catch (Exception e) {
                    AlertStreams.dispatchToRoot(e);
                }
            }
        }
        commentValueLabel.setText(sb.toString());

        // Output logic of the Transaction title section
        String titleTemplate = """
                %s %s
                """;
        String versionType = null;

        if (isItAConcept) {
            versionType = "Concept";
        } else if (isItASemantic && !isItAnAxiom) {
            versionType = "Description";
        } else if (isItAnAxiom) {
            versionType = "Axiom";
        }

        String transactionType = newlyCreated ? "Created" : "Changed";
        transactionLabel.setText(titleTemplate.formatted(versionType, transactionType));
    }

}
