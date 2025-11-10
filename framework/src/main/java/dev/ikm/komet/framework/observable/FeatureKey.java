package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.observable.key.*;
import dev.ikm.tinkar.common.binary.Encodable;

/**
 * If we want to include a dynamic value for a locator, such as specifying a locator for a specific pattern,
 * we can't use enums. We could use a record that contains an enum. But that is redundant, perhaps.
 */
public sealed interface FeatureKey extends Encodable {

    default Feature<?> feature() {
        if (!this.isResolvable()) {
            throw new IllegalStateException("Key is not resolvable: " + this);
        }
        return switch (this) {
            case ChronologyFeature chronologyFeature ->
                    ObservableEntity.get(chronologyFeature.nid()).getFeature(chronologyFeature);
            case VersionFeature versionFeature ->
                    ObservableEntity.get(versionFeature.nid()).getFeature(versionFeature);
        };
    }
    static FeatureKey anyVersion() {
        return Entity.Version(FeatureKey.WILDCARD, FeatureKey.WILDCARD);
    }

    /**
     * Determines whether this {@code FeatureKey} is sufficiently specified
     * to uniquely identify and retrieve a single feature.
     * <p>
     * A resolvable key contains enough information (i.e., is not a wildcard or ambiguous)
     * to be used for unambiguous feature lookup.
     *
     * @return {@code true} if this key uniquely identifies a specific feature;
     *         {@code false} if it is too general or ambiguous for resolution.
     */
    boolean isResolvable();

    class Entity {
        public static ChronologyFeature.Chronology Object() {
            return new ChronologyKey();
        }

        public static ChronologyFeature.Chronology Object(int nid) {
            return new ChronologyKey(nid);
        }

        public static ChronologyFeature.PublicId PublicId() {
            return new PublicIdKey();
        }

        public static ChronologyFeature.PublicId PublicId(int nid) {
            return new PublicIdKey(nid);
        }

        public static ChronologyFeature.VersionSet VersionSet() {
            return new VersionSetKey();
        }

        public static ChronologyFeature.VersionSet VersionSet(int nid) {
            return new VersionSetKey(nid);
        }

        public static ChronologyFeature.Version Version(int nid, int stampNid) {
            return new VersionKey(nid, stampNid);
        }

        public static ChronologyFeature.Semantic.Pattern SemanticPattern() {
            return new PatternForSemanticKey();
        }

        public static ChronologyFeature.Semantic.Pattern SemanticPattern(int nid) {
            return new PatternForSemanticKey(nid);
        }

        public static ChronologyFeature.Semantic.ReferencedComponent SemanticReferencedComponent() {
            return new ReferencedComponentForSemanticKey();
        }

        public static ChronologyFeature.Semantic.ReferencedComponent SemanticReferencedComponent(int nid) {
            return new ReferencedComponentForSemanticKey(nid);
        }
    }

    class Version {
        public static VersionFeature.VersionStamp VersionStamp(int nid, int stampNid) {
            return new VersionStampKey(nid, stampNid);
        }

        public static VersionFeature.VersionStamp VersionStamp() {
            return new VersionStampKey();
        }

        public static VersionFeature.Pattern.PatternMeaning PatternMeaning() {
            return new PatternMeaningKey();
        }

        public static VersionFeature.Pattern.PatternMeaning PatternMeaning(int nid, int stampNid) {
            return new PatternMeaningKey(nid, stampNid);
        }

        public static VersionFeature.Pattern.PatternPurpose PatternPurpose() {
            return new PatternPurposeKey();
        }

        public static VersionFeature.Pattern.PatternPurpose PatternPurpose(int nid, int stampNid) {
            return new PatternPurposeKey(nid, stampNid);
        }

        public static VersionFeature.Pattern.FieldDefinitionList PatternFieldDefinitionList() {
            return new FieldDefinitionListKey();
        }

        public static VersionFeature.Pattern.FieldDefinitionList PatternFieldDefinitionList(int nid, int stampNid) {
            return new FieldDefinitionListKey(nid, stampNid);
        }

        public static VersionFeature.Pattern.FieldDefinitionListItem PatternFieldDefinitionListItem(int index) {
            return new FieldDefinitionListItemKey(index, FeatureKey.WILDCARD);
        }

        public static VersionFeature.Pattern.FieldDefinitionListItem PatternFieldDefinitionListItem(int index, int patternNid) {
            return new FieldDefinitionListItemKey(index, patternNid);
        }

        public static VersionFeature.Pattern.FieldDefinitionListItem PatternFieldDefinitionListItem(int nid, int index, int patternNid, int stampNid) {
            return new FieldDefinitionListItemKey(nid, index, patternNid, stampNid);
        }

