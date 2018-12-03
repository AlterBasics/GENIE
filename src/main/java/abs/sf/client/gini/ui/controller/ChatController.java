package abs.sf.client.gini.ui.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import abs.ixi.client.xmpp.packet.Roster;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.ui.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChatController implements Initializable{
	
	    @FXML
	    private AnchorPane anchorPane;
	   
	    @FXML
	    private ListView userList;
	    
	    @FXML
	    private	List<RosterItem> rosterItems;
	    
	    private List<Roster.RosterItem> contacts;
	    
	    private double xOffset;

		private double yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		/* Drag and Drop */
		anchorPane.setOnMousePressed(event -> {
			xOffset = Launcher.getPrimaryStage().getX() - event.getScreenX();
			yOffset = Launcher.getPrimaryStage().getY() - event.getScreenY();
			anchorPane.setCursor(Cursor.CLOSED_HAND);
		});

		anchorPane.setOnMouseDragged(event -> {
			Launcher.getPrimaryStage().setX(event.getScreenX() + xOffset);
			Launcher.getPrimaryStage().setY(event.getScreenY() + yOffset);

		});

		anchorPane.setOnMouseReleased(event -> {
			anchorPane.setCursor(Cursor.DEFAULT);
		});
		
	}
	
	public void start (Stage stage) throws IOException{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DesktopAppDesign.fxml"));
		String fxmlDocPath = "DesktopAppDesign.fxml";
		FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
		
		AnchorPane root = (AnchorPane) loader.load(fxmlStream);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Ginni");
		stage.show();
				
	}
	
	 public void setUserVisibleHint(boolean isVisibleToUser,int position) throws StringflowErrorException {
		
	        if (isVisibleToUser) {
				AppUserManager userManager = (AppUserManager) abs.ixi.client.core.Platform.getInstance()
						.getUserManager();
	            userList = (ListView) userManager.getRosterItemList();
	            userList.setCellFactory(ComboBoxListCell.forListView(userManager.getRosterItemList()));
	            Roster.RosterItem contactModel = contacts.get(position);
	            
	        }
	        

	            
	            
	        }

}
