package abs.sf.client.gini.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Callback;
import abs.ixi.client.io.StreamNegotiator;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.ui.Launcher;
import abs.sf.client.gini.ui.utils.AppProperties;
import abs.sf.client.gini.ui.utils.JFXUtils;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class LoginController extends APPController implements Initializable {
	private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

	@FXML
	private TextField usernameTextfield;

	@FXML
	private PasswordField passwordField;

	@FXML
	private BorderPane borderPane;

	private double xOffset;

	private double yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/* Drag and Drop */
		borderPane.setOnMousePressed(event -> {
			xOffset = Launcher.getPrimaryStage().getX() - event.getScreenX();
			yOffset = Launcher.getPrimaryStage().getY() - event.getScreenY();
			borderPane.setCursor(Cursor.CLOSED_HAND);
		});

		borderPane.setOnMouseDragged(event -> {
			Launcher.getPrimaryStage().setX(event.getScreenX() + xOffset);
			Launcher.getPrimaryStage().setY(event.getScreenY() + yOffset);

		});

		borderPane.setOnMouseReleased(event -> {
			borderPane.setCursor(Cursor.DEFAULT);
		});

	}

	private void showChatView() {
		try {
			Parent root = ResourceLoader.getInstance().loadChatController();
			Scene scene = new Scene(root);
			scene.setRoot(root);
			showSceneOnPrimeryStage(scene);

		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Failed to load ChatView", e);
			JFXUtils.showAlert("Failed to load ChatView", AlertType.WARNING);
		}
	}

	public void loginButtonAction() {
		final String userName = usernameTextfield.getText();
		final String password = passwordField.getText();

		boolean validated = validateInputs(userName, password);

		if (validated) {
			AppUserManager userManager = (AppUserManager) abs.ixi.client.core.Platform.getInstance()
					.getUserManager();

			try {
				userManager.loginUser(userName, password, AppProperties.getInstance().getDomainName(),
						new Callback<StreamNegotiator.NegotiationResult, Exception>() {
							@Override
							public void onSuccess(StreamNegotiator.NegotiationResult result) {

								if (result.isSuccess()) {
									try {
										AppProperties.getInstance().setUsername(userName);
										AppProperties.getInstance().setPassword(password);
										AppProperties.getInstance().setLoginStatus(true);

										javafx.application.Platform.runLater(new Runnable() {

											@Override
											public void run() {
												showChatView();
											}
										});

									} catch (StringflowErrorException e) {
										LOGGER.log(Level.WARNING, "Stringflow error after successfully login", e);
										JFXUtils.showStringflowErrorAlert(e.getMessage());

									}

								} else {
									final String failureMesssage;
									if (result.getError() == StreamNegotiator.NegotiationError.AUTHENTICATION_FAILED) {
										failureMesssage = "Entered userName Password are incorrect";

									} else if (result.getError() == StreamNegotiator.NegotiationError.TIME_OUT) {
										failureMesssage = "Server response timed out. try again...";

									} else {
										failureMesssage = "Something went wrong. Please try after sometime";
									}

									JFXUtils.showAlertOnApplicationThread(failureMesssage, AlertType.WARNING);
								}
							}

							@Override
							public void onFailure(Exception e) {
								JFXUtils.showAlertOnApplicationThread("Loggin failed due to some error" + e.getMessage(),
										AlertType.WARNING);
							}
						});

			} catch (StringflowErrorException e) {
				LOGGER.log(Level.WARNING, "Stringflow error during login", e);
				JFXUtils.showStringflowErrorAlert(e.getMessage());
			}

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

}