        public static VersionFeature.Semantic.FieldList SemanticFieldList() {
            return new SemanticFieldListKey();
        }

        public static VersionFeature.Semantic.FieldList SemanticFieldList(int nid, int stampNid) {
            return new SemanticFieldListKey(nid, stampNid);
        }

        public static VersionFeature.Semantic.FieldListItem SemanticFieldListItem(int index) {
            return new SemanticFieldListItemKey(index, FeatureKey.WILDCARD);
        }

        public static VersionFeature.Semantic.FieldListItem SemanticFieldListItem(int index, int patternNid) {
            return new SemanticFieldListItemKey(index, patternNid);
        }

        public static VersionFeature.Semantic.FieldListItem SemanticFieldListItem(int nid, int index, int patternNid, int stampNid) {
            return new SemanticFieldListItemKey(nid, index, patternNid, stampNid);
        }

        public static VersionFeature.Stamp.Status StampStatus(int nid) {
            return new StatusForStampKey(nid, nid);
        }

        public static VersionFeature.Stamp.Status StampStatus() {
            return new StatusForStampKey();
        }

        public static VersionFeature.Stamp.Time StampTime() {
            return new TimeForStampKey();
        }

        public static VersionFeature.Stamp.Time StampTime(int nid) {
            return new TimeForStampKey(nid, nid);
        }

        public static VersionFeature.Stamp.Author StampAuthor() {
            return new AuthorForStampKey();
        }

        public static VersionFeature.Stamp.Author StampAuthor(int nid) {
            return new AuthorForStampKey(nid, nid);
        }

        public static VersionFeature.Stamp.Module StampModule() {
            return new ModuleForStampKey();
        }

        public static VersionFeature.Stamp.Module StampModule(int nid) {
            return new ModuleForStampKey(nid, nid);
        }

        public static VersionFeature.Stamp.Path StampPath() {
            return new PathForStampKey();
        }

        public static VersionFeature.Stamp.Path StampPath(int nid) {
            return new PathForStampKey(nid, nid);
        }
    }


    int WILDCARD = Integer.MAX_VALUE;

    /**
     * Native identifier or wildcard for the component being located.
     *
     * @return
     */
    int nid();

    default boolean match(FeatureKey another) {
        return match(this, another);
    }

    /**
     * Determines whether two {@link FeatureKey} objects match based on specific conditions.
     * This method checks for equality and implements additional matching rules for specific
     * subclasses of {@link FeatureKey}, such as {@link FieldDefinitionListItemKey} and
     * {@link SemanticFieldListItemKey}. Note that a wildcard will match any number (index, nid, or patternNid),
     * but that any specific number will not match a wildcard. Matches are therefore order-dependent. A.matches(B)
     * does not guarantee that B.matches(A).
     *
     * @param first  the first {@link FeatureKey} to compare.
     * @param second the second {@link FeatureKey} to compare.
     * @return true if the two {@link FeatureKey} objects are considered a match based on
     * equality or the defined rules for a pattern wildcard, false otherwise.
     */
    static boolean match(FeatureKey first, FeatureKey second) {
        if (first.getClass() != second.getClass()) {
            return false;
        }
        if (first.equals(second)) {
            return true;
        }
        // Classes are equal, but fields are not. Check for wildcards.
        return switch (first) {

            case VersionKey firstLocator
                    when second instanceof VersionKey secondLocator ->
                    (firstLocator.nid() == WILDCARD || firstLocator.nid() == secondLocator.nid())
                            && (firstLocator.stampNid() == WILDCARD || firstLocator.stampNid() == secondLocator.stampNid());

            case FieldDefinitionListItemKey firstLocator
                    when second instanceof FieldDefinitionListItemKey secondLocator ->
                    (firstLocator.nid() == WILDCARD || firstLocator.nid() == secondLocator.nid())
                            && (firstLocator.patternNid() == WILDCARD || firstLocator.patternNid() == secondLocator.patternNid())
                            && (firstLocator.stampNid() == WILDCARD || firstLocator.stampNid() == secondLocator.stampNid())
                            && (firstLocator.index() == WILDCARD || firstLocator.index() == secondLocator.index());

            case SemanticFieldListItemKey firstLocator
                    when second instanceof SemanticFieldListItemKey secondLocator ->
                    (firstLocator.nid() == WILDCARD || firstLocator.nid() == secondLocator.nid())
                            && (firstLocator.patternNid() == WILDCARD || firstLocator.patternNid() == secondLocator.patternNid())
                            && (firstLocator.stampNid() == WILDCARD || firstLocator.stampNid() == secondLocator.stampNid())
                            && (firstLocator.index() == WILDCARD || firstLocator.index() == secondLocator.index());

            // All other classes have only nids: no index, patternNid, or stampNid.
            default -> first.nid() == WILDCARD;
        };
    }

