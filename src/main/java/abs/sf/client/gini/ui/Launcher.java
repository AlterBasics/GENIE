package abs.sf.client.gini.ui;

import abs.sf.client.gini.ui.utils.AppProperties;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application {
	private static Stage pStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		pStage = primaryStage;

		setupPrimaryStage();

		Parent root = ResourceLoader.getInstance().loadLoginController();
		Scene mainScene = new Scene(root);
		mainScene.setRoot(root);

		primaryStage.setScene(mainScene);

		primaryStage.show();
	}

	private void setupPrimaryStage() {
		pStage.initStyle(StageStyle.UNDECORATED);
		pStage.setResizable(true);
		pStage.setTitle(AppProperties.getInstance().getApplicationName());
		pStage.getIcons().add(ResourceLoader.getInstance().loadApplicationIconImage());
		pStage.setOnCloseRequest((e) -> {
			Platform.exit();
			System.exit(0);
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}
}