package abs.sf.client.genie.ui.controller;

import java.io.IOException;
import java.io.InputStream;

import abs.ixi.client.Platform;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ContactCell {
	@FXML
	private HBox contactHBox;

	@FXML
	private ImageView contactImageView;

	@FXML
	private Label contactNameLabel;

	@FXML
	private Label contactStatusLabel;

	private RosterItem rosterItem;

	public ContactCell(RosterItem rosterItem) throws Exception {
		initView();
		this.rosterItem = rosterItem;

		this.setCellData();
	}

	private void initView() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.CONTACT_CELL_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();
	}

	private void setCellData() throws StringflowErrorException  {
		this.contactNameLabel.setText(rosterItem.getName());
		// TODO: show presence later
		this.contactStatusLabel.setText("");

		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();

		InputStream contactImageStream = userManager.getUserAvatar(this.rosterItem.getJid());

		if (contactImageStream != null) {
			this.contactImageView.setImage(new Image(contactImageStream));
		}
	}

	public HBox getContactCellGraphics() {
		return this.contactHBox;
	}

	public RosterItem getContact() {
		return this.rosterItem;
	}

	public JID getContactJID() {
		return this.rosterItem.getJid();
	}
}
