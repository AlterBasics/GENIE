package abs.sf.client.gini.ui.controller;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.client.chatwindow.CellRenderer;
import com.messages.Message;
import com.messages.User;

import abs.ixi.client.xmpp.packet.Roster;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.managers.AppUserManager;
import abs.sf.client.gini.ui.Launcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChatController implements Initializable{
	
	    @FXML
	    private AnchorPane anchorPane;
	    
	    @FXML
	    private TextField tvUserName , tvSearch, tfStatuss, tfMessage;
	   
	    @FXML
	    private Text tvContactMood, tvContactState,tvcontactCountry, tvStatus ;
	    
	    @FXML
	    private ImageView ivFile, ivq, ivSmiley, ivSend, ivNew, ivAdd , idNumber,
	                             idHome, ivSearch, ivVideo, ivCall, ivContactsUserImage, ivUser  ;
	    
	    @FXML
	    private Slider ivSlider;
	    
	    @FXML
	    private Tab btnContacts, btnRecent;
	    
	    @FXML
	    private MenuBar ivMenuBar;
	    
	    @FXML
	    private List<RosterItem> userList;
	    
	   
	    
	    private double xOffset;

		private double yOffset;
		
		private ObservableList<RosterItem> items = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// TODO Auto-generated method stub
		/* Drag and Drop */
//		anchorPane.setOnMousePressed(event -> {
//			xOffset = Launcher.getPrimaryStage().getX() - event.getScreenX();
//			yOffset = Launcher.getPrimaryStage().getY() - event.getScreenY();
//			anchorPane.setCursor(Cursor.CLOSED_HAND);
//		});
//
//		anchorPane.setOnMouseDragged(event -> {
//			Launcher.getPrimaryStage().setX(event.getScreenX() + xOffset);
//			Launcher.getPrimaryStage().setY(event.getScreenY() + yOffset);
//
//		});
//
//		anchorPane.setOnMouseReleased(event -> {
//			anchorPane.setCursor(Cursor.DEFAULT);
//		});
		
	}
	
	public void start (Stage stage) throws IOException{
		Platform.runLater(() -> {
			FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
			Parent window = null;
			try {
				window = (Pane) fmxlLoader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Stage stage = Launcher.getPrimaryStage();
			Scene scene = new Scene(window);
			stage.setMaxWidth(350);
			stage.setMaxHeight(420);
			stage.setResizable(false);
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.show();
			
		});
				
	}
	
	 public void setUserVisibleHint(boolean isVisibleToUser,int position) throws StringflowErrorException {
		
	        if (isVisibleToUser) {
				AppUserManager userManager = (AppUserManager) abs.ixi.client.core.Platform.getInstance()
						.getUserManager();
				userList =  userManager.getRosterItemList();
				
	            
	        }
	           
	        }
	
	
}
