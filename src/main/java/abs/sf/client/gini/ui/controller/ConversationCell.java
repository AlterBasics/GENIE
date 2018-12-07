package abs.sf.client.gini.ui.controller;

import java.io.IOException;

import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.ui.utils.ResourceLoader;
import abs.sf.client.gini.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
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

	public ConversationCell(Conversation conversation) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.COVERSATION_CELL_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();

		this.conversation = conversation;

		initView();
	}

	private void initView() {
		this.contactNameLabel.setText(conversation.getPeerName());
		this.lastMessageOrTypingLabel.setText(conversation.getLastChatLine());
		this.unreadMessageCountLabel.setText(Integer.toString(conversation.getUnreadChatLines()));
		this.lastMessageTimeLabel.setText(conversation.getDisplayTime());

		if (!conversation.isOnline()) {
			this.presenceImageView.setDisable(true);
		}

		if (conversation.isGroup()) {
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
