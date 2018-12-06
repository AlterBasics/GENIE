package abs.sf.client.gini.ui.controller;

import java.io.IOException;

import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.sf.client.gini.ui.utils.Resources;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
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

	public ContactCell(RosterItem rosterItem) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource(Resources.CONTACT_CELL_VIEW_FXML));

		fxmlLoader.setController(this);
		fxmlLoader.load();

		this.rosterItem = rosterItem;

		initView();
	}

	private void initView() {
		this.contactNameLabel.setText(rosterItem.getName());

		// TODO: show presence later
		this.contactStatusLabel.setText("");

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
