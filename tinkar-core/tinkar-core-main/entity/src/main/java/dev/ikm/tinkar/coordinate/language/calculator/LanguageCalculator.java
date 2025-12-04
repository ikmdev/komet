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
package dev.ikm.tinkar.coordinate.language.calculator;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * The LanguageCalculator class provides functionality for working with language coordinates
 * and handling descriptions for various components. It includes methods for retrieving descriptions,
 * preferred texts, fully qualified names, and semantic text for specified components or entities.
 * Language preferences and type-specific filters can be applied to refine the retrieval of descriptions.
 */
public interface LanguageCalculator {

    /**
     * Retrieves a list of the language coordinate records of this calculator. The
     * language coordinate records are used in list order to try and fulfil a request for a
     * description in the proper language and dialect.
     *
     * @return an immutable list of language coordinate records
     */
    ImmutableList<LanguageCoordinateRecord> languageCoordinateList();

    /**
     * Retrieves a list of semantic entities representing descriptions for the given component.
     *
     * @param entityFacade the entity facade representing the component for which descriptions are to be retrieved
     * @return an immutable list of semantic entities corresponding to the descriptions for the specified component
     */
    default ImmutableList<SemanticEntity> getDescriptionsForComponent(EntityFacade entityFacade) {
        return getDescriptionsForComponent(entityFacade.nid());
    }

    /**
     * Gets all descriptions from the first pattern in the language coordinate pattern priority list
     * that contains any descriptions.
     *
     * @param componentNid
     * @return descriptions from the first pattern in the the language coordinate pattern priority list
     * that contains any descriptions.
     */
    ImmutableList<SemanticEntity> getDescriptionsForComponent(int componentNid);


    /**
     * Retrieves a list of descriptions associated with a specific component of the given type.
     *
     * @param entityFacade The entity representing the component for which descriptions are being retrieved.
     * @param descriptionTypeFacade The type of descriptions to filter and retrieve for the given component.
     * @return An immutable list of semantic entity versions representing the descriptions of the specified type for the component.
     */
    default ImmutableList<SemanticEntityVersion> getDescriptionsForComponentOfType(EntityFacade entityFacade,
                                                                                   ConceptFacade descriptionTypeFacade) {
        return getDescriptionsForComponentOfType(entityFacade.nid(), descriptionTypeFacade.nid());
    }

    /**
     * Retrieves a list of SemanticEntityVersion objects that represent descriptions
     * for a given component of a specified type.
     *
     * @param componentNid the identifier of the component for which descriptions are retrieved
     * @param descriptionTypeNid the identifier of the description type to filter the results
     * @return an immutable list of SemanticEntityVersion objects matching the specified component and description type
     */
    ImmutableList<SemanticEntityVersion> getDescriptionsForComponentOfType(int componentNid,
                                                                           int descriptionTypeNid);

    /**
     * Retrieves a list of preferred description text for the specified components.
     *
     * @param ids the set of component identifiers for which the preferred descriptions are to be retrieved
     * @return an immutable list of preferred description texts corresponding to the specified component identifiers
     */
    default ImmutableList<String> getPreferredDescriptionTextListForComponents(IntIdSet ids) {
        return getPreferredDescriptionTextListForComponents(ids.toArray());
    }

    /**
     * Retrieves a list of preferred description texts for the specified components.
     * This method processes each provided component identifier (nid) and retrieves
     * its preferred description text. If a preferred description is not available,
     * it falls back to an alternative or the nid itself.
     *
     * @param nids an array of component identifiers for which the preferred description texts
     *             are to be retrieved.
     * @return an immutable list of preferred description texts corresponding to the provided component identifiers.
     */
    default ImmutableList<String> getPreferredDescriptionTextListForComponents(int... nids) {
        MutableList<String> descriptionTextList = Lists.mutable.ofInitialCapacity(nids.length);
        for (int nid : nids) {
            descriptionTextList.add(getPreferredDescriptionTextWithFallbackOrNid(nid));
        }
        return descriptionTextList.toImmutable();
    }

    default String getPreferredDescriptionTextWithFallbackOrNid(int nid) {
        Optional<String> optionalResult = getRegularDescriptionText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        optionalResult = getFullyQualifiedNameText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        optionalResult = getSemanticText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        return Integer.toString(nid);
    }

