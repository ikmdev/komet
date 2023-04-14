package dev.ikm.komet.framework.docbook;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.text.DescriptionToToken;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class DocBook {
    private static final Logger LOG = LoggerFactory.getLogger(DocBook.class);

    public static String getInlineEntry(EntityFacade concept,
                                        ViewProperties viewProperties) {
        boolean defined = isDefined(concept.nid(), viewProperties);
        boolean multiParent = isMultiparent(concept.nid(), viewProperties);
        String conceptChar;
        String conceptCharColor;
        String conceptUuid = concept.publicId().asUuidList().toString();
        if (defined) {
            if (multiParent) {
                conceptChar = "&#xF060; ";
                conceptCharColor = "#5ec200;";
            } else {
                conceptChar = "&#xF12F; ";
                conceptCharColor = "#5ec200;";
            }
        } else {
            if (multiParent) {
                conceptChar = "&#xF061; ";
                conceptCharColor = "#FF4E08;";
            } else {
                conceptChar = "&#xF2D8; ";
                conceptCharColor = "#FF4E08;";
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<link xlink:href=\"#ge_solor_");
        builder.append(DescriptionToToken.get(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(concept)));
        builder.append("_");
        builder.append(conceptUuid);
        builder.append("\"><inlinemediaobject>\n");
        builder.append("            <imageobject>\n");
        builder.append("                <imagedata>\n");
        builder.append("                    <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"126px\"\n");
        builder.append("                        height=\"14px\">\n");
        builder.append("                        <text x=\"1\" y=\"7\"\n");
        builder.append("                          style=\"font-size: 9pt; font-family: Open Sans Condensed Light, Symbol, Material Design Icons; baseline-shift: sub;\"\n");
        builder.append("                          >[<tspan dy=\"1.5\"\n");
        builder.append("                          style=\"font-family: Material Design Icons; fill: ");
        builder.append(conceptCharColor);
        builder.append(" stroke: ");
        builder.append(conceptCharColor);
        builder.append(" \"\n");
        builder.append("                          >");
        builder.append(conceptChar);
        builder.append("</tspan>\n");
        builder.append("                          <tspan dy=\"-1.5\"/>");
        builder.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(concept));
        builder.append("]</text>\n");
        builder.append("                    </svg>\n");
        builder.append("                </imagedata>\n");
        builder.append("            </imageobject>\n");
        builder.append("        </inlinemediaobject></link> \n");

        return builder.toString();
    }

    public static boolean isDefined(int conceptNid, ViewProperties viewProperties) {
        return viewProperties.calculator().isDefined(conceptNid);
    }

    public static boolean isMultiparent(int conceptNid, ViewProperties viewProperties) {
        return viewProperties.calculator().isMultiparent(conceptNid);
    }

    public static String getGlossentry(int entityNid,
                                       ViewProperties viewProperties, String svgString) {
        return getGlossentry(Entity.getFast(entityNid), viewProperties, svgString);
    }

    public static String getGlossentry(EntityFacade entity,
                                       ViewProperties viewProperties, String svgString) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n          <row><entry/><entry>");
        builder.append(svgString);
        builder.append("\n          </entry></row>");
        return makeGlossentry(entity, viewProperties, builder.toString());
    }

    private static String makeGlossentry(EntityFacade entity,
                                         ViewProperties viewProperties, String definitionSvg) {
        StringBuilder builder = new StringBuilder();
        builder.append("<glossentry xml:id=\"ge_solor_");
        builder.append(DescriptionToToken.get(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entity)));
        builder.append("_");
        builder.append(entity.publicId().asUuidList());
        builder.append("\">\n");
        builder.append("   ").append("<glossterm>");
        builder.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entity));
        builder.append("</glossterm>\n");
        builder.append("   ").append("<glossdef>");
        builder.append("\n   <informaltable frame=\"topbot\" rowsep=\"0\" colsep=\"0\">");
        builder.append("\n   <?dbfo keep-together=\"always\" ?>");
        builder.append("\n      <tgroup cols=\"2\" align=\"left\">");
        builder.append("\n      <colspec colname=\"c1\" colnum=\"1\" colwidth=\"15pt\"/>");
        builder.append("\n      <colspec colname=\"c2\" colnum=\"2\" colwidth=\"260pt\"/>");
        builder.append("\n          <tbody>");
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Descriptions:</entry></row>");
        // add row for each description
        addDescriptions(builder, entity, viewProperties);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Codes:</entry></row>");
        // add row for each code
        addCodes(builder, entity, viewProperties);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Text definition:</entry></row>");
        // add row for each text definition
        addTextDefinition(builder, entity, viewProperties);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Axioms:</entry></row>");
        builder.append(definitionSvg);
        builder.append("\n          </tbody>");
        builder.append("\n      </tgroup>");
        builder.append("\n   </informaltable>");
        builder.append("\n   </glossdef>");
        builder.append("\n</glossentry>");
        return builder.toString();
    }

    private static void addDescriptions(StringBuilder builder, EntityFacade entity, ViewProperties viewProperties) {
        ImmutableList<SemanticEntity> descriptions = viewProperties.calculator().getDescriptionsForComponent(entity);
        HashMap<Integer, SemanticEntityVersion> nidDescriptionVersionMap = new HashMap<>();
        for (SemanticEntity descriptionChronology : descriptions) {
            Latest<SemanticEntityVersion> latestDescriptionVersion = viewProperties.calculator().latest(descriptionChronology);
            if (latestDescriptionVersion.isPresent()) {
                SemanticEntityVersion descriptionVersion = latestDescriptionVersion.get();
                Latest<Field<ConceptFacade>> descriptionType = viewProperties.calculator().getFieldForSemanticWithMeaning(descriptionVersion.nid(), TinkarTerm.DESCRIPTION_TYPE);

                descriptionType.ifPresent(descriptionTypeField -> {
                    if (descriptionTypeField.value().nid() == TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid() ||
                            descriptionTypeField.value().nid() == TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()) {
                        nidDescriptionVersionMap.put(descriptionVersion.nid(), descriptionVersion);
                    }
                });
            }
        }

        Latest<SemanticEntityVersion> latestFQN = viewProperties.calculator().getFullyQualifiedDescription(entity);
        if (latestFQN.isPresent()) {
            //TODO is inefficient to retrieve by NID when we already have the latestFQN...
            Latest<Field<String>> textField = viewProperties.calculator().getFieldForSemanticWithMeaning(latestFQN.get().nid(), TinkarTerm.TEXT_FOR_DESCRIPTION);
            addDescriptionText(builder,
                    textField.get().value());
            nidDescriptionVersionMap.remove(latestFQN.get().nid());
        }
        Latest<SemanticEntityVersion> latestPreferredName = viewProperties.calculator().getRegularDescription(entity);
        if (latestPreferredName.isPresent()) {
            //TODO is inefficient to retrieve by NID when we already have the latestFQN...
            Latest<Field<String>> textField = viewProperties.calculator().getFieldForSemanticWithMeaning(latestFQN.get().nid(), TinkarTerm.TEXT_FOR_DESCRIPTION);
            addDescriptionText(builder, textField.get().value());
            nidDescriptionVersionMap.remove(latestPreferredName.get().nid());
        }
        for (SemanticEntityVersion name : nidDescriptionVersionMap.values()) {
            Latest<Field<String>> textField = viewProperties.calculator().getFieldForSemanticWithMeaning(latestFQN.get().nid(), TinkarTerm.TEXT_FOR_DESCRIPTION);
            addDescriptionText(builder, textField.get().value());
        }
    }

    private static void addCodes(StringBuilder builder, EntityFacade entity, ViewProperties viewProperties) {
        for (UUID uuid : entity.publicId().asUuidList()) {
            builder.append("          ").append("<row><entry/><entry>UUID: ");
            builder.append(uuid.toString());
            builder.append("</entry></row>");
        }
        LOG.warn("addCodes is incomplete. Review prior implementation. ");
    }

    private static void addTextDefinition(StringBuilder builder, EntityFacade entity, ViewProperties viewProperties) {
        Optional<String> defText = viewProperties.calculator().getDefinitionDescriptionText(entity);
        if (defText.isPresent()) {
            addDescriptionText(builder, defText.get());
        } else {
            addDescriptionText(builder, "Ø");
        }

    }

    private static void addDescriptionText(StringBuilder builder, String fullySpecifiedText) {
        builder.append("          ").append("<row><entry/><entry>");
        builder.append(fullySpecifiedText);
        builder.append("</entry></row>");
    }

    public static String getGlossentry(EntityFacade entity, ViewProperties viewProperties) {
        StringBuilder builder = new StringBuilder();
        addInferredDefinition(builder, entity, viewProperties);
        return makeGlossentry(entity, viewProperties, builder.toString());

    }

    private static void addInferredDefinition(StringBuilder builder, EntityFacade entityFacade, ViewProperties viewProperties) {
        Latest<DiTreeEntity> definition = viewProperties.calculator().getAxiomTreeForEntity(entityFacade, PremiseType.INFERRED);
        if (definition.isPresent()) {
            DiTreeEntity logicGraphSemantic = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraphSemantic.toString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }

    private static void addStatedDefinition(StringBuilder builder, EntityFacade entityFacade, ViewProperties viewProperties) {
        Latest<DiTreeEntity> definition = viewProperties.calculator().getAxiomTreeForEntity(entityFacade, PremiseType.STATED);
        if (definition.isPresent()) {
            DiTreeEntity logicGraphSemantic = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraphSemantic.toString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }
}
