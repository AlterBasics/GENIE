package abs.sf.client.genie.db.mapper;

import java.sql.SQLException;

import java.sql.ResultSet;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Roster;

public class RosterItemRowMapper implements RowMapper<Roster.RosterItem> {
	@Override
	public Roster.RosterItem map(ResultSet rs) throws SQLException {
		String jid = rs.getString(2);
		String name = rs.getString(3);

		try {
			return new Roster().new RosterItem(new JID(jid), name);

		} catch (InvalidJabberId invalidJabberId) {
			// Swallow Exception
		}

		return null;
	}
}
