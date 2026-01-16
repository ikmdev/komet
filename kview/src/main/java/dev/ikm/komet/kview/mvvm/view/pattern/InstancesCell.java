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
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class InstancesCell<T> extends ListCell<T> {

    private final HBox mainContainer;
    private final VBox textContainer;
    private final Label icon;
    private final ImageView iconImageView;
    private final Label title;
    private final Label subTitle;
    private final PatternViewModel patternViewModel;

    public InstancesCell(PatternViewModel patternViewModel) {
        this.patternViewModel = patternViewModel;

        mainContainer = new HBox();
        textContainer = new VBox();
        title = new Label();
        subTitle = new Label();
        icon = new Label();
        iconImageView = new ImageView();

        iconImageView.setFitWidth(24);
        iconImageView.setFitHeight(24);

        icon.setGraphic(iconImageView);

        textContainer.getChildren().addAll(title, subTitle);
        mainContainer.getChildren().addAll(icon, textContainer);

        listViewProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                // Subtracting 40 to leave space for a possible scroll bar (a scrollbar will show when there are many items)
                prefWidthProperty().bind(getListView().widthProperty().subtract(40));
                setMaxWidth(Control.USE_PREF_SIZE);
                listViewProperty().removeListener(this);
            }
        });

        // CSS styleclasses
        getStyleClass().add("instance-cell");
        mainContainer.getStyleClass().add("pattern-instance");
        textContainer.getStyleClass().add("text-container");
        title.getStyleClass().add("title");
        subTitle.getStyleClass().add("sub-title");
        icon.getStyleClass().add("icon");
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        ViewProperties viewProperties = patternViewModel.getViewProperties();
        if (item != null && !empty) {
            if (item instanceof String stringItem) {
                setGraphic(null);
                setText(stringItem);
            } else if (item instanceof Integer nid) {
                StampCalculator stampCalculator = viewProperties.calculator().stampCalculator();

                String entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid);
                Entity entity = Entity.getFast(nid);

                // generate the subtitle of status and last updated
                EntityVersion latest = (EntityVersion) stampCalculator.latest(entity).get();
                LocalDate date = Instant.ofEpochMilli(latest.stamp().time()).atZone(ZoneId.systemDefault()).toLocalDate();
                String instanceSubTitle = latest.stamp().state().toString()
                        + ", Last Updated " + DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(date);
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

                title.setText(entityDescriptionText);

                if (!entityDescriptionText.isEmpty()) {
                    Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                    iconImageView.setImage(identicon);
                }

                subTitle.setText(instanceSubTitle);

                setText(null);
                setGraphic(mainContainer);
            }
        }
    }
}
