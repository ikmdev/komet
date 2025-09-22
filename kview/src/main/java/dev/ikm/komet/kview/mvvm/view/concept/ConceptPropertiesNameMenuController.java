package dev.ikm.komet.kview.mvvm.view.concept;



import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext.*;
import javafx.collections.ObservableList;
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

        ObservableList<Object> fqnSemanticProps = conceptViewModelNext.getObservableList(ConceptPropertyKeys.ASOCIATED_FQN_DESCRIPTION_SEMANTICS);
        fqnSemanticProps.subscribe(() -> {
           if(fqnSemanticProps.isEmpty())  {
               editFullyQualifiedNameButton.setText("ADD FULLY QUALIFIED");
           } else {
               editFullyQualifiedNameButton.setText("EDIT FULLY QUALIFIED");
           }
        });

        if(fqnSemanticProps.isEmpty())  {
            editFullyQualifiedNameButton.setText("ADD FULLY QUALIFIED");
        } else {
            editFullyQualifiedNameButton.setText("EDIT FULLY QUALIFIED");
        }


        editFullyQualifiedNameButton.setOnMouseClicked( mouseEvent -> {
            conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NAME_FORM);

        });

        addOtherNameButton.setOnMouseClicked( mouseEvent -> {
            conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NAME_FORM);

        });

        closePropertiesPanelButton.setOnMouseClicked(mouseEvent -> {
            conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NONE);
        });

    }
}
