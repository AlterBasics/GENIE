package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;

public class ChatRoomMemberRowMapper implements RowMapper<ChatRoom.ChatRoomMember> {
    private ChatRoom chatRoom;

    public ChatRoomMemberRowMapper(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    @Override
    public ChatRoom.ChatRoomMember map(Cursor cursor) throws SQLException {
        String jid = cursor.getString(0);
        String name = cursor.getString(1);
        ChatRoom.Affiliation affiliation = ChatRoom.Affiliation.valueFrom(cursor.getString(2));
        ChatRoom.Role role = ChatRoom.Role.valueFrom(cursor.getString(3));

        try {
            ChatRoom.ChatRoomMember member = chatRoom.new ChatRoomMember(new JID(jid), name, affiliation, role, true);
            return member;

        } catch (InvalidJabberId invalidJabberId) {
            //Swallow Exception
        }

        return null;
    }
}
