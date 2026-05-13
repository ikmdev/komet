package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.cell;

import dev.ikm.komet.kview.mvvm.view.genpurpose.control.table.SemanticRow;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SemanticIdenticonCell extends TableCell<SemanticRow, Image> {
    private ImageView identiconImageView;

    public SemanticIdenticonCell() {
        identiconImageView = new ImageView();

        setGraphic(identiconImageView);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        identiconImageView.getStyleClass().add("semantic-identicon");
        getStyleClass().add("semantic-identicon-cell");
    }

    @Override
    protected void updateItem(Image identiconImage, boolean empty) {
        super.updateItem(identiconImage, empty);

        if (empty || identiconImage == null) {
            setGraphic(null);
            return;
        }

        identiconImageView.setImage(identiconImage);

        setGraphic(identiconImageView);
    }
}