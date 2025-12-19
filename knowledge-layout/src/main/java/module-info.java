import dev.ikm.komet.layout.orchestration.WindowMenuProvider;
import dev.ikm.komet.layout.orchestration.WindowMenuService;

module dev.ikm.komet.layout {

    requires dev.ikm.komet.framework;
    requires dev.ikm.jpms.recordbuilder.core;
    requires java.compiler;
    requires javafx.base;

    exports dev.ikm.komet.layout;
    exports dev.ikm.komet.layout.action;
    exports dev.ikm.komet.layout.area;
    exports dev.ikm.komet.layout.component;
    exports dev.ikm.komet.layout.context;
    exports dev.ikm.komet.layout.event;
    exports dev.ikm.komet.layout.orchestration;
    exports dev.ikm.komet.layout.orchestration.process;
    exports dev.ikm.komet.layout.preferences;
    exports dev.ikm.komet.layout.selection;
    exports dev.ikm.komet.layout.selection.element;
    exports dev.ikm.komet.layout.version.field;
    exports dev.ikm.komet.layout.window;
    exports dev.ikm.komet.layout.editor.model;
    exports dev.ikm.komet.layout.editor;
    exports dev.ikm.komet.layout_engine.blueprint;

    opens dev.ikm.komet.layout to javafx.fxml;
    opens dev.ikm.layout.app to javafx.fxml;
    exports dev.ikm.komet.layout_engine.component.menu;

    // Primary service interface for discovering ALL area factories (built-in and plugins)
    provides dev.ikm.komet.layout.KlArea.Factory
            with dev.ikm.komet.layout_engine.component.area.BooleanFieldArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.StringFieldArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.GenericArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.GenericArea.BlueFactory,
                 dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory32,
                 dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory64,
                 dev.ikm.komet.layout_engine.component.area.SimpleVersionArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.SimpleVersionList.Factory,
                 dev.ikm.komet.layout_engine.component.area.MultiVersionArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.SupplementalTestArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.ChronologyDetailsArea.Factory;

    // Type-specific service interfaces for targeted discovery
    provides dev.ikm.komet.layout.area.KlAreaForBoolean.Factory
            with dev.ikm.komet.layout_engine.component.area.BooleanFieldArea.Factory;

    provides dev.ikm.komet.layout.area.KlAreaForString.Factory
            with dev.ikm.komet.layout_engine.component.area.StringFieldArea.Factory;

    provides dev.ikm.komet.layout.area.KlAreaForObject.Factory
            with dev.ikm.komet.layout_engine.component.area.GenericArea.Factory,
                 dev.ikm.komet.layout_engine.component.area.GenericArea.BlueFactory;

    provides dev.ikm.komet.layout.area.KlAreaForPublicId.Factory
            with dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory,
                    dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory32,
                    dev.ikm.komet.layout_engine.component.area.PublicIdArea.Factory64;

    provides dev.ikm.komet.layout.area.KlAreaForGenericVersion.Factory
            with dev.ikm.komet.layout_engine.component.area.SimpleVersionArea.Factory;

    provides dev.ikm.komet.layout.area.KlAreaForListOfVersions.Factory
            with dev.ikm.komet.layout_engine.component.area.SimpleVersionList.Factory;

    provides dev.ikm.komet.layout.area.KlMultiVersionArea.Factory
            with dev.ikm.komet.layout_engine.component.area.MultiVersionArea.Factory;

    provides dev.ikm.komet.layout.area.KlSupplementalArea.Factory
            with dev.ikm.komet.layout_engine.component.area.SupplementalTestArea.Factory;

    provides dev.ikm.komet.layout.component.KlGenericChronologyArea.Factory
            with dev.ikm.komet.layout_engine.component.area.ChronologyDetailsArea.Factory;

    provides dev.ikm.komet.layout.orchestration.WindowRestoreProvider
        with dev.ikm.komet.layout.orchestration.WindowRestoreMenuProvider;

    provides WindowMenuService
            with WindowMenuProvider;

    provides dev.ikm.komet.layout.orchestration.WindowCreateProvider
            with dev.ikm.komet.layout.orchestration.NewWindowMenuProvider;
}