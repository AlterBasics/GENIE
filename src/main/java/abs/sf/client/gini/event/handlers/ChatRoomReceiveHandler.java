package abs.sf.client.gini.event.handlers;

import java.util.List;

import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.Conversation;

/**
 * {@link EventHandler} implementation to handle {@link EventType#CHAT_ROOM_RECEIVE}
 */
public class ChatRoomReceiveHandler implements EventHandler {
    @Override
    public void handle(Event event) {

        List<ChatRoom> chatRooms = (List<ChatRoom>) event.getSource();

        if (CollectionUtils.isNullOrEmpty(chatRooms)) {
            return;
        }

        for (ChatRoom room : chatRooms) {
            DbManager.getInstance().addOrUpdateChatRoom(room);

            if (!CollectionUtils.isNullOrEmpty(room.getMembers())) {
                for (ChatRoom.ChatRoomMember member : room.getMembers()) {
                    if (member.isExist()) {
                        DbManager.getInstance().addOrUpdateChatRoomMember(member);

                    } else {
                        DbManager.getInstance().removeRoomMember(member);
                    }
                }
            }

            addingGroupToConversations(room);
        }
    }

    private void addingGroupToConversations(ChatRoom room) {
        synchronized (ConversationTable.class) {
            if (!DbManager.getInstance().conversationExists(room.getRoomJID().getBareJID())) {
                ChatLine chatLine = new ChatLine(UUIDGenerator.secureId(), room.getRoomJID().getBareJID(), ChatLine.Direction.RECEIVE);
                chatLine.setPeerResource(room.getRoomJID().getNode());
                chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE); //-1 indicates received messages
                chatLine.setContentType(ChatLine.ContentType.TEXT);
                chatLine.setText("Welcome");
                chatLine.setCreateTime(DateUtils.currentTimeInMiles());
                chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

                DbManager.getInstance().addToChatStore(chatLine);

                Conversation conv = new Conversation(chatLine);
                conv.setUnreadChatLines(1);
                conv.setGroup(true);
                DbManager.getInstance().addConversation(conv);
            }
        }
    }

}
