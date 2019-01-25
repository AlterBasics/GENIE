package abs.sf.client.genie;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Callback;
import abs.ixi.client.io.StreamNegotiator;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.ui.utils.AppProperties;
import abs.sf.client.genie.ui.utils.JFXUtils;
import abs.sf.client.genie.ui.utils.ResourceLoader;
import abs.sf.client.genie.utils.SDKLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application {
	private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	private static Stage pStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		pStage = primaryStage;

		loadStringflowSDK();

		setupPrimaryStage();

		Parent root;
		if (AppProperties.getInstance().isPreviouslyLoggedin()) {
			login();
			root = ResourceLoader.getInstance().loadLoadingView();
		} else {
			createDatabaseSchema();
			root = ResourceLoader.getInstance().loadLoginController();
		}

		Scene mainScene = new Scene(root);
		mainScene.setRoot(root);

		pStage.setScene(mainScene);

		pStage.show();

	}

	private void createDatabaseSchema() throws StringflowErrorException {
		try {

			SDKLoader.createDatabaseSchema(AppProperties.getInstance().getH2DatabaseFilePath());

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to create datatbase Schema", e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
			throw e;
		}
	}

	private void setupPrimaryStage() {
		pStage.initStyle(StageStyle.DECORATED);
		pStage.setResizable(true);
		pStage.getIcons().add(ResourceLoader.getInstance().loadApplicationIconImage());
		pStage.setOnCloseRequest((e) -> {
			Platform.exit();
			System.exit(0);
		});

		try {

			pStage.setTitle(AppProperties.getInstance().getApplicationName());

		} catch (StringflowErrorException e) {

			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}

	private void loadStringflowSDK() throws StringflowErrorException {
		try {
			SDKLoader.loadSDK(AppProperties.getInstance().getXMPPServerIP(),
					AppProperties.getInstance().getXMPPServerPort(), AppProperties.getInstance().getMediaServerIP(),
					AppProperties.getInstance().getMediaServerPort());

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Stringflow Error while loading Stringflow SDK", e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
			throw e;
		}
	}

	private void login() {
		AppUserManager userManager = (AppUserManager) abs.ixi.client.Platform.getInstance().getUserManager();

		try {
			userManager.loginUser(AppProperties.getInstance().getUsername(), AppProperties.getInstance().getPassword(),
					AppProperties.getInstance().getDomainName(),
					new Callback<StreamNegotiator.NegotiationResult, Exception>() {
						@Override
						public void onSuccess(StreamNegotiator.NegotiationResult result) {
							if (!result.isSuccess()) {
								final String failureMesssage;
								if (result.getError() == StreamNegotiator.NegotiationError.AUTHENTICATION_FAILED) {
									failureMesssage = "App userName Password have currupted. Please login again";
									resetApp();
									showLoginView();
									return;

								} else if (result.getError() == StreamNegotiator.NegotiationError.TIME_OUT) {
									failureMesssage = "Server response timed out. try again...";

								} else {
									failureMesssage = "Something went wrong. Please try after sometime";
								}
								initiateBackgroundLogin();
								JFXUtils.showAlertOnApplicationThread(failureMesssage, AlertType.WARNING);

							}

							showChatBaseView();

						}

						@Override
						public void onFailure(Exception e) {
							initiateBackgroundLogin();
							JFXUtils.showAlertOnApplicationThread(
									"App Loading failed due to some error" + e.getMessage(), AlertType.WARNING);
							showChatBaseView();
						}
					});

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Stringflow error during login", e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}

	}

	protected void resetApp() {
		try {
			AppProperties.getInstance().removeUsername();
			AppProperties.getInstance().removePassword();
			AppProperties.getInstance().removeLoginStatus();
			SDKLoader.unloadSdk();
			loadStringflowSDK();
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, "Failed to reset App " + e.getMessage(), e);
			e.printStackTrace();
			JFXUtils.showAlert("Failed to reset app with error " + e.getMessage(), AlertType.WARNING);
		}
	}

	private void showLoginView() {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					Parent root = ResourceLoader.getInstance().loadLoginController();
					Scene scene = new Scene(root);
					scene.setRoot(root);
					pStage.setScene(scene);

					pStage.show();
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, "Failed to load ChatView " + e.getMessage(), e);
					e.printStackTrace();
					JFXUtils.showAlert("Failed to load ChatBaseView " + e.getMessage(), AlertType.WARNING);
				}
			}
		});

	}

	private void showChatBaseView() {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					Parent root = ResourceLoader.getInstance().loadChatBaseController();
					Scene scene = new Scene(root);
					scene.setRoot(root);
					pStage.setScene(scene);

					pStage.show();
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, "Failed to load ChatView " + e.getMessage(), e);
					e.printStackTrace();
					JFXUtils.showAlert("Failed to load ChatBaseView " + e.getMessage(), AlertType.WARNING);
				}
			}
		});

	}

	private void initiateBackgroundLogin() {
		try {
			if (AppProperties.getInstance().isPreviouslyLoggedin()) {
				LOGGER.info("starting background login process");
				abs.ixi.client.Platform.getInstance().getUserManager().loginInBackground(
						AppProperties.getInstance().getUsername(), AppProperties.getInstance().getPassword(),
						AppProperties.getInstance().getDomainName());
			}

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to initiate background login process", e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}
	}
}