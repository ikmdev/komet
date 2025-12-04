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

/**
 * <h2>Tinkar Entity System</h2>
 *
 * <p>Provides the core entity model for representing versioned, immutable knowledge artifacts in
 * the Tinkar framework. This package defines the four fundamental entity types that form the
 * foundation of all knowledge representation: Concepts, Semantics, Patterns, and STAMPs.</p>
 *
 * <h3>Core Design Principles</h3>
 *
 * <ul>
 * <li><strong>Immutability</strong> - All entities and versions are immutable, ensuring thread safety
 * and enabling safe caching and concurrent access</li>
 * <li><strong>Versioning</strong> - Every entity maintains a complete chronology of all versions,
 * supporting temporal queries and audit trails</li>
 * <li><strong>Provenance</strong> - All changes carry STAMP metadata (Status, Time, Author, Module, Path)
 * for complete change attribution</li>
 * <li><strong>Distributed Identity</strong> - Entities use both local NIDs (Native IDs) and universal
 * PublicIds (UUIDs) for efficient local access and global uniqueness</li>
 * <li><strong>Semantic Extensibility</strong> - Patterns and semantics enable flexible, schema-driven
 * knowledge modeling without schema migration</li>
 * </ul>
 *
 * <h3>The Four Entity Types</h3>
 *
 * <h4>1. ConceptEntity - Representing Ideas and Meanings</h4>
 * <p>{@link dev.ikm.tinkar.entity.ConceptEntity} represents concepts—the fundamental ideas, terms,
 * and classifications in the knowledge graph. Examples include "Pneumonia", "Patient", "Qualifier value",
 * or "SNOMED CT Core Module".</p>
 *
 * <p>Key characteristics:</p>
 * <ul>
 * <li>Represent abstract meanings and ideas</li>
 * <li>Have no inherent structure beyond identity and versions</li>
 * <li>Gain meaning through semantic annotations (descriptions, axioms, relationships)</li>
 * <li>Serve as the foundation for terminology and classification</li>
 * <li>Can be referenced by semantics to receive additional meaning</li>
 * </ul>
 *
 * <pre>{@code
 * // Access a concept entity
 * ConceptEntity pneumonia = Entity.getConceptForNid(pneumoniaNid);
 *
 * // Get latest version
 * StampCalculator calc = StampCalculatorWithCache.getCalculator(stampCoord);
 * Latest<ConceptVersion> latest = calc.latest(pneumonia);
 * }</pre>
 *
 * <h4>2. SemanticEntity - Adding Meaning Through Annotation</h4>
 * <p>{@link dev.ikm.tinkar.entity.SemanticEntity} represents semantic annotations that add meaning
 * to other entities. Semantics reference a component (concept, pattern, or another semantic) and
 * conform to a pattern that defines their structure.</p>
 *
 * <p>Key characteristics:</p>
 * <ul>
 * <li><strong>Reference a Component</strong> - Every semantic references exactly one entity
 * (concept, pattern, or semantic)</li>
 * <li><strong>Conform to a Pattern</strong> - Structure defined by a PatternEntity</li>
 * <li><strong>Carry Field Values</strong> - Data fields as specified by the pattern</li>
 * <li><strong>Extensible Meaning</strong> - Add descriptions, axioms, relationships, or any
 * structured data to entities</li>
 * <li><strong>Recursive Structure</strong> - Semantics can reference other semantics, enabling
 * nested annotations (e.g., dialect information on descriptions)</li>
 * </ul>
 *
 * <p>Common semantic types:</p>
 * <ul>
 * <li><strong>Descriptions</strong> - Human-readable names for concepts (e.g., "Pneumonia",
 * "Inflammation of lung")</li>
 * <li><strong>Axioms</strong> - Logical definitions using description logic (e.g., stating that
 * pneumonia is a kind of lung disease)</li>
 * <li><strong>Relationships</strong> - Associations between concepts (e.g., "finding site" → "lung")</li>
 * <li><strong>Dialects</strong> - Language variants for descriptions (e.g., US English acceptability)</li>
 * <li><strong>Mappings</strong> - Cross-references to external terminologies</li>
 * </ul>
 *
 * <pre>{@code
 * // Get semantics for a concept
 * ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 * Entity.provider().forEachSemanticForComponent(conceptNid, semantic -> {
 *     // semantic is a SemanticEntity
 *     PatternEntity pattern = semantic.pattern();
 *     Entity referencedComponent = semantic.referencedComponent();
 *     ImmutableList<Object> fieldValues = semantic.lastVersion().fieldValues();
 *
 *     // Process semantic based on pattern
 *     if (pattern.nid() == TinkarTerm.DESCRIPTION_PATTERN.nid()) {
 *         String descriptionText = (String) fieldValues.get(0);
 *         // ... process description
 *     }
 * });
 * }</pre>
 *
 * <h4>3. PatternEntity - Defining Semantic Structure</h4>
 * <p>{@link dev.ikm.tinkar.entity.PatternEntity} defines the schema for semantic entities. Patterns
 * specify what fields a semantic has, their data types, meanings, and purposes.</p>
 *
 * <p>Key characteristics:</p>
 * <ul>
 * <li><strong>Schema Definition</strong> - Define structure for semantic entities</li>
 * <li><strong>Field Definitions</strong> - Specify field meanings, data types, and purposes</li>
 * <li><strong>Organizing Element</strong> - Group semantics by purpose (e.g., all descriptions,
 * all axioms)</li>
 * <li><strong>Enable Queries</strong> - Find all semantics conforming to a pattern</li>
 * <li><strong>Support Evolution</strong> - Patterns can be versioned to support schema changes</li>
 * </ul>
 *
 * <p>Pattern field definitions include:</p>
 * <ul>
 * <li><strong>Meaning</strong> - Concept identifying what the field represents</li>
 * <li><strong>Data Type</strong> - String, Integer, Concept reference, DiTree, etc.</li>
 * <li><strong>Purpose</strong> - How the field is used (e.g., DESCRIPTION_TEXT)</li>
 * </ul>
 *
 * <pre>{@code
 * // Example: Description Pattern defines structure for descriptions
 * PatternEntity descriptionPattern = Entity.getPatternForNid(
 *     TinkarTerm.DESCRIPTION_PATTERN.nid()
 * );
 *
 * PatternEntityVersion patternVersion = descriptionPattern.lastVersion();
 * ImmutableList<FieldDefinitionForEntity> fieldDefs = patternVersion.fieldDefinitions();
 *
 * // Field 0: Description text (String)
 * // Field 1: Description type (Concept - FQN, Regular Name, etc.)
 * // Field 2: Language (Concept - English, Spanish, etc.)
 * // Field 3: Case significance (Concept)
 * }</pre>
 *
 * <h4>4. StampEntity - Change Metadata</h4>
 * <p>{@link dev.ikm.tinkar.entity.StampEntity} represents the provenance metadata for every version
 * of every entity. STAMP stands for Status, Time, Author, Module, Path.</p>
 *
 * <p>STAMP components:</p>
 * <ul>
 * <li><strong>Status</strong> - ACTIVE or INACTIVE lifecycle state</li>
 * <li><strong>Time</strong> - Timestamp of version creation</li>
 * <li><strong>Author</strong> - Concept identifying who made the change</li>
 * <li><strong>Module</strong> - Concept identifying organizational module</li>
 * <li><strong>Path</strong> - Concept identifying development/release path</li>
 * </ul>
 *
 * <pre>{@code
 * // Access STAMP for a version
 * ConceptVersion version = latest.get();
 * int stampNid = version.stampNid();
 * StampEntity stamp = Entity.getStamp(stampNid);
 *
 * State state = stamp.state();        // ACTIVE or INACTIVE
 * long time = stamp.time();            // Timestamp
 * int authorNid = stamp.authorNid();   // Who
 * int moduleNid = stamp.moduleNid();   // What module
 * int pathNid = stamp.pathNid();       // What path
 * }</pre>
 *
 * <h3>Entity Identity: NIDs and PublicIds</h3>
 *
 * <p>Entities use a dual identity system:</p>
 *
 * <dl>
 * <dt><strong>NID (Native ID)</strong></dt>
 * <dd>Local integer identifier for efficient in-memory operations. NIDs are:
 * <ul>
 *   <li>Negative integers (positive integers reserved for other uses)</li>
 *   <li>Unique within a single knowledge base instance</li>
 *   <li>Fast for lookups, comparisons, and storage</li>
 *   <li>Not portable across knowledge base instances</li>
 * </ul>
 * </dd>
 *
 * <dt><strong>PublicId</strong></dt>
 * <dd>Universally unique identifier based on UUIDs. PublicIds are:
 * <ul>
 *   <li>Globally unique across all knowledge base instances</li>
 *   <li>Used for distribution, export, and synchronization</li>
 *   <li>Can contain multiple UUIDs for identity merging</li>
 *   <li>Convertible to/from NIDs via the PrimitiveData service</li>
 * </ul>
 * </dd>
 * </dl>
 *
 * <pre>{@code
 * // NID-based access (fast, local)
 * ConceptEntity concept = Entity.getFast(nid);
 *
 * // PublicId-based access (universal)
 * PublicId publicId = Entity.provider().publicId(nid);
 * Optional<Integer> nidOpt = Entity.provider().nidForPublicId(publicId);
 * }</pre>
 *
 * <h3>Entity Versioning and Chronology</h3>
 *
 * <p>Every entity is a <strong>chronology</strong>—a collection of versions ordered by time:</p>
 *
 * <ul>
 * <li>Entities maintain <strong>all versions</strong> ever created</li>
 * <li>Versions are <strong>immutable</strong>—never modified or deleted</li>
 * <li>New changes create <strong>new versions</strong> with new STAMP metadata</li>
 * <li><strong>Latest version</strong> determined by STAMP coordinates</li>
 * <li><strong>Temporal queries</strong> can access any historical version</li>
 * </ul>
 *
 * <pre>{@code
 * ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 *
 * // Get all versions (complete history)
 * ImmutableList<ConceptVersion> allVersions = concept.versions();
 *
 * // Get version at specific point in time
 * StampCoordinateRecord historicStamp = StampCoordinateRecord.make(
 *     StateSet.ACTIVE,
 *     StampPositionRecord.make(historicTime, path),
 *     IntIds.set.empty()
 * );
 * StampCalculator calc = StampCalculatorWithCache.getCalculator(historicStamp);
 * Latest<ConceptVersion> historicVersion = calc.latest(concept);
 * }</pre>
 *
 * <h3>Semantic Organization via Patterns</h3>
 *
 * <p>Patterns serve as the organizing element for semantics, enabling efficient queries and
 * structured knowledge representation:</p>
 *
 * <pre>{@code
 * // Find all descriptions for a concept
 * Entity.provider().forEachSemanticForComponentOfPattern(
 *     conceptNid,
 *     TinkarTerm.DESCRIPTION_PATTERN.nid(),
 *     semantic -> {
 *         // Process each description semantic
 *         SemanticEntityVersion version = semantic.lastVersion();
 *         String text = (String) version.fieldValues().get(0);
 *         int typeNid = (Integer) version.fieldValues().get(1);
 *     }
 * );
 *
 * // Find all axioms for a concept
 * Entity.provider().forEachSemanticForComponentOfPattern(
 *     conceptNid,
 *     TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
 *     semantic -> {
 *         // Process each axiom semantic
 *         DiTreeEntity axiomTree = (DiTreeEntity) semantic.lastVersion().fieldValues().get(0);
 *     }
 * );
 * }</pre>
 *
 * <h3>Recursive Semantic Structure</h3>
 *
 * <p>Semantics can reference other semantics, enabling nested annotations:</p>
 *
 * <pre>{@code
 * // Description semantic references concept
 * SemanticEntity description = ...;
 * Entity referencedConcept = description.referencedComponent();
 *
 * // Dialect semantic references description semantic
 * SemanticEntity dialect = ...;
 * Entity referencedDescription = dialect.referencedComponent();
 * // referencedDescription is itself a SemanticEntity
 *
 * // Find top-level component (concept or pattern)
 * Entity topComponent = semantic.topEnclosingComponent();
 * // Walks up the semantic chain to find the ultimate non-semantic component
 * }</pre>
 *
 * <h3>Entity Access Patterns</h3>
 *
 * <h4>Direct Access (Recommended: Use EntityHandle)</h4>
 * <pre>{@code
 * // Type-safe access via EntityHandle
 * ConceptEntity concept = EntityHandle.forNid(conceptNid)
 *     .concept()
 *     .orElseThrow();
 *
 * PatternEntity pattern = EntityHandle.forNid(patternNid)
 *     .pattern()
 *     .orElseThrow();
 * }</pre>
 *
 * <h4>Legacy Direct Access (Deprecated)</h4>
 * <pre>{@code
 * // Legacy static methods (being phased out)
 * ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 * PatternEntity pattern = Entity.getPatternForNid(patternNid);
 * Entity entity = Entity.getFast(nid);  // Returns appropriate subtype
 * }</pre>
 *
 * <h4>Provider-Based Access</h4>
 * <pre>{@code
 * // Access via EntityService provider
 * EntityService provider = Entity.provider();
 * Entity entity = provider.getEntityFast(nid);
 *
 * // Iterate over all entities
 * provider.forEachEntity(entity -> {
 *     // Process each entity
 * });
 * }</pre>
 *
 * <h3>Entity Construction and Modification</h3>
 *
 * <p>Entities are immutable, so "modification" means creating new versions:</p>
 *
 * <pre>{@code
 * // Create new concept
 * ConceptRecord newConcept = ConceptRecord.build(
 *     publicId,
 *     stampNid
 * );
 *
 * // Create new concept version (modification)
 * ConceptVersionRecord newVersion = ConceptVersionRecord.build(
 *     conceptNid,
 *     newStampNid
 * );
 *
 * // Create semantic to annotate concept
 * SemanticRecord description = SemanticRecord.build(
 *     publicId,
 *     TinkarTerm.DESCRIPTION_PATTERN.nid(),
 *     conceptNid,  // references concept
 *     stampNid,
 *     fieldValues  // [text, type, language, case]
 * );
 * }</pre>
 *
 * <h3>Entity Lifecycle and State</h3>
 *
 * <p>Entities and their versions can be ACTIVE or INACTIVE:</p>
 *
 * <ul>
 * <li><strong>ACTIVE</strong> - Current, valid knowledge</li>
 * <li><strong>INACTIVE</strong> - Deprecated, retired, or erroneous content</li>
 * <li>State is part of STAMP metadata, so different versions can have different states</li>
 * <li>STAMP coordinates control which states are visible in queries</li>
 * </ul>
 *
 * <pre>{@code
 * // Inactivate a concept (create new inactive version)
 * StampEntity inactiveStamp = StampRecord.build(
 *     State.INACTIVE,
 *     System.currentTimeMillis(),
 *     authorNid,
 *     moduleNid,
 *     pathNid
 * );
 *
 * ConceptVersionRecord inactiveVersion = ConceptVersionRecord.build(
 *     conceptNid,
 *     inactiveStamp.nid()
 * );
 *
 * // Query will see concept as inactive with appropriate STAMP coordinate
 * }</pre>
 *
 * <h3>Performance Considerations</h3>
 *
 * <ul>
 * <li><strong>Use NIDs for internal operations</strong> - Much faster than PublicIds</li>
 * <li><strong>Cache entities when reusing</strong> - Entity.getFast() is optimized but not free</li>
 * <li><strong>Use STAMP calculators</strong> - Built-in caching for version resolution</li>
 * <li><strong>Batch semantic queries</strong> - forEachSemanticForComponent more efficient
 * than individual lookups</li>
 * <li><strong>Consider lastVersion() carefully</strong> - Use STAMP calculators when possible
 * for proper version resolution</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All entity types are immutable and thread-safe:</p>
 * <ul>
 * <li>Safe to share entities across threads</li>
 * <li>Safe to cache entities in static fields</li>
 * <li>No synchronization needed for entity access</li>
 * <li>Version lists are immutable collections</li>
 * </ul>
 *
 * <h3>Key Subpackages</h3>
 *
 * <ul>
 * <li>{@link dev.ikm.tinkar.entity.graph} - Graph structures for axioms and navigation</li>
 * <li>{@link dev.ikm.tinkar.entity.transaction} - Transaction management for entity changes</li>
 * <li>{@link dev.ikm.tinkar.entity.transform} - Entity transformation and conversion utilities</li>
 * <li>{@link dev.ikm.tinkar.entity.export} - Entity export and serialization</li>
 * <li>{@link dev.ikm.tinkar.entity.load} - Entity loading and import</li>
 * <li>{@link dev.ikm.tinkar.entity.aggregator} - Entity aggregation and analysis</li>
 * </ul>
 *
 * @see dev.ikm.tinkar.entity.Entity
 * @see dev.ikm.tinkar.entity.ConceptEntity
 * @see dev.ikm.tinkar.entity.SemanticEntity
 * @see dev.ikm.tinkar.entity.PatternEntity
 * @see dev.ikm.tinkar.entity.StampEntity
 * @see dev.ikm.tinkar.entity.EntityHandle
 * @see dev.ikm.tinkar.entity.EntityService
 * @see dev.ikm.tinkar.coordinate
 */
package dev.ikm.tinkar.entity;
