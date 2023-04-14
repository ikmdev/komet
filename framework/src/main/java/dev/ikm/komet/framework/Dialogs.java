package dev.ikm.komet.framework;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.controlsfx.dialog.ExceptionDialog;
import dev.ikm.tinkar.common.alert.AlertObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * CommonDialogs
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author kec
 */
public class Dialogs {

    private static final Logger LOG = LoggerFactory.getLogger(Dialogs.class);

    private Dialogs() {
        // hidden
    }

    public static void showInformationDialog(String title, String message) {
        showInformationDialog(title, message, null);
    }

    public static void showInformationDialog(String title, String message, Window parentWindow) {
        showDialog(AlertType.INFORMATION, title, message, parentWindow);
    }

    private static void showDialog(AlertType alertType, String title, String message, Window parentWindow) {
        Runnable r = () -> {
            Alert informationDialog = createAlertDialog(alertType, Modality.NONE, parentWindow);
            informationDialog.setTitle("");
            informationDialog.getDialogPane().setHeaderText(title);
            informationDialog.getDialogPane().setContentText(message);
            informationDialog.initStyle(StageStyle.UTILITY);
            informationDialog.setResizable(true);
            informationDialog.showAndWait();
        };
        show(r);
    }

    private static Alert createAlertDialog(AlertType type, Modality modality, Window owner) {
        Alert dialog = new Alert(type, "");
        dialog.initModality(modality);
        dialog.initOwner(owner);
        dialog.setResizable(true);
        return dialog;
    }

    private static void show(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public static void showInformationDialog(String title, Node content) {
        Runnable r = () -> {
            Alert informationDialog = createAlertDialog(AlertType.INFORMATION, Modality.NONE);
            informationDialog.setTitle("");
            informationDialog.getDialogPane().setHeaderText(title);
            informationDialog.getDialogPane().setContent(content);
            informationDialog.initStyle(StageStyle.UTILITY);
            informationDialog.setResizable(true);
            informationDialog.showAndWait();
        };
        show(r);
    }

    private static Alert createAlertDialog(AlertType type, Modality modality) {
        return createAlertDialog(type, modality, null);
    }

    public static void showWarningDialog(String title, String message, Window parentWindow) {
        showDialog(AlertType.WARNING, title, message, parentWindow);
    }

    public static void showErrorDialog(String title, String message, Throwable throwable) {
        showErrorDialog(title, message, throwable, null);
    }

    public static void showErrorDialog(String title, String message, Throwable throwable, Window parentWindow) {
        LOG.error(message, throwable);
        Runnable showDialog = () -> {
            ExceptionDialog dlg = new ExceptionDialog(throwable);
            dlg.setTitle(title);
            dlg.setHeaderText(message);
            dlg.getDialogPane().setHeaderText(throwable.getMessage());
            dlg.initStyle(StageStyle.UTILITY);
            dlg.initOwner(parentWindow);
            dlg.setResizable(true);

            dlg.showAndWait();
        };
        show(showDialog);
    }

    public static void showErrorDialog(String title, String message, String details) {
        showErrorDialog(title, message, details, null);
    }

    public static void showErrorDialog(String title, String message, String details, Window parentWindow) {
        Runnable showDialog = () -> {
            Alert dlg = createAlertDialog(AlertType.ERROR, Modality.NONE, parentWindow);
            dlg.setTitle(title);
            dlg.getDialogPane().setHeaderText(message);
            dlg.getDialogPane().setContentText(details);
            dlg.setResizable(true);
            dlg.showAndWait();
        };

        show(showDialog);
    }

    public static Optional<ButtonType> showYesNoDialog(String title, String question) {
        return showYesNoDialog(title, question, null);
    }

    public static Optional<ButtonType> showYesNoDialog(String title, String question, Window parentWindow) {
        Callable<Optional<ButtonType>> showDialog = () -> {
            Alert dlg = createAlertDialog(AlertType.INFORMATION, Modality.NONE, parentWindow);
            dlg.setTitle("");
            dlg.getDialogPane().setHeaderText(title);
            dlg.getDialogPane().setContentText(question);
            dlg.getButtonTypes().clear();
            dlg.getButtonTypes().add(ButtonType.NO);
            dlg.getButtonTypes().add(ButtonType.YES);
            dlg.setResizable(true);
            return dlg.showAndWait();
        };
        final FutureTask<Optional<ButtonType>> showTask = new FutureTask<>(showDialog);

        show(showTask);
        try {
            return showTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Optional<ButtonType> showDialogForAlert(AlertObject alertObject) {
        return showDialogForAlert(alertObject, null);
    }

    public static Optional<ButtonType> showDialogForAlert(AlertObject alertObject, Window parentWindow) {
        return switch (alertObject.getAlertType()) {
            case SUCCESS, INFORMATION -> {
                showInformationDialog(alertObject.getAlertTitle(), alertObject.getAlertDescription(), parentWindow);
                yield Optional.empty();
            }

            case ERROR -> {
                if (alertObject.getThrowable().isPresent()) {
                    showErrorDialog(alertObject.getAlertTitle(), alertObject.getAlertDescription(), alertObject.getThrowable().get(), parentWindow);
                } else {
                    showErrorDialog(alertObject.getAlertTitle(), alertObject.getAlertCategory().toString(), alertObject.getAlertDescription(), parentWindow);
                }
                yield Optional.empty();
            }

            case WARNING -> {
                showWarningDialog(alertObject.getAlertTitle(), alertObject.getAlertDescription(), parentWindow);
                yield Optional.empty();
            }

            case CONFIRMATION -> showYesNoDialog(alertObject.getAlertTitle(), alertObject.getAlertDescription(), parentWindow);

            default -> {
                showErrorDialog(alertObject.getAlertTitle(), alertObject.getAlertType().toString() + " " +
                        alertObject.getAlertCategory().toString(), alertObject.getAlertDescription(), parentWindow);
                yield Optional.empty();
            }
        };
    }

}
