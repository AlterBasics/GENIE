package abs.sf.client.gini.ui.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class JFXUtils {
	public static void showAlert(String message, AlertType alertType) {
		Platform.runLater(() -> {
			Alert alert = new Alert(alertType);
			// alert.setTitle("Warning!");
			alert.setHeaderText(message);
			// alert.setContentText("Please check for firewall issues and check
			// if the server is running.");
			alert.showAndWait();
		});
	}
}
