import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.concurrent.TaskListsService;
import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampService;

module dev.ikm.komet.application {

    exports dev.ikm.komet.app to javafx.graphics;
    opens dev.ikm.komet.app to javafx.fxml;

    // Not happy that I have to specify these here... Can't dynamically add modules?
    requires dev.ikm.tinkar.provider.spinedarray;
    requires dev.ikm.tinkar.provider.mvstore;
    requires dev.ikm.tinkar.provider.ephemeral;
    // End not happy...

    requires javafx.controls;
    requires javafx.fxml;
    requires nsmenufx;
    requires org.controlsfx.controls;
    requires dev.ikm.komet.classification;
    requires dev.ikm.komet.details;
    requires dev.ikm.komet.executor;
    requires dev.ikm.komet.framework;
    requires dev.ikm.komet.list;
    requires dev.ikm.komet.navigator;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;
    requires dev.ikm.komet.search;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.provider.entity;
    requires dev.ikm.tinkar.terms;
    requires org.kordamp.ikonli.javafx;

    uses DataServiceController;
    uses DefaultDescriptionForNidService;
    uses EntityService;
    uses KometNodeFactory;
    uses PublicIdService;
    uses StampService;
    uses TaskListsService;

    // For ScenicView...
    //requires org.scenicview.scenicview;
}