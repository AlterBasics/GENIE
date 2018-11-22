package abs.sf.client.gini.event.handlers;

import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.xmpp.PresenceSubscription;
import abs.ixi.client.xmpp.packet.Roster;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.utils.SFSDKProperties;

/**
 * {@link EventHandler} implementation to handle
 * {@link EventType#ROSTER_UPDATE} event.
 */
public class RosterUpdateHandler implements EventHandler {
    @Override
    public void handle(Event event) {
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
    }
}
