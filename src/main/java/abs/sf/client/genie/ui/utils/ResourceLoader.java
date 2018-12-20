package abs.sf.client.genie.ui.utils;

import java.io.IOException;

import abs.sf.client.genie.ui.controller.ChatController1;
import abs.sf.client.genie.ui.controller.LoginController;
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
	public Parent loadChatBaseController() throws IOException {

		return FXMLLoader.load(getClass().getClassLoader().getResource(Resources.CHAT_BASE_VIEW_FXML));
	}

	/**
	 * Load {@link ChatController1} with fxml {@link Resources#MAIN_VIEW_FXML}
	 * 
	 * @return
	 * @throws IOException
	 */
	public Parent loadLoadingView() throws IOException {

		return FXMLLoader.load(getClass().getClassLoader().getResource(Resources.LOADING_VIEW_FXML));
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

	public Image loadMessageNotDeliveredToServerImage() {
		return new Image(getClass().getClassLoader().getResource(Resources.MESSAGE_NOT_DELIVERED_TO_SERVER).toString());
	}

	public Image loadMessageDeliverToServerImage() {
		return new Image(
				getClass().getClassLoader().getResource(Resources.MESSAGE_DELIVERED_TO_SERVER_IMAGE).toString());
	}

	public Image loadMessageDeliveredToReceiverImage() {
		return new Image(
				getClass().getClassLoader().getResource(Resources.MESSAGE_DELIVERED_TO_RECEIVER_IMAGE).toString());
	}

	public Image loadMessageHasViewdByReceiverImage() {
		return new Image(
				getClass().getClassLoader().getResource(Resources.MESSAGE_VIEWED_BY_RECEIVER_IMAGE).toString());
	}
}
