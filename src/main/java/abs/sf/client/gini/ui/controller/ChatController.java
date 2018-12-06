package abs.sf.client.gini.ui.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.ui.utils.JFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ChatController implements Initializable {
	private static final Logger LOGGER = Logger.getLogger(ChatController.class.getName());

	@FXML
	private ImageView userProfileImageView;

	@FXML
	private Label userNameLabel, userStatusLabel;

	@FXML
	private ListView<HBox> contarctsListView;

	private JID userJID;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setupUserData();
	}

	private void setupUserData() {
		try {
			this.userJID = Platform.getInstance().getUserJID();

			AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

			UserProfileData userProfileData = userManager.getCachedUserProfileData(this.userJID);

			if (userProfileData == null) {
				userProfileData = userManager.getUserProfileData(this.userJID);
			}

			String userName = userProfileData.getUserName();
			this.userNameLabel.setText(StringUtils.isNullOrEmpty(userName) ? this.userJID.getNode() : userName);

			InputStream profileStream = userManager.getUserAvatar(this.userJID);

			if (profileStream != null) {
				userProfileImageView.setImage(new Image(profileStream));
			}

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to setup user data due to Stringflow error : " + e.getMessage(), e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}

	}

}