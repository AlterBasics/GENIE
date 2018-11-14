package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Presence.PresenceStatus;
import abs.sf.client.gini.messaging.UserPresence;

public class PresenceRowMapper implements RowMapper<UserPresence> {

    @Override
    public UserPresence map(Cursor cursor) throws SQLException {
        boolean online = StringUtils.safeEquals(cursor.getString(1), PresenceType.AVAILABLE.val(), false) ? true : false;

        UserPresence presence = new UserPresence(cursor.getString(0), online);

        presence.setMood(cursor.getString(2));
        presence.setStatus(PresenceStatus.valueFrom(cursor.getString(3)));
        presence.setLastUpdateTime(DateUtils.displayTime(cursor.getLong(4)));

        return presence;
    }
}
