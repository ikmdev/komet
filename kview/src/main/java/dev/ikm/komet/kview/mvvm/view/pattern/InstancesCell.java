package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
            } else if (item instanceof Long nid) {
                StampCalculator stampCalculator = viewProperties.calculator().stampCalculator();

                title.setText(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid));

                EntityHandle.get(nid).ifPresent(entity -> {
                    Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                    iconImageView.setImage(identicon);

                    // generate the subtitle of status and last updated
                    EntityVersion latest = (EntityVersion) stampCalculator.latest(entity).get();
                    LocalDate date = Instant.ofEpochMilli(latest.stamp().time()).atZone(ZoneId.systemDefault()).toLocalDate();
                    subTitle.setText(latest.stamp().state().toString()
                            + ", Last Updated " + DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(date));

                    title.setText(PatternInstanceDescriptionHelper.titleFor(viewProperties.calculator(), entity));
                });

                setText(null);
                setGraphic(mainContainer);
            }
        }
    }
}
