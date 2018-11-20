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
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.sf.client.gini.db.Database;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.mapper.ConversationRowMapper;
import abs.sf.client.gini.db.object.ChatArchiveTable;
import abs.sf.client.gini.db.object.ChatRoomMemberTable;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.DatabaseTable;
import abs.sf.client.gini.db.object.MediaStoreTable;
import abs.sf.client.gini.db.object.PollResponseTable;
import abs.sf.client.gini.db.object.PresenceTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.db.object.UndeliverStanzaTable;
import abs.sf.client.gini.db.object.UserProfileTable;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.Conversation;

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
		tables.add(new PollResponseTable());
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
			SQLHelper.closeConnection(conn);
			SQLHelper.closeStatement(ps);
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
			SQLHelper.closeConnection(conn);
			SQLHelper.closeStatement(ps);
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
			return count > 0 ? true : false;

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

}
