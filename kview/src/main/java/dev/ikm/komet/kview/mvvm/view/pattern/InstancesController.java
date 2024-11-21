package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.IDENTIFIER_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.PATH_MEMBERSHIP_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.UK_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.US_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.eclipse.collections.api.list.ImmutableList;

import java.text.NumberFormat;
import java.time.Instant;
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