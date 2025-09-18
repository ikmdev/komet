package dev.ikm.komet.kview.mvvm.view.concept;



import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptPropertiesNameMenuController {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptPropertiesNameMenuController.class);

    public static final String EDIT_MENU_FXML_FILE = "concept-prop-name-menu.fxml";

    @FXML private Button editFullyQualifiedNameButton;
    @FXML private Button closePropertiesPanelButton;
    @FXML private Button addOtherNameButton;

    @InjectViewModel
    ConceptViewModelNext conceptViewModelNext;

    public ConceptPropertiesNameMenuController() {

    }

    @FXML
    public void initialize() {

        editFullyQualifiedNameButton.setOnMouseClicked( mouseEvent -> {
            conceptViewModelNext.setValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NAME_FORM);

        });

        addOtherNameButton.setOnMouseClicked( mouseEvent -> {
            conceptViewModelNext.setValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NAME_FORM);

        });

        // TODO: make sure slideIn is triggerd correctly?
        closePropertiesPanelButton.setOnMouseClicked(mouseEvent -> {
            conceptViewModelNext.setValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NONE);
        });

    }
}
