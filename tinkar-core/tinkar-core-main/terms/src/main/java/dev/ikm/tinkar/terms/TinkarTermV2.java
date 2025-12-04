package dev.ikm.tinkar.terms;

import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;

import java.util.UUID;

/**
 * Tinkar Term Binding class to enable programmatic access to tinkar data elements known to be stored in an Komet database.
 * @author  Test Author
 */
public class TinkarTermV2 {

    /**
     * Namespace used in the UUID creation process for tinkar components (e.g., Concept, Pattern, Semantic, and STAMP)
     */
    public static final UUID NAMESPACE = UUID.fromString("f7ef93c7-97d6-4b52-a16e-f09877e3cf98");

    /**
     * Java binding for the pattern described as US Dialect Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>08f9112c-c041-56d3-b89b-63258f070074
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents United States of America English dialect (SOLOR).
     * </ul>
     */
    public static final Pattern US_DIALECT_PATTERN = Pattern.make("US Dialect Pattern", UUID.fromString("08f9112c-c041-56d3-b89b-63258f070074"));

    /**
     * Java binding for the pattern described as Comment pattern and identified by the following as UUID(s):
     * <ul>
     * <li>3734fb0a-4c14-5831-9a61-4743af609e7a
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a String display field that represents Comment (SOLOR).
     * </ul>
     */
    public static final Pattern COMMENT_PATTERN = Pattern.make("Comment pattern", UUID.fromString("3734fb0a-4c14-5831-9a61-4743af609e7a"));

    /**
     * Java binding for the pattern described as Concept field pattern and identified by the following as UUID(s):
     * <ul>
     * <li>3e510cb9-1666-4676-9334-d288a56bf155
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Public ID field.
     * <li>Field 1 is a Component Id display set that represents Concept versions field.
     * </ul>
     */
    public static final Pattern CONCEPT_FIELD_PATTERN = Pattern.make("Concept field pattern", UUID.fromString("3e510cb9-1666-4676-9334-d288a56bf155"));

    /**
     * Java binding for the pattern described as Module origins pattern (SOLOR) and identified by the following as UUID(s):
     * <ul>
     * <li>536b0ec4-4974-47ae-93a6-ae6c4d169780
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component Id display set that represents Module origins (SOLOR).
     * </ul>
     */
    public static final Pattern MODULE_ORIGINS_PATTERN = Pattern.make("Module origins pattern (SOLOR)", UUID.fromString("536b0ec4-4974-47ae-93a6-ae6c4d169780"));

    /**
     * Java binding for the pattern described as GB Dialect Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>561f817a-130e-5e56-984d-910e9991558c
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Great Britain English dialect.
     * </ul>
     */
    public static final Pattern GB_DIALECT_PATTERN = Pattern.make("GB Dialect Pattern", UUID.fromString("561f817a-130e-5e56-984d-910e9991558c"));

    /**
     * Java binding for the pattern described as Pattern Chronology Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>5bc93adb-9d39-43fe-a7a4-1492245b7efb
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Public ID field.
     * <li>Field 1 is a Component Id display set that represents Pattern versions field.
     * </ul>
     */
    public static final Pattern PATTERN_CHRONOLOGY_PATTERN = Pattern.make("Pattern Chronology Pattern", UUID.fromString("5bc93adb-9d39-43fe-a7a4-1492245b7efb"));

    /**
     * Java binding for the pattern described as Identifier Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>5d60e14b-c410-5172-9559-3c4253278ae2
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Identifier Source.
     * <li>Field 1 is a String display field that represents Identifier Value (SOLOR).
     * </ul>
     */
    public static final Pattern IDENTIFIER_PATTERN = Pattern.make("Identifier Pattern", UUID.fromString("5d60e14b-c410-5172-9559-3c4253278ae2"));

    /**
     * Java binding for the pattern described as Semantic Chronology Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>5f0ad6ca-638e-4052-82b0-3f564ac99b3f
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Public ID field.
     * <li>Field 1 is a Component display field that represents Semantic pattern field.
     * <li>Field 2 is a Component display field that represents Semantic referenced component field.
     * <li>Field 3 is a Component Id display set that represents Semantic versions set.
     * </ul>
     */
    public static final Pattern SEMANTIC_CHRONOLOGY_PATTERN = Pattern.make("Semantic Chronology Pattern", UUID.fromString("5f0ad6ca-638e-4052-82b0-3f564ac99b3f"));

    /**
     * Java binding for the pattern described as Tinkar base model component pattern and identified by the following as UUID(s):
     * <ul>
     * <li>6070f6f5-893d-5144-adce-7d305c391cf9
     * </ul>
     */
    public static final Pattern TINKAR_BASE_MODEL_COMPONENT_PATTERN = Pattern.make("Tinkar base model component pattern", UUID.fromString("6070f6f5-893d-5144-adce-7d305c391cf9"));

    /**
     * Java binding for the pattern described as Path origins pattern (SOLOR) and identified by the following as UUID(s):
     * <ul>
     * <li>70f89dd5-2cdb-59bb-bbaa-98527513547c
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Path concept (SOLOR).
     * <li>Field 1 is a Instant literal (SOLOR) that represents Path origins (SOLOR).
     * </ul>
     */
    public static final Pattern PATH_ORIGINS_PATTERN = Pattern.make("Path origins pattern (SOLOR)", UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c"));

    /**
     * Java binding for the pattern described as STAMP version field pattern and identified by the following as UUID(s):
     * <ul>
     * <li>73c798cf-bc77-49a2-84f7-4c0f4bc4c012
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents STAMP field.
     * <li>Field 1 is a Component display field that represents Status field.
     * <li>Field 2 is a String display field that represents Time field.
     * <li>Field 3 is a Component display field that represents Author field.
     * <li>Field 4 is a Component display field that represents Module field.
     * <li>Field 5 is a Component display field that represents Path field.
     * </ul>
     */
    public static final Pattern STAMP_VERSION_FIELD_PATTERN = Pattern.make("STAMP version field pattern", UUID.fromString("73c798cf-bc77-49a2-84f7-4c0f4bc4c012"));

    /**
     * Java binding for the pattern described as Concept Version Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>7943a5f1-538b-4fda-8acb-019e0bec125b
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents STAMP field.
     * </ul>
     */
    public static final Pattern CONCEPT_VERSION_PATTERN = Pattern.make("Concept Version Pattern", UUID.fromString("7943a5f1-538b-4fda-8acb-019e0bec125b"));

    /**
     * Java binding for the pattern described as Sementic version field pattern and identified by the following as UUID(s):
     * <ul>
     * <li>82f93e84-cee1-44bc-bb6d-4cc2a722048b
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents STAMP field.
     * <li>Field 1 is a Component Id display set that represents Semantic field field.
     * </ul>
     */
    public static final Pattern SEMENTIC_VERSION_FIELD_PATTERN = Pattern.make("Sementic version field pattern", UUID.fromString("82f93e84-cee1-44bc-bb6d-4cc2a722048b"));

    /**
     * Java binding for the pattern described as Value Constraint Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>922697f7-36ba-4afc-9dd5-f29d54b0fdec
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Concept display field (SOLOR) that represents Value Constraint Source (SOLOR).
     * <li>Field 1 is a Concept display field (SOLOR) that represents Minimum Value Operator (SOLOR).
     * <li>Field 2 is a Float display field that represents Reference Range Minimum (SOLOR).
     * <li>Field 3 is a Component display field that represents Maximum Value Operator (SOLOR).
     * <li>Field 4 is a Float display field that represents Reference Range Maximum (SOLOR).
     * <li>Field 5 is a String display field that represents Example UCUM Units (SOLOR).
     * </ul>
     */
    public static final Pattern VALUE_CONSTRAINT_PATTERN = Pattern.make("Value Constraint Pattern", UUID.fromString("922697f7-36ba-4afc-9dd5-f29d54b0fdec"));

    /**
     * Java binding for the pattern described as EL++ Inferred Axioms Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>9f011812-15c9-5b1b-85f8-bb262bc1b2a2
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a DiTree display field that represents EL++ Inferred terminological axioms.
     * </ul>
     */
    public static final Pattern EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN = Pattern.make("EL++ Inferred Axioms Pattern", UUID.fromString("9f011812-15c9-5b1b-85f8-bb262bc1b2a2"));

    /**
     * Java binding for the pattern described as STAMP pattern and identified by the following as UUID(s):
     * <ul>
     * <li>9fd67fee-abf9-551d-9d0e-76a4b1e8b4ee
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Status value.
     * <li>Field 1 is a Long (SOLOR) that represents Time for version (SOLOR).
     * <li>Field 2 is a Component display field that represents Author for version (SOLOR).
     * <li>Field 3 is a Component display field that represents Module for version (SOLOR).
     * <li>Field 4 is a Component display field that represents Path for version.
     * </ul>
     */
    public static final Pattern STAMP_PATTERN = Pattern.make("STAMP pattern", UUID.fromString("9fd67fee-abf9-551d-9d0e-76a4b1e8b4ee"));

    /**
     * Java binding for the pattern described as Component Version Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>a38b7d2d-8fa5-4206-9185-a1af9f81be2c
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents STAMP field.
     * </ul>
     */
    public static final Pattern COMPONENT_VERSION_PATTERN = Pattern.make("Component Version Pattern", UUID.fromString("a38b7d2d-8fa5-4206-9185-a1af9f81be2c"));

    /**
     * Java binding for the pattern described as Description Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>a4de0039-2625-5842-8a4c-d1ce6aebf021
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Language concept nid for description (SOLOR).
     * <li>Field 1 is a String display field that represents Text for description.
     * <li>Field 2 is a Component display field that represents Description case significance.
     * <li>Field 3 is a Component display field that represents Description type.
     * </ul>
     */
    public static final Pattern DESCRIPTION_PATTERN = Pattern.make("Description Pattern", UUID.fromString("a4de0039-2625-5842-8a4c-d1ce6aebf021"));

    /**
     * Java binding for the pattern described as Inferred Navigation Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>a53cc42d-c07e-5934-96b3-2ede3264474e
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component Id display set that represents Relationship destination.
     * <li>Field 1 is a Component Id display set that represents Relationship origin.
     * </ul>
     */
    public static final Pattern INFERRED_NAVIGATION_PATTERN = Pattern.make("Inferred Navigation Pattern", UUID.fromString("a53cc42d-c07e-5934-96b3-2ede3264474e"));

    /**
     * Java binding for the pattern described as Pattern Version Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>a90f8a4d-ae13-476b-98b8-814914f9704e
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents STAMP field.
     * <li>Field 1 is a Component display field that represents Pattern meaning field.
     * <li>Field 2 is a Component display field that represents Pattern purpose field.
     * <li>Field 3 is a Component Id display set that represents Field definition field.
     * </ul>
     */
    public static final Pattern PATTERN_VERSION_PATTERN = Pattern.make("Pattern Version Pattern", UUID.fromString("a90f8a4d-ae13-476b-98b8-814914f9704e"));

    /**
     * Java binding for the pattern described as Version control path pattern and identified by the following as UUID(s):
     * <ul>
     * <li>add1db57-72fe-53c8-a528-1614bda20ec6
     * </ul>
     */
    public static final Pattern VERSION_CONTROL_PATH_PATTERN = Pattern.make("Version control path pattern", UUID.fromString("add1db57-72fe-53c8-a528-1614bda20ec6"));

    /**
     * Java binding for the pattern described as OWL Axiom Syntax Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a String display field that represents Axiom Syntax (SOLOR).
     * </ul>
     */
    public static final Pattern OWL_AXIOM_SYNTAX_PATTERN = Pattern.make("OWL Axiom Syntax Pattern", UUID.fromString("c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b"));

    /**
     * Java binding for the pattern described as Component Chronology Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>c48db76d-5eb0-4ff5-84d0-5c3c4ec77767
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Public ID field.
     * <li>Field 1 is a Component Id display set that represents Component versions field.
     * </ul>
     */
    public static final Pattern COMPONENT_CHRONOLOGY_PATTERN = Pattern.make("Component Chronology Pattern", UUID.fromString("c48db76d-5eb0-4ff5-84d0-5c3c4ec77767"));

    /**
     * Java binding for the pattern described as Stated Navigation Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>d02957d6-132d-5b3c-adba-505f5778d998
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component Id display set that represents Relationship destination.
     * <li>Field 1 is a Component Id display set that represents Relationship origin.
     * </ul>
     */
    public static final Pattern STATED_NAVIGATION_PATTERN = Pattern.make("Stated Navigation Pattern", UUID.fromString("d02957d6-132d-5b3c-adba-505f5778d998"));

    /**
     * Java binding for the pattern described as SOLOR concept assemblage (SOLOR) and identified by the following as UUID(s):
     * <ul>
     * <li>d39b3ecd-9a80-5009-a8ac-0b947f95ca7c
     * </ul>
     */
    public static final Pattern SOLOR_CONCEPT_ASSEMBLAGE = Pattern.make("SOLOR concept assemblage (SOLOR)", UUID.fromString("d39b3ecd-9a80-5009-a8ac-0b947f95ca7c"));

    /**
     * Java binding for the pattern described as STAMP Chronology Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>e16abc7a-2a7b-42af-b168-d77aec8116ea
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a Component display field that represents Public ID field.
     * <li>Field 1 is a Component Id display set that represents STAMP versions field.
     * </ul>
     */
    public static final Pattern STAMP_CHRONOLOGY_PATTERN = Pattern.make("STAMP Chronology Pattern", UUID.fromString("e16abc7a-2a7b-42af-b168-d77aec8116ea"));

    /**
     * Java binding for the pattern described as EL++ Stated Axioms Pattern and identified by the following as UUID(s):
     * <ul>
     * <li>e813eb92-7d07-5035-8d43-e81249f5b36e
     * </ul>
     * <p>
     * Pattern contains the following fields
     * <ul>
     * <li>Field 0 is a DiTree display field that represents EL++ Stated terminological axioms.
     * </ul>
     */
    public static final Pattern EL_PLUS_PLUS_STATED_AXIOMS_PATTERN = Pattern.make("EL++ Stated Axioms Pattern", UUID.fromString("e813eb92-7d07-5035-8d43-e81249f5b36e"));

    /**
     * Java binding for the concept described as Fully qualified name description type and identified by the following UUID(s):
     * <ul>
     * <li>00791270-77c9-32b6-b34f-d932569bd2bf
     * <li>5e1fe940-8faf-11db-b606-0800200c9a66
     * </ul>
     */
    public static final Concept FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE = Concept.make("Fully qualified name description type", UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"), UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));

    /**
     * Java binding for the concept described as English Language and identified by the following UUID(s):
     * <ul>
     * <li>02018e5a-46ba-5297-92f1-6931b9f98a12
     * <li>06d905ea-c647-3af9-bfe5-2514e135b558
     * <li>45021920-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept ENGLISH_LANGUAGE = Concept.make("English Language", UUID.fromString("02018e5a-46ba-5297-92f1-6931b9f98a12"), UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"), UUID.fromString("45021920-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Field definition data type field and identified by the following UUID(s):
     * <ul>
     * <li>02273b53-fce7-4cbe-921d-2cff67e81ad5
     * </ul>
     */
    public static final Concept FIELD_DEFINITION_DATA_TYPE_FIELD = Concept.make("Field definition data type field", UUID.fromString("02273b53-fce7-4cbe-921d-2cff67e81ad5"));

    /**
     * Java binding for the concept described as Inactive state and identified by the following UUID(s):
     * <ul>
     * <li>03004053-c23e-5206-8514-fb551dd328f4
     * </ul>
     */
    public static final Concept INACTIVE_STATE = Concept.make("Inactive state", UUID.fromString("03004053-c23e-5206-8514-fb551dd328f4"));

    /**
     * Java binding for the concept described as Boolean substitution (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>03559f9d-f1e4-5485-894b-4d457f145d54
     * </ul>
     */
    public static final Concept BOOLEAN_SUBSTITUTION = Concept.make("Boolean substitution (SOLOR)", UUID.fromString("03559f9d-f1e4-5485-894b-4d457f145d54"));

    /**
     * Java binding for the concept described as Status for version (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0608e233-d79d-5076-985b-9b1ea4e14b4c
     * </ul>
     */
    public static final Concept STATUS_FOR_VERSION = Concept.make("Status for version (SOLOR)", UUID.fromString("0608e233-d79d-5076-985b-9b1ea4e14b4c"));

    /**
     * Java binding for the concept described as Starter Data Authoring (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>070deb74-acc5-46bf-b9c6-eaee1b58ef52
     * </ul>
     */
    public static final Concept STARTER_DATA_AUTHORING = Concept.make("Starter Data Authoring (SOLOR)", UUID.fromString("070deb74-acc5-46bf-b9c6-eaee1b58ef52"));

    /**
     * Java binding for the concept described as Has Dose Form (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>072e7737-e22e-36b5-89d2-4815f0529c63
     * </ul>
     */
    public static final Concept HAS_DOSE_FORM = Concept.make("Has Dose Form (SOLOR)", UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));

    /**
     * Java binding for the concept described as Concept constraints(SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>081273cd-92dd-593c-9d9b-63d33838e70b
     * </ul>
     */
    public static final Concept CONCEPT_CONSTRAINTS = Concept.make("Concept constraints(SOLOR)", UUID.fromString("081273cd-92dd-593c-9d9b-63d33838e70b"));

    /**
     * Java binding for the concept described as Russian language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0818dbb7-3fe1-59d7-99c2-c8dc42ff2cce
     * </ul>
     */
    public static final Concept RUSSIAN_LANGUAGE = Concept.make("Russian language (SOLOR)", UUID.fromString("0818dbb7-3fe1-59d7-99c2-c8dc42ff2cce"));

    /**
     * Java binding for the concept described as Boolean (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>08f2fb74-980d-5157-b92c-4ff1eac6a506
     * </ul>
     */
    public static final Concept BOOLEAN = Concept.make("Boolean (SOLOR)", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506"));

    /**
     * Java binding for the concept described as Value Constraint Source (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>09aa031a-6290-4ec9-a44c-23928a767da3
     * </ul>
     */
    public static final Concept VALUE_CONSTRAINT_SOURCE = Concept.make("Value Constraint Source (SOLOR)", UUID.fromString("09aa031a-6290-4ec9-a44c-23928a767da3"));

    /**
     * Java binding for the concept described as Active state and identified by the following UUID(s):
     * <ul>
     * <li>09f12001-0e4f-51e2-9852-44862a4a0db4
     * </ul>
     */
    public static final Concept ACTIVE_STATE = Concept.make("Active state", UUID.fromString("09f12001-0e4f-51e2-9852-44862a4a0db4"));

    /**
     * Java binding for the concept described as Component for semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0bc32c16-698e-5719-8bd5-efa099c7d782
     * </ul>
     */
    public static final Concept COMPONENT_FOR_SEMANTIC = Concept.make("Component for semantic (SOLOR)", UUID.fromString("0bc32c16-698e-5719-8bd5-efa099c7d782"));

    /**
     * Java binding for the concept described as Role type to add (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0c3ca846-0374-5a5c-8da4-67e0e2e28868
     * </ul>
     */
    public static final Concept ROLE_TYPE_TO_ADD = Concept.make("Role type to add (SOLOR)", UUID.fromString("0c3ca846-0374-5a5c-8da4-67e0e2e28868"));

    /**
     * Java binding for the concept described as EL++ Stated Concept Definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0c464a4a-a0bc-53ef-9c01-ef5a049f2656
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_STATED_CONCEPT_DEFINITION = Concept.make("EL++ Stated Concept Definition (SOLOR)", UUID.fromString("0c464a4a-a0bc-53ef-9c01-ef5a049f2656"));

    /**
     * Java binding for the concept described as Dynamic referenced component restriction (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>0d94ceeb-e24f-5f1a-84b2-1ac35f671db5
     * </ul>
     */
    public static final Concept DYNAMIC_REFERENCED_COMPONENT_RESTRICTION = Concept.make("Dynamic referenced component restriction (SOLOR)", UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"));

    /**
     * Java binding for the concept described as Description case sensitive and identified by the following UUID(s):
     * <ul>
     * <li>0def37bc-7e1b-384b-a6a3-3e3ceee9c52e
     * </ul>
     */
    public static final Concept DESCRIPTION_CASE_SENSITIVE = Concept.make("Description case sensitive", UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));

    /**
     * Java binding for the concept described as Spanish language and identified by the following UUID(s):
     * <ul>
     * <li>0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e
     * <li>45021c36-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept SPANISH_LANGUAGE = Concept.make("Spanish language", UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"), UUID.fromString("45021c36-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Concept type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>106f3ba1-63b8-5596-8dbe-524fa2e89fc0
     * </ul>
     */
    public static final Concept CONCEPT_TYPE = Concept.make("Concept type (SOLOR)", UUID.fromString("106f3ba1-63b8-5596-8dbe-524fa2e89fc0"));

    /**
     * Java binding for the concept described as Status value and identified by the following UUID(s):
     * <ul>
     * <li>10b873e2-8247-5ab5-9dec-4edef37fc219
     * </ul>
     */
    public static final Concept STATUS_VALUE = Concept.make("Status value", UUID.fromString("10b873e2-8247-5ab5-9dec-4edef37fc219"));

    /**
     * Java binding for the concept described as Description for dialect/description pair (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1137767a-ad8b-5bc5-9842-a1f9b09d1ecc
     * </ul>
     */
    public static final Concept DESCRIPTION_FOR_DIALECT_FORWARDSLASH_DESCRIPTION_PAIR = Concept.make("Description for dialect/description pair (SOLOR)", UUID.fromString("1137767a-ad8b-5bc5-9842-a1f9b09d1ecc"));

    /**
     * Java binding for the concept described as Path for user (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>12131382-1535-5a77-928b-6eacad221ea2
     * </ul>
     */
    public static final Concept PATH_FOR_USER = Concept.make("Path for user (SOLOR)", UUID.fromString("12131382-1535-5a77-928b-6eacad221ea2"));

    /**
     * Java binding for the concept described as Component semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>127e7274-0b88-5519-a9db-85d4b9ce6a4a
     * </ul>
     */
    public static final Concept COMPONENT_SEMANTIC = Concept.make("Component semantic (SOLOR)", UUID.fromString("127e7274-0b88-5519-a9db-85d4b9ce6a4a"));

    /**
     * Java binding for the concept described as Acceptable (foundation metadata concept) and identified by the following UUID(s):
     * <ul>
     * <li>12b9e103-060e-3256-9982-18c1191af60e
     * </ul>
     */
    public static final Concept ACCEPTABLE_OPENPARENTHESIS_FOUNDATION_METADATA_CONCEPT_CLOSEPARENTHESIS_ = Concept.make("Acceptable (foundation metadata concept)", UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));

    /**
     * Java binding for the concept described as Data Property Set Axioms and identified by the following UUID(s):
     * <ul>
     * <li>1402d311-0b4b-4014-81d2-e715c6696346
     * </ul>
     */
    public static final Concept DATA_PROPERTY_SET_AXIOMS = Concept.make("Data Property Set Axioms", UUID.fromString("1402d311-0b4b-4014-81d2-e715c6696346"));

    /**
     * Java binding for the concept described as EL++ Stated terminological axioms and identified by the following UUID(s):
     * <ul>
     * <li>1412bd09-eb0c-5107-9756-10c1c417d385
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS = Concept.make("EL++ Stated terminological axioms", UUID.fromString("1412bd09-eb0c-5107-9756-10c1c417d385"));

    /**
     * Java binding for the concept described as Field definition field and identified by the following UUID(s):
     * <ul>
     * <li>14171f07-e74f-409a-b555-06b478818f76
     * </ul>
     */
    public static final Concept FIELD_DEFINITION_FIELD = Concept.make("Field definition field", UUID.fromString("14171f07-e74f-409a-b555-06b478818f76"));

    /**
     * Java binding for the concept described as Korean Language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1464f995-d658-5e9d-86e0-8308a6fa57eb
     * </ul>
     */
    public static final Concept KOREAN_LANGUAGE = Concept.make("Korean Language (SOLOR)", UUID.fromString("1464f995-d658-5e9d-86e0-8308a6fa57eb"));

    /**
     * Java binding for the concept described as Comment (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>147832d4-b9b8-5062-8891-19f9c4e4760a
     * </ul>
     */
    public static final Concept COMMENT = Concept.make("Comment (SOLOR)", UUID.fromString("147832d4-b9b8-5062-8891-19f9c4e4760a"));

    /**
     * Java binding for the concept described as Description-logic profile (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>14eadb10-fbd0-5999-af37-05728a8503af
     * </ul>
     */
    public static final Concept DESCRIPTION_DASH_LOGIC_PROFILE = Concept.make("Description-logic profile (SOLOR)", UUID.fromString("14eadb10-fbd0-5999-af37-05728a8503af"));

    /**
     * Java binding for the concept described as Time field and identified by the following UUID(s):
     * <ul>
     * <li>15293325-c16b-4f2e-8109-5b22b3355bcd
     * </ul>
     */
    public static final Concept TIME_FIELD = Concept.make("Time field", UUID.fromString("15293325-c16b-4f2e-8109-5b22b3355bcd"));

    /**
     * Java binding for the concept described as Semantic field name (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>15489c68-673d-503e-bff7-e9d59e5dc15c
     * </ul>
     */
    public static final Concept SEMANTIC_FIELD_NAME = Concept.make("Semantic field name (SOLOR)", UUID.fromString("15489c68-673d-503e-bff7-e9d59e5dc15c"));

    /**
     * Java binding for the concept described as Concept assemblage for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>16486419-5d1c-574f-bde6-21910ad66f44
     * </ul>
     */
    public static final Concept CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE = Concept.make("Concept assemblage for logic coordinate (SOLOR)", UUID.fromString("16486419-5d1c-574f-bde6-21910ad66f44"));

    /**
     * Java binding for the concept described as Concept details tree table (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1655edd8-7b73-52c5-98b0-263d1ab3a90b
     * </ul>
     */
    public static final Concept CONCEPT_DETAILS_TREE_TABLE = Concept.make("Concept details tree table (SOLOR)", UUID.fromString("1655edd8-7b73-52c5-98b0-263d1ab3a90b"));

    /**
     * Java binding for the concept described as Correlation expression (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1711815f-99a1-5d1a-8f1e-75dc7bf41928
     * </ul>
     */
    public static final Concept CORRELATION_EXPRESSION = Concept.make("Correlation expression (SOLOR)", UUID.fromString("1711815f-99a1-5d1a-8f1e-75dc7bf41928"));

    /**
     * Java binding for the concept described as Description initial character case sensitive (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>17915e0d-ed38-3488-a35c-cda966db306a
     * </ul>
     */
    public static final Concept DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE = Concept.make("Description initial character case sensitive (SOLOR)", UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));

