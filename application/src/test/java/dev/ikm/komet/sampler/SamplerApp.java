package dev.ikm.komet.sampler;

import com.pixelduke.control.NavigationPane;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SamplerApp extends Application {
    private static final String SAMPLER_APP_BASE_STYLESHEET = SamplerApp.class.getResource("sampler-app-base.css").toExternalForm();

    private static final String HOME = "Sampler_Home.fxml";

    private static final String SORTED_COMBO_BOX_SAMPLER = "Sampler_SortedComboBox.fxml";
    private static final String CONCEPT_NAVIGATOR_SAMPLER = "Sampler_KLConceptNavigatorControl.fxml";

    private static final String BOOLEAN_SAMPLER = "Sampler_Boolean.fxml";
    private static final String STRING_SAMPLER = "Sampler_KLStringControl.fxml";
    private static final String INTEGER_SAMPLER = "Sampler_KLIntegerControl.fxml";
    private static final String FLOAT_SAMPLER = "Sampler_KLFloatControl.fxml";
    private static final String IMAGE_SAMPLER = "Sampler_KLImageControl.fxml";

    private static final String READ_ONLY_STRING_SAMPLER = "Sampler_KLReadOnlyString.fxml";
    private static final String READ_ONLY_COMPONENT_SAMPLER = "Sampler_KLReadOnlyComponent.fxml";
    private static final String READ_ONLY_STRING_LIST_SAMPLER = "Sampler_KLReadOnlyStringList.fxml";
    private static final String READ_ONLY_IMAGE_SAMPLER = "Sampler_KLReadOnlyImage.fxml";

    public static SamplerApp INSTANCE;

    public static void main(String[] args) {
        launch(args);
    }

    private NavigationPane navigationPane;

    @Override
    public void start(Stage stage) {
        System.setProperty("prism.lcdtext", "false"); // nicer fonts (not necessary for this sample)
        INSTANCE = this;

        navigationPane = new NavigationPane();

        addItemsToNavigationPane();

        stage.setTitle("Komet Sampler");

        StackPane root = new StackPane(navigationPane);
        Scene scene = new Scene(root, 1250, 630);

        scene.getStylesheets().add(SAMPLER_APP_BASE_STYLESHEET);
        scene.getStylesheets().add(AbstractBasicController.class.getResource("kview.css").toExternalForm());

        // Show stage
        stage.setScene(scene);
        stage.show();

    }

    private void addItemsToNavigationPane() {
        // menu items
        navigationPane.getMenuItems().add(createMenuItemOrMenu("Home", "home-20.png", HOME, false));

        // Read-Only Data Controls
        Menu readOnlyControls = createMenu("Read-Only Data Controls", "eye-20.png");
        readOnlyControls.getItems().add(createMenuItemWithContent("String Control", READ_ONLY_STRING_SAMPLER));
        readOnlyControls.getItems().add(createMenuItemWithContent("String List Control", READ_ONLY_STRING_LIST_SAMPLER));
        readOnlyControls.getItems().add(createMenuItemWithContent("Component Control", READ_ONLY_COMPONENT_SAMPLER));
        readOnlyControls.getItems().add(createMenuItemWithContent("Image Control", READ_ONLY_IMAGE_SAMPLER));

        // Editable Data Controls
        Menu editableControlsMenu = createMenu("Editable Data Controls", "edit-row-20.png");
        editableControlsMenu.getItems().add(createMenuItemWithContent("Boolean Control", BOOLEAN_SAMPLER));
        editableControlsMenu.getItems().add(createMenuItemWithContent("String Control", STRING_SAMPLER));
        editableControlsMenu.getItems().add(createMenuItemWithContent("Integer Control", INTEGER_SAMPLER));
        editableControlsMenu.getItems().add(createMenuItemWithContent("Float Control", FLOAT_SAMPLER));
        editableControlsMenu.getItems().add(createMenuItemWithContent("Image Control", IMAGE_SAMPLER));

        // Other Controls
        Menu otherControlsMenu = createMenu("Other Controls", "plus-math-20.png");
        otherControlsMenu.getItems().add(createMenuItemWithContent("Sorted ComboBox", SORTED_COMBO_BOX_SAMPLER));
        otherControlsMenu.getItems().add(createMenuItemWithContent("Concept Navigator", CONCEPT_NAVIGATOR_SAMPLER));

        navigationPane.getMenuItems().addAll(
                readOnlyControls,
                editableControlsMenu,
                otherControlsMenu
        );

        // footer menu items

        // settings
        navigationPane.setSettingsVisible(true);
    }

    private Menu createMenu(String labelText, String imageFilename) {
        return (Menu) createMenuItemOrMenu(labelText, imageFilename, null, true);
    }

    private MenuItem createMenuItemWithoutContent(String labelText, String imageFileName) {
        return createMenuItemOrMenu(labelText, imageFileName, null,false);
    }

    private MenuItem createMenuItemWithContent(String labelText, String contentFileName) {
        return createMenuItemOrMenu(labelText, null, contentFileName,false);
    }

    private MenuItem createMenuItemOrMenu(String labelText, String imageFilename, String contentFileName, boolean isMenu) {
        ImageView imageView;
        MenuItem menuItem;

        if (!isMenu) {
            menuItem = new MenuItem();
        } else {
            menuItem = new Menu();
        }

        if (imageFilename != null) {
            imageView = new ImageView(SamplerApp.class.getResource(imageFilename).toExternalForm());
            menuItem.setGraphic(imageView);
        }

        menuItem.setText(labelText);

        // Set content when item selected
        if (contentFileName != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(SamplerApp.class.getResource(contentFileName));

                final Parent root = fxmlLoader.load();

                menuItem.setOnAction(event -> {
                    navigationPane.setContent(root);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return menuItem;
    }

}