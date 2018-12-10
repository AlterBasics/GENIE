package abs.sf.client.genie.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.sf.client.genie.messaging.ChatLine;

public class ChatLineRowMapper implements RowMapper<ChatLine> {

	@Override
	public ChatLine map(ResultSet rs) throws SQLException {
		ChatLine line = new ChatLine();
		line.setConversationId(rs.getString(2));
		line.setMessageId(rs.getString(3));
		line.setPeerBareJid(rs.getString(4));
		line.setPeerResource(rs.getString(5));
		line.setDirection(ChatLine.Direction.valueFrom(rs.getInt(6)));
		line.setText(rs.getString(7));
		line.setContentType(rs.getString(8));
		line.setContentId(rs.getLong(9));
		line.setDisplayTime(DateUtils.displayTime(rs.getLong(10)));
		line.setMessageStatus(ChatLine.MessageStatus.statusFrom(rs.getInt(11)));
		line.setMarkable(rs.getInt(12) == 1 ? true : false);
		line.setHaveSean(rs.getInt(13) == 1 ? true : false);
		line.setCsnActive(rs.getInt(14) == 1 ? true : false);
		
		return line;
	}

}
