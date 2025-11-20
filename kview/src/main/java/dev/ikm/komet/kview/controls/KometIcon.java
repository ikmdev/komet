package dev.ikm.komet.kview.controls;

import javafx.scene.layout.StackPane;

public class KometIcon extends StackPane {

    public enum IconValue {
        PENCIL("pencil"),
        TRASH("trash"),
        PLUS("plus"),
        POPULATE("populate"),
        KL_VIEW_ONLY("kl-view-only"),           // View only KL Editor Window
        KL_EDITABLE_VIEW("kl-editable-layout");         // Editable KL Editor Window

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

    public static KometIcon create(IconValue iconValue, String... additionalStyleClasses) {
        KometIcon kometIcon = create(iconValue);
        kometIcon.getStyleClass().addAll(additionalStyleClasses);
        return kometIcon;
    }

    public KometIcon() {
        getStyleClass().add("icon");
    }
}