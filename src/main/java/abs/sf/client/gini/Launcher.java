package abs.sf.client.gini;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application {
	private static Stage pStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		pStage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("views/LoginView.fxml"));
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setTitle("GINI v1.0");
		primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/plug.png").toString()));
		Scene mainScene = new Scene(root, 350, 420);
		mainScene.setRoot(root);
		primaryStage.setResizable(false);
		primaryStage.setScene(mainScene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> Platform.exit());
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}
}