package abs.sf.client.gini.db.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.org.apache.regexp.internal.recompile;

import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.ObjectUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.ixi.client.xmpp.packet.ChatRoom.ChatRoomMember;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.sf.client.gini.db.Database;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.mapper.ChatLineRowMapper;
import abs.sf.client.gini.db.mapper.ChatRoomMemberRowMapper;
import abs.sf.client.gini.db.mapper.ChatRoomRowMapper;
import abs.sf.client.gini.db.mapper.ConversationRowMapper;
import abs.sf.client.gini.db.mapper.MediaRowMapper;
import abs.sf.client.gini.db.mapper.PresenceRowMapper;
import abs.sf.client.gini.db.mapper.RosterItemRowMapper;
import abs.sf.client.gini.db.mapper.UndeliverStanzaRowMapper;
import abs.sf.client.gini.db.mapper.UserProfileRowMapper;
import abs.sf.client.gini.db.object.ChatArchiveTable;
import abs.sf.client.gini.db.object.ChatRoomMemberTable;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.DatabaseTable;
import abs.sf.client.gini.db.object.MediaStoreTable;
import abs.sf.client.gini.db.object.PresenceTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.db.object.UndeliverStanzaTable;
import abs.sf.client.gini.db.object.UserProfileTable;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.ChatLine.MessageStatus;
import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.messaging.MediaContent;
import abs.sf.client.gini.messaging.UserPresence;

public class H2Database implements Database {
	private static final Logger LOGGER = Logger.getLogger(H2Database.class.getName());

	private String url;
	private String user;
	private String password;

	private static List<DatabaseTable> tables;

	static {

		tables = new ArrayList<>();

		tables.add(new ChatArchiveTable());
		tables.add(new ChatRoomMemberTable());
		tables.add(new ChatStoreTable());
		tables.add(new ConversationTable());
		tables.add(new MediaStoreTable());
		tables.add(new PresenceTable());
		tables.add(new RosterTable());
		tables.add(new UserProfileTable());
		tables.add(new UndeliverStanzaTable());
	}

	public H2Database() {

	}

	@Override
	public void init() throws InitializationErrorException {
		// TODO Auto-generated method stub

	}

	private Connection getConnection() throws DbException {
		LOGGER.info("Getting database connection...");

		try {

			return DriverManager.getConnection(url, user, password);

		} catch (SQLException e) {
			LOGGER.warning("Error while getting database connection : " + e.getMessage());

			throw new DbException("Failed to get database connection", e);
		}

	}

