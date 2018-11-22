package abs.sf.client.gini.db;

import java.util.List;

import abs.ixi.client.core.Initializable;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.UserSearchData.Item;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.messaging.ChatLine;
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

	/**
	 * Add {@link ChatLine} in {@link ChatStoreTable}.
	 *
	 * @param line
	 * @return
	 * @throws SQLException
	 */
	void addToChatStore(ChatLine line) throws DbException;

	/**
	 * Update delivery status for a {@link ChatLine} in {@link ChatStoreTable}
	 *
	 * @param messageId
	 * @param messageStatus
	 * @throws SQLException
	 */
	void updateDeliveryStatus(String messageId, ChatLine.MessageStatus messageStatus) throws DbException;

	/**
	 * Check message is delivered or not
	 * 
	 * @param messageId
	 * @return
	 * @throws DbException
	 */
	boolean isMessageAlreadyDelivered(String messageId) throws DbException;

	/**
	 * Fetch All unRead chatLines for a given pear {@link JID}
	 *
	 * @param pearJID
	 * @return
	 */
	List<ChatLine> getAllUnreadChatLines(String pearJID) throws DbException;

	/**
	 * mark Message is Viewed by the user
	 * 
	 * @param messageId
	 * @throws DbException
	 */
	void markMessageViewed(String messageId) throws DbException;

	/**
	 * Fetch Conversation chat line for a given pear {@link JID}}
	 * 
	 * @param messageId
	 * @throws DbException
	 */
	List<ChatLine> fetchConversationChatlines(String pearJID) throws DbException;

	/**
	 * Check message with given messageId for that pearJID already exist or not.
	 * 
	 * @param messageId
	 * @return throw DbException
	 */
	boolean isMessageAlreadyExist(String pearJID, String messageId) throws DbException;

	/**
	 *
	 * Fetch all the chatlines which have not been delivered to server as yet. A
	 * {@link ChatLine} with delivery status 0 indicates undelivered chatline.
	 *
	 * @return
	 */
	List<ChatLine> getUndeliveredMessages() throws DbException;

	/**
	 * Add Roster Item
	 * 
	 * @param item
	 */
	void addRosterItem(RosterItem item) throws DbException;

	/**
	 * Update Roster Item
	 * 
	 * @param item
	 * @throws DbException
	 */

	void updateRosterItem(RosterItem item) throws DbException;

	/**
	 * Get Roster Item List
	 * 
	 * @return
	 * @throws DbException
	 */
	List<RosterItem> getRosterList() throws DbException;

	/**
	 * Delete Roster Item {@link Item}
	 */
	void deleteRosterItem(RosterItem item) throws DbException;

	/**
	 * Deleting Roster Item
	 * 
	 * @param jid
	 */
	void deleteRosterItem(String jid) throws DbException;

	/**
	 * Getting Roster Item Name
	 * 
	 * @param itemJID
	 */
	String getRosterItemName(String itemJID) throws DbException;

	/**
	 * Add or Update Roster Item
	 * 
	 * @param item
	 */
	void addOrUpdateRosterItem(RosterItem item) throws DbException;

	/**
	 * Checking it is Roster group or not
	 * 
	 * @param jid
	 */
	boolean isRosterGroup(String jid) throws DbException;

	/**
	 * Add or Update Chat Room {@link ChatRoom}
	 */
	void addOrUpdateChatRoom(ChatRoom chatRoom) throws DbException;

	/**
	 * Add new Chat Room {@link ChatRoom}
	 */
	void addChatRoom(ChatRoom chatRoom) throws DbException;

	/**
	 * Updating ChatRoom
	 * 
	 * @param chatRoom
	 * @throws DbException
	 */
	void updateChatRoom(ChatRoom chatRoom) throws DbException;

	/**
	 * Updating chat room subject
	 * 
	 */
	void updateChatRoomSubject(String roomJID, String subject) throws DbException;
}
