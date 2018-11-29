package abs.sf.client.gini.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.xmpp.PresenceSubscription;
import abs.ixi.client.xmpp.packet.Roster;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.utils.SFSDKProperties;

/**
 * {@link EventHandler} implementation to handle {@link EventType#ROSTER_UPDATE}
 * event.
 */
public class RosterUpdateHandler implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(RosterUpdateHandler.class.getName());

	@Override
	public void handle(Event event) {
		try {
			Object source = event.getSource();

			if (source instanceof Roster) {
				Roster roster = (Roster) source;

				int newVer = roster.getVersion();
				int oldVer = SFSDKProperties.getInstance().getRosterVersion();

				if (oldVer < newVer && roster.getItems() != null) {
					synchronized (RosterTable.class) {

						for (Roster.RosterItem item : roster.getItems()) {
							if (item.getSubscription() == PresenceSubscription.REMOVE) {
								DbManager.getInstance().deleteRosterItem(item);

							} else {
								DbManager.getInstance().addOrUpdateRosterItem(item);
							}
						}

						SFSDKProperties.getInstance().setRosterVersion(newVer);

					}
				}
			}

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to handle Roster update Event due to " + e.getMessage(), e);
		}
	}
}