    /**
     * Retrieves the regular description text associated with the specified entity ID.
     *
     * @param entityNid the unique identifier of the entity whose description text is to be retrieved
     * @return an Optional containing the regular description text if available; otherwise, an empty Optional
     */
    Optional<String> getRegularDescriptionText(int entityNid);

    /**
     * Retrieves the fully qualified name text for a given component identified by its nid.
     *
     * @param componentNid the nid of the component for which the fully qualified name text is to be retrieved
     * @return an Optional containing the fully qualified name text if available; otherwise, an empty Optional
     */
    default Optional<String> getFullyQualifiedNameText(int componentNid) {
        return getDescriptionTextForComponentOfType(componentNid, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid());
    }

    /**
     * Retrieves the semantic text for a given unique identifier.
     *
     * @param nid The unique identifier (nid) for which the semantic text needs to be fetched.
     * @return An Optional containing the semantic text if present, or an empty Optional if no semantic text is available for the given nid.
     */
    Optional<String> getSemanticText(int nid);

    /**
     * Retrieves the description text of a specific type for a given component.
     *
     * @param entityNid The identifier of the entity or component for which the description is being fetched.
     * @param descriptionTypeNid The identifier of the description type to retrieve for the component.
     * @return An {@link Optional} containing the description text if it exists, otherwise an empty {@link Optional}.
     */
    Optional<String> getDescriptionTextForComponentOfType(int entityNid, int descriptionTypeNid);

    /**
     * Gets the text of type {@link TinkarTerm#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if
     * no matching description type is found in this or any nested language coordinates
     *
     * @param componentFacade the component to get a regular name for.
     * @return the regular name text
     */
    default Optional<String> getFullyQualifiedNameText(EntityFacade componentFacade) {
        return getFullyQualifiedNameText(componentFacade.nid());
    }

    /**
     * Retrieves a list of preferred description text for the given components.
     *
     * @param entities an array of EntityFacade objects for which preferred description texts should be retrieved
     * @return an immutable list of preferred description texts, one for each provided component
     */
    default ImmutableList<String> getPreferredDescriptionTextListForComponents(EntityFacade... entities) {
        MutableList<String> descriptionTextList = Lists.mutable.ofInitialCapacity(entities.length);
        for (EntityFacade entity : entities) {
            descriptionTextList.add(getPreferredDescriptionTextWithFallbackOrNid(entity));
        }
        return descriptionTextList.toImmutable();
    }

    /**
     * Retrieves the preferred description text for the provided entity, with fallback mechanisms, or returns the NID if no description is available.
     *
     * @param entityFacade the entity facade to retrieve the description text or NID for
     * @return the preferred description text, a fallback description text, or the NID if no descriptions are available
     */
    default String getPreferredDescriptionTextWithFallbackOrNid(EntityFacade entityFacade) {
        return getPreferredDescriptionTextWithFallbackOrNid(entityFacade.nid());
    }

    /**
     * Generates an immutable list of fully qualified text representations for the provided components.
     *
     * @param entities a variable number of EntityFacade objects for which fully qualified text needs to be generated
     * @return an immutable list of strings representing the fully qualified text for the given entities
     */
    default ImmutableList<String> getFullyQualifiedTextListForComponents(EntityFacade... entities) {
        MutableList<String> descriptionTextList = Lists.mutable.ofInitialCapacity(entities.length);
        for (EntityFacade entity : entities) {
            descriptionTextList.add(getFullyQualifiedNameTextOrNid(entity));
        }
        return descriptionTextList.toImmutable();
    }

    /**
     * Retrieves the fully qualified name text or the NID (Numeric Identifier) for the given entity.
     *
     * @param entityFacade an instance of EntityFacade representing the entity for which the fully qualified name
     *                     text or NID is to be retrieved.
     * @return a string representing the fully qualified name text if available,
     *         otherwise returns the NID of the provided entity.
     */
    default String getFullyQualifiedNameTextOrNid(EntityFacade entityFacade) {
        return getFullyQualifiedNameTextOrNid(entityFacade.nid());
    }

    /**
     * Retrieves the fully qualified name (FQN) text for the specified component Nid.
     * If no FQN text is available, the method returns the Nid as a string.
     *
     * @param componentNid the Nid of the component for which to retrieve the fully qualified name text
     * @return the fully qualified name text if available; otherwise, the Nid as a string
     */
    default String getFullyQualifiedNameTextOrNid(int componentNid) {
        Optional<String> optionalText = getDescriptionTextForComponentOfType(componentNid, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid());
        if (optionalText.isPresent()) {
            return optionalText.get();
        }
        return Integer.toString(componentNid);
    }

