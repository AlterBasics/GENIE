package abs.sf.client.genie.managers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.Platform;
import abs.ixi.client.PresenceManager;
import abs.ixi.client.core.Packet;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Presence;
import abs.sf.client.genie.db.DbManager;
import abs.sf.client.genie.db.exception.DbException;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.messaging.UserPresence;

public class AppPresenceManager extends PresenceManager {
	private static final Logger LOGGER = Logger.getLogger(AppPresenceManager.class.getName());

	public AppPresenceManager(XMPPStreamManager streamManager) {
		super(streamManager);
	}

	@Override
	public void collect(Packet packet) {
		forwardPacket(packet);

		if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			Presence.PresenceType type = presence.getType();

			if (!presence.isMuc()
					&& (type == Presence.PresenceType.UNAVAILABLE || type == Presence.PresenceType.AVAILABLE)) {

				try {
					DbManager.getInstance().addOrUpdatePresence(presence.getFrom().getBareJID(), type);

				} catch (DbException e) {
					String errorMessage = "Failed to update user presence data for pearJID : "
							+ presence.getFrom().getBareJID() + " due to Database operation failure";
					LOGGER.log(Level.WARNING, errorMessage, e);
				}
			}

			if (presence.isVCardUpdate()) {
				AppUserManager androidUserManager = (AppUserManager) Platform.getInstance().getUserManager();
				androidUserManager.reloadUserData(presence.getFrom());
			}
		}
	}

	public UserPresence getUserPresence(JID userJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().getPresenceDetails(userJID.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to get user presence detail for userJID " + userJID + " due to "
					+ e.getMessage();

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}
}
