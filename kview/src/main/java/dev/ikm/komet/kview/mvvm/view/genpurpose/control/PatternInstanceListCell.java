/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.view.pattern.PatternInstanceDescriptionHelper;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Consumer;

/**
 * A card in the KL Pattern window's INSTANCES list: the instance's identicon, its title, when it
 * was last updated, and a status chip. The item is the instance's nid.
 * <p>
 * The chip carries the version's {@link State} as a pseudo-class ({@code :active},
 * {@code :inactive}, …) for the stylesheet to colour.
 * <p>
 * Double-clicking the card opens the instance (see the constructor), and the card drags like a
 * next-gen search result or a pattern navigator row: the dragboard carries the instance as a
 * {@link KometClipboard} and the card's user data its {@link DragAndDropInfo}, so every drop
 * target that takes a semantic from those takes one from here.
 */
public class PatternInstanceListCell extends ListCell<Long> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private final ViewCalculator viewCalculator;

    private final HBox card = new HBox();
    private final ImageView identicon = new ImageView();
    private final Label title = new Label();
    private final Label subTitle = new Label();
    private final Label statusChip = new Label();

    private State chipState;

    /** The instance the card shows; null while the cell is empty. */
    private Entity<?> instance;

    /**
     * @param viewCalculator the view the instance is described and stamped through
     * @param onOpen         what opens the instance when its card is double-clicked
     */
    public PatternInstanceListCell(ViewCalculator viewCalculator, Consumer<EntityFacade> onOpen) {
        this.viewCalculator = viewCalculator;

        card.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 && instance != null) {
                onOpen.accept(instance);
            }
        });
        card.setOnDragDetected(mouseEvent -> {
            if (instance == null) {
                return;
            }
            Dragboard dragboard = card.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            // Standard-size drag image with canonical cursor placement (right of the identicon).
            KonceptDragSource.setDragView(dragboard, card);
            dragboard.setContent(new KometClipboard(EntityFacade.make(instance.nid())));
            mouseEvent.consume();
        });

        identicon.setFitWidth(24);
        identicon.setFitHeight(24);

        VBox textContainer = new VBox(title, subTitle);
        textContainer.getStyleClass().add("text-container");
        textContainer.setMinWidth(0);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        // A long title gives way (ellipsis), never the chip: when the row is short of room the HBox
        // shrinks every child towards its minimum, and a label's minimum is its ellipsis alone.
        statusChip.setMinWidth(Region.USE_PREF_SIZE);

        card.getChildren().addAll(identicon, textContainer, statusChip);
        card.setAlignment(Pos.CENTER_LEFT);

        getStyleClass().add("instance-cell");
        card.getStyleClass().add("instance-card");
        title.getStyleClass().add("title");
        subTitle.getStyleClass().add("sub-title");
        statusChip.getStyleClass().add("status-chip");

        // The card takes the list's width rather than asking for its content's: a long title
        // would otherwise widen the list and bring in a horizontal scroll bar.
        setPrefWidth(0);
    }

    @Override
    protected void updateItem(Long nid, boolean empty) {
        super.updateItem(nid, empty);
        if (empty || nid == null) {
            instance = null;
            card.setUserData(null);
            setGraphic(null);
            return;
        }

        instance = null;
        card.setUserData(null);
        title.setText(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(nid));
        EntityHandle.get(nid).ifPresent(entity -> {
            instance = entity;
            card.setUserData(new DragAndDropInfo(DragAndDropType.SEMANTIC, entity.publicId()));
            identicon.setImage(Identicon.generateIdenticonImage(entity.publicId()));
            title.setText(PatternInstanceDescriptionHelper.titleFor(viewCalculator, entity));

            Latest<EntityVersion> latest = viewCalculator.stampCalculator().latest(nid);
            EntityVersion latestVersion = latest.get();
            LocalDate date = Instant.ofEpochMilli(latestVersion.stamp().time()).atZone(ZoneId.systemDefault()).toLocalDate();
            subTitle.setText("Last updated " + DATE_FORMAT.format(date));
            setChipState(latestVersion.stamp().state());
        });
        setGraphic(card);
    }

    private void setChipState(State state) {
        if (chipState != null) {
            statusChip.pseudoClassStateChanged(pseudoClassFor(chipState), false);
        }
        chipState = state;
        statusChip.pseudoClassStateChanged(pseudoClassFor(state), true);
        String name = state.name();
        statusChip.setText(name.charAt(0) + name.substring(1).toLowerCase());
    }

    private static PseudoClass pseudoClassFor(State state) {
        return PseudoClass.getPseudoClass(state.name().toLowerCase());
    }
}
