package abs.sf.client.gini.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import abs.ixi.client.core.Callback;
import abs.ixi.client.io.StreamNegotiator;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.ui.Launcher;
import abs.sf.client.gini.ui.utils.AppProperties;
import abs.sf.client.gini.ui.utils.JFXUtils;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import abs.sf.client.gini.utils.SDKLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class LoginController implements Initializable {
	@FXML
	private TextField usernameTextfield;

	@FXML
	private PasswordField passwordField;

	@FXML
	public TextField hostnameTextfield;

	@FXML
	private TextField portTextfield;

	@FXML
	private BorderPane borderPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SDKLoader.loadSDK(AppProperties.getInstance().getXMPPServerIP(),
				AppProperties.getInstance().getXMPPServerPort(), AppProperties.getInstance().getMediaServerIP(),
				AppProperties.getInstance().getMediaServerPort());

		if (AppProperties.getInstance().isPreviouslyLoggedin()) {
			Parent root = ResourceLoader.getInstance().loadChatController();
			Scene mainScene = new Scene(root);
			mainScene.setRoot(root);

			Launcher.getPrimaryStage().setScene(mainScene);

			Launcher.getPrimaryStage().show();
		}

	}

	private void showChatView() {
		try {
			Parent root = ResourceLoader.getInstance().loadChatController();
			Scene scene = new Scene(root);
			scene.setRoot(root);
			Launcher.getPrimaryStage().setScene(scene);
			Launcher.getPrimaryStage().show();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void loginButtonAction() throws IOException {
		final String userName = usernameTextfield.getText();
		final String password = passwordField.getText();

		boolean validated = validateInputs(userName, password);

		if (validated) {
			AndroidUserManager userManager = (AndroidUserManager) abs.ixi.client.core.Platform.getInstance()
					.getUserManager();

			userManager.loginUser(userName, password, AppProperties.getInstance().getDomainName(),
					new Callback<StreamNegotiator.NegotiationResult, Exception>() {
						@Override
						public void onSuccess(StreamNegotiator.NegotiationResult result) {

							if (result.isSuccess()) {
								AppProperties.getInstance().setUsername(userName);
								AppProperties.getInstance().setPassword(password);
								AppProperties.getInstance().setLoginStatus(true);

							} else {
								final String failureMesssage;
								if (result.getError() == StreamNegotiator.NegotiationError.AUTHENTICATION_FAILED) {
									failureMesssage = "Entered userName Password are incorrect";

								} else if (result.getError() == StreamNegotiator.NegotiationError.TIME_OUT) {
									failureMesssage = "Server response timed out. try again...";

								} else {
									failureMesssage = "Something went wrong. Please try after sometime";
								}

								JFXUtils.showAlert(failureMesssage, AlertType.WARNING);
							}
						}

						@Override
						public void onFailure(Exception e) {
							JFXUtils.showAlert(e.getMessage(), AlertType.WARNING);
						}
					});
		}
	}

	private boolean validateInputs(final String userName, final String password) {
		if (StringUtils.isNullOrEmpty(userName)) {
			JFXUtils.showAlert("Please enter user name", AlertType.INFORMATION);
			return false;

		} else if (StringUtils.isNullOrEmpty(password)) {
			JFXUtils.showAlert("Please enter password", AlertType.INFORMATION);
			return false;
		}

		return true;
	}

	// public void showScene() throws IOException {
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
	//
	// }

	/* Terminates Application */
	public void closeSystem() {
		Platform.exit();
		System.exit(0);
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
