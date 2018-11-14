package abs.sf.client.gini.event.handlers;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.xmpp.packet.Roster;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.utils.SharedPrefProxy;

/**
 * An {@link EventHandler} implementation to handle
 * {@link EventType#ROSTER_RECEIVE} events.
 */
public class RosterReceiveHandler implements EventHandler {
    @Override
    public void handle(Event event) {
        Object source = event.getSource();

        if (source instanceof Roster) {
            Roster roster = (Roster) source;

            int newVer = roster.getVersion();
            int oldVer = SharedPrefProxy.getInstance().getRosterVersion();

            if (oldVer < newVer) {
                synchronized (RosterTable.class) {
                    DbManager.getInstance().truncateTable(RosterTable.TABLE_NAME);

                    if (!CollectionUtils.isNullOrEmpty(roster.getItems())) {
                        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();

                        for (Roster.RosterItem rosterItem : roster.getItems()) {
                            DbManager.getInstance().addRosterItem(rosterItem);
                        }

                    }

                    SharedPrefProxy.getInstance().setRosterVersion(newVer);
                }
            }
        }
    }
}
