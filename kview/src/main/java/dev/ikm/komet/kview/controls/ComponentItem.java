package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.common.id.PublicId;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class ComponentItem {

    public ComponentItem() {
        this(null, null, null, false);
    }

    public ComponentItem(String text, Image icon) {
        this(text, icon, null, false);
    }

    public ComponentItem(String text, Image icon, PublicId publicId, boolean isConcept) {
        this.text.set(text);
        this.icon.set(icon);
        this.publicId = publicId;
        this.isConcept = isConcept;
    }

    public ComponentItem(ComponentItem other) {
        this.text.set(other.text.get());
        this.icon.set(other.icon.get());
        this.publicId = other.publicId;
        this.isConcept = other.isConcept;
    }

    // -- text
    private StringProperty text = new SimpleStringProperty();
    public String getText() { return text.get(); }
    public StringProperty textProperty() { return text; }
    public void setText(String text) { this.text.set(text); }

    // -- icon
    private ObjectProperty<Image> icon = new SimpleObjectProperty<>();
    public Image getIcon() { return icon.get(); }
    public ObjectProperty<Image> iconProperty() { return icon; }
    public void setIcon(Image icon) { this.icon.set(icon); }

    // -- public id
    private PublicId publicId;
    public void setPublicId(PublicId publicId) { this.publicId = publicId; }
    public PublicId getPublicId() { return publicId; }

    // -- is concept
    private boolean isConcept;
    public boolean isConcept() { return isConcept; }
    public void setConcept(boolean concept) { isConcept = concept; }
}