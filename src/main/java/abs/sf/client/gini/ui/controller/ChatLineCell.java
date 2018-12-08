package abs.sf.client.gini.ui.controller;

import javafx.scene.Parent;

public interface ChatLineCell {
	public ChatLineCellType getChatLineCellType();
	
	public Parent getChatLineCellGraphics();
	
	public enum ChatLineCellType {
		SEND, RECEIVE;
	}
}
