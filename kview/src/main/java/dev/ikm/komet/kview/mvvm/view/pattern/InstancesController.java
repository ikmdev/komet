package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

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
        loadInstances();
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
        Entity patternItem = patternViewModel.getPropertyValue(PATTERN);
        setMetaTitle(patternItem.description());
        int patternNid = patternItem.nid();
        AtomicInteger childCount = new AtomicInteger();

        // populate the collection of instance for each pattern
        PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
            if (childCount.incrementAndGet() < MAX_INSTANCES) {
                patternChildren.add(semanticNid);
            }
        });
        if (childCount.get() >= MAX_INSTANCES) {
            NumberFormat numberFormat = NumberFormat.getInstance();
            patternChildren.add(numberFormat.format(childCount.get() - MAX_INSTANCES) + " additional semantics suppressed...");
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