package abs.sf.client.gini.db.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.UserSearchData.Item;
import abs.sf.client.gini.db.Database;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.mapper.ChatLineRowMapper;
import abs.sf.client.gini.db.mapper.ConversationRowMapper;
import abs.sf.client.gini.db.mapper.RosterItemRowMapper;
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
import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.messaging.ChatLine.MessageStatus;

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

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_UNREAD_CONVERSATION_CHAT_LINES, new Object[] {pearJID},
					new ChatLineRowMapper());

			return chatlines;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void setMessageIsViewed(String messageId) throws DbException {
		LOGGER.info("Set Message is Viewed by the user or not :" + messageId);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_IS_MESSAGE_VIEWED,
					new Object[] {1,messageId});

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
	public List<ChatLine> fetchConversationChatlines(String pearJID) throws DbException{
		LOGGER.info(" Fetch all the Conversation ChatLines :" + pearJID);

		Connection conn = this.getConnection();

		try {

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_CONVERSATION_CHAT_LINES, new Object[] {pearJID},
					new ChatLineRowMapper());

			return chatlines;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public boolean isMessageAlreadyExist(String peerJID, String messageId) throws DbException {
		LOGGER.info("Checking message is delivered or not for mesageId :" + messageId);
		Connection conn = this.getConnection();

		try {

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_LINE_COUNT,
					new String[] {peerJID, messageId });
			
			return Count >0;

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public List<ChatLine> getUndeliveredMessages() throws DbException {
		LOGGER.info("Fetching All Undeliverd Message : " );

		Connection conn = this.getConnection();

		try {

			List<ChatLine> chatlines = SQLHelper.query(conn, SQLQuery.FETCH_UNDELIVERED_CHAT_LINES, new Object[] {},
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
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CONVERSATION,
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
					new Object[] { item.getJid().getBareJID(),item.getName(),0 });

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
		LOGGER.info("Fetching All Roster List : " );

		Connection conn = this.getConnection();

		try {

			List<RosterItem> rosterItems = SQLHelper.query(conn, SQLQuery.FETCH_ROSTER_ITEMS ,new Object[] {},
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
					new Object[] { item.getJid().getBareJID()});

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
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_ROSTER_ITEM,
					new Object[] {jid});

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
		LOGGER.info("Fetching  Roster Item Name : " + itemJID );

		Connection conn = this.getConnection();

		try {

			return SQLHelper.queryString(conn, SQLQuery.FETCH_USER_NAME ,new Object[] {itemJID});


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
					new String[] {item.getJid().getBareJID() });
			
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

			int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_GROUP_COUNT,
					new String[] {jid });
			
			return Count >0;

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
					new String[] {chatRoom.getRoomJID().getBareJID() });
			
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
					new Object[] {chatRoom.getRoomJID().getBareJID(),chatRoom.getName(),chatRoom.getSubject(),chatRoom.getAccessMode().PUBLIC,
							chatRoom.getAccessMode().PRIVATE, 1});

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
		LOGGER.info("Updating Chat Room Subject for roomJID :" + roomJID + subject);
		Connection conn = this.getConnection();

		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_CHAT_ROOM_SUBJECT,
					new Object[] {subject,roomJID });

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Failed to update Chat Room Subject");
			throw new DbException("Failed to update Chat Room Subject", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
		
	}
	
	
	

}
