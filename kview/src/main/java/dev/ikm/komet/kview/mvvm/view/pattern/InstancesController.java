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

    public ListView getPatternInstancesListView() {
        return this.instancesListView;
    }

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

        // populate the pattern instances as a list view
        ListView<Object> patternInstances = getPatternInstancesListView();

        // set the cell factory for each pattern's instances list
        patternInstances.setCellFactory(p -> new ListCell<>() {
            private final Label label;

            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                label = new Label();
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                ViewProperties viewProperties = patternViewModel.getViewProperties();
                if (item != null && !empty) {
                    if (item instanceof String stringItem) {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setText(stringItem);
                    } else if (item instanceof Integer nid) {
                        String entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid);
                        Entity entity = Entity.getFast(nid);
                        if (entity instanceof SemanticEntity<?> semanticEntity) {
                            if (semanticEntity.patternNid() == IDENTIFIER_PATTERN_PROXY.nid()) {
                                //TODO Move better string descriptions to language calculator
                                Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestId.get().fieldValues();
                                entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                        ": " + fields.get(1);
                            } else if (semanticEntity.patternNid() == INFERRED_DEFINITION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Inferred definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == INFERRED_NAVIGATION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Inferred is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == PATH_MEMBERSHIP_PROXY.nid()) {
                                entityDescriptionText =
                                        viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == STATED_DEFINITION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Stated definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == STATED_NAVIGATION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Stated is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == UK_DIALECT_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestAcceptability.get().fieldValues();
                                entityDescriptionText =
                                        "UK dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == US_DIALECT_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestAcceptability.get().fieldValues();
                                entityDescriptionText =
                                        "US dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestPathOrigins.get().fieldValues();
                                entityDescriptionText =
                                        viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()) +
                                                " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                                " on " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0));
                            }
                        }

                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        label.setText(entityDescriptionText);
                        if (!entityDescriptionText.isEmpty()) {
                            Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                            ImageView imageView = new ImageView(identicon);
                            imageView.setFitWidth(24);
                            imageView.setFitHeight(24);
                            label.setGraphic(imageView);
                        }
                        label.getStyleClass().add("pattern-instance");
                        setGraphic(label);
                    }
                }
            }
        });
        if (hasChildren) {
            Platform.runLater(() -> patternInstances.setItems(patternChildren));
        }
    }

    private void setMetaTitle(String description) {

    }

}