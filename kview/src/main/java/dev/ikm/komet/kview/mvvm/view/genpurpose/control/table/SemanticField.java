package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.entity.FieldRecord;
import javafx.beans.property.ObjectProperty;

public class SemanticField<T> {
    private int dataType;
    private ObservableField<T> observableField;
    private String fieldTitle;

    public SemanticField(FieldRecord fieldRecord, ObservableField<T> observableField, ObservableView observableView) {
        final FeatureDefinition featureDef = fieldRecord.fieldDefinition(observableView.calculator());

        this.dataType = featureDef.dataTypeNid();
        this.observableField = observableField;
        this.fieldTitle = observableView.getDescriptionTextOrNid(featureDef.meaningNid());
    }

    public int getDataType() { return dataType; }

    public String getFieldTitle() { return fieldTitle; }

    public ObservableField<T> getObservableField() { return observableField; }

    public ObjectProperty<T> observableFieldProperty() { return observableField.editableValueProperty(); }
}