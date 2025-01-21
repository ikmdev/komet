module dev.ikm.komet.layout {

    requires dev.ikm.komet.framework;

    exports dev.ikm.komet.layout;
    exports dev.ikm.komet.layout.action;
    exports dev.ikm.komet.layout.component;
    exports dev.ikm.komet.layout.component.multi;
    exports dev.ikm.komet.layout.component.version.field;
    exports dev.ikm.komet.layout.component.version;
    exports dev.ikm.komet.layout.container;
    exports dev.ikm.komet.layout.context;
    exports dev.ikm.komet.layout.event;
    exports dev.ikm.komet.layout.orchestration;
    exports dev.ikm.komet.layout.orchestration.process;
    exports dev.ikm.komet.layout.preferences;
    exports dev.ikm.komet.layout.selection;
    exports dev.ikm.komet.layout.selection.element;
    exports dev.ikm.komet.layout.window;

    opens dev.ikm.komet.layout to javafx.fxml;
    opens dev.ikm.layout.app to javafx.fxml;

}