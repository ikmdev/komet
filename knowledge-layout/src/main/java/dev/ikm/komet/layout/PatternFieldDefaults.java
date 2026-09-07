package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * A pattern's field defaults: the values a new semantic of that pattern starts out with, in every
 * KL window that creates one.
 *
 * <p>The defaults are stored in the data store as a <em>defaults semantic</em> — a semantic of the
 * pattern itself, referencing the pattern, whose fields hold the defaults. A field left
 * {@linkplain #isBlank blank} in the defaults semantic has no default. The defaults semantic's identity is derived
 * from the pattern's ({@link #defaultsSemanticId}), so there is at most one per pattern, and its versions
 * carry the {@link KlTerms#FIELD_DEFAULTS_MODULE} module so views can keep it apart from
 * terminology content. The defaults semantic must not be displayed as content ({@link #isDefaultsSemantic} tells it
 * from the pattern's real semantics).
 */
public final class PatternFieldDefaults {

    private static final String DEFAULTS_SEMANTIC_DISCRIMINATOR = "field-defaults";

    private PatternFieldDefaults() {
    }

    /**
     * The identity of the defaults semantic holding the defaults of the pattern with the passed in
     * identity — T5-derived from the pattern's identity, so the same pattern always maps to the same
     * defaults semantic.
     */
    public static PublicId defaultsSemanticId(PublicId patternId) {
        UUID namespace = patternId.asUuidArray()[0];
        return PublicIds.of(UuidT5Generator.get(namespace, DEFAULTS_SEMANTIC_DISCRIMINATOR));
    }

    /**
     * Whether the semantic is a pattern's defaults semantic rather than one of its real
     * semantics: it carries the {@linkplain #defaultsSemanticId derived identity} and every one of
     * its versions is stamped with the {@link KlTerms#FIELD_DEFAULTS_MODULE defaults module}.
     * The identity is how the code reaches the defaults; the module is what marks them as defaults
     * in the data — a semantic with the identity but written to a content module is not trusted.
     */
    public static boolean isDefaultsSemantic(SemanticEntity<?> semantic) {
        return PublicId.equals(semantic.publicId(), defaultsSemanticId(PrimitiveData.publicId(semantic.patternNid())))
                && isInDefaultsModule(semantic);
    }

    private static boolean isInDefaultsModule(SemanticEntity<?> semantic) {
        int defaultsModuleNid = KlTerms.FIELD_DEFAULTS_MODULE.nid();
        return !semantic.versions().isEmpty()
                && semantic.versions().stream().allMatch(version -> version.stamp().moduleNid() == defaultsModuleNid);
    }

    /**
     * The pattern's defaults semantic, when one has been written (see {@link #isDefaultsSemantic}).
     */
    @SuppressWarnings("unchecked")
    public static Optional<SemanticEntity<SemanticEntityVersion>> defaultsSemantic(int patternNid) {
        PublicId defaultsSemanticId = defaultsSemanticId(PrimitiveData.publicId(patternNid));
        // hasPublicId first: EntityHandle.get would mint a nid for a never-seen public id.
        if (!PrimitiveData.get().hasPublicId(defaultsSemanticId)) {
            return Optional.empty();
        }
        return EntityHandle.get(defaultsSemanticId).asSemantic()
                .filter(PatternFieldDefaults::isDefaultsSemantic)
                .map(semantic -> (SemanticEntity<SemanticEntityVersion>) semantic);
    }

    /**
     * The defaults semantic's latest <em>published</em> version for the view, when the pattern has a
     * defaults semantic the view holds one of. Defaults take effect on Publish: an unpublished
     * version — one being edited in a Pattern window, or a blank one left behind by a window that
     * opened its DEFAULTS tab and closed without publishing — is never the source of defaults,
     * even though the view's own "latest" would rank it first.
     */
    public static Optional<ObservableSemanticVersion> defaultsSemanticVersion(int patternNid, ViewCalculator viewCalculator) {
        return defaultsSemantic(patternNid).flatMap(defaultsSemantic -> {
            ObservableSemanticSnapshot snapshot = ObservableEntityHandle.get(defaultsSemantic.nid())
                    .expectSemantic()
                    .getSnapshot(viewCalculator);
            Latest<ObservableSemanticVersion> latest = snapshot.getLatestVersion();
            if (latest.isPresent() && !latest.get().uncommitted()) {
                return Optional.of(latest.get());
            }
            // The view's latest is unpublished: fall back to the newest published version.
            return snapshot.getHistoricVersions().stream()
                    .filter(version -> !version.uncommitted())
                    .max(Comparator.comparingLong(version -> version.stamp().time()));
        });
    }

    /**
     * The pattern's defaults, one value per field in pattern order — a {@linkplain #isBlank blank}
     * value where the field has no default. Empty when the pattern has no defaults semantic.
     */
    public static ImmutableList<Object> defaultValues(int patternNid, ViewCalculator viewCalculator) {
        return defaultsSemanticVersion(patternNid, viewCalculator)
                .map(ObservableSemanticVersion::fieldValues)
                .orElse(Lists.immutable.empty());
    }

    /**
     * Fills the blank fields of a new semantic from the pattern's defaults: every field that is
     * still blank and has a non-blank default takes the default. Fields already holding a value
     * (e.g. seeded from a section's display filter) are left alone.
     *
     * @param editableFields the new semantic's editable fields, in pattern order
     * @param defaults       the pattern's defaults ({@link #defaultValues}), in pattern order
     */
    public static void applyDefaults(List<? extends ObservableField.Editable<?>> editableFields, List<Object> defaults) {
        List<Object> currentValues = new ArrayList<>(editableFields.size());
        for (ObservableField.Editable<?> editableField : editableFields) {
            currentValues.add(editableField.getValue());
        }
        for (int fieldIndex : defaultedFieldIndices(currentValues, defaults)) {
            editableFields.get(fieldIndex).setObjectValue(defaults.get(fieldIndex));
        }
    }

    /**
     * The indices of the fields a default applies to: those whose current value is
     * {@linkplain #isBlank blank} while the default at the same index is not. Indices past the
     * shorter of the two lists never apply, and neither does a logical definition: definitions
     * are authored per concept, never defaulted.
     */
    public static List<Integer> defaultedFieldIndices(List<Object> currentValues, List<Object> defaults) {
        List<Integer> indices = new ArrayList<>();
        int fieldCount = Math.min(currentValues.size(), defaults.size());
        for (int i = 0; i < fieldCount; i++) {
            Object defaultValue = defaults.get(i);
            if (isBlank(currentValues.get(i)) && !isBlank(defaultValue) && !(defaultValue instanceof DiTreeEntity)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Whether a field value is blank — the value a new semantic's field starts with, which in a
     * defaults semantic means "no default". What counts as blank is defined where those starting
     * values are generated: {@link ObservableComposer#isDefaultFieldValue}.
     */
    public static boolean isBlank(Object value) {
        return ObservableComposer.isDefaultFieldValue(value);
    }
}
