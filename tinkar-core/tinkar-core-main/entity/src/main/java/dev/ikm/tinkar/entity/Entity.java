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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Chronology;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

/**
 * Immutable, thread-safe representation of a Tinkar entity providing the foundation for terminology
 * management, semantic modeling, and knowledge representation.
 * <p>
 * {@code Entity} is the core abstraction in Tinkar, representing versioned knowledge artifacts that
 * maintain their identity across time while supporting multiple versions, collaborative editing, and
 * distributed synchronization. Entities are fully immutable, making them safe for concurrent access,
 * caching, and use in calculations or background processing.
 *
 * <h2>What is an Entity?</h2>
 * <p>
 * An {@code Entity} is an <b>immutable, versioned knowledge artifact</b> that provides:
 * <ul>
 *   <li><b>Immutability:</b> Once created, entity data never changes - updates create new versions</li>
 *   <li><b>Thread Safety:</b> Can be safely accessed from any thread without synchronization</li>
 *   <li><b>Version History:</b> Maintains complete chronology of all changes via {@link EntityVersion}s</li>
 *   <li><b>Unique Identity:</b> Identified by NID (native identifier) and {@link PublicId} (universally unique)</li>
 * </ul>
 *
 * <h2>Four Entity Types</h2>
 * <p>
 * This interface has four primary implementations representing different knowledge artifact types:
 * <ul>
 *   <li>{@link ConceptEntity} - Represents a concept (idea, term, or classification)</li>
 *   <li>{@link SemanticEntity} - Represents semantic annotations or relationships attached to other entities</li>
 *   <li>{@link PatternEntity} - Defines the structure/schema for semantic entities</li>
 *   <li>{@link StampEntity} - Represents change metadata (Status, Time, Author, Module, Path)</li>
 * </ul>
 *
 * <h2>⚠️ How to Access: Use EntityHandle</h2>
 * <p>
 * <b>DO NOT</b> call the static {@code get()}, {@code getOrThrow()}, or {@code getFast()} methods on this
 * interface directly. They are deprecated and will be made module-internal in a future release. Instead,
 * use {@link EntityHandle}, which provides a fluent, type-safe API for accessing entities.
 *
 * <h3>Why Use EntityHandle?</h3>
 * <ul>
 *   <li><b>Type Safety:</b> Compile-time checks ensure you're working with the correct entity type
 *       (Concept, Semantic, Pattern, or Stamp)</li>
 *   <li><b>Null Safety:</b> Explicit handling of absent entities via {@link java.util.Optional} or
 *       fluent conditional methods</li>
 *   <li><b>Composability:</b> Chain operations fluently without manual type checks or casts</li>
 *   <li><b>Three Access Patterns:</b> Side effects ({@code ifXxx}), safe extraction ({@code asXxx}),
 *       or direct assertion ({@code expectXxx}) - choose the right pattern for your use case</li>
 *   <li><b>Flexible Identifiers:</b> Accept NID (int), {@link PublicId}, or {@link EntityFacade} interchangeably</li>
 * </ul>
 *
 * <h3>Correct Usage Examples</h3>
 * <pre>{@code
 * // ✅ CORRECT: Use EntityHandle for type-safe access
 * ConceptEntity concept = EntityHandle.getConceptOrThrow(conceptNid);
 * String description = concept.description();
 *
 * // ✅ CORRECT: Fluent API with type checking
 * EntityHandle.get(nid)
 *     .ifConcept(concept -> processConcept(concept))
 *     .ifSemantic(semantic -> processSemantic(semantic))
 *     .ifAbsent(() -> LOG.warn("Entity {} not found", nid));
 *
 * // ✅ CORRECT: Safe Optional-based extraction
 * EntityHandle.get(userInputNid)
 *     .asConcept()
 *     .ifPresent(concept -> displayConcept(concept));
 *
 * // ✅ CORRECT: Works with PublicId and EntityFacade too
 * ConceptEntity concept = EntityHandle.getConceptOrThrow(publicId);
 * SemanticEntity semantic = EntityHandle.getSemanticOrThrow(entityFacade);
 *
 * // ❌ WRONG: Direct static method (deprecated, will be removed)
 * Optional<Entity> entity = Entity.get(nid); // DON'T DO THIS
 * Entity entity = Entity.getFast(nid);        // DON'T DO THIS
 * }</pre>
 *
 * <h2>When to Use Entity vs ObservableEntity</h2>
 * <table border="1" cellpadding="5">
 * <caption>Entity vs ObservableEntity Comparison</caption>
 * <tr>
 *   <th>Use Case</th>
 *   <th>Use Entity</th>
 *   <th>Use ObservableEntity</th>
 * </tr>
 * <tr>
 *   <td><b>Calculations/Logic</b></td>
 *   <td>✅ Preferred - immutable, efficient</td>
 *   <td>❌ Unnecessary overhead</td>
 * </tr>
 * <tr>
 *   <td><b>Background Processing</b></td>
 *   <td>✅ Thread-safe, any thread</td>
 *   <td>❌ Requires JavaFX thread</td>
 * </tr>
 * <tr>
 *   <td><b>Immutability</b></td>
 *   <td>✅ Fully immutable</td>
 *   <td>⚠️ Mutable wrapper</td>
 * </tr>
 * <tr>
 *   <td><b>Caching</b></td>
 *   <td>✅ Safe to cache indefinitely</td>
 *   <td>⚠️ Canonical pool only</td>
 * </tr>
 * <tr>
 *   <td><b>UI Binding</b></td>
 *   <td>❌ Not reactive</td>
 *   <td>✅ Direct JavaFX property binding</td>
 * </tr>
 * <tr>
 *   <td><b>Change Notifications</b></td>
 *   <td>❌ Manual polling</td>
 *   <td>✅ Automatic listeners</td>
 * </tr>
 * </table>
 *
 * <h2>Version Management</h2>
 * <p>
 * Each entity maintains a complete version history accessed via {@link #versions()}. Versions are
 * ordered chronologically and include all changes made to the entity over time. Each version is
 * associated with a {@link StampEntity} that records who made the change, when, in what module,
 * and on what development path.
 *
 * <pre>{@code
 * ConceptEntity concept = EntityHandle.getConceptOrThrow(nid);
 * ImmutableList<ConceptEntityVersion> versions = concept.versions();
 * 
 * // Get latest version
 * ConceptEntityVersion latest = versions.get(versions.size() - 1);
 * 
 * // Iterate through history
 * for (ConceptEntityVersion version : versions) {
 *     StampEntity stamp = EntityHandle.getStampOrThrow(version.stampNid());
 *     System.out.println("Changed at: " + stamp.time());
 * }
 * }</pre>
 *
 * <h2>Identity and Identification</h2>
 * <p>
 * Entities can be identified in three ways, all supported by {@link EntityHandle}:
 * <ul>
 *   <li><b>NID (Native ID):</b> Integer identifier unique within this system (int)</li>
 *   <li><b>PublicId:</b> Universally unique identifier for cross-system synchronization (UUIDs)</li>
 *   <li><b>EntityFacade:</b> Interface providing access to both NID and PublicId</li>
 * </ul>
 *
 * <pre>{@code
 * // All three approaches work with EntityHandle
 * ConceptEntity c1 = EntityHandle.getConceptOrThrow(nid);
 * ConceptEntity c2 = EntityHandle.getConceptOrThrow(publicId);
 * ConceptEntity c3 = EntityHandle.getConceptOrThrow(entityFacade);
 * }</pre>
 *
 * <h2>Thread Safety and Performance</h2>
 * <p>
 * {@code Entity} instances are fully immutable and thread-safe. They can be:
 * <ul>
 *   <li>Safely shared across threads without synchronization</li>
 *   <li>Cached indefinitely (they never change)</li>
 *   <li>Used in parallel streams and concurrent collections</li>
 *   <li>Passed between background tasks and UI threads</li>
 * </ul>
 * <p>
 * For best performance in tight loops or frequent access, use {@link EntityService#getEntityFast(int)}
 * via {@link EntityHandle} methods, which skip Optional wrapping. For most use cases, the standard
 * {@link EntityHandle} API provides the best balance of safety and performance.
 *
 * @param <V> the version type ({@link ConceptEntityVersion}, {@link SemanticEntityVersion},
 *           {@link PatternEntityVersion}, or {@link StampEntityVersion})
 * @see EntityHandle
 * @see ConceptEntity
 * @see SemanticEntity
 * @see PatternEntity
 * @see StampEntity
 * @see EntityVersion
 */