    /**
     * Retrieves a list of description texts or NIDs for the provided components.
     *
     * @param entities an array of EntityFacade objects representing the components for which
     *                 description texts or NIDs are to be retrieved
     * @return an immutable list of description texts or NIDs corresponding to the provided components
     */
    default ImmutableList<String> getDescriptionTextListForComponents(EntityFacade... entities) {
        MutableList<String> descriptionTextList = Lists.mutable.ofInitialCapacity(entities.length);
        for (EntityFacade entity : entities) {
            descriptionTextList.add(getDescriptionTextOrNid(entity));
        }
        return descriptionTextList.toImmutable();
    }

    /**
     * Retrieves the description text or nid (numeric identifier) for a given entity.
     *
     * @param entity The entity from which the description text or nid is to be retrieved.
     * @return A string representation of the description text or nid associated with the entity.
     */
    default String getDescriptionTextOrNid(EntityFacade entity) {
        return getDescriptionTextOrNid(entity.nid());
    }

    /**
     * Retrieves the description text for the given component ID (NID). If a description is not available,
     * returns the string representation of the component NID.
     *
     * @param componentNid the unique identifier (NID) of the component for which the description is requested
     * @return the description text if available, otherwise the string representation of the component NID
     */
    default String getDescriptionTextOrNid(int componentNid) {
        Optional<String> text = getDescriptionText(componentNid);
        if (text.isPresent()) {
            return text.get();
        }
        return Integer.toString(componentNid);
    }

    /**
     * @param componentNid
     * @return Return the latestDescription according to the type and dialect preferences of this {@code LanguageCoordinate}.
     */
    Optional<String> getDescriptionText(int componentNid);

    /*
    Allow the pattern to also define a pattern for user text.
     */
    Optional<String> getUserText();

    /**
     * @param entity
     * @return Return the latestDescription according to the type and dialect preferences of this {@code LanguageCoordinate}.
     * @see #getDescriptionText(int)
     */
    default Optional<String> getDescriptionText(EntityFacade entity) {
        return this.getDescriptionText(entity.nid());
    }

    default Latest<SemanticEntityVersion> getDescription(EntityFacade entityFacade) {
        return getDescription(entityFacade.nid());
    }

    /**
     * Return the latestDescription according to the type and dialect preferences of this {@code LanguageCoordinate}.
     * or a nested {@code LanguageCoordinate}
     *
     * @param entityNid the concept nid.
     * @return an optional latestDescription best matching the {@code LanguageCoordinate} constraints.
     */
    default Latest<SemanticEntityVersion> getDescription(int entityNid) {
        return getDescription(getDescriptionsForComponent(entityNid));
    }

    /**
     * Return the latestDescription according to the pattern, type and dialect preferences of this {@code LanguageCoordinate}.
     *
     * @param descriptionList descriptions to consider
     * @return an optional latestDescription best matching the {@code LanguageCoordinate} constraints.
     */
    default Latest<SemanticEntityVersion> getDescription(ImmutableList<SemanticEntity> descriptionList) {
        return getSpecifiedDescription(descriptionList);
    }

