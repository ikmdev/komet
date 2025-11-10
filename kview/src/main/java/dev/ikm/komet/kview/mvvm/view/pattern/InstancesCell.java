package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.*;
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
import java.util.concurrent.atomic.AtomicReference;

import static dev.ikm.tinkar.terms.TinkarTermV2.*;

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

                final AtomicReference<String> entityDescriptionText =
                        new AtomicReference<>(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid));

                EntityHandle.get(nid).ifPresent(entity -> {
                    Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                    iconImageView.setImage(identicon);

                    // generate the subtitle of status and last updated
                    EntityVersion latest = (EntityVersion) stampCalculator.latest(entity).get();
                    LocalDate date = Instant.ofEpochMilli(latest.stamp().time()).atZone(ZoneId.systemDefault()).toLocalDate();
                    subTitle.setText(latest.stamp().state().toString()
                            + ", Last Updated " + DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(date));

                    if (entity instanceof SemanticEntity<?> semanticEntity) {
                        if (semanticEntity.patternNid() == IDENTIFIER_PATTERN.nid()) {
                            //TODO Move better string descriptions to language calculator
                            Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                            ImmutableList fields = latestId.get().fieldValues();
                            entityDescriptionText.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                    ": " + fields.get(1));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.publicId())) {
                            entityDescriptionText.set("Inferred definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(INFERRED_NAVIGATION_PATTERN.publicId())) {
                            entityDescriptionText.set("Inferred is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(VERSION_CONTROL_PATH_PATTERN.publicId())) {
                            entityDescriptionText.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.publicId())) {
                            entityDescriptionText.set("Stated definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(STATED_NAVIGATION_PATTERN.publicId())) {
                            entityDescriptionText.set("Stated is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(GB_DIALECT_PATTERN.publicId())) {
                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                            ImmutableList fields = latestAcceptability.get().fieldValues();
                            entityDescriptionText.set("UK dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(US_DIALECT_PATTERN.publicId())) {
                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                            ImmutableList fields = latestAcceptability.get().fieldValues();
                            entityDescriptionText.set("US dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()));
                        } else if (semanticEntity.patternNid() == Entity.nidForPattern(PATH_ORIGINS.publicId())) {
                            Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                            ImmutableList fields = latestPathOrigins.get().fieldValues();
                            entityDescriptionText.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()) +
                                            " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                            " on " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)));
                        }
                    }
                });

                title.setText(entityDescriptionText.get());
                setText(null);
                setGraphic(mainContainer);
            }
        }
    }
}
