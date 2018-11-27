package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;

public class ChatRoomMemberRowMapper implements RowMapper<ChatRoom.ChatRoomMember> {

	@Override
	public ChatRoom.ChatRoomMember map(ResultSet rs) throws SQLException {
		String jid = rs.getString(1);
		String name = rs.getString(2);
		ChatRoom.Affiliation affiliation = ChatRoom.Affiliation.valueFrom(rs.getString(3));
		ChatRoom.Role role = ChatRoom.Role.valueFrom(rs.getString(4));

		try {
			ChatRoom.ChatRoomMember member = chatRoom.new ChatRoomMember(new JID(jid), name, affiliation, role, true);
			return member;

		} catch (InvalidJabberId invalidJabberId) {
			// Swallow Exception
		}

		return null;
	}
}
