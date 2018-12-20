package abs.sf.client.genie.ui.controller;

import java.io.IOException;

import abs.sf.client.genie.messaging.ChatLine;
import abs.sf.client.genie.ui.utils.ResourceLoader;
import abs.sf.client.genie.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class SendChatLineCell implements ChatLineCell {
	@FXML
	private AnchorPane chatLineAnchorPane;

	@FXML
	private Label messageLabel;

	@FXML
	private Label messageTimeLabel;

	@FXML
	private ImageView messageStatusImageView;

	private ChatLine chatLine;

	public SendChatLineCell(ChatLine chatLine) throws IOException {
		initView();
		this.chatLine = chatLine;
		this.setCellData();
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.SEND_CHATLINE_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();

	}

	private void setCellData() {
		this.messageLabel.setText(this.chatLine.getText());
		this.messageTimeLabel.setText(this.chatLine.getDisplayTime());
		setChatLineStatusImage();
	}

	private void setChatLineStatusImage() {
		switch (this.chatLine.getMessageStatus()) {

		case NOT_DELIVERED_TO_SERVER:
			this.messageStatusImageView.setImage(ResourceLoader.getInstance().loadMessageNotDeliveredToServerImage());
			break;

		case DELIVERED_TO_SERVER:
			this.messageStatusImageView.setImage(ResourceLoader.getInstance().loadMessageDeliverToServerImage());
			break;

		case DELIVERED_TO_RECEIVER:
			this.messageStatusImageView.setImage(ResourceLoader.getInstance().loadMessageDeliveredToReceiverImage());
			break;

		case RECEIVER_IS_ACKNOWLEDGED:
			this.messageStatusImageView.setImage(ResourceLoader.getInstance().loadMessageDeliveredToReceiverImage());
			break;

		case RECEIVER_HAS_VIEWED:
			this.messageStatusImageView.setImage(ResourceLoader.getInstance().loadMessageHasViewdByReceiverImage());
			break;

		default:
			this.messageStatusImageView.setVisible(false);
			break;

		}
	}

	@Override
	public Parent getChatLineCellGraphics() {
		return this.chatLineAnchorPane;
	}

	@Override
	public ChatLineCellType getChatLineCellType() {
		return ChatLineCellType.SEND;
	}
}
