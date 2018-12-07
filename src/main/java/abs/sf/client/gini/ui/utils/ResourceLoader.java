package abs.sf.client.gini.ui.utils;

import java.io.IOException;

import abs.sf.client.gini.ui.controller.ChatController1;
import abs.sf.client.gini.ui.controller.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;

public class ResourceLoader {
	public static ResourceLoader instance;

	/**
	 * Restricting access to local
	 * 
	 */
	private ResourceLoader() {
	}

	/**
	 * Returns the singleton instance of {@code SFSDKProperties}
	 */
	public static ResourceLoader getInstance() {
		if (instance == null) {
			instance = new ResourceLoader();
		}

		return instance;
	}

	/**
	 * Load {@link LoginController} with fxml {@link Resources#LOGIN_VIEW_FXML}.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Parent loadLoginController() throws IOException {
		return FXMLLoader.load(getClass().getClassLoader().getResource(Resources.LOGIN_VIEW_FXML));
	}

	/**
	 * Load {@link ChatController1} with fxml {@link Resources#MAIN_VIEW_FXML}
	 * 
	 * @return
	 * @throws IOException
	 */
	public Parent loadChatController() throws IOException {

		return FXMLLoader.load(getClass().getClassLoader().getResource(Resources.CHAT_VIEW_FXML));
	}

	/**
	 * Load Application icon Image resource {@link Resources#APP_ICON_IMAGE}.
	 * 
	 * @return
	 */
	public Image loadApplicationIconImage() {
		return new Image(getClass().getClassLoader().getResource(Resources.APP_ICON_IMAGE).toString());
	}

	/**
	 * Load group default image icon resource
	 * {@link Resources#GROUP_DEFAULT_IMAGE}.
	 * 
	 * @return
	 */
	public Image loadGroupDefaultImage() {
		return new Image(getClass().getClassLoader().getResource(Resources.GROUP_DEFAULT_IMAGE).toString());
	}
}
