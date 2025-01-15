package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Helper class to have common methods to avoid code redundancy.
 */

public class GenEditingHelper {

    /**
     *
     * @param viewProperties viewProperties cannot be null. Required to get the calculator.
     * @param semanticEntityVersionLatest
     * @param fieldRecord
     * @return the observable field
     * @param <T>
     */

    public static <T> ObservableField<T> getObservableFields(ViewProperties viewProperties, Latest<SemanticEntityVersion> semanticEntityVersionLatest, FieldRecord<Object> fieldRecord){
        ObservableSemantic observableSemantic = ObservableEntity.get(semanticEntityVersionLatest.get().nid());
        ObservableSemanticSnapshot observableSemanticSnapshot = observableSemantic.getSnapshot(viewProperties.calculator());
        ImmutableList<ObservableField> observableFields = observableSemanticSnapshot.getLatestFields().get();
        return observableFields.get(fieldRecord.fieldIndex());
    }
}
