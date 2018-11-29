package abs.sf.client.gini.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.xmpp.packet.Roster;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.utils.SFSDKProperties;

/**
 * An {@link EventHandler} implementation to handle
 * {@link EventType#ROSTER_RECEIVE} events.
 */
public class RosterReceiveHandler implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(RosterReceiveHandler.class.getName());

	@Override
	public void handle(Event event) {
		try {
			Object source = event.getSource();

			if (source instanceof Roster) {
				Roster roster = (Roster) source;

				int newVer = roster.getVersion();
				int oldVer = SFSDKProperties.getInstance().getRosterVersion();

				if (oldVer < newVer) {
					synchronized (RosterTable.class) {
						DbManager.getInstance().clearRosterData();

						if (!CollectionUtils.isNullOrEmpty(roster.getItems())) {
							for (Roster.RosterItem rosterItem : roster.getItems()) {
								DbManager.getInstance().addRosterItem(rosterItem);
							}

						}

						SFSDKProperties.getInstance().setRosterVersion(newVer);
					}
				}
			}

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to handle Roster Received Event due to " + e.getMessage(), e);
		}
	}
}
