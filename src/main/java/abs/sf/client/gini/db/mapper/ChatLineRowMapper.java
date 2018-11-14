package abs.sf.client.gini.db.mapper;


import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.gini.messaging.ChatLine;

public class ChatLineRowMapper implements RowMapper<ChatLine> {

    @Override
    public ChatLine map(Cursor cursor) throws SQLException {
        ChatLine line = new ChatLine();
        line.setConversationId(cursor.getString(1));
        line.setMessageId(cursor.getString(2));
        line.setPeerBareJid(cursor.getString(3));
        line.setPeerResource(cursor.getString(4));
        line.setDirection(ChatLine.Direction.valueFrom(cursor.getInt(5)));
        line.setText(cursor.getString(6));
        line.setContentType(cursor.getString(7));
        line.setContentId(cursor.getLong(8));
        line.setDisplayTime(DateUtils.displayTime(cursor.getLong(9)));
        line.setMessageStatus(ChatLine.MessageStatus.statusFrom(cursor.getInt(10)));
        line.setMarkable(cursor.getInt(11) == 1 ? true : false);
        line.setHaveSean(cursor.getInt(12) == 1 ? true : false);
        line.setCsnActive(cursor.getInt(13) == 1 ? true : false);

        return line;
    }

}
