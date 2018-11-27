package abs.sf.client.gini.ui.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class JFXUtils {
	public static void showAlert(final String message, final AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static void showAlertOnApplicationThread(final String message, final AlertType alertType) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				showAlert(message, alertType);

			}
		});
	}

	public static void showAlert(final String title, final String message, final AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static void showAlertOnApplicationThread(final String title, final String message,
			final AlertType alertType) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				showAlert(title, message, alertType);

			}
		});
	}

	public static void showStringflowErrorAlert(final String errorMessage) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Stringflow Error Warning!");
		alert.setContentText(errorMessage);
		alert.showAndWait();
	}

	public static void showStringflowErrorAlertOnApplicationThread(final String errorMessage) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				showStringflowErrorAlert(errorMessage);

			}
		});
	}

}
