package dev.ikm.komet.kview.controls;

import javafx.scene.layout.StackPane;

public class KometIcon extends StackPane {

    public enum IconValue {
        PENCIL("pencil"),
        TRASH("trash"),
        PLUS("plus");

        private String styleClass;

        IconValue(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    public static KometIcon create(IconValue iconValue) {
        KometIcon kometIcon = new KometIcon();
        kometIcon.getStyleClass().add(iconValue.getStyleClass());
        return kometIcon;
    }

    public KometIcon() {
        getStyleClass().add("icon");
    }
}