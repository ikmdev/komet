package dev.ikm.komet.kview.mvvm.view.navigation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

public class PatternNavEntryController {

    @FXML
    private HBox patternEntryHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private Text patternName;

    @FXML
    private Button showContextButton;

    @FXML
    private ContextMenu contextMenu;

    @FXML
    private VBox instancesVBox;
}
