package dev.ikm.komet.kview.mvvm.view.concept;



import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext;import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConceptPropertiesMenuController {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptPropertiesMenuController.class);

    public static final String EDIT_MENU_FXML_FILE = "concept-prop-menu.fxml";

    @FXML private Label conceptTitleLabel;
    @FXML private Button editDescriptionsButton;
    @FXML private Button editAxiomsButton;

    @InjectViewModel
    ConceptViewModelNext conceptViewModelNext;

    public ConceptPropertiesMenuController() {

    }

    @FXML
    public void initialize() {

        // TODO:
        SimpleStringProperty fqnTitleTextProp = conceptViewModelNext.getProperty("add fqn text to conceptModel. E.g get it via NID");

        conceptTitleLabel.textProperty().bind(
                Bindings.concat("Edit: ", fqnTitleTextProp)
        );

        // open the name menu pane
        editDescriptionsButton.setOnMouseClicked(mouseEvent -> {
            conceptViewModelNext.setValue(ConceptViewModelNext.ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, ConceptViewModelNext.SelectedPropertyWindowKind.NAME_MENU);

        });

        // TODO: editAxiomsButton when Axiom Controller is implemented
    }

}