    /**
     * TODO needs update. Add info on patterns, and we don't use getNextPriorityLanguageCoordinate anymore...
     * The developer can pass an ordered list of language coordinates to the language stampCoordinateRecord.
     * <p>
     * Gets the specified description(s).
     * <p>
     * Iterates over the list of supplied descriptions, finding the descriptions that match the highest ranked
     * {@link LanguageCoordinate#descriptionTypePreferenceNidList()} (first item in the array) and the
     * {@link LanguageCoordinate#languageConceptNid()}.  If no descriptions match, the process is repeated
     * with each subsequent item in {@link LanguageCoordinate#descriptionTypePreferenceNidList()}, walking
     * through the array one by one.
     * <p>
     * For any given step, if multiple descriptions match the criteria, an ACTIVE description should have priority over
     * an inactive one.
     * <p>
     * To be returned, a description MUST match one of the description types, and the specified language.
     * <p>
     * If the specified language {@link LanguageCoordinate#languageConceptNid()} is {@link TinkarTerm#LANGUAGE},
     * then language will be considered to always match, ignoring the actual value of the language in the description.
     * This allows this method to be used with a fallback behavior - where it will match a description of any language,
     * but still rank by the requested type.
     * <p>
     * For any descriptions that matched the criteria, they are then compared with the requested
     * {@link LanguageCoordinate#dialectPatternPreferenceNidList()}
     * The dialect preferences are evaluated in array order.  Each description that has a dialect annotation that matches
     * the dialect preference, with a type of {@link TinkarTerm#PREFERRED}, it is advanced to the next ranking step (below)
     * <p>
     * If none of the descriptions has a dialect annotation of type {@link TinkarTerm#PREFERRED} that matches a dialect
     * in the {@link LanguageCoordinate#dialectPatternPreferenceNidList()}, then all matching language / type matching
     * descriptions are advanced to the next ranking step (below).
     * <p>
     * The final ranking step, is to evaluate {@link LanguageCoordinate#modulePreferenceNidListForLanguage()}
     * The module list is evaluated in order.  If a description matches the requested module, then it is placed
     * into the top position, so it is returned via {@link Latest#get()}.  All other descriptions are still
     * returned, but as part of the {@link Latest#contradictions()}.
     * <p>
     * If none of the description match a specified module ranking, then the descriptions are returned in an arbitrary order,
     * between {@link Latest#get()} and {@link Latest#contradictions()}.
     *
     * @param descriptionList List of descriptions to consider.
     * @return the specified description
     */
    Latest<SemanticEntityVersion> getSpecifiedDescription(ImmutableList<SemanticEntity> descriptionList);

    /**
     * Extracts text content from a given semantic version entity.
     *
     * @param semanticEntityVersion the semantic entity version from which the text should be extracted
     * @return an Optional containing the extracted text if present, or an empty Optional if no text is available
     */
    Optional<String> getTextFromSemanticVersion(SemanticEntityVersion semanticEntityVersion);

    /**
     * Retrieves the textual representation associated with the given stamp Nid. The
     * string representation for the given stamp is based on the
     * description type preferences of the view coordinate.
     * The returned string includes details such as state, time, author, module, and path.
     *
     * @param stampNid the unique identifier of the stamp for which the text is to be retrieved
     * @return the text corresponding to the specified stamp Nid
     */
    default String getTextForStamp(int stampNid) {
        return getTextForStamp(Entity.getStamp(stampNid));
    }
    /**
     * Creates a string representation for the given stamp, based on the
     * description type preferences of the view coordinate.
     * The returned string includes details such as state, time, author, module, and path.
     *
     * @param stamp the StampEntity instance for which the text representation is to be generated
     * @return a formatted string containing the state, time, author, module, and path details of the given stamp
     */
    default String getTextForStamp(StampEntity stamp) {
        StampVersion lastVersion = stamp.lastVersion();
        return "s:" + getDescriptionTextOrNid(lastVersion.stateNid()) +
                " t:" + DateTimeUtil.format(lastVersion.time(), DateTimeUtil.SEC_FORMATTER) +
                " a:" + getDescriptionTextOrNid(lastVersion.authorNid()) +
                " m:" + getDescriptionTextOrNid(lastVersion.moduleNid()) +
                " p:" + getDescriptionTextOrNid(lastVersion.pathNid());
    }

