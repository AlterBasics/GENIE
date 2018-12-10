package abs.sf.client.gini.event.handlers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.Event.EventType;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.Conversation;

/**
 * {@link EventHandler} implementation to handle
 * {@link EventType#CHAT_ROOM_RECEIVE}
 */
public class ChatRoomReceiveHandler implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(ChatRoomReceiveHandler.class.getName());

	@Override
	public void handle(Event event) {
		try {
			@SuppressWarnings("unchecked")
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
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to handle ChatRoom Received Event due to " + e.getMessage(), e);
		}
	}

	private void  addingGroupToConversations(ChatRoom room) throws Exception {
		if (!DbManager.getInstance().conversationExists(room.getRoomJID().getBareJID())) {
			ChatLine chatLine = new ChatLine(UUIDGenerator.secureId(), room.getRoomJID().getBareJID(),
					ChatLine.Direction.RECEIVE);
			chatLine.setPeerResource(room.getRoomJID().getNode());
			chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE);
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
