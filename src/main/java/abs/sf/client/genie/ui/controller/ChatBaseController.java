package abs.sf.client.genie.ui.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppChatManager;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.messaging.ChatLine;
import abs.sf.client.genie.messaging.ChatListener;
import abs.sf.client.genie.messaging.Conversation;
import abs.sf.client.genie.ui.utils.JFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ChatBaseController implements Initializable, ChatListener {
	private static final Logger LOGGER = Logger.getLogger(ChatBaseController.class.getName());

	private static final String DEFAULT_PRESENCE_STATUS = "online";

	@FXML
	private AnchorPane contactChatViewAnchorPane;

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
	private Label welcomeMessageLabel;

	@FXML
	private ListView<RosterItem> contactsListView;

	@FXML
	ObservableList<RosterItem> contactsObservableList = FXCollections.observableArrayList();

	@FXML
	private ListView<Conversation> conversationsListView;

	@FXML
	private ObservableList<Conversation> conversationsObservableList = FXCollections.observableArrayList();

	private ContactChatController chatController;

	private JID userJID;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setupUserData();
		this.setContactsListView();
		this.setConverstionsListView();
		setDefaultSelectedTab();
		registerAsChatListener();
	}

	private void registerAsChatListener() {
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();
		chatManager.addChatListener(this);
	}

	private void setDefaultSelectedTab() {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(conversationsTab);
	}

	private boolean isConversationTabSelected() {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		return selectionModel.getSelectedItem() == conversationsTab;
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
			String name = StringUtils.isNullOrEmpty(userName) ? this.userJID.getNode() : userName;
			this.userNameLabel.setText(name);
			this.welcomeMessageLabel.setText(getWelcomeMessage(name));

			this.userStatusLabel.setText(DEFAULT_PRESENCE_STATUS);

			InputStream profileStream = userManager.getUserAvatar(this.userJID);

			if (profileStream != null) {
				this.userProfileImageView.setImage(new Image(profileStream));
			}

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to setup user data due to Stringflow error : " + e.getMessage(), e);
			JFXUtils.showStringflowErrorAlert(e.getMessage());
		}

	}

	private String getWelcomeMessage(String name) {
		return "Welcome, " + name;
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
								LOGGER.log(Level.WARNING, "Failed to load Contact cell for roster item "
										+ rosterItem.getJid() + " due to " + e.getMessage(), e);

								JFXUtils.showAlert("Failed to load Contact cell for roster item " + rosterItem.getJid()
										+ " due to " + e.getMessage(), AlertType.WARNING);
							}
						}

					}

				};

			});

			this.contactsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					RosterItem clickedContact = contactsListView.getSelectionModel().getSelectedItem();
					openContactChatView(clickedContact.getJid());
				}
			});
		} catch (

		StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "failed to setup contact list view" + e);
			JFXUtils.showAlert("Failed to setup contact list view due to " + e.getMessage(), AlertType.WARNING);
		}
	}

	private void openContactChatView(JID contactJID) {
		try {
			this.chatController = new ContactChatController(contactJID);
			this.contactChatViewAnchorPane.getChildren().setAll(chatController.getContactChatViewGraphics());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Failed to open contact chat view for contactJID " + contactJID + " due to " + e.getMessage(), e);
			JFXUtils.showAlert(
					"Failed to open contact chat view for contactJID " + contactJID + " due to " + e.getMessage(),
					AlertType.WARNING);
		}

	}

	public void setConverstionsListView() {
		LOGGER.info("Setting Conversations List View");
		try {
			AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();
			List<Conversation> conversationsList = chatManager.getAllConversations();

			this.conversationsObservableList.setAll(conversationsList);
			this.conversationsListView.setItems(this.conversationsObservableList);

			this.conversationsListView.setCellFactory((u) -> {

				return new ListCell<Conversation>() {
					@Override
					protected void updateItem(Conversation conversation, boolean bool) {
						super.updateItem(conversation, bool);

						if (conversation != null) {
							try {
								ConversationCell conversationCell = new ConversationCell(conversation);
								setGraphic(conversationCell.getConversationCellGraphics());
							} catch (Exception e) {
								LOGGER.log(Level.WARNING, "Failed to load Conversation cell for pear jid "
										+ conversation.getPeerJid() + " due to " + e.getMessage(), e);

								JFXUtils.showAlert("Failed to load Contact cell for pear jid  "
										+ conversation.getPeerJid() + " due to " + e.getMessage(), AlertType.WARNING);
							}
						}

					}

				};

			});

			this.conversationsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					Conversation conversation = conversationsListView.getSelectionModel().getSelectedItem();
					try {

						openContactChatView(new JID(conversation.getPeerJid()));
						refreshConversations();
					} catch (InvalidJabberId e) {
						LOGGER.log(Level.WARNING,
								"Failed to setup conversation list on mouse click listener for pearJID : "
										+ conversation.getPeerJid() + "due to " + e.getMessage(),
								e);
						JFXUtils.showAlert("Failed to setup conversation list on mouse click listener for pearJID : "
								+ conversation.getPeerJid() + "due to " + e.getMessage(), AlertType.WARNING);

					}
				}
			});

		} catch (

		StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "failed to setup conversation list view" + e);
			JFXUtils.showAlert("Failed to setup conversation list view due to " + e.getMessage(), AlertType.WARNING);
		}
	}

	public void refreshConversations() {
		try {
			AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();
			List<Conversation> conversationsList = chatManager.getAllConversations();

			this.conversationsObservableList.setAll(conversationsList);
		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "failed to refresh conversation list view" + e);
			JFXUtils.showAlert("Failed to refresh conversation list view due to " + e.getMessage(), AlertType.WARNING);
		}

	}

	public void refreshContacts() {
		try {
			AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

			List<RosterItem> rosterItemList = userManager.getRosterItemList();
			this.contactsObservableList.setAll(rosterItemList);
		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "failed to refresh conversation list view" + e);
			JFXUtils.showAlert("Failed to refresh conversation list view due to " + e.getMessage(), AlertType.WARNING);
		}

	}

	@Override
	public void onNewMessageReceived(ChatLine line) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onNewMessageReceived(line);
				}

				if (isConversationTabSelected()) {
					refreshConversations();
				}
			}
		});
	}

	@Override
	public void onNewMessageSend(ChatLine line) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onNewMessageSend(line);
				}

				if (isConversationTabSelected()) {
					refreshConversations();
				}
			}
		});

	}

	@Override
	public void onMessageDeliveredToServer(String messageId, JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onMessageDeliveredToServer(messageId, contactJID);
				}
			}
		});
	}

	@Override
	public void onMessageDeliveredToReceiver(String messageId, JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onMessageDeliveredToReceiver(messageId, contactJID);
				}
			}
		});

	}

	@Override
	public void onMessageAcknowledgedToReceiver(String messageId, JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onMessageAcknowledgedToReceiver(messageId, contactJID);
				}
			}
		});
	}

	@Override
	public void onMessageViewedByReceiver(String messageId, JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onMessageViewedByReceiver(messageId, contactJID);
				}
			}
		});
	}

	@Override
	public void onContactTypingStarted(JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onContactTypingStarted(contactJID);
				}

				conversationsObservableList.stream()
						.filter((v) -> StringUtils.safeEquals(v.getPeerJid(), contactJID.getBareJID())).findFirst()
						.ifPresent((v) -> {
							v.setTyping(false);
						});
			}
		});
	}

	@Override
	public void onContactTypingPaused(JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onContactTypingPaused(contactJID);
				}

				conversationsObservableList.stream()
						.filter((v) -> StringUtils.safeEquals(v.getPeerJid(), contactJID.getBareJID())).findFirst()
						.ifPresent((v) -> {
							v.setTyping(false);
						});
			}
		});

	}

	@Override
	public void onContactInactivityInUserChat(JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onContactInactivityInUserChat(contactJID);
				}

				conversationsObservableList.stream()
						.filter((v) -> StringUtils.safeEquals(v.getPeerJid(), contactJID.getBareJID())).findFirst()
						.ifPresent((v) -> {
							v.setTyping(false);
						});
			}
		});

	}

	@Override
	public void onContactGoneFromUserChat(JID contactJID) {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (chatController != null) {
					chatController.onContactGoneFromUserChat(contactJID);
				}

				conversationsObservableList.stream()
						.filter((v) -> StringUtils.safeEquals(v.getPeerJid(), contactJID.getBareJID())).findFirst()
						.ifPresent((v) -> {
							v.setTyping(false);
						});
			}
		});

	}

}