package abs.sf.client.gini.ui.controller;

import abs.sf.client.gini.ui.Launcher;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

public abstract class APPController implements Initializable { 

	public void showSceneOnPrimeryStage(Scene scene) {
		Launcher.getPrimaryStage().setScene(scene);
		Launcher.getPrimaryStage().show();
		// Platform.runLater(() -> {
		// Stage stage = (Stage) hostnameTextfield.getScene().getWindow();
		// stage.setResizable(true);
		// stage.setWidth(1040);
		// stage.setHeight(620);
		//
		// stage.setOnCloseRequest((WindowEvent e) -> {
		// Platform.exit();
		// System.exit(0);
		// });
		// stage.setScene(this.scene);
		// stage.setMinWidth(800);
		// stage.setMinHeight(300);
		// ResizeHelper.addResizeListener(stage);
		// stage.centerOnScreen();
		// con.setUsernameLabel(usernameTextfield.getText());
		// });
	}
	

	/* This displays an alert message to the user */
	public void showErrorDialog(String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Warning!");
			alert.setHeaderText(message);
			alert.setContentText("Please check for firewall issues and check if the server is running.");
			alert.showAndWait();
		});
	}
}
