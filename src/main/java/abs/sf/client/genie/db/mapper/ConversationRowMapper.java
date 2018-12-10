package abs.sf.client.genie.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.genie.messaging.Conversation;

public class ConversationRowMapper implements RowMapper<Conversation> {

	@Override
	public Conversation map(ResultSet rs) throws SQLException {
	//	int conversationId = rs.getInt(1);
		String jid = rs.getString(2);
		String chatLine = rs.getString(3);
		String chatLineType = rs.getString(4);
		long lastUpdateTime = rs.getLong(5);
		int unreadChatlines = rs.getInt(6);
		int isGroup = rs.getShort(9);

		String name;

		if (isGroup == 1) {
			name = rs.getString(8);

		} else {
			name = rs.getString(7);
		}

		if (StringUtils.isNullOrEmpty(name)) {
			try {
				name = new JID(jid).getNode();

			} catch (InvalidJabberId invalidJabberId) {
				name = "unknown";
			}
		}

		Conversation conv = new Conversation(jid, name, isGroup == 1);
		conv.setUnreadChatLines(unreadChatlines);
		conv.setLastChatLine(chatLine);
		conv.setLastChatLineType(chatLineType);
		conv.setLastUpdateTime(lastUpdateTime);
		conv.setDisplayTime(DateUtils.displayTime(lastUpdateTime));

		return conv;
	}
}
