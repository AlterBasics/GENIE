package abs.sf.client.gini.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.ui.utils.AppProperties;
import abs.sf.client.gini.ui.utils.JFXUtils;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import abs.sf.client.gini.utils.SDKLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
		root = ResourceLoader.getInstance().loadChatController();
//		if (AppProperties.getInstance().isPreviouslyLoggedin()) {
//			root = ResourceLoader.getInstance().loadChatController();
//			initiateBackgroundLogin();
//
//		} else {
//			root = ResourceLoader.getInstance().loadLoginController();
//		}

		Scene mainScene = new Scene(root);
		mainScene.setRoot(root);

		pStage.setScene(mainScene);

		pStage.show();
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

	private void initiateBackgroundLogin() {
		try {
			if (AppProperties.getInstance().isPreviouslyLoggedin()) {
				LOGGER.info("starting background login process");
				abs.ixi.client.core.Platform.getInstance().getUserManager().loginInBackground(
						AppProperties.getInstance().getUsername(), AppProperties.getInstance().getPassword(),
						AppProperties.getInstance().getDomainName());
			}

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to initiate background login process", e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}
	}
}