    /**
     * Java binding for the concept described as Module options for edit coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>19305aff-95d9-55d9-b015-825cc68eadc7
     * </ul>
     */
    public static final Concept MODULE_OPTIONS_FOR_EDIT_COORDINATE = Concept.make("Module options for edit coordinate (SOLOR)", UUID.fromString("19305aff-95d9-55d9-b015-825cc68eadc7"));

    /**
     * Java binding for the concept described as Public ID field and identified by the following UUID(s):
     * <ul>
     * <li>196838c5-55f4-4e40-8618-b9ce60685c2f
     * </ul>
     */
    public static final Concept PUBLIC_ID_FIELD = Concept.make("Public ID field", UUID.fromString("196838c5-55f4-4e40-8618-b9ce60685c2f"));

    /**
     * Java binding for the concept described as Semantic pattern field and identified by the following UUID(s):
     * <ul>
     * <li>19dd5dd3-1075-4113-a437-5f1f7c2d55bc
     * </ul>
     */
    public static final Concept SEMANTIC_PATTERN_FIELD = Concept.make("Semantic pattern field", UUID.fromString("19dd5dd3-1075-4113-a437-5f1f7c2d55bc"));

    /**
     * Java binding for the concept described as Component versions field and identified by the following UUID(s):
     * <ul>
     * <li>1a852426-422a-48db-a618-c906ac4c8e6c
     * </ul>
     */
    public static final Concept COMPONENT_VERSIONS_FIELD = Concept.make("Component versions field", UUID.fromString("1a852426-422a-48db-a618-c906ac4c8e6c"));

    /**
     * Java binding for the concept described as Path concept (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1b9d9f95-fc0a-55ac-b2e6-7c8b37660046
     * </ul>
     */
    public static final Concept PATH_CONCEPT = Concept.make("Path concept (SOLOR)", UUID.fromString("1b9d9f95-fc0a-55ac-b2e6-7c8b37660046"));

    /**
     * Java binding for the concept described as Gretel (User) and identified by the following UUID(s):
     * <ul>
     * <li>1c0023ed-559e-3311-9e55-bd4bd9e5628f
     * </ul>
     */
    public static final Concept GRETEL_OPENPARENTHESIS_USER_CLOSEPARENTHESIS_ = Concept.make("Gretel (User)", UUID.fromString("1c0023ed-559e-3311-9e55-bd4bd9e5628f"));

    /**
     * Java binding for the concept described as Digraph for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1cdacc80-0dea-580f-a77b-8a6b273eb673
     * </ul>
     */
    public static final Concept DIGRAPH_FOR_LOGIC_COORDINATE = Concept.make("Digraph for logic coordinate (SOLOR)", UUID.fromString("1cdacc80-0dea-580f-a77b-8a6b273eb673"));

    /**
     * Java binding for the concept described as Signed integer (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c
     * </ul>
     */
    public static final Concept SIGNED_INTEGER = Concept.make("Signed integer (SOLOR)", UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c"));

    /**
     * Java binding for the concept described as Development path and identified by the following UUID(s):
     * <ul>
     * <li>1f200ca6-960e-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept DEVELOPMENT_PATH = Concept.make("Development path", UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Master path and identified by the following UUID(s):
     * <ul>
     * <li>1f20134a-960e-11e5-8994-feff819cdc9f
     * <li>2faa9260-8fb2-11db-b606-0800200c9a66
     * </ul>
     */
    public static final Concept MASTER_PATH = Concept.make("Master path", UUID.fromString("1f20134a-960e-11e5-8994-feff819cdc9f"), UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));

    /**
     * Java binding for the concept described as EL++ logic profile (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1f201e12-960e-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_LOGIC_PROFILE = Concept.make("EL++ logic profile (SOLOR)", UUID.fromString("1f201e12-960e-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as SnoRocket classifier (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1f201fac-960e-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept SNOROCKET_CLASSIFIER = Concept.make("SnoRocket classifier (SOLOR)", UUID.fromString("1f201fac-960e-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Logic coordinate properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1fa63819-5ac1-5938-95b1-47871a5f2b17
     * </ul>
     */
    public static final Concept LOGIC_COORDINATE_PROPERTIES = Concept.make("Logic coordinate properties (SOLOR)", UUID.fromString("1fa63819-5ac1-5938-95b1-47871a5f2b17"));

    /**
     * Java binding for the concept described as Instant literal (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>1fbf42e2-42b7-591f-b7fd-ba5de659529e
     * </ul>
     */
    public static final Concept INSTANT_LITERAL = Concept.make("Instant literal (SOLOR)", UUID.fromString("1fbf42e2-42b7-591f-b7fd-ba5de659529e"));

    /**
     * Java binding for the concept described as Axioms and identified by the following UUID(s):
     * <ul>
     * <li>20746b91-521a-45a6-89da-0fe32384a67f
     * </ul>
     */
    public static final Concept AXIOMS = Concept.make("Axioms", UUID.fromString("20746b91-521a-45a6-89da-0fe32384a67f"));

    /**
     * Java binding for the concept described as Literal value (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>208a40a7-e615-5efa-9de0-2e2a5a8488b7
     * </ul>
     */
    public static final Concept LITERAL_VALUE = Concept.make("Literal value (SOLOR)", UUID.fromString("208a40a7-e615-5efa-9de0-2e2a5a8488b7"));

    /**
     * Java binding for the concept described as Path options for edit coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>2110c10c-9174-55aa-8ffe-91650c77d0b3
     * </ul>
     */
    public static final Concept PATH_OPTIONS_FOR_EDIT_COORDINATE = Concept.make("Path options for edit coordinate (SOLOR)", UUID.fromString("2110c10c-9174-55aa-8ffe-91650c77d0b3"));

    /**
     * Java binding for the concept described as Dutch language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>21d11bd1-3dab-5034-9625-81b9ae2bd8e7
     * <li>45022280-9567-11e5-8994-feff819cdc9f
     * <li>674ad858-0224-3f90-bcf0-bc4cab753d2d
     * </ul>
     */
    public static final Concept DUTCH_LANGUAGE = Concept.make("Dutch language (SOLOR)", UUID.fromString("21d11bd1-3dab-5034-9625-81b9ae2bd8e7"), UUID.fromString("45022280-9567-11e5-8994-feff819cdc9f"), UUID.fromString("674ad858-0224-3f90-bcf0-bc4cab753d2d"));

    /**
     * Java binding for the concept described as Include Lower Bound and identified by the following UUID(s):
     * <ul>
     * <li>2300a210-d722-48af-8c36-118a3f980312
     * </ul>
     */
    public static final Concept INCLUDE_LOWER_BOUND = Concept.make("Include Lower Bound", UUID.fromString("2300a210-d722-48af-8c36-118a3f980312"));

    /**
     * Java binding for the concept described as Concept substitution (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>23483990-b738-5f43-bc03-befd43928a37
     * </ul>
     */
    public static final Concept CONCEPT_SUBSTITUTION = Concept.make("Concept substitution (SOLOR)", UUID.fromString("23483990-b738-5f43-bc03-befd43928a37"));

    /**
     * Java binding for the concept described as El profile set operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>2352b7a2-11fd-5a68-8ece-fcb3b36570da
     * </ul>
     */
    public static final Concept EL_PROFILE_SET_OPERATOR = Concept.make("El profile set operator (SOLOR)", UUID.fromString("2352b7a2-11fd-5a68-8ece-fcb3b36570da"));

    /**
     * Java binding for the concept described as Allowed states for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>23f69f6f-a502-5876-a835-2b1b4d5ce91e
     * </ul>
     */
    public static final Concept ALLOWED_STATES_FOR_STAMP_COORDINATE = Concept.make("Allowed states for stamp coordinate (SOLOR)", UUID.fromString("23f69f6f-a502-5876-a835-2b1b4d5ce91e"));

