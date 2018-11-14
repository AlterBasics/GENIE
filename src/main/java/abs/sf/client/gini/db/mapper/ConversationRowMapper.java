package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.gini.messaging.Conversation;

public class ConversationRowMapper implements RowMapper<Conversation> {


    @Override
    public Conversation map(Cursor cursor) throws SQLException {
        int conversationId = cursor.getInt(0);
        String jid = cursor.getString(1);
        String chatLine = cursor.getString(2);
        String chatLineType = cursor.getString(3);
        long lastUpdateTime = cursor.getLong(4);
        int unreadChatlines = cursor.getInt(5);
        int isGroup = cursor.getShort(8);

        String name;

        if(isGroup == 1) {
            name = cursor.getString(7);

        } else {
            name = cursor.getString(6);
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
