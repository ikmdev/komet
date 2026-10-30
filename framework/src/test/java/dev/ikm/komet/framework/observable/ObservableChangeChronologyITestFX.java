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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.observable.ObservableChangeChronology.FieldChange;
import dev.ikm.komet.framework.observable.ObservableChangeChronology.VersionChange;
import dev.ikm.komet.framework.observable.key.FieldDefinitionListItemKey;
import dev.ikm.komet.framework.observable.key.PatternMeaningKey;
import dev.ikm.komet.framework.observable.key.PatternPurposeKey;
import dev.ikm.komet.framework.observable.key.SemanticFieldListItemKey;
import dev.ikm.komet.framework.observable.key.TimeForStampKey;
import dev.ikm.komet.framework.observable.key.VersionKey;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.NonExistentValue;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import javafx.collections.ListChangeListener;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ObservableChangeChronology} follows the canonical observable of its entity: a version
 * written is an add, a pending version edited or committed is a replace under the same key, a
 * canceled one is a remove, and every entry is announced again when a semantic's pattern is
 * revised. One chronology per entity, whatever the view.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableChangeChronologyITestFX {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    @BeforeAll
    void setupDatabase() throws IOException {
        CachingService.clearAll();
        // Without a root the search provider names its Lucene directory after the JVM UUID string,
        // whose colons Windows refuses; a temporary root keeps the index somewhere every OS allows.
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, Files.createTempDirectory("observable-change-chronology").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    void teardownDatabase() {
        PrimitiveData.stop();
    }

    @Test
    @RunOnJavaFXThread
    void writingAVersionIsOneAddWithItsFieldChanges() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        assertEquals(1, chronology.changes().size(), "the committed version's entry");
        FieldChange first = commentField(chronology.changes().getFirst());
        assertInstanceOf(NonExistentValue.class, first.prior().value(), "a first version's field did not exist before");
        assertEquals(StampRecord.nonExistentStamp().nid(), first.prior().versionStampNid(), "its prior stamp is the nonexistent stamp");
        assertEquals("first", first.current().value());

        Recorder recorder = new Recorder(chronology);
        ObservableComposer composer = composer(KernelConcept.USER);
        saveComment(composer, comment, "second");

        assertEquals(List.of("add"), recorder.events, "one add");
        assertEquals(2, chronology.changes().size());
        VersionChange pending = chronology.changes().getFirst();
        assertTrue(pending.version().getVersionRecord().uncommitted(), "the pending version is newest");
        assertEquals(new VersionKey(comment.nid, pending.version().getVersionRecord().stampNid()), pending.key());
        FieldChange edit = commentField(pending);
        assertEquals("first", edit.prior().value(), "the pending version follows the committed one");
        assertEquals("second", edit.current().value());
        assertEquals(Long.MAX_VALUE, stampTimeField(pending).current().value(), "its stamp time is not set yet");
        composer.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void editingAPendingVersionInPlaceIsOneReplaceWithTheSameKey() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        ObservableComposer composer = composer(KernelConcept.USER);
        ObservableSemanticVersion.Editable editable = saveComment(composer, comment, "second");
        VersionChange before = chronology.changes().getFirst();

        Recorder recorder = new Recorder(chronology);
        setComment(editable, "third");
        editable.save();

        assertEquals(List.of("replace"), recorder.events, "one replace");
        VersionChange after = chronology.changes().getFirst();
        assertSame(before, recorder.removed.getFirst());
        assertSame(after, recorder.added.getFirst());
        assertEquals(before.key(), after.key(), "the same version key");
        assertSame(before.version(), after.version(), "the same observable version, set in place");
        assertEquals("first", commentField(after).prior().value());
        assertEquals("third", commentField(after).current().value(), "the new value");
        composer.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void committingIsOneReplaceWhoseStampTimeIsNowSet() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        ObservableComposer composer = composer(KernelConcept.USER);
        saveComment(composer, comment, "second");
        VersionChange pending = chronology.changes().getFirst();

        Recorder recorder = new Recorder(chronology);
        composer.commit();

        assertEquals(List.of("replace"), recorder.events, "one replace");
        VersionChange committed = chronology.changes().getFirst();
        assertEquals(pending.key(), committed.key(), "the same version key");
        assertSame(pending.version(), committed.version(), "the same observable version, set in place");
        long time = Entity.getStamp(committed.key().stampNid()).time();
        assertNotEquals(Long.MAX_VALUE, time, "the stamp is committed");
        assertEquals(time, stampTimeField(committed).current().value(), "the entry records the commit time");
        assertEquals("second", commentField(committed).current().value());
    }

    @Test
    @RunOnJavaFXThread
    void cancelingAPendingVersionIsOneRemove() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        ObservableComposer canceled = composer(KernelConcept.USER);
        saveComment(canceled, comment, "canceled");
        ObservableComposer kept = composer(KernelConcept.KOMET_USER);
        saveComment(kept, comment, "kept");
        assertEquals(3, chronology.changes().size());
        VersionChange keptBefore = pending(chronology, "kept");
        VersionChange canceledEntry = pending(chronology, "canceled");

        Recorder recorder = new Recorder(chronology);
        canceled.cancel();

        assertEquals(List.of("remove"), recorder.events, "one remove");
        assertSame(canceledEntry, recorder.removed.getFirst(), "the canceled version's entry");
        assertEquals(2, chronology.changes().size());
        VersionChange keptAfter = pending(chronology, "kept");
        assertSame(keptBefore, keptAfter, "the other pending version's entry is unchanged");
        assertEquals("first", commentField(keptAfter).prior().value(), "and still follows the committed version");
        kept.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void twoPendingVersionsFromTwoAuthorsAreBothPresentEachOnTheCommittedVersion() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        ObservableComposer first = composer(KernelConcept.USER);
        saveComment(first, comment, "pending first");
        ObservableComposer second = composer(KernelConcept.KOMET_USER);
        saveComment(second, comment, "pending second");

        assertEquals(3, chronology.changes().size(), "the committed version and both pending ones");
        Set<Object> pending = chronology.changes().stream()
                .filter(change -> change.version().getVersionRecord().uncommitted())
                .map(change -> commentField(change).current().value())
                .collect(Collectors.toSet());
        assertEquals(Set.of("pending first", "pending second"), pending);
        for (VersionChange change : chronology.changes()) {
            if (change.version().getVersionRecord().uncommitted()) {
                assertEquals("first", commentField(change).prior().value(), "each pending version follows the committed one");
            }
        }
        first.cancel();
        second.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void aPatternEditRecordsMeaningPurposeAndFieldDefinitionsUnderThePatternKeys() {
        PublicId patternId = PublicIds.newRandom();
        ObservableComposer creator = composer(KernelConcept.USER);
        long patternNid = writePattern(creator, patternId, KernelConcept.ENGLISH_LANGUAGE, KernelConcept.ENGLISH_LANGUAGE,
                KernelConcept.PREFERRED);
        creator.commit();
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(patternNid);
        assertEquals(1, chronology.changes().size());
        VersionChange first = chronology.changes().getFirst();
        assertEquals(1, first.changes().count(change -> change.key() instanceof PatternMeaningKey), "a first version records its meaning");
        assertEquals(1, first.changes().count(change -> change.key() instanceof PatternPurposeKey), "and its purpose");
        assertEquals(2, first.changes().count(change -> change.key() instanceof FieldDefinitionListItemKey), "and each field definition");

        Recorder recorder = new Recorder(chronology);
        ObservableComposer editor = composer(KernelConcept.USER);
        ObservablePatternVersion.Editable editable = editor.composePattern(patternId).getEditableVersion();
        editable.getMeaningProperty().set(KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE);
        // The first field's purpose changes; the second field stays as it was.
        editable.setFieldDefinitions(fieldDefinitions(editable.getEditStamp().nid(), patternNid,
                KernelConcept.PREFERRED, KernelConcept.PREFERRED));
        editable.save();

        assertEquals(List.of("add"), recorder.events, "the pending pattern version is one add");
        VersionChange pending = chronology.changes().getFirst();
        FieldChange meaning = pending.changes().detect(change -> change.key() instanceof PatternMeaningKey);
        assertNotNull(meaning, "the meaning changed");
        assertEquals(new PatternMeaningKey(patternNid, pending.key().stampNid()), meaning.key());
        assertEquals(KernelConcept.ENGLISH_LANGUAGE.nid(), nid(meaning.prior().value()));
        assertEquals(KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE.nid(), nid(meaning.current().value()));
        assertNull(pending.changes().detect(change -> change.key() instanceof PatternPurposeKey), "the purpose did not change");
        FieldChange definition = pending.changes().detect(change -> change.key() instanceof FieldDefinitionListItemKey);
        assertNotNull(definition, "the first field's purpose changed");
        assertEquals(new FieldDefinitionListItemKey(patternNid, 0, patternNid, pending.key().stampNid()), definition.key());
        assertEquals(KernelConcept.ENGLISH_LANGUAGE.nid(), ((FieldDefinitionForEntity) definition.prior().value()).purposeNid());
        assertEquals(KernelConcept.PREFERRED.nid(), ((FieldDefinitionForEntity) definition.current().value()).purposeNid());
        assertEquals(1, pending.changes().count(change -> change.key() instanceof FieldDefinitionListItemKey),
                "the second field, unchanged, is not recorded");
        editor.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void revisingASemanticsPatternReannouncesEveryEntryAsAReplace() {
        PublicId patternId = PublicIds.newRandom();
        ObservableComposer creator = composer(KernelConcept.USER);
        long patternNid = writePattern(creator, patternId, KernelConcept.ENGLISH_LANGUAGE, KernelConcept.ENGLISH_LANGUAGE,
                KernelConcept.PREFERRED);
        creator.commit();
        ObservableComposer writer = composer(KernelConcept.USER);
        var semanticComposer = writer.composeSemantic(PublicIds.newRandom(), KernelConcept.USER, EntityProxy.Pattern.make(patternId));
        setComment(semanticComposer.getEditableVersion(), "field one");
        semanticComposer.save();
        writer.commit();
        long semanticNid = semanticComposer.getEntity().nid();
        ObservableChangeChronology chronology = ObservableEntityHandle.getChangeChronologyOrThrow(semanticNid);
        List<VersionChange> entries = List.copyOf(chronology.changes());
        assertEquals(1, entries.size());

        Recorder recorder = new Recorder(chronology);
        ObservableComposer editor = composer(KernelConcept.USER);
        ObservablePatternVersion.Editable patternEditable = editor.composePattern(patternId).getEditableVersion();
        patternEditable.setFieldDefinitions(fieldDefinitions(patternEditable.getEditStamp().nid(), patternNid,
                KernelConcept.PREFERRED, KernelConcept.PREFERRED));
        patternEditable.save();

        assertEquals(List.of("replace"), recorder.events, "each entry announced again as a replace");
        assertEquals(entries, recorder.removed, "the same entries");
        assertEquals(entries, recorder.added, "with the same content");
        assertSame(entries.getFirst(), recorder.added.getFirst(), "the very same object");
        assertEquals(entries, List.copyOf(chronology.changes()));
        editor.cancel();
    }

    @Test
    @RunOnJavaFXThread
    void twoViewsOnDifferentCoordinatesGetTheSameInstance() {
        Semantic comment = committedComment("first");
        ObservableChangeChronology onDevelopment = ObservableEntityHandle.getChangeChronologyOrThrow(comment.nid);
        ObservableChangeChronology onMaster = ObservableEntityHandle.get(comment.nid).asChangeChronology().orElseThrow();
        assertSame(onDevelopment, onMaster, "one chronology per entity, whatever the view");
        // Each view names the fields through its own calculator.
        FieldChange field = commentField(onDevelopment.changes().getFirst());
        ViewCalculator view = Calculators.View.Default();
        assertEquals(field.current().fieldDefinition(view).meaningNid(),
                view.latestPatternEntityVersion(KernelPattern.COMMENT_PATTERN.nid()).get().fieldDefinitions().get(0).meaningNid(),
                "the field is named from the view's pattern version");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** A comment semantic on a concept of its own, by its nid and public id. */
    private record Semantic(long nid, PublicId publicId, ObservableConcept concept) {
    }

    private static ObservableComposer composer(ConceptFacade author) {
        return ObservableComposer.builder()
                .viewCalculator(Calculators.View.Default())
                .author(author)
                .module(KernelConcept.PRIMORDIAL_MODULE)
                .path(KernelConcept.DEVELOPMENT_PATH)
                .defaultState(State.ACTIVE)
                .transactionComment("change chronology test")
                .build();
    }

    /** A new concept with one committed comment of the given text. */
    private static Semantic committedComment(String text) {
        ObservableComposer composer = composer(KernelConcept.USER);
        var conceptComposer = composer.composeConcept(PublicIds.newRandom());
        conceptComposer.save();
        ObservableConcept concept = conceptComposer.getEntity();
        PublicId semanticId = PublicIds.newRandom();
        var semanticComposer = composer.composeSemantic(semanticId, concept, KernelPattern.COMMENT_PATTERN);
        setComment(semanticComposer.getEditableVersion(), text);
        semanticComposer.save();
        composer.commit();
        return new Semantic(semanticComposer.getEntity().nid(), semanticId, concept);
    }

    /** Saves a pending version of the comment with the given text in the composer's transaction. */
    private static ObservableSemanticVersion.Editable saveComment(ObservableComposer composer, Semantic comment, String text) {
        ObservableSemanticVersion.Editable editable = composer
                .composeSemantic(comment.publicId(), comment.concept(), KernelPattern.COMMENT_PATTERN)
                .getEditableVersion();
        setComment(editable, text);
        editable.save();
        return editable;
    }

    @SuppressWarnings("unchecked")
    private static void setComment(ObservableSemanticVersion.Editable editable, String text) {
        ((ObservableField.Editable<Object>) editable.getEditableFields().get(0)).setValue(text);
    }

    /** A pattern with two string fields, their purposes given, saved in the composer's transaction. */
    private static long writePattern(ObservableComposer composer, PublicId patternId, ConceptFacade meaning,
                                     ConceptFacade firstPurpose, ConceptFacade secondPurpose) {
        var patternComposer = composer.composePattern(patternId);
        ObservablePatternVersion.Editable editable = patternComposer.getEditableVersion();
        editable.getMeaningProperty().set(meaning);
        editable.getPurposeProperty().set(KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE);
        long patternNid = PrimitiveData.nid(patternId);
        editable.setFieldDefinitions(fieldDefinitions(editable.getEditStamp().nid(), patternNid, firstPurpose, secondPurpose));
        patternComposer.save();
        return patternNid;
    }

    private static org.eclipse.collections.api.list.ImmutableList<FieldDefinitionRecord> fieldDefinitions(
            long stampNid, long patternNid, ConceptFacade firstPurpose, ConceptFacade secondPurpose) {
        MutableList<FieldDefinitionRecord> definitions = Lists.mutable.empty();
        definitions.add(new FieldDefinitionRecord(KernelConcept.STRING.nid(), firstPurpose.nid(),
                KernelConcept.ENGLISH_LANGUAGE.nid(), stampNid, patternNid, 0));
        definitions.add(new FieldDefinitionRecord(KernelConcept.STRING.nid(), secondPurpose.nid(),
                KernelConcept.PREFERRED.nid(), stampNid, patternNid, 1));
        return definitions.toImmutable();
    }

    /** The pending entry whose comment is the given text. */
    private static VersionChange pending(ObservableChangeChronology chronology, String text) {
        return chronology.changes().stream()
                .filter(change -> change.version().getVersionRecord().uncommitted()
                        && text.equals(commentField(change).current().value()))
                .findFirst().orElseThrow();
    }

    /** The entry's change to the comment pattern's only field. */
    private static FieldChange commentField(VersionChange change) {
        FieldChange field = change.changes().detect(fieldChange ->
                fieldChange.key() instanceof SemanticFieldListItemKey key
                        && key.patternNid() == KernelPattern.COMMENT_PATTERN.nid() && key.index() == 0);
        assertNotNull(field, "the comment field of " + change);
        return field;
    }

    private static FieldChange stampTimeField(VersionChange change) {
        FieldChange field = change.changes().detect(fieldChange -> fieldChange.key() instanceof TimeForStampKey);
        assertNotNull(field, "the stamp time field of " + change);
        return field;
    }

    private static long nid(Object value) {
        return ((EntityFacade) value).nid();
    }

    /** What the chronology announced, as list changes: one word per change, and the items it moved. */
    private static final class Recorder implements ListChangeListener<VersionChange> {
        final List<String> events = new ArrayList<>();
        final List<VersionChange> added = new ArrayList<>();
        final List<VersionChange> removed = new ArrayList<>();

        Recorder(ObservableChangeChronology chronology) {
            chronology.changes().addListener(this);
        }

        @Override
        public void onChanged(Change<? extends VersionChange> change) {
            while (change.next()) {
                if (change.wasReplaced()) {
                    events.add("replace");
                    removed.addAll(change.getRemoved());
                    added.addAll(change.getAddedSubList());
                } else if (change.wasAdded()) {
                    events.add("add");
                    added.addAll(change.getAddedSubList());
                } else if (change.wasRemoved()) {
                    events.add("remove");
                    removed.addAll(change.getRemoved());
                } else {
                    events.add("permute");
                }
            }
        }
    }
}