    /**
     * Java binding for the concept described as Preferred (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>266f1bc3-3361-39f3-bffe-69db9daea56e
     * </ul>
     */
    public static final Concept PREFERRED = Concept.make("Preferred (SOLOR)", UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));

    /**
     * Java binding for the concept described as Laterality (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>26ca4590-bbe5-327c-a40a-ba56dc86996b
     * </ul>
     */
    public static final Concept LATERALITY = Concept.make("Laterality (SOLOR)", UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));

    /**
     * Java binding for the concept described as String field and identified by the following UUID(s):
     * <ul>
     * <li>27d3905b-b19a-41ff-bed1-fc55f49f8ce4
     * </ul>
     */
    public static final Concept STRING_FIELD = Concept.make("String field", UUID.fromString("27d3905b-b19a-41ff-bed1-fc55f49f8ce4"));

    /**
     * Java binding for the concept described as Stated Definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>28608bd3-ac73-4fe8-a5f0-1efe0d6650a8
     * </ul>
     */
    public static final Concept STATED_DEFINITION = Concept.make("Stated Definition (SOLOR)", UUID.fromString("28608bd3-ac73-4fe8-a5f0-1efe0d6650a8"));

    /**
     * Java binding for the concept described as Chronicle properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>2ba2ef47-30af-57ec-9073-38693f020d7e
     * </ul>
     */
    public static final Concept CHRONICLE_PROPERTIES = Concept.make("Chronicle properties (SOLOR)", UUID.fromString("2ba2ef47-30af-57ec-9073-38693f020d7e"));

    /**
     * Java binding for the concept described as Or (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>2c940bcf-22a8-5fc9-b232-580021e758ed
     * </ul>
     */
    public static final Concept OR = Concept.make("Or (SOLOR)", UUID.fromString("2c940bcf-22a8-5fc9-b232-580021e758ed"));

    /**
     * Java binding for the concept described as UUID data type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>2faa9262-8fb2-11db-b606-0800200c9a66
     * <li>680f3f6c-7a2a-365d-b527-8c9a96dd1a94
     * </ul>
     */
    public static final Concept UUID_DATA_TYPE = Concept.make("UUID data type (SOLOR)", UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66"), UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));

    /**
     * Java binding for the concept described as Russian dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>300126d1-2604-579f-a59b-e3c1179a173a
     * </ul>
     */
    public static final Concept RUSSIAN_DIALECT = Concept.make("Russian dialect (SOLOR)", UUID.fromString("300126d1-2604-579f-a59b-e3c1179a173a"));

    /**
     * Java binding for the concept described as Position on path (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>31173582-a49d-51c6-813f-f42d0976aaea
     * </ul>
     */
    public static final Concept POSITION_ON_PATH = Concept.make("Position on path (SOLOR)", UUID.fromString("31173582-a49d-51c6-813f-f42d0976aaea"));

    /**
     * Java binding for the concept described as Polish dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>315cd879-1557-5a30-b325-a5d3df9e1c2b
     * </ul>
     */
    public static final Concept POLISH_DIALECT = Concept.make("Polish dialect (SOLOR)", UUID.fromString("315cd879-1557-5a30-b325-a5d3df9e1c2b"));

    /**
     * Java binding for the concept described as Array (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>318622e6-dd7a-5651-851d-2d5c2af85767
     * </ul>
     */
    public static final Concept ARRAY = Concept.make("Array (SOLOR)", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767"));

    /**
     * Java binding for the concept described as DiTree display field and identified by the following UUID(s):
     * <ul>
     * <li>32f64fc6-5371-11eb-ae93-0242ac130002
     * </ul>
     */
    public static final Concept DITREE_DISPLAY_FIELD = Concept.make("DiTree display field", UUID.fromString("32f64fc6-5371-11eb-ae93-0242ac130002"));

    /**
     * Java binding for the concept described as Author for edit coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>337e93ba-531b-59a4-8153-57dca00e58d2
     * </ul>
     */
    public static final Concept AUTHOR_FOR_EDIT_COORDINATE = Concept.make("Author for edit coordinate (SOLOR)", UUID.fromString("337e93ba-531b-59a4-8153-57dca00e58d2"));

    /**
     * Java binding for the concept described as Czech language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>33aa2d26-0541-557c-b796-904cbf245101
     * </ul>
     */
    public static final Concept CZECH_LANGUAGE = Concept.make("Czech language (SOLOR)", UUID.fromString("33aa2d26-0541-557c-b796-904cbf245101"));

    /**
     * Java binding for the concept described as Creative Commons BY license (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3415a972-7850-57cd-aa86-a572ca1c2ceb
     * </ul>
     */
    public static final Concept CREATIVE_COMMONS_BY_LICENSE = Concept.make("Creative Commons BY license (SOLOR)", UUID.fromString("3415a972-7850-57cd-aa86-a572ca1c2ceb"));

    /**
     * Java binding for the concept described as Vertex state set (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>347cd3f2-8130-5032-8960-091e194e9afe
     * </ul>
     */
    public static final Concept VERTEX_STATE_SET = Concept.make("Vertex state set (SOLOR)", UUID.fromString("347cd3f2-8130-5032-8960-091e194e9afe"));

    /**
     * Java binding for the concept described as Users module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>349161ba-9a6a-5c9c-a78f-281f19cfc057
     * </ul>
     */
    public static final Concept USERS_MODULE = Concept.make("Users module (SOLOR)", UUID.fromString("349161ba-9a6a-5c9c-a78f-281f19cfc057"));

    /**
     * Java binding for the concept described as Destination module for edit coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>349cfd1d-10fd-5f8d-a0a5-d5ef0932b4da
     * </ul>
     */
    public static final Concept DESTINATION_MODULE_FOR_EDIT_COORDINATE = Concept.make("Destination module for edit coordinate (SOLOR)", UUID.fromString("349cfd1d-10fd-5f8d-a0a5-d5ef0932b4da"));

    /**
     * Java binding for the concept described as KOMET module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>34a6dae3-e5e9-50db-a9ee-69c1067911d8
     * </ul>
     */
    public static final Concept KOMET_MODULE = Concept.make("KOMET module (SOLOR)", UUID.fromString("34a6dae3-e5e9-50db-a9ee-69c1067911d8"));

    /**
     * Java binding for the concept described as Description core type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>351955ff-30f4-5806-a0a5-5dda79756377
     * </ul>
     */
    public static final Concept DESCRIPTION_CORE_TYPE = Concept.make("Description core type (SOLOR)", UUID.fromString("351955ff-30f4-5806-a0a5-5dda79756377"));

    /**
     * Java binding for the concept described as Pattern purpose field and identified by the following UUID(s):
     * <ul>
     * <li>352c821b-7a11-454c-a127-48ad3206573d
     * </ul>
     */
    public static final Concept PATTERN_PURPOSE_FIELD = Concept.make("Pattern purpose field", UUID.fromString("352c821b-7a11-454c-a127-48ad3206573d"));

    /**
     * Java binding for the concept described as Withdrawn state and identified by the following UUID(s):
     * <ul>
     * <li>35fd4750-6e43-5fa3-ba7f-f2ad376052bc
     * </ul>
     */
    public static final Concept WITHDRAWN_STATE = Concept.make("Withdrawn state", UUID.fromString("35fd4750-6e43-5fa3-ba7f-f2ad376052bc"));

    /**
     * Java binding for the concept described as Logically equivalent to (Solor) and identified by the following UUID(s):
     * <ul>
     * <li>3642d9a3-8e23-5289-836b-366c0b1e2900
     * </ul>
     */
    public static final Concept LOGICALLY_EQUIVALENT_TO_OPENPARENTHESIS_SOLOR_CLOSEPARENTHESIS_ = Concept.make("Logically equivalent to (Solor)", UUID.fromString("3642d9a3-8e23-5289-836b-366c0b1e2900"));

    /**
     * Java binding for the concept described as Reference Range Minimum (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>37c35a88-9e27-42ca-b626-186773c4b612
     * </ul>
     */
    public static final Concept REFERENCE_RANGE_MINIMUM = Concept.make("Reference Range Minimum (SOLOR)", UUID.fromString("37c35a88-9e27-42ca-b626-186773c4b612"));

    /**
     * Java binding for the concept described as Language nid for language coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>38e0c7b8-1e33-56a2-9eb2-ee20c4960684
     * </ul>
     */
    public static final Concept LANGUAGE_NID_FOR_LANGUAGE_COORDINATE = Concept.make("Language nid for language coordinate (SOLOR)", UUID.fromString("38e0c7b8-1e33-56a2-9eb2-ee20c4960684"));

    /**
     * Java binding for the concept described as Concept versions field and identified by the following UUID(s):
     * <ul>
     * <li>3a08b5f1-f17e-4db5-8cf9-c6540f26f241
     * </ul>
     */
    public static final Concept CONCEPT_VERSIONS_FIELD = Concept.make("Concept versions field", UUID.fromString("3a08b5f1-f17e-4db5-8cf9-c6540f26f241"));

    /**
     * Java binding for the concept described as Stated premise type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3b0dbd3b-2e53-3a30-8576-6c7fa7773060
     * <li>3fde38f6-e079-3cdc-a819-eda3ec74732d
     * </ul>
     */
    public static final Concept STATED_PREMISE_TYPE = Concept.make("Stated premise type (SOLOR)", UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"), UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));

    /**
     * Java binding for the concept described as STAMP field and identified by the following UUID(s):
     * <ul>
     * <li>3d821e64-a2ee-4414-8949-1bc92ef5d5b6
     * </ul>
     */
    public static final Concept STAMP_FIELD = Concept.make("STAMP field", UUID.fromString("3d821e64-a2ee-4414-8949-1bc92ef5d5b6"));

    /**
     * Java binding for the concept described as Semantic type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3daac6c4-78c5-5271-9c63-6e28f80e0c52
     * </ul>
     */
    public static final Concept SEMANTIC_TYPE = Concept.make("Semantic type (SOLOR)", UUID.fromString("3daac6c4-78c5-5271-9c63-6e28f80e0c52"));

    /**
     * Java binding for the concept described as Vertex display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3e56c6b6-5371-11eb-ae93-0242ac130002
     * </ul>
     */
    public static final Concept VERTEX_DISPLAY_FIELD = Concept.make("Vertex display field (SOLOR)", UUID.fromString("3e56c6b6-5371-11eb-ae93-0242ac130002"));

    /**
     * Java binding for the concept described as Object Properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3ef4311c-70c0-5149-9e06-53d745f85b15
     * </ul>
     */
    public static final Concept OBJECT_PROPERTIES = Concept.make("Object Properties (SOLOR)", UUID.fromString("3ef4311c-70c0-5149-9e06-53d745f85b15"));

    /**
     * Java binding for the concept described as Connective operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3fdcaadc-d972-58e9-84f1-b3a39903b076
     * </ul>
     */
    public static final Concept CONNECTIVE_OPERATOR = Concept.make("Connective operator (SOLOR)", UUID.fromString("3fdcaadc-d972-58e9-84f1-b3a39903b076"));

    /**
     * Java binding for the concept described as Module exclusion set for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3fe047f0-33b0-5254-91c2-43e65f90d30b
     * </ul>
     */
    public static final Concept MODULE_EXCLUSION_SET_FOR_STAMP_COORDINATE = Concept.make("Module exclusion set for stamp coordinate (SOLOR)", UUID.fromString("3fe047f0-33b0-5254-91c2-43e65f90d30b"));

    /**
     * Java binding for the concept described as Annotation type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>3fe77951-58c9-51b3-8e7e-65edcf7ace0a
     * </ul>
     */
    public static final Concept ANNOTATION_TYPE = Concept.make("Annotation type (SOLOR)", UUID.fromString("3fe77951-58c9-51b3-8e7e-65edcf7ace0a"));

    /**
     * Java binding for the concept described as Unit of Measure and identified by the following UUID(s):
     * <ul>
     * <li>40afdda5-89d6-4b80-8181-1ddd6eb92dc8
     * </ul>
     */
    public static final Concept UNIT_OF_MEASURE = Concept.make("Unit of Measure", UUID.fromString("40afdda5-89d6-4b80-8181-1ddd6eb92dc8"));

    /**
     * Java binding for the concept described as Module and identified by the following UUID(s):
     * <ul>
     * <li>40d1c869-b509-32f8-b735-836eac577a67
     * </ul>
     */
    public static final Concept MODULE = Concept.make("Module", UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));

    /**
     * Java binding for the concept described as Semantic referenced component field and identified by the following UUID(s):
     * <ul>
     * <li>4111ba1e-c818-4c5d-9fed-34d07298d009
     * </ul>
     */
    public static final Concept SEMANTIC_REFERENCED_COMPONENT_FIELD = Concept.make("Semantic referenced component field", UUID.fromString("4111ba1e-c818-4c5d-9fed-34d07298d009"));

    /**
     * Java binding for the concept described as View coordinate key (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4211cf36-bd75-586a-805c-51f059e2eaaa
     * </ul>
     */
    public static final Concept VIEW_COORDINATE_KEY = Concept.make("View coordinate key (SOLOR)", UUID.fromString("4211cf36-bd75-586a-805c-51f059e2eaaa"));

    /**
     * Java binding for the concept described as Boolean field and identified by the following UUID(s):
     * <ul>
     * <li>4229683e-8772-4936-abd5-edc5a180f4d1
     * </ul>
     */
    public static final Concept BOOLEAN_FIELD = Concept.make("Boolean field", UUID.fromString("4229683e-8772-4936-abd5-edc5a180f4d1"));

    /**
     * Java binding for the concept described as Language coordinate name (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>42dff20f-5ed2-559a-91ad-91d44a573c63
     * </ul>
     */
    public static final Concept LANGUAGE_COORDINATE_NAME = Concept.make("Language coordinate name (SOLOR)", UUID.fromString("42dff20f-5ed2-559a-91ad-91d44a573c63"));

    /**
     * Java binding for the concept described as Path and identified by the following UUID(s):
     * <ul>
     * <li>4459d8cf-5a6f-3952-9458-6d64324b27b7
     * </ul>
     */
    public static final Concept PATH = Concept.make("Path", UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));

    /**
     * Java binding for the concept described as Description type preference list for language coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>44c7eab6-fdb8-5427-9d7a-52ab63f7a6f9
     * </ul>
     */
    public static final Concept DESCRIPTION_TYPE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE = Concept.make("Description type preference list for language coordinate (SOLOR)", UUID.fromString("44c7eab6-fdb8-5427-9d7a-52ab63f7a6f9"));

    /**
     * Java binding for the concept described as Inverse tree list (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>45167fb6-e01d-53a8-8be3-768aae18729d
     * </ul>
     */
    public static final Concept INVERSE_TREE_LIST = Concept.make("Inverse tree list (SOLOR)", UUID.fromString("45167fb6-e01d-53a8-8be3-768aae18729d"));

    /**
     * Java binding for the concept described as Module origins (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>462862d4-5df9-426e-b785-a1264e24769f
     * </ul>
     */
    public static final Concept MODULE_ORIGINS = Concept.make("Module origins (SOLOR)", UUID.fromString("462862d4-5df9-426e-b785-a1264e24769f"));

    /**
     * Java binding for the concept described as Role and identified by the following UUID(s):
     * <ul>
     * <li>46ae9325-dd24-5008-8fda-80cf1f0977c7
     * </ul>
     */
    public static final Concept ROLE = Concept.make("Role", UUID.fromString("46ae9325-dd24-5008-8fda-80cf1f0977c7"));

    /**
     * Java binding for the concept described as Directed graph (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>47a787a7-bdce-528d-bfcc-fde1add8d599
     * </ul>
     */
    public static final Concept DIRECTED_GRAPH = Concept.make("Directed graph (SOLOR)", UUID.fromString("47a787a7-bdce-528d-bfcc-fde1add8d599"));

    /**
     * Java binding for the concept described as Query clauses (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4905348c-ba1d-58ae-821f-52877d9acee3
     * </ul>
     */
    public static final Concept QUERY_CLAUSES = Concept.make("Query clauses (SOLOR)", UUID.fromString("4905348c-ba1d-58ae-821f-52877d9acee3"));

    /**
     * Java binding for the concept described as Boolean literal (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>49f41695-66a7-5471-846d-21c168f54c19
     * </ul>
     */
    public static final Concept BOOLEAN_LITERAL = Concept.make("Boolean literal (SOLOR)", UUID.fromString("49f41695-66a7-5471-846d-21c168f54c19"));

    /**
     * Java binding for the concept described as Default Data Concept and identified by the following UUID(s):
     * <ul>
     * <li>4a32d2ad-baca-42b5-a432-4c4ae6431668
     * </ul>
     */
    public static final Concept DEFAULT_DATA_CONCEPT = Concept.make("Default Data Concept", UUID.fromString("4a32d2ad-baca-42b5-a432-4c4ae6431668"));

    /**
     * Java binding for the concept described as Classifier for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4b90e89d-2a0e-5ca3-8ae5-7498d148a9d2
     * </ul>
     */
    public static final Concept CLASSIFIER_FOR_LOGIC_COORDINATE = Concept.make("Classifier for logic coordinate (SOLOR)", UUID.fromString("4b90e89d-2a0e-5ca3-8ae5-7498d148a9d2"));

    /**
     * Java binding for the concept described as Inferred navigation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4bc6c333-7fc9-52f1-942d-f8decba19dc2
     * </ul>
     */
    public static final Concept INFERRED_NAVIGATION = Concept.make("Inferred navigation (SOLOR)", UUID.fromString("4bc6c333-7fc9-52f1-942d-f8decba19dc2"));

    /**
     * Java binding for the concept described as Unmodeled role concept (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4be7118f-e6ab-5dc7-bcba-b2cc8b028492
     * </ul>
     */
    public static final Concept UNMODELED_ROLE_CONCEPT = Concept.make("Unmodeled role concept (SOLOR)", UUID.fromString("4be7118f-e6ab-5dc7-bcba-b2cc8b028492"));

    /**
     * Java binding for the concept described as Navigation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4d9707d8-adf0-5b15-89fc-039e4ff6fec8
     * </ul>
     */
    public static final Concept NAVIGATION = Concept.make("Navigation (SOLOR)", UUID.fromString("4d9707d8-adf0-5b15-89fc-039e4ff6fec8"));

    /**
     * Java binding for the concept described as Display Fields and identified by the following UUID(s):
     * <ul>
     * <li>4e627b9c-cecb-5563-82fc-cb0ee25113b1
     * </ul>
     */
    public static final Concept DISPLAY_FIELDS = Concept.make("Display Fields", UUID.fromString("4e627b9c-cecb-5563-82fc-cb0ee25113b1"));

    /**
     * Java binding for the concept described as Author for version (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4eb9de0d-7486-5f18-a9b4-82e3432f4103
     * </ul>
     */
    public static final Concept AUTHOR_FOR_VERSION = Concept.make("Author for version (SOLOR)", UUID.fromString("4eb9de0d-7486-5f18-a9b4-82e3432f4103"));

    /**
     * Java binding for the concept described as Membership semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4fa29287-a80e-5f83-abab-4b587973e7b7
     * </ul>
     */
    public static final Concept MEMBERSHIP_SEMANTIC = Concept.make("Membership semantic (SOLOR)", UUID.fromString("4fa29287-a80e-5f83-abab-4b587973e7b7"));

    /**
     * Java binding for the concept described as Semantic versions set and identified by the following UUID(s):
     * <ul>
     * <li>4fd69aed-556f-4938-94cc-ea7ea707ccef
     * </ul>
     */
    public static final Concept SEMANTIC_VERSIONS_SET = Concept.make("Semantic versions set", UUID.fromString("4fd69aed-556f-4938-94cc-ea7ea707ccef"));

    /**
     * Java binding for the concept described as Author for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>4fda23b8-b016-5d2a-97d5-7ff779d60701
     * </ul>
     */
    public static final Concept AUTHOR_FOR_STAMP_COORDINATE = Concept.make("Author for stamp coordinate (SOLOR)", UUID.fromString("4fda23b8-b016-5d2a-97d5-7ff779d60701"));

    /**
     * Java binding for the concept described as Temporal Axiom and identified by the following UUID(s):
     * <ul>
     * <li>5144d836-18d8-4881-a377-2d4640b710a9
     * </ul>
     */
    public static final Concept TEMPORAL_AXIOM = Concept.make("Temporal Axiom", UUID.fromString("5144d836-18d8-4881-a377-2d4640b710a9"));

    /**
     * Java binding for the concept described as Development module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>529a7069-bd33-59e6-b2ce-537fa874360a
     * </ul>
     */
    public static final Concept DEVELOPMENT_MODULE = Concept.make("Development module (SOLOR)", UUID.fromString("529a7069-bd33-59e6-b2ce-537fa874360a"));

    /**
     * Java binding for the concept described as Interval Lower Bound and identified by the following UUID(s):
     * <ul>
     * <li>52b3e38a-fccb-4779-aa61-4e87abd56419
     * </ul>
     */
    public static final Concept INTERVAL_LOWER_BOUND = Concept.make("Interval Lower Bound", UUID.fromString("52b3e38a-fccb-4779-aa61-4e87abd56419"));

    /**
     * Java binding for the concept described as Transitive Feature (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>53f866d0-fd61-5c85-a16c-150bd619a0ac
     * </ul>
     */
    public static final Concept TRANSITIVE_FEATURE = Concept.make("Transitive Feature (SOLOR)", UUID.fromString("53f866d0-fd61-5c85-a16c-150bd619a0ac"));

    /**
     * Java binding for the concept described as Component versions set and identified by the following UUID(s):
     * <ul>
     * <li>54d670f1-234d-485a-a354-e1fa7eea1bf2
     * </ul>
     */
    public static final Concept COMPONENT_VERSIONS_SET = Concept.make("Component versions set", UUID.fromString("54d670f1-234d-485a-a354-e1fa7eea1bf2"));

    /**
     * Java binding for the concept described as Uninitialized Component (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>55f74246-0a25-57ac-9473-a788d08fb656
     * </ul>
     */
    public static final Concept UNINITIALIZED_COMPONENT = Concept.make("Uninitialized Component (SOLOR)", UUID.fromString("55f74246-0a25-57ac-9473-a788d08fb656"));

    /**
     * Java binding for the concept described as Instant substitution (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>56599345-31c5-5817-9d36-57dd3a626b3a
     * </ul>
     */
    public static final Concept INSTANT_SUBSTITUTION = Concept.make("Instant substitution (SOLOR)", UUID.fromString("56599345-31c5-5817-9d36-57dd3a626b3a"));

    /**
     * Java binding for the concept described as Case significance concept nid for description (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>57271621-3f3c-58dd-8148-2674bc11b7e5
     * </ul>
     */
    public static final Concept CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION = Concept.make("Case significance concept nid for description (SOLOR)", UUID.fromString("57271621-3f3c-58dd-8148-2674bc11b7e5"));

    /**
     * Java binding for the concept described as Irish language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>58e82fc4-1492-5cf8-8997-43800360bbd6
     * </ul>
     */
    public static final Concept IRISH_LANGUAGE = Concept.make("Irish language (SOLOR)", UUID.fromString("58e82fc4-1492-5cf8-8997-43800360bbd6"));

    /**
     * Java binding for the concept described as Extended description type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>5a2e7786-3e41-11dc-8314-0800200c9a66
     * </ul>
     */
    public static final Concept EXTENDED_DESCRIPTION_TYPE = Concept.make("Extended description type (SOLOR)", UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66"));

    /**
     * Java binding for the concept described as Identifier Source and identified by the following UUID(s):
     * <ul>
     * <li>5a87935c-d654-548f-82a2-0c06e3801162
     * </ul>
     */
    public static final Concept IDENTIFIER_SOURCE = Concept.make("Identifier Source", UUID.fromString("5a87935c-d654-548f-82a2-0c06e3801162"));

    /**
     * Java binding for the concept described as Equal to (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>5c9b5844-1434-5111-83d5-cb7cb0be12d9
     * </ul>
     */
    public static final Concept EQUAL_TO = Concept.make("Equal to (SOLOR)", UUID.fromString("5c9b5844-1434-5111-83d5-cb7cb0be12d9"));

    /**
     * Java binding for the concept described as Feature (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>5e76a88e-794a-5fdd-8eb2-4a9e4b1386b6
     * </ul>
     */
    public static final Concept FEATURE = Concept.make("Feature (SOLOR)", UUID.fromString("5e76a88e-794a-5fdd-8eb2-4a9e4b1386b6"));

    /**
     * Java binding for the concept described as KOMET user list (SOLOR and identified by the following UUID(s):
     * <ul>
     * <li>5e77558d-97d0-52b6-adf0-d54beb97b3a6
     * </ul>
     */
    public static final Concept KOMET_USER_LIST_OPENPARENTHESIS_SOLOR = Concept.make("KOMET user list (SOLOR", UUID.fromString("5e77558d-97d0-52b6-adf0-d54beb97b3a6"));

    /**
     * Java binding for the concept described as German Language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>5f144b18-76a8-5c7e-8480-55a5030d707f
     * </ul>
     */
    public static final Concept GERMAN_LANGUAGE = Concept.make("German Language (SOLOR)", UUID.fromString("5f144b18-76a8-5c7e-8480-55a5030d707f"));

    /**
     * Java binding for the concept described as DiGraph display field and identified by the following UUID(s):
     * <ul>
     * <li>60113dfe-2bad-11eb-adc1-0242ac120002
     * </ul>
     */
    public static final Concept DIGRAPH_DISPLAY_FIELD = Concept.make("DiGraph display field", UUID.fromString("60113dfe-2bad-11eb-adc1-0242ac120002"));

    /**
     * Java binding for the concept described as Stated navigation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>614017af-9903-53d9-aab4-15fd02193dce
     * </ul>
     */
    public static final Concept STATED_NAVIGATION = Concept.make("Stated navigation (SOLOR)", UUID.fromString("614017af-9903-53d9-aab4-15fd02193dce"));

    /**
     * Java binding for the concept described as Order for concept attachments  (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6167efcb-50e8-534d-9827-fdd60b02ae00
     * </ul>
     */
    public static final Concept ORDER_FOR_CONCEPT_ATTACHMENTS_ = Concept.make("Order for concept attachments  (SOLOR)", UUID.fromString("6167efcb-50e8-534d-9827-fdd60b02ae00"));

    /**
     * Java binding for the concept described as KOMET user (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>61c1a544-2acf-58cd-8cc0-9ac581d4227e
     * </ul>
     */
    public static final Concept KOMET_USER = Concept.make("KOMET user (SOLOR)", UUID.fromString("61c1a544-2acf-58cd-8cc0-9ac581d4227e"));

    /**
     * Java binding for the concept described as Dynamic column data types (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>61da7e50-f606-5ba0-a0df-83fd524951e7
     * </ul>
     */
    public static final Concept DYNAMIC_COLUMN_DATA_TYPES = Concept.make("Dynamic column data types (SOLOR)", UUID.fromString("61da7e50-f606-5ba0-a0df-83fd524951e7"));

    /**
     * Java binding for the concept described as Interval Upper Bound and identified by the following UUID(s):
     * <ul>
     * <li>6565f774-ff6c-4882-832f-31ddc462adf7
     * </ul>
     */
    public static final Concept INTERVAL_UPPER_BOUND = Concept.make("Interval Upper Bound", UUID.fromString("6565f774-ff6c-4882-832f-31ddc462adf7"));

    /**
     * Java binding for the concept described as Greater than (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>65af466b-360c-58b2-8b7d-2854150029a8
     * </ul>
     */
    public static final Concept GREATER_THAN = Concept.make("Greater than (SOLOR)", UUID.fromString("65af466b-360c-58b2-8b7d-2854150029a8"));

    /**
     * Java binding for the concept described as Has Active Ingredient (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>65bf3b7f-c854-36b5-81c3-4915461020a8
     * </ul>
     */
    public static final Concept HAS_ACTIVE_INGREDIENT = Concept.make("Has Active Ingredient (SOLOR)", UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));

    /**
     * Java binding for the concept described as Path field and identified by the following UUID(s):
     * <ul>
     * <li>6622a391-e2e6-45a0-97e1-c58cd0184092
     * </ul>
     */
    public static final Concept PATH_FIELD = Concept.make("Path field", UUID.fromString("6622a391-e2e6-45a0-97e1-c58cd0184092"));

    /**
     * Java binding for the concept described as Module for version (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>67cd64f1-96d7-5110-b847-556c055ac063
     * </ul>
     */
    public static final Concept MODULE_FOR_VERSION = Concept.make("Module for version (SOLOR)", UUID.fromString("67cd64f1-96d7-5110-b847-556c055ac063"));

    /**
     * Java binding for the concept described as Czech dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6979e268-0b59-542f-bac0-313c5ddf6a2e
     * </ul>
     */
    public static final Concept CZECH_DIALECT = Concept.make("Czech dialect (SOLOR)", UUID.fromString("6979e268-0b59-542f-bac0-313c5ddf6a2e"));

    /**
     * Java binding for the concept described as Order for description attachments (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>69ee3f13-e2ba-5a96-9b91-5eecfad8e587
     * </ul>
     */
    public static final Concept ORDER_FOR_DESCRIPTION_ATTACHMENTS = Concept.make("Order for description attachments (SOLOR)", UUID.fromString("69ee3f13-e2ba-5a96-9b91-5eecfad8e587"));

    /**
     * Java binding for the concept described as Data property set and identified by the following UUID(s):
     * <ul>
     * <li>6b8ed642-de72-4aee-953d-42e5db92c0ab
     * </ul>
     */
    public static final Concept DATA_PROPERTY_SET = Concept.make("Data property set", UUID.fromString("6b8ed642-de72-4aee-953d-42e5db92c0ab"));

    /**
     * Java binding for the concept described as Sufficient concept definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6d9cd46e-8a8f-310a-a298-3e55dcf7a986
     * </ul>
     */
    public static final Concept SUFFICIENT_CONCEPT_DEFINITION = Concept.make("Sufficient concept definition (SOLOR)", UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986"));

    /**
     * Java binding for the concept described as Less than or equal to (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6dfacbd5-8344-5794-9fda-bec95b2aa6c9
     * </ul>
     */
    public static final Concept LESS_THAN_OR_EQUAL_TO = Concept.make("Less than or equal to (SOLOR)", UUID.fromString("6dfacbd5-8344-5794-9fda-bec95b2aa6c9"));

    /**
     * Java binding for the concept described as US Nursing dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6e447636-1085-32ff-bc36-6748a45255de
     * </ul>
     */
    public static final Concept US_NURSING_DIALECT = Concept.make("US Nursing dialect (SOLOR)", UUID.fromString("6e447636-1085-32ff-bc36-6748a45255de"));

    /**
     * Java binding for the concept described as Path origins (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6e6a112e-7d8c-53c7-aaf1-c46e2d69743c
     * </ul>
     */
    public static final Concept PATH_ORIGINS = Concept.make("Path origins (SOLOR)", UUID.fromString("6e6a112e-7d8c-53c7-aaf1-c46e2d69743c"));

    /**
     * Java binding for the concept described as Description focus (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6edf734d-7f57-5430-9164-6ee0824fd94b
     * </ul>
     */
    public static final Concept DESCRIPTION_FOCUS = Concept.make("Description focus (SOLOR)", UUID.fromString("6edf734d-7f57-5430-9164-6ee0824fd94b"));

    /**
     * Java binding for the concept described as Float display field and identified by the following UUID(s):
     * <ul>
     * <li>6efe7087-3e3c-5b45-8109-90d7652b1506
     * </ul>
     */
    public static final Concept FLOAT_DISPLAY_FIELD = Concept.make("Float display field", UUID.fromString("6efe7087-3e3c-5b45-8109-90d7652b1506"));

    /**
     * Java binding for the concept described as Less than (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6f96e8cf-5568-5e49-8a90-aa6c65125ee9
     * </ul>
     */
    public static final Concept LESS_THAN = Concept.make("Less than (SOLOR)", UUID.fromString("6f96e8cf-5568-5e49-8a90-aa6c65125ee9"));

    /**
     * Java binding for the concept described as Interval Role Type and identified by the following UUID(s):
     * <ul>
     * <li>6fa58611-af37-402e-a0c2-6ee1d6068651
     * </ul>
     */
    public static final Concept INTERVAL_ROLE_TYPE = Concept.make("Interval Role Type", UUID.fromString("6fa58611-af37-402e-a0c2-6ee1d6068651"));

    /**
     * Java binding for the concept described as Korean dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>6fb2eb9c-fb9e-5959-802c-fb17bcba3079
     * </ul>
     */
    public static final Concept KOREAN_DIALECT = Concept.make("Korean dialect (SOLOR)", UUID.fromString("6fb2eb9c-fb9e-5959-802c-fb17bcba3079"));

    /**
     * Java binding for the concept described as Definition description type and identified by the following UUID(s):
     * <ul>
     * <li>700546a3-09c7-3fc2-9eb9-53d318659a09
     * </ul>
     */
    public static final Concept DEFINITION_DESCRIPTION_TYPE = Concept.make("Definition description type", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));

    /**
     * Java binding for the concept described as Sandbox path module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>715bd36d-6090-5b37-8ae7-88c9e532010e
     * </ul>
     */
    public static final Concept SANDBOX_PATH_MODULE = Concept.make("Sandbox path module (SOLOR)", UUID.fromString("715bd36d-6090-5b37-8ae7-88c9e532010e"));

    /**
     * Java binding for the concept described as Double (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>7172e6ac-a05a-5a34-8275-aef430b18207
     * </ul>
     */
    public static final Concept DOUBLE = Concept.make("Double (SOLOR)", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207"));

    /**
     * Java binding for the concept described as Uncategorized phenomenon (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>722f5ac8-1f5c-5d8f-96bb-370d79596f66
     * </ul>
     */
    public static final Concept UNCATEGORIZED_PHENOMENON = Concept.make("Uncategorized phenomenon (SOLOR)", UUID.fromString("722f5ac8-1f5c-5d8f-96bb-370d79596f66"));

    /**
     * Java binding for the concept described as Object (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>72765109-6b53-3814-9b05-34ebddd16592
     * </ul>
     */
    public static final Concept OBJECT = Concept.make("Object (SOLOR)", UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));

    /**
     * Java binding for the concept described as Reference Range Maximum (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>72d58983-b1e1-4ca9-833f-0e40c1defd39
     * </ul>
     */
    public static final Concept REFERENCE_RANGE_MAXIMUM = Concept.make("Reference Range Maximum (SOLOR)", UUID.fromString("72d58983-b1e1-4ca9-833f-0e40c1defd39"));

    /**
     * Java binding for the concept described as Description version properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>732aad24-4add-59d6-bbc9-840a8b9dc754
     * </ul>
     */
    public static final Concept DESCRIPTION_VERSION_PROPERTIES = Concept.make("Description version properties (SOLOR)", UUID.fromString("732aad24-4add-59d6-bbc9-840a8b9dc754"));

    /**
     * Java binding for the concept described as Path for path coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>748e073c-fea7-58dd-8aa3-f18fdd82ddfc
     * </ul>
     */
    public static final Concept PATH_FOR_PATH_COORDINATE = Concept.make("Path for path coordinate (SOLOR)", UUID.fromString("748e073c-fea7-58dd-8aa3-f18fdd82ddfc"));

    /**
     * Java binding for the concept described as Case insensitive evaluation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>74bbdaff-f061-5807-b334-3c88ac3e9421
     * </ul>
     */
    public static final Concept CASE_INSENSITIVE_EVALUATION = Concept.make("Case insensitive evaluation (SOLOR)", UUID.fromString("74bbdaff-f061-5807-b334-3c88ac3e9421"));

    /**
     * Java binding for the concept described as Field definition meaning field and identified by the following UUID(s):
     * <ul>
     * <li>74dffbed-0bef-44a4-8ad6-8cff84fe47ae
     * </ul>
     */
    public static final Concept FIELD_DEFINITION_MEANING_FIELD = Concept.make("Field definition meaning field", UUID.fromString("74dffbed-0bef-44a4-8ad6-8cff84fe47ae"));

    /**
     * Java binding for the concept described as Pattern field and identified by the following UUID(s):
     * <ul>
     * <li>751790c7-e1e4-42bc-b531-54c54bd6eebd
     * </ul>
     */
    public static final Concept PATTERN_FIELD = Concept.make("Pattern field", UUID.fromString("751790c7-e1e4-42bc-b531-54c54bd6eebd"));

    /**
     * Java binding for the concept described as French dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>75d00a0d-8e46-5e42-ad34-3e46269b28a3
     * </ul>
     */
    public static final Concept FRENCH_DIALECT = Concept.make("French dialect (SOLOR)", UUID.fromString("75d00a0d-8e46-5e42-ad34-3e46269b28a3"));

    /**
     * Java binding for the concept described as Role type and identified by the following UUID(s):
     * <ul>
     * <li>76320274-be2a-5ba0-b3e8-e6d2e383ee6a
     * </ul>
     */
    public static final Concept ROLE_TYPE = Concept.make("Role type", UUID.fromString("76320274-be2a-5ba0-b3e8-e6d2e383ee6a"));

    /**
     * Java binding for the concept described as Logic coordinate name (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>78972f14-e0f6-5f72-bf82-59310b5f7b26
     * </ul>
     */
    public static final Concept LOGIC_COORDINATE_NAME = Concept.make("Logic coordinate name (SOLOR)", UUID.fromString("78972f14-e0f6-5f72-bf82-59310b5f7b26"));

    /**
     * Java binding for the concept described as Maximum Value Operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>7b8916ab-fd50-41df-8fc2-0b2a7a78be6d
     * </ul>
     */
    public static final Concept MAXIMUM_VALUE_OPERATOR = Concept.make("Maximum Value Operator (SOLOR)", UUID.fromString("7b8916ab-fd50-41df-8fc2-0b2a7a78be6d"));

    /**
     * Java binding for the concept described as Pattern versions field and identified by the following UUID(s):
     * <ul>
     * <li>7b8ecbbf-55b4-41bc-acbf-51824e74446a
     * </ul>
     */
    public static final Concept PATTERN_VERSIONS_FIELD = Concept.make("Pattern versions field", UUID.fromString("7b8ecbbf-55b4-41bc-acbf-51824e74446a"));

    /**
     * Java binding for the concept described as Model concept and identified by the following UUID(s):
     * <ul>
     * <li>7bbd4210-381c-11e7-9598-0800200c9a66
     * </ul>
     */
    public static final Concept MODEL_CONCEPT = Concept.make("Model concept", UUID.fromString("7bbd4210-381c-11e7-9598-0800200c9a66"));

    /**
     * Java binding for the concept described as Integrated Knowledge Management (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>7c21b6c5-cf11-5af9-893b-743f004c97f5
     * </ul>
     */
    public static final Concept INTEGRATED_KNOWLEDGE_MANAGEMENT = Concept.make("Integrated Knowledge Management (SOLOR)", UUID.fromString("7c21b6c5-cf11-5af9-893b-743f004c97f5"));

    /**
     * Java binding for the concept described as Logical Definition and identified by the following UUID(s):
     * <ul>
     * <li>7dccd042-b0b8-5cec-a1bc-6de676b92f4b
     * </ul>
     */
    public static final Concept LOGICAL_DEFINITION = Concept.make("Logical Definition", UUID.fromString("7dccd042-b0b8-5cec-a1bc-6de676b92f4b"));

    /**
     * Java binding for the concept described as Field value field and identified by the following UUID(s):
     * <ul>
     * <li>7e4a96fc-0522-4d74-a7d1-ca74be3bc236
     * </ul>
     */
    public static final Concept FIELD_VALUE_FIELD = Concept.make("Field value field", UUID.fromString("7e4a96fc-0522-4d74-a7d1-ca74be3bc236"));

    /**
     * Java binding for the concept described as Reflexive Feature (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>7e779e4a-61ed-5c4a-aacc-03cf524b7c73
     * </ul>
     */
    public static final Concept REFLEXIVE_FEATURE = Concept.make("Reflexive Feature (SOLOR)", UUID.fromString("7e779e4a-61ed-5c4a-aacc-03cf524b7c73"));

    /**
     * Java binding for the concept described as Concept versions set and identified by the following UUID(s):
     * <ul>
     * <li>806c7f9f-52f9-4b53-9758-122899b28a76
     * </ul>
     */
    public static final Concept CONCEPT_VERSIONS_SET = Concept.make("Concept versions set", UUID.fromString("806c7f9f-52f9-4b53-9758-122899b28a76"));

    /**
     * Java binding for the concept described as Sandbox path and identified by the following UUID(s):
     * <ul>
     * <li>80710ea6-983c-5fa0-8908-e479f1f03ea9
     * </ul>
     */
    public static final Concept SANDBOX_PATH = Concept.make("Sandbox path", UUID.fromString("80710ea6-983c-5fa0-8908-e479f1f03ea9"));

    /**
     * Java binding for the concept described as Action properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>80ba281c-7d47-57cf-8100-82b69bce998b
     * </ul>
     */
    public static final Concept ACTION_PROPERTIES = Concept.make("Action properties (SOLOR)", UUID.fromString("80ba281c-7d47-57cf-8100-82b69bce998b"));

    /**
     * Java binding for the concept described as Example UCUM Units (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>80cd4978-314d-46e3-bc13-9980280ae955
     * </ul>
     */
    public static final Concept EXAMPLE_UCUM_UNITS = Concept.make("Example UCUM Units (SOLOR)", UUID.fromString("80cd4978-314d-46e3-bc13-9980280ae955"));

    /**
     * Java binding for the concept described as Description semantic and identified by the following UUID(s):
     * <ul>
     * <li>81487d5f-6115-51e2-a3b3-93d783888eb8
     * </ul>
     */
    public static final Concept DESCRIPTION_SEMANTIC = Concept.make("Description semantic", UUID.fromString("81487d5f-6115-51e2-a3b3-93d783888eb8"));

    /**
     * Java binding for the concept described as Concrete value operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>843b0b55-8785-5544-93f6-581da9cf1ff3
     * </ul>
     */
    public static final Concept CONCRETE_VALUE_OPERATOR = Concept.make("Concrete value operator (SOLOR)", UUID.fromString("843b0b55-8785-5544-93f6-581da9cf1ff3"));

    /**
     * Java binding for the concept described as UNIVERSALLY_UNIQUE_IDENTIFIER and identified by the following UUID(s):
     * <ul>
     * <li>845274b5-9644-3799-94c6-e0ea37e7d1a4
     * </ul>
     */
    public static final Concept UNIVERSALLY_UNDERSCORE_UNIQUE_UNDERSCORE_IDENTIFIER = Concept.make("UNIVERSALLY_UNIQUE_IDENTIFIER", UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4"));

    /**
     * Java binding for the concept described as Dialect for dialect/description pair (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>850bc47d-5235-5bce-99f4-c41f8a163d69
     * </ul>
     */
    public static final Concept DIALECT_FOR_DIALECT_FORWARDSLASH_DESCRIPTION_PAIR = Concept.make("Dialect for dialect/description pair (SOLOR)", UUID.fromString("850bc47d-5235-5bce-99f4-c41f8a163d69"));

    /**
     * Java binding for the concept described as Double display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>85ff6e8f-9151-5428-a5f0-e07844b69260
     * </ul>
     */
    public static final Concept DOUBLE_DISPLAY_FIELD = Concept.make("Double display field (SOLOR)", UUID.fromString("85ff6e8f-9151-5428-a5f0-e07844b69260"));

    /**
     * Java binding for the concept described as Root for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>862cc189-bbcb-51a0-89a4-16e1854be247
     * </ul>
     */
    public static final Concept ROOT_FOR_LOGIC_COORDINATE = Concept.make("Root for logic coordinate (SOLOR)", UUID.fromString("862cc189-bbcb-51a0-89a4-16e1854be247"));

    /**
     * Java binding for the concept described as Description and identified by the following UUID(s):
     * <ul>
     * <li>87118daf-d28c-55fb-8657-cd6bc8425600
     * </ul>
     */
    public static final Concept DESCRIPTION = Concept.make("Description", UUID.fromString("87118daf-d28c-55fb-8657-cd6bc8425600"));

    /**
     * Java binding for the concept described as Reference Range (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>87ce975b-309b-47f4-a6c6-4ae6df6649a1
     * </ul>
     */
    public static final Concept REFERENCE_RANGE = Concept.make("Reference Range (SOLOR)", UUID.fromString("87ce975b-309b-47f4-a6c6-4ae6df6649a1"));

    /**
     * Java binding for the concept described as Sufficient set and identified by the following UUID(s):
     * <ul>
     * <li>8aa48cfd-485b-5140-beb9-0d122f7812d9
     * </ul>
     */
    public static final Concept SUFFICIENT_SET = Concept.make("Sufficient set", UUID.fromString("8aa48cfd-485b-5140-beb9-0d122f7812d9"));

    /**
     * Java binding for the concept described as Exact (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8aa6421d-4966-5230-ae5f-aca96ee9c2c1
     * </ul>
     */
    public static final Concept EXACT = Concept.make("Exact (SOLOR)", UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"));

    /**
     * Java binding for the concept described as Referenced component subtype restriction (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8af1045e-1122-5072-9f29-ce7da9337915
     * </ul>
     */
    public static final Concept REFERENCED_COMPONENT_SUBTYPE_RESTRICTION = Concept.make("Referenced component subtype restriction (SOLOR)", UUID.fromString("8af1045e-1122-5072-9f29-ce7da9337915"));

    /**
     * Java binding for the concept described as French Language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e
     * <li>01707e47-5f6d-555e-80af-3c1ffb297eaa
     * <li>45021dbc-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept FRENCH_LANGUAGE = Concept.make("French Language (SOLOR)", UUID.fromString("8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e"), UUID.fromString("01707e47-5f6d-555e-80af-3c1ffb297eaa"), UUID.fromString("45021dbc-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Semantic field and identified by the following UUID(s):
     * <ul>
     * <li>8b6c69d7-a5aa-4db2-bcea-8c7b2817b02f
     * </ul>
     */
    public static final Concept SEMANTIC_FIELD = Concept.make("Semantic field", UUID.fromString("8b6c69d7-a5aa-4db2-bcea-8c7b2817b02f"));

    /**
     * Java binding for the concept described as Component field and identified by the following UUID(s):
     * <ul>
     * <li>8bd36a0c-d05d-46b7-a79a-d11477705cc1
     * </ul>
     */
    public static final Concept COMPONENT_FIELD = Concept.make("Component field", UUID.fromString("8bd36a0c-d05d-46b7-a79a-d11477705cc1"));

    /**
     * Java binding for the concept described as Text for description and identified by the following UUID(s):
     * <ul>
     * <li>8bdcbe5d-e92e-5c10-845e-b585e6061672
     * </ul>
     */
    public static final Concept TEXT_FOR_DESCRIPTION = Concept.make("Text for description", UUID.fromString("8bdcbe5d-e92e-5c10-845e-b585e6061672"));

    /**
     * Java binding for the concept described as Regular name description type and identified by the following UUID(s):
     * <ul>
     * <li>8bfba944-3965-3946-9bcb-1e80a5da63a2
     * <li>d6fad981-7df6-3388-94d8-238cc0465a79
     * </ul>
     */
    public static final Concept REGULAR_NAME_DESCRIPTION_TYPE = Concept.make("Regular name description type", UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"), UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));

    /**
     * Java binding for the concept described as Value Constraint (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8c55fb86-92d8-42a9-ad70-1e23abbf7eec
     * </ul>
     */
    public static final Concept VALUE_CONSTRAINT = Concept.make("Value Constraint (SOLOR)", UUID.fromString("8c55fb86-92d8-42a9-ad70-1e23abbf7eec"));

    /**
     * Java binding for the concept described as Grouping (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8d76ead7-6c75-5d25-84d4-ca76d928f8a6
     * </ul>
     */
    public static final Concept GROUPING = Concept.make("Grouping (SOLOR)", UUID.fromString("8d76ead7-6c75-5d25-84d4-ca76d928f8a6"));

    /**
     * Java binding for the concept described as Axiom Syntax (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8da1c508-c2a2-4899-b26d-87f8b98a7558
     * </ul>
     */
    public static final Concept AXIOM_SYNTAX = Concept.make("Axiom Syntax (SOLOR)", UUID.fromString("8da1c508-c2a2-4899-b26d-87f8b98a7558"));

    /**
     * Java binding for the concept described as Semantic field fields set and identified by the following UUID(s):
     * <ul>
     * <li>8dcfc1a1-31f2-46f7-8247-0a17a6d7c6c0
     * </ul>
     */
    public static final Concept SEMANTIC_FIELD_FIELDS_SET = Concept.make("Semantic field fields set", UUID.fromString("8dcfc1a1-31f2-46f7-8247-0a17a6d7c6c0"));

    /**
     * Java binding for the concept described as Correlation properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8f682e00-3d9e-5506-bd19-b2169d6c8752
     * </ul>
     */
    public static final Concept CORRELATION_PROPERTIES = Concept.make("Correlation properties (SOLOR)", UUID.fromString("8f682e00-3d9e-5506-bd19-b2169d6c8752"));

    /**
     * Java binding for the concept described as Lithuanian language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8fa63274-70e3-5b11-9669-1b7bdb372b1a
     * <li>e9645d95-8a1f-3825-8feb-0bc2ee825694
     * <li>45022410-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept LITHUANIAN_LANGUAGE = Concept.make("Lithuanian language (SOLOR)", UUID.fromString("8fa63274-70e3-5b11-9669-1b7bdb372b1a"), UUID.fromString("e9645d95-8a1f-3825-8feb-0bc2ee825694"), UUID.fromString("45022410-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Field substitution (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>8fdce1aa-ca82-5abc-8cfa-230c14688abc
     * </ul>
     */
    public static final Concept FIELD_SUBSTITUTION = Concept.make("Field substitution (SOLOR)", UUID.fromString("8fdce1aa-ca82-5abc-8cfa-230c14688abc"));

    /**
     * Java binding for the concept described as Referenced component type restriction (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>902f97b6-2ef4-59d7-b6f9-01278a00061c
     * </ul>
     */
    public static final Concept REFERENCED_COMPONENT_TYPE_RESTRICTION = Concept.make("Referenced component type restriction (SOLOR)", UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"));

    /**
     * Java binding for the concept described as Concept to find (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>91687b29-3bbb-540b-9de6-91246c75afd0
     * </ul>
     */
    public static final Concept CONCEPT_TO_FIND = Concept.make("Concept to find (SOLOR)", UUID.fromString("91687b29-3bbb-540b-9de6-91246c75afd0"));

    /**
     * Java binding for the concept described as Existential restriction and identified by the following UUID(s):
     * <ul>
     * <li>91e9080f-78f6-5d23-891d-f5b6e77995c8
     * </ul>
     */
    public static final Concept EXISTENTIAL_RESTRICTION = Concept.make("Existential restriction", UUID.fromString("91e9080f-78f6-5d23-891d-f5b6e77995c8"));

    /**
     * Java binding for the concept described as Any component (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>927da7ac-3403-5ccc-b07b-88f60cc3a5f8
     * </ul>
     */
    public static final Concept ANY_COMPONENT = Concept.make("Any component (SOLOR)", UUID.fromString("927da7ac-3403-5ccc-b07b-88f60cc3a5f8"));

    /**
     * Java binding for the concept described as Field definition purpose field and identified by the following UUID(s):
     * <ul>
     * <li>93239959-50e6-4645-b5fc-6d47da92e666
     * </ul>
     */
    public static final Concept FIELD_DEFINITION_PURPOSE_FIELD = Concept.make("Field definition purpose field", UUID.fromString("93239959-50e6-4645-b5fc-6d47da92e666"));

    /**
     * Java binding for the concept described as Version Properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>93f844df-38e5-5167-ba94-2c948b8bd07c
     * </ul>
     */
    public static final Concept VERSION_PROPERTIES = Concept.make("Version Properties (SOLOR)", UUID.fromString("93f844df-38e5-5167-ba94-2c948b8bd07c"));

    /**
     * Java binding for the concept described as Description acceptability and identified by the following UUID(s):
     * <ul>
     * <li>96b61063-0d29-5aea-9652-3f5f328aadc3
     * </ul>
     */
    public static final Concept DESCRIPTION_ACCEPTABILITY = Concept.make("Description acceptability", UUID.fromString("96b61063-0d29-5aea-9652-3f5f328aadc3"));

    /**
     * Java binding for the concept described as Field definitions set and identified by the following UUID(s):
     * <ul>
     * <li>975de83e-ab99-4a9e-9051-4cbf310a2123
     * </ul>
     */
    public static final Concept FIELD_DEFINITIONS_SET = Concept.make("Field definitions set", UUID.fromString("975de83e-ab99-4a9e-9051-4cbf310a2123"));

    /**
     * Java binding for the concept described as Swedish language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9784a791-8fdb-32f7-88da-74ab135fe4e3
     * <li>45022848-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept SWEDISH_LANGUAGE = Concept.make("Swedish language (SOLOR)", UUID.fromString("9784a791-8fdb-32f7-88da-74ab135fe4e3"), UUID.fromString("45022848-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Danish language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>987681fb-f3ef-595d-90e2-067baf2bc71f
     * <li>45021f10-9567-11e5-8994-feff819cdc9f
     * <li>7e462e33-6d94-38ae-a044-492a857a6853
     * </ul>
     */
    public static final Concept DANISH_LANGUAGE = Concept.make("Danish language (SOLOR)", UUID.fromString("987681fb-f3ef-595d-90e2-067baf2bc71f"), UUID.fromString("45021f10-9567-11e5-8994-feff819cdc9f"), UUID.fromString("7e462e33-6d94-38ae-a044-492a857a6853"));

    /**
     * Java binding for the concept described as Role restriction (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>988bb02a-9b4a-4ef9-937e-fa8a6afc6c42
     * </ul>
     */
    public static final Concept ROLE_RESTRICTION = Concept.make("Role restriction (SOLOR)", UUID.fromString("988bb02a-9b4a-4ef9-937e-fa8a6afc6c42"));

    /**
     * Java binding for the concept described as Include Upper Bound and identified by the following UUID(s):
     * <ul>
     * <li>990b7e1d-3dcc-4c6e-a068-e30400607d50
     * </ul>
     */
    public static final Concept INCLUDE_UPPER_BOUND = Concept.make("Include Upper Bound", UUID.fromString("990b7e1d-3dcc-4c6e-a068-e30400607d50"));

    /**
     * Java binding for the concept described as Pattern meaning field and identified by the following UUID(s):
     * <ul>
     * <li>996d0023-a355-422f-a84d-16dda6ece1b0
     * </ul>
     */
    public static final Concept PATTERN_MEANING_FIELD = Concept.make("Pattern meaning field", UUID.fromString("996d0023-a355-422f-a84d-16dda6ece1b0"));

    /**
     * Java binding for the concept described as Property sequence implication and identified by the following UUID(s):
     * <ul>
     * <li>9a47a5db-42a6-49ee-9083-54bc305a9456
     * </ul>
     */
    public static final Concept PROPERTY_SEQUENCE_IMPLICATION = Concept.make("Property sequence implication", UUID.fromString("9a47a5db-42a6-49ee-9083-54bc305a9456"));

    /**
     * Java binding for the concept described as Byte array (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9a84fecf-708d-5de4-9c5f-e17973229e0f
     * </ul>
     */
    public static final Concept BYTE_ARRAY = Concept.make("Byte array (SOLOR)", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f"));

    /**
     * Java binding for the concept described as Interval property set and identified by the following UUID(s):
     * <ul>
     * <li>9afc988a-3724-4754-8b74-651426472b19
     * </ul>
     */
    public static final Concept INTERVAL_PROPERTY_SET = Concept.make("Interval property set", UUID.fromString("9afc988a-3724-4754-8b74-651426472b19"));

    /**
     * Java binding for the concept described as Semantic display field type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9c3dfc88-51e4-5e51-a59a-88dd580162b7
     * </ul>
     */
    public static final Concept SEMANTIC_DISPLAY_FIELD_TYPE = Concept.make("Semantic display field type (SOLOR)", UUID.fromString("9c3dfc88-51e4-5e51-a59a-88dd580162b7"));

    /**
     * Java binding for the concept described as Axiom focus (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9c6fbddd-58bd-5881-b926-c813bbff849b
     * </ul>
     */
    public static final Concept AXIOM_FOCUS = Concept.make("Axiom focus (SOLOR)", UUID.fromString("9c6fbddd-58bd-5881-b926-c813bbff849b"));

    /**
     * Java binding for the concept described as SOLOR overlay module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9ecc154c-e490-5cf8-805d-d2865d62aef3
     * <li>1f2016a6-960e-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept SOLOR_OVERLAY_MODULE = Concept.make("SOLOR overlay module (SOLOR)", UUID.fromString("9ecc154c-e490-5cf8-805d-d2865d62aef3"), UUID.fromString("1f2016a6-960e-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Inferred assemblage for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>9ecf4d76-4346-5e5d-8316-bdff48a5c154
     * </ul>
     */
    public static final Concept INFERRED_ASSEMBLAGE_FOR_LOGIC_COORDINATE = Concept.make("Inferred assemblage for logic coordinate (SOLOR)", UUID.fromString("9ecf4d76-4346-5e5d-8316-bdff48a5c154"));

    /**
     * Java binding for the concept described as Description type for description (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a00c5ad7-5b8a-5592-a28c-64057dd3caab
     * </ul>
     */
    public static final Concept DESCRIPTION_TYPE_FOR_DESCRIPTION = Concept.make("Description type for description (SOLOR)", UUID.fromString("a00c5ad7-5b8a-5592-a28c-64057dd3caab"));

    /**
     * Java binding for the concept described as Meaning and identified by the following UUID(s):
     * <ul>
     * <li>a06158ff-e08a-5d7d-bcfa-6cbfdb138910
     * </ul>
     */
    public static final Concept MEANING = Concept.make("Meaning", UUID.fromString("a06158ff-e08a-5d7d-bcfa-6cbfdb138910"));

    /**
     * Java binding for the concept described as Pattern versions set and identified by the following UUID(s):
     * <ul>
     * <li>a254ccee-ef02-4504-9645-0a2ed7af955d
     * </ul>
     */
    public static final Concept PATTERN_VERSIONS_SET = Concept.make("Pattern versions set", UUID.fromString("a254ccee-ef02-4504-9645-0a2ed7af955d"));

    /**
     * Java binding for the concept described as Description dialect pair (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a27bbbf8-57b5-590c-8650-1247f6f963eb
     * </ul>
     */
    public static final Concept DESCRIPTION_DIALECT_PAIR = Concept.make("Description dialect pair (SOLOR)", UUID.fromString("a27bbbf8-57b5-590c-8650-1247f6f963eb"));

    /**
     * Java binding for the concept described as Path coordinate name (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a293a9a0-eb1e-5418-83c7-bec376b62245
     * </ul>
     */
    public static final Concept PATH_COORDINATE_NAME = Concept.make("Path coordinate name (SOLOR)", UUID.fromString("a293a9a0-eb1e-5418-83c7-bec376b62245"));

    /**
     * Java binding for the concept described as Intrinsic role (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a2d37d2d-ac49-589f-ba36-ac9b8808b00b
     * </ul>
     */
    public static final Concept INTRINSIC_ROLE = Concept.make("Intrinsic role (SOLOR)", UUID.fromString("a2d37d2d-ac49-589f-ba36-ac9b8808b00b"));

    /**
     * Java binding for the concept described as Relationship destination and identified by the following UUID(s):
     * <ul>
     * <li>a3dd69af-355c-54ce-ba13-2902a7ae9551
     * </ul>
     */
    public static final Concept RELATIONSHIP_DESTINATION = Concept.make("Relationship destination", UUID.fromString("a3dd69af-355c-54ce-ba13-2902a7ae9551"));

    /**
     * Java binding for the concept described as Conditional triggers (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a3e4ac54-db82-5345-8713-7e0da98bbb0a
     * </ul>
     */
    public static final Concept CONDITIONAL_TRIGGERS = Concept.make("Conditional triggers (SOLOR)", UUID.fromString("a3e4ac54-db82-5345-8713-7e0da98bbb0a"));

    /**
     * Java binding for the concept described as String display field and identified by the following UUID(s):
     * <ul>
     * <li>a46aaf11-b37a-32d6-abdc-707f084ec8f5
     * </ul>
     */
    public static final Concept STRING_DISPLAY_FIELD = Concept.make("String display field", UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5"));

    /**
     * Java binding for the concept described as Inferred premise type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a4c6bf72-8fb6-11db-b606-0800200c9a66
     * <li>1290e6ba-48d0-31d2-8d62-e133373c63f5
     * </ul>
     */
    public static final Concept INFERRED_PREMISE_TYPE = Concept.make("Inferred premise type (SOLOR)", UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"), UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"));

    /**
     * Java binding for the concept described as Role group and identified by the following UUID(s):
     * <ul>
     * <li>a63f4bf2-a040-11e5-8994-feff819cdc9f
     * <li>f97abff6-a221-57a1-9cd6-e79e723bfe2a
     * <li>051fbfed-3c40-3130-8c09-889cb7b7b5b6
     * </ul>
     */
    public static final Concept ROLE_GROUP = Concept.make("Role group", UUID.fromString("a63f4bf2-a040-11e5-8994-feff819cdc9f"), UUID.fromString("f97abff6-a221-57a1-9cd6-e79e723bfe2a"), UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"));

    /**
     * Java binding for the concept described as Partial (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a7f9574c-8e8b-515d-9c21-9896063cc3b8
     * </ul>
     */
    public static final Concept PARTIAL = Concept.make("Partial (SOLOR)", UUID.fromString("a7f9574c-8e8b-515d-9c21-9896063cc3b8"));

    /**
     * Java binding for the concept described as Health concept (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a892950a-0847-300c-b477-4e3cbb945225
     * <li>ee9ac5d2-a07c-3981-a57a-f7f26baf38d8
     * <li>f6daf03a-93d6-5bab-8dc9-f60c327cf012
     * </ul>
     */
    public static final Concept HEALTH_CONCEPT = Concept.make("Health concept (SOLOR)", UUID.fromString("a892950a-0847-300c-b477-4e3cbb945225"), UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"), UUID.fromString("f6daf03a-93d6-5bab-8dc9-f60c327cf012"));

    /**
     * Java binding for the concept described as Author field and identified by the following UUID(s):
     * <ul>
     * <li>a9210ad6-cc48-47df-86e5-2192d56704a6
     * </ul>
     */
    public static final Concept AUTHOR_FIELD = Concept.make("Author field", UUID.fromString("a9210ad6-cc48-47df-86e5-2192d56704a6"));

    /**
     * Java binding for the concept described as Case sensitive evaluation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a95e5dbc-a179-57f9-9cdd-6de8c026396d
     * </ul>
     */
    public static final Concept CASE_SENSITIVE_EVALUATION = Concept.make("Case sensitive evaluation (SOLOR)", UUID.fromString("a95e5dbc-a179-57f9-9cdd-6de8c026396d"));

    /**
     * Java binding for the concept described as Time for version (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a9b0dfb2-f463-5dae-8ba8-7f2e8385571b
     * </ul>
     */
    public static final Concept TIME_FOR_VERSION = Concept.make("Time for version (SOLOR)", UUID.fromString("a9b0dfb2-f463-5dae-8ba8-7f2e8385571b"));

    /**
     * Java binding for the concept described as Referenced component nid for semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>a9ba4749-c11f-5f35-a991-21796fb89ddc
     * </ul>
     */
    public static final Concept REFERENCED_COMPONENT_NID_FOR_SEMANTIC = Concept.make("Referenced component nid for semantic (SOLOR)", UUID.fromString("a9ba4749-c11f-5f35-a991-21796fb89ddc"));

    /**
     * Java binding for the concept described as Chinese language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>aacbc859-e9a0-5e01-b6a9-9a255a47b0c9
     * <li>ba2efe6b-fe56-3d91-ae0f-3b389628f74c
     * <li>45022532-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept CHINESE_LANGUAGE = Concept.make("Chinese language (SOLOR)", UUID.fromString("aacbc859-e9a0-5e01-b6a9-9a255a47b0c9"), UUID.fromString("ba2efe6b-fe56-3d91-ae0f-3b389628f74c"), UUID.fromString("45022532-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Description list for concept (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ab3e8771-7c7c-5e57-8acf-147b16da36e2
     * </ul>
     */
    public static final Concept DESCRIPTION_LIST_FOR_CONCEPT = Concept.make("Description list for concept (SOLOR)", UUID.fromString("ab3e8771-7c7c-5e57-8acf-147b16da36e2"));

    /**
     * Java binding for the concept described as ImmutableCoordinate Properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ab41a788-8a83-5452-8dc0-2d8375e0bfe6
     * </ul>
     */
    public static final Concept IMMUTABLECOORDINATE_PROPERTIES = Concept.make("ImmutableCoordinate Properties (SOLOR)", UUID.fromString("ab41a788-8a83-5452-8dc0-2d8375e0bfe6"));

    /**
     * Java binding for the concept described as Order for axiom attachments (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>abcb0946-20e1-5483-8469-3e8fa0ce20c4
     * </ul>
     */
    public static final Concept ORDER_FOR_AXIOM_ATTACHMENTS = Concept.make("Order for axiom attachments (SOLOR)", UUID.fromString("abcb0946-20e1-5483-8469-3e8fa0ce20c4"));

    /**
     * Java binding for the concept described as Concept display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557
     * </ul>
     */
    public static final Concept CONCEPT_DISPLAY_FIELD = Concept.make("Concept display field (SOLOR)", UUID.fromString("ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557"));

    /**
     * Java binding for the concept described as Necessary set and identified by the following UUID(s):
     * <ul>
     * <li>acaa2eba-8364-5493-b24c-b3884d34bb60
     * </ul>
     */
    public static final Concept NECESSARY_SET = Concept.make("Necessary set", UUID.fromString("acaa2eba-8364-5493-b24c-b3884d34bb60"));

    /**
     * Java binding for the concept described as Correlation reference expression (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>acb73d95-7c96-590c-9f24-23da54f95ff2
     * </ul>
     */
    public static final Concept CORRELATION_REFERENCE_EXPRESSION = Concept.make("Correlation reference expression (SOLOR)", UUID.fromString("acb73d95-7c96-590c-9f24-23da54f95ff2"));

    /**
     * Java binding for the concept described as Feature Role Type and identified by the following UUID(s):
     * <ul>
     * <li>acb8d47e-adac-491d-bc60-78e94cacd312
     * </ul>
     */
    public static final Concept FEATURE_ROLE_TYPE = Concept.make("Feature Role Type", UUID.fromString("acb8d47e-adac-491d-bc60-78e94cacd312"));

    /**
     * Java binding for the concept described as Description type and identified by the following UUID(s):
     * <ul>
     * <li>ad0c19e8-2ccc-59c1-8b7e-c56c03aca8eb
     * </ul>
     */
    public static final Concept DESCRIPTION_TYPE = Concept.make("Description type", UUID.fromString("ad0c19e8-2ccc-59c1-8b7e-c56c03aca8eb"));

    /**
     * Java binding for the concept described as Relationship origin and identified by the following UUID(s):
     * <ul>
     * <li>ad22d43b-3ee7-550b-9660-a6e68af347c2
     * </ul>
     */
    public static final Concept RELATIONSHIP_ORIGIN = Concept.make("Relationship origin", UUID.fromString("ad22d43b-3ee7-550b-9660-a6e68af347c2"));

    /**
     * Java binding for the concept described as Path for version and identified by the following UUID(s):
     * <ul>
     * <li>ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f
     * </ul>
     */
    public static final Concept PATH_FOR_VERSION = Concept.make("Path for version", UUID.fromString("ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f"));

    /**
     * Java binding for the concept described as Data Concept and identified by the following UUID(s):
     * <ul>
     * <li>ae7069d1-67fa-4470-a56f-0d24a8fcea83
     * </ul>
     */
    public static final Concept DATA_CONCEPT = Concept.make("Data Concept", UUID.fromString("ae7069d1-67fa-4470-a56f-0d24a8fcea83"));

    /**
     * Java binding for the concept described as Semantic versions field and identified by the following UUID(s):
     * <ul>
     * <li>aeb73410-a679-4ea8-93fe-7c4785599778
     * </ul>
     */
    public static final Concept SEMANTIC_VERSIONS_FIELD = Concept.make("Semantic versions field", UUID.fromString("aeb73410-a679-4ea8-93fe-7c4785599778"));

    /**
     * Java binding for the concept described as Description logic profile for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>aef80e34-b2dd-5dca-a989-3e0ee2699be3
     * </ul>
     */
    public static final Concept DESCRIPTION_LOGIC_PROFILE_FOR_LOGIC_COORDINATE = Concept.make("Description logic profile for logic coordinate (SOLOR)", UUID.fromString("aef80e34-b2dd-5dca-a989-3e0ee2699be3"));

    /**
     * Java binding for the concept described as Language specification for language coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b0ad4d77-e1bc-5fd1-922e-5fad675e9bfd
     * </ul>
     */
    public static final Concept LANGUAGE_SPECIFICATION_FOR_LANGUAGE_COORDINATE = Concept.make("Language specification for language coordinate (SOLOR)", UUID.fromString("b0ad4d77-e1bc-5fd1-922e-5fad675e9bfd"));

    /**
     * Java binding for the concept described as Text comparison measure semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b1531e68-4e7a-5194-b1f9-9aaace269372
     * </ul>
     */
    public static final Concept TEXT_COMPARISON_MEASURE_SEMANTIC = Concept.make("Text comparison measure semantic (SOLOR)", UUID.fromString("b1531e68-4e7a-5194-b1f9-9aaace269372"));

    /**
     * Java binding for the concept described as Array display field (Solor) and identified by the following UUID(s):
     * <ul>
     * <li>b168ad04-f814-5036-b886-fd4913de88c8
     * </ul>
     */
    public static final Concept ARRAY_DISPLAY_FIELD_OPENPARENTHESIS_SOLOR_CLOSEPARENTHESIS_ = Concept.make("Array display field (Solor)", UUID.fromString("b168ad04-f814-5036-b886-fd4913de88c8"));

    /**
     * Java binding for the concept described as Primordial state and identified by the following UUID(s):
     * <ul>
     * <li>b17bde5d-98ed-5416-97cf-2d837d75159d
     * </ul>
     */
    public static final Concept PRIMORDIAL_STATE = Concept.make("Primordial state", UUID.fromString("b17bde5d-98ed-5416-97cf-2d837d75159d"));

    /**
     * Java binding for the concept described as Inferred Definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b1abf4dc-9838-4b46-ac55-10c4f92ba10b
     * </ul>
     */
    public static final Concept INFERRED_DEFINITION = Concept.make("Inferred Definition (SOLOR)", UUID.fromString("b1abf4dc-9838-4b46-ac55-10c4f92ba10b"));

    /**
     * Java binding for the concept described as Temporal Set Axioms and identified by the following UUID(s):
     * <ul>
     * <li>b253e725-d7cd-46e3-bc3a-5db8b3ffbd52
     * </ul>
     */
    public static final Concept TEMPORAL_SET_AXIOMS = Concept.make("Temporal Set Axioms", UUID.fromString("b253e725-d7cd-46e3-bc3a-5db8b3ffbd52"));

    /**
     * Java binding for the concept described as El++ Inferred Concept Definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b2897aa0-a697-5bf2-9156-7a437c6a5057
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_INFERRED_CONCEPT_DEFINITION = Concept.make("El++ Inferred Concept Definition (SOLOR)", UUID.fromString("b2897aa0-a697-5bf2-9156-7a437c6a5057"));

    /**
     * Java binding for the concept described as Identifier Value (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b32dd26b-c3fc-487e-987e-16ace71a0d0f
     * </ul>
     */
    public static final Concept IDENTIFIER_VALUE = Concept.make("Identifier Value (SOLOR)", UUID.fromString("b32dd26b-c3fc-487e-987e-16ace71a0d0f"));

    /**
     * Java binding for the concept described as EL++ terminological axioms and identified by the following UUID(s):
     * <ul>
     * <li>b3ec50c4-e8cf-4720-b192-31374705f3b7
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_TERMINOLOGICAL_AXIOMS = Concept.make("EL++ terminological axioms", UUID.fromString("b3ec50c4-e8cf-4720-b192-31374705f3b7"));

    /**
     * Java binding for the concept described as Decimal display field and identified by the following UUID(s):
     * <ul>
     * <li>b413fe94-4ada-4aee-96f9-22be19699d40
     * </ul>
     */
    public static final Concept DECIMAL_DISPLAY_FIELD = Concept.make("Decimal display field", UUID.fromString("b413fe94-4ada-4aee-96f9-22be19699d40"));

    /**
     * Java binding for the concept described as Canceled state and identified by the following UUID(s):
     * <ul>
     * <li>b42c1948-7645-5da8-a888-de6ec020ab98
     * </ul>
     */
    public static final Concept CANCELED_STATE = Concept.make("Canceled state", UUID.fromString("b42c1948-7645-5da8-a888-de6ec020ab98"));

    /**
     * Java binding for the concept described as Semantic field concepts (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b4316cb8-14fe-5b32-b03b-f5f966c87819
     * </ul>
     */
    public static final Concept SEMANTIC_FIELD_CONCEPTS = Concept.make("Semantic field concepts (SOLOR)", UUID.fromString("b4316cb8-14fe-5b32-b03b-f5f966c87819"));

    /**
     * Java binding for the concept described as Part of (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b4c3f6f9-6937-30fd-8412-d0c77f8a7f73
     * </ul>
     */
    public static final Concept PART_OF = Concept.make("Part of (SOLOR)", UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));

    /**
     * Java binding for the concept described as Is-a inferred navigation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b620768f-1479-5afa-a027-5a9ae6caf0d5
     * </ul>
     */
    public static final Concept IS_DASH_A_INFERRED_NAVIGATION = Concept.make("Is-a inferred navigation (SOLOR)", UUID.fromString("b620768f-1479-5afa-a027-5a9ae6caf0d5"));

    /**
     * Java binding for the concept described as EL++ Inferred terminological axioms and identified by the following UUID(s):
     * <ul>
     * <li>b6d3be7d-1d7f-5c44-a425-5357f878c212
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS = Concept.make("EL++ Inferred terminological axioms", UUID.fromString("b6d3be7d-1d7f-5c44-a425-5357f878c212"));

    /**
     * Java binding for the concept described as Semantic properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b717ae48-5488-5dda-a980-97855001cc99
     * </ul>
     */
    public static final Concept SEMANTIC_PROPERTIES = Concept.make("Semantic properties (SOLOR)", UUID.fromString("b717ae48-5488-5dda-a980-97855001cc99"));

    /**
     * Java binding for the concept described as STAMP versions field and identified by the following UUID(s):
     * <ul>
     * <li>b8251bea-4248-4a46-8b4a-349500693a9f
     * </ul>
     */
    public static final Concept STAMP_VERSIONS_FIELD = Concept.make("STAMP versions field", UUID.fromString("b8251bea-4248-4a46-8b4a-349500693a9f"));

    /**
     * Java binding for the concept described as Axiom origin (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>b868bd83-5cd4-5d84-9cf7-b08674fbc79b
     * </ul>
     */
    public static final Concept AXIOM_ORIGIN = Concept.make("Axiom origin (SOLOR)", UUID.fromString("b868bd83-5cd4-5d84-9cf7-b08674fbc79b"));

    /**
     * Java binding for the concept described as Dialect and identified by the following UUID(s):
     * <ul>
     * <li>b9c34574-c9ac-503b-aa24-456a0ec949a2
     * </ul>
     */
    public static final Concept DIALECT = Concept.make("Dialect", UUID.fromString("b9c34574-c9ac-503b-aa24-456a0ec949a2"));

    /**
     * Java binding for the concept described as Temporal Type and identified by the following UUID(s):
     * <ul>
     * <li>ba3191ee-a260-41a6-99fd-74a22fdc937e
     * </ul>
     */
    public static final Concept TEMPORAL_TYPE = Concept.make("Temporal Type", UUID.fromString("ba3191ee-a260-41a6-99fd-74a22fdc937e"));

    /**
     * Java binding for the concept described as Tinkar Model concept and identified by the following UUID(s):
     * <ul>
     * <li>bc59d656-83d3-47d8-9507-0e656ea95463
     * </ul>
     */
    public static final Concept TINKAR_MODEL_CONCEPT = Concept.make("Tinkar Model concept", UUID.fromString("bc59d656-83d3-47d8-9507-0e656ea95463"));

    /**
     * Java binding for the concept described as United States of America English dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>bca0a686-3516-3daf-8fcf-fe396d13cfad
     * </ul>
     */
    public static final Concept UNITED_STATES_OF_AMERICA_ENGLISH_DIALECT = Concept.make("United States of America English dialect (SOLOR)", UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));

    /**
     * Java binding for the concept described as Italian Language (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>bdd59458-381a-5818-8577-60525f11ac6c
     * </ul>
     */
    public static final Concept ITALIAN_LANGUAGE = Concept.make("Italian Language (SOLOR)", UUID.fromString("bdd59458-381a-5818-8577-60525f11ac6c"));

    /**
     * Java binding for the concept described as Modules for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>bf69c4f1-95c9-5956-a10a-d3ba9276c019
     * </ul>
     */
    public static final Concept MODULES_FOR_STAMP_COORDINATE = Concept.make("Modules for stamp coordinate (SOLOR)", UUID.fromString("bf69c4f1-95c9-5956-a10a-d3ba9276c019"));

    /**
     * Java binding for the concept described as Dialect assemblage preference list for language coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c060ffbf-e95f-5960-b296-8a3255c820ac
     * </ul>
     */
    public static final Concept DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE = Concept.make("Dialect assemblage preference list for language coordinate (SOLOR)", UUID.fromString("c060ffbf-e95f-5960-b296-8a3255c820ac"));

    /**
     * Java binding for the concept described as English Dialect and identified by the following UUID(s):
     * <ul>
     * <li>c0836284-f631-3c86-8cfc-56a67814efab
     * </ul>
     */
    public static final Concept ENGLISH_DIALECT = Concept.make("English Dialect", UUID.fromString("c0836284-f631-3c86-8cfc-56a67814efab"));

    /**
     * Java binding for the concept described as Irish dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c0f77638-6274-5b40-b832-ac1cba7ec515
     * </ul>
     */
    public static final Concept IRISH_DIALECT = Concept.make("Irish dialect (SOLOR)", UUID.fromString("c0f77638-6274-5b40-b832-ac1cba7ec515"));

    /**
     * Java binding for the concept described as Tree list (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c11dd7a1-0ba1-5378-81d6-3efdba1e074b
     * </ul>
     */
    public static final Concept TREE_LIST = Concept.make("Tree list (SOLOR)", UUID.fromString("c11dd7a1-0ba1-5378-81d6-3efdba1e074b"));

    /**
     * Java binding for the concept described as Logical expression display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c16eb414-8840-54f8-9bd2-e2f1ab37e19d
     * </ul>
     */
    public static final Concept LOGICAL_EXPRESSION_DISPLAY_FIELD = Concept.make("Logical expression display field (SOLOR)", UUID.fromString("c16eb414-8840-54f8-9bd2-e2f1ab37e19d"));

    /**
     * Java binding for the concept described as Greater than or equal to (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c1baba19-e918-5d2c-8fa4-b0ad93e03186
     * </ul>
     */
    public static final Concept GREATER_THAN_OR_EQUAL_TO = Concept.make("Greater than or equal to (SOLOR)", UUID.fromString("c1baba19-e918-5d2c-8fa4-b0ad93e03186"));

    /**
     * Java binding for the concept described as Primordial module and identified by the following UUID(s):
     * <ul>
     * <li>c2012321-3903-532e-8a5f-b13e4ca46e86
     * </ul>
     */
    public static final Concept PRIMORDIAL_MODULE = Concept.make("Primordial module", UUID.fromString("c2012321-3903-532e-8a5f-b13e4ca46e86"));

    /**
     * Java binding for the concept described as Concept version (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c202f992-3f4b-5f30-9b32-e376f68367d1
     * </ul>
     */
    public static final Concept CONCEPT_VERSION = Concept.make("Concept version (SOLOR)", UUID.fromString("c202f992-3f4b-5f30-9b32-e376f68367d1"));

    /**
     * Java binding for the concept described as Phenomenon and identified by the following UUID(s):
     * <ul>
     * <li>c2e8bc47-3353-5e02-b0d1-2a5916efed4d
     * </ul>
     */
    public static final Concept PHENOMENON = Concept.make("Phenomenon", UUID.fromString("c2e8bc47-3353-5e02-b0d1-2a5916efed4d"));

    /**
     * Java binding for the concept described as Inverse name (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c342d18a-ec1c-5583-bfe3-59e6324ae189
     * </ul>
     */
    public static final Concept INVERSE_NAME = Concept.make("Inverse name (SOLOR)", UUID.fromString("c342d18a-ec1c-5583-bfe3-59e6324ae189"));

    /**
     * Java binding for the concept described as Description case significance and identified by the following UUID(s):
     * <ul>
     * <li>c3dde9ea-b144-5f49-845a-20cc7d305250
     * <li>f30b0312-2c85-3e65-8609-2d89f8437d34
     * </ul>
     */
    public static final Concept DESCRIPTION_CASE_SIGNIFICANCE = Concept.make("Description case significance", UUID.fromString("c3dde9ea-b144-5f49-845a-20cc7d305250"), UUID.fromString("f30b0312-2c85-3e65-8609-2d89f8437d34"));

    /**
     * Java binding for the concept described as Purpose and identified by the following UUID(s):
     * <ul>
     * <li>c3dffc48-6493-54df-a2f0-14be8ba03091
     * </ul>
     */
    public static final Concept PURPOSE = Concept.make("Purpose", UUID.fromString("c3dffc48-6493-54df-a2f0-14be8ba03091"));

    /**
     * Java binding for the concept described as Sandbox module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95
     * </ul>
     */
    public static final Concept SANDBOX_MODULE = Concept.make("Sandbox module (SOLOR)", UUID.fromString("c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95"));

    /**
     * Java binding for the concept described as Navigation vertex (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c7f01834-34ca-5f8b-8f80-193fbeb12eae
     * </ul>
     */
    public static final Concept NAVIGATION_VERTEX = Concept.make("Navigation vertex (SOLOR)", UUID.fromString("c7f01834-34ca-5f8b-8f80-193fbeb12eae"));

    /**
     * Java binding for the concept described as Semantic list for chronicle (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c809b2c0-9235-5f64-bbda-34210d91bdf8
     * </ul>
     */
    public static final Concept SEMANTIC_LIST_FOR_CHRONICLE = Concept.make("Semantic list for chronicle (SOLOR)", UUID.fromString("c809b2c0-9235-5f64-bbda-34210d91bdf8"));

    /**
     * Java binding for the concept described as Module for user (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11
     * </ul>
     */
    public static final Concept MODULE_FOR_USER = Concept.make("Module for user (SOLOR)", UUID.fromString("c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11"));

    /**
     * Java binding for the concept described as Feature Type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c9120d8b-1acc-5267-9f33-fa716abdb69d
     * </ul>
     */
    public static final Concept FEATURE_TYPE = Concept.make("Feature Type (SOLOR)", UUID.fromString("c9120d8b-1acc-5267-9f33-fa716abdb69d"));

    /**
     * Java binding for the concept described as Polish Language (Language) and identified by the following UUID(s):
     * <ul>
     * <li>c924b887-da88-3a72-b8ea-fa86990467c9
     * <li>45022140-9567-11e5-8994-feff819cdc9f
     * </ul>
     */
    public static final Concept POLISH_LANGUAGE_OPENPARENTHESIS_LANGUAGE_CLOSEPARENTHESIS_ = Concept.make("Polish Language (Language)", UUID.fromString("c924b887-da88-3a72-b8ea-fa86990467c9"), UUID.fromString("45022140-9567-11e5-8994-feff819cdc9f"));

    /**
     * Java binding for the concept described as Sandbox component (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>c93829b2-aa78-5a84-ac9a-c34307844166
     * </ul>
     */
    public static final Concept SANDBOX_COMPONENT = Concept.make("Sandbox component (SOLOR)", UUID.fromString("c93829b2-aa78-5a84-ac9a-c34307844166"));

    /**
     * Java binding for the concept described as Is-a and identified by the following UUID(s):
     * <ul>
     * <li>c93a30b9-ba77-3adb-a9b8-4589c9f8fb25
     * <li>46bccdc4-8fb6-11db-b606-0800200c9a66
     * </ul>
     */
    public static final Concept IS_DASH_A = Concept.make("Is-a", UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"), UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));

    /**
     * Java binding for the concept described as Property set Axioms and identified by the following UUID(s):
     * <ul>
     * <li>ca2fdefd-0481-41cb-8074-41a78f94034d
     * </ul>
     */
    public static final Concept PROPERTY_SET_AXIOMS = Concept.make("Property set Axioms", UUID.fromString("ca2fdefd-0481-41cb-8074-41a78f94034d"));

    /**
     * Java binding for the concept described as Annotation property set and identified by the following UUID(s):
     * <ul>
     * <li>cb9e33de-f82c-495d-89fa-69afecbcd47d
     * </ul>
     */
    public static final Concept ANNOTATION_PROPERTY_SET = Concept.make("Annotation property set", UUID.fromString("cb9e33de-f82c-495d-89fa-69afecbcd47d"));

    /**
     * Java binding for the concept described as Blank Concept and identified by the following UUID(s):
     * <ul>
     * <li>cd23d88d-2fcd-4007-8829-97e37bf336aa
     * </ul>
     */
    public static final Concept BLANK_CONCEPT = Concept.make("Blank Concept", UUID.fromString("cd23d88d-2fcd-4007-8829-97e37bf336aa"));

    /**
     * Java binding for the concept described as Language concept nid for description (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>cd56cceb-8507-5ae5-a928-16079fe6f832
     * </ul>
     */
    public static final Concept LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION = Concept.make("Language concept nid for description (SOLOR)", UUID.fromString("cd56cceb-8507-5ae5-a928-16079fe6f832"));

    /**
     * Java binding for the concept described as Image display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>cd9ea037-0af9-586b-9369-7bc044cdb8f7
     * </ul>
     */
    public static final Concept IMAGE_DISPLAY_FIELD = Concept.make("Image display field (SOLOR)", UUID.fromString("cd9ea037-0af9-586b-9369-7bc044cdb8f7"));

    /**
     * Java binding for the concept described as Float substitution (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>cf18fe25-bd21-586e-9da4-da6cb335fd12
     * </ul>
     */
    public static final Concept FLOAT_SUBSTITUTION = Concept.make("Float substitution (SOLOR)", UUID.fromString("cf18fe25-bd21-586e-9da4-da6cb335fd12"));

    /**
     * Java binding for the concept described as Stated assemblage for logic coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>cfd2a47e-8169-5e71-9122-d5b73efd990a
     * </ul>
     */
    public static final Concept STATED_ASSEMBLAGE_FOR_LOGIC_COORDINATE = Concept.make("Stated assemblage for logic coordinate (SOLOR)", UUID.fromString("cfd2a47e-8169-5e71-9122-d5b73efd990a"));

    /**
     * Java binding for the concept described as Property Sequence (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d0d759fd-510f-475a-900e-b1439b4536e1
     * </ul>
     */
    public static final Concept PROPERTY_SEQUENCE = Concept.make("Property Sequence (SOLOR)", UUID.fromString("d0d759fd-510f-475a-900e-b1439b4536e1"));

    /**
     * Java binding for the concept described as Logical expression semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d19306b1-4744-5028-a715-17ca4a4d657f
     * </ul>
     */
    public static final Concept LOGICAL_EXPRESSION_SEMANTIC = Concept.make("Logical expression semantic (SOLOR)", UUID.fromString("d19306b1-4744-5028-a715-17ca4a4d657f"));

    /**
     * Java binding for the concept described as NID (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d1a17272-9785-51aa-8bde-cc556ab32ebb
     * </ul>
     */
    public static final Concept NID = Concept.make("NID (SOLOR)", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb"));

    /**
     * Java binding for the concept described as Extended relationship type (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d41d928f-8a97-55c1-aa6c-a289b413fbfd
     * </ul>
     */
    public static final Concept EXTENDED_RELATIONSHIP_TYPE = Concept.make("Extended relationship type (SOLOR)", UUID.fromString("d41d928f-8a97-55c1-aa6c-a289b413fbfd"));

    /**
     * Java binding for the concept described as Not Applicable (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d4cc29ae-c0c1-563a-985d-5165a768dd44
     * </ul>
     */
    public static final Concept NOT_APPLICABLE = Concept.make("Not Applicable (SOLOR)", UUID.fromString("d4cc29ae-c0c1-563a-985d-5165a768dd44"));

    /**
     * Java binding for the concept described as Is-a stated navigation (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d555dde9-c97e-5480-819a-7472eda3dbfa
     * </ul>
     */
    public static final Concept IS_DASH_A_STATED_NAVIGATION = Concept.make("Is-a stated navigation (SOLOR)", UUID.fromString("d555dde9-c97e-5480-819a-7472eda3dbfa"));

    /**
     * Java binding for the concept described as Tree amalgam properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d6151a47-4610-5a5c-abd0-42c82be9b633
     * </ul>
     */
    public static final Concept TREE_AMALGAM_PROPERTIES = Concept.make("Tree amalgam properties (SOLOR)", UUID.fromString("d6151a47-4610-5a5c-abd0-42c82be9b633"));

    /**
     * Java binding for the concept described as Boolean display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d6b9e2cc-31c6-5e80-91b7-7537690aae32
     * </ul>
     */
    public static final Concept BOOLEAN_DISPLAY_FIELD = Concept.make("Boolean display field (SOLOR)", UUID.fromString("d6b9e2cc-31c6-5e80-91b7-7537690aae32"));

    /**
     * Java binding for the concept described as Version list for chronicle (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>d6f27f80-8e20-58fe-8d69-66ad4644f969
     * </ul>
     */
    public static final Concept VERSION_LIST_FOR_CHRONICLE = Concept.make("Version list for chronicle (SOLOR)", UUID.fromString("d6f27f80-8e20-58fe-8d69-66ad4644f969"));

    /**
     * Java binding for the concept described as Float literal (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>da754dd9-9961-5819-91f5-8245d49850b4
     * </ul>
     */
    public static final Concept FLOAT_LITERAL = Concept.make("Float literal (SOLOR)", UUID.fromString("da754dd9-9961-5819-91f5-8245d49850b4"));

    /**
     * Java binding for the concept described as Promotion Path for Edit Coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>db124d3b-c1bb-530e-8fd4-577f570355be
     * </ul>
     */
    public static final Concept PROMOTION_PATH_FOR_EDIT_COORDINATE = Concept.make("Promotion Path for Edit Coordinate (SOLOR)", UUID.fromString("db124d3b-c1bb-530e-8fd4-577f570355be"));

    /**
     * Java binding for the concept described as Integer field and identified by the following UUID(s):
     * <ul>
     * <li>db249d1f-ea2e-4608-ae13-166ed20ca825
     * </ul>
     */
    public static final Concept INTEGER_FIELD = Concept.make("Integer field", UUID.fromString("db249d1f-ea2e-4608-ae13-166ed20ca825"));

    /**
     * Java binding for the concept described as Express axiom syntax (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>db55557c-e9ee-4504-aae3-df695b6d6c57
     * </ul>
     */
    public static final Concept EXPRESS_AXIOM_SYNTAX = Concept.make("Express axiom syntax (SOLOR)", UUID.fromString("db55557c-e9ee-4504-aae3-df695b6d6c57"));

    /**
     * Java binding for the concept described as Byte array display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dbdd8df2-aec3-596b-88fc-7b83b5594a45
     * </ul>
     */
    public static final Concept BYTE_ARRAY_DISPLAY_FIELD = Concept.make("Byte array display field (SOLOR)", UUID.fromString("dbdd8df2-aec3-596b-88fc-7b83b5594a45"));

    /**
     * Java binding for the concept described as Concept focus (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dca9854d-9e4c-5e8a-8b30-6c1af6901bb8
     * </ul>
     */
    public static final Concept CONCEPT_FOCUS = Concept.make("Concept focus (SOLOR)", UUID.fromString("dca9854d-9e4c-5e8a-8b30-6c1af6901bb8"));

    /**
     * Java binding for the concept described as Decimal (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dccb0476-3b63-3d48-b5a2-85bd0ad2fa61
     * </ul>
     */
    public static final Concept DECIMAL = Concept.make("Decimal (SOLOR)", UUID.fromString("dccb0476-3b63-3d48-b5a2-85bd0ad2fa61"));

    /**
     * Java binding for the concept described as Tinkar Starter Data Author (User) and identified by the following UUID(s):
     * <ul>
     * <li>dd96b2ea-6d7b-3791-ad74-bbdc67c493c1
     * </ul>
     */
    public static final Concept TINKAR_STARTER_DATA_AUTHOR_OPENPARENTHESIS_USER_CLOSEPARENTHESIS_ = Concept.make("Tinkar Starter Data Author (User)", UUID.fromString("dd96b2ea-6d7b-3791-ad74-bbdc67c493c1"));

    /**
     * Java binding for the concept described as Uniquely identify knowledge graph components and identified by the following UUID(s):
     * <ul>
     * <li>dde9a93d-250c-449b-bea0-ba1133d1387b
     * </ul>
     */
    public static final Concept UNIQUELY_IDENTIFY_KNOWLEDGE_GRAPH_COMPONENTS = Concept.make("Uniquely identify knowledge graph components", UUID.fromString("dde9a93d-250c-449b-bea0-ba1133d1387b"));

    /**
     * Java binding for the concept described as Module preference order for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ddeda759-e89c-5186-aa40-d63070756ab4
     * </ul>
     */
    public static final Concept MODULE_PREFERENCE_ORDER_FOR_STAMP_COORDINATE = Concept.make("Module preference order for stamp coordinate (SOLOR)", UUID.fromString("ddeda759-e89c-5186-aa40-d63070756ab4"));

    /**
     * Java binding for the concept described as Boolean reference (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>de49d207-a26e-5f8a-b905-953a4dd13c21
     * </ul>
     */
    public static final Concept BOOLEAN_REFERENCE = Concept.make("Boolean reference (SOLOR)", UUID.fromString("de49d207-a26e-5f8a-b905-953a4dd13c21"));

    /**
     * Java binding for the concept described as UUID display field (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dea8cb0f-9bb5-56bb-af27-a14943cb24ba
     * </ul>
     */
    public static final Concept UUID_DISPLAY_FIELD = Concept.make("UUID display field (SOLOR)", UUID.fromString("dea8cb0f-9bb5-56bb-af27-a14943cb24ba"));

    /**
     * Java binding for the concept described as Long (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dea8cdf1-de75-5991-9791-79714e4a964d
     * </ul>
     */
    public static final Concept LONG = Concept.make("Long (SOLOR)", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d"));

    /**
     * Java binding for the concept described as Minimum Value Operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ded98e42-f74a-4432-9ae7-01b94dc2fdea
     * </ul>
     */
    public static final Concept MINIMUM_VALUE_OPERATOR = Concept.make("Minimum Value Operator (SOLOR)", UUID.fromString("ded98e42-f74a-4432-9ae7-01b94dc2fdea"));

    /**
     * Java binding for the concept described as Inclusion set and identified by the following UUID(s):
     * <ul>
     * <li>def77c09-e1eb-40f2-931a-e7cf2ce0e597
     * </ul>
     */
    public static final Concept INCLUSION_SET = Concept.make("Inclusion set", UUID.fromString("def77c09-e1eb-40f2-931a-e7cf2ce0e597"));

    /**
     * Java binding for the concept described as Sufficient concept definition operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>dfa80f36-dbe6-5006-8509-c497a26ceab5
     * </ul>
     */
    public static final Concept SUFFICIENT_CONCEPT_DEFINITION_OPERATOR = Concept.make("Sufficient concept definition operator (SOLOR)", UUID.fromString("dfa80f36-dbe6-5006-8509-c497a26ceab5"));

    /**
     * Java binding for the concept described as Property pattern implication (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e0de0d09-6e27-5738-bc8f-0fc94bb115fc
     * </ul>
     */
    public static final Concept PROPERTY_PATTERN_IMPLICATION = Concept.make("Property pattern implication (SOLOR)", UUID.fromString("e0de0d09-6e27-5738-bc8f-0fc94bb115fc"));

    /**
     * Java binding for the concept described as Primordial UUID for chronicle (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e0fcafbc-7191-5cdc-b14a-19d4d97f71bd
     * </ul>
     */
    public static final Concept PRIMORDIAL_UUID_FOR_CHRONICLE = Concept.make("Primordial UUID for chronicle (SOLOR)", UUID.fromString("e0fcafbc-7191-5cdc-b14a-19d4d97f71bd"));

    /**
     * Java binding for the concept described as Necessary but not sufficient concept definition (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e1a12059-3b01-3296-9532-d10e49d0afc3
     * </ul>
     */
    public static final Concept NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION = Concept.make("Necessary but not sufficient concept definition (SOLOR)", UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3"));

    /**
     * Java binding for the concept described as Komet issue (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e1dd7bf2-224d-53a5-a5fb-7b25b05d17a6
     * </ul>
     */
    public static final Concept KOMET_ISSUE = Concept.make("Komet issue (SOLOR)", UUID.fromString("e1dd7bf2-224d-53a5-a5fb-7b25b05d17a6"));

    /**
     * Java binding for the concept described as Property set (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e273b5c0-c012-5e53-990c-aec5c2cb33a7
     * </ul>
     */
    public static final Concept PROPERTY_SET = Concept.make("Property set (SOLOR)", UUID.fromString("e273b5c0-c012-5e53-990c-aec5c2cb33a7"));

    /**
     * Java binding for the concept described as Component Id display set and identified by the following UUID(s):
     * <ul>
     * <li>e283af51-2e8f-44fa-9bf1-89a99a7c7631
     * </ul>
     */
    public static final Concept COMPONENT_ID_DISPLAY_SET = Concept.make("Component Id display set", UUID.fromString("e283af51-2e8f-44fa-9bf1-89a99a7c7631"));

    /**
     * Java binding for the concept described as Component Id display list and identified by the following UUID(s):
     * <ul>
     * <li>e553d3f1-63e1-4292-a3a9-af646fe44292
     * </ul>
     */
    public static final Concept COMPONENT_ID_DISPLAY_LIST = Concept.make("Component Id display list", UUID.fromString("e553d3f1-63e1-4292-a3a9-af646fe44292"));

    /**
     * Java binding for the concept described as Module field and identified by the following UUID(s):
     * <ul>
     * <li>e6359a86-a1df-4721-8a1a-1f1f075ec3d9
     * </ul>
     */
    public static final Concept MODULE_FIELD = Concept.make("Module field", UUID.fromString("e6359a86-a1df-4721-8a1a-1f1f075ec3d9"));

    /**
     * Java binding for the concept described as Definition root (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e7271c01-6ed4-5240-963f-34d1f24153b0
     * </ul>
     */
    public static final Concept DEFINITION_ROOT = Concept.make("Definition root (SOLOR)", UUID.fromString("e7271c01-6ed4-5240-963f-34d1f24153b0"));

    /**
     * Java binding for the concept described as Default module for edit coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e83d322c-e275-5392-a5db-1de5fe98acb5
     * </ul>
     */
    public static final Concept DEFAULT_MODULE_FOR_EDIT_COORDINATE = Concept.make("Default module for edit coordinate (SOLOR)", UUID.fromString("e83d322c-e275-5392-a5db-1de5fe98acb5"));

    /**
     * Java binding for the concept described as Presentation unit different (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e86d3887-717b-545f-b6b5-611210913b23
     * </ul>
     */
    public static final Concept PRESENTATION_UNIT_DIFFERENT = Concept.make("Presentation unit different (SOLOR)", UUID.fromString("e86d3887-717b-545f-b6b5-611210913b23"));

    /**
     * Java binding for the concept described as Concept reference (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e89148c7-4fe2-52f8-abb9-6a53605d20cb
     * </ul>
     */
    public static final Concept CONCEPT_REFERENCE = Concept.make("Concept reference (SOLOR)", UUID.fromString("e89148c7-4fe2-52f8-abb9-6a53605d20cb"));

    /**
     * Java binding for the concept described as Taxonomy operator (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e9252365-7a43-57ea-bf94-3f23bab4ef99
     * </ul>
     */
    public static final Concept TAXONOMY_OPERATOR = Concept.make("Taxonomy operator (SOLOR)", UUID.fromString("e9252365-7a43-57ea-bf94-3f23bab4ef99"));

    /**
     * Java binding for the concept described as Primordial path and identified by the following UUID(s):
     * <ul>
     * <li>e95b6718-f824-5540-817b-8e79544eb97a
     * </ul>
     */
    public static final Concept PRIMORDIAL_PATH = Concept.make("Primordial path", UUID.fromString("e95b6718-f824-5540-817b-8e79544eb97a"));

    /**
     * Java binding for the concept described as Vertex sort (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>e973f077-a99d-59e6-b7bd-804e87e0e639
     * </ul>
     */
    public static final Concept VERTEX_SORT = Concept.make("Vertex sort (SOLOR)", UUID.fromString("e973f077-a99d-59e6-b7bd-804e87e0e639"));

    /**
     * Java binding for the concept described as Instant field and identified by the following UUID(s):
     * <ul>
     * <li>e9bde1bc-aa72-430a-afe1-aa8aec8833b4
     * </ul>
     */
    public static final Concept INSTANT_FIELD = Concept.make("Instant field", UUID.fromString("e9bde1bc-aa72-430a-afe1-aa8aec8833b4"));

    /**
     * Java binding for the concept described as Language coordinate properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ea1a52f7-0305-5487-8766-e846330f167a
     * </ul>
     */
    public static final Concept LANGUAGE_COORDINATE_PROPERTIES = Concept.make("Language coordinate properties (SOLOR)", UUID.fromString("ea1a52f7-0305-5487-8766-e846330f167a"));

    /**
     * Java binding for the concept described as Great Britain English dialect and identified by the following UUID(s):
     * <ul>
     * <li>eb9a5e42-3cba-356d-b623-3ed472e20b30
     * </ul>
     */
    public static final Concept GREAT_BRITAIN_ENGLISH_DIALECT = Concept.make("Great Britain English dialect", UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));

    /**
     * Java binding for the concept described as Concept field and identified by the following UUID(s):
     * <ul>
     * <li>ebe2aa74-f100-41b2-8d75-2d8f06ce5e4e
     * </ul>
     */
    public static final Concept CONCEPT_FIELD = Concept.make("Concept field", UUID.fromString("ebe2aa74-f100-41b2-8d75-2d8f06ce5e4e"));

    /**
     * Java binding for the concept described as Path coordinate properties (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ec41e427-f009-5e45-a643-6dc658d63d83
     * </ul>
     */
    public static final Concept PATH_COORDINATE_PROPERTIES = Concept.make("Path coordinate properties (SOLOR)", UUID.fromString("ec41e427-f009-5e45-a643-6dc658d63d83"));

    /**
     * Java binding for the concept described as Description not case sensitive and identified by the following UUID(s):
     * <ul>
     * <li>ecea41a2-f596-3d98-99d1-771b667e55b8
     * </ul>
     */
    public static final Concept DESCRIPTION_NOT_CASE_SENSITIVE = Concept.make("Description not case sensitive", UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));

    /**
     * Java binding for the concept described as Field categories and identified by the following UUID(s):
     * <ul>
     * <li>ed230c7c-20f9-470d-8566-5057f92748a5
     * </ul>
     */
    public static final Concept FIELD_CATEGORIES = Concept.make("Field categories", UUID.fromString("ed230c7c-20f9-470d-8566-5057f92748a5"));

    /**
     * Java binding for the concept described as Interval role and identified by the following UUID(s):
     * <ul>
     * <li>ed9d3506-65ad-48ea-bd01-95474fecdbc4
     * </ul>
     */
    public static final Concept INTERVAL_ROLE = Concept.make("Interval role", UUID.fromString("ed9d3506-65ad-48ea-bd01-95474fecdbc4"));

    /**
     * Java binding for the concept described as STAMP versions set and identified by the following UUID(s):
     * <ul>
     * <li>edb90567-7822-4129-a406-b359b825f922
     * </ul>
     */
    public static final Concept STAMP_VERSIONS_SET = Concept.make("STAMP versions set", UUID.fromString("edb90567-7822-4129-a406-b359b825f922"));

    /**
     * Java binding for the concept described as EL++ digraph (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>ee04d7db-3407-568f-9b93-7b1f9f5bb0fc
     * </ul>
     */
    public static final Concept EL_PLUS_PLUS_DIGRAPH = Concept.make("EL++ digraph (SOLOR)", UUID.fromString("ee04d7db-3407-568f-9b93-7b1f9f5bb0fc"));

    /**
     * Java binding for the concept described as Implication set and identified by the following UUID(s):
     * <ul>
     * <li>ee467a5b-9292-4e0a-a165-3b1a359a8c98
     * </ul>
     */
    public static final Concept IMPLICATION_SET = Concept.make("Implication set", UUID.fromString("ee467a5b-9292-4e0a-a165-3b1a359a8c98"));

    /**
     * Java binding for the concept described as Component type focus (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f1f179d0-26af-5123-9b29-9fc6cd01dd29
     * </ul>
     */
    public static final Concept COMPONENT_TYPE_FOCUS = Concept.make("Component type focus (SOLOR)", UUID.fromString("f1f179d0-26af-5123-9b29-9fc6cd01dd29"));

    /**
     * Java binding for the concept described as Status field and identified by the following UUID(s):
     * <ul>
     * <li>f2c79ebb-3095-44ea-831f-992aed48801f
     * </ul>
     */
    public static final Concept STATUS_FIELD = Concept.make("Status field", UUID.fromString("f2c79ebb-3095-44ea-831f-992aed48801f"));

    /**
     * Java binding for the concept described as Path origins for stamp path (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f33e1668-34dd-53dd-8728-31b29934b482
     * </ul>
     */
    public static final Concept PATH_ORIGINS_FOR_STAMP_PATH = Concept.make("Path origins for stamp path (SOLOR)", UUID.fromString("f33e1668-34dd-53dd-8728-31b29934b482"));

    /**
     * Java binding for the concept described as Module preference list for language coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f36e7ca6-34a2-58b5-8b25-736457515f9c
     * </ul>
     */
    public static final Concept MODULE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE = Concept.make("Module preference list for language coordinate (SOLOR)", UUID.fromString("f36e7ca6-34a2-58b5-8b25-736457515f9c"));

    /**
     * Java binding for the concept described as Module preference list for stamp coordinate (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f56ef2df-6758-5271-a587-317a4fed6c2e
     * </ul>
     */
    public static final Concept MODULE_PREFERENCE_LIST_FOR_STAMP_COORDINATE = Concept.make("Module preference list for stamp coordinate (SOLOR)", UUID.fromString("f56ef2df-6758-5271-a587-317a4fed6c2e"));

    /**
     * Java binding for the concept described as Language and identified by the following UUID(s):
     * <ul>
     * <li>f56fa231-10f9-5e7f-a86d-a1d61b5b56e3
     * </ul>
     */
    public static final Concept LANGUAGE = Concept.make("Language", UUID.fromString("f56fa231-10f9-5e7f-a86d-a1d61b5b56e3"));

    /**
     * Java binding for the concept described as Semantic field field and identified by the following UUID(s):
     * <ul>
     * <li>f6572c76-b5c0-41da-99c0-4344694e7e3c
     * </ul>
     */
    public static final Concept SEMANTIC_FIELD_FIELD = Concept.make("Semantic field field", UUID.fromString("f6572c76-b5c0-41da-99c0-4344694e7e3c"));

    /**
     * Java binding for the concept described as SOLOR module (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f680c868-f7e5-5d0e-91f2-615eca8f8fd2
     * </ul>
     */
    public static final Concept SOLOR_MODULE = Concept.make("SOLOR module (SOLOR)", UUID.fromString("f680c868-f7e5-5d0e-91f2-615eca8f8fd2"));

    /**
     * Java binding for the concept described as Author and identified by the following UUID(s):
     * <ul>
     * <li>f7495b58-6630-3499-a44e-2052b5fcf06c
     * </ul>
     */
    public static final Concept AUTHOR = Concept.make("Author", UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));

    /**
     * Java binding for the concept described as Disjoint with (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f8433993-9a2d-5377-b564-80a45c7b7824
     * </ul>
     */
    public static final Concept DISJOINT_WITH = Concept.make("Disjoint with (SOLOR)", UUID.fromString("f8433993-9a2d-5377-b564-80a45c7b7824"));

    /**
     * Java binding for the concept described as UUID list for component (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f8e3238e-7424-5a40-8649-a8d164382fec
     * </ul>
     */
    public static final Concept UUID_LIST_FOR_COMPONENT = Concept.make("UUID list for component (SOLOR)", UUID.fromString("f8e3238e-7424-5a40-8649-a8d164382fec"));

    /**
     * Java binding for the concept described as Anonymous concept (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f8f936d4-3ac7-5629-9f65-9452608056a1
     * </ul>
     */
    public static final Concept ANONYMOUS_CONCEPT = Concept.make("Anonymous concept (SOLOR)", UUID.fromString("f8f936d4-3ac7-5629-9f65-9452608056a1"));

    /**
     * Java binding for the concept described as Standard Korean dialect (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>f90722cc-5e40-5b9b-a2a6-f4dfa312a6a9
     * </ul>
     */
    public static final Concept STANDARD_KOREAN_DIALECT = Concept.make("Standard Korean dialect (SOLOR)", UUID.fromString("f90722cc-5e40-5b9b-a2a6-f4dfa312a6a9"));

    /**
     * Java binding for the concept described as Role operator and identified by the following UUID(s):
     * <ul>
     * <li>f9860cb8-a7c7-5743-9d7c-ffc6e8a24a0f
     * </ul>
     */
    public static final Concept ROLE_OPERATOR = Concept.make("Role operator", UUID.fromString("f9860cb8-a7c7-5743-9d7c-ffc6e8a24a0f"));

    /**
     * Java binding for the concept described as And (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>fa113d51-07d2-587c-8930-0bce207d506d
     * </ul>
     */
    public static final Concept AND = Concept.make("And (SOLOR)", UUID.fromString("fa113d51-07d2-587c-8930-0bce207d506d"));

    /**
     * Java binding for the concept described as Component display field and identified by the following UUID(s):
     * <ul>
     * <li>fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3
     * </ul>
     */
    public static final Concept COMPONENT_DISPLAY_FIELD = Concept.make("Component display field", UUID.fromString("fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3"));

    /**
     * Java binding for the concept described as Float (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>fb591801-7b37-525d-980d-98a1c63ceee0
     * </ul>
     */
    public static final Concept FLOAT = Concept.make("Float (SOLOR)", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0"));

    /**
     * Java binding for the concept described as Concept semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>fbf054fb-ceaf-5ab8-b946-bbcc4835ce07
     * </ul>
     */
    public static final Concept CONCEPT_SEMANTIC = Concept.make("Concept semantic (SOLOR)", UUID.fromString("fbf054fb-ceaf-5ab8-b946-bbcc4835ce07"));

    /**
     * Java binding for the concept described as Universal Restriction and identified by the following UUID(s):
     * <ul>
     * <li>fc18c082-c6ad-52d2-b568-cc9568ace6c9
     * </ul>
     */
    public static final Concept UNIVERSAL_RESTRICTION = Concept.make("Universal Restriction", UUID.fromString("fc18c082-c6ad-52d2-b568-cc9568ace6c9"));

    /**
     * Java binding for the concept described as Logic graph for semantic (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>fc2a0662-2396-575b-95f0-e9b38a418620
     * </ul>
     */
    public static final Concept LOGIC_GRAPH_FOR_SEMANTIC = Concept.make("Logic graph for semantic (SOLOR)", UUID.fromString("fc2a0662-2396-575b-95f0-e9b38a418620"));

    /**
     * Java binding for the concept described as Navigation concept set (SOLOR) and identified by the following UUID(s):
     * <ul>
     * <li>fc965c5d-ad17-555e-bcb5-b78fd45c8c5f
     * </ul>
     */
    public static final Concept NAVIGATION_CONCEPT_SET = Concept.make("Navigation concept set (SOLOR)", UUID.fromString("fc965c5d-ad17-555e-bcb5-b78fd45c8c5f"));

    /**
     * Java binding for the concept described as Integer display Field and identified by the following UUID(s):
     * <ul>
     * <li>ff59c300-9c4e-5e77-a35d-6a133eb3440f
     * </ul>
     */
    public static final Concept INTEGER_DISPLAY_FIELD = Concept.make("Integer display Field", UUID.fromString("ff59c300-9c4e-5e77-a35d-6a133eb3440f"));
}