public interface Entity<V extends EntityVersion>
        extends Chronology<V>,
        EntityFacade,
        IdentifierData {
    // TODO: Make this and related interface sealed, but add ObservableEntity (or similarly named) as a non-sealed interface interface for extension
    Logger LOG = LoggerFactory.getLogger(Entity.class);

    static int nid(Component component) {
        return provider().nidForComponent(component);
    }

    static EntityService provider() {
        return EntityService.get();
    }

    static int nid(PublicId publicId) {
        return provider().nidForPublicId(publicId);
    }


    static int nidForSemantic(int patternNid, PublicId entityPublicId) {
        return provider().nidFor(patternNid, entityPublicId);
    }

    static int nidForSemantic(PublicId patternPublicId, PublicId semanticPublicId) {
        return provider().nidForSemantic(patternPublicId, semanticPublicId);
    }

    static int nidForSemantic(EntityFacade patternFacade, PublicId semanticPublicId) {
        return provider().nidForSemantic(patternFacade.publicId(), semanticPublicId);
    }

    static int nidForPattern(PublicId patternPublicId) {
        return provider().nidForPattern(patternPublicId);
    }

    static int nidForStamp(PublicId stampPublicId) {
        return provider().nidForStamp(stampPublicId);
    }

    static int nidForConcept(PublicId conceptPublicId) {
        return provider().nidForConcept(conceptPublicId);
    }

    static Optional<ConceptEntity> getConceptForSemantic(SemanticFacade semanticFacade) {
        return getConceptForSemantic(semanticFacade.nid());
    }

    static <V extends EntityVersion> Optional<V> getVersion(int nid, int stampNid) {
        return Optional.ofNullable(getVersionFast(nid, stampNid));
    }

    static <V extends EntityVersion> V getVersionFast(int nid, int stampNid) {
        Entity<EntityVersion> entity = EntityService.get().getEntityFast(nid);
        if (entity != null) {
            for (EntityVersion version : entity.versions()) {
                if (version.stampNid() == stampNid) {
                    return (V) version;
                }
            }
        }
        return null;
    }

    @Override
    ImmutableList<V> versions();

    static Optional<ConceptEntity> getConceptForSemantic(int semanticNid) {
        Optional<? extends Entity<? extends EntityVersion>> optionalEntity = get(semanticNid);
        if (optionalEntity.isPresent()) {
            if (optionalEntity.get() instanceof SemanticEntity semanticEntity) {
                Entity<?> referencedEntity = getFast(semanticEntity.referencedComponentNid());
                if (referencedEntity instanceof ConceptEntity conceptEntity) {
                    return Optional.of(conceptEntity);
                } else if (referencedEntity instanceof SemanticEntity referencedSemantic) {
                    return getConceptForSemantic(referencedSemantic);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#get(int)}.
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    static <T extends Entity<V>, V extends EntityVersion> Optional<T> packagePrivateGet(int nid) {
        return EntityService.get().getEntity(nid);
    }

    /**
     * @deprecated Use {@link EntityHandle#get(int)} instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API, which provides better type safety, null handling,
     * and composability. This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * Optional<Entity> entity = Entity.get(nid);
     *
     * // New (recommended):
     * EntityHandle handle = EntityHandle.get(nid);
     * Optional<Entity<?>> entity = handle.entity();
     * }</pre>
     *
     * @see EntityHandle#get(int)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> Optional<T> get(int nid) {
        return packagePrivateGet(nid);
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#getConceptOrThrow(int)} or type-specific methods.
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    static <T extends Entity<V>, V extends EntityVersion> T packagePrivateGetOrThrow(int nid) {
        return (T) EntityService.get().getEntity(nid).get();
    }

    /**
     * @deprecated Use {@link EntityHandle#getConceptOrThrow(int)} or type-specific methods instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API, which provides better type safety and composability.
     * This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * Entity entity = Entity.getOrThrow(nid);
     *
     * // New (recommended - type-safe):
     * ConceptEntity concept = EntityHandle.getConceptOrThrow(nid);
     * SemanticEntity semantic = EntityHandle.getSemanticOrThrow(nid);
     * }</pre>
     *
     * @see EntityHandle#getConceptOrThrow(int)
     * @see EntityHandle#getSemanticOrThrow(int)
     * @see EntityHandle#getPatternOrThrow(int)
     * @see EntityHandle#getStampOrThrow(int)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> T getOrThrow(int nid) {
        return packagePrivateGetOrThrow(nid);
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#get(EntityFacade)}.
     */
    static Optional<Entity<?>> packagePrivateGet(EntityFacade facade) {
        return EntityService.get().packagePrivateGetEntity(facade.nid());
    }

    /**
     * @deprecated Use {@link EntityHandle#get(EntityFacade)} instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API. This method will be made module-internal in a future release.
     *
     * @see EntityHandle#get(EntityFacade)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> Optional<T> get(EntityFacade facade) {
        return (Optional<T>) packagePrivateGet(facade);
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#getConceptOrThrow(EntityFacade)} or type-specific methods.
     */
    static  Entity<?> packagePrivateGetOrThrow(EntityFacade facade) {
        return EntityService.get().getEntity(facade.nid()).get();
    }

    /**
     * @deprecated Use {@link EntityHandle#getConceptOrThrow(EntityFacade)} or type-specific methods instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API. This method will be made module-internal in a future release.
     *
     * @see EntityHandle#getConceptOrThrow(EntityFacade)
     * @see EntityHandle#getSemanticOrThrow(EntityFacade)
     * @see EntityHandle#getPatternOrThrow(EntityFacade)
     * @see EntityHandle#getStampOrThrow(EntityFacade)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> T getOrThrow(EntityFacade facade) {
        return (T) packagePrivateGetOrThrow(facade);
    }

    /**
     * @deprecated Use {@link EntityHandle#get(int)} instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API. This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * Entity entity = Entity.getFast(nid);
     *
     * // New (recommended):
     * Entity entity = EntityHandle.get(nid).orNull();
     * // Or with type safety:
     * ConceptEntity concept = EntityHandle.getConceptOrThrow(nid);
     * }</pre>
     *
     * @see EntityHandle#get(int)
     * @see EntityHandle#getConceptOrThrow(int)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> T getFast(int nid) {
        return (T) packagePrivateGetFast(nid);
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#get(int)}.
     */
    static Entity<?> packagePrivateGetFast(int nid) {
        return EntityService.get().getEntityFast(nid);
    }

    /**
     * Package-private method for internal use by EntityHandle.
     * External code should use {@link EntityHandle#get(EntityFacade)}.
     */
    static Entity<?> packagePrivateGetFast(EntityFacade facade) {
        return EntityService.get().getEntityFast(facade.nid());
    }

    /**
     * @deprecated Use {@link EntityHandle#get(EntityFacade)} instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link EntityHandle} API. This method will be made module-internal in a future release.
     *
     * @see EntityHandle#get(EntityFacade)
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
    @Deprecated(since = "Current", forRemoval = true)
    static <T extends Entity<V>, V extends EntityVersion> T getFast(EntityFacade facade) {
        return (T) packagePrivateGetFast(facade);
    }

    static <T extends StampEntity<? extends StampEntityVersion>> T getStamp(int nid) {
        return EntityService.get().getStampFast(nid);
    }

    default Optional<V> getVersion(int stampNid) {
        return Optional.ofNullable(getVersionFast(stampNid));
    }

    default V getVersionFast(int stampNid) {
        for (V version : versions()) {
            if (version.stampNid() == stampNid) {
                return version;
            }
        }
        return null;
    }

    default Optional<V> getVersion(PublicId stampId) {
        return Optional.ofNullable(getVersionFast(stampId));
    }

    default V getVersionFast(PublicId stampId) {
        int stampNid = nid(stampId);
        for (V version : versions()) {
            if (version.stampNid() == stampNid) {
                return version;
            }
        }
        return null;
    }

    default IntIdSet stampNids() {
        MutableIntList stampNids = IntLists.mutable.withInitialCapacity(versions().size());
        for (EntityVersion version : versions()) {
            stampNids.add(version.stampNid());
        }
        return IntIds.set.of(stampNids.toArray());
    }

    byte[] getBytes();

    FieldDataType entityDataType();

    FieldDataType versionDataType();

    default String entityToString() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(this.getClass().getSimpleName());
            sb.append("{");
            try {
                Optional<String> stringOptional = PrimitiveData.textOptional(this.nid());
                if (stringOptional.isPresent()) {
                    sb.append(stringOptional.get());
                    sb.append(' ');
                }
            } catch (Throwable t) {
                AlertStreams.dispatchToRoot(t);
            }
            sb.append("<");
            sb.append(nid());
            sb.append("> ");
            sb.append(Arrays.toString(publicId().asUuidArray()));
            sb.append(", ");
            sb.append(entityToStringExtras());
            for (EntityVersion version : versions()) {
                try {
                    sb.append("\nv: ").append(version).append(",");
                } catch (Throwable e) {
                    LOG.error("Error creating string for <" + version.nid() + ">", e);
                    sb.append("<").append(version.nid()).append(">,");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            LOG.error("Error creating string for <" + nid() + ">", e);

        }
        return sb.toString();
    }

    int nid();

    @Override
    default PublicId publicId() {
        return IdentifierData.super.publicId();
    }

    default String entityToStringExtras() {
        return "";
    }

    /**
     * @return true if all versions of entity are canceled.
     */
    default boolean canceled() {
        for (EntityVersion v : versions()) {
            if (!v.canceled()) {
                return false;
            }
        }
        return true;
    }

    default boolean committed() {
        return !uncommitted();
    }

    default boolean uncommitted() {
        return versions().stream().anyMatch(v -> v.uncommitted());
    }

    default ImmutableIntList uncommittedStampNids() {
        return IntLists.immutable.of(versions().stream()
                .filter(v -> v.uncommitted()).mapToInt(v -> v.stampNid()).toArray());
    }
}
