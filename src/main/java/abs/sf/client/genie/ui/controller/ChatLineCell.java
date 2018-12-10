package abs.sf.client.genie.ui.controller;

import javafx.scene.Parent;

public interface ChatLineCell {
	public ChatLineCellType getChatLineCellType();
	
	public Parent getChatLineCellGraphics();
	
	public enum ChatLineCellType {
		SEND, RECEIVE;
	}
}
