package abs.sf.client.genie.ui.controller;

import java.io.IOException;

import abs.sf.client.genie.messaging.ChatLine;
import abs.sf.client.genie.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ReceiveChatlineCell implements ChatLineCell{
	@FXML
	private AnchorPane chatLineAnchorPane;

	@FXML
	private Label messageLabel;

	@FXML
	private Label messageTimeLabel;

	@FXML
	private Label pearNameLabel;

	private ChatLine chatLine;

	public ReceiveChatlineCell(ChatLine chatLine) throws IOException {
		initView();
		this.chatLine = chatLine;
		this.setCellData();
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.RECEIVE_CHATLINE_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();

	}

	private void setCellData() {
		this.messageLabel.setText(this.chatLine.getText());
		this.messageTimeLabel.setText(this.chatLine.getDisplayTime());
		this.pearNameLabel.setText(this.chatLine.getPeerName());
	}

	@Override
	public Parent getChatLineCellGraphics() {
		return this.chatLineAnchorPane;
	}

	@Override
	public ChatLineCellType getChatLineCellType() {
		return ChatLineCellType.RECEIVE;
	}
}
