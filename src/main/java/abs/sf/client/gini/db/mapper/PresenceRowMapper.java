package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.packet.Presence.PresenceStatus;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.sf.client.gini.messaging.UserPresence;

public class PresenceRowMapper implements RowMapper<UserPresence> {

	@Override
	public UserPresence map(ResultSet rs) throws SQLException {

		boolean online = StringUtils.safeEquals(rs.getString(2), PresenceType.AVAILABLE.val(), false);
		UserPresence presence = new UserPresence(rs.getString(1), online);

		presence.setMood(rs.getString(3));
		presence.setStatus(PresenceStatus.valueFrom(rs.getString(4)));
		presence.setLastUpdateTime(DateUtils.displayTime(rs.getLong(5)));

		return presence;
	}
}
