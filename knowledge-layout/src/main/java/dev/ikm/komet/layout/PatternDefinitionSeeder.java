package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableComposer.EntityComposer;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the pattern-definition patterns of {@link PatternDefinitionTerms} — and the concepts
 * their field definitions reference — into the data store, so a pattern's own definition can be
 * authored through patterns like any other knowledge. Once seeded they appear in the KL editor
 * pattern browser and compose into windows with no special handling. Every stored pattern's
 * inline definition is then projected into semantics of these patterns (see
 * {@link #projectStoredPatternDefinitions}), so existing patterns display through
 * pattern-definition sections too.
 * <p>
 * A pattern created in the standard Pattern window is defined the other way round: its
 * definition is entered as semantics of these patterns, and its inline definition is empty until
 * {@link #writeInlineDefinition} writes it from them when the pattern publishes — the inline
 * definition being what a new semantic of the pattern takes its fields from. Which of the two is
 * the source for a given pattern is what {@link #isDefinedBySemantics} tells.
 * <p>
 * Seeding is idempotent: every component is written under a fixed public id (semantic ids are
 * derived by type-5 UUID from their referenced component), and components already present in
 * the store are left untouched.
 */
public final class PatternDefinitionSeeder {

    private static final String MEANING_AND_PURPOSE_DISCRIMINATOR = "meaning-and-purpose";
    private static final String FIELD_DISCRIMINATOR_PREFIX = "field-";

    /** Field of a pattern-definition pattern: all fields are component-typed. */
    private record FieldSpec(EntityProxy.Concept meaning, EntityProxy.Concept purpose) {
    }

    /** One field of a pattern's definition, as its Fields-pattern semantic states it. */
    private record FieldFromSemantic(int dataTypeNid, int purposeNid, int meaningNid) {
    }

    private PatternDefinitionSeeder() {
    }

    /**
     * Creates the pattern-definition patterns and their supporting concepts if they are not yet
     * present in the data store. Must be called on the JavaFX application thread (the composer
     * requires it).
     *
     * @param viewCalculator the view calculator the composer resolves against
     */
    public static void ensureSeeded(ViewCalculator viewCalculator) {
        ObservableComposer composer = ObservableComposer.create(viewCalculator, State.ACTIVE,
                TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH,
                "Seed pattern-definition patterns");

        ensureConcept(composer, PatternDefinitionTerms.PATTERN_DEFINITION);
        ensureConcept(composer, PatternDefinitionTerms.MEANING_AND_PURPOSE);
        ensureConcept(composer, PatternDefinitionTerms.FIELD_DEFINITION);
        ensureConcept(composer, KlTerms.PATTERN_MEANING_ATTRIBUTE);
        ensureConcept(composer, KlTerms.PATTERN_PURPOSE_ATTRIBUTE);
        ensureConcept(composer, KlTerms.FIELD_DATA_TYPE);
        ensureConcept(composer, KlTerms.FIELD_PURPOSE);
        ensureConcept(composer, KlTerms.FIELD_MEANING);
        ensureConcept(composer, KlTerms.FIELD_DEFAULTS_MODULE);

        ensurePattern(composer, PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN,
                PatternDefinitionTerms.MEANING_AND_PURPOSE, PatternDefinitionTerms.PATTERN_DEFINITION,
                "Meaning and Purpose Pattern",
                List.of(new FieldSpec(TinkarTerm.MEANING, KlTerms.PATTERN_MEANING_ATTRIBUTE),
                        new FieldSpec(TinkarTerm.PURPOSE, KlTerms.PATTERN_PURPOSE_ATTRIBUTE)));

        ensurePattern(composer, PatternDefinitionTerms.FIELDS_PATTERN,
                PatternDefinitionTerms.FIELD_DEFINITION, PatternDefinitionTerms.PATTERN_DEFINITION,
                "Fields Pattern",
                List.of(new FieldSpec(KlTerms.FIELD_DATA_TYPE, KlTerms.FIELD_DATA_TYPE),
                        new FieldSpec(KlTerms.FIELD_PURPOSE, KlTerms.FIELD_PURPOSE),
                        new FieldSpec(KlTerms.FIELD_MEANING, KlTerms.FIELD_MEANING)));

        projectStoredPatternDefinitions(composer, viewCalculator);

        composer.commit();
    }

    /**
     * Projects every stored pattern's definition — its meaning, purpose and field definitions,
     * which live inline in the pattern's own version record — into semantics of the
     * pattern-definition patterns, so pattern-definition sections in KL windows display existing
     * patterns like any other data. Semantic identities are T5-derived from the described pattern
     * ({@code "meaning-and-purpose"}, {@code "field-0"}, {@code "field-1"}, …), and semantics
     * already matching the inline definition are left untouched, so reconciliation is idempotent
     * and re-runs pick up later changes to a pattern's definition.
     */
    private static void projectStoredPatternDefinitions(ObservableComposer composer,
                                                        ViewCalculator viewCalculator) {
        MutableIntList patternNids = IntLists.mutable.empty();
        PrimitiveData.get().forEachPatternNid(patternNids::add);
        patternNids.forEach(patternNid -> viewCalculator.latest(patternNid).ifPresent(version -> {
            if (version instanceof PatternEntityVersion patternVersion) {
                projectPatternDefinition(composer, patternNid, patternVersion);
            }
        }));
    }

    private static void projectPatternDefinition(ObservableComposer composer, int patternNid,
                                                 PatternEntityVersion patternVersion) {
        if (isDefinedBySemantics(patternNid)) {
            // The semantics are the source of this pattern's definition, not a projection of it
            // (see writeInlineDefinition): projecting would describe each field a second time.
            return;
        }

        EntityProxy.Pattern describedPattern = EntityProxy.Pattern.make(patternNid);
        ensureProjectedSemantic(composer, describedPattern,
                PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN,
                semanticId(describedPattern.publicId(), MEANING_AND_PURPOSE_DISCRIMINATOR),
                List.of(EntityProxy.Concept.make(patternVersion.semanticMeaningNid()),
                        EntityProxy.Concept.make(patternVersion.semanticPurposeNid())));

        var fieldDefinitions = patternVersion.fieldDefinitions();
        for (int i = 0; i < fieldDefinitions.size(); i++) {
            var fieldDefinition = fieldDefinitions.get(i);
            ensureProjectedSemantic(composer, describedPattern, PatternDefinitionTerms.FIELDS_PATTERN,
                    semanticId(describedPattern.publicId(), FIELD_DISCRIMINATOR_PREFIX + i),
                    List.of(EntityProxy.Concept.make(fieldDefinition.dataTypeNid()),
                            EntityProxy.Concept.make(fieldDefinition.purposeNid()),
                            EntityProxy.Concept.make(fieldDefinition.meaningNid())));
        }
        // Field projections at indexes beyond the current field count (a later pattern version
        // with fewer fields) are not retired here: the semantic editable API has no state setter
        // yet. Field-count shrinkage does not occur in practice today.
    }

    /**
     * Whether the pattern-definition semantics referencing the pattern are the source of its
     * definition (the pattern was created in the standard Pattern window, which stores the
     * definition as those semantics) rather than a projection of its inline definition. The
     * projection mints every semantic under a T5-derived identity, the window under a random one:
     * a referencing semantic with an identity other than the projection's
     * ({@link #isProjectedSemanticId}) means the semantics came first.
     */
    private static boolean isDefinedBySemantics(int patternNid) {
        PublicId patternId = PrimitiveData.publicId(patternNid);
        List<PublicId> semanticIds = new ArrayList<>();
        PrimitiveData.get().forEachSemanticNidForComponentOfPattern(patternNid,
                PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN.nid(),
                semanticNid -> semanticIds.add(PrimitiveData.publicId(semanticNid)));
        int meaningAndPurposeCount = semanticIds.size();
        PrimitiveData.get().forEachSemanticNidForComponentOfPattern(patternNid,
                PatternDefinitionTerms.FIELDS_PATTERN.nid(),
                semanticNid -> semanticIds.add(PrimitiveData.publicId(semanticNid)));
        int fieldSemanticCount = semanticIds.size() - meaningAndPurposeCount;
        return semanticIds.stream()
                .anyMatch(semanticId -> !isProjectedSemanticId(patternId, semanticId, fieldSemanticCount));
    }

    /**
     * Whether a pattern-definition semantic's identity is one {@link #projectStoredPatternDefinitions}
     * derives for the described pattern: the meaning-and-purpose id, or a field id at an index
     * below the pattern's count of field semantics.
     */
    static boolean isProjectedSemanticId(PublicId patternId, PublicId semanticId, int fieldSemanticCount) {
        if (PublicId.equals(semanticId, semanticId(patternId, MEANING_AND_PURPOSE_DISCRIMINATOR))) {
            return true;
        }
        for (int i = 0; i < fieldSemanticCount; i++) {
            if (PublicId.equals(semanticId, semanticId(patternId, FIELD_DISCRIMINATOR_PREFIX + i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes a pattern's inline definition — its meaning, purpose and field definitions, which
     * live in the pattern's own version record and are what a new semantic of the pattern takes
     * its fields from — from the pattern-definition semantics describing it: the reverse of
     * {@link #projectStoredPatternDefinitions}, for a pattern defined by those semantics — created
     * in the standard Pattern window (ikmdev/komet-desktop#192). The meaning and purpose come from
     * the pattern's {@link PatternDefinitionTerms#MEANING_AND_PURPOSE_PATTERN} semantic, the
     * fields from its {@link PatternDefinitionTerms#FIELDS_PATTERN} semantics in the order the
     * store lists them — creation order, the order the window's Fields table shows them. The
     * semantics are read at the view's latest, so ones not published yet count: they publish
     * together with the pattern. Nothing is written when the inline definition already says the
     * same, so a publish that changed no definition adds no pattern version.
     * <p>
     * Must be called on the JavaFX application thread (the composer requires it).
     *
     * @param patternComposer the pattern's composer, in the transaction the definition publishes with
     * @param viewCalculator  the view the definition semantics are read at
     */
    public static void writeInlineDefinition(
            EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> patternComposer,
            ViewCalculator viewCalculator) {
        ObservablePattern pattern = patternComposer.getEntity();
        int patternNid = pattern.nid();
        Latest<PatternEntityVersion> currentVersion = viewCalculator.latestPatternEntityVersion(patternNid);

        // Absent a meaning-and-purpose semantic the pattern keeps what it has (a new pattern's
        // composer placeholders otherwise).
        int meaningNid = currentVersion.ifAbsentOrFunction(TinkarTerm.MEANING::nid,
                PatternEntityVersion::semanticMeaningNid);
        int purposeNid = currentVersion.ifAbsentOrFunction(TinkarTerm.PURPOSE::nid,
                PatternEntityVersion::semanticPurposeNid);
        PatternEntityVersion meaningAndPurposePattern = viewCalculator
                .latestPatternEntityVersion(PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN).get();
        List<SemanticEntityVersion> meaningAndPurposeSemantics = latestActiveSemantics(patternNid,
                PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN, viewCalculator);
        if (!meaningAndPurposeSemantics.isEmpty()) {
            SemanticEntityVersion meaningAndPurpose = meaningAndPurposeSemantics.getFirst();
            meaningNid = conceptNid(meaningAndPurpose, meaningAndPurposePattern.indexForMeaning(TinkarTerm.MEANING));
            purposeNid = conceptNid(meaningAndPurpose, meaningAndPurposePattern.indexForMeaning(TinkarTerm.PURPOSE));
        }

        PatternEntityVersion fieldsPattern = viewCalculator
                .latestPatternEntityVersion(PatternDefinitionTerms.FIELDS_PATTERN).get();
        int dataTypeIndex = fieldsPattern.indexForMeaning(KlTerms.FIELD_DATA_TYPE);
        int purposeIndex = fieldsPattern.indexForMeaning(KlTerms.FIELD_PURPOSE);
        int meaningIndex = fieldsPattern.indexForMeaning(KlTerms.FIELD_MEANING);
        List<FieldFromSemantic> fields = new ArrayList<>();
        for (SemanticEntityVersion fieldSemantic : latestActiveSemantics(patternNid,
                PatternDefinitionTerms.FIELDS_PATTERN, viewCalculator)) {
            fields.add(new FieldFromSemantic(conceptNid(fieldSemantic, dataTypeIndex),
                    conceptNid(fieldSemantic, purposeIndex), conceptNid(fieldSemantic, meaningIndex)));
        }

        if (currentVersion.isPresent()
                && matchesInlineDefinition(currentVersion.get(), meaningNid, purposeNid, fields)) {
            return;
        }

        ObservablePatternVersion.Editable patternEditable = patternComposer.getEditableVersion();
        patternEditable.getMeaningProperty().set(EntityProxy.Concept.make(meaningNid));
        patternEditable.getPurposeProperty().set(EntityProxy.Concept.make(purposeNid));
        int stampNid = patternEditable.getEditStamp().nid();
        MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.ofInitialCapacity(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            FieldFromSemantic field = fields.get(i);
            fieldDefinitions.add(new FieldDefinitionRecord(field.dataTypeNid(), field.purposeNid(),
                    field.meaningNid(), stampNid, patternNid, i));
        }
        patternEditable.setFieldDefinitions(fieldDefinitions.toImmutable());
        patternComposer.save();
    }

    private static boolean matchesInlineDefinition(PatternEntityVersion version, int meaningNid,
                                                   int purposeNid, List<FieldFromSemantic> fields) {
        if (version.semanticMeaningNid() != meaningNid || version.semanticPurposeNid() != purposeNid) {
            return false;
        }
        ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions = version.fieldDefinitions();
        if (fieldDefinitions.size() != fields.size()) {
            return false;
        }
        for (int i = 0; i < fields.size(); i++) {
            FieldDefinitionForEntity fieldDefinition = fieldDefinitions.get(i);
            FieldFromSemantic field = fields.get(i);
            if (fieldDefinition.dataTypeNid() != field.dataTypeNid()
                    || fieldDefinition.purposeNid() != field.purposeNid()
                    || fieldDefinition.meaningNid() != field.meaningNid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * The latest active versions, at the view, of the definition-pattern semantics referencing the
     * pattern, in the order the store lists the semantics. Unpublished versions count: the view's
     * latest ranks them first.
     */
    private static List<SemanticEntityVersion> latestActiveSemantics(int patternNid,
                                                                     EntityProxy.Pattern definitionPattern,
                                                                     ViewCalculator viewCalculator) {
        List<SemanticEntityVersion> versions = new ArrayList<>();
        EntityService.get().forEachSemanticForComponentOfPattern(patternNid, definitionPattern.nid(), semantic -> {
            Latest<SemanticEntityVersion> latest = viewCalculator.latest(semantic);
            if (latest.isPresent() && latest.get().active()) {
                versions.add(latest.get());
            }
        });
        return versions;
    }

    private static int conceptNid(SemanticEntityVersion semantic, int fieldIndex) {
        return ((EntityFacade) semantic.fieldValues().get(fieldIndex)).nid();
    }

    private static void ensureProjectedSemantic(ObservableComposer composer, EntityFacade describedPattern,
                                                EntityProxy.Pattern projectionPattern, PublicId semanticId,
                                                List<EntityProxy.Concept> values) {
        if (matchesExisting(semanticId, values)) {
            return;
        }
        var semanticComposer = composer.composeSemantic(semanticId, describedPattern, projectionPattern);
        var editable = semanticComposer.getEditableVersion();
        for (int i = 0; i < values.size(); i++) {
            editable.getEditableField(i).setObjectValue(values.get(i));
        }
        semanticComposer.save();
    }

    private static boolean matchesExisting(PublicId semanticId, List<EntityProxy.Concept> values) {
        if (!PrimitiveData.get().hasPublicId(semanticId)) {
            return false;
        }
        return EntityHandle.get(semanticId).asSemantic().map(semantic -> {
            SemanticEntityVersion latest = (SemanticEntityVersion) semantic.versions().getLast();
            if (!latest.active() || latest.fieldValues().size() != values.size()) {
                return false;
            }
            for (int i = 0; i < values.size(); i++) {
                if (!(latest.fieldValues().get(i) instanceof EntityFacade value)
                        || value.nid() != values.get(i).nid()) {
                    return false;
                }
            }
            return true;
        }).orElse(false);
    }

    private static void ensureConcept(ObservableComposer composer, EntityProxy.Concept concept) {
        if (isPresent(concept)) {
            return;
        }
        var conceptComposer = composer.composeConcept(concept.publicId());
        conceptComposer.save();
        writeDescription(composer, conceptComposer.getEntity(), concept.description(),
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, "fqn");
    }

    private static void ensurePattern(ObservableComposer composer, EntityProxy.Pattern pattern,
                                      EntityProxy.Concept meaning, EntityProxy.Concept purpose,
                                      String synonym, List<FieldSpec> fields) {
        if (isPresent(pattern)) {
            return;
        }
        var patternComposer = composer.composePattern(pattern.publicId());
        var patternEditable = patternComposer.getEditableVersion();
        patternEditable.getMeaningProperty().set(meaning);
        patternEditable.getPurposeProperty().set(purpose);

        int patternNid = PrimitiveData.nid(pattern.publicId());
        int stampNid = patternEditable.getEditStamp().nid();
        MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.ofInitialCapacity(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            fieldDefinitions.add(new FieldDefinitionRecord(TinkarTerm.COMPONENT_FIELD.nid(),
                    field.purpose().nid(), field.meaning().nid(), stampNid, patternNid, i));
        }
        patternEditable.setFieldDefinitions(fieldDefinitions.toImmutable());
        patternComposer.save();

        writeDescription(composer, patternComposer.getEntity(), pattern.description(),
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, "fqn");
        writeDescription(composer, patternComposer.getEntity(), synonym,
                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, "synonym");
    }

    private static void writeDescription(ObservableComposer composer, EntityFacade referenced,
                                         String text, EntityProxy.Concept descriptionType,
                                         String discriminator) {
        PublicId descriptionId = semanticId(referenced.publicId(), discriminator);
        var descriptionComposer = composer.composeSemantic(descriptionId, referenced,
                TinkarTerm.DESCRIPTION_PATTERN);
        var description = descriptionComposer.getEditableVersion();
        description.getEditableField(0).setObjectValue(TinkarTerm.ENGLISH_LANGUAGE);
        description.getEditableField(1).setObjectValue(text);
        description.getEditableField(2).setObjectValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
        description.getEditableField(3).setObjectValue(descriptionType);
        descriptionComposer.save();

        var dialectComposer = composer.composeSemantic(semanticId(descriptionId, "us-dialect"),
                descriptionComposer.getEntity(), TinkarTerm.US_DIALECT_PATTERN);
        dialectComposer.getEditableVersion().getEditableField(0).setObjectValue(TinkarTerm.ACCEPTABLE);
        dialectComposer.save();
    }

    private static PublicId semanticId(PublicId referencedId, String discriminator) {
        UUID namespace = referencedId.asUuidArray()[0];
        return PublicIds.of(UuidT5Generator.get(namespace, discriminator));
    }

    private static boolean isPresent(EntityFacade facade) {
        // hasPublicId first: EntityHandle.get would mint a nid for a never-seen public id.
        PublicId publicId = facade.publicId();
        return PrimitiveData.get().hasPublicId(publicId) && EntityHandle.get(publicId).isPresent();
    }
}