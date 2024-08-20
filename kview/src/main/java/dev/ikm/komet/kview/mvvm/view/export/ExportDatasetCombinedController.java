package dev.ikm.komet.kview.mvvm.view.export;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.MASTER_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.PRIMORDIAL_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.SANDBOX_PATH;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.ExportViewModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportDatasetCombinedController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportDatasetCombinedController.class);

    @InjectViewModel
    private ExportViewModel exportViewModel;

    @FXML
    private ComboBox<String> exportOptions;

    @FXML
    private ComboBox<EntityFacade> pathOptions;

    @FXML
    public void initialize() {

        exportOptions.getItems().addAll("Change set", "FHIR");

        // path options are disabled.  in the future, when they are enabled, we can
        // add them
        setUpPathOptions();

    }

    private void setUpPathOptions() {
        pathOptions.setConverter((new StringConverter<EntityFacade>() {
            @Override
            public String toString(EntityFacade conceptEntity) {
                ViewCalculator viewCalculator = getViewProperties().calculator();
                return (conceptEntity != null) ? viewCalculator.getRegularDescriptionText(conceptEntity).get() : "";
            }

            @Override
            public EntityFacade fromString(String s) {
                return null;
            }
        }));
        pathOptions.getItems().addAll(exportViewModel.getPaths());
    }

    private ViewProperties getViewProperties() {
        return exportViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
}