    /**
     * Represents an item in a list with an index property.
     * This interface provides a method to retrieve a locator that contains the index of the item
     * and serves as a base for various types of list items with specific characteristics.
     *
     * <p>
     * Key Characteristics:
     * <p>- A sealed interface, ensuring the implementation subclasses are finite and known.
     * <p>- Permits derivation by specific list item types with additional properties or behaviors.
     * <p>- Focuses on maintaining an index for each item.
     * <p>
     * Implementing classes are expected to refine this interface to represent specific
     * types of list items while adhering to the index-based organizational model.
     */
    sealed interface ListItem {
        int index();
    }

    sealed interface EnclosingComponent {

    }

    /**
     * Represents a locator for property of a chronology entity. This interface acts as the root
     * of a hierarchical structure that defines various property locators associated with
     * a chronology, such as identifiers, versions, and semantics. It extends the
     * {@link FeatureKey} interface and utilizes the sealed interface mechanism to
     * restrict and organize its subtypes, ensuring that all possible subtypes
     * are explicitly declared and known at compile time.
     */
    sealed interface ChronologyFeature extends FeatureKey {
        sealed interface Chronology extends ChronologyFeature, EnclosingComponent permits ChronologyKey {
        }

        sealed interface PublicId extends ChronologyFeature permits PublicIdKey {
        }

        /**
         * Represents a version list property within the chronology framework. The `VersionList`
         * interface extends the `ChronologyProperty` interface and is a sealed interface
         * restricted to known implementations. This allows for controlled subtype hierarchies
         * directly related to version list functionalities.
         * <p>
         * Key characteristics:
         * <p> - Provides a method to retrieve an instance of a version list locator.
         * <p> - Acts as a specific property type in the broader chronology property hierarchy.
         * <p> - Designed to ensure compile-time safety for supported implementations.
         *
         */
        sealed interface VersionSet extends ChronologyFeature permits VersionSetKey {
        }

        /**
         * Represents an item in a version list as part of a chronology property hierarchy.
         * This interface extends both `ListItem` and `ChronologyProperty` to define an item
         * that is associated with a specific index and belongs to the version-related scope
         * of a chronology.
         * <p>
         * Key characteristics:
         * - Sealed interface ensuring implementations are finite and explicitly defined.
         * - Combines indexing capabilities from `ListItem` with properties tied to
         * chronology versioning.
         * - Provides a static factory method to retrieve a specific implementation
         * of `VersionListItem` based on its index.
         * <p>
         * Method Details:
         * - `get(int index)`: Retrieves an instance of `VersionListItem` with the specified index.
         * This is implemented via an underlying constructor call to the appropriate subtype
         * (`VersionListItemLocator`).
         * <p>
         * Intended use cases include scenarios requiring the association of specific
         * indices with properties in a versioned chronology data structure.
         */
        sealed interface Version extends ChronologyFeature, EnclosingComponent permits VersionKey {
            int stampNid();
        }

        sealed interface Semantic extends ChronologyFeature {
            sealed interface Pattern extends Semantic permits PatternForSemanticKey {
            }

            sealed interface ReferencedComponent extends Semantic permits ReferencedComponentForSemanticKey {
            }
        }
    }

    sealed interface VersionFeature extends FeatureKey {
        int stampNid();

        sealed interface PatternDefinedItem extends VersionFeature {
            //TODO: We are transitioning to all Features being pattern defined items. Need to consider how to update.
            int patternNid();
        }

        sealed interface VersionStamp extends VersionFeature permits VersionStampKey {
        }

        sealed interface Pattern extends VersionFeature {
            sealed interface PatternMeaning extends Pattern permits PatternMeaningKey {
            }

            sealed interface PatternPurpose extends Pattern permits PatternPurposeKey {
            }

            sealed interface FieldDefinitionList extends Pattern permits FieldDefinitionListKey {
            }

            sealed interface FieldDefinitionListItem extends Pattern, ListItem, PatternDefinedItem permits FieldDefinitionListItemKey {
            }
        }

        sealed interface Semantic extends VersionFeature {
            sealed interface FieldList extends Semantic permits SemanticFieldListKey {
            }

            sealed interface FieldListItem extends Semantic, ListItem, PatternDefinedItem permits SemanticFieldListItemKey {
            }
        }

        sealed interface Stamp extends VersionFeature {
            sealed interface Status extends Stamp permits StatusForStampKey {
            }

            sealed interface Time extends Stamp permits TimeForStampKey {
            }

            sealed interface Author extends Stamp permits AuthorForStampKey {
            }

            sealed interface Module extends Stamp permits ModuleForStampKey {
            }

            sealed interface Path extends Stamp permits PathForStampKey {
            }
        }
    }
}


