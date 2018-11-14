package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;

public class ChatRoomRowMapper implements RowMapper<ChatRoom> {
    @Override
    public ChatRoom map(Cursor cursor) throws SQLException {
        String jid = cursor.getString(0);
        String name = cursor.getString(1);
        String subject = cursor.getString(2);
        ChatRoom.AccessMode accessMode = cursor.getString(3) == null
                ? ChatRoom.AccessMode.PUBLIC : ChatRoom.AccessMode.valueFrom(cursor.getString(3));
        try {

            ChatRoom room = new ChatRoom(new JID(jid), name, subject, accessMode);
            return room;

        } catch (InvalidJabberId invalidJabberId) {
            //Swallow Exception
        }

        return null;
    }
}
