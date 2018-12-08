package abs.sf.client.gini.ui.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Presence;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppChatManager;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.ui.utils.JFXUtils;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import abs.sf.client.gini.ui.utils.Resources;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ContactChatController {
	private static final Logger LOGGER = Logger.getLogger(ContactChatController.class.getName());

	@FXML
	private VBox contactChatViewVBox;

	@FXML
	private ImageView contactImageView;

	@FXML
	private ImageView onlineStatusImageView;

	@FXML
	private Label contactNameLabel;

	@FXML
	private Label typingLabel;

	@FXML
	private TextArea messageTextArea;

	@FXML
	private ListView<ChatLine> chatListView;

	@FXML
	private ObservableList<ChatLine> chatObservableList = FXCollections.observableArrayList();

	private JID contactJID;

	private boolean isGroup;

	public ContactChatController(JID contactJID) throws Exception {
		initView();
		this.contactJID = contactJID;

		setViewData();
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.CONTACT_CHAT_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();
	}

	private void setViewData() throws Exception {
		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();
		String contactRosterName = userManager.getRosterItemName(this.contactJID);

		this.contactNameLabel
				.setText(StringUtils.isNullOrEmpty(contactRosterName) ? this.contactJID.getNode() : contactRosterName);

		Presence presence = userManager.getUserPresence(this.contactJID);

		if (presence != null && presence.getType() == PresenceType.AVAILABLE) {
			this.onlineStatusImageView.setVisible(true);
		}

		this.isGroup = userManager.checkIsChatRoom(this.contactJID);

		InputStream contactImageStream = userManager.getUserAvatar(this.contactJID);

		if (contactImageStream != null) {
			this.contactImageView.setImage(new Image(contactImageStream));

		} else if (isGroup) {
			this.contactImageView.setImage(ResourceLoader.getInstance().loadGroupDefaultImage());
		}

		this.typingLabel.setVisible(false);

		setChatLineListView();
	}

	private void setChatLineListView() throws StringflowErrorException {
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();
		List<ChatLine> allChatLines = chatManager.getAllConversationChatLines(contactJID);

		this.chatObservableList.setAll(allChatLines);
		this.chatListView.setItems(this.chatObservableList);

		this.chatListView.setCellFactory((u) -> {

			return new ListCell<ChatLine>() {
				@Override
				protected void updateItem(ChatLine chatLine, boolean bool) {
					super.updateItem(chatLine, bool);

					if (chatLine != null) {
						try {
							ChatLineCell contactCell = new ChatLineCell(chatLine);
							// setGraphic(contactCell.getContactCellGraphics());
						} catch (Exception e) {
							LOGGER.log(Level.WARNING,
									"Failed to load ChatLine cell for given chatLine " + chatLine.getContentId(), e);

							JFXUtils.showAlert(
									"Failed to load ChatLine cell for given chatLine" + chatLine.getContentId(),
									AlertType.WARNING);
						}
					}

				}

			};

		});
	}

	public VBox getContactChatViewGraphics() {
		return this.contactChatViewVBox;
	}

}
