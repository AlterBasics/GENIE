package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;

public class ChatRoomRowMapper implements RowMapper<ChatRoom> {
	@Override
	public ChatRoom map(ResultSet rs) throws SQLException {
		String jid = rs.getString(1);
		String name = rs.getString(2);
		String subject = rs.getString(3);
		ChatRoom.AccessMode accessMode = rs.getString(4) == null ? ChatRoom.AccessMode.PRIVATE
				: ChatRoom.AccessMode.valueFrom(rs.getString(4));
		try {

			ChatRoom room = new ChatRoom(new JID(jid), name, subject, accessMode);
			return room;

		} catch (InvalidJabberId invalidJabberId) {
			// Swallow Exception
		}

		return null;
	}
}
