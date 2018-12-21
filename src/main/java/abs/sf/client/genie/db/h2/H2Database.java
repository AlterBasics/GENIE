package abs.sf.client.genie.db.h2;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.jdbcx.JdbcConnectionPool;

import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.ObjectUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.ChatRoom.AccessMode;
import abs.ixi.client.xmpp.packet.ChatRoom.ChatRoomMember;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.ixi.client.xmpp.packet.UserProfileData.Address;
import abs.ixi.client.xmpp.packet.UserProfileData.UserAvtar;
import abs.sf.client.genie.db.Database;
import abs.sf.client.genie.db.exception.DbException;
import abs.sf.client.genie.db.mapper.ChatLineRowMapper;
import abs.sf.client.genie.db.mapper.ChatRoomMemberRowMapper;
import abs.sf.client.genie.db.mapper.ChatRoomRowMapper;
import abs.sf.client.genie.db.mapper.ConversationRowMapper;
import abs.sf.client.genie.db.mapper.MediaRowMapper;
import abs.sf.client.genie.db.mapper.PresenceRowMapper;
import abs.sf.client.genie.db.mapper.RosterItemRowMapper;
import abs.sf.client.genie.db.mapper.RowMapper;
import abs.sf.client.genie.db.mapper.UndeliverStanzaRowMapper;
import abs.sf.client.genie.db.mapper.UserProfileRowMapper;
import abs.sf.client.genie.db.object.ChatArchiveTable;
import abs.sf.client.genie.db.object.ChatRoomMemberTable;
import abs.sf.client.genie.db.object.ChatStoreTable;
import abs.sf.client.genie.db.object.ConversationTable;
import abs.sf.client.genie.db.object.DatabaseTable;
import abs.sf.client.genie.db.object.MediaStoreTable;
import abs.sf.client.genie.db.object.PresenceTable;
import abs.sf.client.genie.db.object.RosterTable;
import abs.sf.client.genie.db.object.UndeliverStanzaTable;
import abs.sf.client.genie.db.object.UserProfileTable;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.messaging.ChatLine;
import abs.sf.client.genie.messaging.ChatLine.MessageStatus;
import abs.sf.client.genie.messaging.Conversation;
import abs.sf.client.genie.messaging.MediaContent;
import abs.sf.client.genie.messaging.UserPresence;
import abs.sf.client.genie.utils.SFSDKProperties;

public class H2Database implements Database {
	private static final Logger LOGGER = Logger.getLogger(H2Database.class.getName());

	private static final String COLON = ";";
	private static final String user = "sf sf";
	private static final String password = "sf";
	// FILE_LOCK=SOCKET;
	private static final String DB_PROPERTIES = "TRACE_LEVEL_FILE=3;TRACE_LEVEL_SYSTEM_OUT=3";

	private String url;
	private String dbFilePath;
	private JdbcConnectionPool cp;

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

	public H2Database() throws DbException {
		try {

			this.dbFilePath = SFSDKProperties.getInstance().getH2DbFilePath();

			if (StringUtils.isNullOrEmpty(this.dbFilePath)) {
				throw new DbException("H2 Database file path not found");
			}

		} catch (StringflowErrorException e) {
			throw new DbException("Failed to get db file Path due  to " + e.getMessage(), e);
		}
	}

	@Override
	public void init() throws InitializationErrorException {
		this.url = buildDbURL();
		this.cp = JdbcConnectionPool.create(url, user, password);
	}

	private String buildDbURL() {
		StringBuilder urlBuilder = new StringBuilder("jdbc:h2:").append(this.dbFilePath);

		if (!StringUtils.isNullOrEmpty(DB_PROPERTIES)) {
			urlBuilder.append(COLON).append(DB_PROPERTIES);
		}

		return urlBuilder.toString();
	}

