package abs.sf.client.genie.ui.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.Platform;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.genie.exception.StringflowException;
import abs.sf.client.genie.managers.AppChatManager;
import abs.sf.client.genie.managers.AppPresenceManager;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.messaging.ChatLine;
import abs.sf.client.genie.messaging.ChatLine.MessageStatus;
import abs.sf.client.genie.messaging.UserPresence;
import abs.sf.client.genie.ui.utils.JFXUtils;
import abs.sf.client.genie.ui.utils.ResourceLoader;
import abs.sf.client.genie.ui.utils.Resources;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class ContactChatController {
	private static final Logger LOGGER = Logger.getLogger(ContactChatController.class.getName());

	private static final AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

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
		setupMessageTextArea();
		sendReadReceiptforAllUnreadMessage();
		markNoUnreadConversation();
	}

	private void sendReadReceiptforAllUnreadMessage() {
		try {
			chatManager.sendAllUnReadMessageReadReceipt(contactJID);
		} catch (StringflowException e) {
			LOGGER.log(Level.WARNING,
					"Failed to send message read receipt for all unread messages with error " + e.getMessage(), e);

			JFXUtils.showAlert(
					"Failed to send message read receipt for all unread messages with error " + e.getMessage(),
					AlertType.WARNING);

		}

	}

	private void markNoUnreadConversation() {
		try {
			chatManager.markNoUnreadConversation(contactJID);
		} catch (StringflowException e) {
			LOGGER.log(Level.WARNING, "Failed to mark no unread conversationd with error " + e.getMessage(), e);

			JFXUtils.showAlert("Failed to mark no unread conversationd with error " + e.getMessage(),
					AlertType.WARNING);

		}
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.CONTACT_CHAT_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();
	}

	private void setupMessageTextArea() {
		messageTextArea.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
			if (ke.getCode().equals(KeyCode.ENTER)) {
				sendButtonAction();
				ke.consume();
			}
		});

	}

	public void sendButtonAction() {
		String msg = messageTextArea.getText().trim();
		messageTextArea.clear();

		if (!StringUtils.isNullOrEmpty(msg)) {
			try {

				chatManager.sendTextMessage(msg, this.contactJID, this.isGroup);

			} catch (StringflowException e) {
				LOGGER.log(Level.WARNING, "Failed to send Message to " + this.contactJID + " due to " + e.getMessage(),
						e);

				JFXUtils.showAlert("Failed to send Message to " + this.contactJID + " due to " + e.getMessage(),
						AlertType.WARNING);
			}
		}
	}

	private void setViewData() throws Exception {
		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

		this.isGroup = userManager.checkIsChatRoom(this.contactJID);

		if (isGroup) {
			String roomSubject = userManager.getChatRoomSubject(this.contactJID);
			this.contactNameLabel
					.setText(StringUtils.isNullOrEmpty(roomSubject) ? this.contactJID.getNode() : roomSubject);

		} else {
			String contactRosterName = userManager.getRosterItemName(this.contactJID);

			this.contactNameLabel.setText(
					StringUtils.isNullOrEmpty(contactRosterName) ? this.contactJID.getNode() : contactRosterName);
		}

		AppPresenceManager presenceManager = (AppPresenceManager) Platform.getInstance().getPresenceManager();
		UserPresence presence = presenceManager.getUserPresence(this.contactJID);

		if (presence != null && presence.isOnline()) {
			this.onlineStatusImageView.setVisible(true);

		} else {
			this.onlineStatusImageView.setVisible(false);
		}

		InputStream contactImageStream = userManager.getUserAvatar(this.contactJID);

		if (contactImageStream != null) {
			this.contactImageView.setImage(new Image(contactImageStream));

		} else if (isGroup) {
			this.contactImageView.setImage(ResourceLoader.getInstance().loadGroupDefaultImage());
		}

		this.typingLabel.setVisible(false);

		setChatLineListView();
	}

	private void setChatLineListView() throws StringflowException {
		List<ChatLine> allChatLines = chatManager.getAllConversationChatLines(this.contactJID, this.isGroup);

		this.chatObservableList.setAll(allChatLines);
		this.chatListView.setItems(this.chatObservableList);

		this.chatListView.setCellFactory((u) -> {

			return new ListCell<ChatLine>() {
				@Override
				protected void updateItem(ChatLine chatLine, boolean bool) {
					super.updateItem(chatLine, bool);

					if (chatLine != null) {
						try {
							ChatLineCell chatLineCell;
							if (chatLine.getDirection() == ChatLine.Direction.RECEIVE) {
								chatLineCell = new ReceiveChatlineCell(chatLine);
							} else {
								chatLineCell = new SendChatLineCell(chatLine);

							}

							setGraphic(chatLineCell.getChatLineCellGraphics());

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

		this.scrollChatListViewAtEnd();
	}

	public VBox getContactChatViewGraphics() {
		return this.contactChatViewVBox;
	}

	private void scrollChatListViewAtEnd() {
		int endEndex = this.chatObservableList.size() - 1;
		this.chatListView.scrollTo(endEndex);
	}

	public void onNewMessageReceived(ChatLine line) {
		if (StringUtils.safeEquals(line.getPeerBareJid(), this.contactJID.getBareJID(), false)) {
			this.chatObservableList.add(line);
			chatListView.refresh();
			scrollChatListViewAtEnd();
			try {
				chatManager.sendMessageReadReceipt(line);
			} catch (StringflowException e) {
				LOGGER.log(Level.WARNING, "Failed to send message read receipt for messageId " + line.getMessageId()
						+ " with error " + e.getMessage(), e);

				JFXUtils.showAlert("Failed to send message read receipt for messageId " + line.getMessageId()
						+ " with error " + e.getMessage(), AlertType.WARNING);
			}

			try {
				chatManager.markNoUnreadConversation(this.contactJID);
			} catch (StringflowException e) {
				LOGGER.log(Level.WARNING, "Failed to mark no unread conversationd with error " + e.getMessage(), e);

				JFXUtils.showAlert("Failed to mark no unread conversationd with error " + e.getMessage(),
						AlertType.WARNING);
			}

		}
	}

	public void onNewMessageSend(ChatLine line) {
		if (StringUtils.safeEquals(line.getPeerBareJid(), this.contactJID.getBareJID(), false)) {
			this.chatObservableList.add(line);
			chatListView.refresh();
			scrollChatListViewAtEnd();
		}
	}

	public void onMessageDeliveredToServer(String messageId, JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			for (int index = this.chatObservableList.size() - 1; index >= 0; index--) {
				ChatLine chaline = this.chatObservableList.get(index);
				if (StringUtils.safeEquals(chaline.getMessageId(), messageId)) {
					chaline.setMessageStatus(MessageStatus.DELIVERED_TO_SERVER);
					chatListView.refresh();
					break;
				}
			}
		}

	}

	public void onMessageDeliveredToReceiver(String messageId, JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			for (int index = this.chatObservableList.size() - 1; index >= 0; index--) {
				ChatLine chaline = this.chatObservableList.get(index);
				if (StringUtils.safeEquals(chaline.getMessageId(), messageId)) {
					chaline.setMessageStatus(MessageStatus.DELIVERED_TO_RECEIVER);
					chatListView.refresh();
					break;
				}
			}
		}
	}

	public void onMessageAcknowledgedToReceiver(String messageId, JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			for (int index = this.chatObservableList.size() - 1; index >= 0; index--) {
				ChatLine chaline = this.chatObservableList.get(index);
				if (StringUtils.safeEquals(chaline.getMessageId(), messageId)) {
					chaline.setMessageStatus(MessageStatus.RECEIVER_IS_ACKNOWLEDGED);
					chatListView.refresh();
					break;
				}
			}
		}
	}

	public void onMessageViewedByReceiver(String messageId, JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			for (int index = this.chatObservableList.size() - 1; index >= 0; index--) {
				ChatLine chaline = this.chatObservableList.get(index);
				if (StringUtils.safeEquals(chaline.getMessageId(), messageId)) {
					chaline.setMessageStatus(MessageStatus.RECEIVER_HAS_VIEWED);
					chatListView.refresh();
					break;
				}
			}
		}
	}

	public void onContactTypingStarted(JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			this.typingLabel.setVisible(true);
			chatListView.refresh();
		}
	}

	public void onContactTypingPaused(JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			this.typingLabel.setVisible(false);
			chatListView.refresh();
		}
	}

	public void onContactInactivityInUserChat(JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			this.typingLabel.setVisible(false);
			chatListView.refresh();
		}
	}

	public void onContactGoneFromUserChat(JID pearJID) {
		if (StringUtils.safeEquals(pearJID.getBareJID(), contactJID.getBareJID(), false)) {
			this.typingLabel.setVisible(false);
			chatListView.refresh();
		}
	}

}
