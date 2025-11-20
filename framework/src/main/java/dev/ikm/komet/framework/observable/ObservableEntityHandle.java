package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A fluent handle for an observable entity that may be absent or one of the four observable entity subtypes
 * (ObservableConcept, ObservableSemantic, ObservablePattern, ObservableStamp).
 * <p>
 * This interface parallels {@link dev.ikm.tinkar.entity.EntityHandle} but operates on observable entities
 * rather than immutable entities. While {@code EntityHandle} works with immutable, thread-safe entity snapshots,
 * {@code ObservableEntityHandle} provides access to JavaFX observable entities backed by a canonical object pool.
 *
 * <h2>Understanding the Observable Entity Paradigm</h2>
 * <p>
 * Three classes work together to provide comprehensive access to observable entities with view-specific filtering:
 * <ol>
 *   <li><b>{@link ObservableEntityHandle}</b> (this interface) - Type-safe entry point for accessing entities
 *       and creating snapshots. Provides fluent API with three patterns: side effects, safe extraction, and
 *       direct assertion.</li>
 *   <li><b>{@link ObservableEntity}</b> - The actual JavaFX-observable wrapper providing property bindings
 *       and change notifications. Use for direct UI binding and reactive programming. Gives access to
 *       <i>all</i> versions across <i>all</i> paths and modules.</li>
 *   <li><b>{@link ObservableEntitySnapshot}</b> - View-specific projection that filters and categorizes
 *       versions according to {@link ViewCalculator} coordinates. Use when you need to determine what's
 *       "current" vs "historic" vs "contradicting" within a specific view context, or for "time travel"
 *       version analysis.</li>
 * </ol>
 *
 * <p><b>Quick Decision Guide</b>
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <caption>Which Class to Use When</caption>
 * <tr>
 *   <th>I Need To...</th>
 *   <th>Use This</th>
 *   <th>Example</th>
 * </tr>
 * <tr>
 *   <td>Get an entity by NID/PublicId safely</td>
 *   <td>{@code ObservableEntityHandle}</td>
 *   <td>{@code ObservableEntityHandle.get(nid).ifConcept(c -> ...)}</td>
 * </tr>
 * <tr>
 *   <td>Work with entity regardless of type</td>
 *   <td>{@code ObservableEntityHandle}</td>
 *   <td>{@code ObservableEntityHandle.get(nid).ifEntity(e -> log(e.nid()))}</td>
 * </tr>
 * <tr>
 *   <td>Bind UI controls to entity properties</td>
 *   <td>{@code ObservableEntity}</td>
 *   <td>{@code label.textProperty().bind(concept.descriptionProperty())}</td>
 * </tr>
 * <tr>
 *   <td>Find "current" version for a view</td>
 *   <td>{@code ObservableEntitySnapshot}</td>
 *   <td>{@code snapshot.getLatestVersion()}</td>
 * </tr>
 * <tr>
 *   <td>Detect version contradictions</td>
 *   <td>{@code ObservableEntitySnapshot}</td>
 *   <td>{@code if (!latest.contradictions().isEmpty())}</td>
 * </tr>
 * <tr>
 *   <td>Show version history ("time travel")</td>
 *   <td>{@code ObservableEntitySnapshot}</td>
 *   <td>{@code snapshot.getHistoricVersions()}</td>
 * </tr>
 * <tr>
 *   <td>React to entity changes</td>
 *   <td>{@code ObservableEntity}</td>
 *   <td>{@code concept.versionProperty().addListener(...)}</td>
 * </tr>
 * </table>
 * <p>
 * <b>Typical Workflow:</b> Use {@code ObservableEntityHandle} to safely retrieve an {@link ObservableEntity},
 * then either bind its properties directly to UI <i>or</i> create an {@link ObservableEntitySnapshot} to
 * analyze versions within a specific view context.
 *
 * <h2>Flexible Entity Access</h2>
 * <p>
 * Observable entities can be accessed using three different identifier types, providing flexibility
 * across different contexts and API boundaries:
 *
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <caption>Entity Access Methods</caption>
 * <tr>
 *   <th>Identifier Type</th>
 *   <th>Use When</th>
 *   <th>Example</th>
 * </tr>
 * <tr>
 *   <td><b>int nid</b></td>
 *   <td>Internal processing, performance-critical paths</td>
 *   <td>{@code ObservableEntityHandle.get(123)}</td>
 * </tr>
 * <tr>
 *   <td><b>PublicId</b></td>
 *   <td>External APIs, UUID-based identification, persistence</td>
 *   <td>{@code ObservableEntityHandle.get(publicId)}</td>
 * </tr>
 * <tr>
 *   <td><b>EntityFacade</b></td>
 *   <td>Working with facades, proxy objects, component abstractions</td>
 *   <td>{@code ObservableEntityHandle.get(entityFacade)}</td>
 * </tr>
 * </table>
 *
 * <p>
 * All three access methods are available for every API method that accepts an entity identifier:
 *
 * <pre>{@code
 * // Using nid (native identifier)
 * ObservableConcept concept1 = ObservableEntityHandle.getConceptOrThrow(123);
 * ObservableEntityHandle.get(123).ifConcept(c -> process(c));
 *
 * // Using PublicId (UUID-based public identifier)
 * PublicId publicId = PublicIds.of(uuid);
 * ObservableConcept concept2 = ObservableEntityHandle.getConceptOrThrow(publicId);
 * ObservableEntityHandle.get(publicId).ifConcept(c -> process(c));
 *
 * // Using EntityFacade (facade/proxy)
 * EntityFacade facade = EntityProxy.make(nid);
 * ObservableConcept concept3 = ObservableEntityHandle.getConceptOrThrow(facade);
 * ObservableEntityHandle.get(facade).ifConcept(c -> process(c));
 *
 * // Snapshots support all three as well
 * ObservableConceptSnapshot snapshot1 =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, calculator);
 * ObservableConceptSnapshot snapshot2 =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(publicId, calculator);
 * ObservableConceptSnapshot snapshot3 =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(facade, calculator);
 * }</pre>
 *
 * <h2>Design Decision: Why Not Generics?</h2>
 * <p>
 * A generic {@code EntityHandle<E extends Entity>} was considered but rejected because:
 * <ul>
 *   <li><b>Different Method Signatures:</b> ObservableEntity requires JavaFX thread checks and has
 *       different return types (Observable* vs Entity*)</li>
 *   <li><b>Lifecycle Differences:</b> Observable entities use weak-referenced singleton pools while
 *       regular entities use standard GC - fundamentally different lifecycles</li>
 *   <li><b>Additional Capabilities:</b> ObservableEntityHandle needs snapshot retrieval methods that
 *       don't apply to regular entities</li>
 *   <li><b>API Clarity:</b> Generic bounds like {@code <E extends Entity, H extends EntityHandle<E>>}
 *       would obscure the simple, fluent API pattern</li>
 *   <li><b>Thread Safety:</b> Observable operations require Platform.runLater() wrapping that doesn't
 *       apply to immutable entities</li>
 * </ul>
 * <p>
 * The parallel interface design maintains clarity while allowing each handle type to provide
 * ecosystem-specific operations (snapshots for Observable, direct access for Entity).
 *
 * <h2>Why a Separate Interface Rather Than Extension?</h2>
 * <p>
 * While {@code ObservableEntity} and {@code Entity} share similar structure (both have Concept, Semantic, Pattern,
 * and Stamp variants), they have fundamentally different characteristics that make a parallel interface design
 * more appropriate than extending {@code EntityHandle}:
 *
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <caption>Comparison: Entity vs ObservableEntity</caption>
 * <tr>
 *   <th>Characteristic</th>
 *   <th>Entity (EntityHandle)</th>
 *   <th>ObservableEntity (ObservableEntityHandle)</th>
 * </tr>
 * <tr>
 *   <td><b>Mutability</b></td>
 *   <td>Immutable records</td>
 *   <td>Mutable with JavaFX properties</td>
 * </tr>
 * <tr>
 *   <td><b>Thread Safety</b></td>
 *   <td>Thread-safe, no restrictions</td>
 *   <td>JavaFX application thread only</td>
 * </tr>
 * <tr>
 *   <td><b>Instance Management</b></td>
 *   <td>Multiple instances allowed</td>
 *   <td>Canonical singleton per nid (weak references)</td>
 * </tr>
 * <tr>
 *   <td><b>Use Case</b></td>
 *   <td>Data snapshots, calculations</td>
 *   <td>UI binding, reactive updates</td>
 * </tr>
 * <tr>
 *   <td><b>Lifecycle</b></td>
 *   <td>Standard GC</td>
 *   <td>Weak reference pool with automatic cleanup</td>
 * </tr>
 * </table>
 *
 * <p>
 * <b>Design Decision Rationale:</b>
 * <ul>
 *   <li><b>Not Extending:</b> Extending {@code EntityHandle} would inherit methods returning wrong types
 *       (Entity instead of ObservableEntity)</li>
 *   <li><b>Not Genericizing:</b> Making {@code EntityHandle<E extends Entity>} would require rewriting
 *       existing code and doesn't cleanly model the Observable/non-Observable dichotomy</li>
 *   <li><b>Parallel Interface:</b> Maintains the proven fluent API pattern while respecting fundamental
 *       differences in threading, mutability, and lifecycle management</li>
 * </ul>
 *
 * <h2>The Three Patterns: When to Use Each</h2>
 * <p>
 * Like {@code EntityHandle}, this interface provides three complementary patterns for type-safe entity processing
 * without manual instanceof checks or casts. Choose the right pattern based on your use case.
 *
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <caption>Observable Entity Access Patterns</caption>
 * <tr>
 *   <th>Pattern</th>
 *   <th>Method</th>
 *   <th>Return Type</th>
 *   <th>Use When</th>
 *   <th>Example</th>
 * </tr>
 * <tr>
 *   <td><b>Side Effects</b></td>
 *   <td>{@code ifConcept()}</td>
 *   <td>{@code ObservableEntityHandle}</td>
 *   <td>Binding to UI, registering listeners</td>
 *   <td>{@code .ifConcept(c -> label.textProperty().bind(c.descriptionProperty()))}</td>
 * </tr>
 * <tr>
 *   <td><b>Safe Extraction</b></td>
 *   <td>{@code asConcept()}</td>
 *   <td>{@code Optional<ObservableConcept>}</td>
 *   <td>Wrong type is a valid possibility</td>
 *   <td>{@code .asConcept().ifPresent(c -> bindToUI(c))}</td>
 * </tr>
 * <tr>
 *   <td><b>Assertion</b></td>
 *   <td>{@code expectConcept()}</td>
 *   <td>{@code ObservableConcept}</td>
 *   <td>Wrong type indicates data corruption</td>
 *   <td>{@code .expectConcept()}</td>
 * </tr>
 * </table>
 *
 * <h2>Pattern 1: Side Effects with {@code ifXxx} Methods</h2>
 * <p>
 * <b>When to use:</b> You want to execute code based on entity type but don't need to return anything.
 * Common for UI bindings and listener registration.
 * <br><b>Returns:</b> {@code this} for chaining.
 * <br><b>Key benefit:</b> Fluent branching for reactive operations.
 *
 * <pre>{@code
 * // General entity access (works with any type)
 * ObservableEntityHandle.get(nid)
 *     .ifEntity(entity -> {
 *         LOG.info("Processing entity: {}", entity.nid());
 *         cache.put(entity.nid(), entity);
 *     })
 *     .ifAbsent(() -> LOG.warn("Entity {} not found", nid));
 *
 * // Bind UI controls to observable properties
 * ObservableEntityHandle.get(nid)
 *     .ifConcept(concept -> {
 *         label.textProperty().bind(concept.descriptionProperty());
 *         statusIcon.visibleProperty().bind(concept.activeProperty());
 *     })
 *     .ifSemantic(semantic -> {
 *         fieldList.itemsProperty().bind(semantic.fieldsProperty());
 *     })
 *     .ifAbsent(() -> showNotFound());

 * // Register listeners for changes
 * ObservableEntityHandle.get(nid)
 *     .ifConcept(concept -> concept.versionProperty().addListener(
 *         (obs, old, newVal) -> refreshUI()
 *     ));
 *
 * // Handle all four types with type-specific operations
 * ObservableEntityHandle.get(nid)
 *     .ifConcept(this::bindConceptToUI)
 *     .ifSemantic(this::bindSemanticToUI)
 *     .ifPattern(this::bindPatternToUI)
 *     .ifStamp(this::bindStampToUI);
 * }</pre>
 *
 * <h2>Pattern 2: Safe Extraction with {@code asXxx} Methods</h2>
 * <p>
 * <b>When to use:</b> Wrong type is a valid possibility, or you need Optional chaining.
 * <br><b>Returns:</b> {@code Optional<ObservableXxx>} - allows standard Optional operations.
 * <br><b>Key benefit:</b> Explicit handling of the "not this type" case.
 *
 * <pre>{@code
 * // Extract and bind if concept
 * public void bindIfConcept(int nid, Label label) {
 *     ObservableEntityHandle.get(nid)
 *         .asConcept()
 *         .ifPresent(concept -> label.textProperty().bind(concept.descriptionProperty()));
 * }
 *
 * // Filter collection for observable concepts
 * List<ObservableConcept> concepts = nids.stream()
 *     .map(ObservableEntityHandle::get)
 *     .map(handle -> handle.asConcept())
 *     .flatMap(Optional::stream)
 *     .toList();
 *
 * // Conditional binding with transformation
 * ObservableEntityHandle.get(nid)
 *     .asSemantic()
 *     .filter(semantic -> semantic.patternNid() == MY_PATTERN)
 *     .ifPresent(semantic -> attachListeners(semantic));
 * }</pre>
 *
 * <h2>Pattern 3: Assertion with {@code expectXxx} Methods</h2>
 * <p>
 * <b>When to use:</b> Entity type is guaranteed by your data model. Wrong type means data corruption.
 * <br><b>Returns:</b> {@code ObservableConcept} (never null) - throws if wrong type.
 * <br><b>Key benefit:</b> Clean, direct access when type is certain. Avoids unsafe casts.
 *
 * <pre>{@code
 * // Field definitions - meaning is ALWAYS a concept per data model
 * public ObservableConcept observableMeaning(int meaningNid) {
 *     return ObservableEntityHandle.get(meaningNid).expectConcept();
 * }
 *
 * // Direct binding with guaranteed type
 * public void bindMeaningDescription(int meaningNid, Label label) {
 *     ObservableConcept meaning = ObservableEntityHandle.get(meaningNid).expectConcept();
 *     label.textProperty().bind(meaning.descriptionProperty());
 * }
 *
 * // UI controller initialization
 * public void initialize(int conceptNid) {
 *     ObservableConcept concept = ObservableEntityHandle.get(conceptNid)
 *         .expectConcept("Controller requires concept entity");
 *
 *     titleLabel.textProperty().bind(concept.descriptionProperty());
 *     statusLabel.textProperty().bind(concept.stateProperty());
 *     versionList.itemsProperty().bind(concept.versionsProperty());
 * }
 * }</pre>
 *
 * <h2>Static Convenience Methods</h2>
 * <p>
 * For the common "get by nid and assert type" pattern, static methods combine fetch + assertion:
 *
 * <pre>{@code
 * // Equivalent to ObservableEntityHandle.get(nid).expectConcept()
 * ObservableConcept meaning = ObservableEntityHandle.getConceptOrThrow(meaningNid);
 * ObservableSemantic semantic = ObservableEntityHandle.getSemanticOrThrow(semanticNid);
 * ObservablePattern pattern = ObservableEntityHandle.getPatternOrThrow(patternNid);
 * ObservableStamp stamp = ObservableEntityHandle.getStampOrThrow(stampNid);
 * }</pre>
 *
 * <h2>Working with Snapshots</h2>
 * <p>
 * Observable entities support snapshot retrieval, which provides a point-in-time view of the entity
 * according to a {@link ViewCalculator}'s coordinate configuration. Snapshots include latest, uncommitted,
 * and historic versions. Snapshot methods follow the same three patterns as entity access:
 *
 * <p><b>Snapshot Pattern 1: Side Effects with {@code ifXxxGetSnapshot()}</b>
 * <pre>{@code
 * ViewCalculator calculator = viewCoordinateRecord.calculator();
 *
 * // Execute code if entity is a concept (gets snapshot, then executes consumer)
 * ObservableEntityHandle.get(nid)
 *     .ifConceptGetSnapshot(calculator, snapshot -> {
 *         snapshot.latestVersion().ifPresent(v -> bindToUI(v));
 *         snapshot.uncommittedVersions().forEach(v -> flagUncommitted(v));
 *     });
 *
 * // Chain multiple type checks
 * ObservableEntityHandle.get(nid)
 *     .ifConceptGetSnapshot(calculator, s -> processConceptSnapshot(s))
 *     .ifSemanticGetSnapshot(calculator, s -> processSemanticSnapshot(s));
 * }</pre>
 *
 * <p><b>Snapshot Pattern 2: Safe Extraction with {@code asXxxSnapshot()}</b>
 * <pre>{@code
 * // Extract snapshot if concept, with Optional handling
 * Optional<ObservableConceptSnapshot> maybeSnapshot =
 *     ObservableEntityHandle.get(nid).asConceptSnapshot(calculator);
 *
 * maybeSnapshot.ifPresent(snapshot ->
 *     snapshot.latestVersion().ifPresent(v -> bindToUI(v))
 * );
 *
 * // Filter collection for concept snapshots
 * List<ObservableConceptSnapshot> conceptSnapshots = nids.stream()
 *     .map(ObservableEntityHandle::get)
 *     .map(handle -> handle.asConceptSnapshot(calculator))
 *     .flatMap(Optional::stream)
 *     .toList();
 * }</pre>
 *
 * <p><b>Snapshot Pattern 3: Direct Access with {@code getXxxSnapshotOrThrow()}</b>
 * <pre>{@code
 * // Single-call snapshot retrieval (nid + calculator → snapshot)
 * ObservableConceptSnapshot conceptSnapshot =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, calculator);
 * conceptSnapshot.latestVersion().ifPresent(version -> bindToUI(version));
 *
 * ObservableSemanticSnapshot semanticSnapshot =
 *     ObservableEntityHandle.getSemanticSnapshotOrThrow(semanticNid, calculator);
 * semanticSnapshot.uncommittedVersions().forEach(v -> flagUncommitted(v));
 *
 * ObservablePatternSnapshot patternSnapshot =
 *     ObservableEntityHandle.getPatternSnapshotOrThrow(patternNid, calculator);
 * patternSnapshot.fieldDefinitions().forEach(field -> processField(field));
 *
 * // Generic snapshot retrieval (when type unknown)
 * Optional<ObservableEntitySnapshot<?, ?>> genericSnapshot =
 *     ObservableEntityHandle.getSnapshot(unknownNid, calculator);
 * }</pre>
 *
 * <h2>Thread Safety Requirements</h2>
 * <p>
 * <b>IMPORTANT:</b> All operations must execute on the JavaFX application thread. The underlying
 * {@link ObservableEntity#get(int)} method enforces this requirement. If you need to access
 * observable entities from a background thread, use {@code Platform.runLater()}:
 *
 * <pre>{@code
 * // Background thread access
 * CompletableFuture.runAsync(() -> {
 *     // Do background work...
 * }).thenRun(() -> {
 *     Platform.runLater(() -> {
 *         // Now safe to access observable entities
 *         ObservableEntityHandle.get(nid)
 *             .ifConcept(concept -> bindToUI(concept));
 *     });
 * });
 * }</pre>
 *
 * <h2>Canonical Instance Guarantee</h2>
 * <p>
 * Due to the {@link ObservableEntity#CANONICAL_INSTANCES} cache, multiple calls to {@code get(nid)} for the same
 * nid return handles to the <b>same observable instance</b> (while strong references exist). This ensures:
 * <ul>
 *   <li>All UI components observe the same object</li>
 *   <li>Changes propagate to all bound properties</li>
 *   <li>Memory efficient - no duplicate observable wrappers</li>
 * </ul>
 * <p>
 * Note: The cache uses weak references, so instances may be garbage collected when no longer strongly
 * referenced. A subsequent call would create a new instance, but while any instance exists, all access
 * returns that same canonical instance.
 *
 * <h2>Choosing the Right Pattern</h2>
 *
 * <p><b>Ask yourself:</b>
 * <ol>
 *   <li><b>Am I binding to UI or registering listeners?</b>
 *     <ul>
 *       <li>Yes → Use {@code ifXxx()} for side effects</li>
 *       <li>No → Continue to question 2</li>
 *     </ul>
 *   </li>
 *   <li><b>Is wrong type a valid possibility?</b>
 *     <ul>
 *       <li>Yes (user input, filtering) → Use {@code asXxx()} with Optional handling</li>
 *       <li>No (data model guarantee) → Use {@code expectXxx()} for direct access</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <h2>Implementation</h2>
 * <p>
 * Implementors need only provide {@link #entity()} which returns an {@link Optional}
 * containing the observable entity (or empty if absent). All fluent methods are provided as
 * default implementations.
 *
 * @see ObservableEntity
 * @see ObservableConcept
 * @see ObservableSemantic
 * @see ObservablePattern
 * @see ObservableStamp
 * @see dev.ikm.tinkar.entity.EntityHandle
 */
public interface ObservableEntityHandle {

    // ========== Core Method - Must Be Implemented ==========
    
    /**
     * Returns the observable entity as an Optional, regardless of its specific type.
     * <p>
     * This is the core method that all other methods build upon. Use this when you need access
     * to the entity without caring about its specific type (Concept, Semantic, Pattern, or Stamp).
     *
     * @return Optional containing the entity if present, empty otherwise
     */
    Optional<ObservableEntity<?>> entity();

    // ========== Pattern 1: Side Effects - General Entity Access ==========

    /**
     * Executes the consumer if an entity is present, regardless of type.
     * <p>
     * Use this when you want to perform operations that work on any entity type,
     * such as logging, caching, or accessing common entity properties (nid, publicId, versions).
     *
     * <pre>{@code
     * // Log any entity
     * ObservableEntityHandle.get(nid)
     *     .ifEntity(entity -> LOG.info("Found entity: {}", entity.nid()));
     *
     * // Cache any entity type
     * ObservableEntityHandle.get(nid)
     *     .ifEntity(entity -> cache.put(entity.nid(), entity))
     *     .ifAbsent(() -> LOG.warn("Entity {} not found", nid));
     * }</pre>
     *
     * @param consumer the code to execute with the entity
     * @return this handle for fluent chaining
     */
    default ObservableEntityHandle ifEntity(Consumer<ObservableEntity<?>> consumer) {
        entity().ifPresent(consumer);
        return this;
    }

    /**
     * Executes the runnable if no entity is present.
     * <p>
     * This complements the {@code ifXxx} methods by handling the absent case. Use this
     * for error handling, logging, or default behavior when an entity is not found.
     *
     * <pre>{@code
     * // Handle missing entity
     * ObservableEntityHandle.get(nid)
     *     .ifConcept(concept -> bindToUI(concept))
     *     .ifSemantic(semantic -> bindToUI(semantic))
     *     .ifAbsent(() -> showNotFoundMessage());
     *
     * // Log missing entities
     * ObservableEntityHandle.get(userInputNid)
     *     .ifEntity(entity -> processEntity(entity))
     *     .ifAbsent(() -> LOG.warn("Entity {} not found", userInputNid));
     * }</pre>
     *
     * @param runnable the code to execute if entity is absent
     * @return this handle for fluent chaining
     */
    default ObservableEntityHandle ifAbsent(Runnable runnable) {
        if (entity().isEmpty()) {
            runnable.run();
        }
        return this;
    }

    // ========== Pattern 2: Safe Extraction - General Entity Access ==========

    /**
     * Returns this handle's entity as an {@link Optional}.
     * <p>
     * This is a convenience method that simply returns {@link #entity()}, providing symmetry
     * with the {@code asXxx()} pattern used for type-specific extraction. Use this when you
     * want Optional-based extraction of the entity regardless of type.
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Extract entity with Optional chaining
     * Optional<ObservableEntity<?>> maybeEntity = ObservableEntityHandle.get(nid).asEntity();
     * maybeEntity.ifPresent(entity -> process(entity));
     *
     * // Filter and map
     * String description = ObservableEntityHandle.get(nid)
     *     .asEntity()
     *     .filter(entity -> entity.versions().size() > 1)
     *     .map(entity -> "Multi-version entity: " + entity.nid())
     *     .orElse("Single or no version");
     *
     * // Extract entity or provide default
     * ObservableEntity<?> entity = ObservableEntityHandle.get(nid)
     *     .asEntity()
     *     .orElseThrow(() -> new IllegalStateException("Entity required"));
     *
     * // Stream processing
     * List<ObservableEntity<?>> entities = nids.stream()
     *     .map(ObservableEntityHandle::get)
     *     .map(handle -> handle.asEntity())
     *     .flatMap(Optional::stream)
     *     .toList();
     * }</pre>
     *
     * @return Optional containing the entity if present, empty otherwise
     * @see #entity() for direct access to the same Optional
     * @see #expectEntity() for assertion-based access that throws if absent
     * @see #ifEntity(Consumer) for side-effect operations
     */
    default Optional<ObservableEntity<?>> asEntity() {
        return entity();
    }

    // entity() method is the safe extraction for general entity access

    // ========== Pattern 3: Assertion - General Entity Access ==========

    /**
     * Returns the entity or throws if absent, regardless of specific type.
     * <p>
     * Use this when you know an entity must exist but don't need a specific type.
     * Throws {@link NoSuchElementException} if the entity is not found.
     *
     * <pre>{@code
     * // Get any entity type (will throw if absent)
     * ObservableEntity<?> entity = ObservableEntityHandle.get(nid).expectEntity();
     * LOG.info("Entity nid: {}, type: {}", entity.nid(), entity.getClass().getSimpleName());
     *
     * // Use common entity operations
     * ObservableEntity<?> entity = ObservableEntityHandle.get(nid).expectEntity();
     * ImmutableList<?> versions = entity.versions();
     * }</pre>
     *
     * @return the entity (never null)
     * @throws NoSuchElementException if the entity is absent
     */
    default ObservableEntity<?> expectEntity() {
        return entity().orElseThrow(() ->
            new NoSuchElementException("Expected entity to be present"));
    }

    /**
     * Returns the entity or throws with a custom message if absent.
     * <p>
     * Use this when you want more descriptive error messages for debugging.
     *
     * <pre>{@code
     * ObservableEntity<?> entity = ObservableEntityHandle.get(nid)
     *     .expectEntity("Required entity for initialization");
     * }</pre>
     *
     * @param message the exception message if entity is absent
     * @return the entity (never null)
     * @throws NoSuchElementException if the entity is absent
     */
    default ObservableEntity<?> expectEntity(String message) {
        return entity().orElseThrow(() -> new NoSuchElementException(message));
    }


    // ========== Static Factory Methods ==========

    /**
     * Retrieves an observable entity by nid and returns a fluent handle for type-safe processing.
     * <p>
     * The returned handle allows chaining type-specific operations without manual
     * instanceof checks or casts. Due to the canonical object pool, multiple calls with
     * the same nid return handles to the same observable instance.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param nid the native identifier
     * @return an ObservableEntityHandle representing the entity, or an empty handle if absent
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableEntityHandle get(int nid) {
        try {
            ObservableEntity<?> entity = ObservableEntity.packagePrivateGet(nid);
            return entity != null ? of(entity) : absent();
        } catch (RuntimeException e) {
            // Entity not found or thread check failed
            return absent();
        }
    }

    /**
     * Retrieves an observable entity by PublicId and returns a fluent handle for type-safe processing.
     * <p>
     * Converts the PublicId to nid and retrieves the entity. Due to the canonical object pool,
     * multiple calls with the same PublicId return handles to the same observable instance.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param publicId the public identifier
     * @return an ObservableEntityHandle representing the entity, or an empty handle if absent
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableEntityHandle get(PublicId publicId) {
        return get(PrimitiveData.nid(publicId));
    }

    /**
     * Retrieves an observable entity by EntityFacade and returns a fluent handle for type-safe processing.
     * <p>
     * Extracts the nid from the EntityFacade and retrieves the entity. Due to the canonical object pool,
     * multiple calls with the same EntityFacade return handles to the same observable instance.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param entityFacade the entity facade
     * @return an ObservableEntityHandle representing the entity, or an empty handle if absent
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableEntityHandle get(EntityFacade entityFacade) {
        return get(entityFacade.nid());
    }

    /**
     * Returns a singleton handle representing an absent observable entity.
     * <p>
     * Use this when you need to explicitly represent absence.
     *
     * @return the absent observable entity handle singleton
     */
    static ObservableEntityHandle absent() {
        return AbsentHandle.INSTANCE;
    }

    /**
     * Creates a handle for an observable entity.
     *
     * @param entity the observable entity to wrap
     * @return a handle wrapping the entity
     */
    static ObservableEntityHandle of(ObservableEntity<?> entity) {
        if (entity == null) {
            return absent();
        }
        return new PresentHandle(entity);
    }

    // ========== Default Implementation: Fluent Type Matching ==========

    /**
     * If entity is present and is an {@link ObservableConcept}, executes the consumer.
     * <p>
     * No casting required - consumer receives ObservableConcept directly.
     * Returns {@code this} for chaining regardless of whether consumer executed.
     *
     * @param consumer the action to perform on the ObservableConcept
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifConcept(Consumer<ObservableConcept> consumer) {
        entity().ifPresent(e -> {
            if (e instanceof ObservableConcept concept) {
                consumer.accept(concept);
            }
        });
        return this;
    }

    /**
     * If entity is present and is an {@link ObservableSemantic}, executes the consumer.
     *
     * @param consumer the action to perform on the ObservableSemantic
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifSemantic(Consumer<ObservableSemantic> consumer) {
        entity().ifPresent(e -> {
            if (e instanceof ObservableSemantic semantic) {
                consumer.accept(semantic);
            }
        });
        return this;
    }

    /**
     * If entity is present and is an {@link ObservablePattern}, executes the consumer.
     *
     * @param consumer the action to perform on the ObservablePattern
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifPattern(Consumer<ObservablePattern> consumer) {
        entity().ifPresent(e -> {
            if (e instanceof ObservablePattern pattern) {
                consumer.accept(pattern);
            }
        });
        return this;
    }

    /**
     * If entity is present and is an {@link ObservableStamp}, executes the consumer.
     *
     * @param consumer the action to perform on the ObservableStamp
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifStamp(Consumer<ObservableStamp> consumer) {
        entity().ifPresent(e -> {
            if (e instanceof ObservableStamp stamp) {
                consumer.accept(stamp);
            }
        });
        return this;
    }

    // ========== Default Implementation: OrElse Methods ==========

    /**
     * If entity is an {@link ObservableConcept}, executes the consumer, otherwise executes elseAction.
     *
     * @param consumer the action to perform if entity is a Concept
     * @param elseAction the action to perform otherwise (absent or wrong type)
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifConceptOrElse(Consumer<ObservableConcept> consumer, Runnable elseAction) {
        Optional<ObservableEntity<?>> opt = entity();
        if (opt.isPresent() && opt.get() instanceof ObservableConcept concept) {
            consumer.accept(concept);
        } else {
            elseAction.run();
        }
        return this;
    }

    /**
     * If entity is an {@link ObservableSemantic}, executes the consumer, otherwise executes elseAction.
     *
     * @param consumer the action to perform if entity is a Semantic
     * @param elseAction the action to perform otherwise (absent or wrong type)
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifSemanticOrElse(Consumer<ObservableSemantic> consumer, Runnable elseAction) {
        Optional<ObservableEntity<?>> opt = entity();
        if (opt.isPresent() && opt.get() instanceof ObservableSemantic semantic) {
            consumer.accept(semantic);
        } else {
            elseAction.run();
        }
        return this;
    }

    /**
     * If entity is an {@link ObservablePattern}, executes the consumer, otherwise executes elseAction.
     *
     * @param consumer the action to perform if an entity is a Pattern
     * @param elseAction the action to perform otherwise (absent or wrong type)
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifPatternOrElse(Consumer<ObservablePattern> consumer, Runnable elseAction) {
        Optional<ObservableEntity<?>> opt = entity();
        if (opt.isPresent() && opt.get() instanceof ObservablePattern pattern) {
            consumer.accept(pattern);
        } else {
            elseAction.run();
        }
        return this;
    }

    /**
     * If entity is an {@link ObservableStamp}, executes the consumer, otherwise executes elseAction.
     *
     * @param consumer the action to perform if entity is a Stamp
     * @param elseAction the action to perform otherwise (absent or wrong type)
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifStampOrElse(Consumer<ObservableStamp> consumer, Runnable elseAction) {
        Optional<ObservableEntity<?>> opt = entity();
        if (opt.isPresent() && opt.get() instanceof ObservableStamp stamp) {
            consumer.accept(stamp);
        } else {
            elseAction.run();
        }
        return this;
    }

    // ========== Default Implementation: General Presence Methods ==========

    /**
     * If entity is present (any type), executes the consumer with the entity.
     *
     * @param consumer the action to perform on the entity
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifPresent(Consumer<ObservableEntity<?>> consumer) {
        entity().ifPresent(consumer);
        return this;
    }

    /**
     * If the entity is present, executes presentAction, otherwise executes absentAction.
     *
     * @param presentAction the action to perform if entity is present
     * @param absentAction the action to perform if entity is absent
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifPresentOrElse(Runnable presentAction, Runnable absentAction) {
        if (entity().isPresent()) {
            presentAction.run();
        } else {
            absentAction.run();
        }
        return this;
    }

    // ========== Snapshot Retrieval Methods - Side Effects Pattern (ifXxxGetSnapshot) ==========

    /**
     * If this entity is a concept, gets its snapshot and executes the consumer.
     * <p>
     * Returns {@code this} for chaining regardless of whether consumer executed.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservableEntityHandle.get(nid)
     *     .ifConceptGetSnapshot(calculator, snapshot ->
     *         snapshot.latestVersion().ifPresent(v -> bindToUI(v))
     *     );
     * }</pre>
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @param consumer the action to perform on the ObservableConceptSnapshot
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifConceptGetSnapshot(ViewCalculator viewCalculator,
                                                         Consumer<ObservableConceptSnapshot> consumer) {
        asConcept().ifPresent(concept -> consumer.accept(concept.getSnapshot(viewCalculator)));
        return this;
    }

    /**
     * If this entity is a semantic, gets its snapshot and executes the consumer.
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @param consumer the action to perform on the ObservableSemanticSnapshot
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifSemanticGetSnapshot(ViewCalculator viewCalculator,
                                                          Consumer<ObservableSemanticSnapshot> consumer) {
        asSemantic().ifPresent(semantic -> consumer.accept(semantic.getSnapshot(viewCalculator)));
        return this;
    }

    /**
     * If this entity is a pattern, gets its snapshot and executes the consumer.
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @param consumer the action to perform on the ObservablePatternSnapshot
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifPatternGetSnapshot(ViewCalculator viewCalculator,
                                                         Consumer<ObservablePatternSnapshot> consumer) {
        asPattern().ifPresent(pattern -> consumer.accept(pattern.getSnapshot(viewCalculator)));
        return this;
    }

    // ========== Snapshot Retrieval Methods - Side Effects Pattern (ifStampGetSnapshot) ==========

    /**
     * If this entity is a stamp, gets its snapshot and executes the consumer.
     * <p>
     * Returns {@code this} for chaining regardless of whether consumer executed.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     * <p>
     * <b>Note:</b> Stamp snapshots have simplified semantics compared to other entity types
     * because stamps represent immutable change metadata (Status, Time, Author, Module, Path).
     * Most stamps have a single version with no contradictions. Historic versions are rare and
     * typically indicate metadata corrections after creation.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservableEntityHandle.get(stampNid)
     *     .ifStampGetSnapshot(calculator, snapshot -> {
     *         Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
     *         latest.ifPresent(stamp -> displayStampMetadata(stamp));
     *     });
     * }</pre>
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @param consumer the action to perform on the ObservableStampSnapshot
     * @return this handle for chaining
     */
    default ObservableEntityHandle ifStampGetSnapshot(ViewCalculator viewCalculator,
                                                       Consumer<ObservableStampSnapshot> consumer) {
        asStamp().ifPresent(stamp -> consumer.accept(stamp.getSnapshot(viewCalculator)));
        return this;
    }

    // ========== Snapshot Retrieval Methods - Safe Extraction Pattern (asStampSnapshot) ==========

    /**
     * Returns a snapshot of this entity as an {@link ObservableStampSnapshot} if it is a stamp.
     * <p>
     * Use this method when wrong type is a valid possibility.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     * <p>
     * <b>Note:</b> Stamp snapshots typically contain a single version with no contradictions.
     * Historic versions are rare and indicate metadata corrections. Uncommitted stamp versions
     * would only exist if stamp metadata was being edited but not yet persisted (very unusual).
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservableEntityHandle.get(nid)
     *     .asStampSnapshot(calculator)
     *     .ifPresent(snapshot -> {
     *         if (!snapshot.getHistoricVersions().isEmpty()) {
     *             LOG.warn("Stamp has metadata corrections: {}",
     *                 snapshot.getHistoricVersions().size());
     *         }
     *     });
     * }</pre>
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @return Optional containing the ObservableStampSnapshot, or empty if absent or not a stamp
     */
    default Optional<ObservableStampSnapshot> asStampSnapshot(ViewCalculator viewCalculator) {
        return asStamp().map(stamp -> stamp.getSnapshot(viewCalculator));
    }

    // ========== Static Stamp Snapshot Methods (Primary API) ==========

    /**
     * Retrieves an observable stamp snapshot by nid and view calculator in a single call.
     * <p>
     * This is the <b>primary API</b> for retrieving stamp snapshots when you have the nid and
     * the entity type is guaranteed by your data model. Combines entity retrieval, type checking,
     * and snapshot creation in one call.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     * <p>
     * <b>Stamp Snapshot Characteristics:</b>
     * <ul>
     *   <li>Most stamps have exactly one version (the original metadata record)</li>
     *   <li>Contradictions are extremely rare (would require metadata corrections on different paths)</li>
     *   <li>Historic versions indicate metadata corrections after initial creation (uncommon)</li>
     *   <li>Uncommitted versions would only exist during active metadata editing (very unusual)</li>
     * </ul>
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ViewCalculator calculator = viewCoordinateRecord.calculator();
     * ObservableStampSnapshot snapshot =
     *     ObservableEntityHandle.getStampSnapshotOrThrow(stampNid, calculator);
     *
     * // Access stamp metadata
     * Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
     * if (latest.isPresent()) {
     *     ObservableStampVersion stamp = latest.get();
     *     statusLabel.textProperty().bind(stamp.stateProperty().asString());
     *     authorLabel.textProperty().bind(stamp.authorProperty().asString());
     *     timeLabel.textProperty().bind(stamp.timeProperty().asString());
     * }
     *
     * // Check for metadata corrections (rare)
     * if (!snapshot.getHistoricVersions().isEmpty()) {
     *     LOG.warn("Stamp {} was corrected {} times",
     *         stampNid, snapshot.getHistoricVersions().size());
     * }
     * }</pre>
     *
     * @param nid the stamp entity nid
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableStampSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     * @see ObservableStampSnapshot
     * @see #asStampSnapshot(ViewCalculator) for safe Optional-based extraction
     * @see #ifStampGetSnapshot(ViewCalculator, Consumer) for side-effect operations
     */
    static ObservableStampSnapshot getStampSnapshotOrThrow(int nid, ViewCalculator viewCalculator) {
        return get(nid).expectStamp().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable stamp snapshot by PublicId and view calculator.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * PublicId stampPublicId = PublicIds.of(uuid);
     * ObservableStampSnapshot snapshot =
     *     ObservableEntityHandle.getStampSnapshotOrThrow(stampPublicId, calculator);
     * snapshot.getLatestVersion().ifPresent(v -> displayMetadata(v));
     * }</pre>
     *
     * @param publicId the stamp entity public identifier
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableStampSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableStampSnapshot getStampSnapshotOrThrow(PublicId publicId, ViewCalculator viewCalculator) {
        return get(publicId).expectStamp().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable stamp snapshot by EntityFacade and view calculator.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * EntityFacade stampFacade = EntityProxy.make(stampNid);
     * ObservableStampSnapshot snapshot =
     *     ObservableEntityHandle.getStampSnapshotOrThrow(stampFacade, calculator);
     *
     * // Process all versions (typically just one)
     * for (ObservableStampVersion version : snapshot.getProcessedVersions()) {
     *     processStampMetadata(version);
     * }
     * }</pre>
     *
     * @param entityFacade the stamp entity facade
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableStampSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableStampSnapshot getStampSnapshotOrThrow(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        return get(entityFacade).expectStamp().getSnapshot(viewCalculator);
    }

    // ========== Generic Snapshot Retrieval ==========

    /**
     * Retrieves a generic snapshot for an entity of unknown type.
     * <p>
     * Use this method when you don't know the entity type in advance. The returned Optional
     * will contain the appropriate snapshot type (Concept, Semantic, Pattern, or Stamp) based
     * on the actual entity type.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     * <p>
     * <b>Note:</b> For most use cases, prefer the type-specific methods ({@link #getConceptSnapshotOrThrow},
     * {@link #getSemanticSnapshotOrThrow}, {@link #getPatternSnapshotOrThrow}, {@link #getStampSnapshotOrThrow})
     * which provide better type safety and clearer semantics.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * // When entity type is unknown (user input, generic processing)
     * Optional<ObservableEntitySnapshot<?, ?>> maybeSnapshot =
     *     ObservableEntityHandle.getSnapshot(unknownNid, calculator);
     *
     * maybeSnapshot.ifPresent(snapshot -> {
     *     switch (snapshot) {
     *         case ObservableConceptSnapshot cs ->
     *             processConceptSnapshot(cs);
     *         case ObservableSemanticSnapshot ss ->
     *             processSemanticSnapshot(ss);
     *         case ObservablePatternSnapshot ps ->
     *             processPatternSnapshot(ps);
     *         case ObservableStampSnapshot sts ->
     *             processStampSnapshot(sts);
     *     }
     * });
     * }</pre>
     *
     * @param nid the entity nid
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return Optional containing the appropriate snapshot type, or empty if entity is absent
     * @throws RuntimeException if not called on JavaFX application thread
     * @see #getConceptSnapshotOrThrow(int, ViewCalculator)
     * @see #getSemanticSnapshotOrThrow(int, ViewCalculator)
     * @see #getPatternSnapshotOrThrow(int, ViewCalculator)
     * @see #getStampSnapshotOrThrow(int, ViewCalculator)
     */
    static Optional<ObservableEntitySnapshot<?, ?>> getSnapshot(int nid, ViewCalculator viewCalculator) {
        return get(nid).entity().map(e -> e.getSnapshot(viewCalculator));
    }

    // ========== Snapshot Retrieval Methods - Safe Extraction Pattern (asXxx) ==========

    /**
     * Returns a snapshot of this entity as an {@link ObservableConceptSnapshot} if it is a concept.
     * <p>
     * Use this method when wrong type is a valid possibility.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservableEntityHandle.get(nid)
     *     .asConceptSnapshot(calculator)
     *     .ifPresent(snapshot -> snapshot.latestVersion().ifPresent(v -> bindToUI(v)));
     * }</pre>
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @return Optional containing the ObservableConceptSnapshot, or empty if absent or not a concept
     */
    default Optional<ObservableConceptSnapshot> asConceptSnapshot(ViewCalculator viewCalculator) {
        return asConcept().map(concept -> concept.getSnapshot(viewCalculator));
    }

    /**
     * Returns a snapshot of this entity as an {@link ObservableSemanticSnapshot} if it is a semantic.
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @return Optional containing the ObservableSemanticSnapshot, or empty if absent or not a semantic
     */
    default Optional<ObservableSemanticSnapshot> asSemanticSnapshot(ViewCalculator viewCalculator) {
        return asSemantic().map(semantic -> semantic.getSnapshot(viewCalculator));
    }

    /**
     * Returns a snapshot of this entity as an {@link ObservablePatternSnapshot} if it is a pattern.
     *
     * @param viewCalculator the view calculator for snapshot computation
     * @return Optional containing the ObservablePatternSnapshot, or empty if absent or not a pattern
     */
    default Optional<ObservablePatternSnapshot> asPatternSnapshot(ViewCalculator viewCalculator) {
        return asPattern().map(pattern -> pattern.getSnapshot(viewCalculator));
    }

    // ========== Static Snapshot Methods (Primary API) ==========

    /**
     * Retrieves an observable concept snapshot by nid and view calculator in a single call.
     * <p>
     * This is the <b>primary API</b> for retrieving concept snapshots when you have the nid and
     * the entity type is guaranteed by your data model. Combines entity retrieval, type checking,
     * and snapshot creation in one call.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ViewCalculator calculator = viewCoordinateRecord.calculator();
     * ObservableConceptSnapshot snapshot =
     *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, calculator);
     * snapshot.latestVersion().ifPresent(version -> bindToUI(version));
     * }</pre>
     *
     * @param nid the concept entity nid
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableConceptSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableConceptSnapshot getConceptSnapshotOrThrow(int nid, ViewCalculator viewCalculator) {
        return get(nid).expectConcept().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable concept snapshot by PublicId and view calculator.
     *
     * @param publicId the concept entity public identifier
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableConceptSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableConceptSnapshot getConceptSnapshotOrThrow(PublicId publicId, ViewCalculator viewCalculator) {
        return get(publicId).expectConcept().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable concept snapshot by EntityFacade and view calculator.
     *
     * @param entityFacade the concept entity facade
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableConceptSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableConceptSnapshot getConceptSnapshotOrThrow(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        return get(entityFacade).expectConcept().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable semantic snapshot by nid and view calculator in a single call.
     * <p>
     * This is the <b>primary API</b> for retrieving semantic snapshots when you have the nid and
     * the entity type is guaranteed by your data model.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservableSemanticSnapshot snapshot =
     *     ObservableEntityHandle.getSemanticSnapshotOrThrow(semanticNid, calculator);
     * snapshot.uncommittedVersions().forEach(v -> flagUncommitted(v));
     * }</pre>
     *
     * @param nid the semantic entity nid
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableSemanticSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemanticSnapshot getSemanticSnapshotOrThrow(int nid, ViewCalculator viewCalculator) {
        return get(nid).expectSemantic().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable semantic snapshot by PublicId and view calculator.
     *
     * @param publicId the semantic entity public identifier
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableSemanticSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemanticSnapshot getSemanticSnapshotOrThrow(PublicId publicId, ViewCalculator viewCalculator) {
        return get(publicId).expectSemantic().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable semantic snapshot by EntityFacade and view calculator.
     *
     * @param entityFacade the semantic entity facade
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservableSemanticSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemanticSnapshot getSemanticSnapshotOrThrow(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        return get(entityFacade).expectSemantic().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable pattern snapshot by nid and view calculator in a single call.
     * <p>
     * This is the <b>primary API</b> for retrieving pattern snapshots when you have the nid and
     * the entity type is guaranteed by your data model.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * ObservablePatternSnapshot snapshot =
     *     ObservableEntityHandle.getPatternSnapshotOrThrow(patternNid, calculator);
     * snapshot.fieldDefinitions().forEach(field -> processField(field));
     * }</pre>
     *
     * @param nid the pattern entity nid
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservablePatternSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePatternSnapshot getPatternSnapshotOrThrow(int nid, ViewCalculator viewCalculator) {
        return get(nid).expectPattern().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable pattern snapshot by PublicId and view calculator.
     *
     * @param publicId the pattern entity public identifier
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservablePatternSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePatternSnapshot getPatternSnapshotOrThrow(PublicId publicId, ViewCalculator viewCalculator) {
        return get(publicId).expectPattern().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves an observable pattern snapshot by EntityFacade and view calculator.
     *
     * @param entityFacade the pattern entity facade
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return the ObservablePatternSnapshot (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePatternSnapshot getPatternSnapshotOrThrow(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        return get(entityFacade).expectPattern().getSnapshot(viewCalculator);
    }

    /**
     * Retrieves a snapshot of any observable entity type by PublicId and view calculator.
     *
     * @param publicId the entity public identifier
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return Optional containing the snapshot, or empty if entity is absent
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static Optional<ObservableEntitySnapshot<?, ?>> getSnapshot(PublicId publicId, ViewCalculator viewCalculator) {
        return get(publicId).entity().map(e -> e.getSnapshot(viewCalculator));
    }

    /**
     * Retrieves a snapshot of any observable entity type by EntityFacade and view calculator.
     *
     * @param entityFacade the entity facade
     * @param viewCalculator the view calculator to use for computing the snapshot
     * @return Optional containing the snapshot, or empty if entity is absent
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static Optional<ObservableEntitySnapshot<?, ?>> getSnapshot(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        return get(entityFacade).entity().map(e -> e.getSnapshot(viewCalculator));
    }

    // ========== Static Convenience Methods ==========

    /**
     * Gets observable entity by nid and returns it as an {@link ObservableConcept}, throwing if absent or wrong type.
     * <p>
     * Convenience method equivalent to {@code get(nid).expectConcept()}.
     * Use when you need to fetch and assert type in one call.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param nid the entity nid
     * @return the ObservableConcept (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     * @see #expectConcept()
     */
    static ObservableConcept getConceptOrThrow(int nid) {
        return get(nid).expectConcept();
    }

    /**
     * Gets observable entity by PublicId and returns it as an {@link ObservableConcept}, throwing if absent or wrong type.
     *
     * @param publicId the entity public identifier
     * @return the ObservableConcept (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableConcept getConceptOrThrow(PublicId publicId) {
        return get(publicId).expectConcept();
    }

    /**
     * Gets observable entity by EntityFacade and returns it as an {@link ObservableConcept}, throwing if absent or wrong type.
     *
     * @param entityFacade the entity facade
     * @return the ObservableConcept (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableConcept getConceptOrThrow(EntityFacade entityFacade) {
        return get(entityFacade).expectConcept();
    }

    /**
     * Gets observable entity by nid and returns it as an {@link ObservableSemantic}, throwing if absent or wrong type.
     * <p>
     * Convenience method equivalent to {@code get(nid).expectSemantic()}.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param nid the entity nid
     * @return the ObservableSemantic (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemantic getSemanticOrThrow(int nid) {
        return get(nid).expectSemantic();
    }

    /**
     * Gets observable entity by PublicId and returns it as an {@link ObservableSemantic}, throwing if absent or wrong type.
     *
     * @param publicId the entity public identifier
     * @return the ObservableSemantic (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemantic getSemanticOrThrow(PublicId publicId) {
        return get(publicId).expectSemantic();
    }

    /**
     * Gets observable entity by EntityFacade and returns it as an {@link ObservableSemantic}, throwing if absent or wrong type.
     *
     * @param entityFacade the entity facade
     * @return the ObservableSemantic (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableSemantic getSemanticOrThrow(EntityFacade entityFacade) {
        return get(entityFacade).expectSemantic();
    }

    /**
     * Gets observable entity by nid and returns it as an {@link ObservablePattern}, throwing if absent or wrong type.
     * <p>
     * Convenience method equivalent to {@code get(nid).expectPattern()}.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param nid the entity nid
     * @return the ObservablePattern (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePattern getPatternOrThrow(int nid) {
        return get(nid).expectPattern();
    }

    /**
     * Gets observable entity by PublicId and returns it as an {@link ObservablePattern}, throwing if absent or wrong type.
     *
     * @param publicId the entity public identifier
     * @return the ObservablePattern (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePattern getPatternOrThrow(PublicId publicId) {
        return get(publicId).expectPattern();
    }

    /**
     * Gets observable entity by EntityFacade and returns it as an {@link ObservablePattern}, throwing if absent or wrong type.
     *
     * @param entityFacade the entity facade
     * @return the ObservablePattern (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservablePattern getPatternOrThrow(EntityFacade entityFacade) {
        return get(entityFacade).expectPattern();
    }

    /**
     * Gets observable entity by nid and returns it as an {@link ObservableStamp}, throwing if absent or wrong type.
     * <p>
     * Convenience method equivalent to {@code get(nid).expectStamp()}.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @param nid the entity nid
     * @return the ObservableStamp (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableStamp getStampOrThrow(int nid) {
        return get(nid).expectStamp();
    }

    /**
     * Gets observable entity by PublicId and returns it as an {@link ObservableStamp}, throwing if absent or wrong type.
     *
     * @param publicId the entity public identifier
     * @return the ObservableStamp (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableStamp getStampOrThrow(PublicId publicId) {
        return get(publicId).expectStamp();
    }

    /**
     * Gets observable entity by EntityFacade and returns it as an {@link ObservableStamp}, throwing if absent or wrong type.
     *
     * @param entityFacade the entity facade
     * @return the ObservableStamp (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     * @throws RuntimeException if not called on JavaFX application thread
     */
    static ObservableStamp getStampOrThrow(EntityFacade entityFacade) {
        return get(entityFacade).expectStamp();
    }

    // ========== Default Implementation: Type Extraction Methods (Optional) ==========

    /**
     * Returns this entity as an {@link ObservableConcept} if it is one.
     * <p>
     * Use this method when you need to extract and return the concept, or when you
     * want to apply transformations using {@link Optional#map(java.util.function.Function)}.
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Extract concept or return null
     * ObservableConcept concept = ObservableEntityHandle.get(nid)
     *     .asConcept()
     *     .orElse(null);
     *
     * // Bind UI if concept
     * ObservableEntityHandle.get(nid)
     *     .asConcept()
     *     .ifPresent(concept -> label.textProperty().bind(concept.descriptionProperty()));
     *
     * // Chain with flatMap
     * Optional<String> result = ObservableEntityHandle.get(nid)
     *     .asConcept()
     *     .flatMap(concept -> concept.findProperty())
     *     .map(prop -> prop.toString());
     * }</pre>
     *
     * @return Optional containing the ObservableConcept, or empty if absent or not a concept
     * @see #ifConcept(Consumer) for side-effect operations
     */
    default Optional<ObservableConcept> asConcept() {
        return entity()
            .filter(e -> e instanceof ObservableConcept)
            .map(e -> (ObservableConcept) e);
    }

    /**
     * Returns this entity as an {@link ObservableSemantic} if it is one.
     * <p>
     * Use this method when you need to extract and return the semantic, or when you
     * want to apply transformations using {@link Optional#map(java.util.function.Function)}.
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Extract semantic with default
     * ObservableSemantic semantic = ObservableEntityHandle.get(nid)
     *     .asSemantic()
     *     .orElseThrow(() -> new IllegalArgumentException("Not a semantic"));
     *
     * // Register listener if semantic
     * ObservableEntityHandle.get(nid)
     *     .asSemantic()
     *     .ifPresent(semantic -> semantic.fieldsProperty().addListener(listener));
     * }</pre>
     *
     * @return Optional containing the ObservableSemantic, or empty if absent or not a semantic
     * @see #ifSemantic(Consumer) for side-effect operations
     */
    default Optional<ObservableSemantic> asSemantic() {
        return entity()
            .filter(e -> e instanceof ObservableSemantic)
            .map(e -> (ObservableSemantic) e);
    }

    /**
     * Returns this entity as an {@link ObservablePattern} if it is one.
     * <p>
     * Use this method when you need to extract and return the pattern, or when you
     * want to apply transformations using {@link Optional#map(java.util.function.Function)}.
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Extract pattern
     * Optional<ObservablePattern> pattern = ObservableEntityHandle.get(nid).asPattern();
     *
     * // Get field definitions
     * ObservableList<FieldDefinition> defs = ObservableEntityHandle.get(nid)
     *     .asPattern()
     *     .map(pattern -> pattern.fieldDefinitionsProperty().get())
     *     .orElse(FXCollections.emptyObservableList());
     * }</pre>
     *
     * @return Optional containing the ObservablePattern, or empty if absent or not a pattern
     * @see #ifPattern(Consumer) for side-effect operations
     */
    default Optional<ObservablePattern> asPattern() {
        return entity()
            .filter(e -> e instanceof ObservablePattern)
            .map(e -> (ObservablePattern) e);
    }

    /**
     * Returns this entity as an {@link ObservableStamp} if it is one.
     * <p>
     * Use this method when you need to extract and return the stamp, or when you
     * want to apply transformations using {@link Optional#map(java.util.function.Function)}.
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Extract stamp
     * ObservableStamp stamp = ObservableEntityHandle.get(nid)
     *     .asStamp()
     *     .orElse(null);
     *
     * // Bind timestamp to label
     * ObservableEntityHandle.get(nid)
     *     .asStamp()
     *     .ifPresent(stamp -> timeLabel.textProperty().bind(stamp.timeProperty().asString()));
     * }</pre>
     *
     * @return Optional containing the ObservableStamp, or empty if absent or not a stamp
     * @see #ifStamp(Consumer) for side-effect operations
     */
    default Optional<ObservableStamp> asStamp() {
        return entity()
            .filter(e -> e instanceof ObservableStamp)
            .map(e -> (ObservableStamp) e);
    }

    // ========== Default Implementation: Type Assertion Methods (Direct) ==========

    /**
     * Returns this entity as an {@link ObservableConcept}, throwing an exception if absent or wrong type.
     * <p>
     * <b>Use this method when:</b> Entity type is guaranteed by your data model. Wrong type indicates
     * data corruption or programming error, not a legitimate alternative case.
     *
     * <p><b>Common Use Cases:</b>
     * <ul>
     *   <li>Field definitions where meaning/datatype/purpose are always concepts</li>
     *   <li>UI controllers that require specific entity types</li>
     *   <li>Data binding scenarios where type is known at compile time</li>
     * </ul>
     *
     * <p><b>Usage Examples:</b>
     * <pre>{@code
     * // Field definition - meaning is ALWAYS a concept per data model
     * public ObservableConcept observableMeaning(int meaningNid) {
     *     return ObservableEntityHandle.get(meaningNid).expectConcept();
     * }
     *
     * // Direct UI binding with guaranteed type
     * ObservableConcept concept = ObservableEntityHandle.get(conceptNid).expectConcept();
     * titleLabel.textProperty().bind(concept.descriptionProperty());
     * }</pre>
     *
     * @return the ObservableConcept (never null)
     * @throws IllegalStateException if entity is absent or not a concept, with descriptive error message
     * @see #asConcept() for safe Optional-based extraction when wrong type is valid
     * @see #ifConcept(Consumer) for side-effect operations
     */
    default ObservableConcept expectConcept() {
        return asConcept().orElseThrow(() ->
            new IllegalStateException(
                entity().map(e -> "Expected ObservableConcept but was " + e.getClass().getSimpleName() +
                                 " with nid: " + e.nid())
                        .orElse("Expected ObservableConcept but entity was absent")
            )
        );
    }

    /**
     * Returns this entity as an {@link ObservableConcept}, throwing an exception with custom message if absent or wrong type.
     * <p>
     * Use this variant when you want to provide domain-specific error context.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * public ObservableConcept dataType() {
     *     return ObservableEntityHandle.get(dataTypeNid())
     *         .expectConcept("FieldDefinition dataType must be an observable concept");
     * }
     * }</pre>
     *
     * @param errorMessage the error message for the exception
     * @return the ObservableConcept (never null)
     * @throws IllegalStateException if entity is absent or not a concept
     */
    default ObservableConcept expectConcept(String errorMessage) {
        return asConcept().orElseThrow(() -> new IllegalStateException(errorMessage));
    }

    /**
     * Returns this entity as an {@link ObservableSemantic}, throwing an exception if absent or wrong type.
     * <p>
     * <b>Use this method when:</b> Entity type is guaranteed by your data model.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * public ObservableSemantic getDefinition(int semanticNid) {
     *     return ObservableEntityHandle.get(semanticNid).expectSemantic();
     * }
     * }</pre>
     *
     * @return the ObservableSemantic (never null)
     * @throws IllegalStateException if entity is absent or not a semantic, with descriptive error message
     * @see #asSemantic() for safe Optional-based extraction when wrong type is valid
     */
    default ObservableSemantic expectSemantic() {
        return asSemantic().orElseThrow(() ->
            new IllegalStateException(
                entity().map(e -> "Expected ObservableSemantic but was " + e.getClass().getSimpleName() +
                                 " with nid: " + e.nid())
                        .orElse("Expected ObservableSemantic but entity was absent")
            )
        );
    }

    /**
     * Returns this entity as an {@link ObservableSemantic}, throwing an exception with custom message if absent or wrong type.
     *
     * @param errorMessage the error message for the exception
     * @return the ObservableSemantic (never null)
     * @throws IllegalStateException if entity is absent or not a semantic
     */
    default ObservableSemantic expectSemantic(String errorMessage) {
        return asSemantic().orElseThrow(() -> new IllegalStateException(errorMessage));
    }

    /**
     * Returns this entity as an {@link ObservablePattern}, throwing an exception if absent or wrong type.
     * <p>
     * <b>Use this method when:</b> Entity type is guaranteed by your data model.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * public ObservablePattern getPatternForSemantic(int patternNid) {
     *     return ObservableEntityHandle.get(patternNid).expectPattern();
     * }
     * }</pre>
     *
     * @return the ObservablePattern (never null)
     * @throws IllegalStateException if entity is absent or not a pattern, with descriptive error message
     * @see #asPattern() for safe Optional-based extraction when wrong type is valid
     */
    default ObservablePattern expectPattern() {
        return asPattern().orElseThrow(() ->
            new IllegalStateException(
                entity().map(e -> "Expected ObservablePattern but was " + e.getClass().getSimpleName() +
                                 " with nid: " + e.nid())
                        .orElse("Expected ObservablePattern but entity was absent")
            )
        );
    }

    /**
     * Returns this entity as an {@link ObservablePattern}, throwing an exception with custom message if absent or wrong type.
     *
     * @param errorMessage the error message for the exception
     * @return the ObservablePattern (never null)
     * @throws IllegalStateException if entity is absent or not a pattern
     */
    default ObservablePattern expectPattern(String errorMessage) {
        return asPattern().orElseThrow(() -> new IllegalStateException(errorMessage));
    }

    /**
     * Returns this entity as an {@link ObservableStamp}, throwing an exception if absent or wrong type.
     * <p>
     * <b>Use this method when:</b> Entity type is guaranteed by your data model.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * public ObservableStamp getVersionStamp(int stampNid) {
     *     return ObservableEntityHandle.get(stampNid).expectStamp();
     * }
     * }</pre>
     *
     * @return the ObservableStamp (never null)
     * @throws IllegalStateException if entity is absent or not a stamp, with descriptive error message
     * @see #asStamp() for safe Optional-based extraction when wrong type is valid
     */
    default ObservableStamp expectStamp() {
        return asStamp().orElseThrow(() ->
            new IllegalStateException(
                entity().map(e -> "Expected ObservableStamp but was " + e.getClass().getSimpleName() +
                                 " with nid: " + e.nid())
                        .orElse("Expected ObservableStamp but entity was absent")
            )
        );
    }

    /**
     * Returns this entity as an {@link ObservableStamp}, throwing an exception with custom message if absent or wrong type.
     *
     * @param errorMessage the error message for the exception
     * @return the ObservableStamp (never null)
     * @throws IllegalStateException if entity is absent or not a stamp
     */
    default ObservableStamp expectStamp(String errorMessage) {
        return asStamp().orElseThrow(() -> new IllegalStateException(errorMessage));
    }

    // ========== Default Implementation: Query Methods ==========

    /**
     * Returns {@code true} if entity is present, {@code false} if absent.
     *
     * @return true if entity is present
     */
    default boolean isPresent() {
        return entity().isPresent();
    }

    /**
     * Returns {@code true} if entity is absent, {@code false} if present.
     *
     * @return true if entity is absent
     */
    default boolean isAbsent() {
        return entity().isEmpty();
    }

    /**
     * Returns {@code true} if entity is present and is an {@link ObservableConcept}.
     *
     * @return true if entity is an ObservableConcept
     */
    default boolean isConcept() {
        return entity().filter(e -> e instanceof ObservableConcept).isPresent();
    }

    /**
     * Returns {@code true} if entity is present and is an {@link ObservableSemantic}.
     *
     * @return true if entity is an ObservableSemantic
     */
    default boolean isSemantic() {
        return entity().filter(e -> e instanceof ObservableSemantic).isPresent();
    }

    /**
     * Returns {@code true} if entity is present and is an {@link ObservablePattern}.
     *
     * @return true if entity is an ObservablePattern
     */
    default boolean isPattern() {
        return entity().filter(e -> e instanceof ObservablePattern).isPresent();
    }

    /**
     * Returns {@code true} if entity is present and is an {@link ObservableStamp}.
     *
     * @return true if entity is an ObservableStamp
     */
    default boolean isStamp() {
        return entity().filter(e -> e instanceof ObservableStamp).isPresent();
    }

    // ========== Default Implementation: Escape Hatches ==========

    /**
     * Returns the observable entity, or null if absent.
     *
     * @return the observable entity, or null if absent
     */
    default ObservableEntity<?> orNull() {
        return entity().orElse(null);
    }

    /**
     * Returns the observable entity, or throws the provided exception if absent.
     *
     * @param exceptionSupplier supplier of exception to throw
     * @param <X> the type of exception to throw
     * @return the observable entity (never null)
     * @throws X if entity is absent
     */
    default <X extends Throwable> ObservableEntity<?> orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return entity().orElseThrow(exceptionSupplier);
    }

    // ========== Hidden Implementation Classes ==========
    //
    // The following nested classes provide the concrete implementations for the ObservableEntityHandle
    // interface. They are intentionally simple, delegating all behavior to the default methods in the
    // interface. This design follows the "Interface-based traits with data records" pattern.
    //
    // Users of ObservableEntityHandle should never directly instantiate these classes. Instead, use
    // the static factory methods: get(int), of(ObservableEntity), or absent().

    /**
     * Hidden record implementation for a present observable entity.
     * <p>
     * This implementation is immutable and provides all fluent methods via the default implementations
     * in {@link ObservableEntityHandle}. The only functionality it provides is holding the entity
     * reference and exposing it via {@link #entity()}.
     * <p>
     * <b>Canonical Instance Behavior:</b> Due to the canonical object pool in {@link ObservableEntity},
     * multiple {@code PresentHandle} instances may wrap the same underlying observable entity instance.
     * This ensures all handles to the same nid observe the same mutable entity, enabling proper
     * JavaFX property binding and change notification.
     * <p>
     * <b>Implementation Note:</b> This is a record rather than a class to leverage automatic
     * immutability guarantees, equals/hashCode generation, and compact syntax. The compact
     * constructor enforces non-null validation.
     *
     * @param entityValue the observable entity being handled (never null, validated in compact constructor)
     */
    record PresentHandle(ObservableEntity<?> entityValue) implements ObservableEntityHandle {
        /**
         * Compact constructor validates entity is non-null.
         * <p>
         * This validation ensures that PresentHandle never wraps a null entity,
         * maintaining the invariant that {@link #entity()} always returns a
         * non-empty Optional for present handles.
         *
         * @throws NullPointerException if entityValue is null
         */
        public PresentHandle {
            if (entityValue == null) {
                throw new NullPointerException("observable entity must not be null");
            }
        }

        /**
         * Returns an Optional containing the wrapped observable entity.
         * <p>
         * For PresentHandle, this always returns a non-empty Optional.
         *
         * @return Optional containing the observable entity (never empty for PresentHandle)
         */
        @Override
        public Optional<ObservableEntity<?>> entity() {
            return Optional.of(entityValue);
        }

        /**
         * Returns a string representation including the entity's nid for debugging.
         *
         * @return string representation for debugging
         */
        @Override
        public String toString() {
            return "PresentHandle[" + entityValue.getClass().getSimpleName() +
                   " nid=" + entityValue.nid() + "]";
        }
    }

    /**
     * Hidden singleton implementation for an absent observable entity.
     * <p>
     * This implementation represents the absence of an observable entity (entity not found,
     * not yet loaded, or lookup failed). It provides all fluent methods via the default
     * implementations in {@link ObservableEntityHandle}, which gracefully handle the empty
     * case by:
     * <ul>
     *   <li>{@code ifXxx()} methods: Skip the consumer action, return this for chaining</li>
     *   <li>{@code asXxx()} methods: Return empty Optional</li>
     *   <li>{@code expectXxx()} methods: Throw IllegalStateException with descriptive message</li>
     *   <li>{@code isPresent()}: Return false</li>
     *   <li>{@code isAbsent()}: Return true</li>
     * </ul>
     * <p>
     * <b>Singleton Pattern:</b> Since all absent handles are identical (they all represent "not found"),
     * a single instance {@link #INSTANCE} is shared across the application. This is more memory-efficient
     * than creating new instances and allows identity comparison for absence checks.
     * <p>
     * <b>Thread Safety:</b> Immutable and stateless, therefore inherently thread-safe.
     */
    final class AbsentHandle implements ObservableEntityHandle {
        /**
         * The singleton instance representing an absent observable entity.
         * <p>
         * This is the only instance of AbsentHandle that should ever exist.
         * Access via {@link ObservableEntityHandle#absent()}.
         */
        static final AbsentHandle INSTANCE = new AbsentHandle();

        /**
         * Private constructor prevents external instantiation.
         * <p>
         * Use {@link ObservableEntityHandle#absent()} to obtain the singleton instance.
         */
        private AbsentHandle() {}

        /**
         * Returns an empty Optional, indicating no observable entity is present.
         * <p>
         * For AbsentHandle, this always returns {@link Optional#empty()}.
         *
         * @return empty Optional (always empty for AbsentHandle)
         */
        @Override
        public Optional<ObservableEntity<?>> entity() {
            return Optional.empty();
        }

        /**
         * Returns a constant string representation for debugging.
         *
         * @return "AbsentHandle" for all instances
         */
        @Override
        public String toString() {
            return "AbsentHandle";
        }
    }
}
