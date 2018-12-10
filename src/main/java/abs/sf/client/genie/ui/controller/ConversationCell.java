package abs.sf.client.genie.ui.controller;

import java.io.IOException;
import java.io.InputStream;

import abs.ixi.client.core.Platform;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.messaging.Conversation;
import abs.sf.client.genie.ui.utils.ResourceLoader;
import abs.sf.client.genie.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ConversationCell {
	@FXML
	private AnchorPane conversationAnchorPane;

	@FXML
	private ImageView contactImageView;

	@FXML
	private ImageView presenceImageView;

	@FXML
	private Label contactNameLabel;

	@FXML
	private Label lastMessageOrTypingLabel;

	@FXML
	private Label unreadMessageCountLabel;

	@FXML
	private Label lastMessageTimeLabel;

	private Conversation conversation;

	public ConversationCell(Conversation conversation) throws Exception {
		initView();
		this.conversation = conversation;

		this.setCellData();
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.COVERSATION_CELL_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();
	}

	private void setCellData() throws StringflowErrorException, InvalidJabberId {
		this.contactNameLabel.setText(conversation.getPeerName());
		this.lastMessageOrTypingLabel.setText(conversation.getLastChatLine());
		this.unreadMessageCountLabel.setText(Integer.toString(conversation.getUnreadChatLines()));
		this.lastMessageTimeLabel.setText(conversation.getDisplayTime());

		if (!conversation.isOnline()) {
			this.presenceImageView.setVisible(false);
		}

		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();
		InputStream contactImageStream = userManager.getUserAvatar(new JID(this.conversation.getPeerJid()));

		if (contactImageStream != null) {
			this.contactImageView.setImage(new Image(contactImageStream));

		} else if (conversation.isGroup()) {
			this.contactImageView.setImage(ResourceLoader.getInstance().loadGroupDefaultImage());
		}
	}

	public Conversation getConversation() {
		return this.conversation;
	}

	public AnchorPane getConversationCellGraphics() {
		return this.conversationAnchorPane;
	}
}
