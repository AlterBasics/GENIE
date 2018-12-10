package abs.sf.client.genie.ui.controller;

import abs.sf.client.genie.ui.Launcher;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Scene;

public abstract class APPController implements Initializable {

	public void showSceneOnPrimeryStage(Scene scene) {
		Launcher.getPrimaryStage().setScene(scene);
		Launcher.getPrimaryStage().show();
	}

	public void closeSystem() {
		Platform.exit();
		System.exit(0);
	}

}
