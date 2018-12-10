package abs.sf.client.gini.db;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import abs.ixi.client.core.Initializable;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.UserSearchData.Item;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.messaging.MediaContent;
import abs.sf.client.gini.messaging.UserPresence;

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
	 * Clear Roster Data
	 * 
	 * @throws DbException
	 */
	void clearRosterData() throws DbException;

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

	/**
	 * Getting Chat room subject
	 * 
	 * @param roomJID
	 */
	String getChatRoomSubject(String roomJID) throws DbException;

	/**
	 * Getting Chat Room Subject
	 * 
	 * @param roomJID,memberNickName
	 */
	String getChatRoomMemberJID(String roomJID, String memberNickName) throws DbException;

	/**
	 * Check Member is ChatRoom Member or not
	 * 
	 * @param roomJID , memberJID
	 */
	boolean isChatRoomMember(JID roomJID, JID memberJID) throws DbException;

	/**
	 * Add or Update ChatRoomMember
	 * 
	 * @param member
	 */
	void addOrUpdateChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException;

	/**
	 * Add ChatRoomMember
	 * 
	 * @param member
	 */
	void addChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException;

	/**
	 * Update ChatRoomMember
	 * 
	 * @param member
	 */
	void updateChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException;

	/**
	 * Delete all RoomMember
	 * 
	 * @param roomJID
	 */
	void deleteAllRoomMembers(String roomJID) throws DbException;

	/**
	 * Remove RoomMember roomJID, memberJID
	 */
	void removeRoomMember(String roomJID, String memberJID) throws DbException;

	/**
	 * Getting Room Member Nick Name
	 * 
	 * @roomJID,memberJID
	 */
	String getRoomMemberNickName(String roomJId, String memberJID) throws DbException;

	/**
	 * Getting ChatRoom Details roomJID
	 */
	ChatRoom getChatRoomDetails(String roomJID) throws DbException;
	
	/**
	 * Getting ChatRoom JID roomName
	 */
	JID getChatRoomJID(String roomName) throws DbException;

	/**
	 * Getting List of Chat Room
	 * 
	 */
	List<ChatRoom> getChatRooms() throws DbException;

	/**
	 * Getting ChatRoomMembers
	 * 
	 * @param room
	 */
	Set<ChatRoom.ChatRoomMember> getChatRoomMembers(ChatRoom room) throws DbException;

	/**
	 * Checking the presence of the user and mood ,status,
	 * 
	 * @param userJID
	 */
	void addPresence(String userJID, PresenceType presence, String mood, String status) throws DbException;

	/**
	 * Update The Presence, mood ,status of the user userJID
	 */
	void updatePresence(String userJID, PresenceType presence, String mood, String status) throws DbException;

	/**
	 * Adding or Updating the Presence of the user and mood or status userJID
	 */
	void addOrUpdatePresence(String userJID, PresenceType presence, String mood, String status) throws DbException;

	/**
	 * 
	 * @param userJID
	 * @return
	 * @throws DbException
	 */
	UserPresence getPresenceDetails(String userJID) throws DbException;

	/**
	 * @param userJID
	 * @throws DbException
	 */
	void deleteUserPresence(String userJID) throws DbException;

	/**
	 * @param userProfileData
	 * @throws DbException
	 */
	void addOrUpdateUserProfileData(UserProfileData userProfileData) throws DbException;

	/**
	 * 
	 * @param userProfileData
	 * @return
	 * @throws DbException
	 */
	void addUserProfile(UserProfileData userProfileData) throws DbException;

	/**
	 * 
	 * @param userProfileData
	 * @return
	 * @throws DbException
	 */
	void updateUserProfile(UserProfileData userProfileData) throws DbException;

	/**
	 * 
	 * @param userJID
	 * @return
	 * @throws DbException
	 */
	UserProfileData getUserProfileData(String userJID) throws DbException;

	/**
	 * 
	 * @param userJID
	 * @return
	 * @throws DbException
	 */
	InputStream getUserAvatar(String userJID) throws DbException;

	/**
	 * 
	 * @param mediaId
	 * @param mediaThumb
	 * @param mediaPath
	 * @param contentType
	 * @return
	 * @throws DbException
	 */
	long storeMedia(String mediaId, byte[] mediaThumb, String mediaPath, String contentType) throws DbException;

	/**
	 * 
	 * @param mediaId
	 * @param mediaPath
	 * @throws DbException
	 */
	void updateMediaPath(String mediaId, String mediaPath) throws DbException;

	/**
	 * 
	 * @param mediaId
	 * @return
	 * @throws DbException
	 */
	MediaContent getMediaDetaisByMediaId(String mediaId) throws DbException;

	/**
	 * @param uuid
	 * @return
	 * @throws DbException
	 */
	MediaContent getMediaDetaisByMediaUUID(Long uuid) throws DbException;

	/**
	 * 
	 * @param mediaId
	 * @return
	 * @throws DbException
	 */
	String getMediaPathByMediaId(String mediaId) throws DbException;

	/**
	 * 
	 * @param uuid
	 * @return
	 * @throws DbException
	 */
	String getMediaPathByMediaUUID(Long uuid) throws DbException;

	/**
	 * @param mediaId
	 * @throws DbException
	 */
	void deleteMedia(String mediaId) throws DbException;

	/**
	 * 
	 * @param mediaId
	 * @throws DbException
	 */
	void deleteMedia(Long mediaId) throws DbException;

	/**
	 * 
	 * @param stanzaCount
	 * @throws DbException
	 */
	void deleteFirstUndeliveredStanza(int stanzaCount) throws DbException;

	/**
	 * 
	 * @param stanza
	 * @throws DbException
	 */
	void persistUndeliverStanza(Stanza stanza) throws DbException;

	/**
	 * 
	 * @throws DbException
	 */
	void deleteAllUndeliverStanzas() throws DbException;

	/**
	 * 
	 * @return
	 * @throws DbException
	 */
	List<Stanza> fetchAllUndeliverStanzas() throws DbException;

	
}
