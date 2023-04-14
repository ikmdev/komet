package dev.ikm.komet.framework;

//~--- non-JDK imports --------------------------------------------------------
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Window;
//~--- JDK imports ------------------------------------------------------------
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

//~--- non-JDK imports --------------------------------------------------------
import static dev.ikm.komet.framework.StyleClasses.HEADER_PANEL;
import org.controlsfx.dialog.ProgressDialog;

//~--- classes ----------------------------------------------------------------
/**
 * Helper class encapsulating various Java FX helper utilities.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FxUtils {
    // TODO should this be replaced with fx css?

    public static final DropShadow RED_DROP_SHADOW = new DropShadow();
    public static final DropShadow GREEN_DROP_SHADOW = new DropShadow();
    public static final DropShadow LIGHT_GREEN_DROP_SHADOW = new DropShadow();

    //~--- static initializers -------------------------------------------------
    static {
        RED_DROP_SHADOW.setColor(Color.RED);
        GREEN_DROP_SHADOW.setColor(Color.GREEN);
        LIGHT_GREEN_DROP_SHADOW.setColor(Color.LIGHTGREEN);
    }

    //~--- constructors --------------------------------------------------------
    // Do not instantiate.
    private FxUtils() {
    }

    ;

    //~--- methods -------------------------------------------------------------

    /**
     * Makes sure thread is NOT the FX application thread.
     */
    public static void checkBackgroundThread() {
        // Throw exception if on FX user thread
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Not on background thread; currentThread = " + Thread.currentThread().getName());
        }
    }

    public static void expandAll(TreeItem<?> ti) {
        ti.setExpanded(true);
        ti.getChildren()
                .forEach(
                        (tiChild) -> {
                            expandAll(tiChild);
                        });
    }

    public static void expandParents(TreeItem<?> ti) {
        TreeItem<?> parent = ti.getParent();

        if (parent != null) {
            ti.getParent()
                    .setExpanded(true);
            expandParents(parent);
        }
    }


    public static AnchorPane setupHeaderPanel(String... strings) {
        AnchorPane anchorPane = new AnchorPane();

        anchorPane.getStyleClass()
                .setAll(HEADER_PANEL.toString());

        Text leftText = new Text(strings[0]);

        AnchorPane.setLeftAnchor(leftText, 3.0);
        AnchorPane.setTopAnchor(leftText, 3.0);
        AnchorPane.setBottomAnchor(leftText, 3.0);
        anchorPane.getChildren()
                .add(leftText);

        if (strings.length > 1) {
            Text rightText = new Text(strings[1]);
            rightText.getStyleClass().setAll(StyleClasses.HEADER_TEXT.toString());

            AnchorPane.setRightAnchor(rightText, 3.0);
            AnchorPane.setTopAnchor(rightText, 3.0);
            AnchorPane.setBottomAnchor(rightText, 3.0);
            anchorPane.getChildren()
                    .add(rightText);
        }

        return anchorPane;
    }

    public static AnchorPane setupHeaderPanel(String leftString, Button rightNode, Button settingNode) {
        AnchorPane anchorPane = new AnchorPane();
        GridPane headerGrid = new GridPane();
        AnchorPane.setLeftAnchor(headerGrid, 1.0);
        AnchorPane.setTopAnchor(headerGrid, 1.0);
        AnchorPane.setBottomAnchor(headerGrid, 1.0);
        AnchorPane.setRightAnchor(headerGrid, 1.0);
        anchorPane.getChildren().add(headerGrid);

        anchorPane.getStyleClass()
                .setAll(HEADER_PANEL.toString());

        Text leftText = new Text(leftString);
        leftText.getStyleClass().setAll(StyleClasses.HEADER_TEXT.toString());
        GridPane.setConstraints(
                leftText,
                0,
                0,
                1,
                1,
                HPos.LEFT,
                VPos.BOTTOM,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        headerGrid.getChildren()
                .add(leftText);

        Pane fillerPane = new Pane();
        GridPane.setConstraints(
                fillerPane,
                1,
                0,
                1,
                1,
                HPos.CENTER,
                VPos.BOTTOM,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2));
        headerGrid.getChildren()
                .add(fillerPane);

        if (settingNode != null) {
            GridPane.setConstraints(
                    settingNode,
                    3,
                    0,
                    1,
                    1,
                    HPos.RIGHT,
                    VPos.BOTTOM,
                    Priority.NEVER,
                    Priority.NEVER,
                    new Insets(2));
            headerGrid.getChildren()
                    .add(settingNode);

            settingNode.setBorder(Border.EMPTY);
            settingNode.setBackground(makeBackground(Color.TRANSPARENT));
        }

        if (rightNode != null) {
            GridPane.setConstraints(
                    rightNode,
                    2,
                    0,
                    1,
                    1,
                    HPos.RIGHT,
                    VPos.BOTTOM,
                    Priority.NEVER,
                    Priority.NEVER,
                    new Insets(2));
            headerGrid.getChildren()
                    .add(rightNode);

            rightNode.setBorder(Border.EMPTY);
            rightNode.setBackground(makeBackground(Color.WHITE));
        } else {
            Pane filler = new Pane();
            GridPane.setConstraints(
                    filler,
                    2,
                    0,
                    1,
                    1,
                    HPos.RIGHT,
                    VPos.BOTTOM,
                    Priority.NEVER,
                    Priority.NEVER,
                    new Insets(2));
            headerGrid.getChildren()
                    .add(filler);
        }

        return anchorPane;
    }

    public static Background makeBackground(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    /**
     * Show a wait dialog IFF the worker is not finished.
     * @param title
     * @param content
     * @param worker
     * @param owner
     */
    public static void waitWithProgress(String title, String content, Worker<?> worker, Window owner) {
        if (worker.getState() == State.READY || worker.isRunning()) {
            ProgressDialog pd = new ProgressDialog(worker);
            pd.setTitle(title);
            pd.setHeaderText(null);
            pd.setContentText(content);
            if (owner != null) {
                pd.initOwner(owner);
            }
            pd.setResizable(true);
            pd.showAndWait();
        }
    }
}
