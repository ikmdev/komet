package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.text.NumberFormat;

import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.PATTERN;

public class InstancesController {

    private static final int MAX_INSTANCES = 150;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    @FXML
    private ListView instancesListView;

    @FXML
    private Label patternMetaTitle;

    @FXML
    private void initialize() {
        if(patternViewModel.getPropertyValue(PATTERN) != null){
            loadInstances();
        }
        patternMetaTitle.textProperty().bind(patternViewModel.getProperty(PATTERN).map(pattern -> {
            String[] patternNameParts = ((EntityFacade)pattern).description().split(" Pattern");
            if (patternNameParts.length > 0) {
                return patternNameParts[0] + " Metadata for...";
            } else {
                return "Metadata for...";
            }
        }));
    }

    private void loadInstances() {
        // load the pattern instances into an observable list
        ObservableList<Object> patternChildren = FXCollections.observableArrayList();
        EntityFacade patternItem = patternViewModel.getPropertyValue(PATTERN);
        setMetaTitle(patternItem.description());
        int patternNid = patternItem.nid();
        // populate the collection of instance for each pattern: read only the semantics shown,
        // and count the rest without reading them (IKE-Network/ike-issues#1249)
        int childCount = EntityService.get().countSemanticsOfPattern(patternNid);
        EntityService.get().semanticsOfPattern(patternNid)
                .limit(MAX_INSTANCES - 1)
                .forEach(semantic -> patternChildren.add(semantic.nid()));
        if (childCount >= MAX_INSTANCES) {
            NumberFormat numberFormat = NumberFormat.getInstance();
            patternChildren.add(numberFormat.format(childCount - MAX_INSTANCES) + " additional semantics suppressed...");
        }
        boolean hasChildren = patternChildren.size() > 0;

        // set the cell factory for each pattern's instances list
        instancesListView.setCellFactory(p -> new InstancesCell<>(patternViewModel));

        if (hasChildren) {
            instancesListView.setItems(patternChildren);
        }
    }

    private void setMetaTitle(String description) {
    }

}