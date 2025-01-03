package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyStringListControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

public class KLReadOnlyStringListControl extends KLReadOnlyBaseControl {

    public enum StringListDataType {
        COMPONENT_ID_SET,
        COMPONENT_ID_LIST
    }

    public KLReadOnlyStringListControl() {
        getStyleClass().add("read-only-string-list-control");
    }

    // -- texts
    private final ObservableList<String> texts = FXCollections.observableArrayList();
    public ObservableList<String> getTexts() {
        return texts;
    }

    // -- data type
    private ObjectProperty<StringListDataType> dataType = new SimpleObjectProperty<>(StringListDataType.COMPONENT_ID_SET);
    public StringListDataType getDataType() { return dataType.get(); }
    public ObjectProperty<StringListDataType> dataTypeProperty() { return dataType; }
    public void setDataType(StringListDataType stringDataType) { this.dataType.set(stringDataType); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyStringListControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-string-list-control.css").toExternalForm();
    }
}