    /**
     * Retrieves the preferred text associated with a given stamp identifier.
     * The returned string includes details such as state, time, author, module, and path.
     *
     * @param stampNid the identifier of the stamp for which the preferred text is to be retrieved
     * @return the preferred text corresponding to the specified stamp identifier
     */
    default String getPreferredTextForStamp(int stampNid) {
        return getPreferredTextForStamp(Entity.getStamp(stampNid));
    }
    /**
     * Constructs a string representation of a {@link StampEntity}'s preferred text
     * by appending information about its state, time, author, module, and path.
     *
     * @param stamp the {@link StampEntity} whose preferred text is to be constructed
     * @return a concatenated string containing the state, time, author, module, and path
     *         information of the given stamp in a preferred representation
     */
    default String getPreferredTextForStamp(StampEntity stamp) {
        StampVersion lastVersion = stamp.lastVersion();
        return "s:" + getPreferredDescriptionTextOrNid(lastVersion.stateNid()) +
                " t:" + DateTimeUtil.format(lastVersion.time(), DateTimeUtil.SEC_FORMATTER) +
                " a:" + getPreferredDescriptionTextOrNid(lastVersion.authorNid()) +
                " m:" + getPreferredDescriptionTextOrNid(lastVersion.moduleNid()) +
                " p:" + getPreferredDescriptionTextOrNid(lastVersion.pathNid());
    }
    /**
     * Retrieves the fully qualified text representation associated with the specified stamp identifier.
     * The returned string includes details such as state, time, author, module, and path.
     *
     * @param stampNid the identifier of the stamp for which the fully qualified text is required
     * @return the fully qualified text corresponding to the provided stamp identifier
     */
    default String getFullyQualifiedTextForStamp(int stampNid) {
        return getFullyQualifiedTextForStamp(Entity.getStamp(stampNid));
    }
    /**
     * Generates a fully qualified text representation for the given stamp entity.
     * The returned string includes details such as state, time, author, module, and path.
     *
     * @param stamp The StampEntity for which the fully qualified representation is to be generated.
     * @return A string containing the fully qualified text representation of the stamp entity,
     *         including its state, time, author, module, and path information.
     */
    default String getFullyQualifiedTextForStamp(StampEntity stamp) {
        StampVersion lastVersion = stamp.lastVersion();
        StringBuilder sb = new StringBuilder();
        return "s:" + getFullyQualifiedNameTextOrNid(lastVersion.stateNid()) +
                " t:" + DateTimeUtil.format(lastVersion.time(), DateTimeUtil.TEXT_FORMAT_WITH_ZONE) +
                " a:" + getFullyQualifiedNameTextOrNid(lastVersion.authorNid()) +
                " m:" + getFullyQualifiedNameTextOrNid(lastVersion.moduleNid()) +
                " p:" + getFullyQualifiedNameTextOrNid(lastVersion.pathNid());
    }

