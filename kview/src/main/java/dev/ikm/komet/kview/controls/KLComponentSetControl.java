package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentSetControlSkin;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.LinkedList;
import java.util.List;

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
    private final ListProperty<EntityProxy> entitiesProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    public final ListProperty<EntityProxy> entitiesProperty() {
       return entitiesProperty;
    }
    public final List<EntityProxy> getEntitiesList() {
       return entitiesProperty.get();
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