	@Override
	public void createDatabaseSchema() throws DbException {
		LOGGER.info("Creating new Db Schema...");

		Connection conn = this.getConnection();

		try {
			for (DatabaseTable table : tables) {
				try {

					LOGGER.info("Creating SQL Table : " + table.getName());
					table.drop(conn);
					table.create(conn);

				} catch (Exception e) {
					LOGGER.warning("Failed to crete SQL table : " + table.getName());
					throw new DbException("Failed to crete SQL table", e);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropDatabase() throws DbException {
		LOGGER.info("Droping all db Schema...");

		Connection conn = this.getConnection();

		try {
			for (DatabaseTable table : tables) {
				try {

					LOGGER.info("droping SQL Table : " + table.getName());
					table.drop(conn);

				} catch (Exception e) {
					LOGGER.warning("Failed to drop SQL table : " + table.getName());
					throw new DbException("Failed to drop SQL table", e);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void cleanUpAllData() throws DbException {
		LOGGER.info("Cleaning all db data...");

		Connection conn = this.getConnection();

		try {
			for (DatabaseTable table : tables) {
				try {

					LOGGER.info("Truncating SQL Table : " + table.getName());
					table.truncate(conn);

				} catch (Exception e) {
					LOGGER.warning("Failed to truncate SQL table : " + table.getName());
					throw new DbException("Failed to truncate SQL table", e);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public List<Conversation> fetchConversations() throws DbException {
		LOGGER.info("Fetching All Conversations");

		Connection conn = this.getConnection();

		try {

			List<Conversation> conversations = SQLHelper.query(conn, SQLQuery.FETCH_CONVERSATIONS,
					new ConversationRowMapper());

			return conversations;

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	/**
	 * Check for user online status
	 * 
	 * @param userJID
	 * @return
	 * @throws DbException
	 */
	public boolean isOnline(String userJID) throws DbException {
		LOGGER.info("Checking online status for user " + userJID);
		Connection conn = this.getConnection();
		try {

			String presence = SQLHelper.queryString(conn, SQLQuery.FETCH_PRESENCE_STATUS, new Object[] { userJID });

			return StringUtils.safeEquals(presence, PresenceType.AVAILABLE.val(), false);

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addConversation(Conversation conv) throws DbException {
		LOGGER.info("Adding new Conversation for jid" + conv.getPeerJid());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CONVERSATION,
					new Object[] { conv.getPeerJid(), conv.getLastChatLine(), conv.getLastChatLineType().name(),
							DateUtils.currentTimeInMiles(), conv.getUnreadChatLines() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add new Conversation");
			throw new DbException("Failed to add new Conversation", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateConversation(Conversation conv) throws DbException {
		LOGGER.info("Updating a Conversation for jid : " + conv.getPeerJid());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_CONVERSATION,
					new Object[] { conv.getLastChatLine(), conv.getLastChatLineType().name(),
							DateUtils.currentTimeInMiles(), conv.getUnreadChatLines(), conv.getPeerJid() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update Conversation");
			throw new DbException("Failed to update a Conversation", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public int getUnreadConversationCount(String peerJID) throws DbException {
		LOGGER.info("Geting unread conversation count user " + peerJID);
		Connection conn = this.getConnection();

		try {

			return SQLHelper.queryInt(conn, SQLQuery.FETCH_UNREAD_CHATLINE_COUNT, new String[] { peerJID });

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void updateUnreadConversationCount(String peerJID, int unreadConversationCount) throws DbException {
		LOGGER.info("Updating unread Conversation count for jid : " + peerJID);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_UNREAD_CONVERSATION_COUNT,
					new Object[] { unreadConversationCount, peerJID });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update unread Conversation count");
			throw new DbException("Failed to update unread Conversation count", e);

		} finally {
			SQLHelper.closeConnection(conn);
			SQLHelper.closeStatement(ps);
		}

	}

	@Override
	public boolean conversationExists(String peerJID) throws DbException {
		LOGGER.info("Checking conversation exists or not for user " + peerJID);
		Connection conn = this.getConnection();

		try {

			int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CONVERSATION_COUNT, new String[] { peerJID });
			return count > 0;

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addToChatStore(ChatLine line) throws DbException {
		LOGGER.info("Adding new chatline for jid for user" + line.getPeerBareJid());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CHATLINE_TO_CHATSTORE,
					new Object[] { line.getConversationId(), line.getMessageId(), line.getPeerBareJid(),
							line.getPeerResource(), line.getDirection().val(), line.getText(),
							line.getContentType().name(), line.getContentId(), line.getCreateTime(),
							line.getMessageStatus().getValue(), line.isMarkable(), line.haveSean(),
							line.isCsnActive() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add new chatline for userJID");
			throw new DbException("Failed to add new chaline", e);

		} finally {
			SQLHelper.closeConnection(conn);
			SQLHelper.closeStatement(ps);
		}

	}

	@Override
	public void updateDeliveryStatus(String messageId, MessageStatus messageStatus) throws DbException {
		LOGGER.info("Updating delivery status for messageId :  " + messageId + " status : " + messageStatus);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_MESSAGE_DELIVERY_STATUS,
					new Object[] { messageStatus.getValue(), messageId });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update message delivery status Conversation count");
			throw new DbException("Failed to update message delivery status Conversation count", e);

		} finally {
			SQLHelper.closeConnection(conn);
			SQLHelper.closeStatement(ps);
		}

	}

	@Override
	public boolean isMessageAlreadyDelivered(String messageId) throws DbException {
		LOGGER.info("Checking message is delivered or not for mesageId :" + messageId);
		Connection conn = this.getConnection();

		try {

			int deliveryStatus = SQLHelper.queryInt(conn, SQLQuery.FETCH_MESSAGE_DELIVERY_STATUS,
					new String[] { messageId });

			return deliveryStatus != ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER.getValue();

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public List<ChatLine> getAllUnreadChatLines(String pearJID) throws DbException {
		LOGGER.info("Fetching All unread chatlines for pear jid : " + pearJID);

		Connection conn = this.getConnection();

		try {

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_UNREAD_CONVERSATION_CHAT_LINES,
					new Object[] { pearJID }, new ChatLineRowMapper());

			return chatlines;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void markMessageViewed(String messageId) throws DbException {
		LOGGER.info("Set Message is Viewed by the user or not :" + messageId);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_MARK_MESSAGE_VIEWED, new Object[] { messageId });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to set the Message is Viewed or not");
			throw new DbException("Failed to set the Message is Viewed or not ", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public List<ChatLine> fetchConversationChatlines(String pearJID) throws DbException {
		LOGGER.info(" Fetch all the Conversation ChatLines :" + pearJID);

		Connection conn = this.getConnection();

		try {

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_CONVERSATION_CHAT_LINES,
					new Object[] { pearJID }, new ChatLineRowMapper());

			return chatlines;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public boolean isMessageAlreadyExist(String pearJID, String messageId) throws DbException {
		LOGGER.info("Checking message with messageID : " + messageId + " for pearJID : " + pearJID
				+ " already exist or not");

		Connection conn = this.getConnection();

		try {

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_LINE_COUNT, new String[] { messageId, pearJID });

			return Count > 0;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public List<ChatLine> getUndeliveredMessages() throws DbException {
		LOGGER.info("Fetching All Undeliverd Message : ");

		Connection conn = this.getConnection();

		try {

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_UNDELIVERED_CHAT_LINES, null,
					new ChatLineRowMapper());

			return chatlines;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addRosterItem(RosterItem item) throws DbException {
		LOGGER.info("Adding new Item for jid" + item.getJid());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_ROSTER_ITEM,
					new Object[] { item.getJid(), item.getName(), 0, });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add new Item");
			throw new DbException("Failed to add new Item", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateRosterItem(RosterItem item) throws DbException {
		LOGGER.info("Updating Roster Item for jid :" + item.getJid().getBareJID());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_ROSTER_ITEM,
					new Object[] { item.getName(), item.getJid().getBareJID() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update Roster Item");
			throw new DbException("Failed to update a Roster Item", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public List<RosterItem> getRosterList() throws DbException {
		LOGGER.info("Fetching All Roster List : ");

		Connection conn = this.getConnection();

		try {

			List<RosterItem> rosterItems = SQLHelper.query(conn, SQLQuery.FETCH_ROSTER_ITEMS, null,
					new RosterItemRowMapper());

			return rosterItems;

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void deleteRosterItem(RosterItem item) throws DbException {
		LOGGER.info("Deleting Roster Item  :" + item.getJid().getBareJID());
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_ROSTER_ITEM,
					new Object[] { item.getJid().getBareJID() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to delete Roster Item");
			throw new DbException("Failed to delete a Roster Item", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void deleteRosterItem(String jid) throws DbException {
		LOGGER.info("Deleting Roster Item for jid :" + jid);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_ROSTER_ITEM, new Object[] { jid });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to delete Roster Item");
			throw new DbException("Failed to delete a Roster Item", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public String getRosterItemName(String itemJID) throws DbException {
		LOGGER.info("Fetching  Roster Item Name : " + itemJID);

		Connection conn = this.getConnection();

		try {

			return SQLHelper.queryString(conn, SQLQuery.FETCH_USER_NAME, new Object[] { itemJID });

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addOrUpdateRosterItem(RosterItem item) throws DbException {
		LOGGER.info("Add or Update Roster Item :" + item);
		Connection conn = this.getConnection();

		try {

			int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_ROSTER_ITEM_COUNT,
					new String[] { item.getJid().getBareJID() });

			if (count == 0) {
				addRosterItem(item);

			} else {
				updateRosterItem(item);
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public boolean isRosterGroup(String jid) throws DbException {
		LOGGER.info("Checking it is Roster group or not :" + jid);
		Connection conn = this.getConnection();

		try {

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_GROUP_COUNT, new String[] { jid });

			return Count > 0;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addOrUpdateChatRoom(ChatRoom chatRoom) throws DbException {
		LOGGER.info("Add or Update Roster Item :" + chatRoom);
		Connection conn = this.getConnection();

		try {

			int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_COUNT,
					new String[] { chatRoom.getRoomJID().getBareJID() });

			if (count == 0) {
				addChatRoom(chatRoom);

			} else {
				updateChatRoom(chatRoom);
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addChatRoom(ChatRoom chatRoom) throws DbException {
		LOGGER.info("Adding new Chat Room " + chatRoom);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CHAT_ROOM,
					new Object[] { chatRoom.getRoomJID().getBareJID(), chatRoom.getName(), chatRoom.getSubject(),
							chatRoom.getAccessMode().val(), 1 });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add new chatRoom");
			throw new DbException("Failed to add new chatRoom", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateChatRoom(ChatRoom chatRoom) throws DbException {
		LOGGER.info("Adding new Chat Room " + chatRoom);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_CHAT_ROOM,
					new Object[] { chatRoom.getName(), chatRoom.getSubject(),
							chatRoom.getAccessMode() != null ? chatRoom.getAccessMode().val() : null,
							chatRoom.getRoomJID().getBareJID() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add new chatRoom");
			throw new DbException("Failed to add new chatRoom", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateChatRoomSubject(String roomJID, String subject) throws DbException {
		LOGGER.info("Updating Chat Room Subject for roomJID :" + roomJID);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_CHAT_ROOM_SUBJECT,
					new Object[] { subject, roomJID });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update Chat Room Subject");
			throw new DbException("Failed to update Chat Room Subject", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void clearRosterData() throws DbException {
		LOGGER.info("Truncating Roster Data");
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_TRUNCATE_ROSTER, new Object[] {});

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to truncte roster table");
			throw new DbException("Failed to truncate roster table", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public String getChatRoomSubject(String roomJID) throws DbException {
		LOGGER.info("Getting Chat Room Subject  : " + roomJID);

		Connection conn = this.getConnection();

		try {

			return SQLHelper.queryString(conn, SQLQuery.FETCH_CHAT_ROOM_SUBJECT, new Object[] { roomJID });

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public String getChatRoomMemberJID(String roomJID, String memberNickName) throws DbException {
		LOGGER.info("Getting Chat Room Member Room JID : " + roomJID + memberNickName);

		Connection conn = this.getConnection();

		try {

			return SQLHelper.queryString(conn, SQLQuery.FETCH_ROOM_MEMBER_JID,
					new Object[] { roomJID, memberNickName });

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public boolean isChatRoomMember(JID roomJID, JID memberJID) throws DbException {
		LOGGER.info("Checking member is Chat Room Member or not :" + roomJID + memberJID);

		Connection conn = getConnection();

		try {

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
					new Object[] { roomJID, memberJID });

			return Count == 1;

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addOrUpdateChatRoomMember(ChatRoomMember member) throws DbException {
		LOGGER.info("Add or Update ChatRoomMember : " + member);

		Connection conn = getConnection();

		try {

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
					new Object[] { member.getUserJID().getBareJID(), member.getRoomJID().getBareJID() });

			if (Count == 0) {

				addChatRoomMember(member);

			} else {

				updateChatRoomMember(member);
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addChatRoomMember(ChatRoomMember member) throws DbException {
		LOGGER.info("Add ChatRoomMember :" + member);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CHAT_ROOM_MEMBER,
					new Object[] { member.getUserJID().getBareJID(), member.getNickName(),
							member.getAffiliation() == null ? null : member.getAffiliation().val(),
							member.getRole() == null ? null : member.getRole().val(), member.getRoomJID() });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to add ChaRoomMember");
			throw new DbException("Failed to add ChaRoomMember", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateChatRoomMember(ChatRoomMember member) throws DbException {
		LOGGER.info("Update ChatRoomMember: " + member);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_CHAT_ROOM_MEMBER, new Object[] {
					member.getNickName(), member.getAffiliation() == null ? null : member.getAffiliation().val(),
					member.getRole() == null ? null : member.getRole().val(), member.getUserJID().getBareJID() });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Update ChatRoomMember");
			throw new DbException("Failed to Update ChatRoomMember", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void deleteAllRoomMembers(String roomJID) throws DbException {
		LOGGER.info("Delete All RoomMember : " + roomJID);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_ALL_ROOM_MEMBER, new Object[] { roomJID });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Delete RoomMember");
			throw new DbException("Failed to Delete RoomMember", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void removeRoomMember(String roomJID, String memberJID) throws DbException {
		LOGGER.info("Remove Room member :" + roomJID + memberJID);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_ROOM_MEMBER,
					new Object[] { memberJID, roomJID });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Update ChaRoomMember");
			throw new DbException("Failed to Update ChaRoomMember", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public String getRoomMemberNickName(String roomJId, String memberJID) throws DbException {
		LOGGER.info("Getting RoomMember Nick Name : " + roomJId + memberJID);

		Connection conn = getConnection();

		try {
			return SQLHelper.queryString(conn, SQLQuery.FETCH_ROOM_MEMBER_NICK_NAME,
					new Object[] { memberJID, roomJId });

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public ChatRoom getChatRoomDetails(String roomJID) throws DbException {
		LOGGER.info("Gettng ChatRoom Details :" + roomJID);

		Connection conn = getConnection();

		try {
			ChatRoom room = (ChatRoom) SQLHelper.query(conn, SQLQuery.FETCH_CHAT_ROOM_DETAILS, new Object[] { roomJID },
					new ChatRoomRowMapper());

			if (room != null) {
				room.setMembers(getChatRoomMembers(room));
			}

			return room;

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public JID getChatRoomJID(String roomName) throws DbException {
		LOGGER.info("Getting ChatRoom JID : " + roomName);
		Connection conn = getConnection();

		try {

			String roomJID = SQLHelper.queryString(conn, SQLQuery.FETCH_CHAT_ROOM_JID, new Object[] { roomName });

			if (!StringUtils.isNullOrEmpty(roomJID)) {
				try {

					return new JID(roomJID);

				} catch (Exception e) {
					// Swallow Exception
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

		return null;
	}

	@Override
	public List<ChatRoom> getChatRooms() throws DbException {
		LOGGER.info("Getting list of ChatRoom");

		Connection conn = getConnection();

		try {

			List<ChatRoom> rooms = SQLHelper.query(conn, SQLQuery.FETCH_CHAT_ROOMS, new ChatRoomRowMapper());

			for (ChatRoom room : rooms) {

				room.setMembers(getChatRoomMembers(room));

			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
		return null;

	}

	@Override
	public Set<ChatRoom.ChatRoomMember> getChatRoomMembers(ChatRoom room) throws DbException {
		LOGGER.info("Getting ChatRoomMembers for room jid : " + room.getRoomJID());

		Connection conn = getConnection();

		try {

			List<ChatRoom.ChatRoomMember> members = SQLHelper.query(conn, SQLQuery.FETCH_CHAT_ROOM_MEMBERS,
					new Object[] { room.getRoomJID().getBareJID() }, new ChatRoomMemberRowMapper(room));

			return new HashSet<>(members);

		} finally {

			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addPresence(String userJID, PresenceType presence, String mood, String status) throws DbException {
		LOGGER.info("Adding the presence of the user : " + userJID);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_PRESENCE,
					new Object[] { userJID, presence.val(), mood, status, DateUtils.currentTimeInMiles() });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Check the Presence of the user and mood and status");
			throw new DbException("Failed to Check the Presence of the user and mood and status", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updatePresence(String userJID, PresenceType presence, String mood, String status) throws DbException {
		LOGGER.info("Checking the presence of the user : " + userJID);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_PRESENCE,
					new Object[] { presence.val(), mood, status, DateUtils.currentTimeInMiles(), userJID });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Update the Presence of the user and mood and status");
			throw new DbException("Failed to Update the Presence of the user and mood and status", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addOrUpdatePresence(String userJID, PresenceType presence, String mood, String status)
			throws DbException {
		LOGGER.info("Add or Update the Presence of the user ,modd and status :" + userJID + presence + mood + status);

		Connection conn = getConnection();

		try {

			long Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_PRESENCE_COUNT, new Object[] { userJID });

			if (Count == 0) {
				addPresence(userJID, presence, mood, status);

			} else {
				updatePresence(userJID, presence, mood, status);
			}

		} finally {

			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public UserPresence getPresenceDetails(String userJID) throws DbException {
		LOGGER.info("Getting Presence detials of the user : " + userJID);

		Connection conn = getConnection();

		try {
			return (UserPresence) SQLHelper.query(conn, SQLQuery.FETCH_PRESENCE_DETAILS, new Object[] { userJID },
					new PresenceRowMapper());

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void deleteUserPresence(String userJID) throws DbException {
		LOGGER.info("Deleting the Presence of the user :" + userJID);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_USER_PRESENE, new Object[] { userJID });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Delete User Presence");
			throw new DbException("Failed to Delete User Presence", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addOrUpdateUserProfileData(UserProfileData userProfileData) throws DbException {
		LOGGER.info("Getting Presence detials of the user : " + userProfileData.getJabberId().getBareJID());

		Connection conn = getConnection();

		try {
			long Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_USER_PROFILE_COUNT,
					new Object[] { userProfileData.getJabberId().getBareJID() });

			if (Count == 0) {
				addUserProfile(userProfileData);

			} else {
				updateUserProfile(userProfileData);
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}
	
	@Override
	public long addUserProfile(UserProfileData userProfileData) throws DbException {
		LOGGER.info("Adding Profile of the user :" + userProfileData);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_USER_PROFILE, 
					new Object[] { userProfileData.getJabberId().getBareJID(),userProfileData.getFirstName(),
							        userProfileData.getMiddleName(), userProfileData.getLastName(),
							         userProfileData.getNickName(),userProfileData.getEmail(),
							          userProfileData.getPhone(),userProfileData.getGender(),
							           userProfileData.getBday(),userProfileData.getAddress() == null ? null : userProfileData.getAddress(),
							             userProfileData.getAvtar(),userProfileData.getDescription()});

			ps.executeUpdate();
		} catch (SQLException e) {

			LOGGER.warning("Failed to Add Profile of the user ");
			throw new DbException("Failed to Add Profile of the user", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public long updateUserProfile(UserProfileData userProfileData) throws DbException {
		LOGGER.info("Updating Profile of the user :" + userProfileData);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_USER_PROFILE, 
					new Object[] { userProfileData.getJabberId().getBareJID()});

			ps.executeUpdate();
		} catch (SQLException e) {

			LOGGER.warning("Failed to Update Profile of the user ");
			throw new DbException("Failed to Update Profile of the user", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public UserProfileData getUserProfileData(String userJID) throws DbException {
		LOGGER.info("Getting User Profile Data :" + userJID);

		Connection conn = getConnection();

		try {

			UserProfileData userProfileData = SQLHelper.query(conn, SQLQuery.FETCH_USER_PROFILE_DATA, new Obect[] {userJID},
					new UserProfileRowMapper());
     
			return userProfileData;
		
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public byte[] getUserAvatarBytes(String userJID) throws DbException {
		LOGGER.info("Getting User Avtar: " + userJID);

		Connection conn = getConnection();

		try {

			return SQLHelper.query(conn, SQLQuery.FETCH_USER_PROFILE_AVATAR, new Object[] {userJID},
					new  RowMapper<byte[]>() );
     
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public long storeMedia(String mediaId, byte[] mediaThumb, String mediaPath, String contentType) throws DbException {
		LOGGER.info("Storing Media : " + mediaId  + mediaThumb + mediaPath + contentType);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_STORE_MEDIA,
					new Object[] { mediaId, mediaThumb, mediaPath, contentType });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Store the Media");
			throw new DbException("Failed to Store the media", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		return 0;

	}
	
	@Override
	public void updateMediaPath(String mediaId, String mediaPath) throws DbException {
		LOGGER.info("Updating User Media Path : " + mediaId + mediaPath);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_MEDIA_PATH,
					new Object[] { mediaId });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Update Media Path");
			throw new DbException("Failed to Update Media Path", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
		
	}
	
	@Override
	public MediaContent getMediaDetaisByMediaId(String mediaId) throws DbException {
		LOGGER.info("Getting Media Details by MediaId: " + mediaId);

		Connection conn = getConnection();

		try {

			return (MediaContent) SQLHelper.query(conn, SQLQuery.FETCH_MEDIA_DETAILS_BY_MEDIA_ID,
					new MediaRowMapper());
     
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public MediaContent getMediaDetaisByMediaUUID(Long uuid) throws DbException {
		LOGGER.info("Getting Media Details by uuid: " + uuid);

		Connection conn = getConnection();

		try {

			return (MediaContent) SQLHelper.query(conn, SQLQuery.FETCH_MEDIA_DETAILS_BY_UUID,
					new MediaRowMapper());
     
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public String getMediaPathByMediaId(String mediaId) throws DbException {
		LOGGER.info("Getting Media Path by mediaId: " + mediaId);

		Connection conn = getConnection();

		try {

		return SQLHelper.queryString(conn, SQLQuery.FETCH_MEDIA_PATH_BY_MEDIA_ID ,new Object[] { mediaId});
     
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public String getMediaPathByMediaUUID(Long uuid) throws DbException {
		LOGGER.info("Getting Media Path by mediaUUID: " + uuid);

		Connection conn = getConnection();

		try {

		return SQLHelper.queryString(conn, SQLQuery.FETCH_MEDIA_PATH_BY_UUID,new Object[] { uuid});
     
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public void deleteMedia(String mediaId) throws DbException {
		LOGGER.info("Deleting the Media by MediaId:" + mediaId);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_MEDIA, new Object[] { mediaId });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Delete the Media");
			throw new DbException("Failed to Delete the Media", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public void deleteMedia(Long mediaId) throws DbException {
		LOGGER.info("Deleting the Media by UUID :" + mediaId);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_MEDIA_UUID, new Object[] { mediaId.toString() });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Delete the Media");
			throw new DbException("Failed to Delete the Media", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
	}
	
	@Override
	public void deleteFirstUndeliveredStanza(Integer stanzaCount) throws DbException {
		LOGGER.info("Deleting First Undelivered Stanza :" + stanzaCount);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_FIRST_UNDELIVERED_STANZAS, new Object[] { stanzaCount.toString() });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Delete First Undelivered Stanza");
			throw new DbException("Failed to Delete First Undelivered Stanza", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public void persistUndeliverStanza(Stanza stanza) throws DbException {
		LOGGER.info("Persisting Undelivered Stanza :" + stanza);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_PERSIST_UNDELIVERD_STANZA, new Object[] { ObjectUtils.serializeObject(stanza) });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to Persist the Undelivered Stanza");
			throw new DbException("Failed to Persist the Undelivered Stanza", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public void deleteAllUndeliverStanzas() throws DbException {
		LOGGER.info("Deleting all Undelivered Stanza :");

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_UNDELIVERD_STANZA, new Object[] { });

			ps.executeUpdate();

		} catch (SQLException e) {

			LOGGER.warning("Failed to delete all the Undelivered Stanza");
			throw new DbException("Failed to delete all the Undelivered Stanza", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	@Override
	public List<Stanza> fetchAllUndeliverStanzas() throws DbException {
		LOGGER.info("Fetching all undelivers Stanzas : ");

		Connection conn = getConnection();

		try {

			List<Stanza> stanzas = SQLHelper.query(conn, SQLQuery.FETCH_ALL_UNDELIVERD_STANZAS,
					new UndeliverStanzaRowMapper());
			
			if (!CollectionUtils.isNullOrEmpty(stanzas)) {
				this.deleteAllUndeliverStanzas();
			}

			return stanzas;

		} finally {

			SQLHelper.closeConnection(conn);
		}
	}
	

}
