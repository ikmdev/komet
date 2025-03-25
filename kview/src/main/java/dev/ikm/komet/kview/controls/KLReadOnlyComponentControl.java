package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentControlSkin;
import javafx.scene.control.Skin;

public class KLReadOnlyComponentControl extends KLReadOnlyBaseControl<ComponentItem> {

    public KLReadOnlyComponentControl() {
        getStyleClass().add("read-only-component-control");
    }

//    // -- text
//    private StringProperty text = new SimpleStringProperty();
//    public String getText() { return text.get(); }
//    public StringProperty textProperty() { return text; }
//    public void setText(String text) { this.text.set(text); }
//
//    // -- icon
//    private ObjectProperty<Image> icon = new SimpleObjectProperty<>();
//    public Image getIcon() { return icon.get(); }
//    public ObjectProperty<Image> iconProperty() { return icon; }
//    public void setIcon(Image icon) { this.icon.set(icon); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-control.css").toExternalForm();
    }
}