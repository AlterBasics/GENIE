package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster;

public class RosterItemRowMapper implements RowMapper<Roster.RosterItem> {
    @Override
    public Roster.RosterItem map(Cursor cursor) throws SQLException {
        String jid = cursor.getString(1);
        String name = cursor.getString(2);

        try {
            return new Roster().new RosterItem(new JID(jid), name);

        } catch (InvalidJabberId invalidJabberId) {
            //Swallow Exception
        }

        return null;
    }
}
