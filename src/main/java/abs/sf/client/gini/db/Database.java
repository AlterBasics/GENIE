package abs.sf.client.gini.db;

import java.util.List;

import abs.ixi.client.core.Initializable;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.messaging.Conversation;

public interface Database extends Initializable {
	/**
	 * It creates DDl for database.
	 */
	void createDatabaseSchema() throws DbException;

	/**
	 * close database
	 */
	void close() throws DbException;

	/**
	 * It will clean up all data and drop all db objects.
	 */
	void dropDatabase() throws DbException;

	/**
	 * It will clean up all application data and but db objects will remain in
	 * db.
	 */
	void cleanUpAllData() throws DbException;

	/**
	 * Fetch all the active conversations. The method join on
	 * {@link RosterTable} to extract peer name (user/group). All the other
	 * required details are directly taken from {@link ConversationTable}.
	 * 
	 * @throws DbException
	 */
	List<Conversation> fetchConversations() throws DbException;

	/**
	 * Check for user online status
	 * 
	 * @param userJID
	 * @return
	 */
	boolean isOnline(String userJID) throws DbException;

	/**
	 * Add a conversation into {@link ConversationTable}. The method does not
	 * check if there is already an entry against a JID. Therefore the caller
	 * must ensure that there is no pre-existing entry in the table; as there
	 * can be only one running conversation with a given JID.
	 *
	 * @param conv {@link Conversation} to be added into table
	 */
	void addConversation(Conversation conv) throws DbException;

	/**
	 * Update conversation
	 *
	 * @param conv
	 */
	void updateConversation(Conversation conv) throws DbException;

	/**
	 * Get UnRead Conversation count from peerJID
	 * 
	 * @param peerJID
	 * @return
	 */
	int getUnreadConversationCount(String peerJID) throws DbException;

	/**
	 * updating unread conversation count for peerJID with given count
	 * 
	 * @param peerJID
	 * @param unreadConversationCount
	 */
	void updateUnreadConversationCount(String peerJID, int unreadConversationCount) throws DbException;
	
	/**
	 * Check if there an active conversation with a given {@link JID}. If there
	 * is an entry found inside {@link ConversationTable}, it is assumed that
	 * there exist an active conversation with the given JID
	 *
	 * @param peerJid
	 * @return
	 */
	boolean conversationExists(String peerJID) throws DbException;

}
