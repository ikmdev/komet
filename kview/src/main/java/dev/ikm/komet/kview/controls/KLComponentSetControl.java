package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentSetControlSkin;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.impl.IntIdSetArray;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.stream.IntStream;

/**
 * <p>KLComponentSetControl is a custom control that acts as a template capable of populating multiple,
 * independent items, with relevant information, as part of a collection. It is made of one or more
 * {@link KLComponentControl KLComponentControls}, where the user can add multiple entities but without
 * duplicates.
 * <p>When two or more distinct entities are present, these can be reordered with drag and drop
 * gestures.</p>
 * <p>If there are no empty KLComponentControls, a {@link javafx.scene.control.Button} allows
 * adding one empty more, so the user can keep adding more items.
 * </p>
 *
 * <pre><code>
 * KLComponentSetControl componentSetControl = new KLComponentSetControl();
 * componentSetControl.setTitle("Component Set Definition");
 * componentSetControl.entitiesProperty().subscribe(entityList -> System.out.println("EntityList = " + entityList));
 * </code></pre>
 *
 * @see KLComponentControl
 * @see KLComponentListControl
 */
public class KLComponentSetControl extends Control {

    /**
     * Creates a KLComponentSetControl
     */
    public KLComponentSetControl() {
        getStyleClass().add("component-set-control");
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
     * This property holds the list of {@link Entity Entities} that have been added to the control
     */
    private final ObjectProperty<IntIdSet> valueProperty =  new SimpleObjectProperty<>(this, null);
    public final ObjectProperty<IntIdSet> valueProperty() {
        return valueProperty;
    }
    public final IntIdSet getValue() {
        return valueProperty.get();
    }

    public final void setValue(IntIdSet intIdSet) {
        valueProperty.set(intIdSet);
    }

    public void addValue(int nid) {
        IntIdSet intIdSet = getValue().with(nid);
        setValue(intIdSet);
    }

    public final void removeValue(int nid) {
        IntStream intStream = valueProperty.get().with(nid).intStream().filter((p) -> p != nid);
        IntIdSet newIntIdSet = IntIdSetArray.newIntIdSet(intStream.toArray());
        valueProperty.set(newIntIdSet);
    }


    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentSetControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLComponentSetControl.class.getResource("component-set-control.css").toExternalForm();
    }
}
