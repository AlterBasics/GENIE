package abs.sf.client.gini.ui.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.ui.utils.JFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChatController implements Initializable {
	private static final Logger LOGGER = Logger.getLogger(ChatController.class.getName());
	private static final String DEFAULT_PRESENCE_STATUS = "online";

	@FXML
	private ImageView userProfileImageView;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab contactsTab;

	@FXML
	private Tab conversationsTab;

	@FXML
	private Label userNameLabel;

	@FXML
	private Label userStatusLabel;

	@FXML
	private ListView<RosterItem> contactsListView;

	@FXML
	ObservableList<RosterItem> contactsObservableList = FXCollections.observableArrayList();

	private JID userJID;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setupUserData();
		setDefaultSelectedTab();
	}

	private void setDefaultSelectedTab() {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(contactsTab);
	}

	private void setupUserData() {
		LOGGER.info("Setting user data");
		try {
			this.userJID = Platform.getInstance().getUserJID();

			AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

			UserProfileData userProfileData = userManager.getCachedUserProfileData(this.userJID);

			if (userProfileData == null) {
				userProfileData = userManager.getUserProfileData(this.userJID);
			}

			String userName = userProfileData.getUserName();
			this.userNameLabel.setText(StringUtils.isNullOrEmpty(userName) ? this.userJID.getNode() : userName);
			this.userStatusLabel.setText(DEFAULT_PRESENCE_STATUS);

			InputStream profileStream = userManager.getUserAvatar(this.userJID);

			if (profileStream != null) {
				userProfileImageView.setImage(new Image(profileStream));
			}

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to setup user data due to Stringflow error : " + e.getMessage(), e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}

	}

	public void setContactsListView() {
		LOGGER.info("Setting Contacts List View");
		try {
			AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

			List<RosterItem> rosterItemList = userManager.getRosterItemList();
			this.contactsObservableList.setAll(rosterItemList);
			this.contactsListView.setItems(this.contactsObservableList);

			this.contactsListView.setCellFactory((u) -> {

				return new ListCell<RosterItem>() {
					@Override
					protected void updateItem(RosterItem rosterItem, boolean bool) {
						super.updateItem(rosterItem, bool);

						if (rosterItem != null) {
							try {
								ContactCell contactCell = new ContactCell(rosterItem);
								setGraphic(contactCell.getContactCellGraphics());
							} catch (Exception e) {
								LOGGER.log(Level.WARNING,
										"Failed to load Contact cell for roster item " + rosterItem.getJid(), e);

								JFXUtils.showAlert("Failed to load Contact cell for roster item " + rosterItem.getJid(),
										AlertType.WARNING);
							}
						}

					}

				};

			});

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "failed to setup contact list view" + e);
			JFXUtils.showAlert("Failed to setup contact list view due to " + e.getMessage(), AlertType.WARNING);
		}
	}

	public void setConverstionsListView() {
		LOGGER.info("Setting Conversations List View");
	}
}