    default String getFullyQualifiedDescriptionTextWithFallbackOrNid(EntityFacade entityFacade) {
        return getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade.nid());
    }

    /**
     * Retrieves the fully qualified description text for the given identifier (nid),
     * with a fallback mechanism when the fully qualified name is not available.
     * It attempts to retrieve a regular description text or semantic text if the
     * fully qualified name is not found. If none are found, the nid is returned as a string.
     *
     * @param nid the identifier for which the fully qualified description text is to be retrieved
     * @return the fully qualified description text, a regular description text, a semantic text,
     *         or the nid as a string if no description is found
     */
    default String getFullyQualifiedDescriptionTextWithFallbackOrNid(int nid) {
        Optional<String> optionalResult = getFullyQualifiedNameText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        optionalResult = getRegularDescriptionText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        optionalResult = getSemanticText(nid);
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        return Integer.toString(nid);
    }

    /**
     * @deprecated use {@code getPreferredDescriptionTextOrNid}
     */

    @Deprecated
    default String getPreferredDescriptionStringOrNid(int nid) {
        return toEntityStringOrNid(nid, this::getRegularDescriptionText);
    }

    /**
     * @deprecated use {@code getPreferredDescriptionTextOrNid}
     */
    @Deprecated
    default String getPreferredDescriptionStringOrNid(EntityFacade entityFacade) {
        return toEntityStringOrNid(entityFacade, this::getRegularDescriptionText);
    }

    /**
     * Retrieves the preferred description text or NID (numeric identifier) for a given entity.
     * The preferred description text is obtained using the provided method reference.
     *
     * @param entityFacade the entity facade representing the entity to retrieve the description or NID for
     * @return the preferred description text if available; otherwise, returns the NID of the entity
     */
    default String getPreferredDescriptionTextOrNid(EntityFacade entityFacade) {
        return toEntityStringOrNid(entityFacade, this::getRegularDescriptionText);
    }

    /**
     * Retrieves the preferred description text for the given nid. If no preferred description is found,
     * the nid itself is returned as a string.
     *
     * @param nid the numerical identifier for which the preferred description is to be retrieved
     * @return the preferred description text if available, otherwise the nid as a string
     */
    default String getPreferredDescriptionTextOrNid(int nid) {
        return toEntityStringOrNid(nid, this::getRegularDescriptionText);
    }

    /**
     * Converts the provided nid to its corresponding entity string using the given function.
     * If no entity string is found, the nid is returned as a string.
     *
     * @param nid the numeric identifier to be converted
     * @param toOptionalEntityString a function that takes an integer nid and returns an Optional containing the entity string if available
     * @return the entity string corresponding to the nid if present, otherwise the nid itself as a string
     */
    default String toEntityStringOrNid(int nid, Function<Integer, Optional<String>> toOptionalEntityString) {
        Optional<String> optionalEntityString = toOptionalEntityString.apply(nid);
        if (optionalEntityString.isPresent()) {
            return optionalEntityString.get();
        }
        return Integer.toString(nid);
    }

    /**
     * Converts the provided {@code entityFacade} to its corresponding entity string or returns its nid as a string
     * if no string representation is available.
     *
     * @param entityFacade the entity facade to be converted to a string representation or nid
     * @param toOptionalEntityString a function that attempts to obtain an optional string representation of the entity facade
     * @return the string representation of the entity facade if available, otherwise the nid of the entity facade as a string
     */
    default String toEntityStringOrNid(EntityFacade entityFacade, Function<EntityFacade, Optional<String>> toOptionalEntityString) {
        Optional<String> optionalEntityString = toOptionalEntityString.apply(entityFacade);
        if (optionalEntityString.isPresent()) {
            return optionalEntityString.get();
        }
        return Integer.toString(entityFacade.nid());
    }

    /**
     * Retrieves the regular description text associated with the given entity.
     *
     * @param entity an EntityFacade representing the entity for which the description text is to be retrieved.
     * @return an Optional containing the regular description text if available; otherwise, an empty Optional.
     */
    default Optional<String> getRegularDescriptionText(EntityFacade entity) {
        return getRegularDescriptionText(entity.nid());
    }

    /**
     * Used where a String property is optionally an Entity XML fragment, or
     * similar circumstances.
     *
     * @param possibleEntityString
     * @return
     */
    default String toPreferredEntityStringOrInputString(String possibleEntityString) {
        return toEntityStringOrInputString(possibleEntityString, this::getRegularDescriptionText);
    }

    /**
     * Transforms a potentially entity-related string into its corresponding entity string or returns
     * the input string if no valid entity transformation is possible.
     *
     * @param possibleEntityString the input string that may represent an entity.
     * @param toOptionalEntityString a function that takes an entity identifier (nid) and returns an
     *                               optional entity string corresponding to that identifier.
     * @return the transformed entity string if the input string corresponds to a valid entity, or the
     *         original input string if no transformation is possible.
     */
    default String toEntityStringOrInputString(String possibleEntityString, Function<Integer, Optional<String>> toOptionalEntityString) {
        Optional<EntityProxy> optionalEntity = ProxyFactory.fromXmlFragmentOptional(possibleEntityString);
        if (optionalEntity.isPresent()) {
            Optional<String> optionalEntityString = toOptionalEntityString.apply(optionalEntity.get().nid());
            if (optionalEntityString.isPresent()) {
                return optionalEntityString.get();
            }
        }
        return possibleEntityString;
    }

    /**
     * Used where a String property is optionally an Entity XML fragment, or
     * similar circumstances.
     *
     * @param possibleEntityString
     * @return
     */
    default String toFullyQualifiedEntityStringOrInputString(String possibleEntityString) {
        return toEntityStringOrInputString(possibleEntityString, this::getFullyQualifiedNameText);
    }

    /**
     * Converts the given EntityFacade instance into a string representation
     * using its public identifier and NID. Allows customization of the
     * description text through a provided function.
     *
     * @param entityFacade The EntityFacade instance that will be converted
     *                     to a string representation.
     * @return A string representing the entity using either its public
     *         identifier and NID, or an appropriate description.
     */
    default String toEntityStringOrPublicIdAndNid(EntityFacade entityFacade) {
        return toEntityStringOrPublicIdAndNid(entityFacade, this::getRegularDescriptionText);
    }

    /**
     * Converts the given {@code EntityFacade} to a string representation using the provided
     * {@code toOptionalEntityString} function. If the function returns an empty {@code Optional},
     * a default string format consisting of the public ID and the nid is used.
     *
     * @param entityFacade the entity facade to be converted into a string representation
     * @param toOptionalEntityString a function that takes an {@code EntityFacade} and
     *                                returns an {@code Optional<String>} containing a string
     *                                representation of the entity, or an empty {@code Optional}
     *                                if no representation is available
     * @return a string representation of the entity using the provided function or,
     *         if absent, a default format combining the public ID and the nid
     */
    default String toEntityStringOrPublicIdAndNid(EntityFacade entityFacade, Function<EntityFacade, Optional<String>> toOptionalEntityString) {
        Optional<String> optionalEntityString = toOptionalEntityString.apply(entityFacade);
        if (optionalEntityString.isPresent()) {
            return optionalEntityString.get();
        }
        return Entity.get(entityFacade).get().publicId().toString() + " <" + Integer.toString(entityFacade.nid()) + ">";
    }

    /**
     * Converts the given object into its string representation using the provided entity string conversion function.
     *
     * @param object the object to be converted to a string representation
     * @param toEntityString a function that defines how an EntityFacade should be converted into a string
     * @return the string representation of the given object
     */
    default String toEntityString(Object object, Function<EntityFacade, String> toEntityString) {
        StringBuilder sb = new StringBuilder();
        toEntityString(object, toEntityString, sb);
        return sb.toString();
    }

    /**
     * Converts the given object into an entity string representation and appends the result to the provided
     * {@code StringBuilder}. This method supports various input types such as {@link EntityFacade},
     * collections, arrays, and strings, processing each type accordingly.
     *
     * @param object the input object to be converted to a string. Supported types include {@link EntityFacade},
     *               collections, arrays, strings, and {@link Long}. If the provided object is null, no processing occurs.
     * @param toEntityString a {@link Function} that converts an {@link EntityFacade} instance into its string representation.
     *                       This function is used to determine how each {@link EntityFacade} is represented as a string.
     * @param sb the {@link StringBuilder} used to accumulate the resulting string representation of the object.
     *           This method appends the generated string directly to the builder.
     */
    default void toEntityString(Object object, Function<EntityFacade, String> toEntityString, StringBuilder sb) {
        if (object == null) {
            return;
        }
        if (object instanceof EntityFacade entityFacade) {
            sb.append(toEntityString.apply(entityFacade));
        } else if (object instanceof Collection collection) {

            if (object instanceof Set set) {
                // a set, so order does not matter. Alphabetic order desirable.
                if (set.isEmpty()) {
                    toEntityString(set.toArray(), toEntityString, sb);
                } else {
                    Object[] conceptSpecs = set.toArray();
                    Arrays.sort(conceptSpecs, (o1, o2) ->
                            NaturalOrder.compareStrings(toEntityString.apply((EntityFacade) o1), toEntityString.apply((EntityFacade) o2)));
                    toEntityString(conceptSpecs, toEntityString, sb);
                }
            } else {
                // not a set, so order matters
                toEntityString(collection.toArray(), toEntityString, sb);
            }
        } else if (object.getClass().isArray()) {
            Object[] a = (Object[]) object;
            final int iMax = a.length - 1;
            if (iMax == -1) {
                sb.append("[]");
            } else {
                sb.append('[');
                int indent = sb.length();
                for (int i = 0; ; i++) {
                    if (i > 0) {
                        sb.append('\u200A');
                    }
                    sb.append(toEntityString(a[i], toEntityString));
                    if (i == iMax) {
                        sb.append(']').toString();
                        return;
                    }
                    if (iMax > 0) {
                        sb.append(",\n");
                        for (int indentIndex = 0; indentIndex < indent; indentIndex++) {
                            sb.append('\u2004'); //
                        }
                    }
                }
            }
        } else if (object instanceof String string) {
            Optional<EntityProxy> optionalEntity = ProxyFactory.fromXmlFragmentOptional(string);
            if (optionalEntity.isPresent()) {
                sb.append(toEntityString(optionalEntity.get(), toEntityString));
            } else {
                sb.append(string);
            }
        } else if (object instanceof Long) {
            sb.append(DateTimeUtil.format((Long) object));
        } else {
            sb.append(object.toString());
        }
    }

    /**
     * Gets the latestDescription of type {@link TinkarTerm#REGULAR_NAME_DESCRIPTION_TYPE}, according to dialect preferences.
     * Will return empty, if no matching description type is found in this or any nested language coordinates
     *
     * @param descriptionList the latestDescription list
     * @return the regular name latestDescription, if available
     */
    default Latest<SemanticEntityVersion> getRegularDescription(ImmutableList<SemanticEntity> descriptionList) {
        return getSpecifiedDescription(descriptionList, IntIds.list.of(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid()));
    }

    /**
     * Same as getSpecifiedDescription(StampFilter stampCoordinateRecord,
     * List<SemanticChronology> descriptionList,
     * LanguageCoordinate languageCoordinate);
     * but allows the descriptionTypePriority to be independent of the coordinate, without forcing a clone of
     * the coordinate.
     *
     * @param descriptionList
     * @param descriptionTypePriority
     * @return
     */

    Latest<SemanticEntityVersion> getSpecifiedDescription(ImmutableList<SemanticEntity> descriptionList,
                                                          IntIdList descriptionTypePriority);

    /**
     * Return a description of type {@link TinkarTerm#DEFINITION_DESCRIPTION_TYPE}, or an empty latest version, if none are of type definition in this or any
     * nested language coordinates
     *
     * @param descriptionList
     * @return
     */
    default Latest<SemanticEntityVersion> getDefinitionDescription(ImmutableList<SemanticEntity> descriptionList) {
        return getSpecifiedDescription(descriptionList, IntIds.list.of(TinkarTerm.DEFINITION_DESCRIPTION_TYPE.nid()));
    }

    /**
     * Retrieves the definition description text for a given entity.
     *
     * @param entityFacade the EntityFacade object representing the entity for which
     *                     the definition description text is to be retrieved.
     * @return an Optional containing the definition description text if available;
     *         otherwise, an empty Optional.
     */
    default Optional<String> getDefinitionDescriptionText(EntityFacade entityFacade) {
        return getDefinitionDescriptionText(entityFacade.nid());
    }

    /**
     * Retrieves the definition description text for a given entity identified by its nid.
     * Utilizes a specific description type to fetch the corresponding text.
     *
     * @param entityNid the nid (numeric identifier) of the entity whose definition description text is to be retrieved
     * @return an Optional containing the definition description text if available, otherwise an empty Optional
     */
    default Optional<String> getDefinitionDescriptionText(int entityNid) {
        return getDescriptionTextForComponentOfType(entityNid, TinkarTerm.DEFINITION_DESCRIPTION_TYPE.nid());
    }

    /**
     * Retrieves the regular description for a given semantic entity.
     *
     * @param entity The {@code EntityFacade} representing the semantic entity for which
     *               the regular description is being retrieved.
     * @return A {@code Latest<SemanticEntityVersion>} containing the latest version of
     *         the regular description for the specified semantic entity.
     */
    default Latest<SemanticEntityVersion> getRegularDescription(EntityFacade entity) {
        return getRegularDescription(entity.nid());
    }

    /**
     * Gets the latestDescription of type {@link TinkarTerm#REGULAR_NAME_DESCRIPTION_TYPE}.  Will return empty, if
     * no matching description type is found in this or any nested language coordinates
     *
     * @param entityNid the conceptId to get the fully specified latestDescription for
     * @return the regular name latestDescription
     */
    default Latest<SemanticEntityVersion> getRegularDescription(int entityNid) {
        return getSpecifiedDescription(getDescriptionsForComponent(entityNid), IntIds.list.of(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid()));
    }

    /**
     * Gets the latestDescription of type {@link TinkarTerm#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if
     * no matching description type is found in this or any nested language coordinates
     *
     * @param descriptionList the latestDescription list
     * @return the regular name latestDescription, if available
     */
    default Latest<SemanticEntityVersion> getFullyQualifiedDescription(ImmutableList<SemanticEntity> descriptionList) {
        return getSpecifiedDescription(descriptionList, IntIds.list.of(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()));
    }

    /**
     * Retrieves the fully qualified description for the given entity.
     *
     * @param entityFacade The entity facade representing the entity for which the fully qualified description is to be obtained.
     * @return The latest version of the fully qualified description encapsulated in a {@code Latest<SemanticEntityVersion>} object.
     */
    default Latest<SemanticEntityVersion> getFullyQualifiedDescription(EntityFacade entityFacade) {
        return getFullyQualifiedDescription(entityFacade.nid());
    }

    /**
     * Gets the latestDescription of type {@link TinkarTerm#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if
     * no matching description type is found in this or any nested language coordinates
     *
     * @param conceptId the conceptId to get the fully specified latestDescription for
     * @return the fully specified latestDescription
     */
    default Latest<SemanticEntityVersion> getFullyQualifiedDescription(int conceptId) {
        return getSpecifiedDescription(getDescriptionsForComponent(conceptId), IntIds.list.of(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()));
    }

}