	private Connection getConnection() throws DbException {
		LOGGER.info("Getting database connection...");

		try {

			return cp.getConnection();

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Error while getting database connection : " + e.getMessage(), e);
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

					synchronized (table.getClass()) {
						table.drop(conn);
						table.create(conn);
					}

				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Failed to crete SQL table : " + table.getName(), e);
					throw new DbException("Failed to crete SQL table", e);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void close() {
		this.cp.dispose();
	}

	@Override
	public void dropDatabase() throws DbException {
		LOGGER.info("Droping all db Schema...");

		Connection conn = this.getConnection();

		try {
			for (DatabaseTable table : tables) {
				try {

					LOGGER.info("droping SQL Table : " + table.getName());

					synchronized (table.getClass()) {
						table.drop(conn);
					}

				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Failed to drop SQL table : " + table.getName(), e);
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
					synchronized (table.getClass()) {
						table.truncate(conn);
					}

				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Failed to truncate SQL table : " + table.getName(), e);
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
			synchronized (ConversationTable.class) {
				synchronized (RosterTable.class) {
					return SQLHelper.query(conn, SQLQuery.FETCH_CONVERSATIONS, new ConversationRowMapper());
				}
			}

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
			String presence;

			synchronized (PresenceTable.class) {
				presence = SQLHelper.queryString(conn, SQLQuery.FETCH_PRESENCE_STATUS, new Object[] { userJID });
			}

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

			synchronized (ConversationTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add new Conversation", e);
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

			synchronized (ConversationTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to update Conversation", e);
			throw new DbException("Failed to update a Conversation", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addOrUpdateConversation(ChatLine line) throws DbException {
		synchronized (ConversationTable.class) {
			if (this.conversationExists(line.getPeerBareJid())) {
				Conversation conv = new Conversation(line);

				if (line.getDirection() == ChatLine.Direction.RECEIVE) {
					int unread = this.getUnreadConversationCount(line.getPeerBareJid());
					conv.setUnreadChatLines(unread + 1);
				}

				this.updateConversation(conv);

			} else {
				Conversation conv = new Conversation(line);

				if (line.getDirection() == ChatLine.Direction.RECEIVE) {
					conv.setUnreadChatLines(1);
				}

				this.addConversation(conv);
			}
		}

	}

	@Override
	public int getUnreadConversationCount(String peerJID) throws DbException {
		LOGGER.info("Geting unread conversation count user " + peerJID);
		Connection conn = this.getConnection();

		try {
			synchronized (ConversationTable.class) {
				return SQLHelper.queryInt(conn, SQLQuery.FETCH_UNREAD_CHATLINE_COUNT, new String[] { peerJID });

			}

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

			synchronized (ConversationTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to update unread Conversation count", e);
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
			synchronized (ConversationTable.class) {
				int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CONVERSATION_COUNT, new String[] { peerJID });
				return count > 0;
			}

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

			synchronized (ChatStoreTable.class) {
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add new chatline for userJID", e);
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

			synchronized (ChatStoreTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to update message delivery status Conversation count", e);
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
			synchronized (ChatStoreTable.class) {
				int deliveryStatus = SQLHelper.queryInt(conn, SQLQuery.FETCH_MESSAGE_DELIVERY_STATUS,
						new String[] { messageId });

				return deliveryStatus != ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER.getValue();

			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public List<ChatLine> getAllUnreadChatLines(String pearJID) throws DbException {
		LOGGER.info("Fetching All unread chatlines for pear jid : " + pearJID);

		Connection conn = this.getConnection();

		try {
			synchronized (ChatStoreTable.class) {
				return SQLHelper.query(conn, SQLQuery.FETCH_UNREAD_CONVERSATION_CHAT_LINES, new Object[] { pearJID },
						new ChatLineRowMapper());
			}

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

			synchronized (ChatStoreTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to set the Message is Viewed or not", e);
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
			synchronized (ChatStoreTable.class) {
				return SQLHelper.query(conn, SQLQuery.FETCH_CONVERSATION_CHAT_LINES, new Object[] { pearJID },
						new ChatLineRowMapper());
			}

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
			synchronized (ChatStoreTable.class) {
				int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_LINE_COUNT,
						new String[] { messageId, pearJID });

				return Count > 0;
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public List<ChatLine> getUndeliveredMessages() throws DbException {
		LOGGER.info("Fetching All Undeliverd Message : ");

		Connection conn = this.getConnection();

		try {
			synchronized (ChatStoreTable.class) {
				return SQLHelper.query(conn, SQLQuery.FETCH_UNDELIVERED_CHAT_LINES, null, new ChatLineRowMapper());
			}

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
					new Object[] { item.getJid().getBareJID(), item.getName(), 0, });

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add new Item", e);
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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to update Roster Item", e);
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
			synchronized (RosterTable.class) {
				return SQLHelper.query(conn, SQLQuery.FETCH_ROSTER_ITEMS, null, new RosterItemRowMapper());
			}

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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to delete Roster Item", e);
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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to delete Roster Item", e);
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
			synchronized (RosterTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_USER_NAME, new Object[] { itemJID });

			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addOrUpdateRosterItem(RosterItem item) throws DbException {
		LOGGER.info("Add or Update Roster Item :" + item);
		Connection conn = this.getConnection();

		try {
			synchronized (RosterTable.class) {
				int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_ROSTER_ITEM_COUNT,
						new String[] { item.getJid().getBareJID() });

				if (count == 0) {
					addRosterItem(item);

				} else {
					updateRosterItem(item);
				}
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
			synchronized (RosterTable.class) {
				int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_GROUP_COUNT, new String[] { jid });

				return Count > 0;
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addOrUpdateChatRoom(ChatRoom chatRoom) throws DbException {
		LOGGER.info("Add or Update Roster Item :" + chatRoom);
		Connection conn = this.getConnection();

		try {
			synchronized (RosterTable.class) {
				int count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_COUNT,
						new String[] { chatRoom.getRoomJID().getBareJID() });

				if (count == 0) {
					addChatRoom(chatRoom);

				} else {
					updateChatRoom(chatRoom);
				}
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

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CHAT_ROOM, new Object[] {
					chatRoom.getRoomJID().getBareJID(), chatRoom.getName(), chatRoom.getSubject(),
					chatRoom.getAccessMode() == null ? AccessMode.PRIVATE.val() : chatRoom.getAccessMode().val(), 1 });

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add new chatRoom", e);
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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add new chatRoom", e);
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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to update Chat Room Subject", e);
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

			synchronized (RosterTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to truncte roster table", e);
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
			synchronized (RosterTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_CHAT_ROOM_SUBJECT, new Object[] { roomJID });
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public String getChatRoomMemberJID(String roomJID, String memberNickName) throws DbException {
		LOGGER.info("Getting Chat Room Member Room JID : " + roomJID + memberNickName);

		Connection conn = this.getConnection();

		try {

			synchronized (ChatRoomMemberTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_ROOM_MEMBER_JID,
						new Object[] { roomJID, memberNickName });
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public boolean isChatRoomMember(JID roomJID, JID memberJID) throws DbException {
		LOGGER.info("Checking member is Chat Room Member or not :" + roomJID + memberJID);

		Connection conn = getConnection();

		try {
			synchronized (ChatRoomMemberTable.class) {
				int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
						new Object[] { roomJID, memberJID });
				return Count == 1;
			}
		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addOrUpdateChatRoomMember(ChatRoomMember member) throws DbException {
		LOGGER.info("Add or Update ChatRoomMember : " + member);

		Connection conn = getConnection();

		try {
			synchronized (ChatRoomMemberTable.class) {
				int Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
						new Object[] { member.getUserJID().getBareJID(), member.getRoomJID().getBareJID() });

				if (Count == 0) {

					addChatRoomMember(member);

				} else {

					updateChatRoomMember(member);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void addChatRoomMember(ChatRoomMember member) throws DbException {
		LOGGER.info("Add ChatRoomMember :" + member.getUserJID().getBareJID() + ":" + member.getNickName() + ":"
				+ member.getAffiliation() == null ? null
						: member.getAffiliation().val() + ":" + member.getRole() == null ? null
								: member.getRole().val() + ":" + member.getRoomJID().getBareJID());

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_CHAT_ROOM_MEMBER,
					new Object[] { member.getUserJID().getBareJID(), member.getNickName(),
							member.getAffiliation() == null ? null : member.getAffiliation().val(),
							member.getRole() == null ? null : member.getRole().val(),
							member.getRoomJID().getBareJID() });

			synchronized (ChatRoomMemberTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to add ChaRoomMember", e);
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

			synchronized (ChatRoomMemberTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to Update ChatRoomMember", e);
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

			synchronized (ChatRoomMemberTable.class) {
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to Delete RoomMember", e);
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

			synchronized (ChatRoomMemberTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Failed to Update ChaRoomMember", e);
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
			synchronized (ChatRoomMemberTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_ROOM_MEMBER_NICK_NAME,
						new Object[] { memberJID, roomJId });
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public ChatRoom getChatRoomDetails(String roomJID) throws DbException {
		LOGGER.info("Gettng ChatRoom Details :" + roomJID);

		Connection conn = getConnection();

		try {
			ChatRoom room;
			synchronized (RosterTable.class) {
				room = (ChatRoom) SQLHelper.query(conn, SQLQuery.FETCH_CHAT_ROOM_DETAILS, new Object[] { roomJID },
						new ChatRoomRowMapper());
			}

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
			String roomJID;

			synchronized (RosterTable.class) {
				roomJID = SQLHelper.queryString(conn, SQLQuery.FETCH_CHAT_ROOM_JID, new Object[] { roomName });
			}

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

			List<ChatRoom> rooms = null;

			synchronized (RosterTable.class) {
				rooms = SQLHelper.query(conn, SQLQuery.FETCH_CHAT_ROOMS, new ChatRoomRowMapper());
			}

			if (rooms != null) {
				for (ChatRoom room : rooms) {

					room.setMembers(getChatRoomMembers(room));

				}
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

			synchronized (ChatRoomMemberTable.class) {
				return new HashSet<>(members);
			}

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

			synchronized (PresenceTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Check the Presence of the user and mood and status", e);
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

			synchronized (PresenceTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Update the Presence of the user and mood and status", e);
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
			synchronized (PresenceTable.class) {
				long Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_PRESENCE_COUNT, new Object[] { userJID });

				if (Count == 0) {
					addPresence(userJID, presence, mood, status);

				} else {
					updatePresence(userJID, presence, mood, status);
				}

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
			synchronized (PresenceTable.class) {

				return SQLHelper.queryForObject(conn, SQLQuery.FETCH_PRESENCE_DETAILS, new Object[] { userJID },
						new PresenceRowMapper());
			}

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
			synchronized (PresenceTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Delete User Presence", e);
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
			synchronized (UserProfileTable.class) {
				long Count = SQLHelper.queryInt(conn, SQLQuery.FETCH_USER_PROFILE_COUNT,
						new Object[] { userProfileData.getJabberId().getBareJID() });

				if (Count == 0) {
					addUserProfile(userProfileData);

				} else {
					updateUserProfile(userProfileData);
				}
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void addUserProfile(UserProfileData userProfileData) throws DbException {
		LOGGER.info("Adding Profile of the user :" + userProfileData);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement(SQLQuery.SQL_INSERT_USER_PROFILE);

			ps.setString(1, userProfileData.getJabberId().getBareJID());
			ps.setString(2, userProfileData.getFirstName());
			ps.setString(3, userProfileData.getMiddleName());
			ps.setString(4, userProfileData.getLastName());
			ps.setString(5, userProfileData.getNickName());
			ps.setString(6, userProfileData.getEmail());
			ps.setString(7, userProfileData.getPhone());
			ps.setString(8, userProfileData.getGender());
			ps.setString(9, userProfileData.getBday());

			if (userProfileData.getAddress() != null) {
				Address address = userProfileData.getAddress();

				ps.setString(10, address.getHome());
				ps.setString(11, address.getStreet());
				ps.setString(12, address.getLocality());
				ps.setString(13, address.getCity());
				ps.setString(14, address.getState());
				ps.setString(15, address.getCountry());
				ps.setString(16, address.getPcode());

			} else {
				ps.setString(10, null);
				ps.setString(11, null);
				ps.setString(12, null);
				ps.setString(13, null);
				ps.setString(14, null);
				ps.setString(15, null);
				ps.setString(16, null);

			}

			if (userProfileData.getAvtar() != null) {
				UserAvtar avatar = userProfileData.getAvtar();

				ps.setBytes(17, Base64.getDecoder().decode(avatar.getBase64EncodedImage()));
				ps.setString(18, avatar.getImageType());

			} else {
				ps.setBytes(17, null);
				ps.setString(18, null);
			}

			if (userProfileData.getDescription() != null) {
				ps.setString(19, userProfileData.getDescription());

			} else {
				ps.setString(19, null);
			}

			synchronized (UserProfileTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.warning("Failed to Add Profile of the user ");
			throw new DbException("Failed to Add Profile of the user", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

	}

	@Override
	public void updateUserProfile(UserProfileData userProfileData) throws DbException {
		LOGGER.info("Updating Profile of the user :" + userProfileData);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(SQLQuery.SQL_UPDATE_USER_PROFILE);

			ps.setString(1, userProfileData.getFirstName());
			ps.setString(2, userProfileData.getMiddleName());
			ps.setString(3, userProfileData.getLastName());
			ps.setString(4, userProfileData.getNickName());
			ps.setString(5, userProfileData.getEmail());
			ps.setString(6, userProfileData.getPhone());
			ps.setString(7, userProfileData.getGender());
			ps.setString(8, userProfileData.getBday());

			if (userProfileData.getAddress() != null) {
				Address address = userProfileData.getAddress();

				ps.setString(9, address.getHome());
				ps.setString(10, address.getStreet());
				ps.setString(11, address.getLocality());
				ps.setString(12, address.getCity());
				ps.setString(13, address.getState());
				ps.setString(14, address.getCountry());
				ps.setString(15, address.getPcode());

			} else {
				ps.setString(9, null);
				ps.setString(10, null);
				ps.setString(11, null);
				ps.setString(12, null);
				ps.setString(13, null);
				ps.setString(14, null);
				ps.setString(15, null);
			}

			if (userProfileData.getAvtar() != null) {
				UserAvtar avatar = userProfileData.getAvtar();

				ps.setBytes(16, Base64.getDecoder().decode(avatar.getBase64EncodedImage()));
				ps.setString(17, avatar.getImageType());

			} else {
				ps.setBytes(16, null);
				ps.setString(17, null);
			}

			ps.setString(18, userProfileData.getDescription());

			ps.setString(19, userProfileData.getJabberId().getBareJID());

			synchronized (UserProfileTable.class) {
				ps.executeUpdate();
			}

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
			synchronized (UserProfileTable.class) {
				return SQLHelper.queryForObject(conn, SQLQuery.FETCH_USER_PROFILE_DATA, new Object[] { userJID },
						new UserProfileRowMapper());
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public InputStream getUserAvatar(String userJID) throws DbException {
		LOGGER.info("Getting User Avtar: " + userJID);

		Connection conn = getConnection();

		try {
			synchronized (UserProfileTable.class) {
				return SQLHelper.queryForObject(conn, SQLQuery.FETCH_USER_PROFILE_AVATAR, new Object[] { userJID },
						new RowMapper<InputStream>() {

							@Override
							public InputStream map(ResultSet rs) throws SQLException {
								return rs.getBlob(1) == null ? null : rs.getBlob(1).getBinaryStream();
							}
						});
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public long storeMedia(String mediaId, byte[] mediaThumb, String mediaPath, String contentType) throws DbException {
		LOGGER.info("Storing Media : " + mediaId + mediaThumb + mediaPath + contentType);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_INSERT_STORE_MEDIA,
					new Object[] { mediaId, mediaThumb, mediaPath, contentType });

			synchronized (MediaStoreTable.class) {
				ps.executeUpdate();
			}

			ResultSet keys = ps.getGeneratedKeys();

			if (keys.next())
				return keys.getLong(1);

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Store Media", e);
			throw new DbException("Failed to Store media", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}

		return 0L;
	}

	@Override
	public void updateMediaPath(String mediaId, String mediaPath) throws DbException {
		LOGGER.info("Updating User Media Path : " + mediaId + mediaPath);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {

			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_UPDATE_MEDIA_PATH, new Object[] { mediaId });

			synchronized (MediaStoreTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Update Media Path", e);
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
			synchronized (MediaStoreTable.class) {
				return SQLHelper.queryForObject(conn, SQLQuery.FETCH_MEDIA_DETAILS_BY_MEDIA_ID,
						new Object[] { mediaId }, new MediaRowMapper());
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public MediaContent getMediaDetaisByMediaUUID(Long uuid) throws DbException {
		LOGGER.info("Getting Media Details by uuid: " + uuid);

		Connection conn = getConnection();

		try {
			synchronized (MediaStoreTable.class) {
				return SQLHelper.queryForObject(conn, SQLQuery.FETCH_MEDIA_DETAILS_BY_UUID, new Object[] { uuid },
						new MediaRowMapper());
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public String getMediaPathByMediaId(String mediaId) throws DbException {
		LOGGER.info("Getting Media Path by mediaId: " + mediaId);

		Connection conn = getConnection();

		try {
			synchronized (MediaStoreTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_MEDIA_PATH_BY_MEDIA_ID, new Object[] { mediaId });
			}

		} finally {
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public String getMediaPathByMediaUUID(Long uuid) throws DbException {
		LOGGER.info("Getting Media Path by mediaUUID: " + uuid);

		Connection conn = getConnection();

		try {
			synchronized (MediaStoreTable.class) {
				return SQLHelper.queryString(conn, SQLQuery.FETCH_MEDIA_PATH_BY_UUID, new Object[] { uuid });
			}

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
			synchronized (MediaStoreTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Delete the Media", e);
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
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_DELETE_MEDIA_UUID,
					new Object[] { mediaId.toString() });

			synchronized (MediaStoreTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Delete the Media", e);
			throw new DbException("Failed to Delete the Media", e);

		} finally {
			SQLHelper.closeStatement(ps);
			SQLHelper.closeConnection(conn);
		}
	}

	@Override
	public void deleteFirstUndeliveredStanza(int stanzaCount) throws DbException {
		LOGGER.info("Deleting First Undelivered Stanza :" + stanzaCount);

		Connection conn = getConnection();
		PreparedStatement ps = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.DELETE_FIRST_UNDELIVERED_STANZAS,
					new Object[] { stanzaCount });

			synchronized (UndeliverStanzaTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Delete First Undelivered Stanza", e);
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
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_PERSIST_UNDELIVERD_STANZA,
					new Object[] { ObjectUtils.serializeObject(stanza) });

			synchronized (UndeliverStanzaTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to Persist the Undelivered Stanza", e);
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
			ps = SQLHelper.createPreparedStatement(conn, SQLQuery.SQL_TRUNCATE_UNDELIVERD_STANZA_TABLE,
					new Object[] {});

			synchronized (UndeliverStanzaTable.class) {
				ps.executeUpdate();
			}

		} catch (SQLException e) {

			LOGGER.log(Level.WARNING, "Failed to delete all the Undelivered Stanza", e);
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
			synchronized (UndeliverStanzaTable.class) {
				return SQLHelper.query(conn, SQLQuery.FETCH_ALL_UNDELIVERD_STANZAS, new UndeliverStanzaRowMapper());
			}

		} finally {

			SQLHelper.closeConnection(conn);
		}
	}

}
