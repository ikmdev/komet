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
package dev.ikm.komet.details.concept;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.NoSuchElementException;
import java.util.UUID;

public enum ObservableFields implements ConceptFacade {
    /**
     * The author nid for edit coordinate.
     */
    AUTHOR_NID_FOR_EDIT_COORDINATE("Author for edit coordinate", "Author"),

    /**
     * The module nid for edit coordinate.
     */
    MODULE_NID_FOR_EDIT_COORDINATE("Default module for edit coordinate", "Default module"),

    DESTINATION_MODULE_NID_FOR_EDIT_COORDINATE("Destination module for edit coordinate", "Destination module"),
    /**
     * The module nid for edit coordinate.
     */
    MODULE_OPTIONS_FOR_EDIT_COORDINATE("Module options for edit coordinate", "Module options"),

    MODULE_FOR_USER("Module for user", "Module for user"),

    /**
     * The path nid for edit coordinate.
     */
    PATH_NID_FOR_EDIT_CORDINATE("Promotion path for edit cordinate", "Promotion path"),

    PATH_OPTIONS_FOR_EDIT_COORDINATE("Path options for edit cordinate", "Path options"),

    PATH_FOR_USER("Path for user", "Path for user"),

    /**
     * The language nid for language coordinate.
     */
    LANGUAGE_NID_FOR_LANGUAGE_COORDINATE("Language nid for language coordinate", "Language nid"),

    /**
     * The language nid for language coordinate.
     */
    LANGUAGE_FOR_LANGUAGE_COORDINATE("Language specification for language coordinate", "Language"),

    DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
            "Dialect assemblage preference list for language coordinate", "Dialect order"),

    MODULE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
            "Module preference list for language coordinate", "Module nids"),

    MODULE_NID_PREFERENCE_LIST_FOR_STAMP_COORDINATE(
            "Module preference list for stamp coordinate", "Module preference list"),

    MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE(
            "Module preference order for stamp coordinate", "Module order"),


    NEXT_PRIORITY_LANGUAGE_COORDINATE(
            "Next priority language coordinate", "Next coordinate"),

    /**
     * The description type nid preference list for language coordinate.
     */
    DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
            "Description type preference list for language coordinate", "Type order"),

    /**
     * The stated assemblage nid for logic coordinate.
     */
    STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("Stated assemblage for logic coordinate", "Stated assemblage"),

    DIGRAPH_FOR_LOGIC_COORDINATE("Digraph for logic coordinate", "Digraph"),

    ROOT_FOR_LOGIC_COORDINATE("Root for logic coordinate", "root"),

    /**
     * The inferred assemblage nid for logic coordinate.
     */
    INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("Inferred assemblage for logic coordinate", "Inferred assemblage"),

    /**
     * The description logic profile nid for logic coordinate.
     */
    DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE("Description logic profile for logic coordinate", "Logic profile"),

    /**
     * The classifier nid for logic coordinate.
     */
    CLASSIFIER_NID_FOR_LOGIC_COORDINATE("Classifier for logic coordinate", "Classifier"),

    CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE("Concept assemblage for logic coordinate", "Concepts to classify"),

    /**
     * The stamp precedence for stamp coordinate.
     */
    STAMP_PRECEDENCE_FOR_STAMP_COORDINATE("Filter precedence for stamp coordinate", "Precedence"),

    /**
     * The stamp position for stamp coordinate.
     */
    STAMP_POSITION_FOR_STAMP_COORDINATE("Filter position for stamp coordinate", "Filter position"),

    MODULE_EXCLUSION_SPECIFICATION_SET_FOR_STAMP_COORDINATE("Module exclusion set for stamp coordinate", "Module exclusions"),

    MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE("Modules for stamp coordinate", "Modules"),

    AUTHOR_SPECIFICATION_SET_FOR_STAMP_COORDINATE("Authors for stamp coordinate", "Authors"),

    DIGRAPH_SPECIFICATION_SET("Navigation concept set", "Navigation set"),
    /**
     * The allowed states for stamp coordinate.
     */
    ALLOWED_STATES_FOR_STAMP_COORDINATE("Allowed states for stamp coordinate", "Allowed states"),

    /**
     * The path nid for stamp path.
     */
    PATH_FOR_PATH_COORDINATE("Path for path coordinate", "Path"),

    /**
     * The path origin list for stamp path.
     */
    PATH_ORIGIN_LIST_FOR_STAMP_PATH("Path origins for stamp path", "Path origins"),

    /**
     * The time for stamp position.
     */
    TIME_FOR_STAMP_POSITION("Position on path", "Path position"),

    /**
     * The path nid for stamp position.
     */
    PATH_NID_FOR_STAMP_POSITION("Path for stamp position", "Path"),

    /**
     * The premise type for taxonomy coordinate.
     */
    PREMISE_TYPE_FOR_TAXONOMY_COORDINATE("Premise type for taxonomy coordinate", "Premise"),

    STAMP_FILTER_FOR_VERTEX("Filter for vertex", "Vertex filter"),
    STAMP_FILTER_FOR_EDGE("Filter for view", "View filter"),
    STAMP_FILTER_FOR_LANGUAGE("Filter for language", "Language filter"),


    /**
     * The stamp coordinate for taxonomy coordinate.
     */
    STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE("Filter coordinate for taxonomy coordinate", "Filter coordinate for taxonomy"),

    /**
     * The stamp coordinate for taxonomy coordinate.
     */
    STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE_DESTINATION("Filter coordinate for taxonomy coordinate destination", "Filter coordinate for taxonomy destination"),

    /**
     * The language coordinate for taxonomy coordinate.
     */
    LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE("Language coordinate for taxonomy coordinate", "Language coordinate"),

    /**
     * The logic coordinate for taxonomy coordinate.
     */
    LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE("Logic coordinate for taxonomy coordinate", "Logic coordinate"),

    CURRENT_ACTIVITY_PROPERTY("Current activity", "Activity"),
    VERTEX_STATUS_SET_PROPERTY("Vertex state set", "Vertex states"),
    VERTEX_SORT_PROPERTY("Vertex sort", "Sort"),
    DIGRAPH_PROPERTY("Digraph for manifold", "Digraph"),
    VIEW_FILTER_FOR_NAVIGATION("View STAMP filter for manifold", "View filter"),
    VERTEX_FILTER_FOR_NAVIGATION("Vertex STAMP filter for manifold", "Vertex filter"),
    STAMP_FILTER_FOR_PATH("Stamp filter for path", "Stamp filter"),

    /**
     * The uuid for taxonomy coordinate.
     */
    UUID_FOR_TAXONOMY_COORDINATE("UUID for taxonomy coordinate", "ImmutableCoordinate UUID"),

    /**
     * The case significance concept nid for description.
     */
    CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION("Case significance concept nid for description", "Case significance"),

    /**
     * The language concept nid for description.
     */
    LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION("Language concept nid for description", "Language for description"),

    /**
     * The text for description.
     */
    TEXT_FOR_DESCRIPTION("Text for description", "Text"),

    /**
     * The description type for description.
     */
    DESCRIPTION_TYPE_FOR_DESCRIPTION("Description type for description", "Description type"),

    /**
     * The version list for chronicle.
     */
    VERSION_LIST_FOR_CHRONICLE("Version list for chronicle", "Versions"),

    CONCEPT_VERSION("Concept version", "Version"),

    /**
     * The native id for chronicle.
     */
    NATIVE_ID_FOR_COMPONENT("Native id for component", "Nid for component"),

    /**
     * The entry sequence for chronicle.
     */
    ENTRY_SEQUENCE_FOR_COMPONENT("Entry sequence for component", "Entry ID"),


    /**
     * The referenced component nid for semantic chronicle.
     */
    REFERENCED_COMPONENT_UUID_FOR_SEMANTIC("Referenced component UUID for semantic", "Referenced component UUID"),

    /**
     * The primordial uuid for chronicle.
     */
    PRIMORDIAL_UUID_FOR_COMPONENT("Primordial UUID for chronicle", "Primordial UUID"),

    /**
     * The uuid list for chronicle.
     */
    UUID_LIST_FOR_COMPONENT("UUID list for component", "UUIDs"),

    /**
     * The committed state for chronicle.
     */
    COMMITTED_STATE_FOR_CHRONICLE("Committed state for chronicle", "Committed state for chronicle"),

    /**
     * The semantic list for chronicle.
     */
    SEMANTIC_LIST_FOR_CHRONICLE("semantic list for chronicle"),

    DESCRIPTION_DIALECT("Description dialect pair"),

    DESCRIPTION_DIALECT_DESCRIPTION("Description for dialect/description pair"),

    DESCRIPTION_DIALECT_DIALECT("Dialect for dialect/description pair"),

    /**
     * The description list for concept.
     */
    DESCRIPTION_LIST_FOR_CONCEPT("description list for concept"),

    STRING_VALUE_FOR_SEMANTIC("String for semantic", "String for semantic"),

    COMPONENT_NID_FOR_SEMANTIC("Component for semantic", "Component"),

    LOGIC_GRAPH_FOR_SEMANTIC("logic graph for semantic", "Logic graph"),

    LONG_VALUE_FOR_SEMANTIC("long value for semantic", "Long value"),

    TYPE_NID_FOR_RF2_REL("Type nid for rf2 relationship", "Rel type"),

    DESTINATION_NID_FOR_RF2_REL("Destination nid for rf2 relationship", "Rel destination"),

    REL_GROUP_FOR_RF2_REL("Relationship group for rf2 relationship", "Rel group"),

    CHARACTERISTIC_NID_FOR_RF2_REL("Characteristic nid for rf2 relationship", "Rel characteristic"),

    MODIFIER_NID_FOR_RF2_REL("modifier nid for rf2 relationship", "Rel modifier"),

    NID1("Component id 1", "Component 1"),
    NID2("Component id 2", "Component 2"),
    NID3("Component id 3", "Component 3"),
    NID4("Component id 4", "Component 4"),
    NID5("Component id 5", "Component 5"),
    NID6("Component id 6", "Component 6"),
    NID7("Component id 7", "Component 7"),
    STR1("String 1"),
    STR2("String 2"),
    STR3("String 3"),
    STR4("String 4"),
    STR5("String 5"),
    STR6("String 6"),
    STR7("String 7"),
    INT1("Integer 1"),
    INT2("Integer 2"),
    INT3("Integer 3"),
    INT4("Integer 4"),
    INT5("Integer 5"),
    INT6("Integer 6"),
    INT7("Integer 7"),
    LONG2("Long 2"),

    CIRCUMSTANCE_PURPOSE_LIST("Action purpose"),
    CIRCUMSTANCE_TIMING("Timing"),

    STATEMENT_NARRATIVE("Statement narrative", "Narrative"),
    STATEMENT_TIME("Statement time", "Statement time"),
    STATEMENT_ID("Statement identifier"),
    STATEMENT_SOR("Statement subject of record", "Statement subject of record"),
    STATEMENT_AUTHORS("Statement authors", "Authors"),
    STATEMENT_SOI("Statement subject of information", "Subject"),
    STATEMENT_TYPE("Statement type", "Type"),
    STATEMENT_TOPIC("Statement action topic", "Action topic"),
    STATEMENT_CIRCUMSTANCE("Statement circumstance", "Circumstance"),
    STATEMENT_ASSOCIATIONS("Statement associations", "Associations"),

    INTERVAL_LOWER_BOUND("Lower bound"),
    INTERVAL_UPPER_BOUND("Upper bound"),
    INTERVAL_INCLUDE_UPPER_BOUND("Include upper bound"),
    INTERVAL_INCLUDE_LOWER_BOUND("Include lower bound"),

    INTERVENTION_RESULT_STATUS("Intervention result status"),

    MEASURE_NARRATIVE("Measure narritive"),
    MEASURE_RESOLUTION("Resolution"),
    MEASURE_SEMANTIC("Measure semantic"),
    OBSERVATION_RESULT_HEALTH_RISK("Health risk"),
    MEASURE_NORMAL_RANGE("Normal range"),

    PARTICIPANT_ID("Participant id"),
    PARTICIPANT_ROLE("Participant role"),

    PERFORMANCE_CIRCUMSTANCE_RESULT("Result"),
    PERFORMANCE_CIRCUMSTANCE_PARTICIPANTS("Participants"),

    REPETITION_PERIOD_START("Period start"),
    REPETITION_PERIOD_DURATION("Period duration"),
    REPETITION_EVENT_FREQUENCY("Event frequency"),
    REPETITION_EVENT_SEPARATION("Event separation"),
    REPETITION_EVENT_DURATION("Event duration"),

    REQUEST_CIRCUMSTANCE_CONDITIONAL_TRIGGERS("Conditional triggers"),
    REQUEST_CIRCUMSTANCE_REQUESTED_PARTICIPANTS("Requested participants"),
    REQUEST_CIRCUMSTANCE_PRIORITY("Request Priority"),
    REQUEST_CIRCUMSTANCE_REPETITIONS("Repetitions"),
    REQUEST_CIRCUMSTANCE_REQUESTED_RESULT("Requested result"),

    STATEMENT_ASSOCIATION_SEMANTIC("Association semantic"),
    STATEMENT_ASSOCIATION_ID("Association id"),

    UNSTRUCTURED_CIRCUMSTANCE_TEXT("Unstructured circumstance text", "Text"),

    STATEMENT_STAMP_COORDINATE("Filter coordinate"),

    STATEMENT_MODE("Statement mode", "Statement mode"),

    LOINC_NUMBER("LOINC number"),
    LOINC_COMPONENT("LOINC component"),
    LOINC_PROPERTY("LOINC property"),
    LOINC_TIME_ASPECT("LOINC time aspect"),
    LOINC_SYSTEM("LOINC system"),
    LOINC_SCALE_TYPE("LOINC scale type"),
    LOINC_METHOD_TYPE("LOINC method type"),
    LOINC_STATUS("LOINC status"),
    LOINC_SHORT_NAME("LOINC short name"),
    LOINC_LONG_COMMON_NAME("LOINC long common name"),

    CORELATION_REFERENCE_EXPRESSION("Corelation reference expression"),
    CORELATION_COMPARISON_EXPRESSION("Corelation comparison expression"),
    CORELATION_EXPRESSION("Corelation expression"),

    GIT_USER_NAME("Git user name"),
    GIT_PASSWORD("Git password"),
    GIT_URL("Git url"),
    GIT_LOCAL_FOLDER("Git local folder"),
    ENABLE_EDIT("Enable editing"),

    KOMET_USER("KOMET user"),
    KOMET_USER_LIST("KOMET user list"),
    CONCEPT_IS_ASSEMBLAGE("Concept is assemblage"),

    SEMANTIC_FIELD_NAME("Semantic field name", "Field name"),
    ASSEMBLAGE_FOR_CONSTRAINT("Assemblage for constraint", "Assemblage for constraint"),
    ROLE_TYPE_TO_ADD("Role type to add", "Role type to add"),

    ASSEMBLAGE_FOR_ACTION("Assemblage for action", "Assemblage for action"),
    VERSION_TYPE_FOR_ACTION("Version type for action", "Version for action"),

    CONCEPT_CONSTRAINTS("Concept constraints"),

    ASSEMBLAGE_LIST_FOR_QUERY("Assemblage list for query", "For list"),

    MANIFOLD_COORDINATE_REFERENCE("Manifold coordinate reference", "manifold"),

    IMAGE_DATA_FOR_SEMANTIC("Image data for semantic", "Image data"),

    AXIOM_ORDER_FOR_DETAILS_PANE("Detail pane axiom order", "Axiom order"),

    DESCRIPTION_TYPE_ORDER_FOR_DETAILS_PANE("Detail pane description type order", "Description type order"),

    DETAIL_ORDER_FOR_DETAILS_PANE("Detail pane order", "Detail order"),

    SEMANTIC_ORDER_FOR_CONCEPT_DETAILS("Order for concept attachments", "Concept attachment order"),

    SEMANTIC_ORDER_FOR_DESCRIPTION_DETAILS("Order for description attachments", "Description attachment order"),

    SEMANTIC_ORDER_FOR_AXIOM_DETAILS("Order for axiom attachments", "Axiom attachment order"),

    WILDCARD_FOR_ORDER("Any component", "Any component"),
    ;
    // this, ObservableFields..toExternalString()

    /**
     * The Constant namespace.
     */
    private static final UUID namespace = UUID.fromString("cbbd1e22-0cac-11e5-a6c0-1697f925ec7b");

    //~--- fieldValues --------------------------------------------------------------

    /**
     * The description.
     */
    String fullyQualifiedDescription;
    String regularDescription;
    private int cachedNid;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable fieldValues.
     *
     * @param fullyQualifiedDescription the description
     */
    ObservableFields(String fullyQualifiedDescription) {
        this.fullyQualifiedDescription = fullyQualifiedDescription;
        this.regularDescription = null;
    }

    ObservableFields(String fullyQualifiedDescription, String regularDescription) {
        this.fullyQualifiedDescription = fullyQualifiedDescription;
        this.regularDescription = regularDescription;
    }

    //~--- methods -------------------------------------------------------------

    //~--- get methods ---------------------------------------------------------


    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.fullyQualifiedDescription;
    }

    @Override
    public PublicId publicId() {
        return PublicIds.of(getUuid());
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public UUID getUuid() {
        return UuidT5Generator.get(namespace, name());
    }

    public String toExternalString() {
        return EntityProxy.Concept.make(nid()).toXmlFragment();
    }

    @Override
    public int nid() throws NoSuchElementException {
        if (cachedNid == 0) {
            cachedNid = PrimitiveData.nid(getUuid());
        }
        return cachedNid;
    }
}
