package abs.sf.client.gini.ui.controller;

import java.io.IOException;

import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class ReceiveChatlineCell implements ChatLineCell{
	@FXML
	private AnchorPane chatLineAnchorPane;

	@FXML
	private TextArea messageTextArea;

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
		this.messageTextArea.setText(this.chatLine.getText());
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
