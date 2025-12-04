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
package dev.ikm.tinkar.terms;

import java.util.UUID;

public class TinkarTerm {
    /**
     * Java binding for the pattern described as <strong><em>Axiom attachment order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code 5a68c86b-35d5-579b-95c0-a2e7a93b7bdd}.
     */
    public static final EntityProxy.Pattern AXIOM_ATTACHMENT_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Axiom attachment order options assemblage (SOLOR)", UUID.fromString("5a68c86b-35d5-579b-95c0-a2e7a93b7bdd"));

    /**
     * Java binding for the pattern described as <strong><em>Axiom order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code 927563ae-cfb7-5a49-9424-1b81bf52f607}.
     */
    public static final EntityProxy.Pattern AXIOM_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Axiom order options assemblage (SOLOR)", UUID.fromString("927563ae-cfb7-5a49-9424-1b81bf52f607"));

    /**
     * Java binding for the pattern described as <strong><em>Comment Pattern</em></strong>;
     * identified by UUID: {@code 3734fb0a-4c14-5831-9a61-4743af609e7a}.
     * <p> Used to support comment semantics.
     * <p> Field 0 is a string that represents the text of the comment.
     */
    public static final EntityProxy.Pattern COMMENT_PATTERN =
            EntityProxy.Pattern.make("Comment pattern", UUID.fromString("3734fb0a-4c14-5831-9a61-4743af609e7a"));

    /**
     * Java binding for the pattern described as <strong><em>Concept attachment order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code f4c488eb-5ecc-50af-ac25-31d5f99c09a7}.
     */
    public static final EntityProxy.Pattern CONCEPT_ATTACHMENT_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Concept attachment order options assemblage (SOLOR)", UUID.fromString("f4c488eb-5ecc-50af-ac25-31d5f99c09a7"));

    /**
     * Java binding for the pattern described as <strong><em>Dependency management assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code b1dbb86b-e283-549e-ba94-5cb7dc3190c1}.
     */
    public static final EntityProxy.Pattern DEPENDENCY_MANAGEMENT_ASSEMBLAGE =
            EntityProxy.Pattern.make("Dependency management assemblage (SOLOR)", UUID.fromString("b1dbb86b-e283-549e-ba94-5cb7dc3190c1"));

    /**
     * Java binding for the pattern described as <strong><em>Description Pattern</em></strong>;
     * identified by UUID: {@code a4de0039-2625-5842-8a4c-d1ce6aebf021}.
     * <p> Used to support Description semantics.
     * <p> Field 0 is a Component that represents the language used by the description text.
     * <p> Field 1 is a String that represents the description text.
     * <p> Field 2 is a Component that represents the text case significance.
     * <p> Field 3 is a Component that represents the description type.
     */
    public static final EntityProxy.Pattern DESCRIPTION_PATTERN =
            EntityProxy.Pattern.make("Description pattern", UUID.fromString("a4de0039-2625-5842-8a4c-d1ce6aebf021"));

    /**
     * Java binding for the pattern described as <strong><em>Description attachment order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code 8d436a25-0ebf-59a8-bc8b-ad70c86d7f6a}.
     */
    public static final EntityProxy.Pattern DESCRIPTION_ATTACHMENT_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Description attachment order options assemblage (SOLOR)", UUID.fromString("8d436a25-0ebf-59a8-bc8b-ad70c86d7f6a"));

    /**
     * Java binding for the pattern described as <strong><em>Description type order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code 4b734c90-7b11-506b-9bc6-d24bf4f8255a}.
     */
    public static final EntityProxy.Pattern DESCRIPTION_TYPE_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Description type order options assemblage (SOLOR)", UUID.fromString("4b734c90-7b11-506b-9bc6-d24bf4f8255a"));

    /**
     * Java binding for the pattern described as <strong><em>Detail order options assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code 58a80695-42ed-5409-92c5-9703fde8916f}.
     */
    public static final EntityProxy.Pattern DETAIL_ORDER_OPTIONS_PATTERN =
            EntityProxy.Pattern.make("Detail order options assemblage (SOLOR)", UUID.fromString("58a80695-42ed-5409-92c5-9703fde8916f"));

    /**
     * Java binding for the pattern described as <strong><em>El Plus Plus Stated Axioms Pattern</em></strong>;
     * identified by UUID: {@code e813eb92-7d07-5035-8d43-e81249f5b36e}.
     * <p> Used to support stated axioms.
     * <p> Field 0 is a Ditree that represents stated terminological axioms.
     */
    public static final EntityProxy.Pattern EL_PLUS_PLUS_STATED_AXIOMS_PATTERN =
            EntityProxy.Pattern.make("EL++ stated form pattern", UUID.fromString("e813eb92-7d07-5035-8d43-e81249f5b36e"));

    /**
     * Java binding for the pattern described as <strong><em>El Plus Plus Inferred Axioms Pattern</em></strong>;
     * identified by UUID: {@code 9f011812-15c9-5b1b-85f8-bb262bc1b2a2}.
     * <p> Used to support inferred axioms.
     * <p> Field 0 is a Ditree that represents the inferred terminological axioms.
     */
    public static final EntityProxy.Pattern EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN =
            EntityProxy.Pattern.make("EL ++ Inferred form pattern", UUID.fromString("9f011812-15c9-5b1b-85f8-bb262bc1b2a2"));

    /**
     * Java binding for the pattern described as <strong><em>EL++ Inferred Digraph (SOLOR)</em></strong>;
     * identified by UUID: {@code aca20339-3fb0-540c-99bb-e366f3a30a1b}.
     */
    public static final EntityProxy.Pattern EL_PLUS_PLUS_INFERRED_DIGRAPH =
            EntityProxy.Pattern.make("EL++ Inferred Digraph (SOLOR)", UUID.fromString("aca20339-3fb0-540c-99bb-e366f3a30a1b"));

    /**
     * Java binding for the pattern described as <strong><em>EL++ Stated Digraph (SOLOR)</em></strong>;
     * identified by UUID: {@code d67c1c32-95ca-54a5-a4de-8b48312eb292}.
     */
    public static final EntityProxy.Pattern EL_PLUS_PLUS_STATED_DIGRAPH =
            EntityProxy.Pattern.make("EL++ Stated Digraph (SOLOR)", UUID.fromString("d67c1c32-95ca-54a5-a4de-8b48312eb292"));

    /**
     * Java binding for the pattern described as <strong><em>GB Dialect Pattern</em></strong>;
     * identified by UUID: {@code 561f817a-130e-5e56-984d-910e9991558c}.
     * <p> Used to define GB Dialect semantics for descriptions.
     * <p> Field 0 is a Component that represents the acceptability of a description for the GB English Dialect.
     */
    public static final EntityProxy.Pattern GB_DIALECT_PATTERN =
            EntityProxy.Pattern.make("GB English dialect", UUID.fromString("561f817a-130e-5e56-984d-910e9991558c"));

    /**
     * Java binding for the pattern described as <strong><em>Identifier Pattern</em></strong>;
     * identified by UUID: {@code 5d60e14b-c410-5172-9559-3c4253278ae2}.
     * <p> Used to support Identifier semantics.
     * <p> Field 0 is a Component that represents the source of the identifier.
     * <p> Field 1 is a string that represents the source of the identifier.
     */
    public static final EntityProxy.Pattern IDENTIFIER_PATTERN =
            EntityProxy.Pattern.make("Identifier Pattern", UUID.fromString("5d60e14b-c410-5172-9559-3c4253278ae2"));

    /**
     * Java binding for the pattern described as <strong><em>Inferred Navigation Pattern</em></strong>;
     * identified by UUID: {@code a53cc42d-c07e-5934-96b3-2ede3264474e}.
     * <p> Used to specify the relationship origins and destinations for concepts based on inferred axioms.
     * <p> Field 0 is a Component ID Set that represents Relationship Destinations.
     * <p> Field 1 is a Component ID Set that represents Relationship Origins.
     */
    public static final EntityProxy.Pattern INFERRED_NAVIGATION_PATTERN =
            EntityProxy.Pattern.make("Inferred navigation", UUID.fromString("a53cc42d-c07e-5934-96b3-2ede3264474e"));

    /**
     * Java binding for the pattern described as <strong><em>Komet Base Model Component Pattern</em></strong>;
     * identified by UUID: {@code bbbbf1fe-00f0-55e0-a19c-6300dbaab9b2}.
     * <p> Used to support Komet Base Model membership semantics.
     * <p> This membership semantic contains no fields and acts as a tag.
     */
    public static final EntityProxy.Pattern KOMET_BASE_MODEL_COMPONENT_PATTERN = EntityProxy.Pattern.make("Komet base model component pattern", UUID.fromString("bbbbf1fe-00f0-55e0-a19c-6300dbaab9b2"));

    /**
     * Java binding for the pattern described as <strong><em>Navigation pattern (SOLOR)</em></strong>;
     * identified by UUID: {@code f0c31815-f786-5ea3-b814-7dc0ba032563}.
     */
    public static final EntityProxy.Pattern NAVIGATION_PATTERN =
            EntityProxy.Pattern.make("Navigation pattern (SOLOR)", UUID.fromString("f0c31815-f786-5ea3-b814-7dc0ba032563"));

    /**
     * Java binding for the pattern described as <strong><em>OWL Axiom Syntax Pattern</em></strong>;
     * identified by UUID: {@code c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b}.
     * <p> Used to support the type of OWL axiom syntax.
     * <p> Field 0 is a String that represents the syntax type of the OWL axioms.
     */
    public static final EntityProxy.Pattern OWL_AXIOM_SYNTAX_PATTERN = EntityProxy.Pattern.make("OWL Axiom Syntax Pattern", UUID.fromString("c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b"));

    /**
     * Java binding for the pattern described as <strong><em>Version control path origin pattern</em></strong>;
     * identified by UUID: {@code 70f89dd5-2cdb-59bb-bbaa-98527513547c}.
     * <p> Used to support path branching semantics.
     * <p> Field 0 is a Component that represents the path concept.
     * <p> Field 1 is an Instant Literal that represents the path origin.
     */
    public static final EntityProxy.Pattern PATH_ORIGINS_PATTERN =
            EntityProxy.Pattern.make("Version control path origin pattern", UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c"));

    /**
     * Java binding for the pattern described as <strong><em>Version control module origin pattern</em></strong>;
     * identified by UUID: {@code 536b0ec4-4974-47ae-93a6-ae6c4d169780}.
     * <p> Used to support module origin semantics.
     * <p> Field 0 is a Component ID Set that represents the originating module (i.e., extended module) concept.
     */
    public static final EntityProxy.Pattern MODULE_ORIGINS_PATTERN =
            EntityProxy.Pattern.make("Version control module origin pattern", UUID.fromString("536b0ec4-4974-47ae-93a6-ae6c4d169780"));

    /**
     * Java binding for the pattern described as <strong><em>Version control path pattern</em></strong>;
     * identified by UUID: {@code add1db57-72fe-53c8-a528-1614bda20ec6}.
     * <p> Used to support path membership semantics.
     * <p> This membership semantic contains no fields and acts as a tag.
     */
    public static final EntityProxy.Pattern PATHS_PATTERN =
            EntityProxy.Pattern.make("Version control path pattern", UUID.fromString("add1db57-72fe-53c8-a528-1614bda20ec6"));

    /**
     * Java binding for the pattern described as <strong><em>SOLOR concept assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code d39b3ecd-9a80-5009-a8ac-0b947f95ca7c}.
     * <p> Used to support SOLOR membership semantics.
     * <p> This membership semantic contains no fields and acts as a tag.
     */
    public static final EntityProxy.Pattern SOLOR_CONCEPT_ASSEMBLAGE =
            EntityProxy.Pattern.make("SOLOR concept assemblage (SOLOR)", UUID.fromString("d39b3ecd-9a80-5009-a8ac-0b947f95ca7c"));

    /**
     * Java binding for the pattern described as <strong><em>STAMP Pattern</em></strong>;
     * identified by UUID: {@code 9fd67fee-abf9-551d-9d0e-76a4b1e8b4ee}.
     * <p> Used to define the structure of STAMP components.
     * <p> Field 0 is a Component that represents the Status.
     * <p> Field 1 is a Long that represents the Time of creation in Epoch Milliseconds.
     * <p> Field 2 is a Component that represents the Author.
     * <p> Field 3 is a Component that represents the Module.
     * <p> Field 4 is a Component that represents the Path.
     */
    public static final EntityProxy.Pattern STAMP_PATTERN =
            EntityProxy.Pattern.make("STAMP pattern", UUID.fromString("9fd67fee-abf9-551d-9d0e-76a4b1e8b4ee"));

    /**
     * Java binding for the pattern described as <strong><em>Stated Navigation Pattern</em></strong>;
     * identified by UUID: {@code d02957d6-132d-5b3c-adba-505f5778d998}.
     * <p> Used to specify the relationship origins and destinations for concepts based on stated axioms.
     * <p> Field 0 is a Component ID Set for the relationship destinations.
     * <p> Field 1 is a Component ID Set for the relationship origins.
     */
    public static final EntityProxy.Pattern STATED_NAVIGATION_PATTERN =
            EntityProxy.Pattern.make("Stated navigation", UUID.fromString("d02957d6-132d-5b3c-adba-505f5778d998"));
    /**
     * Java binding for the concept described as <strong><em>Starter Data Authoring (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/070deb74-acc5-46bf-b9c6-eaee1b58ef52">
     * 070deb74-acc5-46bf-b9c6-eaee1b58ef52</a>}.
     */
    public static final EntityProxy.Concept STARTER_DATA_AUTHORING =
            EntityProxy.Concept.make("Starter Data Authoring (SOLOR)", UUID.fromString("070deb74-acc5-46bf-b9c6-eaee1b58ef52"));
    /**
     * Java binding for the pattern described as <strong><em>Tinkar Base Model Component Pattern</em></strong>;
     * identified by UUID: {@code 6070f6f5-893d-5144-adce-7d305c391cf9}.
     * <p> Used to support Tinkar Base Model membership semantics.
     * <p> This membership semantic contains no fields and acts as a tag.
     */
    public static final EntityProxy.Pattern TINKAR_BASE_MODEL_COMPONENT_PATTERN = EntityProxy.Pattern.make("Tinkar base model component pattern", UUID.fromString("6070f6f5-893d-5144-adce-7d305c391cf9"));

    /**
     * Java binding for the pattern described as <strong><em>US Dialect Pattern</em></strong>;
     * identified by UUID: {@code 08f9112c-c041-56d3-b89b-63258f070074}.
     * <p> Used to define US Dialect semantics for descriptions.
     * <p> Field 0 is a Component that represents the acceptability of a description for the US English Dialect.
     */
    public static final EntityProxy.Pattern US_DIALECT_PATTERN =
            EntityProxy.Pattern.make("US Dialect pattern", UUID.fromString("08f9112c-c041-56d3-b89b-63258f070074"));


    /**
     * Java binding for the pattern described as <strong><em>Value Constraint Pattern</em></strong>;
     * identified by UUID: {@code 922697f7-36ba-4afc-9dd5-f29d54b0fdec}.
     * <p> Used to define the value constraint pattern.
     * <p> Field 0 is a Concept that represents the Value Constraint Source.
     * <p> Field 1 is a Concept that represents the Minimum Value Operator.
     * <p> Field 2 is a Float that represents the Reference Range Minimum.
     * <p> Field 3 is a Component that represents the Maximum Value Operator.
     * <p> Field 4 is a Float that represents the Reference Range Maximum.
     * <p> Field 5 is a String that represents the Example UCUM Units.
     */
    public static final EntityProxy.Pattern VALUE_CONSTRAINT_PATTERN =
            EntityProxy.Pattern.make("Value Constraint Pattern", UUID.fromString("922697f7-36ba-4afc-9dd5-f29d54b0fdec"));

    /**
     * Java binding for the concept described as <strong><em>Acceptable (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/12b9e103-060e-3256-9982-18c1191af60e">
     * 12b9e103-060e-3256-9982-18c1191af60e</a>}.
     */
    public static final EntityProxy.Concept ACCEPTABLE =
            EntityProxy.Concept.make("Acceptable (SOLOR)", UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));
    /**
     * Java binding for the concept described as <strong><em>Action name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cbbbbd04-cf98-5ab6-a336-de3a5ea988c2">
     * cbbbbd04-cf98-5ab6-a336-de3a5ea988c2</a>}.
     */
    public static final EntityProxy.Concept ACTION_NAME =
            EntityProxy.Concept.make("Action name (SOLOR)", UUID.fromString("cbbbbd04-cf98-5ab6-a336-de3a5ea988c2"));
    /**
     * Java binding for the concept described as <strong><em>Action properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/80ba281c-7d47-57cf-8100-82b69bce998b">
     * 80ba281c-7d47-57cf-8100-82b69bce998b</a>}.
     */
    public static final EntityProxy.Concept ACTION_PROPERTIES =
            EntityProxy.Concept.make("Action properties (SOLOR)", UUID.fromString("80ba281c-7d47-57cf-8100-82b69bce998b"));
    /**
     * Java binding for the concept described as <strong><em>Action purpose (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7a468a13-d65c-5ae2-a290-d4bd66f4f9fd">
     * 7a468a13-d65c-5ae2-a290-d4bd66f4f9fd</a>}.
     */
    public static final EntityProxy.Concept ACTION_PURPOSE =
            EntityProxy.Concept.make("Action purpose (SOLOR)", UUID.fromString("7a468a13-d65c-5ae2-a290-d4bd66f4f9fd"));
    /**
     * Java binding for the concept described as <strong><em>Active ingredient is different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0fcbce59-cd8d-559a-bc19-9f5dca60962e">
     * 0fcbce59-cd8d-559a-bc19-9f5dca60962e</a>}.
     */
    public static final EntityProxy.Concept ACTIVE_INGREDIENT_IS_DIFFERENT =
            EntityProxy.Concept.make("Active ingredient is different (SOLOR)", UUID.fromString("0fcbce59-cd8d-559a-bc19-9f5dca60962e"));
    /**
     * Java binding for the concept described as <strong><em>Active only description Lucene match (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e047b6ea-c00f-11e7-abc4-cec278b6b50a">
     * e047b6ea-c00f-11e7-abc4-cec278b6b50a</a>}.
     */
    public static final EntityProxy.Concept ACTIVE_ONLY_DESCRIPTION_LUCENE_MATCH____QUERY_CLAUSE =
            EntityProxy.Concept.make("Active only description Lucene match (query clause)", UUID.fromString("e047b6ea-c00f-11e7-abc4-cec278b6b50a"));
    /**
     * Java binding for the concept described as <strong><em>Active only description regex match (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/48bafde1-02a4-5d74-b1e4-8909e7e5b5fc">
     * 48bafde1-02a4-5d74-b1e4-8909e7e5b5fc</a>}.
     */
    public static final EntityProxy.Concept ACTIVE_ONLY_DESCRIPTION_REGEX_MATCH____QUERY_CLAUSE =
            EntityProxy.Concept.make("Active only description regex match (query clause)", UUID.fromString("48bafde1-02a4-5d74-b1e4-8909e7e5b5fc"));
    /**
     * Java binding for the concept described as <strong><em>Active status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/09f12001-0e4f-51e2-9852-44862a4a0db4">
     * 09f12001-0e4f-51e2-9852-44862a4a0db4</a>}.
     */
    public static final EntityProxy.Concept ACTIVE_STATE =
            EntityProxy.Concept.make("Active state (SOLOR)", UUID.fromString("09f12001-0e4f-51e2-9852-44862a4a0db4"));
    /**
     * Java binding for the concept described as <strong><em>Activities panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b219cca8-b82b-5ab8-83f6-a24c89e9ce19">
     * b219cca8-b82b-5ab8-83f6-a24c89e9ce19</a>}.
     */
    public static final EntityProxy.Concept ACTIVITIES_PANEL =
            EntityProxy.Concept.make("Activities panel (SOLOR)", UUID.fromString("b219cca8-b82b-5ab8-83f6-a24c89e9ce19"));
    /**
     * Java binding for the concept described as <strong><em>All child criterion are satisfied for component (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1e360-579e-11e7-907b-a6006ad3dba0">
     * d9c1e360-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ALL_CHILD_CRITERION_ARE_SATISFIED_FOR_COMPONENT____QUERY_CLAUSE =
            EntityProxy.Concept.make("All child criterion are satisfied for component (query clause)", UUID.fromString("d9c1e360-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Allergen (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/77129c1f-fb6c-5a55-a818-3404bf7fa9e9">
     * 77129c1f-fb6c-5a55-a818-3404bf7fa9e9</a>}.
     */
    public static final EntityProxy.Concept ALLERGEN =
            EntityProxy.Concept.make("Allergen (SOLOR)", UUID.fromString("77129c1f-fb6c-5a55-a818-3404bf7fa9e9"));
    /**
     * Java binding for the concept described as <strong><em>Allowed states for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/23f69f6f-a502-5876-a835-2b1b4d5ce91e">
     * 23f69f6f-a502-5876-a835-2b1b4d5ce91e</a>}.
     */
    public static final EntityProxy.Concept ALLOWED_STATES_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Allowed states for stamp coordinate (SOLOR)", UUID.fromString("23f69f6f-a502-5876-a835-2b1b4d5ce91e"));
    /**
     * Java binding for the concept described as <strong><em>And (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fa113d51-07d2-587c-8930-0bce207d506d">
     * fa113d51-07d2-587c-8930-0bce207d506d</a>}.
     */
    public static final EntityProxy.Concept AND =
            EntityProxy.Concept.make("And (SOLOR)", UUID.fromString("fa113d51-07d2-587c-8930-0bce207d506d"));
    /**
     * Java binding for the concept described as <strong><em>Annotation type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3fe77951-58c9-51b3-8e7e-65edcf7ace0a">
     * 3fe77951-58c9-51b3-8e7e-65edcf7ace0a</a>}.
     */
    public static final EntityProxy.Concept ANNOTATION_TYPE =
            EntityProxy.Concept.make("Annotation type (SOLOR)", UUID.fromString("3fe77951-58c9-51b3-8e7e-65edcf7ace0a"));
    /**
     * Java binding for the concept described as <strong><em>Anonymous concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f8f936d4-3ac7-5629-9f65-9452608056a1">
     * f8f936d4-3ac7-5629-9f65-9452608056a1</a>}.
     */
    public static final EntityProxy.Concept ANONYMOUS_CONCEPT =
            EntityProxy.Concept.make("Anonymous concept (SOLOR)", UUID.fromString("f8f936d4-3ac7-5629-9f65-9452608056a1"));
    /**
     * Java binding for the concept described as <strong><em>Any assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6b8b1f01-9da2-585e-828c-eb1c7b93d250">
     * 6b8b1f01-9da2-585e-828c-eb1c7b93d250</a>}.
     */
    public static final EntityProxy.Concept ANY_ASSEMBLAGE =
            EntityProxy.Concept.make("Any assemblage (SOLOR)", UUID.fromString("6b8b1f01-9da2-585e-828c-eb1c7b93d250"));
    /**
     * Java binding for the concept described as <strong><em>Any child criterion is satisfied for component (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f24c-579e-11e7-907b-a6006ad3dba0">
     * d9c1f24c-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ANY_CHILD_CRITERION_IS_SATISFIED_FOR_COMPONENT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Any child criterion is satisfied for component (query clause)", UUID.fromString("d9c1f24c-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Any component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/927da7ac-3403-5ccc-b07b-88f60cc3a5f8">
     * 927da7ac-3403-5ccc-b07b-88f60cc3a5f8</a>}.
     */
    public static final EntityProxy.Concept ANY_COMPONENT =
            EntityProxy.Concept.make("Any component (SOLOR)", UUID.fromString("927da7ac-3403-5ccc-b07b-88f60cc3a5f8"));
    /**
     * Java binding for the concept described as <strong><em>Apache 2 license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a4516185-deb8-5db1-8db8-10dbe021ffa5">
     * a4516185-deb8-5db1-8db8-10dbe021ffa5</a>}.
     */
    public static final EntityProxy.Concept APACHE_2_LICENSE =
            EntityProxy.Concept.make("Apache 2 license (SOLOR)", UUID.fromString("a4516185-deb8-5db1-8db8-10dbe021ffa5"));
    /**
     * Java binding for the concept described as <strong><em>Array (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/318622e6-dd7a-5651-851d-2d5c2af85767">
     * 318622e6-dd7a-5651-851d-2d5c2af85767</a>}.
     */
    public static final EntityProxy.Concept ARRAY =
            EntityProxy.Concept.make("Array (SOLOR)", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767"));
    /**
     * Java binding for the concept described as <strong><em>Array field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b168ad04-f814-5036-b886-fd4913de88c8">
     * b168ad04-f814-5036-b886-fd4913de88c8</a>}.
     */
    public static final EntityProxy.Concept ARRAY_FIELD =
            EntityProxy.Concept.make("Array field (SOLOR)", UUID.fromString("b168ad04-f814-5036-b886-fd4913de88c8"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3e0cd740-2cc6-3d68-ace7-bad2eb2621da">
     * 3e0cd740-2cc6-3d68-ace7-bad2eb2621da</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE =
            EntityProxy.Concept.make("Assemblage (SOLOR)", UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage 1 to join (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fef1e58b-a8c8-53bb-9ded-88f1e792264a">
     * fef1e58b-a8c8-53bb-9ded-88f1e792264a</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_1_TO_JOIN =
            EntityProxy.Concept.make("Assemblage 1 to join (SOLOR)", UUID.fromString("fef1e58b-a8c8-53bb-9ded-88f1e792264a"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage 2 to join (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b8500a4b-96a8-52de-b9fe-5f1596394b7c">
     * b8500a4b-96a8-52de-b9fe-5f1596394b7c</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_2_TO_JOIN =
            EntityProxy.Concept.make("Assemblage 2 to join (SOLOR)", UUID.fromString("b8500a4b-96a8-52de-b9fe-5f1596394b7c"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage Lucene match (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20a5c-579e-11e7-907b-a6006ad3dba0">
     * d9c20a5c-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_LUCENE_MATCH____QUERY_CLAUSE =
            EntityProxy.Concept.make("Assemblage Lucene match (query clause)", UUID.fromString("d9c20a5c-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage contains component (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20976-579e-11e7-907b-a6006ad3dba0">
     * d9c20976-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_CONTAINS_COMPONENT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Assemblage contains component (query clause)", UUID.fromString("d9c20976-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage contains concept (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c208a4-579e-11e7-907b-a6006ad3dba0">
     * d9c208a4-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_CONTAINS_CONCEPT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Assemblage contains concept (query clause)", UUID.fromString("d9c208a4-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage contains kind-of concept (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20b38-579e-11e7-907b-a6006ad3dba0">
     * d9c20b38-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Assemblage contains kind-of concept (query clause)", UUID.fromString("d9c20b38-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage contains string (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c207c8-579e-11e7-907b-a6006ad3dba0">
     * d9c207c8-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_CONTAINS_STRING____QUERY_CLAUSE =
            EntityProxy.Concept.make("Assemblage contains string (query clause)", UUID.fromString("d9c207c8-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage for action (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1391d8f7-15ff-5a80-a5af-e3a3d193368d">
     * 1391d8f7-15ff-5a80-a5af-e3a3d193368d</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_FOR_ACTION =
            EntityProxy.Concept.make("Assemblage for action (SOLOR)", UUID.fromString("1391d8f7-15ff-5a80-a5af-e3a3d193368d"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage for constraint (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6ccbdc74-2f5c-5963-92e8-1569814b54b9">
     * 6ccbdc74-2f5c-5963-92e8-1569814b54b9</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_FOR_CONSTRAINT =
            EntityProxy.Concept.make("Assemblage for constraint (SOLOR)", UUID.fromString("6ccbdc74-2f5c-5963-92e8-1569814b54b9"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage list for query (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a586ebe5-2f14-57b2-a6f5-b5a3ab38f670">
     * a586ebe5-2f14-57b2-a6f5-b5a3ab38f670</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_LIST_FOR_QUERY =
            EntityProxy.Concept.make("Assemblage list for query (SOLOR)", UUID.fromString("a586ebe5-2f14-57b2-a6f5-b5a3ab38f670"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage membership type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cdb0dd74-c3d9-58dc-80b5-3c42be4abb33">
     * cdb0dd74-c3d9-58dc-80b5-3c42be4abb33</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_MEMBERSHIP_TYPE =
            EntityProxy.Concept.make("Assemblage membership type (SOLOR)", UUID.fromString("cdb0dd74-c3d9-58dc-80b5-3c42be4abb33"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage nid for component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3de7b91a-d384-5651-95eb-161f13cde990">
     * 3de7b91a-d384-5651-95eb-161f13cde990</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_NID_FOR_COMPONENT =
            EntityProxy.Concept.make("Assemblage nid for component (SOLOR)", UUID.fromString("3de7b91a-d384-5651-95eb-161f13cde990"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3173cdfe-8721-51ee-9e21-a9bb61a6d4ae">
     * 3173cdfe-8721-51ee-9e21-a9bb61a6d4ae</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_PANEL =
            EntityProxy.Concept.make("Assemblage panel (SOLOR)", UUID.fromString("3173cdfe-8721-51ee-9e21-a9bb61a6d4ae"));
    /**
     * Java binding for the concept described as <strong><em>Assemblage related to path management (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/22808293-68e3-5397-84fe-f5e4e5394537">
     * 22808293-68e3-5397-84fe-f5e4e5394537</a>}.
     */
    public static final EntityProxy.Concept ASSEMBLAGE_RELATED_TO_PATH_MANAGEMENT =
            EntityProxy.Concept.make("Assemblage related to path management (SOLOR)", UUID.fromString("22808293-68e3-5397-84fe-f5e4e5394537"));
    /**
     * Java binding for the concept described as <strong><em>Associated parameter (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a44e673b-14c4-525b-99be-0b5dfa0280c8">
     * a44e673b-14c4-525b-99be-0b5dfa0280c8</a>}.
     */
    public static final EntityProxy.Concept ASSOCIATED_PARAMETER____QUERY_CLAUSE =
            EntityProxy.Concept.make("Associated parameter (query clause)", UUID.fromString("a44e673b-14c4-525b-99be-0b5dfa0280c8"));
    /**
     * Java binding for the concept described as <strong><em>Association id (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/73a6982f-92b7-5e66-bedc-c9a9407697f0">
     * 73a6982f-92b7-5e66-bedc-c9a9407697f0</a>}.
     */
    public static final EntityProxy.Concept ASSOCIATION_ID =
            EntityProxy.Concept.make("Association id (SOLOR)", UUID.fromString("73a6982f-92b7-5e66-bedc-c9a9407697f0"));
    /**
     * Java binding for the concept described as <strong><em>Association semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/326d1efc-ca0e-5d02-9377-900f722f7cfa">
     * 326d1efc-ca0e-5d02-9377-900f722f7cfa</a>}.
     */
    public static final EntityProxy.Concept ASSOCIATION_SEMANTIC =
            EntityProxy.Concept.make("Association semantic (SOLOR)", UUID.fromString("326d1efc-ca0e-5d02-9377-900f722f7cfa"));
    /**
     * Java binding for the concept described as <strong><em>Author for edit coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/337e93ba-531b-59a4-8153-57dca00e58d2">
     * 337e93ba-531b-59a4-8153-57dca00e58d2</a>}.
     */
    public static final EntityProxy.Concept AUTHOR_FOR_EDIT_COORDINATE =
            EntityProxy.Concept.make("Author for edit coordinate (SOLOR)", UUID.fromString("337e93ba-531b-59a4-8153-57dca00e58d2"));
    /**
     * Java binding for the concept described as <strong><em>Author for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4eb9de0d-7486-5f18-a9b4-82e3432f4103">
     * 4eb9de0d-7486-5f18-a9b4-82e3432f4103</a>}.
     */
    public static final EntityProxy.Concept AUTHOR_FOR_VERSION =
            EntityProxy.Concept.make("Author for version (SOLOR)", UUID.fromString("4eb9de0d-7486-5f18-a9b4-82e3432f4103"));
    /**
     * Java binding for the concept described as <strong><em>Authors for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4fda23b8-b016-5d2a-97d5-7ff779d60701">
     * 4fda23b8-b016-5d2a-97d5-7ff779d60701</a>}.
     */
    public static final EntityProxy.Concept AUTHORS_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Authors for stamp coordinate (SOLOR)", UUID.fromString("4fda23b8-b016-5d2a-97d5-7ff779d60701"));
    /**
     * Java binding for the concept described as <strong><em>Automation issue (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/10fb9c01-f831-5d69-8360-f5a62eaafe19">
     * 10fb9c01-f831-5d69-8360-f5a62eaafe19</a>}.
     */
    public static final EntityProxy.Concept AUTOMATION_ISSUE =
            EntityProxy.Concept.make("Automation issue (SOLOR)", UUID.fromString("10fb9c01-f831-5d69-8360-f5a62eaafe19"));
    /**
     * Java binding for the concept described as <strong><em>Automation issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/23d890e5-2979-5337-b842-b0a8b8c336ae">
     * 23d890e5-2979-5337-b842-b0a8b8c336ae</a>}.
     */
    public static final EntityProxy.Concept AUTOMATION_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("Automation issue assemblage (SOLOR)", UUID.fromString("23d890e5-2979-5337-b842-b0a8b8c336ae"));
    /**
     * Java binding for the concept described as <strong><em>Automation rule assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/688cdb52-94a4-5c1c-af2f-f66896bb0df0">
     * 688cdb52-94a4-5c1c-af2f-f66896bb0df0</a>}.
     */
    public static final EntityProxy.Concept AUTOMATION_RULE_ASSEMBLAGE =
            EntityProxy.Concept.make("Automation rule assemblage (SOLOR)", UUID.fromString("688cdb52-94a4-5c1c-af2f-f66896bb0df0"));

    /**
     * Java binding for the concept described as <strong><em>Axiom focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9c6fbddd-58bd-5881-b926-c813bbff849b">
     * 9c6fbddd-58bd-5881-b926-c813bbff849b</a>}.
     */
    public static final EntityProxy.Concept AXIOM_FOCUS =
            EntityProxy.Concept.make("Axiom focus (SOLOR)", UUID.fromString("9c6fbddd-58bd-5881-b926-c813bbff849b"));

    /**
     * Java binding for the concept described as <strong><em>Axiom origin (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b868bd83-5cd4-5d84-9cf7-b08674fbc79b">
     * b868bd83-5cd4-5d84-9cf7-b08674fbc79b</a>}.
     */
    public static final EntityProxy.Concept AXIOM_ORIGIN =
            EntityProxy.Concept.make("Axiom origin (SOLOR)", UUID.fromString("b868bd83-5cd4-5d84-9cf7-b08674fbc79b"));
    /**
     * Java binding for the concept described as <strong><em>Axiom Syntax (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8da1c508-c2a2-4899-b26d-87f8b98a7558">
     * 8da1c508-c2a2-4899-b26d-87f8b98a7558</a>}.
     */
    public static final EntityProxy.Concept AXIOM_SYNTAX =
            EntityProxy.Concept.make("Axiom Syntax (SOLOR)", UUID.fromString("8da1c508-c2a2-4899-b26d-87f8b98a7558"));
    /**
     * Java binding for the concept described as <strong><em>Body structure (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4be3f62e-28d5-3bb4-a424-9aa7856a1790">
     * 4be3f62e-28d5-3bb4-a424-9aa7856a1790</a>}.
     */
    public static final EntityProxy.Concept BODY_STRUCTURE =
            EntityProxy.Concept.make("Body structure (SOLOR)", UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));
    /**
     * Java binding for the concept described as <strong><em>Boolean field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6b9e2cc-31c6-5e80-91b7-7537690aae32">
     * d6b9e2cc-31c6-5e80-91b7-7537690aae32</a>}.
     */
    public static final EntityProxy.Concept BOOLEAN_FIELD =
            EntityProxy.Concept.make("Boolean field (SOLOR)", UUID.fromString("d6b9e2cc-31c6-5e80-91b7-7537690aae32"));
    /**
     * Java binding for the concept described as <strong><em>Boolean literal (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/49f41695-66a7-5471-846d-21c168f54c19">
     * 49f41695-66a7-5471-846d-21c168f54c19</a>}.
     */
    public static final EntityProxy.Concept BOOLEAN_LITERAL =
            EntityProxy.Concept.make("Boolean literal (SOLOR)", UUID.fromString("49f41695-66a7-5471-846d-21c168f54c19"));
    /**
     * Java binding for the concept described as <strong><em>Boolean reference (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/de49d207-a26e-5f8a-b905-953a4dd13c21">
     * de49d207-a26e-5f8a-b905-953a4dd13c21</a>}.
     */
    public static final EntityProxy.Concept BOOLEAN_REFERENCE =
            EntityProxy.Concept.make("Boolean reference (SOLOR)", UUID.fromString("de49d207-a26e-5f8a-b905-953a4dd13c21"));
    /**
     * Java binding for the concept described as <strong><em>Boolean substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/03559f9d-f1e4-5485-894b-4d457f145d54">
     * 03559f9d-f1e4-5485-894b-4d457f145d54</a>}.
     */
    public static final EntityProxy.Concept BOOLEAN_SUBSTITUTION =
            EntityProxy.Concept.make("Boolean substitution (SOLOR)", UUID.fromString("03559f9d-f1e4-5485-894b-4d457f145d54"));
    /**
     * Java binding for the concept described as <strong><em>Boss substances are different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9eb3da70-0f2a-58db-a6c2-a8bea6703331">
     * 9eb3da70-0f2a-58db-a6c2-a8bea6703331</a>}.
     */
    public static final EntityProxy.Concept BOSS_SUBSTANCES_ARE_DIFFERENT =
            EntityProxy.Concept.make("Boss substances are different (SOLOR)", UUID.fromString("9eb3da70-0f2a-58db-a6c2-a8bea6703331"));
    /**
     * Java binding for the concept described as <strong><em>Broad to Narrow (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c1068428-a986-5c12-9583-9b2d3a24fdc6">
     * c1068428-a986-5c12-9583-9b2d3a24fdc6</a>}.
     */
    public static final EntityProxy.Concept BROAD_TO_NARROW =
            EntityProxy.Concept.make("Broad to Narrow (SOLOR)", UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"));
    /**
     * Java binding for the concept described as <strong><em>Business rules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7ebc6742-8586-58c3-b49d-765fb5a93f35">
     * 7ebc6742-8586-58c3-b49d-765fb5a93f35</a>}.
     */
    public static final EntityProxy.Concept BUSINESS_RULES =
            EntityProxy.Concept.make("Business rules (SOLOR)", UUID.fromString("7ebc6742-8586-58c3-b49d-765fb5a93f35"));
    /**
     * Java binding for the concept described as <strong><em>Byte array field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dbdd8df2-aec3-596b-88fc-7b83b5594a45">
     * dbdd8df2-aec3-596b-88fc-7b83b5594a45</a>}.
     */
    public static final EntityProxy.Concept BYTE_ARRAY_FIELD =
            EntityProxy.Concept.make("Byte array field (SOLOR)", UUID.fromString("dbdd8df2-aec3-596b-88fc-7b83b5594a45"));
    /**
     * Java binding for the concept described as <strong><em>CPT® modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a9fe12fd-670e-5d5f-bd81-f2fa4a69bc7a">
     * a9fe12fd-670e-5d5f-bd81-f2fa4a69bc7a</a>}.
     */
    public static final EntityProxy.Concept CPT_MODULES =
            EntityProxy.Concept.make("CPT® modules (SOLOR)", UUID.fromString("a9fe12fd-670e-5d5f-bd81-f2fa4a69bc7a"));
    /**
     * Java binding for the concept described as <strong><em>CVX Code (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ab93a5aa-22dc-5b43-bc17-0909ec83aa23">
     * ab93a5aa-22dc-5b43-bc17-0909ec83aa23</a>}.
     */
    public static final EntityProxy.Concept CVX_CODE =
            EntityProxy.Concept.make("CVX Code (SOLOR)", UUID.fromString("ab93a5aa-22dc-5b43-bc17-0909ec83aa23"));
    /**
     * Java binding for the concept described as <strong><em>CVX Definition Assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e14a3f46-83fb-5b13-b393-33b27286ef2e">
     * e14a3f46-83fb-5b13-b393-33b27286ef2e</a>}.
     */
    public static final EntityProxy.Concept CVX_DEFINITION_ASSEMBLAGE =
            EntityProxy.Concept.make("CVX Definition Assemblage (SOLOR)", UUID.fromString("e14a3f46-83fb-5b13-b393-33b27286ef2e"));
    /**
     * Java binding for the concept described as <strong><em>CVX Description ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/debe745f-9ae1-59f9-8111-f13764e64b30">
     * debe745f-9ae1-59f9-8111-f13764e64b30</a>}.
     */
    public static final EntityProxy.Concept CVX_DESCRIPTION_ID =
            EntityProxy.Concept.make("CVX Description ID (SOLOR)", UUID.fromString("debe745f-9ae1-59f9-8111-f13764e64b30"));
    /**
     * Java binding for the concept described as <strong><em>CVX modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c1c7ca21-9130-5409-9cf6-bf401f471268">
     * c1c7ca21-9130-5409-9cf6-bf401f471268</a>}.
     */
    public static final EntityProxy.Concept CVX_MODULES =
            EntityProxy.Concept.make("CVX modules (SOLOR)", UUID.fromString("c1c7ca21-9130-5409-9cf6-bf401f471268"));
    /**
     * Java binding for the concept described as <strong><em>Canceled status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b42c1948-7645-5da8-a888-de6ec020ab98">
     * b42c1948-7645-5da8-a888-de6ec020ab98</a>}.
     */
    public static final EntityProxy.Concept CANCELED_STATE =
            EntityProxy.Concept.make("Canceled state (SOLOR)", UUID.fromString("b42c1948-7645-5da8-a888-de6ec020ab98"));
    /**
     * Java binding for the concept described as <strong><em>Case insensitive evaluation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/74bbdaff-f061-5807-b334-3c88ac3e9421">
     * 74bbdaff-f061-5807-b334-3c88ac3e9421</a>}.
     */
    public static final EntityProxy.Concept CASE_INSENSITIVE_EVALUATION =
            EntityProxy.Concept.make("Case insensitive evaluation (SOLOR)", UUID.fromString("74bbdaff-f061-5807-b334-3c88ac3e9421"));
    /**
     * Java binding for the concept described as <strong><em>Case sensitive evaluation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a95e5dbc-a179-57f9-9cdd-6de8c026396d">
     * a95e5dbc-a179-57f9-9cdd-6de8c026396d</a>}.
     */
    public static final EntityProxy.Concept CASE_SENSITIVE_EVALUATION =
            EntityProxy.Concept.make("Case sensitive evaluation (SOLOR)", UUID.fromString("a95e5dbc-a179-57f9-9cdd-6de8c026396d"));
    /**
     * Java binding for the concept described as <strong><em>Case significance concept nid for description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/57271621-3f3c-58dd-8148-2674bc11b7e5">
     * 57271621-3f3c-58dd-8148-2674bc11b7e5</a>}.
     */
    public static final EntityProxy.Concept CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION =
            EntityProxy.Concept.make("Case significance concept nid for description (SOLOR)", UUID.fromString("57271621-3f3c-58dd-8148-2674bc11b7e5"));
    /**
     * Java binding for the concept described as <strong><em>Center pane defaults (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7fb52e9d-f77c-5ef9-8191-677365e02b4b">
     * 7fb52e9d-f77c-5ef9-8191-677365e02b4b</a>}.
     */
    public static final EntityProxy.Concept CENTER_PANE_DEFAULTS =
            EntityProxy.Concept.make("Center pane defaults (SOLOR)", UUID.fromString("7fb52e9d-f77c-5ef9-8191-677365e02b4b"));
    /**
     * Java binding for the concept described as <strong><em>Center pane options (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/99361b98-715c-596b-9b99-ad276fbd5aba">
     * 99361b98-715c-596b-9b99-ad276fbd5aba</a>}.
     */
    public static final EntityProxy.Concept CENTER_PANE_OPTIONS =
            EntityProxy.Concept.make("Center pane options (SOLOR)", UUID.fromString("99361b98-715c-596b-9b99-ad276fbd5aba"));
    /**
     * Java binding for the concept described as <strong><em>Center tab nodes (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1224e3ff-3411-590f-bb9b-4d4c820dc3ee">
     * 1224e3ff-3411-590f-bb9b-4d4c820dc3ee</a>}.
     */
    public static final EntityProxy.Concept CENTER_TAB_NODES =
            EntityProxy.Concept.make("Center tab nodes (SOLOR)", UUID.fromString("1224e3ff-3411-590f-bb9b-4d4c820dc3ee"));
    /**
     * Java binding for the concept described as <strong><em>Characteristic nid for rf2 relationship (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2b9b0347-0f38-52e0-88bf-c4a70357d7d2">
     * 2b9b0347-0f38-52e0-88bf-c4a70357d7d2</a>}.
     */
    public static final EntityProxy.Concept CHARACTERISTIC_NID_FOR_RF2_RELATIONSHIP =
            EntityProxy.Concept.make("Characteristic nid for rf2 relationship (SOLOR)", UUID.fromString("2b9b0347-0f38-52e0-88bf-c4a70357d7d2"));
    /**
     * Java binding for the concept described as <strong><em>Child of (internal use relationship type)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/91947d30-7555-5400-bbe2-4415472cff1b">
     * 91947d30-7555-5400-bbe2-4415472cff1b</a>}.
     */
    public static final EntityProxy.Concept CHILD_OF____INTERNAL_USE_RELATIONSHIP_TYPE =
            EntityProxy.Concept.make("Child of (internal use relationship type)", UUID.fromString("91947d30-7555-5400-bbe2-4415472cff1b"));
    /**
     * Java binding for the concept described as <strong><em>Chinese language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ba2efe6b-fe56-3d91-ae0f-3b389628f74c">
     * ba2efe6b-fe56-3d91-ae0f-3b389628f74c</a>}.
     */
    public static final EntityProxy.Concept CHINESE_LANGUAGE =
            EntityProxy.Concept.make("Chinese language (SOLOR)", UUID.fromString("ba2efe6b-fe56-3d91-ae0f-3b389628f74c"), UUID.fromString("45022532-9567-11e5-8994-feff819cdc9f"), UUID.fromString("aacbc859-e9a0-5e01-b6a9-9a255a47b0c9"));
    /**
     * Java binding for the concept described as <strong><em>Chronicle properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2ba2ef47-30af-57ec-9073-38693f020d7e">
     * 2ba2ef47-30af-57ec-9073-38693f020d7e</a>}.
     */
    public static final EntityProxy.Concept CHRONICLE_PROPERTIES =
            EntityProxy.Concept.make("Chronicle properties (SOLOR)", UUID.fromString("2ba2ef47-30af-57ec-9073-38693f020d7e"));
    /**
     * Java binding for the concept described as <strong><em>Circumstance properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0e1edc84-74b6-5d47-bd4d-49076cca5222">
     * 0e1edc84-74b6-5d47-bd4d-49076cca5222</a>}.
     */
    public static final EntityProxy.Concept CIRCUMSTANCE_PROPERTIES =
            EntityProxy.Concept.make("Circumstance properties (SOLOR)", UUID.fromString("0e1edc84-74b6-5d47-bd4d-49076cca5222"));
    /**
     * Java binding for the concept described as <strong><em>Classification results panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/98f503c9-d486-5013-b107-a2ab34d1a422">
     * 98f503c9-d486-5013-b107-a2ab34d1a422</a>}.
     */
    public static final EntityProxy.Concept CLASSIFICATION_RESULTS_PANEL =
            EntityProxy.Concept.make("Classification results panel (SOLOR)", UUID.fromString("98f503c9-d486-5013-b107-a2ab34d1a422"));
    /**
     * Java binding for the concept described as <strong><em>Classifier for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4b90e89d-2a0e-5ca3-8ae5-7498d148a9d2">
     * 4b90e89d-2a0e-5ca3-8ae5-7498d148a9d2</a>}.
     */
    public static final EntityProxy.Concept CLASSIFIER_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Classifier for logic coordinate (SOLOR)", UUID.fromString("4b90e89d-2a0e-5ca3-8ae5-7498d148a9d2"));
    /**
     * Java binding for the concept described as <strong><em>Clinical statement issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8208bfa9-0e7f-5c26-983c-da320ef36689">
     * 8208bfa9-0e7f-5c26-983c-da320ef36689</a>}.
     */
    public static final EntityProxy.Concept CLINICAL_STATEMENT_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("Clinical statement issue assemblage (SOLOR)", UUID.fromString("8208bfa9-0e7f-5c26-983c-da320ef36689"));
    /**
     * Java binding for the concept described as <strong><em>Clinical statement properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9f9ebad2-ca84-5918-b667-0bb8c3ff58ad">
     * 9f9ebad2-ca84-5918-b667-0bb8c3ff58ad</a>}.
     */
    public static final EntityProxy.Concept CLINICAL_STATEMENT_PROPERTIES =
            EntityProxy.Concept.make("Clinical statement properties (SOLOR)", UUID.fromString("9f9ebad2-ca84-5918-b667-0bb8c3ff58ad"));
    /**
     * Java binding for the concept described as <strong><em>Clinvar Definition Assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/586dddc4-6660-512d-b44d-4903e9f89340">
     * 586dddc4-6660-512d-b44d-4903e9f89340</a>}.
     */
    public static final EntityProxy.Concept CLINVAR_DEFINITION_ASSEMBLAGE =
            EntityProxy.Concept.make("Clinvar Definition Assemblage (SOLOR)", UUID.fromString("586dddc4-6660-512d-b44d-4903e9f89340"));
    /**
     * Java binding for the concept described as <strong><em>Clinvar Description ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a5a68bfc-7d52-5408-a503-4e3d09a0235b">
     * a5a68bfc-7d52-5408-a503-4e3d09a0235b</a>}.
     */
    public static final EntityProxy.Concept CLINVAR_DESCRIPTION_ID =
            EntityProxy.Concept.make("Clinvar Description ID (SOLOR)", UUID.fromString("a5a68bfc-7d52-5408-a503-4e3d09a0235b"));
    /**
     * Java binding for the concept described as <strong><em>Clinvar Gene to Phenotype Non-Defining Taxonomy (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8e1cc058-190a-5bd0-8a6f-dbf2d7a4cd24">
     * 8e1cc058-190a-5bd0-8a6f-dbf2d7a4cd24</a>}.
     */
    public static final EntityProxy.Concept CLINVAR_GENE_TO_PHENOTYPE_NON_DEFINING_TAXONOMY =
            EntityProxy.Concept.make("Clinvar Gene to Phenotype Non-Defining Taxonomy (SOLOR)", UUID.fromString("8e1cc058-190a-5bd0-8a6f-dbf2d7a4cd24"));
    /**
     * Java binding for the concept described as <strong><em>Clinvar Variant ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8c36992e-3b8c-5a82-989a-85e15ca119aa">
     * 8c36992e-3b8c-5a82-989a-85e15ca119aa</a>}.
     */
    public static final EntityProxy.Concept CLINVAR_VARIANT_ID =
            EntityProxy.Concept.make("Clinvar Variant ID (SOLOR)", UUID.fromString("8c36992e-3b8c-5a82-989a-85e15ca119aa"));
    /**
     * Java binding for the concept described as <strong><em>Clinvar Variant to Gene Non-Defining Taxonomy (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/92501201-abe5-50b0-b606-ff66e30bbed4">
     * 92501201-abe5-50b0-b606-ff66e30bbed4</a>}.
     */
    public static final EntityProxy.Concept CLINVAR_VARIANT_TO_GENE_NON_DEFINING_TAXONOMY =
            EntityProxy.Concept.make("Clinvar Variant to Gene Non-Defining Taxonomy (SOLOR)", UUID.fromString("92501201-abe5-50b0-b606-ff66e30bbed4"));
    /**
     * Java binding for the concept described as <strong><em>Code (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2d01956c-912b-59aa-913f-ee91264d0d1c">
     * 2d01956c-912b-59aa-913f-ee91264d0d1c</a>}.
     */
    public static final EntityProxy.Concept CODE =
            EntityProxy.Concept.make("Code (SOLOR)", UUID.fromString("2d01956c-912b-59aa-913f-ee91264d0d1c"));
    /**
     * Java binding for the concept described as <strong><em>Column default value (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4d3e79aa-ab74-5858-beb3-15e0888986cb">
     * 4d3e79aa-ab74-5858-beb3-15e0888986cb</a>}.
     */
    public static final EntityProxy.Concept COLUMN_DEFAULT_VALUE =
            EntityProxy.Concept.make("Column default value (SOLOR)", UUID.fromString("4d3e79aa-ab74-5858-beb3-15e0888986cb"));
    /**
     * Java binding for the concept described as <strong><em>Column name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/89c0ded2-fd69-5654-a386-ded850d258a1">
     * 89c0ded2-fd69-5654-a386-ded850d258a1</a>}.
     */
    public static final EntityProxy.Concept COLUMN_NAME =
            EntityProxy.Concept.make("Column name (SOLOR)", UUID.fromString("89c0ded2-fd69-5654-a386-ded850d258a1"));
    /**
     * Java binding for the concept described as <strong><em>Column order (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8c501747-846a-5cea-8fd6-c9dd3dfc674f">
     * 8c501747-846a-5cea-8fd6-c9dd3dfc674f</a>}.
     */
    public static final EntityProxy.Concept COLUMN_ORDER =
            EntityProxy.Concept.make("Column order (SOLOR)", UUID.fromString("8c501747-846a-5cea-8fd6-c9dd3dfc674f"));
    /**
     * Java binding for the concept described as <strong><em>Column required (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8a89ef19-bd5a-5e25-aa57-1172fbb437b6">
     * 8a89ef19-bd5a-5e25-aa57-1172fbb437b6</a>}.
     */
    public static final EntityProxy.Concept COLUMN_REQUIRED =
            EntityProxy.Concept.make("Column required (SOLOR)", UUID.fromString("8a89ef19-bd5a-5e25-aa57-1172fbb437b6"));
    /**
     * Java binding for the concept described as <strong><em>Column type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dbfd9bd2-b84f-574a-ab9e-64ba3bb94793">
     * dbfd9bd2-b84f-574a-ab9e-64ba3bb94793</a>}.
     */
    public static final EntityProxy.Concept COLUMN_TYPE =
            EntityProxy.Concept.make("Column type (SOLOR)", UUID.fromString("dbfd9bd2-b84f-574a-ab9e-64ba3bb94793"));
    /**
     * Java binding for the concept described as <strong><em>Column validator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f295c3ba-d416-563d-8427-8b5d3e324192">
     * f295c3ba-d416-563d-8427-8b5d3e324192</a>}.
     */
    public static final EntityProxy.Concept COLUMN_VALIDATOR =
            EntityProxy.Concept.make("Column validator (SOLOR)", UUID.fromString("f295c3ba-d416-563d-8427-8b5d3e324192"));
    /**
     * Java binding for the concept described as <strong><em>Column validator data (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/50ea8378-8355-5a5d-bae2-ce7c10e92636">
     * 50ea8378-8355-5a5d-bae2-ce7c10e92636</a>}.
     */
    public static final EntityProxy.Concept COLUMN_VALIDATOR_DATA =
            EntityProxy.Concept.make("Column validator data (SOLOR)", UUID.fromString("50ea8378-8355-5a5d-bae2-ce7c10e92636"));
    /**
     * Java binding for the concept described as <strong><em>Comment (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/147832d4-b9b8-5062-8891-19f9c4e4760a">
     * 147832d4-b9b8-5062-8891-19f9c4e4760a</a>}.
     */
    public static final EntityProxy.Concept COMMENT =
            EntityProxy.Concept.make("Comment (SOLOR)", UUID.fromString("147832d4-b9b8-5062-8891-19f9c4e4760a"));
    /**
     * Java binding for the concept described as <strong><em>Committed state for chronicle (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cc69a07e-3749-5f03-99ca-fdad00a15209">
     * cc69a07e-3749-5f03-99ca-fdad00a15209</a>}.
     */
    public static final EntityProxy.Concept COMMITTED_STATE_FOR_CHRONICLE =
            EntityProxy.Concept.make("Committed state for chronicle (SOLOR)", UUID.fromString("cc69a07e-3749-5f03-99ca-fdad00a15209"));
    /**
     * Java binding for the concept described as <strong><em>Committed state for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cc1e1cd7-0627-513d-aba6-f2a6a1326906">
     * cc1e1cd7-0627-513d-aba6-f2a6a1326906</a>}.
     */
    public static final EntityProxy.Concept COMMITTED_STATE_FOR_VERSION =
            EntityProxy.Concept.make("Committed state for version (SOLOR)", UUID.fromString("cc1e1cd7-0627-513d-aba6-f2a6a1326906"));
    /**
     * Java binding for the concept described as <strong><em>Communicate a concept with speech or writing (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/29295e0b-36c8-5b10-a3de-81ca9a978c00">
     * 29295e0b-36c8-5b10-a3de-81ca9a978c00</a>}.
     */
    public static final EntityProxy.Concept COMMUNICATE_A_CONCEPT_WITH_SPEECH_OR_WRITING =
            EntityProxy.Concept.make("Communicate a concept with speech or writing (SOLOR)", UUID.fromString("29295e0b-36c8-5b10-a3de-81ca9a978c00"));
    /**
     * Java binding for the concept described as <strong><em>Completion panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fd757dca-0403-51b5-8fe3-e9e2bcf1a53a">
     * fd757dca-0403-51b5-8fe3-e9e2bcf1a53a</a>}.
     */
    public static final EntityProxy.Concept COMPLETION_PANEL =
            EntityProxy.Concept.make("Completion panel (SOLOR)", UUID.fromString("fd757dca-0403-51b5-8fe3-e9e2bcf1a53a"));
    /**
     * Java binding for the concept described as <strong><em>Component Id list field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e553d3f1-63e1-4292-a3a9-af646fe44292">
     * e553d3f1-63e1-4292-a3a9-af646fe44292</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_LIST_FIELD =
            EntityProxy.Concept.make("Component Id list field (SOLOR)", UUID.fromString("e553d3f1-63e1-4292-a3a9-af646fe44292"));
    /**
     * Java binding for the concept described as <strong><em>Component Id set field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e283af51-2e8f-44fa-9bf1-89a99a7c7631">
     * e283af51-2e8f-44fa-9bf1-89a99a7c7631</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_SET_FIELD =
            EntityProxy.Concept.make("Component Id set field (SOLOR)", UUID.fromString("e283af51-2e8f-44fa-9bf1-89a99a7c7631"));
    /**
     * Java binding for the concept described as <strong><em>Component field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3">
     * fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_FIELD =
            EntityProxy.Concept.make("Component field (SOLOR)", UUID.fromString("fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3"));
    /**
     * Java binding for the concept described as <strong><em>Component for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0bc32c16-698e-5719-8bd5-efa099c7d782">
     * 0bc32c16-698e-5719-8bd5-efa099c7d782</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_FOR_SEMANTIC =
            EntityProxy.Concept.make("Component for semantic (SOLOR)", UUID.fromString("0bc32c16-698e-5719-8bd5-efa099c7d782"));
    /**
     * Java binding for the concept described as <strong><em>Component id 1 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1de8e38d-9dd8-55bc-a01f-c6e0f71e4f6c">
     * 1de8e38d-9dd8-55bc-a01f-c6e0f71e4f6c</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_1 =
            EntityProxy.Concept.make("Component id 1 (SOLOR)", UUID.fromString("1de8e38d-9dd8-55bc-a01f-c6e0f71e4f6c"));
    /**
     * Java binding for the concept described as <strong><em>Component id 2 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8d8fd8f4-6134-50de-a864-1535ace8a778">
     * 8d8fd8f4-6134-50de-a864-1535ace8a778</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_2 =
            EntityProxy.Concept.make("Component id 2 (SOLOR)", UUID.fromString("8d8fd8f4-6134-50de-a864-1535ace8a778"));
    /**
     * Java binding for the concept described as <strong><em>Component id 3 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2228163f-4ed3-5f4b-85a4-898351e53cf9">
     * 2228163f-4ed3-5f4b-85a4-898351e53cf9</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_3 =
            EntityProxy.Concept.make("Component id 3 (SOLOR)", UUID.fromString("2228163f-4ed3-5f4b-85a4-898351e53cf9"));
    /**
     * Java binding for the concept described as <strong><em>Component id 4 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8fe0c246-7c3e-55a0-a4d6-4ecfc5f9250c">
     * 8fe0c246-7c3e-55a0-a4d6-4ecfc5f9250c</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_4 =
            EntityProxy.Concept.make("Component id 4 (SOLOR)", UUID.fromString("8fe0c246-7c3e-55a0-a4d6-4ecfc5f9250c"));
    /**
     * Java binding for the concept described as <strong><em>Component id 5 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7da7bb2b-c967-5fb6-8826-108aaf927ce7">
     * 7da7bb2b-c967-5fb6-8826-108aaf927ce7</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_5 =
            EntityProxy.Concept.make("Component id 5 (SOLOR)", UUID.fromString("7da7bb2b-c967-5fb6-8826-108aaf927ce7"));
    /**
     * Java binding for the concept described as <strong><em>Component id 6 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e4ddfd97-2be2-5c1e-a8e8-b35ae5fc2692">
     * e4ddfd97-2be2-5c1e-a8e8-b35ae5fc2692</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_6 =
            EntityProxy.Concept.make("Component id 6 (SOLOR)", UUID.fromString("e4ddfd97-2be2-5c1e-a8e8-b35ae5fc2692"));
    /**
     * Java binding for the concept described as <strong><em>Component id 7 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/94e8a416-75bd-52ea-9c01-30e07e4e8ea5">
     * 94e8a416-75bd-52ea-9c01-30e07e4e8ea5</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_ID_7 =
            EntityProxy.Concept.make("Component id 7 (SOLOR)", UUID.fromString("94e8a416-75bd-52ea-9c01-30e07e4e8ea5"));
    /**
     * Java binding for the concept described as <strong><em>Component is NOT member of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/117cf5cd-80aa-58b0-b216-fd60c953af22">
     * 117cf5cd-80aa-58b0-b216-fd60c953af22</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_IS_NOT_MEMBER_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Component is NOT member of (query clause)", UUID.fromString("117cf5cd-80aa-58b0-b216-fd60c953af22"));
    /**
     * Java binding for the concept described as <strong><em>Component is active (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/45df0b38-67ec-11e7-907b-a6006ad3dba0">
     * 45df0b38-67ec-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_IS_ACTIVE____QUERY_CLAUSE =
            EntityProxy.Concept.make("Component is active (query clause)", UUID.fromString("45df0b38-67ec-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Component is inactive (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/50e719a8-67ec-11e7-907b-a6006ad3dba0">
     * 50e719a8-67ec-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_IS_INACTIVE____QUERY_CLAUSE =
            EntityProxy.Concept.make("Component is inactive (query clause)", UUID.fromString("50e719a8-67ec-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Component is member of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/380e0514-906b-5675-9ac4-6e788b1269cd">
     * 380e0514-906b-5675-9ac4-6e788b1269cd</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_IS_MEMBER_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Component is member of (query clause)", UUID.fromString("380e0514-906b-5675-9ac4-6e788b1269cd"));
    /**
     * Java binding for the concept described as <strong><em>Component list panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f62a6b12-3bd0-532a-804f-aae670a90d09">
     * f62a6b12-3bd0-532a-804f-aae670a90d09</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_LIST_PANEL =
            EntityProxy.Concept.make("Component list panel (SOLOR)", UUID.fromString("f62a6b12-3bd0-532a-804f-aae670a90d09"));
    /**
     * Java binding for the concept described as <strong><em>Component semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/127e7274-0b88-5519-a9db-85d4b9ce6a4a">
     * 127e7274-0b88-5519-a9db-85d4b9ce6a4a</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_SEMANTIC =
            EntityProxy.Concept.make("Component semantic (SOLOR)", UUID.fromString("127e7274-0b88-5519-a9db-85d4b9ce6a4a"));
    /**
     * Java binding for the concept described as <strong><em>Component type focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f1f179d0-26af-5123-9b29-9fc6cd01dd29">
     * f1f179d0-26af-5123-9b29-9fc6cd01dd29</a>}.
     */
    public static final EntityProxy.Concept COMPONENT_TYPE_FOCUS =
            EntityProxy.Concept.make("Component type focus (SOLOR)", UUID.fromString("f1f179d0-26af-5123-9b29-9fc6cd01dd29"));
    /**
     * Java binding for the concept described as <strong><em>Composite action panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0c1c3ef7-f17d-5636-aa1d-2b91f4b0c52e">
     * 0c1c3ef7-f17d-5636-aa1d-2b91f4b0c52e</a>}.
     */
    public static final EntityProxy.Concept COMPOSITE_ACTION_PANEL =
            EntityProxy.Concept.make("Composite action panel (SOLOR)", UUID.fromString("0c1c3ef7-f17d-5636-aa1d-2b91f4b0c52e"));
    /**
     * Java binding for the concept described as <strong><em>ConDOR classifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d86ed948-8135-5d08-8bd6-7379e9213c8c">
     * d86ed948-8135-5d08-8bd6-7379e9213c8c</a>}.
     */
    public static final EntityProxy.Concept CONDOR_CLASSIFIER =
            EntityProxy.Concept.make("ConDOR classifier (SOLOR)", UUID.fromString("d86ed948-8135-5d08-8bd6-7379e9213c8c"));
    /**
     * Java binding for the concept described as <strong><em>Concept assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e8060eec-b9b9-11e7-abc4-cec278b6b50a">
     * e8060eec-b9b9-11e7-abc4-cec278b6b50a</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_ASSEMBLAGE =
            EntityProxy.Concept.make("Concept assemblage (SOLOR)", UUID.fromString("e8060eec-b9b9-11e7-abc4-cec278b6b50a"));
    /**
     * Java binding for the concept described as <strong><em>Concept assemblage for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/16486419-5d1c-574f-bde6-21910ad66f44">
     * 16486419-5d1c-574f-bde6-21910ad66f44</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Concept assemblage for logic coordinate (SOLOR)", UUID.fromString("16486419-5d1c-574f-bde6-21910ad66f44"));

    /**
     * Java binding for the concept described as <strong><em>Concept builder panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/561d55e0-1661-5480-9a0a-bd450b350c84">
     * 561d55e0-1661-5480-9a0a-bd450b350c84</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_BUILDER_PANEL =
            EntityProxy.Concept.make("Concept builder panel (SOLOR)", UUID.fromString("561d55e0-1661-5480-9a0a-bd450b350c84"));
    /**
     * Java binding for the concept described as <strong><em>Concept constraints (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/081273cd-92dd-593c-9d9b-63d33838e70b">
     * 081273cd-92dd-593c-9d9b-63d33838e70b</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_CONSTRAINTS =
            EntityProxy.Concept.make("Concept constraints (SOLOR)", UUID.fromString("081273cd-92dd-593c-9d9b-63d33838e70b"));
    /**
     * Java binding for the concept described as <strong><em>Concept details classification-results-linked panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eca3493a-5a90-5025-8d8b-42df5ce11401">
     * eca3493a-5a90-5025-8d8b-42df5ce11401</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_CLASSIFICATION_RESULTS_LINKED_PANEL =
            EntityProxy.Concept.make("Concept details classification-results-linked panel (SOLOR)", UUID.fromString("eca3493a-5a90-5025-8d8b-42df5ce11401"));
    /**
     * Java binding for the concept described as <strong><em>Concept details list-view-linked panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/444d5686-edda-53d1-8b37-1ec03013c96f">
     * 444d5686-edda-53d1-8b37-1ec03013c96f</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_LIST_VIEW_LINKED_PANEL =
            EntityProxy.Concept.make("Concept details list-view-linked panel (SOLOR)", UUID.fromString("444d5686-edda-53d1-8b37-1ec03013c96f"));
    /**
     * Java binding for the concept described as <strong><em>Concept details new-concept-linked panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5b99a62b-6208-5491-8606-c65f0861e075">
     * 5b99a62b-6208-5491-8606-c65f0861e075</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_NEW_CONCEPT_LINKED_PANEL =
            EntityProxy.Concept.make("Concept details new-concept-linked panel (SOLOR)", UUID.fromString("5b99a62b-6208-5491-8606-c65f0861e075"));
    /**
     * Java binding for the concept described as <strong><em>Concept details panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9c71cce9-9080-56c1-ad17-bf2896dcbe85">
     * 9c71cce9-9080-56c1-ad17-bf2896dcbe85</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_PANEL =
            EntityProxy.Concept.make("Concept details panel (SOLOR)", UUID.fromString("9c71cce9-9080-56c1-ad17-bf2896dcbe85"));
    /**
     * Java binding for the concept described as <strong><em>Concept details search-linked panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/75c01bcd-277e-5ecd-9dd8-fd01c5695481">
     * 75c01bcd-277e-5ecd-9dd8-fd01c5695481</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_SEARCH_LINKED_PANEL =
            EntityProxy.Concept.make("Concept details search-linked panel (SOLOR)", UUID.fromString("75c01bcd-277e-5ecd-9dd8-fd01c5695481"));
    /**
     * Java binding for the concept described as <strong><em>Concept details taxonomy-linked panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fe45a2c4-c776-5586-a08d-a2cfe4105166">
     * fe45a2c4-c776-5586-a08d-a2cfe4105166</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_TAXONOMY_LINKED_PANEL =
            EntityProxy.Concept.make("Concept details taxonomy-linked panel (SOLOR)", UUID.fromString("fe45a2c4-c776-5586-a08d-a2cfe4105166"));
    /**
     * Java binding for the concept described as <strong><em>Concept details tree table (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1655edd8-7b73-52c5-98b0-263d1ab3a90b">
     * 1655edd8-7b73-52c5-98b0-263d1ab3a90b</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_DETAILS_TREE_TABLE =
            EntityProxy.Concept.make("Concept details tree table (SOLOR)", UUID.fromString("1655edd8-7b73-52c5-98b0-263d1ab3a90b"));
    /**
     * Java binding for the concept described as <strong><em>Concept field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557">
     * ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_FIELD =
            EntityProxy.Concept.make("Concept field (SOLOR)", UUID.fromString("ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557"));
    /**
     * Java binding for the concept described as <strong><em>Concept focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dca9854d-9e4c-5e8a-8b30-6c1af6901bb8">
     * dca9854d-9e4c-5e8a-8b30-6c1af6901bb8</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_FOCUS =
            EntityProxy.Concept.make("Concept focus (SOLOR)", UUID.fromString("dca9854d-9e4c-5e8a-8b30-6c1af6901bb8"));
    /**
     * Java binding for the concept described as <strong><em>Concept for component (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20070-579e-11e7-907b-a6006ad3dba0">
     * d9c20070-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_FOR_COMPONENT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Concept for component (query clause)", UUID.fromString("d9c20070-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Concept has taxonomy distance from (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9533dce4-efde-51a3-94f8-a4fb06b9d08c">
     * 9533dce4-efde-51a3-94f8-a4fb06b9d08c</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_HAS_TAXONOMY_DISTANCE_FROM =
            EntityProxy.Concept.make("Concept has taxonomy distance from (SOLOR)", UUID.fromString("9533dce4-efde-51a3-94f8-a4fb06b9d08c"));
    /**
     * Java binding for the concept described as <strong><em>Concept image (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d2806aeb-31a2-58a0-b13c-4bfaedbb0a7a">
     * d2806aeb-31a2-58a0-b13c-4bfaedbb0a7a</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IMAGE =
            EntityProxy.Concept.make("Concept image (SOLOR)", UUID.fromString("d2806aeb-31a2-58a0-b13c-4bfaedbb0a7a"));
    /**
     * Java binding for the concept described as <strong><em>Concept is (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f602-579e-11e7-907b-a6006ad3dba0">
     * d9c1f602-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IS____QUERY_CLAUSE =
            EntityProxy.Concept.make("Concept is (query clause)", UUID.fromString("d9c1f602-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Concept is assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/73d35147-52ef-56aa-ac88-9571064b3831">
     * 73d35147-52ef-56aa-ac88-9571064b3831</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IS_ASSEMBLAGE =
            EntityProxy.Concept.make("Concept is assemblage (SOLOR)", UUID.fromString("73d35147-52ef-56aa-ac88-9571064b3831"));
    /**
     * Java binding for the concept described as <strong><em>Concept is child of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1fddc-579e-11e7-907b-a6006ad3dba0">
     * d9c1fddc-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IS_CHILD_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Concept is child of (query clause)", UUID.fromString("d9c1fddc-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Concept is descendent of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20142-579e-11e7-907b-a6006ad3dba0">
     * d9c20142-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IS_DESCENDENT_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Concept is descendent of (query clause)", UUID.fromString("d9c20142-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Concept is kind of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f6d4-579e-11e7-907b-a6006ad3dba0">
     * d9c1f6d4-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_IS_KIND_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Concept is kind of (query clause)", UUID.fromString("d9c1f6d4-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Concept properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f96000ff-ea8b-5ee9-9cd6-66438c27e73d">
     * f96000ff-ea8b-5ee9-9cd6-66438c27e73d</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_PROPERTIES =
            EntityProxy.Concept.make("Concept properties (SOLOR)", UUID.fromString("f96000ff-ea8b-5ee9-9cd6-66438c27e73d"));
    /**
     * Java binding for the concept described as <strong><em>Concept reference (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e89148c7-4fe2-52f8-abb9-6a53605d20cb">
     * e89148c7-4fe2-52f8-abb9-6a53605d20cb</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_REFERENCE =
            EntityProxy.Concept.make("Concept reference (SOLOR)", UUID.fromString("e89148c7-4fe2-52f8-abb9-6a53605d20cb"));
    /**
     * Java binding for the concept described as <strong><em>Concept semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fbf054fb-ceaf-5ab8-b946-bbcc4835ce07">
     * fbf054fb-ceaf-5ab8-b946-bbcc4835ce07</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_SEMANTIC =
            EntityProxy.Concept.make("Concept semantic (SOLOR)", UUID.fromString("fbf054fb-ceaf-5ab8-b946-bbcc4835ce07"));
    /**
     * Java binding for the concept described as <strong><em>Concept substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/23483990-b738-5f43-bc03-befd43928a37">
     * 23483990-b738-5f43-bc03-befd43928a37</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_SUBSTITUTION =
            EntityProxy.Concept.make("Concept substitution (SOLOR)", UUID.fromString("23483990-b738-5f43-bc03-befd43928a37"));
    /**
     * Java binding for the concept described as <strong><em>Concept to find (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/91687b29-3bbb-540b-9de6-91246c75afd0">
     * 91687b29-3bbb-540b-9de6-91246c75afd0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_TO_FIND =
            EntityProxy.Concept.make("Concept to find (SOLOR)", UUID.fromString("91687b29-3bbb-540b-9de6-91246c75afd0"));
    /**
     * Java binding for the concept described as <strong><em>Concept type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/106f3ba1-63b8-5596-8dbe-524fa2e89fc0">
     * 106f3ba1-63b8-5596-8dbe-524fa2e89fc0</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_TYPE =
            EntityProxy.Concept.make("Concept type (SOLOR)", UUID.fromString("106f3ba1-63b8-5596-8dbe-524fa2e89fc0"));
    /**
     * Java binding for the concept described as <strong><em>Concept version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c202f992-3f4b-5f30-9b32-e376f68367d1">
     * c202f992-3f4b-5f30-9b32-e376f68367d1</a>}.
     */
    public static final EntityProxy.Concept CONCEPT_VERSION =
            EntityProxy.Concept.make("Concept version (SOLOR)", UUID.fromString("c202f992-3f4b-5f30-9b32-e376f68367d1"));
    /**
     * Java binding for the concept described as <strong><em>Concrete domain operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/843b0b55-8785-5544-93f6-581da9cf1ff3">
     * 843b0b55-8785-5544-93f6-581da9cf1ff3</a>}.
     */
    public static final EntityProxy.Concept CONCRETE_DOMAIN_OPERATOR =
            EntityProxy.Concept.make("Concrete domain operator (SOLOR)", UUID.fromString("843b0b55-8785-5544-93f6-581da9cf1ff3"));
    /**
     * Java binding for the concept described as <strong><em>Conditional triggers (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a3e4ac54-db82-5345-8713-7e0da98bbb0a">
     * a3e4ac54-db82-5345-8713-7e0da98bbb0a</a>}.
     */
    public static final EntityProxy.Concept CONDITIONAL_TRIGGERS =
            EntityProxy.Concept.make("Conditional triggers (SOLOR)", UUID.fromString("a3e4ac54-db82-5345-8713-7e0da98bbb0a"));
    /**
     * Java binding for the concept described as <strong><em>Configuration name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2debbffc-e145-565b-acb4-94011e06dfb9">
     * 2debbffc-e145-565b-acb4-94011e06dfb9</a>}.
     */
    public static final EntityProxy.Concept CONFIGURATION_NAME =
            EntityProxy.Concept.make("Configuration name (SOLOR)", UUID.fromString("2debbffc-e145-565b-acb4-94011e06dfb9"));
    /**
     * Java binding for the concept described as <strong><em>Configuration properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a64c5cb4-831a-5277-8cf7-281ee6583d4a">
     * a64c5cb4-831a-5277-8cf7-281ee6583d4a</a>}.
     */
    public static final EntityProxy.Concept CONFIGURATION_PROPERTIES =
            EntityProxy.Concept.make("Configuration properties (SOLOR)", UUID.fromString("a64c5cb4-831a-5277-8cf7-281ee6583d4a"));
    /**
     * Java binding for the concept described as <strong><em>Connective operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3fdcaadc-d972-58e9-84f1-b3a39903b076">
     * 3fdcaadc-d972-58e9-84f1-b3a39903b076</a>}.
     */
    public static final EntityProxy.Concept CONNECTIVE_OPERATOR =
            EntityProxy.Concept.make("Connective operator (SOLOR)", UUID.fromString("3fdcaadc-d972-58e9-84f1-b3a39903b076"));
    /**
     * Java binding for the concept described as <strong><em>Content Metadata (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/586a8622-2573-58c4-8b3c-cf1ffac056a3">
     * 586a8622-2573-58c4-8b3c-cf1ffac056a3</a>}.
     */
    public static final EntityProxy.Concept CONTENT_METADATA =
            EntityProxy.Concept.make("Content Metadata (SOLOR)", UUID.fromString("586a8622-2573-58c4-8b3c-cf1ffac056a3"));
    /**
     * Java binding for the concept described as <strong><em>Content issue (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ff6ded23-7e5b-5d2c-9967-75b08486ba97">
     * ff6ded23-7e5b-5d2c-9967-75b08486ba97</a>}.
     */
    public static final EntityProxy.Concept CONTENT_ISSUE =
            EntityProxy.Concept.make("Content issue (SOLOR)", UUID.fromString("ff6ded23-7e5b-5d2c-9967-75b08486ba97"));
    /**
     * Java binding for the concept described as <strong><em>Content issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2b8a32c3-9e14-586d-aeb2-fcdcaf1f3a4a">
     * 2b8a32c3-9e14-586d-aeb2-fcdcaf1f3a4a</a>}.
     */
    public static final EntityProxy.Concept CONTENT_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("Content issue assemblage (SOLOR)", UUID.fromString("2b8a32c3-9e14-586d-aeb2-fcdcaf1f3a4a"));
    /**
     * Java binding for the concept described as <strong><em>Content license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b3305461-6954-574e-9124-285a02d4ecae">
     * b3305461-6954-574e-9124-285a02d4ecae</a>}.
     */
    public static final EntityProxy.Concept CONTENT_LICENSE =
            EntityProxy.Concept.make("Content license (SOLOR)", UUID.fromString("b3305461-6954-574e-9124-285a02d4ecae"));
    /**
     * Java binding for the concept described as <strong><em>Converted IBDF Artifact Classifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cb07b84b-dd45-553c-b5c9-af79899c9978">
     * cb07b84b-dd45-553c-b5c9-af79899c9978</a>}.
     */
    public static final EntityProxy.Concept CONVERTED_IBDF_ARTIFACT_CLASSIFIER =
            EntityProxy.Concept.make("Converted IBDF Artifact Classifier (SOLOR)", UUID.fromString("cb07b84b-dd45-553c-b5c9-af79899c9978"));
    /**
     * Java binding for the concept described as <strong><em>Converted IBDF Artifact Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d2671aca-2db4-5c43-bc92-ce728ff2340b">
     * d2671aca-2db4-5c43-bc92-ce728ff2340b</a>}.
     */
    public static final EntityProxy.Concept CONVERTED_IBDF_ARTIFACT_VERSION =
            EntityProxy.Concept.make("Converted IBDF Artifact Version (SOLOR)", UUID.fromString("d2671aca-2db4-5c43-bc92-ce728ff2340b"));
    /**
     * Java binding for the concept described as <strong><em>Converter Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c9e9adad-d5bf-55d7-9573-8e3c548019de">
     * c9e9adad-d5bf-55d7-9573-8e3c548019de</a>}.
     */
    public static final EntityProxy.Concept CONVERTER_VERSION =
            EntityProxy.Concept.make("Converter Version (SOLOR)", UUID.fromString("c9e9adad-d5bf-55d7-9573-8e3c548019de"));
    /**
     * Java binding for the concept described as <strong><em>Copyright (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/57b405d5-20b5-5aa3-923c-ead3af1e692e">
     * 57b405d5-20b5-5aa3-923c-ead3af1e692e</a>}.
     */
    public static final EntityProxy.Concept COPYRIGHT =
            EntityProxy.Concept.make("Copyright (SOLOR)", UUID.fromString("57b405d5-20b5-5aa3-923c-ead3af1e692e"));
    /**
     * Java binding for the concept described as <strong><em>Copyright free work (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4d268bfc-026d-53a4-b7d0-cbe3ee109337">
     * 4d268bfc-026d-53a4-b7d0-cbe3ee109337</a>}.
     */
    public static final EntityProxy.Concept COPYRIGHT_FREE_WORK =
            EntityProxy.Concept.make("Copyright free work (SOLOR)", UUID.fromString("4d268bfc-026d-53a4-b7d0-cbe3ee109337"));
    /**
     * Java binding for the concept described as <strong><em>Corelation comparison expression (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6d23690-54e9-5475-92b5-ac02f91ba9d8">
     * d6d23690-54e9-5475-92b5-ac02f91ba9d8</a>}.
     */
    public static final EntityProxy.Concept CORELATION_COMPARISON_EXPRESSION =
            EntityProxy.Concept.make("Corelation comparison expression (SOLOR)", UUID.fromString("d6d23690-54e9-5475-92b5-ac02f91ba9d8"));
    /**
     * Java binding for the concept described as <strong><em>Corelation expression (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1711815f-99a1-5d1a-8f1e-75dc7bf41928">
     * 1711815f-99a1-5d1a-8f1e-75dc7bf41928</a>}.
     */
    public static final EntityProxy.Concept CORELATION_EXPRESSION =
            EntityProxy.Concept.make("Corelation expression (SOLOR)", UUID.fromString("1711815f-99a1-5d1a-8f1e-75dc7bf41928"));
    /**
     * Java binding for the concept described as <strong><em>Corelation reference expression (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/acb73d95-7c96-590c-9f24-23da54f95ff2">
     * acb73d95-7c96-590c-9f24-23da54f95ff2</a>}.
     */
    public static final EntityProxy.Concept CORELATION_REFERENCE_EXPRESSION =
            EntityProxy.Concept.make("Corelation reference expression (SOLOR)", UUID.fromString("acb73d95-7c96-590c-9f24-23da54f95ff2"));
    /**
     * Java binding for the concept described as <strong><em>Correlation properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8f682e00-3d9e-5506-bd19-b2169d6c8752">
     * 8f682e00-3d9e-5506-bd19-b2169d6c8752</a>}.
     */
    public static final EntityProxy.Concept CORRELATION_PROPERTIES =
            EntityProxy.Concept.make("Correlation properties (SOLOR)", UUID.fromString("8f682e00-3d9e-5506-bd19-b2169d6c8752"));
    /**
     * Java binding for the concept described as <strong><em>Count of base different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a95d75e3-bf61-5dbb-927a-86a074919f0e">
     * a95d75e3-bf61-5dbb-927a-86a074919f0e</a>}.
     */
    public static final EntityProxy.Concept COUNT_OF_BASE_DIFFERENT =
            EntityProxy.Concept.make("Count of base different (SOLOR)", UUID.fromString("a95d75e3-bf61-5dbb-927a-86a074919f0e"));
    /**
     * Java binding for the concept described as <strong><em>Creative Commons BY license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3415a972-7850-57cd-aa86-a572ca1c2ceb">
     * 3415a972-7850-57cd-aa86-a572ca1c2ceb</a>}.
     */
    public static final EntityProxy.Concept CREATIVE_COMMONS_BY_LICENSE =
            EntityProxy.Concept.make("Creative Commons BY license (SOLOR)", UUID.fromString("3415a972-7850-57cd-aa86-a572ca1c2ceb"));
    /**
     * Java binding for the concept described as <strong><em>Current activity (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/926f691f-3418-565c-900e-322ba797888e">
     * 926f691f-3418-565c-900e-322ba797888e</a>}.
     */
    public static final EntityProxy.Concept CURRENT_ACTIVITY =
            EntityProxy.Concept.make("Current activity (SOLOR)", UUID.fromString("926f691f-3418-565c-900e-322ba797888e"));
    /**
     * Java binding for the concept described as <strong><em>Czech dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6979e268-0b59-542f-bac0-313c5ddf6a2e">
     * 6979e268-0b59-542f-bac0-313c5ddf6a2e</a>}.
     */
    public static final EntityProxy.Concept CZECH_DIALECT =
            EntityProxy.Concept.make("Czech dialect (SOLOR)", UUID.fromString("6979e268-0b59-542f-bac0-313c5ddf6a2e"));
    /**
     * Java binding for the concept described as <strong><em>Czech language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/33aa2d26-0541-557c-b796-904cbf245101">
     * 33aa2d26-0541-557c-b796-904cbf245101</a>}.
     */
    public static final EntityProxy.Concept CZECH_LANGUAGE =
            EntityProxy.Concept.make("Czech language (SOLOR)", UUID.fromString("33aa2d26-0541-557c-b796-904cbf245101"));
    /**
     * Java binding for the concept described as <strong><em>Danish language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7e462e33-6d94-38ae-a044-492a857a6853">
     * 7e462e33-6d94-38ae-a044-492a857a6853</a>}.
     */
    public static final EntityProxy.Concept DANISH_LANGUAGE =
            EntityProxy.Concept.make("Danish language (SOLOR)", UUID.fromString("7e462e33-6d94-38ae-a044-492a857a6853"), UUID.fromString("45021f10-9567-11e5-8994-feff819cdc9f"), UUID.fromString("987681fb-f3ef-595d-90e2-067baf2bc71f"));
    /**
     * Java binding for the concept described as <strong><em>Database UUID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/49b882a1-05e4-52cf-96d8-5de024b24632">
     * 49b882a1-05e4-52cf-96d8-5de024b24632</a>}.
     */
    public static final EntityProxy.Concept DATABASE_UUID =
            EntityProxy.Concept.make("Database UUID (SOLOR)", UUID.fromString("49b882a1-05e4-52cf-96d8-5de024b24632"));
    /**
     * Java binding for the concept described as <strong><em>Datastore location (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/41d8441e-86d4-5adf-863e-ad199fd854c3">
     * 41d8441e-86d4-5adf-863e-ad199fd854c3</a>}.
     */
    public static final EntityProxy.Concept DATASTORE_LOCATION =
            EntityProxy.Concept.make("Datastore location (SOLOR)", UUID.fromString("41d8441e-86d4-5adf-863e-ad199fd854c3"));
    /**
     * Java binding for the concept described as <strong><em>Default module for edit coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e83d322c-e275-5392-a5db-1de5fe98acb5">
     * e83d322c-e275-5392-a5db-1de5fe98acb5</a>}.
     */
    public static final EntityProxy.Concept DEFAULT_MODULE_FOR_EDIT_COORDINATE =
            EntityProxy.Concept.make("Default module for edit coordinate (SOLOR)", UUID.fromString("e83d322c-e275-5392-a5db-1de5fe98acb5"));
    /**
     * Java binding for the concept described as <strong><em>Definition description type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/700546a3-09c7-3fc2-9eb9-53d318659a09">
     * 700546a3-09c7-3fc2-9eb9-53d318659a09</a>}.
     */
    public static final EntityProxy.Concept DEFINITION_DESCRIPTION_TYPE =
            EntityProxy.Concept.make("Definition description type (SOLOR)", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));
    /**
     * Java binding for the concept described as <strong><em>Definition root (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e7271c01-6ed4-5240-963f-34d1f24153b0">
     * e7271c01-6ed4-5240-963f-34d1f24153b0</a>}.
     */
    public static final EntityProxy.Concept DEFINITION_ROOT =
            EntityProxy.Concept.make("Definition root (SOLOR)", UUID.fromString("e7271c01-6ed4-5240-963f-34d1f24153b0"));

    /**
     * Java binding for the concept described as <strong><em>Description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/87118daf-d28c-55fb-8657-cd6bc8425600">
     * 87118daf-d28c-55fb-8657-cd6bc8425600</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION =
            EntityProxy.Concept.make("Description (SOLOR)", UUID.fromString("87118daf-d28c-55fb-8657-cd6bc8425600"));
    /**
     * Java binding for the concept described as <strong><em>Description Lucene match (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f7a6-579e-11e7-907b-a6006ad3dba0">
     * d9c1f7a6-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_LUCENE_MATCH____QUERY_CLAUSE =
            EntityProxy.Concept.make("Description Lucene match (query clause)", UUID.fromString("d9c1f7a6-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Description acceptability (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/96b61063-0d29-5aea-9652-3f5f328aadc3">
     * 96b61063-0d29-5aea-9652-3f5f328aadc3</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_ACCEPTABILITY =
            EntityProxy.Concept.make("Description acceptability (SOLOR)", UUID.fromString("96b61063-0d29-5aea-9652-3f5f328aadc3"));
    /**
     * Java binding for the concept described as <strong><em>Description assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c9b9a4ac-3a1c-516c-bbef-3a13e30df27d">
     * c9b9a4ac-3a1c-516c-bbef-3a13e30df27d</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_ASSEMBLAGE =
            EntityProxy.Concept.make("Description assemblage (SOLOR)", UUID.fromString("c9b9a4ac-3a1c-516c-bbef-3a13e30df27d"));

    /**
     * Java binding for the concept described as <strong><em>Description case sensitive (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0def37bc-7e1b-384b-a6a3-3e3ceee9c52e">
     * 0def37bc-7e1b-384b-a6a3-3e3ceee9c52e</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_CASE_SENSITIVE =
            EntityProxy.Concept.make("Description case sensitive (SOLOR)", UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));
    /**
     * Java binding for the concept described as <strong><em>Description case significance (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c3dde9ea-b144-5f49-845a-20cc7d305250">
     * c3dde9ea-b144-5f49-845a-20cc7d305250</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_CASE_SIGNIFICANCE =
            EntityProxy.Concept.make("Description case significance (SOLOR)", UUID.fromString("c3dde9ea-b144-5f49-845a-20cc7d305250"), UUID.fromString("f30b0312-2c85-3e65-8609-2d89f8437d34"));
    /**
     * Java binding for the concept described as <strong><em>Description core type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/351955ff-30f4-5806-a0a5-5dda79756377">
     * 351955ff-30f4-5806-a0a5-5dda79756377</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_CORE_TYPE =
            EntityProxy.Concept.make("Description core type (SOLOR)", UUID.fromString("351955ff-30f4-5806-a0a5-5dda79756377"));
    /**
     * Java binding for the concept described as <strong><em>Description dialect pair (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a27bbbf8-57b5-590c-8650-1247f6f963eb">
     * a27bbbf8-57b5-590c-8650-1247f6f963eb</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_DIALECT_PAIR =
            EntityProxy.Concept.make("Description dialect pair (SOLOR)", UUID.fromString("a27bbbf8-57b5-590c-8650-1247f6f963eb"));
    /**
     * Java binding for the concept described as <strong><em>Description focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6edf734d-7f57-5430-9164-6ee0824fd94b">
     * 6edf734d-7f57-5430-9164-6ee0824fd94b</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_FOCUS =
            EntityProxy.Concept.make("Description focus (SOLOR)", UUID.fromString("6edf734d-7f57-5430-9164-6ee0824fd94b"));
    /**
     * Java binding for the concept described as <strong><em>Description for dialect/description pair (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1137767a-ad8b-5bc5-9842-a1f9b09d1ecc">
     * 1137767a-ad8b-5bc5-9842-a1f9b09d1ecc</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_FOR_DIALECT_AND_OR_DESCRIPTION_PAIR =
            EntityProxy.Concept.make("Description for dialect/description pair (SOLOR)", UUID.fromString("1137767a-ad8b-5bc5-9842-a1f9b09d1ecc"));
    /**
     * Java binding for the concept described as <strong><em>Description initial character case sensitive (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/17915e0d-ed38-3488-a35c-cda966db306a">
     * 17915e0d-ed38-3488-a35c-cda966db306a</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE =
            EntityProxy.Concept.make("Description initial character case sensitive (SOLOR)", UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));
    /**
     * Java binding for the concept described as <strong><em>Description logic profile for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/aef80e34-b2dd-5dca-a989-3e0ee2699be3">
     * aef80e34-b2dd-5dca-a989-3e0ee2699be3</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_LOGIC_PROFILE_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Description logic profile for logic coordinate (SOLOR)", UUID.fromString("aef80e34-b2dd-5dca-a989-3e0ee2699be3"));
    /**
     * Java binding for the concept described as <strong><em>Description not case sensitive (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ecea41a2-f596-3d98-99d1-771b667e55b8">
     * ecea41a2-f596-3d98-99d1-771b667e55b8</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_NOT_CASE_SENSITIVE =
            EntityProxy.Concept.make("Description not case sensitive (SOLOR)", UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));
    /**
     * Java binding for the concept described as <strong><em>Description regex match (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1ff9e-579e-11e7-907b-a6006ad3dba0">
     * d9c1ff9e-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_REGEX_MATCH____QUERY_CLAUSE =
            EntityProxy.Concept.make("Description regex match (query clause)", UUID.fromString("d9c1ff9e-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Description semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/81487d5f-6115-51e2-a3b3-93d783888eb8">
     * 81487d5f-6115-51e2-a3b3-93d783888eb8</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_SEMANTIC =
            EntityProxy.Concept.make("Description semantic (SOLOR)", UUID.fromString("81487d5f-6115-51e2-a3b3-93d783888eb8"));
    /**
     * Java binding for the concept described as <strong><em>Description type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ad0c19e8-2ccc-59c1-8b7e-c56c03aca8eb">
     * ad0c19e8-2ccc-59c1-8b7e-c56c03aca8eb</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_TYPE =
            EntityProxy.Concept.make("Description type (SOLOR)", UUID.fromString("ad0c19e8-2ccc-59c1-8b7e-c56c03aca8eb"));
    /**
     * Java binding for the concept described as <strong><em>Description type for description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a00c5ad7-5b8a-5592-a28c-64057dd3caab">
     * a00c5ad7-5b8a-5592-a28c-64057dd3caab</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_TYPE_FOR_DESCRIPTION =
            EntityProxy.Concept.make("Description type for description (SOLOR)", UUID.fromString("a00c5ad7-5b8a-5592-a28c-64057dd3caab"));
    /**
     * Java binding for the concept described as <strong><em>Description type in source terminology (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ef7d9808-a839-5119-a604-b777268eb719">
     * ef7d9808-a839-5119-a604-b777268eb719</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY =
            EntityProxy.Concept.make("Description type in source terminology (SOLOR)", UUID.fromString("ef7d9808-a839-5119-a604-b777268eb719"));

    /**
     * Java binding for the concept described as <strong><em>Description type preference list for language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/44c7eab6-fdb8-5427-9d7a-52ab63f7a6f9">
     * 44c7eab6-fdb8-5427-9d7a-52ab63f7a6f9</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_TYPE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Description type preference list for language coordinate (SOLOR)", UUID.fromString("44c7eab6-fdb8-5427-9d7a-52ab63f7a6f9"));
    /**
     * Java binding for the concept described as <strong><em>Description version properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/732aad24-4add-59d6-bbc9-840a8b9dc754">
     * 732aad24-4add-59d6-bbc9-840a8b9dc754</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_VERSION_PROPERTIES =
            EntityProxy.Concept.make("Description version properties (SOLOR)", UUID.fromString("732aad24-4add-59d6-bbc9-840a8b9dc754"));
    /**
     * Java binding for the concept described as <strong><em>Description-logic classifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/511f6c23-6d09-5466-af2d-dd70cb0bd5e4">
     * 511f6c23-6d09-5466-af2d-dd70cb0bd5e4</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_LOGIC_CLASSIFIER =
            EntityProxy.Concept.make("Description-logic classifier (SOLOR)", UUID.fromString("511f6c23-6d09-5466-af2d-dd70cb0bd5e4"));
    /**
     * Java binding for the concept described as <strong><em>Description-logic profile (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/14eadb10-fbd0-5999-af37-05728a8503af">
     * 14eadb10-fbd0-5999-af37-05728a8503af</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_LOGIC_PROFILE =
            EntityProxy.Concept.make("Description-logic profile (SOLOR)", UUID.fromString("14eadb10-fbd0-5999-af37-05728a8503af"));
    /**
     * Java binding for the concept described as <strong><em>Description/dialect properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e52b42c8-15d5-50d0-992c-bac006c963c4">
     * e52b42c8-15d5-50d0-992c-bac006c963c4</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_AND_OR_DIALECT_PROPERTIES =
            EntityProxy.Concept.make("Description/dialect properties (SOLOR)", UUID.fromString("e52b42c8-15d5-50d0-992c-bac006c963c4"));
    /**
     * Java binding for the concept described as <strong><em>Destination module for edit coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/349cfd1d-10fd-5f8d-a0a5-d5ef0932b4da">
     * 349cfd1d-10fd-5f8d-a0a5-d5ef0932b4da</a>}.
     */
    public static final EntityProxy.Concept DESTINATION_MODULE_FOR_EDIT_COORDINATE =
            EntityProxy.Concept.make("Destination module for edit coordinate (SOLOR)", UUID.fromString("349cfd1d-10fd-5f8d-a0a5-d5ef0932b4da"));
    /**
     * Java binding for the concept described as <strong><em>Destination nid for rf2 relationship (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2ae7be24-6056-5c95-9372-9dc6e17594b4">
     * 2ae7be24-6056-5c95-9372-9dc6e17594b4</a>}.
     */
    public static final EntityProxy.Concept DESTINATION_NID_FOR_RF2_RELATIONSHIP =
            EntityProxy.Concept.make("Destination nid for rf2 relationship (SOLOR)", UUID.fromString("2ae7be24-6056-5c95-9372-9dc6e17594b4"));
    /**
     * Java binding for the concept described as <strong><em>Detail nodes (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6e5a6c1-2976-5bfa-847b-14ca0e6417d1">
     * d6e5a6c1-2976-5bfa-847b-14ca0e6417d1</a>}.
     */
    public static final EntityProxy.Concept DETAIL_NODES =
            EntityProxy.Concept.make("Detail nodes (SOLOR)", UUID.fromString("d6e5a6c1-2976-5bfa-847b-14ca0e6417d1"));

    /**
     * Java binding for the concept described as <strong><em>Detail pane axiom order (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/698ab126-7a9d-52f1-97d7-341310b42039">
     * 698ab126-7a9d-52f1-97d7-341310b42039</a>}.
     */
    public static final EntityProxy.Concept DETAIL_PANE_AXIOM_ORDER =
            EntityProxy.Concept.make("Detail pane axiom order (SOLOR)", UUID.fromString("698ab126-7a9d-52f1-97d7-341310b42039"));
    /**
     * Java binding for the concept described as <strong><em>Detail pane description type order (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/39b5762b-5146-53d8-9949-200bcf5cc10f">
     * 39b5762b-5146-53d8-9949-200bcf5cc10f</a>}.
     */
    public static final EntityProxy.Concept DETAIL_PANE_DESCRIPTION_TYPE_ORDER =
            EntityProxy.Concept.make("Detail pane description type order (SOLOR)", UUID.fromString("39b5762b-5146-53d8-9949-200bcf5cc10f"));
    /**
     * Java binding for the concept described as <strong><em>Detail pane order (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b74f3734-fc2c-5d42-b790-b6c49f56e047">
     * b74f3734-fc2c-5d42-b790-b6c49f56e047</a>}.
     */
    public static final EntityProxy.Concept DETAIL_PANE_ORDER =
            EntityProxy.Concept.make("Detail pane order (SOLOR)", UUID.fromString("b74f3734-fc2c-5d42-b790-b6c49f56e047"));
    /**
     * Java binding for the concept described as <strong><em>Development module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/529a7069-bd33-59e6-b2ce-537fa874360a">
     * 529a7069-bd33-59e6-b2ce-537fa874360a</a>}.
     */
    public static final EntityProxy.Concept DEVELOPMENT_MODULE =
            EntityProxy.Concept.make("Development module (SOLOR)", UUID.fromString("529a7069-bd33-59e6-b2ce-537fa874360a"));
    /**
     * Java binding for the concept described as <strong><em>Development path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1f200ca6-960e-11e5-8994-feff819cdc9f">
     * 1f200ca6-960e-11e5-8994-feff819cdc9f</a>}.
     */
    public static final EntityProxy.Concept DEVELOPMENT_PATH =
            EntityProxy.Concept.make("Development path (SOLOR)", UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>DiGraph field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/60113dfe-2bad-11eb-adc1-0242ac120002">
     * 60113dfe-2bad-11eb-adc1-0242ac120002</a>}.
     */
    public static final EntityProxy.Concept DIGRAPH_FIELD =
            EntityProxy.Concept.make("DiGraph field (SOLOR)", UUID.fromString("60113dfe-2bad-11eb-adc1-0242ac120002"));
    /**
     * Java binding for the concept described as <strong><em>DiTree field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/32f64fc6-5371-11eb-ae93-0242ac130002">
     * 32f64fc6-5371-11eb-ae93-0242ac130002</a>}.
     */
    public static final EntityProxy.Concept DITREE_FIELD =
            EntityProxy.Concept.make("DiTree field (SOLOR)", UUID.fromString("32f64fc6-5371-11eb-ae93-0242ac130002"));
    /**
     * Java binding for the concept described as <strong><em>Dialect assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b9c34574-c9ac-503b-aa24-456a0ec949a2">
     * b9c34574-c9ac-503b-aa24-456a0ec949a2</a>}.
     */
    public static final EntityProxy.Concept DIALECT_ASSEMBLAGE =
            EntityProxy.Concept.make("Dialect assemblage (SOLOR)", UUID.fromString("b9c34574-c9ac-503b-aa24-456a0ec949a2"));
    /**
     * Java binding for the concept described as <strong><em>Dialect assemblage preference list for language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c060ffbf-e95f-5960-b296-8a3255c820ac">
     * c060ffbf-e95f-5960-b296-8a3255c820ac</a>}.
     */
    public static final EntityProxy.Concept DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Dialect assemblage preference list for language coordinate (SOLOR)", UUID.fromString("c060ffbf-e95f-5960-b296-8a3255c820ac"));
    /**
     * Java binding for the concept described as <strong><em>Dialect for dialect/description pair (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/850bc47d-5235-5bce-99f4-c41f8a163d69">
     * 850bc47d-5235-5bce-99f4-c41f8a163d69</a>}.
     */
    public static final EntityProxy.Concept DIALECT_FOR_DIALECT_AND_OR_DESCRIPTION_PAIR =
            EntityProxy.Concept.make("Dialect for dialect/description pair (SOLOR)", UUID.fromString("850bc47d-5235-5bce-99f4-c41f8a163d69"));
    /**
     * Java binding for the concept described as <strong><em>Digraph for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1cdacc80-0dea-580f-a77b-8a6b273eb673">
     * 1cdacc80-0dea-580f-a77b-8a6b273eb673</a>}.
     */
    public static final EntityProxy.Concept DIGRAPH_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Digraph for logic coordinate (SOLOR)", UUID.fromString("1cdacc80-0dea-580f-a77b-8a6b273eb673"));
    /**
     * Java binding for the concept described as <strong><em>Digraph for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8ce28c31-fa04-5880-9ddd-3fabb34bd0b0">
     * 8ce28c31-fa04-5880-9ddd-3fabb34bd0b0</a>}.
     */
    public static final EntityProxy.Concept DIGRAPH_FOR_MANIFOLD =
            EntityProxy.Concept.make("Digraph for manifold (SOLOR)", UUID.fromString("8ce28c31-fa04-5880-9ddd-3fabb34bd0b0"));
    /**
     * Java binding for the concept described as <strong><em>Directed graph (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/47a787a7-bdce-528d-bfcc-fde1add8d599">
     * 47a787a7-bdce-528d-bfcc-fde1add8d599</a>}.
     */
    public static final EntityProxy.Concept DIRECTED_GRAPH =
            EntityProxy.Concept.make("Directed graph (SOLOR)", UUID.fromString("47a787a7-bdce-528d-bfcc-fde1add8d599"));
    /**
     * Java binding for the concept described as <strong><em>Discrete measure semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2bd0936f-62dd-5425-bc38-1eca8abb8242">
     * 2bd0936f-62dd-5425-bc38-1eca8abb8242</a>}.
     */
    public static final EntityProxy.Concept DISCRETE_MEASURE_SEMANTIC =
            EntityProxy.Concept.make("Discrete measure semantic (SOLOR)", UUID.fromString("2bd0936f-62dd-5425-bc38-1eca8abb8242"));
    /**
     * Java binding for the concept described as <strong><em>Disjoint with (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f8433993-9a2d-5377-b564-80a45c7b7824">
     * f8433993-9a2d-5377-b564-80a45c7b7824</a>}.
     */
    public static final EntityProxy.Concept DISJOINT_WITH =
            EntityProxy.Concept.make("Disjoint with (SOLOR)", UUID.fromString("f8433993-9a2d-5377-b564-80a45c7b7824"));
    /**
     * Java binding for the concept described as <strong><em>Display Fields (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4e627b9c-cecb-5563-82fc-cb0ee25113b1">
     * 4e627b9c-cecb-5563-82fc-cb0ee25113b1</a>}.
     */
    public static final EntityProxy.Concept DISPLAY_FIELDS =
            EntityProxy.Concept.make("Display Fields (SOLOR)", UUID.fromString("4e627b9c-cecb-5563-82fc-cb0ee25113b1"));
    /**
     * Java binding for the concept described as <strong><em>Dose form is different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a3553feb-930e-545a-bab5-e07be41f44a1">
     * a3553feb-930e-545a-bab5-e07be41f44a1</a>}.
     */
    public static final EntityProxy.Concept DOSE_FORM_IS_DIFFERENT =
            EntityProxy.Concept.make("Dose form is different (SOLOR)", UUID.fromString("a3553feb-930e-545a-bab5-e07be41f44a1"));
    /**
     * Java binding for the concept described as <strong><em>Double field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/85ff6e8f-9151-5428-a5f0-e07844b69260">
     * 85ff6e8f-9151-5428-a5f0-e07844b69260</a>}.
     */
    public static final EntityProxy.Concept DOUBLE_FIELD =
            EntityProxy.Concept.make("Double field (SOLOR)", UUID.fromString("85ff6e8f-9151-5428-a5f0-e07844b69260"));
    /**
     * Java binding for the concept described as <strong><em>Dutch language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/674ad858-0224-3f90-bcf0-bc4cab753d2d">
     * 674ad858-0224-3f90-bcf0-bc4cab753d2d</a>}.
     */
    public static final EntityProxy.Concept DUTCH_LANGUAGE =
            EntityProxy.Concept.make("Dutch language (SOLOR)", UUID.fromString("674ad858-0224-3f90-bcf0-bc4cab753d2d"), UUID.fromString("45022280-9567-11e5-8994-feff819cdc9f"), UUID.fromString("21d11bd1-3dab-5034-9625-81b9ae2bd8e7"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic assemblage definition panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ddc115e0-a9af-526f-a83e-6fbeafbb9b0f">
     * ddc115e0-a9af-526f-a83e-6fbeafbb9b0f</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_ASSEMBLAGE_DEFINITION_PANEL =
            EntityProxy.Concept.make("Dynamic assemblage definition panel (SOLOR)", UUID.fromString("ddc115e0-a9af-526f-a83e-6fbeafbb9b0f"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic assemblages (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e18265b7-5406-52b6-baf0-4cfb867829b4">
     * e18265b7-5406-52b6-baf0-4cfb867829b4</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_ASSEMBLAGES =
            EntityProxy.Concept.make("Dynamic assemblages (SOLOR)", UUID.fromString("e18265b7-5406-52b6-baf0-4cfb867829b4"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic column data types (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/61da7e50-f606-5ba0-a0df-83fd524951e7">
     * 61da7e50-f606-5ba0-a0df-83fd524951e7</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_COLUMN_DATA_TYPES =
            EntityProxy.Concept.make("Dynamic column data types (SOLOR)", UUID.fromString("61da7e50-f606-5ba0-a0df-83fd524951e7"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic columns (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/46ddb9a2-0e10-586a-8b54-8e66333e9b77">
     * 46ddb9a2-0e10-586a-8b54-8e66333e9b77</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_COLUMNS =
            EntityProxy.Concept.make("Dynamic columns (SOLOR)", UUID.fromString("46ddb9a2-0e10-586a-8b54-8e66333e9b77"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic definition description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b0372953-4f20-58b8-ad04-20c2239c7d4e">
     * b0372953-4f20-58b8-ad04-20c2239c7d4e</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_DEFINITION_DESCRIPTION =
            EntityProxy.Concept.make("Dynamic definition description (SOLOR)", UUID.fromString("b0372953-4f20-58b8-ad04-20c2239c7d4e"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic extension definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/406e872b-2e19-5f5e-a71d-e4e4b2c68fe5">
     * 406e872b-2e19-5f5e-a71d-e4e4b2c68fe5</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_EXTENSION_DEFINITION =
            EntityProxy.Concept.make("Dynamic extension definition (SOLOR)", UUID.fromString("406e872b-2e19-5f5e-a71d-e4e4b2c68fe5"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic metadata (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9769773c-7b70-523d-8fc5-b16621ffa57c">
     * 9769773c-7b70-523d-8fc5-b16621ffa57c</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_METADATA =
            EntityProxy.Concept.make("Dynamic metadata (SOLOR)", UUID.fromString("9769773c-7b70-523d-8fc5-b16621ffa57c"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic namespace (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eb0c13ff-74fd-5987-88a0-6f5d75269e9d">
     * eb0c13ff-74fd-5987-88a0-6f5d75269e9d</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_NAMESPACE =
            EntityProxy.Concept.make("Dynamic namespace (SOLOR)", UUID.fromString("eb0c13ff-74fd-5987-88a0-6f5d75269e9d"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic referenced component restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0d94ceeb-e24f-5f1a-84b2-1ac35f671db5">
     * 0d94ceeb-e24f-5f1a-84b2-1ac35f671db5</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_REFERENCED_COMPONENT_RESTRICTION =
            EntityProxy.Concept.make("Dynamic referenced component restriction (SOLOR)", UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"));
    /**
     * Java binding for the concept described as <strong><em>Dynamic semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8ed01f85-4ecc-5a40-8061-d537106d9c9e">
     * 8ed01f85-4ecc-5a40-8061-d537106d9c9e</a>}.
     */
    public static final EntityProxy.Concept DYNAMIC_SEMANTIC =
            EntityProxy.Concept.make("Dynamic semantic (SOLOR)", UUID.fromString("8ed01f85-4ecc-5a40-8061-d537106d9c9e"));
    /**
     * Java binding for the concept described as <strong><em>EL profile set operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2352b7a2-11fd-5a68-8ece-fcb3b36570da">
     * 2352b7a2-11fd-5a68-8ece-fcb3b36570da</a>}.
     */
    public static final EntityProxy.Concept EL_PROFILE_SET_OPERATOR =
            EntityProxy.Concept.make("EL profile set operator (SOLOR)", UUID.fromString("2352b7a2-11fd-5a68-8ece-fcb3b36570da"));
    /**
     * Java binding for the concept described as <strong><em>EL++ Inferred Concept Definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b2897aa0-a697-5bf2-9156-7a437c6a5057">
     * b2897aa0-a697-5bf2-9156-7a437c6a5057</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_INFERRED_CONCEPT_DEFINITION =
            EntityProxy.Concept.make("EL++ Inferred Concept Definition (SOLOR)", UUID.fromString("b2897aa0-a697-5bf2-9156-7a437c6a5057"));

    /**
     * Java binding for the concept described as <strong><em>EL++ Inferred terminological axioms (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b6d3be7d-1d7f-5c44-a425-5357f878c212">
     * b6d3be7d-1d7f-5c44-a425-5357f878c212</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS =
            EntityProxy.Concept.make("EL++ Inferred terminological axioms (SOLOR)", UUID.fromString("b6d3be7d-1d7f-5c44-a425-5357f878c212"));
    /**
     * Java binding for the concept described as <strong><em>EL++ Stated Concept Definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0c464a4a-a0bc-53ef-9c01-ef5a049f2656">
     * 0c464a4a-a0bc-53ef-9c01-ef5a049f2656</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_STATED_CONCEPT_DEFINITION =
            EntityProxy.Concept.make("EL++ Stated Concept Definition (SOLOR)", UUID.fromString("0c464a4a-a0bc-53ef-9c01-ef5a049f2656"));

    /**
     * Java binding for the concept described as <strong><em>EL++ Stated terminological axioms (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1412bd09-eb0c-5107-9756-10c1c417d385">
     * 1412bd09-eb0c-5107-9756-10c1c417d385</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS =
            EntityProxy.Concept.make("EL++ Stated terminological axioms (SOLOR)", UUID.fromString("1412bd09-eb0c-5107-9756-10c1c417d385"));
    /**
     * Java binding for the concept described as <strong><em>EL++ digraph (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ee04d7db-3407-568f-9b93-7b1f9f5bb0fc">
     * ee04d7db-3407-568f-9b93-7b1f9f5bb0fc</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_DIGRAPH =
            EntityProxy.Concept.make("EL++ digraph (SOLOR)", UUID.fromString("ee04d7db-3407-568f-9b93-7b1f9f5bb0fc"));
    /**
     * Java binding for the concept described as <strong><em>EL++ profile (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1f201e12-960e-11e5-8994-feff819cdc9f">
     * 1f201e12-960e-11e5-8994-feff819cdc9f</a>}.
     */
    public static final EntityProxy.Concept EL_PLUS_PLUS_PROFILE =
            EntityProxy.Concept.make("EL++ profile (SOLOR)", UUID.fromString("1f201e12-960e-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>Editor comment (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2b38b1a9-ce6e-5be2-8885-65cd76f40929">
     * 2b38b1a9-ce6e-5be2-8885-65cd76f40929</a>}.
     */
    public static final EntityProxy.Concept EDITOR_COMMENT =
            EntityProxy.Concept.make("Editor comment (SOLOR)", UUID.fromString("2b38b1a9-ce6e-5be2-8885-65cd76f40929"));
    /**
     * Java binding for the concept described as <strong><em>Editor comment context (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2e4187ca-ba45-5a87-8484-1f86801a331a">
     * 2e4187ca-ba45-5a87-8484-1f86801a331a</a>}.
     */
    public static final EntityProxy.Concept EDITOR_COMMENT_CONTEXT =
            EntityProxy.Concept.make("Editor comment context (SOLOR)", UUID.fromString("2e4187ca-ba45-5a87-8484-1f86801a331a"));
    /**
     * Java binding for the concept described as <strong><em>Effective Date (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a332f7bc-f7c1-58cd-a834-cd2660b984da">
     * a332f7bc-f7c1-58cd-a834-cd2660b984da</a>}.
     */
    public static final EntityProxy.Concept EFFECTIVE_DATE =
            EntityProxy.Concept.make("Effective Date (SOLOR)", UUID.fromString("a332f7bc-f7c1-58cd-a834-cd2660b984da"));
    /**
     * Java binding for the concept described as <strong><em>Enable center pane (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cf719bfb-f1ed-52df-aac2-fb7dfd5441a9">
     * cf719bfb-f1ed-52df-aac2-fb7dfd5441a9</a>}.
     */
    public static final EntityProxy.Concept ENABLE_CENTER_PANE =
            EntityProxy.Concept.make("Enable center pane (SOLOR)", UUID.fromString("cf719bfb-f1ed-52df-aac2-fb7dfd5441a9"));
    /**
     * Java binding for the concept described as <strong><em>Enable editing (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6b4067f1-26fa-51e0-8af5-316924e715ce">
     * 6b4067f1-26fa-51e0-8af5-316924e715ce</a>}.
     */
    public static final EntityProxy.Concept ENABLE_EDITING =
            EntityProxy.Concept.make("Enable editing (SOLOR)", UUID.fromString("6b4067f1-26fa-51e0-8af5-316924e715ce"));
    /**
     * Java binding for the concept described as <strong><em>Enable left pane (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/83a2580f-c7fe-5727-8046-cda4b4756617">
     * 83a2580f-c7fe-5727-8046-cda4b4756617</a>}.
     */
    public static final EntityProxy.Concept ENABLE_LEFT_PANE =
            EntityProxy.Concept.make("Enable left pane (SOLOR)", UUID.fromString("83a2580f-c7fe-5727-8046-cda4b4756617"));
    /**
     * Java binding for the concept described as <strong><em>Enable right pane (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6c613e68-534f-5380-ac14-62ccad2fb2cb">
     * 6c613e68-534f-5380-ac14-62ccad2fb2cb</a>}.
     */
    public static final EntityProxy.Concept ENABLE_RIGHT_PANE =
            EntityProxy.Concept.make("Enable right pane (SOLOR)", UUID.fromString("6c613e68-534f-5380-ac14-62ccad2fb2cb"));
    /**
     * Java binding for the concept described as <strong><em>Enclosing concept (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f5111e6c-681d-11e7-907b-a6006ad3dba0">
     * f5111e6c-681d-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept ENCLOSING_CONCEPT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Enclosing concept (query clause)", UUID.fromString("f5111e6c-681d-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>English dialect assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c0836284-f631-3c86-8cfc-56a67814efab">
     * c0836284-f631-3c86-8cfc-56a67814efab</a>}.
     */
    public static final EntityProxy.Concept ENGLISH_DIALECT_ASSEMBLAGE =
            EntityProxy.Concept.make("English dialect assemblage (SOLOR)", UUID.fromString("c0836284-f631-3c86-8cfc-56a67814efab"));
    /**
     * Java binding for the concept described as <strong><em>English language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/06d905ea-c647-3af9-bfe5-2514e135b558">
     * 06d905ea-c647-3af9-bfe5-2514e135b558</a>}.
     */
    public static final EntityProxy.Concept ENGLISH_LANGUAGE =
            EntityProxy.Concept.make("English language (SOLOR)", UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"), UUID.fromString("45021920-9567-11e5-8994-feff819cdc9f"), UUID.fromString("2018e5a-46ba-5297-92f1-6931b9f98a12"));
    /**
     * Java binding for the concept described as <strong><em>Entry sequence for component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f804340c-0fe5-5867-a3c0-4b17432c5df8">
     * f804340c-0fe5-5867-a3c0-4b17432c5df8</a>}.
     */
    public static final EntityProxy.Concept ENTRY_SEQUENCE_FOR_COMPONENT =
            EntityProxy.Concept.make("Entry sequence for component (SOLOR)", UUID.fromString("f804340c-0fe5-5867-a3c0-4b17432c5df8"));
    /**
     * Java binding for the concept described as <strong><em>Equal to (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5c9b5844-1434-5111-83d5-cb7cb0be12d9">
     * 5c9b5844-1434-5111-83d5-cb7cb0be12d9</a>}.
     */
    public static final EntityProxy.Concept EQUAL_TO =
            EntityProxy.Concept.make("Equal to (SOLOR)", UUID.fromString("5c9b5844-1434-5111-83d5-cb7cb0be12d9"));
    /**
     * Java binding for the concept described as <strong><em>Equivalence Type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8e84c657-5f47-51b8-8ebf-89a9d025a9ef">
     * 8e84c657-5f47-51b8-8ebf-89a9d025a9ef</a>}.
     */
    public static final EntityProxy.Concept EQUIVALENCE_TYPE =
            EntityProxy.Concept.make("Equivalence Type (SOLOR)", UUID.fromString("8e84c657-5f47-51b8-8ebf-89a9d025a9ef"));
    /**
     * Java binding for the concept described as <strong><em>Equivalence Types (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/83204ca8-bd51-530c-af04-5edbec04a7c6">
     * 83204ca8-bd51-530c-af04-5edbec04a7c6</a>}.
     */
    public static final EntityProxy.Concept EQUIVALENCE_TYPES =
            EntityProxy.Concept.make("Equivalence Types (SOLOR)", UUID.fromString("83204ca8-bd51-530c-af04-5edbec04a7c6"));
    /**
     * Java binding for the concept described as <strong><em>Event (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c7243365-510d-3e5f-82b3-7286b27d7698">
     * c7243365-510d-3e5f-82b3-7286b27d7698</a>}.
     */
    public static final EntityProxy.Concept EVENT =
            EntityProxy.Concept.make("Event (SOLOR)", UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698"));
    /**
     * Java binding for the concept described as <strong><em>Event duration (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/140238f0-63a4-54b5-b5b4-50b1ba8dee10">
     * 140238f0-63a4-54b5-b5b4-50b1ba8dee10</a>}.
     */
    public static final EntityProxy.Concept EVENT_DURATION =
            EntityProxy.Concept.make("Event duration (SOLOR)", UUID.fromString("140238f0-63a4-54b5-b5b4-50b1ba8dee10"));
    /**
     * Java binding for the concept described as <strong><em>Event frequency (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5d7dedab-1bbf-5ac4-97a3-928742678fee">
     * 5d7dedab-1bbf-5ac4-97a3-928742678fee</a>}.
     */
    public static final EntityProxy.Concept EVENT_FREQUENCY =
            EntityProxy.Concept.make("Event frequency (SOLOR)", UUID.fromString("5d7dedab-1bbf-5ac4-97a3-928742678fee"));
    /**
     * Java binding for the concept described as <strong><em>Event separation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/29162cad-efc0-5864-b61e-1c00d5e21075">
     * 29162cad-efc0-5864-b61e-1c00d5e21075</a>}.
     */
    public static final EntityProxy.Concept EVENT_SEPARATION =
            EntityProxy.Concept.make("Event separation (SOLOR)", UUID.fromString("29162cad-efc0-5864-b61e-1c00d5e21075"));
    /**
     * Java binding for the concept described as <strong><em>Exact (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8aa6421d-4966-5230-ae5f-aca96ee9c2c1">
     * 8aa6421d-4966-5230-ae5f-aca96ee9c2c1</a>}.
     */
    public static final EntityProxy.Concept EXACT =
            EntityProxy.Concept.make("Exact (SOLOR)", UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"));
    /**
     * Java binding for the concept described as <strong><em>Existential measurement semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/57e1643b-da06-5684-a2ef-044727c25b81">
     * 57e1643b-da06-5684-a2ef-044727c25b81</a>}.
     */
    public static final EntityProxy.Concept EXISTENTIAL_MEASUREMENT_SEMANTIC =
            EntityProxy.Concept.make("Existential measurement semantic (SOLOR)", UUID.fromString("57e1643b-da06-5684-a2ef-044727c25b81"));
    /**
     * Java binding for the concept described as <strong><em>Existential restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/91e9080f-78f6-5d23-891d-f5b6e77995c8">
     * 91e9080f-78f6-5d23-891d-f5b6e77995c8</a>}.
     */
    public static final EntityProxy.Concept EXISTENTIAL_RESTRICTION =
            EntityProxy.Concept.make("Existential restriction (SOLOR)", UUID.fromString("91e9080f-78f6-5d23-891d-f5b6e77995c8"));
    /**
     * Java binding for the concept described as <strong><em>Exploration nodes (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/10e347e7-7416-5265-ba12-309990496be2">
     * 10e347e7-7416-5265-ba12-309990496be2</a>}.
     */
    public static final EntityProxy.Concept EXPLORATION_NODES =
            EntityProxy.Concept.make("Exploration nodes (SOLOR)", UUID.fromString("10e347e7-7416-5265-ba12-309990496be2"));
    /**
     * Java binding for the concept described as <strong><em>Export specification panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/04b4e66a-6cc0-596e-ae45-d181d23c1b69">
     * 04b4e66a-6cc0-596e-ae45-d181d23c1b69</a>}.
     */
    public static final EntityProxy.Concept EXPORT_SPECIFICATION_PANEL =
            EntityProxy.Concept.make("Export specification panel (SOLOR)", UUID.fromString("04b4e66a-6cc0-596e-ae45-d181d23c1b69"));
    /**
     * Java binding for the concept described as <strong><em>Express axiom syntax (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/db55557c-e9ee-4504-aae3-df695b6d6c57">
     * db55557c-e9ee-4504-aae3-df695b6d6c57</a>}.
     */
    public static final EntityProxy.Concept EXPRESS_AXIOM_SYNTAX =
            EntityProxy.Concept.make("Express axiom syntax (SOLOR)", UUID.fromString("db55557c-e9ee-4504-aae3-df695b6d6c57"));
    /**
     * Java binding for the concept described as <strong><em>Extended description type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5a2e7786-3e41-11dc-8314-0800200c9a66">
     * 5a2e7786-3e41-11dc-8314-0800200c9a66</a>}.
     */
    public static final EntityProxy.Concept EXTENDED_DESCRIPTION_TYPE =
            EntityProxy.Concept.make("Extended description type (SOLOR)", UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66"));
    /**
     * Java binding for the concept described as <strong><em>Extended relationship type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d41d928f-8a97-55c1-aa6c-a289b413fbfd">
     * d41d928f-8a97-55c1-aa6c-a289b413fbfd</a>}.
     */
    public static final EntityProxy.Concept EXTENDED_RELATIONSHIP_TYPE =
            EntityProxy.Concept.make("Extended relationship type (SOLOR)", UUID.fromString("d41d928f-8a97-55c1-aa6c-a289b413fbfd"));
    /**
     * Java binding for the concept described as <strong><em>Extended search panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0db1a50b-c2ed-588b-a4c7-3e753bad565e">
     * 0db1a50b-c2ed-588b-a4c7-3e753bad565e</a>}.
     */
    public static final EntityProxy.Concept EXTENDED_SEARCH_PANEL =
            EntityProxy.Concept.make("Extended search panel (SOLOR)", UUID.fromString("0db1a50b-c2ed-588b-a4c7-3e753bad565e"));
    /**
     * Java binding for the concept described as <strong><em>External data assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0fba52dd-0727-5e87-a41d-e30ec88b7f87">
     * 0fba52dd-0727-5e87-a41d-e30ec88b7f87</a>}.
     */
    public static final EntityProxy.Concept EXTERNAL_DATA_ASSEMBLAGE =
            EntityProxy.Concept.make("External data assemblage (SOLOR)", UUID.fromString("0fba52dd-0727-5e87-a41d-e30ec88b7f87"));
    /**
     * Java binding for the concept described as <strong><em>External user ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/00e6cca4-3c5b-5f2e-b2d8-2c4a6f8f6b46">
     * 00e6cca4-3c5b-5f2e-b2d8-2c4a6f8f6b46</a>}.
     */
    public static final EntityProxy.Concept EXTERNAL_USER_ID =
            EntityProxy.Concept.make("External user ID (SOLOR)", UUID.fromString("00e6cca4-3c5b-5f2e-b2d8-2c4a6f8f6b46"));
    /**
     * Java binding for the concept described as <strong><em>FHIR URI (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/872859c9-4c1a-5aaa-b39f-89e72b331c55">
     * 872859c9-4c1a-5aaa-b39f-89e72b331c55</a>}.
     */
    public static final EntityProxy.Concept FHIR_URI =
            EntityProxy.Concept.make("FHIR URI (SOLOR)", UUID.fromString("872859c9-4c1a-5aaa-b39f-89e72b331c55"));
    /**
     * Java binding for the concept described as <strong><em>FHIR modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5ac8432f-ebb6-5e5c-96a9-09ccdd665f5b">
     * 5ac8432f-ebb6-5e5c-96a9-09ccdd665f5b</a>}.
     */
    public static final EntityProxy.Concept FHIR_MODULES =
            EntityProxy.Concept.make("FHIR modules (SOLOR)", UUID.fromString("5ac8432f-ebb6-5e5c-96a9-09ccdd665f5b"));
    /**
     * Java binding for the concept described as <strong><em>FLWOR query panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/802a0e02-de52-558e-bab3-f99038147862">
     * 802a0e02-de52-558e-bab3-f99038147862</a>}.
     */
    public static final EntityProxy.Concept FLWOR_QUERY_PANEL =
            EntityProxy.Concept.make("FLWOR query panel (SOLOR)", UUID.fromString("802a0e02-de52-558e-bab3-f99038147862"));
    /**
     * Java binding for the concept described as <strong><em>Father of subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eada0aea-a14c-549d-89c8-9aa48aa6d184">
     * eada0aea-a14c-549d-89c8-9aa48aa6d184</a>}.
     */
    public static final EntityProxy.Concept FATHER_OF_SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Father of subject of record (SOLOR)", UUID.fromString("eada0aea-a14c-549d-89c8-9aa48aa6d184"));
    /**
     * Java binding for the concept described as <strong><em>Feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5e76a88e-794a-5fdd-8eb2-4a9e4b1386b6">
     * 5e76a88e-794a-5fdd-8eb2-4a9e4b1386b6</a>}.
     */
    public static final EntityProxy.Concept FEATURE =
            EntityProxy.Concept.make("Feature (SOLOR)", UUID.fromString("5e76a88e-794a-5fdd-8eb2-4a9e4b1386b6"));
    /**
     * Java binding for the concept described as <strong><em>Feature type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c9120d8b-1acc-5267-9f33-fa716abdb69d">
     * c9120d8b-1acc-5267-9f33-fa716abdb69d</a>}.
     */
    public static final EntityProxy.Concept FEATURE_TYPE =
            EntityProxy.Concept.make("Feature type (SOLOR)", UUID.fromString("c9120d8b-1acc-5267-9f33-fa716abdb69d"));
    /**
     * Java binding for the concept described as <strong><em>Field 1 to join (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2413d6a3-0b00-579f-880d-00eee38feef3">
     * 2413d6a3-0b00-579f-880d-00eee38feef3</a>}.
     */
    public static final EntityProxy.Concept FIELD_1_TO_JOIN =
            EntityProxy.Concept.make("Field 1 to join (SOLOR)", UUID.fromString("2413d6a3-0b00-579f-880d-00eee38feef3"));
    /**
     * Java binding for the concept described as <strong><em>Field 2 to join (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/73fa5921-4a8c-5626-a15a-f8b955f52930">
     * 73fa5921-4a8c-5626-a15a-f8b955f52930</a>}.
     */
    public static final EntityProxy.Concept FIELD_2_TO_JOIN =
            EntityProxy.Concept.make("Field 2 to join (SOLOR)", UUID.fromString("73fa5921-4a8c-5626-a15a-f8b955f52930"));
    /**
     * Java binding for the concept described as <strong><em>Field substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8fdce1aa-ca82-5abc-8cfa-230c14688abc">
     * 8fdce1aa-ca82-5abc-8cfa-230c14688abc</a>}.
     */
    public static final EntityProxy.Concept FIELD_SUBSTITUTION =
            EntityProxy.Concept.make("Field substitution (SOLOR)", UUID.fromString("8fdce1aa-ca82-5abc-8cfa-230c14688abc"));
    /**
     * Java binding for the concept described as <strong><em>Filter coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2ff00052-ec8e-55e8-96ba-f1270ce53697">
     * 2ff00052-ec8e-55e8-96ba-f1270ce53697</a>}.
     */
    public static final EntityProxy.Concept FILTER_COORDINATE =
            EntityProxy.Concept.make("Filter coordinate (SOLOR)", UUID.fromString("2ff00052-ec8e-55e8-96ba-f1270ce53697"));
    /**
     * Java binding for the concept described as <strong><em>Filter coordinate for taxonomy coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d2bd12ef-2b26-5b95-9ab0-e78bce4a3e9c">
     * d2bd12ef-2b26-5b95-9ab0-e78bce4a3e9c</a>}.
     */
    public static final EntityProxy.Concept FILTER_COORDINATE_FOR_TAXONOMY_COORDINATE =
            EntityProxy.Concept.make("Filter coordinate for taxonomy coordinate (SOLOR)", UUID.fromString("d2bd12ef-2b26-5b95-9ab0-e78bce4a3e9c"));
    /**
     * Java binding for the concept described as <strong><em>Filter coordinate name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/90067741-d1b0-59cb-a926-bd220e5b042f">
     * 90067741-d1b0-59cb-a926-bd220e5b042f</a>}.
     */
    public static final EntityProxy.Concept FILTER_COORDINATE_NAME =
            EntityProxy.Concept.make("Filter coordinate name (SOLOR)", UUID.fromString("90067741-d1b0-59cb-a926-bd220e5b042f"));
    /**
     * Java binding for the concept described as <strong><em>Filter coordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b4b680d9-aeff-575e-8b36-363dd9bf0e78">
     * b4b680d9-aeff-575e-8b36-363dd9bf0e78</a>}.
     */
    public static final EntityProxy.Concept FILTER_COORDINATE_PROPERTIES =
            EntityProxy.Concept.make("Filter coordinate properties (SOLOR)", UUID.fromString("b4b680d9-aeff-575e-8b36-363dd9bf0e78"));
    /**
     * Java binding for the concept described as <strong><em>Filter for language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/acdf2877-ee7c-50bc-9404-a5e96b25c9fc">
     * acdf2877-ee7c-50bc-9404-a5e96b25c9fc</a>}.
     */
    public static final EntityProxy.Concept FILTER_FOR_LANGUAGE =
            EntityProxy.Concept.make("Filter for language (SOLOR)", UUID.fromString("acdf2877-ee7c-50bc-9404-a5e96b25c9fc"));
    /**
     * Java binding for the concept described as <strong><em>Filter for vertex (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/36259e73-30ca-50ad-a83d-aa561effbcf1">
     * 36259e73-30ca-50ad-a83d-aa561effbcf1</a>}.
     */
    public static final EntityProxy.Concept FILTER_FOR_VERTEX =
            EntityProxy.Concept.make("Filter for vertex (SOLOR)", UUID.fromString("36259e73-30ca-50ad-a83d-aa561effbcf1"));
    /**
     * Java binding for the concept described as <strong><em>Filter for view (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/70d11ed5-ba0c-55b4-9905-dcb16034ca0e">
     * 70d11ed5-ba0c-55b4-9905-dcb16034ca0e</a>}.
     */
    public static final EntityProxy.Concept FILTER_FOR_VIEW =
            EntityProxy.Concept.make("Filter for view (SOLOR)", UUID.fromString("70d11ed5-ba0c-55b4-9905-dcb16034ca0e"));
    /**
     * Java binding for the concept described as <strong><em>Filter position for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7ccab8e5-99ad-5bc4-b7a0-6182807aaf71">
     * 7ccab8e5-99ad-5bc4-b7a0-6182807aaf71</a>}.
     */
    public static final EntityProxy.Concept FILTER_POSITION_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Filter position for stamp coordinate (SOLOR)", UUID.fromString("7ccab8e5-99ad-5bc4-b7a0-6182807aaf71"));
    /**
     * Java binding for the concept described as <strong><em>Filter precedence for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/be33c0e7-e927-5f21-be18-39c4f4e56bd7">
     * be33c0e7-e927-5f21-be18-39c4f4e56bd7</a>}.
     */
    public static final EntityProxy.Concept FILTER_PRECEDENCE_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Filter precedence for stamp coordinate (SOLOR)", UUID.fromString("be33c0e7-e927-5f21-be18-39c4f4e56bd7"));
    /**
     * Java binding for the concept described as <strong><em>Filter sequence for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0b026997-e0c6-59b1-871f-8486710f7ac1">
     * 0b026997-e0c6-59b1-871f-8486710f7ac1</a>}.
     */
    public static final EntityProxy.Concept FILTER_SEQUENCE_FOR_VERSION =
            EntityProxy.Concept.make("Filter sequence for version (SOLOR)", UUID.fromString("0b026997-e0c6-59b1-871f-8486710f7ac1"));
    /**
     * Java binding for the concept described as <strong><em>Finding (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5a353bd3-b41c-5e18-a088-c40fe13d0cfe">
     * 5a353bd3-b41c-5e18-a088-c40fe13d0cfe</a>}.
     */
    public static final EntityProxy.Concept FINDING =
            EntityProxy.Concept.make("Finding (SOLOR)", UUID.fromString("5a353bd3-b41c-5e18-a088-c40fe13d0cfe"), UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c"));
    /**
     * Java binding for the concept described as <strong><em>Float field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6efe7087-3e3c-5b45-8109-90d7652b1506">
     * 6efe7087-3e3c-5b45-8109-90d7652b1506</a>}.
     */
    public static final EntityProxy.Concept FLOAT_FIELD =
            EntityProxy.Concept.make("Float field (SOLOR)", UUID.fromString("6efe7087-3e3c-5b45-8109-90d7652b1506"));

    /**
     * Java binding for the concept described as <strong><em>Decimal field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b413fe94-4ada-4aee-96f9-22be19699d40">
     * b413fe94-4ada-4aee-96f9-22be19699d40</a>}.
     */
    public static final EntityProxy.Concept DECIMAL_FIELD =
            EntityProxy.Concept.make("Decimal field (SOLOR)", UUID.fromString("b413fe94-4ada-4aee-96f9-22be19699d40"));


    /**
     * Java binding for the concept described as <strong><em>Decimal</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dccb0476-3b63-3d48-b5a2-85bd0ad2fa61">
     * dccb0476-3b63-3d48-b5a2-85bd0ad2fa61</a>}.
     */
    public static final EntityProxy.Concept DECIMAL =
            EntityProxy.Concept.make("Decimal (SOLOR)", UUID.fromString("dccb0476-3b63-3d48-b5a2-85bd0ad2fa61"));
    /**
     * Java binding for the concept described as <strong><em>Float literal (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/da754dd9-9961-5819-91f5-8245d49850b4">
     * da754dd9-9961-5819-91f5-8245d49850b4</a>}.
     */
    public static final EntityProxy.Concept FLOAT_LITERAL =
            EntityProxy.Concept.make("Float literal (SOLOR)", UUID.fromString("da754dd9-9961-5819-91f5-8245d49850b4"));
    /**
     * Java binding for the concept described as <strong><em>Float substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cf18fe25-bd21-586e-9da4-da6cb335fd12">
     * cf18fe25-bd21-586e-9da4-da6cb335fd12</a>}.
     */
    public static final EntityProxy.Concept FLOAT_SUBSTITUTION =
            EntityProxy.Concept.make("Float substitution (SOLOR)", UUID.fromString("cf18fe25-bd21-586e-9da4-da6cb335fd12"));
    /**
     * Java binding for the concept described as <strong><em>For assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/50a1d901-b60f-538b-98ad-7a2dc870df6a">
     * 50a1d901-b60f-538b-98ad-7a2dc870df6a</a>}.
     */
    public static final EntityProxy.Concept FOR_ASSEMBLAGE =
            EntityProxy.Concept.make("For assemblage (SOLOR)", UUID.fromString("50a1d901-b60f-538b-98ad-7a2dc870df6a"));
    /**
     * Java binding for the concept described as <strong><em>Force (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/32213bf6-c073-3ce1-b0c7-9463e43af2f1">
     * 32213bf6-c073-3ce1-b0c7-9463e43af2f1</a>}.
     */
    public static final EntityProxy.Concept FORCE =
            EntityProxy.Concept.make("Force (SOLOR)", UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1"));
    /**
     * Java binding for the concept described as <strong><em>French dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/75d00a0d-8e46-5e42-ad34-3e46269b28a3">
     * 75d00a0d-8e46-5e42-ad34-3e46269b28a3</a>}.
     */
    public static final EntityProxy.Concept FRENCH_DIALECT =
            EntityProxy.Concept.make("French dialect (SOLOR)", UUID.fromString("75d00a0d-8e46-5e42-ad34-3e46269b28a3"));
    /**
     * Java binding for the concept described as <strong><em>French language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e">
     * 8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e</a>}.
     */
    public static final EntityProxy.Concept FRENCH_LANGUAGE =
            EntityProxy.Concept.make("French language (SOLOR)", UUID.fromString("8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e"), UUID.fromString("45021dbc-9567-11e5-8994-feff819cdc9f"), UUID.fromString("01707e47-5f6d-555e-80af-3c1ffb297eaa"));
    /**
     * Java binding for the concept described as <strong><em>Fully qualified name description type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/00791270-77c9-32b6-b34f-d932569bd2bf">
     * 00791270-77c9-32b6-b34f-d932569bd2bf</a>}.
     */
    public static final EntityProxy.Concept FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE =
            EntityProxy.Concept.make("Fully qualified name description type (SOLOR)", UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"), UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));
    /**
     * Java binding for the concept described as <strong><em>Fully qualified name for concept (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f8eb8a8c-57aa-11e7-907b-a6006ad3dba0">
     * f8eb8a8c-57aa-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept FULLY_QUALIFIED_NAME_FOR_CONCEPT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Fully qualified name for concept (query clause)", UUID.fromString("f8eb8a8c-57aa-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Functional feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dc2aa130-6bfe-5561-98a1-1da55bd1e456">
     * dc2aa130-6bfe-5561-98a1-1da55bd1e456</a>}.
     */
    public static final EntityProxy.Concept FUNCTIONAL_FEATURE =
            EntityProxy.Concept.make("Functional feature (SOLOR)", UUID.fromString("dc2aa130-6bfe-5561-98a1-1da55bd1e456"));
    /**
     * Java binding for the concept described as <strong><em>GB English dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eb9a5e42-3cba-356d-b623-3ed472e20b30">
     * eb9a5e42-3cba-356d-b623-3ed472e20b30</a>}.
     */
    public static final EntityProxy.Concept GB_ENGLISH_DIALECT =
            EntityProxy.Concept.make("GB English dialect (SOLOR)", UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
    /**
     * Java binding for the concept described as <strong><em>GEM Flags (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/21bab5a4-18a5-5848-905d-2d99305090d9">
     * 21bab5a4-18a5-5848-905d-2d99305090d9</a>}.
     */
    public static final EntityProxy.Concept GEM_FLAGS =
            EntityProxy.Concept.make("GEM Flags (SOLOR)", UUID.fromString("21bab5a4-18a5-5848-905d-2d99305090d9"));
    /**
     * Java binding for the concept described as <strong><em>Generated administration of module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dc188bee-4750-5ddc-b5ae-9be0d3c2ebf9">
     * dc188bee-4750-5ddc-b5ae-9be0d3c2ebf9</a>}.
     */
    public static final EntityProxy.Concept GENERATED_ADMINISTRATION_OF_MODULE =
            EntityProxy.Concept.make("Generated administration of module (SOLOR)", UUID.fromString("dc188bee-4750-5ddc-b5ae-9be0d3c2ebf9"));
    /**
     * Java binding for the concept described as <strong><em>German language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5f144b18-76a8-5c7e-8480-55a5030d707f">
     * 5f144b18-76a8-5c7e-8480-55a5030d707f</a>}.
     */
    public static final EntityProxy.Concept GERMAN_LANGUAGE =
            EntityProxy.Concept.make("German language (SOLOR)", UUID.fromString("5f144b18-76a8-5c7e-8480-55a5030d707f"));
    /**
     * Java binding for the concept described as <strong><em>Git local folder (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fb2646f2-0a96-525c-b8e1-b31eac65fa86">
     * fb2646f2-0a96-525c-b8e1-b31eac65fa86</a>}.
     */
    public static final EntityProxy.Concept GIT_LOCAL_FOLDER =
            EntityProxy.Concept.make("Git local folder (SOLOR)", UUID.fromString("fb2646f2-0a96-525c-b8e1-b31eac65fa86"));
    /**
     * Java binding for the concept described as <strong><em>Git password (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2a2e8d64-57f0-51c1-af49-daca40f02628">
     * 2a2e8d64-57f0-51c1-af49-daca40f02628</a>}.
     */
    public static final EntityProxy.Concept GIT_PASSWORD =
            EntityProxy.Concept.make("Git password (SOLOR)", UUID.fromString("2a2e8d64-57f0-51c1-af49-daca40f02628"));
    /**
     * Java binding for the concept described as <strong><em>Git url (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f70f1634-aaf3-57e6-8850-b4ac50719c49">
     * f70f1634-aaf3-57e6-8850-b4ac50719c49</a>}.
     */
    public static final EntityProxy.Concept GIT_URL =
            EntityProxy.Concept.make("Git url (SOLOR)", UUID.fromString("f70f1634-aaf3-57e6-8850-b4ac50719c49"));
    /**
     * Java binding for the concept described as <strong><em>Git user name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f3fd24e3-b8e8-5684-a9d3-69d531d0cb16">
     * f3fd24e3-b8e8-5684-a9d3-69d531d0cb16</a>}.
     */
    public static final EntityProxy.Concept GIT_USER_NAME =
            EntityProxy.Concept.make("Git user name (SOLOR)", UUID.fromString("f3fd24e3-b8e8-5684-a9d3-69d531d0cb16"));
    /**
     * Java binding for the concept described as <strong><em>Greater than (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/65af466b-360c-58b2-8b7d-2854150029a8">
     * 65af466b-360c-58b2-8b7d-2854150029a8</a>}.
     */
    public static final EntityProxy.Concept GREATER_THAN =
            EntityProxy.Concept.make("Greater than (SOLOR)", UUID.fromString("65af466b-360c-58b2-8b7d-2854150029a8"));
    /**
     * Java binding for the concept described as <strong><em>Greater than or equal to (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c1baba19-e918-5d2c-8fa4-b0ad93e03186">
     * c1baba19-e918-5d2c-8fa4-b0ad93e03186</a>}.
     */
    public static final EntityProxy.Concept GREATER_THAN_OR_EQUAL_TO =
            EntityProxy.Concept.make("Greater than or equal to (SOLOR)", UUID.fromString("c1baba19-e918-5d2c-8fa4-b0ad93e03186"));
    /**
     * Java binding for the concept described as <strong><em>Groovy scripting panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a8c26aad-1641-5e97-bbba-cc6af2ce084a">
     * a8c26aad-1641-5e97-bbba-cc6af2ce084a</a>}.
     */
    public static final EntityProxy.Concept GROOVY_SCRIPTING_PANEL =
            EntityProxy.Concept.make("Groovy scripting panel (SOLOR)", UUID.fromString("a8c26aad-1641-5e97-bbba-cc6af2ce084a"));
    /**
     * Java binding for the concept described as <strong><em>Grouping (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8d76ead7-6c75-5d25-84d4-ca76d928f8a6">
     * 8d76ead7-6c75-5d25-84d4-ca76d928f8a6</a>}.
     */
    public static final EntityProxy.Concept GROUPING =
            EntityProxy.Concept.make("Grouping (SOLOR)", UUID.fromString("8d76ead7-6c75-5d25-84d4-ca76d928f8a6"));
    /**
     * Java binding for the concept described as <strong><em>HL7® v3 modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/28110240-0cc6-5991-82ea-b616a32c30f0">
     * 28110240-0cc6-5991-82ea-b616a32c30f0</a>}.
     */
    public static final EntityProxy.Concept HL7_V3_MODULES =
            EntityProxy.Concept.make("HL7® v3 modules (SOLOR)", UUID.fromString("28110240-0cc6-5991-82ea-b616a32c30f0"));
    /**
     * Java binding for the concept described as <strong><em>Has active ingredient (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/65bf3b7f-c854-36b5-81c3-4915461020a8">
     * 65bf3b7f-c854-36b5-81c3-4915461020a8</a>}.
     */
    public static final EntityProxy.Concept HAS_ACTIVE_INGREDIENT =
            EntityProxy.Concept.make("Has active ingredient (SOLOR)", UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));
    /**
     * Java binding for the concept described as <strong><em>Has dose form (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/072e7737-e22e-36b5-89d2-4815f0529c63">
     * 072e7737-e22e-36b5-89d2-4815f0529c63</a>}.
     */
    public static final EntityProxy.Concept HAS_DOSE_FORM =
            EntityProxy.Concept.make("Has dose form (SOLOR)", UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));
    /**
     * Java binding for the concept described as <strong><em>Health concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ee9ac5d2-a07c-3981-a57a-f7f26baf38d8">
     * ee9ac5d2-a07c-3981-a57a-f7f26baf38d8</a>}.
     */
    public static final EntityProxy.Concept HEALTH_CONCEPT =
            EntityProxy.Concept.make("Health concept (SOLOR)", UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"), UUID.fromString("a892950a-0847-300c-b477-4e3cbb945225"), UUID.fromString("f6daf03a-93d6-5bab-8dc9-f60c327cf012"));
    /**
     * Java binding for the concept described as <strong><em>Health risk (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/66e012a8-b06f-5bf7-a424-7e2fabd73570">
     * 66e012a8-b06f-5bf7-a424-7e2fabd73570</a>}.
     */
    public static final EntityProxy.Concept HEALTH_RISK =
            EntityProxy.Concept.make("Health risk (SOLOR)", UUID.fromString("66e012a8-b06f-5bf7-a424-7e2fabd73570"));
    /**
     * Java binding for the concept described as <strong><em>ICD10 modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a0e6cfda-7abd-5d96-9114-8d7640110c9e">
     * a0e6cfda-7abd-5d96-9114-8d7640110c9e</a>}.
     */
    public static final EntityProxy.Concept ICD10_MODULES =
            EntityProxy.Concept.make("ICD10 modules (SOLOR)", UUID.fromString("a0e6cfda-7abd-5d96-9114-8d7640110c9e"));
    /**
     * Java binding for the concept described as <strong><em>IHTSDO Classifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9">
     * 7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9</a>}.
     */
    public static final EntityProxy.Concept IHTSDO_CLASSIFIER =
            EntityProxy.Concept.make("IHTSDO Classifier (SOLOR)", UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
    /**
     * Java binding for the concept described as <strong><em>ISO 8601 interval after statement time (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/53fb2295-49f9-58ec-8d71-167974e70eae">
     * 53fb2295-49f9-58ec-8d71-167974e70eae</a>}.
     */
    public static final EntityProxy.Concept ISO_8601_INTERVAL_AFTER_STATEMENT_TIME =
            EntityProxy.Concept.make("ISO 8601 interval after statement time (SOLOR)", UUID.fromString("53fb2295-49f9-58ec-8d71-167974e70eae"));
    /**
     * Java binding for the concept described as <strong><em>ISO 8601 interval prior to statement time (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0b7d4e60-7ef5-57fa-ad0e-ee13433b7ee1">
     * 0b7d4e60-7ef5-57fa-ad0e-ee13433b7ee1</a>}.
     */
    public static final EntityProxy.Concept ISO_8601_INTERVAL_PRIOR_TO_STATEMENT_TIME =
            EntityProxy.Concept.make("ISO 8601 interval prior to statement time (SOLOR)", UUID.fromString("0b7d4e60-7ef5-57fa-ad0e-ee13433b7ee1"));
    /**
     * Java binding for the concept described as <strong><em>ISO 8601 representation of dates and times (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/38baca53-e626-5196-91a5-76e05cb3e115">
     * 38baca53-e626-5196-91a5-76e05cb3e115</a>}.
     */
    public static final EntityProxy.Concept ISO_8601_REPRESENTATION_OF_DATES_AND_TIMES =
            EntityProxy.Concept.make("ISO 8601 representation of dates and times (SOLOR)", UUID.fromString("38baca53-e626-5196-91a5-76e05cb3e115"));
    /**
     * Java binding for the concept described as <strong><em>Identifier source (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5a87935c-d654-548f-82a2-0c06e3801162">
     * 5a87935c-d654-548f-82a2-0c06e3801162</a>}.
     */
    public static final EntityProxy.Concept IDENTIFIER_SOURCE =
            EntityProxy.Concept.make("Identifier source (SOLOR)", UUID.fromString("5a87935c-d654-548f-82a2-0c06e3801162"));
    /**
     * Java binding for the concept described as <strong><em>Image assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f342ba61-a7b2-5833-9c96-8a478eb8b489">
     * f342ba61-a7b2-5833-9c96-8a478eb8b489</a>}.
     */
    public static final EntityProxy.Concept IMAGE_ASSEMBLAGE =
            EntityProxy.Concept.make("Image assemblage (SOLOR)", UUID.fromString("f342ba61-a7b2-5833-9c96-8a478eb8b489"));
    /**
     * Java binding for the concept described as <strong><em>Image data for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6f89b025-e286-5541-8672-d71c379e29b0">
     * 6f89b025-e286-5541-8672-d71c379e29b0</a>}.
     */
    public static final EntityProxy.Concept IMAGE_DATA_FOR_SEMANTIC =
            EntityProxy.Concept.make("Image data for semantic (SOLOR)", UUID.fromString("6f89b025-e286-5541-8672-d71c379e29b0"));
    /**
     * Java binding for the concept described as <strong><em>Image field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cd9ea037-0af9-586b-9369-7bc044cdb8f7">
     * cd9ea037-0af9-586b-9369-7bc044cdb8f7</a>}.
     */
    public static final EntityProxy.Concept IMAGE_FIELD =
            EntityProxy.Concept.make("Image field (SOLOR)", UUID.fromString("cd9ea037-0af9-586b-9369-7bc044cdb8f7"));
    /**
     * Java binding for the concept described as <strong><em>Image semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5c31cb70-a042-59b8-a21c-6aca1c03f907">
     * 5c31cb70-a042-59b8-a21c-6aca1c03f907</a>}.
     */
    public static final EntityProxy.Concept IMAGE_SEMANTIC =
            EntityProxy.Concept.make("Image semantic (SOLOR)", UUID.fromString("5c31cb70-a042-59b8-a21c-6aca1c03f907"));
    /**
     * Java binding for the concept described as <strong><em>Immediate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bffcefca-d520-3d4a-ac37-ce8376376136">
     * bffcefca-d520-3d4a-ac37-ce8376376136</a>}.
     */
    public static final EntityProxy.Concept IMMEDIATE =
            EntityProxy.Concept.make("Immediate (SOLOR)", UUID.fromString("bffcefca-d520-3d4a-ac37-ce8376376136"));
    /**
     * Java binding for the concept described as <strong><em>ImmutableCoordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ab41a788-8a83-5452-8dc0-2d8375e0bfe6">
     * ab41a788-8a83-5452-8dc0-2d8375e0bfe6</a>}.
     */
    public static final EntityProxy.Concept IMMUTABLECOORDINATE_PROPERTIES =
            EntityProxy.Concept.make("ImmutableCoordinate properties (SOLOR)", UUID.fromString("ab41a788-8a83-5452-8dc0-2d8375e0bfe6"));
    /**
     * Java binding for the concept described as <strong><em>Import specification panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/14830e36-3067-56b0-b5b7-a7f297279f4a">
     * 14830e36-3067-56b0-b5b7-a7f297279f4a</a>}.
     */
    public static final EntityProxy.Concept IMPORT_SPECIFICATION_PANEL =
            EntityProxy.Concept.make("Import specification panel (SOLOR)", UUID.fromString("14830e36-3067-56b0-b5b7-a7f297279f4a"));
    /**
     * Java binding for the concept described as <strong><em>Inclusion Set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/def77c09-e1eb-40f2-931a-e7cf2ce0e597">
     * def77c09-e1eb-40f2-931a-e7cf2ce0e597</a>}.
     */
    public static final EntityProxy.Concept INCLUSION_SET =
            EntityProxy.Concept.make("Inclusion Set (SOLOR)", UUID.fromString("def77c09-e1eb-40f2-931a-e7cf2ce0e597"));
    /**
     * Java binding for the concept described as <strong><em>Inactive status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/03004053-c23e-5206-8514-fb551dd328f4">
     * 03004053-c23e-5206-8514-fb551dd328f4</a>}.
     */
    public static final EntityProxy.Concept INACTIVE_STATE =
            EntityProxy.Concept.make("Inactive state (SOLOR)", UUID.fromString("03004053-c23e-5206-8514-fb551dd328f4"));
    /**
     * Java binding for the concept described as <strong><em>Include defining taxonomy (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b1c4d674-6809-5098-bf81-859bdd35a1cb">
     * b1c4d674-6809-5098-bf81-859bdd35a1cb</a>}.
     */
    public static final EntityProxy.Concept INCLUDE_DEFINING_TAXONOMY =
            EntityProxy.Concept.make("Include defining taxonomy (SOLOR)", UUID.fromString("b1c4d674-6809-5098-bf81-859bdd35a1cb"));
    /**
     * Java binding for the concept described as <strong><em>Include lower bound (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9b570048-7387-5cc7-9bb2-26e15beb2d03">
     * 9b570048-7387-5cc7-9bb2-26e15beb2d03</a>}.
     */
    public static final EntityProxy.Concept INCLUDE_LOWER_BOUND =
            EntityProxy.Concept.make("Include lower bound (SOLOR)", UUID.fromString("9b570048-7387-5cc7-9bb2-26e15beb2d03"));
    /**
     * Java binding for the concept described as <strong><em>Include upper bound (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e22f3eaf-d378-5abc-a0ab-3f6fbba59ad1">
     * e22f3eaf-d378-5abc-a0ab-3f6fbba59ad1</a>}.
     */
    public static final EntityProxy.Concept INCLUDE_UPPER_BOUND =
            EntityProxy.Concept.make("Include upper bound (SOLOR)", UUID.fromString("e22f3eaf-d378-5abc-a0ab-3f6fbba59ad1"));
    /**
     * Java binding for the concept described as <strong><em>Inferred assemblage for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9ecf4d76-4346-5e5d-8316-bdff48a5c154">
     * 9ecf4d76-4346-5e5d-8316-bdff48a5c154</a>}.
     */
    public static final EntityProxy.Concept INFERRED_ASSEMBLAGE_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Inferred assemblage for logic coordinate (SOLOR)", UUID.fromString("9ecf4d76-4346-5e5d-8316-bdff48a5c154"));
    /**
     * Java binding for the concept described as <strong><em>Inferred navigation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4bc6c333-7fc9-52f1-942d-f8decba19dc2">
     * 4bc6c333-7fc9-52f1-942d-f8decba19dc2</a>}.
     */
    public static final EntityProxy.Concept INFERRED_NAVIGATION =
            EntityProxy.Concept.make("Inferred navigation (SOLOR)", UUID.fromString("4bc6c333-7fc9-52f1-942d-f8decba19dc2"));
    /**
     * Java binding for the concept described as <strong><em>Inferred premise type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1290e6ba-48d0-31d2-8d62-e133373c63f5">
     * 1290e6ba-48d0-31d2-8d62-e133373c63f5</a>}.
     */
    public static final EntityProxy.Concept INFERRED_PREMISE_TYPE =
            EntityProxy.Concept.make("Inferred premise type (SOLOR)", UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"), UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    /**
     * Java binding for the concept described as <strong><em>Ingredient strength (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f3abdf49-781c-569e-90be-8a2bea8a9a46">
     * f3abdf49-781c-569e-90be-8a2bea8a9a46</a>}.
     */
    public static final EntityProxy.Concept INGREDIENT_STRENGTH =
            EntityProxy.Concept.make("Ingredient strength (SOLOR)", UUID.fromString("f3abdf49-781c-569e-90be-8a2bea8a9a46"));
    /**
     * Java binding for the concept described as <strong><em>Instance mode (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fa0b8eeb-374c-5a31-a5b4-b6334abe31f5">
     * fa0b8eeb-374c-5a31-a5b4-b6334abe31f5</a>}.
     */
    public static final EntityProxy.Concept INSTANCE_MODE =
            EntityProxy.Concept.make("Instance mode (SOLOR)", UUID.fromString("fa0b8eeb-374c-5a31-a5b4-b6334abe31f5"));
    /**
     * Java binding for the concept described as <strong><em>Instant literal (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1fbf42e2-42b7-591f-b7fd-ba5de659529e">
     * 1fbf42e2-42b7-591f-b7fd-ba5de659529e</a>}.
     */
    public static final EntityProxy.Concept INSTANT_LITERAL =
            EntityProxy.Concept.make("Instant literal (SOLOR)", UUID.fromString("1fbf42e2-42b7-591f-b7fd-ba5de659529e"));
    /**
     * Java binding for the concept described as <strong><em>Instant substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/56599345-31c5-5817-9d36-57dd3a626b3a">
     * 56599345-31c5-5817-9d36-57dd3a626b3a</a>}.
     */
    public static final EntityProxy.Concept INSTANT_SUBSTITUTION =
            EntityProxy.Concept.make("Instant substitution (SOLOR)", UUID.fromString("56599345-31c5-5817-9d36-57dd3a626b3a"));
    /**
     * Java binding for the concept described as <strong><em>Integer 1 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/18e42b76-7668-5bd4-89c5-782ad7adf8ab">
     * 18e42b76-7668-5bd4-89c5-782ad7adf8ab</a>}.
     */
    public static final EntityProxy.Concept INTEGER_1 =
            EntityProxy.Concept.make("Integer 1 (SOLOR)", UUID.fromString("18e42b76-7668-5bd4-89c5-782ad7adf8ab"));
    /**
     * Java binding for the concept described as <strong><em>Integer 2 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fe74d354-6cc3-58f6-9e99-c35ae9113994">
     * fe74d354-6cc3-58f6-9e99-c35ae9113994</a>}.
     */
    public static final EntityProxy.Concept INTEGER_2 =
            EntityProxy.Concept.make("Integer 2 (SOLOR)", UUID.fromString("fe74d354-6cc3-58f6-9e99-c35ae9113994"));
    /**
     * Java binding for the concept described as <strong><em>Integer 3 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d609694a-cb90-5097-a7ef-e9959e6d7ff6">
     * d609694a-cb90-5097-a7ef-e9959e6d7ff6</a>}.
     */
    public static final EntityProxy.Concept INTEGER_3 =
            EntityProxy.Concept.make("Integer 3 (SOLOR)", UUID.fromString("d609694a-cb90-5097-a7ef-e9959e6d7ff6"));
    /**
     * Java binding for the concept described as <strong><em>Integer 4 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8b94103d-cf52-5ed2-867f-a7114203012e">
     * 8b94103d-cf52-5ed2-867f-a7114203012e</a>}.
     */
    public static final EntityProxy.Concept INTEGER_4 =
            EntityProxy.Concept.make("Integer 4 (SOLOR)", UUID.fromString("8b94103d-cf52-5ed2-867f-a7114203012e"));
    /**
     * Java binding for the concept described as <strong><em>Integer 5 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a67a559e-192a-5e3e-ad4b-cf0352ae85e3">
     * a67a559e-192a-5e3e-ad4b-cf0352ae85e3</a>}.
     */
    public static final EntityProxy.Concept INTEGER_5 =
            EntityProxy.Concept.make("Integer 5 (SOLOR)", UUID.fromString("a67a559e-192a-5e3e-ad4b-cf0352ae85e3"));
    /**
     * Java binding for the concept described as <strong><em>Integer 6 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/deb05769-7f26-5584-aff0-45bf9e5dd36c">
     * deb05769-7f26-5584-aff0-45bf9e5dd36c</a>}.
     */
    public static final EntityProxy.Concept INTEGER_6 =
            EntityProxy.Concept.make("Integer 6 (SOLOR)", UUID.fromString("deb05769-7f26-5584-aff0-45bf9e5dd36c"));
    /**
     * Java binding for the concept described as <strong><em>Integer 7 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/98076fa6-7b8f-5f58-8eb3-3bbcea9e679d">
     * 98076fa6-7b8f-5f58-8eb3-3bbcea9e679d</a>}.
     */
    public static final EntityProxy.Concept INTEGER_7 =
            EntityProxy.Concept.make("Integer 7 (SOLOR)", UUID.fromString("98076fa6-7b8f-5f58-8eb3-3bbcea9e679d"));
    /**
     * Java binding for the concept described as <strong><em>Integer field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ff59c300-9c4e-5e77-a35d-6a133eb3440f">
     * ff59c300-9c4e-5e77-a35d-6a133eb3440f</a>}.
     */
    public static final EntityProxy.Concept INTEGER_FIELD =
            EntityProxy.Concept.make("Integer field (SOLOR)", UUID.fromString("ff59c300-9c4e-5e77-a35d-6a133eb3440f"));
    /**
     * Java binding for the concept described as <strong><em>Integer literal (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e3cc30f1-ec22-52ff-a8b6-0536b06b6a4c">
     * e3cc30f1-ec22-52ff-a8b6-0536b06b6a4c</a>}.
     */
    public static final EntityProxy.Concept INTEGER_LITERAL =
            EntityProxy.Concept.make("Integer literal (SOLOR)", UUID.fromString("e3cc30f1-ec22-52ff-a8b6-0536b06b6a4c"));
    /**
     * Java binding for the concept described as <strong><em>Integer reference (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/11d47ca2-4207-5aaa-a068-196038aeee4c">
     * 11d47ca2-4207-5aaa-a068-196038aeee4c</a>}.
     */
    public static final EntityProxy.Concept INTEGER_REFERENCE =
            EntityProxy.Concept.make("Integer reference (SOLOR)", UUID.fromString("11d47ca2-4207-5aaa-a068-196038aeee4c"));
    /**
     * Java binding for the concept described as <strong><em>Integer semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a248fe51-70db-5a3f-9d23-9cf20afa2b4d">
     * a248fe51-70db-5a3f-9d23-9cf20afa2b4d</a>}.
     */
    public static final EntityProxy.Concept INTEGER_SEMANTIC =
            EntityProxy.Concept.make("Integer semantic (SOLOR)", UUID.fromString("a248fe51-70db-5a3f-9d23-9cf20afa2b4d"));
    /**
     * Java binding for the concept described as <strong><em>Integer substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2598e0db-888e-5b4c-84d3-56dc6b07a126">
     * 2598e0db-888e-5b4c-84d3-56dc6b07a126</a>}.
     */
    public static final EntityProxy.Concept INTEGER_SUBSTITUTION =
            EntityProxy.Concept.make("Integer substitution (SOLOR)", UUID.fromString("2598e0db-888e-5b4c-84d3-56dc6b07a126"));
    /**
     * Java binding for the concept described as <strong><em>Interval properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eaa1ca46-3197-5e6d-bd90-37429be4e735">
     * eaa1ca46-3197-5e6d-bd90-37429be4e735</a>}.
     */
    public static final EntityProxy.Concept INTERVAL_PROPERTIES =
            EntityProxy.Concept.make("Interval properties (SOLOR)", UUID.fromString("eaa1ca46-3197-5e6d-bd90-37429be4e735"));
    /**
     * Java binding for the concept described as <strong><em>Intervention result status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/99d6d90b-2bd6-5c69-ac42-578401eb61e3">
     * 99d6d90b-2bd6-5c69-ac42-578401eb61e3</a>}.
     */
    public static final EntityProxy.Concept INTERVENTION_RESULT_STATUS =
            EntityProxy.Concept.make("Intervention result status (SOLOR)", UUID.fromString("99d6d90b-2bd6-5c69-ac42-578401eb61e3"));
    /**
     * Java binding for the concept described as <strong><em>Intrinsic role (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a2d37d2d-ac49-589f-ba36-ac9b8808b00b">
     * a2d37d2d-ac49-589f-ba36-ac9b8808b00b</a>}.
     */
    public static final EntityProxy.Concept INTRINSIC_ROLE =
            EntityProxy.Concept.make("Intrinsic role (SOLOR)", UUID.fromString("a2d37d2d-ac49-589f-ba36-ac9b8808b00b"));
    /**
     * Java binding for the concept described as <strong><em>Inverse feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6a30728e-a89d-5985-ad17-f0cbdd9dd1c8">
     * 6a30728e-a89d-5985-ad17-f0cbdd9dd1c8</a>}.
     */
    public static final EntityProxy.Concept INVERSE_FEATURE =
            EntityProxy.Concept.make("Inverse feature (SOLOR)", UUID.fromString("6a30728e-a89d-5985-ad17-f0cbdd9dd1c8"));
    /**
     * Java binding for the concept described as <strong><em>Inverse name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c342d18a-ec1c-5583-bfe3-59e6324ae189">
     * c342d18a-ec1c-5583-bfe3-59e6324ae189</a>}.
     */
    public static final EntityProxy.Concept INVERSE_NAME =
            EntityProxy.Concept.make("Inverse name (SOLOR)", UUID.fromString("c342d18a-ec1c-5583-bfe3-59e6324ae189"));
    /**
     * Java binding for the concept described as <strong><em>Inverse tree list (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/45167fb6-e01d-53a8-8be3-768aae18729d">
     * 45167fb6-e01d-53a8-8be3-768aae18729d</a>}.
     */
    public static final EntityProxy.Concept INVERSE_TREE_LIST =
            EntityProxy.Concept.make("Inverse tree list (SOLOR)", UUID.fromString("45167fb6-e01d-53a8-8be3-768aae18729d"));
    /**
     * Java binding for the concept described as <strong><em>Irish dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c0f77638-6274-5b40-b832-ac1cba7ec515">
     * c0f77638-6274-5b40-b832-ac1cba7ec515</a>}.
     */
    public static final EntityProxy.Concept IRISH_DIALECT =
            EntityProxy.Concept.make("Irish dialect (SOLOR)", UUID.fromString("c0f77638-6274-5b40-b832-ac1cba7ec515"));
    /**
     * Java binding for the concept described as <strong><em>Irish language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/58e82fc4-1492-5cf8-8997-43800360bbd6">
     * 58e82fc4-1492-5cf8-8997-43800360bbd6</a>}.
     */
    public static final EntityProxy.Concept IRISH_LANGUAGE =
            EntityProxy.Concept.make("Irish language (SOLOR)", UUID.fromString("58e82fc4-1492-5cf8-8997-43800360bbd6"));
    /**
     * Java binding for the concept described as <strong><em>Is-a (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/46bccdc4-8fb6-11db-b606-0800200c9a66">
     * 46bccdc4-8fb6-11db-b606-0800200c9a66</a>}.
     */
    public static final EntityProxy.Concept IS_A =
            EntityProxy.Concept.make("Is-a (SOLOR)", UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"), UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
    /**
     * Java binding for the concept described as <strong><em>Is-a inferred navigation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b620768f-1479-5afa-a027-5a9ae6caf0d5">
     * b620768f-1479-5afa-a027-5a9ae6caf0d5</a>}.
     */
    public static final EntityProxy.Concept IS_A_INFERRED_NAVIGATION =
            EntityProxy.Concept.make("Is-a inferred navigation (SOLOR)", UUID.fromString("b620768f-1479-5afa-a027-5a9ae6caf0d5"));
    /**
     * Java binding for the concept described as <strong><em>Is-a stated navigation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d555dde9-c97e-5480-819a-7472eda3dbfa">
     * d555dde9-c97e-5480-819a-7472eda3dbfa</a>}.
     */
    public static final EntityProxy.Concept IS_A_STATED_NAVIGATION =
            EntityProxy.Concept.make("Is-a stated navigation (SOLOR)", UUID.fromString("d555dde9-c97e-5480-819a-7472eda3dbfa"));
    /**
     * Java binding for the concept described as <strong><em>Issue management assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1ca711f1-2b8a-57e8-8c39-a066d97f8ba3">
     * 1ca711f1-2b8a-57e8-8c39-a066d97f8ba3</a>}.
     */
    public static final EntityProxy.Concept ISSUE_MANAGEMENT_ASSEMBLAGE =
            EntityProxy.Concept.make("Issue management assemblage (SOLOR)", UUID.fromString("1ca711f1-2b8a-57e8-8c39-a066d97f8ba3"));
    /**
     * Java binding for the concept described as <strong><em>Italian language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bdd59458-381a-5818-8577-60525f11ac6c">
     * bdd59458-381a-5818-8577-60525f11ac6c</a>}.
     */
    public static final EntityProxy.Concept ITALIAN_LANGUAGE =
            EntityProxy.Concept.make("Italian language (SOLOR)", UUID.fromString("bdd59458-381a-5818-8577-60525f11ac6c"));
    /**
     * Java binding for the concept described as <strong><em>Item active (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/14348c8a-652e-5bd3-a498-b459881f5972">
     * 14348c8a-652e-5bd3-a498-b459881f5972</a>}.
     */
    public static final EntityProxy.Concept ITEM_ACTIVE =
            EntityProxy.Concept.make("Item active (SOLOR)", UUID.fromString("14348c8a-652e-5bd3-a498-b459881f5972"));
    /**
     * Java binding for the concept described as <strong><em>Item count (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ea3c3d2a-4a76-5441-90f8-a8caa4903a3f">
     * ea3c3d2a-4a76-5441-90f8-a8caa4903a3f</a>}.
     */
    public static final EntityProxy.Concept ITEM_COUNT =
            EntityProxy.Concept.make("Item count (SOLOR)", UUID.fromString("ea3c3d2a-4a76-5441-90f8-a8caa4903a3f"));
    /**
     * Java binding for the concept described as <strong><em>Item name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0e5cb409-cf01-5748-8966-c40d4e5c56ae">
     * 0e5cb409-cf01-5748-8966-c40d4e5c56ae</a>}.
     */
    public static final EntityProxy.Concept ITEM_NAME =
            EntityProxy.Concept.make("Item name (SOLOR)", UUID.fromString("0e5cb409-cf01-5748-8966-c40d4e5c56ae"));
    /**
     * Java binding for the concept described as <strong><em>Japanese language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/aa789d52-2278-54cb-9a13-f41c36249f77">
     * aa789d52-2278-54cb-9a13-f41c36249f77</a>}.
     */
    public static final EntityProxy.Concept JAPANESE_LANGUAGE =
            EntityProxy.Concept.make("Japanese language (SOLOR)", UUID.fromString("aa789d52-2278-54cb-9a13-f41c36249f77"), UUID.fromString("450226cc-9567-11e5-8994-feff819cdc9f"), UUID.fromString("7d090f5d-b7fb-5457-8183-da668d50a18e"));
    /**
     * Java binding for the concept described as <strong><em>Join query clause (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/18fa4f5c-6691-597d-897a-93c6f709ec66">
     * 18fa4f5c-6691-597d-897a-93c6f709ec66</a>}.
     */
    public static final EntityProxy.Concept JOIN_QUERY_CLAUSE =
            EntityProxy.Concept.make("Join query clause (SOLOR)", UUID.fromString("18fa4f5c-6691-597d-897a-93c6f709ec66"));
    /**
     * Java binding for the concept described as <strong><em>KOMET issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c6f25132-fa66-586b-9966-126a387560d9">
     * c6f25132-fa66-586b-9966-126a387560d9</a>}.
     */
    public static final EntityProxy.Concept KOMET_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("KOMET issue assemblage (SOLOR)", UUID.fromString("c6f25132-fa66-586b-9966-126a387560d9"));
    /**
     * Java binding for the concept described as <strong><em>KOMET module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/34a6dae3-e5e9-50db-a9ee-69c1067911d8">
     * 34a6dae3-e5e9-50db-a9ee-69c1067911d8</a>}.
     */
    public static final EntityProxy.Concept KOMET_MODULE =
            EntityProxy.Concept.make("KOMET module (SOLOR)", UUID.fromString("34a6dae3-e5e9-50db-a9ee-69c1067911d8"));
    /**
     * Java binding for the concept described as <strong><em>KOMET preference properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eefb2eac-fe64-50c1-8576-a5329c2a9c80">
     * eefb2eac-fe64-50c1-8576-a5329c2a9c80</a>}.
     */
    public static final EntityProxy.Concept KOMET_PREFERENCE_PROPERTIES =
            EntityProxy.Concept.make("KOMET preference properties (SOLOR)", UUID.fromString("eefb2eac-fe64-50c1-8576-a5329c2a9c80"));
    /**
     * Java binding for the concept described as <strong><em>KOMET user (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/61c1a544-2acf-58cd-8cc0-9ac581d4227e">
     * 61c1a544-2acf-58cd-8cc0-9ac581d4227e</a>}.
     */
    public static final EntityProxy.Concept KOMET_USER =
            EntityProxy.Concept.make("KOMET user (SOLOR)", UUID.fromString("61c1a544-2acf-58cd-8cc0-9ac581d4227e"));
    /**
     * Java binding for the concept described as <strong><em>KOMET user list (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5e77558d-97d0-52b6-adf0-d54beb97b3a6">
     * 5e77558d-97d0-52b6-adf0-d54beb97b3a6</a>}.
     */
    public static final EntityProxy.Concept KOMET_USER_LIST =
            EntityProxy.Concept.make("KOMET user list (SOLOR)", UUID.fromString("5e77558d-97d0-52b6-adf0-d54beb97b3a6"));
    /**
     * Java binding for the concept described as <strong><em>Komet environment assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1ad6554d-e3c6-5138-93ee-96178cd06712">
     * 1ad6554d-e3c6-5138-93ee-96178cd06712</a>}.
     */
    public static final EntityProxy.Concept KOMET_ENVIRONMENT_ASSEMBLAGE =
            EntityProxy.Concept.make("Komet environment assemblage (SOLOR)", UUID.fromString("1ad6554d-e3c6-5138-93ee-96178cd06712"));
    /**
     * Java binding for the concept described as <strong><em>Komet issue (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e1dd7bf2-224d-53a5-a5fb-7b25b05d17a6">
     * e1dd7bf2-224d-53a5-a5fb-7b25b05d17a6</a>}.
     */
    public static final EntityProxy.Concept KOMET_ISSUE =
            EntityProxy.Concept.make("Komet issue (SOLOR)", UUID.fromString("e1dd7bf2-224d-53a5-a5fb-7b25b05d17a6"));
    /**
     * Java binding for the concept described as <strong><em>Komet panels (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b3d1cdf6-27a5-502d-8f16-ed026a7b9d15">
     * b3d1cdf6-27a5-502d-8f16-ed026a7b9d15</a>}.
     */
    public static final EntityProxy.Concept KOMET_PANELS =
            EntityProxy.Concept.make("Komet panels (SOLOR)", UUID.fromString("b3d1cdf6-27a5-502d-8f16-ed026a7b9d15"));
    /**
     * Java binding for the concept described as <strong><em>Korean dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6fb2eb9c-fb9e-5959-802c-fb17bcba3079">
     * 6fb2eb9c-fb9e-5959-802c-fb17bcba3079</a>}.
     */
    public static final EntityProxy.Concept KOREAN_DIALECT =
            EntityProxy.Concept.make("Korean dialect (SOLOR)", UUID.fromString("6fb2eb9c-fb9e-5959-802c-fb17bcba3079"));
    /**
     * Java binding for the concept described as <strong><em>Korean language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1464f995-d658-5e9d-86e0-8308a6fa57eb">
     * 1464f995-d658-5e9d-86e0-8308a6fa57eb</a>}.
     */
    public static final EntityProxy.Concept KOREAN_LANGUAGE =
            EntityProxy.Concept.make("Korean language (SOLOR)", UUID.fromString("1464f995-d658-5e9d-86e0-8308a6fa57eb"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0f20a292-bae0-5f5f-b35d-a6bb75693ed6">
     * 0f20a292-bae0-5f5f-b35d-a6bb75693ed6</a>}.
     */
    public static final EntityProxy.Concept LIVD_ASSEMBLAGE =
            EntityProxy.Concept.make("LIVD Assemblage (SOLOR)", UUID.fromString("0f20a292-bae0-5f5f-b35d-a6bb75693ed6"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Equipment UID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9dc2e6b4-ea53-5300-bac0-670e882f80fe">
     * 9dc2e6b4-ea53-5300-bac0-670e882f80fe</a>}.
     */
    public static final EntityProxy.Concept LIVD_EQUIPMENT_UID =
            EntityProxy.Concept.make("LIVD Equipment UID (SOLOR)", UUID.fromString("9dc2e6b4-ea53-5300-bac0-670e882f80fe"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Equipment UID Type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cec037c6-6b46-5b7a-956c-1a204d0c0cb7">
     * cec037c6-6b46-5b7a-956c-1a204d0c0cb7</a>}.
     */
    public static final EntityProxy.Concept LIVD_EQUIPMENT_UID_TYPE =
            EntityProxy.Concept.make("LIVD Equipment UID Type (SOLOR)", UUID.fromString("cec037c6-6b46-5b7a-956c-1a204d0c0cb7"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Manufacturer (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0ccd52c1-73db-56a4-a704-70b97c37fd18">
     * 0ccd52c1-73db-56a4-a704-70b97c37fd18</a>}.
     */
    public static final EntityProxy.Concept LIVD_MANUFACTURER =
            EntityProxy.Concept.make("LIVD Manufacturer (SOLOR)", UUID.fromString("0ccd52c1-73db-56a4-a704-70b97c37fd18"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Model (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c07ef5d9-0d5d-5c9d-b24e-72ba249c9b68">
     * c07ef5d9-0d5d-5c9d-b24e-72ba249c9b68</a>}.
     */
    public static final EntityProxy.Concept LIVD_MODEL =
            EntityProxy.Concept.make("LIVD Model (SOLOR)", UUID.fromString("c07ef5d9-0d5d-5c9d-b24e-72ba249c9b68"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Publication Version ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/82e329a4-44e4-5e8c-92f9-9c18be72f8ae">
     * 82e329a4-44e4-5e8c-92f9-9c18be72f8ae</a>}.
     */
    public static final EntityProxy.Concept LIVD_PUBLICATION_VERSION_ID =
            EntityProxy.Concept.make("LIVD Publication Version ID (SOLOR)", UUID.fromString("82e329a4-44e4-5e8c-92f9-9c18be72f8ae"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Semantics (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c7360b7c-f972-51ec-ac30-8077149ab63d">
     * c7360b7c-f972-51ec-ac30-8077149ab63d</a>}.
     */
    public static final EntityProxy.Concept LIVD_SEMANTICS =
            EntityProxy.Concept.make("LIVD Semantics (SOLOR)", UUID.fromString("c7360b7c-f972-51ec-ac30-8077149ab63d"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Analyte Code (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4bce9e26-d08e-5eef-83d0-67d44a91fffd">
     * 4bce9e26-d08e-5eef-83d0-67d44a91fffd</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_ANALYTE_CODE =
            EntityProxy.Concept.make("LIVD Vendor Analyte Code (SOLOR)", UUID.fromString("4bce9e26-d08e-5eef-83d0-67d44a91fffd"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Analyte Name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f4f5544f-ff58-5201-a563-a9ab7ca5f4f0">
     * f4f5544f-ff58-5201-a563-a9ab7ca5f4f0</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_ANALYTE_NAME =
            EntityProxy.Concept.make("LIVD Vendor Analyte Name (SOLOR)", UUID.fromString("f4f5544f-ff58-5201-a563-a9ab7ca5f4f0"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Comment (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7f1d4908-abc4-5b3d-9847-9eee7a52952d">
     * 7f1d4908-abc4-5b3d-9847-9eee7a52952d</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_COMMENT =
            EntityProxy.Concept.make("LIVD Vendor Comment (SOLOR)", UUID.fromString("7f1d4908-abc4-5b3d-9847-9eee7a52952d"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Reference ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b60b1d2e-7ba8-5a32-a7ba-0a01925bf824">
     * b60b1d2e-7ba8-5a32-a7ba-0a01925bf824</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_REFERENCE_ID =
            EntityProxy.Concept.make("LIVD Vendor Reference ID (SOLOR)", UUID.fromString("b60b1d2e-7ba8-5a32-a7ba-0a01925bf824"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Result Description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5e6620f3-cc53-5316-8639-1029ad836424">
     * 5e6620f3-cc53-5316-8639-1029ad836424</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_RESULT_DESCRIPTION =
            EntityProxy.Concept.make("LIVD Vendor Result Description (SOLOR)", UUID.fromString("5e6620f3-cc53-5316-8639-1029ad836424"));
    /**
     * Java binding for the concept described as <strong><em>LIVD Vendor Specimen Description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ceaca81f-a7bb-5f29-8067-293422df4b9b">
     * ceaca81f-a7bb-5f29-8067-293422df4b9b</a>}.
     */
    public static final EntityProxy.Concept LIVD_VENDOR_SPECIMEN_DESCRIPTION =
            EntityProxy.Concept.make("LIVD Vendor Specimen Description (SOLOR)", UUID.fromString("ceaca81f-a7bb-5f29-8067-293422df4b9b"));
    /**
     * Java binding for the concept described as <strong><em>LOINC ID assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fed88dd4-4421-5059-bd7f-ee80507b3f4a">
     * fed88dd4-4421-5059-bd7f-ee80507b3f4a</a>}.
     */
    public static final EntityProxy.Concept LOINC_ID_ASSEMBLAGE =
            EntityProxy.Concept.make("LOINC ID assemblage (SOLOR)", UUID.fromString("fed88dd4-4421-5059-bd7f-ee80507b3f4a"));
    /**
     * Java binding for the concept described as <strong><em>LOINC component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2a89681f-7a8a-514e-bdbe-71d463acba2c">
     * 2a89681f-7a8a-514e-bdbe-71d463acba2c</a>}.
     */
    public static final EntityProxy.Concept LOINC_COMPONENT =
            EntityProxy.Concept.make("LOINC component (SOLOR)", UUID.fromString("2a89681f-7a8a-514e-bdbe-71d463acba2c"));
    /**
     * Java binding for the concept described as <strong><em>LOINC concept assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d4d1bb43-bf2f-5c4e-b8b8-f0be8a5cca83">
     * d4d1bb43-bf2f-5c4e-b8b8-f0be8a5cca83</a>}.
     */
    public static final EntityProxy.Concept LOINC_CONCEPT_ASSEMBLAGE =
            EntityProxy.Concept.make("LOINC concept assemblage (SOLOR)", UUID.fromString("d4d1bb43-bf2f-5c4e-b8b8-f0be8a5cca83"));
    /**
     * Java binding for the concept described as <strong><em>LOINC long common name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/811dfa3b-bbbb-554e-99b7-c6848cbb35c2">
     * 811dfa3b-bbbb-554e-99b7-c6848cbb35c2</a>}.
     */
    public static final EntityProxy.Concept LOINC_LONG_COMMON_NAME =
            EntityProxy.Concept.make("LOINC long common name (SOLOR)", UUID.fromString("811dfa3b-bbbb-554e-99b7-c6848cbb35c2"));
    /**
     * Java binding for the concept described as <strong><em>LOINC method type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c5ec8c3b-04fd-5180-821a-dd1761c718a4">
     * c5ec8c3b-04fd-5180-821a-dd1761c718a4</a>}.
     */
    public static final EntityProxy.Concept LOINC_METHOD_TYPE =
            EntityProxy.Concept.make("LOINC method type (SOLOR)", UUID.fromString("c5ec8c3b-04fd-5180-821a-dd1761c718a4"));
    /**
     * Java binding for the concept described as <strong><em>LOINC number (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/218792ea-9d15-5f31-a0e2-4cd2423b1ab1">
     * 218792ea-9d15-5f31-a0e2-4cd2423b1ab1</a>}.
     */
    public static final EntityProxy.Concept LOINC_NUMBER =
            EntityProxy.Concept.make("LOINC number (SOLOR)", UUID.fromString("218792ea-9d15-5f31-a0e2-4cd2423b1ab1"));
    /**
     * Java binding for the concept described as <strong><em>LOINC property (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ad218c89-7800-547f-b25f-53078e1b88fd">
     * ad218c89-7800-547f-b25f-53078e1b88fd</a>}.
     */
    public static final EntityProxy.Concept LOINC_PROPERTY =
            EntityProxy.Concept.make("LOINC property (SOLOR)", UUID.fromString("ad218c89-7800-547f-b25f-53078e1b88fd"));
    /**
     * Java binding for the concept described as <strong><em>LOINC record assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/282b86b7-8ae4-5e6a-9dbd-849aabc67922">
     * 282b86b7-8ae4-5e6a-9dbd-849aabc67922</a>}.
     */
    public static final EntityProxy.Concept LOINC_RECORD_ASSEMBLAGE =
            EntityProxy.Concept.make("LOINC record assemblage (SOLOR)", UUID.fromString("282b86b7-8ae4-5e6a-9dbd-849aabc67922"));
    /**
     * Java binding for the concept described as <strong><em>LOINC record assemblage - Dynamic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3995e48f-c77c-5b04-88d9-549dffae95f8">
     * 3995e48f-c77c-5b04-88d9-549dffae95f8</a>}.
     */
    public static final EntityProxy.Concept LOINC_RECORD_ASSEMBLAGE___DYNAMIC =
            EntityProxy.Concept.make("LOINC record assemblage - Dynamic (SOLOR)", UUID.fromString("3995e48f-c77c-5b04-88d9-549dffae95f8"));
    /**
     * Java binding for the concept described as <strong><em>LOINC scale type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/51e1677f-ba00-539e-af87-5e60d7e7e8b0">
     * 51e1677f-ba00-539e-af87-5e60d7e7e8b0</a>}.
     */
    public static final EntityProxy.Concept LOINC_SCALE_TYPE =
            EntityProxy.Concept.make("LOINC scale type (SOLOR)", UUID.fromString("51e1677f-ba00-539e-af87-5e60d7e7e8b0"));
    /**
     * Java binding for the concept described as <strong><em>LOINC short name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/393fde21-e075-585b-b0ee-8a647aee3569">
     * 393fde21-e075-585b-b0ee-8a647aee3569</a>}.
     */
    public static final EntityProxy.Concept LOINC_SHORT_NAME =
            EntityProxy.Concept.make("LOINC short name (SOLOR)", UUID.fromString("393fde21-e075-585b-b0ee-8a647aee3569"));
    /**
     * Java binding for the concept described as <strong><em>LOINC status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/16fcc695-25f1-5404-89e8-257486ce870c">
     * 16fcc695-25f1-5404-89e8-257486ce870c</a>}.
     */
    public static final EntityProxy.Concept LOINC_STATUS =
            EntityProxy.Concept.make("LOINC status (SOLOR)", UUID.fromString("16fcc695-25f1-5404-89e8-257486ce870c"));
    /**
     * Java binding for the concept described as <strong><em>LOINC system (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/533d328c-19da-5da2-a5fa-5d7411839d07">
     * 533d328c-19da-5da2-a5fa-5d7411839d07</a>}.
     */
    public static final EntityProxy.Concept LOINC_SYSTEM =
            EntityProxy.Concept.make("LOINC system (SOLOR)", UUID.fromString("533d328c-19da-5da2-a5fa-5d7411839d07"));
    /**
     * Java binding for the concept described as <strong><em>LOINC time aspect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e95e4a48-6723-5f54-866b-9cd372f97ccc">
     * e95e4a48-6723-5f54-866b-9cd372f97ccc</a>}.
     */
    public static final EntityProxy.Concept LOINC_TIME_ASPECT =
            EntityProxy.Concept.make("LOINC time aspect (SOLOR)", UUID.fromString("e95e4a48-6723-5f54-866b-9cd372f97ccc"));
    /**
     * Java binding for the concept described as <strong><em>LOINC® issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3ede70cf-dc88-57bd-9521-07dbd39098b0">
     * 3ede70cf-dc88-57bd-9521-07dbd39098b0</a>}.
     */
    public static final EntityProxy.Concept LOINC_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("LOINC® issue assemblage (SOLOR)", UUID.fromString("3ede70cf-dc88-57bd-9521-07dbd39098b0"));
    /**
     * Java binding for the concept described as <strong><em>LOINC® license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2c6f846b-a61d-5358-afdd-5e2309157408">
     * 2c6f846b-a61d-5358-afdd-5e2309157408</a>}.
     */
    public static final EntityProxy.Concept LOINC_LICENSE =
            EntityProxy.Concept.make("LOINC® license (SOLOR)", UUID.fromString("2c6f846b-a61d-5358-afdd-5e2309157408"));
    /**
     * Java binding for the concept described as <strong><em>LOINC® modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ab0b825b-09bc-5029-948b-6b53ed0decc5">
     * ab0b825b-09bc-5029-948b-6b53ed0decc5</a>}.
     */
    public static final EntityProxy.Concept LOINC_MODULES =
            EntityProxy.Concept.make("LOINC® modules (SOLOR)", UUID.fromString("ab0b825b-09bc-5029-948b-6b53ed0decc5"));
    /**
     * Java binding for the concept described as <strong><em>Language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f56fa231-10f9-5e7f-a86d-a1d61b5b56e3">
     * f56fa231-10f9-5e7f-a86d-a1d61b5b56e3</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE =
            EntityProxy.Concept.make("Language (SOLOR)", UUID.fromString("f56fa231-10f9-5e7f-a86d-a1d61b5b56e3"));
    /**
     * Java binding for the concept described as <strong><em>Language concept nid for description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cd56cceb-8507-5ae5-a928-16079fe6f832">
     * cd56cceb-8507-5ae5-a928-16079fe6f832</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION =
            EntityProxy.Concept.make("Language concept nid for description (SOLOR)", UUID.fromString("cd56cceb-8507-5ae5-a928-16079fe6f832"));
    /**
     * Java binding for the concept described as <strong><em>Language coordinate for taxonomy coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8a4d34a8-5bba-5f1e-9e4c-78aadbf9683a">
     * 8a4d34a8-5bba-5f1e-9e4c-78aadbf9683a</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE =
            EntityProxy.Concept.make("Language coordinate for taxonomy coordinate (SOLOR)", UUID.fromString("8a4d34a8-5bba-5f1e-9e4c-78aadbf9683a"));
    /**
     * Java binding for the concept described as <strong><em>Language coordinate key for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2621ca26-8efb-56e2-9b1c-61313c40c27f">
     * 2621ca26-8efb-56e2-9b1c-61313c40c27f</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD =
            EntityProxy.Concept.make("Language coordinate key for manifold (SOLOR)", UUID.fromString("2621ca26-8efb-56e2-9b1c-61313c40c27f"));
    /**
     * Java binding for the concept described as <strong><em>Language coordinate name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/42dff20f-5ed2-559a-91ad-91d44a573c63">
     * 42dff20f-5ed2-559a-91ad-91d44a573c63</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_COORDINATE_NAME =
            EntityProxy.Concept.make("Language coordinate name (SOLOR)", UUID.fromString("42dff20f-5ed2-559a-91ad-91d44a573c63"));
    /**
     * Java binding for the concept described as <strong><em>Language coordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ea1a52f7-0305-5487-8766-e846330f167a">
     * ea1a52f7-0305-5487-8766-e846330f167a</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_COORDINATE_PROPERTIES =
            EntityProxy.Concept.make("Language coordinate properties (SOLOR)", UUID.fromString("ea1a52f7-0305-5487-8766-e846330f167a"));
    /**
     * Java binding for the concept described as <strong><em>Language nid for language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/38e0c7b8-1e33-56a2-9eb2-ee20c4960684">
     * 38e0c7b8-1e33-56a2-9eb2-ee20c4960684</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_NID_FOR_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Language nid for language coordinate (SOLOR)", UUID.fromString("38e0c7b8-1e33-56a2-9eb2-ee20c4960684"));
    /**
     * Java binding for the concept described as <strong><em>Language specification for language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b0ad4d77-e1bc-5fd1-922e-5fad675e9bfd">
     * b0ad4d77-e1bc-5fd1-922e-5fad675e9bfd</a>}.
     */
    public static final EntityProxy.Concept LANGUAGE_SPECIFICATION_FOR_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Language specification for language coordinate (SOLOR)", UUID.fromString("b0ad4d77-e1bc-5fd1-922e-5fad675e9bfd"));
    /**
     * Java binding for the concept described as <strong><em>Laterality (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/26ca4590-bbe5-327c-a40a-ba56dc86996b">
     * 26ca4590-bbe5-327c-a40a-ba56dc86996b</a>}.
     */
    public static final EntityProxy.Concept LATERALITY =
            EntityProxy.Concept.make("Laterality (SOLOR)", UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));
    /**
     * Java binding for the concept described as <strong><em>Latin american spanish dialect assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/835d3fe2-7bd7-3aa8-a52a-25e203b0afbe">
     * 835d3fe2-7bd7-3aa8-a52a-25e203b0afbe</a>}.
     */
    public static final EntityProxy.Concept LATIN_AMERICAN_SPANISH_DIALECT_ASSEMBLAGE =
            EntityProxy.Concept.make("Latin american spanish dialect assemblage (SOLOR)", UUID.fromString("835d3fe2-7bd7-3aa8-a52a-25e203b0afbe"));
    /**
     * Java binding for the concept described as <strong><em>Left pane dafaults (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/559f9d01-9435-53da-ac85-6021a80e6353">
     * 559f9d01-9435-53da-ac85-6021a80e6353</a>}.
     */
    public static final EntityProxy.Concept LEFT_PANE_DAFAULTS =
            EntityProxy.Concept.make("Left pane dafaults (SOLOR)", UUID.fromString("559f9d01-9435-53da-ac85-6021a80e6353"));
    /**
     * Java binding for the concept described as <strong><em>Left pane options (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/45504f7e-d2fc-5e0f-bc78-26ce4226d54f">
     * 45504f7e-d2fc-5e0f-bc78-26ce4226d54f</a>}.
     */
    public static final EntityProxy.Concept LEFT_PANE_OPTIONS =
            EntityProxy.Concept.make("Left pane options (SOLOR)", UUID.fromString("45504f7e-d2fc-5e0f-bc78-26ce4226d54f"));
    /**
     * Java binding for the concept described as <strong><em>Left tab nodes (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d7293c45-ebdb-5f0d-bff5-fe0ca1500edf">
     * d7293c45-ebdb-5f0d-bff5-fe0ca1500edf</a>}.
     */
    public static final EntityProxy.Concept LEFT_TAB_NODES =
            EntityProxy.Concept.make("Left tab nodes (SOLOR)", UUID.fromString("d7293c45-ebdb-5f0d-bff5-fe0ca1500edf"));
    /**
     * Java binding for the concept described as <strong><em>Less than (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6f96e8cf-5568-5e49-8a90-aa6c65125ee9">
     * 6f96e8cf-5568-5e49-8a90-aa6c65125ee9</a>}.
     */
    public static final EntityProxy.Concept LESS_THAN =
            EntityProxy.Concept.make("Less than (SOLOR)", UUID.fromString("6f96e8cf-5568-5e49-8a90-aa6c65125ee9"));
    /**
     * Java binding for the concept described as <strong><em>Less than or equal to (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6dfacbd5-8344-5794-9fda-bec95b2aa6c9">
     * 6dfacbd5-8344-5794-9fda-bec95b2aa6c9</a>}.
     */
    public static final EntityProxy.Concept LESS_THAN_OR_EQUAL_TO =
            EntityProxy.Concept.make("Less than or equal to (SOLOR)", UUID.fromString("6dfacbd5-8344-5794-9fda-bec95b2aa6c9"));
    /**
     * Java binding for the concept described as <strong><em>Let item key (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c709cac4-684b-5baf-9e59-f821c6d2eb88">
     * c709cac4-684b-5baf-9e59-f821c6d2eb88</a>}.
     */
    public static final EntityProxy.Concept LET_ITEM_KEY =
            EntityProxy.Concept.make("Let item key (SOLOR)", UUID.fromString("c709cac4-684b-5baf-9e59-f821c6d2eb88"));
    /**
     * Java binding for the concept described as <strong><em>Lineage focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6492d279-34f8-5b88-8cd2-b5652a00316f">
     * 6492d279-34f8-5b88-8cd2-b5652a00316f</a>}.
     */
    public static final EntityProxy.Concept LINEAGE_FOCUS =
            EntityProxy.Concept.make("Lineage focus (SOLOR)", UUID.fromString("6492d279-34f8-5b88-8cd2-b5652a00316f"));
    /**
     * Java binding for the concept described as <strong><em>Literal value (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/208a40a7-e615-5efa-9de0-2e2a5a8488b7">
     * 208a40a7-e615-5efa-9de0-2e2a5a8488b7</a>}.
     */
    public static final EntityProxy.Concept LITERAL_VALUE =
            EntityProxy.Concept.make("Literal value (SOLOR)", UUID.fromString("208a40a7-e615-5efa-9de0-2e2a5a8488b7"));
    /**
     * Java binding for the concept described as <strong><em>Lithuanian language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e9645d95-8a1f-3825-8feb-0bc2ee825694">
     * e9645d95-8a1f-3825-8feb-0bc2ee825694</a>}.
     */
    public static final EntityProxy.Concept LITHUANIAN_LANGUAGE =
            EntityProxy.Concept.make("Lithuanian language (SOLOR)", UUID.fromString("e9645d95-8a1f-3825-8feb-0bc2ee825694"), UUID.fromString("45022410-9567-11e5-8994-feff819cdc9f"), UUID.fromString("8fa63274-70e3-5b11-9669-1b7bdb372b1a"));
    /**
     * Java binding for the concept described as <strong><em>Logic assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8c1c873d-9fdf-5e35-a61a-c0a3f1b8f52c">
     * 8c1c873d-9fdf-5e35-a61a-c0a3f1b8f52c</a>}.
     */
    public static final EntityProxy.Concept LOGIC_ASSEMBLAGE =
            EntityProxy.Concept.make("Logic assemblage (SOLOR)", UUID.fromString("8c1c873d-9fdf-5e35-a61a-c0a3f1b8f52c"));
    /**
     * Java binding for the concept described as <strong><em>Logic coordinate for taxonomy coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7fc0e823-c58a-56df-a6a1-d42445bcbf6a">
     * 7fc0e823-c58a-56df-a6a1-d42445bcbf6a</a>}.
     */
    public static final EntityProxy.Concept LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE =
            EntityProxy.Concept.make("Logic coordinate for taxonomy coordinate (SOLOR)", UUID.fromString("7fc0e823-c58a-56df-a6a1-d42445bcbf6a"));
    /**
     * Java binding for the concept described as <strong><em>Logic coordinate key for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/300bf628-b40d-534f-9072-7b38c4f6f1b5">
     * 300bf628-b40d-534f-9072-7b38c4f6f1b5</a>}.
     */
    public static final EntityProxy.Concept LOGIC_COORDINATE_KEY_FOR_MANIFOLD =
            EntityProxy.Concept.make("Logic coordinate key for manifold (SOLOR)", UUID.fromString("300bf628-b40d-534f-9072-7b38c4f6f1b5"));
    /**
     * Java binding for the concept described as <strong><em>Logic coordinate name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/78972f14-e0f6-5f72-bf82-59310b5f7b26">
     * 78972f14-e0f6-5f72-bf82-59310b5f7b26</a>}.
     */
    public static final EntityProxy.Concept LOGIC_COORDINATE_NAME =
            EntityProxy.Concept.make("Logic coordinate name (SOLOR)", UUID.fromString("78972f14-e0f6-5f72-bf82-59310b5f7b26"));
    /**
     * Java binding for the concept described as <strong><em>Logic coordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1fa63819-5ac1-5938-95b1-47871a5f2b17">
     * 1fa63819-5ac1-5938-95b1-47871a5f2b17</a>}.
     */
    public static final EntityProxy.Concept LOGIC_COORDINATE_PROPERTIES =
            EntityProxy.Concept.make("Logic coordinate properties (SOLOR)", UUID.fromString("1fa63819-5ac1-5938-95b1-47871a5f2b17"));
    /**
     * Java binding for the concept described as <strong><em>Logic details panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6ba3d602-3acd-59b3-af94-2abb75383f7e">
     * 6ba3d602-3acd-59b3-af94-2abb75383f7e</a>}.
     */
    public static final EntityProxy.Concept LOGIC_DETAILS_PANEL =
            EntityProxy.Concept.make("Logic details panel (SOLOR)", UUID.fromString("6ba3d602-3acd-59b3-af94-2abb75383f7e"));
    /**
     * Java binding for the concept described as <strong><em>Logical Definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7dccd042-b0b8-5cec-a1bc-6de676b92f4b">
     * 7dccd042-b0b8-5cec-a1bc-6de676b92f4b</a>}.
     */
    public static final EntityProxy.Concept LOGICAL_DEFINITION =
            EntityProxy.Concept.make("Logical Definition (SOLOR)", UUID.fromString("7dccd042-b0b8-5cec-a1bc-6de676b92f4b"));
    /**
     * Java binding for the concept described as <strong><em>Logical expression field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c16eb414-8840-54f8-9bd2-e2f1ab37e19d">
     * c16eb414-8840-54f8-9bd2-e2f1ab37e19d</a>}.
     */
    public static final EntityProxy.Concept LOGICAL_EXPRESSION_FIELD =
            EntityProxy.Concept.make("Logical expression field (SOLOR)", UUID.fromString("c16eb414-8840-54f8-9bd2-e2f1ab37e19d"));
    /**
     * Java binding for the concept described as <strong><em>Logical expression semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d19306b1-4744-5028-a715-17ca4a4d657f">
     * d19306b1-4744-5028-a715-17ca4a4d657f</a>}.
     */
    public static final EntityProxy.Concept LOGICAL_EXPRESSION_SEMANTIC =
            EntityProxy.Concept.make("Logical expression semantic (SOLOR)", UUID.fromString("d19306b1-4744-5028-a715-17ca4a4d657f"));
    /**
     * Java binding for the concept described as <strong><em>Logical feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e5c4b8e8-8bf6-5924-99a7-394801adaf77">
     * e5c4b8e8-8bf6-5924-99a7-394801adaf77</a>}.
     */
    public static final EntityProxy.Concept LOGICAL_FEATURE =
            EntityProxy.Concept.make("Logical feature (SOLOR)", UUID.fromString("e5c4b8e8-8bf6-5924-99a7-394801adaf77"));
    /**
     * Java binding for the concept described as <strong><em>Logically equivalent to (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3642d9a3-8e23-5289-836b-366c0b1e2900">
     * 3642d9a3-8e23-5289-836b-366c0b1e2900</a>}.
     */
    public static final EntityProxy.Concept LOGICALLY_EQUIVALENT_TO =
            EntityProxy.Concept.make("Logically equivalent to (SOLOR)", UUID.fromString("3642d9a3-8e23-5289-836b-366c0b1e2900"));
    /**
     * Java binding for the concept described as <strong><em>Long 2 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1549dbb4-4c4c-50b0-a351-11c07cbe72bc">
     * 1549dbb4-4c4c-50b0-a351-11c07cbe72bc</a>}.
     */
    public static final EntityProxy.Concept LONG_2 =
            EntityProxy.Concept.make("Long 2 (SOLOR)", UUID.fromString("1549dbb4-4c4c-50b0-a351-11c07cbe72bc"));
    /**
     * Java binding for the concept described as <strong><em>Long field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9574952e-6507-589f-b789-9e9c5d81e50b">
     * 9574952e-6507-589f-b789-9e9c5d81e50b</a>}.
     */
    public static final EntityProxy.Concept LONG_FIELD =
            EntityProxy.Concept.make("Long field (SOLOR)", UUID.fromString("9574952e-6507-589f-b789-9e9c5d81e50b"));
    /**
     * Java binding for the concept described as <strong><em>Lower bound (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cba8ff04-a389-5d71-af9c-5f6d9249cb63">
     * cba8ff04-a389-5d71-af9c-5f6d9249cb63</a>}.
     */
    public static final EntityProxy.Concept LOWER_BOUND =
            EntityProxy.Concept.make("Lower bound (SOLOR)", UUID.fromString("cba8ff04-a389-5d71-af9c-5f6d9249cb63"));
    /**
     * Java binding for the concept described as <strong><em>MVX modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/68f5b75b-b6a6-58bb-b9ca-6904686274d2">
     * 68f5b75b-b6a6-58bb-b9ca-6904686274d2</a>}.
     */
    public static final EntityProxy.Concept MVX_MODULES =
            EntityProxy.Concept.make("MVX modules (SOLOR)", UUID.fromString("68f5b75b-b6a6-58bb-b9ca-6904686274d2"));
    /**
     * Java binding for the concept described as <strong><em>Manifold coordinate key (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/64d9dea8-aafd-5c8a-bce6-a208f91eb82e">
     * 64d9dea8-aafd-5c8a-bce6-a208f91eb82e</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_COORDINATE_KEY =
            EntityProxy.Concept.make("Manifold coordinate key (SOLOR)", UUID.fromString("64d9dea8-aafd-5c8a-bce6-a208f91eb82e"));
    /**
     * Java binding for the concept described as <strong><em>Manifold coordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/81be7ecb-ebe2-5021-a0fc-c423f8290f08">
     * 81be7ecb-ebe2-5021-a0fc-c423f8290f08</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_COORDINATE_PROPERTIES =
            EntityProxy.Concept.make("Manifold coordinate properties (SOLOR)", UUID.fromString("81be7ecb-ebe2-5021-a0fc-c423f8290f08"));
    /**
     * Java binding for the concept described as <strong><em>Manifold coordinate reference (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a061c070-b286-590c-98df-2cdeafcb318c">
     * a061c070-b286-590c-98df-2cdeafcb318c</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_COORDINATE_REFERENCE =
            EntityProxy.Concept.make("Manifold coordinate reference (SOLOR)", UUID.fromString("a061c070-b286-590c-98df-2cdeafcb318c"));
    /**
     * Java binding for the concept described as <strong><em>Manifold focus (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f5e760fa-4f04-5287-bb4a-f97920d9e6e4">
     * f5e760fa-4f04-5287-bb4a-f97920d9e6e4</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_FOCUS =
            EntityProxy.Concept.make("Manifold focus (SOLOR)", UUID.fromString("f5e760fa-4f04-5287-bb4a-f97920d9e6e4"));
    /**
     * Java binding for the concept described as <strong><em>Manifold history (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d87c2e80-3847-5d2d-af62-65512543673f">
     * d87c2e80-3847-5d2d-af62-65512543673f</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_HISTORY =
            EntityProxy.Concept.make("Manifold history (SOLOR)", UUID.fromString("d87c2e80-3847-5d2d-af62-65512543673f"));
    /**
     * Java binding for the concept described as <strong><em>Manifold name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a7f6da29-ea92-5b67-a888-6bc3743e7ae7">
     * a7f6da29-ea92-5b67-a888-6bc3743e7ae7</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_NAME =
            EntityProxy.Concept.make("Manifold name (SOLOR)", UUID.fromString("a7f6da29-ea92-5b67-a888-6bc3743e7ae7"));
    /**
     * Java binding for the concept described as <strong><em>Manifold selection (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e80d3d81-c9f2-51e9-b139-f420b3693b1e">
     * e80d3d81-c9f2-51e9-b139-f420b3693b1e</a>}.
     */
    public static final EntityProxy.Concept MANIFOLD_SELECTION =
            EntityProxy.Concept.make("Manifold selection (SOLOR)", UUID.fromString("e80d3d81-c9f2-51e9-b139-f420b3693b1e"));
    /**
     * Java binding for the concept described as <strong><em>Map Pathway ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e90d3645-8d4a-5ca7-b6ea-78fbc2d85084">
     * e90d3645-8d4a-5ca7-b6ea-78fbc2d85084</a>}.
     */
    public static final EntityProxy.Concept MAP_PATHWAY_ID =
            EntityProxy.Concept.make("Map Pathway ID (SOLOR)", UUID.fromString("e90d3645-8d4a-5ca7-b6ea-78fbc2d85084"));
    /**
     * Java binding for the concept described as <strong><em>Mapping Display Fields (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8d6463c2-b0ec-5e34-a882-1208d52703ea">
     * 8d6463c2-b0ec-5e34-a882-1208d52703ea</a>}.
     */
    public static final EntityProxy.Concept MAPPING_DISPLAY_FIELDS =
            EntityProxy.Concept.make("Mapping Display Fields (SOLOR)", UUID.fromString("8d6463c2-b0ec-5e34-a882-1208d52703ea"));
    /**
     * Java binding for the concept described as <strong><em>Mapping NID Extension (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/276bf07c-4aa7-5176-9853-5f4bd294f163">
     * 276bf07c-4aa7-5176-9853-5f4bd294f163</a>}.
     */
    public static final EntityProxy.Concept MAPPING_NID_EXTENSION =
            EntityProxy.Concept.make("Mapping NID Extension (SOLOR)", UUID.fromString("276bf07c-4aa7-5176-9853-5f4bd294f163"));
    /**
     * Java binding for the concept described as <strong><em>Mapping Purpose (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e5de9548-35b9-5e3b-9968-fd9c0a665b51">
     * e5de9548-35b9-5e3b-9968-fd9c0a665b51</a>}.
     */
    public static final EntityProxy.Concept MAPPING_PURPOSE =
            EntityProxy.Concept.make("Mapping Purpose (SOLOR)", UUID.fromString("e5de9548-35b9-5e3b-9968-fd9c0a665b51"));
    /**
     * Java binding for the concept described as <strong><em>Mapping Semantic Type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01">
     * aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01</a>}.
     */
    public static final EntityProxy.Concept MAPPING_SEMANTIC_TYPE =
            EntityProxy.Concept.make("Mapping Semantic Type (SOLOR)", UUID.fromString("aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01"));
    /**
     * Java binding for the concept described as <strong><em>Mapping String Extension (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/095f1fae-1fc0-5e5d-8d87-675d712522d5">
     * 095f1fae-1fc0-5e5d-8d87-675d712522d5</a>}.
     */
    public static final EntityProxy.Concept MAPPING_STRING_EXTENSION =
            EntityProxy.Concept.make("Mapping String Extension (SOLOR)", UUID.fromString("095f1fae-1fc0-5e5d-8d87-675d712522d5"));
    /**
     * Java binding for the concept described as <strong><em>Maps to code (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7551ee44-c027-519b-8fb2-f643791ccda1">
     * 7551ee44-c027-519b-8fb2-f643791ccda1</a>}.
     */
    public static final EntityProxy.Concept MAPS_TO_CODE =
            EntityProxy.Concept.make("Maps to code (SOLOR)", UUID.fromString("7551ee44-c027-519b-8fb2-f643791ccda1"));
    /**
     * Java binding for the concept described as <strong><em>Maps to name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4e35eb79-265c-59ff-9376-1732aedb94c9">
     * 4e35eb79-265c-59ff-9376-1732aedb94c9</a>}.
     */
    public static final EntityProxy.Concept MAPS_TO_NAME =
            EntityProxy.Concept.make("Maps to name (SOLOR)", UUID.fromString("4e35eb79-265c-59ff-9376-1732aedb94c9"));
    /**
     * Java binding for the concept described as <strong><em>Marked parent (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5b5adb62-6ced-5013-b849-cad9d1bd34f3">
     * 5b5adb62-6ced-5013-b849-cad9d1bd34f3</a>}.
     */
    public static final EntityProxy.Concept MARKED_PARENT =
            EntityProxy.Concept.make("Marked parent (SOLOR)", UUID.fromString("5b5adb62-6ced-5013-b849-cad9d1bd34f3"));
    /**
     * Java binding for the concept described as <strong><em>Mass measurement semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cda282b6-5c4f-539e-ad88-64f88a61263e">
     * cda282b6-5c4f-539e-ad88-64f88a61263e</a>}.
     */
    public static final EntityProxy.Concept MASS_MEASUREMENT_SEMANTIC =
            EntityProxy.Concept.make("Mass measurement semantic (SOLOR)", UUID.fromString("cda282b6-5c4f-539e-ad88-64f88a61263e"));
    /**
     * Java binding for the concept described as <strong><em>Master path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1f20134a-960e-11e5-8994-feff819cdc9f">
     * 1f20134a-960e-11e5-8994-feff819cdc9f</a>}.
     */
    public static final EntityProxy.Concept MASTER_PATH =
            EntityProxy.Concept.make("Master path (SOLOR)", UUID.fromString("1f20134a-960e-11e5-8994-feff819cdc9f"), UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));
    /**
     * Java binding for the concept described as <strong><em>Maternal ancestor of subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bb34ccb6-857d-5cbd-8114-8c34a14b1e42">
     * bb34ccb6-857d-5cbd-8114-8c34a14b1e42</a>}.
     */
    public static final EntityProxy.Concept MATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Maternal ancestor of subject of record (SOLOR)", UUID.fromString("bb34ccb6-857d-5cbd-8114-8c34a14b1e42"));
    /**
     * Java binding for the concept described as <strong><em>Meaning (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a06158ff-e08a-5d7d-bcfa-6cbfdb138910">
     * a06158ff-e08a-5d7d-bcfa-6cbfdb138910</a>}.
     */
    public static final EntityProxy.Concept MEANING =
            EntityProxy.Concept.make("Meaning (SOLOR)", UUID.fromString("a06158ff-e08a-5d7d-bcfa-6cbfdb138910"));
    /**
     * Java binding for the concept described as <strong><em>Measure narritive (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eaccc6f1-89a3-5c01-9038-0b34bf1216a3">
     * eaccc6f1-89a3-5c01-9038-0b34bf1216a3</a>}.
     */
    public static final EntityProxy.Concept MEASURE_NARRITIVE =
            EntityProxy.Concept.make("Measure narritive (SOLOR)", UUID.fromString("eaccc6f1-89a3-5c01-9038-0b34bf1216a3"));
    /**
     * Java binding for the concept described as <strong><em>Measure properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/76736a68-f7fb-5fcb-9be2-b4d0b2fc7763">
     * 76736a68-f7fb-5fcb-9be2-b4d0b2fc7763</a>}.
     */
    public static final EntityProxy.Concept MEASURE_PROPERTIES =
            EntityProxy.Concept.make("Measure properties (SOLOR)", UUID.fromString("76736a68-f7fb-5fcb-9be2-b4d0b2fc7763"));
    /**
     * Java binding for the concept described as <strong><em>Measure semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/918f446b-9597-51df-b1b5-63da1db9c92b">
     * 918f446b-9597-51df-b1b5-63da1db9c92b</a>}.
     */
    public static final EntityProxy.Concept MEASURE_SEMANTIC =
            EntityProxy.Concept.make("Measure semantic (SOLOR)", UUID.fromString("918f446b-9597-51df-b1b5-63da1db9c92b"));
    /**
     * Java binding for the concept described as <strong><em>Measurement semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/080d08ad-67ee-5944-ace6-acccb4cef30f">
     * 080d08ad-67ee-5944-ace6-acccb4cef30f</a>}.
     */
    public static final EntityProxy.Concept MEASUREMENT_SEMANTIC =
            EntityProxy.Concept.make("Measurement semantic (SOLOR)", UUID.fromString("080d08ad-67ee-5944-ace6-acccb4cef30f"));
    /**
     * Java binding for the concept described as <strong><em>Medication (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5032532f-6b58-31f9-84c1-4a365dde4449">
     * 5032532f-6b58-31f9-84c1-4a365dde4449</a>}.
     */
    public static final EntityProxy.Concept MEDICATION =
            EntityProxy.Concept.make("Medication (SOLOR)", UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));
    /**
     * Java binding for the concept described as <strong><em>Membership semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4fa29287-a80e-5f83-abab-4b587973e7b7">
     * 4fa29287-a80e-5f83-abab-4b587973e7b7</a>}.
     */
    public static final EntityProxy.Concept MEMBERSHIP_SEMANTIC =
            EntityProxy.Concept.make("Membership semantic (SOLOR)", UUID.fromString("4fa29287-a80e-5f83-abab-4b587973e7b7"));
    /**
     * Java binding for the concept described as <strong><em>Metadata Modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/04769bab-9ec6-5f79-aa0f-888a3ca8379c">
     * 04769bab-9ec6-5f79-aa0f-888a3ca8379c</a>}.
     */
    public static final EntityProxy.Concept METADATA_MODULES =
            EntityProxy.Concept.make("Metadata Modules (SOLOR)", UUID.fromString("04769bab-9ec6-5f79-aa0f-888a3ca8379c"));
    /**
     * Java binding for the concept described as <strong><em>Milimeters of mercury (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/713876c4-a832-5302-8baa-41cd7e7bcd2d">
     * 713876c4-a832-5302-8baa-41cd7e7bcd2d</a>}.
     */
    public static final EntityProxy.Concept MILIMETERS_OF_MERCURY =
            EntityProxy.Concept.make("Milimeters of mercury (SOLOR)", UUID.fromString("713876c4-a832-5302-8baa-41cd7e7bcd2d"));
    /**
     * Java binding for the concept described as <strong><em>Mode (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ea584999-1ddd-583d-af7d-c337c1b4c1b8">
     * ea584999-1ddd-583d-af7d-c337c1b4c1b8</a>}.
     */
    public static final EntityProxy.Concept MODE =
            EntityProxy.Concept.make("Mode (SOLOR)", UUID.fromString("ea584999-1ddd-583d-af7d-c337c1b4c1b8"));
    /**
     * Java binding for the concept described as <strong><em>Model concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7bbd4210-381c-11e7-9598-0800200c9a66">
     * 7bbd4210-381c-11e7-9598-0800200c9a66</a>}.
     */
    public static final EntityProxy.Concept MODEL_CONCEPT =
            EntityProxy.Concept.make("Model concept (SOLOR)", UUID.fromString("7bbd4210-381c-11e7-9598-0800200c9a66"));
    /**
     * Java binding for the concept described as <strong><em>Module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/40d1c869-b509-32f8-b735-836eac577a67">
     * 40d1c869-b509-32f8-b735-836eac577a67</a>}.
     */
    public static final EntityProxy.Concept MODULE =
            EntityProxy.Concept.make("Module (SOLOR)", UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));
    /**
     * Java binding for the concept described as <strong><em>Module assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c98f592a-f9e0-5d83-acb1-05941bef6e40">
     * c98f592a-f9e0-5d83-acb1-05941bef6e40</a>}.
     */
    public static final EntityProxy.Concept MODULE_ASSEMBLAGE =
            EntityProxy.Concept.make("Module assemblage (SOLOR)", UUID.fromString("c98f592a-f9e0-5d83-acb1-05941bef6e40"));
    /**
     * Java binding for the concept described as <strong><em>Module exclusion set for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3fe047f0-33b0-5254-91c2-43e65f90d30b">
     * 3fe047f0-33b0-5254-91c2-43e65f90d30b</a>}.
     */
    public static final EntityProxy.Concept MODULE_EXCLUSION_SET_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Module exclusion set for stamp coordinate (SOLOR)", UUID.fromString("3fe047f0-33b0-5254-91c2-43e65f90d30b"));
    /**
     * Java binding for the concept described as <strong><em>Module for user (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11">
     * c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11</a>}.
     */
    public static final EntityProxy.Concept MODULE_FOR_USER =
            EntityProxy.Concept.make("Module for user (SOLOR)", UUID.fromString("c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11"));
    /**
     * Java binding for the concept described as <strong><em>Module for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/67cd64f1-96d7-5110-b847-556c055ac063">
     * 67cd64f1-96d7-5110-b847-556c055ac063</a>}.
     */
    public static final EntityProxy.Concept MODULE_FOR_VERSION =
            EntityProxy.Concept.make("Module for version (SOLOR)", UUID.fromString("67cd64f1-96d7-5110-b847-556c055ac063"));
    /**
     * Java binding for the concept described as <strong><em>Module options for edit coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/19305aff-95d9-55d9-b015-825cc68eadc7">
     * 19305aff-95d9-55d9-b015-825cc68eadc7</a>}.
     */
    public static final EntityProxy.Concept MODULE_OPTIONS_FOR_EDIT_COORDINATE =
            EntityProxy.Concept.make("Module options for edit coordinate (SOLOR)", UUID.fromString("19305aff-95d9-55d9-b015-825cc68eadc7"));
    /**
     * Java binding for the concept described as <strong><em>Module preference list for language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f36e7ca6-34a2-58b5-8b25-736457515f9c">
     * f36e7ca6-34a2-58b5-8b25-736457515f9c</a>}.
     */
    public static final EntityProxy.Concept MODULE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Module preference list for language coordinate (SOLOR)", UUID.fromString("f36e7ca6-34a2-58b5-8b25-736457515f9c"));
    /**
     * Java binding for the concept described as <strong><em>Module preference list for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f56ef2df-6758-5271-a587-317a4fed6c2e">
     * f56ef2df-6758-5271-a587-317a4fed6c2e</a>}.
     */
    public static final EntityProxy.Concept MODULE_PREFERENCE_LIST_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Module preference list for stamp coordinate (SOLOR)", UUID.fromString("f56ef2df-6758-5271-a587-317a4fed6c2e"));
    /**
     * Java binding for the concept described as <strong><em>Module preference order for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ddeda759-e89c-5186-aa40-d63070756ab4">
     * ddeda759-e89c-5186-aa40-d63070756ab4</a>}.
     */
    public static final EntityProxy.Concept MODULE_PREFERENCE_ORDER_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Module preference order for stamp coordinate (SOLOR)", UUID.fromString("ddeda759-e89c-5186-aa40-d63070756ab4"));
    /**
     * Java binding for the concept described as <strong><em>Modules for stamp coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bf69c4f1-95c9-5956-a10a-d3ba9276c019">
     * bf69c4f1-95c9-5956-a10a-d3ba9276c019</a>}.
     */
    public static final EntityProxy.Concept MODULES_FOR_STAMP_COORDINATE =
            EntityProxy.Concept.make("Modules for stamp coordinate (SOLOR)", UUID.fromString("bf69c4f1-95c9-5956-a10a-d3ba9276c019"));
    /**
     * Java binding for the concept described as <strong><em>Mother of subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/270737bc-2454-5bb5-b9eb-c142f2cc6004">
     * 270737bc-2454-5bb5-b9eb-c142f2cc6004</a>}.
     */
    public static final EntityProxy.Concept MOTHER_OF_SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Mother of subject of record (SOLOR)", UUID.fromString("270737bc-2454-5bb5-b9eb-c142f2cc6004"));
    /**
     * Java binding for the concept described as <strong><em>NCBI Gene ID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1286e2a2-7bab-567e-8067-41e5c8709424">
     * 1286e2a2-7bab-567e-8067-41e5c8709424</a>}.
     */
    public static final EntityProxy.Concept NCBI_GENE_ID =
            EntityProxy.Concept.make("NCBI Gene ID (SOLOR)", UUID.fromString("1286e2a2-7bab-567e-8067-41e5c8709424"));
    /**
     * Java binding for the concept described as <strong><em>NDC codes available (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/29f638a2-1bc0-54e7-b068-854ba3c4b1b0">
     * 29f638a2-1bc0-54e7-b068-854ba3c4b1b0</a>}.
     */
    public static final EntityProxy.Concept NDC_CODES_AVAILABLE =
            EntityProxy.Concept.make("NDC codes available (SOLOR)", UUID.fromString("29f638a2-1bc0-54e7-b068-854ba3c4b1b0"));
    /**
     * Java binding for the concept described as <strong><em>NUCC modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1cf4956f-3304-5c5b-9093-dc31fbec2f4d">
     * 1cf4956f-3304-5c5b-9093-dc31fbec2f4d</a>}.
     */
    public static final EntityProxy.Concept NUCC_MODULES =
            EntityProxy.Concept.make("NUCC modules (SOLOR)", UUID.fromString("1cf4956f-3304-5c5b-9093-dc31fbec2f4d"));
    /**
     * Java binding for the concept described as <strong><em>Name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6406b947-0b71-5a56-a95f-4c7940c8f3e0">
     * 6406b947-0b71-5a56-a95f-4c7940c8f3e0</a>}.
     */
    public static final EntityProxy.Concept NAME =
            EntityProxy.Concept.make("Name (SOLOR)", UUID.fromString("6406b947-0b71-5a56-a95f-4c7940c8f3e0"));
    /**
     * Java binding for the concept described as <strong><em>Narrow to Broad (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/250d3a08-4f28-5127-8758-e8df4947f89c">
     * 250d3a08-4f28-5127-8758-e8df4947f89c</a>}.
     */
    public static final EntityProxy.Concept NARROW_TO_BROAD =
            EntityProxy.Concept.make("Narrow to Broad (SOLOR)", UUID.fromString("250d3a08-4f28-5127-8758-e8df4947f89c"));
    /**
     * Java binding for the concept described as <strong><em>Native id for component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7e93198d-fa02-596e-943c-34aebff860a5">
     * 7e93198d-fa02-596e-943c-34aebff860a5</a>}.
     */
    public static final EntityProxy.Concept NATIVE_ID_FOR_COMPONENT =
            EntityProxy.Concept.make("Native id for component (SOLOR)", UUID.fromString("7e93198d-fa02-596e-943c-34aebff860a5"));
    /**
     * Java binding for the concept described as <strong><em>Navigation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4d9707d8-adf0-5b15-89fc-039e4ff6fec8">
     * 4d9707d8-adf0-5b15-89fc-039e4ff6fec8</a>}.
     */
    public static final EntityProxy.Concept NAVIGATION =
            EntityProxy.Concept.make("Navigation (SOLOR)", UUID.fromString("4d9707d8-adf0-5b15-89fc-039e4ff6fec8"));
    /**
     * Java binding for the concept described as <strong><em>Navigation concept set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fc965c5d-ad17-555e-bcb5-b78fd45c8c5f">
     * fc965c5d-ad17-555e-bcb5-b78fd45c8c5f</a>}.
     */
    public static final EntityProxy.Concept NAVIGATION_CONCEPT_SET =
            EntityProxy.Concept.make("Navigation concept set (SOLOR)", UUID.fromString("fc965c5d-ad17-555e-bcb5-b78fd45c8c5f"));

    /**
     * Java binding for the concept described as <strong><em>Navigation vertex (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c7f01834-34ca-5f8b-8f80-193fbeb12eae">
     * c7f01834-34ca-5f8b-8f80-193fbeb12eae</a>}.
     */
    public static final EntityProxy.Concept NAVIGATION_VERTEX =
            EntityProxy.Concept.make("Navigation vertex (SOLOR)", UUID.fromString("c7f01834-34ca-5f8b-8f80-193fbeb12eae"));
    /**
     * Java binding for the concept described as <strong><em>Necessary but not sufficient concept definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e1a12059-3b01-3296-9532-d10e49d0afc3">
     * e1a12059-3b01-3296-9532-d10e49d0afc3</a>}.
     */
    public static final EntityProxy.Concept NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION =
            EntityProxy.Concept.make("Necessary but not sufficient concept definition (SOLOR)", UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3"));
    /**
     * Java binding for the concept described as <strong><em>Necessary set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/acaa2eba-8364-5493-b24c-b3884d34bb60">
     * acaa2eba-8364-5493-b24c-b3884d34bb60</a>}.
     */
    public static final EntityProxy.Concept NECESSARY_SET =
            EntityProxy.Concept.make("Necessary set (SOLOR)", UUID.fromString("acaa2eba-8364-5493-b24c-b3884d34bb60"));
    /**
     * Java binding for the concept described as <strong><em>Next priority language coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8bff9950-5981-560c-8d0c-ce6d0a1bffd8">
     * 8bff9950-5981-560c-8d0c-ce6d0a1bffd8</a>}.
     */
    public static final EntityProxy.Concept NEXT_PRIORITY_LANGUAGE_COORDINATE =
            EntityProxy.Concept.make("Next priority language coordinate (SOLOR)", UUID.fromString("8bff9950-5981-560c-8d0c-ce6d0a1bffd8"));
    /**
     * Java binding for the concept described as <strong><em>Node operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5767542b-2aaa-515b-ac9e-279a2c035243">
     * 5767542b-2aaa-515b-ac9e-279a2c035243</a>}.
     */
    public static final EntityProxy.Concept NODE_OPERATOR =
            EntityProxy.Concept.make("Node operator (SOLOR)", UUID.fromString("5767542b-2aaa-515b-ac9e-279a2c035243"));
    /**
     * Java binding for the concept described as <strong><em>Normal member (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bebbda5d-2fa4-5106-8f02-f2d4673fb1c9">
     * bebbda5d-2fa4-5106-8f02-f2d4673fb1c9</a>}.
     */
    public static final EntityProxy.Concept NORMAL_MEMBER =
            EntityProxy.Concept.make("Normal member (SOLOR)", UUID.fromString("bebbda5d-2fa4-5106-8f02-f2d4673fb1c9"));
    /**
     * Java binding for the concept described as <strong><em>Normal range (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2a954776-40eb-5f49-9cdf-2b80c2a80657">
     * 2a954776-40eb-5f49-9cdf-2b80c2a80657</a>}.
     */
    public static final EntityProxy.Concept NORMAL_RANGE =
            EntityProxy.Concept.make("Normal range (SOLOR)", UUID.fromString("2a954776-40eb-5f49-9cdf-2b80c2a80657"));
    /**
     * Java binding for the concept described as <strong><em>Not (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1ea9a-579e-11e7-907b-a6006ad3dba0">
     * d9c1ea9a-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept NOT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Not (query clause)", UUID.fromString("d9c1ea9a-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Not Applicable (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d4cc29ae-c0c1-563a-985d-5165a768dd44">
     * d4cc29ae-c0c1-563a-985d-5165a768dd44</a>}.
     */
    public static final EntityProxy.Concept NOT_APPLICABLE =
            EntityProxy.Concept.make("Not Applicable (SOLOR)", UUID.fromString("d4cc29ae-c0c1-563a-985d-5165a768dd44"));
    /**
     * Java binding for the concept described as <strong><em>OID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/374ce9a6-7f66-5c70-94ae-9aeea2f95c73">
     * 374ce9a6-7f66-5c70-94ae-9aeea2f95c73</a>}.
     */
    public static final EntityProxy.Concept OID =
            EntityProxy.Concept.make("OID (SOLOR)", UUID.fromString("374ce9a6-7f66-5c70-94ae-9aeea2f95c73"));
    /**
     * Java binding for the concept described as <strong><em>Object (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/72765109-6b53-3814-9b05-34ebddd16592">
     * 72765109-6b53-3814-9b05-34ebddd16592</a>}.
     */
    public static final EntityProxy.Concept OBJECT =
            EntityProxy.Concept.make("Object (SOLOR)", UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
    /**
     * Java binding for the concept described as <strong><em>Object properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3ef4311c-70c0-5149-9e06-53d745f85b15">
     * 3ef4311c-70c0-5149-9e06-53d745f85b15</a>}.
     */
    public static final EntityProxy.Concept OBJECT_PROPERTIES =
            EntityProxy.Concept.make("Object properties (SOLOR)", UUID.fromString("3ef4311c-70c0-5149-9e06-53d745f85b15"));
    /**
     * Java binding for the concept described as <strong><em>Observation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e7d177f1-84b1-5e00-89d1-e26399ca20dd">
     * e7d177f1-84b1-5e00-89d1-e26399ca20dd</a>}.
     */
    public static final EntityProxy.Concept OBSERVATION =
            EntityProxy.Concept.make("Observation (SOLOR)", UUID.fromString("e7d177f1-84b1-5e00-89d1-e26399ca20dd"), UUID.fromString("d678e7a6-5562-3ff1-800e-ab070e329824"));
    /**
     * Java binding for the concept described as <strong><em>Or (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2c940bcf-22a8-5fc9-b232-580021e758ed">
     * 2c940bcf-22a8-5fc9-b232-580021e758ed</a>}.
     */
    public static final EntityProxy.Concept OR =
            EntityProxy.Concept.make("Or (SOLOR)", UUID.fromString("2c940bcf-22a8-5fc9-b232-580021e758ed"));
    /**
     * Java binding for the concept described as <strong><em>Order for axiom attachments (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/abcb0946-20e1-5483-8469-3e8fa0ce20c4">
     * abcb0946-20e1-5483-8469-3e8fa0ce20c4</a>}.
     */
    public static final EntityProxy.Concept ORDER_FOR_AXIOM_ATTACHMENTS =
            EntityProxy.Concept.make("Order for axiom attachments (SOLOR)", UUID.fromString("abcb0946-20e1-5483-8469-3e8fa0ce20c4"));
    /**
     * Java binding for the concept described as <strong><em>Order for concept attachments (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6167efcb-50e8-534d-9827-fdd60b02ae00">
     * 6167efcb-50e8-534d-9827-fdd60b02ae00</a>}.
     */
    public static final EntityProxy.Concept ORDER_FOR_CONCEPT_ATTACHMENTS =
            EntityProxy.Concept.make("Order for concept attachments (SOLOR)", UUID.fromString("6167efcb-50e8-534d-9827-fdd60b02ae00"));
    /**
     * Java binding for the concept described as <strong><em>Order for description attachments (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/69ee3f13-e2ba-5a96-9b91-5eecfad8e587">
     * 69ee3f13-e2ba-5a96-9b91-5eecfad8e587</a>}.
     */
    public static final EntityProxy.Concept ORDER_FOR_DESCRIPTION_ATTACHMENTS =
            EntityProxy.Concept.make("Order for description attachments (SOLOR)", UUID.fromString("69ee3f13-e2ba-5a96-9b91-5eecfad8e587"));
    /**
     * Java binding for the concept described as <strong><em>Organism (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0bab48ac-3030-3568-93d8-aee0f63bf072">
     * 0bab48ac-3030-3568-93d8-aee0f63bf072</a>}.
     */
    public static final EntityProxy.Concept ORGANISM =
            EntityProxy.Concept.make("Organism (SOLOR)", UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072"));
    /**
     * Java binding for the concept described as <strong><em>Origin Filter coordinate key for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bb3bfeb0-5090-5512-9281-c2ce60927820">
     * bb3bfeb0-5090-5512-9281-c2ce60927820</a>}.
     */
    public static final EntityProxy.Concept ORIGIN_FILTER_COORDINATE_KEY_FOR_MANIFOLD =
            EntityProxy.Concept.make("Origin Filter coordinate key for manifold (SOLOR)", UUID.fromString("bb3bfeb0-5090-5512-9281-c2ce60927820"));
    /**
     * Java binding for the concept described as <strong><em>Part of (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b4c3f6f9-6937-30fd-8412-d0c77f8a7f73">
     * b4c3f6f9-6937-30fd-8412-d0c77f8a7f73</a>}.
     */
    public static final EntityProxy.Concept PART_OF =
            EntityProxy.Concept.make("Part of (SOLOR)", UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));
    /**
     * Java binding for the concept described as <strong><em>Partial (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a7f9574c-8e8b-515d-9c21-9896063cc3b8">
     * a7f9574c-8e8b-515d-9c21-9896063cc3b8</a>}.
     */
    public static final EntityProxy.Concept PARTIAL =
            EntityProxy.Concept.make("Partial (SOLOR)", UUID.fromString("a7f9574c-8e8b-515d-9c21-9896063cc3b8"));
    /**
     * Java binding for the concept described as <strong><em>Participant id (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ee2361f2-580a-513c-8f00-168664c741b5">
     * ee2361f2-580a-513c-8f00-168664c741b5</a>}.
     */
    public static final EntityProxy.Concept PARTICIPANT_ID =
            EntityProxy.Concept.make("Participant id (SOLOR)", UUID.fromString("ee2361f2-580a-513c-8f00-168664c741b5"));
    /**
     * Java binding for the concept described as <strong><em>Participant properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4a96cc5a-a6e8-5e09-ac9f-5f9c97cb66ca">
     * 4a96cc5a-a6e8-5e09-ac9f-5f9c97cb66ca</a>}.
     */
    public static final EntityProxy.Concept PARTICIPANT_PROPERTIES =
            EntityProxy.Concept.make("Participant properties (SOLOR)", UUID.fromString("4a96cc5a-a6e8-5e09-ac9f-5f9c97cb66ca"));
    /**
     * Java binding for the concept described as <strong><em>Participant role (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/34de9980-6c1b-55de-a52e-6b4c585c1d47">
     * 34de9980-6c1b-55de-a52e-6b4c585c1d47</a>}.
     */
    public static final EntityProxy.Concept PARTICIPANT_ROLE =
            EntityProxy.Concept.make("Participant role (SOLOR)", UUID.fromString("34de9980-6c1b-55de-a52e-6b4c585c1d47"));
    /**
     * Java binding for the concept described as <strong><em>Participants (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/66030366-9e46-5999-8f1f-69013f816a24">
     * 66030366-9e46-5999-8f1f-69013f816a24</a>}.
     */
    public static final EntityProxy.Concept PARTICIPANTS =
            EntityProxy.Concept.make("Participants (SOLOR)", UUID.fromString("66030366-9e46-5999-8f1f-69013f816a24"));
    /**
     * Java binding for the concept described as <strong><em>Paternal ancestor of subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e6ec4948-9167-5c16-9698-ab2747e5cdf3">
     * e6ec4948-9167-5c16-9698-ab2747e5cdf3</a>}.
     */
    public static final EntityProxy.Concept PATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Paternal ancestor of subject of record (SOLOR)", UUID.fromString("e6ec4948-9167-5c16-9698-ab2747e5cdf3"));
    /**
     * Java binding for the concept described as <strong><em>Path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4459d8cf-5a6f-3952-9458-6d64324b27b7">
     * 4459d8cf-5a6f-3952-9458-6d64324b27b7</a>}.
     */
    public static final EntityProxy.Concept PATH =
            EntityProxy.Concept.make("Path (SOLOR)", UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
    /**
     * Java binding for the concept described as <strong><em>Path concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1b9d9f95-fc0a-55ac-b2e6-7c8b37660046">
     * 1b9d9f95-fc0a-55ac-b2e6-7c8b37660046</a>}.
     */
    public static final EntityProxy.Concept PATH_CONCEPT =
            EntityProxy.Concept.make("Path concept (SOLOR)", UUID.fromString("1b9d9f95-fc0a-55ac-b2e6-7c8b37660046"));
    /**
     * Java binding for the concept described as <strong><em>Path coordinate name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a293a9a0-eb1e-5418-83c7-bec376b62245">
     * a293a9a0-eb1e-5418-83c7-bec376b62245</a>}.
     */
    public static final EntityProxy.Concept PATH_COORDINATE_NAME =
            EntityProxy.Concept.make("Path coordinate name (SOLOR)", UUID.fromString("a293a9a0-eb1e-5418-83c7-bec376b62245"));
    /**
     * Java binding for the concept described as <strong><em>Path coordinate properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ec41e427-f009-5e45-a643-6dc658d63d83">
     * ec41e427-f009-5e45-a643-6dc658d63d83</a>}.
     */
    public static final EntityProxy.Concept PATH_COORDINATE_PROPERTIES =
            EntityProxy.Concept.make("Path coordinate properties (SOLOR)", UUID.fromString("ec41e427-f009-5e45-a643-6dc658d63d83"));
    /**
     * Java binding for the concept described as <strong><em>Path for path coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/748e073c-fea7-58dd-8aa3-f18fdd82ddfc">
     * 748e073c-fea7-58dd-8aa3-f18fdd82ddfc</a>}.
     */
    public static final EntityProxy.Concept PATH_FOR_PATH_COORDINATE =
            EntityProxy.Concept.make("Path for path coordinate (SOLOR)", UUID.fromString("748e073c-fea7-58dd-8aa3-f18fdd82ddfc"));
    /**
     * Java binding for the concept described as <strong><em>Path for stamp position (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b15bc7e9-766e-5fea-bb57-97e5f11306f9">
     * b15bc7e9-766e-5fea-bb57-97e5f11306f9</a>}.
     */
    public static final EntityProxy.Concept PATH_FOR_STAMP_POSITION =
            EntityProxy.Concept.make("Path for stamp position (SOLOR)", UUID.fromString("b15bc7e9-766e-5fea-bb57-97e5f11306f9"));
    /**
     * Java binding for the concept described as <strong><em>Path for user (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/12131382-1535-5a77-928b-6eacad221ea2">
     * 12131382-1535-5a77-928b-6eacad221ea2</a>}.
     */
    public static final EntityProxy.Concept PATH_FOR_USER =
            EntityProxy.Concept.make("Path for user (SOLOR)", UUID.fromString("12131382-1535-5a77-928b-6eacad221ea2"));
    /**
     * Java binding for the concept described as <strong><em>Path for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f">
     * ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f</a>}.
     */
    public static final EntityProxy.Concept PATH_FOR_VERSION =
            EntityProxy.Concept.make("Path for version (SOLOR)", UUID.fromString("ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f"));
    /**
     * Java binding for the concept described as <strong><em>Path options for edit cordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2110c10c-9174-55aa-8ffe-91650c77d0b3">
     * 2110c10c-9174-55aa-8ffe-91650c77d0b3</a>}.
     */
    public static final EntityProxy.Concept PATH_OPTIONS_FOR_EDIT_CORDINATE =
            EntityProxy.Concept.make("Path options for edit cordinate (SOLOR)", UUID.fromString("2110c10c-9174-55aa-8ffe-91650c77d0b3"));
    /**
     * Java binding for the concept described as <strong><em>Path origins (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6e6a112e-7d8c-53c7-aaf1-c46e2d69743c">
     * 6e6a112e-7d8c-53c7-aaf1-c46e2d69743c</a>}.
     */
    public static final EntityProxy.Concept PATH_ORIGINS =
            EntityProxy.Concept.make("Path origins (SOLOR)", UUID.fromString("6e6a112e-7d8c-53c7-aaf1-c46e2d69743c"));
    /**
     * Java binding for the concept described as <strong><em>Module origins (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/462862d4-5df9-426e-b785-a1264e24769fc">
     * 462862d4-5df9-426e-b785-a1264e24769fc</a>}.
     */
    public static final EntityProxy.Concept MODULE_ORIGINS =
            EntityProxy.Concept.make("Module origins (SOLOR)", UUID.fromString("462862d4-5df9-426e-b785-a1264e24769f"));
    /**
     * Java binding for the concept described as <strong><em>Path origins for stamp path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f33e1668-34dd-53dd-8728-31b29934b482">
     * f33e1668-34dd-53dd-8728-31b29934b482</a>}.
     */
    public static final EntityProxy.Concept PATH_ORIGINS_FOR_STAMP_PATH =
            EntityProxy.Concept.make("Path origins for stamp path (SOLOR)", UUID.fromString("f33e1668-34dd-53dd-8728-31b29934b482"));
    /**
     * Java binding for the concept described as <strong><em>Path precedence (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fba850b7-de84-5df2-ab0b-d1caa6a358ff">
     * fba850b7-de84-5df2-ab0b-d1caa6a358ff</a>}.
     */
    public static final EntityProxy.Concept PATH_PRECEDENCE =
            EntityProxy.Concept.make("Path precedence (SOLOR)", UUID.fromString("fba850b7-de84-5df2-ab0b-d1caa6a358ff"));

    /**
     * Java binding for the concept described as <strong><em>Performance circumstance properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d2fd0659-1bde-50a3-830b-e64cbda0d0cc">
     * d2fd0659-1bde-50a3-830b-e64cbda0d0cc</a>}.
     */
    public static final EntityProxy.Concept PERFORMANCE_CIRCUMSTANCE_PROPERTIES =
            EntityProxy.Concept.make("Performance circumstance properties (SOLOR)", UUID.fromString("d2fd0659-1bde-50a3-830b-e64cbda0d0cc"));
    /**
     * Java binding for the concept described as <strong><em>Performance statement (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1c0d30a0-b7aa-5b2a-b295-4cd5c68ab4ec">
     * 1c0d30a0-b7aa-5b2a-b295-4cd5c68ab4ec</a>}.
     */
    public static final EntityProxy.Concept PERFORMANCE_STATEMENT =
            EntityProxy.Concept.make("Performance statement (SOLOR)", UUID.fromString("1c0d30a0-b7aa-5b2a-b295-4cd5c68ab4ec"));
    /**
     * Java binding for the concept described as <strong><em>Period duration (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7364cef5-5062-5d7e-9b34-c8bf32281c67">
     * 7364cef5-5062-5d7e-9b34-c8bf32281c67</a>}.
     */
    public static final EntityProxy.Concept PERIOD_DURATION =
            EntityProxy.Concept.make("Period duration (SOLOR)", UUID.fromString("7364cef5-5062-5d7e-9b34-c8bf32281c67"));
    /**
     * Java binding for the concept described as <strong><em>Period start (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5d5de213-b02f-53ae-b3ef-b7fb4de60aec">
     * 5d5de213-b02f-53ae-b3ef-b7fb4de60aec</a>}.
     */
    public static final EntityProxy.Concept PERIOD_START =
            EntityProxy.Concept.make("Period start (SOLOR)", UUID.fromString("5d5de213-b02f-53ae-b3ef-b7fb4de60aec"));
    /**
     * Java binding for the concept described as <strong><em>Persona instance name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/479b62fc-0ef5-5189-a0c4-78613ddfbfab">
     * 479b62fc-0ef5-5189-a0c4-78613ddfbfab</a>}.
     */
    public static final EntityProxy.Concept PERSONA_INSTANCE_NAME =
            EntityProxy.Concept.make("Persona instance name (SOLOR)", UUID.fromString("479b62fc-0ef5-5189-a0c4-78613ddfbfab"));
    /**
     * Java binding for the concept described as <strong><em>Persona name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/52d8c90d-6173-579a-8702-054e09721a9e">
     * 52d8c90d-6173-579a-8702-054e09721a9e</a>}.
     */
    public static final EntityProxy.Concept PERSONA_NAME =
            EntityProxy.Concept.make("Persona name (SOLOR)", UUID.fromString("52d8c90d-6173-579a-8702-054e09721a9e"));
    /**
     * Java binding for the concept described as <strong><em>Persona properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e07375c5-4992-56f9-acc8-6431b3cf6ed3">
     * e07375c5-4992-56f9-acc8-6431b3cf6ed3</a>}.
     */
    public static final EntityProxy.Concept PERSONA_PROPERTIES =
            EntityProxy.Concept.make("Persona properties (SOLOR)", UUID.fromString("e07375c5-4992-56f9-acc8-6431b3cf6ed3"));
    /**
     * Java binding for the concept described as <strong><em>Phenomenon (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c2e8bc47-3353-5e02-b0d1-2a5916efed4d">
     * c2e8bc47-3353-5e02-b0d1-2a5916efed4d</a>}.
     */
    public static final EntityProxy.Concept PHENOMENON =
            EntityProxy.Concept.make("Phenomenon (SOLOR)", UUID.fromString("c2e8bc47-3353-5e02-b0d1-2a5916efed4d"));
    /**
     * Java binding for the concept described as <strong><em>Polish dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/315cd879-1557-5a30-b325-a5d3df9e1c2b">
     * 315cd879-1557-5a30-b325-a5d3df9e1c2b</a>}.
     */
    public static final EntityProxy.Concept POLISH_DIALECT =
            EntityProxy.Concept.make("Polish dialect (SOLOR)", UUID.fromString("315cd879-1557-5a30-b325-a5d3df9e1c2b"));
    /**
     * Java binding for the concept described as <strong><em>Polish language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c924b887-da88-3a72-b8ea-fa86990467c9">
     * c924b887-da88-3a72-b8ea-fa86990467c9</a>}.
     */
    public static final EntityProxy.Concept POLISH_LANGUAGE =
            EntityProxy.Concept.make("Polish language (SOLOR)", UUID.fromString("c924b887-da88-3a72-b8ea-fa86990467c9"), UUID.fromString("45022140-9567-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>Polymorphic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3d634fd6-1498-5e8b-b914-e75b42018397">
     * 3d634fd6-1498-5e8b-b914-e75b42018397</a>}.
     */
    public static final EntityProxy.Concept POLYMORPHIC =
            EntityProxy.Concept.make("Polymorphic (SOLOR)", UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397"));
    /**
     * Java binding for the concept described as <strong><em>Polymorphic field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9c3e4a52-bfa8-5f42-8fb1-3681f5a58ecb">
     * 9c3e4a52-bfa8-5f42-8fb1-3681f5a58ecb</a>}.
     */
    public static final EntityProxy.Concept POLYMORPHIC_FIELD =
            EntityProxy.Concept.make("Polymorphic field (SOLOR)", UUID.fromString("9c3e4a52-bfa8-5f42-8fb1-3681f5a58ecb"));
    /**
     * Java binding for the concept described as <strong><em>Position on path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/31173582-a49d-51c6-813f-f42d0976aaea">
     * 31173582-a49d-51c6-813f-f42d0976aaea</a>}.
     */
    public static final EntityProxy.Concept POSITION_ON_PATH =
            EntityProxy.Concept.make("Position on path (SOLOR)", UUID.fromString("31173582-a49d-51c6-813f-f42d0976aaea"));
    /**
     * Java binding for the concept described as <strong><em>Precedence (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/da613def-d8dc-5079-bb9e-500e991efaca">
     * da613def-d8dc-5079-bb9e-500e991efaca</a>}.
     */
    public static final EntityProxy.Concept PRECEDENCE =
            EntityProxy.Concept.make("Precedence (SOLOR)", UUID.fromString("da613def-d8dc-5079-bb9e-500e991efaca"));
    /**
     * Java binding for the concept described as <strong><em>Preferred (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/266f1bc3-3361-39f3-bffe-69db9daea56e">
     * 266f1bc3-3361-39f3-bffe-69db9daea56e</a>}.
     */
    public static final EntityProxy.Concept PREFERRED =
            EntityProxy.Concept.make("Preferred (SOLOR)", UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
    /**
     * Java binding for the concept described as <strong><em>Preferred name for concept (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f882-579e-11e7-907b-a6006ad3dba0">
     * d9c1f882-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept PREFERRED_NAME_FOR_CONCEPT____QUERY_CLAUSE =
            EntityProxy.Concept.make("Preferred name for concept (query clause)", UUID.fromString("d9c1f882-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Premise type for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7df6c6cb-9f91-5515-a05b-6b2886938363">
     * 7df6c6cb-9f91-5515-a05b-6b2886938363</a>}.
     */
    public static final EntityProxy.Concept PREMISE_TYPE_FOR_MANIFOLD =
            EntityProxy.Concept.make("Premise type for manifold (SOLOR)", UUID.fromString("7df6c6cb-9f91-5515-a05b-6b2886938363"));
    /**
     * Java binding for the concept described as <strong><em>Premise type for taxonomy coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/58527f0b-b8b0-5c22-a37f-e4185f143c52">
     * 58527f0b-b8b0-5c22-a37f-e4185f143c52</a>}.
     */
    public static final EntityProxy.Concept PREMISE_TYPE_FOR_TAXONOMY_COORDINATE =
            EntityProxy.Concept.make("Premise type for taxonomy coordinate (SOLOR)", UUID.fromString("58527f0b-b8b0-5c22-a37f-e4185f143c52"));
    /**
     * Java binding for the concept described as <strong><em>Prescribable (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0f14ccba-f8aa-56a9-9b21-4005501ede4f">
     * 0f14ccba-f8aa-56a9-9b21-4005501ede4f</a>}.
     */
    public static final EntityProxy.Concept PRESCRIBABLE =
            EntityProxy.Concept.make("Prescribable (SOLOR)", UUID.fromString("0f14ccba-f8aa-56a9-9b21-4005501ede4f"));
    /**
     * Java binding for the concept described as <strong><em>Presentation unit different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e86d3887-717b-545f-b6b5-611210913b23">
     * e86d3887-717b-545f-b6b5-611210913b23</a>}.
     */
    public static final EntityProxy.Concept PRESENTATION_UNIT_DIFFERENT =
            EntityProxy.Concept.make("Presentation unit different (SOLOR)", UUID.fromString("e86d3887-717b-545f-b6b5-611210913b23"));
    /**
     * Java binding for the concept described as <strong><em>Pressure measure semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/62728a7c-7546-5f70-83c4-2859e08dd9be">
     * 62728a7c-7546-5f70-83c4-2859e08dd9be</a>}.
     */
    public static final EntityProxy.Concept PRESSURE_MEASURE_SEMANTIC =
            EntityProxy.Concept.make("Pressure measure semantic (SOLOR)", UUID.fromString("62728a7c-7546-5f70-83c4-2859e08dd9be"));
    /**
     * Java binding for the concept described as <strong><em>Primordial UUID for chronicle (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e0fcafbc-7191-5cdc-b14a-19d4d97f71bd">
     * e0fcafbc-7191-5cdc-b14a-19d4d97f71bd</a>}.
     */
    public static final EntityProxy.Concept PRIMORDIAL_UUID_FOR_CHRONICLE =
            EntityProxy.Concept.make("Primordial UUID for chronicle (SOLOR)", UUID.fromString("e0fcafbc-7191-5cdc-b14a-19d4d97f71bd"));
    /**
     * Java binding for the concept described as <strong><em>Primordial module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c2012321-3903-532e-8a5f-b13e4ca46e86">
     * c2012321-3903-532e-8a5f-b13e4ca46e86</a>}.
     */
    public static final EntityProxy.Concept PRIMORDIAL_MODULE =
            EntityProxy.Concept.make("Primordial module (SOLOR)", UUID.fromString("c2012321-3903-532e-8a5f-b13e4ca46e86"));
    /**
     * Java binding for the concept described as <strong><em>Primordial path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e95b6718-f824-5540-817b-8e79544eb97a">
     * e95b6718-f824-5540-817b-8e79544eb97a</a>}.
     */
    public static final EntityProxy.Concept PRIMORDIAL_PATH =
            EntityProxy.Concept.make("Primordial path (SOLOR)", UUID.fromString("e95b6718-f824-5540-817b-8e79544eb97a"));
    /**
     * Java binding for the concept described as <strong><em>Primordial status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b17bde5d-98ed-5416-97cf-2d837d75159d">
     * b17bde5d-98ed-5416-97cf-2d837d75159d</a>}.
     */
    public static final EntityProxy.Concept PRIMORDIAL_STATE =
            EntityProxy.Concept.make("Primordial state (SOLOR)", UUID.fromString("b17bde5d-98ed-5416-97cf-2d837d75159d"));
    /**
     * Java binding for the concept described as <strong><em>Priority (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/61c1f346-2103-3032-8066-2add812a5b74">
     * 61c1f346-2103-3032-8066-2add812a5b74</a>}.
     */
    public static final EntityProxy.Concept PRIORITY =
            EntityProxy.Concept.make("Priority (SOLOR)", UUID.fromString("61c1f346-2103-3032-8066-2add812a5b74"));
    /**
     * Java binding for the concept described as <strong><em>Procedure (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bfbced4b-ad7d-30aa-ae5c-f848ccebd45b">
     * bfbced4b-ad7d-30aa-ae5c-f848ccebd45b</a>}.
     */
    public static final EntityProxy.Concept PROCEDURE =
            EntityProxy.Concept.make("Procedure (SOLOR)", UUID.fromString("fe927a8b-bb07-5aa5-89c3-896fe3ce7e9c"));
    /**
     * Java binding for the concept described as <strong><em>Promotion destination path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/77c96285-89e5-5877-a65d-2e5fa3f008ce">
     * 77c96285-89e5-5877-a65d-2e5fa3f008ce</a>}.
     */
    public static final EntityProxy.Concept PROMOTION_DESTINATION_PATH =
            EntityProxy.Concept.make("Promotion destination path (SOLOR)", UUID.fromString("77c96285-89e5-5877-a65d-2e5fa3f008ce"));
    /**
     * Java binding for the concept described as <strong><em>Promotion path for edit cordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/db124d3b-c1bb-530e-8fd4-577f570355be">
     * db124d3b-c1bb-530e-8fd4-577f570355be</a>}.
     */
    public static final EntityProxy.Concept PROMOTION_PATH_FOR_EDIT_CORDINATE =
            EntityProxy.Concept.make("Promotion path for edit cordinate (SOLOR)", UUID.fromString("db124d3b-c1bb-530e-8fd4-577f570355be"));
    /**
     * Java binding for the concept described as <strong><em>Promotion source path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b542314d-1eeb-5cf0-ac4d-baa225a32675">
     * b542314d-1eeb-5cf0-ac4d-baa225a32675</a>}.
     */
    public static final EntityProxy.Concept PROMOTION_SOURCE_PATH =
            EntityProxy.Concept.make("Promotion source path (SOLOR)", UUID.fromString("b542314d-1eeb-5cf0-ac4d-baa225a32675"));
    /**
     * Java binding for the concept described as <strong><em>Property pattern implication (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e0de0d09-6e27-5738-bc8f-0fc94bb115fc">
     * e0de0d09-6e27-5738-bc8f-0fc94bb115fc</a>}.
     */
    public static final EntityProxy.Concept PROPERTY_PATTERN_IMPLICATION =
            EntityProxy.Concept.make("Property pattern implication (SOLOR)", UUID.fromString("e0de0d09-6e27-5738-bc8f-0fc94bb115fc"));
    /**
     * Java binding for the concept described as <strong><em>Property sequence (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d0d759fd-510f-475a-900e-b1439b4536e1">
     * d0d759fd-510f-475a-900e-b1439b4536e1</a>}.
     */
    public static final EntityProxy.Concept PROPERTY_SEQUENCE =
            EntityProxy.Concept.make("Property sequence (SOLOR)", UUID.fromString("d0d759fd-510f-475a-900e-b1439b4536e1"));
    /**
     * Java binding for the concept described as <strong><em>Property set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e273b5c0-c012-5e53-990c-aec5c2cb33a7">
     * e273b5c0-c012-5e53-990c-aec5c2cb33a7</a>}.
     */
    public static final EntityProxy.Concept PROPERTY_SET =
            EntityProxy.Concept.make("Property set (SOLOR)", UUID.fromString("e273b5c0-c012-5e53-990c-aec5c2cb33a7"));
    /**
     * Java binding for the concept described as <strong><em>Provider class assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8551edb2-6e34-52fe-9113-d25b742b303a">
     * 8551edb2-6e34-52fe-9113-d25b742b303a</a>}.
     */
    public static final EntityProxy.Concept PROVIDER_CLASS_ASSEMBLAGE =
            EntityProxy.Concept.make("Provider class assemblage (SOLOR)", UUID.fromString("8551edb2-6e34-52fe-9113-d25b742b303a"));
    /**
     * Java binding for the concept described as <strong><em>Purpose (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c3dffc48-6493-54df-a2f0-14be8ba03091">
     * c3dffc48-6493-54df-a2f0-14be8ba03091</a>}.
     */
    public static final EntityProxy.Concept PURPOSE =
            EntityProxy.Concept.make("Purpose (SOLOR)", UUID.fromString("c3dffc48-6493-54df-a2f0-14be8ba03091"));
    /**
     * Java binding for the concept described as <strong><em>Quality assurance rule assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8f6fbc92-afff-53cb-a43f-5b79c24066ce">
     * 8f6fbc92-afff-53cb-a43f-5b79c24066ce</a>}.
     */
    public static final EntityProxy.Concept QUALITY_ASSURANCE_RULE_ASSEMBLAGE =
            EntityProxy.Concept.make("Quality assurance rule assemblage (SOLOR)", UUID.fromString("8f6fbc92-afff-53cb-a43f-5b79c24066ce"));
    /**
     * Java binding for the concept described as <strong><em>Quality assurance rule issue (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/60eb511e-312a-5c7a-a34c-2ab260d1d9c3">
     * 60eb511e-312a-5c7a-a34c-2ab260d1d9c3</a>}.
     */
    public static final EntityProxy.Concept QUALITY_ASSURANCE_RULE_ISSUE =
            EntityProxy.Concept.make("Quality assurance rule issue (SOLOR)", UUID.fromString("60eb511e-312a-5c7a-a34c-2ab260d1d9c3"));
    /**
     * Java binding for the concept described as <strong><em>Quality assurance rule issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fd22d5ff-6d29-5b10-a307-d7d451f6e3ba">
     * fd22d5ff-6d29-5b10-a307-d7d451f6e3ba</a>}.
     */
    public static final EntityProxy.Concept QUALITY_ASSURANCE_RULE_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("Quality assurance rule issue assemblage (SOLOR)", UUID.fromString("fd22d5ff-6d29-5b10-a307-d7d451f6e3ba"));
    /**
     * Java binding for the concept described as <strong><em>Query clause parameters (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0878b177-7194-5736-8ae5-0684716845a6">
     * 0878b177-7194-5736-8ae5-0684716845a6</a>}.
     */
    public static final EntityProxy.Concept QUERY_CLAUSE_PARAMETERS =
            EntityProxy.Concept.make("Query clause parameters (SOLOR)", UUID.fromString("0878b177-7194-5736-8ae5-0684716845a6"));
    /**
     * Java binding for the concept described as <strong><em>Query clauses (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4905348c-ba1d-58ae-821f-52877d9acee3">
     * 4905348c-ba1d-58ae-821f-52877d9acee3</a>}.
     */
    public static final EntityProxy.Concept QUERY_CLAUSES =
            EntityProxy.Concept.make("Query clauses (SOLOR)", UUID.fromString("4905348c-ba1d-58ae-821f-52877d9acee3"));
    /**
     * Java binding for the concept described as <strong><em>Query string (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/96f32995-5268-560b-ab6d-605a34c22867">
     * 96f32995-5268-560b-ab6d-605a34c22867</a>}.
     */
    public static final EntityProxy.Concept QUERY_STRING =
            EntityProxy.Concept.make("Query string (SOLOR)", UUID.fromString("96f32995-5268-560b-ab6d-605a34c22867"));
    /**
     * Java binding for the concept described as <strong><em>Query string is regex (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/aba7815c-4b27-59e3-993a-cec1997eda35">
     * aba7815c-4b27-59e3-993a-cec1997eda35</a>}.
     */
    public static final EntityProxy.Concept QUERY_STRING_IS_REGEX =
            EntityProxy.Concept.make("Query string is regex (SOLOR)", UUID.fromString("aba7815c-4b27-59e3-993a-cec1997eda35"));
    /**
     * Java binding for the concept described as <strong><em>RF2 inferred relationship assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e3436c74-2491-50fa-b43c-13d83238648c">
     * e3436c74-2491-50fa-b43c-13d83238648c</a>}.
     */
    public static final EntityProxy.Concept RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE =
            EntityProxy.Concept.make("RF2 inferred relationship assemblage (SOLOR)", UUID.fromString("e3436c74-2491-50fa-b43c-13d83238648c"));
    /**
     * Java binding for the concept described as <strong><em>RF2 legacy relationship implication assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b7c0f2ee-5481-5122-8910-6d89543ff278">
     * b7c0f2ee-5481-5122-8910-6d89543ff278</a>}.
     */
    public static final EntityProxy.Concept RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE =
            EntityProxy.Concept.make("RF2 legacy relationship implication assemblage (SOLOR)", UUID.fromString("b7c0f2ee-5481-5122-8910-6d89543ff278"));
    /**
     * Java binding for the concept described as <strong><em>RF2 stated relationship assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c5c57241-e1c3-5c8b-85c6-0edffb28cfd0">
     * c5c57241-e1c3-5c8b-85c6-0edffb28cfd0</a>}.
     */
    public static final EntityProxy.Concept RF2_STATED_RELATIONSHIP_ASSEMBLAGE =
            EntityProxy.Concept.make("RF2 stated relationship assemblage (SOLOR)", UUID.fromString("c5c57241-e1c3-5c8b-85c6-0edffb28cfd0"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9d4f0198-4f84-5172-b78c-f47fd048c851">
     * 9d4f0198-4f84-5172-b78c-f47fd048c851</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is (query clause)", UUID.fromString("9d4f0198-4f84-5172-b78c-f47fd048c851"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is NOT active (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1fbdd3c7-efd1-59ec-a648-79582d0f8f1e">
     * 1fbdd3c7-efd1-59ec-a648-79582d0f8f1e</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_NOT_ACTIVE____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is NOT active (query clause)", UUID.fromString("1fbdd3c7-efd1-59ec-a648-79582d0f8f1e"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is NOT kind-of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/82ae0a41-8a1c-5cb5-8087-5562608b3373">
     * 82ae0a41-8a1c-5cb5-8087-5562608b3373</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_NOT_KIND_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is NOT kind-of (query clause)", UUID.fromString("82ae0a41-8a1c-5cb5-8087-5562608b3373"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is NOT member of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c92e5540-0d38-5f08-94e2-6ae673ee9c6b">
     * c92e5540-0d38-5f08-94e2-6ae673ee9c6b</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_NOT_MEMBER_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is NOT member of (query clause)", UUID.fromString("c92e5540-0d38-5f08-94e2-6ae673ee9c6b"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is active (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6f0f08f-d2db-5143-9466-cb60073879f3">
     * d6f0f08f-d2db-5143-9466-cb60073879f3</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_ACTIVE____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is active (query clause)", UUID.fromString("d6f0f08f-d2db-5143-9466-cb60073879f3"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is kind-of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/74ba168f-7932-5f3d-a8c5-28dc6d9fe647">
     * 74ba168f-7932-5f3d-a8c5-28dc6d9fe647</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_KIND_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is kind-of (query clause)", UUID.fromString("74ba168f-7932-5f3d-a8c5-28dc6d9fe647"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component is member of (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d39a3b5f-a862-5b52-93ed-0a7a0bbe329f">
     * d39a3b5f-a862-5b52-93ed-0a7a0bbe329f</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_IS_MEMBER_OF____QUERY_CLAUSE =
            EntityProxy.Concept.make("Referenced component is member of (query clause)", UUID.fromString("d39a3b5f-a862-5b52-93ed-0a7a0bbe329f"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component nid for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a9ba4749-c11f-5f35-a991-21796fb89ddc">
     * a9ba4749-c11f-5f35-a991-21796fb89ddc</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_NID_FOR_SEMANTIC =
            EntityProxy.Concept.make("Referenced component nid for semantic (SOLOR)", UUID.fromString("a9ba4749-c11f-5f35-a991-21796fb89ddc"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component subtype restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8af1045e-1122-5072-9f29-ce7da9337915">
     * 8af1045e-1122-5072-9f29-ce7da9337915</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_SUBTYPE_RESTRICTION =
            EntityProxy.Concept.make("Referenced component subtype restriction (SOLOR)", UUID.fromString("8af1045e-1122-5072-9f29-ce7da9337915"));
    /**
     * Java binding for the concept described as <strong><em>Referenced component type restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/902f97b6-2ef4-59d7-b6f9-01278a00061c">
     * 902f97b6-2ef4-59d7-b6f9-01278a00061c</a>}.
     */
    public static final EntityProxy.Concept REFERENCED_COMPONENT_TYPE_RESTRICTION =
            EntityProxy.Concept.make("Referenced component type restriction (SOLOR)", UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"));
    /**
     * Java binding for the concept described as <strong><em>Reflection class assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/43f5bdcb-c902-5ea2-9ed7-2572fa468bae">
     * 43f5bdcb-c902-5ea2-9ed7-2572fa468bae</a>}.
     */
    public static final EntityProxy.Concept REFLECTION_CLASS_ASSEMBLAGE =
            EntityProxy.Concept.make("Reflection class assemblage (SOLOR)", UUID.fromString("43f5bdcb-c902-5ea2-9ed7-2572fa468bae"));
    /**
     * Java binding for the concept described as <strong><em>Reflexive feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7e779e4a-61ed-5c4a-aacc-03cf524b7c73">
     * 7e779e4a-61ed-5c4a-aacc-03cf524b7c73</a>}.
     */
    public static final EntityProxy.Concept REFLEXIVE_PROPERTY =
            EntityProxy.Concept.make("Reflexive property (SOLOR)", UUID.fromString("7e779e4a-61ed-5c4a-aacc-03cf524b7c73"));
    /**
     * Java binding for the concept described as <strong><em>Regular name description type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8bfba944-3965-3946-9bcb-1e80a5da63a2">
     * 8bfba944-3965-3946-9bcb-1e80a5da63a2</a>}.
     */
    public static final EntityProxy.Concept REGULAR_NAME_DESCRIPTION_TYPE =
            EntityProxy.Concept.make("Regular name description type (SOLOR)", UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"), UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));
    /**
     * Java binding for the concept described as <strong><em>Relationship destination (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a3dd69af-355c-54ce-ba13-2902a7ae9551">
     * a3dd69af-355c-54ce-ba13-2902a7ae9551</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_DESTINATION =
            EntityProxy.Concept.make("Relationship destination (SOLOR)", UUID.fromString("a3dd69af-355c-54ce-ba13-2902a7ae9551"));
    /**
     * Java binding for the concept described as <strong><em>Relationship group for rf2 relationship (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d5a3cc0d-4483-5da4-b205-c7ca265d1667">
     * d5a3cc0d-4483-5da4-b205-c7ca265d1667</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_GROUP_FOR_RF2_RELATIONSHIP =
            EntityProxy.Concept.make("Relationship group for rf2 relationship (SOLOR)", UUID.fromString("d5a3cc0d-4483-5da4-b205-c7ca265d1667"));
    /**
     * Java binding for the concept described as <strong><em>Relationship is circular (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1fcec-579e-11e7-907b-a6006ad3dba0">
     * d9c1fcec-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_IS_CIRCULAR____QUERY_CLAUSE =
            EntityProxy.Concept.make("Relationship is circular (query clause)", UUID.fromString("d9c1fcec-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Relationship origin (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ad22d43b-3ee7-550b-9660-a6e68af347c2">
     * ad22d43b-3ee7-550b-9660-a6e68af347c2</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_ORIGIN =
            EntityProxy.Concept.make("Relationship origin (SOLOR)", UUID.fromString("ad22d43b-3ee7-550b-9660-a6e68af347c2"));
    /**
     * Java binding for the concept described as <strong><em>Relationship restriction (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c20c0a-579e-11e7-907b-a6006ad3dba0">
     * d9c20c0a-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_RESTRICTION____QUERY_CLAUSE =
            EntityProxy.Concept.make("Relationship restriction (query clause)", UUID.fromString("d9c20c0a-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Relationship type (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c211be-579e-11e7-907b-a6006ad3dba0">
     * d9c211be-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_TYPE____QUERY_CLAUSE =
            EntityProxy.Concept.make("Relationship type (query clause)", UUID.fromString("d9c211be-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>Relationship type in source terminology (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/46bc0e6b-0e64-5aa6-af27-a823e9156dfc">
     * 46bc0e6b-0e64-5aa6-af27-a823e9156dfc</a>}.
     */
    public static final EntityProxy.Concept RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY =
            EntityProxy.Concept.make("Relationship type in source terminology (SOLOR)", UUID.fromString("46bc0e6b-0e64-5aa6-af27-a823e9156dfc"));
    /**
     * Java binding for the concept described as <strong><em>Repetition properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/29220101-eda8-51b8-9cd6-8c5e36f1606a">
     * 29220101-eda8-51b8-9cd6-8c5e36f1606a</a>}.
     */
    public static final EntityProxy.Concept REPETITION_PROPERTIES =
            EntityProxy.Concept.make("Repetition properties (SOLOR)", UUID.fromString("29220101-eda8-51b8-9cd6-8c5e36f1606a"));
    /**
     * Java binding for the concept described as <strong><em>Repetitions (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2ba18dc3-fd5c-55da-8a81-5d7aa8bf2d64">
     * 2ba18dc3-fd5c-55da-8a81-5d7aa8bf2d64</a>}.
     */
    public static final EntityProxy.Concept REPETITIONS =
            EntityProxy.Concept.make("Repetitions (SOLOR)", UUID.fromString("2ba18dc3-fd5c-55da-8a81-5d7aa8bf2d64"));
    /**
     * Java binding for the concept described as <strong><em>Replacement concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/68bc0528-ab9b-5c20-98f2-bb0152bbd2f5">
     * 68bc0528-ab9b-5c20-98f2-bb0152bbd2f5</a>}.
     */
    public static final EntityProxy.Concept REPLACEMENT_CONCEPT =
            EntityProxy.Concept.make("Replacement concept (SOLOR)", UUID.fromString("68bc0528-ab9b-5c20-98f2-bb0152bbd2f5"));
    /**
     * Java binding for the concept described as <strong><em>Represents association (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5252bafb-1ba7-5a35-b1a2-48d7a65fa477">
     * 5252bafb-1ba7-5a35-b1a2-48d7a65fa477</a>}.
     */
    public static final EntityProxy.Concept REPRESENTS_ASSOCIATION =
            EntityProxy.Concept.make("Represents association (SOLOR)", UUID.fromString("5252bafb-1ba7-5a35-b1a2-48d7a65fa477"));
    /**
     * Java binding for the concept described as <strong><em>Request Priority (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cd28f877-efad-5c1c-94f7-183db2678e0f">
     * cd28f877-efad-5c1c-94f7-183db2678e0f</a>}.
     */
    public static final EntityProxy.Concept REQUEST_PRIORITY =
            EntityProxy.Concept.make("Request Priority (SOLOR)", UUID.fromString("cd28f877-efad-5c1c-94f7-183db2678e0f"));
    /**
     * Java binding for the concept described as <strong><em>Request circumstance properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4731162b-95fd-5913-b366-027e6fda9202">
     * 4731162b-95fd-5913-b366-027e6fda9202</a>}.
     */
    public static final EntityProxy.Concept REQUEST_CIRCUMSTANCE_PROPERTIES =
            EntityProxy.Concept.make("Request circumstance properties (SOLOR)", UUID.fromString("4731162b-95fd-5913-b366-027e6fda9202"));
    /**
     * Java binding for the concept described as <strong><em>Request statement (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2e2a53ac-a1bc-5eca-bef5-56fa31ee7ea7">
     * 2e2a53ac-a1bc-5eca-bef5-56fa31ee7ea7</a>}.
     */
    public static final EntityProxy.Concept REQUEST_STATEMENT =
            EntityProxy.Concept.make("Request statement (SOLOR)", UUID.fromString("2e2a53ac-a1bc-5eca-bef5-56fa31ee7ea7"));
    /**
     * Java binding for the concept described as <strong><em>Requested participants (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7ebf4ff2-6856-5c75-8f6f-4220c0ddc4a8">
     * 7ebf4ff2-6856-5c75-8f6f-4220c0ddc4a8</a>}.
     */
    public static final EntityProxy.Concept REQUESTED_PARTICIPANTS =
            EntityProxy.Concept.make("Requested participants (SOLOR)", UUID.fromString("7ebf4ff2-6856-5c75-8f6f-4220c0ddc4a8"));
    /**
     * Java binding for the concept described as <strong><em>Requested result (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6333e18b-f20c-55f8-b3f7-9458db12ad3f">
     * 6333e18b-f20c-55f8-b3f7-9458db12ad3f</a>}.
     */
    public static final EntityProxy.Concept REQUESTED_RESULT =
            EntityProxy.Concept.make("Requested result (SOLOR)", UUID.fromString("6333e18b-f20c-55f8-b3f7-9458db12ad3f"));
    /**
     * Java binding for the concept described as <strong><em>Resolution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d998c36f-d438-5aa2-b479-e125c6698f91">
     * d998c36f-d438-5aa2-b479-e125c6698f91</a>}.
     */
    public static final EntityProxy.Concept RESOLUTION =
            EntityProxy.Concept.make("Resolution (SOLOR)", UUID.fromString("d998c36f-d438-5aa2-b479-e125c6698f91"));
    /**
     * Java binding for the concept described as <strong><em>Result (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/664641ed-6ac7-50d2-8aa4-19257ed30aae">
     * 664641ed-6ac7-50d2-8aa4-19257ed30aae</a>}.
     */
    public static final EntityProxy.Concept RESULT =
            EntityProxy.Concept.make("Result (SOLOR)", UUID.fromString("664641ed-6ac7-50d2-8aa4-19257ed30aae"));
    /**
     * Java binding for the concept described as <strong><em>Result properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9d5c921d-53e3-5e1f-bc55-5d0ffefb5da4">
     * 9d5c921d-53e3-5e1f-bc55-5d0ffefb5da4</a>}.
     */
    public static final EntityProxy.Concept RESULT_PROPERTIES =
            EntityProxy.Concept.make("Result properties (SOLOR)", UUID.fromString("9d5c921d-53e3-5e1f-bc55-5d0ffefb5da4"));
    /**
     * Java binding for the concept described as <strong><em>Right pane defaults (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/58b4b6d2-1a91-51d0-8e3f-7476d60e6888">
     * 58b4b6d2-1a91-51d0-8e3f-7476d60e6888</a>}.
     */
    public static final EntityProxy.Concept RIGHT_PANE_DEFAULTS =
            EntityProxy.Concept.make("Right pane defaults (SOLOR)", UUID.fromString("58b4b6d2-1a91-51d0-8e3f-7476d60e6888"));
    /**
     * Java binding for the concept described as <strong><em>Right pane options (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dfdb03ee-0679-5382-bee4-5ff904c097b3">
     * dfdb03ee-0679-5382-bee4-5ff904c097b3</a>}.
     */
    public static final EntityProxy.Concept RIGHT_PANE_OPTIONS =
            EntityProxy.Concept.make("Right pane options (SOLOR)", UUID.fromString("dfdb03ee-0679-5382-bee4-5ff904c097b3"));
    /**
     * Java binding for the concept described as <strong><em>Right tab nodes (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/760bff67-ac0b-5343-9689-c52fb15e8746">
     * 760bff67-ac0b-5343-9689-c52fb15e8746</a>}.
     */
    public static final EntityProxy.Concept RIGHT_TAB_NODES =
            EntityProxy.Concept.make("Right tab nodes (SOLOR)", UUID.fromString("760bff67-ac0b-5343-9689-c52fb15e8746"));
    /**
     * Java binding for the concept described as <strong><em>Role (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6155818b-09ed-388e-82ce-caa143423e99">
     * 6155818b-09ed-388e-82ce-caa143423e99</a>}.
     */
    public static final EntityProxy.Concept ROLE =
            EntityProxy.Concept.make("Role (SOLOR)", UUID.fromString("46ae9325-dd24-5008-8fda-80cf1f0977c7"));
    /**
     * Java binding for the concept described as <strong><em>Role group (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a63f4bf2-a040-11e5-8994-feff819cdc9f">
     * a63f4bf2-a040-11e5-8994-feff819cdc9f</a>}.
     */
    public static final EntityProxy.Concept ROLE_GROUP =
            EntityProxy.Concept.make("Role group (SOLOR)", UUID.fromString("a63f4bf2-a040-11e5-8994-feff819cdc9f"), UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"), UUID.fromString("f97abff6-a221-57a1-9cd6-e79e723bfe2a"));
    /**
     * Java binding for the concept described as <strong><em>Role operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f9860cb8-a7c7-5743-9d7c-ffc6e8a24a0f">
     * f9860cb8-a7c7-5743-9d7c-ffc6e8a24a0f</a>}.
     */
    public static final EntityProxy.Concept ROLE_OPERATOR =
            EntityProxy.Concept.make("Role operator (SOLOR)", UUID.fromString("f9860cb8-a7c7-5743-9d7c-ffc6e8a24a0f"));
    /**
     * Java binding for the concept described as <strong><em>Role restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/988bb02a-9b4a-4ef9-937e-fa8a6afc6c42">
     * 988bb02a-9b4a-4ef9-937e-fa8a6afc6c42</a>}.
     */
    public static final EntityProxy.Concept ROLE_RESTRICTION =
            EntityProxy.Concept.make("Role restriction (SOLOR)", UUID.fromString("988bb02a-9b4a-4ef9-937e-fa8a6afc6c42"));
    /**
     * Java binding for the concept described as <strong><em>Role type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/76320274-be2a-5ba0-b3e8-e6d2e383ee6a">
     * 76320274-be2a-5ba0-b3e8-e6d2e383ee6a</a>}.
     */
    public static final EntityProxy.Concept ROLE_TYPE =
            EntityProxy.Concept.make("Role type (SOLOR)", UUID.fromString("76320274-be2a-5ba0-b3e8-e6d2e383ee6a"));
    /**
     * Java binding for the concept described as <strong><em>Role type to add (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0c3ca846-0374-5a5c-8da4-67e0e2e28868">
     * 0c3ca846-0374-5a5c-8da4-67e0e2e28868</a>}.
     */
    public static final EntityProxy.Concept ROLE_TYPE_TO_ADD =
            EntityProxy.Concept.make("Role type to add (SOLOR)", UUID.fromString("0c3ca846-0374-5a5c-8da4-67e0e2e28868"));
    /**
     * Java binding for the concept described as <strong><em>Root for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/862cc189-bbcb-51a0-89a4-16e1854be247">
     * 862cc189-bbcb-51a0-89a4-16e1854be247</a>}.
     */
    public static final EntityProxy.Concept ROOT_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Root for logic coordinate (SOLOR)", UUID.fromString("862cc189-bbcb-51a0-89a4-16e1854be247"));
    /**
     * Java binding for the concept described as <strong><em>Routine (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/90581618-c1c5-3e6e-ab21-80b18ded492c">
     * 90581618-c1c5-3e6e-ab21-80b18ded492c</a>}.
     */
    public static final EntityProxy.Concept ROUTINE =
            EntityProxy.Concept.make("Routine (SOLOR)", UUID.fromString("90581618-c1c5-3e6e-ab21-80b18ded492c"));
    /**
     * Java binding for the concept described as <strong><em>Rule assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9d6de877-5315-59bb-8148-bcfc0dac5e45">
     * 9d6de877-5315-59bb-8148-bcfc0dac5e45</a>}.
     */
    public static final EntityProxy.Concept RULE_ASSEMBLAGE =
            EntityProxy.Concept.make("Rule assemblage (SOLOR)", UUID.fromString("9d6de877-5315-59bb-8148-bcfc0dac5e45"));
    /**
     * Java binding for the concept described as <strong><em>Russian dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/300126d1-2604-579f-a59b-e3c1179a173a">
     * 300126d1-2604-579f-a59b-e3c1179a173a</a>}.
     */
    public static final EntityProxy.Concept RUSSIAN_DIALECT =
            EntityProxy.Concept.make("Russian dialect (SOLOR)", UUID.fromString("300126d1-2604-579f-a59b-e3c1179a173a"));
    /**
     * Java binding for the concept described as <strong><em>Russian language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0818dbb7-3fe1-59d7-99c2-c8dc42ff2cce">
     * 0818dbb7-3fe1-59d7-99c2-c8dc42ff2cce</a>}.
     */
    public static final EntityProxy.Concept RUSSIAN_LANGUAGE =
            EntityProxy.Concept.make("Russian language (SOLOR)", UUID.fromString("0818dbb7-3fe1-59d7-99c2-c8dc42ff2cce"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm Assemblages (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3cf11353-7357-54f9-a8b8-0ef6b6c16fef">
     * 3cf11353-7357-54f9-a8b8-0ef6b6c16fef</a>}.
     */
    public static final EntityProxy.Concept RXNORM_ASSEMBLAGES =
            EntityProxy.Concept.make("RxNorm Assemblages (SOLOR)", UUID.fromString("3cf11353-7357-54f9-a8b8-0ef6b6c16fef"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm Asserted (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/18c08ad6-cfe1-5e61-bea4-ab5b0f91a59f">
     * 18c08ad6-cfe1-5e61-bea4-ab5b0f91a59f</a>}.
     */
    public static final EntityProxy.Concept RXNORM_ASSERTED =
            EntityProxy.Concept.make("RxNorm Asserted (SOLOR)", UUID.fromString("18c08ad6-cfe1-5e61-bea4-ab5b0f91a59f"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm CUI (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/492b1a88-dbce-56a0-a405-6c7742f3be86">
     * 492b1a88-dbce-56a0-a405-6c7742f3be86</a>}.
     */
    public static final EntityProxy.Concept RXNORM_CUI =
            EntityProxy.Concept.make("RxNorm CUI (SOLOR)", UUID.fromString("492b1a88-dbce-56a0-a405-6c7742f3be86"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm concept assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/914cd34d-c97a-5fc5-abac-53bfb161eca0">
     * 914cd34d-c97a-5fc5-abac-53bfb161eca0</a>}.
     */
    public static final EntityProxy.Concept RXNORM_CONCEPT_ASSEMBLAGE =
            EntityProxy.Concept.make("RxNorm concept assemblage (SOLOR)", UUID.fromString("914cd34d-c97a-5fc5-abac-53bfb161eca0"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm inferred (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7458c0f2-3aab-53f7-bcd5-7cb3310c69f6">
     * 7458c0f2-3aab-53f7-bcd5-7cb3310c69f6</a>}.
     */
    public static final EntityProxy.Concept RXNORM_INFERRED =
            EntityProxy.Concept.make("RxNorm inferred (SOLOR)", UUID.fromString("7458c0f2-3aab-53f7-bcd5-7cb3310c69f6"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5225140a-9c43-554a-95bb-3b705d27ad7b">
     * 5225140a-9c43-554a-95bb-3b705d27ad7b</a>}.
     */
    public static final EntityProxy.Concept RXNORM_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("RxNorm issue assemblage (SOLOR)", UUID.fromString("5225140a-9c43-554a-95bb-3b705d27ad7b"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9ca299bb-61b8-5aaa-b1c1-131600067947">
     * 9ca299bb-61b8-5aaa-b1c1-131600067947</a>}.
     */
    public static final EntityProxy.Concept RXNORM_LICENSE =
            EntityProxy.Concept.make("RxNorm license (SOLOR)", UUID.fromString("9ca299bb-61b8-5aaa-b1c1-131600067947"));
    /**
     * Java binding for the concept described as <strong><em>RxNorm modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f2b37ab2-7a97-555c-8f9a-aca843185c93">
     * f2b37ab2-7a97-555c-8f9a-aca843185c93</a>}.
     */
    public static final EntityProxy.Concept RXNORM_MODULES =
            EntityProxy.Concept.make("RxNorm modules (SOLOR)", UUID.fromString("f2b37ab2-7a97-555c-8f9a-aca843185c93"));
    /**
     * Java binding for the concept described as <strong><em>SCTID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0418a591-f75b-39ad-be2c-3ab849326da9">
     * 0418a591-f75b-39ad-be2c-3ab849326da9</a>}.
     */
    public static final EntityProxy.Concept SCTID =
            EntityProxy.Concept.make("SCTID (SOLOR)", UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"), UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9"), UUID.fromString("ab9a0e0a-6359-5462-859c-96c3d4ef2341"));
    /**
     * Java binding for the concept described as <strong><em>SH profile (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3de219ce-b5f3-5ed7-b65f-4a0ee8418add">
     * 3de219ce-b5f3-5ed7-b65f-4a0ee8418add</a>}.
     */
    public static final EntityProxy.Concept SH_PROFILE =
            EntityProxy.Concept.make("SH profile (SOLOR)", UUID.fromString("3de219ce-b5f3-5ed7-b65f-4a0ee8418add"));
    /**
     * Java binding for the concept described as <strong><em>SKOS alternate label (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3a6e4722-7fe5-5a13-b82a-fcdbc44a0991">
     * 3a6e4722-7fe5-5a13-b82a-fcdbc44a0991</a>}.
     */
    public static final EntityProxy.Concept SKOS_ALTERNATE_LABEL =
            EntityProxy.Concept.make("SKOS alternate label (SOLOR)", UUID.fromString("3a6e4722-7fe5-5a13-b82a-fcdbc44a0991"));
    /**
     * Java binding for the concept described as <strong><em>SKOS definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d30465d9-5086-5e9c-97be-729fd0f2f3ef">
     * d30465d9-5086-5e9c-97be-729fd0f2f3ef</a>}.
     */
    public static final EntityProxy.Concept SKOS_DEFINITION =
            EntityProxy.Concept.make("SKOS definition (SOLOR)", UUID.fromString("d30465d9-5086-5e9c-97be-729fd0f2f3ef"));
    /**
     * Java binding for the concept described as <strong><em>SKOS preferred label (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/330f64c0-a68e-5fde-8c0a-4227cf0d68ad">
     * 330f64c0-a68e-5fde-8c0a-4227cf0d68ad</a>}.
     */
    public static final EntityProxy.Concept SKOS_PREFERRED_LABEL =
            EntityProxy.Concept.make("SKOS preferred label (SOLOR)", UUID.fromString("330f64c0-a68e-5fde-8c0a-4227cf0d68ad"));
    /**
     * Java binding for the concept described as <strong><em>SNOMED CT® core modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1b4f1ba5-b725-390f-8c3b-33ec7096bdca">
     * 1b4f1ba5-b725-390f-8c3b-33ec7096bdca</a>}.
     */
    public static final EntityProxy.Concept SNOMED_CT_CORE_MODULES =
            EntityProxy.Concept.make("SNOMED CT® core modules (SOLOR)", UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
    /**
     * Java binding for the concept described as <strong><em>SNOMED® affiliates license (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4e7d8a63-1f36-56bb-9217-daa6da1b47e7">
     * 4e7d8a63-1f36-56bb-9217-daa6da1b47e7</a>}.
     */
    public static final EntityProxy.Concept SNOMED_AFFILIATES_LICENSE =
            EntityProxy.Concept.make("SNOMED® affiliates license (SOLOR)", UUID.fromString("4e7d8a63-1f36-56bb-9217-daa6da1b47e7"));
    /**
     * Java binding for the concept described as <strong><em>SNOMED® issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/436534b0-cc66-55e5-8ba0-22615d6df74b">
     * 436534b0-cc66-55e5-8ba0-22615d6df74b</a>}.
     */
    public static final EntityProxy.Concept SNOMED_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("SNOMED® issue assemblage (SOLOR)", UUID.fromString("436534b0-cc66-55e5-8ba0-22615d6df74b"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR automation rule module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/421186fb-3f11-5547-9363-b16caa97891e">
     * 421186fb-3f11-5547-9363-b16caa97891e</a>}.
     */
    public static final EntityProxy.Concept SOLOR_AUTOMATION_RULE_MODULE =
            EntityProxy.Concept.make("SOLOR automation rule module (SOLOR)", UUID.fromString("421186fb-3f11-5547-9363-b16caa97891e"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7c21b6c5-cf11-5af9-893b-743f004c97f5">
     * 7c21b6c5-cf11-5af9-893b-743f004c97f5</a>}.
     */
    public static final EntityProxy.Concept SOLOR_CONCEPT =
            EntityProxy.Concept.make("SOLOR concept (SOLOR)", UUID.fromString("7c21b6c5-cf11-5af9-893b-743f004c97f5"));

    /**
     * Java binding for the concept described as <strong><em>SOLOR issue assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ca5800e8-02ec-5145-86dc-5c7eccb6088a">
     * ca5800e8-02ec-5145-86dc-5c7eccb6088a</a>}.
     */
    public static final EntityProxy.Concept SOLOR_ISSUE_ASSEMBLAGE =
            EntityProxy.Concept.make("SOLOR issue assemblage (SOLOR)", UUID.fromString("ca5800e8-02ec-5145-86dc-5c7eccb6088a"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f680c868-f7e5-5d0e-91f2-615eca8f8fd2">
     * f680c868-f7e5-5d0e-91f2-615eca8f8fd2</a>}.
     */
    public static final EntityProxy.Concept SOLOR_MODULE =
            EntityProxy.Concept.make("SOLOR module (SOLOR)", UUID.fromString("f680c868-f7e5-5d0e-91f2-615eca8f8fd2"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR overlay module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9ecc154c-e490-5cf8-805d-d2865d62aef3">
     * 9ecc154c-e490-5cf8-805d-d2865d62aef3</a>}.
     */
    public static final EntityProxy.Concept SOLOR_OVERLAY_MODULE =
            EntityProxy.Concept.make("SOLOR overlay module (SOLOR)", UUID.fromString("9ecc154c-e490-5cf8-805d-d2865d62aef3"), UUID.fromString("1f2016a6-960e-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR quality assurance rule module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5dd5d129-fda2-5295-aee7-426bd58fb438">
     * 5dd5d129-fda2-5295-aee7-426bd58fb438</a>}.
     */
    public static final EntityProxy.Concept SOLOR_QUALITY_ASSURANCE_RULE_MODULE =
            EntityProxy.Concept.make("SOLOR quality assurance rule module (SOLOR)", UUID.fromString("5dd5d129-fda2-5295-aee7-426bd58fb438"));
    /**
     * Java binding for the concept described as <strong><em>SOLOR temporary concept module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7abc2bf8-2894-587d-b02b-e6e613dbd3f2">
     * 7abc2bf8-2894-587d-b02b-e6e613dbd3f2</a>}.
     */
    public static final EntityProxy.Concept SOLOR_TEMPORARY_CONCEPT_MODULE =
            EntityProxy.Concept.make("SOLOR temporary concept module (SOLOR)", UUID.fromString("7abc2bf8-2894-587d-b02b-e6e613dbd3f2"));
    /**
     * Java binding for the concept described as <strong><em>SOPT modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b5aa2432-eb4e-5a23-a1ad-9df5663d4fe9">
     * b5aa2432-eb4e-5a23-a1ad-9df5663d4fe9</a>}.
     */
    public static final EntityProxy.Concept SOPT_MODULES =
            EntityProxy.Concept.make("SOPT modules (SOLOR)", UUID.fromString("b5aa2432-eb4e-5a23-a1ad-9df5663d4fe9"));
    /**
     * Java binding for the concept described as <strong><em>SRF inferred relationship assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b218d92d-df8e-52d2-9214-c84a64862a23">
     * b218d92d-df8e-52d2-9214-c84a64862a23</a>}.
     */
    public static final EntityProxy.Concept SRF_INFERRED_RELATIONSHIP_ASSEMBLAGE =
            EntityProxy.Concept.make("SRF inferred relationship assemblage (SOLOR)", UUID.fromString("b218d92d-df8e-52d2-9214-c84a64862a23"));
    /**
     * Java binding for the concept described as <strong><em>SRF legacy relationship implication assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d16114d4-5df9-58d9-bafd-216bf336cf18">
     * d16114d4-5df9-58d9-bafd-216bf336cf18</a>}.
     */
    public static final EntityProxy.Concept SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE =
            EntityProxy.Concept.make("SRF legacy relationship implication assemblage (SOLOR)", UUID.fromString("d16114d4-5df9-58d9-bafd-216bf336cf18"));
    /**
     * Java binding for the concept described as <strong><em>SRF stated relationship assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c2b97bad-d5e0-5eb5-ba13-b9e6824411e0">
     * c2b97bad-d5e0-5eb5-ba13-b9e6824411e0</a>}.
     */
    public static final EntityProxy.Concept SRF_STATED_RELATIONSHIP_ASSEMBLAGE =
            EntityProxy.Concept.make("SRF stated relationship assemblage (SOLOR)", UUID.fromString("c2b97bad-d5e0-5eb5-ba13-b9e6824411e0"));
    /**
     * Java binding for the concept described as <strong><em>Sandbox component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c93829b2-aa78-5a84-ac9a-c34307844166">
     * c93829b2-aa78-5a84-ac9a-c34307844166</a>}.
     */
    public static final EntityProxy.Concept SANDBOX_COMPONENT =
            EntityProxy.Concept.make("Sandbox component (SOLOR)", UUID.fromString("c93829b2-aa78-5a84-ac9a-c34307844166"));
    /**
     * Java binding for the concept described as <strong><em>Sandbox module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95">
     * c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95</a>}.
     */
    public static final EntityProxy.Concept SANDBOX_MODULE =
            EntityProxy.Concept.make("Sandbox module (SOLOR)", UUID.fromString("c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95"));
    /**
     * Java binding for the concept described as <strong><em>Sandbox path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/80710ea6-983c-5fa0-8908-e479f1f03ea9">
     * 80710ea6-983c-5fa0-8908-e479f1f03ea9</a>}.
     */
    public static final EntityProxy.Concept SANDBOX_PATH =
            EntityProxy.Concept.make("Sandbox path (SOLOR)", UUID.fromString("80710ea6-983c-5fa0-8908-e479f1f03ea9"));
    /**
     * Java binding for the concept described as <strong><em>Sandbox path module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/715bd36d-6090-5b37-8ae7-88c9e532010e">
     * 715bd36d-6090-5b37-8ae7-88c9e532010e</a>}.
     */
    public static final EntityProxy.Concept SANDBOX_PATH_MODULE =
            EntityProxy.Concept.make("Sandbox path module (SOLOR)", UUID.fromString("715bd36d-6090-5b37-8ae7-88c9e532010e"));
    /**
     * Java binding for the concept described as <strong><em>Semantic field concepts (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b4316cb8-14fe-5b32-b03b-f5f966c87819">
     * b4316cb8-14fe-5b32-b03b-f5f966c87819</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_FIELD_CONCEPTS =
            EntityProxy.Concept.make("Semantic field concepts (SOLOR)", UUID.fromString("b4316cb8-14fe-5b32-b03b-f5f966c87819"));
    /**
     * Java binding for the concept described as <strong><em>Semantic field data types assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2fc4663f-c389-590e-9445-df02e277ddb1">
     * 2fc4663f-c389-590e-9445-df02e277ddb1</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE =
            EntityProxy.Concept.make("Semantic field data types assemblage (SOLOR)", UUID.fromString("2fc4663f-c389-590e-9445-df02e277ddb1"));
    /**
     * Java binding for the concept described as <strong><em>Semantic field name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/15489c68-673d-503e-bff7-e9d59e5dc15c">
     * 15489c68-673d-503e-bff7-e9d59e5dc15c</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_FIELD_NAME =
            EntityProxy.Concept.make("Semantic field name (SOLOR)", UUID.fromString("15489c68-673d-503e-bff7-e9d59e5dc15c"));
    /**
     * Java binding for the concept described as <strong><em>Semantic field type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9c3dfc88-51e4-5e51-a59a-88dd580162b7">
     * 9c3dfc88-51e4-5e51-a59a-88dd580162b7</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_FIELD_TYPE =
            EntityProxy.Concept.make("Semantic field type (SOLOR)", UUID.fromString("9c3dfc88-51e4-5e51-a59a-88dd580162b7"));
    /**
     * Java binding for the concept described as <strong><em>Semantic fields assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ac6d947d-384e-5293-a3b8-5f0c318ee0f7">
     * ac6d947d-384e-5293-a3b8-5f0c318ee0f7</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_FIELDS_ASSEMBLAGE =
            EntityProxy.Concept.make("Semantic fields assemblage (SOLOR)", UUID.fromString("ac6d947d-384e-5293-a3b8-5f0c318ee0f7"));
    /**
     * Java binding for the concept described as <strong><em>Semantic properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b717ae48-5488-5dda-a980-97855001cc99">
     * b717ae48-5488-5dda-a980-97855001cc99</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_PROPERTIES =
            EntityProxy.Concept.make("Semantic properties (SOLOR)", UUID.fromString("b717ae48-5488-5dda-a980-97855001cc99"));
    /**
     * Java binding for the concept described as <strong><em>Semantic tree table panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/54ef2e1d-ddb3-5980-bf1a-156dbeefd4c6">
     * 54ef2e1d-ddb3-5980-bf1a-156dbeefd4c6</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_TREE_TABLE_PANEL =
            EntityProxy.Concept.make("Semantic tree table panel (SOLOR)", UUID.fromString("54ef2e1d-ddb3-5980-bf1a-156dbeefd4c6"));
    /**
     * Java binding for the concept described as <strong><em>Semantic type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3daac6c4-78c5-5271-9c63-6e28f80e0c52">
     * 3daac6c4-78c5-5271-9c63-6e28f80e0c52</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_TYPE =
            EntityProxy.Concept.make("Semantic type (SOLOR)", UUID.fromString("3daac6c4-78c5-5271-9c63-6e28f80e0c52"));
    /**
     * Java binding for the concept described as <strong><em>Sequence (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/83e8e74e-596e-5622-b945-17dbe8e9c05c">
     * 83e8e74e-596e-5622-b945-17dbe8e9c05c</a>}.
     */
    public static final EntityProxy.Concept SEQUENCE =
            EntityProxy.Concept.make("Sequence (SOLOR)", UUID.fromString("83e8e74e-596e-5622-b945-17dbe8e9c05c"));
    /**
     * Java binding for the concept described as <strong><em>Signed integer (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c">
     * 1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c</a>}.
     */
    public static final EntityProxy.Concept SIGNED_INTEGER =
            EntityProxy.Concept.make("Signed integer (SOLOR)", UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c"));
    /**
     * Java binding for the concept described as <strong><em>Signify a concept in writing (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5064b2fa-6f93-5326-899a-9a2ddb63f0a2">
     * 5064b2fa-6f93-5326-899a-9a2ddb63f0a2</a>}.
     */
    public static final EntityProxy.Concept SIGNIFY_A_CONCEPT_IN_WRITING =
            EntityProxy.Concept.make("Signify a concept in writing (SOLOR)", UUID.fromString("5064b2fa-6f93-5326-899a-9a2ddb63f0a2"));
    /**
     * Java binding for the concept described as <strong><em>Simple search panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/42ca3cab-fc8f-5aba-a2af-4f025f4779b1">
     * 42ca3cab-fc8f-5aba-a2af-4f025f4779b1</a>}.
     */
    public static final EntityProxy.Concept SIMPLE_SEARCH_PANEL =
            EntityProxy.Concept.make("Simple search panel (SOLOR)", UUID.fromString("42ca3cab-fc8f-5aba-a2af-4f025f4779b1"));
    /**
     * Java binding for the concept described as <strong><em>Skin of region template (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d03968a5-ddbe-5ec5-bc12-91146bf231eb">
     * d03968a5-ddbe-5ec5-bc12-91146bf231eb</a>}.
     */
    public static final EntityProxy.Concept SKIN_OF_REGION_TEMPLATE =
            EntityProxy.Concept.make("Skin of region template (SOLOR)", UUID.fromString("d03968a5-ddbe-5ec5-bc12-91146bf231eb"));
    /**
     * Java binding for the concept described as <strong><em>SnoRocket classifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1f201fac-960e-11e5-8994-feff819cdc9f">
     * 1f201fac-960e-11e5-8994-feff819cdc9f</a>}.
     */
    public static final EntityProxy.Concept SNOROCKET_CLASSIFIER =
            EntityProxy.Concept.make("SnoRocket classifier (SOLOR)", UUID.fromString("1f201fac-960e-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>Solor LIVD module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a5810c24-8698-56c1-bb60-ef844b66262c">
     * a5810c24-8698-56c1-bb60-ef844b66262c</a>}.
     */
    public static final EntityProxy.Concept SOLOR_LIVD_MODULE =
            EntityProxy.Concept.make("Solor LIVD module (SOLOR)", UUID.fromString("a5810c24-8698-56c1-bb60-ef844b66262c"));
    /**
     * Java binding for the concept described as <strong><em>Solor UMLS module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7a25cea4-7a50-5bf2-98e7-63083cab4c99">
     * 7a25cea4-7a50-5bf2-98e7-63083cab4c99</a>}.
     */
    public static final EntityProxy.Concept SOLOR_UMLS_MODULE =
            EntityProxy.Concept.make("Solor UMLS module (SOLOR)", UUID.fromString("7a25cea4-7a50-5bf2-98e7-63083cab4c99"));
    /**
     * Java binding for the concept described as <strong><em>Solor foundation module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/676a8e10-f75a-5574-a493-3a95aef6ec35">
     * 676a8e10-f75a-5574-a493-3a95aef6ec35</a>}.
     */
    public static final EntityProxy.Concept SOLOR_FOUNDATION_MODULE =
            EntityProxy.Concept.make("Solor foundation module (SOLOR)", UUID.fromString("676a8e10-f75a-5574-a493-3a95aef6ec35"));
    /**
     * Java binding for the concept described as <strong><em>Solor genomic module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/155639f5-972e-5ea4-8f0b-0bd891274ef8">
     * 155639f5-972e-5ea4-8f0b-0bd891274ef8</a>}.
     */
    public static final EntityProxy.Concept SOLOR_GENOMIC_MODULE =
            EntityProxy.Concept.make("Solor genomic module (SOLOR)", UUID.fromString("155639f5-972e-5ea4-8f0b-0bd891274ef8"));
    /**
     * Java binding for the concept described as <strong><em>Source Artifact Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1fd6e6e7-1e56-5687-817b-f46df5139492">
     * 1fd6e6e7-1e56-5687-817b-f46df5139492</a>}.
     */
    public static final EntityProxy.Concept SOURCE_ARTIFACT_VERSION =
            EntityProxy.Concept.make("Source Artifact Version (SOLOR)", UUID.fromString("1fd6e6e7-1e56-5687-817b-f46df5139492"));
    /**
     * Java binding for the concept described as <strong><em>Source Code System (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/32e30e80-3fac-5317-80cf-d85eab22fa9e">
     * 32e30e80-3fac-5317-80cf-d85eab22fa9e</a>}.
     */
    public static final EntityProxy.Concept SOURCE_CODE_SYSTEM =
            EntityProxy.Concept.make("Source Code System (SOLOR)", UUID.fromString("32e30e80-3fac-5317-80cf-d85eab22fa9e"));
    /**
     * Java binding for the concept described as <strong><em>Source Code Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5b3479cb-25b2-5965-a031-54238588218f">
     * 5b3479cb-25b2-5965-a031-54238588218f</a>}.
     */
    public static final EntityProxy.Concept SOURCE_CODE_VERSION =
            EntityProxy.Concept.make("Source Code Version (SOLOR)", UUID.fromString("5b3479cb-25b2-5965-a031-54238588218f"));
    /**
     * Java binding for the concept described as <strong><em>Source Content Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/94c096c4-73df-58a5-b795-76f35d2eb38e">
     * 94c096c4-73df-58a5-b795-76f35d2eb38e</a>}.
     */
    public static final EntityProxy.Concept SOURCE_CONTENT_VERSION =
            EntityProxy.Concept.make("Source Content Version (SOLOR)", UUID.fromString("94c096c4-73df-58a5-b795-76f35d2eb38e"));
    /**
     * Java binding for the concept described as <strong><em>Source Release Date (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/995e6103-bfa9-5cef-90b9-c8801bd6c3f4">
     * 995e6103-bfa9-5cef-90b9-c8801bd6c3f4</a>}.
     */
    public static final EntityProxy.Concept SOURCE_RELEASE_DATE =
            EntityProxy.Concept.make("Source Release Date (SOLOR)", UUID.fromString("995e6103-bfa9-5cef-90b9-c8801bd6c3f4"));
    /**
     * Java binding for the concept described as <strong><em>Spanish dialect assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/03615ef2-aa56-336d-89c5-a1b5c4cee8f6">
     * 03615ef2-aa56-336d-89c5-a1b5c4cee8f6</a>}.
     */
    public static final EntityProxy.Concept SPANISH_DIALECT_ASSEMBLAGE =
            EntityProxy.Concept.make("Spanish dialect assemblage (SOLOR)", UUID.fromString("03615ef2-aa56-336d-89c5-a1b5c4cee8f6"));
    /**
     * Java binding for the concept described as <strong><em>Spanish language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e">
     * 0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e</a>}.
     */
    public static final EntityProxy.Concept SPANISH_LANGUAGE =
            EntityProxy.Concept.make("Spanish language (SOLOR)", UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"), UUID.fromString("45021c36-9567-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>Specimen (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3680e12d-c14c-39cb-ac89-2ae1fa125d41">
     * 3680e12d-c14c-39cb-ac89-2ae1fa125d41</a>}.
     */
    public static final EntityProxy.Concept SPECIMEN =
            EntityProxy.Concept.make("Specimen (SOLOR)", UUID.fromString("3680e12d-c14c-39cb-ac89-2ae1fa125d41"));
    /**
     * Java binding for the concept described as <strong><em>Stamp filter for path (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/509e39b1-351f-51bc-9223-698ffff8a418">
     * 509e39b1-351f-51bc-9223-698ffff8a418</a>}.
     */
    public static final EntityProxy.Concept STAMP_FILTER_FOR_PATH =
            EntityProxy.Concept.make("Stamp filter for path (SOLOR)", UUID.fromString("509e39b1-351f-51bc-9223-698ffff8a418"));
    /**
     * Java binding for the concept described as <strong><em>Standard Korean dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f90722cc-5e40-5b9b-a2a6-f4dfa312a6a9">
     * f90722cc-5e40-5b9b-a2a6-f4dfa312a6a9</a>}.
     */
    public static final EntityProxy.Concept STANDARD_KOREAN_DIALECT =
            EntityProxy.Concept.make("Standard Korean dialect (SOLOR)", UUID.fromString("f90722cc-5e40-5b9b-a2a6-f4dfa312a6a9"));
    /**
     * Java binding for the concept described as <strong><em>Stated assemblage for logic coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cfd2a47e-8169-5e71-9122-d5b73efd990a">
     * cfd2a47e-8169-5e71-9122-d5b73efd990a</a>}.
     */
    public static final EntityProxy.Concept STATED_ASSEMBLAGE_FOR_LOGIC_COORDINATE =
            EntityProxy.Concept.make("Stated assemblage for logic coordinate (SOLOR)", UUID.fromString("cfd2a47e-8169-5e71-9122-d5b73efd990a"));
    /**
     * Java binding for the concept described as <strong><em>Stated navigation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/614017af-9903-53d9-aab4-15fd02193dce">
     * 614017af-9903-53d9-aab4-15fd02193dce</a>}.
     */
    public static final EntityProxy.Concept STATED_NAVIGATION =
            EntityProxy.Concept.make("Stated navigation (SOLOR)", UUID.fromString("614017af-9903-53d9-aab4-15fd02193dce"));
    /**
     * Java binding for the concept described as <strong><em>Stated premise type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3b0dbd3b-2e53-3a30-8576-6c7fa7773060">
     * 3b0dbd3b-2e53-3a30-8576-6c7fa7773060</a>}.
     */
    public static final EntityProxy.Concept STATED_PREMISE_TYPE =
            EntityProxy.Concept.make("Stated premise type (SOLOR)", UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"), UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    /**
     * Java binding for the concept described as <strong><em>Statement action topic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a53a2dc5-3176-5cab-a49e-118f5683b71f">
     * a53a2dc5-3176-5cab-a49e-118f5683b71f</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_ACTION_TOPIC =
            EntityProxy.Concept.make("Statement action topic (SOLOR)", UUID.fromString("a53a2dc5-3176-5cab-a49e-118f5683b71f"));
    /**
     * Java binding for the concept described as <strong><em>Statement association properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c8723701-ce58-5b52-a5a5-56366c58ecd7">
     * c8723701-ce58-5b52-a5a5-56366c58ecd7</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_ASSOCIATION_PROPERTIES =
            EntityProxy.Concept.make("Statement association properties (SOLOR)", UUID.fromString("c8723701-ce58-5b52-a5a5-56366c58ecd7"));
    /**
     * Java binding for the concept described as <strong><em>Statement associations (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/74343c86-79c1-5609-8e95-475dd4e69b62">
     * 74343c86-79c1-5609-8e95-475dd4e69b62</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_ASSOCIATIONS =
            EntityProxy.Concept.make("Statement associations (SOLOR)", UUID.fromString("74343c86-79c1-5609-8e95-475dd4e69b62"));
    /**
     * Java binding for the concept described as <strong><em>Statement authors (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e14db060-e01d-5534-80f7-30fa2915cb4c">
     * e14db060-e01d-5534-80f7-30fa2915cb4c</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_AUTHORS =
            EntityProxy.Concept.make("Statement authors (SOLOR)", UUID.fromString("e14db060-e01d-5534-80f7-30fa2915cb4c"));
    /**
     * Java binding for the concept described as <strong><em>Statement circumstance (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9cd44998-dab8-5890-8678-dd2a5386be8f">
     * 9cd44998-dab8-5890-8678-dd2a5386be8f</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_CIRCUMSTANCE =
            EntityProxy.Concept.make("Statement circumstance (SOLOR)", UUID.fromString("9cd44998-dab8-5890-8678-dd2a5386be8f"));
    /**
     * Java binding for the concept described as <strong><em>Statement identifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7da62607-16ba-56d6-ae67-db66828a8da9">
     * 7da62607-16ba-56d6-ae67-db66828a8da9</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_IDENTIFIER =
            EntityProxy.Concept.make("Statement identifier (SOLOR)", UUID.fromString("7da62607-16ba-56d6-ae67-db66828a8da9"));
    /**
     * Java binding for the concept described as <strong><em>Statement mode (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/78b676a0-1352-5de2-a832-fe8697c6a6ea">
     * 78b676a0-1352-5de2-a832-fe8697c6a6ea</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_MODE =
            EntityProxy.Concept.make("Statement mode (SOLOR)", UUID.fromString("78b676a0-1352-5de2-a832-fe8697c6a6ea"));
    /**
     * Java binding for the concept described as <strong><em>Statement narrative (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7af1f6ea-e924-5502-984c-86592cc71686">
     * 7af1f6ea-e924-5502-984c-86592cc71686</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_NARRATIVE =
            EntityProxy.Concept.make("Statement narrative (SOLOR)", UUID.fromString("7af1f6ea-e924-5502-984c-86592cc71686"));
    /**
     * Java binding for the concept described as <strong><em>Statement properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3c4c4989-5eef-54c3-a2df-f2df030b5ddd">
     * 3c4c4989-5eef-54c3-a2df-f2df030b5ddd</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_PROPERTIES =
            EntityProxy.Concept.make("Statement properties (SOLOR)", UUID.fromString("3c4c4989-5eef-54c3-a2df-f2df030b5ddd"));
    /**
     * Java binding for the concept described as <strong><em>Statement subject of information (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/67fd9ce1-b4f2-53e2-a1de-b17593b75e2b">
     * 67fd9ce1-b4f2-53e2-a1de-b17593b75e2b</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_SUBJECT_OF_INFORMATION =
            EntityProxy.Concept.make("Statement subject of information (SOLOR)", UUID.fromString("67fd9ce1-b4f2-53e2-a1de-b17593b75e2b"));
    /**
     * Java binding for the concept described as <strong><em>Statement subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7691d774-cb9a-5f91-ba50-956ca36b1198">
     * 7691d774-cb9a-5f91-ba50-956ca36b1198</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Statement subject of record (SOLOR)", UUID.fromString("7691d774-cb9a-5f91-ba50-956ca36b1198"));
    /**
     * Java binding for the concept described as <strong><em>Statement time (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a9b2ea04-fc10-59a5-8b7d-9420629e4ac2">
     * a9b2ea04-fc10-59a5-8b7d-9420629e4ac2</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_TIME =
            EntityProxy.Concept.make("Statement time (SOLOR)", UUID.fromString("a9b2ea04-fc10-59a5-8b7d-9420629e4ac2"));
    /**
     * Java binding for the concept described as <strong><em>Statement type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0a8095b0-3fb8-5e10-b4ae-075a97bb2354">
     * 0a8095b0-3fb8-5e10-b4ae-075a97bb2354</a>}.
     */
    public static final EntityProxy.Concept STATEMENT_TYPE =
            EntityProxy.Concept.make("Statement type (SOLOR)", UUID.fromString("0a8095b0-3fb8-5e10-b4ae-075a97bb2354"));
    /**
     * Java binding for the concept described as <strong><em>Status for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0608e233-d79d-5076-985b-9b1ea4e14b4c">
     * 0608e233-d79d-5076-985b-9b1ea4e14b4c</a>}.
     */
    public static final EntityProxy.Concept STATUS_FOR_VERSION =
            EntityProxy.Concept.make("Status for version (SOLOR)", UUID.fromString("0608e233-d79d-5076-985b-9b1ea4e14b4c"));
    /**
     * Java binding for the concept described as <strong><em>Status value (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/10b873e2-8247-5ab5-9dec-4edef37fc219">
     * 10b873e2-8247-5ab5-9dec-4edef37fc219</a>}.
     */
    public static final EntityProxy.Concept STATUS_VALUE =
            EntityProxy.Concept.make("Status value (SOLOR)", UUID.fromString("10b873e2-8247-5ab5-9dec-4edef37fc219"));
    /**
     * Java binding for the concept described as <strong><em>String (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a46aaf11-b37a-32d6-abdc-707f084ec8f5">
     * a46aaf11-b37a-32d6-abdc-707f084ec8f5</a>}.
     */
    public static final EntityProxy.Concept STRING =
            EntityProxy.Concept.make("String (SOLOR)", UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5"));
    /**
     * Java binding for the concept described as <strong><em>String 1 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e1ea51d7-2ecb-5d23-94ec-6947f1c06c97">
     * e1ea51d7-2ecb-5d23-94ec-6947f1c06c97</a>}.
     */
    public static final EntityProxy.Concept STRING_1 =
            EntityProxy.Concept.make("String 1 (SOLOR)", UUID.fromString("e1ea51d7-2ecb-5d23-94ec-6947f1c06c97"));
    /**
     * Java binding for the concept described as <strong><em>String 2 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ac8c0a46-82b4-5a30-82c6-d0bf7d4862e8">
     * ac8c0a46-82b4-5a30-82c6-d0bf7d4862e8</a>}.
     */
    public static final EntityProxy.Concept STRING_2 =
            EntityProxy.Concept.make("String 2 (SOLOR)", UUID.fromString("ac8c0a46-82b4-5a30-82c6-d0bf7d4862e8"));
    /**
     * Java binding for the concept described as <strong><em>String 3 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fd3b443f-7195-5b52-adc0-637bd9d9379d">
     * fd3b443f-7195-5b52-adc0-637bd9d9379d</a>}.
     */
    public static final EntityProxy.Concept STRING_3 =
            EntityProxy.Concept.make("String 3 (SOLOR)", UUID.fromString("fd3b443f-7195-5b52-adc0-637bd9d9379d"));
    /**
     * Java binding for the concept described as <strong><em>String 4 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fbf9278d-5cf5-524c-ac29-00a0ea3b7f58">
     * fbf9278d-5cf5-524c-ac29-00a0ea3b7f58</a>}.
     */
    public static final EntityProxy.Concept STRING_4 =
            EntityProxy.Concept.make("String 4 (SOLOR)", UUID.fromString("fbf9278d-5cf5-524c-ac29-00a0ea3b7f58"));
    /**
     * Java binding for the concept described as <strong><em>String 5 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f549b466-a6c1-5b82-a6e5-bc9b530c93da">
     * f549b466-a6c1-5b82-a6e5-bc9b530c93da</a>}.
     */
    public static final EntityProxy.Concept STRING_5 =
            EntityProxy.Concept.make("String 5 (SOLOR)", UUID.fromString("f549b466-a6c1-5b82-a6e5-bc9b530c93da"));
    /**
     * Java binding for the concept described as <strong><em>String 6 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eaa25354-2842-57e1-a8e2-3ea54ce26caf">
     * eaa25354-2842-57e1-a8e2-3ea54ce26caf</a>}.
     */
    public static final EntityProxy.Concept STRING_6 =
            EntityProxy.Concept.make("String 6 (SOLOR)", UUID.fromString("eaa25354-2842-57e1-a8e2-3ea54ce26caf"));
    /**
     * Java binding for the concept described as <strong><em>String 7 (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2d88c94c-7cd8-5ed8-913c-a6cdff869e9a">
     * 2d88c94c-7cd8-5ed8-913c-a6cdff869e9a</a>}.
     */
    public static final EntityProxy.Concept STRING_7 =
            EntityProxy.Concept.make("String 7 (SOLOR)", UUID.fromString("2d88c94c-7cd8-5ed8-913c-a6cdff869e9a"));
    /**
     * Java binding for the concept described as <strong><em>String field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8d0fdf86-9c18-50b4-b59f-fb83db9cbcaf">
     * 8d0fdf86-9c18-50b4-b59f-fb83db9cbcaf</a>}.
     */
    public static final EntityProxy.Concept STRING_FIELD =
            EntityProxy.Concept.make("String field (SOLOR)", UUID.fromString("8d0fdf86-9c18-50b4-b59f-fb83db9cbcaf"));
    /**
     * Java binding for the concept described as <strong><em>String for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/5c80e7e4-25de-5da4-9bfa-ed5200f5a623">
     * 5c80e7e4-25de-5da4-9bfa-ed5200f5a623</a>}.
     */
    public static final EntityProxy.Concept STRING_FOR_SEMANTIC =
            EntityProxy.Concept.make("String for semantic (SOLOR)", UUID.fromString("5c80e7e4-25de-5da4-9bfa-ed5200f5a623"));
    /**
     * Java binding for the concept described as <strong><em>String literal (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/60afe044-7437-5634-be10-d7834a1cd0db">
     * 60afe044-7437-5634-be10-d7834a1cd0db</a>}.
     */
    public static final EntityProxy.Concept STRING_LITERAL =
            EntityProxy.Concept.make("String literal (SOLOR)", UUID.fromString("60afe044-7437-5634-be10-d7834a1cd0db"));
    /**
     * Java binding for the concept described as <strong><em>String semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e196e48a-760b-5cd3-b5f0-8e5b3bb49627">
     * e196e48a-760b-5cd3-b5f0-8e5b3bb49627</a>}.
     */
    public static final EntityProxy.Concept STRING_SEMANTIC =
            EntityProxy.Concept.make("String semantic (SOLOR)", UUID.fromString("e196e48a-760b-5cd3-b5f0-8e5b3bb49627"));
    /**
     * Java binding for the concept described as <strong><em>Subject of information (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4aebb90a-e361-5d07-b5e2-2250b7d8b60d">
     * 4aebb90a-e361-5d07-b5e2-2250b7d8b60d</a>}.
     */
    public static final EntityProxy.Concept SUBJECT_OF_INFORMATION =
            EntityProxy.Concept.make("Subject of information (SOLOR)", UUID.fromString("4aebb90a-e361-5d07-b5e2-2250b7d8b60d"));
    /**
     * Java binding for the concept described as <strong><em>Subject of record (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8fbaaefc-e7df-5cd3-8f91-788cbb4997c9">
     * 8fbaaefc-e7df-5cd3-8f91-788cbb4997c9</a>}.
     */
    public static final EntityProxy.Concept SUBJECT_OF_RECORD =
            EntityProxy.Concept.make("Subject of record (SOLOR)", UUID.fromString("8fbaaefc-e7df-5cd3-8f91-788cbb4997c9"));
    /**
     * Java binding for the concept described as <strong><em>Substance (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/95f41098-8391-3f5e-9d61-4b019f1de99d">
     * 95f41098-8391-3f5e-9d61-4b019f1de99d</a>}.
     */
    public static final EntityProxy.Concept SUBSTANCE =
            EntityProxy.Concept.make("Substance (SOLOR)", UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));
    /**
     * Java binding for the concept described as <strong><em>Substance does not exist (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/1a1d8b6c-a224-577d-a7ad-1bde086a1468">
     * 1a1d8b6c-a224-577d-a7ad-1bde086a1468</a>}.
     */
    public static final EntityProxy.Concept SUBSTANCE_DOES_NOT_EXIST =
            EntityProxy.Concept.make("Substance does not exist (SOLOR)", UUID.fromString("1a1d8b6c-a224-577d-a7ad-1bde086a1468"));
    /**
     * Java binding for the concept described as <strong><em>Sufficient concept definition (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6d9cd46e-8a8f-310a-a298-3e55dcf7a986">
     * 6d9cd46e-8a8f-310a-a298-3e55dcf7a986</a>}.
     */
    public static final EntityProxy.Concept SUFFICIENT_CONCEPT_DEFINITION =
            EntityProxy.Concept.make("Sufficient concept definition (SOLOR)", UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986"));
    /**
     * Java binding for the concept described as <strong><em>Sufficient concept definition operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dfa80f36-dbe6-5006-8509-c497a26ceab5">
     * dfa80f36-dbe6-5006-8509-c497a26ceab5</a>}.
     */
    public static final EntityProxy.Concept SUFFICIENT_CONCEPT_DEFINITION_OPERATOR =
            EntityProxy.Concept.make("Sufficient concept definition operator (SOLOR)", UUID.fromString("dfa80f36-dbe6-5006-8509-c497a26ceab5"));
    /**
     * Java binding for the concept described as <strong><em>Sufficient set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8aa48cfd-485b-5140-beb9-0d122f7812d9">
     * 8aa48cfd-485b-5140-beb9-0d122f7812d9</a>}.
     */
    public static final EntityProxy.Concept SUFFICIENT_SET =
            EntityProxy.Concept.make("Sufficient set (SOLOR)", UUID.fromString("8aa48cfd-485b-5140-beb9-0d122f7812d9"));
    /**
     * Java binding for the concept described as <strong><em>Swedish language (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9784a791-8fdb-32f7-88da-74ab135fe4e3">
     * 9784a791-8fdb-32f7-88da-74ab135fe4e3</a>}.
     */
    public static final EntityProxy.Concept SWEDISH_LANGUAGE =
            EntityProxy.Concept.make("Swedish language (SOLOR)", UUID.fromString("9784a791-8fdb-32f7-88da-74ab135fe4e3"), UUID.fromString("45022848-9567-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>Symmetric feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7fba36ae-ecef-56b1-a7d2-9e5c9861406e">
     * 7fba36ae-ecef-56b1-a7d2-9e5c9861406e</a>}.
     */
    public static final EntityProxy.Concept SYMMETRIC_FEATURE =
            EntityProxy.Concept.make("Symmetric feature (SOLOR)", UUID.fromString("7fba36ae-ecef-56b1-a7d2-9e5c9861406e"));
    /**
     * Java binding for the concept described as <strong><em>Synchronization item properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8edb0545-3003-58d4-8dbd-42a13770c0b2">
     * 8edb0545-3003-58d4-8dbd-42a13770c0b2</a>}.
     */
    public static final EntityProxy.Concept SYNCHRONIZATION_ITEM_PROPERTIES =
            EntityProxy.Concept.make("Synchronization item properties (SOLOR)", UUID.fromString("8edb0545-3003-58d4-8dbd-42a13770c0b2"));
    /**
     * Java binding for the concept described as <strong><em>System dashboard panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3bab2038-3c4d-5823-9bba-39dd1cf83bfc">
     * 3bab2038-3c4d-5823-9bba-39dd1cf83bfc</a>}.
     */
    public static final EntityProxy.Concept SYSTEM_DASHBOARD_PANEL =
            EntityProxy.Concept.make("System dashboard panel (SOLOR)", UUID.fromString("3bab2038-3c4d-5823-9bba-39dd1cf83bfc"));
    /**
     * Java binding for the concept described as <strong><em>Target Code System (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6b31a67a-7e6d-57c0-8609-52912076fce8">
     * 6b31a67a-7e6d-57c0-8609-52912076fce8</a>}.
     */
    public static final EntityProxy.Concept TARGET_CODE_SYSTEM =
            EntityProxy.Concept.make("Target Code System (SOLOR)", UUID.fromString("6b31a67a-7e6d-57c0-8609-52912076fce8"));
    /**
     * Java binding for the concept described as <strong><em>Target Code Version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b5165f68-b934-5c79-ac71-bd5375f7c809">
     * b5165f68-b934-5c79-ac71-bd5375f7c809</a>}.
     */
    public static final EntityProxy.Concept TARGET_CODE_VERSION =
            EntityProxy.Concept.make("Target Code Version (SOLOR)", UUID.fromString("b5165f68-b934-5c79-ac71-bd5375f7c809"));
    /**
     * Java binding for the concept described as <strong><em>Target Terminology Date (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d8f2ba8a-c81d-5acf-8c2b-79af9fd645e8">
     * d8f2ba8a-c81d-5acf-8c2b-79af9fd645e8</a>}.
     */
    public static final EntityProxy.Concept TARGET_TERMINOLOGY_DATE =
            EntityProxy.Concept.make("Target Terminology Date (SOLOR)", UUID.fromString("d8f2ba8a-c81d-5acf-8c2b-79af9fd645e8"));
    /**
     * Java binding for the concept described as <strong><em>Taxonomy configuration name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a1f9c031-eb1b-57a1-82d7-5f69fea256e1">
     * a1f9c031-eb1b-57a1-82d7-5f69fea256e1</a>}.
     */
    public static final EntityProxy.Concept TAXONOMY_CONFIGURATION_NAME =
            EntityProxy.Concept.make("Taxonomy configuration name (SOLOR)", UUID.fromString("a1f9c031-eb1b-57a1-82d7-5f69fea256e1"));
    /**
     * Java binding for the concept described as <strong><em>Taxonomy configuration properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0c6b0a95-9627-57dc-bc7f-9162696401fa">
     * 0c6b0a95-9627-57dc-bc7f-9162696401fa</a>}.
     */
    public static final EntityProxy.Concept TAXONOMY_CONFIGURATION_PROPERTIES =
            EntityProxy.Concept.make("Taxonomy configuration properties (SOLOR)", UUID.fromString("0c6b0a95-9627-57dc-bc7f-9162696401fa"));
    /**
     * Java binding for the concept described as <strong><em>Taxonomy configuration roots (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/64a26cc9-f816-5754-a310-b4fd5c42597c">
     * 64a26cc9-f816-5754-a310-b4fd5c42597c</a>}.
     */
    public static final EntityProxy.Concept TAXONOMY_CONFIGURATION_ROOTS =
            EntityProxy.Concept.make("Taxonomy configuration roots (SOLOR)", UUID.fromString("64a26cc9-f816-5754-a310-b4fd5c42597c"));
    /**
     * Java binding for the concept described as <strong><em>Taxonomy operator (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e9252365-7a43-57ea-bf94-3f23bab4ef99">
     * e9252365-7a43-57ea-bf94-3f23bab4ef99</a>}.
     */
    public static final EntityProxy.Concept TAXONOMY_OPERATOR =
            EntityProxy.Concept.make("Taxonomy operator (SOLOR)", UUID.fromString("e9252365-7a43-57ea-bf94-3f23bab4ef99"));
    /**
     * Java binding for the concept described as <strong><em>Taxonomy panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/55773602-b8f4-560b-990b-5df1cfb1af23">
     * 55773602-b8f4-560b-990b-5df1cfb1af23</a>}.
     */
    public static final EntityProxy.Concept TAXONOMY_PANEL =
            EntityProxy.Concept.make("Taxonomy panel (SOLOR)", UUID.fromString("55773602-b8f4-560b-990b-5df1cfb1af23"));
    /**
     * Java binding for the concept described as <strong><em>Template concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/81ba30f2-808d-59a1-879d-c53028860df9">
     * 81ba30f2-808d-59a1-879d-c53028860df9</a>}.
     */
    public static final EntityProxy.Concept TEMPLATE_CONCEPT =
            EntityProxy.Concept.make("Template concept (SOLOR)", UUID.fromString("81ba30f2-808d-59a1-879d-c53028860df9"));
    /**
     * Java binding for the concept described as <strong><em>Template merge (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cfe87117-7ca1-53ee-896f-ded7fd01cea8">
     * cfe87117-7ca1-53ee-896f-ded7fd01cea8</a>}.
     */
    public static final EntityProxy.Concept TEMPLATE_MERGE =
            EntityProxy.Concept.make("Template merge (SOLOR)", UUID.fromString("cfe87117-7ca1-53ee-896f-ded7fd01cea8"));
    /**
     * Java binding for the concept described as <strong><em>Template mode (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/727dd2af-4cca-5cdb-b776-75dc7f4c1733">
     * 727dd2af-4cca-5cdb-b776-75dc7f4c1733</a>}.
     */
    public static final EntityProxy.Concept TEMPLATE_MODE =
            EntityProxy.Concept.make("Template mode (SOLOR)", UUID.fromString("727dd2af-4cca-5cdb-b776-75dc7f4c1733"));
    /**
     * Java binding for the concept described as <strong><em>Test module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9f81007e-390a-59ea-aacb-056822c0ac57">
     * 9f81007e-390a-59ea-aacb-056822c0ac57</a>}.
     */
    public static final EntityProxy.Concept TEST_MODULE =
            EntityProxy.Concept.make("Test module (SOLOR)", UUID.fromString("9f81007e-390a-59ea-aacb-056822c0ac57"));
    /**
     * Java binding for the concept described as <strong><em>Test promotion module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ceb8b477-0127-5078-887e-1cf476c9d2a8">
     * ceb8b477-0127-5078-887e-1cf476c9d2a8</a>}.
     */
    public static final EntityProxy.Concept TEST_PROMOTION_MODULE =
            EntityProxy.Concept.make("Test promotion module (SOLOR)", UUID.fromString("ceb8b477-0127-5078-887e-1cf476c9d2a8"));
    /**
     * Java binding for the concept described as <strong><em>Text comparison measure semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b1531e68-4e7a-5194-b1f9-9aaace269372">
     * b1531e68-4e7a-5194-b1f9-9aaace269372</a>}.
     */
    public static final EntityProxy.Concept TEXT_COMPARISON_MEASURE_SEMANTIC =
            EntityProxy.Concept.make("Text comparison measure semantic (SOLOR)", UUID.fromString("b1531e68-4e7a-5194-b1f9-9aaace269372"));
    /**
     * Java binding for the concept described as <strong><em>Text for description (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8bdcbe5d-e92e-5c10-845e-b585e6061672">
     * 8bdcbe5d-e92e-5c10-845e-b585e6061672</a>}.
     */
    public static final EntityProxy.Concept TEXT_FOR_DESCRIPTION =
            EntityProxy.Concept.make("Text for description (SOLOR)", UUID.fromString("8bdcbe5d-e92e-5c10-845e-b585e6061672"));
    /**
     * Java binding for the concept described as <strong><em>Time for version (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a9b0dfb2-f463-5dae-8ba8-7f2e8385571b">
     * a9b0dfb2-f463-5dae-8ba8-7f2e8385571b</a>}.
     */
    public static final EntityProxy.Concept TIME_FOR_VERSION =
            EntityProxy.Concept.make("Time for version (SOLOR)", UUID.fromString("a9b0dfb2-f463-5dae-8ba8-7f2e8385571b"));
    /**
     * Java binding for the concept described as <strong><em>Time measurement semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6bd7c916-2bf1-5ae4-996a-6390074bf27f">
     * 6bd7c916-2bf1-5ae4-996a-6390074bf27f</a>}.
     */
    public static final EntityProxy.Concept TIME_MEASUREMENT_SEMANTIC =
            EntityProxy.Concept.make("Time measurement semantic (SOLOR)", UUID.fromString("6bd7c916-2bf1-5ae4-996a-6390074bf27f"));
    /**
     * Java binding for the concept described as <strong><em>Time precedence (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/cd5ccb52-9a7c-5e35-9d82-4d936bc3b086">
     * cd5ccb52-9a7c-5e35-9d82-4d936bc3b086</a>}.
     */
    public static final EntityProxy.Concept TIME_PRECEDENCE =
            EntityProxy.Concept.make("Time precedence (SOLOR)", UUID.fromString("cd5ccb52-9a7c-5e35-9d82-4d936bc3b086"));
    /**
     * Java binding for the concept described as <strong><em>Timing (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/0a1e07c0-3505-5155-bb45-6786a24137ac">
     * 0a1e07c0-3505-5155-bb45-6786a24137ac</a>}.
     */
    public static final EntityProxy.Concept TIMING =
            EntityProxy.Concept.make("Timing (SOLOR)", UUID.fromString("0a1e07c0-3505-5155-bb45-6786a24137ac"));
    /**
     * Java binding for the concept described as <strong><em>Transaction list panel (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/44087b87-880d-5e80-aced-d2f801da4642">
     * 44087b87-880d-5e80-aced-d2f801da4642</a>}.
     */
    public static final EntityProxy.Concept TRANSACTION_LIST_PANEL =
            EntityProxy.Concept.make("Transaction list panel (SOLOR)", UUID.fromString("44087b87-880d-5e80-aced-d2f801da4642"));
    /**
     * Java binding for the concept described as <strong><em>Transitive feature (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/53f866d0-fd61-5c85-a16c-150bd619a0ac">
     * 53f866d0-fd61-5c85-a16c-150bd619a0ac</a>}.
     */
    public static final EntityProxy.Concept TRANSITIVE_PROPERTY =
            EntityProxy.Concept.make("Transitive property (SOLOR)", UUID.fromString("53f866d0-fd61-5c85-a16c-150bd619a0ac"));
    /**
     * Java binding for the concept described as <strong><em>Tree amalgam properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6151a47-4610-5a5c-abd0-42c82be9b633">
     * d6151a47-4610-5a5c-abd0-42c82be9b633</a>}.
     */
    public static final EntityProxy.Concept TREE_AMALGAM_PROPERTIES =
            EntityProxy.Concept.make("Tree amalgam properties (SOLOR)", UUID.fromString("d6151a47-4610-5a5c-abd0-42c82be9b633"));
    /**
     * Java binding for the concept described as <strong><em>Tree list (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c11dd7a1-0ba1-5378-81d6-3efdba1e074b">
     * c11dd7a1-0ba1-5378-81d6-3efdba1e074b</a>}.
     */
    public static final EntityProxy.Concept TREE_LIST =
            EntityProxy.Concept.make("Tree list (SOLOR)", UUID.fromString("c11dd7a1-0ba1-5378-81d6-3efdba1e074b"));
    /**
     * Java binding for the concept described as <strong><em>Type (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/757ec1aa-946b-5ae3-a7ed-0c5d5dee07f0">
     * 757ec1aa-946b-5ae3-a7ed-0c5d5dee07f0</a>}.
     */
    public static final EntityProxy.Concept TYPE =
            EntityProxy.Concept.make("Type (SOLOR)", UUID.fromString("757ec1aa-946b-5ae3-a7ed-0c5d5dee07f0"));
    /**
     * Java binding for the concept described as <strong><em>Type nid for rf2 relationship (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ee9dc230-d54e-5843-9ece-7ae47abf67b8">
     * ee9dc230-d54e-5843-9ece-7ae47abf67b8</a>}.
     */
    public static final EntityProxy.Concept TYPE_NID_FOR_RF2_RELATIONSHIP =
            EntityProxy.Concept.make("Type nid for rf2 relationship (SOLOR)", UUID.fromString("ee9dc230-d54e-5843-9ece-7ae47abf67b8"));
    /**
     * Java binding for the concept described as <strong><em>Type of statement (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eefc0031-43b0-5eed-aec4-9fe9ed66c624">
     * eefc0031-43b0-5eed-aec4-9fe9ed66c624</a>}.
     */
    public static final EntityProxy.Concept TYPE_OF_STATEMENT =
            EntityProxy.Concept.make("Type of statement (SOLOR)", UUID.fromString("eefc0031-43b0-5eed-aec4-9fe9ed66c624"));
    /**
     * Java binding for the concept described as <strong><em>UMLS equivalency assemblage (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8f93945b-9d35-5ea1-b04f-c79fe6372e0f">
     * 8f93945b-9d35-5ea1-b04f-c79fe6372e0f</a>}.
     */
    public static final EntityProxy.Concept UMLS_EQUIVALENCY_ASSEMBLAGE =
            EntityProxy.Concept.make("UMLS equivalency assemblage (SOLOR)", UUID.fromString("8f93945b-9d35-5ea1-b04f-c79fe6372e0f"));
    /**
     * Java binding for the concept described as <strong><em>US English dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/bca0a686-3516-3daf-8fcf-fe396d13cfad">
     * bca0a686-3516-3daf-8fcf-fe396d13cfad</a>}.
     */
    public static final EntityProxy.Concept US_ENGLISH_DIALECT =
            EntityProxy.Concept.make("US English dialect (SOLOR)", UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
    /**
     * Java binding for the concept described as <strong><em>US Extension modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/37478433-8a8e-5f9b-805f-cd8e226550a3">
     * 37478433-8a8e-5f9b-805f-cd8e226550a3</a>}.
     */
    public static final EntityProxy.Concept US_EXTENSION_MODULES =
            EntityProxy.Concept.make("US Extension modules (SOLOR)", UUID.fromString("37478433-8a8e-5f9b-805f-cd8e226550a3"));
    /**
     * Java binding for the concept described as <strong><em>US Government Work (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9f031ac8-4737-508b-8874-a6c1a6e134e2">
     * 9f031ac8-4737-508b-8874-a6c1a6e134e2</a>}.
     */
    public static final EntityProxy.Concept US_GOVERNMENT_WORK =
            EntityProxy.Concept.make("US Government Work (SOLOR)", UUID.fromString("9f031ac8-4737-508b-8874-a6c1a6e134e2"));
    /**
     * Java binding for the concept described as <strong><em>US Nursing dialect (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6e447636-1085-32ff-bc36-6748a45255de">
     * 6e447636-1085-32ff-bc36-6748a45255de</a>}.
     */
    public static final EntityProxy.Concept US_NURSING_DIALECT =
            EntityProxy.Concept.make("US Nursing dialect (SOLOR)", UUID.fromString("6e447636-1085-32ff-bc36-6748a45255de"));
    /**
     * Java binding for the concept described as <strong><em>UUID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2faa9262-8fb2-11db-b606-0800200c9a66">
     * 2faa9262-8fb2-11db-b606-0800200c9a66</a>}.
     */
    public static final EntityProxy.Concept UUID_DATA_TYPE =
            EntityProxy.Concept.make("UUID (SOLOR)", UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66"), UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));
    /**
     * Java binding for the concept described as <strong><em>UUID field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dea8cb0f-9bb5-56bb-af27-a14943cb24ba">
     * dea8cb0f-9bb5-56bb-af27-a14943cb24ba</a>}.
     */
    public static final EntityProxy.Concept UUID_FIELD =
            EntityProxy.Concept.make("UUID field (SOLOR)", UUID.fromString("dea8cb0f-9bb5-56bb-af27-a14943cb24ba"));
    /**
     * Java binding for the concept described as <strong><em>UUID for taxonomy coordinate (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9889b642-ddec-52b5-9572-8c56c0655bee">
     * 9889b642-ddec-52b5-9572-8c56c0655bee</a>}.
     */
    public static final EntityProxy.Concept UUID_FOR_TAXONOMY_COORDINATE =
            EntityProxy.Concept.make("UUID for taxonomy coordinate (SOLOR)", UUID.fromString("9889b642-ddec-52b5-9572-8c56c0655bee"));
    /**
     * Java binding for the concept described as <strong><em>UUID list for component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f8e3238e-7424-5a40-8649-a8d164382fec">
     * f8e3238e-7424-5a40-8649-a8d164382fec</a>}.
     */
    public static final EntityProxy.Concept UUID_LIST_FOR_COMPONENT =
            EntityProxy.Concept.make("UUID list for component (SOLOR)", UUID.fromString("f8e3238e-7424-5a40-8649-a8d164382fec"));
    /**
     * Java binding for the concept described as <strong><em>Uncategorized phenomenon (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/722f5ac8-1f5c-5d8f-96bb-370d79596f66">
     * 722f5ac8-1f5c-5d8f-96bb-370d79596f66</a>}.
     */
    public static final EntityProxy.Concept UNCATEGORIZED_PHENOMENON =
            EntityProxy.Concept.make("Uncategorized phenomenon (SOLOR)", UUID.fromString("722f5ac8-1f5c-5d8f-96bb-370d79596f66"));
    /**
     * Java binding for the concept described as <strong><em>Unicode evaluation (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/977e682d-9611-5316-9791-f349b1d10fed">
     * 977e682d-9611-5316-9791-f349b1d10fed</a>}.
     */
    public static final EntityProxy.Concept UNICODE_EVALUATION =
            EntityProxy.Concept.make("Unicode evaluation (SOLOR)", UUID.fromString("977e682d-9611-5316-9791-f349b1d10fed"));
    /**
     * Java binding for the concept described as <strong><em>Uninitialized component (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/55f74246-0a25-57ac-9473-a788d08fb656">
     * 55f74246-0a25-57ac-9473-a788d08fb656</a>}.
     */
    public static final EntityProxy.Concept UNINITIALIZED_COMPONENT =
            EntityProxy.Concept.make("Uninitialized component (SOLOR)", UUID.fromString("55f74246-0a25-57ac-9473-a788d08fb656"));
    /**
     * Java binding for the concept described as <strong><em>Units different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e157a439-90be-5c5c-89b5-779b59a674fd">
     * e157a439-90be-5c5c-89b5-779b59a674fd</a>}.
     */
    public static final EntityProxy.Concept UNITS_DIFFERENT =
            EntityProxy.Concept.make("Units different (SOLOR)", UUID.fromString("e157a439-90be-5c5c-89b5-779b59a674fd"));
    /**
     * Java binding for the concept described as <strong><em>Universal restriction (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fc18c082-c6ad-52d2-b568-cc9568ace6c9">
     * fc18c082-c6ad-52d2-b568-cc9568ace6c9</a>}.
     */
    public static final EntityProxy.Concept UNIVERSAL_RESTRICTION =
            EntityProxy.Concept.make("Universal restriction (SOLOR)", UUID.fromString("fc18c082-c6ad-52d2-b568-cc9568ace6c9"));
    /**
     * Java binding for the concept described as <strong><em>Universally Unique Identifier (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/845274b5-9644-3799-94c6-e0ea37e7d1a4">
     * 845274b5-9644-3799-94c6-e0ea37e7d1a4</a>}.
     */
    public static final EntityProxy.Concept UNIVERSALLY_UNIQUE_IDENTIFIER =
            EntityProxy.Concept.make("Universally Unique Identifier (SOLOR)", UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4"));
    /**
     * Java binding for the concept described as <strong><em>Unmappable (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e5f7f98f-9607-55a7-bbc4-25f2e61df23d">
     * e5f7f98f-9607-55a7-bbc4-25f2e61df23d</a>}.
     */
    public static final EntityProxy.Concept UNMAPPABLE =
            EntityProxy.Concept.make("Unmappable (SOLOR)", UUID.fromString("e5f7f98f-9607-55a7-bbc4-25f2e61df23d"));
    /**
     * Java binding for the concept described as <strong><em>Unmodeled concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/04f4c95f-8d7d-5f8a-a3db-425616ebe9fa">
     * 04f4c95f-8d7d-5f8a-a3db-425616ebe9fa</a>}.
     */
    public static final EntityProxy.Concept UNMODELED_CONCEPT =
            EntityProxy.Concept.make("Unmodeled concept (SOLOR)", UUID.fromString("04f4c95f-8d7d-5f8a-a3db-425616ebe9fa"));
    /**
     * Java binding for the concept described as <strong><em>Unmodeled feature concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6cf5b272-7456-5028-9a33-411a9cec9ae9">
     * 6cf5b272-7456-5028-9a33-411a9cec9ae9</a>}.
     */
    public static final EntityProxy.Concept UNMODELED_FEATURE_CONCEPT =
            EntityProxy.Concept.make("Unmodeled feature concept (SOLOR)", UUID.fromString("6cf5b272-7456-5028-9a33-411a9cec9ae9"));
    /**
     * Java binding for the concept described as <strong><em>Unmodeled role concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4be7118f-e6ab-5dc7-bcba-b2cc8b028492">
     * 4be7118f-e6ab-5dc7-bcba-b2cc8b028492</a>}.
     */
    public static final EntityProxy.Concept UNMODELED_ROLE_CONCEPT =
            EntityProxy.Concept.make("Unmodeled role concept (SOLOR)", UUID.fromString("4be7118f-e6ab-5dc7-bcba-b2cc8b028492"));
    /**
     * Java binding for the concept described as <strong><em>Unmodeled taxonomic concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/aab21dff-c0d5-5154-a648-32cc1a20a883">
     * aab21dff-c0d5-5154-a648-32cc1a20a883</a>}.
     */
    public static final EntityProxy.Concept UNMODELED_TAXONOMIC_CONCEPT =
            EntityProxy.Concept.make("Unmodeled taxonomic concept (SOLOR)", UUID.fromString("aab21dff-c0d5-5154-a648-32cc1a20a883"));
    /**
     * Java binding for the concept described as <strong><em>Unstructured circumstance properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ecede75a-34f8-554b-8918-41ba0f58480d">
     * ecede75a-34f8-554b-8918-41ba0f58480d</a>}.
     */
    public static final EntityProxy.Concept UNSTRUCTURED_CIRCUMSTANCE_PROPERTIES =
            EntityProxy.Concept.make("Unstructured circumstance properties (SOLOR)", UUID.fromString("ecede75a-34f8-554b-8918-41ba0f58480d"));
    /**
     * Java binding for the concept described as <strong><em>Unstructured circumstance text (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ad771b63-12ad-5bfb-93f5-4a0eed219b5d">
     * ad771b63-12ad-5bfb-93f5-4a0eed219b5d</a>}.
     */
    public static final EntityProxy.Concept UNSTRUCTURED_CIRCUMSTANCE_TEXT =
            EntityProxy.Concept.make("Unstructured circumstance text (SOLOR)", UUID.fromString("ad771b63-12ad-5bfb-93f5-4a0eed219b5d"));
    /**
     * Java binding for the concept described as <strong><em>Upper bound (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3d3fa560-cdc0-5119-af6b-e14c33abd664">
     * 3d3fa560-cdc0-5119-af6b-e14c33abd664</a>}.
     */
    public static final EntityProxy.Concept UPPER_BOUND =
            EntityProxy.Concept.make("Upper bound (SOLOR)", UUID.fromString("3d3fa560-cdc0-5119-af6b-e14c33abd664"));
    /**
     * Java binding for the concept described as <strong><em>User (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f7495b58-6630-3499-a44e-2052b5fcf06c">
     * f7495b58-6630-3499-a44e-2052b5fcf06c</a>}.
     */
    public static final EntityProxy.Concept USER =
            EntityProxy.Concept.make("User (SOLOR)", UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
    /**
     * Java binding for the concept described as <strong><em>VA Station IEN (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/416f763f-12b1-55a1-bf32-8f6001fc0eff">
     * 416f763f-12b1-55a1-bf32-8f6001fc0eff</a>}.
     */
    public static final EntityProxy.Concept VA_STATION_IEN =
            EntityProxy.Concept.make("VA Station IEN (SOLOR)", UUID.fromString("416f763f-12b1-55a1-bf32-8f6001fc0eff"));
    /**
     * Java binding for the concept described as <strong><em>VA Station Number (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/172ee183-a183-5f6c-8527-afbc658dd49f">
     * 172ee183-a183-5f6c-8527-afbc658dd49f</a>}.
     */
    public static final EntityProxy.Concept VA_STATION_NUMBER =
            EntityProxy.Concept.make("VA Station Number (SOLOR)", UUID.fromString("172ee183-a183-5f6c-8527-afbc658dd49f"));
    /**
     * Java binding for the concept described as <strong><em>VHAT modules (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8aa5fda8-33e9-5eaf-88e8-dd8a024d2489">
     * 8aa5fda8-33e9-5eaf-88e8-dd8a024d2489</a>}.
     */
    public static final EntityProxy.Concept VHAT_MODULES =
            EntityProxy.Concept.make("VHAT modules (SOLOR)", UUID.fromString("8aa5fda8-33e9-5eaf-88e8-dd8a024d2489"), UUID.fromString("1f201520-960e-11e5-8994-feff819cdc9f"));
    /**
     * Java binding for the concept described as <strong><em>VUID (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/eba19d37-837e-5136-8e0f-a0deab52f0cf">
     * eba19d37-837e-5136-8e0f-a0deab52f0cf</a>}.
     */
    public static final EntityProxy.Concept VUID =
            EntityProxy.Concept.make("VUID (SOLOR)", UUID.fromString("eba19d37-837e-5136-8e0f-a0deab52f0cf"));
    /**
     * Java binding for the concept described as <strong><em>Vaccine (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ae69846d-4fee-5cfe-9e9c-684b6d53154d">
     * ae69846d-4fee-5cfe-9e9c-684b6d53154d</a>}.
     */
    public static final EntityProxy.Concept VACCINE =
            EntityProxy.Concept.make("Vaccine (SOLOR)", UUID.fromString("ae69846d-4fee-5cfe-9e9c-684b6d53154d"));
    /**
     * Java binding for the concept described as <strong><em>Value (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d94e271f-0e9b-5159-8691-6c29c7689ffb">
     * d94e271f-0e9b-5159-8691-6c29c7689ffb</a>}.
     */
    public static final EntityProxy.Concept VALUE =
            EntityProxy.Concept.make("Value (SOLOR)", UUID.fromString("d94e271f-0e9b-5159-8691-6c29c7689ffb"));
    /**
     * Java binding for the concept described as <strong><em>Values different (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/b633242f-e4a0-5cb1-a0e4-f0a0bf9319dd">
     * b633242f-e4a0-5cb1-a0e4-f0a0bf9319dd</a>}.
     */
    public static final EntityProxy.Concept VALUES_DIFFERENT =
            EntityProxy.Concept.make("Values different (SOLOR)", UUID.fromString("b633242f-e4a0-5cb1-a0e4-f0a0bf9319dd"));
    /**
     * Java binding for the concept described as <strong><em>Version list for chronicle (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d6f27f80-8e20-58fe-8d69-66ad4644f969">
     * d6f27f80-8e20-58fe-8d69-66ad4644f969</a>}.
     */
    public static final EntityProxy.Concept VERSION_LIST_FOR_CHRONICLE =
            EntityProxy.Concept.make("Version list for chronicle (SOLOR)", UUID.fromString("d6f27f80-8e20-58fe-8d69-66ad4644f969"));
    /**
     * Java binding for the concept described as <strong><em>Version properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/93f844df-38e5-5167-ba94-2c948b8bd07c">
     * 93f844df-38e5-5167-ba94-2c948b8bd07c</a>}.
     */
    public static final EntityProxy.Concept VERSION_PROPERTIES =
            EntityProxy.Concept.make("Version properties (SOLOR)", UUID.fromString("93f844df-38e5-5167-ba94-2c948b8bd07c"));
    /**
     * Java binding for the concept described as <strong><em>Version type for action (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fe19bd11-46d8-51ec-90c8-c3bd955703ce">
     * fe19bd11-46d8-51ec-90c8-c3bd955703ce</a>}.
     */
    public static final EntityProxy.Concept VERSION_TYPE_FOR_ACTION =
            EntityProxy.Concept.make("Version type for action (SOLOR)", UUID.fromString("fe19bd11-46d8-51ec-90c8-c3bd955703ce"));
    /**
     * Java binding for the concept described as <strong><em>Vertex STAMP filter for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/75ce5835-2676-5bac-a01d-f1ef1e700d9f">
     * 75ce5835-2676-5bac-a01d-f1ef1e700d9f</a>}.
     */
    public static final EntityProxy.Concept VERTEX_STAMP_FILTER_FOR_MANIFOLD =
            EntityProxy.Concept.make("Vertex STAMP filter for manifold (SOLOR)", UUID.fromString("75ce5835-2676-5bac-a01d-f1ef1e700d9f"));
    /**
     * Java binding for the concept described as <strong><em>Vertex field (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/3e56c6b6-5371-11eb-ae93-0242ac130002">
     * 3e56c6b6-5371-11eb-ae93-0242ac130002</a>}.
     */
    public static final EntityProxy.Concept VERTEX_FIELD =
            EntityProxy.Concept.make("Vertex field (SOLOR)", UUID.fromString("3e56c6b6-5371-11eb-ae93-0242ac130002"));
    /**
     * Java binding for the concept described as <strong><em>Vertex sort (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e973f077-a99d-59e6-b7bd-804e87e0e639">
     * e973f077-a99d-59e6-b7bd-804e87e0e639</a>}.
     */
    public static final EntityProxy.Concept VERTEX_SORT =
            EntityProxy.Concept.make("Vertex sort (SOLOR)", UUID.fromString("e973f077-a99d-59e6-b7bd-804e87e0e639"));
    /**
     * Java binding for the concept described as <strong><em>Vertex state set (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/347cd3f2-8130-5032-8960-091e194e9afe">
     * 347cd3f2-8130-5032-8960-091e194e9afe</a>}.
     */
    public static final EntityProxy.Concept VERTEX_STATE_SET =
            EntityProxy.Concept.make("Vertex state set (SOLOR)", UUID.fromString("347cd3f2-8130-5032-8960-091e194e9afe"));
    /**
     * Java binding for the concept described as <strong><em>Veterinary medicine only (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ce237c9d-b7a2-5310-9b76-29c2a62a1ac1">
     * ce237c9d-b7a2-5310-9b76-29c2a62a1ac1</a>}.
     */
    public static final EntityProxy.Concept VETERINARY_MEDICINE_ONLY =
            EntityProxy.Concept.make("Veterinary medicine only (SOLOR)", UUID.fromString("ce237c9d-b7a2-5310-9b76-29c2a62a1ac1"));
    /**
     * Java binding for the concept described as <strong><em>View STAMP filter for manifold (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/945f6d99-78f7-505e-9d45-153eb23c4a51">
     * 945f6d99-78f7-505e-9d45-153eb23c4a51</a>}.
     */
    public static final EntityProxy.Concept VIEW_STAMP_FILTER_FOR_MANIFOLD =
            EntityProxy.Concept.make("View STAMP filter for manifold (SOLOR)", UUID.fromString("945f6d99-78f7-505e-9d45-153eb23c4a51"));
    /**
     * Java binding for the concept described as <strong><em>View coordinate key (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/4211cf36-bd75-586a-805c-51f059e2eaaa">
     * 4211cf36-bd75-586a-805c-51f059e2eaaa</a>}.
     */
    public static final EntityProxy.Concept VIEW_COORDINATE_KEY =
            EntityProxy.Concept.make("View coordinate key (SOLOR)", UUID.fromString("4211cf36-bd75-586a-805c-51f059e2eaaa"));
    /**
     * Java binding for the concept described as <strong><em>Window configuration name (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/a93cf6c2-8cc1-5cb7-9af9-7d27d6dbc29e">
     * a93cf6c2-8cc1-5cb7-9af9-7d27d6dbc29e</a>}.
     */
    public static final EntityProxy.Concept WINDOW_CONFIGURATION_NAME =
            EntityProxy.Concept.make("Window configuration name (SOLOR)", UUID.fromString("a93cf6c2-8cc1-5cb7-9af9-7d27d6dbc29e"));
    /**
     * Java binding for the concept described as <strong><em>Window configuration properties (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8b6cc217-da45-5712-bd88-559d110a0ef9">
     * 8b6cc217-da45-5712-bd88-559d110a0ef9</a>}.
     */
    public static final EntityProxy.Concept WINDOW_CONFIGURATION_PROPERTIES =
            EntityProxy.Concept.make("Window configuration properties (SOLOR)", UUID.fromString("8b6cc217-da45-5712-bd88-559d110a0ef9"));
    /**
     * Java binding for the concept described as <strong><em>Window height (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/42a7d496-c6fd-542d-8980-425d738090a7">
     * 42a7d496-c6fd-542d-8980-425d738090a7</a>}.
     */
    public static final EntityProxy.Concept WINDOW_HEIGHT =
            EntityProxy.Concept.make("Window height (SOLOR)", UUID.fromString("42a7d496-c6fd-542d-8980-425d738090a7"));
    /**
     * Java binding for the concept described as <strong><em>Window width (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f65a21ff-66a1-5b0d-a871-6f66b7601ce2">
     * f65a21ff-66a1-5b0d-a871-6f66b7601ce2</a>}.
     */
    public static final EntityProxy.Concept WINDOW_WIDTH =
            EntityProxy.Concept.make("Window width (SOLOR)", UUID.fromString("f65a21ff-66a1-5b0d-a871-6f66b7601ce2"));
    /**
     * Java binding for the concept described as <strong><em>Window x position (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/2ac1bb5c-f68c-5d32-a436-51fc4e75d308">
     * 2ac1bb5c-f68c-5d32-a436-51fc4e75d308</a>}.
     */
    public static final EntityProxy.Concept WINDOW_X_POSITION =
            EntityProxy.Concept.make("Window x position (SOLOR)", UUID.fromString("2ac1bb5c-f68c-5d32-a436-51fc4e75d308"));
    /**
     * Java binding for the concept described as <strong><em>Window y position (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/6ce4ecd7-d9fb-5a66-8bab-46ae8bb4b282">
     * 6ce4ecd7-d9fb-5a66-8bab-46ae8bb4b282</a>}.
     */
    public static final EntityProxy.Concept WINDOW_Y_POSITION =
            EntityProxy.Concept.make("Window y position (SOLOR)", UUID.fromString("6ce4ecd7-d9fb-5a66-8bab-46ae8bb4b282"));
    /**
     * Java binding for the concept described as <strong><em>Withdrawn status (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/35fd4750-6e43-5fa3-ba7f-f2ad376052bc">
     * 35fd4750-6e43-5fa3-ba7f-f2ad376052bc</a>}.
     */
    public static final EntityProxy.Concept WITHDRAWN_STATE =
            EntityProxy.Concept.make("Withdrawn state (SOLOR)", UUID.fromString("35fd4750-6e43-5fa3-ba7f-f2ad376052bc"));
    /**
     * Java binding for the concept described as <strong><em>XOR (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f42c-579e-11e7-907b-a6006ad3dba0">
     * d9c1f42c-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept XOR____QUERY_CLAUSE =
            EntityProxy.Concept.make("XOR (query clause)", UUID.fromString("d9c1f42c-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>and not (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1ec02-579e-11e7-907b-a6006ad3dba0">
     * d9c1ec02-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept AND_NOT____QUERY_CLAUSE =
            EntityProxy.Concept.make("and not (query clause)", UUID.fromString("d9c1ec02-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>boolean (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/08f2fb74-980d-5157-b92c-4ff1eac6a506">
     * 08f2fb74-980d-5157-b92c-4ff1eac6a506</a>}.
     */
    public static final EntityProxy.Concept BOOLEAN =
            EntityProxy.Concept.make("boolean (SOLOR)", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506"));
    /**
     * Java binding for the concept described as <strong><em>byte array (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9a84fecf-708d-5de4-9c5f-e17973229e0f">
     * 9a84fecf-708d-5de4-9c5f-e17973229e0f</a>}.
     */
    public static final EntityProxy.Concept BYTE_ARRAY =
            EntityProxy.Concept.make("byte array (SOLOR)", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f"));
    /**
     * Java binding for the concept described as <strong><em>changed between STAMPs (query clause)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d9c1f530-579e-11e7-907b-a6006ad3dba0">
     * d9c1f530-579e-11e7-907b-a6006ad3dba0</a>}.
     */
    public static final EntityProxy.Concept CHANGED_BETWEEN_STAMPS____QUERY_CLAUSE =
            EntityProxy.Concept.make("changed between STAMPs (query clause)", UUID.fromString("d9c1f530-579e-11e7-907b-a6006ad3dba0"));
    /**
     * Java binding for the concept described as <strong><em>description list for concept (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/ab3e8771-7c7c-5e57-8acf-147b16da36e2">
     * ab3e8771-7c7c-5e57-8acf-147b16da36e2</a>}.
     */
    public static final EntityProxy.Concept DESCRIPTION_LIST_FOR_CONCEPT =
            EntityProxy.Concept.make("description list for concept (SOLOR)", UUID.fromString("ab3e8771-7c7c-5e57-8acf-147b16da36e2"));
    /**
     * Java binding for the concept described as <strong><em>double (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/7172e6ac-a05a-5a34-8275-aef430b18207">
     * 7172e6ac-a05a-5a34-8275-aef430b18207</a>}.
     */
    public static final EntityProxy.Concept DOUBLE =
            EntityProxy.Concept.make("double (SOLOR)", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207"));
    /**
     * Java binding for the concept described as <strong><em>float (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fb591801-7b37-525d-980d-98a1c63ceee0">
     * fb591801-7b37-525d-980d-98a1c63ceee0</a>}.
     */
    public static final EntityProxy.Concept FLOAT =
            EntityProxy.Concept.make("float (SOLOR)", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0"));
    /**
     * Java binding for the concept described as <strong><em>logic graph for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/fc2a0662-2396-575b-95f0-e9b38a418620">
     * fc2a0662-2396-575b-95f0-e9b38a418620</a>}.
     */
    public static final EntityProxy.Concept LOGIC_GRAPH_FOR_SEMANTIC =
            EntityProxy.Concept.make("logic graph for semantic (SOLOR)", UUID.fromString("fc2a0662-2396-575b-95f0-e9b38a418620"));
    /**
     * Java binding for the concept described as <strong><em>long (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/dea8cdf1-de75-5991-9791-79714e4a964d">
     * dea8cdf1-de75-5991-9791-79714e4a964d</a>}.
     */
    public static final EntityProxy.Concept LONG =
            EntityProxy.Concept.make("long (SOLOR)", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d"));
    /**
     * Java binding for the concept described as <strong><em>long value for semantic (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/8beae8a8-7a45-52ba-aaf9-16bfa08f4917">
     * 8beae8a8-7a45-52ba-aaf9-16bfa08f4917</a>}.
     */
    public static final EntityProxy.Concept LONG_VALUE_FOR_SEMANTIC =
            EntityProxy.Concept.make("long value for semantic (SOLOR)", UUID.fromString("8beae8a8-7a45-52ba-aaf9-16bfa08f4917"));
    /**
     * Java binding for the concept described as <strong><em>mapping metadata (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9b5de306-e582-58e3-a23a-0dbf49cbdfe7">
     * 9b5de306-e582-58e3-a23a-0dbf49cbdfe7</a>}.
     */
    public static final EntityProxy.Concept MAPPING_METADATA =
            EntityProxy.Concept.make("mapping metadata (SOLOR)", UUID.fromString("9b5de306-e582-58e3-a23a-0dbf49cbdfe7"));
    /**
     * Java binding for the concept described as <strong><em>mapping namespace (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/9b93f811-7b66-5024-bebf-6a7019743e88">
     * 9b93f811-7b66-5024-bebf-6a7019743e88</a>}.
     */
    public static final EntityProxy.Concept MAPPING_NAMESPACE =
            EntityProxy.Concept.make("mapping namespace (SOLOR)", UUID.fromString("9b93f811-7b66-5024-bebf-6a7019743e88"));
    /**
     * Java binding for the concept described as <strong><em>milligram (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/64b14d8e-5893-5927-a165-28d7ea0a1357">
     * 64b14d8e-5893-5927-a165-28d7ea0a1357</a>}.
     */
    public static final EntityProxy.Concept MILLIGRAM =
            EntityProxy.Concept.make("milligram (SOLOR)", UUID.fromString("64b14d8e-5893-5927-a165-28d7ea0a1357"));
    /**
     * Java binding for the concept described as <strong><em>modifier nid for rf2 relationship (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/13c83f6a-d8fa-5644-b51e-351cb6150fea">
     * 13c83f6a-d8fa-5644-b51e-351cb6150fea</a>}.
     */
    public static final EntityProxy.Concept MODIFIER_NID_FOR_RF2_RELATIONSHIP =
            EntityProxy.Concept.make("modifier nid for rf2 relationship (SOLOR)", UUID.fromString("13c83f6a-d8fa-5644-b51e-351cb6150fea"));
    /**
     * Java binding for the concept described as <strong><em>nid (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d1a17272-9785-51aa-8bde-cc556ab32ebb">
     * d1a17272-9785-51aa-8bde-cc556ab32ebb</a>}.
     */
    public static final EntityProxy.Concept NID =
            EntityProxy.Concept.make("nid (SOLOR)", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb"));
    /**
     * Java binding for the concept described as <strong><em>semantic list for chronicle (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/c809b2c0-9235-5f64-bbda-34210d91bdf8">
     * c809b2c0-9235-5f64-bbda-34210d91bdf8</a>}.
     */
    public static final EntityProxy.Concept SEMANTIC_LIST_FOR_CHRONICLE =
            EntityProxy.Concept.make("semantic list for chronicle (SOLOR)", UUID.fromString("c809b2c0-9235-5f64-bbda-34210d91bdf8"));
    /**
     * Java binding for the concept described as <strong><em>string substitution (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d3b6b681-cd5f-5378-8a1f-f21b50dec56c">
     * d3b6b681-cd5f-5378-8a1f-f21b50dec56c</a>}.
     */
    public static final EntityProxy.Concept STRING_SUBSTITUTION =
            EntityProxy.Concept.make("string substitution (SOLOR)", UUID.fromString("d3b6b681-cd5f-5378-8a1f-f21b50dec56c"));
    /**
     * Java binding for the concept described as <strong><em>target (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/e598e12f-3d39-56ac-be68-4e9fca98fb7a">
     * e598e12f-3d39-56ac-be68-4e9fca98fb7a</a>}.
     */
    public static final EntityProxy.Concept TARGET =
            EntityProxy.Concept.make("target (SOLOR)", UUID.fromString("e598e12f-3d39-56ac-be68-4e9fca98fb7a"));
    /**
     * Java binding for the concept described as <strong><em>users module (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/349161ba-9a6a-5c9c-a78f-281f19cfc057">
     * 349161ba-9a6a-5c9c-a78f-281f19cfc057</a>}.
     */
    public static final EntityProxy.Concept USERS_MODULE =
            EntityProxy.Concept.make("users module (SOLOR)", UUID.fromString("349161ba-9a6a-5c9c-a78f-281f19cfc057"));
    /**
     * Java binding for the concept described as <strong><em>© Informatics, Incorporated (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/f892783f-4aa4-5ba8-a0bf-8a99c4149155">
     * f892783f-4aa4-5ba8-a0bf-8a99c4149155</a>}.
     */
    public static final EntityProxy.Concept C_INFORMATICS_INCORPORATED =
            EntityProxy.Concept.make("© Informatics, Incorporated (SOLOR)", UUID.fromString("f892783f-4aa4-5ba8-a0bf-8a99c4149155"));
    /**
     * Java binding for the concept described as <strong><em>© Regenstrief Institute, Inc. and © The Logical Observation Identifiers Names and Codes LOINC Committee (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/005d1366-7865-5055-9cd7-2b40a0396326">
     * 005d1366-7865-5055-9cd7-2b40a0396326</a>}.
     */
    public static final EntityProxy.Concept C_REGENSTRIEF_INSTITUTE_INC_AND_C_THE_LOGICAL_OBSERVATION_IDENTIFIERS_NAMES_AND_CODES_LOINC_COMMITTEE =
            EntityProxy.Concept.make("© Regenstrief Institute, Inc. and © The Logical Observation Identifiers Names and Codes LOINC Committee (SOLOR)", UUID.fromString("005d1366-7865-5055-9cd7-2b40a0396326"));
    /**
     * Java binding for the concept described as <strong><em>© SNOMED International (SOLOR)</em></strong>;
     * identified by UUID: {@code
     * <a href="http://localhost:8080/terminology/rest/concept/d03b0cc5-dfdf-5580-b162-f2fb0e15eb94">
     * d03b0cc5-dfdf-5580-b162-f2fb0e15eb94</a>}.
     */
    public static final EntityProxy.Concept C_SNOMED_INTERNATIONAL =
            EntityProxy.Concept.make("© SNOMED International (SOLOR)", UUID.fromString("d03b0cc5-dfdf-5580-b162-f2fb0e15eb94"));

    public static final EntityProxy.Concept EXAMPLE_UCUM_UNITS =
            EntityProxy.Concept.make("Example UCUM Units", UUID.fromString("80cd4978-314d-46e3-bc13-9980280ae955"));

    public static final EntityProxy.Concept INFERRED_DEFINITION =
            EntityProxy.Concept.make("Inferred Definition", UUID.fromString("b1abf4dc-9838-4b46-ac55-10c4f92ba10b"));

    public static final EntityProxy.Concept IDENTIFIER_VALUE =
            EntityProxy.Concept.make("Identifier Value", UUID.fromString("b32dd26b-c3fc-487e-987e-16ace71a0d0f"));

    public static final EntityProxy.Concept MAXIMUM_VALUE_OPERATOR =
            EntityProxy.Concept.make("Maximum Value Operator", UUID.fromString("7b8916ab-fd50-41df-8fc2-0b2a7a78be6d"));

    public static final EntityProxy.Concept MINIMUM_VALUE_OPERATOR =
            EntityProxy.Concept.make("Minimum Value Operator", UUID.fromString("ded98e42-f74a-4432-9ae7-01b94dc2fdea"));

    public static final EntityProxy.Concept REFERENCE_RANGE =
            EntityProxy.Concept.make("Reference Range", UUID.fromString("87ce975b-309b-47f4-a6c6-4ae6df6649a1"));

    public static final EntityProxy.Concept REFERENCE_RANGE_MAXIMUM =
            EntityProxy.Concept.make("Reference Range Maximum", UUID.fromString("72d58983-b1e1-4ca9-833f-0e40c1defd39"));

    public static final EntityProxy.Concept REFERENCE_RANGE_MINIMUM =
            EntityProxy.Concept.make("Reference Range Minimum", UUID.fromString("37c35a88-9e27-42ca-b626-186773c4b612"));

    public static final EntityProxy.Concept STATED_DEFINITION =
            EntityProxy.Concept.make("Stated Definition", UUID.fromString("28608bd3-ac73-4fe8-a5f0-1efe0d6650a8"));

    public static final EntityProxy.Concept VALUE_CONSTRAINT =
            EntityProxy.Concept.make("Value Constraint", UUID.fromString("8c55fb86-92d8-42a9-ad70-1e23abbf7eec"));

    public static final EntityProxy.Concept VALUE_CONSTRAINT_SOURCE =
            EntityProxy.Concept.make("Value Constraint Source", UUID.fromString("09aa031a-6290-4ec9-a44c-23928a767da3"));

    public static final EntityProxy.Concept EL_PLUS_PLUS_TERMINOLOGICAL_AXIOMS =
            EntityProxy.Concept.make("EL++ terminological axioms (SOLOR)", UUID.fromString("b3ec50c4-e8cf-4720-b192-31374705f3b7"));

    public static final EntityProxy.Concept TINKAR_MODEL_CONCEPT =
            EntityProxy.Concept.make("Tinkar Model concept (SOLOR)", UUID.fromString("bc59d656-83d3-47d8-9507-0e656ea95463"));

    public static final EntityProxy.Concept CONCEPT_MODEL_DATA_ATTRIBUTE =
            EntityProxy.Concept.make("Concept model data attribute (SOLOR)", UUID.fromString("e418d7a7-2760-3746-ba2e-253b5e383147"));

    public static final EntityProxy.Concept ANNOTATION_PROPERTY_SET =
            EntityProxy.Concept.make("Annotation property set (SOLOR)", UUID.fromString("cb9e33de-f82c-495d-89fa-69afecbcd47d"));

    public static final EntityProxy.Concept DATA_PROPERTY_SET =
            EntityProxy.Concept.make("Data property set (SOLOR)", UUID.fromString("6b8ed642-de72-4aee-953d-42e5db92c0ab"));

    public static final EntityProxy.Concept PROPERTY_SEQUENCE_IMPLICATION =
            EntityProxy.Concept.make("Property sequence implication (SOLOR)", UUID.fromString("9a47a5db-42a6-49ee-9083-54bc305a9456"));

	// Interval property set: [9afc988a-3724-4754-8b74-651426472b19]
	/**
	 * Java binding for the concept described as Interval property set and
	 * identified by the following UUID(s):
	 * <ul>
	 * <li>9afc988a-3724-4754-8b74-651426472b19
	 * </ul>
	 */
	public static final EntityProxy.Concept INTERVAL_PROPERTY_SET = EntityProxy.Concept.make("Interval property set",
			UUID.fromString("9afc988a-3724-4754-8b74-651426472b19"));

	// Interval role: [ed9d3506-65ad-48ea-bd01-95474fecdbc4]
	/**
	 * Java binding for the concept described as Interval role and identified by the
	 * following UUID(s):
	 * <ul>
	 * <li>ed9d3506-65ad-48ea-bd01-95474fecdbc4
	 * </ul>
	 */
	public static final EntityProxy.Concept INTERVAL_ROLE = EntityProxy.Concept.make("Interval role",
			UUID.fromString("ed9d3506-65ad-48ea-bd01-95474fecdbc4"));

	// Interval Role Type: [6fa58611-af37-402e-a0c2-6ee1d6068651]
	/**
	 * Java binding for the concept described as Interval Role Type and identified
	 * by the following UUID(s):
	 * <ul>
	 * <li>6fa58611-af37-402e-a0c2-6ee1d6068651
	 * </ul>
	 */
	public static final EntityProxy.Concept INTERVAL_ROLE_TYPE = EntityProxy.Concept.make("Interval Role Type",
			UUID.fromString("6fa58611-af37-402e-a0c2-6ee1d6068651"));

	// Interval Lower Bound: [52b3e38a-fccb-4779-aa61-4e87abd56419]
	/**
	 * Java binding for the concept described as Interval Lower Bound and identified
	 * by the following UUID(s):
	 * <ul>
	 * <li>52b3e38a-fccb-4779-aa61-4e87abd56419
	 * </ul>
	 */
	public static final EntityProxy.Concept INTERVAL_LOWER_BOUND = EntityProxy.Concept.make("Interval Lower Bound",
			UUID.fromString("52b3e38a-fccb-4779-aa61-4e87abd56419"));

	// Lower Bound Open: [a0096ba1-0718-4c03-ad8f-8143c44091e7]
	public static final EntityProxy.Concept LOWER_BOUND_OPEN = EntityProxy.Concept.make("Lower Bound Open",
			UUID.fromString("a0096ba1-0718-4c03-ad8f-8143c44091e7"));

	// Interval Upper Bound: [6565f774-ff6c-4882-832f-31ddc462adf7]
	/**
	 * Java binding for the concept described as Interval Upper Bound and identified
	 * by the following UUID(s):
	 * <ul>
	 * <li>6565f774-ff6c-4882-832f-31ddc462adf7
	 * </ul>
	 */
	public static final EntityProxy.Concept INTERVAL_UPPER_BOUND = EntityProxy.Concept.make("Interval Upper Bound",
			UUID.fromString("6565f774-ff6c-4882-832f-31ddc462adf7"));

	// Upper Bound Open: [c20b3b1e-112f-4cb2-b901-4046db844629]
	public static final EntityProxy.Concept UPPER_BOUND_OPEN = EntityProxy.Concept.make("Upper Bound Open",
			UUID.fromString("c20b3b1e-112f-4cb2-b901-4046db844629"));

	// Unit of Measure: [40afdda5-89d6-4b80-8181-1ddd6eb92dc8]
	/**
	 * Java binding for the concept described as Unit of Measure and identified by
	 * the following UUID(s):
	 * <ul>
	 * <li>40afdda5-89d6-4b80-8181-1ddd6eb92dc8
	 * </ul>
	 */
	public static final EntityProxy.Concept UNIT_OF_MEASURE = EntityProxy.Concept.make("Unit of Measure",
			UUID.fromString("40afdda5-89d6-4b80-8181-1ddd6eb92dc8"));

    /**
     * ConceptProxy for: "Tinkar root concept".
     */
    public static EntityProxy.Concept ROOT_VERTEX = EntityProxy.Concept.make("Root vertex", UUID.fromString("7c21b6c5-cf11-5af9-893b-743f004c97f5"));

}
