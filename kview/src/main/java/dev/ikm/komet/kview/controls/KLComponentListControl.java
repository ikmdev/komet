package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentListControlSkin;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.impl.IntIdListArray;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * <p>KLComponentListControl is a custom control that acts as a template capable of populating multiple,
 * independent items, with relevant information, as part of a collection. It is made of one or more
 * {@link KLComponentControl KLComponentControls}, where the user can add multiple entities, including
 * duplicates.
 * <p>When two or more distinct entities are present, these can be reordered with drag and drop
 * gestures.</p>
 * <p>If there are no empty KLComponentControls, a {@link javafx.scene.control.Button} allows
 * adding one empty more, so the user can keep adding more items.
 * </p>
 *
 * <pre><code>
 * KLComponentListControl componentListControl = new KLComponentListControl();
 * componentListControl.setTitle("Component List Definition");
 * componentListControl.entitiesProperty().subscribe(entityList -> System.out.println("EntityList = " + entityList));
 * </code></pre>
 *
 * @see KLComponentControl
 * @see KLComponentSetControl
 */
public class KLComponentListControl extends Control {

    /**
     * Creates a KLComponentListControl
     */
    public KLComponentListControl() {
        getStyleClass().add("component-list-control");
    }

    /**
     * A string property that sets the title of the control, if any
     */
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
       return titleProperty;
    }
    public final String getTitle() {
       return titleProperty.get();
    }
    public final void setTitle(String value) {
        titleProperty.set(value);
    }

    /**
     * This property holds an {@link IntIdList} that have been added to the control
     */
    private final ObjectProperty<IntIdList> valueProperty = new SimpleObjectProperty<>(IntIds.list.empty());
    public final ObjectProperty<IntIdList> valueProperty() {
        return valueProperty;
    }
    public final IntIdList getValue() {
        return valueProperty.get();
    }
    public void setValue(IntIdList intIdList) {
        valueProperty.set(intIdList);
    }

    public void addValue(int nid) {
        IntIdList intIdList = getValue().with(nid);
        setValue(intIdList);
    }
    public void addValue(int index, int nid) {
        IntIdList intIdList = getValue().with(nid);
        setValue(intIdList);
    }
    public final int removeIndexItem(int index) {
        int[] nids = valueProperty.get().toArray();
        int[] nids2 = new int[nids.length-1];
        int j = -1;
        for (int i = 0; i < nids.length; i++) {
            if (nids[i] == index) {
                continue;
            }
            j++;
            nids2[j] = nids[i];
        }
        IntIdList newIntIdList = new IntIdListArray(nids2);
        valueProperty.set(newIntIdList);
        return nids[index];
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentListControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLComponentListControl.class.getResource("component-list-control.css").toExternalForm();
    }
}
